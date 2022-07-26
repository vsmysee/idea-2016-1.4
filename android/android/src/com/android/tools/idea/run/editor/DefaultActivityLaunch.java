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
package com.android.tools.idea.run.editor;

import com.android.tools.idea.run.AndroidApplicationLauncher;
import com.android.tools.idea.run.AndroidRunConfiguration;
import com.android.tools.idea.run.ValidationError;
import com.android.tools.idea.run.activity.ActivityLocator;
import com.android.tools.idea.run.activity.AndroidActivityLauncher;
import com.android.tools.idea.run.activity.DefaultActivityLocator;
import com.android.tools.idea.run.activity.MavenDefaultActivityLocator;
import com.google.common.collect.ImmutableList;
import com.intellij.openapi.project.Project;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public class DefaultActivityLaunch extends LaunchOption<DefaultActivityLaunch.State> {
  public static final DefaultActivityLaunch INSTANCE = new DefaultActivityLaunch();

  public static final class State extends LaunchOptionState {
    @Override
    public AndroidApplicationLauncher getLauncher(@NotNull AndroidFacet facet, @NotNull String extraAmOptions) {
      return new AndroidActivityLauncher(getActivityLocator(facet), extraAmOptions);
    }

    @NotNull
    @Override
    public List<ValidationError> checkConfiguration(@NotNull AndroidFacet facet) {
      try {
        getActivityLocator(facet).validate();
        return ImmutableList.of();
      }
      catch (ActivityLocator.ActivityLocatorException e) {
        // The launch will probably fail, but we allow the user to continue in case we are looking at stale data.
        return ImmutableList.of(ValidationError.warning(e.getMessage()));
      }
    }

    @NotNull
    private static ActivityLocator getActivityLocator(@NotNull AndroidFacet facet) {
      return facet.getProperties().USE_CUSTOM_COMPILER_MANIFEST
             ? new MavenDefaultActivityLocator(facet)
             : new DefaultActivityLocator(facet);
    }
  }

  @NotNull
  @Override
  public String getId() {
    return AndroidRunConfiguration.LAUNCH_DEFAULT_ACTIVITY;
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return "Default Activity";
  }

  @NotNull
  @Override
  public State createState() {
    // there is no state to save in this case
    return new State();
  }

  @NotNull
  @Override
  public LaunchOptionConfigurable<State> createConfigurable(@NotNull Project project, @NotNull LaunchOptionConfigurableContext context) {
    return new LaunchOptionConfigurable<State>() {
      @Nullable
      @Override
      public JComponent createComponent() {
        return null;
      }

      @Override
      public void resetFrom(@NotNull State state) {
      }

      @Override
      public void applyTo(@NotNull State state) {
      }
    };
  }
}
