package org.blinksd.utils;

import static android.os.Build.VERSION.SDK_INT;
import static org.blinksd.board.SuperBoardApplication.getResConfiguration;
import static org.blinksd.board.SuperBoardApplication.getSBApplication;
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
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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
            boolean isLight = Build.VERSION.SDK_INT < 31 && satisfiesTextContrast(convertARGBtoRGB(color));
            if (isLight)
                color = getDarkerColor(color);
        }

        v.setBackgroundColor(color);
        return v;
    }

    private static int findGestureHeight(Context ctx) {
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
            return Settings.Secure.getInt(getSBApplication().getContentResolver(), "navigation_mode") == 2;
        } catch (Throwable t) {
            return false;
        }
    }

    /** @noinspection JavaReflectionMemberAccess*/
    @SuppressLint({"DiscouragedApi", "InternalInsetResource"})
    public static int navbarH(Context ctx) {
        if (isColorized()) {
            if (!isGesturesEnabled() && isLand() && !isTablet()) return 0;
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

    public static void disableEdgeToEdge(Window window) {
        MiniLSPass.setField(window, "mEdgeToEdgeEnforced", false);
    }
}
