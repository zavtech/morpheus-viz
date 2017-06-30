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

import com.zavtech.morpheus.viz.chart.Chart;
import com.zavtech.morpheus.viz.chart.ChartEngine;
import com.zavtech.morpheus.viz.google.GChartEngine;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;

/**
 * An example that loads an ATP dataset and computes statistics on the top 10 players
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public class BarChart2 {

    public static void main(String[] args) {
        final DataFrame<String,String> frame = createDataset();
        ChartEngine.setDefaultEngine(new GChartEngine());
        Chart.of(frame, chart -> {
            chart.title().withText("Winner Serve Outcome Statistics per hour of play in 2013 (ATP)");
            chart.title().withFont(new Font("Arial", Font.PLAIN, 16));
            chart.axes().domain().label().withText("Serve Outcomes");
            chart.axes().domain().label().withFont(new Font("Arial", Font.PLAIN, 14));
            chart.axes().domain().label().withColor(Color.BLACK);
            chart.axes().range(0).label().withText("Mean / Hour of Play");
            chart.axes().domain().label().withFont(new Font("Arial", Font.ITALIC, 16));
            chart.axes().range(0).label().withColor(Color.BLACK);
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
        final DataFrame<Integer,String> frame = DataFrame.read().csv(options -> {
            options.setResource("/tennis/atp_matches_2013.csv");
            options.setColumnType("w_ace", Double.class);  //explicit parser to double, otherwise they will be ints by default
            options.setColumnType("w_1stIn", Double.class);
            options.setColumnType("w_1stWon", Double.class);
            options.setColumnType("w_2ndWon", Double.class);
        });
        final Array<String> surfaces = frame.colAt("surface").distinct();
        final Array<String> metrics = Array.of("Aces (W)", "1st Serve In (W)", "1s Serve Won (W)", "2nd Serve Won (W)");
        return DataFrame.of(metrics, String.class, columns -> {
            surfaces.forEach(surface -> {
                final DataFrame<Integer,String> surfaceFrame = frame.rows().select(row -> row.getValue("surface").equals(surface));
                surfaceFrame.colAt("w_ace").applyDoubles(v -> v.getDouble() / v.frame().data().getDouble(v.rowOrdinal(), "minutes"));
                surfaceFrame.colAt("w_1stIn").applyDoubles(v -> v.getDouble() / v.frame().data().getDouble(v.rowOrdinal(), "minutes"));
                surfaceFrame.colAt("w_1stWon").applyDoubles(v -> v.getDouble() / v.frame().data().getDouble(v.rowOrdinal(), "minutes"));
                surfaceFrame.colAt("w_2ndWon").applyDoubles(v -> v.getDouble() / v.frame().data().getDouble(v.rowOrdinal(), "minutes"));
                columns.add(surface, Double.class).applyDoubles(v -> {
                    switch (v.rowOrdinal()) {
                        case 0: return surfaceFrame.colAt("w_ace").stats().mean() * 60;
                        case 1: return surfaceFrame.colAt("w_1stIn").stats().mean() * 60;
                        case 2: return surfaceFrame.colAt("w_1stWon").stats().mean() * 60;
                        case 3: return surfaceFrame.colAt("w_2ndWon").stats().mean() * 60;
                        default:    return Double.NaN;
                    }
                });
            });
        });
    }
}
