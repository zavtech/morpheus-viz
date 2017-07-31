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
package com.zavtech.morpheus.viz.util;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;


/**
 * An interface to a ColorModel of distinct high contrast colors that can be associated with keys
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public interface ColorModel {

    Supplier<ColorModel> DEFAULT = DefaultColors::new;
    Supplier<ColorModel> KELLY = KellyColors::new;
    Supplier<ColorModel> GOLDEN_RATIO = GoldenRatioColors::new;
    Supplier<ColorModel> CIE_MODEL = CIEModel::new;

    /**
     * Registers a color for the key specified
     * @param key   the key for color
     * @param color the color reference
     * @return the prior color, could be null
     */
    Color put(Object key, Color color);

    /**
     * Returns the color for the key specified
     * @param key the key for requested color
     * @return the color for key, randomly assigned if not configured
     */
    Color getColor(Object key);

    /**
     * Resets this color model
     * @return  this color model
     */
    ColorModel reset();

    /**
     * Returns a HEX string representation of the color
     * @param color     the color instance
     * @return          the HEX string
     */
    static String toHexString(Color color) {
        final int r = color.getRed();
        final int g = color.getGreen();
        final int b = color.getBlue();
        return String.format("#%02x%02x%02x", r, g, b);
    }
}



/**
 * A convenience base class for building fixed size color models
 */
abstract class ColorModelBase implements ColorModel {

    private Map<Object,Color> colorMap = new HashMap<>();

    /**
     * Constructor
     */
    ColorModelBase() {
        super();
    }

    /**
     * Returns the next most distinct color for this model
     * @return      the next most distinct color model
     */
    protected abstract Color next();

    @Override()
    public ColorModel reset() {
        this.colorMap.clear();
        return this;
    }

    @Override()
    public Color put(Object key, Color color) {
        return colorMap.put(key, color);
    }

    @Override()
    public Color getColor(Object key) {
        Color color = colorMap.get(key);
        if (color == null) {
            color = next();
            colorMap.put(key, color);
        }
        return color;
    }
}


/**
 * A convenience base class for building fixed size color models.
 */
abstract class ColorModelFixed extends ColorModelBase {

    private int index = -1;
    private Color[] colors;

    /**
     * Constructor
     */
    ColorModelFixed() {
        this.colors = createColors();
    }

    /**
     * Returns the distinct colors for this model
     * @return      the distinct colors for this model
     */
    abstract Color[] createColors();


    @Override
    protected Color next() {
        index++;
        if (index >= colors.length) {
            index = 0;
            return colors[index];
        } else {
            return colors[index];
        }
    }
}


/**
 * https://sashat.me/2017/01/11/list-of-20-simple-distinct-colors/
 */
class DefaultColors extends ColorModelFixed {
    @Override
    Color[] createColors() {
        return new Color[] {
            new Color(230, 25, 75),     // red
            new Color(60, 180, 75),     // green
            new Color(255, 225, 25),    // yellow
            new Color(0, 130, 200),     // blue
            new Color(245, 130, 48),    // orange
            new Color(145, 30, 180),    // purple
            new Color(70, 240, 240),    // cyan
            new Color(240, 50, 230),    // magenta
            new Color(210, 245, 60),    // lime
            new Color(250, 190, 190),   // pink
            new Color(0, 128, 128),     // teal
            new Color(230, 190, 255),   // lavendar
            new Color(170, 110, 40),    // brown
            new Color(255, 250, 200),   // beige
            new Color(128, 0, 0),       // maroon
            new Color(170, 255, 195),   // mint
            new Color(128, 128, 0),     // olive
            new Color(255, 215, 180),   // coral
            new Color(0, 0, 200),       // navy
            new Color(128, 128, 128),   // grey
            new Color(196, 68, 65),     // pale-red
            new Color(140, 188, 79),    // light green
            new Color(122, 88, 146),    // light purple
            new Color(29, 115, 170),    // pale-blue
            new Color(255, 200, 25),    // yellow
            new Color(0, 154, 178),     // blue-green
            new Color(244, 133, 51),    // light orange
            new Color(139, 170, 209),   // light blue
            new Color(180, 56, 148),    // light purple
        };
    }
}


/**
 * Kelly's 22 colors of Maximum Contrast
 */
