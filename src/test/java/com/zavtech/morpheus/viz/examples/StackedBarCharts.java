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

import java.io.File;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.stream.Stream;

import org.testng.annotations.Test;

import com.zavtech.morpheus.range.Range;
import com.zavtech.morpheus.viz.chart.Chart;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;

/**
 * An example that loads an ATP dataset and computes statistics on the top 10 players
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public class StackedBarCharts {


    @Test()
    public void stackedBars1() throws Exception {

        Range<Year> years = Range.of(2000, 2010).map(Year::of);
        DataFrame<Year,String> frame = DataFrame.of(years, String.class, columns -> {
            Stream.of("A", "B", "C", "D", "E", "F", "G").forEach(label -> {
                columns.add(label, Array.randn(10).applyDoubles(v -> Math.abs(v.getDouble())).cumSum());
            });
        });

        frame.out().print();

        Chart.create().htmlMode();

        Chart.create().withBarPlot(frame, true, chart -> {
            chart.plot().axes().domain().label().withText("Year");
            chart.plot().axes().range(0).label().withText("Random Value");
            chart.title().withText("Stacked Bar Chart - Categorical Domain Axis");
            chart.subtitle().withText("Cumulative Sum of Random Uniform Data");
            chart.legend().on();
            chart.writerPng(new File("../morpheus-docs/docs/images/charts/chart-stacked-bars-1.png"), 845, 450, true);
            chart.show();
        });

        Chart.create().withBarPlot(frame, true, chart -> {
            chart.plot().axes().domain().label().withText("Year");
            chart.plot().axes().range(0).label().withText("Random Value");
            chart.plot().orient().horizontal();
            chart.title().withText("Stacked Bar Chart - Categorical Domain Axis");
            chart.subtitle().withText("Cumulative Sum of Random Uniform Data");
            chart.legend().on();
            chart.writerPng(new File("../morpheus-docs/docs/images/charts/chart-stacked-bars-2.png"), 845, 450, true);
            chart.show();
        });


        Thread.currentThread().join();
    }


    @Test()
    public void stackedBars2() throws Exception {

        int rowCount = 40;
        LocalDateTime start = LocalDateTime.of(2014, 1, 1, 8, 30);
        Range<LocalDateTime> rowKeys = Range.of(0, rowCount).map(i -> start.plusMinutes(i * 10));
        DataFrame<LocalDateTime,String> frame = DataFrame.of(rowKeys, String.class, columns -> {
            Stream.of("A", "B", "C", "D", "E", "F").forEach(label -> {
                columns.add(label, Array.randn(40, 1, 5).cumSum());
            });
        });

        Chart.create().htmlMode();

        Chart.create().withBarPlot(frame, true, chart -> {
            chart.plot().data().at(0).withLowerDomainInterval(t -> t.minusMinutes(10));
            chart.plot().axes().domain().label().withText("Year");
            chart.plot().axes().range(0).label().withText("Random Value");
            chart.title().withText("Stacked Bar Chart - Continuous Domain Axis");
            chart.subtitle().withText("Random Uniform Data");
            chart.legend().on();
            chart.writerPng(new File("../morpheus-docs/docs/images/charts/chart-stacked-bars-3.png"), 845, 450, true);
            chart.show();
        });

        Chart.create().withBarPlot(frame, true, chart -> {
            chart.plot().orient().horizontal();
            chart.plot().data().at(0).withLowerDomainInterval(t -> t.minusMinutes(10));
            chart.plot().axes().domain().label().withText("Year");
            chart.plot().axes().range(0).label().withText("Random Value");
            chart.title().withText("Stacked Bar Chart - Continuous Domain Axis");
            chart.subtitle().withText("Random Uniform Data");
            chart.legend().on();
            chart.writerPng(new File("../morpheus-docs/docs/images/charts/chart-stacked-bars-3.png"), 845, 450, true);
            chart.show();
        });


        Thread.currentThread().join();
    }

}
