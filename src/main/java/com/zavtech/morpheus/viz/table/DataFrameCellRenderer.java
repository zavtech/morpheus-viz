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
package com.zavtech.morpheus.viz.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * A specialized renderer that formats cell values of various types for a DataFrameTable.
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public class DataFrameCellRenderer extends DefaultTableCellRenderer {

    private static final Logger LOG = Logger.getLogger(DataFrameCellRenderer.class.getName());

    private static int PERF_COUNT = 10000;

    private int count;
    private long time;
    private DataFrameTable dataFrameTable;


    /**
     * Constructor
     * @param dataFrameTable     the table reference
     */
    DataFrameCellRenderer(DataFrameTable dataFrameTable) {
        this.dataFrameTable = dataFrameTable;
    }

    /**
     * Returns a cell property that yields colors for numeric values
     * @param zero          the color for zero values
     * @param positive      the color for positive values
     * @param negative      the color for negative values
     * @return              the newly created property
     */
    static DataFrameCellProperty createColorProperty(final Color zero, final Color positive, final Color negative) {
        return new DataFrameCellProperty() {
            public Object getValue(Object value) throws IllegalArgumentException {
                if (value instanceof Number) {
                    final double number = ((Number)value).doubleValue();
                    return number > 0d ? positive : number < 0d ? negative : zero;
                } else {
                    return zero;
                }
            }
        };
    }


    @Override()
    @SuppressWarnings("unchecked")
    public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int rowIndex, int colIndex) {
        final long t1 = System.currentTimeMillis();
        final JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, selected, focus, rowIndex, colIndex);
        try {
            final int columnIndex = dataFrameTable.getColumnIndex(table, colIndex);
            final DataFrameCellFormat format = dataFrameTable.getCellFormat(rowIndex, columnIndex, false);
            final DataFrameCellFormat cellFormat = format != null ? format : dataFrameTable.getDefaultCellFormat();
            final Font font = cellFormat != null ? cellFormat.getFont() : null;
            final Color background = getBackground(table, cellFormat, value, selected);
            final Color foreground = getForeground(table, cellFormat, value, selected);
            label.setBackground(background);
            label.setForeground(foreground);
            label.setFont(font != null ? font : table.getFont());
            final String text = cellFormat.format(value);
            label.setText(text);
            if (value == null) {
                label.setHorizontalAlignment(SwingConstants.CENTER);
            } else if (value instanceof Double) {
                final int alignment = cellFormat.getAlignment(SwingUtilities.RIGHT);
                label.setHorizontalAlignment(alignment);
            } else if (value instanceof Float) {
                final int alignment = cellFormat.getAlignment(SwingUtilities.RIGHT);
                label.setHorizontalAlignment(alignment);
            } else if (value instanceof Number) {
                final int alignment = cellFormat.getAlignment(SwingUtilities.RIGHT);
                label.setHorizontalAlignment(alignment);
            } else if (value instanceof String) {
                final int alignment = cellFormat.getAlignment(SwingUtilities.LEFT);
                label.setHorizontalAlignment(alignment);
            } else if (value instanceof Date) {
                final int alignment = cellFormat.getAlignment(SwingUtilities.LEFT);
                label.setHorizontalAlignment(alignment);
            } else if (value instanceof Calendar) {
                final int alignment = cellFormat.getAlignment(SwingUtilities.LEFT);
                label.setHorizontalAlignment(alignment);
            } else if (value instanceof Boolean) {
                final int alignment = cellFormat.getAlignment(SwingUtilities.CENTER);
                label.setHorizontalAlignment(alignment);
            } else {
                final int alignment = cellFormat.getAlignment(SwingUtilities.RIGHT);
                label.setHorizontalAlignment(alignment);
            }
        } catch (Throwable t) {
            LOG.log(Level.SEVERE, t.getMessage(), t);
        }
        final long t2 = System.currentTimeMillis();
        this.time += (t2-t1);
        this.count++;
        if (count % PERF_COUNT == 0) {
            LOG.info("DataFrameCellRenderer cell time=" + (((double)time / (double)PERF_COUNT) * 1000000d) + " nanos, total time=" + time + " millis");
            this.time = 0;
            this.count = 0;
        }
        return label;
    }

    /**
     * Returns the background color for the row and column coordinates
     * @param table     the table
     * @param format    the cell format
     * @param value     the cell value
     * @param selected  true if selected
     * @return          the background color
     */
    @SuppressWarnings("unchecked")
    protected final Color getBackground(JTable table, DataFrameCellFormat format, Object value, boolean selected) {
        final Object background = format != null ? format.getBackground() : null;
        if (background instanceof Color) {
            final Color color = (Color)background;
            return selected ? color.darker() : color;
        } else if (background instanceof DataFrameCellProperty) {
            final DataFrameCellProperty property = (DataFrameCellProperty)background;
            final Object result = property.getValue(value);
            if (result instanceof Color) return selected ? ((Color)result).darker() : (Color)result;
        }
        return selected ? table.getSelectionBackground() : table.getBackground();
    }

    /**
     * Returns the foreground color for the row and column coordinates
     * @param table     the table
     * @param format    the cell format
     * @param value     the cell value
     * @param selected  true if selected
     * @return          the forefround color
     */
    @SuppressWarnings("unchecked")
    protected final Color getForeground(JTable table, DataFrameCellFormat format, Object value, boolean selected) {
        final Object foreground = format != null ? format.getForeground() : null;
        if (foreground instanceof Color) {
            final Color color = (Color)foreground;
            return selected ? color.darker() : color;
        } else if (foreground instanceof DataFrameCellProperty) {
            final DataFrameCellProperty property = (DataFrameCellProperty)foreground;
            final Object result = property.getValue(value);
            if (result instanceof Color) return selected ? ((Color)result).brighter() : (Color)result;
        }
        return selected ? table.getSelectionForeground() : table.getForeground();
    }
}
