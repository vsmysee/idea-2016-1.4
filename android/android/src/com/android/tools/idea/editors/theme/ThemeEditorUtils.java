/*
 * Copyright (C) 2014 The Android Open Source Project
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
package com.android.tools.idea.editors.theme;

import com.android.SdkConstants;
import com.android.builder.model.SourceProvider;
import com.android.ide.common.rendering.api.ItemResourceValue;
import com.android.ide.common.rendering.api.ResourceValue;
import com.android.ide.common.rendering.api.StyleResourceValue;
import com.android.ide.common.res2.ResourceItem;
import com.android.ide.common.resources.ResourceResolver;
import com.android.ide.common.resources.ResourceUrl;
import com.android.ide.common.resources.configuration.FolderConfiguration;
import com.android.ide.common.resources.configuration.VersionQualifier;
import com.android.resources.ResourceFolderType;
import com.android.resources.ResourceType;
import com.android.sdklib.IAndroidTarget;
import com.android.tools.idea.actions.OverrideResourceAction;
import com.android.tools.idea.configurations.Configuration;
import com.android.tools.idea.configurations.ConfigurationManager;
import com.android.tools.idea.configurations.ResourceResolverCache;
import com.android.tools.idea.configurations.ThemeSelectionPanel;
import com.android.tools.idea.editors.theme.datamodels.ConfiguredElement;
import com.android.tools.idea.editors.theme.datamodels.EditedStyleItem;
import com.android.tools.idea.editors.theme.datamodels.ThemeEditorStyle;
import com.android.tools.idea.gradle.AndroidGradleModel;
import com.android.tools.idea.javadoc.AndroidJavaDocRenderer;
import com.android.tools.idea.model.AndroidModuleInfo;
import com.android.tools.idea.rendering.*;
import com.google.common.base.Predicate;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.Processor;
import org.jetbrains.android.dom.attrs.AttributeDefinition;
import org.jetbrains.android.dom.attrs.AttributeFormat;
import org.jetbrains.android.dom.resources.ResourceElement;
import org.jetbrains.android.dom.resources.Style;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.uipreview.ChooseResourceDialog;
import org.jetbrains.android.util.AndroidResourceUtil;
import org.jetbrains.android.util.AndroidUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Utility class for static methods which are used in different classes of theme editor
 */
public class ThemeEditorUtils {
  private static final Logger LOG = Logger.getInstance(ThemeEditorUtils.class);

  private static final Cache<String, String> ourTooltipCache = CacheBuilder.newBuilder().weakValues().maximumSize(30) // To be able to cache roughly one screen of attributes
    .build();
  private static final Set<String> DEFAULT_THEMES = ImmutableSet.of("Theme.AppCompat.NoActionBar", "Theme.AppCompat.Light.NoActionBar");
  private static final Set<String> DEFAULT_THEMES_FALLBACK =
    ImmutableSet.of("Theme.Material.NoActionBar", "Theme.Material.Light.NoActionBar");

  private static final String[] CUSTOM_WIDGETS_JAR_PATHS = {
    // Bundled path
    "/plugins/android/lib/androidWidgets/theme-editor-widgets.jar",
    // Development path
    "/../adt/idea/android/lib/androidWidgets/theme-editor-widgets.jar",
    // IDEA plugin Development path
    "/community/android/android/lib/androidWidgets/theme-editor-widgets.jar"
  };

  private ThemeEditorUtils() {
  }

  @NotNull
  public static String generateToolTipText(@NotNull final ItemResourceValue resValue,
                                           @NotNull final Module module,
                                           @NotNull final Configuration configuration) {
    final LocalResourceRepository repository = AppResourceRepository.getAppResources(module, true);
    if (repository == null) {
      return "";
    }

    String tooltipKey = resValue.toString() + module.toString() + configuration.toString() + repository.getModificationCount();

    String cachedTooltip = ourTooltipCache.getIfPresent(tooltipKey);
    if (cachedTooltip != null) {
      return cachedTooltip;
    }

    String tooltipContents = AndroidJavaDocRenderer.renderItemResourceWithDoc(module, configuration, resValue);
    ourTooltipCache.put(tooltipKey, tooltipContents);

    return tooltipContents;
  }

  /**
   * Returns html that will be displayed in attributes table for a given item.
   * For example: deprecated attrs will be with a strike through
   */
  @NotNull
  public static String getDisplayHtml(EditedStyleItem item) {
    return item.isDeprecated() ? "<html><body><strike>" + item.getQualifiedName() + "</strike></body></html>" : item.getQualifiedName();
  }

