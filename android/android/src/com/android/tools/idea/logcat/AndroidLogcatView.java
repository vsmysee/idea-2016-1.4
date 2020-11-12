/*
 * Copyright 2000-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.tools.idea.logcat;

import com.android.ddmlib.Client;
import com.android.ddmlib.IDevice;
import com.android.tools.idea.ddms.DeviceContext;
import com.intellij.diagnostic.logging.LogConsoleBase;
import com.intellij.diagnostic.logging.LogFormatter;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.SideBorder;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.android.util.AndroidBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import static javax.swing.BoxLayout.X_AXIS;

/**
 * A UI panel which wraps a console that prints output from Android's logging system.
 */
public abstract class AndroidLogcatView implements Disposable {
  public static final Key<AndroidLogcatView> ANDROID_LOGCAT_VIEW_KEY = Key.create("ANDROID_LOGCAT_VIEW_KEY");

  static final String SELECTED_APP_FILTER = AndroidBundle.message("android.logcat.filters.selected");
  static final String NO_FILTERS = AndroidBundle.message("android.logcat.filters.none");
  static final String EDIT_FILTER_CONFIGURATION = AndroidBundle.message("android.logcat.filters.edit");

  private final Project myProject;
  private final DeviceContext myDeviceContext;

  private JPanel myPanel;
  private DefaultComboBoxModel myFilterComboBoxModel;

  private volatile IDevice myDevice;
  private final AndroidLogConsole myLogConsole;
  private final AndroidLogFilterModel myLogFilterModel;

  private final IDevice myPreselectedDevice;

  @NotNull
  private ConfiguredFilter mySelectedAppFilter;

  @NotNull
  private ConfiguredFilter myNoFilter;

  /**
   * Called internally when the device may have changed, or been significantly altered.
   * @param forceReconnect Forces the logcat connection to restart even if the device has not changed.
   */
  private void notifyDeviceUpdated(final boolean forceReconnect) {
    UIUtil.invokeAndWaitIfNeeded(new Runnable() {
      @Override
      public void run() {
        if (myProject.isDisposed()) {
          return;
        }
        if (forceReconnect) {
          myDevice = null;
        }
        updateLogConsole();
      }
    });
  }

  @NotNull
  public final Project getProject() {
    return myProject;
  }

  @NotNull
  public final LogConsoleBase getLogConsole() {
    return myLogConsole;
  }

  public final void clearLogcat(@Nullable IDevice device) {
    if (device == null) {
      return;
    }

    myLogFilterModel.beginRejectingOldMessages();
    AndroidLogcatUtils.clearLogcat(myProject, device);

    // In theory, we only need to clear the console. However, due to issues in the platform, clearing logcat via "logcat -c" could
    // end up blocking the current logcat readers. As a result, we need to issue a restart of the logging to work around the platform bug.
    // See https://code.google.com/p/android/issues/detail?id=81164 and https://android-review.googlesource.com/#/c/119673
    // NOTE: We can avoid this and just clear the console if we ever decide to stop issuing a "logcat -c" to the device or if we are
    // confident that https://android-review.googlesource.com/#/c/119673 doesn't happen anymore.
    if (device.equals(getSelectedDevice())) {
      notifyDeviceUpdated(true);
    }
  }

  /**
   * Logcat view with device obtained from {@link DeviceContext}
   */
  public AndroidLogcatView(@NotNull final Project project, @NotNull DeviceContext deviceContext) {
    this(project, null, deviceContext);
  }

