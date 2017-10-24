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
import java.awt.geom.Ellipse2D;

import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.AreaRenderer;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.CategoryDataset;

import com.zavtech.morpheus.viz.chart.xy.XyRender;

/**
 * The rendering adapter for category plots
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
class JFCatRender implements XyRender {

    private int index;
    private JFCatPlot plot;

    /**
     * Constructor
     * @param index     the dataset index
     * @param plot      the plot adapter
     */
    JFCatRender(JFCatPlot plot, int index) {
        this.plot = plot;
        this.index = index;
    }

    @Override()
    public void withDots() {
        this.withDots(4);
    }


    @Override
    public void withDots(int diameter) {
        this.plot.underlying().setRenderer(index, new MorpheusDotRenderer(index, diameter));
    }


    @Override()
    public void withShapes() {
        this.plot.underlying().setRenderer(index, new LineAndShapeRenderer(false, false));
    }


    @Override()
    public void withArea(boolean shapes) {
        this.plot.underlying().setRenderer(index, new AreaRenderer());
    }


    @Override()
    public void withBars(boolean stacked, double margin) {
        this.plot.underlying().setRenderer(index, stacked ? new MorpheusStackedBarRenderer(index) : new MorpheusBarRenderer(index));
    }


    @Override()
    public void withLines(boolean shapes, boolean dashed) {
        this.plot.underlying().setRenderer(index, new LineAndShapeRenderer(true, false));
    }


    @Override()
    public void withSpline(boolean shapes, boolean dashed) {
        throw new UnsupportedOperationException("Not supported for category plots");
    }


    /**
     * A Morpheus extension of the standard BarRenderer
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
                final CategoryPlot catPlot = getPlot();
                final CategoryDataset dataset = catPlot.getDataset(datasetIndex);
                if (dataset != null) {
                    final Comparable seriesKey = dataset.getRowKey(series);
                    final Color color = plot.getSeriesColor(seriesKey);
                    return color != null ? color : plot.getColorModel().getColor(seriesKey);
                }
                return super.getSeriesPaint(series);
            } catch (Exception ex) {
                ex.printStackTrace();
                return super.getSeriesPaint(series);
            }
        }
    }


    /**
     * A Morpheus extension of the standard StackedBarRenderer
     */
    private class MorpheusStackedBarRenderer extends StackedBarRenderer {

        private int datasetIndex;

        /**
         * Constructor
         * @param datasetIndex  the dataset index
         */
        MorpheusStackedBarRenderer(int datasetIndex) {
            this.datasetIndex = datasetIndex;
            this.setBarPainter(new StandardBarPainter());
            this.setDrawBarOutline(true);
            this.setShadowVisible(false);
        }

        @Override
        public Paint getSeriesPaint(int series) {
            try {
                final CategoryPlot catPlot = getPlot();
                final CategoryDataset dataset = catPlot.getDataset(datasetIndex);
                if (dataset != null) {
                    final Comparable seriesKey = dataset.getRowKey(series);
                    final Color color = plot.getSeriesColor(seriesKey);
                    return color != null ? color : plot.getColorModel().getColor(seriesKey);
                }
                return super.getSeriesPaint(series);
            } catch (Exception ex) {
                ex.printStackTrace();
                return super.getSeriesPaint(series);
            }
        }
    }

    /**
     * A Morpheus render for rendering dots on a category plot
     */
    private class MorpheusDotRenderer extends LineAndShapeRenderer {


        private int datasetIndex;
        private Shape dotShape;

        /**
         * Constructor
         * @param datasetIndex  the dataset index
         * @param diameter      the diameter in pixels
         */
        MorpheusDotRenderer(int datasetIndex, int diameter) {
            this.datasetIndex = datasetIndex;
            this.dotShape = new Ellipse2D.Double(-4/2, -4/2, diameter, diameter);
            this.setBaseShape(dotShape);
            this.setBaseLinesVisible(false);
            this.setBaseShapesVisible(true);
        }


        @Override
        public Paint getSeriesPaint(int series) {
            try {
                final CategoryPlot categoryPlot = getPlot();
                final CategoryDataset dataset = categoryPlot.getDataset(datasetIndex);
                if (dataset != null) {
                    final Comparable seriesKey = dataset.getRowKey(series);
                    final Color color = plot.getSeriesColor(seriesKey);
                    return color != null ? color : plot.getColorModel().getColor(seriesKey);
                }
                return super.getSeriesPaint(series);
            } catch (Exception ex) {
                ex.printStackTrace();
                return super.getSeriesPaint(series);
            }
        }


        @Override
        public Shape getSeriesShape(int series) {
            return super.getSeriesShape(series);
        }
    }

}