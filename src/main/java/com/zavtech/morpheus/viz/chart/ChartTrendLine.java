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
package com.zavtech.morpheus.viz.chart;

/**
 * An interface to manage trend-lines on a chart
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public interface ChartTrendLine {

    /**
     * Adds a linear trend line for the series key specified
     * @param seriesKey     the series key to add trend line for
     * @param trendKey      the key to assign to the calculated trend series
     */
    <S extends Comparable,T extends Comparable> ChartSeriesStyle add(S seriesKey, T trendKey);

    /**
     * Removes a linear trend line for the trend key specified
     * @param trendKey  the trend key of the trend line to remove
     */
    <T extends Comparable> void remove(T trendKey);
}
