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
package org.moe.designer.designSurface.graphics;

import java.awt.*;

public class LineInsertFeedback extends com.intellij.designer.designSurface.feedbacks.LineInsertFeedback {
  private final DrawingStyle myStyle;

  public LineInsertFeedback(DrawingStyle style, boolean horizontal) {
    super(Color.RED /* should not be used */, horizontal);
    myStyle = style;
  }

  @Override
  protected void paintHorizontal(Graphics g, Dimension size) {
    int lineWidth = myStyle.getLineWidth();
    int middle = lineWidth / 2;
    DesignerGraphics.drawLine(myStyle, g, 0, middle, size.width, middle);
  }

  @Override
  protected void paintVertical(Graphics g, Dimension size) {
    int lineWidth = myStyle.getLineWidth();
    int middle = lineWidth / 2;
    DesignerGraphics.drawLine(myStyle, g, middle, 0, middle, size.height);
  }
}