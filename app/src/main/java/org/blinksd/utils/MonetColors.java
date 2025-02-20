package org.blinksd.utils;

import static android.content.res.Configuration.UI_MODE_NIGHT_MASK;
import static android.content.res.Configuration.UI_MODE_NIGHT_YES;
import static org.blinksd.utils.ResourcesUtils.getColor;
import static org.blinksd.utils.SystemUtils.isPermGranted;

import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;

import org.blinksd.board.SuperBoardApplication;
import org.blinksd.board.services.KeyboardThemeApi;

import java.util.LinkedHashMap;
import java.util.Map;

@SuppressLint({"InlinedApi"})
public class MonetColors extends LinkedHashMap<String, int[][]> {
    static final String COLOR_SCHEME_DEFAULT = "default";
    static final String COLOR_SCHEME_AMOLED = "amoled";
    private WallpaperManager wallpaperManager;

    private static final int[] LIGHT_DEF_MONET_SCHEME = {
            android.R.color.system_neutral1_900,  /* Key text color */
            android.R.color.system_neutral1_50,   /* Keyboard background color */
            android.R.color.system_neutral1_100,  /* Key color */
            android.R.color.system_neutral1_200,  /* Key press color */
            android.R.color.system_neutral1_200,  /* Key2 color */
            android.R.color.system_neutral1_300,  /* Key2 press color */
            android.R.color.system_accent1_300,   /* Enter color */
            android.R.color.system_accent1_400,   /* Enter press color */
    };

    private static final int[] DARK_DEF_MONET_SCHEME = {
            android.R.color.system_neutral1_100,  /* Key text color */
            android.R.color.system_neutral1_800,  /* Keyboard background color */
            android.R.color.system_neutral1_600,  /* Key color */
            android.R.color.system_neutral1_500,  /* Key press color */
            android.R.color.system_neutral1_700,  /* Key2 color */
            android.R.color.system_neutral1_600,  /* Key2 press color */
            android.R.color.system_accent1_500,   /* Enter color */
            android.R.color.system_accent1_600,   /* Enter press color */
    };

    private static final int[] AMOLED_MONET_SCHEME = {
            android.R.color.system_neutral1_200,  /* Key text color */
            android.R.color.system_neutral1_1000, /* Keyboard background color */
            android.R.color.system_neutral1_800,  /* Key color */
            android.R.color.system_neutral1_700,  /* Key press color */
            android.R.color.system_neutral1_900,  /* Key2 color */
            android.R.color.system_neutral1_800,  /* Key2 press color */
            android.R.color.system_accent1_700,   /* Enter color */
            android.R.color.system_accent1_600,   /* Enter press color */
    };

    private ColorExtractor colorExtractor;

    public MonetColors() {
        put(COLOR_SCHEME_DEFAULT, new int[][]{LIGHT_DEF_MONET_SCHEME, DARK_DEF_MONET_SCHEME});
        put(COLOR_SCHEME_AMOLED, new int[][]{LIGHT_DEF_MONET_SCHEME, AMOLED_MONET_SCHEME});
    }

    public boolean isMonetEnabled() {
        return isSystemMonetEnabled() || SuperDBHelper.getBooleanOrDefault(SettingMap.SET_USE_COMPAT_MONET);
    }

