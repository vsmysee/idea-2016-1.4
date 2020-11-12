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
package com.android.tools.idea.uibuilder.handlers.grid;

import com.android.annotations.NonNull;
import com.android.tools.idea.uibuilder.api.*;
import com.android.tools.idea.uibuilder.model.NlComponent;
import com.android.tools.idea.uibuilder.model.SegmentType;

import java.util.List;

public final class GridLayoutHandler extends ViewGroupHandler {
  @Override
  public DragHandler createDragHandler(@NonNull ViewEditor editor,
                                       @NonNull NlComponent layout,
                                       @NonNull List<NlComponent> components,
                                       @NonNull DragType type) {
    return new GridDragHandler(editor, this, layout, components, type);
  }

  @Override
  public ResizeHandler createResizeHandler(@NonNull ViewEditor editor,
                                           @NonNull NlComponent child,
                                           SegmentType horizontalEdgeType,
                                           SegmentType verticalEdgeType) {
    return new DefaultResizeHandler(editor, this, child, horizontalEdgeType, verticalEdgeType);
  }
}
