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

import java.time.format.DateTimeFormatter;

import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.util.text.parser.Parser;
import com.zavtech.morpheus.viz.chart.ChartEngine;
import com.zavtech.morpheus.viz.google.GChartEngine;

public class LineChart2 {

    public static void main(String[] args) {
        ChartEngine.setDefaultEngine(new GChartEngine());
        final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        final DataFrame<Integer,String> df0 = DataFrame.read().csv(options -> {
            options.setResource("/tennis/atp_matches_2013.csv");
            options.setHeader(true);
            options.setParser("tourney_date", Parser.ofLocalDate(dateFormatter));
        });
        df0.rows().select(row -> row.getValue("winner_name").equals("Roger Federer")).out().print();
        /*
        final DataFrame<LocalDate,String> df1 = df0.rows().mapKeys(row -> LocalDate.of((int)row.getLong(0), (int)row.getLong(1), 1));
        final DataFrame<LocalDate,String> df2 = df1.rows().filter(row -> row.key().compareTo(startDate) >= 0).select(row -> !row.hasNulls());
        final DataFrame<LocalDate,String> temperature = df2.cols().select(Arrays.asList("tmax", "tmin"));
        Chart.of(temperature, chart -> {
            chart.title().withFont(new Font("Verdana", Font.BOLD, 18));
            chart.title().withText("Heathrow Airport Weather Data");
            chart.axes().domain().label().withText("Date");
            chart.axes().domain().label().withFont(new Font("Arial", Font.BOLD, 14));
            chart.axes().range(0).label().withText("Temperature (Degrees Celsius)");
            chart.axes().range(0).label().withFont(new Font("Arial", Font.BOLD, 14));
            chart.plot(0).withLines();
            chart.legend().on();
            chart.show();
        });
        */
    }

}
