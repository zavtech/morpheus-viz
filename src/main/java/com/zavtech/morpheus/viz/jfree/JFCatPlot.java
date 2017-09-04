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

import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;

import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.viz.chart.ChartException;
import com.zavtech.morpheus.viz.chart.xy.XyAxes;
import com.zavtech.morpheus.viz.chart.xy.XyDataset;
import com.zavtech.morpheus.viz.chart.xy.XyModel;
import com.zavtech.morpheus.viz.chart.xy.XyOrient;
import com.zavtech.morpheus.viz.chart.xy.XyPlot;
import com.zavtech.morpheus.viz.chart.xy.XyPlotBase;
import com.zavtech.morpheus.viz.chart.xy.XyRender;
import com.zavtech.morpheus.viz.chart.xy.XyTrend;

/**
 * The plot definition for category plots
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
class JFCatPlot<X extends Comparable> extends XyPlotBase<X> implements XyPlot<X> {

    private CategoryPlot plot;
    private Map<Integer,JFCatDataset<X,? extends Comparable>> datasetMap = new LinkedHashMap<>();

    /**
     * Constructor
     * @param domainAxis    the domain axis
     * @param rangeAxis     the range axis
     */
    JFCatPlot(CategoryAxis domainAxis, ValueAxis rangeAxis) {
        this.plot = new CategoryPlot(null, domainAxis, rangeAxis, null);
        this.plot.getRangeAxis().setAutoRange(true);
        this.plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
        this.plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        this.plot.setDomainGridlinesVisible(true);
        this.plot.setRangeGridlinesVisible(true);
        this.plot.setDomainGridlinePaint(Color.DARK_GRAY);
        this.plot.setRangeGridlinePaint(Color.DARK_GRAY);
        this.plot.setDatasetRenderingOrder(DatasetRenderingOrder.REVERSE);
        if (rangeAxis instanceof NumberAxis) {
            ((NumberAxis)rangeAxis).setAutoRangeIncludesZero(false);
        }
    }

    /**
     * Returns the underlying JFreeChart plot object
     * @return      the underlying plot object
     */
    CategoryPlot underlying() {
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
    public <S extends Comparable> XyTrend trend(S seriesKey) {
        throw new UnsupportedOperationException("Trend lines are not supported for categorical / discrete XY plots");
    }


    @Override
    public XyRender render(int index) {
        return new JFCatRender(this, index);
    }


    /**
     * Returns the number of none null data sets for this plot
     * @return      the data set count for plot
     */
    private int getDatasetCount() {
        int count = 0;
        for (int i=0; i<plot.getDatasetCount(); ++i) {
            final CategoryDataset dataSet = plot.getDataset(i);
            if (dataSet != null) {
                ++count;
            }
        }
        return count;
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
     * An XyModel adapter for category plots
     */
    private class ModelAdapter<S extends Comparable> implements XyModel<X,S> {

        /**
         * Constructor
         */
        private ModelAdapter() {
            super();
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
        public XyDataset<X,S> at(int index) {
            final JFCatDataset<X,?> dataset = datasetMap.get(index);
            if (dataset != null) return (XyDataset<X,S>)dataset;
            else throw new IllegalArgumentException("No chart data located at index: " + index);
        }

        @Override
        public int add(DataFrame<X,S> frame) {
            final int index = getDatasetCount();
            final JFCatDataset<X,S> dataset = JFCatDataset.of(() -> frame);
            datasetMap.put(index, dataset);
            plot.setDataset(index, dataset);
            return index;
        }

        @Override
        public int add(DataFrame<?, S> frame, S domainKey) {
            final int index = getDatasetCount();
            final JFCatDataset<X,S> dataset = JFCatDataset.of(domainKey, () -> frame);
            datasetMap.put(index, dataset);
            plot.setDataset(index, dataset);
            return index;
        }

        @Override
        public XyDataset<X,S> update(int index, DataFrame<X, S> frame) {
            final JFCatDataset<X,S> dataset = JFCatDataset.of(() -> frame);
            datasetMap.put(index, dataset);
            plot.setDataset(index, dataset);
            return dataset;
        }

        @Override
        public XyDataset<X,S> update(int index, DataFrame<?, S> frame, S domainKey) {
            final JFCatDataset<X,S> dataset = JFCatDataset.of(domainKey, () -> frame);
            datasetMap.put(index, dataset);
            plot.setDataset(index, dataset);
            return dataset;
        }

        @Override
        public void remove(int index) {
            final XyDataset<X,?> dataset = datasetMap.remove(index);
            if (dataset == null) {
                throw new ChartException("No chart data model exists for id: " + index);
            } else {
                plot.setDataset(index, null);
            }
        }

        @Override
        public void removeAll() {
            final int count = getDatasetCount();
            datasetMap.clear();
            for (int i=0; i<count; ++i) {
                plot.setDataset(i, null);
            }
        }
    }

}
