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

package com.android.build.gradle.internal.scope;

import static com.android.builder.core.BuilderConstants.FD_REPORTS;
import static com.android.builder.model.AndroidProject.FD_GENERATED;
import static com.android.builder.model.AndroidProject.FD_LOGS;
import static com.android.builder.model.AndroidProject.FD_INTERMEDIATES;
import static com.android.builder.model.AndroidProject.FD_OUTPUTS;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.build.gradle.AndroidConfig;
import com.android.build.gradle.AndroidGradleOptions;
import com.android.build.gradle.internal.SdkHandler;
import com.android.builder.core.AndroidBuilder;
import com.android.builder.model.AndroidProject;
import com.google.common.base.CharMatcher;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import org.gradle.api.Project;
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry;

import java.io.File;

/**
 * A scope containing data for the Android plugin.
 */
public class GlobalScope {
    @NonNull
    private Project project;
    @NonNull
    private AndroidBuilder androidBuilder;
    @NonNull
    private AndroidConfig extension;
    @NonNull
    private SdkHandler sdkHandler;
    @NonNull
    private ToolingModelBuilderRegistry toolingRegistry;

    @NonNull
    private final File intermediatesDir;
    @NonNull
    private final File generatedDir;
    @NonNull
    private final File reportsDir;
    @NonNull
    private final File outputsDir;
    @Nullable
    private File mockableAndroidJarFile;

    public GlobalScope(
            @NonNull Project project,
            @NonNull AndroidBuilder androidBuilder,
            @NonNull AndroidConfig extension,
            @NonNull SdkHandler sdkHandler,
            @NonNull ToolingModelBuilderRegistry toolingRegistry) {
        this.project = project;
        this.androidBuilder = androidBuilder;
        this.extension = extension;
        this.sdkHandler = sdkHandler;
        this.toolingRegistry = toolingRegistry;
        intermediatesDir = new File(getBuildDir(), FD_INTERMEDIATES);
        generatedDir = new File(getBuildDir(), FD_GENERATED);
        reportsDir = new File(getBuildDir(), FD_REPORTS);
        outputsDir = new File(getBuildDir(), FD_OUTPUTS);
    }

    @NonNull
    public Project getProject() {
        return project;
    }

    @NonNull
    public AndroidConfig getExtension() {
        return extension;
    }

    @NonNull
    public AndroidBuilder getAndroidBuilder() {
        return androidBuilder;
    }

    @NonNull
    public String getProjectBaseName() {
        return (String) project.property("archivesBaseName");
    }

    @NonNull
    public SdkHandler getSdkHandler() {
        return sdkHandler;
    }

    @NonNull
    public ToolingModelBuilderRegistry getToolingRegistry() {
        return toolingRegistry;
    }

    @NonNull
    public File getBuildDir() {
        return project.getBuildDir();
    }

    @NonNull
    public File getIntermediatesDir() {
        return intermediatesDir;
    }

    @NonNull
    public File getGeneratedDir() {
        return generatedDir;
    }

    @NonNull
    public File getReportsDir() {
        return reportsDir;
    }

    public File getTestResultsFolder() {
        return new File(getBuildDir(), "test-results");
    }

    public File getTestReportFolder() {
        return new File(getBuildDir(), "reports/tests");
    }

    @NonNull
    public File getMockableAndroidJarFile() {

        if (mockableAndroidJarFile == null) {
            // Since the file ends up in $rootProject.buildDir, it will survive clean
            // operations - projects generated by AS don't have a top-level clean task that
            // would delete the top-level build directory. This means that the name has to
            // encode all the necessary information, otherwise the task will be UP-TO-DATE
            // even if the file should be regenerated. That's why we put the SDK version and
            // "default-values" in there, so if one project uses the returnDefaultValues flag,
            // it will just generate a new file and not change the semantics for other
            // sub-projects. There's an implicit "v1" there as well, if we ever change the
            // generator logic, the names will have to be changed.
            String fileExt;
            if (getExtension().getTestOptions().getUnitTests().isReturnDefaultValues()) {
                fileExt = ".default-values.jar";
            } else {
                fileExt = ".jar";
            }
            File outDir = new File(
                    getProject().getRootProject().getBuildDir(),
                    AndroidProject.FD_GENERATED);

            CharMatcher safeCharacters =
                    CharMatcher.JAVA_LETTER_OR_DIGIT.or(CharMatcher.anyOf("-."));
            String sdkName =
                    safeCharacters.negate().replaceFrom(
                            getExtension().getCompileSdkVersion(), '-');
            mockableAndroidJarFile = new File(outDir, "mockable-" + sdkName + fileExt);
        }
        return mockableAndroidJarFile;
    }

    @NonNull
    public File getOutputsDir() {
        return outputsDir;
    }

    @NonNull
    public String getDefaultApkLocation() {
        return getBuildDir() + "/" + FD_OUTPUTS + "/apk";
    }

    @NonNull
    public String getApkLocation() {
        return Objects.firstNonNull(
                AndroidGradleOptions.getApkLocation(project),
                getDefaultApkLocation());
    }
}
