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
import com.android.ide.common.rendering.api.ItemResourceValue;
import com.android.ide.common.rendering.api.StyleResourceValue;
import com.android.ide.common.resources.ResourceResolver;
import com.android.ide.common.resources.ResourceUrl;
import com.android.sdklib.IAndroidTarget;
import com.android.tools.idea.configurations.Configuration;
import com.android.tools.idea.editors.theme.datamodels.ThemeEditorStyle;
import com.android.tools.lint.checks.ApiLookup;
import com.google.common.base.Strings;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.android.dom.attrs.AttributeDefinition;
import org.jetbrains.android.dom.attrs.AttributeDefinitions;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.inspections.lint.IntellijLintClient;
import org.jetbrains.android.sdk.AndroidTargetData;
import org.jetbrains.android.util.AndroidResourceUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.android.SdkConstants.PREFIX_RESOURCE_REF;
import static com.android.SdkConstants.STYLE_RESOURCE_PREFIX;
import static com.android.SdkConstants.TAG_STYLE;

/**
 * Utility methods for style resolution.
 */
public class ResolutionUtils {

  private static final Logger LOG = Logger.getInstance(ResolutionUtils.class);

  private static final Pattern RESOURCE_URL_MATCHER = Pattern.compile("@(.*:)?(.+/)(.+)");

  // Utility methods class isn't meant to be constructed, all methods are static.
  private ResolutionUtils() { }

  /**
   * @return ResourceUrl representation of a style from qualifiedName
   * e.g. for "android:Theme" returns "@android:style/Theme" or for "AppTheme" returns "@style/AppTheme"
   */
  @NotNull
  public static String getStyleResourceUrl(@NotNull String qualifiedName) {
    int colonIndex = qualifiedName.indexOf(':');
    if (colonIndex != -1) {
      // The theme name contains a namespace, change the format to be "@namespace:style/ThemeName"
      String namespace = qualifiedName.substring(0, colonIndex + 1); // Name space plus + colon
      String themeNameWithoutNamespace = StringUtil.trimStart(qualifiedName, namespace);
      return PREFIX_RESOURCE_REF + namespace + TAG_STYLE + "/" + themeNameWithoutNamespace;
    }

    return STYLE_RESOURCE_PREFIX + qualifiedName;
  }

  /**
   * @return qualified name of a style from ResourceUrl representation
   * e.g. for "@android:style/Theme" returns "android:Theme" or for "@style/AppTheme" returns "AppTheme"
   */
  @NotNull
  public static String getQualifiedNameFromResourceUrl(@NotNull String styleResourceUrl) {
    Matcher matcher = RESOURCE_URL_MATCHER.matcher(styleResourceUrl);
    boolean matches = matcher.find();
    assert matches;

    String namespace = Strings.nullToEmpty(matcher.group(1)); // the namespace containing the colon (if existing)
    String resourceName = matcher.group(3); // the resource name

    return namespace + resourceName;
  }

  /**
   * @return name without qualifier
   * e.g. for "android:Theme" returns "Theme" or for "AppTheme" returns "AppTheme"
   */
  @NotNull
  public static String getNameFromQualifiedName(@NotNull String qualifiedName) {
    if (qualifiedName.startsWith(SdkConstants.PREFIX_ANDROID)) {
      return qualifiedName.substring(SdkConstants.PREFIX_ANDROID.length());
    }
    return qualifiedName;
  }

  /**
   * Returns the style name, including the appropriate namespace.
   */
  @NotNull
  public static String getQualifiedStyleName(@NotNull StyleResourceValue style) {
    return (style.isFramework() ? SdkConstants.PREFIX_ANDROID : "") + style.getName();
  }

  /**
   * Returns the item name, including the appropriate namespace.
   */
  @NotNull
  public static String getQualifiedItemName(@NotNull ItemResourceValue item) {
    return (item.isFrameworkAttr() ? SdkConstants.PREFIX_ANDROID : "") + item.getName();
  }

  /**
   * Returns item value, maybe with "android:" qualifier,
   * If item is inside of the framework style, "android:" qualifier will be added
   * For example: For a value "@color/black" which is inside the "Theme.Holo.Light.DarkActionBar" style,
   * will be returned as "@android:color/black"
   */
  @NotNull
  public static String getQualifiedValue(@NotNull ItemResourceValue item) {
    ResourceUrl url = ResourceUrl.parse(item.getRawXmlValue(), item.isFramework());
    return url == null ? item.getRawXmlValue() : url.toString();
  }

