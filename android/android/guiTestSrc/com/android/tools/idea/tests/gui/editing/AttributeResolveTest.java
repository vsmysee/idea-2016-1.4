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
package com.android.tools.idea.tests.gui.editing;

import com.android.tools.idea.tests.gui.framework.GuiTestCase;
import com.android.tools.idea.tests.gui.framework.IdeGuiTest;
import com.android.tools.idea.tests.gui.framework.fixture.EditorFixture;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static com.android.SdkConstants.FN_BUILD_GRADLE;
import static com.intellij.openapi.util.io.FileUtil.appendToFile;
import static com.intellij.openapi.util.io.FileUtil.join;
import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class AttributeResolveTest extends GuiTestCase {

  @Test @IdeGuiTest
  public void testResolveNewlyAddedTag() throws IOException {
    myProjectFrame = importProjectAndWaitForProjectSyncToFinish("LayoutTest");
    EditorFixture editor = myProjectFrame.getEditor();

    // TODO add dependency using new parser API
    File appBuildFile = new File(myProjectFrame.getProjectPath(), join("app", FN_BUILD_GRADLE));
    assertThat(appBuildFile).isFile();
    appendToFile(appBuildFile, "\ndependencies { compile 'com.android.support:cardview-v7:22.1.1' }\n");
    myProjectFrame.requestProjectSync();

    editor.open("app/src/main/res/layout/layout2.xml", EditorFixture.Tab.EDITOR);
    editor.moveTo(editor.findOffset("^<TextView"));
    editor.enterText("<android.support.v7.widget.CardView android:onClick=\"onCreate\" /\n");
    editor.moveTo(editor.findOffset("on^Create"));

    myProjectFrame.waitForBackgroundTasksToFinish();

    editor.invokeAction(EditorFixture.EditorAction.GOTO_DECLARATION);

    assertEquals("MyActivity.java", editor.getCurrentFileName());
  }
}
