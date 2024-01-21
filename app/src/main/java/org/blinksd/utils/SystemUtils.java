package org.blinksd.utils;

import static android.os.Build.VERSION.SDK_INT;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.provider.Settings;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowManager;

import org.blinksd.board.InputService;
import org.blinksd.board.SuperBoardApplication;

import java.lang.reflect.Method;

public class SystemUtils {

    public static boolean isNotColorizeNavbar() {
        return getSystemProp("ro.build.version.emui").length() > 1;
    }

    @SuppressLint("PrivateApi")
    public static String getSystemProp(String key) {
        try {
            Class<?> propClass = Class.forName("android.os.SystemProperties");
            Method getMethod = propClass.getMethod("get", String.class);
            return (String) getMethod.invoke(null, key);
        } catch (Throwable ignored) {}
        return "";
    }

    @SuppressLint("PrivateApi")
    public static boolean detectNavbar(InputService inputService) {
        if (SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return WindowManagerServiceUtils.hasNavigationBar(inputService);
        }

        return (!(KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK) &&
                KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME)));
    }

    @SuppressLint("ResourceType")
    public static View createNavbarLayout(Context ctx, int color) {
        View v = new View(ctx);
        v.setId(android.R.attr.gravity);
        v.setLayoutParams(new ViewGroup.LayoutParams(-1, isColorized() ? navbarH(ctx) : -1));
        boolean isLight = Build.VERSION.SDK_INT < 31 && ColorUtils.satisfiesTextContrast(Color.rgb(Color.red(color), Color.green(color), Color.blue(color)));
        if (isLight)
            color = ColorUtils.getDarkerColor(color);
        v.setBackgroundColor(color);
        return v;
    }

    public static int findGestureHeight(Context ctx) {
        try {
            if (SDK_INT >= 29) {
                if (SDK_INT > 30) {
                    WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
                    boolean gesturesEnabled = isGesturesEnabled();

                    // TODO: Detect Android 12L+ Taskbar
                    int type = gesturesEnabled ? WindowInsets.Type.systemGestures() : WindowInsets.Type.navigationBars();
                    return (int) (wm.getCurrentWindowMetrics()
                            .getWindowInsets()
                            .getInsets(type)
                            .bottom * (gesturesEnabled ? 1.5 : 1));
                }

                // For SDK 30 or below, use old method
                // Because new method reports wrong size
                return DensityUtils.dpInt(48);
            }
        } catch (Throwable ignored) {}
        return 0;
    }

    public static boolean isGesturesEnabled() {
        try {
            return Settings.Secure.getInt(SuperBoardApplication.getApplication().getContentResolver(), "navigation_mode") == 2;
        } catch (Throwable t) {
            return false;
        }
    }

    /** @noinspection JavaReflectionMemberAccess*/
    public static int navbarH(Context ctx) {
        if (isColorized()) {
            if (!isGesturesEnabled() && isLand(ctx) && !isTablet(ctx)) return 0;
            int gestureHeight = findGestureHeight(ctx);
            if (gestureHeight > 0) return gestureHeight;
            Resources res = ctx.getResources();
            int resourceId = 0;

            try {
                resourceId = android.R.dimen.class
                        .getDeclaredField("navigation_bar_height").getInt(null);
            } catch (Throwable ignored) {}

            return resourceId > 0 ? res.getDimensionPixelSize(resourceId) : 0;
        }
        return 0;
    }

    public static boolean isColorized() {
        return !(isNotColorizeNavbar() || !SuperDBHelper.getBooleanOrDefault(SettingMap.SET_COLORIZE_NAVBAR));
    }

    private static boolean isTablet(Context ctx) {
        if (SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            return ctx.getResources().getConfiguration().smallestScreenWidthDp >= 600;
        }

        return false;
    }

    private static boolean isLand(Context ctx) {
        return ctx.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

}
