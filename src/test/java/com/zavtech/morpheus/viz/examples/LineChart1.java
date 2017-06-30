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
import java.time.LocalDate;

import com.zavtech.morpheus.viz.chart.Chart;
import com.zavtech.morpheus.viz.chart.ChartEngine;
import com.zavtech.morpheus.viz.google.GChartEngine;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;

/**
 * A line chart example that displays GDP per capita data from the World Bank as a time series.
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public class LineChart1 {

    private static final Array<String> countries = Array.of(
            "Brazil", "Germany", "Norway", "Singapore", "Sweden", "United Kingdom", "United States", "World",
            "United Arab Emirates", "Canada", "Switzerland", "Denmark", "Euro area", "France", "Luxembourg"
    );

    public static void main(String[] args) {
        ChartEngine.setDefaultEngine(new GChartEngine());
        final DataFrame<LocalDate,String> tempDiff = createDataset();
        Chart.of(tempDiff, chart -> {
            chart.title().withFont(new Font("Verdana", Font.BOLD, 18));
            chart.title().withText("GDP Per Capita Across Countries (World Bank)");
            chart.axes().domain().label().withText("Date");
            chart.axes().domain().label().withFont(new Font("Arial", Font.BOLD, 14));
            chart.axes().range(0).label().withText("GDP Per Capita in USD");
            chart.axes().range(0).label().withFont(new Font("Arial", Font.BOLD, 14));
            chart.plot(0).withLines();
            chart.legend().on();
            chart.show();
        });
    }

    /**
     * Creates a time series dataset of GDP per capita for select countries from World Bank Dataset
     * We need to transform the data in several ways before its ready to present, namely:
     *  1. Filter out all columns that have a non-numeric column key, as we just want the years
     *  2. Convert these column keys from a year represented as a String to an Integer
     *  3. Convert these years to a LocalDate at the last day of that year (ie. 31-Dec)
     *  4. Filter the rows to only include the countries of interest
     *  5. Transpose the dataset so dates are on the row axis, and countries are column keys
     * @return      the newly created dataset
     */
    private static DataFrame<LocalDate,String> createDataset() {
        return DataFrame.read().<String>csv(options -> {
            options.setResource("/worldbank/gdp_per_capita.csv");
            options.setRowKeyParser(String.class, values -> values[0]);
        })
        .cols().select(col -> col.key().matches("\\d+"))
        .cols().mapKeys(col -> Integer.parseInt(col.key()))
        .cols().mapKeys(col -> LocalDate.of(col.key(), 1, 1).plusYears(1).minusDays(1))
        .rows().select(countries).transpose();
    }


}
