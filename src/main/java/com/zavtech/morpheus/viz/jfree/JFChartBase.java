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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import javax.swing.JFrame;

import com.zavtech.morpheus.util.Bounds;
import com.zavtech.morpheus.viz.chart.Chart;
import com.zavtech.morpheus.viz.chart.ChartAxes;
import com.zavtech.morpheus.viz.chart.ChartAxis;
import com.zavtech.morpheus.viz.chart.ChartBase;
import com.zavtech.morpheus.viz.chart.ChartException;
import com.zavtech.morpheus.viz.chart.ChartFormat;
import com.zavtech.morpheus.viz.chart.ChartLabel;
import com.zavtech.morpheus.viz.chart.ChartLegend;
import com.zavtech.morpheus.viz.chart.ChartOrientation;
import com.zavtech.morpheus.viz.chart.ChartTextStyle;
import com.zavtech.morpheus.viz.util.ColorModel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.data.Range;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

/**
 * A convenience base class for building various types of chart types
 * @param <T>   the plot type
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
abstract class JFChartBase<T extends Plot,X extends Comparable> extends ChartBase<X> {

    private T plot;
    private JFreeChart freeChart;
    private ChartPanel chartPanel;
    private ColorModel colorModel;

    /**
     * Constructor
     * @param plot      the plot for this chart
     * @param legend    true to enable legend
     */
    JFChartBase(T plot, boolean legend) {
        this.plot = plot;
        this.colorModel = new ColorModel.CIEModel();
        this.freeChart = new JFreeChart(null, new Font("Arial", Font.PLAIN, 4), plot, legend);
        this.freeChart.setBackgroundPaint(Color.WHITE);
        this.chartPanel = new ChartPanel(freeChart);
        this.chartPanel.setMouseZoomable(true);
        this.chartPanel.setRefreshBuffer(true);
    }

    @Override
    public ChartLabel title() {
        return new TitleAdapter(freeChart, false);
    }

    @Override
    public ChartLabel subtitle() {
        return new TitleAdapter(freeChart, true);
    }

    @Override
    public ChartAxes axes() {
        return new AxesAdapter(freeChart.getPlot());
    }

    @Override
    public ChartLegend legend() {
        return new LegendAdapter(freeChart);
    }

    @Override
    public ChartOrientation orientation() {
        return new OrientationAdapter(freeChart.getPlot());
    }

    @Override
    public Chart withColorModel(ColorModel colorModel) {
        this.colorModel = colorModel;
        return this;
    }

    /**
     * Returns the plot for this chart
     * @return  the plot for chart
     */
    public T getPlot() {
        return plot;
    }

    /**
     * Returns a reference to the chart panel
     * @return  the JFreeChart chart panel
     */
    public ChartPanel getChartPanel() {
        return chartPanel;
    }

    /**
     * Returns the JFreeChart reference for this chart
     * @return  the JFreeChart reference
     */
    public JFreeChart getFreeChart() {
        return freeChart;
    }

    /**
     * Returns a reference to the color model for this chart
     * @return      the color model for chart
     */
    public ColorModel getColorModel() {
        return colorModel;
    }

    /**
     * Creates a JFrame containing this Chart and sets it visible
     * @return          the JFrame
     */
    public Chart show() {
        return show(1024, 600);
    }

    /**
     * Creates a JFrame containing this Chart and sets it visible
     * @param width     the frame width
     * @param height    the frame height
     * @return          the JFrame
     */
    public Chart show(int width, int height) {
        final JFrame frame = new JFrame();
        frame.getContentPane().setLayout(new BorderLayout(0, 0));
        frame.getContentPane().add(chartPanel, BorderLayout.CENTER);
        frame.getContentPane().setBackground(Color.WHITE);
        frame.pack();
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
        return this;
    }

    @Override
    public Chart writerPng(File file, int width, int height) {
        BufferedOutputStream os = null;
        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                if (!parent.mkdirs()) {
                    System.err.printf("Unable to create directory for %s", file.getAbsolutePath());
                }
            }
            os = new BufferedOutputStream(new FileOutputStream(file));
            writerPng(os, width, height);
            return this;
        } catch (Exception ex) {
            throw new ChartException(ex.getMessage(), ex);
        } finally {
            try {
                if (os != null) {
                    os.flush();
                    os.close();;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public Chart writerPng(OutputStream os, int width, int height) {
        try {
            ChartUtilities.writeChartAsPNG(os, freeChart, width, height);
            return this;
        } catch (Exception ex) {
            throw new ChartException(ex.getMessage(), ex);
        }
    }

    /**
     * A JFreeChart adapter for the ChartAxes interface
     */
    private class AxesAdapter implements ChartAxes {

        private Plot plot;

        /**
         * Constructor
         * @param plot  the plot reference
         */
        private AxesAdapter(Plot plot) {
            this.plot = plot;
        }

        @Override
        public ChartAxis domain() {
            if (plot instanceof XYPlot) {
                final XYPlot xyPlot = (XYPlot)plot;
                return new AxisAdapter(xyPlot.getDomainAxis(), 0, true);
            } else if (plot instanceof CategoryPlot) {
                final CategoryPlot categoryPlot = (CategoryPlot)getPlot();
                return new AxisAdapter(categoryPlot.getDomainAxis(), 0, true);
            } else {
                throw new RuntimeException("Unsupported plot type: " + plot.getPlotType());
            }
        }

        @Override
        public ChartAxis range(int index) {
            if (plot instanceof XYPlot) {
                final XYPlot xyPlot = (XYPlot)plot;
                final Axis axis = xyPlot.getRangeAxis(index);
                if (axis != null) {
                    return new AxisAdapter(axis, index, false);
                } else {
                    final ValueAxis newAxis = new NumberAxis();
                    xyPlot.setRangeAxis(index, newAxis);
                    return new AxisAdapter(newAxis, index, false);
                }
            } else if (plot instanceof CategoryPlot) {
                final CategoryPlot categoryPlot = (CategoryPlot)getPlot();
                final Axis axis = categoryPlot.getRangeAxis(index);
                if (axis != null) {
                    return new AxisAdapter(axis, index, false);
                } else {
                    final ValueAxis newAxis = new NumberAxis();
                    categoryPlot.setRangeAxis(index, newAxis);
                    return new AxisAdapter(newAxis, index, false);
                }
            } else {
                throw new RuntimeException("Unsupported plot type: " + plot.getPlotType());
            }
        }
    }


    /**
     * An adapter implementation for the ChartOrientation interface
     */
    private class OrientationAdapter implements ChartOrientation {

        private Plot plot;

        /**
         * Constructor
         * @param plot  the plot reference
         */
        private OrientationAdapter(Plot plot) {
            this.plot = plot;
        }

        @Override
        public void vertical() {
            if (plot instanceof XYPlot) {
                ((XYPlot)plot).setOrientation(PlotOrientation.VERTICAL);
            } else if (plot instanceof CategoryPlot) {
                ((CategoryPlot)plot).setOrientation(PlotOrientation.VERTICAL);
            } else {
                throw new ChartException("Unsupported plot type: " + plot.getClass());
            }
        }

        @Override
        public void horizontal() {
            if (plot instanceof XYPlot) {
                ((XYPlot)plot).setOrientation(PlotOrientation.HORIZONTAL);
            } else if (plot instanceof CategoryPlot) {
                ((CategoryPlot)plot).setOrientation(PlotOrientation.HORIZONTAL);
            } else {
                throw new ChartException("Unsupported plot type: " + plot.getClass());
            }
        }
    }


    /**
     * A JFreeChart ChartLabel adapter for the chart title
     */
    private class TitleAdapter implements ChartLabel {

        private JFreeChart chart;
        private boolean subtitle;

        /**
         * Constructor
         * @param chart the chart reference
         * @param subtitle  true if this represents a subtitle
         */
        private TitleAdapter(JFreeChart chart, boolean subtitle) {
            this.chart = chart;
            this.subtitle = subtitle;
        }

        @Override
        public ChartLabel withText(String text) {
            this.getTitle().setText(text);
            return this;
        }

        @Override
        public ChartLabel withColor(Color color) {
            this.getTitle().setPaint(color);
            return this;
        }

        @Override
        public ChartLabel withFont(Font font) {
            this.getTitle().setFont(font);
            return this;
        }

        /**
         * Returns the title for the chart
         * @return  the chart title
         */
        private TextTitle getTitle() {
            if (subtitle) {
                if (chart.getSubtitleCount() > 0) {
                    for (int i=0; i<chart.getSubtitleCount(); ++i) {
                        final Title title = chart.getSubtitle(i);
                        if (title instanceof TextTitle) {
                            return (TextTitle)title;
                        }
                    }
                }
                final TextTitle subtitle = new TextTitle();
                this.chart.addSubtitle(subtitle);
                return subtitle;
            } else {
                TextTitle title = chart.getTitle();
                if (title == null) {
                    title = new TextTitle();
                    title.setFont(new Font("Arial", Font.BOLD, 15));
                    chart.setTitle(title);
                }
                return title;
            }
        }
    }


    /**
     * A JFreeChart ChartAxis adapter for a ValueAxis
     */
    private class AxisAdapter implements ChartAxis {

        private Axis axis;
        private int index;
        private boolean domain;
        private Bounds<?> range;

        /**
         * Constructor
         * @param axis      the axis reference
         * @param index     the index for this axis
         * @param domain    true if adapter is for domain axis
         */
        private AxisAdapter(Axis axis, int index, boolean domain) {
            this.axis = axis;
            this.index = index;
            this.domain = domain;
        }

        @Override
        public ChartLabel label() {
            return new AxisLabelAdapter(axis);
        }

        @Override
        public ChartFormat format() {
            return new AxisFormatAdapter(axis);
        }

        @Override
        public ChartTextStyle ticks() {
            return new AxisStyleAdapter(axis);
        }

        @Override
        public ChartAxis asLogScale() {
            if (axis instanceof CategoryAxis) {
                throw new ChartException("Cannot convert discrete axis to continuous axis");
            } else if (!(axis instanceof LogAxis)) {
                final String label = axis.getLabel();
                final LogAxis logAxis = new LogAxis(label);
                logAxis.setLabelFont(axis.getLabelFont());
                logAxis.setTickLabelFont(axis.getTickLabelFont());
                logAxis.setLabelPaint(axis.getLabelPaint());
                logAxis.setTickLabelPaint(axis.getTickLabelPaint());
                this.axis = logAxis;
                if (range != null) withRange(range);
                final Plot plot = getPlot();
                if (domain && plot instanceof XYPlot) {
                    ((XYPlot)plot).setDomainAxis(index, logAxis);
                } else if (!domain && plot instanceof XYPlot) {
                    ((XYPlot)plot).setRangeAxis(index, logAxis);
                } else if (plot instanceof CategoryPlot) {
                    ((CategoryPlot)plot).setRangeAxis(index, logAxis, true);
                } else {
                    throw new ChartException("Unsupported plot type for log scale: " + plot);
                }
            }
            return this;
        }

        @Override
        public ChartAxis asLinearScale() {
            if (axis instanceof CategoryAxis) {
                throw new ChartException("Cannot convert discrete axis to continuous axis");
            } else if (!(axis instanceof NumberAxis)) {
                final String label = axis.getLabel();
                final NumberAxis linearAxis = new NumberAxis(label);
                linearAxis.setLabelFont(axis.getLabelFont());
                linearAxis.setTickLabelFont(axis.getTickLabelFont());
                linearAxis.setLabelPaint(axis.getLabelPaint());
                linearAxis.setTickLabelPaint(axis.getTickLabelPaint());
                this.axis = linearAxis;
                if (range != null) withRange(range);
                final Plot plot = getPlot();
                if (domain && plot instanceof XYPlot) {
                    ((XYPlot)plot).setDomainAxis(index, linearAxis);
                } else if (!domain && plot instanceof XYPlot) {
                    ((XYPlot)plot).setRangeAxis(index, linearAxis);
                } else if (plot instanceof CategoryPlot) {
                    ((CategoryPlot)plot).setRangeAxis(index, linearAxis);
                } else {
                    throw new ChartException("Unsupported plot type for log scale: " + plot);
                }
            }
            return this;
        }

        @Override
        public ChartAxis asDateScale() {
            if (axis instanceof CategoryAxis) {
                throw new ChartException("Cannot convert discrete axis to continuous axis");
            } else if (!(axis instanceof NumberAxis)) {
                final String label = axis.getLabel();
                final DateAxis dateAxis = new DateAxis(label);
                dateAxis.setLabelFont(axis.getLabelFont());
                dateAxis.setTickLabelFont(axis.getTickLabelFont());
                dateAxis.setLabelPaint(axis.getLabelPaint());
                dateAxis.setTickLabelPaint(axis.getTickLabelPaint());
                this.axis = dateAxis;
                if (range != null) withRange(range);
                final Plot plot = getPlot();
                if (domain && plot instanceof XYPlot) {
                    ((XYPlot)plot).setDomainAxis(index, dateAxis);
                } else if (!domain && plot instanceof XYPlot) {
                    ((XYPlot)plot).setRangeAxis(index, dateAxis);
                } else if (plot instanceof CategoryPlot) {
                    ((CategoryPlot)plot).setRangeAxis(index, dateAxis);
                } else {
                    throw new ChartException("Unsupported plot type for log scale: " + plot);
                }
            }
            return this;
        }

        @Override
        public ChartAxis withRange(Bounds<?> range) {
            this.range = range;
            if (axis != null) {
                if (axis instanceof NumberAxis) {
                    final NumberAxis numberAxis = (NumberAxis)axis;
                    final double lower = ((Number)range.lower()).doubleValue();
                    final double upper = ((Number)range.upper()).doubleValue();
                    final Range autoRange = new Range(lower, upper);
                    numberAxis.setDefaultAutoRange(autoRange);
                    numberAxis.setRange(lower, upper);
                } else if (axis instanceof DateAxis) {
                    final DateAxis dateAxis = (DateAxis)axis;
                    final long lower = toEpochMillis(range.lower());
                    final long upper = toEpochMillis(range.upper());
                    final Range autoRange = new Range(lower, upper);
                    dateAxis.setDefaultAutoRange(autoRange);
                } else {
                    throw new IllegalStateException("Can only set range of numeric or date axis");
                }
            }
            return this;
        }


        /**
         * Converts some argument into epoch millis if it can
         * @param value     the value to convert
         * @return          the epoch millis
         */
        private long toEpochMillis(Object value) {
            if (value instanceof Number) {
                return ((Number)value).longValue();
            } else if (value instanceof Date) {
                return ((Date)value).getTime();
            } else if (value instanceof LocalDate) {
                final LocalDate date = (LocalDate)value;
                final LocalDateTime localDateTime = LocalDateTime.of(date, LocalTime.MIDNIGHT);
                return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            } else {
                throw new IllegalArgumentException("Cannot resolve date value from " + value);
            }
        }
    }

    /**
     * A JFreeChart ChartFormat adapter
     */
    private class AxisFormatAdapter implements ChartFormat {

        private Axis axis;

        /**
         * Constructor
         * @param axis    the axis reference
         */
        private AxisFormatAdapter(Axis axis) {
            this.axis = axis;
        }

        @Override
        public void withPattern(String pattern) {
            if (axis instanceof NumberAxis) {
                ((NumberAxis)axis).setNumberFormatOverride(new DecimalFormat(pattern));
            } else if (axis instanceof LogAxis) {
                ((LogAxis)axis).setNumberFormatOverride(new DecimalFormat(pattern));
            } else if (axis instanceof DateAxis) {
                ((DateAxis)axis).setDateFormatOverride(new SimpleDateFormat(pattern));
            }
        }
    }


    /**
     * A JFreeChart ChartLabel adapter for an axis
     */
    private class AxisLabelAdapter implements ChartLabel {

        private Axis axis;

        /**
         * Constructor
         * @param axis  the axis for this adapter
         */
        private AxisLabelAdapter(Axis axis) {
            this.axis = axis;
        }

        @Override
        public ChartLabel withText(String text) {
            this.axis.setLabel(text);
            return this;
        }

        @Override
        public ChartLabel withColor(Color color) {
            this.axis.setLabelPaint(color);
            return this;
        }

        @Override
        public ChartLabel withFont(Font font) {
            this.axis.setLabelFont(font);
            return this;
        }
    }


    /**
     * A JFreeChart text style adapter for an axis
     */
    private class AxisStyleAdapter implements ChartTextStyle<ChartTextStyle> {

        private Axis axis;

        /**
         * Constructor
         * @param axis  the axis for this adapter
         */
        private AxisStyleAdapter(Axis axis) {
            this.axis = axis;
        }

        @Override
        public ChartTextStyle withColor(Color color) {
            this.axis.setAxisLinePaint(color);
            this.axis.setTickLabelPaint(color);
            return this;
        }

        @Override
        public ChartTextStyle withFont(Font font) {
            this.axis.setTickLabelFont(font);
            return this;
        }
    }


    /**
     * A JFreeChart adapter to manage the chart legend
     */
    private class LegendAdapter implements ChartLegend {

        private JFreeChart chart;

        /**
         * Constructor
         * @param chart     the chart reference
         */
        private LegendAdapter(JFreeChart chart) {
            this.chart = chart;
        }

        @Override
        public ChartLegend on() {
            LegendTitle legend = chart.getLegend();
            if (legend == null) {
                final Plot plot = chart.getPlot();
                legend = new LegendTitle(plot);
                this.chart.addLegend(legend);
                this.chart.getLegend().setVisible(true);
                this.chart.getLegend().setPosition(RectangleEdge.RIGHT);
                this.chart.getLegend().setItemLabelPadding(new RectangleInsets(1, 1, 1, 5));
                this.chart.getLegend().setLegendItemGraphicPadding(new RectangleInsets(1, 5, 1, 1));
            }
            return this;
        }

        @Override
        public ChartLegend off() {
            LegendTitle legend = chart.getLegend();
            if (legend != null) {
                legend.setVisible(false);
            }
            return this;
        }

        @Override
        public ChartLegend right() {
            this.on();
            this.chart.getLegend().setPosition(RectangleEdge.RIGHT);
            return this;
        }

        @Override
        public ChartLegend left() {
            this.on();
            this.chart.getLegend().setPosition(RectangleEdge.LEFT);
            return this;
        }

        @Override
        public ChartLegend top() {
            this.on();
            this.chart.getLegend().setPosition(RectangleEdge.TOP);
            return this;
        }

        @Override
        public ChartLegend bottom() {
            this.on();
            this.chart.getLegend().setPosition(RectangleEdge.BOTTOM);
            return this;
        }
    }

}
