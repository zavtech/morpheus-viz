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

import java.util.function.Function;
import java.util.function.IntFunction;

import com.zavtech.morpheus.frame.DataFrame;

/**
 * Interface to an XY dataset that is associated with a single DataFrame which holds the data for an XYPlot.
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public interface XyDataset<X extends Comparable,S extends Comparable> {

    /**
     * Triggers a refresh of this data based on the bound supplier
     */
    void refresh();

    /**
     * Returns true if this dataset is empty
     * @return  true if no data
     */
    boolean isEmpty();

    /**
     * Clears all data for this dataset
     * @param notify    if true, fire notification event
     */
    void clear(boolean notify);

    /**
     * Returns the current frame for this data, which can be null
     * @return      the frame for this data
     */
    <R> DataFrame<R,S> frame();

    /**
     * Returns the domain type for this dataset
     * @return  the domain type for dataset
     */
    Class<X> domainType();

    /**
     * Returns the domain function for this dataset
     * @return  the domain function the yield domain values for an index
     */
    IntFunction<X> domainFunction();

    /**
     * Returns true if this data contains the series specified
     * @param seriesKey     the series key
     * @return              true if this data contains the series
     */
    boolean contains(S seriesKey);

    /**
     * Sets the function to supply the lower domain interval value for this dataset
     * The lower domain interval function accepts a domain key and returns the interval to subtract
     * @param lowerIntervalFunction    the lower domain interval function
     * @return  this model reference
     */
    XyDataset<X,S> withLowerDomainInterval(Function<X,X> lowerIntervalFunction);

    /**
     * Sets the function to supply the upper domain interval value for this dataset
     * The upper domain interval function accepts a domain key and returns the interval to subtract
     * @param upperIntervalFunction    the upper domain interval function
     * @return  this model reference
     */
    XyDataset<X,S> withUpperDomainInterval(Function<X,X> upperIntervalFunction);

}
