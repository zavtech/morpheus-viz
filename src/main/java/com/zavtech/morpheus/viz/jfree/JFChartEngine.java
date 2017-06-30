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
import java.security.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.swing.*;

import com.zavtech.morpheus.viz.chart.Chart;
import com.zavtech.morpheus.viz.chart.ChartEngine;
import com.zavtech.morpheus.frame.DataFrame;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.data.RangeType;

/**
 * A ChartEngine implementation used to create JFreeChart instances based on the Morpheus charting API.
 *
 * @link http://www.jfree.org/jfreechart/
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public class JFChartEngine extends ChartEngine {

    private static final Set<Class<?>> timeTypeSet = new HashSet<>();

    /**
     * Static initializer
     */
    static {
        timeTypeSet.add(Date.class);
        timeTypeSet.add(LocalDate.class);
        timeTypeSet.add(LocalDateTime.class);
        timeTypeSet.add(ZonedDateTime.class);
        timeTypeSet.add(Timestamp.class);
        timeTypeSet.add(java.sql.Date.class);
        timeTypeSet.add(Calendar.class);
    }


    @Override
    public <X extends Comparable, S extends Comparable> Chart<X> create(DataFrame<X, S> frame) {
        if (frame == null) {
            throw new IllegalArgumentException("The DataFrame cannot be null");
        } else {
            final Class<X> domainKeyType = frame.rows().keyType();
            if (Integer.class.equals(domainKeyType)) {
                final NumberAxis domainAxis = new NumberAxis();
                final NumberAxis rangeAxis = new NumberAxis();
                domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
                final Chart<X> chart = new JFChartXY<>(domainAxis, rangeAxis, false);
                chart.data().add(frame);
                chart.plot(0).withLines();
                return chart;
            } else if (Number.class.isAssignableFrom(domainKeyType)) {
                final NumberAxis domainAxis = new NumberAxis();
                final NumberAxis rangeAxis = new NumberAxis();
                final Chart<X> chart = new JFChartXY<>(domainAxis, rangeAxis, false);
                chart.data().add(frame);
                chart.plot(0).withLines();
                return chart;
            } else if (isTimeBased(domainKeyType)) {
                final JFDateAxis domainAxis = new JFDateAxis();
                final NumberAxis rangeAxis = new NumberAxis();
                final Chart<X> chart = new JFChartXY<>(domainAxis, rangeAxis, false);
                chart.data().add(frame);
                chart.plot(0).withLines();
                return chart;
            } else {
                final NumberAxis rangeAxis = new NumberAxis();
                final Chart<X> chart = new JFChartCategory<>(rangeAxis);
                chart.data().add(frame);
                chart.plot(0).withBars(0d);
                return chart;
            }
        }
    }

    @Override
    public <X extends Comparable, S extends Comparable> Chart<X> create(DataFrame<?,S> frame, Class<X> domainType, S domainKey) {
        if (frame == null) {
            throw new IllegalArgumentException("The DataFrame cannot be null");
        } else {
            if (Integer.class.equals(domainType)) {
                final NumberAxis domainAxis = new NumberAxis();
                final NumberAxis rangeAxis = new NumberAxis();
                domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
                final Chart<X> chart = new JFChartXY<>(domainAxis, rangeAxis, false);
                chart.data().add(frame, domainKey);
                chart.plot(0).withLines();
                return chart;
            } else if (Number.class.isAssignableFrom(domainType)) {
                final NumberAxis domainAxis = new NumberAxis();
                final NumberAxis rangeAxis = new NumberAxis();
                final Chart<X> chart = new JFChartXY<>(domainAxis, rangeAxis, false);
                chart.data().add(frame, domainKey);
                chart.plot(0).withLines();
                return chart;
            } else if (isTimeBased(domainType)) {
                final JFDateAxis domainAxis = new JFDateAxis();
                final NumberAxis rangeAxis = new NumberAxis();
                final Chart<X> chart = new JFChartXY<>(domainAxis, rangeAxis, false);
                chart.data().add(frame, domainKey);
                chart.plot(0).withLines();
                return chart;
            } else {
                final NumberAxis rangeAxis = new NumberAxis();
                final Chart<X> chart = new JFChartCategory<>(rangeAxis);
                chart.data().add(frame, domainKey);
                chart.plot(0).withBars(0d);
                return chart;
            }
        }
    }


    @Override
    public void show(int rows, int cols, Iterable<Chart> charts) {
        final JFrame frame = new JFrame();
        frame.getContentPane().setLayout(new GridLayout(rows, cols));
        for (Chart chart : charts) {
            final JFChartBase jChart = (JFChartBase)chart;
            frame.getContentPane().add(jChart.getChartPanel());
        }
        frame.getContentPane().setBackground(Color.WHITE);
        frame.pack();
        frame.setSize(1024, 768);
        frame.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }



    /**
     * Returns true if the data type is time series related
     * @param type  the data type
     * @return      true if time related
     */
    private boolean isTimeBased(Class<?> type) {
        return timeTypeSet.contains(type);
    }
}
