package org.blinksd.utils;

import android.content.res.Resources;
import android.util.DisplayMetrics;

@SuppressWarnings("unused")
public class DensityUtils {

    public static float dp(float px) {
        return getDisplayMetrics().density * px;
    }

    public static int dpInt(float px) {
        return (int) dp(px);
    }

    public static float wp(float px) {
        return (getScreenWidth() / 100f) * px;
    }

    public static int wpInt(float px) {
        return (int) wp(px);
    }

    public static float hp(float px) {
        return (getScreenHeight() / 100f) * px;
    }

    public static int hpInt(float px) {
        return (int) hp(px);
    }

    public static float mp(float px) {
        return (Math.min(getScreenWidth(), getScreenHeight()) / 100f) * px;
    }

    public static int mpInt(float percent) {
        return (int) mp(percent);
    }

    public static DisplayMetrics getDisplayMetrics() {
        return Resources.getSystem().getDisplayMetrics();
    }

    public static int getScreenWidth() {
        return getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return getDisplayMetrics().heightPixels;
    }

    public static float getFloatNumberFromInt(int i) {
        return i / 10.0f;
    }

    public static int getIntNumberFromFloat(float i) {
        return (int) (i * 10);
    }
}
