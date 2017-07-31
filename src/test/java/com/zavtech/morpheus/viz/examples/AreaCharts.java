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
import java.util.stream.Stream;

import org.testng.annotations.Test;

import com.zavtech.morpheus.range.Range;
import com.zavtech.morpheus.viz.chart.Chart;
import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;

public class AreaCharts {


    @Test()
    public void areaChart1() throws Exception {

        int rowCount = 100;
        Range<Integer> rowKeys = Range.of(0, rowCount);
        DataFrame<Integer,String> frame = DataFrame.of(rowKeys, String.class, columns -> {
            Stream.of("A", "B", "C", "D", "E").forEach(label -> {
                columns.add(label, Array.randn(rowCount, 10d, 100d).cumSum());
            });
        });

        Chart.create().htmlMode();

        Chart.create().withAreaPlot(frame, true, chart -> {
            chart.plot().axes().domain().label().withText("X-Value");
            chart.plot().axes().range(0).label().withText("Random Value");
            chart.title().withText("Stacked Area Chart");
            chart.subtitle().withText("Cumulative Sum of Random Normal Data");
            chart.legend().on();
            chart.writerPng(new File("../morpheus-docs/docs/images/charts/chart-area-1.png"), 845, 450, true);
            chart.show();
        });

        Chart.create().withAreaPlot(frame, false, chart -> {
            chart.plot().axes().domain().label().withText("X-Value");
            chart.plot().axes().range(0).label().withText("Random Value");
            chart.title().withText("Overlapping Area Chart");
            chart.subtitle().withText("Cumulative Sum of Random Normal Data");
            chart.legend().on();
            chart.writerPng(new File("../morpheus-docs/docs/images/charts/chart-area-2.png"), 845, 450, true);
            chart.show();
        });

        Thread.currentThread().join();

    }



}
