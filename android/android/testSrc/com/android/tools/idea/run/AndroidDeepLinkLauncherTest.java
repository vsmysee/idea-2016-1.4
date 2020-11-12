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
package com.android.tools.idea.run;

import junit.framework.TestCase;

public class AndroidDeepLinkLauncherTest extends TestCase {
  public void testAmStartCommandForDeepLink() {
    assertEquals("am start  -a android.intent.action.VIEW -c android.intent.category.BROWSABLE -d example://host/path",
                 AndroidDeepLinkLauncher.getLaunchDeepLinkCommand("example://host/path", null, "", ""));
    assertEquals("am start -D -a android.intent.action.VIEW -c android.intent.category.BROWSABLE -d example://host/path",
                 AndroidDeepLinkLauncher.getLaunchDeepLinkCommand("example://host/path", null, "-D", ""));
    assertEquals("am start -D -a android.intent.action.VIEW -c android.intent.category.BROWSABLE -d example://host/path --es aa 'bb'",
                 AndroidDeepLinkLauncher.getLaunchDeepLinkCommand("example://host/path", null, "-D", "--es aa 'bb'"));
    assertEquals("am start  -a android.intent.action.VIEW -c android.intent.category.BROWSABLE -d example://host/path com.example",
                 AndroidDeepLinkLauncher.getLaunchDeepLinkCommand("example://host/path", "com.example", "", ""));
  }
}