  @Nullable
  private static StyleResourceValue getStyleResourceValue(@NotNull ResourceResolver resolver, @NotNull String qualifiedStyleName) {
    assert !qualifiedStyleName.startsWith(SdkConstants.ANDROID_STYLE_RESOURCE_PREFIX);
    assert !qualifiedStyleName.startsWith(SdkConstants.STYLE_RESOURCE_PREFIX);
    String styleName;
    boolean isFrameworkStyle;

    if (qualifiedStyleName.startsWith(SdkConstants.PREFIX_ANDROID)) {
      styleName = qualifiedStyleName.substring(SdkConstants.PREFIX_ANDROID.length());
      isFrameworkStyle = true;
    } else {
      styleName = qualifiedStyleName;
      isFrameworkStyle = false;
    }

    return resolver.getStyle(styleName, isFrameworkStyle);
  }

  /**
   * Constructs a {@link ThemeEditorStyle} instance for a theme with the given name and source module, using the passed resolver.
   */
  @Nullable
  public static ThemeEditorStyle getStyle(@NotNull Configuration configuration, @NotNull ResourceResolver resolver, @NotNull final String qualifiedStyleName, @Nullable Module module) {
    final StyleResourceValue style = getStyleResourceValue(resolver, qualifiedStyleName);
    return style == null ? null : new ThemeEditorStyle(configuration, style, module);
  }

  @Nullable
  public static ThemeEditorStyle getStyle(@NotNull Configuration configuration, @NotNull final String qualifiedStyleName, @Nullable Module module) {
    ResourceResolver resolver = configuration.getResourceResolver();
    assert resolver != null;

    return getStyle(configuration, configuration.getResourceResolver(), qualifiedStyleName, module);
  }

  @Nullable
  public static AttributeDefinition getAttributeDefinition(@NotNull Configuration configuration, @NotNull ItemResourceValue itemResValue) {
    AttributeDefinitions definitions;
    Module module = configuration.getModule();

    if (itemResValue.isFrameworkAttr()) {
      IAndroidTarget target = configuration.getTarget();
      assert target != null;

      AndroidTargetData androidTargetData = AndroidTargetData.getTargetData(target, module);
      assert androidTargetData != null;

      definitions = androidTargetData.getAllAttrDefs(module.getProject());
    }
    else {
      AndroidFacet facet = AndroidFacet.getInstance(module);
      assert facet != null : String.format("Module %s is not an Android module", module.getName());

      definitions = facet.getLocalResourceManager().getAttributeDefinitions();
    }
    if (definitions == null) {
      return null;
    }
    return definitions.getAttrDefByName(itemResValue.getName());
  }

  /**
   * Returns the Api level at which was defined the attribute or value with the name passed as argument.
   * Returns -1 if the name argument is null or not the name of a framework attribute or resource,
   * or if it is the name of a framework attribute or resource defined in API 1, or if no Lint client found.
   */
  public static int getOriginalApiLevel(@Nullable String name, @NotNull Project project) {
    if (name == null) {
      return -1;
    }

    ApiLookup apiLookup = IntellijLintClient.getApiLookup(project);
    if (apiLookup == null) {
      // There is no Lint API database for this project
      LOG.warn("Could not find Lint client for project " + project.getName());
      return -1;
    }

    ResourceUrl resUrl = ResourceUrl.parse(name);
    if (resUrl == null) {
      // It is an attribute
      if (!name.startsWith(SdkConstants.ANDROID_NS_NAME_PREFIX)) {
        // not an android attribute
        return -1;
      }
      return apiLookup.getFieldVersion("android/R$attr", name.substring(SdkConstants.ANDROID_NS_NAME_PREFIX_LEN));
    } else {
      if (!resUrl.framework) {
        // not an android value
        return -1;
      }
      return apiLookup.getFieldVersion("android/R$" + resUrl.type, AndroidResourceUtil.getFieldNameByResourceName(resUrl.name));
    }
  }

  @Nullable("if this style doesn't have parent")
  public static String getParentQualifiedName(@NotNull StyleResourceValue style) {
    String parentName = ResourceResolver.getParentName(style);
    if (parentName == null) {
      return null;
    }
    if (parentName.startsWith(SdkConstants.PREFIX_RESOURCE_REF)) {
      parentName = getQualifiedNameFromResourceUrl(parentName);
    }
    if (style.isFramework() && !parentName.startsWith(SdkConstants.PREFIX_ANDROID)) {
      parentName = SdkConstants.PREFIX_ANDROID + parentName;
    }
    return parentName;
  }
}
