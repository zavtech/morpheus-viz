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
import java.io.File;
import java.io.OutputStream;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.commons.math3.special.Erf;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrameLeastSquares;
import com.zavtech.morpheus.frame.DataFrameRow;
import com.zavtech.morpheus.util.Bounds;
import com.zavtech.morpheus.viz.util.ColorModel;
import com.zavtech.morpheus.frame.DataFrame;

/**
 * The top level interface to the Morpheus Chart abstraction API which can be implemented against various underlying libraries.
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public interface Chart<X extends Comparable> {

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
     * Returns the axis interface for this chart
     * @return  the axis interface for this chart
     */
    ChartAxes axes();

    /**
     * Returns a reference to the chart data interface
     * @return  the chart data management interface
     */
    ChartData<X> data();

    /**
     * Returns the chart legend control for this chart
     * @return      the legend control interface
     */
    ChartLegend legend();

    /**
     * Returns the chart plot style interface for the dataset index
     * @param index the dataset index, 0 being the first dataset
     * @return      the plot style interface for the dataset specified
     */
    ChartPlotStyle plot(int index);

    /**
     * Returns the trend line controller interface for this chart
     * @return  the trend line controller interface
     */
    ChartTrendLine trendLine();

    /**
     * Returns the interface to set the chart orientation
     * @return  the orientation interface
     */
    ChartOrientation orientation();

    /**
     * Returns the series style interface for the series key specified
     * @param seriesKey the series key to operate
     * @return          the style interface for the series key
     */
    ChartSeriesStyle style(Comparable seriesKey);

    /**
     * Applies a color model to choose series colors for this chart
     * @param colorModel    the color model for chart
     * @return              this chart referemce
     */
    Chart withColorModel(ColorModel colorModel);

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
     * @param file      the file reference
     * @param width     the image width
     * @param height    the image height
     * @return          this chart
     */
    Chart writerPng(File file, int width, int height);

    /**
     * Writes a PNH image of the chart to the output stream
     * @param os        the output stream
     * @param width     the image width
     * @param height    the image height
     * @return          this chart
     */
    Chart writerPng(OutputStream os, int width, int height);


    /**
     * Creates a new Chart for plotting XY data
     * @param consumer  the consumer to configure aspects of the chart
     * @return          the newly created chart
     */
    static <X extends Comparable,S extends Comparable> Chart<X> of(DataFrame<X,S> frame, Consumer<Chart<X>> consumer) {
        final ChartEngine engine = ChartEngine.getDefaultEngine();
        final Function<Chart<X>,Chart<X>> function = chart -> { consumer.accept(chart); return chart; };
        return function.apply(engine.create(frame));
    }

    /**
     * Creates a new Chart for plotting XY data
     * @param frame     the data frame
     * @param domainKey the column key to use to construct domain axis
     * @param consumer  the consumer to configure aspects of the chart
     * @return          the newly created chart
     */
    static <X extends Comparable,S extends Comparable> Chart<X> of(DataFrame<?,S> frame, S domainKey, Class<X> domainType, Consumer<Chart<X>> consumer) {
        final ChartEngine engine = ChartEngine.getDefaultEngine();
        final Function<Chart<X>,Chart<X>> function = chart -> { consumer.accept(chart); return chart; };
        return function.apply(engine.create(frame, domainType, domainKey));
    }


    /**
     * Creates a new Chart for plotting XY data
     * @param frame     the data frame
     * @param domainKey the column key to use to construct domain axis
     * @param consumer  the consumer to configure aspects of the chart
     * @return          the newly created chart
     */
    static <X extends Comparable,S extends Comparable> Chart withPoints(DataFrame<?,S> frame, S domainKey, Class<X> domainType, Consumer<Chart<?>> consumer) {
        final ChartEngine engine = ChartEngine.getDefaultEngine();
        final Chart<X> chart = engine.create(frame, domainType, domainKey);
        chart.plot(0).withPoints();
        consumer.accept(chart);
        return chart;
    }


    /**
     * Returns a Chart to plot a histogram based on the column data in the frame provided
     * @param data          the data from which to generate a histogram for each column
     * @param binCount      the number of bins to include in the histogram
     * @param consumer      the optional consumer to configure the chart
     * @param <C>           the column key type for frame
     * @return              the newly created chart
     */
    static <R,C extends Comparable> Chart hist(DataFrame<R,C> data, int binCount, Consumer<Chart<?>> consumer) {
        if (data.colCount() < 1) {
            throw new ChartException("The histogram data frame should contain at least one 1 column with frequency values");
        } else if (data.rowCount() < 2) {
            throw new ChartException("The histogram data frame should have at least 2 rows");
        } else {
            final DataFrame<Double,C> hist = data.cols().hist(binCount);
            final double first = hist.rows().key(0);
            final double second = hist.rows().key(1);
            final double stepSize = second - first;
            return Chart.of(hist, chart -> {
                chart.plot(0).withBars(0d);
                chart.data().at(0).withDomainInterval(v -> v + stepSize);
                chart.title().withText("Histogram");
                chart.title().withFont(new Font("Arial", Font.PLAIN, 16));
                chart.axes().range(0).label().withText("Frequency");
                chart.axes().domain().label().withText("Values");
                if (consumer != null) {
                    consumer.accept(chart);
                }
            });
        }
    }


    /**
     * Returns a Chart to plot a histogram of the frquency distribution for a specific column in a DataFrame
     * @param data          the DataFrame of data to compute a histogram
     * @param columnKey     the key of the column to generate the histogram for
     * @param consumer      the optional consumer to configure the chart
     * @param <C>           the column key type for frame
     * @return              the newly created chart
     */
    static <R extends Comparable,C extends Comparable> Chart<R> hist(DataFrame<R,C> data, C columnKey, int binCount, Consumer<Chart<?>> consumer) {
        return Chart.hist(data.cols().select(columnKey), binCount, consumer);
    }


    /**
     * Generates a plot of the Autocorrelation function (ACF) given the Least Squares model
     * @param model         the least squares model
     * @param maxLags       the max lags for ACF plot
     * @param bound         the value of the boundary for hypothesis check
     * @param <R>           the row key type
     * @param <C>           the column key type
     * @return              the resulting chart object
     */
    static <R extends Comparable,C extends Comparable> Chart acf(DataFrameLeastSquares<R,C> model, int maxLags, double bound) {
        return Chart.acf(model, maxLags, bound, null);
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
    static <R extends Comparable,C extends Comparable> Chart acf(DataFrameLeastSquares<R,C> model, int maxLags, double alpha, Consumer<Chart<?>> consumer) {
        final DataFrame<Integer,String> acf = model.getResidualsAcf(maxLags);
        final Array<String> bounds = Array.of("Upper", "Lower");
        final Array<Integer> lags = acf.rows().keyArray();
        final double erfInv = Math.sqrt(2d) * Erf.erfInv(1d - alpha);
        final double upper = 1d * erfInv / Math.sqrt(maxLags);
        final double lower = -1d * erfInv / Math.sqrt(maxLags);
        final int maxLag = acf.rows().lastKey().orElseThrow(() -> new RuntimeException("No data in autocorrelation matrix"));
        final DataFrame<Integer,String> boundsFrame = DataFrame.ofDoubles(lags, bounds, v -> v.colOrdinal() == 0 ? upper : lower);
        return Chart.of(acf, chart -> {
            chart.data().add(boundsFrame);
            chart.plot(0).withBars(0d);
            chart.plot(1).withLines();
            chart.data().at(0).withDomainInterval(v -> v + 1);
            chart.axes().domain().label().withText("Lag");
            chart.axes().range(0).label().withText("Autocorrelation");
            chart.title().withText("Autocorrelation Function (ACF)");
            chart.title().withFont(new Font("Arial", Font.BOLD, 16));
            chart.axes().domain().withRange(Bounds.of(-1, (double)maxLag));
            chart.style("Upper").withColor(Color.BLUE).withDashes(true).withLineWidth(1f);
            chart.style("Lower").withColor(Color.BLUE).withDashes(true).withLineWidth(1f);
            if (consumer != null) {
                consumer.accept(chart);
            }
        });
    }


    /**
     * Returns a chart that plots regression residuals against the fitted values in a regression
     * @param model         the least squares model
     * @param <R>           the row key type
     * @param <C>           the column key type
     * @return              the resulting chart
     */
    static <R extends Comparable,C extends Comparable> Chart residualsVsFitted(DataFrameLeastSquares<R,C> model) {
        return residualsVsFitted(model, null);
    }


    /**
     * Returns a chart that plots regression residuals against the fitted values in a regression
     * @param model         the least squares model
     * @param consumer      the user provide chart configurator
     * @param <R>           the row key type
     * @param <C>           the column key type
     * @return              the resulting chart
     */
    static <R extends Comparable,C extends Comparable> Chart residualsVsFitted(DataFrameLeastSquares<R,C> model, Consumer<Chart<?>> consumer) {
        final DataFrame<R,String> residuals = model.getResiduals();
        final DataFrame<R,String> fittedValues = model.getFittedValues();
        final DataFrame<R,String> zeroLine = fittedValues.copy().cols().add("Zero", Double.class, v -> 0d);
        final DataFrame<R,String> combined = DataFrame.concatColumns(residuals, fittedValues);
        return Chart.of(combined, "Fitted", Double.class, chart -> {
            chart.data().add(zeroLine, "Fitted");
            chart.plot(0).withPoints();
            chart.plot(1).withLines();
            chart.style("Zero").withColor(Color.BLACK).withLineWidth(2f);
            chart.style("Residuals").withColor(Color.RED).withPointsVisible(true);
            chart.title().withText("Least Squares Residuals vs Fitted Values");
            chart.title().withFont(new Font("Arial", Font.BOLD, 15));
            chart.axes().domain().label().withText("Fitted Values");
            chart.axes().domain().format().withPattern("0.00;-0.00");
            chart.axes().range(0).label().withText("Residuals");
            chart.axes().range(0).format().withPattern("0.00;-0.00");
            chart.legend().on().bottom();
            if (consumer != null) {
                consumer.accept(chart);
            }
        });
    }


    /**
     * Returns a chart that plots regression residuals versus the order of the residuals
     * @param model         the least squares model
     * @param <R>           the row key type
     * @param <C>           the column key type
     * @return              the resulting chart
     */
    static <R extends Comparable,C extends Comparable> Chart residualsVsOrder(DataFrameLeastSquares<R,C> model) {
        return Chart.residualsVsOrder(model, null);
    }


    /**
     * Returns a chart that plots regression residuals versus the order of the residuals
     * @param model         the least squares model
     * @param consumer      the user provide chart configurator
     * @param <R>           the row key type
     * @param <C>           the column key type
     * @return              the resulting chart
     */
    static <R extends Comparable,C extends Comparable> Chart residualsVsOrder(DataFrameLeastSquares<R,C> model, Consumer<Chart<?>> consumer) {
        final DataFrame<Integer,String> residuals = model.getResiduals().rows().mapKeys(DataFrameRow::ordinal);
        return Chart.of(residuals, chart -> {
            chart.plot(0).withPoints();
            chart.style("Residuals").withColor(Color.RED).withPointsVisible(true).withLineWidth(1f);
            chart.title().withText("Least Squares Residuals vs Order");
            chart.title().withFont(new Font("Verdana", Font.BOLD, 16));
            chart.axes().domain().label().withText("Order");
            chart.axes().domain().format().withPattern("0;-0");
            chart.axes().range(0).label().withText("Residuals");
            chart.axes().range(0).format().withPattern("0.00;-0.00");
            chart.legend().on().bottom();
            if (consumer != null) {
                consumer.accept(chart);
            }
        });
    }



}


