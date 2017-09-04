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

import com.zavtech.morpheus.util.Bounds;
import com.zavtech.morpheus.viz.chart.ChartFormat;
import com.zavtech.morpheus.viz.chart.ChartLabel;
import com.zavtech.morpheus.viz.chart.ChartTextStyle;

/**
 * An interface to an axis in an XyPlot.
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public interface XyAxis {

    /**
     * Returns the label for this axis
     * @return  the label for axis
     */
    ChartLabel label();

    /**
     * Returns the format for this axis
     * @return  the format for this axis
     */
    ChartFormat format();

    /**
     * Returns the style interface for this axis
     * @return      the style interface
     */
    ChartTextStyle ticks();

    /**
     * Marks this axis as a log scale axis
     * @return  this axis
     */
    XyAxis asLogScale();

    /**
     * Marks this axis as a linear numeric axis
     * @return  this axis
     */
    XyAxis asLinearScale();

    /**
     * Marks this axis as a linear date axis
     * @return  this axis
     */
    XyAxis asDateScale();

    /**
     * Sets the upper and lower bounds for the range of this axis
     * @param range     the range for this axis
     * @return          this axis
     */
    XyAxis withRange(Bounds<?> range);
}
