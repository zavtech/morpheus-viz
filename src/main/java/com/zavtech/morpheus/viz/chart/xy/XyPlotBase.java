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
package com.zavtech.morpheus.viz.chart.xy;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import com.zavtech.morpheus.viz.chart.ChartShape;
import com.zavtech.morpheus.viz.util.ColorModel;

/**
 * A convenience base class implementation of XyPlot
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public abstract class XyPlotBase<X extends Comparable> implements XyPlot<X> {

    private ColorModel colorModel;
    private Map<Comparable,StyleAdapter> styleMap = new HashMap<>();

    /**
     * Constructor
     */
    protected XyPlotBase() {
        this.colorModel = ColorModel.DEFAULT.get();
    }

    /**
     * Returns a reference to the color model for this chart
     * @return      the color model for chart
     */
    public ColorModel getColorModel() {
        return colorModel;
    }


    @Override
    public XyPlot<X> withColorModel(ColorModel colorModel) {
        this.colorModel = colorModel;
        return this;
    }


    @Override
    public XyStyle style(Comparable seriesKey) {
        StyleAdapter style = styleMap.get(seriesKey);
        if (style == null) {
            style = new StyleAdapter();
            styleMap.put(seriesKey, style);
        }
        return style;
    }

    /**
     * Returns the series override color, or null if not set
     * @param seriesKey the series key
     * @return          the series color, or null
     */
    public Color getSeriesColor(Comparable seriesKey) {
        StyleAdapter adapter = styleMap.get(seriesKey);
        return adapter != null && adapter.color != null ? adapter.color : colorModel.getColor(seriesKey);
    }

    /**
     * Returns the series override line width, or null if not set
     * This is only relevant for series rendered with lines
     * @param seriesKey the series key
     * @return          the series line width, or null
     */
    public Float getSeriesLineWidth(Comparable seriesKey) {
        StyleAdapter adapter = styleMap.get(seriesKey);
        return adapter != null ? adapter.lineWidth : null;
    }

    /**
     * Returns the series override shape, or null if not set
     * This is only relevant for series rendered with points
     * @param seriesKey the series key
     * @return          the series shape, or null
     */
    public ChartShape getSeriesPointShape(Comparable seriesKey) {
        StyleAdapter adapter = styleMap.get(seriesKey);
        return adapter != null ? adapter.pointShape : null;
    }

    /**
     * Returns the series override point visibility, or null if not set
     * @param seriesKey the series key
     * @return          the series point visibility, or null
     */
    public Boolean isSeriesPointsVisible(Comparable seriesKey) {
        StyleAdapter adapter = styleMap.get(seriesKey);
        return adapter != null ? adapter.pointsVisible : null;
    }

    /**
     * Returns the series override to control whether line is dashed or not
     * @param seriesKey the series key
     * @return          true if the series line should be dashed
     */
    public Boolean isSeriesDashedLine(Comparable seriesKey) {
        StyleAdapter adapter = styleMap.get(seriesKey);
        return adapter != null ? adapter.dashes : null;
    }



    /**
     * A XyStyle adapter implementation
     */
    private class StyleAdapter implements XyStyle {

        private Color color;
        private Boolean dashes;
        private ChartShape pointShape;
        private Float lineWidth;
        private Boolean pointsVisible;

        /**
         * Constructor
         */
        private StyleAdapter() {
            super();
        }

        @Override
        public XyStyle withColor(Color color) {
            this.color = color;
            return this;
        }

        @Override
        public XyStyle withDashes(boolean dashed) {
            this.dashes = dashed;
            return this;
        }

        @Override
        public XyStyle withLineWidth(float lineWidth) {
            this.lineWidth = lineWidth;
            return this;
        }

        @Override
        public XyStyle withPointShape(ChartShape pointShape) {
            this.pointShape = pointShape;
            this.pointsVisible = true;
            return this;
        }

        @Override
        public XyStyle withPointsVisible(boolean pointsVisible) {
            this.pointsVisible = pointsVisible;
            return this;
        }
    }

}
