/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

/*
 * Created by IntelliJ IDEA.
 * User: yole
 * Date: 15.11.2006
 * Time: 17:53:24
 */
package com.intellij.openapi.diff.impl.patch;

import com.intellij.openapi.util.text.StringUtil;

import java.util.Arrays;

public abstract class FilePatch {
  private String myBeforeName;
  private String myAfterName;
  private String myBeforeVersionId;
  private String myAfterVersionId;
  private String myBaseRevisionText;

  public String getBeforeName() {
    return myBeforeName;
  }

  public String getAfterName() {
    return myAfterName;
  }

  public String getBeforeFileName() {
    String[] pathNameComponents = myBeforeName.split("/");
    return pathNameComponents [pathNameComponents.length-1];
  }

  public String getAfterFileName() {
    String[] pathNameComponents = myAfterName.split("/");
    return pathNameComponents [pathNameComponents.length-1];
  }

  public void setBeforeName(final String fileName) {
    myBeforeName = fileName;  
  }

  public void setAfterName(final String fileName) {
    myAfterName = fileName;
  }

  public String getBeforeVersionId() {
    return myBeforeVersionId;
  }

  public void setBeforeVersionId(final String beforeVersionId) {
    myBeforeVersionId = beforeVersionId;
  }

  public String getAfterVersionId() {
    return myAfterVersionId;
  }

  public void setAfterVersionId(final String afterVersionId) {
    myAfterVersionId = afterVersionId;
  }

  public String getAfterNameRelative(int skipDirs) {
    String[] components = myAfterName.split("/");
    return StringUtil.join(components, skipDirs, components.length, "/");
  }
  
  public String getBaseRevisionText() {
    return myBaseRevisionText;
  }

  public void setBaseRevisionText(String baseRevisionText) {
    myBaseRevisionText = baseRevisionText;
  }

  public abstract boolean isNewFile();

  public abstract boolean isDeletedFile();

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    FilePatch patch = (FilePatch)o;

    if (myBeforeName != null ? !myBeforeName.equals(patch.myBeforeName) : patch.myBeforeName != null) return false;
    if (myAfterName != null ? !myAfterName.equals(patch.myAfterName) : patch.myAfterName != null) return false;
    if (myBeforeVersionId != null ? !myBeforeVersionId.equals(patch.myBeforeVersionId) : patch.myBeforeVersionId != null) return false;
    if (myAfterVersionId != null ? !myAfterVersionId.equals(patch.myAfterVersionId) : patch.myAfterVersionId != null) return false;
    if (myBaseRevisionText != null ? !myBaseRevisionText.equals(patch.myBaseRevisionText) : patch.myBaseRevisionText != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(new Object[]{myBeforeName, myAfterName, myBeforeVersionId, myAfterVersionId, myBaseRevisionText});
  }
}