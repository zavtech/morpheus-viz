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
 * A class used to output a Javascript array.
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public class JsArray {

    private int length;
    private boolean inline;
    private JsCode script;

    /**
     * Constructor
     * @param script    the script to write to
     * @param inline    true if array should be written in one line
     */
    JsArray(JsCode script, boolean inline) {
        this.script = script;
        this.inline = inline;
    }

    /**
     * Returns true if this array is using inline formatting
     * @return  true if array is using inline formatting
     */
    public boolean isInline() {
        return inline;
    }

    public JsArray append(boolean value) {
        if (length > 0) {
            this.script.write(",");
        }
        if (!inline) {
            this.script.newLine();
            this.script.write(String.valueOf(value));
            this.length++;
        } else {
            this.script.write(String.valueOf(value));
            this.length++;
        }
        return this;
    }

    public JsArray append(int value) {
        if (length > 0) {
            this.script.write(",");
        }
        if (!inline) {
            this.script.newLine();
            this.script.write(String.valueOf(value));
            this.length++;
        } else {
            this.script.write(String.valueOf(value));
            this.length++;
        }
        return this;
    }


    public JsArray append(double value) {
        if (length > 0) {
            this.script.write(",");
        }
        if (!inline) {
            this.script.newLine();
            this.script.write(String.valueOf(value));
            this.length++;
        } else {
            this.script.write(String.valueOf(value));
            this.length++;
        }
        return this;
    }

    public JsArray append(String value) {
        if (value == null || value.length() == 0) {
            return append("null", false);
        } else {
            return append(value, true);
        }
    }

    public JsArray append(String value, boolean quotes) {
        if (length > 0) {
            this.script.write(",");
        }
        if (!inline) {
            this.script.newLine();
            this.script.write(quotes ? "\"" : "");
            this.script.write(value);
            this.script.write(quotes ? "\"" : "");
            this.length++;
        } else {
            this.script.write(quotes ? "\"" : "");
            this.script.write(value);
            this.script.write(quotes ? "\"" : "");
            this.length++;
        }
        return this;
    }



    public JsArray appendObject(Consumer<JsObject> consumer) {
        return appendObject(false, consumer);
    }


    public JsArray appendObject(boolean inline, Consumer<JsObject> consumer) {
        if (length > 0) {
            this.script.write(",");
        }
        if (!isInline()) {
            this.script.newLine();
            this.script.indent(4);
        }
        this.script.write("{");
        if (!inline) {
            this.script.newLine();
            this.script.indent(4);
        }
        consumer.accept(new JsObject(script, inline));
        if (!inline) {
            this.script.unident(4);
            this.script.newLine();
        }
        this.script.write("}");
        if (!isInline()) {
            this.script.unident(4);
        }
        this.length++;
        return this;
    }


    public JsArray appendArray(boolean inline, Consumer<JsArray> consumer) {
        if (length > 0) {
            this.script.write(",");
        }
        this.script.newLine();
        this.script.indent(4);
        this.script.write("[");
        consumer.accept(new JsArray(script, inline));
        if (!inline) {
            this.script.newLine();
        }
        this.script.write("]");
        this.script.unident(4);
        this.length++;
        return this;
    }



}
