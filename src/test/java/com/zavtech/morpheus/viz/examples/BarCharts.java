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
package com.zavtech.morpheus.viz.examples;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.Base64;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.testng.annotations.Test;

import com.zavtech.morpheus.range.Range;
import com.zavtech.morpheus.util.IO;
import com.zavtech.morpheus.viz.chart.Chart;

import com.zavtech.morpheus.array.Array;
import com.zavtech.morpheus.frame.DataFrame;

/**
 * A bar chart example that displays GDP per capita data from the World Bank
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public class BarCharts {


    @Test()
    public void barChart1() throws Exception {

        Range<Year> years = Range.of(2000, 2006).map(Year::of);
        DataFrame<Year,String> data = DataFrame.of(years, String.class, columns -> {
            Stream.of("A", "B", "C", "D").forEach(label -> {
                columns.add(label, Array.of(Double.class, 6).applyDoubles(v -> Math.random()).cumSum());
            });
        });

        Chart.create().htmlMode();

        Chart.create().withBarPlot(data, false, chart -> {
            chart.plot().axes().domain().label().withText("Year");
            chart.plot().axes().range(0).label().withText("Random Value");
            chart.title().withText("Bar Chart - Categorical Domain Axis");
            chart.subtitle().withText("Cumulative Sum of Random Uniform Data");
            chart.legend().on();
            chart.writerPng(new File("../morpheus-docs/docs/images/charts/chart-bars-1.png"), 845, 450, true);
            chart.show();
        });

        Chart.create().withBarPlot(data, false, chart -> {
            chart.plot().orient().horizontal();
            chart.plot().axes().domain().label().withText("Year");
            chart.plot().axes().range(0).label().withText("Random Value");
            chart.title().withText("Bar Chart - Categorical Domain Axis");
            chart.subtitle().withText("Cumulative Sum of Random Uniform Data");
            chart.legend().on();
            chart.writerPng(new File("../morpheus-docs/docs/images/charts/chart-bars-2.png"), 845, 450, true);
            chart.show();
        });


        Thread.currentThread().join();
    }


    @Test()
    public void barChart2() throws Exception {

        int rowCount = 20;
        LocalDateTime start = LocalDateTime.of(2014, 1, 1, 8, 30);
        Range<LocalDateTime> rowKeys = Range.of(0, rowCount).map(i -> start.plusMinutes(i * 10));
        DataFrame<LocalDateTime,String> frame = DataFrame.of(rowKeys, String.class, columns -> {
            columns.add("A", Array.of(Double.class, rowCount).applyDoubles(v -> Math.random()).cumSum());
        });

        frame.out().print();

        Chart.create().htmlMode();

        Chart.create().withBarPlot(frame, false, chart -> {
            chart.plot().axes().domain().label().withText("Year");
            chart.plot().axes().range(0).label().withText("Random Value");
            chart.title().withText("Bar Chart - Continuous Domain Axis");
            chart.subtitle().withText("Cumulative Sum of Random Uniform Data");
            chart.legend().on();
            chart.writerPng(new File("../morpheus-docs/docs/images/charts/chart-bars-3.png"), 845, 450, true);
            chart.show();
        });

        Chart.create().withBarPlot(frame, false, chart -> {
            chart.plot().data().at(0).withLowerDomainInterval(t -> t.minusMinutes(10));
            chart.plot().axes().domain().label().withText("Year");
            chart.plot().axes().range(0).label().withText("Random Value");
            chart.title().withText("Bar Chart - Continuous Domain Axis");
            chart.subtitle().withText("Cumulative Sum of Random Uniform Data");
            chart.legend().on();
            chart.writerPng(new File("../morpheus-docs/docs/images/charts/chart-bars-4.png"), 845, 450, true);
            chart.show();
        });


        Thread.currentThread().join();
    }


    @Test()
    public void toBase64() throws IOException {
        final File file = new File("../morpheus-docs/docs/images/charts/chart-bars-2.png");
        final BufferedImage image = ImageIO.read(file);
        final String imageString = encodeToString(image, "png");
        IO.println(imageString);
    }




    String encodeToString(BufferedImage image, String type) throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(image, type, bos);
        final byte[] imageBytes = bos.toByteArray();
        final Base64.Encoder encoder = Base64.getEncoder();
        final byte[] base64 = encoder.encode(imageBytes);
        return new String(base64);
    }

}
