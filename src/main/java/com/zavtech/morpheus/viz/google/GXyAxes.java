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
package com.zavtech.morpheus.viz.google;

import java.util.HashMap;
import java.util.Map;

import com.zavtech.morpheus.viz.chart.xy.XyAxes;
import com.zavtech.morpheus.viz.chart.xy.XyAxis;

/**
 * Class summary goes here...
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
 class GXyAxes implements XyAxes {

    private GXyPlot plot;
    private GXyAxis domainAxis = new GXyAxis(true);
    private Map<Integer,GXyAxis> rangeAxesMap = new HashMap<>();

    /**
     * Constructor
     */
    GXyAxes(GXyPlot plot) {
        this.plot = plot;
        this.rangeAxesMap.put(0, new GXyAxis(false));
    }


    @Override
    public XyAxis domain() {
        return domainAxis;
    }


    @Override
    public XyAxis range(int index) {
        GXyAxis rangeAxis = rangeAxesMap.get(index);
        if (rangeAxis == null) {
            rangeAxis = new GXyAxis(false);
            rangeAxesMap.put(index, rangeAxis);
        }
        return rangeAxis;
    }


    /**
     * Returns the number of range axis
     * @return      the number of range axis
     */
    int rangeAxisCount() {
        return rangeAxesMap.size();
    }

}
