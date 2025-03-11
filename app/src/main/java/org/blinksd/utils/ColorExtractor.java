package org.blinksd.utils;

import static org.blinksd.utils.ColorUtils.HSLToColor;
import static org.blinksd.utils.ColorUtils.colorToHSL;

import android.graphics.Bitmap;
import android.util.Pair;

import org.blinksd.color_extractor.palette.Palette;
import org.blinksd.color_extractor.palette.extras.cam.Cam;
import org.blinksd.color_extractor.palette.quantizers.CelebiQuantizer;
import org.blinksd.color_extractor.palette.quantizers.ColorCutQuantizer;
import org.blinksd.color_extractor.palette.quantizers.Quantizer;
import org.blinksd.color_extractor.palette.quantizers.VariationalKMeansQuantizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("ConstantConditions")
public class ColorExtractor {
    // Maximum size that a bitmap can have to keep our calculations valid
    private static final int MAX_BITMAP_SIZE = 112;

    // Even though we have a maximum size, we'll mainly match bitmap sizes
    // using the area instead. This way our comparisons are aspect ratio independent.
    private static final int MAX_WALLPAPER_EXTRACTION_AREA = MAX_BITMAP_SIZE * MAX_BITMAP_SIZE;

    // When extracting the main colors, only consider colors
    // present in at least MIN_COLOR_OCCURRENCE of the image
    private static final float MIN_COLOR_OCCURRENCE = 0.05f;

    private final List<Integer> colorInts;
    private List<Map<Integer, Integer>> colorMapCache;

    private ColorExtractor(List<Integer> colorInts) {
        this.colorInts = colorInts;
    }

    public static ColorExtractor extractFromBitmap(
            Bitmap bitmap, QuantizerType quantizerType, int maxColors, int correction) {
        assert bitmap != null : "bitmap cannot be null";
        assert quantizerType != null : "quantizerType cannot be null";
        assert maxColors >= 1 : "maxColors should be >= 1";
        assert correction >= 1 : "correction should be >= 1";

        final int bitmapArea = bitmap.getWidth() * bitmap.getHeight();
        boolean shouldRecycle = false;
        if (bitmapArea > MAX_WALLPAPER_EXTRACTION_AREA) {
            shouldRecycle = true;
            Pair<Integer, Integer> optimalSize = calculateOptimalSize(bitmap.getWidth(), bitmap.getHeight());
            bitmap = Bitmap.createScaledBitmap(bitmap, optimalSize.first,
                    optimalSize.second, true /* filter */);
        }

        Quantizer quantizer;
        switch (quantizerType) {
            case CELEBI:
                quantizer = new CelebiQuantizer();
                break;
            case COLOR_CUT:
                quantizer = new ColorCutQuantizer();
                break;
            case VAR_K_MEANS:
                quantizer = new VariationalKMeansQuantizer();
                break;
            default:
                throw new RuntimeException("Unknown quantizer type");
        }

        final Palette palette = Palette
                .from(bitmap, quantizer)
                .maximumColorCount(/* 12 */ maxColors)
                .resizeBitmapArea(MAX_WALLPAPER_EXTRACTION_AREA)
                .generate();

        List<Palette.Swatch> swatches = new ArrayList<>();
        final float minColorArea = bitmap.getWidth() * bitmap.getHeight() * MIN_COLOR_OCCURRENCE;
        for (Palette.Swatch swatch : palette.getSwatches()) {
            if (swatch.getPopulation() >= minColorArea) {
                swatches.add(swatch);
            }
        }
        Collections.sort(swatches, (a, b) -> b.getPopulation() - a.getPopulation());

        if (shouldRecycle) {
            bitmap.recycle();
        }

        final Map<Integer, Integer> colorToPopulation = new HashMap<>();
        for (int i = 0; i < swatches.size(); i++) {
            Palette.Swatch swatch = swatches.get(i);
            int colorInt = swatch.getInt();
            colorToPopulation.put(colorInt, swatch.getPopulation());
        }

        final Map<Integer, Cam> colorToCam = new HashMap<>();
        for (int color : colorToPopulation.keySet()) {
            colorToCam.put(color, Cam.fromInt(color));
        }
        final double[] hueProportions = hueProportions(colorToCam, colorToPopulation);
        final Map<Integer, Double> colorToHueProportion = colorToHueProportion(
                colorToPopulation.keySet(), colorToCam, hueProportions);

        final Map<Integer, Double> colorToScore = new HashMap<>();
        for (Map.Entry<Integer, Double> mapEntry : colorToHueProportion.entrySet()) {
            int color = mapEntry.getKey();
            double proportion = mapEntry.getValue();
            double score = score(colorToCam.get(color), proportion);
            colorToScore.put(color, score);
        }
        ArrayList<Map.Entry<Integer, Double>> mapEntries = new ArrayList<>(colorToScore.entrySet());
        Collections.sort(mapEntries, (a, b) -> b.getValue().compareTo(a.getValue()));

        List<Integer> colorsByScoreDescending = new ArrayList<>();
        for (Map.Entry<Integer, Double> colorToScoreEntry : mapEntries) {
            colorsByScoreDescending.add(colorToScoreEntry.getKey());
        }

        List<Integer> mainColorInts = new ArrayList<>();
        findSeedColorLoop:
        for (int color : colorsByScoreDescending) {
            Cam cam = colorToCam.get(color);
            for (int otherColor : mainColorInts) {
                Cam otherCam = colorToCam.get(otherColor);
                if (hueDiff(cam, otherCam) < /* 15 */ correction) {
                    continue findSeedColorLoop;
                }
            }
            mainColorInts.add(color);
        }

        clearDuplicates(mainColorInts);
        return new ColorExtractor(mainColorInts);
    }

