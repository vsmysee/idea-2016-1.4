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
package com.android.tools.idea.editors.gfxtrace.renderers;

import javax.swing.*;
import java.awt.*;

public class RenderUtils {

  public static void drawImage(Component c, Graphics g, Image image, int x, int y, int w, int h) {
    int imageWidth = image.getWidth(c), imageHeight = image.getHeight(c);
    float f = Math.min((float)w / imageWidth, (float)h / imageHeight);
    imageWidth = (int)(f * imageWidth);
    imageHeight = (int)(f * imageHeight);
    g.drawImage(image, x + (w - imageWidth) / 2, y + (h - imageHeight) / 2, imageWidth, imageHeight, c);
  }

  public static void drawIcon(Component c, Graphics g, Icon icon, int x, int y, int w, int h) {
    icon.paintIcon(c, g, x + (w - icon.getIconWidth()) / 2, y + (h - icon.getIconHeight()) / 2);
  }

  public static void drawCroppedImage(Component c, Graphics g, Image image, int x, int y, int w, int h) {
    int imageWidth = image.getWidth(c), imageHeight = image.getHeight(c);
    float f = Math.max((float)w / imageWidth, (float)h / imageHeight);
    int offsetX = (int)((-Math.min(0, w - (f * imageWidth)) / 2) / f);
    int offsetY = (int)((-Math.min(0, h - (int)(f * imageHeight)) / 2) / f);
    g.drawImage(image, x, y, x + w, y + h, offsetX, offsetY, imageWidth - offsetX, imageHeight - offsetY, c);
  }
}
