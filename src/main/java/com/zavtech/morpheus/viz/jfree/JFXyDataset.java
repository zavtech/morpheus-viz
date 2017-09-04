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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.TableXYDataset;
import org.jfree.data.xy.XYZDataset;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.viz.chart.xy.XyDataset;

/**
 * An implementation of the Morpheus XyDataset interface and various JFreeChart interfaces to support plotting of a DataFrame in JFreeChart
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
class JFXyDataset<X extends Comparable,S extends Comparable> extends AbstractXYDataset implements XyDataset<X,S>, IntervalXYDataset, TableXYDataset, XYZDataset {

    private static final Double NAN = Double.NaN;

    private DataFrame<?,S> frame;
    private Array<Integer> colOrdinals;
    private Supplier<Class<X>> domainType;
    private IntFunction<X> domainValueFunction;
    private Consumer<JFXyDataset<X,S>> refreshHandler;
    private Function<X,X> lowerDomainIntervalFunction;
    private Function<X,X> upperDomainIntervalFunction;


    /**
     * Constructor
     * @param refreshHandler    the refresh handler for this model
     */
    private JFXyDataset(Consumer<JFXyDataset<X,S>> refreshHandler) {
        this.refreshHandler = refreshHandler;
        this.refresh();
    }


    /**
     * Returns a newly created model using a frame supplier where the domain is presented by the DataFrame row keys
     * @param frameSupplier     the DataFrame supplier for this model
     * @param <X>               the domain key type
     * @param <S>               the series key type
     * @return                  the newly created model
     */
    static <X extends Comparable,S extends Comparable> JFXyDataset<X,S> of(Supplier<DataFrame<X,S>> frameSupplier) {
        return new JFXyDataset<>(dataset -> {
            try {
                final DataFrame<X,S> frame = frameSupplier.get();
                if (frame != null) {
                    final Supplier<Class<X>> domainType = () -> frame.rows().keyType();
                    final Array<Integer> colOrdinals = Array.of(IntStream.range(0, frame.colCount()).toArray());
                    dataset.update(frame, colOrdinals, domainType, rowIndex -> frame.rows().key(rowIndex));
                } else {
                    dataset.clear(true);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }


    /**
     * Returns a newly created model using a frame supplier where the domain is presented by a column in the DataFrame
     * @param domainAxisKey     the DataFrame column key for the domain
     * @param frameSupplier     the DataFrame supplier for this model
     * @param <X>               the domain key type
     * @param <S>               the series key type
     * @return                  the newly created model
     */
    @SuppressWarnings("unchecked")
    static <X extends Comparable,S extends Comparable> JFXyDataset<X,S> of(S domainAxisKey, Supplier<DataFrame<?,S>> frameSupplier) {
        return new JFXyDataset<>(dataset -> {
            try {
                final DataFrame<?,S> frame = frameSupplier.get();
                if (frame != null) {
                    final int domainAxisColOrdinal = frame.cols().ordinalOf(domainAxisKey);
                    final Supplier<Class<X>> domainType = () -> (Class<X>)frame.cols().type(domainAxisKey);
                    final Array<Integer> colOrdinals = Array.of(IntStream.range(0, frame.colCount()).filter(i -> i != domainAxisColOrdinal).toArray());
                    dataset.update(frame, colOrdinals, domainType, rowIndex -> frame.data().getValue(rowIndex, domainAxisColOrdinal));
                } else {
                    dataset.clear(true);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }


    /**
     * Updates this model with the DataFrame, series column ordinals and domain value function
     * @param frame                 the DataFrame to accept
     * @param colOrdinals           the series column ordinals
     * @param domainValueFunction   the domain value function
     */
    private void update(DataFrame<?,S> frame, Array<Integer> colOrdinals, Supplier<Class<X>> domainType, IntFunction<X> domainValueFunction) {
        try {
            this.frame = frame;
            this.domainType = domainType;
            this.colOrdinals = colOrdinals;
            this.domainValueFunction = domainValueFunction;
        } finally {
            fireDatasetChanged();
        }
    }


    @Override
    public void refresh() {
        this.refreshHandler.accept(this);
    }


    @Override
    public final boolean isEmpty() {
        return frame == null || frame.rowCount() == 0 || colOrdinals == null || colOrdinals.length() == 0;
    }


    @Override
    public final void clear(boolean notify) {
        this.frame = null;
        this.colOrdinals = null;
        if (notify) {
            fireDatasetChanged();
        }
    }


    @Override
    public Class<X> domainType() {
        return isEmpty() ? null : domainType.get();
    }


    @Override
    @SuppressWarnings("unchecked")
    public final <R> DataFrame<R,S> frame() {
        return (DataFrame<R,S>)frame;
    }


    @Override
    public final IntFunction<X> domainFunction() {
        return domainValueFunction;
    }


    @Override
    public final boolean contains(S seriesKey) {
        return !isEmpty() && frame.cols().contains(seriesKey);
    }


    @Override
    public final XyDataset<X,S> withLowerDomainInterval(Function<X,X> lowerIntervalFunction) {
        this.lowerDomainIntervalFunction = lowerIntervalFunction;
        return this;
    }


    @Override
    public final XyDataset<X,S> withUpperDomainInterval(Function<X,X> upperIntervalFunction) {
        this.upperDomainIntervalFunction = upperIntervalFunction;
        return null;
    }


    @Override
    public final int getItemCount() {
        return isEmpty() ? 0 : frame.rowCount();
    }


    @Override
    public final int getSeriesCount() {
        return isEmpty() ? 0 : colOrdinals.length();
    }


    @Override
    public final S getSeriesKey(int series) {
        if (isEmpty()) {
            return null;
        } else {
            final int colOrdinal = colOrdinals.getInt(series);
            return frame.cols().key(colOrdinal);
        }
    }


    @Override
    public final int getItemCount(int series) {
        return isEmpty() ? 0 : frame.rowCount();
    }


    @Override
    public final Number getZ(int series, int item) {
        return null;
    }


    @Override
    public final double getZValue(int series, int item) {
        return 0;
    }

    @Override
    public final Number getX(int series, int item) {
        final double value = getXValue(series, item);
        return Double.isNaN(value) ? NAN : value;
    }


    @Override
    public final Number getY(int series, int item) {
        final double value = getYValue(series, item);
        return Double.isNaN(value) ? NAN : value;
    }


    @Override
    public final Number getStartX(int series, int item) {
        final double value = getStartXValue(series, item);
        return Double.isNaN(value) ? NAN : value;
    }


    @Override
    public final Number getEndX(int series, int item) {
        final double value = getEndXValue(series, item);
        return Double.isNaN(value) ? NAN : value;
    }


    @Override
    public final Number getStartY(int series, int item) {
        final double value = getStartYValue(series, item);
        return Double.isNaN(value) ? NAN : value;
    }


    @Override
    public Number getEndY(int series, int item) {
        final double value = getEndYValue(series, item);
        return Double.isNaN(value) ? NAN : value;
    }


    @Override
    public final double getXValue(int series, int item) {
        if (isEmpty()) {
            return Double.NaN;
        } else {
            final X domainValue = domainValueFunction.apply(item);
            return toNumber(domainValue).doubleValue();
        }
    }


    @Override
    public final double getYValue(int series, int item) {
        if (isEmpty()) {
            return Double.NaN;
        } else {
            final int colOrdinal = colOrdinals.getInt(series);
            return frame.data().getDouble(item, colOrdinal);
        }
    }


    @Override
    public final double getStartXValue(int series, int item) {
        if (isEmpty()) {
            return Double.NaN;
        } else {
            final X domainValue = domainValueFunction.apply(item);
            if (lowerDomainIntervalFunction != null) {
                final X startValueKey = lowerDomainIntervalFunction.apply(domainValue);
                final Number startValue = toNumber(startValueKey);
                return startValue != null ? startValue.doubleValue() : Double.NaN;
            } else {
                final Number startValue = toNumber(domainValue);
                return startValue != null ? startValue.doubleValue() : Double.NaN;
            }
        }
    }


    @Override
    public final double getEndXValue(int series, int item) {
        if (isEmpty()) {
            return Double.NaN;
        } else {
            final X domainValue = domainValueFunction.apply(item);
            if (upperDomainIntervalFunction != null) {
                final X endValueKey = upperDomainIntervalFunction.apply(domainValue);
                final Number endValue = toNumber(endValueKey);
                return endValue != null ? endValue.doubleValue() : Double.NaN;
            } else {
                final Number endValue = toNumber(domainValue);
                return endValue != null ? endValue.doubleValue() : Double.NaN;
            }
        }
    }


    @Override
    public final double getStartYValue(int series, int item) {
        if (isEmpty()) {
            return Double.NaN;
        } else {
            final int colOrdinal = colOrdinals.getInt(series);
            return frame.data().getDouble(item, colOrdinal);
        }
    }


    @Override
    public final double getEndYValue(int series, int item) {
        if (isEmpty()) {
            return Double.NaN;
        } else {
            final int colOrdinal = colOrdinals.getInt(series);
            return frame.data().getDouble(item, colOrdinal);
        }
    }


    @Override
    public void fireDatasetChanged() {
        super.fireDatasetChanged();
    }


    /**
     * Returns a numeric representation of the value argument
     * @param value     the value to turn into a number
     * @return          the numeric value
     */
    private Number toNumber(Object value) {
        if (value == null) {
            return Double.NaN;
        } else if (value instanceof Number) {
            return (Number)value;
        } else if (value instanceof Date) {
            return ((Date)value).getTime();
        } else if (value instanceof LocalDate) {
            return ((LocalDate)value).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
        } else if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).toInstant(ZoneOffset.UTC).toEpochMilli();
        } else if (value instanceof ZonedDateTime) {
            return ((ZonedDateTime)value).toInstant().toEpochMilli();
        } else if (value instanceof Calendar) {
            return ((Calendar)value).getTimeInMillis();
        } else {
            throw new IllegalArgumentException("Unable to convert value to a Number: " + value);
        }
    }
}