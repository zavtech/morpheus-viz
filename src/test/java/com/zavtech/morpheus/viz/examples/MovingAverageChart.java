/**
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

import java.time.LocalDate;

import com.zavtech.morpheus.viz.chart.Chart;
import com.zavtech.morpheus.viz.jfree.TestProvider;
import com.zavtech.morpheus.frame.DataFrame;

/**
 * A test of a time series chart...
 *
 * @author  Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public class MovingAverageChart {

    public static void main(String[] args) throws Exception {
        final DataFrame<LocalDate,String> frame = TestProvider.getQuotes("aapl");
        final DataFrame<LocalDate,String> close = frame.cols().select(column -> column.key().equalsIgnoreCase("Adj Close"));
        final DataFrame<LocalDate,String> closeSma = close.calc().ema(50).cols().mapKeys(key -> key + "(SMA)");
        final LocalDate startDate = LocalDate.of(2013, 6, 14);
        closeSma.update(close, false, true);
        final DataFrame<LocalDate,String> dataset = closeSma.rows().select(row -> row.key().isAfter(startDate));
        Chart.of(dataset, chart -> {
            chart.title().withText("Apple");
            chart.axes().domain().label().withText("Date");
            chart.axes().range(0).label().withText("Price");
            chart.show();
        });
    }

}
