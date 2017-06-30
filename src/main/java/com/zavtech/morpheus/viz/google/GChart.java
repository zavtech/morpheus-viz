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
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.zavtech.morpheus.viz.chart.Chart;
import com.zavtech.morpheus.viz.chart.ChartAxes;
import com.zavtech.morpheus.viz.chart.ChartAxis;
import com.zavtech.morpheus.viz.chart.ChartBase;
import com.zavtech.morpheus.viz.chart.ChartData;
import com.zavtech.morpheus.viz.chart.ChartPlotStyle;
import com.zavtech.morpheus.viz.chart.ChartModel;
import com.zavtech.morpheus.viz.chart.ChartEngine;
import com.zavtech.morpheus.viz.chart.ChartException;
import com.zavtech.morpheus.viz.chart.ChartFormat;
import com.zavtech.morpheus.viz.chart.ChartLabel;
import com.zavtech.morpheus.viz.chart.ChartLegend;
import com.zavtech.morpheus.viz.chart.ChartOrientation;
import com.zavtech.morpheus.viz.chart.ChartSeriesStyle;
import com.zavtech.morpheus.viz.chart.ChartTextStyle;
import com.zavtech.morpheus.viz.chart.ChartTrendLine;
import com.zavtech.morpheus.viz.util.ColorModel;
import com.zavtech.morpheus.viz.util.XWilkinson;
import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.index.Index;
import com.zavtech.morpheus.util.Bounds;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

