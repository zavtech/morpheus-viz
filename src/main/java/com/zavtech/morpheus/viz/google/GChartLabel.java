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

import java.awt.*;
import java.util.function.Consumer;

import com.zavtech.morpheus.viz.chart.ChartLabel;
import com.zavtech.morpheus.viz.util.ColorModel;
import com.zavtech.morpheus.viz.js.JsObject;

/**
 * Class summary goes here...
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
class GChartLabel implements ChartLabel, Consumer<JsObject> {

    private Font font;
    private Color color;
    private String text;


    /**
     * Constructor
     */
    GChartLabel() {
        this(Color.BLACK, new Font("Arial", Font.PLAIN, 12));
    }

    /**
     * Constructor
     * @param color     the color for label
     * @param font      the font for label
     */
    GChartLabel(Color color, Font font) {
        this.color = color;
        this.font = font;
    }

    /**
     * Returns the font for label
     * @return      the font
     */
    Font getFont() {
        return font;
    }

    /**
     * Returns the text color for label
     * @return      the text color
     */
    Color getColor() {
        return color;
    }

    /**
     * Returns the text for labe;
     * @return      the text for label
     */
    String getText() {
        return text;
    }

    /**
     * Returns true if the font is bold
     * @return  true if bold
     */
    boolean isBold() {
        return font != null && font.isBold();
    }

    /**
     * Returns true if font is italic
     * @return  true if font is italic
     */
    boolean isItalic() {
        return font != null && font.isItalic();
    }

    /**
     * Returns the value of the text color as a hex string
     * @return      the hex value for color
     */
    String getColorHex() {
        return ColorModel.toHexString(color);
    }

    @Override
    public ChartLabel withText(String text) {
        this.text = text;
        return this;
    }

    @Override
    public ChartLabel withColor(Color color) {
        this.color = color;
        return this;
    }

    @Override
    public ChartLabel withFont(Font font) {
        this.font = font;
        return this;
    }

    @Override
    public void accept(JsObject label) {
        label.newAttribute("color", this.getColorHex());
        label.newAttribute("fontName", getFont().getName());
        label.newAttribute("fontSize", getFont().getSize());
        label.newAttribute("bold", getFont().isBold());
        label.newAttribute("italic", getFont().isItalic());
    }

}
