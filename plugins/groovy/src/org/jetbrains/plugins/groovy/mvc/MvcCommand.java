/*
 * Copyright 2000-2016 JetBrains s.r.o.
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
package org.jetbrains.plugins.groovy.mvc;

import com.intellij.execution.configurations.ParametersList;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Sergey Evdokimov
 */
public class MvcCommand {

  public static final Collection<String> ourEnvironments = Arrays.asList("prod", "test", "dev");

  private final Map<String, String> myEnvVariables = ContainerUtil.newHashMap();
  private boolean myPassParentEnvs = true;

  private @Nullable String myEnv;
  private @Nullable String myCommand;
  private @Nullable String myVmOptions;

  private final ArrayList<String> myArgs = new ArrayList<String>();
  private final ArrayList<String> myProperties = new ArrayList<String>();

  public MvcCommand() {
  }

  public MvcCommand(@Nullable String command, String... args) {
    myCommand = command;
    Collections.addAll(myArgs, args);
  }

  @Nullable
  public String getEnv() {
    return myEnv;
  }

  @Nullable
  public String getCommand() {
    return myCommand;
  }

  @Nullable
  public String getVmOptions() {
    return myVmOptions;
  }

  public MvcCommand setVmOptions(@Nullable String vmOptions) {
    myVmOptions = vmOptions;
    return this;
  }

  /**
   * @return MODIFIABLE map of environment variables
   */
  @NotNull
  public Map<String, String> getEnvVariables() {
    return myEnvVariables;
  }

  public MvcCommand setEnvVariables(@NotNull Map<String, String> envVariables) {
    if (myEnvVariables != envVariables) {
      myEnvVariables.clear();
      myEnvVariables.putAll(envVariables);
    }
    return this;
  }

  public boolean isPassParentEnvs() {
    return myPassParentEnvs;
  }

  public MvcCommand setPassParentEnvs(boolean passParentEnv) {
    myPassParentEnvs = passParentEnv;
    return this;
  }


  /**
   * @return MODIFIABLE list of arguments
   */
  public ArrayList<String> getArgs() {
    return myArgs;
  }

  public void setArgs(@NotNull List<String> args) {
    if (args == myArgs) return;

    myArgs.clear();
    myArgs.addAll(args);
  }

  /**
   * @return MODIFIABLE list of system properties definition written before command (e.g. -Dgrails.port=9090 run-app)
   */
  public ArrayList<String> getProperties() {
    return myProperties;
  }

  public void setProperties(@NotNull List<String> properties) {
    if (myProperties == properties) return;

    myProperties.clear();
    myProperties.addAll(properties);
  }

  public void addToParametersList(@NotNull ParametersList list) {
    if (myEnv != null) {
      list.add(myEnv);
    }

    list.addAll(myProperties);

    if (myCommand != null) {
      list.add(myCommand);
    }

    list.addAll(myArgs);
  }

  @NotNull
  public static MvcCommand parse(@NotNull String cmd) {
    String[] args = ParametersList.parse(cmd);

    MvcCommand res = new MvcCommand();

    int i = 0;

    while (res.myCommand == null && i < args.length) {
      String s = args[i];

      if (s.startsWith("-D")) {
        res.myProperties.add(s);
      }
      else if (res.myEnv == null && ourEnvironments.contains(s)) {
        res.myEnv = s;
      }
      else {
        res.myCommand = s;
      }

      i++;
    }

    res.myArgs.addAll(Arrays.asList(args).subList(i, args.length));

    return res;
  }
}