/**
 * A Chart implementation that uses the Google Charting library to render charts in a browser
 *
 * @param <X>   the type for the domain
 *
 * @author  Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public class GChart<X extends Comparable> extends ChartBase<X> {

    private static final Map<Class,Type> typeMap = new HashMap<>();

    private DataAdapter dataAdapter = new DataAdapter();
    private LegendAdapter legend = new LegendAdapter();
    private AxesAdapter axesAdapter = new AxesAdapter();
    private AxisAdapter domainAxis = new AxisAdapter(true);
    private Map<Integer,AxisAdapter> rangeAxesMap = new HashMap<>();
    private PlotStyleAdapter plotStyleAdapter = new PlotStyleAdapter();
    private TrendLineAdapter trendLineAdapter = new TrendLineAdapter();
    private OrientationAdapter orientation = new OrientationAdapter();
    private LabelAdapter titleAdapter = new LabelAdapter(Color.BLACK, new Font("Arial", Font.PLAIN, 16));


    /**
     * An enum of the data types supported by Google charts
     */
    private enum Type {

        BOOLEAN("boolean"),
        STRING("string"),
        NUMBER("number"),
        DATE("date"),
        DATETIME("datetime"),
        LOCAL_TIME("timeofday");

        private String label;

        /**
         * Constructor
         * @param label the label for this type
         */
        Type(String label) {
            this.label = label;
        }
    }

    /**
     * Static initializer
     */
    static {
        typeMap.put(String.class, Type.STRING);
        typeMap.put(Boolean.class, Type.BOOLEAN);
        typeMap.put(String.class, Type.STRING);
        typeMap.put(Number.class, Type.NUMBER);
        typeMap.put(Double.class, Type.NUMBER);
        typeMap.put(Integer.class, Type.NUMBER);
        typeMap.put(Float.class, Type.NUMBER);
        typeMap.put(Long.class, Type.NUMBER);
        typeMap.put(Date.class, Type.DATETIME);
        typeMap.put(LocalDate.class, Type.DATE);
        typeMap.put(LocalDateTime.class, Type.DATETIME);
        typeMap.put(ZonedDateTime.class, Type.DATETIME);
        typeMap.put(LocalTime.class, Type.LOCAL_TIME);
    }

    /**
     * Constructor
     */
    GChart() {
        this.rangeAxesMap.put(0, new AxisAdapter(false));
    }

    @Override
    public ChartLabel title() {
        return titleAdapter;
    }

    @Override
    public ChartLabel subtitle() {
        return null;
    }

    @Override
    public ChartLegend legend() {
        return legend;
    }

    @Override
    public ChartAxes axes() {
        return axesAdapter;
    }

    @Override
    public ChartData<X> data() {
        return dataAdapter;
    }

    @Override
    public ChartTrendLine trendLine() {
        return trendLineAdapter;
    }

    @Override
    public ChartOrientation orientation() {
        return orientation;
    }

    @Override
    public ChartPlotStyle plot(int index) {
        return plotStyleAdapter;
    }

    @Override
    public Chart withColorModel(ColorModel colorModel) {
        return this;
    }

    @Override
    public Chart show() {
        return show(1024, 768);
    }

    /**
     * Returns a Javascript array definition for the dataset
     * @param dataset   the dataset to encode as Javascript array
     * @return          the Javascript array
     */
    private String toJavascript(ChartModel<X,? extends Comparable> dataset) {
        final Class<?> domainClass = dataset.getDomainKeyType();
        final Type domainType = typeMap.getOrDefault(domainClass, Type.STRING);
        final StringBuilder text = new StringBuilder();
        text.append("[\n\t\t  { id: \"domain\", label: \"Domain\", type: \"");
        text.append(domainType.label);
        text.append("\"},\n");
        for (int i=0; i<dataset.getSeriesCount(); ++i) {
            final Comparable seriesKey = dataset.getSeriesKey(i);
            text.append("\t\t  { id: \"").append(seriesKey);
            text.append("\", label: \"").append(seriesKey);
            text.append("\", type: \"number\"}");
            text.append(i < dataset.getSeriesCount()-1 ? ",\n" : "\n\t\t]");

        }
        final Function<Object,String> domainValueFunc = createDomainValueFunction(domainClass);
        for (int i=0; i<dataset.getSize(); ++i) {
            final Comparable domainValue = dataset.getDomainKey(i);
            final String stringValue = domainValueFunc.apply(domainValue);
            text.append(",\n\t\t[");
            text.append(stringValue);
            text.append("");
            for (int j=0; j<dataset.getSeriesCount(); ++j) {
                final Number value = dataset.getRangeValue(i, j);
                text.append(", ");
                text.append(value.doubleValue());
            }
            text.append("]");
        }
        return text.toString();
    }


    /**
     * Creates a function that yields a long time value in epoch millis given some input
     * @param dataType      the data type
     * @return              the function to resolve epoch millis
     */
    private Function<Object,String> createDomainValueFunction(Class<?> dataType) {
        if (Number.class.isAssignableFrom(dataType)) {
            return value -> value == null ? "null" : String.valueOf(value);
        } else if (Date.class.isAssignableFrom(dataType)) {
            return value -> value == null ? "null" : "new Date(" + ((Date)value).getTime() + ")";
        } else if (LocalDate.class.isAssignableFrom(dataType)) {
            return value -> value == null ? "null" : "new Date(" + ((LocalDate)value).toEpochDay() * 86400 * 1000 + ")";
        } else if (LocalDateTime.class.isAssignableFrom(dataType)) {
            return value -> value == null ? "null" : "new Date(" + ((LocalDateTime)value).toInstant(ZoneOffset.UTC).toEpochMilli() + ")";
        } else if (ZonedDateTime.class.isAssignableFrom(dataType)) {
            return value -> value == null ? "null" : "new Date(" + ((ZonedDateTime)value).toInstant().toEpochMilli() + ")";
        } else if (Calendar.class.isAssignableFrom(dataType)) {
            return value -> value == null ? "null" : "new Date(" + ((Calendar)value).getTimeInMillis() + ")";
        } else {
            return value -> value == null ? "null" : "'" + value.toString() + "'";
        }
    }


    @SuppressWarnings("unchecked")
    public Chart show(int width, int height) {
        BufferedWriter writer = null;
        try {
            final Map<String,Object> attributeMap = new HashMap<>();

            final ChartModel<X,? extends Comparable> model = dataAdapter.getUnifiedModel();
            final List<GChartTrendLine> trendLines = getTrendLineList(model).collect(Collectors.toList());
            final String datasetAsJavascript = toJavascript(model);
            final String rangeTicks = getRangeTicks(model, 10);

            attributeMap.put("dataset", datasetAsJavascript);
            attributeMap.put("chartType", plotStyleAdapter.chartType);
            attributeMap.put("trendLines", trendLines);
            attributeMap.put("backgroundColorStroke", "#666");
            attributeMap.put("backgroundColorFill", "white");
            attributeMap.put("backgroundColorStrokeWidth", "0");
            attributeMap.put("curveType", plotStyleAdapter.curved ? "function" : "None");
            attributeMap.put("isStacked", plotStyleAdapter.stacked);

            attributeMap.put("title", titleAdapter.text);
            attributeMap.put("titleTextColor", titleAdapter.getColorHex());
            attributeMap.put("titleFontName", titleAdapter.getFont().getName());
            attributeMap.put("titleFontSize", titleAdapter.getFont().getSize());
            attributeMap.put("titleFontBold", titleAdapter.isBold());
            attributeMap.put("titleFontItalic", titleAdapter.isItalic());

            attributeMap.put("domainAxis", domainAxis.toMap());
            attributeMap.put("orientation", orientation.vertical ? "vertical" : "horizontal");
            attributeMap.put("rangeAxes", new ArrayList());
            attributeMap.put("rangeAxisCount", rangeAxesMap.size());

            for (int key : rangeAxesMap.keySet()) {
                final AxisAdapter axisAdapter = rangeAxesMap.get(key);
                final Map<String,Object> axisMap = axisAdapter.toMap();
                ((List<Object>)attributeMap.get("rangeAxes")).add(axisMap);
            }

            final File dir = new File(System.getProperty("user.home"), ".morpheus/charts");
            final File file = new File(dir, UUID.randomUUID().toString() + ".html");
            if (file.getParentFile().mkdirs()) System.out.println("Created directory: " + dir.getAbsolutePath());
            writer = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file))));

            final Configuration config = new Configuration(Configuration.VERSION_2_3_24);
            config.setClassForTemplateLoading(GChart.class, "/");
            config.setDefaultEncoding("UTF-8");
            config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            config.setLogTemplateExceptions(false);
            final Template template = config.getTemplate("/google/gchart.ftlh");
            template.process(attributeMap, writer);
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
     * Returns the list of trend line descriptors based on the model provided
     * @param model     the unified model for this chart
     * @return          the list of trend line details
     */
    @SuppressWarnings("unchecked")
    private Stream<GChartTrendLine> getTrendLineList(ChartModel<X,? extends Comparable> model) {
        final List<Comparable> seriesList = trendLineAdapter.getSeriesList();
        final Index<Comparable> index = (Index<Comparable>)Index.of(model.getSeriesKeys());
        return seriesList.stream().map(seriesKey -> {
            final int seriesIndex = index.getIndexForKey(seriesKey);
            final Comparable trendKey = trendLineAdapter.getTrendKey(seriesKey);
            final Color color = getSeriesColor(trendKey);
            final float lineWidth = getSeriesLineWidth(trendKey);
            final int lineWidthInt = Float.isNaN(lineWidth) ? 2 : Math.max(2, (int)lineWidth);
            return new GChartTrendLine(seriesIndex, color != null ? color : Color.BLACK, lineWidthInt, 1, true, false);
        });
    }

    @SuppressWarnings("unchecked")
    private String getRangeTicks(ChartModel<X,? extends Comparable> dataset, int count) {
        final Optional<Bounds<Number>> bounds = dataset.getSeriesBounds();
        if (!bounds.isPresent()) {
            return null;
        } else {
            final double min = bounds.get().lower().doubleValue();
            final double max = bounds.get().upper().doubleValue();
            final double minAdj = min + 0.1 * (min - max);
            final double maxAdj = max + 0.1 * (max - min);
            final XWilkinson xWilkinson = XWilkinson.base10();
            final XWilkinson.Label label = xWilkinson.search(minAdj, maxAdj, count);
            final StringBuilder text = new StringBuilder();
            final List<Double> ticks = label.getList();
            text.append("[");
            for (int i=0; i<ticks.size(); ++i) {
                text.append(ticks.get(i));
                text.append(i < ticks.size() - 1 ? ", " : "");
            }
            text.append("]");
            return text.toString();
        }
    }


    /**
     * An adapter for the ChartAxes interface
     */
    private class AxesAdapter implements ChartAxes {

        @Override
        public ChartAxis domain() {
            return domainAxis;
        }

        @Override
        public ChartAxis range(int index) {
            AxisAdapter rangeAxis = rangeAxesMap.get(index);
            if (rangeAxis == null) {
                rangeAxis = new AxisAdapter(false);
                rangeAxesMap.put(index, rangeAxis);
            }
            return rangeAxis;
        }
    }


    /**
     * A ChartAxis adapter for Google charts
     */
    private class AxisAdapter implements ChartAxis {

        private boolean log = false;
        private LabelAdapter label;
        private FormatAdapter format;
        private TextStyleAdapter ticks;

        /**
         * Constructor
         * @param domain    true if this represents a domain axis
         */
        private AxisAdapter(boolean domain) {
            this.format = new FormatAdapter(domain);
            this.label = new LabelAdapter(Color.BLACK, new Font("Arial", Font.PLAIN, 12));
            this.ticks = new TextStyleAdapter(Color.BLACK, new Font("Arial", Font.PLAIN, 12));
        }

        /**
         * Returns a property map that describes this axis
         * @return  the map of properties for this axis
         */
        private Map<String,Object> toMap() {
            final Map<String,Object> map = new HashMap<>();
            map.put("axisScaleType", this.log ? "log" : null);
            map.put("axisFormat", this.format.getPattern());
            map.put("axisTitleText" , this.label.text);
            map.put("axisTitleTextColor", this.label.getColorHex());
            map.put("axisTitleFontName", this.label.getFont().getName());
            map.put("axisTitleFontSize", this.label.getFont().getSize());
            map.put("axisTitleFontBold", this.label.isBold());
            map.put("axisTitleFontItalic", this.label.isItalic());
            map.put("axisTickTextColor", this.ticks.getColorHex());
            map.put("axisTickFontName", this.ticks.getFont().getName());
            map.put("axisTickFontSize", this.ticks.getFont().getSize());
            map.put("axisTickFontBold", this.ticks.isBold());
            map.put("axisTickFontItalic", this.ticks.isItalic());
            return map;
        }

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
            return ticks;
        }

        @Override
        public ChartAxis asLogScale() {
            this.log = true;
            return this;
        }

        @Override
        public ChartAxis asLinearScale() {
            this.log = false;
            return this;
        }

        @Override
        public ChartAxis asDateScale() {
            this.log = false;
            return this;
        }

        @Override
        public ChartAxis withRange(Bounds<?> range) {
            return this;
        }
    }


    /**
     * An adapter implementation for the ChartOrientation interface
     */
    private class OrientationAdapter implements ChartOrientation {

        private boolean vertical;

        @Override
        public void vertical() {
            this.vertical = true;
        }

        @Override
        public void horizontal() {
            this.vertical = false;
        }
    }

    /**
     * A ChartLegend adapter for Google charts
     */
    private class LegendAdapter implements ChartLegend {

        @Override
        public ChartLegend on() {
            return this;
        }

        @Override
        public ChartLegend off() {
            return this;
        }

        @Override
        public ChartLegend right() {
            return this;
        }

        @Override
        public ChartLegend left() {
            return this;
        }

        @Override
        public ChartLegend top() {
            return this;
        }

        @Override
        public ChartLegend bottom() {
            return this;
        }
    }

    /**
     * A ChartLabel adapter for Google charts
     */
    private class LabelAdapter extends TextStyleAdapter<ChartLabel> implements ChartLabel {

        private String text;

        /**
         * Constructor
         * @param color     the color
         * @param font      the font
         */
        private LabelAdapter(Color color, Font font) {
            super(color, font);
        }

        @Override
        public ChartLabel withText(String text) {
            this.text = text;
            return this;
        }
    }


    /**
     * A ChartTextStyle adapter for Google charts
     */
    private class TextStyleAdapter<T extends ChartTextStyle> implements ChartTextStyle<T> {

        private Color color;
        private Font font;

        /**
         * Constructor
         * @param color     the color
         * @param font      the font
         */
        private TextStyleAdapter(Color color, Font font) {
            this.color = color;
            this.font = font;
        }

        /**
         * Returns the color as a HEX string
         * @return  the color string
         */
        String getColorHex() {
            final int r = color.getRed();
            final int g = color.getGreen();
            final int b = color.getBlue();
            return String.format("#%02x%02x%02x", r, g, b);
        }

        @Override
        @SuppressWarnings("unchecked")
        public T withColor(Color color) {
            this.color = color;
            return (T)this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T withFont(Font font) {
            this.font = font;
            return (T)this;
        }

        /**
         * Returns the font for this adapter
         * @return  teh font
         */
        public Font getFont() {
            return font;
        }

        /**
         * Returns true if font is bold
         * @return  true if bold
         */
        public boolean isBold() {
            return font.getStyle() == Font.BOLD || font.getStyle() == (Font.BOLD | Font.ITALIC);
        }

        /**
         * Returns true if font is italic
         * @return  true if italic
         */
        boolean isItalic() {
            return font.getStyle() == Font.ITALIC || font.getStyle() == (Font.BOLD | Font.ITALIC);
        }

    }


    /**
     * A ChartFormat adapter for Google charts
     */
    private class FormatAdapter implements ChartFormat {

        private boolean domain;
        private String pattern;

        /**
         * Constructor
         * @param domain    true if this represents the domain axis
         */
        private FormatAdapter(boolean domain) {
            this.domain = domain;
        }

        @Override
        public void withPattern(String pattern) {
            this.pattern = pattern;
        }

        /**
         * Returns the pattern to render axis tick labels
         * @return  the pattern for axis tick value
         */
        String getPattern() {
            if (pattern != null) {
                return pattern;
            } else {
                final ChartModel model = dataAdapter.getFirstModel();
                final Class<?> typeClass = domain ? model.getDomainKeyType() : Number.class;
                final Type type = typeMap.getOrDefault(typeClass, Type.STRING);
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


    /**
     * A ChartData adapter implementation used to manage datasets for a Google chart
     */
    private class DataAdapter implements ChartData<X> {

        private Map<Integer,ChartModel<X,? extends Comparable>> modelMap = new LinkedHashMap<>();

        /**
         * Returns a reference to the first model for this adapter
         * @return      the first model for adapter
         */
        private ChartModel<X,? extends Comparable> getFirstModel() {
            return modelMap.values().iterator().next();
        }

        /**
         * Returns a unified chart model based on all underlying models in this adapter
         * @return      the unified chart data model
         */
        @SuppressWarnings("unchecked")
        private <S extends Comparable> ChartModel<X,S> getUnifiedModel() {
            if (modelMap.size() == 0) {
                return ChartModel.of(DataFrame::empty);
            } else if (modelMap.size() == 1) {
                return (ChartModel<X,S>)modelMap.values().iterator().next();
            } else {
                final Stream<ChartModel<X,S>> models = modelMap.values().stream().map(m -> (ChartModel<X,S>)m);
                return ChartModel.combine(models.collect(Collectors.toList()));
            }
        }

        @Override
        public ChartData<X> setRangeAxis(int dataset, int axis) {
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <S extends Comparable> ChartModel<X,S> at(int index) {
            final ChartModel<X,? extends Comparable> model = modelMap.get(index);
            if (model != null) return (ChartModel<X,S>)model;
            else throw new IllegalArgumentException("No chart data located at index: " + index);
        }

        @Override
        public <S extends Comparable> ChartModel<X,S> add(DataFrame<X,S> frame) {
            final int index = modelMap.size();
            final ChartModel<X,S> model = ChartModel.of(() -> frame);
            this.modelMap.put(index, model);
            return model;
        }

        @Override
        public <S extends Comparable> ChartModel<X,S> add(DataFrame<?,S> frame, S domainKey) {
            final int index = modelMap.size();
            final ChartModel<X,S> model = ChartModel.of(domainKey, () -> frame);
            this.modelMap.put(index, model);
            return model;
        }

        @Override
        public <S extends Comparable> ChartModel<X,S> update(int index, DataFrame<X,S> frame) {
            return null;
        }

        @Override
        public <S extends Comparable> ChartModel<X,S> update(int index, DataFrame<?,S> frame, S domainKey) {
            return null;
        }

        @Override
        public void removeAll() {
            this.modelMap.clear();
        }

        @Override
        public void remove(int index) {
            final ChartModel<X,? extends Comparable> model = modelMap.remove(index);
            if (model == null) {
                throw new ChartException("No chart data model exists for id: " + index);
            }
        }
    }


    /**
     * A ChartDataStyle for Google charts
     */
    private class PlotStyleAdapter implements ChartPlotStyle {

        private boolean bars = true;
        private boolean stacked = false;
        private boolean curved = false;
        private String chartType = "lines";


        @Override
        public ChartPlotStyle withArea(boolean shapes) {
            this.bars = false;
            this.curved = false;
            this.stacked = false;
            this.chartType = "area";
            return this;
        }

        @Override
        public ChartPlotStyle withBars(double margin) {
            this.bars = true;
            this.curved = false;
            this.stacked = false;
            this.chartType = "bars";
            return this;
        }

        @Override
        public ChartPlotStyle withLines() {
            this.bars = false;
            this.curved = false;
            this.stacked = false;
            this.chartType = "lines";
            return this;
        }

        @Override
        public ChartPlotStyle withPoints() {
            this.bars = false;
            this.curved = false;
            this.stacked = false;
            this.chartType = "scatter";
            return this;
        }

        @Override
        public ChartPlotStyle withLinesAndPoints() {
            return null;
        }

        @Override
        public ChartPlotStyle withSpline() {
            this.bars = false;
            this.curved = true;
            this.stacked = false;
            this.chartType = "lines";
            return this;
        }

        @Override
        public ChartPlotStyle withStackedBars(double marked) {
            this.bars = true;
            this.curved = false;
            this.stacked = true;
            this.chartType = "bars";
            return this;
        }
    }


    /**
     * A ChartTrendLines adapter for google charts
     */
    private class TrendLineAdapter implements ChartTrendLine {

        private Map<Comparable,Comparable> seriesKeyMap = new LinkedHashMap<>();

        /**
         * Returns the set of series for which trend lines should be added
         * @return      the set of series keys
         */
        private List<Comparable> getSeriesList() {
            return new ArrayList<>(seriesKeyMap.keySet());
        }

        /**
         * Returns the trend key for the series key specified
         * @param seriesKey the series key
         * @return          the corresponding trend key
         */
        private Comparable getTrendKey(Comparable seriesKey) {
            return seriesKeyMap.get(seriesKey);
        }

        @Override
        public <S extends Comparable,T extends Comparable> ChartSeriesStyle add(S seriesKey, T trendKey) {
            this.seriesKeyMap.put(seriesKey, trendKey);
            return style(trendKey);
        }

        @Override
        public <S extends Comparable> void remove(S trendKey) {

        }
    }


    public static void main(String[] args) {
        ChartEngine.setDefaultEngine(new GChartEngine());
        final Array<String> metrics = Array.of("Population", "Under 16s");
        final Array<String> cities = Array.of("London", "Manchester", "Birmingham", "Southampton", "Bristol");
        final DataFrame<String,String> frame = DataFrame.ofDoubles(cities, metrics).applyDoubles(v -> Math.random());
        frame.out().print();
        Chart.of(frame, chart -> {
            chart.legend().on();
            chart.title().withText("Test Chart");
            chart.title().withColor(Color.GRAY);
            chart.axes().domain().label().withText("Cities");
            chart.axes().range(0).label().withText("Population");
            chart.plot(0).withBars(0d);
            chart.show();
        });
    }

}
