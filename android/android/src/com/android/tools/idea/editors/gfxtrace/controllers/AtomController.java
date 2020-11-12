/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.tools.idea.editors.gfxtrace.controllers;

import com.android.tools.idea.ddms.EdtExecutor;
import com.android.tools.idea.editors.gfxtrace.GfxTraceEditor;
import com.android.tools.idea.editors.gfxtrace.LoadingCallback;
import com.android.tools.idea.editors.gfxtrace.renderers.RenderUtils;
import com.android.tools.idea.editors.gfxtrace.renderers.TreeRenderer;
import com.android.tools.idea.editors.gfxtrace.service.RenderSettings;
import com.android.tools.idea.editors.gfxtrace.service.ServiceClient;
import com.android.tools.idea.editors.gfxtrace.service.WireframeMode;
import com.android.tools.idea.editors.gfxtrace.service.atom.Atom;
import com.android.tools.idea.editors.gfxtrace.service.atom.AtomGroup;
import com.android.tools.idea.editors.gfxtrace.service.atom.AtomList;
import com.android.tools.idea.editors.gfxtrace.service.atom.Observation;
import com.android.tools.idea.editors.gfxtrace.service.image.FetchedImage;
import com.android.tools.idea.editors.gfxtrace.service.memory.PoolID;
import com.android.tools.idea.editors.gfxtrace.service.path.*;
import com.android.tools.idea.editors.gfxtrace.widgets.LoadingIndicator;
import com.android.tools.idea.editors.gfxtrace.widgets.Repaintables;
import com.android.tools.idea.logcat.RegexFilterComponent;
import com.android.tools.rpclib.binary.BinaryObject;
import com.google.common.base.Objects;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ConcurrencyUtil;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class AtomController extends TreeController {
  public static JComponent createUI(GfxTraceEditor editor) {
    return new AtomController(editor).myPanel;
  }

  @NotNull private static final Logger LOG = Logger.getInstance(GfxTraceEditor.class);
  private final PathStore<AtomsPath> myAtomsPath = new PathStore<AtomsPath>();
  private final PathStore<DevicePath> myRenderDevice = new PathStore<DevicePath>();

  public static class Node {
    public final long index;
    public final Atom atom;

    public Node(long index, Atom atom) {
      this.index = index;
      this.atom = atom;
    }
  }

  public static class Group {
    public static final int THUMBNAIL_SIZE = JBUI.scale(18);
    public static final int PREVIEW_SIZE = JBUI.scale(200);

    private static final RenderSettings THUMBNAIL_SETTINGS =
      new RenderSettings().setMaxWidth(PREVIEW_SIZE).setMaxHeight(PREVIEW_SIZE).setWireframeMode(WireframeMode.noWireframe());

    public final AtomGroup group;
    public final Atom lastLeaf;
    public final long indexOfLastLeaf;

    private ListenableFuture<FetchedImage> thumbnail;
    private DevicePath lastDevicePath;

    public Group(AtomGroup group, Atom lastLeaf, long indexOfLastLeaf) {
      this.group = group;
      this.lastLeaf = lastLeaf;
      this.indexOfLastLeaf = indexOfLastLeaf;
    }

    public ListenableFuture<FetchedImage> getThumbnail(ServiceClient client, @NotNull DevicePath devicePath, @NotNull AtomsPath atomsPath) {
      synchronized (this) {
        if (thumbnail == null || !Objects.equal(lastDevicePath, devicePath)) {
          lastDevicePath = devicePath;
          thumbnail = FetchedImage.load(client, client.getFramebufferColor(
            devicePath, new AtomPath().setAtoms(atomsPath).setIndex(indexOfLastLeaf), THUMBNAIL_SETTINGS));
        }
        return thumbnail;
      }
    }
  }

  public static class Memory {
    public final long index;
    public final Observation observation;
    public final boolean isRead;

    public Memory(long index, Observation observation, boolean isRead) {
      this.index = index;
      this.observation = observation;
      this.isRead = isRead;
    }
  }

  @NotNull private RegexFilterComponent mySearchField = new RegexFilterComponent(AtomController.class.getName(), 10, false);

  private AtomController(@NotNull GfxTraceEditor editor) {
    super(editor, GfxTraceEditor.LOADING_CAPTURE);
    myPanel.add(mySearchField, BorderLayout.NORTH);
    myPanel.setBorder(BorderFactory.createTitledBorder(myScrollPane.getBorder(), "GPU Commands"));
    myScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
    myTree.setLargeModel(true); // Set some performance optimizations for large models.
    myTree.addTreeSelectionListener(new TreeSelectionListener() {
      @Override
      public void valueChanged(TreeSelectionEvent treeSelectionEvent) {
        if (myAtomsPath.getPath() == null) return;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)myTree.getLastSelectedPathComponent();
        if (node == null || node.getUserObject() == null) return;
        Object object = node.getUserObject();
        if (object instanceof Group) {
          myEditor.activatePath(myAtomsPath.getPath().index(((Group)object).group.getRange().getLast()), AtomController.this);
        }
        else if (object instanceof Node) {
          myEditor.activatePath(myAtomsPath.getPath().index(((Node)object).index), AtomController.this);
        }
        else if (object instanceof Memory) {
          Memory memory = (Memory)object;
          myEditor.activatePath(
            myAtomsPath.getPath().index(memory.index).memoryAfter(PoolID.applicationPool(), memory.observation.getRange()),
            AtomController.this);
        }
      }
    });
    mySearchField.getTextEditor().addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent evt) {
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
          findNextNode(mySearchField.getPattern());
        }
      }
    });
    MouseAdapter mouseHandler = new MouseAdapter() {
      private static final int PREVIEW_HOVER_DELAY_MS = 500;
      private final ScheduledExecutorService scheduler = ConcurrencyUtil.newSingleScheduledThreadExecutor("PreviewHover");
      private Group lastHoverGroup;
      private Future<?> lastScheduledFuture = Futures.immediateFuture(null);
      private Balloon lastShownBalloon;

      @Override
      public void mouseEntered(MouseEvent event) {
        updateHoveringGroupFor(event.getX(), event.getY());
      }

      @Override
      public void mouseExited(MouseEvent event) {
        setHoveringGroup(null, 0, 0);
      }

      @Override
      public void mouseMoved(MouseEvent event) {
        updateHoveringGroupFor(event.getX(), event.getY());
      }

      @Override
      public void mouseWheelMoved(MouseWheelEvent event) {
        setHoveringGroup(null, 0, 0);

        // Bubble the event.
        JScrollPane ancestor = (JBScrollPane)SwingUtilities.getAncestorOfClass(JBScrollPane.class, myTree);
        if (ancestor != null) {
          MouseWheelEvent converted = (MouseWheelEvent)SwingUtilities.convertMouseEvent(myTree, event, ancestor);
          for (MouseWheelListener listener : ancestor.getMouseWheelListeners()) {
            listener.mouseWheelMoved(converted);
          }
        }

        // Update the hover position after the scroll.
        Point location = new Point(MouseInfo.getPointerInfo().getLocation());
        SwingUtilities.convertPointFromScreen(location, myTree);
        updateHoveringGroupFor(location.x, location.y);
      }

      private void updateHoveringGroupFor(int mouseX, int mouseY) {
        TreePath path = myTree.getClosestPathForLocation(mouseX, mouseY);
        if (path != null) {
          Object userObject = ((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject();
          if (userObject instanceof AtomController.Group) {
            Group group = (Group)userObject;
            if (shouldShowPreview(group)) {
              Rectangle bounds = myTree.getPathBounds(path);
              int x = mouseX - bounds.x, y = mouseY - bounds.y;
              if (x >= 0 && y >= 0 && x < Group.THUMBNAIL_SIZE && y < Group.THUMBNAIL_SIZE) {
                setHoveringGroup(group, bounds.x + Group.THUMBNAIL_SIZE, bounds.y + Group.THUMBNAIL_SIZE / 2);
                return;
              }
            }
          }
        }
        setHoveringGroup(null, 0, 0);
      }

      private synchronized void setHoveringGroup(final Group group, final int x, final int y) {
        if (group != lastHoverGroup) {
          lastScheduledFuture.cancel(true);
          lastHoverGroup = group;
          if (group != null) {
            lastScheduledFuture = scheduler.schedule(new Runnable() {
              @Override
              public void run() {
                hover(group, x, y);
              }
            }, PREVIEW_HOVER_DELAY_MS, TimeUnit.MILLISECONDS);
          }
        }
        if (group == null && lastShownBalloon != null) {
          lastShownBalloon.hide();
          lastShownBalloon = null;
        }
      }

      private void hover(final Group group, final int x, final int y) {
        final Object lock = this;
        ApplicationManager.getApplication().invokeLater(new Runnable() {
          @Override
          public void run() {
            synchronized (lock) {
              if (group == lastHoverGroup) {
                if (lastShownBalloon != null) {
                  lastShownBalloon.hide();
                }
                DevicePath device = myRenderDevice.getPath();
                AtomsPath atoms = myAtomsPath.getPath();
                if (device != null && atoms != null) {
                  lastShownBalloon = JBPopupFactory.getInstance().createBalloonBuilder(new PreviewPanel(
                      group.getThumbnail(myEditor.getClient(), device, atoms)))
                    .setAnimationCycle(100)
                    .createBalloon();
                  lastShownBalloon.show(new RelativePoint(myTree, new Point(x, y)), Balloon.Position.atRight);
                }
              }
            }
          }
        });
      }

      class PreviewPanel extends JComponent {
        private Image image;

        public PreviewPanel(final ListenableFuture<FetchedImage> imageFuture) {
          if (imageFuture.isDone()) {
            image = Futures.getUnchecked(imageFuture).icon.getImage();
          } else {
            imageFuture.addListener(new Runnable() {
              @Override
              public void run() {
                image = Futures.getUnchecked(imageFuture).icon.getImage();
                Balloon parent = lastShownBalloon;
                if (parent != null) {
                  parent.revalidate();
                }
              }
            }, EdtExecutor.INSTANCE);
          }
        }

        @Override
        public Dimension getPreferredSize() {
          return (image == null) ?
                 new Dimension(Group.PREVIEW_SIZE, Group.PREVIEW_SIZE) : new Dimension(image.getWidth(this), image.getHeight(this));
        }

        @Override
        protected void paintComponent(Graphics g) {
          g.setColor(getBackground());
          g.fillRect(0, 0, getWidth(), getHeight());
          if (image == null) {
            LoadingIndicator.paint(this, g, 0, 0, getWidth(), getHeight());
            LoadingIndicator.scheduleForRedraw(Repaintables.forComponent(this));
          } else {
            RenderUtils.drawImage(this, g, image, 0, 0, getWidth(), getHeight());
          }
        }
      }
    };
    myTree.addMouseListener(mouseHandler);
    myTree.addMouseMotionListener(mouseHandler);
    myTree.addMouseWheelListener(mouseHandler);
  }

  @NotNull
  @Override
  protected TreeCellRenderer getRenderer() {
    return new TreeRenderer() {
      @Override
      public void customizeCellRenderer(
          @NotNull final JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.customizeCellRenderer(tree, value, selected, expanded, leaf, row, hasFocus);
        Object userObject = ((DefaultMutableTreeNode)value).getUserObject();
        DevicePath device = myRenderDevice.getPath();
        AtomsPath atoms = myAtomsPath.getPath();
        if (userObject instanceof Group && device != null && atoms != null) {
          Group group = (Group)userObject;
          if (shouldShowPreview(group)) {
            ListenableFuture<FetchedImage> iconFuture =
              group.getThumbnail(myEditor.getClient(), device, atoms);
            final FetchedImage image;
            if (iconFuture.isDone()) {
              image = Futures.getUnchecked(iconFuture);
            } else {
              image = null;
              iconFuture.addListener(new Runnable() {
                @Override
                public void run() {
                  tree.repaint();
                }
              }, MoreExecutors.sameThreadExecutor());
            }
            setIcon(new Icon() {
              @Override
              public void paintIcon(Component component, Graphics g, int x, int y) {
                if (image == null) {
                  LoadingIndicator.paint(tree, g, x, y, Group.THUMBNAIL_SIZE, Group.THUMBNAIL_SIZE);
                  LoadingIndicator.scheduleForRedraw(Repaintables.forComponent(tree));
                } else {
                  ImageIcon icon = image.icon;
                  RenderUtils.drawImage(tree, g, icon.getImage(), x, y, Group.THUMBNAIL_SIZE, Group.THUMBNAIL_SIZE);
                }
              }

              @Override
              public int getIconWidth() {
                return Group.THUMBNAIL_SIZE;
              }

              @Override
              public int getIconHeight() {
                return Group.THUMBNAIL_SIZE;
              }
            });
          }
        }
      }
    };
  }

  private void findNextNode(Pattern pattern) {
    DefaultMutableTreeNode start = (DefaultMutableTreeNode)myTree.getLastSelectedPathComponent();
    if (start == null) {
      start = (DefaultMutableTreeNode)myTree.getModel().getRoot();
    }
    DefaultMutableTreeNode change = findMatchingChild(start, pattern);
    if (change == null) {
      DefaultMutableTreeNode node = start;
      while (change == null && node != null) {
        change = findMatchingSibling(node, pattern);
        node = (DefaultMutableTreeNode)node.getParent();
      }
    }
    if (change == null && start != myTree.getModel().getRoot()) {
      // TODO: this searches the entire tree again.
      change = findMatchingChild((DefaultMutableTreeNode)myTree.getModel().getRoot(), pattern);
    }
    if (change != null) {
      TreePath path = new TreePath(change.getPath());
      myTree.setSelectionPath(path);
      myTree.scrollPathToVisible(path);
    }
  }

  private DefaultMutableTreeNode findMatchingChild(DefaultMutableTreeNode node, Pattern pattern) {
    for (int i = 0; i < node.getChildCount(); i++) {
      DefaultMutableTreeNode child = (DefaultMutableTreeNode)node.getChildAt(i);
      if (matches(child, pattern)) {
        return child;
      }
      DefaultMutableTreeNode result = findMatchingChild(child, pattern);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  private DefaultMutableTreeNode findMatchingSibling(DefaultMutableTreeNode node, Pattern pattern) {
    DefaultMutableTreeNode sibling = node.getNextSibling();
    while (sibling != null) {
      if (matches(sibling, pattern)) {
        return sibling;
      }
      DefaultMutableTreeNode result = findMatchingChild(sibling, pattern);
      if (result != null) {
        return result;
      }
      sibling = sibling.getNextSibling();
    }
    return null;
  }

  private boolean matches(DefaultMutableTreeNode child, Pattern pattern) {
    Object node = child.getUserObject();
    if (node instanceof Node) {
      return pattern.matcher(((Node)node).atom.getName()).find();
    }
    return false;
  }

  private static boolean shouldShowPreview(Group group) {
    return group.lastLeaf.isEndOfFrame() || group.lastLeaf.isDrawCall();
  }

  private void selectDeepestVisibleNode(AtomPath atomPath) {
    if (atomPath == null) {
      return;
    }

    Object object = myTree.getModel().getRoot();
    assert (object instanceof DefaultMutableTreeNode);
    DefaultMutableTreeNode root = (DefaultMutableTreeNode)object;
    selectDeepestVisibleNode(root, new TreePath(root), atomPath.getIndex());
  }

  private void selectDeepestVisibleNode(DefaultMutableTreeNode node, TreePath path, long atomIndex) {
    if (node.isLeaf() || !myTree.isExpanded(path)) {
      myTree.setSelectionPath(path);
      myTree.scrollPathToVisible(path);
      return;
    }
    // Search through the list for now.
    for (Enumeration it = node.children(); it.hasMoreElements(); ) {
      Object obj = it.nextElement();
      assert (obj instanceof DefaultMutableTreeNode);
      DefaultMutableTreeNode child = (DefaultMutableTreeNode)obj;
      Object object = child.getUserObject();
      boolean matches = false;
      if ((object instanceof Group) && (((Group)object).group.getRange().contains(atomIndex)) ||
          (object instanceof Node) && ((((Node)object).index == atomIndex))) {
        matches = true;
      }
      if (matches) {
        selectDeepestVisibleNode(child, path.pathByAddingChild(child), atomIndex);
      }
    }
  }

  @Override
  public void notifyPath(PathEvent event) {
    boolean updateAtoms = myAtomsPath.updateIfNotNull(CapturePath.atoms(event.findCapturePath()));
    if (myRenderDevice.updateIfNotNull(event.findDevicePath())) {
      // Only the icons would need to be changed.
      myTree.repaint();
    }

    if (event.source != this) {
      selectDeepestVisibleNode(event.findAtomPath());
    }

    if (updateAtoms && myAtomsPath.getPath() != null) {
      myTree.getEmptyText().setText("");
      myLoadingPanel.startLoading();
      final ListenableFuture<AtomList> atomF = myEditor.getClient().get(myAtomsPath.getPath());
      final ListenableFuture<AtomGroup> hierarchyF = myEditor.getClient().get(myAtomsPath.getPath().getCapture().hierarchy());
      Futures.addCallback(Futures.allAsList(atomF, hierarchyF), new LoadingCallback<java.util.List<BinaryObject>>(LOG, myLoadingPanel) {
        @Override
        public void onSuccess(@Nullable final java.util.List<BinaryObject> all) {
          myLoadingPanel.stopLoading();
          DefaultMutableTreeNode root = new DefaultMutableTreeNode("Stream", true);
          ((AtomGroup)all.get(1)).addChildren(root, (AtomList)all.get(0));
          setRoot(root);
        }
      }, EdtExecutor.INSTANCE);
    }
  }
}
