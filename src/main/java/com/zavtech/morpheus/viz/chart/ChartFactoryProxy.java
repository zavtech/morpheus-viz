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

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.zavtech.morpheus.viz.chart.pie.PiePlot;
import com.zavtech.morpheus.viz.chart.xy.XyPlot;
import com.zavtech.morpheus.viz.google.GChartFactory;
import com.zavtech.morpheus.viz.jfree.JFChartFactory;

/**
 * A ChartFactory that delegates all calls to some underlying factory that is implemented against a specific charting framework.
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public class ChartFactoryProxy implements ChartFactory {

    private ChartFactory defaultFactory = new JFChartFactory();
    private ChartFactory swingFactory = new JFChartFactory();
    private ChartFactory htmlFactory = new GChartFactory();

    /**
     * Constructor
     */
    ChartFactoryProxy() {
        super();
    }

    /**
     * Switches this selector into HTML chart mode
     */
    public void htmlMode() {
        this.defaultFactory = htmlFactory;
    }

    /**
     * Switches the selector into Swing chart mode
     */
    public void swingMode() {
        this.defaultFactory = swingFactory;
    }

    /**
     * Returns the HTML based chart factory
     * @return      the HTML based chart factory
     */
    public ChartFactory asHtml() {
        return htmlFactory;
    }

    /**
     * Returns the Swing based chart factory
     * @return      the Swing based chart factory
     */
    public ChartFactory asSwing() {
        return swingFactory;
    }


    @Override
    public boolean isSupported(Chart<?> chart) {
        return true;
    }


    @Override
    public String javascript(Chart... charts) {
        return defaultFactory.javascript(charts);
    }


    @Override
    public String javascript(Iterable<Chart<?>> charts) {
        return defaultFactory.javascript(charts);
    }


    @Override
    public void show(int columns, Iterable<Chart<?>> charts) {
        final Iterator<Chart<?>> iterator = charts.iterator();
        if (iterator.hasNext()) {
            final Chart<?> first = iterator.next();
            if (htmlFactory.isSupported(first)) {
                htmlFactory.show(columns, charts);
            } else if (swingFactory.isSupported(first)) {
                swingFactory.show(columns, charts);
            }
        }
    }


    @Override
    public void show(int columns, Stream<Chart<?>> charts) {
        final List<Chart<?>> chartList = charts.collect(Collectors.toList());
        final Iterator<Chart<?>> iterator = chartList.iterator();
        if (iterator.hasNext()) {
            final Chart<?> first = iterator.next();
            if (htmlFactory.isSupported(first)) {
                htmlFactory.show(columns, chartList);
            } else if (swingFactory.isSupported(first)) {
                swingFactory.show(columns, chartList);
            }
        }
    }


    @Override
    public <X extends Comparable> Chart<XyPlot<X>> ofXY(Class<X> domainType, Consumer<Chart<XyPlot<X>>> configurator) {
        return defaultFactory.ofXY(domainType, configurator);
    }

    @Override
    public <X extends Comparable, S extends Comparable> Chart<PiePlot<X, S>> ofPiePlot(boolean is3d, Consumer<Chart<PiePlot<X, S>>> configurator) {
        return defaultFactory.ofPiePlot(is3d, configurator);
    }
}
