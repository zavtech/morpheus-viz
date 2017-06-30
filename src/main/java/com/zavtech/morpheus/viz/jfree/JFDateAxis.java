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

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTick;
import org.jfree.chart.axis.Tick;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;

/**
 * A JFreeChart DateAxis extension which adds support for rotating tick labels
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
class JFDateAxis extends DateAxis {

    private double tickAngle;

    /**
     * Constructor
     */
    JFDateAxis() {
        this(null);
    }

    /**
     * Constructor
     * @param label     the label for axis
     */
    JFDateAxis(String label) {
        this(label, 0d);
    }

    /**
     * Constructor
     * @param label     the label for axis
     * @param tickAngle the tick label angle radians
     */
    JFDateAxis(String label, double tickAngle) {
        super(label);
        this.tickAngle = tickAngle;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List refreshTicksVertical(Graphics2D g2, Rectangle2D dataArea, RectangleEdge edge) {
        final List ticks = super.refreshTicksVertical(g2, dataArea, edge);
        return tickAngle == 0d ? ticks : rotate(ticks);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List refreshTicksHorizontal(Graphics2D g2, Rectangle2D dataArea, RectangleEdge edge) {
        final List ticks = super.refreshTicksHorizontal(g2, dataArea, edge);
        return tickAngle == 0d ? ticks : rotate(ticks);
    }

    /**
     * Rotates date ticks by specific tick angle
     * @param ticks     the ticks to rotate
     * @return          the rotated ticks
     */
    private List<Tick> rotate(List<Tick> ticks) {
        try {
            final List<Tick> ticksRotated = new ArrayList<>();
            for (Tick tick : ticks) {
                if (tick instanceof DateTick) {
                    final DateTick dateTick = (DateTick)tick;
                    final Date date = dateTick.getDate();
                    final String tickLabel = dateTick.getText();
                    final TextAnchor tickAnchor = TextAnchor.BASELINE_RIGHT;
                    final TextAnchor rotationAnchor = TextAnchor.BASELINE_RIGHT;
                    ticksRotated.add(new DateTick(date, tickLabel, tickAnchor, rotationAnchor, tickAngle));
                } else {
                    ticksRotated.add(tick);
                }
            }
            return ticksRotated;
        } catch (Exception ex) {
            ex.printStackTrace();
            return ticks;
        }
    }
}
