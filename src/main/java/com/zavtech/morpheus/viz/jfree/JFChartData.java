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
import java.util.List;
import java.util.function.Function;

import com.zavtech.morpheus.viz.chart.ChartModel;
import com.zavtech.morpheus.index.Index;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.AbstractDataset;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;

/**
 * A factory that creates JFreeChart dataset wrappers around a Morpheus ChartDataset object.
 *
 * @author  Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
class JFChartData {

    /**
     * Returns a new XYDataset wrapping the Morpheus ChartDataset specified
     * @param model the model to wrap in an XYDataset
     * @return      the newly created XYDataset
     */
    static XYDataset ofXYSeries(ChartModel<Comparable,Comparable> model) {
        return new XYData(model);
    }

    /**
     * Returns a new CategoryDataset wrapping the Morpheus ChartDataset specified
     * @param model the model to wrap in a CategoryDataset
     * @return      the newly created CategoryDataset
     */
    static CategoryDataset ofCategories(ChartModel<?,?> model) {
        return new CategoryData<>(model);
    }


    /**
     * An implementation of an XYDataset that wraps a Morpheus ChartDataset
     */
    private static class XYData extends AbstractXYDataset implements IntervalXYDataset {

        private ChartModel<Comparable,Comparable> model;
        private Function<Object,Number> domainValueFunction;

        /**
         * Constructor
         * @param model   the model reference
         */
        private XYData(ChartModel<Comparable,Comparable> model) {
            this.model = model;
            this.domainValueFunction = createDomainValueFunction(model.getDomainKeyType());
            this.model.addDatasetListener(d -> fireDatasetChanged());
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
                return value -> value == null ? Double.NaN : ((LocalDate)value).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
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

        @Override
        public int getSeriesCount() {
            return model.getSeriesCount();
        }

        @Override
        public Comparable getSeriesKey(int series) {
            return model.getSeriesKey(series);
        }

        @Override
        public int getItemCount(int series) {
            return model.getSize();
        }

        @Override
        public Number getX(int series, int index) {
            final Object domainKey = model.getDomainKey(index);
            return domainValueFunction.apply(domainKey);
        }

        @Override
        public Number getY(int series, int index) {
            return model.getRangeValue(index, series);
        }

        @Override
        public Number getStartX(int series, int index) {
            final Object domainKey = model.getDomainKey(index);
            final Number value = domainValueFunction.apply(domainKey);
            return value != null ? value.doubleValue() : Double.NaN;
        }

        @Override
        public double getStartXValue(int series, int index) {
            final Object domainKey = model.getDomainKey(index);
            final Number value = domainValueFunction.apply(domainKey);
            return value != null ? value.doubleValue() : Double.NaN;
        }

        @Override
        public Number getEndX(int series, int index) {
            final Object domainKey = model.getDomainKey(index);
            final Function<Comparable,Comparable> function = model.getDomainInterval();
            if (function != null) {
                final Comparable endValueKey = function.apply((Comparable)domainKey);
                final Number endValue = domainValueFunction.apply(endValueKey);
                return endValue != null ? endValue.doubleValue() : Double.NaN;
            } else {
                final Number endValue = domainValueFunction.apply(domainKey);
                return endValue != null ? endValue.doubleValue() : Double.NaN;
            }
        }

        @Override
        public double getEndXValue(int series, int index) {
            final Object domainKey = model.getDomainKey(index);
            final Function<Comparable,Comparable> function = model.getDomainInterval();
            if (function != null) {
                final Comparable endValueKey = function.apply((Comparable)domainKey);
                final Number endValue = domainValueFunction.apply(endValueKey);
                return endValue != null ? endValue.doubleValue() : Double.NaN;
            } else {
                final Number endValue = domainValueFunction.apply(domainKey);
                return endValue != null ? endValue.doubleValue() : Double.NaN;
            }
        }

        @Override
        public Number getStartY(int series, int index) {
            return model.getRangeValue(index, series);
        }

        @Override
        public Number getEndY(int series, int index) {
            return model.getRangeValue(index, series);
        }

        @Override
        public double getStartYValue(int series, int index) {
            final Number value = model.getRangeValue(index, series);
            return value != null ? value.doubleValue() : Double.NaN;
        }

        @Override
        public double getEndYValue(int series, int index) {
            final Number value = model.getRangeValue(index, series);
            return value != null ? value.doubleValue() : Double.NaN;
        }
    }


    /**
     * An implementation of CategoryDataset that wraps a Morpheus ChartDataset
     */
    private static class CategoryData<X extends Comparable,S extends Comparable> extends AbstractDataset implements CategoryDataset {

        private Index<S> seriesKeys;
        private Index<X> domainKeys;
        private ChartModel<X,S> model;

        /**
         * Constructor
         * @param model   the model to wrap
         */
        private CategoryData(ChartModel<X,S> model) {
            this.model = model;
            this.model.addDatasetListener(data -> refresh());
            this.refresh();
        }

        void refresh() {
            this.seriesKeys = Index.of(model.getSeriesKeys());
            this.domainKeys = Index.of(model.getDomainKeys());
        }

        @Override
        public int getRowCount() {
            return seriesKeys.size();
        }

        @Override
        public int getColumnCount() {
            return domainKeys.size();
        }

        @Override
        public List getRowKeys() {
            return seriesKeys.toList();
        }

        @Override
        public List getColumnKeys() {
            return domainKeys.toList();
        }

        @Override
        public Comparable getRowKey(int rowIndex) {
            return seriesKeys.getKey(rowIndex);
        }

        @Override
        public Comparable getColumnKey(int colIndex) {
            return domainKeys.getKey(colIndex);
        }

        @Override
        @SuppressWarnings("unchecked")
        public int getRowIndex(Comparable rowKey) {
            return seriesKeys.getIndexForKey((S)rowKey);
        }

        @Override
        @SuppressWarnings("unchecked")
        public int getColumnIndex(Comparable colKey) {
            return domainKeys.getIndexForKey((X)colKey);
        }

        @Override
        @SuppressWarnings("unchecked")
        public Number getValue(Comparable rowKey, Comparable colKey) {
            final int colIndex = seriesKeys.getIndexForKey((S)rowKey);
            final int rowIndex = domainKeys.getIndexForKey((X)colKey);
            return model.getRangeValue(rowIndex, colIndex);
        }

        @Override
        public Number getValue(int rowIndex, int colIndex) {
            return model.getRangeValue(colIndex, rowIndex);
        }
    }
}
