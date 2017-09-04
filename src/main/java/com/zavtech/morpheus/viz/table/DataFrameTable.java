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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.index.Index;

/**
 * A Swing GUI component that displays a DataFrame in a standard JTable with additional functionality.
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public class DataFrameTable extends javax.swing.JPanel {

    private static final Logger LOG = Logger.getLogger(DataFrameTable.class.getName());

    private static Map<Integer, String> fontStyleNameMap = new LinkedHashMap<>();
    private static Map<Integer, Icon> fontStyleIconMap = new LinkedHashMap<>();
    private static Map<Integer, String> alignmentNameMap = new LinkedHashMap<>();
    private static Map<Integer, Icon> alignmentIconMap = new LinkedHashMap<>();
    private static Map<Integer, String> formatNameMap = new LinkedHashMap<>();
    private static Map<Integer, Icon> formatIconMap = new LinkedHashMap<>();

    private int decimalCount = 4;
    private JScrollPane scrollPane;
    private JTable leftTable = new JTable();
    private JTable rightTable = new JTable();
    private DataFrameCellEditor editor = new DataFrameCellEditor();
    private DataFrameTableModel model = new DataFrameTableModel();
    private DataFrameCellRenderer renderer = new DataFrameCellRenderer(this);
    private DataFrameCellFormat defaultCellFormat = new DataFrameCellFormat();
    private ListSelectionModel selectionModel = new DefaultListSelectionModel();
    private TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);
    private Map<Object,DataFrameCellFormat> formatMap = new HashMap<>();
    private Index<Object> rowIndex = Index.of(Object.class, 2000);
    private Index<Object> colIndex = Index.of(Object.class, 100);
    private DataFrame<Object,Object> formatFrame = DataFrame.ofObjects(rowIndex, colIndex);

    private JPopupMenu popupMenu = new JPopupMenu();
    private JMenu fontMenu = new JMenu("Font...");
    private JMenu styleMenu = new JMenu("Style....");
    private JMenu dateFormatMenu = new JMenu("Date Format...");
    private JMenu decimalFormatMenu = new JMenu("Number Format...");
    private JMenu alignmentMenu = new JMenu("Alignment...");

    private Action bestFitAction = createBestFitAction();
    private Action exportCsvAction = createCsvExportAction();
    private Action backgroundAction = createFillColorAction();
    private Action foregroundAction = createFontColorAction();
    private Action increasePrecisionAction = createIncreasePrecisionAction();
    private Action decreasePrecisionAction = createDecreasePrecisionAction();


    private TableColumnModelListener columnModelListener = new TableColumnModelListener() {
        public void columnAdded(TableColumnModelEvent e)         { resizeLeftTable(); }
        public void columnMarginChanged(ChangeEvent e)           { resizeLeftTable(); }
        public void columnMoved(TableColumnModelEvent e)         { resizeLeftTable(); }
        public void columnRemoved(TableColumnModelEvent e)       { resizeLeftTable(); }
        public void columnSelectionChanged(ListSelectionEvent e) { resizeLeftTable(); }
    };


    private MouseListener mouseListener = new MouseAdapter() {
        @Override()
        public void mousePressed(MouseEvent event) {
            if (event.isPopupTrigger() && !popupMenu.isVisible()) {
                popupMenu.show((Component)event.getSource(), event.getX(), event.getY());
            } else if (event.getSource() == rightTable) {
                if (!rightTable.getCellSelectionEnabled()) {
                    leftTable.clearSelection();
                    leftTable.setRowSelectionAllowed(false);
                    leftTable.setCellSelectionEnabled(true);
                    rightTable.setRowSelectionAllowed(false);
                    rightTable.setCellSelectionEnabled(true);
                }
            } else if (event.getSource() == leftTable) {
                if (rightTable.getCellSelectionEnabled()) {
                    leftTable.setCellSelectionEnabled(false);
                    leftTable.setRowSelectionAllowed(true);
                    rightTable.setCellSelectionEnabled(false);
                    rightTable.setRowSelectionAllowed(true);
                }
            }
        }
        @Override()
        public void mouseClicked(MouseEvent event) {
            if (event.isPopupTrigger() && !popupMenu.isVisible()) {
                popupMenu.show((Component)event.getSource(), event.getX(), event.getY());
            }
        }
        @Override()
        public void mouseReleased(MouseEvent event) {
            if (event.isPopupTrigger() && !popupMenu.isVisible()) {
                popupMenu.show((Component)event.getSource(), event.getX(), event.getY());
            }
        }
    };



    /**
     * Static initializer
     */
    static {
        try {
            fontStyleNameMap.put(Font.PLAIN, "Plain Text");
            fontStyleNameMap.put(Font.BOLD, "Bold Text");
            fontStyleNameMap.put(Font.ITALIC, "Italic Text");
            fontStyleIconMap.put(Font.PLAIN, getIcon(16, "font.png"));
            fontStyleIconMap.put(Font.BOLD, getIcon(16, "text_bold.png"));
            fontStyleIconMap.put(Font.ITALIC, getIcon(16, "text_italic.png"));
            formatNameMap.put(DataFrameCellFormat.DECIMAL, "Decimal");
            formatNameMap.put(DataFrameCellFormat.PERCENT, "Percent");
            formatNameMap.put(DataFrameCellFormat.SCIENTIFIC, "Scientific");
            formatNameMap.put(DataFrameCellFormat.BASIS_POINTS, "Basis Points");
            formatIconMap.put(DataFrameCellFormat.DECIMAL, getIcon(16, "number.png"));
            formatIconMap.put(DataFrameCellFormat.PERCENT, getIcon(16, "percent.png"));
            formatIconMap.put(DataFrameCellFormat.SCIENTIFIC, getIcon(16, "scientific.png"));
            formatIconMap.put(DataFrameCellFormat.BASIS_POINTS, getIcon(16, "bps.png"));
            alignmentNameMap.put(SwingConstants.LEFT, "Align Left");
            alignmentNameMap.put(SwingConstants.CENTER, "Align Center");
            alignmentNameMap.put(SwingConstants.RIGHT, "Align Right");
            alignmentIconMap.put(SwingConstants.LEFT, getIcon(16, "text_align_left.png"));
            alignmentIconMap.put(SwingConstants.CENTER, getIcon(16, "text_align_center.png"));
            alignmentIconMap.put(SwingConstants.RIGHT, getIcon(16, "text_align_right.png"));
        } catch (Throwable t) {
            LOG.log(Level.SEVERE, t.getMessage(), t);
        }
    }


    /**
     * Constructor
     */
    public DataFrameTable() {
        this(null);
    }

    /**
     * Constructor
     * @param frame     the DataFrame for this table
     */
    public DataFrameTable(DataFrame frame) {
        try {
            this.initPopupMenu();
            this.initTable(leftTable);
            this.initTable(rightTable);
            this.leftTable.getColumnModel().addColumnModelListener(columnModelListener);
            this.scrollPane = new JScrollPane(rightTable);
            this.scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, leftTable.getTableHeader());
            this.scrollPane.setRowHeaderView(leftTable);
            this.setLayout(new BorderLayout(0, 0));
            this.add(scrollPane, BorderLayout.CENTER);
            this.setDataFrame(frame);
            this.scrollPane.getRowHeader().addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent event) {
                    final JViewport leftViewPort = scrollPane.getRowHeader();
                    final JViewport rightViewPort = scrollPane.getViewport();
                    final Point leftPoint = leftViewPort.getViewPosition();
                    final Point rightPoint = rightViewPort.getViewPosition();
                    if (leftPoint.y != rightPoint.y) {
                        rightViewPort.setViewPosition(new Point(rightPoint.x, leftPoint.y));
                    }
                }
            });

        } catch (Throwable t) {
            LOG.log(Level.SEVERE, t.getMessage(), t);
        }
    }


    /**
     * Initializes a table component for this component
     * @param table the table
     */
    private void initTable(JTable table) {
        try {
            table.setAutoCreateColumnsFromModel(false);
            table.setModel(model);
            table.setRowSorter(sorter);
            table.setShowGrid(true);
            table.setRowHeight(16);
            table.setGridColor(Color.GRAY);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            table.setDefaultRenderer(Object.class, renderer);
            table.setDefaultRenderer(String.class, renderer);
            table.setDefaultRenderer(Double.class, renderer);
            table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            table.setSelectionModel(selectionModel);
            table.addMouseListener(mouseListener);
        } catch (Throwable t) {
            LOG.log(Level.SEVERE, t.getMessage(), t);
        } finally {
            createColumns(table);
        }
    }

    /**
     * Called to ofInts columns for the target table
     * @param table the table reference
     */
    protected void createColumns(JTable table) {
        table.setColumnModel(new DefaultTableColumnModel());
        if (table == leftTable) {
            table.addColumn(new TableColumn(0, 100, renderer, editor));
        } else {
            for (int i=1; i<model.getColumnCount(); ++i) {
                table.addColumn(new TableColumn(i, 100, renderer, editor));
            }
        }
    }

    /**
     * Initializes the popup menu for this component
     */
    private void initPopupMenu() {
        try {
            this.fontMenu.setIcon(getIcon(16, "font.png"));
            this.fontMenu.add(createFontAction());
            this.fontMenu.addSeparator();
            this.fontMenu.add(createFontStyleAction(Font.PLAIN));
            this.fontMenu.add(createFontStyleAction(Font.BOLD));
            this.fontMenu.add(createFontStyleAction(Font.ITALIC));
            this.styleMenu.setIcon(getIcon(16, "color.png"));
            this.styleMenu.add(backgroundAction);
            this.styleMenu.add(foregroundAction);
            this.styleMenu.add(createConditionalFontColorAction());
            this.alignmentMenu.setIcon(getIcon(16, "text_align_right.png"));
            this.alignmentMenu.add(createAlignmentAction(SwingConstants.LEFT));
            this.alignmentMenu.add(createAlignmentAction(SwingConstants.CENTER));
            this.alignmentMenu.add(createAlignmentAction(SwingConstants.RIGHT));
            this.dateFormatMenu.setIcon(getIcon(16, "date.png"));
            this.dateFormatMenu.add(createDateFormatAction("dd-MMM-yyyy"));
            this.dateFormatMenu.add(createDateFormatAction("dd/MM/yyyy"));
            this.dateFormatMenu.add(createDateFormatAction("dd-MM-yyyy"));
            this.dateFormatMenu.add(createDateFormatAction("MM/dd/yyyy"));
            this.dateFormatMenu.add(createDateFormatAction("MM-dd-yyyy"));
            this.dateFormatMenu.add(createDateFormatAction("yyyy-MM-dd"));
            this.dateFormatMenu.add(createDateFormatAction("dd-MMM-yyyy HH:mm"));
            this.dateFormatMenu.add(createDateFormatAction("dd/MM/yyyy HH:mm"));
            this.dateFormatMenu.add(createDateFormatAction("dd-MM-yyyy HH:mm"));
            this.dateFormatMenu.add(createDateFormatAction("MM/dd/yyyy HH:mm"));
            this.dateFormatMenu.add(createDateFormatAction("MM-dd-yyyy HH:mm"));
            this.dateFormatMenu.add(createDateFormatAction("yyyy-MM-dd HH:mm"));
            this.decimalFormatMenu.setIcon(getIcon(16, "number.png"));
            this.decimalFormatMenu.add(createNumberTypeAction(DataFrameCellFormat.DECIMAL, "Decimal"));
            this.decimalFormatMenu.add(createNumberTypeAction(DataFrameCellFormat.PERCENT, "Percent"));
            this.decimalFormatMenu.add(createNumberTypeAction(DataFrameCellFormat.SCIENTIFIC, "Scientific"));
            this.decimalFormatMenu.add(createNumberTypeAction(DataFrameCellFormat.BASIS_POINTS, "Basis Points"));
            this.popupMenu.add(fontMenu);
            this.popupMenu.add(styleMenu);
            this.popupMenu.add(alignmentMenu);
            this.popupMenu.add(dateFormatMenu);
            this.popupMenu.add(decimalFormatMenu);
            this.popupMenu.add(createClearFormatAction());
            this.popupMenu.addSeparator();
            this.popupMenu.add(bestFitAction);
            this.popupMenu.addSeparator();
            this.popupMenu.add(exportCsvAction);
        } catch (Throwable t) {
            LOG.log(Level.SEVERE, t.getMessage(), t);
        }
    }


    /**
     * Returns a reference to the data frame bound to this viewer
     * @return the data frame bound to viewer, can be null
     */
    @SuppressWarnings("unchecked")
    public <R,C> DataFrame<R,C> getDataFrame() {
        return (DataFrame<R,C>)model.getFrame();
    }


    /**
     * Sets the data frame instance to display in this viewer
     * @param frame the data frame to display, can be null
     */
    public void setDataFrame(DataFrame frame) {
        try {
            this.model.setFrame(frame);
            this.leftTable.getColumnModel().removeColumnModelListener(columnModelListener);
            this.createColumns(leftTable);
            this.createColumns(rightTable);
            this.leftTable.getColumnModel().addColumnModelListener(columnModelListener);
            this.model.fireTableStructureChanged();
        } finally {
            this.resizeLeftTable();
            this.validateActions();
        }
    }

    /**
     * Returns the default format for cells
     * @return  the default format for cells
     */
    public DataFrameCellFormat getDefaultCellFormat() {
        return defaultCellFormat;
    }

    /**
     * Sets the default cell format for this table
     * @param defaultCellFormat     the default cell format
     */
    public void setDefaultCellFormat(DataFrameCellFormat defaultCellFormat) {
        this.defaultCellFormat = defaultCellFormat;
    }

    /**
     * Returns the cell format for the row header cell (ie fixed column)
     * @param row       the row key
     * @param create    if true, ofInts a new cell format if none exists
     * @return          the cell format, null if none exists and ofInts == false
     */
    public DataFrameCellFormat getRowHeaderFormat(Object row, boolean create) {
        DataFrameCellFormat format = formatMap.get(row);
        if (format != null) return format;
        else if (!create) format = defaultCellFormat;
        else {
            format = new DataFrameCellFormat();
            formatMap.put(row, format);
        }
        return format;
    }


    /**
     * Returns the cell format for row and column key
     * @param row       the row key
     * @param column    the column key
     * @param create    if true, ofInts a new cell format if none exists
     * @return          the cell format, null if none exists and ofInts == false
     */
    public DataFrameCellFormat getCellFormat(Object row, Object column, boolean create) {
        if (formatFrame.rows().contains(row) && formatFrame.cols().contains(column)) {
            final int rowIndex = formatFrame.rows().ordinalOf(row);
            final int colIndex = formatFrame.cols().ordinalOf(column);
            DataFrameCellFormat format = formatFrame.data().getValue(rowIndex, colIndex);
            if (format == null && create) {
                format = new DataFrameCellFormat();
                formatFrame.data().setValue(row, column, format);
            }
            return format;
        } else if (!create) {
            return defaultCellFormat;
        } else {
            formatFrame.rows().add(row);
            formatFrame.cols().add(column, DataFrameCellFormat.class);
            final DataFrameCellFormat format = new DataFrameCellFormat();
            this.formatFrame.data().setValue(row, column, format);
            return format;
        }
    }

    /**
     * Returns the cell format for the row and column index
     * @param rowIndex  the row index in view space
     * @param colIndex  the column index in view space
     * @param create    if true, ofInts a new cell format if none exists
     * @return          the cell format, null if none exists and ofInts == false
     */
    @SuppressWarnings("unchecked")
    public final DataFrameCellFormat getCellFormat(int rowIndex, int colIndex, boolean create) {
        final DataFrame<Object,Object> data = model.getFrame();
        if (colIndex < leftTable.getColumnCount()) {
            final int rowModelIndex = leftTable.convertRowIndexToModel(rowIndex);
            final Object row = data.rows().key(rowModelIndex);
            return getRowHeaderFormat(row, create);
        } else {
            final int actualColIndex = colIndex - leftTable.getColumnCount();
            final int rowModelIndex = rightTable.convertRowIndexToModel(rowIndex);
            final int colModelIndex = rightTable.convertColumnIndexToModel(actualColIndex);
            final Object row = data.rows().key(rowModelIndex);
            final Object column = data.cols().key(colModelIndex - 1);
            return getCellFormat(row, column, create);
        }
    }


    /**
     * Returns the column index of the combined table
     * @param table         the JTable reference
     * @param colIndex      the column index on the JTable
     * @return              the mapped column index
     */
    int getColumnIndex(JTable table, int colIndex) {
        return leftTable == table ? colIndex : colIndex + leftTable.getColumnCount();
    }


    /**
     * Called to set the decimal count for this editor
     * @param decimalCount the decimal count for editor
     */
    public void setDecimalCount(int decimalCount) {
        this.decimalCount = decimalCount;
    }


    /**
     * Returns a new action to change the alignment for selected cells
     * @param alignment the alignment as per SwingConstants
     * @return the newly created action
     */
    private Action createAlignmentAction(final int alignment) {
        final String name = alignmentNameMap.get(alignment);
        final Icon icon = alignmentIconMap.get(alignment);
        final String description = null;
        return new CustomAction(name, icon, description) {
            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent e) {
                try {
                    final int[] rows = getSelectedRows();
                    final int[] columns = getSelectedColumns();
                    for (int row : rows) {
                        for (int column : columns) {
                            final DataFrameCellFormat format = getCellFormat(row, column, true);
                            format.setAlignment(alignment);
                        }
                    }
                } catch (Throwable t) {
                    LOG.log(Level.SEVERE, t.getMessage(), t);
                } finally {
                    model.fireTableDataChanged();
                }
            }
        };
    }


    /**
     * Returns a new action to change the font type
     * @return the newly created action
     */
    private Action createFontAction() {
        return new CustomAction("Font Face", null, null) {
            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent e) {
                try {
                    final DataFrameTable parent = DataFrameTable.this;
                    final String msg = "Select that font to accept to selected cells";
                    final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                    final String[] fonts = ge.getAvailableFontFamilyNames();
                    final String fontName = (String) JOptionPane.showInputDialog(parent, msg, "Font", JOptionPane.INFORMATION_MESSAGE, null, fonts, fonts[0]);
                    if (fontName != null) {
                        final Font font = new Font(fontName, Font.PLAIN, 11);
                        final int[] rows = getSelectedRows();
                        final int[] columns = getSelectedColumns();
                        for (int row : rows) {
                            for (int column : columns) {
                                getCellFormat(row, column, true).setFont(font);
                            }
                        }
                    }
                } catch (Throwable t) {
                    LOG.log(Level.SEVERE, t.getMessage(), t);
                } finally {
                    model.fireTableDataChanged();
                }
            }
        };
    }


    /**
     * Returns a new action to change the font style for selected cells
     * @param style the font style
     * @return the newly created action
     */
    private Action createFontStyleAction(final int style) {
        final String name = fontStyleNameMap.get(style);
        final Icon icon = fontStyleIconMap.get(style);
        final String description = null;
        return new CustomAction(name, icon, description) {
            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent e) {
                try {
                    final int[] rows = getSelectedRows();
                    final int[] columns = getSelectedColumns();
                    for (int row : rows) {
                        for (int column : columns) {
                            final DataFrameCellFormat cellFormat = getCellFormat(row, column, true);
                            final Font font = cellFormat.getFont() != null ? cellFormat.getFont() : rightTable.getFont();
                            cellFormat.setFont(new Font(font.getName(), style, font.getSize()));
                        }
                    }
                } catch (Throwable t) {
                    LOG.log(Level.SEVERE, t.getMessage(), t);
                } finally {
                    model.fireTableDataChanged();
                }
            }
        };
    }

    /**
     * Returns a new action to accept a specific date format to selected cells
     * @param pattern   the date format pattern
     * @return          the newly created action
     */
    private Action createDateFormatAction(final String pattern) {
        final Icon icon = getIcon(16, "date.png");
        final String name = pattern + "  (" + new SimpleDateFormat(pattern).format(new Date()) + ")";
        return new CustomAction(name, icon, name) {
            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent e) {
                try {
                    final int[] rows = getSelectedRows();
                    final int[] columns = getSelectedColumns();
                    for (int row : rows) {
                        for (int column : columns) {
                            getCellFormat(row, column, true).setDatePattern(pattern);
                        }
                    }
                } catch (Throwable t) {
                    LOG.log(Level.SEVERE, t.getMessage(), t);
                } finally {
                    model.fireTableDataChanged();
                }
            }
        };
    }


    /**
     * Creates a new action to accept a specific type of formatting to selected cells
     * @param numberType    the number type
     * @param description the action description
     * @return newly created action
     */
    private Action createNumberTypeAction(final Integer numberType, String description) {
        final Icon icon = formatIconMap.get(numberType);
        final String name = formatNameMap.get(numberType);
        return new CustomAction(name, icon, description) {
            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent e) {
                try {
                    final int[] rows = getSelectedRows();
                    final int[] columns = getSelectedColumns();
                    for (int row : rows) {
                        for (int column : columns) {
                            final DataFrameCellFormat format = getCellFormat(row, column, true);
                            if (format != null) {
                                format.setNumberType(numberType);
                            }
                        }
                    }
                } catch (Throwable t) {
                    LOG.log(Level.SEVERE, t.getMessage(), t);
                } finally {
                    model.fireTableDataChanged();
                }
            }
        };
    }


    /**
     * Creates a new action to increase decimal precision
     * @return newly created action
     */
    private Action createIncreasePrecisionAction() {
        final Icon icon = getIcon(16, "add-decimal-place.png");
        return new CustomAction("Increase Preicision", icon, "Increases numeric precision of selected cells") {
            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent e) {
                try {
                    final int[] rows = getSelectedRows();
                    final int[] columns = getSelectedColumns();
                    for (int row : rows) {
                        for (int column : columns) {
                            getCellFormat(row, column, true).increasePrecision();
                        }
                    }
                } catch (Throwable t) {
                    LOG.log(Level.SEVERE, t.getMessage(), t);
                } finally {
                    model.fireTableDataChanged();
                }
            }
        };
    }


    /**
     * Creates a new action to decrease decimal precision
     * @return newly created action
     */
    private Action createDecreasePrecisionAction() {
        final Icon icon = getIcon(16, "delete-decimal-place.png");
        return new CustomAction("Decrease Preicision", icon, "Decreases numeric precision of selected cells") {
            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent e) {
                try {
                    final int[] rows = getSelectedRows();
                    final int[] columns = getSelectedColumns();
                    for (int row : rows) {
                        for (int column : columns) {
                            getCellFormat(row, column, true).decreasePrecision();
                        }
                    }
                } catch (Throwable t) {
                    LOG.log(Level.SEVERE, t.getMessage(), t);
                } finally {
                    model.fireTableDataChanged();
                }
            }
        };
    }


    /**
     * Creates a new action to export data to excel
     * @return newly created action
     */
    private Action createCsvExportAction() {
        final Icon icon = getIcon(16, "excel.png");
        return new CustomAction("Export to CSV", icon, "Decreases numeric precision of selected cells") {
            public void actionPerformed(ActionEvent e) {
                try {
                    if (decimalCount > 0) {
                        decimalCount--;
                    }
                } catch (Throwable t) {
                    LOG.log(Level.SEVERE, t.getMessage(), t);
                }
            }
        };
    }



    /**
     * Creates a new action to best fit columns
     * @return newly created action
     */
    private Action createBestFitAction() {
        final Icon icon = getIcon(16, "table_best_fit.png");
        return new CustomAction("Best Fit Columns", icon, "Resizes columns to optimal widths based on content") {
            public void actionPerformed(ActionEvent e) {
                try {
                    final TableColumnModel columnModel = rightTable.getColumnModel();
                    for (int j = 0; j < columnModel.getColumnCount(); ++j) {
                        int width = -1;
                        final TableColumn column = columnModel.getColumn(j);
                        final int modelIndex = column.getModelIndex();
                        final int columnIndex = rightTable.convertColumnIndexToView(modelIndex);
                        final TableCellRenderer headerRenderer = rightTable.getTableHeader().getDefaultRenderer();
                        if (headerRenderer != null) {
                            final String columnName = rightTable.getColumnName(j);
                            final Component component = headerRenderer.getTableCellRendererComponent(rightTable, columnName, false, false, 0, columnIndex);
                            if (component instanceof JComponent) {
                                final JComponent jcomp = (JComponent) component;
                                int stringWidth = (int) jcomp.getPreferredSize().getWidth();
                                stringWidth = stringWidth + jcomp.getInsets().left + jcomp.getInsets().right + 30;
                                width = (stringWidth > width ? stringWidth : width);
                            }
                        }
                        final TableModel model = rightTable.getModel();
                        for (int i = 0; i < model.getRowCount(); ++i) {
                            final Object value = model.getValueAt(i, modelIndex);
                            final Component component = renderer.getTableCellRendererComponent(rightTable, value, false, false, i, columnIndex);
                            if (component instanceof JLabel) {
                                final JLabel label = (JLabel) component;
                                int stringWidth = (int) label.getPreferredSize().getWidth();
                                stringWidth = stringWidth + label.getInsets().left + label.getInsets().right + 4;
                                width = (stringWidth > width ? stringWidth : width);
                            }
                        }
                        if (width > 0) {
                            column.setPreferredWidth(width);
                        }
                    }
                } catch (Throwable t) {
                    LOG.log(Level.SEVERE, t.getMessage(), t);
                }
            }
        };
    }


    /**
     * Creates a new action to change fill color of selected cells
     * @return newly created action
     */
    private Action createFillColorAction() {
        final Icon icon = getIcon(16, "color.png");
        return new CustomAction("Fill Color", icon, "Sets the background for selected cells") {
            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent e) {
                try {
                    final Color color = JColorChooser.showDialog(DataFrameTable.this, "Background", Color.WHITE);
                    if (color != null) {
                        final int[] rows = getSelectedRows();
                        final int[] columns = getSelectedColumns();
                        for (int row : rows) {
                            for (int column : columns) {
                                final DataFrameCellFormat format = getCellFormat(row, column, true);
                                format.setBackground(color);
                            }
                        }
                    }
                } catch (Throwable t) {
                    LOG.log(Level.SEVERE, t.getMessage(), t);
                } finally {
                    model.fireTableDataChanged();
                }
            }
        };
    }


    /**
     * Creates a new action to change font color of selected cells
     * @return newly created action
     */
    private Action createFontColorAction() {
        final Icon icon = getIcon(16, "edit-color.png");
        return new CustomAction("Font Color", icon, "Sets the font color for selected cells") {
            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent e) {
                try {
                    final Color color = JColorChooser.showDialog(DataFrameTable.this, "Foreground", Color.WHITE);
                    if (color != null) {
                        final int[] rows = getSelectedRows();
                        final int[] columns = getSelectedColumns();
                        for (int row : rows) {
                            for (int column : columns) {
                                final DataFrameCellFormat format = getCellFormat(row, column, true);
                                format.setForeground(color);
                            }
                        }
                    }
                } catch (Throwable t) {
                    LOG.log(Level.SEVERE, t.getMessage(), t);
                } finally {
                    model.fireTableDataChanged();
                }
            }
        };
    }


    /**
     * Creates a new action to change font color of selected cells
     * @return newly created action
     */
    private Action createConditionalFontColorAction() {
        final Icon icon = getIcon(16, "edit-color.png");
        return new CustomAction("Green / Red Font Color", icon, "Sets the font color for selected cells") {
            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent e) {
                try {
                    final int[] rows = getSelectedRows();
                    final int[] columns = getSelectedColumns();
                    for (int row : rows) {
                        for (int column : columns) {
                            final Color zero = leftTable.getForeground();
                            final Color positive = Color.GREEN;
                            final Color negative = Color.RED;
                            final DataFrameCellFormat format = getCellFormat(row, column, true);
                            format.setForeground(DataFrameCellRenderer.createColorProperty(zero, positive, negative));
                        }
                    }
                } catch (Throwable t) {
                    LOG.log(Level.SEVERE, t.getMessage(), t);
                } finally {
                    model.fireTableDataChanged();
                }
            }
        };
    }

    /**
     * Creates a new action to clear the formatting on selected cells
     * @return newly created action
     */
    private Action createClearFormatAction() {
        final Icon icon = getIcon(16, "edit-color.png");
        return new CustomAction("Clear Formatting", icon, "Removes any formatting applied to the selected cells") {
            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent e) {
                try {
                    final int[] rows = getSelectedRows();
                    final int[] columns = getSelectedColumns();
                    for (int row : rows) {
                        for (int column : columns) {
                            final DataFrameCellFormat format = getCellFormat(row, column, false);
                            if (format != null) {
                                //format.clear();
                            }
                        }
                    }
                } catch (Throwable t) {
                    LOG.log(Level.SEVERE, t.getMessage(), t);
                } finally {
                    model.fireTableDataChanged();
                }
            }
        };
    }




    /**
     * Called to validate this components actions
     */
    private void validateActions() {
        this.increasePrecisionAction.setEnabled(true);
        this.decreasePrecisionAction.setEnabled(true);
        this.exportCsvAction.setEnabled(model.getRowCount() > 0);
    }


    /**
     * Returns the selected row indices in view space
     * @return      the selected row indices
     */
    public int[] getSelectedRows() {
        return rightTable.getSelectedRows();
    }


    /**
     * Returns the selected column indices in view space
     * @return      the selected column indices
     */
    public int[] getSelectedColumns() {
        final int[] columns1 = leftTable.getSelectedColumns();
        final int[] columns2 = rightTable.getSelectedColumns();
        final int[] columns = new int[columns1.length + columns2.length];
        System.arraycopy(columns1, 0, columns, 0, columns1.length);
        for (int i=0; i<columns2.length; ++i) {
            columns[columns1.length + i] = columns2[i] + leftTable.getColumnCount();
        }
        return columns;
    }


    /**
     * Returns a new icon for of the size and name specified
     * @param size the icon size (16 | 32)
     * @param name the icon name
     * @return the icon, null if gailed to load
     */
    private static ImageIcon getIcon(int size, String name) {
        final String path = "/icons/" + size + "x" + size + "/" + name;
        final URL url = DataFrameTable.class.getResource(path);
        if (url == null) LOG.warning("No icon found for path");
        return url != null ? new ImageIcon(url) : null;
    }

    /**
     * Called to resize the row header viewport based on the size of the left table
     */
    private void resizeLeftTable() {
        try {
            this.scrollPane.getRowHeader().setPreferredSize(leftTable.getPreferredSize());
            this.scrollPane.getRowHeader().setMaximumSize(leftTable.getPreferredSize());
        } catch (Throwable t) {
            LOG.log(Level.SEVERE, t.getMessage(), t);
        }
    }

    /**
     * Convenience class to ofInts actions
     */
    private static abstract class CustomAction extends AbstractAction {
        private CustomAction(String name, Icon icon, String description) {
            super(name);
            if (icon != null) putValue(Action.SMALL_ICON, icon);
            if (description != null) putValue(Action.SHORT_DESCRIPTION, description);
        }
    }

}
