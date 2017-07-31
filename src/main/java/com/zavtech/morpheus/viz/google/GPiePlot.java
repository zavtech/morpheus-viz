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
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntFunction;

import com.zavtech.morpheus.viz.chart.pie.PieModel;
import com.zavtech.morpheus.viz.chart.pie.PieLabels;
import com.zavtech.morpheus.viz.chart.pie.PiePlot;
import com.zavtech.morpheus.viz.chart.pie.PieSection;
import com.zavtech.morpheus.viz.js.JsObject;
import com.zavtech.morpheus.viz.util.ColorModel;

/**
 * An implementation of the PiePlot for the Google Chart Adapter.
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
class GPiePlot<X extends Comparable,S extends Comparable> implements PiePlot<X,S> {

    private boolean is3d;
    private double pieHole = 0d;
    private double startAngle = 0d;
    private GPieLabels labels = new GPieLabels();
    private GPieModel<X,S> model = new GPieModel<>();
    private ColorModel colorModel = ColorModel.DEFAULT.get();
    private Color sectionOutlineColor = Color.WHITE;
    private Map<X,GPieSection> sectionMap = new HashMap<>();

    /**
     * Constructor
     * @param is3d  true for 3d pie plot
     */
    GPiePlot(boolean is3d) {
        this.is3d = is3d;
    }


    @Override
    public PieModel<X,S> data() {
        return model;
    }


    @Override
    public PieLabels labels() {
        return labels;
    }


    @Override
    public PieSection section(X itemKey) {
        GPieSection section = sectionMap.get(itemKey);
        if (section == null) {
            section = new GPieSection();
            sectionMap.put(itemKey, section);
        }
        return section;
    }


    @Override
    public PiePlot<X,S> withStartAngle(double degrees) {
        this.startAngle = degrees;
        return this;
    }


    @Override
    public PiePlot<X,S> withPieHole(double percent) {
        this.pieHole = percent;
        return this;
    }


    @Override
    public PiePlot<X, S> withSectionOutlineColor(Color color) {
        this.sectionOutlineColor = color;
        return this;
    }


    /**
     * Enriches the JsObject with options for these axes
     * @param options   the options to enrich
     */
    @SuppressWarnings("unchecked")
    public void accept(JsObject options) {
        options.newAttribute("is3D", is3d);
        options.newAttribute("pieHole", pieHole);
        options.newAttribute("pieStartAngle", startAngle);
        options.newAttribute("pieSliceText", labels.labelType != null ? labels.labelType.value : "none");
        options.newAttribute("pieSliceBorderColor", ColorModel.toHexString(sectionOutlineColor));
        options.newObject("pieSliceTextStyle", textStyle -> {
            textStyle.newAttribute("color", ColorModel.toHexString(labels.color));
            textStyle.newAttribute("fontName", labels.font.getName());
            textStyle.newAttribute("fontSize", labels.font.getSize());
        });
        if (!model.isEmpty()) {
            final IntFunction<X> itemFunction = model.getItemFunction();
            options.newArray("slices", slices -> {
                model.getFrame().rows().forEach(row -> {
                    final X item = itemFunction.apply(row.ordinal());
                    final GPieSection section = (GPieSection)section(item);
                    final Color color = section.color != null ? section.color : colorModel.getColor(item);
                    slices.appendObject(slice -> {
                        slice.newAttribute("color", ColorModel.toHexString(color));
                        slice.newAttribute("offset", section.offset);
                    });
                });
            });
        }
    }


    /**
     * Implementation of PieSection
     */
    private class GPieSection implements PieSection {

        private Color color;
        private double offset = 0d;

        @Override
        public PieSection withColor(Color color) {
            this.color = color;
            return this;
        }

        @Override
        public PieSection withOffset(double offset) {
            this.offset = offset;
            return this;
        }
    }

    private enum LabelType {

        NAME("label"),
        VALUE("value"),
        PERCENT("percentage");

        private String value;

        LabelType(String value) {
            this.value = value;
        }
    }


    /**
     * Implementation of the PieLabels interface
     */
    private class GPieLabels implements PieLabels {

        private LabelType labelType = LabelType.PERCENT;
        private Color color = Color.WHITE;
        private Font font = new Font("Arial", Font.PLAIN, 12);

        @Override
        public PieLabels off() {
            this.labelType = null;
            return this;
        }

        @Override
        public PieLabels on() {
            this.labelType = labelType != null ? labelType : LabelType.PERCENT;
            return this;
        }

        @Override
        public PieLabels withName() {
            this.labelType = LabelType.NAME;
            return this;
        }

        @Override
        public PieLabels withValue() {
            this.labelType = LabelType.VALUE;
            return this;
        }

        @Override
        public PieLabels withPercent() {
            this.labelType = LabelType.PERCENT;
            return this;
        }

        @Override
        public PieLabels withFont(Font font) {
            this.font = font;
            return this;
        }

        @Override
        public PieLabels withTextColor(Color color) {
            this.color = color;
            return this;
        }

        @Override
        public PieLabels withBackgroundColor(Color color) {
            return this;
        }
    }
}
