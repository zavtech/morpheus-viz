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

import com.zavtech.morpheus.viz.chart.xy.XyPlot;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.data.xy.XYDataset;

/**
 * A Chart used to implement XY series plots
 *
 * @param <X>   the type for the domain
 *
 * @author  Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
class JFXyChart<X extends Comparable> extends JFChartBase<XyPlot<X>> implements ChartMouseListener {

    /**
     * Constructor
     * @param domainAxis    the domain axis
     * @param rangeAxis     the range axis
     * @param legend        true to show legends
     */
    JFXyChart(ValueAxis domainAxis, ValueAxis rangeAxis, boolean legend) {
        super(new JFXyPlot<>(domainAxis, rangeAxis), legend);
        this.chartPanel().addChartMouseListener(this);
    }


    @Override
    public void chartMouseClicked(ChartMouseEvent event) {

    }


    @Override
    public void chartMouseMoved(ChartMouseEvent event) {
        try {
            final ChartEntity entity = event.getEntity();
            if (entity instanceof XYItemEntity) {
                final XYItemEntity xyEntity = (XYItemEntity)entity;
                final XYDataset dataset = xyEntity.getDataset();
                final int seriesIndex = xyEntity.getSeriesIndex();
                final Comparable seriesKey = dataset.getSeriesKey(seriesIndex);
                if (seriesKey != null) {
                    //todo: Add series highlighting
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
