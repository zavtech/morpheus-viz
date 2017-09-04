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

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import org.jfree.data.general.AbstractDataset;
import org.jfree.data.general.PieDataset;

import com.zavtech.morpheus.array.ArrayBuilder;
import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.index.Index;
import com.zavtech.morpheus.viz.chart.pie.PieModel;
import com.zavtech.morpheus.viz.chart.pie.PieModelDefault;

/**
 * The PieModel implementation for JFreeChart based pie charts
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
class JFPieModel<X extends Comparable,S extends Comparable> extends AbstractDataset implements PieModel<X,S>, PieDataset {

    private Index<X> itemKeys = Index.empty();
    private PieModelDefault<X,S> model = new PieModelDefault<>();

    /**
     * Constructor
     */
    JFPieModel() {
        super();
    }

    /**
     * Updates the item keys for this model
     */
    private void updateItemKeys() {
        if (isEmpty()) {
            this.itemKeys = Index.empty();
        } else {
            final int size = model.getFrame().rowCount();
            final ArrayBuilder<X> builder = ArrayBuilder.of(size);
            IntStream.range(0, size).forEach(i -> builder.add(model.getItemFunction().apply(i)));
            this.itemKeys = Index.of(builder.toArray());
        }
    }


    @Override
    public Iterable<X> keys() {
        return model.keys();
    }


    @Override
    public final boolean isEmpty() {
        return model.isEmpty();
    }


    @Override
    public void clear(boolean notify) {
        this.model.clear(notify);
        if (notify) {
            fireDatasetChanged();
        }
    }


    @Override
    public void apply(DataFrame<X,S> frame) {
        this.model.apply(frame);
        this.updateItemKeys();
    }


    @Override
    public void apply(DataFrame<X,S> frame, S valueKey) {
        this.model.apply(frame, valueKey);
        this.updateItemKeys();
    }


    @Override
    public void apply(DataFrame<?,S> frame, S itemKey, S valueKey) {
        this.model.apply(frame, itemKey, valueKey);
        this.updateItemKeys();
    }


    @Override
    public final int getItemCount() {
        return isEmpty() ? 0 : model.getFrame().rowCount();
    }


    @Override
    public final List getKeys() {
        return isEmpty() ? Collections.emptyList() : itemKeys.toList();
    }


    @Override
    public final Comparable getKey(int index) {
        return isEmpty() ? null : (Comparable)itemKeys.getKey(index);
    }


    @Override
    @SuppressWarnings("unchecked")
    public final int getIndex(Comparable itemKey) {
        return isEmpty() ? -1 : itemKeys.getOrdinalForKey((X)itemKey);
    }


    @Override
    @SuppressWarnings("unchecked")
    public final Number getValue(Comparable itemKey) {
        if (isEmpty()) {
            return null;
        } else {
            final int ordinal = itemKeys.getIndexForKey((X)itemKey);
            return model.getValueFunction().applyAsDouble(ordinal);
        }
    }


    @Override
    public final Number getValue(int index) {
        if (isEmpty()) {
            return null;
        } else {
            return model.getValueFunction().applyAsDouble(index);
        }
    }
}