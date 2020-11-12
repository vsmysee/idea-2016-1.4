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
package com.android.tools.idea.editors.gfxtrace.service.path;


import com.android.tools.rpclib.binary.BinaryObject;
import org.jetbrains.annotations.NotNull;

public abstract class Path implements BinaryObject {
  private String myString;

  public abstract Path getParent();

  @Override
  public String toString() {
    if (myString == null) {
      myString = stringPath(new StringBuilder()).toString();
    }
    return myString;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    // Default to string comparison if the implementations don't override
    return toString().equals(((Path)o).toString());
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  public abstract StringBuilder stringPath(StringBuilder builder);

  @NotNull
  public static Path wrap(BinaryObject object) {
    return (Path)object;
  }

  @NotNull
  public BinaryObject unwrap() {
    return this;
  }

  /**
   * as returns a path to the object this path refers to, but converted to type.
   * @param type The target type.
   * @return The path to the converted object.
   */
  public AsPath as(Object type) {
    AsPath p = new AsPath();
    p.setObject(this);
    p.setType(type);
    return p;
  }
}
