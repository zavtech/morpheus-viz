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
import java.util.stream.Stream;

import org.testng.annotations.Test;

import com.zavtech.morpheus.range.Range;
import com.zavtech.morpheus.viz.chart.Chart;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;

/**
 * A line chart example that displays GDP per capita data from the World Bank as a time series.
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public class LineCharts {

    @Test()
    public void lineChart1() throws Exception {
        int rowCount = 1000;
        LocalDate startDate = LocalDate.of(2013, 1, 1);
        Range<LocalDate> dates = Range.of(0, rowCount).map(startDate::plusDays);
        DataFrame<LocalDate,String> frame = DataFrame.of(dates, String.class, columns -> {
            columns.add("A", Array.randn(rowCount).cumSum());
        });

        frame.out().print();

        Chart.create().withLinePlot(frame, chart -> {
            chart.writerPng(new File("../morpheus-docs/docs/images/charts/chart-basic-1.png"), 845, 450, true);
            chart.show();
        });

        Thread.currentThread().join();
    }


    @Test()
    public void lineChart2() throws Exception {
        int rowCount = 1000;
        LocalDate startDate = LocalDate.of(2013, 1, 1);
        Range<Integer> rowKeys = Range.of(0, rowCount);
        Range<LocalDate> dates = rowKeys.map(startDate::plusDays);
        DataFrame<Integer,String> frame = DataFrame.of(rowKeys, String.class, columns -> {
            columns.add("DataDate", dates);
            Stream.of("A", "B", "C", "D").forEach(label -> {
                columns.add(label, Array.randn(rowCount).cumSum());
            });
        });

        frame.out().print();

        //Display chart using "DataDate' column values for x-axis
        Chart.create().withLinePlot(frame, "DataDate", chart -> {
            chart.legend().on().bottom();
            chart.writerPng(new File("../morpheus-docs/docs/images/charts/chart-basic-2.png"), 845, 450, true);
            chart.show();
        });

        Thread.currentThread().join();

    }



    @Test()
    public void lineChart3() throws Exception {
        int rowCount = 1000;
        LocalDate startDate = LocalDate.of(2013, 1, 1);
        Range<Integer> rowKeys = Range.of(0, rowCount);
        Range<LocalDate> dates = rowKeys.map(startDate::plusDays);
        DataFrame<Integer,String> frame = DataFrame.of(rowKeys, String.class, columns -> {
            columns.add("DataDate", dates);
            Stream.of("A", "B", "C", "D").forEach(label -> {
                columns.add(label, Array.randn(rowCount).cumSum());
            });
        });

        //Add a total column that sumns A+B+C+D
        frame.cols().add("Total", Double.class, v -> v.row().stats().sum());

        frame.out().print();

        Chart.create().asHtml().withLinePlot(frame, "DataDate", chart -> {
            chart.title().withText("Example Time Series Chart");
            chart.subtitle().withText("Cumulative Sum of Random Normal Data");
            chart.plot().axes().domain().label().withText("Data Date");
            chart.plot().axes().range(0).label().withText("Random Value");
            chart.plot().style("Total").withLineWidth(2f).withColor(Color.BLACK);
            chart.legend().on();
            //chart.writerPng(new File("../morpheus-docs/docs/images/charts/chart-basic-3.png"), 845, 450, true);
            chart.show();

            System.out.println(Chart.create().asHtml().javascript(chart));

        });

        Thread.currentThread().join();
    }



    @Test()
    public void lineChart4() throws Exception {
        int rowCount = 1000;
        Range<Integer> rowKeys = Range.of(0, rowCount);

        DataFrame<Integer,String> frame = DataFrame.of(rowKeys, String.class, columns -> {
            Stream.of("A", "B").forEach(c -> columns.add(c, Array.randn(rowCount).cumSum()));
            Stream.of("C", "D").forEach(c -> {
                columns.add(c, Array.randn(rowCount).mapToDoubles(v -> v.getDouble() * 100).cumSum());
            });
        });

        Chart.create().withLinePlot(frame.cols().select("A", "B"), chart -> {
            chart.plot().<String>data().add(frame.cols().select("C", "D"));
            chart.plot().data().setRangeAxis(1, 1);
            chart.plot().axes().domain().label().withText("Data Date");
            chart.plot().axes().range(0).label().withText("Random Value-1");
            chart.plot().axes().range(1).label().withText("Random Value-2");
            chart.title().withText("Time Series Chart - Multiple Axis");
            chart.subtitle().withText("Cumulative Sum of Random Normal Data");
            chart.legend().on();
            chart.writerPng(new File("../morpheus-docs/docs/images/charts/chart-basic-4.png"), 845, 450, true);
            chart.show();
        });

        Thread.currentThread().join();
    }




    @Test()
    public void lineChart5() throws Exception {
        int rowCount = 20;
        Range<Integer> rowKeys = Range.of(0, rowCount);
        DataFrame<Integer,String> frame = DataFrame.of(rowKeys, String.class, columns -> {
            Stream.of("A", "B", "C", "D", "E", "F").forEach(label -> {
                columns.add(label, Array.randn(rowCount).cumSum());
            });
        });

        Chart.create().htmlMode();

        Chart.create().withLinePlot(frame.cols().select("A", "B"), chart -> {
            chart.plot().<String>data().add(frame.cols().select("C", "D"));
            chart.plot().<String>data().add(frame.cols().select("E", "F"));
            chart.plot().render(0).withLines(true, false);
            chart.plot().render(1).withSpline(false, false);
            chart.plot().render(2).withLines(false, true);
            chart.title().withText("Time Series Chart - Multiple Renderers");
            chart.subtitle().withText("Cumulative Sum of Random Normal Data");
            chart.plot().axes().domain().label().withText("Data Date");
            chart.plot().axes().range(0).label().withText("Random Value");
            chart.legend().on();
            chart.writerPng(new File("../morpheus-docs/docs/images/charts/chart-basic-5.png"), 845, 450, true);
            chart.show();
        });

        Thread.currentThread().join();
    }



}
