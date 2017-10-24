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

import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StackedXYAreaRenderer2;
import org.jfree.chart.renderer.xy.StackedXYBarRenderer;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYAreaRenderer2;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYDataset;

import com.zavtech.morpheus.viz.chart.ChartShape;
import com.zavtech.morpheus.viz.chart.xy.XyRender;

/**
 * The rendering implementation for xy plots
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
class JFXyRender implements XyRender {

    private int index;
    private JFXyPlot<?> plot;


    /**
     * Constructor
     * @param plot      the plot reference
     * @param index     the dataset index
     */
    JFXyRender(JFXyPlot<?> plot, int index) {
        this.plot = plot;
        this.index = index;
    }

    @Override
    public void withDots() {
        this.withDots(4);
    }


    @Override
    public void withDots(int diameter) {
        this.plot.underlying().setRenderer(index, new MorpheusDotRenderer(index, diameter));
    }


    @Override()
    public void withShapes() {
        this.plot.underlying().setRenderer(index, new MorpheusLineAndShapeRenderer(false, true, index, false));
    }


    @Override()
    public void withArea(boolean stacked) {
        this.plot.underlying().setRenderer(index, stacked ? new MorpheusStackedAreaRenderer(index) : new MorpheusAreaRenderer(index));
    }


    @Override()
    public void withLines(boolean shapes, boolean dashed) {
        this.plot.underlying().setRenderer(index, new MorpheusLineAndShapeRenderer(true, shapes, index, dashed));
    }


    @Override()
    public void withSpline(boolean shapes, boolean dashed) {
        this.plot.underlying().setRenderer(index, new MorpheusSplineRenderer(shapes, 5, index, dashed));
    }


    @Override()
    public void withBars(boolean stacked, double margin) {
        this.plot.underlying().setRenderer(index, stacked ? new MorpheusStackedBarRenderer(margin, index) : new MorpheusBarRenderer(index));
    }



    /**
     * An extension of a JFreeChart renderer that integrates support for series specific styling
     */
    private class MorpheusLineAndShapeRenderer extends XYLineAndShapeRenderer {

        private int datasetIndex;
        private JFChartShapes shapes = new JFChartShapes();

        /**
         * Constructor
         * @param lines         true for lines to be shown by default
         * @param shapes        true for points to be displayed by default
         * @param datasetIndex  the dataset index for this renderer
         * @param dotted        true for dotted lines
         */
        MorpheusLineAndShapeRenderer(boolean lines, boolean shapes, int datasetIndex, boolean dotted) {
            super(lines, shapes);
            this.datasetIndex = datasetIndex;
            if (dotted) {
                this.setBaseStroke(new BasicStroke(
                    2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    1.0f, new float[] {2.0f, 6.0f}, 0.0f
                ));
            }
            if (!lines && shapes) {
                this.setDrawOutlines(true);
                this.setUseOutlinePaint(true);
                this.setBaseOutlinePaint(Color.GRAY);
            }
        }

        @Override
        public Paint getSeriesPaint(int series) {
            try {
                final XYPlot xyPlot = getPlot();
                final XYDataset dataset = xyPlot.getDataset(datasetIndex);
                if (dataset != null) {
                    final Comparable seriesKey = dataset.getSeriesKey(series);
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
        public Stroke getSeriesStroke(int series) {
            try {
                final XYPlot xyPlot = getPlot();
                final XYDataset dataset = xyPlot.getDataset(datasetIndex);
                if (dataset != null) {
                    final Comparable seriesKey = dataset.getSeriesKey(series);
                    final Float lineWidth = plot.getSeriesLineWidth(seriesKey);
                    final Boolean dashed = plot.isSeriesDashedLine(seriesKey);
                    if (dashed != null && dashed) {
                        final float[] dash = new float[] {2.0f, 6.0f};
                        final float width = lineWidth != null && !Float.isNaN(lineWidth) ? lineWidth : 1f;
                        return new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, dash, 0.0f);
                    } else {
                        return lineWidth != null && !Float.isNaN(lineWidth) ? new BasicStroke(lineWidth) : super.getBaseStroke();
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
                final XYPlot xyPlot = getPlot();
                final XYDataset dataset = xyPlot.getDataset(datasetIndex);
                if (dataset != null) {
                    final Comparable seriesKey = dataset.getSeriesKey(series);
                    final Boolean visible = plot.isSeriesPointsVisible(seriesKey);
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
                final XYPlot xyPlot = getPlot();
                final XYDataset dataset = xyPlot.getDataset(datasetIndex);
                if (dataset != null) {
                    final Comparable seriesKey = dataset.getSeriesKey(series);
                    final Float lineWidth = plot.getSeriesLineWidth(seriesKey);
                    if (lineWidth != null && lineWidth == 0f) {
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
                final XYPlot xyPlot = getPlot();
                final XYDataset dataset = xyPlot.getDataset(datasetIndex);
                if (dataset != null) {
                    final Comparable seriesKey = dataset.getSeriesKey(series);
                    final ChartShape pointShape = plot.getSeriesPointShape(seriesKey);
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
    private class MorpheusSplineRenderer extends XYSplineRenderer {

        private int datasetIndex;
        private JFChartShapes shapes = new JFChartShapes();

        /**
         * Constructor
         * @param shapes        true to include shapes at each datum value
         * @param precision     the number of points between data items
         * @param datasetIndex  the dataset index for this renderer
         */
        MorpheusSplineRenderer(boolean shapes, int precision, int datasetIndex, boolean dotted) {
            super(precision);
            this.setBaseShapesVisible(shapes);
            this.datasetIndex = datasetIndex;
            if (dotted) {
                this.setBaseStroke(new BasicStroke(
                    2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    1.0f, new float[] {2.0f, 6.0f}, 0.0f
                ));
            }
        }

        @Override
        public Paint getSeriesPaint(int series) {
            try {
                final XYPlot xyPlot = getPlot();
                final XYDataset dataset = xyPlot.getDataset(datasetIndex);
                if (dataset != null) {
                    final Comparable seriesKey = dataset.getSeriesKey(series);
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
        public Stroke getSeriesStroke(int series) {
            try {
                final XYPlot xyPlot = getPlot();
                final XYDataset dataset = xyPlot.getDataset(datasetIndex);
                if (dataset != null) {
                    final Comparable seriesKey = dataset.getSeriesKey(series);
                    final Float lineWidth = plot.getSeriesLineWidth(seriesKey);
                    final Boolean dashed = plot.isSeriesDashedLine(seriesKey);
                    if (dashed != null && dashed) {
                        final float[] dash = new float[] {2.0f, 6.0f};
                        final float width = !Float.isNaN(lineWidth) ? lineWidth : 1f;
                        return new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, dash, 0.0f);
                    } else {
                        return lineWidth != null && !Float.isNaN(lineWidth) ? new BasicStroke(lineWidth) : super.getSeriesStroke(series);
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
                final XYPlot xyPlot = getPlot();
                final XYDataset dataset = xyPlot.getDataset(datasetIndex);
                if (dataset != null) {
                    final Comparable seriesKey = dataset.getSeriesKey(series);
                    final Boolean visible = plot.isSeriesPointsVisible(seriesKey);
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
                final XYPlot xyPlot = getPlot();
                final XYDataset dataset = xyPlot.getDataset(datasetIndex);
                if (dataset != null) {
                    final Comparable seriesKey = dataset.getSeriesKey(series);
                    final Float lineWidth = plot.getSeriesLineWidth(seriesKey);
                    if (lineWidth != null && lineWidth == 0f) {
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
                final XYPlot xyPlot = getPlot();
                final XYDataset dataset = xyPlot.getDataset(datasetIndex);
                if (dataset != null) {
                    final Comparable seriesKey = dataset.getSeriesKey(series);
                    final ChartShape pointShape = plot.getSeriesPointShape(seriesKey);
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
     * A Morpheus extension for drawing withBarPlot in an XY plot
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
                final XYPlot xyPlot = getPlot();
                final XYDataset dataset = xyPlot.getDataset(datasetIndex);
                if (dataset != null) {
                    final Comparable seriesKey = dataset.getSeriesKey(series);
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
     * A Morpheus extension for drawing stacked withBarPlot in an XY plot
     */
    private class MorpheusStackedBarRenderer extends StackedXYBarRenderer {

        private int datasetIndex;

        /**
         * Constructor
         * @param margin    the margin for this renderer
         * @param datasetIndex  the dataset index for this renderer
         */
        MorpheusStackedBarRenderer(double margin, int datasetIndex) {
            super(margin);
            this.datasetIndex = datasetIndex;
            this.setBarPainter(new StandardXYBarPainter());
            this.setDrawBarOutline(true);
            this.setShadowVisible(false);
        }

        @Override
        public Paint getSeriesPaint(int series) {
            try {
                final XYPlot xyPlot = getPlot();
                final XYDataset dataset = xyPlot.getDataset(datasetIndex);
                if (dataset != null) {
                    final Comparable seriesKey = dataset.getSeriesKey(series);
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
     * A Morpheus extension for drawing scatter charts with dots
     */
    private class MorpheusDotRenderer extends XYLineAndShapeRenderer {

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
            this.setBaseToolTipGenerator(plot::getXyTooltip);
        }

        public Shape getSeriesShape(int series) {
            return dotShape;
        }


        @Override
        public Paint getSeriesPaint(int series) {
            try {
                final XYPlot xyPlot = getPlot();
                final XYDataset dataset = xyPlot.getDataset(datasetIndex);
                if (dataset != null) {
                    final Comparable seriesKey = dataset.getSeriesKey(series);
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
     * A Morpheus extension for drawing Area Charts
     */
    private class MorpheusAreaRenderer extends XYAreaRenderer2 {

        private int datasetIndex;

        /**
         * Constructor
         * @param datasetIndex  the dataset index for renderer
         */
        MorpheusAreaRenderer(int datasetIndex) {
            this.datasetIndex = datasetIndex;
            this.setOutline(false);
            this.setBaseCreateEntities(false);
        }

        @Override
        public Paint getSeriesPaint(int series) {
            try {
                final XYPlot xyPlot = getPlot();
                final XYDataset dataset = xyPlot.getDataset(datasetIndex);
                if (dataset != null) {
                    final Comparable seriesKey = dataset.getSeriesKey(series);
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
     * A Morpheus extension for drawing Area Charts
     */
    private class MorpheusStackedAreaRenderer extends StackedXYAreaRenderer2 {

        private int datasetIndex;

        /**
         * Constructor
         * @param datasetIndex  the dataset index for renderer
         */
        MorpheusStackedAreaRenderer(int datasetIndex) {
            this.datasetIndex = datasetIndex;
            //this.setOutline(false);
            //this.setBaseCreateEntities(false);
        }

        @Override
        public Paint getSeriesPaint(int series) {
            try {
                final XYPlot xyPlot = getPlot();
                final XYDataset dataset = xyPlot.getDataset(datasetIndex);
                if (dataset != null) {
                    final Comparable seriesKey = dataset.getSeriesKey(series);
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


}
