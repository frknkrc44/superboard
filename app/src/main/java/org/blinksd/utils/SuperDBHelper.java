package org.blinksd.utils;

import static org.blinksd.board.SuperBoardApplication.getAppDB;
import static org.blinksd.board.SuperBoardApplication.getSettings;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;

import org.blinksd.board.SuperBoardApplication;
import org.frknkrc44.minidb.SuperMiniDB;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class SuperDBHelper {
    private static final List<String> THEME_PROPS = Arrays.asList(
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

    public static SuperMiniDB getDefault(Context c) {
        return new SuperMiniDB(c.getPackageName(), c.getFilesDir(), false);
    }

    public static String getStringOrDefault(String key) {
        SuperMiniDB db = SuperBoardApplication.getAppDB();
        String ret = "";
        if (!db.isDBContainsKey(key)) {
            return getSettings().getDefaults(key).toString();
        }

        return db.getString(key, ret);
    }

    public static int getFloatPercentOrDefault(String key) {
        return DensityUtils.mpInt(DensityUtils.getFloatNumberFromInt(getIntOrDefault(key)));
    }

    public static float getFloatedIntOrDefault(String key) {
        return DensityUtils.getFloatNumberFromInt(getIntOrDefault(key));
    }

    public static int getIntOrDefault(String key) {
        if (SuperBoardApplication.getMonetColors().isMonetEnabled()) {
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
            if ((boolean) item.dependencyEnabled != depValue) return false;

            checkedKeys.add(item.dependency);
            item = getSettings().get(item.dependency);
        }

        return true;
    }

    private static int getMonetColorValue(String key) {
        MonetColors monetColors = SuperBoardApplication.getMonetColors();
        return switch (key) {
            case SettingMap.SET_ENTER_BGCLR -> monetColors.getEnterColor();
            case SettingMap.SET_ENTER_PRESS_BGCLR -> monetColors.getEnterPressColor();
            case SettingMap.SET_KEY_BGCLR -> monetColors.getKeyColor();
            case SettingMap.SET_KEY_PRESS_BGCLR -> monetColors.getKeyPressColor();
            case SettingMap.SET_KEY2_BGCLR -> monetColors.getKey2Color();
            case SettingMap.SET_KEY2_PRESS_BGCLR -> monetColors.getKey2PressColor();
            case SettingMap.SET_KEYBOARD_BGCLR -> monetColors.getKeyboardColor();
            case SettingMap.SET_KEY_TEXTCLR -> monetColors.getTextColor();
            default -> Integer.parseInt(getStringOrDefault(key));
        };
    }

    public static void removeKey(String key) {
        SuperMiniDB db = SuperBoardApplication.getAppDB();
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

    public static void setColorsFromBitmap(Bitmap b) {
        if (b == null) return;
        int c = ColorUtils.getBitmapColor(b);
        getAppDB().putInteger(SettingMap.SET_KEYBOARD_BGCLR, c - 0xAA000000);
        int keyClr = c - 0xAA000000;
        int keyPressClr = ColorUtils.getDarkerColor(keyClr);
        int keyPress2Clr = ColorUtils.getDarkerColor(keyPressClr);
        getAppDB().putInteger(SettingMap.SET_KEY_BGCLR, keyClr);
        getAppDB().putInteger(SettingMap.SET_KEY2_BGCLR, keyPressClr);
        getAppDB().putInteger(SettingMap.SET_KEY_PRESS_BGCLR, keyPressClr);
        getAppDB().putInteger(SettingMap.SET_KEY2_PRESS_BGCLR, keyPress2Clr);
        boolean isLight = ColorUtils.satisfiesTextContrast(c);
        getAppDB().putInteger(SettingMap.SET_ENTER_BGCLR, ColorUtils.getDarkerColor(keyPress2Clr));
        keyClr = isLight ? 0xFF212121 : 0xFFDEDEDE;
        getAppDB().putInteger(SettingMap.SET_KEY_TEXTCLR, keyClr);
        getAppDB().putInteger(SettingMap.SET_KEY_SHADOWCLR, keyClr ^ 0x00FFFFFF);
        getAppDB().writeAll();
    }

    public static Map<String, String> exportAllToMap(List<String> except) {
        Map<String, String> exportMap = new HashMap<>();

        for (String key : SuperBoardApplication.getAppDB().getKeys()) {
            if (except.contains(key)) {
                continue;
            }

            String value = SuperBoardApplication.getAppDB().getString(key, null);
            if (value != null) {
                exportMap.put(key, value);
            }
        }

        return exportMap;
    }

    public static Map<String, String> exportAllExceptTheme() {
        return exportAllToMap(THEME_PROPS);
    }
}
