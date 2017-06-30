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
import java.util.LinkedHashMap;
import java.util.Map;

import com.zavtech.morpheus.viz.chart.ChartData;
import com.zavtech.morpheus.viz.chart.ChartException;
import com.zavtech.morpheus.viz.chart.ChartModel;
import com.zavtech.morpheus.viz.chart.ChartPlotStyle;
import com.zavtech.morpheus.viz.chart.ChartTrendLine;
import com.zavtech.morpheus.frame.DataFrame;

import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.AreaRenderer;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.xy.XYDataset;

/**
 * A Chart used to implement Category plots
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
class JFChartCategory<X extends Comparable> extends JFChartBase<CategoryPlot,X> {

    private DataAdapter dataAdapter;


    /**
     * Constructor
     * @param rangeAxis     the range axis for chart
     */
    JFChartCategory(ValueAxis rangeAxis) {
        super(new CategoryPlot(null, new CategoryAxis(), rangeAxis, null), false);
        this.dataAdapter = new DataAdapter(getPlot());
        this.getPlot().setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
        this.getPlot().setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        this.getPlot().setDomainGridlinesVisible(true);
        this.getPlot().setRangeGridlinesVisible(true);
        this.getPlot().setDomainGridlinePaint(Color.DARK_GRAY);
        this.getPlot().setRangeGridlinePaint(Color.DARK_GRAY);
        if (rangeAxis instanceof NumberAxis) {
            ((NumberAxis)rangeAxis).setAutoRangeIncludesZero(false);
        }
    }

    @Override
    public ChartData<X> data() {
        return dataAdapter;
    }

    @Override
    public ChartTrendLine trendLine() {
        return null;
    }

    @Override
    public ChartPlotStyle plot(int index) {
        return new PlotStyle(index, getPlot());
    }


    /**
     * Returns the number of none null data sets for this plot
     * @return      the data set count for plot
     */
    private int getDatasetCount() {
        int count = 0;
        for (int i=0; i<getPlot().getDatasetCount(); ++i) {
            final CategoryDataset dataSet = getPlot().getDataset(i);
            if (dataSet != null) {
                ++count;
            }
        }
        return count;
    }


    /**
     * A ChartData adapter implementation used to manage datasets on a JFreeChart XYPlot
     */
    private class DataAdapter implements ChartData<X> {

        private CategoryPlot plot;
        private Map<Integer,ChartModel<X,? extends Comparable>> modelMap = new LinkedHashMap<>();

        /**
         * Constructor
         * @param plot  the plot for this chart
         */
        private DataAdapter(CategoryPlot plot) {
            this.plot = plot;
        }

        @Override
        public ChartData<X> setRangeAxis(int dataset, int axis) {
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <S extends Comparable> ChartModel<X,S> at(int index) {
            final ChartModel<X,? extends Comparable> model = modelMap.get(index);
            if (model != null) return (ChartModel<X,S>)model;
            else throw new IllegalArgumentException("No chart data located at index: " + index);
        }

        @Override
        public <S extends Comparable> ChartModel<X,S> add(DataFrame<X,S> frame) {
            final int index = getDatasetCount();
            final ChartModel<X,S> model = ChartModel.of(() -> frame);
            final CategoryDataset dataset = JFChartData.ofCategories(model);
            this.modelMap.put(index, model);
            this.plot.setDataset(index, dataset);
            return model;
        }

        @Override
        public <S extends Comparable> ChartModel<X,S> add(DataFrame<?, S> frame, S domainKey) {
            final int index = getDatasetCount();
            final ChartModel<X,S> model = ChartModel.of(domainKey, () -> frame);
            final CategoryDataset dataset = JFChartData.ofCategories(model);
            this.modelMap.put(index, model);
            this.plot.setDataset(index, dataset);
            return model;
        }

        @Override
        public <S extends Comparable> ChartModel<X,S> update(int index, DataFrame<X, S> frame) {
            final ChartModel<X,S> model = ChartModel.of(() -> frame);
            final CategoryDataset dataset = JFChartData.ofCategories(model);
            this.modelMap.put(index, model);
            this.plot.setDataset(index, dataset);
            return model;
        }

        @Override
        public <S extends Comparable> ChartModel<X,S> update(int index, DataFrame<?, S> frame, S domainKey) {
            final ChartModel<X,S> model = ChartModel.of(domainKey, () -> frame);
            final CategoryDataset dataset = JFChartData.ofCategories(model);
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
     * The style implementation for this chart
     */
    private class PlotStyle implements ChartPlotStyle {

        private int index;
        private CategoryPlot plot;

        /**
         * Constructor
         * @param index     the dataset index
         * @param plot      the plot for this style
         */
        private PlotStyle(int index, CategoryPlot plot) {
            this.index = index;
            this.plot = plot;
        }

        @Override()
        public ChartPlotStyle withArea(boolean shapes) {
            this.plot.setRenderer(index, new AreaRenderer());
            return this;
        }

        @Override()
        public ChartPlotStyle withBars(double margin) {
            this.plot.setRenderer(index, new MorpheusBarRenderer(index));
            return this;
        }

        @Override()
        public ChartPlotStyle withLines() {
            this.plot.setRenderer(index, new LineAndShapeRenderer(true, false));
            return this;
        }

        @Override()
        public ChartPlotStyle withPoints() {
            this.plot.setRenderer(index, new LineAndShapeRenderer(false, false));
            return this;
        }

        @Override()
        public ChartPlotStyle withLinesAndPoints() {
            this.plot.setRenderer(index, new LineAndShapeRenderer(true, true));
            return this;
        }

        @Override()
        public ChartPlotStyle withSpline() {
            throw new UnsupportedOperationException("Not supported for category plots");
        }

        @Override
        public ChartPlotStyle withStackedBars(double marked) {
            final StackedBarRenderer renderer = new StackedBarRenderer();
            renderer.setBarPainter(new StandardBarPainter());
            renderer.setDrawBarOutline(true);
            renderer.setShadowVisible(false);
            this.plot.setRenderer(index, renderer);
            return this;
        }
    }


    /**
     * An extension of the standard bar renderer for painting bars.
     */
    private class MorpheusBarRenderer extends BarRenderer {

        private int datasetIndex;

        /**
         * Constructor
         * @param datasetIndex  the dataset index
         */
        MorpheusBarRenderer(int datasetIndex) {
            this.datasetIndex = datasetIndex;
            this.setBarPainter(new StandardBarPainter());
            this.setDrawBarOutline(true);
            this.setShadowVisible(false);
            this.setItemMargin(0.01d);
        }


        @Override
        public Paint getSeriesPaint(int series) {
            try {
                final CategoryPlot plot = getPlot();
                final CategoryDataset dataset = plot.getDataset(datasetIndex);
                if (dataset != null) {
                    final Comparable seriesKey = dataset.getRowKey(series);
                    System.out.println("Series: " + seriesKey);
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
