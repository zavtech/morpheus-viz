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

import com.zavtech.morpheus.frame.DataFrame;

/**
 * An interface to the data model that is bound to a PiePlot.
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public interface PieModel<X extends Comparable,S extends Comparable> {

    /**
     * Returns true if this dataset is empty
     * @return  true if no data
     */
    boolean isEmpty();

    /**
     * Returns an iterable for section keys
     * @return  the iterable of pie section keys
     */
    Iterable<X> keys();

    /**
     * Clears all data for this dataset
     * @param notify    if true, fire notification event
     */
    void clear(boolean notify);

    /**
     * Applies data from the first numeric column in the DataFrame, using row keys as items
     * @param frame         the DataFrame reference
     */
    void apply(DataFrame<X,S> frame);

    /**
     * Applies data from the specified column in the DataFrame, using row keys as items
     * @param frame         the DataFrame reference
     * @param valueKey       the column key that contains the pie plor data values
     */
    void apply(DataFrame<X,S> frame, S valueKey);

    /**
     * Applies data from the valueKey column in the DataFrame, using items from the itemKey column
     * @param frame         the DataFrame reference
     * @param valueKey      the column key that contains the pie plor data values
     * @param itemKey       the column key that contains the pie plot labels
     */
    void apply(DataFrame<?,S> frame, S itemKey, S valueKey);

}
