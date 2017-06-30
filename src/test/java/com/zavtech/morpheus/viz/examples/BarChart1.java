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

import java.awt.Font;

import com.zavtech.morpheus.viz.chart.Chart;
import com.zavtech.morpheus.viz.chart.ChartEngine;
import com.zavtech.morpheus.viz.google.GChartEngine;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;

/**
 * A bar chart example that displays GDP per capita data from the World Bank
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public class BarChart1 {

    /**
     * Main line
     * @param args
     */
    public static void main(String[] args) {
        final DataFrame<String,String> frame = createDataset();
        ChartEngine.setDefaultEngine(new GChartEngine());
        Chart.of(frame, chart -> {
            chart.title().withText("World Bank GBP Per Capita measured in USD");
            chart.title().withFont(new Font("Arial", Font.PLAIN, 18));
            chart.axes().domain().label().withText("Region");
            chart.axes().domain().label().withFont(new Font("Arial", Font.PLAIN, 16));
            chart.axes().range(0).label().withText("GBP per capita USD");
            chart.axes().domain().label().withFont(new Font("Arial", Font.ITALIC, 16));
            chart.orientation().vertical();
            chart.plot(0).withBars(0d);
            chart.legend().on();
            chart.show();
        });
    }

    /**
     * Loads the ATP dataset for 2013, finds top 10 players, and computes various stats for those players
     * @return      the dataset to plot
     */
    private static DataFrame<String,String> createDataset() {
        final Array<String> years = Array.of("1980", "1985", "1990", "1995", "2000", "2005", "2010");
        final DataFrame<String,String> frame = DataFrame.read().csv(options -> {
            options.setResource("/worldbank/gdp_per_capita.csv");
            options.setRowKeyParser(String.class, values -> values[0]);
        });
        return frame.cols().select(years).rows().select(Array.of(
            "Brazil", "Germany", "Norway", "Singapore", "Sweden", "United Kingdom", "United States", "World"
        ));
    }
}
