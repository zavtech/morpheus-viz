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

import com.zavtech.morpheus.util.Bounds;
import com.zavtech.morpheus.viz.chart.ChartFormat;
import com.zavtech.morpheus.viz.chart.ChartLabel;
import com.zavtech.morpheus.viz.chart.ChartTextStyle;
import com.zavtech.morpheus.viz.chart.xy.XyAxis;
import com.zavtech.morpheus.viz.js.JsObject;

/**
 * An implementation of XyAxis interface for Google Charts
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
class GXyAxis implements XyAxis {

    private GChartLabel label;
    private GChartFormat format;
    private GChartTextStyle ticks;
    private boolean log = false;

    /**
     * Constructor
     * @param domain true if this represents a domain axis
     */
    GXyAxis(boolean domain) {
        this.format = new GChartFormat(domain);
        this.label = new GChartLabel(Color.BLACK, new Font("Arial", Font.PLAIN, 12));
        this.ticks = new GChartTextStyle(Color.BLACK, new Font("Arial", Font.PLAIN, 12));
    }

    @Override
    public ChartLabel label() {
        return label;
    }

    @Override
    public ChartFormat format() {
        return format;
    }

    @Override
    public ChartTextStyle ticks() {
        return ticks;
    }

    @Override
    public XyAxis asLogScale() {
        this.log = true;
        return this;
    }

    @Override
    public XyAxis asLinearScale() {
        this.log = false;
        return this;
    }

    @Override
    public XyAxis asDateScale() {
        this.log = false;
        this.format.withPattern("dd-MMM-yyyy");
        return this;
    }

    @Override
    public XyAxis withRange(Bounds<?> range) {
        return this;
    }


    /**
     * Enriches the options of this axis on the JsObject provided.
     * @param axis      the axis JsObject to accept configuration to
     * @param dataset   the dataset for the plot
     */
    public void accept(JsObject axis, GXyDataset dataset) {
        axis.setIgnoreNulls(true);
        axis.newAttribute("baselineColor", ticks.label().getColorHex());
        axis.newAttribute("scaleType", this.log ? "log" : null);
        axis.newAttribute("textPosition", "out");
        axis.newAttribute("title", label.getText());
        axis.newAttribute("viewWindowMode", "maximized");
        axis.newAttribute("format", format.getPattern(dataset));
        axis.newObject("titleTextStyle", label);
        axis.newObject("textStyle", ticks);
        axis.newAttribute("gridLines", "");
    }
}
