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
package com.zavtech.morpheus.viz.chart.pie;

import java.util.Collections;
import java.util.function.IntFunction;
import java.util.function.IntToDoubleFunction;
import java.util.stream.IntStream;

import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.frame.DataFrameColumn;

/**
 * A convenience base class for building PieModel implementations
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public class PieModelDefault<X extends Comparable,S extends Comparable> implements PieModel<X,S> {

    private DataFrame<?,S> frame;
    private IntFunction<X> itemFunction;
    private IntToDoubleFunction valueFunction;

    /**
     * Constructor
     */
    public PieModelDefault() {
        super();
    }

    /**
     * Returns the frame reference for model, which can be null
     * @return      the frame reference
     */
    public final DataFrame<?,S> getFrame() {
        return frame;
    }

    /**
     * Returns the item function for model
     * @return  the item function
     */
    public final IntFunction<X> getItemFunction() {
        return itemFunction;
    }

    /**
     * Returns the value function for model
     * @return  the value function
     */
    public final IntToDoubleFunction getValueFunction() {
        return valueFunction;
    }

    /**
     * Updates this dataset with the frame, data ordinal and label function
     * @param frame             the frame reference
     * @param itemFunction      the item function
     * @param valueFunction     the value function
     */
    private void update(DataFrame<?,S> frame, IntFunction<X> itemFunction, IntToDoubleFunction valueFunction) {
        if (frame == null) {
            clear(false);
        } else {
            this.frame = frame;
            this.itemFunction = itemFunction;
            this.valueFunction = valueFunction;
        }
    }


    @Override
    public Iterable<X> keys() {
        return () -> {
            if (isEmpty()) {
                return Collections.<X>emptyList().iterator();
            } else {
                return IntStream.range(0, frame.rowCount()).mapToObj(itemFunction).iterator();
            }
        };
    }


    @Override
    public final boolean isEmpty() {
        return frame == null || itemFunction == null;
    }


    @Override
    public void clear(boolean notify) {
        this.frame = null;
        this.itemFunction = null;
        this.valueFunction = null;
    }


    @Override
    public void apply(DataFrame<X,S> frame) {
        if (frame == null || frame.cols().stream().filter(DataFrameColumn::isNumeric).count() == 0) {
            clear(true);
        } else {
            frame.cols().first(DataFrameColumn::isNumeric).ifPresent(column -> {
                final int valueColOrdinal = column.ordinal();
                final IntFunction<X> itemFunction = ordinal -> frame.rows().key(ordinal);
                final IntToDoubleFunction valueFunction = ordinal -> frame.data().getDouble(ordinal, valueColOrdinal);
                this.update(frame, itemFunction, valueFunction);
            });
        }
    }


    @Override
    public void apply(DataFrame<X,S> frame, S valueKey) {
        if (frame == null) {
            clear(true);
        } else {
            final int valueColOrdinal = frame.cols().ordinalOf(valueKey);
            final IntFunction<X> itemFunction = ordinal -> frame.rows().key(ordinal);
            final IntToDoubleFunction valueFunction = ordinal -> frame.data().getDouble(ordinal, valueColOrdinal);
            this.update(frame, itemFunction, valueFunction);
        }
    }


    @Override
    public void apply(DataFrame<?,S> frame, S itemKey, S valueKey) {
        if (frame == null) {
            clear(true);
        } else {
            final int valueColOrdinal = frame.cols().ordinalOf(valueKey);
            final int itemColOrdinal = frame.cols().ordinalOf(itemKey);
            final IntFunction<X> itemFunction = ordinal -> frame.data().getValue(ordinal, itemColOrdinal);
            final IntToDoubleFunction valueFunction = ordinal -> frame.data().getDouble(ordinal, valueColOrdinal);
            this.update(frame, itemFunction, valueFunction);
        }
    }

}
