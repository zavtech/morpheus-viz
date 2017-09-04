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

import java.io.File;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.testng.annotations.Test;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.range.Range;
import com.zavtech.morpheus.viz.chart.Chart;

/**
 * Javascript examples to generate regression charts
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public class RegressionCharts {


    /**
     * Returns a 2D sample dataset given a slope and intercept while adding white noise based on sigma.
     * @param alpha     the intercept term for data
     * @param beta      the slope term for for data
     * @param sigma     the standard deviation for noise
     * @param stepSize  the step size for domain variable
     * @param n         the size of the sample to generate
     * @return          the frame of XY values
     */
    private DataFrame<Integer,String> scatter(double alpha, double beta, double sigma, double stepSize, int n) {
        final Array<Double> xValues = Array.of(Double.class, n).applyDoubles(v -> 0d + v.index() * stepSize);
        final Array<Double> yValues = Array.of(Double.class, n).applyDoubles(v -> {
            final double yfit = alpha + beta * xValues.getDouble(v.index());
            return new NormalDistribution(yfit, sigma).sample();
        });
        final Array<Integer> rowKeys = Range.of(0, n).toArray();
        return DataFrame.of(rowKeys, String.class, columns -> {
            columns.add("X", xValues);
            columns.add("Y", yValues);
        }).cols().demean(true);
    }


    @Test()
    public void regression1() throws Exception {
        DataFrame<Integer,String> frame = scatter(4d, 1d, 80d, 0.5d, 1000);
        Chart.create().withScatterPlot(frame, false, "X", chart -> {
            chart.plot().axes().domain().label().withText("X-Value");
            chart.plot().axes().range(0).label().withText("Y-Value");
            chart.plot().trend("Y");
            chart.title().withText("Regression Chart");
            chart.subtitle().withText("Single DataFrame, Single Series");
            //chart.writerPng(new File("../morpheus-docs/docs/images/charts/chart-regress-1.png"), 845, 450, true);
            chart.show();
        });

        Thread.currentThread().join();
    }


    @Test()
    public void regression2() throws Exception {

        DataFrame<Integer,String> frame1 = DataFrame.concatColumns(
            scatter(4d, 1d, 80d, 0.5, 500).cols().replaceKey("Y", "A"),
            scatter(4d, 4d, 100d, 0.5, 500).cols().replaceKey("Y", "B")
        );

        DataFrame<Integer,String> frame2 = DataFrame.concatColumns(
            scatter(4d, -3d, 80d, 0.55, 600).cols().replaceKey("Y", "C"),
            scatter(4d, -10d, 100d, 0.45, 600).cols().replaceKey("Y", "D")
        );

        //Chart.create().html();

        Chart.create().withScatterPlot(frame1, false, "X", chart -> {
            chart.plot().<String>data().add(frame2, "X");
            chart.plot().render(1).withDots();
            chart.plot().axes().domain().label().withText("X-Value");
            chart.plot().axes().range(0).label().withText("Y-Value");
            chart.plot().trend("A");
            chart.plot().trend("B");
            chart.plot().trend("C");
            chart.title().withText("Regression Chart");
            chart.subtitle().withText("Multiple DataFrame, Multiple Series");
            chart.legend().on();
            chart.writerPng(new File("../morpheus-docs/docs/images/charts/chart-regress-2.png"), 845, 450, true);
            chart.show();
        });

        Thread.currentThread().join();
    }


}
