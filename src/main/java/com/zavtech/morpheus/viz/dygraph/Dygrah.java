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

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import com.zavtech.morpheus.util.Bounds;
import com.zavtech.morpheus.viz.chart.Chart;
import com.zavtech.morpheus.viz.chart.ChartAxes;
import com.zavtech.morpheus.viz.chart.ChartAxis;
import com.zavtech.morpheus.viz.chart.ChartData;
import com.zavtech.morpheus.viz.chart.ChartModel;
import com.zavtech.morpheus.viz.chart.ChartEngine;
import com.zavtech.morpheus.viz.chart.ChartFormat;
import com.zavtech.morpheus.viz.chart.ChartLabel;
import com.zavtech.morpheus.viz.chart.ChartPlotStyle;
import com.zavtech.morpheus.viz.chart.ChartLegend;
import com.zavtech.morpheus.viz.chart.ChartOrientation;
import com.zavtech.morpheus.viz.chart.ChartSeriesStyle;
import com.zavtech.morpheus.viz.chart.ChartTextStyle;
import com.zavtech.morpheus.viz.chart.ChartTrendLine;
import com.zavtech.morpheus.viz.util.ColorModel;
import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.util.text.Formats;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

/**
 *
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public class Dygrah<X extends Comparable> implements Chart<X> {

    private ChartModel dataset;
    private PlotStyle style = new PlotStyle();
    private LabelAdapter title = new LabelAdapter();
    private AxesAdapter axesAdapter = new AxesAdapter();
    private AxisAdapter domainAxis = new AxisAdapter();
    private AxisAdapter rangeAxis1 = new AxisAdapter();
    private AxisAdapter rangeAxis2 = new AxisAdapter();


    /**
     * Constructor
     */
    public Dygrah() {
        super();
    }

    @Override
    public ChartLabel title() {
        return title;
    }

    @Override
    public ChartLabel subtitle() {
        return null;
    }

    @Override
    public ChartAxes axes() {
        return axesAdapter;
    }

    @Override
    public ChartLegend legend() {
        return null;
    }

    @Override
    public ChartData<X> data() {
        return null;
    }

    @Override
    public Chart withColorModel(ColorModel colorModel) {
        return this;
    }

    @Override
    public ChartTrendLine trendLine() {
        return null;
    }

    @Override
    public ChartOrientation orientation() {
        return null;
    }

    @Override
    public ChartPlotStyle plot(int index) {
        switch (index) {
            case 0:     return style;
            default:    throw new IllegalArgumentException("Only one dataset currently supported");
        }
    }

    @Override
    public ChartSeriesStyle style(Comparable seriesKey) {
        return null;
    }

    @Override
    public Chart show() {
        return show(1024, 768);
    }

    @Override
    public Chart show(int width, int height) {
        BufferedWriter writer = null;
        try {
            final Configuration config = new Configuration(Configuration.VERSION_2_3_24);
            config.setClassForTemplateLoading(Dygrah.class, "/");
            config.setDefaultEncoding("UTF-8");
            config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            config.setLogTemplateExceptions(false);
            final String seriesLabels = getSeriesLabels();
            final String datasetValue = getDataset();
            final Map<String,Object> model = new HashMap<>();
            model.put("dataset", datasetValue);
            model.put("labels", seriesLabels);
            model.put("title", title.text);
            model.put("xAxisLabel", domainAxis.label.text);
            model.put("y1AxisLabel", rangeAxis1.label.text);
            model.put("y2AxisLabel", rangeAxis2.label.text);
            model.put("users", Arrays.asList("User1", "User2", "User3", "User4", "User5"));
            final File dir = new File(System.getProperty("user.home"), ".morpheus/dygraph");
            final File file = new File(dir, UUID.randomUUID().toString() + ".html");
            file.getParentFile().mkdirs();

            writer = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file))));
            final Template template = config.getTemplate("/dygraph/dygraph.ftlh");
            template.process(model, writer);
            Desktop.getDesktop().browse(file.toURI());
            return this;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate Dygraph chart", ex);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    @Override
    public Chart writerPng(File file, int width, int height) {
        return null;
    }

    @Override
    public Chart writerPng(OutputStream os, int width, int height) {
        return null;
    }

    /**
     * Returns a comma separated list of series names
     * @return      the comma separated series names
     */
    private String getSeriesLabels() {
        final Formats formats = new Formats();
        final int seriesCount = dataset.getSeriesCount();
        final StringBuilder text = new StringBuilder();
        text.append("\"");
        text.append("WTF?");
        text.append("\"");
        for (int i=0; i<seriesCount; ++i) {
            final Comparable seriesKey = dataset.getSeriesKey(i);
            text.append(", ");
            text.append("\"");
            text.append(formats.format(seriesKey));
            text.append("\"");
        }
        return text.toString();
    }

    /**
     * Returns the dataset encoded as a string to place in HTML
     * @return      the dataset to place in HTML
     */
    private String getDataset() {
        final StringBuilder text = new StringBuilder();
        final int itemCount = dataset.getSize();
        final int seriesCount = dataset.getSeriesCount();
        final Class<?> domainType = dataset.getDomainKeyType();
        final Function<Object,Number> rangeValueFunction = createDomainValueFunction(domainType);
        for (int i=0; i<itemCount; ++i) {
            final Comparable domainValue = dataset.getDomainKey(i);
            text.append(text.length() > 0 ? "\n\t\t" : "");
            text.append("[ new Date(");
            text.append(rangeValueFunction.apply(domainValue));
            text.append(")");
            for (int j=0; j<seriesCount; ++j) {
                text.append(", ");
                text.append(dataset.getRangeValue(i, j));
            }
            text.append(" ],");
        }
        return text.toString();
    }


    /**
     * Creates a function that yields a long time value in epoch millis given some input
     * @param dataType      the data type
     * @return              the function to resolve epoch millis
     */
    private Function<Object,Number> createDomainValueFunction(Class<?> dataType) {
        if (Number.class.isAssignableFrom(dataType)) {
            return value -> (Number)value;
        } else if (Date.class.isAssignableFrom(dataType)) {
            return value -> value == null ? Double.NaN : ((Date)value).getTime();
        } else if (LocalDate.class.isAssignableFrom(dataType)) {
            return value -> value == null ? Double.NaN : ((LocalDate)value).toEpochDay() * 86400 * 1000;
        } else if (LocalDateTime.class.isAssignableFrom(dataType)) {
            return value -> value == null ? Double.NaN : ((LocalDateTime)value).toInstant(ZoneOffset.UTC).toEpochMilli();
        } else if (ZonedDateTime.class.isAssignableFrom(dataType)) {
            return value -> value == null ? Double.NaN : ((ZonedDateTime)value).toInstant().toEpochMilli();
        } else if (Calendar.class.isAssignableFrom(dataType)) {
            return value -> value == null ? Double.NaN : ((Calendar)value).getTimeInMillis();
        } else {
            throw new IllegalArgumentException("Cannot create time resolver for type: " + dataType);
        }
    }


    /**
     * A Dygraph adapter for the ChartAxes adapter
     */
    private class AxesAdapter implements ChartAxes {

        @Override
        public ChartAxis domain() {
            return domainAxis;
        }

        @Override
        public ChartAxis range(int index) {
            switch (index) {
                case 0: return rangeAxis1;
                case 1: return rangeAxis2;
                default:    throw new IllegalArgumentException("Unsupported range index: " + index);
            }
        }
    }


    private class AxisAdapter implements ChartAxis {

        private LabelAdapter label = new LabelAdapter();
        private FormatAdapter format = new FormatAdapter();
        private StyleAdapter style = new StyleAdapter();

        @Override
        public ChartLabel label() {
            return label;
        }

        @Override
        public ChartFormat format() {
            return format;
        }

        @Override
        public ChartTextStyle ticks() {
            return style;
        }

        @Override
        public ChartAxis asLogScale() {
            return this;
        }

        @Override
        public ChartAxis asLinearScale() {
            return this;
        }

        @Override
        public ChartAxis asDateScale() {
            return this;
        }

        @Override
        public ChartAxis withRange(Bounds<?> range) {
            return this;
        }
    }


    private class LabelAdapter implements ChartLabel {

        private String text;

        @Override
        public ChartLabel withText(String text) {
            return this;
        }

        @Override
        public ChartLabel withColor(Color color) {
            return this;
        }

        @Override
        public ChartLabel withFont(Font font) {
            return this;
        }
    }


    private class StyleAdapter implements ChartTextStyle<ChartTextStyle> {

        @Override
        public ChartTextStyle withColor(Color color) {
            return this;
        }

        @Override
        public ChartTextStyle withFont(Font font) {
            return this;
        }
    }


    private class FormatAdapter implements ChartFormat {

        private String pattern;

        @Override
        public void withPattern(String pattern) {
            this.pattern = pattern;
        }
    }



    private class PlotStyle implements ChartPlotStyle {

        @Override
        public ChartPlotStyle withArea(boolean shapes) {
            return null;
        }

        @Override
        public ChartPlotStyle withBars(double margin) {
            return null;
        }

        @Override
        public ChartPlotStyle withLines() {
            return null;
        }

        @Override
        public ChartPlotStyle withPoints() {
            return null;
        }

        @Override
        public ChartPlotStyle withLinesAndPoints() {
            return null;
        }

        @Override
        public ChartPlotStyle withSpline() {
            return null;
        }

        @Override
        public ChartPlotStyle withStackedBars(double marked) {
            return null;
        }
    }


    public static void main(String[] args) throws Exception {
        final DataFrame<LocalDate,String> frame = DataFrame.read().csv(options -> {
            options.setResource("/Users/witdxav/returns.csv");
            options.setRowKeyParser(LocalDate.class, values -> LocalDate.parse(values[0]));
        });
        ChartEngine.setDefaultEngine(new DygraphEngine());
        Chart.of(frame, chart -> {
            chart.title().withText("Stock Returns");
            chart.axes().domain().label().withText("Date");
            chart.axes().range(0).label().withText("Cumulative Return (%)");
            chart.data().add(frame);
            chart.plot(0).withLines();
            chart.show();
        });
    }
}
