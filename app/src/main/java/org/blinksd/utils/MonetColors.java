package org.blinksd.utils;

import static org.blinksd.utils.ResourcesUtils.getColor;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.res.Configuration;
import android.os.Build;

import org.blinksd.board.SuperBoardApplication;

import java.util.LinkedHashMap;

@SuppressLint("UseRequiresApi")
public class MonetColors extends LinkedHashMap<String, int[][]> {
    static final String COLOR_SCHEME_DEFAULT = "default";
    static final String COLOR_SCHEME_AMOLED = "amoled";

    @TargetApi(Build.VERSION_CODES.S)
    private static final int[] LIGHT_DEF_MONET_SCHEME = {
            android.R.color.system_neutral1_900, /* Key text color */
            android.R.color.system_neutral1_50,  /* Keyboard background color */
            android.R.color.system_neutral1_100, /* Key color */
            android.R.color.system_neutral1_200, /* Key press color */
            android.R.color.system_neutral1_200, /* Key2 color */
            android.R.color.system_neutral1_300, /* Key2 press color */
            android.R.color.system_accent1_300,  /* Enter color */
            android.R.color.system_accent1_400,  /* Enter press color */
    };

    @TargetApi(Build.VERSION_CODES.S)
    private static final int[] DARK_DEF_MONET_SCHEME = {
            android.R.color.system_neutral1_100, /* Key text color */
            android.R.color.system_neutral1_800, /* Keyboard background color */
            android.R.color.system_neutral1_600, /* Key color */
            android.R.color.system_neutral1_500, /* Key press color */
            android.R.color.system_neutral1_700, /* Key2 color */
            android.R.color.system_neutral1_600, /* Key2 press color */
            android.R.color.system_accent1_500,  /* Enter color */
            android.R.color.system_accent1_600,  /* Enter press color */
    };

    @TargetApi(Build.VERSION_CODES.S)
    private static final int[] AMOLED_MONET_SCHEME = {
            android.R.color.system_neutral1_200, /* Key text color */
            android.R.color.system_neutral1_1000, /* Keyboard background color */
            android.R.color.system_neutral1_800, /* Key color */
            android.R.color.system_neutral1_700, /* Key press color */
            android.R.color.system_neutral1_900, /* Key2 color */
            android.R.color.system_neutral1_800, /* Key2 press color */
            android.R.color.system_accent1_700,  /* Enter color */
            android.R.color.system_accent1_600,  /* Enter press color */
    };

    public MonetColors() {
        put(COLOR_SCHEME_DEFAULT, new int[][]{ LIGHT_DEF_MONET_SCHEME, DARK_DEF_MONET_SCHEME });
        put(COLOR_SCHEME_AMOLED, new int[][]{  LIGHT_DEF_MONET_SCHEME, AMOLED_MONET_SCHEME   });
    }

    public int getIndexByKey(String key) {
        assert key != null;
        int idx = 0;
        for (String nextKey : keySet()) {
            if (key.equals(nextKey)) {
                return idx;
            }
            ;
            idx++;
        }

        return -1;
    }

    public String getKeyByIndex(int index) {
        assert index >= 0;
        int idx = 0;
        for (String nextKey : keySet()) {
            if (idx == index) {
                return nextKey;
            }
            ;
            idx++;
        }

        return null;
    }

    public int getTextColor() {
        return getColorFromIndex(0);
    }

    public int getKeyboardColor() {
        return getColorFromIndex(1);
    }

    public int getKeyColor() {
        return getColorFromIndex(2);
    }

    public int getKeyPressColor() {
        return getColorFromIndex(3);
    }

    public int getKey2Color() {
        return getColorFromIndex(4);
    }

    public int getKey2PressColor() {
        return getColorFromIndex(5);
    }

    public int getEnterColor() {
        return getColorFromIndex(6);
    }

    public int getEnterPressColor() {
        return getColorFromIndex(7);
    }

    private int getColorFromIndex(int index) {
        switch (SuperDBHelper.getStringOrDefault(SettingMap.SET_MONET_COLOR_SCHEME)) {
            case COLOR_SCHEME_DEFAULT:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    return getColor(isDark() ? DARK_DEF_MONET_SCHEME[index] : LIGHT_DEF_MONET_SCHEME[index]);
                }
                break;
            case COLOR_SCHEME_AMOLED:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    return getColor(isDark() ? AMOLED_MONET_SCHEME[index] : LIGHT_DEF_MONET_SCHEME[index]);
                }
                break;
        }

        throw new RuntimeException("Pre-12 isn't supported yet");
    }

    private static boolean isDark() {
        Configuration conf = SuperBoardApplication.getResConfiguration();
        return (conf.uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }
}
