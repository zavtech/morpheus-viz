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

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.Calendar;
import java.util.Date;
import java.util.EventObject;
import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.JTextComponent;

import com.zavtech.morpheus.util.text.SmartFormat;

/**
 * A generic TableCellEditor that can be used to edit values of various types.
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public class DataFrameCellEditor extends AbstractCellEditor implements TableCellEditor  {

    private Object value;
    private Component editor;
    private boolean selectAll = true;

    private ActionListener actionListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            stopCellEditing();
        }
    };


    /**
     * Constructor
     */
    public DataFrameCellEditor() {
        super();
    }

    /**
     * Returns true if this editor is in select all mode
     * @return      true if in select all mode
     */
    public boolean isSelectAll() {
        return selectAll;
    }

    /**
     * Sets whether this editor performs selectAll on edit
     * @param selectAll true to select all text on edit
     */
    public void setSelectAll(boolean selectAll) {
        this.selectAll = selectAll;
    }


    @Override()
    public boolean isCellEditable(EventObject event) {
        if (event instanceof MouseEvent) {
            final MouseEvent me = (MouseEvent)event;
            return me.getClickCount() == 2;
        } else {
            return false;
        }
    }


    @Override()
    public boolean shouldSelectCell(EventObject event) {
        if (editor instanceof JTextComponent) {
            final JTextComponent textComponent = (JTextComponent)editor;
            if (selectAll) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        textComponent.requestFocus();
                        textComponent.selectAll();
                    }
                });
            }
            return true;
        } else {
            return false;
        }
    }


    @Override()
    public boolean stopCellEditing() {
        try {
            if (editor instanceof JFormattedTextField) {
                this.value = ((JFormattedTextField)editor).getValue();
                this.fireEditingStopped();
                return true;
            } else if (editor instanceof JTextField) {
                this.value = ((JTextField) editor).getText();
                this.fireEditingStopped();
                return true;
            } else if (editor instanceof JSpinner) {
                final JSpinner spinner = (JSpinner)editor;
                this.value = spinner.getValue();
                this.fireEditingStopped();
                return true;
            } else {
                this.fireEditingStopped();
                return true;
            }
        } catch (Throwable t) {
            return false;
        }
    }


    @Override()
    public void cancelCellEditing() {
        fireEditingCanceled();
    }


    /** @inheritDoc */
    public Object getCellEditorValue() {
        return value;
    }


    /** @inheritDoc */
    public Component getTableCellEditorComponent(JTable table, Object value, boolean selected, int rowIndex, int colIndex) {
        final TableCellRenderer renderer = table.getCellRenderer(rowIndex, colIndex);
        final JComponent component = (JComponent)renderer.getTableCellRendererComponent(table, value, selected, false, rowIndex, colIndex);
        final Font font = component.getFont();
        System.out.println("getTableCellEditorComponent() value=" + value + "row=" + rowIndex + " column=" + colIndex);
        if (value == null) {
            final Format format = new SmartFormat();
            final JFormattedTextField smartEditor = new JFormattedTextField(format);
            smartEditor.setBorder(component.getBorder());
            smartEditor.setFont(font);
            smartEditor.setValue(value);
            smartEditor.addActionListener(actionListener);
            editor = smartEditor;
        } else if (value instanceof Double) {
            final Format doubleFormat = new DecimalFormat("0.00####;-0.00####");
            final JFormattedTextField doubleEditor = new JFormattedTextField(doubleFormat);
            doubleEditor.setFont(font);
            doubleEditor.setValue(value);
            doubleEditor.setBorder(component.getBorder());
            doubleEditor.addActionListener(actionListener);
            doubleEditor.setHorizontalAlignment(SwingConstants.RIGHT);
            editor = doubleEditor;
        } else if (value instanceof Integer) {
            final Format integerFormat = new DecimalFormat("0;-0");
            final JFormattedTextField integerEditor = new JFormattedTextField(integerFormat);
            integerEditor.setFont(font);
            integerEditor.setBorder(component.getBorder());
            integerEditor.setHorizontalAlignment(SwingConstants.RIGHT);
            integerEditor.setValue(value);
            integerEditor.addActionListener(actionListener);
            editor = integerEditor;
        } else if (value instanceof String) {
            final JTextField stringEditor = new JTextField();
            stringEditor.setFont(font);
            stringEditor.setBorder(component.getBorder());
            stringEditor.setText(value.toString());
            stringEditor.addActionListener(actionListener);
            editor = stringEditor;
        } else if (value instanceof Date) {
            final SpinnerDateModel model = new SpinnerDateModel();
            final JSpinner spinner = new JSpinner(model);
            final JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spinner, "dd-MMM-yyyy");
            dateEditor.getTextField().setFont(font);
            dateEditor.getTextField().setBorder(component.getBorder());
            spinner.setFont(font);
            spinner.setEditor(dateEditor);
            spinner.setValue(value);
            editor = spinner;
        } else if (value instanceof Calendar) {
            final SpinnerDateModel model = new SpinnerDateModel();
            final JSpinner spinner = new JSpinner(model);
            final JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spinner, "dd-MMM-yyyy");
            dateEditor.getTextField().setFont(font);
            dateEditor.getTextField().setBorder(component.getBorder());
            dateEditor.getModel().setValue(((Calendar)value).getTime());
            editor = dateEditor;
        }
        return editor;
    }

}
