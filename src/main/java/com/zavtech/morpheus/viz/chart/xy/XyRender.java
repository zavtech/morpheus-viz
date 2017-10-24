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
package com.zavtech.morpheus.viz.chart.xy;

/**
 * An interface to the rendering controller for an XyPlot
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public interface XyRender {

    /**
     * Configures the renderer to draw dots in a scatter plot
     */
    void withDots();

    /**
     * Configures the renderer to draw dots in a scatter plot
     * @param diameter  the diameter of dots measured in pixels
     */
    void withDots(int diameter);

    /**
     * Configures the renderer to draw shapes in a scatter plot
     */
    void withShapes();

    /**
     * Configures the renderer to draw lines with optional shapes and dashes
     * @param shapes    if true, include shapes at each datum
     * @param dashed    if true, make the line dashed
     */
    void withLines(boolean shapes, boolean dashed);

    /**
     * Configures the renderer to draw splines with optional shapes and dashes
     * @param shapes    if true, include shapes at each datum
     * @param dashed    if true, make the line dashed
     */
    void withSpline(boolean shapes, boolean dashed);

    /**
     * Configures the renderer to draw bars either stacked or unstacked
     * @param stacked   if true, the bars will be stacked
     * @param margin    the margin between the bars
     */
    void withBars(boolean stacked, double margin);

    /**
     * Configures the renderer to draw area either stacked or unstacked
     * @param stacked   if true, the areas will be stacked
     */
    void withArea(boolean stacked);

}