  private AndroidLogcatView(final Project project, @Nullable IDevice preselectedDevice, @Nullable DeviceContext deviceContext) {
    myDeviceContext = deviceContext;
    myProject = project;
    myPreselectedDevice = preselectedDevice;

    Disposer.register(myProject, this);

    myLogFilterModel =
      new AndroidLogFilterModel() {
        @Nullable private ConfiguredFilter myConfiguredFilter;

        @Override
        protected void saveLogLevel(String logLevelName) {
          AndroidLogcatPreferences.getInstance(project).TOOL_WINDOW_LOG_LEVEL = logLevelName;
        }

        @Override
        public String getSelectedLogLevelName() {
          return AndroidLogcatPreferences.getInstance(project).TOOL_WINDOW_LOG_LEVEL;
        }

        @Override
        protected void setConfiguredFilter(@Nullable ConfiguredFilter filter) {
          AndroidLogcatPreferences.getInstance(project).TOOL_WINDOW_CONFIGURED_FILTER = filter != null ? filter.getName() : "";
          myConfiguredFilter = filter;
        }

        @Nullable
        @Override
        protected ConfiguredFilter getConfiguredFilter() {
          return myConfiguredFilter;
        }
      };

    AndroidLogcatFormatter logFormatter = new AndroidLogcatFormatter(AndroidLogcatPreferences.getInstance(project));
    myLogConsole = new AndroidLogConsole(project, myLogFilterModel, logFormatter);

    if (preselectedDevice == null && deviceContext != null) {
      DeviceContext.DeviceSelectionListener deviceSelectionListener =
        new DeviceContext.DeviceSelectionListener() {
          @Override
          public void deviceSelected(@Nullable IDevice device) {
            notifyDeviceUpdated(false);
          }

          @Override
          public void deviceChanged(@NotNull IDevice device, int changeMask) {
            if (device == myDevice && ((changeMask & IDevice.CHANGE_STATE) == IDevice.CHANGE_STATE)) {
              notifyDeviceUpdated(true);
            }
          }

          @Override
          public void clientSelected(@Nullable final Client c) {
            boolean reselect = myFilterComboBoxModel.getSelectedItem() == mySelectedAppFilter;
            AndroidConfiguredLogFilters.FilterEntry f;
            if (c != null) {
              f = AndroidConfiguredLogFilters.getInstance(myProject).createFilterForProcess(c.getClientData().getPid());
            }
            else {
              f = new AndroidConfiguredLogFilters.FilterEntry();
            }
            // Replace mySelectedAppFilter
            int index = myFilterComboBoxModel.getIndexOf(mySelectedAppFilter);
            if (index >= 0) {
              myFilterComboBoxModel.removeElementAt(index);
              mySelectedAppFilter = ConfiguredFilter.compile(f, SELECTED_APP_FILTER);
              myFilterComboBoxModel.insertElementAt(mySelectedAppFilter, index);
            }
            if (reselect) {
              myFilterComboBoxModel.setSelectedItem(mySelectedAppFilter);
            }
          }
        };
      deviceContext.addListener(deviceSelectionListener, this);
    }

    mySelectedAppFilter = ConfiguredFilter.compile(new AndroidConfiguredLogFilters.FilterEntry(), SELECTED_APP_FILTER);
    myNoFilter = ConfiguredFilter.compile(new AndroidConfiguredLogFilters.FilterEntry(), NO_FILTERS);

    JComponent consoleComponent = myLogConsole.getComponent();

    final ConsoleView console = myLogConsole.getConsole();
    if (console != null) {
      final ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN,
                                                                                    myLogConsole.getOrCreateActions(), false);
      toolbar.setTargetComponent(console.getComponent());
      final JComponent tbComp1 = toolbar.getComponent();
      myPanel.add(tbComp1, BorderLayout.WEST);
    }

    myPanel.add(consoleComponent, BorderLayout.CENTER);
    Disposer.register(this, myLogConsole);