    public final List<Map<Integer, Integer>> mapColors() {
        if (colorMapCache == null) {
            List<Map<Integer, Integer>> colorMaps = new ArrayList<>();
            for (int colorInt : colorInts) {
                Map<Integer, Integer> colorMap = new LinkedHashMap<>();
                for (int i = 5; i < 95; i += 5) {
                    colorMap.put(i * 10, getColorWithBrightness(colorInt, i / 100f));
                }

                colorMaps.add(colorMap);
            }

            // find opposite color if there's no another color
            if (colorMaps.size() == 1) {
                Map<Integer, Integer> second = new LinkedHashMap<>();
                for (int key : colorMaps.get(0).keySet()) {
                    float[] hsl = new float[3];
                    ColorUtils.colorToHSL(colorMaps.get(0).get(key), hsl);
                    hsl[0] = (hsl[0] + 180) % 360;
                    second.put(key, ColorUtils.HSLToColor(hsl));
                }
                colorMaps.add(second);
            }

            colorMapCache = colorMaps;
        }

        return colorMapCache;
    }

    private static void clearDuplicates(List<Integer> colors) {
        ArrayList<Integer> notDuplicates = new ArrayList<>();
        for(int item : colors)
            if(!notDuplicates.contains(item))
                notDuplicates.add(item);
        colors.clear();
        colors.addAll(notDuplicates);
    }

    private static int wrapDegrees(int degrees) {
        if (degrees < 0) {
            return (degrees % 360) + 360;
        } else if (degrees >= 360) {
            return degrees % 360;
        } else {
            return degrees;
        }
    }

    private static double[] hueProportions(Map<Integer, Cam> colorToCam,
                                           Map<Integer, Integer> colorToPopulation) {
        final double[] proportions = new double[360];

        double totalPopulation = 0;
        for (Map.Entry<Integer, Integer> entry : colorToPopulation.entrySet()) {
            totalPopulation += entry.getValue();
        }

        for (Map.Entry<Integer, Integer> entry : colorToPopulation.entrySet()) {
            final int color = (int) entry.getKey();
            final int population = colorToPopulation.get(color);
            final Cam cam = colorToCam.get(color);
            final int hue = wrapDegrees(Math.round(cam.getHue()));
            proportions[hue] = proportions[hue] + ((double) population / totalPopulation);
        }

        return proportions;
    }

    private static double hueDiff(Cam a, Cam b) {
        return (180f - Math.abs(Math.abs(a.getHue() - b.getHue()) - 180f));
    }

    private static double score(Cam cam, double proportion) {
        return cam.getChroma() + (proportion * 100);
    }

    private static Map<Integer, Double> colorToHueProportion(Set<Integer> colors,
                                                             Map<Integer, Cam> colorToCam, double[] hueProportions) {
        Map<Integer, Double> colorToHueProportion = new HashMap<>();
        for (int color : colors) {
            final int hue = wrapDegrees(Math.round(colorToCam.get(color).getHue()));
            double proportion = 0.0;
            for (int i = hue - 15; i < hue + 15; i++) {
                proportion += hueProportions[wrapDegrees(i)];
            }
            colorToHueProportion.put(color, proportion);
        }
        return colorToHueProportion;
    }

    private static Pair<Integer, Integer> calculateOptimalSize(int width, int height) {
        // Calculate how big the bitmap needs to be.
        // This avoids unnecessary processing and allocation inside Palette.
        final int requestedArea = width * height;
        double scale = 1;
        if (requestedArea > MAX_WALLPAPER_EXTRACTION_AREA) {
            scale = Math.sqrt(MAX_WALLPAPER_EXTRACTION_AREA / (double) requestedArea);
        }
        int newWidth = (int) (width * scale);
        int newHeight = (int) (height * scale);
        // Dealing with edge cases of the drawable being too wide or too tall.
        // Width or height would end up being 0, in this case we'll set it to 1.
        if (newWidth == 0) {
            newWidth = 1;
        }
        if (newHeight == 0) {
            newHeight = 1;
        }

        return new Pair<>(newWidth, newHeight);
    }

    private static int getColorWithBrightness(int color, float brightness) {
        if(color == 0)
            return color;

        float[] hsl = new float[3];
        colorToHSL(color, hsl);

        hsl[1] = brightness >= .5f ? (1f - brightness) / 2 : brightness;
        hsl[2] = 1f - brightness;

        return HSLToColor(hsl);
    }

    public enum QuantizerType {
        CELEBI,
        COLOR_CUT,
        VAR_K_MEANS,
    }
}
