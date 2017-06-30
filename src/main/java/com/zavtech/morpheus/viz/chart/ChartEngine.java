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

import com.zavtech.morpheus.viz.jfree.JFChartEngine;
import com.zavtech.morpheus.frame.DataFrame;

/**
 *
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */

public abstract class ChartEngine {

    private static ChartEngine defaultEngine = new JFChartEngine();

    /**
     * Returns a reference to the default chart engine
     * @return  the default chart engine
     */
    public static ChartEngine getDefaultEngine() {
        return defaultEngine;
    }

    /**
     * Sets a reference to the default chart engine
     * @param defaultEngine     the default chart engine
     */
    public static void setDefaultEngine(ChartEngine defaultEngine) {
        ChartEngine.defaultEngine = defaultEngine;
    }

    public abstract void show(int rows, int cols, Iterable<Chart> charts);

    /**
     * Returns a newly created Chart to display the data frame provided
     * @param frame     the DataFrame for the chart
     * @return          the newly created chart
     */
    public abstract <X extends Comparable,S extends Comparable> Chart<X> create(DataFrame<X,S> frame);

    /**
     * Returns a newly created Chart to display the data frame provided
     * @param frame         the DataFrame for the chart
     * @param domainType    the domain class type
     * @param domainKey     the column key in the frame that defines the domain
     * @return              the newly created chart
     */
    public abstract <X extends Comparable,S extends Comparable> Chart<X> create(DataFrame<?,S> frame, Class<X> domainType, S domainKey);

}
