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
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Class summary goes here...
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
enum GDataType {

    BOOLEAN("boolean"),
    STRING("string"),
    NUMBER("number"),
    DATE("date"),
    DATETIME("datetime"),
    LOCAL_TIME("timeofday");

    private static final Map<Class,GDataType> typeMap = new HashMap<>();

    /**
     * Static initializer
     */
    static {
        typeMap.put(String.class, GDataType.STRING);
        typeMap.put(Boolean.class, GDataType.BOOLEAN);
        typeMap.put(String.class, GDataType.STRING);
        typeMap.put(Number.class, GDataType.NUMBER);
        typeMap.put(Double.class, GDataType.NUMBER);
        typeMap.put(Integer.class, GDataType.NUMBER);
        typeMap.put(Float.class, GDataType.NUMBER);
        typeMap.put(Long.class, GDataType.NUMBER);
        typeMap.put(Date.class, GDataType.DATETIME);
        typeMap.put(LocalDate.class, GDataType.DATE);
        typeMap.put(LocalDateTime.class, GDataType.DATETIME);
        typeMap.put(ZonedDateTime.class, GDataType.DATETIME);
        typeMap.put(LocalTime.class, GDataType.LOCAL_TIME);
    }

    private String label;

    /**
     * Constructor
     * @param label the label for this type
     */
    GDataType(String label) {
        this.label = label;
    }


    /**
     * Returns the data type for the class specified
     * @param type          the class instance
     * @param defaultType   the default type when no match
     * @return              the Google Chart data type
     */
    public static GDataType getDataType(Class<?> type, GDataType defaultType) {
        return typeMap.getOrDefault(type, defaultType);
    }


    /**
     * Returns the label for this type
     * @return  the label for this type
     */
    public String getLabel() {
        return label;
    }
}
