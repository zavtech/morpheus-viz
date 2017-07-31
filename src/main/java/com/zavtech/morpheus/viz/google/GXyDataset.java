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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.range.Range;
import com.zavtech.morpheus.util.IO;
import com.zavtech.morpheus.viz.chart.xy.XyDataset;
import com.zavtech.morpheus.viz.js.Javascript;

/**
 * An implementation of the XyDataset interface to be used with Google charts
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
class GXyDataset<X extends Comparable,S extends Comparable> implements XyDataset<X,S> {

    private DataFrame<?,S> frame;
    private Array<Integer> colOrdinals;
    private Supplier<Class<X>> domainType;
    private IntFunction<X> domainValueFunction;
    private Consumer<GXyDataset<X,S>> refreshHandler;


    /**
     * Constructor
     * @param refreshHandler    the refresh handler
     */
    private GXyDataset(Consumer<GXyDataset<X,S>> refreshHandler) {
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
    static <X extends Comparable,S extends Comparable> GXyDataset<X,S> of(Supplier<DataFrame<X,S>> frameSupplier) {
        return new GXyDataset<>(dataset -> {
            try {
                final DataFrame<X,S> frame = frameSupplier.get();
                if (frame != null) {
                    final Array<Integer> colOrdinals = Array.of(IntStream.range(0, frame.colCount()).toArray());
                    final Supplier<Class<X>> domainType = () -> frame.rows().keyType();
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
    static <X extends Comparable,S extends Comparable> GXyDataset<X,S> of(S domainAxisKey, Supplier<DataFrame<?,S>> frameSupplier) {
        return new GXyDataset<>(dataset -> {
            try {
                final DataFrame<?,S> frame = frameSupplier.get();
                if (frame != null) {
                    final int domainAxisColOrdinal = frame.cols().ordinalOf(domainAxisKey);
                    final Array<Integer> colOrdinals = Array.of(IntStream.range(0, frame.colCount()).filter(i -> i != domainAxisColOrdinal).toArray());
                    final Supplier<Class<X>> domainType = () -> (Class<X>)frame.cols().type(domainAxisKey);
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
        this.frame = frame;
        this.colOrdinals = colOrdinals;
        this.domainType = domainType;
        this.domainValueFunction = domainValueFunction;
    }


    @Override
    public void refresh() {
        this.refreshHandler.accept(this);
    }


    @Override
    public boolean isEmpty() {
        return frame == null || frame.rowCount() == 0;
    }


    @Override
    public void clear(boolean notify) {
        this.frame = null;
        this.colOrdinals = null;
        this.domainValueFunction = null;
    }


    @Override
    @SuppressWarnings("unchecked")
    public <R> DataFrame<R,S> frame() {
        return (DataFrame<R,S>)frame;
    }


    @Override
    public Class<X> domainType() {
        return isEmpty() ? null : domainType.get();
    }


    @Override
    public boolean contains(S seriesKey) {
        return !isEmpty() && frame.cols().contains(seriesKey);
    }


    @Override
    public IntFunction<X> domainFunction() {
        return domainValueFunction;
    }


    @Override
    public XyDataset<X,S> withLowerDomainInterval(Function<X, X> lowerIntervalFunction) {
        return null;
    }


    @Override
    public XyDataset<X,S> withUpperDomainInterval(Function<X, X> upperIntervalFunction) {
        return null;
    }


    /**
     * Returns the class of the domain values in this dataset
     * @return  the class of the domain values in this dataset
     */
    @SuppressWarnings("unchecked")
    Class<X> getDomainKeyType() {
        if (isEmpty()) {
            return (Class<X>)Double.class;
        } else {
            for (int i=0; i<frame.rowCount(); ++i) {
                final X value = domainValueFunction.apply(i);
                if (value != null) {
                    return (Class<X>)value.getClass();
                }
            }
            return (Class<X>)Double.class;
        }
    }


    /**
     * Returns the iterable of domain values for this dataset
     * @return      the iterable domain values
     */
    public Iterable<X> getDomainValues() {
        if (isEmpty()) {
            return Collections.emptyList();
        } else {
            return Range.of(0, frame.rowCount()).map(domainValueFunction::apply);
        }
    }

    /**
     * Returns the series keys for this dataset
     * @return      the series keys for dataset
     */
    public Iterable<S> getSeriesKeys() {
        if (isEmpty()) {
            return Collections.emptyList();
        } else {
            return colOrdinals.map(v -> frame.cols().key(v.getInt()));
        }
    }


    /**
     * Returns the number of series in this dataset
     * @return      the number of series
     */
    public int getSeriesCount() {
        return isEmpty() ? 0 : colOrdinals.length();
    }


    /**
     * Returns the series for the index specified
     * @param series    the series index
     * @return          the series key
     */
    public S getSeriesKey(int series) {
        return isEmpty() ? null : frame.cols().key(colOrdinals.getInt(series));
    }


    /**
     * Returns the number of values per series
     * @return      the number of values per series
     */
    public int getDomainSize() {
        return isEmpty() ? 0 : frame.rowCount();
    }


    /**
     * Returns the domain value for the item index
     * @param item  the item index
     * @return      the corresponding domain value
     */
    public X getDomainValue(int item) {
        return isEmpty() ? null : domainValueFunction.apply(item);
    }


    /**
     * Returns the range value for the item and series index
     * @param item      the item index
     * @param series    the series index
     * @return          the range value
     */
    public double getRangeValue(int item, int series) {
        if (isEmpty()) {
            return Double.NaN;
        } else {
            final int colOrdinal = colOrdinals.getInt(series);
            return frame.data().getDouble(item, colOrdinal);
        }
    }


    public void accept(Javascript script) {
        final Class<?> domainClass = domainType();
        final GDataType domainType = GDataType.getDataType(domainClass, GDataType.STRING);
        script.newArray(array -> {
            array.appendArray(false, header -> {
                header.appendObject(true, domain -> {
                    domain.newAttribute("id", "domain");
                    domain.newAttribute("label", "Domain");
                    domain.newAttribute("type", domainType.getLabel());
                });
                for (int i=0; i<getSeriesCount(); ++i) {
                    final Comparable seriesKey = getSeriesKey(i);
                    header.appendObject(true, series -> {
                        series.newAttribute("id", seriesKey.toString());
                        series.newAttribute("label", seriesKey.toString());
                        series.newAttribute("type", "number");
                    });
                }
            });
            final Function<Object,String> domainValueFunc = createDomainFunction(domainClass);
            for (int i = 0; i<getDomainSize(); ++i) {
                final int index = i;
                final X domainValue = getDomainValue(i);
                final String stringValue = domainValueFunc.apply(domainValue);
                array.appendArray(true, series -> {
                    series.append(stringValue, false);
                    for (int j=0; j<getSeriesCount(); ++j) {
                        final double value = getRangeValue(index, j);
                        if (Double.isNaN(value)) {
                            series.append(null);
                        } else {
                            series.append(value);

                        }
                    }
                });
            }
        });
    }

    /**
     * Creates a function that yields a long time value in epoch millis given some input
     * @param dataType      the data type
     * @return              the function to resolve epoch millis
     */
    private Function<Object,String> createDomainFunction(Class<?> dataType) {
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


    public static void main(String[] args) {
        final Range<LocalDate> rowAxis = Range.ofLocalDates("2017-01-01", "2017-06-01");
        final Array<String> colAxis = Array.of("A", "B", "C", "D");
        final DataFrame<LocalDate,String> frame = DataFrame.ofDoubles(rowAxis, colAxis, v -> Math.random());
        final GXyDataset dataset = GXyDataset.of(() -> frame);
        final Javascript js = new Javascript();
        dataset.accept(js);
        IO.println(js.toString());
    }

}
