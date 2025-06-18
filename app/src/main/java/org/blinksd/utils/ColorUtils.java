package org.blinksd.utils;

import static android.graphics.Color.alpha;
import static android.graphics.Color.argb;
import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.luminance;
import static android.graphics.Color.red;
import static android.graphics.Color.rgb;
import static org.blinksd.board.SuperBoardApplication.getSBApplication;

import android.annotation.SuppressLint;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.widget.ImageView;

// Copied from support library
// Android Open Source Project

@SuppressWarnings({"deprecation", "all"})
public final class ColorUtils {
    private static final ThreadLocal<double[]> TEMP_ARRAY = new ThreadLocal<>();

    private ColorUtils() {}

    public static void setColorFilter(Drawable drawable, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            drawable.setColorFilter(new BlendModeColorFilter(color, BlendMode.SRC_ATOP));
        } else {
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }
    }

    public static void setColorFilter(ImageView view, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            view.setColorFilter(new BlendModeColorFilter(color, BlendMode.SRC_ATOP));
        } else {
            view.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }
    }

    public static int getColorFromHSV(float hue, float sat, float val) {
        if (hue > 360f) {
            hue = 360f;
        } else if (hue < 0f) {
            hue = 0f;
        }

        return Color.HSVToColor(new float[]{hue, 1f - (sat / 100f), val / 100f});
    }

    public static int[] getHSVFromColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);

        int[] out = new int[3];

        out[0] = (int) hsv[0];
        out[1] = (int) ((1f - hsv[1]) * 100);
        out[2] = (int) (hsv[2] * 100);

        return out;
    }

    public static int compositeColors(int foreground, int background) {
        int bgAlpha = alpha(background);
        int fgAlpha = alpha(foreground);
        int a = compositeAlpha(fgAlpha, bgAlpha);
        int r = compositeComponent(red(foreground), fgAlpha,
                red(background), bgAlpha, a);
        int g = compositeComponent(green(foreground), fgAlpha,
                green(background), bgAlpha, a);
        int b = compositeComponent(blue(foreground), fgAlpha,
                blue(background), bgAlpha, a);
        return argb(a, r, g, b);
    }

    private static int compositeAlpha(int foregroundAlpha, int backgroundAlpha) {
        return 0xFF - (((0xFF - backgroundAlpha) * (0xFF - foregroundAlpha)) / 0xFF);
    }

    private static int compositeComponent(int fgC, int fgA, int bgC, int bgA, int a) {
        if (a == 0) return 0;
        return ((0xFF * fgC * fgA) + (bgC * bgA * (0xFF - fgA))) / (a * 0xFF);
    }

    public static double calculateLuminance(int color) {
        if (Build.VERSION.SDK_INT >= 24)
            return luminance(color);

        final double[] result = getTempDouble3Array();
        colorToXYZ(color, result);
        // Luminance is the Y component
        return result[1] / 100;
    }

    public static void RGBToHSL(int r, int g, int b, float[] outHsl) {
        final float rf = r / 255f;
        final float gf = g / 255f;
        final float bf = b / 255f;

        final float max = Math.max(rf, Math.max(gf, bf));
        final float min = Math.min(rf, Math.min(gf, bf));
        final float deltaMaxMin = max - min;

        float h, s;
        float l = (max + min) / 2f;

        if (max == min) {
            // Monochromatic
            h = s = 0f;
        } else {
            if (max == rf) {
                h = ((gf - bf) / deltaMaxMin) % 6f;
            } else if (max == gf) {
                h = ((bf - rf) / deltaMaxMin) + 2f;
            } else {
                h = ((rf - gf) / deltaMaxMin) + 4f;
            }

            s = deltaMaxMin / (1f - Math.abs(2f * l - 1f));
        }

        h = (h * 60f) % 360f;
        if (h < 0) {
            h += 360f;
        }

        outHsl[0] = constrain(h, 360f);
        outHsl[1] = constrain(s, 1f);
        outHsl[2] = constrain(l, 1f);
    }

    public static void colorToHSL(int color, float[] outHsl) {
        RGBToHSL(red(color), green(color), blue(color), outHsl);
    }

    public static int HSLToColor(float[] hsl) {
        final float h = hsl[0];
        final float s = hsl[1];
        final float l = hsl[2];

        final float c = (1f - Math.abs(2 * l - 1f)) * s;
        final float m = l - 0.5f * c;
        final float x = c * (1f - Math.abs((h / 60f % 2f) - 1f));

        final int hueSegment = (int) h / 60;

        int r = 0, g = 0, b = 0;

        switch (hueSegment) {
            case 0:
                r = Math.round(255 * (c + m));
                g = Math.round(255 * (x + m));
                b = Math.round(255 * m);
                break;
            case 1:
                r = Math.round(255 * (x + m));
                g = Math.round(255 * (c + m));
                b = Math.round(255 * m);
                break;
            case 2:
                r = Math.round(255 * m);
                g = Math.round(255 * (c + m));
                b = Math.round(255 * (x + m));
                break;
            case 3:
                r = Math.round(255 * m);
                g = Math.round(255 * (x + m));
                b = Math.round(255 * (c + m));
                break;
            case 4:
                r = Math.round(255 * (x + m));
                g = Math.round(255 * m);
                b = Math.round(255 * (c + m));
                break;
            case 5:
            case 6:
                r = Math.round(255 * (c + m));
                g = Math.round(255 * m);
                b = Math.round(255 * (x + m));
                break;
        }

        r = constrain(r);
        g = constrain(g);
        b = constrain(b);

        return rgb(r, g, b);
    }

    public static double[] getTempDouble3Array() {
        double[] result = TEMP_ARRAY.get();
        if (result == null) {
            result = new double[3];
            TEMP_ARRAY.set(result);
        }
        return result;
    }

    public static double calculateContrast(int foreground, int background) {
        if (alpha(foreground) < 255) {
            // If the foreground is translucent, composite the foreground over the background
            foreground = compositeColors(foreground, background);
        }

        final double luminance1 = calculateLuminance(foreground) + 0.05;
        final double luminance2 = calculateLuminance(background) + 0.05;

        // Now return the lighter luminance divided by the darker luminance
        return Math.max(luminance1, luminance2) / Math.min(luminance1, luminance2);
    }

    public static void colorToXYZ(int color, double[] outXyz) {
        RGBToXYZ(red(color), green(color), blue(color), outXyz);
    }

    public static void RGBToXYZ(int r, int g, int b, double[] outXyz) {
        if (outXyz.length != 3) {
            throw new IllegalArgumentException("outXyz must have a length of 3.");
        }

        double sr = r / 255.0;
        sr = sr < 0.04045 ? sr / 12.92 : Math.pow((sr + 0.055) / 1.055, 2.4);
        double sg = g / 255.0;
        sg = sg < 0.04045 ? sg / 12.92 : Math.pow((sg + 0.055) / 1.055, 2.4);
        double sb = b / 255.0;
        sb = sb < 0.04045 ? sb / 12.92 : Math.pow((sb + 0.055) / 1.055, 2.4);

        outXyz[0] = 100 * (sr * 0.4124 + sg * 0.3576 + sb * 0.1805);
        outXyz[1] = 100 * (sr * 0.2126 + sg * 0.7152 + sb * 0.0722);
        outXyz[2] = 100 * (sr * 0.0193 + sg * 0.1192 + sb * 0.9505);
    }

    public static int XYZToColor(double x, double y, double z) {
        double r = (x * 3.2406 + y * -1.5372 + z * -0.4986) / 100;
        double g = (x * -0.9689 + y * 1.8758 + z * 0.0415) / 100;
        double b = (x * 0.0557 + y * -0.2040 + z * 1.0570) / 100;

        r = r > 0.0031308 ? 1.055 * Math.pow(r, 1 / 2.4) - 0.055 : 12.92 * r;
        g = g > 0.0031308 ? 1.055 * Math.pow(g, 1 / 2.4) - 0.055 : 12.92 * g;
        b = b > 0.0031308 ? 1.055 * Math.pow(b, 1 / 2.4) - 0.055 : 12.92 * b;

        return rgb(
                constrain((int) Math.round(r * 255)),
                constrain((int) Math.round(g * 255)),
                constrain((int) Math.round(b * 255)));
    }

    public static boolean satisfiesTextContrast(int color) {
        return satisfiesTextContrast(color, 0xFF000000);
    }

    public static boolean satisfiesTextContrast(int backgroundColor, int foregroundColor) {
        if (alpha(backgroundColor) > 0x88)
            return calculateContrast(foregroundColor, backgroundColor) >= 10;
        return false;
    }

    public static int invertColor(int color) {
        return color ^ 0x00FFFFFF;
    }

    public static int getBitmapColor(Bitmap bitmap) {
        if (bitmap == null) return 0xFF000000;
        bitmap = Bitmap.createScaledBitmap(bitmap, 64, 64, false);
        int width = bitmap.getWidth(), height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        int color, count = 0, r = 0, g = 0, b = 0, a;
        for (int pixel : pixels) {
            color = pixel;
            a = alpha(color);
            if (a > 0) {
                color = (a < 255) ? convertARGBtoRGB(color) : color;
                r += red(color);
                g += green(color);
                b += blue(color);
                count++;
            }
        }
        if (r == g && g == b && r == 0) {
            count = 1;
        }
        r /= count;
        g /= count;
        b /= count;
        r = (r << 16) & 0x00FF0000;
        g = (g << 8) & 0x0000FF00;
        b = b & 0x000000FF;
        color = 0xFF000000 | r | g | b;
        return color;
    }

    public static int convertARGBtoRGB(int color) {
        return setAlphaForColor(0xFF, color);
    }

    public static int setAlphaForColor(int alpha, int color) {
        return argb(alpha, red(color), green(color), blue(color));
    }

    public static int getDarkerColor(int color) {
        int[] state = {red(color), green(color), blue(color)};
        for (int i = 0; i < state.length; i++) {
            state[i] = (int) (state[i] / 1.2f);
        }
        return argb(alpha(color), state[0], state[1], state[2]);
    }

    @SuppressLint("UseRequiresApi")
    public static int getAccentColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ResourcesUtils.getColor(android.R.color.system_accent1_700);
        }

        TypedArray arr = getSBApplication()
                .obtainStyledAttributes(0, new int[]{android.R.attr.colorAccent});
        int color = arr.getColor(0, Defaults.ENTER_BACKGROUND_COLOR);
        arr.recycle();

        try {
            arr.close();
        } catch (Throwable ignored) {}

        return color;
    }

    private static float constrain(float amount, float high) {
        return amount < 0.0f ? 0.0f : Math.min(amount, high);
    }

    private static int constrain(int amount) {
        return amount < 0 ? 0 : Math.min(amount, 255);
    }

    public static String colorIntToString(int a, int r, int g, int b) {
        return colorIntToString(a, r, g, b, true);
    }

    public static String colorIntToString(int a, int r, int g, int b, boolean addHash) {
        return colorIntToString(argb(a, r, g, b), addHash);
    }

    public static String colorIntToString(int colorInt) {
        return colorIntToString(colorInt, true);
    }

    public static String colorIntToString(int colorInt, boolean addHash) {
        return String.format(addHash ? "#%08X" : "%08X", colorInt);
    }
}
