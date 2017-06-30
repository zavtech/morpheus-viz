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
import java.time.Year;
import com.zavtech.morpheus.viz.chart.Chart;
import com.zavtech.morpheus.viz.chart.ChartEngine;
import com.zavtech.morpheus.viz.google.GChartEngine;

import com.zavtech.morpheus.frame.DataFrame;


public class StackedBarChart {

    public static void main(String[] args) throws Exception {
        final DataFrame<Year,String> returns = DataFrame.read().csv(options -> {
            options.setResource("/annual-returns.csv");
            options.setRowKeyParser(Year.class, values -> Year.parse(values[0]));
        });
        ChartEngine.setDefaultEngine(new GChartEngine());
        Chart.of(returns, chart -> {
            chart.legend().on();
            chart.orientation().vertical();
            chart.title().withText("Annualised Asset Returns from 2000-2012");
            chart.title().withFont(new Font("Verdana", Font.BOLD, 18));
            chart.plot(0).withStackedBars(0d);
            chart.axes().domain().label().withText("Years");
            chart.axes().range(0).label().withText("Returns");
            chart.axes().range(0).format().withPattern("#,##0.##%");
            //chart.orientation().horizontal();
            //chart.data().at(0).withRangeAxis(1);
            chart.show();
        });
    }
}
