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
package com.zavtech.morpheus.viz.chart.xy;

import java.awt.*;

/**
 * An interface to a linear trend line in an XyPlot.
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public interface XyTrend {

    /**
     * Returns the series key for this trend line
     * @return      the series key
     */
    Comparable seriesKey();

    /**
     * Removes this trend line from the chart
     * @return      this trend line
     */
    XyTrend clear();

    /**
     * Sets the color for the trend line
     * @param color the color for trend line
     * @return this trend controller
     */
    XyTrend withColor(Color color);

    /**
     * Sets the line width for the trend line
     * @param width the line width
     * @return this trend controller
     */
    XyTrend withLineWidth(float width);

}
