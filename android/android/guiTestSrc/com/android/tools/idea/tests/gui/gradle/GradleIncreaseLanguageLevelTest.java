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
package com.android.tools.idea.tests.gui.gradle;

import com.android.tools.idea.tests.gui.framework.BelongsToTestGroups;
import com.android.tools.idea.tests.gui.framework.GuiTestCase;
import com.android.tools.idea.tests.gui.framework.IdeGuiTest;
import com.android.tools.idea.tests.gui.framework.IdeGuiTestSetup;
import com.android.tools.idea.tests.gui.framework.fixture.EditorFixture;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static com.android.tools.idea.tests.gui.framework.TestGroup.PROJECT_SUPPORT;
import static com.android.tools.idea.tests.gui.framework.fixture.EditorFixture.EditorAction.UNDO;
import static com.intellij.lang.annotation.HighlightSeverity.ERROR;

// Temporarily disabled due to https://code.google.com/p/android/issues/detail?id=183801
@Ignore
@BelongsToTestGroups({PROJECT_SUPPORT})
@IdeGuiTestSetup(skipSourceGenerationOnSync = true)
public class GradleIncreaseLanguageLevelTest extends GuiTestCase {
  @Test @IdeGuiTest
  public void testIncreaseLanguageLevel() throws IOException {
    myProjectFrame = importProjectAndWaitForProjectSyncToFinish("MultiModule");
    EditorFixture editor = myProjectFrame.getEditor();
    editor.open("library/src/androidTest/java/com/android/library/ApplicationTest.java");
    editor.moveTo(editor.findOffset("super(Application.class);^"));
    editor.enterText("\nfloat x = 1_000;");
    editor.moveTo(editor.findOffset("1_0^00;"));
    editor.waitForCodeAnalysisHighlightCount(ERROR, 1);
    editor.invokeIntentionAction("Set language level to 7");
    editor.waitForCodeAnalysisHighlightCount(ERROR, 0);

    myProjectFrame.getEditor().invokeAction(UNDO);
    myProjectFrame.waitForGradleProjectSyncToFinish();
    myProjectFrame.findMessageDialog("Undo").clickOk();
    editor.waitForCodeAnalysisHighlightCount(ERROR, 1);
  }
}
