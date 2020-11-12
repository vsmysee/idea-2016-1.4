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
package com.android.tools.idea.gradle.dsl.model.android;

import com.android.tools.idea.gradle.dsl.parser.android.ProductFlavorDslElement;
import com.android.tools.idea.gradle.dsl.parser.elements.*;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public final class ProductFlavorModel {
  private static final String APPLICATION_ID = "applicationId";
  private static final String CONSUMER_PROGUARD_FILES = "consumerProguardFiles";
  private static final String DIMENSION = "dimension";
  private static final String MANIFEST_PLACEHOLDERS = "manifestPlaceholders";
  private static final String MAX_SDK_VERSION = "maxSdkVersion";
  private static final String MIN_SDK_VERSION = "minSdkVersion";
  private static final String MULTI_DEX_ENABLED = "multiDexEnabled";
  private static final String PROGUARD_FILES = "proguardFiles";
  private static final String RES_CONFIGS = "resConfigs";
  private static final String RES_VALUES = "resValues";
  private static final String TARGET_SDK_VERSION = "targetSdkVersion";
  private static final String TEST_APPLICATION_ID = "testApplicationId";
  private static final String TEST_FUNCTIONAL_TEST = "testFunctionalTest";
  private static final String TEST_HANDLE_PROFILING = "testHandleProfiling";
  private static final String TEST_INSTRUMENTATION_RUNNER = "testInstrumentationRunner";
  private static final String TEST_INSTRUMENTATION_RUNNER_ARGUMENTS = "testInstrumentationRunnerArguments";
  private static final String USE_JACK = "useJack";
  private static final String VERSION_CODE = "versionCode";
  private static final String VERSION_NAME = "versionName";

  private final ProductFlavorDslElement myDslElement;

  public ProductFlavorModel(@NotNull ProductFlavorDslElement dslElement) {
    myDslElement = dslElement;
  }

  @NotNull
  public String name() {
    return myDslElement.getName();
  }

  @Nullable
  public String applicationId() {
    return myDslElement.getProperty(APPLICATION_ID, String.class);
  }

  @NotNull
  public ProductFlavorModel setApplicationId(@NotNull String applicationId) {
    myDslElement.setLiteralProperty(APPLICATION_ID, applicationId);
    return this;
  }

  @NotNull
  public ProductFlavorModel removeApplicationId() {
    myDslElement.removeProperty(APPLICATION_ID);
    return this;
  }

  @Nullable
  public List<String> consumerProguardFiles() {
    return myDslElement.getListProperty(CONSUMER_PROGUARD_FILES, String.class);
  }

  @NotNull
  public ProductFlavorModel addConsumerProguardFile(@NotNull String consumerProguardFile) {
    myDslElement.addToListProperty(CONSUMER_PROGUARD_FILES, consumerProguardFile);
    return this;
  }

  @NotNull
  public ProductFlavorModel removeConsumerProguardFile(@NotNull String consumerProguardFile) {
    myDslElement.removeFromListProperty(CONSUMER_PROGUARD_FILES, consumerProguardFile);
    return this;
  }

  @NotNull
  public ProductFlavorModel removeAllConsumerProguardFiles() {
    myDslElement.removeProperty(CONSUMER_PROGUARD_FILES);
    return this;
  }

  @NotNull
  public ProductFlavorModel replaceConsumerProguardFile(@NotNull String oldConsumerProguardFile,
                                                          @NotNull String newConsumerProguardFile) {
    myDslElement.replaceInListProperty(CONSUMER_PROGUARD_FILES, oldConsumerProguardFile, newConsumerProguardFile);
    return this;
  }

  @Nullable
  public String dimension() {
    return myDslElement.getProperty(DIMENSION, String.class);
  }

  @NotNull
  public ProductFlavorModel setDimension(@NotNull String dimension) {
    myDslElement.setLiteralProperty(DIMENSION, dimension);
    return this;
  }

  @NotNull
  public ProductFlavorModel removeDimension() {
    myDslElement.removeProperty(DIMENSION);
    return this;
  }

  @Nullable
  public Map<String, Object> manifestPlaceholders() {
    return myDslElement.getMapProperty(MANIFEST_PLACEHOLDERS, Object.class);
  }

  @NotNull
  public ProductFlavorModel setManifestPlaceholder(@NotNull String name, @NotNull String value) {
    myDslElement.setInMapProperty(MANIFEST_PLACEHOLDERS, name, value);
    return this;
  }

  @NotNull
  public ProductFlavorModel setManifestPlaceholder(@NotNull String name, int value) {
    myDslElement.setInMapProperty(MANIFEST_PLACEHOLDERS, name, value);
    return this;
  }

  @NotNull
  public ProductFlavorModel setManifestPlaceholder(@NotNull String name, boolean value) {
    myDslElement.setInMapProperty(MANIFEST_PLACEHOLDERS, name, value);
    return this;
  }

  @NotNull
  public ProductFlavorModel removeManifestPlaceholder(@NotNull String name) {
    myDslElement.removeFromMapProperty(MANIFEST_PLACEHOLDERS, name);
    return this;
  }

  @NotNull
  public ProductFlavorModel removeAllManifestPlaceholders() {
    myDslElement.removeProperty(MANIFEST_PLACEHOLDERS);
    return this;
  }

  @Nullable
  public Integer maxSdkVersion() {
    return myDslElement.getProperty(MAX_SDK_VERSION, Integer.class);
  }

  @NotNull
  public ProductFlavorModel setMaxSdkVersion(int maxSdkVersion) {
    myDslElement.setLiteralProperty(MAX_SDK_VERSION, maxSdkVersion);
    return this;
  }

  @NotNull
  public ProductFlavorModel removeMaxSdkVersion() {
    myDslElement.removeProperty(MAX_SDK_VERSION);
    return this;
  }

  @Nullable
  public String minSdkVersion() {
    Integer intValue = myDslElement.getProperty(MIN_SDK_VERSION, Integer.class);
    return intValue != null ? intValue.toString() : myDslElement.getProperty(MIN_SDK_VERSION, String.class);
  }

  @NotNull
  public ProductFlavorModel setMinSdkVersion(int minSdkVersion) {
    myDslElement.setLiteralProperty(MIN_SDK_VERSION, minSdkVersion);
    return this;
  }

  @NotNull
  public ProductFlavorModel setMinSdkVersion(@NotNull String minSdkVersion) {
    myDslElement.setLiteralProperty(MIN_SDK_VERSION, minSdkVersion);
    return this;
  }

  @NotNull
  public ProductFlavorModel removeMinSdkVersion() {
    myDslElement.removeProperty(MIN_SDK_VERSION);
    return this;
  }

  @Nullable
  public Boolean multiDexEnabled() {
    return myDslElement.getProperty(MULTI_DEX_ENABLED, Boolean.class);
  }

  @NotNull
  public ProductFlavorModel setMultiDexEnabled(boolean multiDexEnabled) {
    myDslElement.setLiteralProperty(MULTI_DEX_ENABLED, multiDexEnabled);
    return this;
  }

  @NotNull
  public ProductFlavorModel removeMultiDexEnabled() {
    myDslElement.removeProperty(MULTI_DEX_ENABLED);
    return this;
  }

  @Nullable
  public List<String> proguardFiles() {
    return myDslElement.getListProperty(PROGUARD_FILES, String.class);
  }

  @NotNull
  public ProductFlavorModel addProguardFile(@NotNull String proguardFile) {
    myDslElement.addToListProperty(PROGUARD_FILES, proguardFile);
    return this;
  }

  @NotNull
  public ProductFlavorModel removeProguardFile(@NotNull String proguardFile) {
    myDslElement.removeFromListProperty(PROGUARD_FILES, proguardFile);
    return this;
  }

  @NotNull
  public ProductFlavorModel removeAllProguardFiles() {
    myDslElement.removeProperty(PROGUARD_FILES);
    return this;
  }

  @NotNull
  public ProductFlavorModel replaceProguardFile(@NotNull String oldProguardFile, @NotNull String newProguardFile) {
    myDslElement.replaceInListProperty(PROGUARD_FILES, oldProguardFile, newProguardFile);
    return this;
  }

  @Nullable
  public List<String> resConfigs() {
    return myDslElement.getListProperty(RES_CONFIGS, String.class);
  }

  @NotNull
  public ProductFlavorModel addResConfig(@NotNull String resConfig) {
    myDslElement.addToListProperty(RES_CONFIGS, resConfig);
    return this;
  }

  @NotNull
  public ProductFlavorModel removeResConfig(@NotNull String resConfig) {
    myDslElement.removeFromListProperty(RES_CONFIGS, resConfig);
    return this;
  }

  @NotNull
  public ProductFlavorModel removeAllResConfigs() {
    myDslElement.removeProperty(RES_CONFIGS);
    return this;
  }

  @NotNull
  public ProductFlavorModel replaceResConfig(@NotNull String oldResConfig, @NotNull String newResConfig) {
    myDslElement.replaceInListProperty(RES_CONFIGS, oldResConfig, newResConfig);
    return this;
  }

  @Nullable
  public List<ResValue> resValues() {
    GradleDslElementList resValues = myDslElement.getProperty(RES_VALUES, GradleDslElementList.class);
    if (resValues == null) {
      return null;
    }

    List<ResValue> result = Lists.newArrayList();
    for (GradleDslElement resValue : resValues.getElements()) {
      if (resValue instanceof GradleDslLiteralList) {
        GradleDslLiteralList listElement = (GradleDslLiteralList)resValue;
        List<String> values = listElement.getValues(String.class);
        if (values.size() == 3) {
          result.add(new ResValue(values.get(0), values.get(1), values.get(2)));
        }
      }
    }
    return result;
  }

  @NotNull
  public ProductFlavorModel addResValue(@NotNull ResValue resValue) {
    GradleDslElementList elementList = myDslElement.getProperty(RES_VALUES, GradleDslElementList.class);
    if (elementList == null) {
      elementList = new GradleDslElementList(myDslElement, RES_VALUES);
      myDslElement.setNewElement(RES_VALUES, elementList);
    }
    elementList.addNewElement(resValue.toLiteralListElement(myDslElement));
    return this;
  }

  @NotNull
  public ProductFlavorModel removeResValue(@NotNull ResValue resValue) {
    GradleDslElementList elementList = myDslElement.getProperty(RES_VALUES, GradleDslElementList.class);
    if (elementList != null) {
      for (GradleDslLiteralList element : elementList.getElements(GradleDslLiteralList.class)) {
        List<String> values = element.getValues(String.class);
        if (values.size() == 3
            && resValue.type().equals(values.get(0)) && resValue.name().equals(values.get(1)) && resValue.value().equals(values.get(2))) {
          elementList.removeElement(element);
        }
      }
    }
    return this;
  }

  @NotNull
  public ProductFlavorModel removeAllResValues() {
    myDslElement.removeProperty(RES_VALUES);
    return this;
  }

  @NotNull
  public ProductFlavorModel replaceResValue(@NotNull ResValue oldResValue, @NotNull ResValue newResValue) {
    GradleDslElementList elementList = myDslElement.getProperty(RES_VALUES, GradleDslElementList.class);
    if (elementList != null) {
      for (GradleDslLiteralList element : elementList.getElements(GradleDslLiteralList.class)) {
        List<GradleDslLiteral> gradleDslLiterals = element.getElements();
        if (gradleDslLiterals.size() == 3
            && oldResValue.type().equals(gradleDslLiterals.get(0).getValue())
            && oldResValue.name().equals(gradleDslLiterals.get(1).getValue())
            && oldResValue.value().equals(gradleDslLiterals.get(2).getValue())) {
          gradleDslLiterals.get(0).setValue(newResValue.type());
          gradleDslLiterals.get(1).setValue(newResValue.name());
          gradleDslLiterals.get(2).setValue(newResValue.value());
        }
      }
    }
    return this;
  }

  @Nullable
  public String targetSdkVersion() {
    Integer intValue = myDslElement.getProperty(TARGET_SDK_VERSION, Integer.class);
    return intValue != null ? intValue.toString() : myDslElement.getProperty(TARGET_SDK_VERSION, String.class);
  }

  @NotNull
  public ProductFlavorModel setTargetSdkVersion(int targetSdkVersion) {
    myDslElement.setLiteralProperty(TARGET_SDK_VERSION, targetSdkVersion);
    return this;
  }

  @NotNull
  public ProductFlavorModel setTargetSdkVersion(@NotNull String targetSdkVersion) {
    myDslElement.setLiteralProperty(TARGET_SDK_VERSION, targetSdkVersion);
    return this;
  }

  @NotNull
  public ProductFlavorModel removeTargetSdkVersion() {
    myDslElement.removeProperty(TARGET_SDK_VERSION);
    return this;
  }

  @Nullable
  public String testApplicationId() {
    return myDslElement.getProperty(TEST_APPLICATION_ID, String.class);
  }

  @NotNull
  public ProductFlavorModel setTestApplicationId(@NotNull String testApplicationId) {
    myDslElement.setLiteralProperty(TEST_APPLICATION_ID, testApplicationId);
    return this;
  }

  @NotNull
  public ProductFlavorModel removeTestApplicationId() {
    myDslElement.removeProperty(TEST_APPLICATION_ID);
    return this;
  }

  @Nullable
  public Boolean testFunctionalTest() {
    return myDslElement.getProperty(TEST_FUNCTIONAL_TEST, Boolean.class);
  }

  @NotNull
  public ProductFlavorModel setTestFunctionalTest(boolean testFunctionalTest) {
    myDslElement.setLiteralProperty(TEST_FUNCTIONAL_TEST, testFunctionalTest);
    return this;
  }

  @NotNull
  public ProductFlavorModel removeTestFunctionalTest() {
    myDslElement.removeProperty(TEST_FUNCTIONAL_TEST);
    return this;
  }

  @Nullable
  public Boolean testHandleProfiling() {
    return myDslElement.getProperty(TEST_HANDLE_PROFILING, Boolean.class);
  }

  @NotNull
  public ProductFlavorModel setTestHandleProfiling(boolean testHandleProfiling) {
    myDslElement.setLiteralProperty(TEST_HANDLE_PROFILING, testHandleProfiling);
    return this;
  }

  @NotNull
  public ProductFlavorModel removeTestHandleProfiling() {
    myDslElement.removeProperty(TEST_HANDLE_PROFILING);
    return this;
  }

  @Nullable
  public String testInstrumentationRunner() {
    return myDslElement.getProperty(TEST_INSTRUMENTATION_RUNNER, String.class);
  }

  @NotNull
  public ProductFlavorModel setTestInstrumentationRunner(@NotNull String testInstrumentationRunner) {
    myDslElement.setLiteralProperty(TEST_INSTRUMENTATION_RUNNER, testInstrumentationRunner);
    return this;
  }

  @NotNull
  public ProductFlavorModel removeTestInstrumentationRunner() {
    myDslElement.removeProperty(TEST_INSTRUMENTATION_RUNNER);
    return this;
  }

  @Nullable
  public Map<String, String> testInstrumentationRunnerArguments() {
    return myDslElement.getMapProperty(TEST_INSTRUMENTATION_RUNNER_ARGUMENTS, String.class);
  }

  @NotNull
  public ProductFlavorModel setTestInstrumentationRunnerArgument(@NotNull String name, @NotNull String value) {
    myDslElement.setInMapProperty(TEST_INSTRUMENTATION_RUNNER_ARGUMENTS, name, value);
    return this;
  }

  @NotNull
  public ProductFlavorModel removeTestInstrumentationRunnerArgument(@NotNull String name) {
    myDslElement.removeFromMapProperty(TEST_INSTRUMENTATION_RUNNER_ARGUMENTS, name);
    return this;
  }

  @NotNull
  public ProductFlavorModel removeAllTestInstrumentationRunnerArguments() {
    myDslElement.removeProperty(TEST_INSTRUMENTATION_RUNNER_ARGUMENTS);
    return this;
  }

  @Nullable
  public Boolean useJack() {
    return myDslElement.getProperty(USE_JACK, Boolean.class);
  }

  @NotNull
  public ProductFlavorModel setUseJack(boolean useJack) {
    myDslElement.setLiteralProperty(USE_JACK, useJack);
    return this;
  }

  @NotNull
  public ProductFlavorModel removeUseJack() {
    myDslElement.removeProperty(USE_JACK);
    return this;
  }

  @Nullable
  public String versionCode() {
    Integer intValue = myDslElement.getProperty(VERSION_CODE, Integer.class);
    return intValue != null ? intValue.toString() : myDslElement.getProperty(VERSION_CODE, String.class);
  }

  @NotNull
  public ProductFlavorModel setVersionCode(int versionCode) {
    myDslElement.setLiteralProperty(VERSION_CODE, versionCode);
    return this;
  }

  @NotNull
  public ProductFlavorModel setVersionCode(@NotNull String versionCode) {
    myDslElement.setLiteralProperty(VERSION_CODE, versionCode);
    return this;
  }

  @NotNull
  public ProductFlavorModel removeVersionCode() {
    myDslElement.removeProperty(VERSION_CODE);
    return this;
  }

  @Nullable
  public String versionName() {
    return myDslElement.getProperty(VERSION_NAME, String.class);
  }

  @NotNull
  public ProductFlavorModel setVersionName(@NotNull String versionName) {
    myDslElement.setLiteralProperty(VERSION_NAME, versionName);
    return this;
  }

  @NotNull
  public ProductFlavorModel removeVersionName() {
    myDslElement.removeProperty(VERSION_NAME);
    return this;
  }

  /**
   * Represents a {@code resValue} statement defined in the product flavor block of the Gradle file.
   */
  public static final class ResValue {
    @NotNull public static final String NAME = "resValue";

    @NotNull private final String myType;
    @NotNull private final String myName;
    @NotNull private final String myValue;

    public ResValue(@NotNull String type, @NotNull String name, @NotNull String value) {
      myType = type;
      myName = name;
      myValue = value;
    }

    @NotNull
    public String type() {
      return myType;
    }

    @NotNull
    public String name() {
      return myName;
    }

    @NotNull
    public String value() {
      return myValue;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(myType, myName, myValue);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof ResValue)) {
        return false;
      }

      ResValue other = (ResValue)o;
      return myType.equals(other.myType) && myName.equals(other.myName) && myValue.equals(other.myValue);
    }

    @Override
    public String toString() {
      return String.format("Type: %1$s, Name: %2$s, Value: %3$s", myType, myName, myValue);
    }

    @NotNull
    private GradleDslLiteralList toLiteralListElement(@NotNull GradleDslElement parent) {
      GradleDslLiteral typeElement = new GradleDslLiteral(parent, NAME);
      typeElement.setValue(myType);
      GradleDslLiteral nameElement = new GradleDslLiteral(parent, NAME);
      nameElement.setValue(myName);
      GradleDslLiteral valueElement = new GradleDslLiteral(parent, NAME);
      valueElement.setValue(myValue);

      GradleDslLiteralList gradleDslLiteralList = new GradleDslLiteralList(parent, NAME);
      gradleDslLiteralList.add(typeElement, nameElement, valueElement);
      return gradleDslLiteralList;
    }
  }
}
