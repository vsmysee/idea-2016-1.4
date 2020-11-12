/*
 * Copyright 2000-2013 JetBrains s.r.o.
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
package org.jetbrains.idea.devkit.dom.impl;

import com.intellij.openapi.util.NullableLazyValue;
import com.intellij.openapi.util.VolatileNullableLazyValue;
import com.intellij.util.xml.DomUtil;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.devkit.dom.IdeaPlugin;

/**
 * @author S. Weinreuter
 */
public abstract class IdeaPluginImpl implements IdeaPlugin {

  private final NullableLazyValue<String> myPluginId = new VolatileNullableLazyValue<String>() {
    @Nullable
    @Override
    protected String compute() {
      String pluginId = null;
      if (DomUtil.hasXml(getId())) {
        pluginId = getId().getStringValue();
      }
      else if (DomUtil.hasXml(getName())) {
        pluginId = getName().getStringValue();
      }
      return pluginId != null ? pluginId.trim() : null;
    }
  };

  public String getPluginId() {
    return myPluginId.getValue();
  }
}
