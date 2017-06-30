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

/**
 * A class used to expose trend line information to the Google charting framework
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public class GChartTrendLine {

    private int seriesIndex;
    private Color color;
    private int lineWidth;
    private double opacity;
    private boolean showR2;
    private boolean visibleInLegend;

    /**
     * Constructor
     * @param seriesIndex       the series index
     * @param color             the color for trend line
     * @param lineWidth         the line width
     * @param opacity           the line opacity
     * @param showR2            true to show R2 in tooltip
     * @param visibleInLegend   true to show trend line in legend
     */
    public GChartTrendLine(int seriesIndex, Color color, int lineWidth, double opacity, boolean showR2, boolean visibleInLegend) {
        this.seriesIndex = seriesIndex;
        this.color = color;
        this.lineWidth = lineWidth;
        this.opacity = opacity;
        this.showR2 = showR2;
        this.visibleInLegend = visibleInLegend;
    }

    /**
     * Returns the series index for this trend line
     * @return  the series index
     */
    public int getSeriesIndex() {
        return seriesIndex;
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
    public int getLineWidth() {
        return lineWidth;
    }

    /**
     * Returns the line opacity
     * @return  the line opacity
     */
    public double getOpacity() {
        return opacity;
    }

    /**
     * Returns true if R-squared should be shown
     * @return  true to display R-squared
     */
    public boolean isShowR2() {
        return showR2;
    }

    /**
     * Returns true if trend line should be shown in legend
     * @return  true to show trend line in legend
     */
    public boolean isVisibleInLegend() {
        return visibleInLegend;
    }

    /**
     * Returns the color as a HEX string
     * @return  the color string
     */
    public String getColorHex() {
        final int r = color.getRed();
        final int g = color.getGreen();
        final int b = color.getBlue();
        return String.format("#%02x%02x%02x", r, g, b);
    }

    @Override
    public String toString() {
        return "GChartTrendLine{" +
                "seriesIndex=" + seriesIndex +
                ", color=" + color +
                ", lineWidth=" + lineWidth +
                ", opacity=" + opacity +
                ", showR2=" + showR2 +
                ", visibleInLegend=" + visibleInLegend +
                '}';
    }
}
