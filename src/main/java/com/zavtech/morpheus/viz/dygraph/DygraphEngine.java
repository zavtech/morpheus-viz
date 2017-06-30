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
package com.zavtech.morpheus.viz.dygraph;

import com.zavtech.morpheus.viz.chart.Chart;
import com.zavtech.morpheus.viz.chart.ChartEngine;
import com.zavtech.morpheus.frame.DataFrame;

/**
 * A ChartEngine implementation used to create Dygraphs instances based on the Morpheus charting API.
 *
 * @link http://dygraphs.com/
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public class DygraphEngine extends ChartEngine {


    @Override
    public <X extends Comparable,S extends Comparable> Chart<X> create(DataFrame<X,S> frame) {
        if (frame == null) {
            throw new IllegalArgumentException("The DataFrame cannot be null");
        } else {
            final Chart<X> chart = new Dygrah<>();
            chart.data().add(frame);
            return chart;
        }
    }

    @Override
    public <X extends Comparable,S extends Comparable> Chart<X> create(DataFrame<?,S> frame, Class<X> domainType, S domainKey) {
        if (frame == null) {
            throw new IllegalArgumentException("The DataFrame cannot be null");
        } else {
            final Chart<X> chart = new Dygrah<>();
            chart.data().add(frame, domainKey);
            return chart;
        }
    }


    @Override
    public void show(int rows, int cols, Iterable<Chart> charts) {

    }
}

