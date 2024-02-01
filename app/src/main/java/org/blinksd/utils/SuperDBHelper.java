package org.blinksd.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
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

@SuppressWarnings("unused")
public class SuperDBHelper {
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

    public static SuperMiniDB getDefaultAsync(Context c, Runnable onLoadFinished) {
        return new SuperMiniDB(c.getPackageName(), c.getFilesDir(), onLoadFinished);
    }

    public static String getStringOrDefault(String key) {
        SuperMiniDB db = SuperBoardApplication.getAppDB();
        String ret = "";
        if (!db.isDBContainsKey(key)) {
            db.putString(key, String.valueOf(
                    SuperBoardApplication.getSettings().getDefaults(key)), true);
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
        if (Build.VERSION.SDK_INT >= 31 && getBooleanOrDefault(SettingMap.SET_USE_MONET)) {
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
        SettingItem item = SuperBoardApplication.getSettings().get(key);
        List<String> checkedKeys = new ArrayList<>();
        checkedKeys.add(key);

        while (item.dependency != null && !checkedKeys.contains(item.dependency)) {
            boolean depValue = getBooleanOrDefault(item.dependency);
            if ((boolean) item.dependencyEnabled != depValue) return false;

            checkedKeys.add(item.dependency);
            item = SuperBoardApplication.getSettings().get(item.dependency);
        }

        return true;
    }

    public static long getLongOrDefault(String key) {
        return Long.parseLong(getStringOrDefault(key));
    }

    public static float getFloatOrDefault(String key) {
        return Float.parseFloat(getStringOrDefault(key));
    }

    public static double getDoubleOrDefault(String key) {
        return Double.parseDouble(getStringOrDefault(key));
    }

    public static byte getByteOrDefault(String key) {
        return Byte.parseByte(getStringOrDefault(key));
    }

    @TargetApi(31)
    private static int getMonetColorValue(String key) {
        Resources res = SuperBoardApplication.getApplication().getResources();
        Configuration conf = res.getConfiguration();
        boolean dark = (conf.uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        switch (key) {
            case SettingMap.SET_ENTER_BGCLR:
                return res.getColor(dark ? android.R.color.system_accent1_500 : android.R.color.system_accent1_300);
            case SettingMap.SET_ENTER_PRESS_BGCLR:
                return res.getColor(dark ? android.R.color.system_accent1_600 : android.R.color.system_accent1_400);
            case SettingMap.SET_KEY_BGCLR:
                return res.getColor(dark ? android.R.color.system_neutral1_600 : android.R.color.system_neutral1_100);
            case SettingMap.SET_KEY_PRESS_BGCLR:
                return res.getColor(dark ? android.R.color.system_neutral1_500 : android.R.color.system_neutral1_200);
            case SettingMap.SET_KEY2_BGCLR:
                return res.getColor(dark ? android.R.color.system_neutral1_700 : android.R.color.system_neutral1_200);
            case SettingMap.SET_KEY2_PRESS_BGCLR:
                return res.getColor(dark ? android.R.color.system_neutral1_600 : android.R.color.system_neutral1_300);
            case SettingMap.SET_KEYBOARD_BGCLR:
                return res.getColor(dark ? android.R.color.system_neutral1_800 : android.R.color.system_neutral1_50);
            case SettingMap.SET_KEY_TEXTCLR:
                return res.getColor(dark ? android.R.color.system_neutral1_100 : android.R.color.system_neutral1_900);
        }

        return Integer.parseInt(getStringOrDefault(key));
    }

    public static void importAllFromJSON(JSONObject json) throws JSONException {
        Map<String, String> importMap = new HashMap<>();
        Iterator<String> it = json.keys();

        while(it.hasNext()) {
            String key = it.next();
            importMap.put(key, json.getString(key));
        }

        importAllFromMap(importMap);
    }

    public static void importAllFromMap(Map<String, String> map) {
        SuperBoardApplication.getAppDB().putDatabaseDump(map);
        SuperBoardApplication.getAppDB().writeAll();
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
