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
package com.zavtech.morpheus.viz.google;

import java.awt.*;
import java.util.function.Consumer;

import com.zavtech.morpheus.viz.chart.ChartTextStyle;
import com.zavtech.morpheus.viz.js.JsObject;

/**
 * Class summary goes here...
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
class GChartTextStyle implements ChartTextStyle, Consumer<JsObject> {

    private GChartLabel label = new GChartLabel();

    /**
     * Constructor
     * @param color     the color
     * @param font      the font
     */
    GChartTextStyle(Color color, Font font) {
        this.label.withColor(color).withFont(font);
    }

    /**
     * Returns the label
     * @return  the label
     */
    GChartLabel label() {
        return label;
    }

    @Override
    public ChartTextStyle withColor(Color color) {
        this.label.withColor(color);
        return this;
    }

    @Override
    public ChartTextStyle withFont(Font font) {
        this.label.withFont(font);
        return this;
    }

    @Override
    public void accept(JsObject object) {
        label.accept(object);
    }


}
