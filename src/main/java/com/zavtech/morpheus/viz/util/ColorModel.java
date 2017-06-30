package com.zavtech.morpheus.viz.util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ColorModel {

    private Map<Object, Color> colorMap = new HashMap<>();

    /**
     * Constructor
     */
    public ColorModel() {
        super();
    }

    /**
     * Resets this color model
     */
    public void reset() {
        this.colorMap.clear();
    }

    /**
     * Registers a color for the key specified
     * @param key   the key for color
     * @param color the color reference
     * @return the prior color, could be null
     */
    public Color put(Object key, Color color) {
        return colorMap.put(key, color);
    }

    /**
     * Returns the color for the key specified
     * @param key the key for requested color
     * @return the color for key, randomly assigned if not configured
     */
    public Color getColor(Object key) {
        Color color = colorMap.get(key);
        if (color == null) {
            color = next();
            colorMap.put(key, color);
        }
        return color;
    }

    /**
     * Returns the next color for this model
     *
     * @return the next unclaimed color for this model
     */
    protected abstract Color next();


    public static Color createColor(float h, float s, float v) {
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


    public static class GoldenRatioColorModel extends ColorModel {

        private double h = 0.8;
        private double goldenRatio = 0.618033988749895;

        @Override()
        protected Color next() {
            h += goldenRatio;
            h %= 1d;
            return createColor((float) h, 0.5f, 0.95f);
        }
    }


    /**
     * A color model based on 128 statically defined colors
     */
    public static class CIEModel extends ColorModel {

        private static final List<Color> palette = new ArrayList<>();

        /**
         * Static initializer
         */
        static {
            //palette.add(Color.decode("#000000"));
            palette.add(Color.decode("#FFFF00"));
            palette.add(Color.decode("#1CE6FF"));
            palette.add(Color.decode("#FF34FF"));
            palette.add(Color.decode("#FF4A46"));
            palette.add(Color.decode("#008941"));
            palette.add(Color.decode("#006FA6"));
            palette.add(Color.decode("#A30059"));
            palette.add(Color.decode("#FFDBE5"));
            palette.add(Color.decode("#7A4900"));
            palette.add(Color.decode("#0000A6"));
            palette.add(Color.decode("#63FFAC"));
            palette.add(Color.decode("#B79762"));
            palette.add(Color.decode("#004D43"));
            palette.add(Color.decode("#8FB0FF"));
            palette.add(Color.decode("#997D87"));
            palette.add(Color.decode("#5A0007"));
            palette.add(Color.decode("#809693"));
            palette.add(Color.decode("#FEFFE6"));
            palette.add(Color.decode("#1B4400"));
            palette.add(Color.decode("#4FC601"));
            palette.add(Color.decode("#3B5DFF"));
            palette.add(Color.decode("#4A3B53"));
            palette.add(Color.decode("#FF2F80"));
            palette.add(Color.decode("#61615A"));
            palette.add(Color.decode("#BA0900"));
            palette.add(Color.decode("#6B7900"));
            palette.add(Color.decode("#00C2A0"));
            palette.add(Color.decode("#FFAA92"));
            palette.add(Color.decode("#FF90C9"));
            palette.add(Color.decode("#B903AA"));
            palette.add(Color.decode("#D16100"));
            palette.add(Color.decode("#DDEFFF"));
            palette.add(Color.decode("#000035"));
            palette.add(Color.decode("#7B4F4B"));
            palette.add(Color.decode("#A1C299"));
            palette.add(Color.decode("#300018"));
            palette.add(Color.decode("#0AA6D8"));
            palette.add(Color.decode("#013349"));
            palette.add(Color.decode("#00846F"));
            palette.add(Color.decode("#372101"));
            palette.add(Color.decode("#FFB500"));
            palette.add(Color.decode("#C2FFED"));
            palette.add(Color.decode("#A079BF"));
            palette.add(Color.decode("#CC0744"));
            palette.add(Color.decode("#C0B9B2"));
            palette.add(Color.decode("#C2FF99"));
            palette.add(Color.decode("#001E09"));
            palette.add(Color.decode("#00489C"));
            palette.add(Color.decode("#6F0062"));
            palette.add(Color.decode("#0CBD66"));
            palette.add(Color.decode("#EEC3FF"));
            palette.add(Color.decode("#456D75"));
            palette.add(Color.decode("#B77B68"));
            palette.add(Color.decode("#7A87A1"));
            palette.add(Color.decode("#788D66"));
            palette.add(Color.decode("#885578"));
            palette.add(Color.decode("#FAD09F"));
            palette.add(Color.decode("#FF8A9A"));
            palette.add(Color.decode("#D157A0"));
            palette.add(Color.decode("#BEC459"));
            palette.add(Color.decode("#456648"));
            palette.add(Color.decode("#0086ED"));
            palette.add(Color.decode("#886F4C"));
            palette.add(Color.decode("#34362D"));
            palette.add(Color.decode("#B4A8BD"));
            palette.add(Color.decode("#00A6AA"));
            palette.add(Color.decode("#452C2C"));
            palette.add(Color.decode("#636375"));
            palette.add(Color.decode("#A3C8C9"));
            palette.add(Color.decode("#FF913F"));
            palette.add(Color.decode("#938A81"));
            palette.add(Color.decode("#575329"));
            palette.add(Color.decode("#00FECF"));
            palette.add(Color.decode("#B05B6F"));
            palette.add(Color.decode("#8CD0FF"));
            palette.add(Color.decode("#3B9700"));
            palette.add(Color.decode("#04F757"));
            palette.add(Color.decode("#C8A1A1"));
            palette.add(Color.decode("#1E6E00"));
            palette.add(Color.decode("#7900D7"));
            palette.add(Color.decode("#A77500"));
            palette.add(Color.decode("#6367A9"));
            palette.add(Color.decode("#A05837"));
            palette.add(Color.decode("#6B002C"));
            palette.add(Color.decode("#772600"));
            palette.add(Color.decode("#D790FF"));
            palette.add(Color.decode("#9B9700"));
            palette.add(Color.decode("#549E79"));
            palette.add(Color.decode("#FFF69F"));
            palette.add(Color.decode("#201625"));
            palette.add(Color.decode("#72418F"));
            palette.add(Color.decode("#BC23FF"));
            palette.add(Color.decode("#99ADC0"));
            palette.add(Color.decode("#3A2465"));
            palette.add(Color.decode("#922329"));
            palette.add(Color.decode("#5B4534"));
            palette.add(Color.decode("#FDE8DC"));
            palette.add(Color.decode("#404E55"));
            palette.add(Color.decode("#0089A3"));
            palette.add(Color.decode("#CB7E98"));
            palette.add(Color.decode("#A4E804"));
            palette.add(Color.decode("#324E72"));
            palette.add(Color.decode("#6A3A4C"));
            palette.add(Color.decode("#83AB58"));
            palette.add(Color.decode("#001C1E"));
            palette.add(Color.decode("#D1F7CE"));
            palette.add(Color.decode("#004B28"));
            palette.add(Color.decode("#C8D0F6"));
            palette.add(Color.decode("#A3A489"));
            palette.add(Color.decode("#806C66"));
            palette.add(Color.decode("#222800"));
            palette.add(Color.decode("#BF5650"));
            palette.add(Color.decode("#E83000"));
            palette.add(Color.decode("#66796D"));
            palette.add(Color.decode("#DA007C"));
            palette.add(Color.decode("#FF1A59"));
            palette.add(Color.decode("#8ADBB4"));
            palette.add(Color.decode("#1E0200"));
            palette.add(Color.decode("#5B4E51"));
            palette.add(Color.decode("#C895C5"));
            palette.add(Color.decode("#320033"));
            palette.add(Color.decode("#FF6832"));
            palette.add(Color.decode("#66E1D3"));
            palette.add(Color.decode("#CFCDAC"));
            palette.add(Color.decode("#D0AC94"));
            palette.add(Color.decode("#7ED379"));
            palette.add(Color.decode("#012C58"));
        }

        private int index = -1;

        @Override
        protected Color next() {
            index++;
            if (index >= palette.size()) {
                index = 0;
                return palette.get(index);
            } else {
                return palette.get(index);
            }
        }
    }
}
