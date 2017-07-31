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

import com.zavtech.morpheus.viz.chart.ChartFormat;

/**
 * An implementation of ChartFormat used to manage formatting options for an axis.
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
class GChartFormat implements ChartFormat {

    private boolean domain;
    private String pattern;

    /**
     * Constructor
     *
     * @param domain true if this represents the domain axis
     */
    GChartFormat(boolean domain) {
        this.domain = domain;
    }

    @Override
    public void withPattern(String pattern) {
        this.pattern = pattern;
    }

    /**
     * Returns the pattern to render axis tick labels
     * @return the pattern for axis tick value
     */
    String getPattern(GXyDataset dataset) {
        if (pattern != null) {
            return pattern;
        } else if (dataset == null) {
            return "auto";
        } else {
            final Class<?> typeClass = domain ? dataset.domainType() : Number.class;
            final GDataType type = GDataType.getDataType(typeClass, GDataType.STRING);
            switch (type) {
                case BOOLEAN:       return "auto";
                case STRING:        return "auto";
                case NUMBER:        return "decimal";
                case DATE:          return "dd-MMM-yyyy";
                case DATETIME:      return "dd-MMM-yyyy HH:mm";
                case LOCAL_TIME:    return "HH:mm";
                default:    throw new IllegalStateException("Unsupported type specified: " + type);
            }
        }
    }
}
