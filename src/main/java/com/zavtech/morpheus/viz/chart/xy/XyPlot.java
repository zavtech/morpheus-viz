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

import com.zavtech.morpheus.viz.util.ColorModel;

/**
 * The interface to an XyPlot which provides access to the axes, orientation, style and model controllers.
 *
 * @author  Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public interface XyPlot<X extends Comparable> {

    /**
     * Returns the plot axes control interface
     * @return  the plot axes control
     */
    XyAxes axes();

    /**
     * Returns the interface to set the chart orientation
     * @return  the orientation interface
     */
    XyOrient orient();

    /**
     * Returns the chart plot style interface for the dataset index
     * @param index the dataset index, 0 being the first dataset
     * @return      the plot style interface for the dataset specified
     */
    XyRender render(int index);

    /**
     * Returns the series style interface for the series key specified
     * @param seriesKey the series key to operate
     * @return          the style interface for the series key
     */
    XyStyle style(Comparable seriesKey);

    /**
     * Applies a color model to choose series colors for this plot
     * @param colorModel    the color model for plot
     * @return              this plot reference
     */
    XyPlot<X> withColorModel(ColorModel colorModel);

    /**
     * Returns the plot data control interface
     * @return  the plot data control interface
     */
    <S extends Comparable> XyModel<X,S> data();

    /**
     * Returns the trend line controller interface for the series specified
     * @param seriesKey     the series key to create trend line for
     * @return  the trend line controller interface for series
     */
    <S extends Comparable> XyTrend trend(S seriesKey);

}
