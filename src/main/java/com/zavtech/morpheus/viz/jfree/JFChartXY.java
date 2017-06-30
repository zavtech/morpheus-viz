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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.zavtech.morpheus.viz.chart.ChartData;
import com.zavtech.morpheus.viz.chart.ChartPlotStyle;
import com.zavtech.morpheus.viz.chart.ChartModel;
import com.zavtech.morpheus.viz.chart.ChartException;
import com.zavtech.morpheus.viz.chart.ChartSeriesStyle;
import com.zavtech.morpheus.viz.chart.ChartShape;
import com.zavtech.morpheus.viz.chart.ChartTrendLine;
import com.zavtech.morpheus.frame.DataFrame;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.StackedXYBarRenderer;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.xy.XYDataset;

/**
 * A Chart used to implement XY series plots
 *
 * @param <X>   the type for the domain
 *
 * @author  Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
class JFChartXY<X extends Comparable> extends JFChartBase<XYPlot,X> {

    private DataAdapter dataAdapter;
    private TrendLineAdapter trendLineAdapter;

    /**
     * Constructor
     * @param domainAxis    the domain axis
     * @param rangeAxis     the range axis
     * @param legend        true to show legends
     */
    JFChartXY(ValueAxis domainAxis, ValueAxis rangeAxis, boolean legend) {
        super(new XYPlot(null, domainAxis, rangeAxis, null), legend);
        this.dataAdapter = new DataAdapter(getPlot());
        this.trendLineAdapter = new TrendLineAdapter();
        this.getPlot().getRangeAxis().setAutoRange(true);
        this.getPlot().setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
        this.getPlot().setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        this.getPlot().setDomainGridlinesVisible(true);
        this.getPlot().setRangeGridlinesVisible(true);
        this.getPlot().setDomainGridlinePaint(Color.DARK_GRAY);
        this.getPlot().setRangeGridlinePaint(Color.DARK_GRAY);
        this.getPlot().setDatasetRenderingOrder(DatasetRenderingOrder.REVERSE);
        if (rangeAxis instanceof NumberAxis) ((NumberAxis)rangeAxis).setAutoRangeIncludesZero(false);
        if (domainAxis instanceof NumberAxis) ((NumberAxis)domainAxis).setAutoRangeIncludesZero(false);
        this.getChartPanel().addChartMouseListener(new ChartMouseListener() {
            @Override
            public void chartMouseClicked(ChartMouseEvent event) {

            }
            @Override
            public void chartMouseMoved(ChartMouseEvent event) {
                try {
                    final ChartEntity entity = event.getEntity();
                    if (entity instanceof XYItemEntity) {
                        final XYItemEntity xyEntity = (XYItemEntity)entity;
                        final XYDataset dataset = xyEntity.getDataset();
                        final int seriesIndex = xyEntity.getSeriesIndex();
                        final Comparable seriesKey = dataset.getSeriesKey(seriesIndex);
                        if (seriesKey != null) {
                            System.out.println("Activate: " + seriesKey);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    @Override
    public ChartData<X> data() {
        return dataAdapter;
    }

    @Override
    public ChartTrendLine trendLine() {
        return trendLineAdapter;
    }

    @Override
    public ChartPlotStyle plot(int index) {
        return new PlotStyleAdapter(getPlot(), index);
    }

    /**
     * Returns the number of none null data sets for this plot
     * @return      the data set count for plot
     */
    private int getDatasetCount() {
        int count = 0;
        for (int i=0; i<getPlot().getDatasetCount(); ++i) {
            final XYDataset dataSet = getPlot().getDataset(i);
            if (dataSet != null) {
                ++count;
            }
        }
        return count;
    }




    /**
     * The style implementation for this chart
     */
    private class PlotStyleAdapter implements ChartPlotStyle {

        private int index;
        private XYPlot plot;

        /**
         * Constructor
         * @param plot      the plot for this adapter
         * @param index     the dataset index
         */
        private PlotStyleAdapter(XYPlot plot, int index) {
            this.plot = plot;
            this.index = index;
        }

        @Override()
        public ChartPlotStyle withArea(boolean shapes) {
            final XYAreaRenderer renderer = new XYAreaRenderer();
            renderer.setOutline(false);
            renderer.setBaseCreateEntities(false);
            this.plot.setForegroundAlpha(0.5f);
            this.plot.setRenderer(index, renderer);
            return this;
        }

        @Override()
        public ChartPlotStyle withBars(double margin) {
            this.plot.setRenderer(index, new MorpheusBarRenderer(index));
            return this;
        }

        @Override()
        public ChartPlotStyle withLines() {
            final LineAndPointRenderer renderer = new LineAndPointRenderer(true, false, index);
            this.plot.setRenderer(index, renderer);
            return this;
        }


        public ChartPlotStyle withDottedLines() {
            final LineAndPointRenderer renderer = new LineAndPointRenderer(true, false, index);
            renderer.setSeriesStroke(
                    2,
                    new BasicStroke(
                            2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                            1.0f, new float[] {2.0f, 6.0f}, 0.0f
                    )
            );
            this.plot.setRenderer(index, renderer);
            return this;
        }

        @Override()
        public ChartPlotStyle withPoints() {
            final LineAndPointRenderer renderer = new LineAndPointRenderer(false, true, index);
            renderer.setDrawOutlines(true);
            renderer.setUseOutlinePaint(true);
            renderer.setBaseOutlinePaint(Color.GRAY);
            this.plot.setRenderer(index, renderer);
            return this;
        }

        @Override()
        public ChartPlotStyle withLinesAndPoints() {
            final LineAndPointRenderer renderer = new LineAndPointRenderer(true, true, index);
            this.plot.setRenderer(index, renderer);
            return this;
        }

        @Override()
        public ChartPlotStyle withSpline() {
            final SplineRenderer renderer = new SplineRenderer(5, index);
            this.plot.setRenderer(index, renderer);
            return this;
        }

        @Override
        public ChartPlotStyle withStackedBars(double margin) {
            this.plot.setRenderer(index, new StackedXYBarRenderer(margin));
            return this;
        }
    }

    /**
     * A ChartData adapter implementation used to manage datasets on a JFreeChart XYPlot
     */
    private class DataAdapter implements ChartData<X> {

        private XYPlot plot;
        private Map<Integer,ChartModel<X,? extends Comparable>> modelMap = new LinkedHashMap<>();

        /**
         * Constructor
         * @param plot  the plot for this chart
         */
        private DataAdapter(XYPlot plot) {
            this.plot = plot;
        }

        /**
         * Returns a stream of the models contained in this adapter
         * @return      the stream of models
         */
        private Stream<ChartModel<X,? extends Comparable>> getModels() {
            return modelMap.values().stream();
        }

        @Override
        @SuppressWarnings("unchecked")
        public <S extends Comparable> ChartModel<X,S> at(int index) {
            final ChartModel<X,? extends Comparable> model = modelMap.get(index);
            if (model != null) {
                return (ChartModel<X,S>)model;
            } else {
                throw new IllegalArgumentException("No chart data located at index: " + index);
            }
        }

        @Override
        public ChartData<X> setRangeAxis(int dataset, int axis) {
            final Axis rangeAxis = getPlot().getRangeAxis(axis);
            if (rangeAxis == null) plot.setRangeAxis(axis, new NumberAxis());
            this.plot.mapDatasetToRangeAxis(dataset, axis);
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <S extends Comparable> ChartModel<X,S> add(DataFrame<X,S> frame) {
            final int index = getDatasetCount();
            final ChartModel<X,S> model = ChartModel.of(() -> frame);
            final XYDataset dataset = JFChartData.ofXYSeries((ChartModel<Comparable,Comparable>)model);
            this.modelMap.put(index, model);
            this.plot.setDataset(index, dataset);
            return model;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <S extends Comparable> ChartModel<X,S> add(DataFrame<?,S> frame, S domainKey) {
            final int index = getDatasetCount();
            final ChartModel<X,S> model = ChartModel.of(domainKey, () -> frame);
            final XYDataset dataset = JFChartData.ofXYSeries((ChartModel<Comparable,Comparable>)model);
            this.modelMap.put(index, model);
            this.plot.setDataset(index, dataset);
            return model;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <S extends Comparable> ChartModel<X,S> update(int index, DataFrame<X,S> frame) {
            final ChartModel<X,S> model = ChartModel.of(() -> frame);
            final XYDataset dataset = JFChartData.ofXYSeries((ChartModel<Comparable,Comparable>)model);
            this.modelMap.put(index, model);
            this.plot.setDataset(index, dataset);
            return model;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <S extends Comparable> ChartModel<X,S> update(int index, DataFrame<?,S> frame, S domainKey) {
            final ChartModel<X,S> model = ChartModel.of(domainKey, () -> frame);
            final XYDataset dataset = JFChartData.ofXYSeries((ChartModel<Comparable,Comparable>)model);
            this.modelMap.put(index, model);
            this.plot.setDataset(index, dataset);
            return model;
        }

        @Override
        public void remove(int index) {
            final ChartModel<X,? extends Comparable> model = modelMap.remove(index);
            if (model == null) {
                throw new ChartException("No chart data model exists for id: " + index);
            } else {
                this.plot.setDataset(index, null);
            }
        }

        @Override
        public void removeAll() {
            final int count = getDatasetCount();
            this.modelMap.clear();
            for (int i=0; i<count; ++i) {
                this.plot.setDataset(i, null);
            }
        }
    }


    /**
     * A ChartTrendLine adapter for the JFreeChart library
     */
    private class TrendLineAdapter implements ChartTrendLine {

        @Override
        @SuppressWarnings("unchecked")
        public <S extends Comparable,T extends Comparable> ChartSeriesStyle add(S seriesKey, T trendKey) {
            final Optional<ChartModel<X,? extends Comparable>> modelOpt = dataAdapter.getModels().filter(m -> m.contains(seriesKey)).findFirst();
            if (!modelOpt.isPresent()) {
                throw new ChartException("No chart data could be located for series: " + seriesKey);
            } else {
                final ChartModel<X,S> model = (ChartModel<X,S>)modelOpt.get();
                final ChartModel<X,T> trendLine = model.createTrendLineDataset(seriesKey, trendKey);
                final XYDataset dataset = JFChartData.ofXYSeries((ChartModel<Comparable,Comparable>)trendLine);
                final int index = getDatasetCount();
                getPlot().setDataset(index, dataset);
                plot(index).withLines();
                style(trendKey).withColor(Color.BLACK);
                style(trendKey).withLineWidth(2f);
                return style(trendKey);
            }
        }

        @Override
        public <S extends Comparable> void remove(S trendKey) {

        }
    }


    /**
     * An extension of a JFreeChart renderer that integrates support for series specific styling
     */
    private class LineAndPointRenderer extends XYLineAndShapeRenderer {

        private int datasetIndex;
        private JFChartShapes shapes = new JFChartShapes();

        /**
         * Constructor
         * @param lines         true for lines to be shown by default
         * @param points        true for points to be displayed by default
         * @param datasetIndex  the dataset index for this renderer
         */
        private LineAndPointRenderer(boolean lines, boolean points, int datasetIndex) {
            super(lines, points);
            this.datasetIndex = datasetIndex;
        }

        @Override
        public Paint getSeriesPaint(int series) {
            try {
                final XYPlot plot = getPlot();
                final XYDataset dataset = plot.getDataset(datasetIndex);
                if (dataset != null) {
                    final Comparable seriesKey = dataset.getSeriesKey(series);
                    final Color color = getSeriesColor(seriesKey);
                    return color != null ? color : getColorModel().getColor(seriesKey);
                }
                return super.getSeriesPaint(series);
            } catch (Exception ex) {
                ex.printStackTrace();
                return super.getSeriesPaint(series);
            }
        }

        @Override
        public Stroke getSeriesStroke(int series) {
            try {
                final XYPlot plot = getPlot();
                final XYDataset dataset = plot.getDataset(datasetIndex);
                if (dataset != null) {
                    final Comparable seriesKey = dataset.getSeriesKey(series);
                    final float lineWidth = getSeriesLineWidth(seriesKey);
                    final boolean dashed = isSeriesDashedLine(seriesKey);
                    if (dashed) {
                        final float[] dash = new float[] {2.0f, 6.0f};
                        final float width = !Float.isNaN(lineWidth) ? lineWidth : 1f;
                        return new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, dash, 0.0f);
                    } else {
                        return !Float.isNaN(lineWidth) ? new BasicStroke(lineWidth) : super.getSeriesStroke(series);
                    }
                }
                return super.getSeriesStroke(series);
            } catch (Exception ex) {
                ex.printStackTrace();
                return super.getSeriesStroke(series);
            }
        }

        @Override
        public Boolean getSeriesShapesVisible(int series) {
            try {
                final XYPlot plot = getPlot();
                final XYDataset dataset = plot.getDataset(datasetIndex);
                if (dataset != null) {
                    final Comparable seriesKey = dataset.getSeriesKey(series);
                    final Boolean visible = isSeriesPointsVisible(seriesKey);
                    return visible != null ? visible : super.getSeriesShapesVisible(series);
                }
                return super.getSeriesShapesVisible(series);
            } catch (Exception ex) {
                ex.printStackTrace();
                return super.getSeriesShapesVisible(series);
            }
        }

        @Override
        public Boolean getSeriesLinesVisible(int series) {
            try {
                final XYPlot plot = getPlot();
                final XYDataset dataset = plot.getDataset(datasetIndex);
                if (dataset != null) {
                    final Comparable seriesKey = dataset.getSeriesKey(series);
                    final float lineWidth = getSeriesLineWidth(seriesKey);
                    if (lineWidth == 0f) {
                        return false;
                    }
                }
                return super.getSeriesLinesVisible(series);
            } catch (Exception ex) {
                ex.printStackTrace();
                return super.getSeriesLinesVisible(series);
            }
        }

        @Override
        public Shape getSeriesShape(int series) {
            try {
                final XYPlot plot = getPlot();
                final XYDataset dataset = plot.getDataset(datasetIndex);
                if (dataset != null) {
                    final Comparable seriesKey = dataset.getSeriesKey(series);
                    final ChartShape pointShape = getSeriesPointShape(seriesKey);
                    if (pointShape != null) {
                        final Shape shape = shapes.getShape(pointShape);
                        if (shape != null) return shape;
                    }
                }
                return super.getSeriesShape(series);
            } catch (Exception ex) {
                ex.printStackTrace();
                return super.getSeriesShape(series);
            }
        }
    }


    /**
     * An extension of an XYSplineRenderer that adds support for series specific styling
     */
    private class SplineRenderer extends XYSplineRenderer {

        private int datasetIndex;
        private JFChartShapes shapes = new JFChartShapes();

        /**
         * Constructor
         * @param precision     the number of points between data items
         * @param datasetIndex  the dataset index for this renderer
         */
        private SplineRenderer(int precision, int datasetIndex) {
            super(precision);
            this.datasetIndex = datasetIndex;
        }

        @Override
        public Paint getSeriesPaint(int series) {
            try {
                final XYPlot plot = getPlot();
                final XYDataset dataset = plot.getDataset(datasetIndex);
                if (dataset != null) {
                    final Comparable seriesKey = dataset.getSeriesKey(series);
                    final Color color = getSeriesColor(seriesKey);
                    return color != null ? color : getColorModel().getColor(seriesKey);
                }
                return super.getSeriesPaint(series);
            } catch (Exception ex) {
                ex.printStackTrace();
                return super.getSeriesPaint(series);
            }
        }

        @Override
        public Stroke getSeriesStroke(int series) {
            try {
                final XYPlot plot = getPlot();
                final XYDataset dataset = plot.getDataset(datasetIndex);
                if (dataset != null) {
                    final Comparable seriesKey = dataset.getSeriesKey(series);
                    final float lineWidth = getSeriesLineWidth(seriesKey);
                    final boolean dashed = isSeriesDashedLine(seriesKey);
                    if (dashed) {
                        final float[] dash = new float[] {2.0f, 6.0f};
                        final float width = !Float.isNaN(lineWidth) ? lineWidth : 1f;
                        return new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, dash, 0.0f);
                    } else {
                        return !Float.isNaN(lineWidth) ? new BasicStroke(lineWidth) : super.getSeriesStroke(series);
                    }
                }
                return super.getSeriesStroke(series);
            } catch (Exception ex) {
                ex.printStackTrace();
                return super.getSeriesStroke(series);
            }
        }

        @Override
        public Boolean getSeriesShapesVisible(int series) {
            try {
                final XYPlot plot = getPlot();
                final XYDataset dataset = plot.getDataset(datasetIndex);
                if (dataset != null) {
                    final Comparable seriesKey = dataset.getSeriesKey(series);
                    final Boolean visible = isSeriesPointsVisible(seriesKey);
                    return visible != null ? visible : super.getSeriesShapesVisible(series);
                }
                return super.getSeriesShapesVisible(series);
            } catch (Exception ex) {
                ex.printStackTrace();
                return super.getSeriesShapesVisible(series);
            }
        }

        @Override
        public Boolean getSeriesLinesVisible(int series) {
            try {
                final XYPlot plot = getPlot();
                final XYDataset dataset = plot.getDataset(datasetIndex);
                if (dataset != null) {
                    final Comparable seriesKey = dataset.getSeriesKey(series);
                    final float lineWidth = getSeriesLineWidth(seriesKey);
                    if (lineWidth == 0f) {
                        return false;
                    }
                }
                return super.getSeriesLinesVisible(series);
            } catch (Exception ex) {
                ex.printStackTrace();
                return super.getSeriesLinesVisible(series);
            }
        }

        @Override
        public Shape getSeriesShape(int series) {
            try {
                final XYPlot plot = getPlot();
                final XYDataset dataset = plot.getDataset(datasetIndex);
                if (dataset != null) {
                    final Comparable seriesKey = dataset.getSeriesKey(series);
                    final ChartShape pointShape = getSeriesPointShape(seriesKey);
                    if (pointShape != null) {
                        final Shape shape = shapes.getShape(pointShape);
                        if (shape != null) return shape;
                    }
                }
                return super.getSeriesShape(series);
            } catch (Exception ex) {
                ex.printStackTrace();
                return super.getSeriesShape(series);
            }
        }
    }


    /**
     * A Morpheus extension for drawing bars in an XY plot
     */
    private class MorpheusBarRenderer extends XYBarRenderer {

        private int datasetIndex;

        /**
         * Constructor
         * @param datasetIndex  the dataset index
         */
        MorpheusBarRenderer(int datasetIndex) {
            this.datasetIndex = datasetIndex;
            this.setBarPainter(new StandardXYBarPainter());
            this.setDrawBarOutline(true);
            this.setShadowVisible(false);
        }


        @Override
        public Paint getSeriesPaint(int series) {
            try {
                final XYPlot plot = getPlot();
                final XYDataset dataset = plot.getDataset(datasetIndex);
                if (dataset != null) {
                    final Comparable seriesKey = dataset.getSeriesKey(series);
                    final Color color = getSeriesColor(seriesKey);
                    return color != null ? color : getColorModel().getColor(seriesKey);
                }
                return super.getSeriesPaint(series);
            } catch (Exception ex) {
                ex.printStackTrace();
                return super.getSeriesPaint(series);
            }
        }

    }

}
