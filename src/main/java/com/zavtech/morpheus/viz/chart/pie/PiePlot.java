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
package com.zavtech.morpheus.viz.chart.pie;

import java.awt.*;

/**
 * An interface to control pie based plots.
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public interface PiePlot<X extends Comparable,S extends Comparable> {

    /**
     * Returns the data interface for this plot
     * @return      the data interface
     */
    PieModel<X,S> data();

    /**
     * Returns the label controller for this plot
     * @return      the label controller
     */
    PieLabels labels();

    /**
     * Returns the section controller interface for the item specified
     * @param itemKey   the key for the section item
     * @return          the section controller
     */
    PieSection section(X itemKey);

    /**
     * Sets the start angle for the first section in degrees
     * @param degrees       the degrees for start angle
     * @return              this pie plot
     */
    PiePlot<X,S> withStartAngle(double degrees);

    /**
     * Sets the size of the pie hole for donut plots
     * This feature does not work on 3D plots and is ignored
     * @param percent   the percent size for pie hole
     * @return          this pie plot
     */
    PiePlot<X,S> withPieHole(double percent);

    /**
     * Sets the outline color for pie sections
     * @param color     the color instance
     * @return          this pie plot controller
     */
    PiePlot<X,S> withSectionOutlineColor(Color color);

}
