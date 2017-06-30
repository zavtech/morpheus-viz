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
package com.zavtech.morpheus.viz.table;

/**
 * An interface to an object that implements some conditional logic that can return arbitrary values depending on the input.
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public interface DataFrameCellProperty<I,O> {

    /**
     * Returns the property value based on the argument
     * @param value     the value to condition the property on
     * @return          the conditional value for property (null permitted)
     * @throws IllegalArgumentException     if the argument is not support
     */
    public O getValue(I value) throws IllegalArgumentException;
}
