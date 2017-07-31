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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;

import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.index.Index;
import com.zavtech.morpheus.viz.chart.ChartException;
import com.zavtech.morpheus.viz.chart.xy.XyDataset;
import com.zavtech.morpheus.viz.chart.xy.XyModel;

/**
 * Class summary goes here...
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
class GXyModel<X extends Comparable,S extends Comparable> implements XyModel<X,S> {

    private GXyPlot<X> plot;
    private GXyDataset<X,S> unified;
    private Map<Integer,Integer> rangeAxisMap = new HashMap<>();
    private Map<Integer,GXyDataset<X,S>> datasetMap = new HashMap<>();


    /**
     * Constructor
     * @param plot  the plot for this model
     */
    GXyModel(GXyPlot<X> plot) {
        this.plot = plot;
    }


    /**
     * Resets the unified to force a rebuild next time requested
     */
    private void reset() {
        this.unified = null;
    }


    /**
     * Returns the first dataset index that contains the series
     * @param seriesKey the series key
     * @return  the dataset index
     */
    int getDatasetIndex(S seriesKey) {
        final OptionalInt maxIndex = datasetMap.keySet().stream().mapToInt(x -> x).max();
        if (!maxIndex.isPresent()) {
            throw new IllegalStateException("No datasets configure for chart model");
        } else {
            for (int index=0; index<100; ++index) {
                final GXyDataset<X,S> dataset = datasetMap.get(index);
                if (dataset != null && !dataset.isEmpty() && dataset.frame().cols().contains(seriesKey)) {
                    return index;
                }
            }
            throw new IllegalArgumentException("Unable to match series in chart data model: " + seriesKey);
        }
    }


    /**
     * Returns the range axis index for the series key
     * @param seriesKey the series key
     * @return          the axis index
     */
    int getRangeAxisIndex(S seriesKey) {
        final OptionalInt maxIndex = datasetMap.keySet().stream().mapToInt(x -> x).max();
        if (!maxIndex.isPresent()) {
            return 0;
        } else {
            for (int datasetIndex=0; datasetIndex<=maxIndex.getAsInt(); ++datasetIndex) {
                final GXyDataset<X,S> dataset = datasetMap.get(datasetIndex);
                if (dataset != null && dataset.contains(seriesKey)) {
                    return rangeAxisMap.getOrDefault(datasetIndex, 0);
                }
            }
            return 0;
        }
    }


    /**
     * Returns a unified XyDataset based on all the datasets in this model
     * @return      the unified dataset
     */
    @SuppressWarnings("unchecked")
    GXyDataset<X,S> getUnifiedDataset() {
        if (unified != null) {
            return unified;
        } else if (datasetMap.size() == 0) {
            this.unified = GXyDataset.of(() -> null);
        } else if (datasetMap.size() == 1) {
            this.unified = datasetMap.values().iterator().next();
        } else {
            this.unified = combine(datasetMap.values());
        }
        return unified;
    }


    /**
     * Combines multiple chart datasets into a single dataset
     * @param datasets  the datasets to combine
     * @return          the combined dataset
     */
    @SuppressWarnings("unchecked")
    private GXyDataset<X,S> combine(Collection<GXyDataset<X,S>> datasets) {
        return GXyDataset.of(() -> {
            final Set<Class<X>> domainKeyTypeSet = datasets.stream().map(GXyDataset::getDomainKeyType).collect(Collectors.toSet());
            final Set<Class<S>> seriesKeyTypeSet = datasets.stream().map(d -> d.frame().cols().keyType()).collect(Collectors.toSet());
            if (domainKeyTypeSet.size() > 1) {
                throw new ChartException("Non-homogeneous key types for domain dimension: " + domainKeyTypeSet);
            } else {
                final Class<X> domainKeyType = domainKeyTypeSet.iterator().next();
                final Class<S> seriesKeyType = seriesKeyTypeSet.size() > 1 ? (Class<S>)Comparable.class : seriesKeyTypeSet.iterator().next();
                final int rowCount = datasets.stream().mapToInt(GXyDataset::getDomainSize).max().orElse(0);
                final int colCount = datasets.stream().mapToInt(GXyDataset::getSeriesCount).sum();
                final Index<X> rows = Index.of(domainKeyType, rowCount);
                final Index<S> columns = Index.of(seriesKeyType, colCount);
                final DataFrame<X,S> frame = DataFrame.ofDoubles(rows, columns);
                datasets.forEach(dataset -> {
                    final Iterable<X> domainKeys = dataset.getDomainValues();
                    frame.rows().addAll(domainKeys);
                    for (int j=0; j<dataset.getSeriesCount(); ++j) {
                        final S seriesKey = dataset.getSeriesKey(j);
                        final int colOrdinal = frame.cols().add(seriesKey, Double.class).ordinal();
                        for (int i=0; i<dataset.getDomainSize(); ++i) {
                            final X domainKey = dataset.getDomainValue(i);
                            final double value = dataset.getRangeValue(i, j);
                            frame.data().setDouble(domainKey, colOrdinal, value);
                        }
                    }
                });
                return frame.rows().sort(true);
            }
        });
    }


    @Override
    public void setRangeAxis(int dataset, int axis) {
        this.rangeAxisMap.put(dataset, axis);
        this.plot.axes().range(axis);
    }


    @Override
    public Class<X> domainType() {
        if (datasetMap.isEmpty()) {
            return null;
        } else {
            return datasetMap.entrySet().iterator().next().getValue().domainType();
        }
    }


    @Override
    @SuppressWarnings("unchecked")
    public XyDataset<X,S> at(int index) {
        try {
            final XyDataset<X,S> dataset = datasetMap.get(index);
            if (dataset == null) {
                throw new ChartException("No dataset exists for index: " + 0);
            } else {
                return dataset;
            }
        } finally {
            reset();
        }
    }


    @Override
    public int add(DataFrame<X,S> frame) {
        try {
            final int index = datasetMap.size();
            final GXyDataset<X,S> dataset = GXyDataset.of(() -> frame);
            this.datasetMap.put(index, dataset);
            return index;
        } finally {
            reset();
        }
    }


    @Override
    public int add(DataFrame<?,S> frame, S domainKey) {
        try {
            final int index = datasetMap.size();
            final GXyDataset<X,S> dataset = GXyDataset.of(domainKey, () -> frame);
            this.datasetMap.put(index, dataset);
            return index;
        } finally {
            reset();
        }
    }


    @Override
    public XyDataset<X,S> update(int index, DataFrame<X,S> frame) {
        try {
            if (!datasetMap.containsKey(index)) {
                throw new ChartException("No dataset exist at index: " + index);
            } else {
                final GXyDataset<X,S> dataset = GXyDataset.of(() -> frame);
                this.datasetMap.put(index, dataset);
                return dataset;
            }
        } finally {
            reset();
        }
    }


    @Override
    public XyDataset<X,S> update(int index, DataFrame<?,S> frame, S domainKey) {
        try {
            if (!datasetMap.containsKey(index)) {
                throw new ChartException("No dataset exist at index: " + index);
            } else {
                final GXyDataset<X,S> dataset = GXyDataset.of(domainKey, () -> frame);
                this.datasetMap.put(index, dataset);
                return dataset;
            }
        } finally {
            reset();
        }
    }


    @Override
    public void removeAll() {
        try {
            this.datasetMap.clear();
        } finally {
            reset();
        }
    }


    @Override
    public void remove(int index) {
        try {
            if (!datasetMap.containsKey(index)) {
                throw new ChartException("No dataset exists for index: " + index);
            } else {
                datasetMap.remove(index);
            }
        } finally {
            reset();
        }
    }

}
