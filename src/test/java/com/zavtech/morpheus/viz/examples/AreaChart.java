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
import java.util.function.ToDoubleFunction;

import com.zavtech.morpheus.viz.chart.Chart;
import com.zavtech.morpheus.viz.chart.ChartEngine;
import com.zavtech.morpheus.viz.google.GChartEngine;
import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;

public class AreaChart {

    public static void main(String[] args) {
        ChartEngine.setDefaultEngine(new GChartEngine());
        final DataFrame<Double,String> frame = createData();
        Chart.of(frame, chart -> {
            chart.title().withText("Cost of a Pint as you leave city centre");
            chart.axes().domain().label().withText("Distance from Centre");
            chart.axes().domain().label().withFont(new Font("Arial", Font.BOLD, 14));
            chart.axes().range(0).label().withText("Cost of Pint (Sterling)");
            chart.plot(0).withArea(false);
            chart.legend().on();
            chart.show();
        });
    }


    private static DataFrame<Double,String> createData() {
        final Array<Double> distances = Array.of(5d, 10d, 20d, 30d, 40d, 50d, 60d, 70d, 80d, 90d, 100d);
        final ToDoubleFunction<Integer> calc = v -> 5d / Math.log(distances.getDouble(v)) + Math.random() / 5d;
        return DataFrame.of(distances, String.class, columns -> {
            columns.add("London", Array.of(Double.class, distances.length()).applyDoubles(v -> 2d + calc.applyAsDouble(v.index())));
            columns.add("Manchester", Array.of(Double.class, distances.length()).applyDoubles(v -> 1d + calc.applyAsDouble(v.index())));
            columns.add("Liverpool", Array.of(Double.class, distances.length()).applyDoubles(v -> 1d + calc.applyAsDouble(v.index())));
            columns.add("Oxford", Array.of(Double.class, distances.length()).applyDoubles(v -> 1.5d + calc.applyAsDouble(v.index())));
            columns.add("Cambridge", Array.of(Double.class, distances.length()).applyDoubles(v -> 1.5d + calc.applyAsDouble(v.index())));
            columns.add("Bristol", Array.of(Double.class, distances.length()).applyDoubles(v -> 0.8d + calc.applyAsDouble(v.index())));
            columns.add("Cardiff", Array.of(Double.class, distances.length()).applyDoubles(v -> 0.6 + calc.applyAsDouble(v.index())));
        });
    }

}