class KellyColors extends ColorModelFixed {
    @Override
    Color[] createColors() {
        return new Color[] {
            Color.decode("0xFFB300"),    // Vivid Yellow
            Color.decode("0x803E75"),    // Strong Purple
            Color.decode("0xFF6800"),    // Vivid Orange
            Color.decode("0xA6BDD7"),    // Very Light Blue
            Color.decode("0xC10020"),    // Vivid Red
            Color.decode("0xCEA262"),    // Grayish Yellow
            Color.decode("0x817066"),    // Medium Gray
            Color.decode("0x007D34"),    // Vivid Green
            Color.decode("0xF6768E"),    // Strong Purplish Pink
            Color.decode("0x00538A"),    // Strong Blue
            Color.decode("0xFF7A5C"),    // Strong Yellowish Pink
            Color.decode("0x53377A"),    // Strong Violet
            Color.decode("0xFF8E00"),    // Vivid Orange Yellow
            Color.decode("0xB32851"),    // Strong Purplish Red
            Color.decode("0xF4C800"),    // Vivid Greenish Yellow
            Color.decode("0x7F180D"),    // Strong Reddish Brown
            Color.decode("0x93AA00"),    // Vivid Yellowish Green
            Color.decode("0x593315"),    // Deep Yellowish Brown
            Color.decode("0xF13A13"),    // Vivid Reddish Orange
            Color.decode("0x232C16")     // Dark Olive Green
        };
    }

    public static void main(String[] args) {
        for (Color color : new KellyColors().createColors()) {
            System.out.printf("\n(%s, %s, %s)", color.getRed(), color.getGreen(), color.getBlue());
        }
    }
}


/**
 * A color model based on 128 statically defined colors
 */
class CIEModel extends ColorModelFixed {
    @Override
    Color[] createColors() {
        return new Color[]{
            Color.decode("#FFFF00"),
            Color.decode("#1CE6FF"),
            Color.decode("#FF34FF"),
            Color.decode("#FF4A46"),
            Color.decode("#008941"),
            Color.decode("#006FA6"),
            Color.decode("#A30059"),
            Color.decode("#FFDBE5"),
            Color.decode("#7A4900"),
            Color.decode("#0000A6"),
            Color.decode("#63FFAC"),
            Color.decode("#B79762"),
            Color.decode("#004D43"),
            Color.decode("#8FB0FF"),
            Color.decode("#997D87"),
            Color.decode("#5A0007"),
            Color.decode("#809693"),
            Color.decode("#FEFFE6"),
            Color.decode("#1B4400"),
            Color.decode("#4FC601"),
            Color.decode("#3B5DFF"),
            Color.decode("#4A3B53"),
            Color.decode("#FF2F80"),
            Color.decode("#61615A"),
            Color.decode("#BA0900"),
            Color.decode("#6B7900"),
            Color.decode("#00C2A0"),
            Color.decode("#FFAA92"),
            Color.decode("#FF90C9"),
            Color.decode("#B903AA"),
            Color.decode("#D16100"),
            Color.decode("#DDEFFF"),
            Color.decode("#000035"),
            Color.decode("#7B4F4B"),
            Color.decode("#A1C299"),
            Color.decode("#300018"),
            Color.decode("#0AA6D8"),
            Color.decode("#013349"),
            Color.decode("#00846F"),
            Color.decode("#372101"),
            Color.decode("#FFB500"),
            Color.decode("#C2FFED"),
            Color.decode("#A079BF"),
            Color.decode("#CC0744"),
            Color.decode("#C0B9B2"),
            Color.decode("#C2FF99"),
            Color.decode("#001E09"),
            Color.decode("#00489C"),
            Color.decode("#6F0062"),
            Color.decode("#0CBD66"),
            Color.decode("#EEC3FF"),
            Color.decode("#456D75"),
            Color.decode("#B77B68"),
            Color.decode("#7A87A1"),
            Color.decode("#788D66"),
            Color.decode("#885578"),
            Color.decode("#FAD09F"),
            Color.decode("#FF8A9A"),
            Color.decode("#D157A0"),
            Color.decode("#BEC459"),
            Color.decode("#456648"),
            Color.decode("#0086ED"),
            Color.decode("#886F4C"),
            Color.decode("#34362D"),
            Color.decode("#B4A8BD"),
            Color.decode("#00A6AA"),
            Color.decode("#452C2C"),
            Color.decode("#636375"),
            Color.decode("#A3C8C9"),
            Color.decode("#FF913F"),
            Color.decode("#938A81"),
            Color.decode("#575329"),
            Color.decode("#00FECF"),
            Color.decode("#B05B6F"),
            Color.decode("#8CD0FF"),
            Color.decode("#3B9700"),
            Color.decode("#04F757"),
            Color.decode("#C8A1A1"),
            Color.decode("#1E6E00"),
            Color.decode("#7900D7"),
            Color.decode("#A77500"),
            Color.decode("#6367A9"),
            Color.decode("#A05837"),
            Color.decode("#6B002C"),
            Color.decode("#772600"),
            Color.decode("#D790FF"),
            Color.decode("#9B9700"),
            Color.decode("#549E79"),
            Color.decode("#FFF69F"),
            Color.decode("#201625"),
            Color.decode("#72418F"),
            Color.decode("#BC23FF"),
            Color.decode("#99ADC0"),
            Color.decode("#3A2465"),
            Color.decode("#922329"),
            Color.decode("#5B4534"),
            Color.decode("#FDE8DC"),
            Color.decode("#404E55"),
            Color.decode("#0089A3"),
            Color.decode("#CB7E98"),
            Color.decode("#A4E804"),
            Color.decode("#324E72"),
            Color.decode("#6A3A4C"),
            Color.decode("#83AB58"),
            Color.decode("#001C1E"),
            Color.decode("#D1F7CE"),
            Color.decode("#004B28"),
            Color.decode("#C8D0F6"),
            Color.decode("#A3A489"),
            Color.decode("#806C66"),
            Color.decode("#222800"),
            Color.decode("#BF5650"),
            Color.decode("#E83000"),
            Color.decode("#66796D"),
            Color.decode("#DA007C"),
            Color.decode("#FF1A59"),
            Color.decode("#8ADBB4"),
            Color.decode("#1E0200"),
            Color.decode("#5B4E51"),
            Color.decode("#C895C5"),
            Color.decode("#320033"),
            Color.decode("#FF6832"),
            Color.decode("#66E1D3"),
            Color.decode("#CFCDAC"),
            Color.decode("#D0AC94"),
            Color.decode("#7ED379"),
            Color.decode("#012C58")
        };
    }
}



