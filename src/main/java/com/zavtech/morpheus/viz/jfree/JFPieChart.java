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

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.PieSectionEntity;

import com.zavtech.morpheus.viz.chart.pie.PiePlot;

/**
 * The chart implementation for creating pie charts.
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
class JFPieChart<X extends Comparable,S extends Comparable> extends JFChartBase<PiePlot<X,S>> {


    /**
     * Constructor
     * @param is3d  true for a 3D Pie Plot
     * @param legend    true to enable legend
     */
    JFPieChart(boolean is3d, boolean legend) {
        super(new JFPiePlot<>(is3d), legend);
    }


    /**
     * Returns the JFreeChart typed pie plot
     * @return  the pie plot
     */
    private JFPiePlot<X,S> piePlot() {
        return (JFPiePlot<X,S>)plot();
    }


    @Override
    public void chartMouseClicked(ChartMouseEvent event) {
        try {
            final ChartEntity entity = event.getEntity();
            if (entity instanceof PieSectionEntity) {
                final PieSectionEntity pieSection = (PieSectionEntity)entity;
                final Comparable itemKey = pieSection.getSectionKey();
                if (itemKey != null) {
                    piePlot().toggle(itemKey);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    @Override
    public void chartMouseMoved(ChartMouseEvent event) {
        try {
            final ChartEntity entity = event.getEntity();
            if (entity instanceof PieSectionEntity) {
                final PieSectionEntity pieSection = (PieSectionEntity)entity;
                final Comparable itemKey = pieSection.getSectionKey();
                if (itemKey != null) {
                    piePlot().highlight(itemKey);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
