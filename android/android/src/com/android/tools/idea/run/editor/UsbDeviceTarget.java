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

import com.android.ddmlib.IDevice;
import com.android.tools.idea.run.*;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;

public class UsbDeviceTarget extends DeployTarget {
  public static final class State extends DeployTargetState {
  }

  @NotNull
  @Override
  public String getId() {
    return TargetSelectionMode.USB_DEVICE.name();
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return "USB Device";
  }

  @NotNull
  @Override
  public DeployTargetState createState() {
    return new State();
  }

  @Override
  public DeployTargetConfigurable createConfigurable(@NotNull Project project,
                                                     Disposable parentDisposable,
                                                     @NotNull DeployTargetConfigurableContext context) {
    return new TargetConfigurable();
  }

  @Nullable
  @Override
  public DeviceTarget getTarget(@NotNull DeployTargetState state,
                                @NotNull AndroidFacet facet,
                                @NotNull DeviceCount deviceCount,
                                boolean debug,
                                int runConfigId,
                                @NotNull ConsolePrinter printer) {
    Collection<IDevice> runningDevices =
      DeviceSelectionUtils.chooseRunningDevice(facet, new TargetDeviceFilter.UsbDeviceFilter(), deviceCount);
    if (runningDevices == null) {
      // The user canceled.
      return null;
    }
    return DeviceTarget.forDevices(runningDevices);
  }

  private static class TargetConfigurable implements DeployTargetConfigurable<State> {
    @Nullable
    @Override
    public JComponent createComponent() {
      return null;
    }

    @Override
    public void resetFrom(@NotNull State state, int uniqueID) {
    }

    @Override
    public void applyTo(@NotNull State state, int uniqueID) {
    }
  }
}
