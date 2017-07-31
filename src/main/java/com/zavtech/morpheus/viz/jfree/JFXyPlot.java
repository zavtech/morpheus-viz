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
package com.zavtech.morpheus.viz.jfree;

import java.awt.*;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;

import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.viz.chart.ChartException;
import com.zavtech.morpheus.viz.chart.xy.XyAxes;
import com.zavtech.morpheus.viz.chart.xy.XyDataset;
import com.zavtech.morpheus.viz.chart.xy.XyModel;
import com.zavtech.morpheus.viz.chart.xy.XyOrient;
import com.zavtech.morpheus.viz.chart.xy.XyPlotBase;
import com.zavtech.morpheus.viz.chart.xy.XyRender;
import com.zavtech.morpheus.viz.chart.xy.XyTrend;
import com.zavtech.morpheus.viz.chart.xy.XyTrendBase;
import com.zavtech.morpheus.viz.html.HtmlWriter;

/**
 * The plot implementation for JFreeChart xy plots.
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
class JFXyPlot<X extends Comparable> extends XyPlotBase<X> {

    private XYPlot plot;
    private Map<Comparable,TrendLine> trendMap = new HashMap<>();
    private Map<Integer,JFXyDataset<X,? extends Comparable>> datasetMap = new LinkedHashMap<>();
    private DecimalFormat decimalFormat = new DecimalFormat("###,##0.####;-###,##0.####");


    /**
     * Constructor
     * @param domainAxis    the domain axis
     * @param rangeAxis     the range axis
     */
    JFXyPlot(ValueAxis domainAxis, ValueAxis rangeAxis) {
        this.plot = new XYPlot(null, domainAxis, rangeAxis, null);
        this.plot.getRangeAxis().setAutoRange(true);
        this.plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
        this.plot.setDomainGridlinesVisible(true);
        this.plot.setRangeGridlinesVisible(true);
        this.plot.setDomainGridlinePaint(Color.DARK_GRAY);
        this.plot.setRangeGridlinePaint(Color.DARK_GRAY);
        this.plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        if (rangeAxis instanceof NumberAxis) ((NumberAxis)rangeAxis).setAutoRangeIncludesZero(false);
        if (domainAxis instanceof NumberAxis) ((NumberAxis)domainAxis).setAutoRangeIncludesZero(false);
    }

    /**
     * Returns the underlying JFreeChart plot object
     * @return      the underlying plot object
     */
    final XYPlot underlying() {
        return plot;
    }

    @Override
    public XyAxes axes() {
        return new JFXyAxes(plot);
    }

    @Override
    public <S extends Comparable> XyModel<X,S> data() {
        return new ModelAdapter<>();
    }

    @Override
    public XyOrient orient() {
        return new OrientAdapter();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S extends Comparable> XyTrend trend(S seriesKey) {
        TrendLine trend = trendMap.get(seriesKey);
        if (trend == null) {
            final ModelAdapter<Comparable> modelAdapter = new ModelAdapter<>();
            final Stream<JFXyDataset<X,Comparable>> datasets = modelAdapter.getModels().map(m -> (JFXyDataset<X,Comparable>)m);
            final Optional<JFXyDataset<X,Comparable>> datasetOpt = datasets.filter(m -> m.contains(seriesKey)).findFirst();
            if (!datasetOpt.isPresent()) {
                throw new ChartException("No chart data could be located for series: " + seriesKey);
            } else {
                final JFXyDataset<X,Comparable> dataset = datasetOpt.get();
                trend = new TrendLine(dataset, seriesKey);
                trendMap.put(seriesKey, trend);
                applyTrend(dataset, trend);
            }
        }
        return trend;
    }

    @Override
    public XyRender render(int index) {
        return new JFXyRender(this, index);
    }

    /**
     * Returns the number of none null data sets for this plot
     * @return      the data set count for plot
     */
    private int getDatasetCount() {
        int count = 0;
        for (int i=0; i<plot.getDatasetCount(); ++i) {
            final XYDataset dataSet = plot.getDataset(i);
            if (dataSet != null) {
                ++count;
            }
        }
        return count;
    }


    /**
     * Applies a trend line to the plot based on the dataset and trend definition
     * @param dataset   the dataset from which to compute the trend line
     * @param trend     the trend line instance
     */
    private void applyTrend(JFXyDataset<X,Comparable> dataset, TrendLine trend) {
        try {
            this.render(trend.datasetIndex).withLines(false, false);
            this.style(trend.trendKey).withColor(trend.lineColor);
            this.style(trend.trendKey).withLineWidth(trend.lineWidth);
            this.plot.getRenderer(trend.datasetIndex).setBaseSeriesVisibleInLegend(false);
            this.plot.getRenderer(trend.datasetIndex).setBaseToolTipGenerator(this::getTrendTooltip);
            this.plot.setDataset(trend.datasetIndex, JFXyDataset.of(() -> {
                final Comparable seriesKey = trend.seriesKey();
                return trend.createTrendData(dataset, seriesKey, trend.trendKey);
            }));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * Returns a tooltip to display details for an XY point
     * @param dataset   the dataset reference
     * @param series    the series index
     * @param item      the item index
     * @return          the tooltip, which can be null
     */
    String getXyTooltip(XYDataset dataset, int series, int item) {
        try {
            final Comparable seriesKey = dataset.getSeriesKey(series);
            final double x = dataset.getXValue(series, item);
            final double y = dataset.getYValue(series, item);
            if (plot.getDomainAxis() instanceof DateAxis) {
                final DateFormat dateFormat = ((DateAxis)plot.getDomainAxis()).getDateFormatOverride();
                final DateFormat formatter = dateFormat != null ? dateFormat : new SimpleDateFormat("dd-MMM-yyyy HH:mm");
                final String xLabel = formatter.format(new Date((long)x));
                final String yLabel = decimalFormat.format(y);
                return HtmlWriter.createHtml(writer -> {
                    writer.newElement("html", html -> {
                        html.newElement("h2", h2 -> h2.text(seriesKey.toString()));
                        html.newElement("h3", h3 -> h3.text(String.format("X = %s, Y = %s", xLabel, yLabel)));
                    });
                });
            } else {
                final String xLabel = decimalFormat.format(x);
                final String yLabel = decimalFormat.format(y);
                return HtmlWriter.createHtml(writer -> {
                    writer.newElement("html", html -> {
                        html.newElement("h2", h2 -> h2.text(seriesKey.toString()));
                        html.newElement("h3", h3 -> h3.text(String.format("X = %s, Y = %s", xLabel, yLabel)));
                    });
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }


    /**
     * Returns a tooltip to display a trend line equation
     * @param dataset   the dataset reference
     * @param series    the series index
     * @param item      the item index
     * @return          the tooltip
     */
    private String getTrendTooltip(XYDataset dataset, int series, int item) {
        try {
            final Comparable seriesKey = dataset.getSeriesKey(series).toString().replace(" (trend)", "");
            final TrendLine trend = trendMap.get(seriesKey);
            if (trend == null) {
                return null;
            } else {
                final String slope = decimalFormat.format(trend.slope());
                final String intercept = decimalFormat.format(trend.intercept());
                final String equation = String.format("Y = %s * X + %s", slope, intercept);
                return HtmlWriter.createHtml(writer -> {
                    writer.newElement("html", html -> {
                        html.newElement("h2", h2 -> h2.text(seriesKey.toString()));
                        html.newElement("h3", h3 -> h3.text(equation));
                    });
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }



    /**
     * An adapter implementation for the ChartOrientation interface
     */
    private class OrientAdapter implements XyOrient {
        @Override
        public void vertical() {
            plot.setOrientation(PlotOrientation.VERTICAL);
        }
        @Override
        public void horizontal() {
            plot.setOrientation(PlotOrientation.HORIZONTAL);
        }
    }




    /**
     * A XyTrend adapter for the JFreeChart library
     */
    private class TrendLine extends XyTrendBase {

        private Color lineColor;
        private float lineWidth;
        private int datasetIndex;
        private Comparable trendKey;


        /**
         * Constructor
         * @param seriesKey the series key
         */
        @SuppressWarnings("unchecked")
        TrendLine(JFXyDataset<X,Comparable> source, Comparable seriesKey) {
            super(seriesKey);
            this.lineWidth = 2f;
            this.lineColor = Color.BLACK;
            this.datasetIndex = getDatasetCount();
            this.trendKey = String.format("%s (trend)", seriesKey);
        }

        @Override
        public XyTrend withColor(Color color) {
            this.lineColor = color;
            JFXyPlot.this.style(trendKey).withColor(color);
            return this;
        }

        @Override
        public XyTrend withLineWidth(float width) {
            this.lineWidth = width;
            JFXyPlot.this.style(trendKey).withLineWidth(width);
            return this;
        }

        @Override
        public XyTrend clear() {
            final Comparable seriesKey = seriesKey();
            plot.setDataset(datasetIndex, null);
            trendMap.remove(seriesKey);
            return this;
        }
    }




    /**
     * An implementation of the XyModel interface that manages data for this plot
     */
    private class ModelAdapter<S extends Comparable> implements XyModel<X,S> {

        /**
         * Constructor
         */
        private ModelAdapter() {
            super();
        }

        /**
         * Returns a stream of the models contained in this adapter
         * @return      the stream of models
         */
        private Stream<JFXyDataset<X,? extends Comparable>> getModels() {
            return datasetMap.values().stream();
        }


        @Override
        public Class<X> domainType() {
            if (datasetMap.isEmpty()) {
                return null;
            } else {
                return datasetMap.entrySet().iterator().next().getValue().domainType();
            }
        }


        @Override
        @SuppressWarnings("unchecked")
        public XyDataset<X,S> at(int index) {
            final JFXyDataset<X,?> dataset = datasetMap.get(index);
            if (dataset != null) {
                return (XyDataset<X,S>)dataset;
            } else {
                throw new IllegalArgumentException("No chart data located at index: " + index);
            }
        }

        @Override
        public void setRangeAxis(int dataset, int axis) {
            final Axis rangeAxis = plot.getRangeAxis(axis);
            if (rangeAxis == null) {
                plot.setRangeAxis(axis, new NumberAxis());
                plot.mapDatasetToRangeAxis(dataset, axis);
            } else {
                plot.mapDatasetToRangeAxis(dataset, axis);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public int add(DataFrame<X,S> frame) {
            final int index = getDatasetCount();
            final JFXyDataset<X,S> dataset = JFXyDataset.of(() -> frame);
            datasetMap.put(index, dataset);
            plot.setDataset(index, dataset);
            render(index).withLines(false, false);
            return index;
        }

        @Override
        @SuppressWarnings("unchecked")
        public int add(DataFrame<?,S> frame, S domainKey) {
            final int index = getDatasetCount();
            final JFXyDataset<X,S> dataset = JFXyDataset.of(domainKey, () -> frame);
            datasetMap.put(index, dataset);
            plot.setDataset(index, dataset);
            render(index).withLines(false, false);
            return index;
        }

        @Override
        @SuppressWarnings("unchecked")
        public XyDataset<X,S> update(int index, DataFrame<X,S> frame) {
            final JFXyDataset<X,S> dataset = JFXyDataset.of(() -> frame);
            datasetMap.put(index, dataset);
            plot.setDataset(index, dataset);
            return dataset;
        }

        @Override
        @SuppressWarnings("unchecked")
        public XyDataset<X,S> update(int index, DataFrame<?,S> frame, S domainKey) {
            final JFXyDataset<X,S> dataset = JFXyDataset.of(domainKey, () -> frame);
            datasetMap.put(index, dataset);
            plot.setDataset(index, dataset);
            return dataset;
        }

        @Override
        public void remove(int index) {
            final JFXyDataset<X,?> dataset = datasetMap.remove(index);
            if (dataset == null) {
                throw new ChartException("No chart dataset exists for id: " + index);
            } else {
                plot.setDataset(index, null);
            }
        }

        @Override
        public void removeAll() {
            final int count = plot.getDatasetCount();
            datasetMap.clear();
            for (int i=0; i<count; ++i) {
                plot.setDataset(i, null);
            }
        }
    }
}
