package org.blinksd.sdb;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import org.blinksd.SuperBoardApplication;
import org.blinksd.board.SettingMap;
import org.blinksd.sdb.SuperMiniDB;
import org.blinksd.utils.layout.DensityUtils;

public class SuperDBHelper {

    private SuperDBHelper() {
    }

    public static SuperMiniDB getDefault(Context c) {
        return new SuperMiniDB(c.getPackageName(), c.getFilesDir());
    }

    public static SuperMiniDB getDefault(Context c, String key) {
        return new SuperMiniDB(c.getPackageName(), c.getFilesDir(), key);
    }

    public static String getValueOrDefault(String key) {
        SuperMiniDB db = SuperBoardApplication.getApplicationDatabase();
        String ret = "";
        if (!db.isDBContainsKey(key)) {
            db.putString(key, "" + SuperBoardApplication.getSettings().getDefaults(key));
            db.refreshKey(key);
        }
        return db.getString(key, ret);
    }

    public static float getFloatedIntValueOrDefault(String key) {
        return DensityUtils.getFloatNumberFromInt(getIntValueOrDefault(key));
    }

    public static int getIntValueOrDefault(String key) {
        if (Build.VERSION.SDK_INT >= 31 && getBooleanValueOrDefault(SettingMap.SET_USE_MONET)) {
            return getMonetColorValue(key);
        }

        return Integer.parseInt(getValueOrDefault(key));
    }

    public static boolean getBooleanValueOrDefault(String key) {
        return Boolean.parseBoolean(getValueOrDefault(key));
    }

    public static long getLongValueOrDefault(String key) {
        return Long.parseLong(getValueOrDefault(key));
    }

    public static float getFloatValueOrDefault(String key) {
        return Float.parseFloat(getValueOrDefault(key));
    }

    public static double getDoubleValueOrDefault(String key) {
        return Double.parseDouble(getValueOrDefault(key));
    }

    public static byte getByteValueOrDefault(String key) {
        return Byte.parseByte(getValueOrDefault(key));
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

        return Integer.parseInt(getValueOrDefault(key));
    }
}
