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

import java.awt.Font;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * One line summary here...
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public class DataFrameCellFormat {

    private static final Logger LOG = Logger.getLogger(DataFrameCellFormat.class.getName());

    public static final int DECIMAL         = 0;
    public static final int PERCENT         = 1;
    public static final int BASIS_POINTS    = 2;
    public static final int SCIENTIFIC      = 3;

    private static final List<Format> decimalFormatList = new ArrayList<Format>();
    private static final List<Format> percentFormatList = new ArrayList<Format>();
    private static final List<Format> scientificFormatList = new ArrayList<Format>();
    private static final List<Format> basisPointsFormatList = new ArrayList<Format>();
    private static final Map<String,Format> dateFormatMap = new HashMap<String,Format>();

    private Font font;
    private int precision = 4;
    private int numberType = DECIMAL;
    private int alignment = -1;
    private String datePattern = "dd-MMM-yyyy";
    private Object background;
    private Object foreground;

    /**
     * Static initializer
     */
    static {
        try {
            createDecimalFormats("#,##0<ZEROS>;-#,##0<ZEROS>", 30, 1, decimalFormatList);
            createDecimalFormats("0<ZEROS>E00;-0<ZEROS>E00", 30, 1, scientificFormatList);
            createDecimalFormats("#,##0<ZEROS>'%';-#,##0<ZEROS>'%'", 30, 100, percentFormatList);
            createDecimalFormats("#,##0<ZEROS>' bps';-#,##0<ZEROS>' bps'", 30, 10000, basisPointsFormatList);
        } catch (Throwable t) {
            LOG.log(Level.SEVERE, t.getMessage(), t);
        }
    }


    /**
     * Constructor
     */
    public DataFrameCellFormat() {
        super();
    }

    /**
     * Returns the font for this format
     * @return      the font
     */
    public Font getFont() {
        return font;
    }

    /**
     * Returns the alignment for this cell format
     * @param defaultAlignment  the default alignment if none defined
     * @return  the alignment
     */
    public int getAlignment(int defaultAlignment) {
        return alignment >= 0 ? alignment : defaultAlignment;
    }

    /**
     * Returns the number type for this format
     * @return      the number type
     */
    public int getNumberType() {
        return numberType;
    }

    /**
     * Returns the precision for this format
     * @return  the precision
     */
    public int getPrecision() {
        return precision;
    }

    /**
     * Returns the date pattern for this format
     * @return      the date pattern
     */
    public String getDatePattern() {
        return datePattern;
    }

    /**
     * Returns the background for this format
     * @return  the background
     */
    public Object getBackground() {
        return background;
    }

    /**
     * Returns the foreground for this format
     * @return  the forground
     */
    public Object getForeground() {
        return foreground;
    }

    /**
     * Sets the font for this format
     * @param font  the font
     */
    public void setFont(Font font) {
        this.font = font;
    }

    /**
     * Sets the alignment for this format
     * @param alignment the alignment
     */
    public void setAlignment(int alignment) {
        this.alignment = alignment;
    }

    /**
     * Sets the precision for this format
     * @param precision the precision
     */
    public void setPrecision(int precision) {
        this.precision = precision;
    }

    /**
     * Sets the number type for this format
     * @param numberType   the number type
     */
    public void setNumberType(int numberType) {
        this.numberType = numberType;
    }

    /**
     * Sets the date pattern for this format
     * @param datePattern   the date pattern
     */
    public void setDatePattern(String datePattern) {
        this.datePattern = datePattern;
    }

    /**
     * Sets the background for this format
     * @param background    the background
     */
    public void setBackground(Object background) {
        this.background = background;
    }

    /**
     * Sets the foreground for this pattern
     * @param foreground    the foreground
     */
    public void setForeground(Object foreground) {
        this.foreground = foreground;
    }

    /**
     * Formats the value of a cell in a readable text
     * @param value     the value to format
     * @return          the formatted text
     */
    public String format(Object value) {
        if (value == null) return null;
        else if (value instanceof Date) return formatDate((Date)value);
        else if (value instanceof Number) return formatDecimal((Number)value);
        else return value.toString();
    }

    /**
     * Called to increase the decimal precision
     */
    public final void increasePrecision() {
        this.precision++;
    }

    /**
     * Called to decrease the decimal precision
     */
    public final void decreasePrecision() {
        this.precision--;
    }

    /**
     * Returns a formatted date string for the row and column coordinates
     * @param date      the date value
     * @return          the formatted string
     */
    protected synchronized String formatDate(Date date) {
        final String pattern = this.getDatePattern();
        Format dateFormat = dateFormatMap.get(pattern);
        if (dateFormat == null) {
            dateFormat = new SimpleDateFormat(pattern);
            dateFormatMap.put(pattern, dateFormat);
        }
        return dateFormat.format(date);
    }

    /**
     * Returns a formatted decimal string for cell value specified
     * @param value     the actual value to render
     * @return          the formatted decimal string
     */
    protected synchronized String formatDecimal(Number value) {
        final int precision = this.getPrecision();
        final int type = this.getNumberType();
        switch (type) {
            case DataFrameCellFormat.DECIMAL:
                return decimalFormatList.get(precision).format(value);
            case DataFrameCellFormat.PERCENT:
                return percentFormatList.get(precision).format(value);
            case DataFrameCellFormat.SCIENTIFIC:
                return scientificFormatList.get(precision).format(value);
            case DataFrameCellFormat.BASIS_POINTS:
                return basisPointsFormatList.get(precision).format(value);
            default:
                return decimalFormatList.get(precision).format(value);
        }
    }


    /**
     * A routine to ofInts a sequence of decimal formats for various precisions
     * @param template      the pattern template
     * @param count         the number for formatters to ofInts each of increasing precision
     * @param multiplier    the multiplier to accept to format
     * @param formats       the list of formats to populate
     */
    private static void createDecimalFormats(String template, int count, int multiplier, List<Format> formats) {
        try {
            for (int i=0; i<count; ++i) {
                final StringBuilder zeros = new StringBuilder(count + 1).append(i > 0 ? "." : "");
                for (int j=0; j<i; ++j) zeros.append("0");
                final String pattern = template.replace("<ZEROS>", zeros.toString());
                final DecimalFormat format = new DecimalFormat(pattern);
                format.setMultiplier(multiplier);
                formats.add(format);
            }
        } catch (Throwable t) {
            LOG.log(Level.SEVERE, t.getMessage(), t);
        }
    }

}
