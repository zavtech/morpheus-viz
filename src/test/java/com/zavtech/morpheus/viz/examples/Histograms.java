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
package com.zavtech.morpheus.viz.examples;

import java.awt.*;
import java.io.File;
import java.util.Optional;

import org.testng.annotations.Test;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.range.Range;
import com.zavtech.morpheus.viz.chart.Chart;

/**
 * Javascript examples to generate histograms
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public class Histograms {


    @Test()
    public void histogram1() throws Exception {

        int recordCount = 1000000;
        DataFrame<Integer,String> frame = DataFrame.of(Range.of(0, recordCount), String.class, columns -> {
            columns.add("A", Array.randn(recordCount));
        });

        Chart.create().htmlMode();

        Chart.create().withHistPlot(frame, 50, chart -> {
            chart.title().withText("Normal Distribution");
            chart.subtitle().withText("Single Distribution");
            chart.writerPng(new File("../morpheus-docs/docs/images/charts/chart-hist-1.png"), 845, 450, true);
            chart.show();
        });

        Thread.currentThread().join();
    }


    @Test()
    public void histogram2() throws Exception {

        int recordCount = 1000000;
        DataFrame<Integer,String> frame = DataFrame.of(Range.of(0, recordCount), String.class, columns -> {
            columns.add("A", Array.randn(recordCount, 0d, 1d));
            columns.add("B", Array.randn(recordCount, 0d, 0.8d));
            columns.add("C", Array.randn(recordCount, 0d, 0.6d));
            columns.add("D", Array.randn(recordCount, 0d, 0.4d));
        });

        Chart.create().htmlMode();

        Chart.create().withHistPlot(frame, 100, chart -> {
            chart.title().withText("Normal Distribution");
            chart.subtitle().withText("Multiple Distributions");
            chart.writerPng(new File("../morpheus-docs/docs/images/charts/chart-hist-2.png"), 845, 450, true);
            chart.legend().on();
            chart.show();
        });

        Thread.currentThread().join();
    }


    @Test()
    public void histogram3() throws Exception {

        int binCount = 100;
        int recordCount = 100000;
        DataFrame<Integer,String> frame = DataFrame.of(Range.of(0, recordCount), String.class, columns -> {
            columns.add("A", Array.randn(recordCount));
        });

        Optional<DataFrame<Double,String>> pdfNormal = frame.colAt("A").<Double>bounds().map(bounds -> {
            final double startX = Math.max(Math.abs(bounds.lower()), Math.abs(bounds.upper()));
            final DataFrame<Double,String> pdf = normal("PDF(N)", -startX, startX, 1000, 0d, 1d);
            final double maxCount = frame.cols().select("A").cols().hist(binCount).stats().max();
            final double maxPdfValue = pdf.stats().max();
            return pdf.applyDoubles(v -> (v.getDouble() / maxPdfValue) * maxCount);
        });

        Chart.create().htmlMode();

        Chart.create().withHistPlot(frame, 100, chart -> {
            chart.plot().<String>data().add(pdfNormal.get());
            chart.plot().render(1).withLines(false, false);
            chart.plot().style("PDF(N)").withColor(Color.BLACK).withLineWidth(1.8f);
            chart.title().withText("Normal Distribution");
            chart.subtitle().withText("With Fitted Scaled PDF");
            chart.writerPng(new File("../morpheus-docs/docs/images/charts/chart-hist-3.png"), 845, 450, true);
            chart.show();
        });

        Thread.currentThread().join();
    }



    /**
     * Returns a single column DataFrame containing values generated from a normal distribution probability density function
     * @param label     the column key for the PDF series
     * @param lower     the lower bound for PDF
     * @param upper     the upper bound for PDF
     * @param count     the number of values to include
     * @param mean      the mean for the distribution
     * @param sigma     the standard deviation for the distribution
     * @return          the DataFrame of Normal PDF values
     */
    @SuppressWarnings("unchecked")
    private <C> DataFrame<Double,C> normal(C label, double lower, double upper, int count, double mean, double sigma) {
        final double stepSize = (upper - lower) / (double)count;
        final Range<Double> xValues = Range.of(lower, upper, stepSize);
        return DataFrame.of(xValues, (Class<C>)label.getClass(), columns -> {
            columns.add(label, xValues.map(x -> {
                final double part1 = 1d / (sigma * Math.sqrt(2d * Math.PI));
                final double part2 = Math.exp(-Math.pow(x - mean, 2d) / (2d * Math.pow(sigma, 2d)));
                return part1 * part2;
            }));
        });
    }

}
