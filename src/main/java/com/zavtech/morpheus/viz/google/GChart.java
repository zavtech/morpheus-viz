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
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Stream;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.range.Range;
import com.zavtech.morpheus.viz.chart.Chart;
import com.zavtech.morpheus.viz.chart.ChartLabel;
import com.zavtech.morpheus.viz.chart.ChartLegend;
import com.zavtech.morpheus.viz.chart.ChartOptions;
import com.zavtech.morpheus.viz.chart.ChartTheme;
import com.zavtech.morpheus.viz.chart.pie.PiePlot;
import com.zavtech.morpheus.viz.html.HtmlCode;
import com.zavtech.morpheus.viz.js.JsCode;

/**
 * A Chart implementation that uses the Google Charting library to render charts in a browser
 *
 * @param <P>   the plot type for this chart
 *
 * @author  Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
class GChart<P> implements Chart<P> {

    private P plot;
    private ChartOptions options = new ChartOptions.Default();
    private LegendAdapter legend = new LegendAdapter();
    private GChartLabel title = new GChartLabel(Color.BLACK, new Font("Arial", Font.BOLD, 16));
    private GChartLabel subtitle = new GChartLabel(Color.BLACK, new Font("Arial", Font.BOLD, 14));

    /**
     * Constructor
     * @param plot  the plot for this chart
     */
    GChart(P plot) {
        this.plot = plot;
    }

    @Override
    public P plot() {
        return plot;
    }

    @Override
    public ChartLabel title() {
        return title;
    }

    @Override
    public ChartLabel subtitle() {
        return subtitle;
    }

    @Override
    public ChartTheme theme() {
        return null;
    }

    @Override
    public ChartOptions options() {
        return options;
    }

    @Override
    public ChartLegend legend() {
        return legend;
    }

    @Override
    public Chart show() {
        return show(1024, 768);
    }


    @SuppressWarnings("unchecked")
    public Chart show(int width, int height) {
        try {
            final String divName = "chart1";
            final String functionName = "drawChart1";
            final HtmlCode writer = new HtmlCode();
            writer.newElement("html", html -> {
                html.newElement("head", head -> {
                    head.newElement("script", script -> {
                        script.newAttribute("type", "text/javascript");
                        script.newAttribute("src", "https://www.gstatic.com/charts/loader.js");
                    });
                    head.newElement("script", script -> {
                        script.newAttribute("type", "text/javascript");
                        script.text(JsCode.create(js -> {
                            js.newLine().write("google.charts.load('current', {'packages':['corechart']});");
                            js.newLine().write("google.charts.setOnLoadCallback(%s);", functionName);
                            this.accept(js, functionName, divName);
                        }));
                    });
                });
                html.newElement("body", body -> {
                    body.newElement("div", div -> {
                        div.newAttribute("id", divName);
                        div.newAttribute("style", "width:100%;height:100%;");
                    });
                });
            });

            final File dir = new File(System.getProperty("user.home"), ".morpheus/charts");
            final File file = new File(dir, UUID.randomUUID().toString() + ".html");
            if (file.getParentFile().mkdirs()) System.out.println("Created directory: " + dir.getAbsolutePath());

            System.out.println(writer.toString());

            writer.flush(file);
            Desktop.getDesktop().browse(file.toURI());
            return this;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate Google chart", ex);
        }
    }


    @Override
    public Chart writerPng(File file, int width, int height, boolean transparent) {
        //throw new UnsupportedOperationException("Google Charts cannot be rendered as a PNG, call Chart.create().javascript() and embed in an HTML page");
        return this;
    }


    @Override
    public Chart writerPng(OutputStream os, int width, int height, boolean transparent) {
        //throw new UnsupportedOperationException("Google Charts cannot be rendered as a PNG, call Chart.create().javascript() and embed in an HTML page");
        return this;
    }


    @Override
    public void accept(JsCode jsCode, String functionName, String divId) {
        jsCode.write("/** This is code generation by the Morpheus Visualization library */");
        jsCode.newFunction(functionName, func -> {
            if (plot() instanceof GXyPlot) {
                final GXyModel model = (GXyModel)((GXyPlot)plot).data();
                final GXyDataset dataset = model.getUnifiedDataset();
                func.write("var data = google.visualization.arrayToDataTable(");
                dataset.accept(func);
                func.write(");");
            } else if (plot instanceof GPiePlot) {
                final GPiePlot plot = (GPiePlot)plot();
                final GPieModel model = (GPieModel)plot.data();
                func.write("var data = google.visualization.arrayToDataTable(");
                model.accept(func);
                func.write(");");
            }
            func.newLine(2);
            func.write("var options = ");
            func.newObject(options -> {
                options.setIgnoreNulls(true);
                options.newAttribute("fontSize", "automatic");
                options.newAttribute("fontName", "Arial");
                options.newAttribute("title", createTitle());
                options.newAttribute("titlePosition", "out");
                options.newObject("titleTextStyle", title);
                options.newObject("backgroundColor", background -> {
                    //background.newAttribute("stroke", "");
                    background.newAttribute("strokeWidth", 0);
                    //background.newAttribute("fill", "");
                });
                options.newObject("chartArea", area -> {
                    area.newAttribute("left", "auto");
                    area.newAttribute("top", "auto");
                    area.newAttribute("width", "80%");
                    area.newAttribute("height", "auto");
                });
                if (legend.enabled) {
                    options.newObject("legend", legend -> {
                        legend.newAttribute("position", this.legend.position);
                        legend.newAttribute("alignment", this.legend.alignment);
                        legend.newObject("textStyle", this.legend.style);
                    });
                }
                if (plot instanceof GXyPlot) {
                    ((GXyPlot)plot).accept(options);
                    options.newObject("explorer", explorer -> {
                        explorer.newAttribute("keepInBounds", true);
                        explorer.newArray("actions", true, a -> {
                            a.append("dragToZoom");
                            a.append("rightClickToReset");
                        });
                    });
                } else if (plot instanceof GPiePlot) {
                    ((GPiePlot)plot).accept(options);
                }
            });

            func.write(";");
            func.newLine();
            func.newLine().write("var target = document.getElementById('%s');", divId);
            func.newLine().write("var chart = new google.visualization.%s(target);", getChartType());
            func.newLine().write("chart.draw(data, options);");
        });
    }


    /**
     * Returns the title / subtitle combo
     * @return  the title / subtitle combo
     */
    private String createTitle() {
        final StringBuilder result = new StringBuilder();
        if (title.getText() != null) result.append(title.getText());
        if (subtitle.getText() != null) {
            final boolean brackets = result.length() > 0;
            result.append(brackets ? " - (" : "");
            result.append(subtitle.getText());
            result.append(brackets ? ")" : "");
        }
        return result.toString();
    }


    /**
     * Returns the Google chart type Javascript class
     * @return      the Google chart type
     */
    private String getChartType() {
        if (plot instanceof PiePlot) {
            return "PieChart";
        }  else if (plot instanceof GXyPlot) {
            final GXyPlot gxyPlot = (GXyPlot)plot;
            final GXyRender render = (GXyRender)gxyPlot.render(0);
            final boolean vertical = gxyPlot.isVertical();
            switch (render.getChartType()) {
                case LINES:     return "LineChart";
                case BARS:      return vertical ? "ColumnChart" : "BarChart";
                case AREA:      return "AreaChart";
                case SHAPES:    return "ScatterChart";
                case DOTS:      return "ScatterChart";
                default:    throw new IllegalStateException("Unsupported chart type: " + render.getChartType());
            }
        } else {
            throw new IllegalArgumentException("Unsupported plot type specified: " + plot.getClass());
        }
    }



    /**
     * A ChartLegend adapter for Google charts
     */
    private class LegendAdapter implements ChartLegend {

        private boolean enabled = false;
        private String position = "right";
        private String alignment = "start";
        private GChartTextStyle style = new GChartTextStyle(Color.BLACK, new Font("Arial", Font.PLAIN, 10));

        @Override
        public ChartLegend on() {
            this.enabled = true;
            return this;
        }

        @Override
        public ChartLegend off() {
            this.enabled = false;
            return this;
        }

        @Override
        public ChartLegend right() {
            this.position = "right";
            return this;
        }

        @Override
        public ChartLegend left() {
            this.position = "left";
            return this;
        }

        @Override
        public ChartLegend top() {
            this.position = "top";
            return this;
        }

        @Override
        public ChartLegend bottom() {
            this.position = "bottom";
            return this;
        }
    }




    public static void main(String[] args) {
        int rowCount = 1000;
        LocalDate startDate = LocalDate.of(2013, 1, 1);
        Range<Integer> rowKeys = Range.of(0, rowCount);
        Range<LocalDate> dates = rowKeys.map(startDate::plusDays);
        DataFrame<Integer,String> frame = DataFrame.of(rowKeys, String.class, columns -> {
            columns.add("DataDate", dates);
            Stream.of("A", "B", "C", "D").forEach(label -> {
                columns.add(label, Array.randn(rowCount).cumSum());
            });
        });

        frame.out().print();

        Chart.create().asHtml().withLinePlot(frame, "DataDate", chart -> {
            chart.legend().on().right();
            chart.title().withText("Test Chart");
            chart.title().withColor(Color.BLACK);
            chart.plot().axes().domain().label().withText("Cities");
            chart.plot().axes().range(0).label().withText("Population");
            chart.show();
        });

    }

}
