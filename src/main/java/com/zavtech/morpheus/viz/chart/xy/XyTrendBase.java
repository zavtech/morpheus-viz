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
package com.zavtech.morpheus.viz.chart.xy;

import java.util.Optional;
import java.util.function.IntFunction;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.frame.DataFrameLeastSquares;
import com.zavtech.morpheus.range.Range;
import com.zavtech.morpheus.util.Bounds;

/**
 * A convenience base class for building XyTrend implementations
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public abstract class XyTrendBase implements XyTrend {

    private Comparable seriesKey;
    private double beta;
    private double intercept;
    private double rSquared;

    /**
     * Constructor
     * @param seriesKey     the series key for trend
     */
    public XyTrendBase(Comparable seriesKey) {
        this.seriesKey = seriesKey;

    }

    @Override
    public Comparable seriesKey() {
        return seriesKey;
    }

    /**
     * Returns the R-squared
     * @return  the R-squared
     */
    public double r2() {
        return rSquared;
    }

    /**
     * Returns the regression slope
     * @return  slope parameter
     */
    public double slope() {
        return beta;
    }

    /**
     * Returns the regression intercept
     * @return  intercept parameter
     */
    public double intercept() {
        return intercept;
    }

    /**
     * Returns a newly created single column DataFrame with the trend line values
     * @param source        the source model from which to create the trend line from
     * @param seriesKey     the series key in the source model from which to generate the trend
     * @param <X>           the domain axis type
     * @return              the newly created trend line DataFrame
     */
    public <X extends Comparable> DataFrame<Double,Comparable> createTrendData(XyDataset<X,Comparable> source, Comparable seriesKey, Comparable trendKey) {
        final DataFrame<Integer,Object> seriesFrame = createSeriesData(source, seriesKey);
        final Optional<Bounds<Number>> regressorRange = seriesFrame.colAt("Regressor").bounds();
        if (!regressorRange.isPresent()) {
            return DataFrame.empty();
        } else {
            final double minValue = regressorRange.get().lower().doubleValue();
            final double maxValue = regressorRange.get().upper().doubleValue();
            final double step1 = ((maxValue - minValue)) / 20d;
            final double step2 = ((maxValue - minValue)) / 10d;
            final Array<Double> values = Range.of(minValue - step1, maxValue + step1 * 2d, step2).toArray();
            return DataFrame.of(values, Comparable.class, columns -> {
                seriesFrame.regress().ols(seriesKey, "Regressor", true, slr -> {
                    this.beta = slr.getBetaValue("Regressor", DataFrameLeastSquares.Field.PARAMETER);
                    this.intercept = slr.getInterceptValue(DataFrameLeastSquares.Field.PARAMETER);
                    this.rSquared = slr.getRSquared();
                    columns.add(trendKey, Double.class).applyDoubles(v -> {
                        final double x = v.rowKey();
                        return beta * x + intercept;
                    });
                    return Optional.empty();
                });
            });
        }
    }


    /**
     * Returns a newly created DataFrame representing the series specified
     * @param seriesKey     the series key
     * @return              the one series DataFrame
     */
    private <X extends Comparable> DataFrame<Integer,Object> createSeriesData(XyDataset<X,Comparable> dataset, Comparable seriesKey) {
        final DataFrame<?,Comparable> frame = dataset.frame();
        final Range<Integer> rowKeys = Range.of(0, frame.rowCount());
        final int seriesIndex = frame.cols().ordinalOf(seriesKey);
        final IntFunction<X> domainFunc = dataset.domainFunction();
        return DataFrame.of(rowKeys, Object.class, columns -> {
            columns.add("Regressor", Double.class).applyDoubles(v -> ((Number)domainFunc.apply(v.rowOrdinal())).doubleValue());
            columns.add(seriesKey, Double.class).applyDoubles(v -> frame.data().getDouble(v.rowOrdinal(), seriesIndex));
        });
    }

}
