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
package com.zavtech.morpheus.viz.jfree;

import java.awt.*;
import java.security.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.swing.*;

import com.zavtech.morpheus.util.Collect;
import com.zavtech.morpheus.viz.chart.Chart;
import com.zavtech.morpheus.viz.chart.ChartFactory;
import com.zavtech.morpheus.viz.chart.pie.PiePlot;
import com.zavtech.morpheus.viz.chart.xy.XyPlot;
import com.zavtech.morpheus.viz.js.JsCode;

import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;

/**
 * A ChartEngine implementation used to create JFreeChart instances based on the Morpheus charting API.
 *
 * @link http://www.jfree.org/jfreechart/
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public class JFChartFactory implements ChartFactory {

    private static final Set<Class<?>> timeTypeSet = new HashSet<>();

    /**
     * Static initializer
     */
    static {
        timeTypeSet.add(Date.class);
        timeTypeSet.add(LocalDate.class);
        timeTypeSet.add(LocalDateTime.class);
        timeTypeSet.add(ZonedDateTime.class);
        timeTypeSet.add(Timestamp.class);
        timeTypeSet.add(java.sql.Date.class);
        timeTypeSet.add(Calendar.class);
    }



    @Override
    public void show(int columns, Stream<Chart<?>> charts) {
        show(columns, Collect.asIterable(charts));
    }


    @Override
    public void show(int columns, Iterable<Chart<?>> charts) {
        final JFrame frame = new JFrame();
        final List<Chart<?>> chartList = Collect.asList(charts);
        final double rowCount = Math.ceil(Math.max(1d, (double)chartList.size() / columns));
        frame.getContentPane().setLayout(new GridLayout((int)rowCount, columns));
        for (Chart chart : chartList) {
            final JFChartBase jChart = (JFChartBase)chart;
            frame.getContentPane().add(jChart.chartPanel());
        }
        frame.getContentPane().setBackground(Color.WHITE);
        frame.pack();
        frame.setSize(1024, 768);
        frame.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }


    @Override
    public boolean isSupported(Chart<?> chart) {
        return chart instanceof JFCatChart || chart instanceof JFPieChart || chart instanceof JFXyChart;
    }


    @Override
    public String javascript(Iterable<Chart<?>> charts) {
        return javascript(Collect.asStream(charts).toArray(Chart[]::new));
    }


    @Override
    public String javascript(Chart... charts) {
        return JsCode.create(jsCode -> {
            jsCode.newLine().write("window.onload = drawCharts");
            jsCode.newLine();
            jsCode.newFunction("drawCharts", init -> {
                jsCode.write("console.info('Writing charts...');");
                for (int i=0; i<charts.length; ++i) {
                    init.newLine();
                    init.write("drawChart_%s();", i);
                }
            });
            for (int i=0; i<charts.length; ++i) {
                final Chart chart = charts[i];
                final String functionName = String.format("drawChart_%s", i);
                final String divId = chart.options().getId().orElse(String.format("chart_%s", i));
                jsCode.newLine().newLine();
                chart.accept(jsCode, functionName, divId);
            }
        });
    }


    @Override
    public <X extends Comparable> Chart<XyPlot<X>> ofXY(Class<X> domainType, Consumer<Chart<XyPlot<X>>> configurator) {
        if (Integer.class.equals(domainType)) {
            final NumberAxis domainAxis = new NumberAxis();
            final NumberAxis rangeAxis = new NumberAxis();
            domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
            final Chart<XyPlot<X>> chart = new JFXyChart<>(domainAxis, rangeAxis, false);
            if (configurator != null) {
                configurator.accept(chart);
            }
            return chart;
        } else if (Number.class.isAssignableFrom(domainType)) {
            final NumberAxis domainAxis = new NumberAxis();
            final NumberAxis rangeAxis = new NumberAxis();
            final Chart<XyPlot<X>> chart = new JFXyChart<>(domainAxis, rangeAxis, false);
            if (configurator != null) {
                configurator.accept(chart);
            }
            return chart;
        } else if (isTimeBased(domainType)) {
            final JFDateAxis domainAxis = new JFDateAxis();
            final NumberAxis rangeAxis = new NumberAxis();
            final Chart<XyPlot<X>> chart = new JFXyChart<>(domainAxis, rangeAxis, false);
            if (configurator != null) {
                configurator.accept(chart);
            }
            return chart;
        } else {
            final CategoryAxis domainAxis = new CategoryAxis();
            final NumberAxis rangeAxis = new NumberAxis();
            final Chart<XyPlot<X>> chart = new JFCatChart<>(domainAxis, rangeAxis, false);
            if (configurator != null) {
                configurator.accept(chart);
            }
            return chart;
        }
    }


    @Override
    public <X extends Comparable, S extends Comparable> Chart<PiePlot<X, S>> ofPiePlot(boolean is3d, Consumer<Chart<PiePlot<X, S>>> configurator) {
        final Chart<PiePlot<X,S>> chart = new JFPieChart<>(is3d, true);
        if (configurator != null) {
            configurator.accept(chart);
        }
        return chart;
    }


    /**
     * Returns true if the data type is time series related
     * @param type  the data type
     * @return      true if time related
     */
    private boolean isTimeBased(Class<?> type) {
        return timeTypeSet.contains(type);
    }

}
