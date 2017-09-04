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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * A convenience API for programmatically creating simple HTML pages in the absence of a templating engine such as Freemarker.
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public class HtmlWriter {

    private String indent = "";
    private StringBuilder html = new StringBuilder();

    /**
     * Constructor
     */
    public HtmlWriter() {
        super();
    }

    /**
     * Convenience method to generate an HTML string given a writer
     * @param consumer  the consumer to call on the writer
     * @return          the resulting html string
     */
    public static String createHtml(Consumer<HtmlWriter> consumer) {
        final HtmlWriter writer = new HtmlWriter();
        consumer.accept(writer);
        return writer.toString().trim();
    }


    /**
     * Increases the indentation for the current line by a certain number of spaces
     * @param count     the number of spaces to indent by
     * @return          this writer
     */
    public HtmlWriter indent(int count) {
        final StringBuilder indentation = new StringBuilder(indent);
        for (int i=0; i<count; ++i) {
            indentation.append(" ");
            this.html.append(" ");
        }
        this.indent = indentation.toString();
        return this;
    }


    /**
     * Removes spaces from the current indentation
     * @param count the number of spaces to remove
     * @return      this code refernce
     */
    public HtmlWriter unident(int count) {
        this.indent = indent.substring(0, indent.length()-count);
        return this;
    }


    /**
     * Starts a new line and indents the new line based on current indentation
     * @return      this code reference
     */
    public HtmlWriter newLine() {
        this.html.append("\n");
        this.html.append(indent);
        return this;
    }

    /**
     * Writes formatted code string to the output buffer
     * @param line  the formatted code String (see String.format() for details)
     * @param args  the arguments for the formatted code string
     * @return      this writer
     */
    public HtmlWriter write(String line, Object... args) {
        if (args == null || args.length == 0) {
            this.html.append(line);
            return this;
        } else {
            this.html.append(String.format(line, args));
            return this;
        }
    }


    /**
     * Creates a new element with the name specified
     * @param name      the HTML element name
     * @param consumer  the consumer used to configure the element
     * @return          this writer
     */
    public HtmlWriter newElement(String name, Consumer<HtmlElement> consumer) {
        final HtmlElement element = new HtmlElement(name, this);
        consumer.accept(element);
        element.close();
        return this;
    }

    /**
     * Flushes the contents of the buffer to a file
     * @param file      the file to flush the HTML buffer to
     * @throws IOException  if there is an IO Exception
     */
    public void flush(File file) throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
            bos.write(html.toString().getBytes());
        }
    }


    @Override
    public String toString() {
        return html.toString();
    }

    /**
     * Quick test to check output
     * @param args
     */
    public static void main(String[] args) {
        final HtmlWriter writer = new HtmlWriter();
        writer.newElement("html", html -> {
            html.newElement("head", head -> {
                head.newElement("title", title -> title.text("This is a test page"));
                head.newElement("script", script -> {
                    script.newAttribute("type", "text/javascript");
                    script.newAttribute("src", "https://www.gstatic.com/charts/loader.js");
                });
                head.newElement("script", script -> {
                    script.newAttribute("type", "text/javascript");
                    script.text("var x = 20;\nvar y = 45;\nvar z = 2323;");
                });
            });
            html.newElement("body", body -> {
                body.newElement("p", p -> p.text("Hello World!"));
                body.newElement("table", table -> {
                    table.newElement("thead", head -> {
                        head.newElement("tr", row -> {
                            row.newElement("td", td -> td.text("Column-0"));
                            row.newElement("td", td -> td.text("Column-1"));
                            row.newElement("td", td -> td.text("Column-2"));
                            row.newElement("td", td -> td.text("Column-3"));
                            row.newElement("td", td -> td.text("Column-4"));
                        });
                    });
                    for (int i=0; i<10; ++i) {
                        table.newElement("tr", row -> {
                            row.newElement("td", td -> td.text(String.valueOf(Math.random())));
                            row.newElement("td", td -> td.text(String.valueOf(Math.random())));
                            row.newElement("td", td -> td.text(String.valueOf(Math.random())));
                            row.newElement("td", td -> td.text(String.valueOf(Math.random())));
                            row.newElement("td", td -> td.text(String.valueOf(Math.random())));
                        });
                    }
                });
            });
        });
        System.out.println(writer.toString());
    }

}
