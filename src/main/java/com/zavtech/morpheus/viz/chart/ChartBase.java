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
import java.util.HashMap;
import java.util.Map;

/**
 * A convenience base class for building chart implementations
 *
 * @param <X>   the type for the domain axis
 *
 * @author  Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public abstract class ChartBase<X extends Comparable> implements Chart<X>  {

    private Map<Comparable,SeriesStyleAdapter> seriesStyleMap = new HashMap<>();

    /**
     * Constructor
     */
    public ChartBase() {
        super();
    }

    @Override
    public ChartSeriesStyle style(Comparable seriesKey) {
        SeriesStyleAdapter seriesStyleAdapter = seriesStyleMap.get(seriesKey);
        if (seriesStyleAdapter == null) {
            seriesStyleAdapter = new SeriesStyleAdapter();
            seriesStyleMap.put(seriesKey, seriesStyleAdapter);
        }
        return seriesStyleAdapter;
    }

    /**
     * Returns the series override color, or null if not set
     * @param seriesKey the series key
     * @return          the series color, or null
     */
    protected Color getSeriesColor(Comparable seriesKey) {
        SeriesStyleAdapter adapter = seriesStyleMap.get(seriesKey);
        return adapter != null ? adapter.color : null;
    }

    /**
     * Returns the series override line width, or null if not set
     * This is only relevant for series rendered with lines
     * @param seriesKey the series key
     * @return          the series line width, or null
     */
    protected float getSeriesLineWidth(Comparable seriesKey) {
        SeriesStyleAdapter adapter = seriesStyleMap.get(seriesKey);
        return adapter != null ? adapter.lineWidth : Float.NaN;
    }

    /**
     * Returns the series override shape, or null if not set
     * This is only relevant for series rendered with points
     * @param seriesKey the series key
     * @return          the series shape, or null
     */
    protected ChartShape getSeriesPointShape(Comparable seriesKey) {
        SeriesStyleAdapter adapter = seriesStyleMap.get(seriesKey);
        return adapter != null ? adapter.pointShape : null;
    }

    /**
     * Returns the series override point visibility, or null if not set
     * @param seriesKey the series key
     * @return          the series point visibility, or null
     */
    protected Boolean isSeriesPointsVisible(Comparable seriesKey) {
        SeriesStyleAdapter adapter = seriesStyleMap.get(seriesKey);
        return adapter != null && adapter.pointsVisible;
    }

    /**
     * Returns the series override to control whether line is dashed or not
     * @param seriesKey the series key
     * @return          true if the series line should be dashed
     */
    protected Boolean isSeriesDashedLine(Comparable seriesKey) {
        SeriesStyleAdapter adapter = seriesStyleMap.get(seriesKey);
        return adapter != null && adapter.dashes;
    }

    /**
     * A ChartSeriesStyle adapter implementation
     */
    private class SeriesStyleAdapter implements ChartSeriesStyle {

        private Color color;
        private boolean dashes;
        private ChartShape pointShape;
        private float lineWidth = Float.NaN;
        private boolean pointsVisible = false;

        /**
         * Constructor
         */
        private SeriesStyleAdapter() {
            super();
        }

        @Override
        public ChartSeriesStyle withColor(Color color) {
            this.color = color;
            return this;
        }

        @Override
        public ChartSeriesStyle withDashes(boolean dashed) {
            this.dashes = dashed;
            return this;
        }

        @Override
        public ChartSeriesStyle withLineWidth(float lineWidth) {
            this.lineWidth = lineWidth;
            return this;
        }

        @Override
        public ChartSeriesStyle withPointShape(ChartShape pointShape) {
            this.pointShape = pointShape;
            return this;
        }

        @Override
        public ChartSeriesStyle withPointsVisible(boolean pointsVisible) {
            this.pointsVisible = pointsVisible;
            return this;
        }
    }

}
