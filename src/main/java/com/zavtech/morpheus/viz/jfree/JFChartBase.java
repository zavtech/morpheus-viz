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
import java.util.Optional;
import javax.swing.JFrame;

import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

import com.zavtech.morpheus.viz.chart.Chart;
import com.zavtech.morpheus.viz.chart.ChartException;
import com.zavtech.morpheus.viz.chart.ChartLabel;
import com.zavtech.morpheus.viz.chart.ChartLegend;
import com.zavtech.morpheus.viz.chart.ChartOptions;
import com.zavtech.morpheus.viz.chart.ChartTheme;

/**
 * A convenience base class for building various types of chart types
 *
 * @param <P>   the plot type
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
abstract class JFChartBase<P> implements Chart<P>, ChartMouseListener {

    private P plot;
    private JFreeChart freeChart;
    private ChartPanel chartPanel;
    private ChartOptions options;


    /**
     * Constructor
     * @param plot      the plot for this chart
     * @param legend    true to enable legend
     */
    JFChartBase(P plot, boolean legend) {
        this.plot = plot;
        this.freeChart = new JFreeChart(null, new Font("Arial", Font.PLAIN, 4), underlying(plot), legend);
        this.freeChart.setBackgroundPaint(Color.WHITE);
        this.chartPanel = new ChartPanel(freeChart);
        this.chartPanel.setMouseZoomable(true);
        this.chartPanel.setRefreshBuffer(true);
        this.chartPanel.addChartMouseListener(this);
        this.options = new ChartOptions.Default();
    }


    /**
     * Returns the JFreeChart plot object from the Morpheus Plot adapter
     * @param plot      the Morpheus plot adapter
     * @return          the JFreeChart plot
     */
    private Plot underlying(P plot) {
        if (plot instanceof JFXyPlot) {
            return ((JFXyPlot)plot).underlying();
        } else if (plot instanceof JFCatPlot) {
            return ((JFCatPlot) plot).underlying();
        } else if (plot instanceof JFPiePlot) {
            return ((JFPiePlot) plot).underlying();
        } else {
            throw new IllegalArgumentException("Unsupported plot type: " + plot);
        }
    }


    /**
     * Returns a reference to the chart panel
     * @return  the JFreeChart chart panel
     */
    ChartPanel chartPanel() {
        return chartPanel;
    }


    /**
     * Returns the JFreeChart reference for this chart
     * @return  the JFreeChart reference
     */
    JFreeChart freeChart() {
        return freeChart;
    }


    @Override
    public P plot() {
        return plot;
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
    public ChartLegend legend() {
        return new LegendAdapter(freeChart);
    }


    @Override
    public ChartOptions options() {
        return options;
    }


    @Override()
    public ChartTheme theme() {
        return new ThemeAdapter();
    }


    @Override
    public Chart show() {
        return show(1024, 600);
    }


    @Override
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
    public Chart writerPng(File file, int width, int height, boolean transparent) {
        BufferedOutputStream os = null;
        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                if (!parent.mkdirs()) {
                    System.err.printf("Unable to create directory for %s", file.getAbsolutePath());
                }
            }
            os = new BufferedOutputStream(new FileOutputStream(file));
            writerPng(os, width, height, transparent);
            return this;
        } catch (Exception ex) {
            throw new ChartException(ex.getMessage(), ex);
        } finally {
            try {
                if (os != null) {
                    os.flush();
                    os.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


    @Override
    public Chart writerPng(OutputStream os, int width, int height, boolean transparent) {
        try {
            if (transparent) {
                freeChart().setBackgroundPaint(new Color(255, 255, 255, 0));
                freeChart().setBackgroundImageAlpha(0.0f);
                freeChart().getPlot().setBackgroundPaint( new Color(255, 255, 255, 0) );
                freeChart().getPlot().setBackgroundImageAlpha(0f);
            }
            ChartUtilities.writeChartAsPNG(os, freeChart, width, height, transparent, 0);
            return this;
        } catch (Exception ex) {
            throw new ChartException(ex.getMessage(), ex);
        } finally {
            try {
                if (os != null) {
                    os.flush();
                    os.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
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


    /**
     * The ChartTheme adapter
     */
    private class ThemeAdapter implements ChartTheme {

        @Override
        public void light() {
            final Color lightColor = Color.WHITE;
            final Color darkColor = Color.BLACK;
            freeChart().setBackgroundPaint(lightColor);
            Optional.ofNullable(freeChart().getTitle()).ifPresent(x -> {
                x.setPaint(darkColor);
                x.setBackgroundPaint(lightColor);
            });
            Optional.ofNullable(freeChart().getSubtitle(0)).ifPresent(x -> {
                if (x instanceof TextTitle) {
                    ((TextTitle)x).setBackgroundPaint(lightColor);
                    ((TextTitle)x).setPaint(darkColor);
                }
            });
            Optional.ofNullable(freeChart().getLegend()).ifPresent(x -> {
                x.setBackgroundPaint(lightColor);
                x.setItemPaint(darkColor);
            });
            final Plot plot = freeChart().getPlot();
            if (plot instanceof XYPlot) {
                final XYPlot xyPlot = (XYPlot)plot;
                xyPlot.setBackgroundPaint(lightColor);
                xyPlot.setDomainGridlinesVisible(true);
                xyPlot.setRangeGridlinesVisible(true);
                xyPlot.setDomainGridlinePaint(Color.DARK_GRAY);
                xyPlot.setRangeGridlinePaint(Color.DARK_GRAY);
                for (int i=0; i<xyPlot.getDatasetCount(); ++i) {
                    xyPlot.getDomainAxis().setLabelPaint(darkColor);
                    xyPlot.getDomainAxis().setTickLabelPaint(darkColor);
                    xyPlot.getDomainAxis().setLabelPaint(darkColor);
                    xyPlot.getDomainAxis().setAxisLinePaint(darkColor);
                    xyPlot.getDomainAxis().setTickMarkPaint(darkColor);
                }
                for (int i=0; i<xyPlot.getRangeAxisCount(); ++i) {
                    xyPlot.getRangeAxis(i).setLabelPaint(darkColor);
                    xyPlot.getRangeAxis(i).setTickLabelPaint(darkColor);
                    xyPlot.getRangeAxis(i).setLabelPaint(darkColor);
                    xyPlot.getRangeAxis(i).setAxisLinePaint(darkColor);
                    xyPlot.getRangeAxis(i).setTickMarkPaint(darkColor);
                }
            }
        }

        @Override
        public void dark() {
            final Color darkColor = Color.BLACK;
            final Color lightColor = Color.LIGHT_GRAY;
            freeChart().setBackgroundPaint(darkColor);
            Optional.ofNullable(freeChart().getTitle()).ifPresent(x -> {
                x.setPaint(lightColor);
                x.setBackgroundPaint(darkColor);
            });
            Optional.ofNullable(freeChart().getSubtitle(0)).ifPresent(x -> {
                if (x instanceof TextTitle) {
                    ((TextTitle)x).setBackgroundPaint(darkColor);
                    ((TextTitle)x).setPaint(lightColor);
                }
            });
            Optional.ofNullable(freeChart().getLegend()).ifPresent(x -> {
                x.setBackgroundPaint(darkColor);
                x.setItemPaint(lightColor);
            });
            final Plot plot = freeChart().getPlot();
            if (plot instanceof XYPlot) {
                final XYPlot xyPlot = (XYPlot)plot;
                xyPlot.setBackgroundPaint(darkColor);
                xyPlot.setDomainGridlinesVisible(true);
                xyPlot.setRangeGridlinesVisible(true);
                xyPlot.setDomainGridlinePaint(Color.LIGHT_GRAY);
                xyPlot.setRangeGridlinePaint(Color.LIGHT_GRAY);
                for (int i=0; i<xyPlot.getDatasetCount(); ++i) {
                    xyPlot.getDomainAxis().setLabelPaint(lightColor);
                    xyPlot.getDomainAxis().setTickLabelPaint(lightColor);
                    xyPlot.getDomainAxis().setLabelPaint(lightColor);
                    xyPlot.getDomainAxis().setAxisLinePaint(lightColor);
                    xyPlot.getDomainAxis().setTickMarkPaint(lightColor);
                }
                for (int i=0; i<xyPlot.getRangeAxisCount(); ++i) {
                    xyPlot.getRangeAxis(i).setLabelPaint(lightColor);
                    xyPlot.getRangeAxis(i).setTickLabelPaint(lightColor);
                    xyPlot.getRangeAxis(i).setLabelPaint(lightColor);
                    xyPlot.getRangeAxis(i).setAxisLinePaint(lightColor);
                    xyPlot.getRangeAxis(i).setTickMarkPaint(lightColor);
                }
            }

        }
    }


}
