/*
 * Copyright 2000-2010 JetBrains s.r.o.
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
package com.intellij.execution;

import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import org.jetbrains.annotations.NotNull;

/**
 * @author nik
 */
public class ExecutionAdapter implements ExecutionListener {
  @Override
  public void processStartScheduled(String executorId, ExecutionEnvironment env) {
  }

  @Override
  public void processStarting(String executorId, @NotNull ExecutionEnvironment env) {
  }

  @Override
  public void processNotStarted(String executorId, @NotNull ExecutionEnvironment env) {
  }

  @Override
  public void processStarted(String executorId, @NotNull ExecutionEnvironment env, @NotNull ProcessHandler handler) {
  }

  @Override
  public void processTerminating(@NotNull RunProfile runProfile, @NotNull ProcessHandler handler) {
  }

  @Override
  public void processTerminated(@NotNull RunProfile runProfile, @NotNull ProcessHandler handler) {
  }
}
