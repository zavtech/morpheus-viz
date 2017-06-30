/**
 * Copyright (C) 2014-2017 Xavier Witdouck
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
package com.zavtech.morpheus.viz.chart;

import java.awt.Color;
import java.awt.Font;

public interface ChartTextStyle<T extends ChartTextStyle> {

    /**
     * Sets the text color
     * @param color color value
     */
    T withColor(Color color);

    /**
     * Sets the font for this text style
     * @param font  sets the font for text
     */
    T withFont(Font font);
}
