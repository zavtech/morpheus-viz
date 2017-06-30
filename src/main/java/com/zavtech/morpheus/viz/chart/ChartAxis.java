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

import com.zavtech.morpheus.range.Range;
import com.zavtech.morpheus.util.Bounds;

public interface ChartAxis {

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
    ChartAxis asLogScale();

    /**
     * Marks this axis as a linear numeric axis
     * @return  this axis
     */
    ChartAxis asLinearScale();

    /**
     * Marks this axis as a linear date axis
     * @return  this axis
     */
    ChartAxis asDateScale();

    /**
     * Sets the upper and lower bounds for the range of this axis
     * @param range     the range for this axis
     * @return          this axis
     */
    ChartAxis withRange(Bounds<?> range);

}
