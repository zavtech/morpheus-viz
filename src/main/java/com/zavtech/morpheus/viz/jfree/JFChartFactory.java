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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.swing.*;

import com.zavtech.morpheus.util.Collect;
import com.zavtech.morpheus.viz.chart.Chart;
import com.zavtech.morpheus.viz.chart.ChartException;
import com.zavtech.morpheus.viz.chart.ChartFactory;
import com.zavtech.morpheus.viz.chart.pie.PiePlot;
import com.zavtech.morpheus.viz.chart.xy.XyPlot;
import com.zavtech.morpheus.viz.js.Javascript;

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
        return Javascript.create(jsWriter -> {
            try {
                jsWriter.newLine().write("window.onload = function() {");
                jsWriter.indent(4);
                jsWriter.newLine().write("console.info('Writing charts...');");
                for (int i=0; i<charts.length; ++i) {
                    final String id = String.format("chart_%s", i);
                    final String image = toBase64Image(charts[i]);
                    jsWriter.newLine();
                    jsWriter.write("drawChart('%s', '%s');", id, image);
                }
                jsWriter.unident(4);
                jsWriter.newLine().write("};");
                jsWriter.newLine().newLine();
                jsWriter.newFunction("drawChart", "chartId, imageString", func -> {
                    func.write("var divElement = document.getElementById(chartId);");
                    func.newLine().write("var imageElement = document.createElement('img');");
                    func.newLine().write("imageElement.setAttribute('src', 'data:image/png;base64,' + imageString);");
                    func.newLine().write("imageElement.setAttribute('alt', 'Embedded Chart');");
                    func.newLine().write("imageElement.setAttribute('class', 'chart');");
                    func.newLine().write("divElement.appendChild(imageElement);");
                });
            } catch (Exception ex) {
                throw new ChartException("Failed to generate Javascript for charts", ex);
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


    /**
     * Returns a base64 encoded string of a PNG image generated from the chart
     * @param chart     the chart reference
     * @return          the base64 encoded image
     * @throws IOException  if there is an I/O exception
     */
    private String toBase64Image(Chart chart) throws IOException {
        final int width = (int)chart.options().getPreferredSize().getWidth();
        final int height = (int)chart.options().getPreferredSize().getHeight();
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        chart.writerPng(baos, width, height, true);
        final byte[] imageBytes = baos.toByteArray();
        final Base64.Encoder encoder = Base64.getEncoder();
        final byte[] base64 = encoder.encode(imageBytes);
        return new String(base64);
    }

}
