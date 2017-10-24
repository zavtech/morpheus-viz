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
import java.util.Optional;

import com.zavtech.morpheus.viz.chart.ChartShape;
import com.zavtech.morpheus.viz.chart.xy.XyRender;

/**
 * The XyRender implementation for the Google Charts adapter
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
class GXyRender implements XyRender {

    enum Type { DOTS, SHAPES, BARS, LINES, AREA, SPLINE }

    private int pointSize = 4;
    private boolean stacked = false;
    private boolean spline = false;
    private boolean dashed = false;
    private boolean shapes = false;
    private Type type = Type.LINES;
    private GXyPlot<? extends Comparable> plot;


    /**
     * Constructor
     * @param plot  the plot this renderer is bound to
     */
    GXyRender(GXyPlot<? extends Comparable> plot) {
        this.plot = plot;
    }

    /**
     * Returns the chart type for this renderer
     * @return      the chart type
     */
    Type getChartType() {
        return type;
    }

    /**
     * Returns the curve type for this render
     * @return  the curve type
     */
    String getCurveType() {
        return spline ? "function" : "none";
    }

    /**
     * Returns true for stacked bar rendering
     * @return  true for stacked bar rendering
     */
    boolean isStacked() {
        return stacked;
    }

    /**
     * Returns true if this renderer is for bars
     * @return  true if configured for bars
     */
    boolean isBars() {
        return type == Type.BARS;
    }

    /**
     * Returns true if this renderer is for lines
     * @return  true if configured for lines
     */
    boolean isLines() {
        return getChartType() == Type.LINES;
    }

    /**
     * Returns true if this renderer is for area
     * @return  true if configured for area
     */
    boolean isArea() {
        return getChartType() == Type.AREA;
    }

    /**
     * Returns true if rendering includes points or shapes
     * @return  true if points or shapes
     */
    boolean hasShapesOrPoints() {
        return type == Type.DOTS || shapes;
    }

    /**
     * Returns true if rendering of scatter points or shapes
     * @return  true if dots or shapes.
     */
    boolean isScatter() {
        return type == Type.DOTS || type == Type.SHAPES;
    }

    /**
     * Returns true if dot rendering is set
     * @return  true if dot rendering
     */
    boolean isDots() {
        return type == Type.DOTS;
    }

    /**
     * Returns the points size for dot rendering
     * @return  the point size
     */
    int getPointSize() {
        return pointSize;
    }


    @Override
    public void withDots() {
        this.withDots(4);
    }


    @Override
    public void withDots(int diameter) {
        this.type = Type.DOTS;
        this.shapes = false;
        this.dashed = false;
        this.spline = false;
        this.stacked = false;
        this.pointSize = diameter;

    }

    @Override
    public void withArea(boolean stacked) {
        this.type = Type.AREA;
        this.spline = false;
        this.stacked = stacked;
        this.shapes = false;
        this.dashed = false;
    }


    @Override
    public void withBars(boolean stacked, double margin) {
        this.type = Type.BARS;
        this.spline = false;
        this.stacked = stacked;
        this.shapes = false;
        this.dashed = false;
    }


    @Override
    public void withLines(boolean shapes, boolean dashed) {
        this.type = Type.LINES;
        this.shapes = shapes;
        this.dashed = dashed;
        this.spline = false;
        this.stacked = false;

    }

    @Override
    public void withShapes() {
        this.type = Type.SHAPES;
        this.shapes = true;
        this.spline = false;
        this.stacked = false;
        this.dashed = false;
    }


    @Override
    public void withSpline(boolean shapes, boolean dashed) {
        this.type = Type.LINES;
        this.spline = true;
        this.shapes = shapes;
        this.dashed = dashed;
        this.stacked = false;
    }


    /**
     * Returns the series override color, or null if not set
     * @param seriesKey the series key
     * @return          the series color, or null
     */
    Color getSeriesColor(Comparable seriesKey) {
        final Color color = plot.getSeriesColor(seriesKey);
        return color != null ? color : plot.getColorModel().getColor(seriesKey);
    }

    /**
     * Returns the series override line width, or null if not set
     * This is only relevant for series rendered with lines
     * @param seriesKey the series key
     * @return          the series line width, or null
     */
    Optional<Float> getSeriesLineWidth(Comparable seriesKey) {
        final Float lineWidth = plot.getSeriesLineWidth(seriesKey);
        return lineWidth != null ? Optional.of(lineWidth) : isLines() ? Optional.of(1f) : Optional.empty();
    }

    /**
     * Returns the series override point visibility, or null if not set
     * @param seriesKey the series key
     * @return          the series point visibility, or null
     */
    boolean isSeriesPointsVisible(Comparable seriesKey) {
        final Boolean visible = plot.isSeriesPointsVisible(seriesKey);
        return visible != null ? visible : shapes || type == Type.DOTS;
    }

    /**
     * Returns the series override to control whether line is dashed or not
     * @param seriesKey the series key
     * @return          true if the series line should be dashed
     */
    boolean isSeriesDashedLine(Comparable seriesKey) {
        final Boolean dashed = plot.isSeriesDashedLine(seriesKey);
        return dashed != null ? dashed : this.dashed;
    }

    /**
     * Returns the series override shape, or null if not set
     * This is only relevant for series rendered with points
     * @param seriesKey the series key
     * @return          the series shape, or null
     */
    Optional<ChartShape> getSeriesPointShape(Comparable seriesKey) {
        ChartShape shape = plot.getSeriesPointShape(seriesKey);
        if (shape != null) {
            return Optional.of(shape);
        } else if (!shapes) {
            return Optional.empty();
        } else {
            return Optional.of(plot.getShapeProvider().getShape(seriesKey));
        }
    }

}
