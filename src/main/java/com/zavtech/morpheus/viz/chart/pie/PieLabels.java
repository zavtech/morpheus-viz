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
 * An interface to control the labelling of sections on a PiePlot.
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public interface PieLabels {

    /**
     * Signals to turn on pie labels
     * @return  this label controller
     */
    PieLabels on();

    /**
     * Signals to turn off pie labels
     * @return  this label controller
     */
    PieLabels off();

    /**
     * Signals to show section names
     * @return  this label controller
     */
    PieLabels withName();

    /**
     * Signals to show section values
     * @return  this label controller
     */
    PieLabels withValue();

    /**
     * Signals to show section percentages
     * @return  this label controller
     */
    PieLabels withPercent();

    /**
     * Sets the font for pie section labels
     * @param font  the font to accept to labels
     * @return  this label controller
     */
    PieLabels withFont(Font font);

    /**
     * Sets the text color for pie section labels
     * @param color the text color
     * @return  this label controller
     */
    PieLabels withTextColor(Color color);

    /**
     * Sets the background color for pie section labels
     * @param color the background color
     * @return  this label controller
     */
    PieLabels withBackgroundColor(Color color);


}
