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

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.AbstractTableModel;

import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.frame.DataFrameEvent;
import com.zavtech.morpheus.frame.DataFrameListener;

/**
 * A Swing <code>TableModel</code> implementation to display a <code>DataFrame</code> in a JTable.
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public class DataFrameTableModel extends AbstractTableModel implements DataFrameListener {

    private static final Logger LOG = Logger.getLogger(DataFrameTableModel.class.getName());

    private boolean mutable;
    private DataFrame<Object,Object> frame;
    private ThreadLocal<Object[]> coordinates = new ThreadLocal<Object[]>() {
        protected Object[] initialValue() {
            return new Object[2];
        }
    };

    /**
     * Constructor
     */
    public DataFrameTableModel() {
        this(null);
    }


    /**
     * Constructor
     * @param frame    the frame to expose through table model
     */
    public DataFrameTableModel(DataFrame frame) {
        this.setFrame(frame);
    }


    /**
     * Returns the frame assigned to this model
     * @return      the frame
     */
    public DataFrame getFrame() {
        return frame;
    }


    /**
     * Sets the frame for this model
     * @param frame    the frame (can be null
     */
    @SuppressWarnings("unchecked")
    public void setFrame(DataFrame frame) {
        if (getFrame() != null) {
            this.getFrame().events().removeDataFrameListener(this);
        }
        this.frame = frame;
        if (getFrame() != null) {
            this.getFrame().events().addDataFrameListener(this);
        }
    }

    /**
     * Sets whether the model will allow matrix changes
     * @param mutable   true to enable editing
     */
    public void setMutable(boolean mutable) {
        this.mutable = mutable;
    }

    /** @inheritDoc */
    @SuppressWarnings("unchecked")
    public void onDataFrameEvent(DataFrameEvent event) {
        try {
            if (event.isSingleElement()) {
                final Object row = event.rowKeys().getValue(0);
                final Object column = event.colKeys().getValue(0);
                final int rowIndex = frame.rows().ordinalOf(row);
                final int colIndex = frame.cols().ordinalOf(column);
                fireTableCellUpdated(rowIndex, colIndex);
            } else if (event.isSingleRow()) {
                final Object row = event.rowKeys().getValue(0);
                final int rowIndex = frame.rows().ordinalOf(row);
                fireTableRowsUpdated(rowIndex, rowIndex);
            } else {
                fireTableDataChanged();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /** @inheritDoc */
    public int getRowCount() {
        return frame != null ? frame.rowCount() : 0;
    }


    /** @inheritDoc */
    public int getColumnCount() {
        return frame != null ? frame.colCount() + 1 : 0;
    }


    @Override()
    public String getColumnName(int colIndex) {
        if (frame == null) return null;
        switch (colIndex) {
            case 0:    return "Index";
            default:    return frame.cols().key(colIndex - 1).toString();
        }
    }


    @Override()
    public Class<?> getColumnClass(int colIndex) {
        if (frame == null) return Object.class;
        switch (colIndex) {
            case 0:     return frame.rowCount() > 0 ? frame.rows().key(0).getClass() : Object.class;
            default:
                final Object key = frame.cols().key(colIndex - 1);
                return frame.cols().type(key);
        }
    }


    @Override()
    public boolean isCellEditable(int rowIndex, int colIndex) {
        if (frame == null) return false;
        switch (colIndex) {
            case 0:    return false;
            default:    return mutable;
        }
    }


    /** @inheritDoc */
    public Object getValueAt(int rowIndex, int colIndex) {
        if (frame == null) {
            return null;
        } else {
            if (colIndex == 0) {
                return frame.rows().key(rowIndex);
            } else {
                try {
                    final Object[] coords = coordinates.get();
                    coords[0] = frame.rows().key(rowIndex);
                    coords[1] = frame.cols().key(colIndex - 1);
                    return frame.data().getValue(rowIndex, colIndex-1);
                } catch (Throwable t) {
                    LOG.log(Level.SEVERE, t.getMessage(), t);
                    return null;
                }
            }
        }
    }


    @Override()
    @SuppressWarnings("unchecked")
    public void setValueAt(Object value, int rowIndex, int colIndex) {
        if (frame != null && colIndex > 0) {
            try {
                this.frame.data().setValue(rowIndex, colIndex-1, value);
            } catch (Throwable t) {
                LOG.log(Level.SEVERE, t.getMessage(), t);
            }
        }
    }

    /**
     * Returns the coordinates of the last call to getValueAt()
     * @return  the coordinates of last getValueAt() on this thread
     */
    Object[] getLastCoordinates() {
        return coordinates.get();
    }

}
