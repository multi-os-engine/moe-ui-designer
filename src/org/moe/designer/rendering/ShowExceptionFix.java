/*
 * Copyright (C) 2013 The Android Open Source Project
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

package org.moe.designer.rendering;

import com.intellij.openapi.project.Project;
//import org.jetbrains.android.util.AndroidUtils;
import org.jetbrains.annotations.NotNull;

public class ShowExceptionFix implements Runnable {
  @NotNull private final Project myProject;
  @NotNull private final Throwable myThrowable;

  public ShowExceptionFix(@NotNull Project project, @NotNull Throwable throwable) {
    myProject = project;
    myThrowable = throwable;
  }

  @Override
  public void run() {
    Throwable t = myThrowable;
    while (t.getCause() != null && t.getCause() != t) {
      t = t.getCause();
    }
//    AndroidUtils.showStackStace(myProject, new Throwable[]{t});
  }
}