    updateLogConsole();
  }

  @NotNull
  public final JPanel createSearchComponent() {
    final JPanel panel = new JPanel();
    final ComboBox editFiltersCombo = new ComboBox();
    myFilterComboBoxModel = new DefaultComboBoxModel();
    editFiltersCombo.setModel(myFilterComboBoxModel);
    String def = AndroidLogcatPreferences.getInstance(myProject).TOOL_WINDOW_CONFIGURED_FILTER;
    if (StringUtil.isEmpty(def)) {
      def = myDeviceContext != null ? SELECTED_APP_FILTER : NO_FILTERS;
    }
    updateFilterCombobox(def);
    applySelectedFilter();
    // note: the listener is added after the initial call to populate the combo
    // boxes in the above call to updateConfiguredFilters
    editFiltersCombo.addItemListener(new ItemListener() {
      @Nullable private ConfiguredFilter myLastSelected;

      @Override
      public void itemStateChanged(ItemEvent e) {
        Object item = e.getItem();
        if (e.getStateChange() == ItemEvent.DESELECTED) {
          if (item instanceof ConfiguredFilter) {
            myLastSelected = (ConfiguredFilter)item;
          }
        }
        else if (e.getStateChange() == ItemEvent.SELECTED) {

          if (item instanceof ConfiguredFilter) {
            applySelectedFilter();
          }
          else {
            assert EDIT_FILTER_CONFIGURATION.equals(item);
            final EditLogFilterDialog dialog =
              new EditLogFilterDialog(AndroidLogcatView.this, myLastSelected == null ? null : myLastSelected.getName());
            dialog.setTitle(AndroidBundle.message("android.logcat.new.filter.dialog.title"));
            if (dialog.showAndGet()) {
              final AndroidConfiguredLogFilters.FilterEntry newEntry = dialog.getCustomLogFiltersEntry();
              updateFilterCombobox(newEntry != null ? newEntry.getName() : null);
            }
            else {
              editFiltersCombo.setSelectedItem(myLastSelected);
            }
          }
        }
      }
    });

    editFiltersCombo.setRenderer(new ColoredListCellRenderer<Object>() {
      @Override
      protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus) {
        if (value instanceof ConfiguredFilter) {
          setBorder(null);
          append(((ConfiguredFilter)value).getName());
        }
        else {
          setBorder(IdeBorderFactory.createBorder(SideBorder.BOTTOM));
          append(value.toString());
        }
      }
    });
    panel.add(editFiltersCombo);

    final JPanel searchComponent = new JPanel();
    searchComponent.setLayout(new BoxLayout(searchComponent, X_AXIS));
    searchComponent.add(myLogConsole.getSearchComponent());
    searchComponent.add(panel);

    return searchComponent;
  }

  protected abstract boolean isActive();

  public final void activate() {
    if (isActive()) {
      updateLogConsole();
    }
    if (myLogConsole != null) {
      myLogConsole.activate();
    }
  }

  private volatile @Nullable AndroidLogcatReceiver myReceiver;

  private void updateLogConsole() {
    IDevice device = getSelectedDevice();
    if (myDevice != device) {
      myDevice = device;
      AndroidLogcatReceiver receiver = myReceiver;
      if (receiver != null) {
        receiver.cancel();
      }
      if (device != null) {
        final ConsoleView console = myLogConsole.getConsole();
        if (console != null) {
          console.clear();
        }
        myReceiver = AndroidLogcatUtils.startLoggingThread(myProject, device, false, myLogConsole);
      }
    }
  }

  @Nullable
  public final IDevice getSelectedDevice() {
    if (myPreselectedDevice != null) {
      return myPreselectedDevice;
    }
    else if (myDeviceContext != null) {
      return myDeviceContext.getSelectedDevice();
    }
    else {
      return null;
    }
  }


  private void applySelectedFilter() {
    final Object filter = myFilterComboBoxModel.getSelectedItem();
    if (filter instanceof ConfiguredFilter) {
      ProgressManager.getInstance().run(new Task.Backgroundable(myProject, LogConsoleBase.APPLYING_FILTER_TITLE) {
        @Override
        public void run(@NotNull ProgressIndicator indicator) {
          myLogFilterModel.updateConfiguredFilter((ConfiguredFilter)filter);
        }
      });
    }
  }

  private void updateFilterCombobox(String select) {
    final AndroidConfiguredLogFilters filters = AndroidConfiguredLogFilters.getInstance(myProject);
    final List<AndroidConfiguredLogFilters.FilterEntry> entries = filters.getFilterEntries();

    myFilterComboBoxModel.removeAllElements();
    if (myDeviceContext != null) {
      myFilterComboBoxModel.addElement(mySelectedAppFilter);
    }
    myFilterComboBoxModel.addElement(myNoFilter);
    myFilterComboBoxModel.addElement(EDIT_FILTER_CONFIGURATION);

    for (AndroidConfiguredLogFilters.FilterEntry entry : entries) {
      final String name = entry.getName();

      ConfiguredFilter filter = ConfiguredFilter.compile(entry, entry.getName());
      myFilterComboBoxModel.addElement(filter);
      if (name.equals(select)) {
        myFilterComboBoxModel.setSelectedItem(filter);
      }
    }
  }

  @NotNull
  public final JPanel getContentPanel() {
    return myPanel;
  }

  @Override
  public final void dispose() {
  }

  private final class MyRestartAction extends AnAction {
    public MyRestartAction() {
      super(AndroidBundle.message("android.restart.logcat.action.text"), AndroidBundle.message("android.restart.logcat.action.description"),
            AllIcons.Actions.Restart);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
      notifyDeviceUpdated(true);
    }
  }

  private final class MyConfigureLogcatHeaderAction extends AnAction {
    public MyConfigureLogcatHeaderAction() {
      super(AndroidBundle.message("android.configure.logcat.header.text"),
            AndroidBundle.message("android.configure.logcat.header.description"), AllIcons.General.GearPlain);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
      ConfigureLogcatFormatDialog dialog = new ConfigureLogcatFormatDialog(myProject);
      if (dialog.showAndGet()) {
        myLogConsole.refresh();
      }
    }
  }

  final class AndroidLogConsole extends LogConsoleBase implements AndroidConsoleWriter {
      private final RegexFilterComponent myRegexFilterComponent = new RegexFilterComponent("LOG_FILTER_HISTORY", 5, true);
      private final AndroidLogcatPreferences myPreferences;

    public AndroidLogConsole(Project project, AndroidLogFilterModel logFilterModel, LogFormatter logFormatter) {
      super(project, null, "", false, logFilterModel, GlobalSearchScope.allScope(project), logFormatter);
      ConsoleView console = getConsole();
      if (console instanceof ConsoleViewImpl) {
        ConsoleViewImpl c = ((ConsoleViewImpl)console);
        c.addCustomConsoleAction(new Separator());
        c.addCustomConsoleAction(new MyRestartAction());
        c.addCustomConsoleAction(new MyConfigureLogcatHeaderAction());
      }
      myPreferences = AndroidLogcatPreferences.getInstance(project);
      myRegexFilterComponent.setFilter(myPreferences.TOOL_WINDOW_CUSTOM_FILTER);
      myRegexFilterComponent.setIsRegex(myPreferences.TOOL_WINDOW_REGEXP_FILTER);
      myRegexFilterComponent.addRegexListener(new RegexFilterComponent.Listener() {
        @Override
        public void filterChanged(RegexFilterComponent filter) {
          myPreferences.TOOL_WINDOW_CUSTOM_FILTER = filter.getFilter();
          myPreferences.TOOL_WINDOW_REGEXP_FILTER = filter.isRegex();
          myLogFilterModel.updateCustomPattern(filter.getPattern());
        }
      });
    }

    @Override
    public boolean isActive() {
      return AndroidLogcatView.this.isActive();
    }

    public void clearLogcat() {
      AndroidLogcatView.this.clearLogcat(getSelectedDevice());
    }

    @NotNull
    @Override
    protected Component getTextFilterComponent() {
      return myRegexFilterComponent;
    }

    @Override
    public synchronized void addMessage(@NotNull String text) {
      super.addMessage(text);
    }

    /**
     * Clear the current logs and replay all old messages. This is useful to do if the display
     * format of the logs have changed, for example.
     */
    public void refresh() {
      // Even if we haven't changed any filter, calling this method quickly refreshes the log as a
      // side effect.
      onTextFilterChange();
    }
  }
}
