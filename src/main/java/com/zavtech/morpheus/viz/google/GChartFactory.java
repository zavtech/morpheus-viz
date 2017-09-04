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

import java.awt.*;
import java.io.File;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.zavtech.morpheus.util.Collect;
import com.zavtech.morpheus.viz.chart.Chart;
import com.zavtech.morpheus.viz.chart.ChartFactory;
import com.zavtech.morpheus.viz.chart.pie.PiePlot;
import com.zavtech.morpheus.viz.chart.xy.XyPlot;
import com.zavtech.morpheus.viz.html.HtmlWriter;
import com.zavtech.morpheus.viz.js.Javascript;

/**
 * A ChartEngine implementation used to create Google chart instances based on the Morpheus charting API.
 *
 * @link https://developers.google.com/chart/
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public class GChartFactory implements ChartFactory {


    /**
     * Constructor
     */
    public GChartFactory() {
        super();
    }


    @Override
    public boolean isSupported(Chart<?> chart) {
        return chart instanceof GChart;
    }


    @Override
    public String javascript(Iterable<Chart<?>> charts) {
        return javascript(Collect.asStream(charts).toArray(Chart[]::new));
    }


    @Override
    public String javascript(Chart... charts) {
        return Javascript.create(jsWriter -> {
            jsWriter.newLine().write("google.charts.load('current', {'packages':['corechart']});");
            jsWriter.newLine().write("google.charts.setOnLoadCallback(%s);", "drawCharts");
            jsWriter.newLine();
            jsWriter.newFunction("drawCharts", init -> {
                for (int i=0; i<charts.length; ++i) {
                    init.write("drawChart_%s()", i);
                    init.newLine();
                }
            });
            for (int i=0; i<charts.length; ++i) {
                final GChart gChart = (GChart)charts[i];
                final String functionName = String.format("drawChart_%s", i);
                final String divName = String.format("chart_%s", i);
                jsWriter.newLine().newLine();
                gChart.accept(jsWriter, functionName, divName);
            }
        });
    }


    @Override
    public void show(int columns, Stream<Chart<?>> charts) {
        show(columns, Collect.asIterable(charts));
    }


    @Override
    public void show(int columns, Iterable<Chart<?>> charts) {
        try {
            final HtmlWriter htmlWriter = new HtmlWriter();
            final AtomicInteger chartIndex = new AtomicInteger(-1);
            htmlWriter.newElement("html", html -> {
                html.newElement("head", head -> {
                    head.newElement("script", script -> {
                        script.newAttribute("type", "text/javascript");
                        script.newAttribute("src", "https://www.gstatic.com/charts/loader.js");
                    });
                    head.newElement("script", script -> {
                        script.newAttribute("type", "text/javascript");
                        script.text(javascript(charts));
                    });
                });

                final String width = String.valueOf((int)(100d / columns)) + "%";
                final String height = String.valueOf((int)(100d / columns * 0.9d)) + "%";
                html.newElement("body", body -> {
                    chartIndex.set(-1);
                    charts.forEach(chart -> {
                        body.newElement("div", div -> {
                            div.newAttribute("id", String.format("chart_%s", chartIndex.incrementAndGet()));
                            div.newAttribute("style", String.format("width:%s;height:%s;float:left;", width, height));
                        });
                    });
                });
            });

            final File dir = new File(System.getProperty("user.home"), ".morpheus/charts");
            final File file = new File(dir, UUID.randomUUID().toString() + ".html");
            if (file.getParentFile().mkdirs()) System.out.println("Created directory: " + dir.getAbsolutePath());

            System.out.println(htmlWriter.toString());

            htmlWriter.flush(file);
            Desktop.getDesktop().browse(file.toURI());
        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate Google chart", ex);
        }
    }


    @Override
    public <X extends Comparable> Chart<XyPlot<X>> ofXY(Class<X> domainType, Consumer<Chart<XyPlot<X>>> configurator) {
        final Chart<XyPlot<X>> chart = new GChart<>(new GXyPlot<>());
        if (configurator != null) {
            configurator.accept(chart);
        }
        return chart;
    }


    @Override
    public <X extends Comparable, S extends Comparable> Chart<PiePlot<X, S>> ofPiePlot(boolean is3d, Consumer<Chart<PiePlot<X, S>>> configurator) {
        final Chart<PiePlot<X,S>> chart = new GChart<>(new GPiePlot<>(is3d));
        if (configurator != null) {
            configurator.accept(chart);
        }
        return chart;
    }
}
