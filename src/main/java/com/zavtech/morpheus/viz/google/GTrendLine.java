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

import java.awt.Color;

import com.zavtech.morpheus.viz.chart.xy.XyTrend;

/**
 * A class used to expose trend line information to the Google charting framework
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
class GTrendLine implements XyTrend {

    private Comparable seriesKey;
    private Color color = Color.BLACK;
    private float lineWidth = 2f;

    /**
     * Constructor
     * @param seriesKey         the series key
     */
    GTrendLine(Comparable seriesKey) {
        this.seriesKey = seriesKey;
    }

    @Override
    public Comparable seriesKey() {
        return seriesKey;
    }

    @Override
    public XyTrend clear() {
        return this;
    }

    @Override
    public XyTrend withColor(Color color) {
        this.color = color;
        return this;
    }

    @Override
    public XyTrend withLineWidth(float width) {
        this.lineWidth = width;
        return this;
    }

    /**
     * Returns the line color
     * @return  the line color
     */
    public Color getColor() {
        return color;
    }

    /**
     * Returns the line width
     * @return  the line width
     */
    float getLineWidth() {
        return lineWidth;
    }
}
