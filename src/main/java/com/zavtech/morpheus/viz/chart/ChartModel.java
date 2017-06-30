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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.array.ArrayCollector;
import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.frame.DataFrameLeastSquares;
import com.zavtech.morpheus.frame.DataFrameLeastSquares.Field;
import com.zavtech.morpheus.index.Index;
import com.zavtech.morpheus.util.Bounds;
import com.zavtech.morpheus.util.ListenerList;
import com.zavtech.morpheus.range.Range;

/**
 * An generalized interface to a data model that can be used bind data to various kinds of charts.
 *
 * @param <X>   the datum key
 * @param <S>   the series key
 *
 * @author  Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public interface ChartModel<X extends Comparable,S extends Comparable> {

    /**
     * Returns the size of the domain for this dataset
     * @return      the number of data items in domain
     */
    int getSize();

    /**
     * Returns the series count for this dataset
     * @return  the series count
     */
    int getSeriesCount();

    /**
     * Returns the domain interval function for this model
     * @return  the domain interval function
     */
    Function<X,X> getDomainInterval();

    /**
     * Updates the frame for this model
     * @param frame the updated frame for this model
     */
    ChartModel<X,S> apply(DataFrame<X,S> frame);

    /**
     * Updates the frame for this model
     * @param frame the updated frame for this model
     * @param domainKey the column key in the frame to use for domain axis
     */
    ChartModel<X,S> apply(DataFrame<?,S> frame, S domainKey);

    /**
     * Sets the domain interval function for this model
     * The domain interval function accepts a domain key and returns the interval to add
     * @param intervalFunction    the domain interval function
     * @return  this model reference
     */
    ChartModel<X,S> withDomainInterval(Function<X,X> intervalFunction);

    /**
     * Returns true if this model contains the series specified
     * @param seriesKey     the series key
     * @return              true if this model contains the series
     */
    boolean contains(Comparable seriesKey);

    /**
     * Returns the domain key type for dataset
     * @return      the domain key type
     */
    Class<X> getDomainKeyType();

    /**
     * Returns the series key type for dataset
     * @return      the series key type
     */
    Class<S> getSeriesKeyType();

    /**
     * Returns the domain keys for this dataset
     * @return  the domain keys for dataset
     */
    Iterable<X> getDomainKeys();

    /**
     * Returns the series keys for this dataset
     * @return  the series keys for dataset
     */
    Iterable<S> getSeriesKeys();

    /**
     * Returns the domain key for the index specified
     * @param domainIndex   the domain index
     * @return              the domain key
     */
    X getDomainKey(int domainIndex);

    /**
     * Returns the series key for the series index
     * @param seriesIndex   the series index
     * @return              the series key
     */
    S getSeriesKey(int seriesIndex);

    /**
     * Returns the data bounds for the domain dimension
     * @return  the data bounds for domain dimension
     */
    Optional<Bounds<X>> getDomainBounds();

    /**
     * Returns the bounds across all series in this dataset
     * @return  the data bounds across all series
     */
    Optional<Bounds<Number>> getSeriesBounds();

    /**
     * Returns the bounds for the series with key provided
     * @param seriesKey the series key
     * @return          the series bounds
     */
    Optional<Bounds<Number>> getSeriesBounds(S seriesKey);

    /**
     * Returns the data type for the series key specified
     * @param seriesKey the series key
     * @return          the series data type
     */
    Class<? extends Number> getSeriesDataType(S seriesKey);

    /**
     * Returns the value for the datum in a series at the index specified
     * @param domainIndex   the domain index
     * @param seriesIndex   the series index
     * @return              the datum value, which could be null or NaN
     */
    Number getRangeValue(int domainIndex, int seriesIndex);

    /**
     * Returns a newly created dataset representing a linear regression trend-line for the series specified
     * @param seriesKey     the series key to calculate linear regression trend-line
     * @return              the newly created dataset, which will dynamically update if this dataset changes
     */
    <T extends Comparable> ChartModel<X,T> createTrendLineDataset(S seriesKey, T trendKey);

    /**
     * Adds a dataset listener to this dataset
     * @param listener  the dataset listener
     */
    void addDatasetListener(Listener listener);

    /**
     * Removes a dataset listener from this dataset
     * @param listener  the dataset listener
     */
    void removeDatasetListener(Listener listener);

    /**
     * Notifies all listeners that this dataset has changed
     */
    void fireDatasetChanged();


    /**
     * Returns a ChartDataset wrapper on the DataFrame provided
     * @param frameSupplier the function that supplies the DataFrame
     * @param <X>           the domain key type
     * @param <S>           the series key type
     * @return              the chart dataset adapter for the supplied DataFrame
     */
    static <X extends Comparable,S extends Comparable> ChartModel<X,S> of(Supplier<DataFrame<?,S>> frameSupplier) {
        return new DataFrameChartModel<>(Optional.empty(), frameSupplier);
    }

    /**
     * Returns a ChartDataset wrapper on the DataFrame provided
     * @param frameSupplier the function that supplies the DataFrame
     * @param domainAxisColumn  the column to use as domain axis
     * @param <X>           the domain key type
     * @param <S>           the series key type
     * @return              the chart dataset adapter for the supplied DataFrame
     */
    static <X extends Comparable,S extends Comparable> ChartModel<X,S> of(S domainAxisColumn, Supplier<DataFrame<?,S>> frameSupplier) {
        return new DataFrameChartModel<>(Optional.of(domainAxisColumn), frameSupplier);
    }


    /**
     * Combines multiple chart datasets into a signal dataset
     * @param models  the datasets to combine
     * @param <X>       the domain key type
     * @return          the combined dataset
     */
    @SuppressWarnings("unchecked")
    static <X extends Comparable,S extends Comparable> ChartModel<X,S> combine(List<ChartModel<X,S>> models) {
        if (models.size() == 1) {
            return models.iterator().next();
        } else {
            return new DataFrameChartModel<>(Optional.empty(), () -> {
                final Set<Class<X>> domainKeyTypeSet = models.stream().map(ChartModel::getDomainKeyType).collect(Collectors.toSet());
                final Set<Class<? extends Comparable>> seriesKeyTypeSet = models.stream().map(ChartModel::getSeriesKeyType).collect(Collectors.toSet());
                if (domainKeyTypeSet.size() > 1) {
                    throw new ChartException("Non-homogeneous key types for domain dimension: " + domainKeyTypeSet);
                } else if (seriesKeyTypeSet.size() > 1) {
                    throw new ChartException("Non-homogeneous key types for series dimension: " + domainKeyTypeSet);
                } else {
                    final Class<X> domainKeyType = domainKeyTypeSet.iterator().next();
                    final Class<S> seriesKeyType = (Class<S>)seriesKeyTypeSet.iterator().next();
                    final int rowCount = models.stream().mapToInt(ChartModel::getSize).max().orElse(0);
                    final int colCount = models.stream().mapToInt(ChartModel::getSeriesCount).sum();
                    final Index<X> rows = Index.of(domainKeyType, rowCount);
                    final Index<S> columns = Index.of(seriesKeyType, colCount);
                    final DataFrame<X,S> frame = DataFrame.of(rows, columns, Object.class);
                    for (ChartModel<X,S> model : models) {
                        final Iterable<X> domainKeys = model.getDomainKeys();
                        frame.rows().addAll(domainKeys);
                        for (int j=0; j<model.getSeriesCount(); ++j) {
                            final S seriesKey = model.getSeriesKey(j);
                            final Class<?> dataType = model.getSeriesDataType(seriesKey);
                            final int colOrdinal = frame.cols().add(seriesKey, dataType).ordinal();
                            for (int i=0; i<model.getSize(); ++i) {
                                final X domainKey = model.getDomainKey(i);
                                final Number value = model.getRangeValue(i, j);
                                frame.data().setValue(domainKey, colOrdinal, value);
                            }
                        }
                    }
                    return frame;
                }
            });
        }
    }


    /**
     * An callback interface to be notified of dataset changed
     */
    interface Listener {

        /**
         * Called to notify that a dataset has changed
         * @param dataset   the dataset reference
         */
        void onDatasetChanged(ChartModel<?,?> dataset);
    }


    /**
     * A convenience base class for build ChartDataset implementations
     * @param <X>   the domain key type
     * @param <S>   the series key type
     */
    abstract class ChartModelBase<X extends Comparable,S extends Comparable> implements ChartModel<X,S> {

        private ListenerList<Listener> listenerList = new ListenerList<>();

        @Override
        public void addDatasetListener(Listener listener) {
            this.listenerList.addListener(listener);
        }

        @Override
        public void removeDatasetListener(Listener listener) {
            this.listenerList.removeListener(listener);
        }

        @Override
        public void fireDatasetChanged() {
            this.listenerList.stream().forEach(listener -> listener.onDatasetChanged(this));
        }
    }


    /**
     * An implementation of ChartDataset that wraps a Morpheus DataFrame that is exposes via a Supplier
     * @param <X>   the domain key type
     * @param <S>   the series key type
     */
    class DataFrameChartModel<X extends Comparable,S extends Comparable> extends ChartModelBase<X,S> {

        private DataFrame<?,S> frame;
        private int domainAxisColIndex;
        private Optional<S> domainAxisColKey;
        private Array<Integer> colIndexes;
        private IntFunction<X> domainValueFunction;
        private Supplier<DataFrame<?,S>> frameSupplier;
        private Function<X,X> intervalFunction = (key) -> key;

        /**
         * Constructor
         * @param frameSupplier     the function that supplier the DataFrame to this model
         * @param domainAxisColKey  the optional column key to used as domain axis
         */
        @SuppressWarnings("unchecked")
        DataFrameChartModel(Optional<S> domainAxisColKey, Supplier<DataFrame<?,S>> frameSupplier) {
            this.frameSupplier = frameSupplier;
            if (domainAxisColKey.isPresent()) {
                this.apply(frameSupplier.get(), domainAxisColKey.get());
            } else {
                this.apply((DataFrame<X,S>)frameSupplier.get());
            }
        }

        @Override
        public ChartModel<X,S> withDomainInterval(Function<X,X> intervalFunction) {
            this.intervalFunction = intervalFunction;
            this.fireDatasetChanged();
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public ChartModel<X,S> apply(DataFrame<X,S> update) {
            if (frame != update) {
                try {
                    this.frame = update;
                    this.frameSupplier = () -> frame;
                    this.domainAxisColIndex = -1;
                    this.domainAxisColKey = Optional.empty();
                    this.domainValueFunction = rowIndex -> (X)frame.rows().key(rowIndex);
                    this.colIndexes = Range.of(0, frame.colCount()).toArray();
                } finally {
                    fireDatasetChanged();
                }
            }
            return this;
        }

        @Override
        public ChartModel<X,S> apply(DataFrame<?,S> update, S domainKey) {
            if (frame != update) {
                try {
                    this.frame = update;
                    this.frameSupplier = () -> frame;
                    this.domainAxisColKey = Optional.of(domainKey);
                    this.domainAxisColIndex = frame.cols().ordinalOf(domainAxisColKey.get());
                    this.domainValueFunction = rowIndex -> frame.data().getValue(rowIndex, domainAxisColIndex);
                    this.colIndexes = Array.of(IntStream.range(0, frame.colCount()).filter(i -> i != domainAxisColIndex).toArray());
                } finally {
                    fireDatasetChanged();
                }
            }
            return this;
        }

        @Override
        public int getSize() {
            return frame.rowCount();
        }

        @Override
        public final int getSeriesCount() {
            return colIndexes.length();
        }

        @Override
        public final Function<X,X> getDomainInterval() {
            return intervalFunction;
        }

        @Override
        @SuppressWarnings("unchecked")
        public final boolean contains(Comparable seriesKey) {
            return frame != null && frame.cols().contains((S)seriesKey);
        }

        @Override
        @SuppressWarnings("unchecked")
        public Class<X> getDomainKeyType() {
            if (frame == null) {
                return (Class)Number.class;
            } else if (!domainAxisColKey.isPresent()) {
                return (Class<X>)frame.rows().keyType();
            } else if (frame.rowCount() == 0) {
                return (Class)Number.class;
            } else {
                final X domainValue = frame.data().getValue(0, domainAxisColIndex);
                return (Class<X>)domainValue.getClass();
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public Class<S> getSeriesKeyType() {
            return frame != null ? frame.cols().keyType() : null;
        }

        @Override
        @SuppressWarnings("unchecked")
        public final S getSeriesKey(int seriesIndex) {
            return frame.cols().key(colIndexes.getInt(seriesIndex));
        }

        @Override
        public final X getDomainKey(int domainIndex) {
            return domainValueFunction.apply(domainIndex);
        }

        @Override
        public final Number getRangeValue(int domainIndex, int seriesIndex) {
            final int colIndex = colIndexes.getInt(seriesIndex);
            return frame.data().getValue(domainIndex, colIndex);
        }

        @Override
        public final Iterable<S> getSeriesKeys() {
            if (!domainAxisColKey.isPresent()) {
                return frame.cols().keyArray();
            } else {
                final Class<S> type = frame.cols().keyType();
                final Stream<S> seriesKeys = colIndexes.stream().ints().mapToObj(i -> frame.cols().key(i));
                return seriesKeys.collect(ArrayCollector.of(type, colIndexes.length()));
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public final Iterable<X> getDomainKeys() {
            return frame != null ? (Iterable<X>)frame.rows().keyArray() : Collections.emptyList();
        }

        @Override
        @SuppressWarnings("unchecked")
        public final Class<? extends Number> getSeriesDataType(S seriesKey) {
            return frame == null ? Double.class : (Class<Number>)frame.cols().type(seriesKey);
        }

        @Override
        @SuppressWarnings("unchecked")
        public final Optional<Bounds<X>> getDomainBounds() {
            if (frame == null) {
                return Optional.empty();
            } else if (domainAxisColKey.isPresent()) {
                final S colKey = domainAxisColKey.get();
                return frame.colAt(colKey).bounds();
            } else {
                int count = 0;
                X minValue = null;
                X maxValue = null;
                for (int i=0; i<frame.rowCount(); ++i) {
                    final X value = domainValueFunction.apply(i);
                    if (value != null) count ++;
                    minValue = minValue == null ? value : value == null ? minValue : minValue.compareTo(value) > 0 ? value : minValue;
                    maxValue = maxValue == null ? value : value == null ? maxValue : maxValue.compareTo(value) < 0 ? value : maxValue;
                }
                return count == 0 ? Optional.empty() : Optional.of(Bounds.of(minValue, maxValue));
            }
        }

        @Override
        public final Optional<Bounds<Number>> getSeriesBounds() {
            if (frame == null) {
                return Optional.empty();
            } else {
                int count = 0;
                Number minValue = null;
                Number maxValue = null;
                for (int i=0; i<frame.rowCount(); ++i) {
                    for (int colIndex : colIndexes) {
                        final Number value = frame.data().getValue(i, colIndex);
                        final double doubleValue = value != null ? value.doubleValue() : Double.NaN;
                        if (!Double.isNaN(doubleValue)) {
                            count++;
                            minValue = minValue == null ? value : Double.compare(doubleValue, minValue.doubleValue()) < 0 ? value : minValue;
                            maxValue = maxValue == null ? value : Double.compare(doubleValue, maxValue.doubleValue()) > 0 ? value : maxValue;
                        }
                    }
                }
                return count == 0 ? Optional.empty() : Optional.of(Bounds.of(minValue, maxValue));
            }
        }


        @Override
        public final Optional<Bounds<Number>> getSeriesBounds(S seriesKey) {
            return frame == null ? Optional.empty() : frame.colAt(seriesKey).bounds();
        }


        @Override()
        @SuppressWarnings("unchecked")
        public <T extends Comparable> ChartModel<X,T> createTrendLineDataset(S seriesKey, T trendKey) {
            return new DataFrameChartModel<>(Optional.empty(), () -> {
                final DataFrame<Integer,Object> seriesFrame = createSeriesDataFrame(seriesKey);
                final Optional<Bounds<Number>> regressorRange = seriesFrame.colAt("Regressor").bounds();
                if (!regressorRange.isPresent()) {
                    return DataFrame.empty();
                } else {
                    final double minValue = regressorRange.get().lower().doubleValue();
                    final double maxValue = regressorRange.get().upper().doubleValue();
                    final double step1 = ((maxValue - minValue)) / 20d;
                    final double step2 = ((maxValue - minValue)) / 10d;
                    final Array<Double> values = Range.of(minValue - step1, maxValue + step1 * 2d, step2).toArray();
                    return DataFrame.of(values, (Class<T>)trendKey.getClass(), columns -> {
                        seriesFrame.regress().ols(seriesKey, "Regressor", true, slr -> {
                            final double beta = slr.getBetaValue("Regressor", Field.PARAMETER);
                            final double intercept = slr.getInterceptValue(Field.PARAMETER);
                            final double r2 = slr.getRSquared();
                            columns.add(trendKey, Double.class).applyDoubles(v -> {
                                final double x = v.rowKey();
                                return beta * x + intercept;
                            });
                            return Optional.empty();
                        });
                    });
                }
            });
        }

        /**
         * Returns a newly created DataFrame representing the series specified
         * @param seriesKey     the series key
         * @return              the one series DataFrame
         */
        private DataFrame<Integer,Object> createSeriesDataFrame(S seriesKey) {
            final Range<Integer> rowRange = Range.of(0, frame.rowCount());
            final int seriesIndex = frame.cols().ordinalOf(seriesKey);
            return DataFrame.of(rowRange, Object.class, columns -> {
                columns.add("Regressor", Double.class).applyDoubles(v -> ((Number)getDomainKey(v.rowOrdinal())).doubleValue());
                columns.add(seriesKey, Double.class).applyDoubles(v -> frame.data().getDouble(v.rowOrdinal(), seriesIndex));
            });
        }
    }

}
