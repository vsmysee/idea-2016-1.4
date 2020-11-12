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
package com.android.tools.idea.gradle.customizer.java;

import com.android.tools.idea.gradle.AndroidGradleModel;
import com.android.tools.idea.gradle.JavaProject;
import com.android.tools.idea.gradle.customizer.ModuleCustomizer;
import com.google.common.collect.Lists;
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.LanguageLevelModuleExtensionImpl;
import com.intellij.pom.java.LanguageLevel;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.intellij.openapi.module.ModuleUtilCore.getAllDependentModules;
import static com.intellij.pom.java.LanguageLevel.JDK_1_6;
import static com.intellij.pom.java.LanguageLevel.JDK_1_8;

/**
 * Configures Java SDK for Java library module.
 */
public class JavaLanguageLevelModuleCustomizer implements ModuleCustomizer<JavaProject> {
  @Override
  public void customizeModule(@NotNull Project project,
                              @NotNull Module module,
                              @NotNull IdeModifiableModelsProvider modelsProvider,
                              @Nullable JavaProject javaProject) {
    if (javaProject == null) {
      return;
    }
    LanguageLevel languageLevel = javaProject.getJavaLanguageLevel();

    if (languageLevel == null) {
      // Java language 1.8 is not supported, fall back to the minimum Java language level in dependent modules.
      List<Module> dependents = modelsProvider.getAllDependentModules(module);
      languageLevel = getMinimumLanguageLevelForAndroidModules(dependents.toArray(new Module[dependents.size()]));
    }

    if (languageLevel == null) {
      // Java language is still not correct. Most likely this module does not have dependents.
      // Get minimum language level from all Android modules.
      Module[] modules = ModuleManager.getInstance(project).getModules();
      languageLevel = getMinimumLanguageLevelForAndroidModules(modules);
    }

    if (languageLevel == null) {
      languageLevel = JDK_1_6; // The minimum safe Java language level.
    }

    modelsProvider.getModifiableRootModel(module).getModuleExtension(LanguageLevelModuleExtensionImpl.class).setLanguageLevel(languageLevel);
  }

  @Nullable
  private static LanguageLevel getMinimumLanguageLevelForAndroidModules(@NotNull Module[] modules) {
    if (modules.length == 0) {
      return null;
    }

    LanguageLevel result = null;

    List<LanguageLevel> languageLevels = Lists.newArrayList();
    for (Module dependency : modules) {
      LanguageLevel dependencyLanguageLevel = getLanguageLevelForAndroidModule(dependency);
      if (dependencyLanguageLevel != null) {
        languageLevels.add(dependencyLanguageLevel);
      }
    }

    for (LanguageLevel dependencyLanguageLevel : languageLevels) {
      if (result == null || result.compareTo(dependencyLanguageLevel) > 0) {
        result = dependencyLanguageLevel;
      }
    }

    return result;
  }

  @Nullable
  private static LanguageLevel getLanguageLevelForAndroidModule(@NotNull Module module) {
    AndroidFacet facet = AndroidFacet.getInstance(module);
    if (facet != null) {
      AndroidGradleModel androidModel = AndroidGradleModel.get(facet);
      if (androidModel != null) {
        return androidModel.getJavaLanguageLevel();
      }
    }
    return null;
  }
}
