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
package com.zavtech.morpheus.viz.examples;

import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.testng.annotations.Test;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.range.Range;
import com.zavtech.morpheus.util.Collect;
import com.zavtech.morpheus.util.IO;
import com.zavtech.morpheus.viz.chart.Chart;
import com.zavtech.morpheus.viz.chart.ChartShape;
import com.zavtech.morpheus.viz.chart.pie.PiePlot;
import com.zavtech.morpheus.viz.chart.xy.XyPlot;
import com.zavtech.morpheus.viz.html.HtmlWriter;

/**
 * Class summary goes here...
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public class MultiCharts {


    private DataFrame<Integer,String> lineData(int rowCount, int seriesCount) {
        LocalDate startDate = LocalDate.of(2013, 1, 1);
        Range<Integer> rowKeys = Range.of(0, rowCount);
        Range<LocalDate> dates = rowKeys.map(startDate::plusDays);
        return DataFrame.of(rowKeys, String.class, columns -> {
            columns.add("DataDate", dates);
            Range.of(0, seriesCount).forEach(series -> {
                switch (series) {
                    case 0: columns.add("A", Array.randn(rowCount).cumSum());   break;
                    case 1: columns.add("B", Array.randn(rowCount).cumSum());   break;
                    case 2: columns.add("C", Array.randn(rowCount).cumSum());   break;
                    case 3: columns.add("D", Array.randn(rowCount).cumSum());   break;
                    case 4: columns.add("E", Array.randn(rowCount).cumSum());   break;
                    case 5: columns.add("F", Array.randn(rowCount).cumSum());   break;
                    case 6: columns.add("G", Array.randn(rowCount).cumSum());   break;
                    case 8: columns.add("H", Array.randn(rowCount).cumSum());   break;
                    case 9: columns.add("I", Array.randn(rowCount).cumSum());   break;
                }
            });
        });
    }

    private DataFrame<LocalDateTime,String> barData(int rowCount) {
        LocalDateTime start = LocalDateTime.of(2014, 1, 1, 8, 30);
        Range<LocalDateTime> rowKeys = Range.of(0, rowCount).map(i -> start.plusMinutes(i * 10));
        return DataFrame.of(rowKeys, String.class, columns -> {
            Stream.of("A", "B", "C", "D", "E", "F").forEach(label -> {
                columns.add(label, Array.randn(40, 1, 5).cumSum());
            });
        });
    }


    /**
     * Returns a 2D sample dataset given a slope and intercept while adding white noise based on sigma.
     * @param alpha     the intercept term for data
     * @param beta      the slope term for for data
     * @param sigma     the standard deviation for noise
     * @param stepSize  the step size for domain variable
     * @param n         the size of the sample to generate
     * @return          the frame of XY values
     */
    private DataFrame<Integer,String> scatter(double alpha, double beta, double sigma, double stepSize, int n) {
        final Array<Double> xValues = Array.of(Double.class, n).applyDoubles(v -> 0d + v.index() * stepSize);
        final Array<Double> yValues = Array.of(Double.class, n).applyDoubles(v -> {
            final double yfit = alpha + beta * xValues.getDouble(v.index());
            return new NormalDistribution(yfit, sigma).sample();
        });
        final Array<Integer> rowKeys = Range.of(0, n).toArray();
        return DataFrame.of(rowKeys, String.class, columns -> {
            columns.add("X", xValues);
            columns.add("Y", yValues);
        }).cols().demean(true);
    }



    private Chart<XyPlot<LocalDate>> linePlotSingleSeries() {
        DataFrame<Integer,String> frame = lineData(1000, 1);
        return Chart.create().withLinePlot(frame, "DataDate", chart -> {
            chart.title().withText("Single DataFrame, Single Series");
            chart.legend().off();
        });
    }


    private Chart<XyPlot<LocalDate>> linePlotMultipleSeries() {
        DataFrame<Integer,String> frame = lineData(1000, 4);
        return Chart.create().withLinePlot(frame, "DataDate", chart -> {
            chart.title().withText("Single DataFrame, Multiple Series");
            chart.legend().on().bottom();
        });
    }


    private Chart<XyPlot<LocalDate>> linePlotMultipleSeriesWithTotalLine() {
        DataFrame<Integer,String> frame = lineData(1000, 4);
        frame.cols().add("Total", Double.class, v -> v.row().stats().sum());
        return Chart.create().withLinePlot(frame, "DataDate", chart -> {
            chart.title().withText("Example Time Series Chart");
            chart.subtitle().withText("Cumulative Sum");
            chart.plot().axes().domain().label().withText("Data Date");
            chart.plot().axes().range(0).label().withText("Random Value");
            chart.plot().style("Total").withLineWidth(2f).withColor(Color.BLACK);
            chart.legend().on();
        });
    }



    private Chart<XyPlot<Integer>> linePlotDualAxisChart() {
        int rowCount = 1000;
        Range<Integer> rowKeys = Range.of(0, rowCount);
        DataFrame<Integer,String> frame = DataFrame.of(rowKeys, String.class, columns -> {
            Stream.of("A", "B").forEach(c -> columns.add(c, Array.randn(rowCount).cumSum()));
            Stream.of("C", "D").forEach(c -> {
                columns.add(c, Array.randn(rowCount).mapToDoubles(v -> v.getDouble() * 100).cumSum());
            });
        });
        return Chart.create().withLinePlot(frame.cols().select("A", "B"), chart -> {
            chart.plot().<String>data().add(frame.cols().select("C", "D"));
            chart.plot().data().setRangeAxis(1, 1);
            chart.plot().axes().domain().label().withText("Data Date");
            chart.plot().axes().range(0).label().withText("Random Value-1");
            chart.plot().axes().range(1).label().withText("Random Value-2");
            chart.title().withText("Time Series Chart - Multiple Axis");
            chart.subtitle().withText("Cumulative Sum");
            chart.legend().on();
        });
    }



    private Chart<XyPlot<Integer>> linePlotWithMultiStyles() {
        int rowCount = 40;
        Range<Integer> rowKeys = Range.of(0, rowCount);
        DataFrame<Integer,String> frame = DataFrame.of(rowKeys, String.class, columns -> {
            Stream.of("A", "B", "C", "D", "E", "F").forEach(label -> {
                columns.add(label, Array.randn(rowCount).cumSum());
            });
        });
        return Chart.create().withLinePlot(frame.cols().select("A", "B"), chart -> {
            chart.plot().<String>data().add(frame.cols().select("C", "D"));
            chart.plot().<String>data().add(frame.cols().select("E", "F"));
            chart.plot().render(0).withLines(true, false);
            chart.plot().render(1).withSpline(false, false);
            chart.plot().render(2).withLines(false, true);
            chart.title().withText("Time Series Chart - Multiple Renderers");
            chart.subtitle().withText("Cumulative Sum");
            chart.plot().axes().domain().label().withText("Data Date");
            chart.plot().axes().range(0).label().withText("Random Value");
            chart.legend().on();
        });
    }


    private Chart<XyPlot<Year>> discreteBarPlot(boolean vertical) {
        Range<Year> years = Range.of(2000, 2006).map(Year::of);
        DataFrame<Year,String> data = DataFrame.of(years, String.class, columns -> {
            Stream.of("A", "B", "C", "D").forEach(label -> {
                columns.add(label, Array.of(Double.class, 6).applyDoubles(v -> Math.random()).cumSum());
            });
        });
        final String prefix = vertical ? "Vertical" : "Horizontal";
        return Chart.create().withBarPlot(data, false, chart -> {
            chart.plot().axes().domain().label().withText("Year");
            chart.plot().axes().range(0).label().withText("Random Value");
            chart.title().withText(prefix + "Bar Chart - Categorical Domain Axis");
            chart.legend().on();
            if (vertical) {
                chart.plot().orient().vertical();
            } else {
                chart.plot().orient().horizontal();
            }
        });
    }


    private Chart<XyPlot<Year>> stackedBarPlotDiscrete(boolean vertical) {
        Range<Year> years = Range.of(2000, 2010).map(Year::of);
        DataFrame<Year,String> frame = DataFrame.of(years, String.class, columns -> {
            Stream.of("A", "B", "C", "D", "E", "F", "G").forEach(label -> {
                columns.add(label, Array.randn(10).applyDoubles(v -> Math.abs(v.getDouble())).cumSum());
            });
        });
        final String prefix = vertical ? "Vertical" : "Horizontal";
        return Chart.create().withBarPlot(frame, true, chart -> {
            chart.plot().axes().domain().label().withText("Year");
            chart.plot().axes().range(0).label().withText("Random Value");
            chart.title().withText(prefix + " Stacked Bar Chart - Categorical Domain Axis");
            chart.legend().on();
            if (vertical) {
                chart.plot().orient().vertical();
            } else {
                chart.plot().orient().horizontal();
            }
        });
    }


    private Chart<XyPlot<LocalDateTime>> stackedBarPlotContinuous(boolean vertical) {
        final String prefix = vertical ? "Vertical" : "Horizontal";
        return Chart.create().withBarPlot(barData(40), true, chart -> {
            chart.plot().data().at(0).withLowerDomainInterval(t -> t.minusMinutes(10));
            chart.plot().axes().domain().label().withText("Year");
            chart.plot().axes().range(0).label().withText("Random Value");
            chart.title().withText(prefix + "Stacked Bar Chart - Continuous Domain Axis");
            chart.legend().on();
            if (vertical) {
                chart.plot().orient().vertical();
            } else {
                chart.plot().orient().horizontal();
            }
        });
    }


    private Chart<XyPlot<Integer>> scatter1() {
        DataFrame<Integer,String> frame = scatter(4d, 0.5d, 20d, 0.5, 1000);
        return Chart.create().withScatterPlot(frame, false, "X", chart -> {
            chart.plot().axes().domain().label().withText("X-Value");
            chart.plot().axes().range(0).label().withText("Y-Value");
            chart.title().withText("Scatter Chart");
            chart.subtitle().withText("Single DataFrame, Single Series");
        });
    }


    private Chart<XyPlot<Integer>> scatter2() {
        DataFrame<Integer,String> frame = DataFrame.concatColumns(
            scatter(4d, 1d, 80d, 0.5, 500).cols().replaceKey("Y", "A"),
            scatter(4d, 6d, 100d, 0.5, 500).cols().replaceKey("Y", "B"),
            scatter(4d, 12d, 180d, 0.5, 500).cols().replaceKey("Y", "C")
        );
        return Chart.create().withScatterPlot(frame, false, "X", chart -> {
            chart.plot().axes().domain().label().withText("X-Value");
            chart.plot().axes().range(0).label().withText("Y-Value");
            chart.plot().style("A").withColor(new Color(255, 225, 25));
            chart.plot().style("B").withColor(new Color(0, 130, 200));
            chart.plot().style("C").withColor(new Color(245, 0, 48)).withPointShape(ChartShape.DIAMOND);
            chart.title().withText("Scatter Chart");
            chart.subtitle().withText("Single DataFrame, Multiple Series, Custom Style");
            chart.legend().on();
        });
    }


    private Chart<XyPlot<Integer>> scatter3() {
        DataFrame<Integer,String> frame1 = DataFrame.concatColumns(
            scatter(4d, 1d, 80d, 0.5, 500).cols().replaceKey("Y", "A"),
            scatter(4d, 3d, 100d, 0.5, 500).cols().replaceKey("Y", "B")
        );
        DataFrame<Integer,String> frame2 = DataFrame.concatColumns(
            scatter(4d, 7d, 80d, 0.55, 600).cols().replaceKey("Y", "C"),
            scatter(4d, -10d, 100d, 0.55, 600).cols().replaceKey("Y", "D")
        );
        return Chart.create().withScatterPlot(frame1, false, "X", chart -> {
            chart.plot().<String>data().add(frame2, "X");
            chart.plot().render(1).withDots();
            chart.plot().axes().domain().label().withText("X-Value");
            chart.plot().axes().range(0).label().withText("Y-Value");
            chart.title().withText("Scatter Chart");
            chart.subtitle().withText("Multiple DataFrames, Multiple Series");
            chart.legend().on();
        });
    }


    private Chart<XyPlot<Integer>> regression1() {
        DataFrame<Integer,String> frame = scatter(4d, 1d, 80d, 0.5d, 1000);
        return Chart.create().withScatterPlot(frame, false, "X", chart -> {
            chart.plot().axes().domain().label().withText("X-Value");
            chart.plot().axes().range(0).label().withText("Y-Value");
            chart.plot().trend("Y");
            chart.title().withText("Regression Chart");
            chart.subtitle().withText("Single DataFrame, Single Series");
        });
    }


    private Chart<XyPlot<Integer>> regression2() {
        DataFrame<Integer,String> frame1 = DataFrame.concatColumns(
            scatter(4d, 1d, 80d, 0.5, 500).cols().replaceKey("Y", "A"),
            scatter(4d, 4d, 100d, 0.5, 500).cols().replaceKey("Y", "B")
        );
        DataFrame<Integer,String> frame2 = DataFrame.concatColumns(
            scatter(4d, -3d, 80d, 0.55, 600).cols().replaceKey("Y", "C"),
            scatter(4d, -10d, 100d, 0.45, 600).cols().replaceKey("Y", "D")
        );
        return Chart.create().withScatterPlot(frame1, false, "X", chart -> {
            chart.plot().<String>data().add(frame2, "X");
            chart.plot().render(1).withDots();
            chart.plot().axes().domain().label().withText("X-Value");
            chart.plot().axes().range(0).label().withText("Y-Value");
            chart.plot().trend("A");
            chart.plot().trend("B");
            chart.plot().trend("C");
            chart.title().withText("Regression Chart");
            chart.subtitle().withText("Multiple DataFrame, Multiple Series");
            chart.legend().on();
        });
    }


    private Chart<XyPlot<Integer>> areaPlotStacked() {
        int rowCount = 100;
        Range<Integer> rowKeys = Range.of(0, rowCount);
        DataFrame<Integer,String> frame = DataFrame.of(rowKeys, String.class, columns -> {
            Stream.of("A", "B", "C", "D", "E").forEach(label -> {
                columns.add(label, Array.randn(rowCount, 10d, 100d).cumSum());
            });
        });
        return Chart.create().withAreaPlot(frame, true, chart -> {
            chart.plot().axes().domain().label().withText("X-Value");
            chart.plot().axes().range(0).label().withText("Random Value");
            chart.title().withText("Stacked Area Chart");
            chart.subtitle().withText("Cumulative Sum of Random Normal Data");
            chart.legend().on();
        });
    }


    private Chart<XyPlot<Integer>> areaPlotUnstacked() {
        int rowCount = 100;
        Range<Integer> rowKeys = Range.of(0, rowCount);
        DataFrame<Integer,String> frame = DataFrame.of(rowKeys, String.class, columns -> {
            Stream.of("A", "B", "C", "D", "E").forEach(label -> {
                columns.add(label, Array.randn(rowCount, 10d, 100d).cumSum());
            });
        });
        return Chart.create().withAreaPlot(frame, false, chart -> {
            chart.plot().axes().domain().label().withText("X-Value");
            chart.plot().axes().range(0).label().withText("Random Value");
            chart.title().withText("Overlapping Area Chart");
            chart.subtitle().withText("Cumulative Sum of Random Normal Data");
            chart.legend().on();
        });
    }


    private Chart<PiePlot<String,String>> piePlot1() {
        DataFrame<String,String> frame = DataFrame.ofDoubles(
            Array.of("AUS", "GBR", "USA", "DEU", "ITA", "ESP", "ZAF"),
            Array.of("Random"),
            value -> Math.random() * 10d
        );
        return Chart.create().withPiePlot(frame, false, chart -> {
            chart.title().withText("Pie Chart of Random Data");
            chart.subtitle().withText("Labels with Section Percent");
            chart.legend().on().right();
        });
    }



    private Chart<PiePlot<String,String>> piePlot2() {
        DataFrame<String, String> frame = DataFrame.ofDoubles(
            Array.of("AUS", "GBR", "USA", "DEU", "ITA", "ESP", "ZAF"),
            Array.of("Random"),
            value -> Math.random() * 10d
        );
        return Chart.create().withPiePlot(frame, false, chart -> {
            chart.title().withText("Donut Pie Chart of Random Data");
            chart.subtitle().withText("Labels with Section Value");
            chart.plot().withPieHole(0.4);
            chart.plot().labels().on().withValue();
            chart.legend().on().right();
        });
    }


    private Chart<PiePlot<String,String>> piePlot3() {
        DataFrame<String, String> frame = DataFrame.ofDoubles(
            Array.of("AUS", "GBR", "USA", "DEU", "ITA", "ESP", "ZAF"),
            Array.of("Random"),
            value -> Math.random() * 10d
        );
        return Chart.create().withPiePlot(frame, true, chart -> {
            chart.title().withText("3D Pie Chart of Random Data");
            chart.subtitle().withText("Labels with Section Name");
            chart.plot().labels().on().withName();
            chart.legend().on().right();
        });
    }



    private Chart<PiePlot<String,String>> piePlot4() {
        DataFrame<String, String> frame = DataFrame.ofDoubles(
            Array.of("AUS", "GBR", "USA", "DEU", "ITA", "ESP", "ZAF"),
            Array.of("Random"),
            value -> Math.random() * 10d
        );
        return Chart.create().withPiePlot(frame, false, chart -> {
            chart.title().withText("Pie Chart of Random Data");
            chart.subtitle().withText("Exploded Pie Section");
            chart.plot().labels().withBackgroundColor(Color.WHITE).withFont(new Font("Arial", Font.BOLD, 11));
            chart.plot().section("AUS").withOffset(0.2);
            chart.plot().withStartAngle(90);
            chart.legend().on().right();
        });
    }


    /**
     * Returns a list of test charts
     * @return  the list of test charts
     */
    public List<Chart<?>> createCharts() {
        return Collect.asList(
                linePlotSingleSeries(),
                linePlotMultipleSeries(),
                linePlotMultipleSeriesWithTotalLine(),
                linePlotDualAxisChart(),
                linePlotWithMultiStyles(),
                discreteBarPlot(true),
                discreteBarPlot(false),
                stackedBarPlotDiscrete(true),
                stackedBarPlotDiscrete(false),
                stackedBarPlotContinuous(true),
                stackedBarPlotContinuous(false),
                scatter1(),
                scatter2(),
                scatter3(),
                regression1(),
                regression2(),
                areaPlotStacked(),
                areaPlotUnstacked(),
                piePlot1(),
                piePlot2(),
                piePlot3(),
                piePlot4()
            );
    }


    @Test()
    public void htmlCharts() throws Exception {
        Chart.create().htmlMode();
        List<Chart<?>> charts = createCharts();
        IO.println(Chart.create().javascript(charts));
        Chart.create().show(2, charts);
    }



    @Test()
    public void swingCharts1() throws Exception {
        Chart.create().swingMode();
        Chart.create().show(3, createCharts());
        Thread.currentThread().join();
    }


    @Test()
    public void smallImages() throws Exception {
        Chart.create().swingMode();
        List<Chart<?>> charts = createCharts();
        AtomicInteger counter = new AtomicInteger();
        charts.forEach(chart -> {
            chart.legend().bottom();
            chart.options().withPreferredSize(400, 280);
            chart.writerPng(new File(String.format("../morpheus-docs/docs/images/gallery/chart-%s.png", counter.incrementAndGet())), 400, 280, true);
        });
    }



    @Test()
    public void swingCharts2() throws Exception {
        Chart.create().swingMode();
        List<Chart<?>> charts = createCharts();
        IO.writeText(Chart.create().javascript(charts), new File("../morpheus-docs/docs/javascript/gallery2.js"));

        try {
            final HtmlWriter htmlWriter = new HtmlWriter();
            final AtomicInteger chartIndex = new AtomicInteger(-1);
            htmlWriter.newElement("html", html -> {
                html.newElement("head", head -> {
                    head.newElement("script", script -> {
                        script.newAttribute("type", "text/javascript");
                        script.text(Chart.create().asSwing().javascript(charts));
                    });
                });

                final int cols = 2;
                final String width = String.valueOf((int)(100d / cols)) + "%";
                final String height = String.valueOf((int)(100d / cols * 0.9d)) + "%";
                html.newElement("body", body -> {
                    chartIndex.set(-1);
                    charts.forEach(chart -> {
                        body.newElement("div", div -> {
                            div.newAttribute("id", String.format("chart_%s", chartIndex.incrementAndGet()));
                            div.newAttribute("style", String.format("width:%s;height:%s;float:left;", width, height));
                        });
                    });
                });
            });

            final File dir = new File(System.getProperty("user.home"), ".morpheus/charts");
            final File file = new File(dir, UUID.randomUUID().toString() + ".html");
            if (file.getParentFile().mkdirs()) System.out.println("Created directory: " + dir.getAbsolutePath());

            System.out.println(htmlWriter.toString());

            htmlWriter.flush(file);
            Desktop.getDesktop().browse(file.toURI());
        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate Google chart", ex);
        }
    }

}
