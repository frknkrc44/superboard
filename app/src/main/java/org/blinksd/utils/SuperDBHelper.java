package org.blinksd.utils;

import static org.blinksd.board.SuperBoardApplication.getAppDB;
import static org.blinksd.board.SuperBoardApplication.getMonetColors;
import static org.blinksd.board.SuperBoardApplication.getSettings;
import static org.blinksd.utils.ColorUtils.invertColor;
import static org.blinksd.utils.ColorUtils.satisfiesTextContrast;
import static org.blinksd.utils.DensityUtils.minPInt;
import static org.blinksd.utils.SystemUtils.isDarkThemeEnabled;

import android.content.Context;
import android.graphics.Bitmap;

import org.blinksd.board.views.ImageSelectorLayout;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class SuperDBHelper {
    private static final List<String> EXCEPT_PROPS = Arrays.asList(
            // don't export theme props
            SettingMap.SET_KEYBOARD_TEXTTYPE_SELECT,
            SettingMap.SET_ICON_THEME,
            SettingMap.SET_KEYBOARD_BGCLR,
            SettingMap.SET_KEY_BGCLR,
            SettingMap.SET_KEY_PRESS_BGCLR,
            SettingMap.SET_KEY2_BGCLR,
            SettingMap.SET_KEY2_PRESS_BGCLR,
            SettingMap.SET_ENTER_BGCLR,
            SettingMap.SET_ENTER_PRESS_BGCLR,
            SettingMap.SET_KEY_SHADOWCLR,
            SettingMap.SET_KEY_TEXTCLR,
            SettingMap.SET_KEY_PADDING,
            SettingMap.SET_KEY_RADIUS,
            SettingMap.SET_KEY_TEXTSIZE,
            SettingMap.SET_KEY_SHADOWSIZE,
            SettingMap.SET_KEY_BG_TYPE,

            // don't export the clipboard history for privacy
            SettingMap.SET_CLIPBOARD_HISTORY
    );

    private SuperDBHelper() {}

    public static SuperDBExt getDefault(Context c) {
        return new SuperDBExt(c.getPackageName(), c.getFilesDir(), false);
    }

    public static String getStringOrDefault(String key) {
        SuperDBExt db = getAppDB();
        String ret = "";
        if (!db.isDBContainsKey(key)) {
            var defValue = getSettings().getDefaults(key);
            if (defValue == null) {
                return "";
            }

            return defValue.toString();
        }

        return db.getString(key, ret);
    }

    public static int getFloatPercentOrDefault(String key) {
        return minPInt(getFloatedIntOrDefault(key));
    }

    public static float getFloatedIntOrDefault(String key) {
        return DensityUtils.getFloatNumberFromInt(getIntOrDefault(key));
    }

    public static int getIntOrDefault(String key) {
        if (getMonetColors().isMonetEnabled()) {
            return getMonetColorValue(key);
        }

        return Integer.parseInt(getStringOrDefault(key));
    }

    public static boolean getBooleanOrDefault(String key) {
        return Boolean.parseBoolean(getStringOrDefault(key));
    }

    public static boolean getBooleanOrDefaultResolved(String key) {
        if (isBooleanDependencyResolved(key)) {
            return getBooleanOrDefault(key);
        }

        return false;
    }

    private static boolean isBooleanDependencyResolved(final String key) {
        SettingItem item = getSettings().get(key);
        List<String> checkedKeys = new ArrayList<>();
        checkedKeys.add(key);

        while (item != null && item.dependency != null && !checkedKeys.contains(item.dependency)) {
            boolean depValue = getBooleanOrDefault(item.dependency);
            if (item.dependencyEnabled != depValue) return false;

            checkedKeys.add(item.dependency);
            item = getSettings().get(item.dependency);
        }

        return true;
    }

    private static int getMonetColorValue(String key) {
        MonetColors monetColors = getMonetColors();
        return switch (key) {
            case SettingMap.SET_KEY_STROKE_COLOR -> 0; // TODO: Add monet stroke color
            case SettingMap.SET_ENTER_BGCLR -> monetColors.getEnterColor();
            case SettingMap.SET_ENTER_PRESS_BGCLR -> monetColors.getEnterPressColor();
            case SettingMap.SET_KEY_BGCLR -> monetColors.getKeyColor();
            case SettingMap.SET_KEY_PRESS_BGCLR -> monetColors.getKeyPressColor();
            case SettingMap.SET_KEY2_BGCLR -> monetColors.getKey2Color();
            case SettingMap.SET_KEY2_PRESS_BGCLR -> monetColors.getKey2PressColor();
            case SettingMap.SET_KEYBOARD_BGCLR -> monetColors.getKeyboardColor();
            case SettingMap.SET_KEY_TEXTCLR, SettingMap.SET_KEY2_TEXTCLR -> monetColors.getTextColor();
            case SettingMap.SET_ENTER_TEXTCLR -> isDarkThemeEnabled() && satisfiesTextContrast(
                        monetColors.getEnterColor(),
                        monetColors.getTextColor())
                    ? invertColor(monetColors.getTextColor())
                    : monetColors.getTextColor();
            default -> Integer.parseInt(getStringOrDefault(key));
        };
    }

    public static void removeKeyFromDB(String key) {
        SuperDBExt db = getAppDB();
        if (db.isDBContainsKey(key)) {
            db.removeKeyFromDB(key);
        }
    }

    public static void importAllFromJSON(JSONObject json) throws JSONException {
        Iterator<String> it = json.keys();

        while(it.hasNext()) {
            String key = it.next();
            getAppDB().putString(key, json.getString(key));
        }

        getAppDB().writeAll();
    }

    public static void setColorsFromBitmap(Bitmap b, ImageSelectorLayout.ColorScheme scheme) {
        var colors = calculateColorsForScheme(b, scheme);
        if (colors == null) return;

        assert colors.length == 11 : "The colors array size is wrong";

        getAppDB().putInteger(SettingMap.SET_KEYBOARD_BGCLR,    colors[ 0]);
        getAppDB().putInteger(SettingMap.SET_KEY_BGCLR,         colors[ 1]);
        getAppDB().putInteger(SettingMap.SET_KEY2_BGCLR,        colors[ 2]);
        getAppDB().putInteger(SettingMap.SET_KEY_PRESS_BGCLR,   colors[ 3]);
        getAppDB().putInteger(SettingMap.SET_KEY2_PRESS_BGCLR,  colors[ 4]);
        getAppDB().putInteger(SettingMap.SET_ENTER_BGCLR,       colors[ 5]);
        getAppDB().putInteger(SettingMap.SET_ENTER_PRESS_BGCLR, colors[ 6]);
        getAppDB().putInteger(SettingMap.SET_KEY_TEXTCLR,       colors[ 7]);
        getAppDB().putInteger(SettingMap.SET_KEY2_TEXTCLR,      colors[ 8]);
        getAppDB().putInteger(SettingMap.SET_ENTER_TEXTCLR,     colors[ 9]);
        getAppDB().putInteger(SettingMap.SET_KEY_SHADOWCLR,     colors[10]);
        getAppDB().writeAll();
    }

    /**
     * Calculate and return colors from bitmap by using the color scheme
     * <br><br>
     * Returned output:
     * <pre>
     *     int[] {
     *          "keyboard background color",
     *          "key normal background color",
     *          "key pressed background color",
     *          "key2 normal background color",
     *          "key2 pressed background color",
     *          "enter normal background color",
     *          "enter pressed background color",
     *          "key text color",
     *          "key2 text color",
     *          "enter text color",
     *          "text shadow color",
     *     }
     * </pre>
     *
     * @param b input image
     * @param scheme color scheme to generate colors
     * @return an int array with colors
     */
    public static int[] calculateColorsForScheme(Bitmap b, ImageSelectorLayout.ColorScheme scheme) {
        if (b == null) return null;

        final int c = ColorUtils.getBitmapColor(b);
        final int keyClr = c - 0xAA000000;

        switch (scheme) {
            case COLORFUL_V1 -> {
                final int keyPressClr = ColorUtils.getDarkerColor(keyClr);
                final int keyPress2Clr = ColorUtils.getDarkerColor(keyPressClr);
                final int enterPressClr = ColorUtils.getDarkerColor(keyPress2Clr);
                final int textClr = satisfiesTextContrast(c) ? 0xFF212121 : 0xFFDEDEDE;

                return new int[] {
                        keyClr,               /* keyboard background color */
                        keyClr,               /* key normal background color */
                        keyPressClr,          /* key pressed background color */
                        keyPressClr,          /* key2 normal background color */
                        keyPress2Clr,         /* key2 pressed background color */
                        keyPress2Clr,         /* enter normal background color */
                        enterPressClr,        /* enter pressed background color */
                        textClr,              /* key text color */
                        textClr,              /* key2 text color */
                        textClr,              /* enter text color */
                        invertColor(textClr), /* text shadow color */
                };
            }

            case COLORFUL_V2 -> {
                final int bgClr = 0xAA000000;
                final int keyPressClr = ColorUtils.getDarkerColor(keyClr);
                final int keyPress2Clr = ColorUtils.getDarkerColor(keyPressClr);
                final int enterPressClr = ColorUtils.getDarkerColor(keyPress2Clr);
                final int textClr = satisfiesTextContrast(c) ? 0xFF212121 : 0xFFDEDEDE;

                return new int[] {
                        bgClr,                /* keyboard background color */
                        0x00000000,           /* key normal background color */
                        keyPressClr,          /* key pressed background color */
                        keyPressClr,          /* key2 normal background color */
                        keyPress2Clr,         /* key2 pressed background color */
                        keyPress2Clr,         /* enter normal background color */
                        enterPressClr,        /* enter pressed background color */
                        textClr,              /* key text color */
                        textClr,              /* key2 text color */
                        textClr,              /* enter text color */
                        invertColor(textClr), /* text shadow color */
                };
            }

            default -> {
                return null;
            }
        }
    }

    public static Map<String, String> exportAllToMap(List<String> except) {
        Map<String, String> exportMap = new HashMap<>();

        for (String key : getAppDB().getKeys()) {
            if (except.contains(key)) {
                continue;
            }

            String value = getAppDB().getString(key, null);
            if (value != null) {
                exportMap.put(key, value);
            }
        }

        return exportMap;
    }

    public static Map<String, String> exportAllExceptTheme() {
        return exportAllToMap(EXCEPT_PROPS);
    }
}
