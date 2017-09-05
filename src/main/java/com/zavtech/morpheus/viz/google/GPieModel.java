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

import java.util.function.Consumer;

import com.zavtech.morpheus.viz.chart.pie.PieModelDefault;
import com.zavtech.morpheus.viz.js.JsCode;

/**
 * A PieModel implementation for the Morpheus Google Charts adapter.
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
class GPieModel<X extends Comparable,S extends Comparable> extends PieModelDefault<X,S> implements Consumer<JsCode> {

    /**
     * Constructor
     */
    GPieModel() {
        super();
    }


    @Override
    public void accept(JsCode script) {
        script.newArray(data -> {
            data.appendArray(false, header -> {
                header.appendObject(true, domain -> {
                    domain.newAttribute("id", "domain");
                    domain.newAttribute("label", "Domain");
                    domain.newAttribute("type", "string");
                });
                header.appendObject(true, domain -> {
                    domain.newAttribute("id", "domain");
                    domain.newAttribute("label", "Range");
                    domain.newAttribute("type", "number");
                });
            });
            if (!isEmpty()) {
                getFrame().rows().forEach(row -> {
                    final X label = getItemFunction().apply(row.ordinal());
                    final double value = getValueFunction().applyAsDouble(row.ordinal());
                    data.appendArray(true, values -> {
                        values.append(label.toString());
                        values.append(value);
                    });
                });
            }
        });
    }

}
