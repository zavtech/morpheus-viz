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
package com.zavtech.morpheus.viz.html;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import com.zavtech.morpheus.util.Collect;

/**
 * A class that defines a convenience API for writing out element content using an HtmlWriter.
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public class HtmlElement {

    private static final Set<String> newLineSet = new HashSet<>();
    private static final Set<String> noIndentSet = new HashSet<>();

    private String name;
    private boolean attrs;
    private HtmlCode writer;
    private boolean mixedContent;

    /**
     * Static initializer
     */
    static {
        newLineSet.addAll(Collect.asSet(
            "html", "body", "head", "script", "div", "p", "h1", "h2", "h3", "h4", "input", "dl", "ol", "area", "canvas",
            "code", "table", "tr", "td", "thead", "form", "hr", "meta", "nav", "ol", "option", "pre", "section", "select",
            "tbody", "textarea", "tfoot", "th", "title"
        ));
    }


    /**
     * Static initializer
     */
    static {
        noIndentSet.add("html");
        noIndentSet.add("body");
        noIndentSet.add("img");
        noIndentSet.add("span");
    }


    /**
     * Constructor
     * @param name      the name for this element
     * @param writer    the writer
     */
    HtmlElement(String name, HtmlCode writer) {
        this.name = name;
        this.attrs = true;
        this.writer = writer;
        if (newLineSet.contains(name.toLowerCase())) {
            this.writer.newLine();
        }
        if (!noIndentSet.contains(name.toLowerCase())) {
            this.writer.indent(4);
        }
        this.writer.write("<%s", name);
    }


    /**
     * Writes text content to the body of this element
     * @param text      the text content to write
     * @return          this element
     */
    public HtmlElement text(String text) {
        if (attrs) {
            this.writer.write(">");
            this.attrs = false;
        }
        final String[] lines = text.split("\n");
        if (lines.length > 1) {
            this.mixedContent = true;
            this.writer.indent(4);
            for (String line : lines) {
                this.writer.newLine();
                this.writer.write(line);
            }
            this.writer.unident(4);
        } else {
            this.writer.write(text);
        }
        return this;
    }


    /**
     * Writes a new attribute key and value for the current element
     * @param name      the attribute name
     * @param value     the attribute value
     * @return          this element
     */
    public HtmlElement newAttribute(String name, String value) {
        if (!attrs) {
            throw new IllegalStateException("The html element named " + name + " has already been closed");
        } else {
            this.writer.write(" %s=\"%s\"", name, value);
            return this;
        }
    }

    /**
     * Writes a new child element content in this element
     * @param name      the child element name
     * @param consumer  the consumer to populate the child element
     * @return          this element
     */
    public HtmlElement newElement(String name, Consumer<HtmlElement> consumer) {
        if (attrs) {
            this.writer.write(">");
            this.attrs = false;
        }
        this.mixedContent = true;
        final HtmlElement element = new HtmlElement(name, writer);
        consumer.accept(element);
        element.close();
        return this;
    }


    /**
     * Closes this element after all attributes and content has been written
     * @return  this element
     */
    HtmlElement close() {
        this.writer.write(!mixedContent ? ">" : "");
        if (mixedContent && newLineSet.contains(name.toLowerCase())) {
            this.writer.newLine();
        }
        this.writer.write("</%s>", name);
        if (!noIndentSet.contains(name.toLowerCase())) {
            this.writer.unident(4);
        }
        return this;
    }

}
