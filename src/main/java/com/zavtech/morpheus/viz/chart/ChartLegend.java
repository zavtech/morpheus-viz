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
 * An interface to control the legend visibility and location on the chart
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public interface ChartLegend {

    /**
     * Turns the chart legend on
     * @return  this legend controller
     */
    ChartLegend on();

    /**
     * Turns the chart legend off
     * @return  this legend controller
     */
    ChartLegend off();

    /**
     * Positions the legend to the right of the plot
     * @return  this legend controller
     */
    ChartLegend right();

    /**
     * Positions the legend to the left of the plot
     * @return  this legend controller
     */
    ChartLegend left();

    /**
     * Positions the legend on the top of the plot
     * @return  this legend controller
     */
    ChartLegend top();

    /**
     * Positions the legend on the bottom of the plot
     * @return  this legend controller
     */
    ChartLegend bottom();

}
