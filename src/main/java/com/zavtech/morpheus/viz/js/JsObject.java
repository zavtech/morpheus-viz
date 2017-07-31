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
package com.zavtech.morpheus.viz.js;

import java.util.function.Consumer;

/**
 * A class that can be used to programmatically create a Javascript object that can be pretty printed
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public class JsObject {

    private Javascript script;
    private boolean inline;
    private int attributeCount = 0;
    private boolean ignoreNulls = false;

    /**
     * Constructor
     * @param script    the script this object will write to
     * @param inline    true to print this object on one line
     */
    JsObject(Javascript script, boolean inline) {
        this.script = script;
        this.inline = inline;
    }

    /**
     * Sets whether this object will ignore attempts to set null attributes
     * @param ignoreNulls   true to ignore null attributes
     */
    public void setIgnoreNulls(boolean ignoreNulls) {
        this.ignoreNulls = ignoreNulls;
    }

    /**
     * Creates a new attribute with the key value provided
     * @param template  the string template to output key / value pair
     * @param key       the key
     * @param value     the value
     * @return          this object
     */
    private JsObject newAttribute(String template, Object key, Object value) {
        if (ignoreNulls && value == null) {
            return this;
        } else {
            if (attributeCount > 0) {
                this.script.write(",");
                if (!inline) {
                    this.script.newLine();
                } else {
                    this.script.write(" ");
                }
            }
            this.script.write(template, key, value);
            this.attributeCount++;
            return this;
        }
    }


    private JsObject createObject(Object key, Consumer<JsObject> consumer) {
        if (attributeCount > 0) {
            this.script.write(",");
            this.script.newLine();
        }
        this.script.write("%s: ", key);
        this.script.newObject(consumer);
        this.attributeCount++;
        return this;
    }


    private JsObject createArray(Object key, boolean inline, Consumer<JsArray> consumer) {
        if (attributeCount > 0) {
            this.script.write(",");
            this.script.newLine();
        }
        this.script.write("%s: ", key);
        this.script.newArray(inline, consumer);
        this.attributeCount++;
        return this;
    }


    /**
     * Adds an attribute value for the key provided
     * @param key       the attribute key
     * @param value     the attribute value
     * @return          this object
     */
    public JsObject newAttribute(Object key, Boolean value) {
        return newAttribute("%s: %s", key, value);
    }

    /**
     * Adds an attribute value for the key provided
     * @param key       the attribute key
     * @param value     the attribute value
     * @return          this object
     */
    public JsObject newAttribute(Object key, Integer value) {
        return newAttribute("%s: %s", key, value);
    }

    /**
     * Adds an attribute value for the key provided
     * @param key       the attribute key
     * @param value     the attribute value
     * @return          this object
     */
    public JsObject newAttribute(Object key, Float value) {
        return newAttribute("%s: %s", key, value);
    }

    /**
     * Adds an attribute value for the key provided
     * @param key       the attribute key
     * @param value     the attribute value
     * @return          this object
     */
    public JsObject newAttribute(Object key, Double value) {
        return newAttribute("%s: %s", key, value);
    }

    /**
     * Adds an attribute value for the key provided
     * @param key       the attribute key
     * @param value     the attribute value
     * @return          this object
     */
    public JsObject newAttribute(Object key, String value) {
        return newAttribute("%s: \"%s\"", key, value);
    }

    /**
     * Adds an object attribute with the key specified
     * @param key       the attribute key
     * @param consumer  the consumer to configure object
     * @return          this object
     */
    public JsObject newObject(Object key, Consumer<JsObject> consumer) {
        return createObject(key, consumer);
    }

    /**
     * Adds an array attribute with the key specified
     * @param key       the attribute key
     * @param consumer  the consumer to populate the array
     * @return          this object
     */
    public JsObject newArray(Object key, Consumer<JsArray> consumer) {
        return createArray(key, false, consumer);
    }

    /**
     * Adds an array attribute with the key specified
     * @param key       the attribute key
     * @param inline    true if array should be written on one line
     * @param consumer  the consumer to populate the array
     * @return          this object
     */
    public JsObject newArray(Object key, boolean inline, Consumer<JsArray> consumer) {
        return createArray(key, inline, consumer);
    }

}