  public static boolean isThemeEditorSelected(@NotNull Project project) {
    for (FileEditor editor : FileEditorManager.getInstance(project).getSelectedEditors()) {
      if (editor instanceof ThemeEditor) {
        return true;
      }
    }
    return false;
  }

  public static void openThemeEditor(@NotNull final Project project) {
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      @Override
      public void run() {
        ThemeEditorVirtualFile file = null;
        final FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);

        for (final FileEditor editor : fileEditorManager.getAllEditors()) {
          if (!(editor instanceof ThemeEditor)) {
            continue;
          }

          ThemeEditor themeEditor = (ThemeEditor)editor;
          if (themeEditor.getVirtualFile().getProject() == project) {
            file = themeEditor.getVirtualFile();
            break;
          }
        }

        // If existing virtual file is found, openEditor with created descriptor is going to
        // show existing editor (without creating a new tab). If we haven't found any existing
        // virtual file, we're creating one here (new tab with theme editor will be opened).
        if (file == null) {
          file = ThemeEditorVirtualFile.getThemeEditorFile(project);
        }
        final OpenFileDescriptor descriptor = new OpenFileDescriptor(project, file);
        fileEditorManager.openEditor(descriptor, true);
      }
    });
  }

  /**
   * Find every attribute in the theme hierarchy and all the possible configurations where it's present.
   * @param style the theme to retrieve all the attributes from
   * @param attributeConfigurations a {@link HashMultimap} where all the attributes and configurations will be stored
   * @param resolver ThemeResolver that would be used to find themes by name.
   */
  private static void findAllAttributes(@NotNull final ThemeEditorStyle style,
                                        @NotNull HashMultimap<String, FolderConfiguration> attributeConfigurations,
                                        @NotNull ThemeResolver resolver) {
    for (ConfiguredElement<String> parentName : style.getParentNames()) {
      ThemeEditorStyle parent = resolver.getTheme(parentName.getElement());

      /* Parent will be null if the theme does not exist in the current configuration */
      if (parent != null) {
        findAllAttributes(parent, attributeConfigurations, resolver);
      }
    }

    Collection<ConfiguredElement<ItemResourceValue>> configuredValues = style.getConfiguredValues();
    for (ConfiguredElement<ItemResourceValue> value : configuredValues) {
      attributeConfigurations.put(ResolutionUtils.getQualifiedItemName(value.getElement()), value.getConfiguration());
    }
  }

  /**
   * Set of {@link ConfiguredElement} items that allows overwriting elements and uses the folder and the attribute name
   * as key.
   */
  static class AttributeInheritanceSet implements Iterable<ConfiguredElement<ItemResourceValue>> {
    private final HashSet<ConfiguredElement<ItemResourceValue>> myAttributes = Sets.newHashSet();
    // Index by attribute configuration and name.
    private final Map<String, ConfiguredElement<ItemResourceValue>> myAttributesIndex = Maps.newHashMap();

    private static String getItemKey(@NotNull ConfiguredElement<ItemResourceValue> item) {
      return String.format("%1$s - %2$s", item.getConfiguration(), ResolutionUtils.getQualifiedItemName(item.getElement()));
    }

    public boolean add(@NotNull ConfiguredElement<ItemResourceValue> value) {
      String key = getItemKey(value);
      ConfiguredElement<ItemResourceValue> existingValue = myAttributesIndex.get(key);

      if (existingValue != null) {
        myAttributes.remove(existingValue);
      }

      myAttributes.add(value);
      myAttributesIndex.put(key, value);

      return existingValue != null;
    }

    @Override
    public Iterator<ConfiguredElement<ItemResourceValue>> iterator() {
      return myAttributes.iterator();
    }

    public void addAll(@NotNull AttributeInheritanceSet existingAttributes) {
      for (ConfiguredElement<ItemResourceValue> value : existingAttributes) {
        add(value);
      }
    }
  }

  public static List<EditedStyleItem> resolveAllAttributes(@NotNull final ThemeEditorStyle style, @NotNull ThemeResolver themeResolver) {
    HashMultimap<String, FolderConfiguration> attributes = HashMultimap.create();
    findAllAttributes(style, attributes, themeResolver);

    ImmutableSet<FolderConfiguration> allConfigurations = ImmutableSet.copyOf(attributes.values());

    Configuration configuration = style.getConfiguration();
    // We create new ResourceResolverCache instead of using cache from myConfiguration to optimize memory instead of time/speed
    // Because, it creates a lot of instances of ResourceResolver here, that won't be used outside of ThemeEditor
    ResourceResolverCache resolverCache = new ResourceResolverCache(configuration.getConfigurationManager());

    // Go over all the existing configurations and resolve each attribute
    Map<String, AttributeInheritanceSet> configuredAttributes = Maps.newHashMap();
    FolderConfiguration fullBaseConfiguration = themeResolver.getConfiguration().getFullConfig();
    for (FolderConfiguration folderConfiguration : allConfigurations) {
      // We apply the folderConfiguration to the full configuration that we get from the current theme resolver. We use the full
      // configuration to simulate what the device would do when resolving attributes and match more specific folders.
      FolderConfiguration fullFolderConfiguration = FolderConfiguration.copyOf(fullBaseConfiguration);
      fullFolderConfiguration.add(folderConfiguration);
      ResourceResolver resolver = resolverCache.getResourceResolver(configuration.getTarget(), style.getStyleResourceUrl(), fullFolderConfiguration);
      StyleResourceValue resolvedStyle = resolver.getStyle(style.getName(), style.isFramework());

      if (resolvedStyle == null) {
        // The style doesn't exist for this configuration
        continue;
      }

      for (String attributeName : attributes.keys()) {
        String noPrefixName = StringUtil.trimStart(attributeName, SdkConstants.PREFIX_ANDROID);
        ResourceValue value = resolver.findItemInStyle(resolvedStyle, noPrefixName, noPrefixName.length() != attributeName.length());

        if (value != null) {
          AttributeInheritanceSet inheritanceSet = configuredAttributes.get(attributeName);
          if (inheritanceSet == null) {
            inheritanceSet = new AttributeInheritanceSet();
            configuredAttributes.put(attributeName, inheritanceSet);
          }
          inheritanceSet.add(ConfiguredElement.create(folderConfiguration, (ItemResourceValue)value));
        }
      }
    }

    // Now build the EditedStyleItems from the resolved attributes
    final ImmutableList.Builder<EditedStyleItem> allValues = ImmutableList.builder();
    for (String attributeName : configuredAttributes.keySet()) {
      Iterable<ConfiguredElement<ItemResourceValue>> configuredValues = configuredAttributes.get(attributeName);
      //noinspection unchecked
      final ConfiguredElement<ItemResourceValue> bestMatch =
        (ConfiguredElement<ItemResourceValue>)style.getConfiguration().getFullConfig()
          .findMatchingConfigurable(ImmutableList.copyOf(configuredValues));

      if (bestMatch == null) {
        allValues.add(new EditedStyleItem(configuredValues.iterator().next(), style));
      }
      else {
        allValues.add(new EditedStyleItem(bestMatch, Iterables
          .filter(configuredValues, new Predicate<ConfiguredElement<ItemResourceValue>>() {
            @Override
            public boolean apply(@Nullable ConfiguredElement<ItemResourceValue> input) {
              return input != bestMatch;
            }
          }), style));
      }
    }

    return allValues.build();
  }

  /**
   * Finds an ItemResourceValue for a given name in a theme inheritance tree
   */
  @Nullable("if there is not an item with that name")
  public static ItemResourceValue resolveItemFromParents(@NotNull final ThemeEditorStyle theme,
                                                         @NotNull String name,
                                                         boolean isFrameworkAttr) {
    ThemeEditorStyle currentTheme = theme;

    for (int i = 0; (i < ResourceResolver.MAX_RESOURCE_INDIRECTION) && currentTheme != null; i++) {
      ItemResourceValue item = currentTheme.getItem(name, isFrameworkAttr);
      if (item != null) {
        return item;
      }
      currentTheme = currentTheme.getParent();
    }
    return null;
  }

  @Nullable
  public static Object extractRealValue(@NotNull final EditedStyleItem item, @NotNull final Class<?> desiredClass) {
    String value = item.getValue();
    if (desiredClass == Boolean.class && ("true".equals(value) || "false".equals(value))) {
      return Boolean.valueOf(value);
    }
    if (desiredClass == Integer.class) {
      try {
        return Integer.parseInt(value);
      } catch (NumberFormatException e) {
        return value;
      }
    }
    return value;
  }

  public static boolean acceptsFormat(@Nullable AttributeDefinition attrDefByName, @NotNull AttributeFormat want) {
    if (attrDefByName == null) {
      return false;
    }
    return attrDefByName.getFormats().contains(want);
  }

  @NotNull
  private static ImmutableCollection<ThemeEditorStyle> findThemes(@NotNull Collection<ThemeEditorStyle> themes, final @NotNull Set<String> names) {
    return ImmutableSet.copyOf(Iterables.filter(themes, new Predicate<ThemeEditorStyle>() {
      @Override
      public boolean apply(@Nullable ThemeEditorStyle theme) {
        return theme != null && names.contains(theme.getName());
      }
    }));
  }

  @NotNull
  public static ImmutableList<Module> findAndroidModules(@NotNull Project project) {
    final ModuleManager manager = ModuleManager.getInstance(project);

    final ImmutableList.Builder<Module> builder = ImmutableList.builder();
    for (Module module : manager.getModules()) {
      final AndroidFacet facet = AndroidFacet.getInstance(module);
      if (facet != null) {
        builder.add(module);
      }
    }

    return builder.build();
  }

  @NotNull
  public static ImmutableList<String> getDefaultThemeNames(@NotNull ThemeResolver themeResolver) {
    Collection<ThemeEditorStyle> readOnlyLibThemes = themeResolver.getExternalLibraryThemes();

    Collection<ThemeEditorStyle> foundThemes = new HashSet<ThemeEditorStyle>();
    foundThemes.addAll(findThemes(readOnlyLibThemes, DEFAULT_THEMES));

    if (foundThemes.isEmpty()) {
      Collection<ThemeEditorStyle> readOnlyFrameworkThemes = themeResolver.getFrameworkThemes();
      foundThemes = new HashSet<ThemeEditorStyle>();
      foundThemes.addAll(findThemes(readOnlyFrameworkThemes, DEFAULT_THEMES_FALLBACK));

      if (foundThemes.isEmpty()) {
        foundThemes.addAll(readOnlyLibThemes);
        foundThemes.addAll(readOnlyFrameworkThemes);
      }
    }
    Set<String> temporarySet = Sets.newTreeSet(String.CASE_INSENSITIVE_ORDER);
    for (ThemeEditorStyle theme : foundThemes) {
      temporarySet.add(theme.getQualifiedName());
    }
    return ImmutableList.copyOf(temporarySet);
  }

  public static int getMinApiLevel(@NotNull Module module) {
    AndroidFacet facet = AndroidFacet.getInstance(module);
    if (facet == null) {
      return 1;
    }
    AndroidModuleInfo moduleInfo = AndroidModuleInfo.get(facet);
    return moduleInfo.getMinSdkVersion().getApiLevel();
  }

  /**
   * Returns the URL for the theme editor custom widgets jar
   */
  @Nullable
  public static URL getCustomWidgetsJarUrl() {
    String homePath = FileUtil.toSystemIndependentName(PathManager.getHomePath());

    StringBuilder notFoundPaths = new StringBuilder();
    for (String path : CUSTOM_WIDGETS_JAR_PATHS) {
      String jarPath = homePath + path;
      VirtualFile root = LocalFileSystem.getInstance().findFileByPath(FileUtil.toSystemIndependentName(jarPath));

      if (root != null) {
        File rootFile = VfsUtilCore.virtualToIoFile(root);
        if (rootFile.exists()) {
          try {
            LOG.debug("Theme editor custom widgets found at " + jarPath);
            return rootFile.toURI().toURL();
          }
          catch (MalformedURLException e) {
            LOG.error(e);
          }
        }
      }
      else {
        notFoundPaths.append(jarPath).append('\n');
      }
    }

    LOG.error("Unable to find theme-editor-widgets.jar in paths:\n" + notFoundPaths.toString());
    return null;
  }

  /**
   * Creates a new style
   * @param module the module where the new style is being created
   * @param newStyleName the new style name
   * @param parentStyleName the name of the new style parent
   * @param fileName name of the xml file where the style will be added (usually "styles.xml")
   * @param folderNames folder names where the style will be added
   * @return true if the style was created or false otherwise
   */
  public static boolean createNewStyle(@NotNull final Module module, final @NotNull String newStyleName, final @Nullable String parentStyleName, final @NotNull String fileName, final @NotNull List<String> folderNames) {
    return new WriteCommandAction<Boolean>(module.getProject(), "Create new style " + newStyleName) {
      @Override
      protected void run(@NotNull Result<Boolean> result) {
        CommandProcessor.getInstance().markCurrentCommandAsGlobal(module.getProject());
        result.setResult(AndroidResourceUtil.
          createValueResource(module, newStyleName, null,
                              ResourceType.STYLE, fileName, folderNames, new Processor<ResourceElement>() {
              @Override
              public boolean process(ResourceElement element) {
                assert element instanceof Style;
                final Style style = (Style)element;

                if (parentStyleName != null) {
                  style.getParentStyle().setStringValue(parentStyleName);
                }

                return true;
              }
            }));
      }
    }.execute().getResultObject();
  }

  /**
   * Creates a new style by displaying the dialog of the {@link NewStyleDialog}.
   * @param defaultParentStyle is used in NewStyleDialog, will be preselected in the parent text field and name will be suggested based on it
   * @param themeEditorContext  current theme editor context
   * @param isTheme whether theme or style will be created
   * @param message is used in NewStyleDialog to display message to user
   * @return the new style name or null if the style wasn't created
   */
  @Nullable
  public static String showCreateNewStyleDialog(@Nullable ThemeEditorStyle defaultParentStyle,
                                                @NotNull final ThemeEditorContext themeEditorContext,
                                                boolean isTheme,
                                                boolean enableParentChoice,
                                                @Nullable final String message,
                                                @Nullable ThemeSelectionPanel.ThemeChangedListener themeChangedListener) {
    // if isTheme is true, defaultParentStyle shouldn't be null
    String defaultParentStyleName = null;
    if (isTheme && defaultParentStyle == null) {
      defaultParentStyleName = getDefaultThemeNames(themeEditorContext.getThemeResolver()).get(0);
    }
    else if (defaultParentStyle != null) {
      defaultParentStyleName = defaultParentStyle.getQualifiedName();
    }

    final NewStyleDialog dialog = new NewStyleDialog(isTheme, themeEditorContext, defaultParentStyleName,
                                                     (defaultParentStyle == null) ? null : defaultParentStyle.getName(), message);
    dialog.enableParentChoice(enableParentChoice);
    if (themeChangedListener != null) {
      dialog.setThemeChangedListener(themeChangedListener);
    }

    boolean createStyle = dialog.showAndGet();
    if (!createStyle) {
      return null;
    }

    int minModuleApi = getMinApiLevel(themeEditorContext.getCurrentContextModule());
    int minAcceptableApi = ResolutionUtils.getOriginalApiLevel(ResolutionUtils.getStyleResourceUrl(dialog.getStyleParentName()), themeEditorContext.getProject());

    final String fileName = AndroidResourceUtil.getDefaultResourceFileName(ResourceType.STYLE);
    FolderConfiguration config = new FolderConfiguration();
    if (minModuleApi < minAcceptableApi) {
      VersionQualifier qualifier = new VersionQualifier(minAcceptableApi);
      config.setVersionQualifier(qualifier);
    }
    final List<String> dirNames = Collections.singletonList(config.getFolderName(ResourceFolderType.VALUES));

    if (fileName == null) {
      LOG.error("Couldn't find a default filename for ResourceType.STYLE");
      return null;
    }

    String parentStyleName = dialog.getStyleParentName();
    boolean isCreated = createNewStyle(
      themeEditorContext.getCurrentContextModule(), dialog.getStyleName(), parentStyleName, fileName, dirNames);

    return isCreated ? dialog.getStyleName() : null;
  }

  /**
   * Checks if the selected theme is AppCompat
   */
  public static boolean isSelectedAppCompatTheme(@NotNull ThemeEditorContext context) {
    ThemeEditorStyle currentTheme = context.getCurrentTheme();
    return currentTheme != null && isAppCompatTheme(currentTheme);
  }

  /**
   * Checks if a theme is AppCompat
   */
  public static boolean isAppCompatTheme(@NotNull ThemeEditorStyle themeEditorStyle) {
    ThemeEditorStyle currentTheme = themeEditorStyle;
    for (int i = 0; (i < ResourceResolver.MAX_RESOURCE_INDIRECTION) && currentTheme != null; i++) {
      // for loop ensures that we don't run into cyclic theme inheritance.
      //TODO: This check is not enough. User themes could also start with "Theme.AppCompat" and not be AppCompat
      if (currentTheme.getName().startsWith("Theme.AppCompat") && currentTheme.getSourceModule() == null) {
        return true;
      }
      currentTheme = currentTheme.getParent();
    }
    return false;
  }

  /**
   * Copies a theme to a values folder with api version apiLevel,
   * potentially creating the necessary folder or file.
   * @param apiLevel api level of the folder the theme is copied to
   * @param toBeCopied theme to be copied
   */
  public static void copyTheme(int apiLevel, @NotNull final XmlTag toBeCopied) {
    ApplicationManager.getApplication().assertWriteAccessAllowed();

    PsiFile file = toBeCopied.getContainingFile();
    assert file instanceof XmlFile : file;
    ResourceFolderType folderType = ResourceHelper.getFolderType(file);
    assert folderType != null : file;
    FolderConfiguration config = ResourceHelper.getFolderConfiguration(file);
    assert config != null : file;

    VersionQualifier qualifier = new VersionQualifier(apiLevel);
    config.setVersionQualifier(qualifier);
    String folder = config.getFolderName(folderType);

    if (folderType != ResourceFolderType.VALUES) {
      OverrideResourceAction.forkResourceFile((XmlFile)file, folder, false);
    }
    else {
      XmlTag tag = OverrideResourceAction.getValueTag(PsiTreeUtil.getParentOfType(toBeCopied, XmlTag.class, false));
      if (tag != null) {
        AndroidFacet facet = AndroidFacet.getInstance(toBeCopied);
        if (facet != null) {
          PsiDirectory dir = null;
          PsiDirectory resFolder = file.getParent();
          if (resFolder != null) {
            resFolder = resFolder.getParent();
          }
          if (resFolder != null) {
            dir = resFolder.findSubdirectory(folder);
            if (dir == null) {
              dir = resFolder.createSubdirectory(folder);
            }
          }
          OverrideResourceAction.forkResourceValue(toBeCopied.getProject(), tag, file, facet, dir, false);
        }
      }
    }
  }

  /**
   * Returns version qualifier of FolderConfiguration.
   * Returns -1, if FolderConfiguration has default version
   */
  public static int getVersionFromConfiguration(@NotNull FolderConfiguration configuration) {
    VersionQualifier qualifier = configuration.getVersionQualifier();
    return (qualifier != null) ? qualifier.getVersion() : -1;
  }

  /**
   * Returns the smallest api level of the folders in folderNames.
   * Returns Integer.MAX_VALUE if folderNames is empty.
   */
  public static int getMinFolderApi(@NotNull List<String> folderNames, @NotNull Module module) {
    int minFolderApi = Integer.MAX_VALUE;
    int minModuleApi = getMinApiLevel(module);
    for (String folderName : folderNames) {
      FolderConfiguration folderConfig = FolderConfiguration.getConfigForFolder(folderName);
      if (folderConfig != null) {
        VersionQualifier version = folderConfig.getVersionQualifier();
        int folderApi = version != null ? version.getVersion() : minModuleApi;
        minFolderApi = Math.min(minFolderApi, folderApi);
      }
    }
    return minFolderApi;
  }

  @NotNull
  public static Configuration getConfigurationForModule(@NotNull Module module) {
    Project project = module.getProject();
    final AndroidFacet facet = AndroidFacet.getInstance(module);
    assert facet != null : "moduleComboModel must contain only Android modules";

    ConfigurationManager configurationManager = facet.getConfigurationManager();

    // Using the project virtual file to set up configuration for the theme editor
    // That fact is hard-coded in computeBestDevice() method in Configuration.java
    // BEWARE if attempting to modify to use a different virtual file
    final VirtualFile projectFile = project.getProjectFile();
    assert projectFile != null;

    return configurationManager.getConfiguration(projectFile);
  }

  /**
   * Given a {@link SourceProvider}, it returns a list of all the available ResourceFolderRepositories
   */
  @NotNull
  public static List<ResourceFolderRepository> getResourceFolderRepositoriesFromSourceSet(@NotNull AndroidFacet facet,
                                                                                          @Nullable SourceProvider provider) {
    if (provider == null) {
      return Collections.emptyList();
    }

    Collection<File> resDirectories = provider.getResDirectories();

    LocalFileSystem fileSystem = LocalFileSystem.getInstance();
    List<ResourceFolderRepository> folders = Lists.newArrayListWithExpectedSize(resDirectories.size());
    for (File dir : resDirectories) {
      VirtualFile virtualFile = fileSystem.findFileByIoFile(dir);
      if (virtualFile != null) {
        folders.add(ResourceFolderRegistry.get(facet, virtualFile));
      }
    }

    return folders;
  }

  /**
   * Interface to visit all the available {@link LocalResourceRepository}
   */
  public interface ResourceFolderVisitor {
    /**
     * @param resources a repository containing resources
     * @param moduleName the module name
     * @param variantName string that identifies the variant used to obtain the resources
     * @param isSelected true if the current passed repository is in an active source set
     */
    void visitResourceFolder(@NotNull LocalResourceRepository resources, String moduleName, @NotNull String variantName, boolean isSelected);
  }

  /**
   * Visits every ResourceFolderRepository. It visits every resource in order, meaning that the later calls may override resources from
   * previous ones.
   */
  public static void acceptResourceResolverVisitor(final @NotNull AndroidFacet mainFacet, final @NotNull ResourceFolderVisitor visitor) {
    // Get all the dependencies of the module in reverse order (first one is the lowest priority one)
    List<AndroidFacet> dependencies =  Lists.reverse(AndroidUtils.getAllAndroidDependencies(mainFacet.getModule(), true));

    // The order of iteration here is important since the resources from the mainFacet will override those in the dependencies.
    for (AndroidFacet dependency : Iterables.concat(dependencies, ImmutableList.of(mainFacet))) {
      AndroidGradleModel androidModel = AndroidGradleModel.get(dependency);
      if (androidModel == null) {
        // For non gradle module, get the main source provider
        SourceProvider provider = dependency.getMainSourceProvider();
        for (LocalResourceRepository resourceRepository : getResourceFolderRepositoriesFromSourceSet(dependency, provider)) {
          visitor.visitResourceFolder(resourceRepository, dependency.getName(), provider.getName(), true);
        }
      } else {
        // For gradle modules, get all source providers and go through them
        // We need to iterate the providers in the returned to make sure that they correctly override each other
        List<SourceProvider> activeProviders = androidModel.getActiveSourceProviders();
        for (SourceProvider provider : activeProviders) {
          for (LocalResourceRepository resourceRepository : getResourceFolderRepositoriesFromSourceSet(dependency, provider)) {
            visitor.visitResourceFolder(resourceRepository, dependency.getName(), provider.getName(), true);
          }
        }

        // Not go through all the providers that are not in the activeProviders
        ImmutableSet<SourceProvider> selectedProviders = ImmutableSet.copyOf(activeProviders);
        for (SourceProvider provider : androidModel.getAllSourceProviders()) {
          if (!selectedProviders.contains(provider)) {
            for (LocalResourceRepository resourceRepository : getResourceFolderRepositoriesFromSourceSet(dependency, provider)) {
              visitor.visitResourceFolder(resourceRepository, dependency.getName(), provider.getName(), false);
            }
          }
        }
      }
    }
  }

  /**
   * Returns the list of the qualified names of all the user-defined themes available from a given module
   */
  @NotNull
  public static ImmutableList<String> getModuleThemeQualifiedNamesList(@NotNull Module module) {
    AndroidFacet facet = AndroidFacet.getInstance(module);
    assert facet != null;
    ConfigurationManager manager = facet.getConfigurationManager();
    // We create a new ResourceResolverCache instead of using cache from myConfiguration to optimize memory instead of time/speed,
    // because we are about to create a lot of instances of ResourceResolver here that won't be used outside of this method
    final ResourceResolverCache resolverCache = new ResourceResolverCache(manager);
    final IAndroidTarget target = manager.getTarget();
    final Map<ResourceValue, Boolean> cache = new HashMap<ResourceValue, Boolean>();
    final Set<String> themeNamesSet = Sets.newTreeSet(String.CASE_INSENSITIVE_ORDER);

    ResourceFolderVisitor visitor = new ResourceFolderVisitor() {
      @Override
      public void visitResourceFolder(@NotNull LocalResourceRepository resources,
                                      String moduleName,
                                      @NotNull String variantName,
                                      boolean isSelected) {
        if (!isSelected) {
          return;
        }
        for (String simpleThemeName : resources.getItemsOfType(ResourceType.STYLE)) {
          String themeStyleResourceUrl = SdkConstants.STYLE_RESOURCE_PREFIX + simpleThemeName;
          List<ResourceItem> themeItems = resources.getResourceItem(ResourceType.STYLE, simpleThemeName);
          assert themeItems != null;
          for (ResourceItem themeItem : themeItems) {
            ResourceResolver resolver = resolverCache.getResourceResolver(target, themeStyleResourceUrl, themeItem.getConfiguration());
            ResourceValue themeItemResourceValue = themeItem.getResourceValue(false);
            assert themeItemResourceValue != null;
            if (resolver.isTheme(themeItemResourceValue, cache)) {
              themeNamesSet.add(simpleThemeName);
              break;
            }
          }
        }
      }
    };

    acceptResourceResolverVisitor(facet, visitor);

    return ImmutableList.copyOf(themeNamesSet);
  }

  @NotNull
  public static ChooseResourceDialog getResourceDialog(@NotNull EditedStyleItem item,
                                                       @NotNull ThemeEditorContext context,
                                                       ResourceType[] allowedTypes) {
    Module module = context.getModuleForResources();
    final Configuration configuration = getConfigurationForModule(module);

    ResourceResolver resourceResolver = configuration.getResourceResolver();
    assert resourceResolver != null;

    ItemResourceValue itemSelectedValue = item.getSelectedValue();

    String value = itemSelectedValue.getValue();
    boolean isFrameworkValue = itemSelectedValue.isFramework();

    String nameSuggestion = value;
    ResourceUrl url = ResourceUrl.parse(value, isFrameworkValue);
    if (url != null) {
      nameSuggestion = url.name;
    }
    nameSuggestion = getDefaultResourceName(context, nameSuggestion);

    return new ChooseResourceDialog(module, configuration, allowedTypes, value, isFrameworkValue,
                                    ChooseResourceDialog.ResourceNameVisibility.FORCE, nameSuggestion);
  }

  /**
   * Build a name for a new resource based on a provided name.
   * @param initialName a name that result should be based on (that might not be vacant)
   */
  @NotNull
  private static String getDefaultResourceName(@NotNull ThemeEditorContext context, final @NotNull String initialName) {
    if (context.getCurrentTheme() == null || !context.getCurrentTheme().isReadOnly()) {
      // If the currently selected theme is not read-only, then the expected
      // behaviour of color picker would be to edit the existing resource.
      return initialName;
    }

    final ResourceResolver resolver = context.getResourceResolver();
    assert resolver != null;
    final ResourceValue value = resolver.findResValue(SdkConstants.COLOR_RESOURCE_PREFIX + initialName, false);

    // Value doesn't exist, safe to use initial guess
    if (value == null) {
      return initialName;
    }

    // Given value exist, need to add a suffix to initialName to make it unique
    for (int i = 1; i <= 50; ++i) {
      final String name = initialName + "_" + i;

      if (resolver.findResValue(SdkConstants.COLOR_RESOURCE_PREFIX + name, false) == null) {
        // Found a vacant name
        return name;
      }
    }

    // Made 50 iterations and still no luck finding a vacant name
    // Just set a default name to empty string so user have to insert the name manually
    return "";
  }

  /**
   * Returns a more user-friendly name of a given theme.
   * Aimed at framework themes with names of the form Theme.*.Light.*
   * or Theme.*.*
   */
  @NotNull
  public static String simplifyThemeName(@NotNull ThemeEditorStyle theme) {
    String result;
    String name = theme.getQualifiedName();
    String[] pieces = name.split("\\.");
    if (pieces.length > 1 && !"Light".equals(pieces[1])) {
      result = pieces[1];
    }
    else {
      result = "Theme";
    }
    ThemeEditorStyle parent = theme;
    while (parent != null) {
      if ("Theme.Light".equals(parent.getName())) {
        return result + " Light";
      }
      else {
        parent = parent.getParent();
      }
    }
    return result + " Dark";
  }

  /**
   * Returns a StringBuilder with the words concatenated into an enumeration w1, w2, ..., w(n-1) and  wn
   */
  @NotNull
  public static String generateWordEnumeration(@NotNull Collection<String> words) {
    StringBuilder sentenceBuilder = new StringBuilder();
    int nWords = words.size();
    int i = 0;
    for (String word : words) {
      sentenceBuilder.append(word);
      if (i < nWords - 2) {
        sentenceBuilder.append(", ");
      }
      else if (i == nWords - 2) {
        sentenceBuilder.append(" and ");
      }
      i++;
    }
    return sentenceBuilder.toString();
  }
}
