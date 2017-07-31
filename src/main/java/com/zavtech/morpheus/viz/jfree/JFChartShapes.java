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

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

import com.zavtech.morpheus.viz.chart.ChartShape;

/**
 * A class that manages a mapping between Morpheus ChartShape definitions and their AWT shape representation
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
class JFChartShapes {

    private Map<ChartShape,Shape> shapeMap = new HashMap<>();

    /**
     * Constructor
     */
    JFChartShapes() {
        this(6d);
    }

    /**
     * Constructor
     * @param size  the size for shapes
     */
    private JFChartShapes(double size) {
        final double delta = size / 2.0;
        shapeMap.put(ChartShape.SQUARE, new Rectangle2D.Double(-delta, -delta, size, size));
        shapeMap.put(ChartShape.CIRCLE, new Ellipse2D.Double(-delta, -delta, size, size));
        shapeMap.put(ChartShape.DIAMOND, new Polygon(intArray(0.0, delta, 0.0, -delta), intArray(-delta, 0.0, delta, 0.0), 4));
        shapeMap.put(ChartShape.TRIANGLE_UP, new Polygon(intArray(0.0, delta, -delta), intArray(-delta, delta, delta), 3));
        shapeMap.put(ChartShape.TRIANGLE_DOWN, new Polygon(intArray(-delta, +delta, 0.0), intArray(-delta, -delta, delta), 3));
        shapeMap.put(ChartShape.TRIANGLE_RIGHT, new Polygon(intArray(0.0, delta, 0.0, -delta), intArray(-delta, 0.0, delta, 0.0), 4));
        shapeMap.put(ChartShape.TRIANGLE_LEFT, new Polygon(intArray(-delta, delta, delta), intArray(0.0, -delta, +delta), 3));
    }

    /**
     * Returns the AWT shape object for the chart shape specifed
     * @param shape the chart shape identifier
     * @return      the AWT shape object, if one exists, otherwise null
     */
    Shape getShape(ChartShape shape) {
        return shapeMap.get(shape);
    }

    private static int[] intArray(double a, double b, double c) {
        return new int[] {(int) a, (int) b, (int) c};
    }

    private static int[] intArray(double a, double b, double c, double d) {
        return new int[] {(int) a, (int) b, (int) c, (int) d};
    }

}
