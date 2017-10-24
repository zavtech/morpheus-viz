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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.zavtech.morpheus.index.Index;
import com.zavtech.morpheus.viz.chart.ChartShape;
import com.zavtech.morpheus.viz.chart.xy.XyAxes;
import com.zavtech.morpheus.viz.chart.xy.XyModel;
import com.zavtech.morpheus.viz.chart.xy.XyOrient;
import com.zavtech.morpheus.viz.chart.xy.XyPlotBase;
import com.zavtech.morpheus.viz.chart.xy.XyRender;
import com.zavtech.morpheus.viz.chart.xy.XyTrend;
import com.zavtech.morpheus.viz.js.JsObject;
import com.zavtech.morpheus.viz.util.ColorModel;

/**
 * The XyPlot implementation for the Google Charts adapter
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
class GXyPlot<X extends Comparable> extends XyPlotBase<X> {

    private GXyAxes axes = new GXyAxes(this);
    private GXyOrient orient = new GXyOrient();
    private GXyModel<X,Comparable> model = new GXyModel<>(this);
    private Map<Integer,GXyRender> renderMap = new HashMap<>();
    private Map<Comparable,GTrendLine> trendLineMap = new HashMap<>();
    private ChartShape.Provider shapeProvider = new ChartShape.DefaultProvider();


    /**
     * Constructor
     */
    GXyPlot() {
        super();
    }


    @Override
    public XyAxes axes() {
        return axes;
    }


    @Override
    @SuppressWarnings("unchecked")
    public <S extends Comparable> XyModel<X,S> data() {
        return (XyModel<X,S>)model;
    }


    @Override
    public XyOrient orient() {
        return orient;
    }


    @Override
    public <S extends Comparable> XyTrend trend(S seriesKey) {
        GTrendLine trend = trendLineMap.get(seriesKey);
        if (trend == null) {
            trend = new GTrendLine(seriesKey);
            trendLineMap.put(seriesKey, trend);
        }
        return trend;
    }


    @Override
    public XyRender render(int index) {
        GXyRender render = renderMap.get(index);
        if (render == null) {
            render = new GXyRender(this);
            renderMap.put(index, render);
        }
        return render;
    }


    /**
     * Returns the render with a more specific type
     * @param index the dataset index
     * @return      the renderer
     */
    private GXyRender renderAt(int index) {
        return (GXyRender)render(index);
    }


    /**
     * Enriches the JsObject with options for these axes
     * @param options   the options to enrich
     */
    public void accept(JsObject options) {
        options.setIgnoreNulls(true);
        options.newAttribute("dataOpacity", 1.0);
        options.newAttribute("axisTitlesPosition", "out");
        this.axisOptions(options);
        this.seriesOptions(options);
        this.trendLineOptions(options);
        if (renderAt(0).isBars()) {
            options.newAttribute("isStacked", renderAt(0).isStacked());
            options.newObject("bar", bar -> bar.newAttribute("groupWidth", "70%"));
        } else if (renderAt(0).isArea()) {
            options.newAttribute("isStacked", renderAt(0).isStacked());
        } else if (renderAt(0).isLines()) {
            options.newAttribute("lineWidth", 1);
            options.newAttribute("curveType", renderAt(0).getCurveType());
        }
        if (!renderAt(0).isBars()) {
            options.newAttribute("orientation", isVertical() ? "vertical" : "horizontal");
        }
    }


    /**
     * Enriches the options with axis configuration
     * @param options   the Javascript options object
     */
    private void axisOptions(JsObject options) {
        final String domain = renderAt(0).isBars() && isHorizontal() ? "vAxis" : "hAxis";
        final String range1 = renderAt(0).isBars() && isHorizontal() ? "hAxis" : "vAxis";
        final String range2 = renderAt(0).isBars() && isHorizontal() ? "hAxes" : "vAxes";
        options.newObject(domain, axis -> {
            final GXyDataset dataset = model.getUnifiedDataset();
            final GXyAxis domainAxis = (GXyAxis)axes().domain();
            domainAxis.accept(axis, dataset);
        });
        if (axes.rangeAxisCount() == 1) {
            options.newObject(range1, axis -> {
                final GXyDataset dataset = model.getUnifiedDataset();
                final GXyAxis rangeAxis = (GXyAxis)axes.range(0);
                rangeAxis.accept(axis, dataset);
            });
        } else {
            options.newObject(range2, vAxes -> {
                for (int i=0; i<axes.rangeAxisCount(); ++i) {
                    final GXyDataset dataset = model.getUnifiedDataset();
                    final GXyAxis rangeAxis = (GXyAxis)axes.range(i);
                    vAxes.newObject(i, axis -> rangeAxis.accept(axis, dataset));
                }
            });
        }
    }


    /**
     * Enriches the options with series configuration
     * @param options   the Javascript options object
     */
    @SuppressWarnings("unchecked")
    private void seriesOptions(JsObject options) {
        options.newObject("series", seriesList -> {
            final GXyDataset dataset = model.getUnifiedDataset();
            for (int i=0; i<dataset.getSeriesCount(); ++i) {
                final Comparable seriesKey = dataset.getSeriesKey(i);
                final int axisIndex = model.getRangeAxisIndex(seriesKey);
                final int datasetIndex = model.getDatasetIndex(seriesKey);
                final GXyRender render = renderAt(datasetIndex);
                final Color color = render.getSeriesColor(seriesKey);
                final Optional<Float> lineWidth = render.getSeriesLineWidth(seriesKey);
                final Optional<ChartShape> shape = render.getSeriesPointShape(seriesKey);
                seriesList.newObject(i, series -> {
                    series.setIgnoreNulls(true);
                    series.newAttribute("targetAxisIndex", axisIndex);
                    series.newAttribute("visibleInLegend", true);
                    series.newAttribute("color", ColorModel.toHexString(color));
                    if (render.hasShapesOrPoints()) {
                        series.newAttribute("pointsVisible", render.isSeriesPointsVisible(seriesKey));
                        series.newAttribute("pointSize", render.getPointSize());
                    }
                    if (render.isLines()) {
                        series.newAttribute("lineWidth", lineWidth.orElse(1f));
                        series.newAttribute("curveType", render.getCurveType());
                        applyShape(series, shape.orElse(null));
                        if (render.isSeriesDashedLine(seriesKey)) {
                            series.newArray("lineDashStyle", true, array -> array.append(10).append(2));
                        }
                    }
                });
            }
        });
    }


    /**
     * Enriches the options with trend line configuration
     * @param options   the Javascript options
     */
    @SuppressWarnings("unchecked")
    private void trendLineOptions(JsObject options) {
        if (trendLineMap.size() > 0) {
            final GXyDataset<X,Comparable> dataset = model.getUnifiedDataset();
            final Iterable<Comparable> seriesKeys = dataset.getSeriesKeys();
            final Index<Comparable> index = Index.of(seriesKeys);
            if (seriesKeys != null) {
                options.newObject("trendlines", trendLines -> {
                    trendLineMap.forEach((key, trend) -> {
                        final int ordinal = index.getOrdinalForKey(key);
                        trendLines.newObject(ordinal, entry -> {
                            entry.newAttribute("type", "linear");
                            entry.newAttribute("color", ColorModel.toHexString(trend.getColor()));
                            entry.newAttribute("lineWidth", trend.getLineWidth());
                            entry.newAttribute("opacity", 1d);
                            entry.newAttribute("showR2", false);
                            entry.newAttribute("visibleInLegend", false);
                        });
                    });
                });
            }
        }
    }


    /**
     * Applies a point shape style to the series entry
     * @param object    the series object
     * @param shape     the chart shape
     */
    private void applyShape(JsObject object, ChartShape shape) {
        if (shape != null) {
            switch (shape) {
                case CIRCLE:            object.newAttribute("pointShape", "circle");    break;
                case SQUARE:            object.newAttribute("pointShape", "square");    break;
                case DIAMOND:           object.newAttribute("pointShape", "diamond");   break;
                case TRIANGLE_UP:       object.newAttribute("pointShape", "triangle");  break;
                case TRIANGLE_DOWN:     object.newObject("pointShape", x -> x.newAttribute("type", "triangle").newAttribute("rotation", 180));  break;
                case TRIANGLE_RIGHT:    object.newObject("pointShape", x -> x.newAttribute("type", "triangle").newAttribute("rotation", 90));   break;
                case TRIANGLE_LEFT:     object.newObject("pointShape", x -> x.newAttribute("type", "triangle").newAttribute("rotation", 270));  break;
                default:                System.err.println("Unsupported shape: " + shape);
            }
        }
    }


    /**
     * Returns true if the orientation is set to horizontal
     * @return  true if horizontal orientation
     */
    private boolean isHorizontal() {
        return !isVertical();
    }


    /**
     * Returns the shape provider for this plot
     * @return      the shape provider
     */
    ChartShape.Provider getShapeProvider() {
        return shapeProvider;
    }

    /**
     * Returns true if the orientation is set to vertical
     * @return  true if vertical orientation
     */
    boolean isVertical() {
        if (orient.horizontal != null) {
            return !orient.horizontal;
        } else if (renderAt(0).isBars()) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * An adapter implementation for the ChartOrientation interface
     */
    private class GXyOrient implements XyOrient {

        private Boolean horizontal;

        @Override
        public void vertical() {
            this.horizontal = false;
        }

        @Override
        public void horizontal() {
            this.horizontal = true;
        }
    }
}
