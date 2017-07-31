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
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.commons.math3.special.Erf;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrameLeastSquares;
import com.zavtech.morpheus.util.Bounds;
import com.zavtech.morpheus.viz.chart.pie.PiePlot;
import com.zavtech.morpheus.viz.chart.xy.XyPlot;
import com.zavtech.morpheus.frame.DataFrame;

/**
 * The factory interface for creating various types of Charts using the Morpheus Charting API
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public interface ChartFactory {

    /**
     * Returns true if this factory supports the chart type specified
     * @param chart     the chart instance
     * @return          true if chart is supported by this factory
     */
    boolean isSupported(Chart<?> chart);

    /**
     * Displays the collection of charts in a grid with the number of columns specified
     * @param columns   the number of columns for chart grid
     * @param charts    the sequence of charts to plot
     */
    void show(int columns, Iterable<Chart<?>> charts);

    /**
     * Displays the collection of charts in a grid with the number of columns specified
     * @param columns   the number of columns for chart grid
     * @param charts    the sequence of charts to plot
     */
    void show(int columns, Stream<Chart<?>> charts);

    /**
     * Returns Javascript to embed in an HTML page which will plot the charts specified.
     * By convention, the user will need to create an html page with <code>div</code> elements which
     * have <code><id</code> attributes labelled "chart_N" where N is 0, 1, 2, N. The first chart
     * in the arguments will be plotted in div with id=chart_0, the second will be plotted in the
     * div with id=chart_1 and so on.
     * @param charts    the sequence of charts to generate Javascript from
     * @return          the resulting Javascript to embed in an HTML page.
     */
    String javascript(Chart<?>... charts);


    /**
     * Returns Javascript to embed in an HTML page which will plot the charts specified.
     * By convention, the user will need to create an html page with <code>div</code> elements which
     * have <code><id</code> attributes labelled "chart_N" where N is 0, 1, 2, N. The first chart
     * in the arguments will be plotted in div with id=chart_0, the second will be plotted in the
     * div with id=chart_1 and so on.
     * @param charts    the sequence of charts to generate Javascript from
     * @return          the resulting Javascript to embed in an HTML page.
     */
    String javascript(Iterable<Chart<?>> charts);


    /**
     * Returns a newly created XY chart and applies it to the configurator provided
     * @param configurator  the chart configurator
     * @param domainType    the data type for the domain axis
     * @param <X>           the domain key type
     * @return              the newly created chart
     */
    <X extends Comparable> Chart<XyPlot<X>> ofXY(Class<X> domainType, Consumer<Chart<XyPlot<X>>> configurator);


    /**
     * Returns a newly created Pie chart and applies it to the configurator provided
     * @param is3d              true for a 3d plot
     * @param configurator      the chart configurator
     * @param <X>               the item key type
     * @return                  the newly created chart
     */
    <X extends Comparable,S extends Comparable> Chart<PiePlot<X,S>> ofPiePlot(boolean is3d, Consumer<Chart<PiePlot<X,S>>> configurator);


    /**
     * Returns a newly created Line Chart using the row keys for the domain axis
     * @param frame         the DataFrame for the chart
     * @param configurator  the configurator to accept config to the chart
     * @return              the newly created chart
     */
    default <X extends Comparable,S extends Comparable> Chart<XyPlot<X>> withLinePlot(DataFrame<X,S> frame, Consumer<Chart<XyPlot<X>>> configurator)  {
        return ofXY(frame.rows().keyType(), chart -> {
            chart.plot().<S>data().add(frame);
            chart.plot().render(0).withLines(false, false);
            if (configurator != null) {
                configurator.accept(chart);
            }
        });
    }


    /**
     * Returns a newly created Line Chart using a column for the domain axis
     * @param frame         the DataFrame for the chart
     * @param domainKey     the column key in the frame that defines the domain
     * @param configurator  the configurator to accept config to the chart
     * @return              the newly created chart
     */
    @SuppressWarnings("unchecked")
    default <X extends Comparable,S extends Comparable> Chart<XyPlot<X>> withLinePlot(DataFrame<?,S> frame, S domainKey, Consumer<Chart<XyPlot<X>>> configurator) {
        return ofXY((Class<X>)frame.cols().type(domainKey), chart -> {
            chart.plot().<S>data().add(frame, domainKey);
            chart.plot().render(0).withLines(false, false);
            if (configurator != null) {
                configurator.accept(chart);
            }
        });
    }


    /**
     * Returns a newly created area chart using the row keys for the domain axis
     * @param frame         the DataFrame for the chart
     * @param stacked       true to generate a stacked area plot, false for overlapping
     * @param configurator  the configurator to accept config to the chart
     * @return              the newly created chart
     */
    default <X extends Comparable,S extends Comparable> Chart<XyPlot<X>> withAreaPlot(DataFrame<X, S> frame, boolean stacked, Consumer<Chart<XyPlot<X>>> configurator) {
        return ofXY(frame.rows().keyType(), chart -> {
            chart.plot().<S>data().add(frame);
            chart.plot().render(0).withArea(stacked);
            if (configurator != null) {
                configurator.accept(chart);
            }
        });
    }


    /**
     * Returns a newly created area chart using a column for the domain axis
     * @param frame         the DataFrame for the chart
     * @param stacked       true to generate a stacked area plot, false for overlapping
     * @param domainKey     the column key in the frame that defines the domain
     * @param configurator  the configurator to accept config to the chart
     * @return              the newly created chart
     */
    @SuppressWarnings("unchecked")
    default <X extends Comparable,S extends Comparable> Chart<XyPlot<X>> withAreaPlot(DataFrame<?, S> frame, boolean stacked, S domainKey, Consumer<Chart<XyPlot<X>>> configurator) {
        return ofXY((Class<X>)frame.cols().type(domainKey), chart -> {
            chart.plot().<S>data().add(frame, domainKey);
            chart.plot().render(0).withArea(stacked);
            if (configurator != null) {
                configurator.accept(chart);
            }
        });
    }


    /**
     * Returns a newly created scatter chart with shapes using the row keys for the domain axis
     * @param frame         the DataFrame for the chart
     * @param shapes        true for series specific shapes
     * @param configurator  the configurator to accept config to the chart
     * @return              the newly created chart
     */
    default <X extends Comparable,S extends Comparable> Chart<XyPlot<X>> withScatterPlot(DataFrame<X, S> frame, boolean shapes, Consumer<Chart<XyPlot<X>>> configurator) {
        return ofXY(frame.rows().keyType(), chart -> {
            chart.plot().<S>data().add(frame);
            if (shapes) {
                chart.plot().render(0).withShapes();
            } else {
                chart.plot().render(0).withDots();
            }
            if (configurator != null) {
                configurator.accept(chart);
            }
        });
    }


    /**
     * Returns a newly created scatter chart with shapes using a column for the domain axis
     * @param frame         the DataFrame for the chart
     * @param shapes        true for series specific shapes
     * @param domainKey     the column key in the frame that defines the domain
     * @param configurator  the configurator to accept config to the chart
     * @return              the newly created chart
     */
    @SuppressWarnings("unchecked")
    default <X extends Comparable,S extends Comparable> Chart<XyPlot<X>> withScatterPlot(DataFrame<?, S> frame, boolean shapes, S domainKey, Consumer<Chart<XyPlot<X>>> configurator) {
        return ofXY((Class<X>)frame.cols().type(domainKey), chart -> {
            chart.plot().<S>data().add(frame, domainKey);
            if (shapes) {
                chart.plot().render(0).withShapes();
            } else {
                chart.plot().render(0).withDots();
            }
            if (configurator != null) {
                configurator.accept(chart);
            }
        });
    }


    /**
     * Returns a newly created Bar Chart using the row keys to build the domain axis
     * @param frame         the DataFrame for the chart
     * @param stacked       true to generate a stacked bar plot, false for non-statcked
     * @param configurator  the configurator to accept config to the chart
     * @return              the newly created chart
     */
    default <X extends Comparable,S extends Comparable> Chart<XyPlot<X>> withBarPlot(DataFrame<X, S> frame, boolean stacked, Consumer<Chart<XyPlot<X>>> configurator) {
        return ofXY(frame.rows().keyType(), chart -> {
            chart.plot().<S>data().add(frame);
            chart.plot().render(0).withBars(stacked, 0d);
            if (configurator != null) {
                configurator.accept(chart);
            }
        });
    }


    /**
     * Returns a newly created Bar Chart using a column to build the domain axis
     * @param frame         the DataFrame for the chart
     * @param stacked       true to generate a stacked bar plot, false for non-statcked
     * @param domainKey     the column key in the frame that defines the domain
     * @param configurator  the configurator to accept config to the chart
     * @return              the newly created chart
     */
    @SuppressWarnings("unchecked")
    default <X extends Comparable,S extends Comparable> Chart<XyPlot<X>> withBarPlot(DataFrame<?, S> frame, boolean stacked, S domainKey, Consumer<Chart<XyPlot<X>>> configurator) {
        return ofXY((Class<X>)frame.cols().type(domainKey), chart -> {
            chart.plot().<S>data().add(frame, domainKey);
            chart.plot().render(0).withBars(stacked, 0d);
            if (configurator != null) {
                configurator.accept(chart);
            }
        });
    }


    /**
     * Returns a newly created Pie Chart using the row keys for labels and the first numeric column for values
     * @param frame         the DataFrame for the chart
     * @param is3d          true for 3D PiePlot
     * @param configurator  the configurator to accept config to the chart
     * @param <X>           the frame row axis type
     * @param <S>           the frame column axis type
     * @return              the newly created chart
     */
    default <X extends Comparable,S extends Comparable> Chart<PiePlot<X,S>> withPiePlot(DataFrame<X,S> frame, boolean is3d, Consumer<Chart<PiePlot<X,S>>> configurator) {
        if (frame == null) {
            throw new IllegalArgumentException("The DataFrame cannot be null");
        } else {
            return ofPiePlot(is3d, chart -> {
                chart.plot().data().apply(frame);
                if (configurator != null) {
                    configurator.accept(chart);
                }
            });
        }
    }


    /**
     * Returns a newly created Pie Chart using the row keys for labels and the values from the column labelled dataKey
     * @param frame         the DataFrame containing data
     * @param is3d          true for 3D PiePlot
     * @param dataKey       the column key to use for data values
     * @param configurator  the configurator to accept config to the chart
     * @param <X>           the frame row axis type
     * @param <S>           the frame column axis type
     * @return              the newly created chart
     */
    default  <X extends Comparable,S extends Comparable> Chart<PiePlot<X,S>> withPiePlot(DataFrame<X,S> frame, boolean is3d,  S dataKey, Consumer<Chart<PiePlot<X,S>>> configurator) {
        if (frame == null) {
            throw new IllegalArgumentException("The DataFrame cannot be null");
        } else {
            return ofPiePlot(is3d, chart -> {
                chart.plot().data().apply(frame, dataKey);
                if (configurator != null) {
                    configurator.accept(chart);
                }
            });
        }
    }


    /**
     * Returns a newly created Pie Chart using labels from the column identified by labelKey and the values from the column labelled dataKey
     * @param frame         the DataFrame containing data
     * @param is3d          true for 3D PiePlot
     * @param dataKey       the column key to use for data values
     * @param labelKey      the column key to use for labels
     * @param configurator  the configurator to accept config to the chart
     * @param <X>           the frame row axis type
     * @param <S>           the frame column axis type
     * @return              the newly created chart
     */
    default <X extends Comparable,S extends Comparable> Chart<PiePlot<X,S>> withPiePlot(DataFrame<?,S> frame, boolean is3d, S dataKey, S labelKey, Consumer<Chart<PiePlot<X,S>>> configurator) {
        if (frame == null) {
            throw new IllegalArgumentException("The DataFrame cannot be null");
        } else {
            return ofPiePlot(is3d, chart -> {
                chart.plot().data().apply(frame, dataKey, labelKey);
                if (configurator != null) {
                    configurator.accept(chart);
                }
            });
        }
    }


    /**
     * Returns a Histogram Bar Chart of the frequency distribution for a all columns in a DataFrame
     * @param frame         the data from which to generate a histogram for each column
     * @param binCount      the number of bins to include in the histogram
     * @param configurator  the optional consumer to configure the chart
     * @param <C>           the series key type for frame
     * @return              the newly created chart
     */
    @SuppressWarnings("unchecked")
    default <R,C extends Comparable> Chart<XyPlot<Double>> withHistPlot(DataFrame<R,C> frame, int binCount, Consumer<Chart<XyPlot<Double>>> configurator)  {
        if (frame == null) {
            throw new IllegalArgumentException("The DataFrame cannot be null");
        } else if (frame.colCount() < 1) {
            throw new ChartException("The histogram data frame should contain at least one 1 column with frequency values");
        } else if (frame.rowCount() < 2) {
            throw new ChartException("The histogram data frame should have at least 2 rows");
        } else {
            final Iterator<C> colKeyIterator = frame.cols().keys().iterator();
            final DataFrame<Double,C> hist0 = frame.cols().hist(binCount, colKeyIterator.next());
            final double stepSize0 = hist0.rows().key(1) - hist0.rows().key(0);
            return withBarPlot(hist0, false, chart -> {
                chart.plot().data().at(0).withLowerDomainInterval(v -> v + stepSize0);
                chart.plot().axes().range(0).label().withText("Frequency");
                chart.plot().axes().domain().label().withText("Values");
                while (colKeyIterator.hasNext()) {
                    final DataFrame<Double,C> histN = frame.cols().hist(binCount, colKeyIterator.next());
                    final double stepSizeN = histN.rows().key(1) - histN.rows().key(0);
                    final int index = chart.plot().<C>data().add(histN);
                    chart.plot().data().at(index).withLowerDomainInterval(v -> v + stepSizeN);
                    chart.plot().render(index).withBars(false, 0d);
                }
                if (configurator != null) {
                    configurator.accept(chart);
                }
            });
        }
    }


    /**
     * Returns a Chart to plot a histogram based on the column data in the frame provided
     * @param frame         the data from which to generate a histogram for each column
     * @param binCount      the number of bins to include in the histogram
     * @param sharedBins    if true and the frame has multiple columns, each series will share the same width bin rather than being computed independently
     * @param configurator  the optional consumer to configure the chart
     * @param <C>           the column key type for frame
     * @return              the newly created chart
     */
    @SuppressWarnings("unchecked")
    default <R,C extends Comparable> Chart<XyPlot<Double>> withHistPlot(DataFrame<R,C> frame, int binCount, boolean sharedBins, Consumer<Chart<XyPlot<Double>>> configurator) {
        if (frame.colCount() < 1) {
            throw new ChartException("The histogram data frame should contain at least one 1 column with frequency values");
        } else if (frame.rowCount() < 2) {
            throw new ChartException("The histogram data frame should have at least 2 rows");
        } else if (!sharedBins) {
            return withHistPlot(frame, binCount, configurator);
        } else {
            final DataFrame<Double,C> hist = frame.cols().hist(binCount);
            final double first = hist.rows().key(0);
            final double second = hist.rows().key(1);
            final double stepSize = second - first;
            return withBarPlot(hist, false, chart -> {
                chart.title().withText("Histogram");
                chart.title().withFont(new Font("Arial", Font.PLAIN, 16));
                chart.plot().data().at(0).withLowerDomainInterval(v -> v + stepSize);
                chart.plot().axes().range(0).label().withText("Frequency");
                chart.plot().axes().domain().label().withText("Values");
                if (configurator != null) {
                    configurator.accept(chart);
                }
            });
        }
    }


    /**
     * Returns a Histogram Bar Chart of the frequency distribution for a specific column in a DataFrame
     * @param frame         the DataFrame of data to generate a histogram for
     * @param binCount      the number of bins to include in the histogram
     * @param columnKey     the key of the column to generate the histogram for
     * @param configurator  the optional consumer to configure the chart
     * @param <C>           the column key type for frame
     * @return              the newly created chart
     */
    @SuppressWarnings("unchecked")
    default  <R,C extends Comparable> Chart<XyPlot<Double>> withHistPlot(DataFrame<R,C> frame, int binCount, C columnKey, Consumer<Chart<XyPlot<Double>>> configurator) {
        if (frame == null) {
            throw new IllegalArgumentException("The DataFrame cannot be null");
        } else if (frame.colCount() < 1) {
            throw new ChartException("The histogram data frame should contain at least one 1 column with frequency values");
        } else if (frame.rowCount() < 2) {
            throw new ChartException("The histogram data frame should have at least 2 rows");
        } else {
            final DataFrame<R,C> series = frame.cols().select(columnKey);
            return withHistPlot(series, binCount, configurator);
        }
    }


    /**
     * Generates a plot of the Autocorrelation function (ACF) given the Least Squares model
     * @param model         the least squares model
     * @param maxLags       the max lags for ACF plot
     * @param alpha         the significance level for confidence intervals (e.g. 0.05 implies 5% level, or 95% confidence interval)
     * @param consumer      the consumer to configure additional options on the chart
     * @param <R>           the row key type
     * @param <C>           the column key type
     * @return              the resulting chart object
     */
    default <R extends Comparable,C extends Comparable> Chart<XyPlot<Integer>> withAcf(DataFrameLeastSquares<R,C> model, int maxLags, double alpha, Consumer<Chart<XyPlot<Integer>>> consumer) {
        final DataFrame<Integer,String> acf = model.getResidualsAcf(maxLags);
        final Array<String> bounds = Array.of("Upper", "Lower");
        final Array<Integer> lags = acf.rows().keyArray();
        final double erfInv = Math.sqrt(2d) * Erf.erfInv(1d - alpha);
        final double upper = 1d * erfInv / Math.sqrt(maxLags);
        final double lower = -1d * erfInv / Math.sqrt(maxLags);
        final int maxLag = acf.rows().lastKey().orElseThrow(() -> new RuntimeException("No data in autocorrelation matrix"));
        final DataFrame<Integer,String> boundsFrame = DataFrame.ofDoubles(lags, bounds, v -> v.colOrdinal() == 0 ? upper : lower);
        return Chart.create().withBarPlot(acf, false, chart -> {
            chart.title().withText("Autocorrelation Function (ACF)");
            chart.title().withFont(new Font("Arial", Font.BOLD, 16));
            chart.plot().<String>data().add(boundsFrame);
            chart.plot().render(1).withLines(false, true);
            chart.plot().data().at(0).withLowerDomainInterval(v -> v + 1);
            chart.plot().axes().domain().label().withText("Lag");
            chart.plot().axes().range(0).label().withText("Autocorrelation");
            chart.plot().axes().domain().withRange(Bounds.of(-1, (double)maxLag));
            chart.plot().style("Upper").withColor(Color.BLUE).withDashes(true).withLineWidth(1f);
            chart.plot().style("Lower").withColor(Color.BLUE).withDashes(true).withLineWidth(1f);
            if (consumer != null) {
                consumer.accept(chart);
            }
        });
    }


    /**
     * Returns a chart that plots regression residuals against the fitted values in a regression
     * @param model         the least squares model
     * @param consumer      the user provide chart configurator
     * @param <R>           the row key type
     * @param <C>           the column key type
     * @return              the resulting chart
     */
    default <R extends Comparable,C extends Comparable> Chart<XyPlot<Double>> withResidualsVsFitted(DataFrameLeastSquares<R,C> model, Consumer<Chart<XyPlot<Double>>> consumer) {
        final DataFrame<R,String> residuals = model.getResiduals();
        final DataFrame<R,String> fittedValues = model.getFittedValues();
        final DataFrame<R,String> zeroLine = fittedValues.copy().cols().add("Zero", Double.class, v -> 0d);
        final DataFrame<R,String> combined = DataFrame.concatColumns(residuals, fittedValues);
        return Chart.create().withLinePlot(combined, "Fitted", chart -> {
            chart.title().withText("Least Squares Residuals vs Fitted Values");
            chart.title().withFont(new Font("Arial", Font.BOLD, 15));
            chart.plot().<String>data().add(zeroLine, "Fitted");
            chart.plot().render(0).withDots();
            chart.plot().render(1).withLines(false, false);
            chart.plot().style("Zero").withColor(Color.BLACK).withLineWidth(2f);
            chart.plot().style("Residuals").withColor(Color.RED).withPointsVisible(true);
            chart.plot().axes().domain().label().withText("Fitted Values");
            chart.plot().axes().domain().format().withPattern("0.00;-0.00");
            chart.plot().axes().range(0).label().withText("Residuals");
            chart.plot().axes().range(0).format().withPattern("0.00;-0.00");
            chart.legend().on().bottom();
            if (consumer != null) {
                consumer.accept(chart);
            }
        });
    }

}
