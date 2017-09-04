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

import org.testng.annotations.Test;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.viz.chart.Chart;

/**
 * Pie charts
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public class PieCharts {


    @Test()
    public void pieChart1() throws Exception {

        DataFrame<String,String> frame = DataFrame.ofDoubles(
            Array.of("AUS", "GBR", "USA", "DEU", "ITA", "ESP", "ZAF"),
            Array.of("Random"),
            value -> Math.random() * 10d
        );

        Chart.create().swingMode();

        Chart.create().withPiePlot(frame, false, chart -> {
            chart.title().withText("Pie Chart of Random Data");
            chart.subtitle().withText("Labels with Section Percent");
            chart.legend().on().right();
            chart.writerPng(new File("../morpheus-docs/docs/images/charts/chart-pie-1.png"), 500, 400, true);
            chart.show();
        });

        Chart.create().withPiePlot(frame, false, "Random", chart -> {
            chart.title().withText("Donut Pie Chart of  Random Data");
            chart.subtitle().withText("Labels with Section Value");
            chart.plot().withPieHole(0.4);
            chart.plot().labels().on().withValue();
            chart.legend().on().right();
            chart.writerPng(new File("../morpheus-docs/docs/images/charts/chart-pie-2.png"), 700, 400, true);
            chart.show();
        });


        Chart.create().withPiePlot(frame, true, chart -> {
            chart.title().withText("3D Pie Chart of Random Data");
            chart.subtitle().withText("Labels with Section Name");
            chart.plot().labels().on().withName();
            chart.legend().on().right();
            chart.writerPng(new File("../morpheus-docs/docs/images/charts/chart-pie-3.png"), 700, 400, true);
            chart.show();
        });

        Chart.create().withPiePlot(frame, false, chart -> {
            chart.title().withText("Pie Chart of Random Data");
            chart.subtitle().withText("Custom Label Style with Exploded Pie Section");
            chart.plot().labels().withBackgroundColor(Color.WHITE).withFont(new Font("Arial", Font.BOLD, 11));
            chart.plot().section("AUS").withOffset(0.2);
            chart.plot().withStartAngle(90);
            chart.legend().on().right();
            chart.writerPng(new File("../morpheus-docs/docs/images/charts/chart-pie-4.png"), 700, 400, true);
            chart.show();
        });

        Thread.currentThread().join();
    }

}
