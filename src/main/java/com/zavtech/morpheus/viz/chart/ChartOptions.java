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

import java.awt.*;

/**
 * An interface that allows for control of various additional options for a Chart.
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public interface ChartOptions {

    /**
     * Returns the preferred size for this chart
     * @return      the preferred size
     */
    Dimension getPreferredSize();

    /**
     * Sets the preferred size for the chart
     * @param width     the preferred width in pixels
     * @param height    the preferred heigth in pixels
     * @return          these options
     */
    ChartOptions withPreferredSize(int width, int height);


    /**
     * A default implementation of the ChartOptions
     */
    class Default implements ChartOptions {

        private Dimension preferredSize = new Dimension(800, 500);

        @Override
        public Dimension getPreferredSize() {
            return preferredSize;
        }

        @Override
        public ChartOptions withPreferredSize(int width, int height) {
            this.preferredSize = new Dimension(width, height);
            return this;
        }
    }

}
