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

package com.android.build.gradle.integration.component

import com.android.build.gradle.integration.common.category.DeviceTests
import com.android.build.gradle.integration.common.fixture.GradleTestProject
import groovy.transform.CompileStatic
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.ClassRule
import org.junit.Test
import org.junit.experimental.categories.Category

/**
 * Assemble tests for ndkVariants.
 */
@CompileStatic
class NdkVariantsTest {

    @ClassRule
    static public GradleTestProject project = GradleTestProject.builder()
            .forExperimentalPlugin(true)
            .fromTestProject("ndkVariants")
            .create()

    @BeforeClass
    static void setUp() {
        project.execute("clean", "assembleDebug");
    }

    @AfterClass
    static void cleanUp() {
        project = null
    }

    @Test
    void lint() {
        project.execute("lint")
    }

    @Test
    @Category(DeviceTests.class)
    public void connnectedAndroidTest() {
        project.executeConnectedCheck();
    }
}
