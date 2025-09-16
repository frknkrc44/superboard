package org.blinksd.utils;

import static android.os.Build.VERSION.SDK_INT;
import static android.view.WindowInsets.Type.systemBars;
import static android.view.WindowInsets.Type.systemGestures;
import static org.blinksd.board.SuperBoardApplication.getResConfiguration;
import static org.blinksd.board.SuperBoardApplication.getSBApplication;
import static org.blinksd.board.SuperBoardApplication.isWatchDevice;
import static org.blinksd.utils.ColorUtils.convertARGBtoRGB;
import static org.blinksd.utils.ColorUtils.getDarkerColor;
import static org.blinksd.utils.ColorUtils.satisfiesTextContrast;
import static org.blinksd.utils.WindowManagerServiceUtils.navbarCustomModeEnabled;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowManager;

import org.blinksd.board.InputService;

import java.lang.reflect.Method;

public final class SystemUtils {
    public static boolean isPermGranted(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return Environment.isExternalStorageManager();
        }

        return context.checkCallingOrSelfPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

    }

    @SuppressLint("InlinedApi")
    public static boolean isDocumentsUiAvailable() {
        for (var feature : new String[]{
                PackageManager.FEATURE_AUTOMOTIVE,
                PackageManager.FEATURE_LEANBACK,
                PackageManager.FEATURE_WATCH,
        }) {
            if (getSBApplication().getPackageManager().hasSystemFeature(feature)) {
                return false;
            }
        }

        return true;
    }

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
        return WindowManagerServiceUtils.hasNavigationBar(inputService);
    }

    @SuppressLint("ResourceType")
    public static View createNavbarLayout(Context ctx, int color) {
        View v = new View(ctx);
        v.setId(android.R.attr.gravity);
        v.setLayoutParams(new ViewGroup.LayoutParams(-1, isColorized() ? navbarH(ctx) : -1));

        boolean isForcedTrans = SuperDBHelper.getBooleanOrDefault(SettingMap.SET_COLORIZE_NAVBAR_ALWAYS_TRANS);
        if (isForcedTrans) {
            color = Color.TRANSPARENT;
        } else {
            boolean isLight = Build.VERSION.SDK_INT < Build.VERSION_CODES.S && satisfiesTextContrast(convertARGBtoRGB(color));
            if (isLight)
                color = getDarkerColor(color);
        }

        v.setBackgroundColor(color);
        return v;
    }

    private static int findGestureHeight(Context ctx) {
        if (SDK_INT == Build.VERSION_CODES.Q) {
            // For SDK 30 or below, use old method
            // Because new method reports wrong size
            return DensityUtils.dpInt(48);
        } else if (SDK_INT >= Build.VERSION_CODES.R) {
            WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
            WindowInsets windowInsets = wm.getCurrentWindowMetrics().getWindowInsets();

            int bottomGestureInset = windowInsets.getInsets(systemGestures()).bottom;
            if (bottomGestureInset > 0)
                return (int) (bottomGestureInset * 1.5f);

            return windowInsets.getInsets(systemBars()).bottom;
        }

        return 0;
    }

    /** @noinspection JavaReflectionMemberAccess*/
    @SuppressLint({"DiscouragedApi", "InternalInsetResource"})
    public static int navbarH(Context ctx) {
        if (isColorized()) {
            if (isLand() && !isTablet()) return 0;
            int gestureHeight = findGestureHeight(ctx);
            if (gestureHeight > 0) return gestureHeight;
            Resources res = ctx.getResources();
            int resourceId;

            try {
                resourceId = android.R.dimen.class
                        .getDeclaredField("navigation_bar_height").getInt(null);
            } catch (Throwable ignored) {
                resourceId = ctx.getResources().getIdentifier(
                        "navigation_bar_height", "dimen", "android");
            }

            return resourceId > 0 ? res.getDimensionPixelSize(resourceId) : 0;
        }
        return 0;
    }

    public static boolean isColorized() {
        return !(isNotColorizeNavbar() || !navbarCustomModeEnabled());
    }

    private static boolean isTablet() {
        return getResConfiguration().smallestScreenWidthDp >= 600;
    }

    public static boolean isLand() {
        return getResConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    public static boolean isDarkThemeEnabled() {
        // use dark mode on watch devices directly
        if (isWatchDevice()) {
            return true;
        }

        return (getResConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    public static float getMultipliedTextSize(float size) {
        return 1.5f * size;
    }
}