/**
 * A color model based on a Golden Ratio algorithm
 */
class GoldenRatioColors extends ColorModelBase {

    private float h = 0.8f;
    private static final float s = 0.5f;
    private static final float v = 0.95f;
    private static final double goldenRatio = 0.618033988749895;


    @Override()
    protected Color next() {
        h += goldenRatio;
        h %= 1d;
        int i;
        float m, n, f;

        float[] hsv = new float[3];
        float[] rgb = new float[3];

        hsv[0] = h;
        hsv[1] = s;
        hsv[2] = v;

        if (hsv[0] == -1) {
            rgb[0] = rgb[1] = rgb[2] = hsv[2];
            return new Color(rgb[0], rgb[1], rgb[2]);
        }
        i = (int) (Math.floor(hsv[0]));
        f = hsv[0] - i;
        if (i % 2 == 0) {
            f = 1 - f;
        }
        m = hsv[2] * (1 - hsv[1]);
        n = hsv[2] * (1 - hsv[1] * f);
        switch (i) {
            case 6:
            case 0:
                rgb[0] = hsv[2];
                rgb[1] = n;
                rgb[2] = m;
                break;
            case 1:
                rgb[0] = n;
                rgb[1] = hsv[2];
                rgb[2] = m;
                break;
            case 2:
                rgb[0] = m;
                rgb[1] = hsv[2];
                rgb[2] = n;
                break;
            case 3:
                rgb[0] = m;
                rgb[1] = n;
                rgb[2] = hsv[2];
                break;
            case 4:
                rgb[0] = n;
                rgb[1] = m;
                rgb[2] = hsv[2];
                break;
            case 5:
                rgb[0] = hsv[2];
                rgb[1] = m;
                rgb[2] = n;
                break;
        }
        return new Color(rgb[0], rgb[1], rgb[2]);
    }
}



