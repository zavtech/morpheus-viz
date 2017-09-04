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

import java.io.File;
import java.io.OutputStream;
import java.util.stream.Stream;

/**
 * The top level interface to the Morpheus Chart abstraction API which can be implemented against various underlying libraries.
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public interface Chart<P> {

    ChartFactoryProxy factory = new ChartFactoryProxy();

    /**
     * Returns a reference to the chart factory instance
     * @return      the chart factory instance
     */
    static ChartFactoryProxy create() {
        return factory;
    }

    /**
     * Displays the collection of charts in a grid with the number of columns specified
     * @param columns   the number of columns for chart grid
     * @param charts    the sequence of charts to plot
     */
    static void show(int columns, Iterable<Chart<?>> charts) {
        factory.show(columns, charts);
    }

    /**
     * Displays the collection of charts in a grid with the number of columns specified
     * @param columns   the number of columns for chart grid
     * @param charts    the sequence of charts to plot
     */
    static void show(int columns, Stream<Chart<?>> charts) {
        factory.show(columns, charts);
    }

    /**
     * Returns the plot interface for this chart
     * @return  the plot interface for this chart
     */
    P plot();

    /**
     * Returns the chart title interface
     * @return  chart title interface
     */
    ChartLabel title();

    /**
     * Returns the chart subtitle interface
     * @return  chart subtitle interface
     */
    ChartLabel subtitle();

    /**
     * Returns the chart legend control for this chart
     * @return      the legend control interface
     */
    ChartLegend legend();

    /**
     * Returns the theme interface for this chart
     * @return      the theme interface
     */
    ChartTheme theme();

    /**
     * Returns the options interface for this chart
     * @return  additional chart options
     */
    ChartOptions options();

    /**
     * Shows this chart on an appropriate output device
     * @return  this chart
     */
    Chart show();

    /**
     * Shows this chart on an appropriate output device
     * @param width     the width for chart
     * @param height    the height for chart
     * @return          this chart
     */
    Chart show(int width, int height);

    /**
     * Writes a PNH image of the chart to the output stream
     * @param file          the file reference
     * @param width         the image width
     * @param height        the image height
     * @param transparent   true for a transparent background
     * @return              this chart
     */
    Chart writerPng(File file, int width, int height, boolean transparent);

    /**
     * Writes a PNH image of the chart to the output stream
     * @param os            the output stream
     * @param width         the image width
     * @param height        the image height
     * @param transparent   true for a transparent background
     * @return              this chart
     */
    Chart writerPng(OutputStream os, int width, int height, boolean transparent);


}


