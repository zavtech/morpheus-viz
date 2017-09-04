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
package com.zavtech.morpheus.viz.jfree;

import java.awt.*;
import java.text.AttributedString;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.plot.RingPlot;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.general.PieDataset;
import org.jfree.ui.RectangleInsets;

import com.zavtech.morpheus.viz.chart.pie.PieModel;
import com.zavtech.morpheus.viz.chart.pie.PieLabels;
import com.zavtech.morpheus.viz.chart.pie.PiePlot;
import com.zavtech.morpheus.viz.chart.pie.PieSection;
import com.zavtech.morpheus.viz.html.HtmlWriter;
import com.zavtech.morpheus.viz.util.ColorModel;

/**
 * An implementation of the PiePlot interface against JFreeChart.
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
class JFPiePlot<X extends Comparable,S extends Comparable> implements PiePlot<X,S> {

    private ColorModel colorModel;
    private org.jfree.chart.plot.PiePlot plot;
    private Color sectionOutlineColor = Color.WHITE;
    private Stroke sectionOutlineStroke = new BasicStroke(1f);
    private JFPieModel<X,S> model = new JFPieModel<>();
    private Map<X,PieSection> sectionMap = new HashMap<>();
    private MorpheusPieLabels labels = new MorpheusPieLabels();


    /**
     * Constructor
     * @param is3d  true for a 3D Pie Plot
     */
    JFPiePlot(boolean is3d) {
        this.plot = is3d ? new MorpheusPiePlot3D() : new MorpheusPiePlot2D();
        this.plot.setDataset(model);
        this.colorModel = ColorModel.DEFAULT.get();
        this.plot.setSectionOutlinesVisible(true);
        this.plot.setOutlineVisible(false);
        this.plot.setIgnoreNullValues(true);
        this.plot.setIgnoreZeroValues(true);
        this.plot.setAutoPopulateSectionPaint(true);
        this.plot.setBaseSectionOutlinePaint(sectionOutlineColor);
        this.plot.setBaseSectionOutlineStroke(sectionOutlineStroke);
        this.plot.setLabelPadding(new RectangleInsets(2, 10, 2, 10));
        this.plot.setInteriorGap(0.02);
        this.plot.setStartAngle(0d);
        this.plot.setSimpleLabels(true);
        this.plot.setToolTipGenerator(this::toolTip);
        this.plot.setStartAngle(90);
        this.labels().on().withPercent();
        if (plot instanceof RingPlot) {
            ((RingPlot)plot).setSectionDepth(1d);
            ((RingPlot)plot).setSeparatorsVisible(false);
        } else if (plot instanceof PiePlot3D) {
            ((PiePlot3D)plot).setDepthFactor(0.2d);
        }
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
        PieSection section = sectionMap.get(itemKey);
        if (section == null) {
            section = new MorpheusPieSection(itemKey);
            sectionMap.put(itemKey, section);
        }
        return section;
    }


    @Override
    public PiePlot<X,S> withStartAngle(double degrees) {
        this.plot.setStartAngle(90 - degrees);
        return this;
    }


    @Override
    public PiePlot<X,S> withPieHole(double percent) {
        if (plot instanceof RingPlot) {
            ((RingPlot)plot).setSectionDepth(1d - percent);
        }
        return this;
    }


    @Override
    public PiePlot<X,S> withSectionOutlineColor(Color color) {
        this.sectionOutlineColor = color;
        this.plot.setBaseSectionOutlinePaint(color);
        return this;
    }


    /**
     * Toggles the selection for the item specified
     * @param itemKey   the section key
     */
    void toggle(Comparable itemKey) {
        //todo; add selection logic to mimic Google chart behaviour
    }


    /**
     * Highlights a section by adjusting the outline for that section
     * @param itemKey   the item key
     */
    void highlight(Comparable itemKey) {
        try {
            this.model.keys().forEach(key -> {
                if (itemKey.equals(key)) {
                    plot.setSectionOutlineStroke(key, new BasicStroke(1.5f));
                    plot.setSectionOutlinePaint(key, Color.BLACK);
                } else {
                    plot.setSectionOutlineStroke(key, sectionOutlineStroke);
                    plot.setSectionOutlinePaint(key, sectionOutlineColor);
                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Returns the tooltip for the section key
     * @param dataset   the dataset
     * @param key       the section key
     * @return          the tooltip, null for none
     */
    private String toolTip(PieDataset dataset, Comparable key) {
        try {
            final double value = Optional.ofNullable(dataset.getValue(key)).map(Number::doubleValue).orElse(Double.NaN);
            if (!Double.isNaN(value)) {
                return HtmlWriter.createHtml(writer -> {
                    writer.newElement("html", html -> {
                        final double total = DatasetUtilities.calculatePieDatasetTotal(dataset);
                        final double percent = value / total;
                        final StringBuilder text = new StringBuilder();
                        text.append(labels.valueFormat.format(value));
                        text.append("  (");
                        text.append(labels.percentFormat.format(percent));
                        text.append(")");
                        html.newElement("h2", h2 -> h2.text(key.toString()));
                        html.newElement("h3", h3 -> h3.text(text.toString()));

                    });
                });
            } else {
                return null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }


    /**
     * Returns a reference to the underlying JFreeChart Plot object
     * @return      the underlying plot object
     */
    org.jfree.chart.plot.PiePlot underlying() {
        return plot;
    }


    private enum LabelType {
        NAME,
        VALUE,
        PERCENT
    }


    /**
     * An implementation of the PieLabels interface for JFreeChart
     */
    private class MorpheusPieLabels implements PieLabels {

        private LabelType labelType = LabelType.PERCENT;
        private DecimalFormat valueFormat = new DecimalFormat("###,##0.##;-###,##0.##");
        private DecimalFormat percentFormat = new DecimalFormat("0.##'%';-0.##'%'");

        /**
         * Constructor
         */
        MorpheusPieLabels() {
            this.percentFormat.setMultiplier(100);
        }

        @Override
        public PieLabels off() {
            plot.setLabelGenerator(null);
            return this;
        }

        @Override
        public PieLabels on() {
            plot.setLabelGenerator(new MorpheusPieSectionLabelGenerator());
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
            plot.setLabelFont(font);
            return this;
        }

        @Override
        public PieLabels withTextColor(Color color) {
            plot.setLabelPaint(color);
            return this;
        }

        @Override
        public PieLabels withBackgroundColor(Color color) {
            plot.setLabelBackgroundPaint(color);
            return this;
        }
    }



    /**
     * An implementation of the PieSection interface for JFreeChart
     */
    private class MorpheusPieSection implements PieSection {

        private X itemKey;
        private Color color;

        /**
         * Constructor
         * @param itemKey   the key for this section
         */
        MorpheusPieSection(X itemKey) {
            this.itemKey = itemKey;
        }

        @Override
        public PieSection withColor(Color color) {
            this.color = color;
            return this;
        }

        @Override
        public PieSection withOffset(double offset) {
            plot.setExplodePercent(itemKey, offset);
            return this;
        }
    }


    /**
     * An extension of JFreeChart PiePlot with Morpheus customizations
     */
    private class MorpheusPiePlot2D extends org.jfree.chart.plot.RingPlot {

        @Override
        protected Paint lookupSectionPaint(Comparable key, boolean autoPopulate) {
            return getColor(key);
        }

        @Override()
        public Paint getSectionPaint(Comparable key) {
            return getColor(key);
        }

        /**
         * Returns the Pie section color for key
         * @param key   the key for pie section
         * @return      the section color
         */
        @SuppressWarnings("unchecked")
        private Paint getColor(Comparable key) {
            try {
                final PieDataset dataset = getDataset();
                if (dataset != null) {
                    final MorpheusPieSection section = (MorpheusPieSection)section((X)key);
                    return section.color != null ? section.color : colorModel.getColor(key);
                }
                return super.getSectionPaint(key);
            } catch (Exception ex) {
                ex.printStackTrace();
                return super.getSectionPaint(key);
            }
        }
    }


    /**
     * An extension of JFreeChart PiePlot with Morpheus customizations
     */
    private class MorpheusPiePlot3D extends org.jfree.chart.plot.PiePlot3D {

        @Override
        protected Paint lookupSectionPaint(Comparable key, boolean autoPopulate) {
            return getColor(key);
        }

        @Override()
        public Paint getSectionPaint(Comparable key) {
            return getColor(key);
        }

        /**
         * Returns the Pie section color for key
         * @param key   the key for pie section
         * @return      the section color
         */
        @SuppressWarnings("unchecked")
        private Paint getColor(Comparable key) {
            try {
                final PieDataset dataset = getDataset();
                if (dataset != null) {
                    final MorpheusPieSection section = (MorpheusPieSection)section((X)key);
                    return section.color != null ? section.color : colorModel.getColor(key);
                }
                return super.getSectionPaint(key);
            } catch (Exception ex) {
                ex.printStackTrace();
                return super.getSectionPaint(key);
            }
        }
    }



    /**
     * An implementation of the PieSectionLabelGenerator to label pie sections
     */
    private class MorpheusPieSectionLabelGenerator implements PieSectionLabelGenerator {

        @Override
        public String generateSectionLabel(PieDataset dataset, Comparable key) {
            try {
                if (labels.labelType == LabelType.NAME) {
                    return key.toString();
                } else if (labels.labelType == LabelType.VALUE) {
                    final double value = Optional.ofNullable(dataset.getValue(key)).map(Number::doubleValue).orElse(Double.NaN);
                    return Double.isNaN(value) ? "" : labels.valueFormat.format(value);
                } else if (labels.labelType == LabelType.PERCENT) {
                    final double value = Optional.ofNullable(dataset.getValue(key)).map(Number::doubleValue).orElse(Double.NaN);
                    final double total = DatasetUtilities.calculatePieDatasetTotal(dataset);
                    final double percent = value / total;
                    return labels.percentFormat.format(percent);
                } else {
                    return null;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }


        @Override
        public AttributedString generateAttributedSectionLabel(PieDataset dataset, Comparable key) {
            return null;
        }
    }

}