    private boolean isSystemMonetEnabled() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                SuperDBHelper.getBooleanOrDefault(SettingMap.SET_USE_MONET);
    }

    public final int getSelectedMonetThemeIndex() {
        return getIndexByKey(SuperDBHelper.getStringOrDefault(SettingMap.SET_MONET_COLOR_SCHEME));
    }

    private int getIndexByKey(String key) {
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

    public final String getKeyByIndex(int index) {
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

    public final int getTextColor() {
        return getColorFromIndex(0);
    }

    public final int getKeyboardColor() {
        return getColorFromIndex(1);
    }

    public final int getKeyColor() {
        return getColorFromIndex(2);
    }

    public final int getKeyPressColor() {
        return getColorFromIndex(3);
    }

    public final int getKey2Color() {
        return getColorFromIndex(4);
    }

    public final int getKey2PressColor() {
        return getColorFromIndex(5);
    }

    public final int getEnterColor() {
        return getColorFromIndex(6);
    }

    public final int getEnterPressColor() {
        return getColorFromIndex(7);
    }

    @SuppressWarnings({"deprecation", "ConstantConditions"})
    private int getColorCompat(int resId) {
        Context context = SuperBoardApplication.getApplication();
        if (wallpaperManager == null) {
            try {
                wallpaperManager = (WallpaperManager) context.getSystemService(Context.WALLPAPER_SERVICE);
            } catch (Throwable ignored) {}

            BroadcastReceiver mOnWallpaperChangedListener = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    colorExtractor = null;
                    KeyboardThemeApi.restartKeyboard();
                }
            };

            SuperBoardApplication.getApplication().registerReceiver(
                    mOnWallpaperChangedListener,
                    new IntentFilter(Intent.ACTION_WALLPAPER_CHANGED)
            );
        }

        if (colorExtractor == null) {
            reloadColors(context);
        }

        if (colorExtractor == null) {
            return 0;
        }

        var mappedColors = colorExtractor.mapColors();
        Map<Integer, Integer> first, second;
        try {
            first = mappedColors.get(0);
            second = mappedColors.get(1);
        } catch (Throwable ignored) {
            return 0;
        }

        return switch (resId) {
            case android.R.color.system_accent1_0, android.R.color.system_accent2_0,
                 android.R.color.system_accent3_0, android.R.color.system_neutral1_0,
                 android.R.color.system_neutral2_0 -> Color.WHITE;
            case android.R.color.system_accent1_1000, android.R.color.system_accent2_1000,
                 android.R.color.system_accent3_1000, android.R.color.system_neutral1_1000,
                 android.R.color.system_neutral2_1000 -> Color.BLACK;
            case android.R.color.system_accent1_50, android.R.color.system_accent1_100,
                 android.R.color.system_accent1_200, android.R.color.system_accent1_300,
                 android.R.color.system_accent1_400, android.R.color.system_accent1_500,
                 android.R.color.system_accent1_600, android.R.color.system_accent1_700,
                 android.R.color.system_accent1_800, android.R.color.system_accent1_900 ->
                    first.get((resId - android.R.color.system_accent1_50) * 100);
            case android.R.color.system_neutral1_50, android.R.color.system_neutral1_100,
                 android.R.color.system_neutral1_200, android.R.color.system_neutral1_300,
                 android.R.color.system_neutral1_400, android.R.color.system_neutral1_500,
                 android.R.color.system_neutral1_600, android.R.color.system_neutral1_700,
                 android.R.color.system_neutral1_800, android.R.color.system_neutral1_900 ->
                    second.get(Math.max((resId - android.R.color.system_neutral1_50) * 100, 50));
            default -> throw new RuntimeException("Unsupported res " + resId);
        };

    }

    @SuppressLint("MissingPermission")
    public void reloadColors(Context context) {
        try {
            if (isPermGranted(context)) {
                Drawable wallpaperDrawable;
                if (wallpaperManager.getWallpaperInfo() != null) {
                    wallpaperDrawable = wallpaperManager.getWallpaperInfo().loadThumbnail(context.getPackageManager());
                } else {
                    wallpaperDrawable = wallpaperManager.getDrawable();
                }
                wallpaperManager.forgetLoadedWallpaper();

                if (wallpaperDrawable instanceof BitmapDrawable bitmapDrawable) {
                    colorExtractor = ColorExtractor.extractFromBitmap(
                            bitmapDrawable.getBitmap(),
                            ColorExtractor.QuantizerType.VAR_K_MEANS,
                            SuperDBHelper.getIntOrDefault(SettingMap.SET_COMPAT_MONET_MAX_COLORS),
                            15
                    );
                }
            }
        } catch (Throwable ignored) {}
    }

    private int getColorFromIndex(int index) {
        return switch (SuperDBHelper.getStringOrDefault(SettingMap.SET_MONET_COLOR_SCHEME)) {
            case COLOR_SCHEME_DEFAULT -> {
                final var resId = isDark() ? DARK_DEF_MONET_SCHEME[index] : LIGHT_DEF_MONET_SCHEME[index];
                yield isSystemMonetEnabled() ? getColor(resId) : getColorCompat(resId);
            }
            case COLOR_SCHEME_AMOLED -> {
                final var resId = isDark() ? AMOLED_MONET_SCHEME[index] : LIGHT_DEF_MONET_SCHEME[index];
                yield isSystemMonetEnabled() ? getColor(resId) : getColorCompat(resId);
            }
            default -> 0;
        };
    }

    private static boolean isDark() {
        return (SuperBoardApplication.getResConfiguration().uiMode & UI_MODE_NIGHT_MASK) == UI_MODE_NIGHT_YES;
    }
}
