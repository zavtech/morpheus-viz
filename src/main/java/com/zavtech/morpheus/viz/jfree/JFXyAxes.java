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
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;

import com.zavtech.morpheus.util.Bounds;
import com.zavtech.morpheus.viz.chart.ChartException;
import com.zavtech.morpheus.viz.chart.ChartFormat;
import com.zavtech.morpheus.viz.chart.ChartLabel;
import com.zavtech.morpheus.viz.chart.ChartTextStyle;
import com.zavtech.morpheus.viz.chart.xy.XyAxes;
import com.zavtech.morpheus.viz.chart.xy.XyAxis;

/**
 * The XyAxes implementation for xy plots using JFreeChart.
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
class JFXyAxes implements XyAxes {

    private Plot plot;

    /**
     * Constructopr
     * @param plot  the plot reference
     */
    JFXyAxes(Plot plot) {
        this.plot = plot;
    }


    @Override
    public XyAxis domain() {
        if (plot instanceof XYPlot) {
            return new AxisAdapter(((XYPlot)plot).getDomainAxis(), 0, true);
        } else if (plot instanceof CategoryPlot) {
            return new AxisAdapter(((CategoryPlot)plot).getDomainAxis(), 0, true);
        } else {
            throw new ChartException("Unsupported plot type for this adapter: " + plot);
        }
    }


    @Override
    public XyAxis range(int index) {
        final Axis axis = getRangeAxis(index);
        if (axis != null) {
            return new AxisAdapter(axis, index, false);
        } else {
            final ValueAxis newAxis = new NumberAxis();
            if (plot instanceof XYPlot) {
                ((XYPlot)plot).setRangeAxis(index, newAxis);
                return new AxisAdapter(newAxis, index, false);
            } else if (plot instanceof CategoryPlot) {
                ((CategoryPlot)plot).setRangeAxis(index, newAxis);
                return new AxisAdapter(newAxis, index, false);
            } else {
                throw new ChartException("Unsupported plot type for this adapter: " + plot);
            }
        }
    }


    /**
     * Returns the range axis for the index specified
     * @param index     the index for range axis
     * @return          the range axis, can be null.
     */
    private Axis getRangeAxis(int index) {
        if (plot instanceof XYPlot) {
            return ((XYPlot)plot).getRangeAxis(index);
        } else if (plot instanceof CategoryPlot) {
            return ((CategoryPlot)plot).getRangeAxis(index);
        } else {
            throw new ChartException("Unsupported plot type for this adapter: " + plot);
        }
    }



    /**
     * An XyAxis adapter for JFreeChart
     */
    private class AxisAdapter implements XyAxis {

        private Axis axis;
        private int index;
        private boolean domain;
        private Bounds<?> range;

        /**
         * Constructor
         * @param axis      the axis reference
         * @param index     the index for this axis
         * @param domain    true if adapter is for domain axis
         */
        private AxisAdapter(Axis axis, int index, boolean domain) {
            this.axis = axis;
            this.index = index;
            this.domain = domain;
        }


        @Override
        public ChartLabel label() {
            return new AxisLabelAdapter(axis);
        }


        @Override
        public ChartFormat format() {
            return new AxisFormatAdapter(axis);
        }


        @Override
        public ChartTextStyle ticks() {
            return new AxisStyleAdapter(axis);
        }


        @Override
        public XyAxis asLogScale() {
            if (axis instanceof CategoryAxis) {
                throw new ChartException("Cannot convert discrete axis to continuous axis");
            } else if (!(axis instanceof LogAxis)) {
                final String label = axis.getLabel();
                final LogAxis logAxis = new LogAxis(label);
                logAxis.setLabelFont(axis.getLabelFont());
                logAxis.setTickLabelFont(axis.getTickLabelFont());
                logAxis.setLabelPaint(axis.getLabelPaint());
                logAxis.setTickLabelPaint(axis.getTickLabelPaint());
                this.axis = logAxis;
                if (range != null) withRange(range);
                if (domain && plot instanceof XYPlot) {
                    ((XYPlot)plot).setDomainAxis(index, logAxis);
                } else if (!domain && plot instanceof XYPlot) {
                    ((XYPlot)plot).setRangeAxis(index, logAxis);
                } else if (plot instanceof CategoryPlot) {
                    ((CategoryPlot)plot).setRangeAxis(index, logAxis, true);
                } else {
                    throw new ChartException("Unsupported plot type for log scale: " + plot);
                }
            }
            return this;
        }


        @Override
        public XyAxis asLinearScale() {
            if (axis instanceof CategoryAxis) {
                throw new ChartException("Cannot convert discrete axis to continuous axis");
            } else if (!(axis instanceof NumberAxis)) {
                final String label = axis.getLabel();
                final NumberAxis linearAxis = new NumberAxis(label);
                linearAxis.setLabelFont(axis.getLabelFont());
                linearAxis.setTickLabelFont(axis.getTickLabelFont());
                linearAxis.setLabelPaint(axis.getLabelPaint());
                linearAxis.setTickLabelPaint(axis.getTickLabelPaint());
                this.axis = linearAxis;
                if (range != null) withRange(range);
                if (domain && plot instanceof XYPlot) {
                    ((XYPlot)plot).setDomainAxis(index, linearAxis);
                } else if (!domain && plot instanceof XYPlot) {
                    ((XYPlot)plot).setRangeAxis(index, linearAxis);
                } else if (plot instanceof CategoryPlot) {
                    ((CategoryPlot)plot).setRangeAxis(index, linearAxis);
                } else {
                    throw new ChartException("Unsupported plot type for log scale: " + plot);
                }
            }
            return this;
        }


        @Override
        public XyAxis asDateScale() {
            if (axis instanceof CategoryAxis) {
                throw new ChartException("Cannot convert discrete axis to continuous axis");
            } else if (!(axis instanceof NumberAxis)) {
                final String label = axis.getLabel();
                final DateAxis dateAxis = new DateAxis(label);
                dateAxis.setLabelFont(axis.getLabelFont());
                dateAxis.setTickLabelFont(axis.getTickLabelFont());
                dateAxis.setLabelPaint(axis.getLabelPaint());
                dateAxis.setTickLabelPaint(axis.getTickLabelPaint());
                this.axis = dateAxis;
                if (range != null) withRange(range);
                if (domain && plot instanceof XYPlot) {
                    ((XYPlot)plot).setDomainAxis(index, dateAxis);
                } else if (!domain && plot instanceof XYPlot) {
                    ((XYPlot)plot).setRangeAxis(index, dateAxis);
                } else if (plot instanceof CategoryPlot) {
                    ((CategoryPlot)plot).setRangeAxis(index, dateAxis);
                } else {
                    throw new ChartException("Unsupported plot type for log scale: " + plot);
                }
            }
            return this;
        }


        @Override
        public XyAxis withRange(Bounds<?> range) {
            this.range = range;
            if (axis != null) {
                if (axis instanceof NumberAxis) {
                    final NumberAxis numberAxis = (NumberAxis)axis;
                    final double lower = ((Number)range.lower()).doubleValue();
                    final double upper = ((Number)range.upper()).doubleValue();
                    final Range autoRange = new Range(lower, upper);
                    numberAxis.setDefaultAutoRange(autoRange);
                    numberAxis.setRange(lower, upper);
                } else if (axis instanceof DateAxis) {
                    final DateAxis dateAxis = (DateAxis)axis;
                    final long lower = toEpochMillis(range.lower());
                    final long upper = toEpochMillis(range.upper());
                    final Range autoRange = new Range(lower, upper);
                    dateAxis.setDefaultAutoRange(autoRange);
                } else {
                    throw new IllegalStateException("Can only set range of numeric or date axis");
                }
            }
            return this;
        }


        /**
         * Converts some argument into epoch millis if it can
         * @param value     the value to convert
         * @return          the epoch millis
         */
        private long toEpochMillis(Object value) {
            if (value instanceof Number) {
                return ((Number)value).longValue();
            } else if (value instanceof Date) {
                return ((Date)value).getTime();
            } else if (value instanceof LocalDate) {
                final LocalDate date = (LocalDate)value;
                final LocalDateTime localDateTime = LocalDateTime.of(date, LocalTime.MIDNIGHT);
                return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            } else {
                throw new IllegalArgumentException("Cannot resolve date value from " + value);
            }
        }
    }



    /**
     * A JFreeChart ChartFormat adapter
     */
    private class AxisFormatAdapter implements ChartFormat {

        private Axis axis;

        /**
         * Constructor
         * @param axis    the axis reference
         */
        private AxisFormatAdapter(Axis axis) {
            this.axis = axis;
        }

        @Override
        public void withPattern(String pattern) {
            if (axis instanceof NumberAxis) {
                ((NumberAxis)axis).setNumberFormatOverride(new DecimalFormat(pattern));
            } else if (axis instanceof LogAxis) {
                ((LogAxis)axis).setNumberFormatOverride(new DecimalFormat(pattern));
            } else if (axis instanceof DateAxis) {
                ((DateAxis)axis).setDateFormatOverride(new SimpleDateFormat(pattern));
            }
        }
    }


    /**
     * A JFreeChart ChartLabel adapter for an axis
     */
    private class AxisLabelAdapter implements ChartLabel {

        private Axis axis;

        /**
         * Constructor
         * @param axis  the axis for this adapter
         */
        private AxisLabelAdapter(Axis axis) {
            this.axis = axis;
        }

        @Override
        public ChartLabel withText(String text) {
            this.axis.setLabel(text);
            return this;
        }

        @Override
        public ChartLabel withColor(Color color) {
            this.axis.setLabelPaint(color);
            return this;
        }

        @Override
        public ChartLabel withFont(Font font) {
            this.axis.setLabelFont(font);
            return this;
        }
    }


    /**
     * A JFreeChart text style adapter for an axis
     */
    private class AxisStyleAdapter implements ChartTextStyle {

        private Axis axis;

        /**
         * Constructor
         * @param axis  the axis for this adapter
         */
        private AxisStyleAdapter(Axis axis) {
            this.axis = axis;
        }

        @Override
        public ChartTextStyle withColor(Color color) {
            this.axis.setAxisLinePaint(color);
            this.axis.setTickLabelPaint(color);
            return this;
        }

        @Override
        public ChartTextStyle withFont(Font font) {
            this.axis.setTickLabelFont(font);
            return this;
        }
    }

}
