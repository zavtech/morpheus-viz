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

import java.awt.Color;

/**
 * An interface to control series specific styling features for rendering
 *
 * @author  Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public interface ChartSeriesStyle {

    /**
     * Sets the color for this series
     * @param color the color for series
     * @return      this style instance
     */
    ChartSeriesStyle withColor(Color color);

    /**
     * Sets whether line based plots should use a dashed line
     * @param dashed    true if lines should be dashed
     * @return          this style instance
     */
    ChartSeriesStyle withDashes(boolean dashed);

    /**
     * Sets the line width for the series for line based plots
     * @param width the line wisth
     * @return      this style instance
     */
    ChartSeriesStyle withLineWidth(float width);

    /**
     * Sets the point shape to use for this series
     * @param shape the shape for series
     * @return      this style instance
     */
    ChartSeriesStyle withPointShape(ChartShape shape);

    /**
     * Sets whether points should be visible for this series
     * @param visible   true to make points visible
     * @return          this style instance
     */
    ChartSeriesStyle withPointsVisible(boolean visible);

}
