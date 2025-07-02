package org.blinksd.utils;

import static android.util.TypedValue.complexToDimension;
import static org.blinksd.board.SuperBoardApplication.getAppResources;
import static org.blinksd.board.SuperBoardApplication.getSBApplication;
import static org.blinksd.utils.ColorUtils.getAccentColor;
import static org.blinksd.utils.ColorUtils.getDarkerColor;
import static org.blinksd.utils.ColorUtils.setAlphaForColor;
import static org.blinksd.utils.DensityUtils.dpInt;
import static org.blinksd.utils.DensityUtils.getFloatNumberFromInt;
import static org.blinksd.utils.DensityUtils.minPInt;
import static org.blinksd.utils.SuperDBHelper.getIntOrDefault;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.util.TypedValue;

public class ResourcesUtils {
    private ResourcesUtils() {}

    @SuppressWarnings("deprecation")
    public static Drawable getDrawable(int resId) {
        Resources res = getAppResources();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            return res.getDrawable(resId, getSBApplication().getTheme());
        }

        return res.getDrawable(resId);
    }

    public static Drawable getTintedDrawable(int resId, int tintColor) {
        Drawable drawable = getDrawable(resId);
        drawable.setTint(tintColor);
        return drawable;
    }

    @SuppressWarnings("deprecation")
    public static int getColor(int resId) {
        Resources res = getAppResources();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return res.getColor(resId, getSBApplication().getTheme());
        }

        return res.getColor(resId);
    }

    public static float getListPreferredItemHeight(Context context) {
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.listPreferredItemHeight, value, true);
        return complexToDimension(value.data, context.getResources().getDisplayMetrics());
    }

    public static Drawable getDefaultKeyBg(boolean pressEffect) {
        return getKeyBg(Defaults.KEY_BACKGROUND_COLOR, Defaults.KEY_PRESS_BACKGROUND_COLOR, pressEffect);
    }

    public static Drawable getKeyBg(int clr, int pressClr, boolean pressEffect) {
        int radius = minPInt(getFloatNumberFromInt(getIntOrDefault(SettingMap.SET_KEY_RADIUS)));
        int strokeWidth = minPInt(getFloatNumberFromInt(getIntOrDefault(SettingMap.SET_KEY_STROKE_WIDTH)));
        int strokeColor = getIntOrDefault(SettingMap.SET_KEY_STROKE_COLOR);
        return getButtonBackground(clr, pressClr, radius, strokeWidth, strokeColor, pressEffect);
    }

    public static Drawable getCircleButtonBackground(int keyColor, int iconColor, boolean pressEffect) {
        int buttonClr = pressEffect ? iconColor : keyColor;
        int keyClr = setAlphaForColor(0x88, pressEffect ? keyColor : iconColor);

        GradientDrawable source = new GradientDrawable();
        source.setColor(buttonClr);
        source.setCornerRadius(64);
        source.setStroke(2, 0);
        return new RippleDrawable(
            ColorStateList.valueOf(keyClr),
            source,
            new ShapeDrawable(new OvalShape()));
    }

    public static Drawable getButtonBackground(int radius, int stroke, boolean pressEffect) {
        return getButtonBackground(radius, stroke, 0, pressEffect);
    }

    public static Drawable getButtonBackground(int radius, int stroke, int strokeColor, boolean pressEffect) {
        int keyClr = getAccentColor();
        int keyPressClr = getDarkerColor(keyClr);
        return getButtonBackground(keyClr, keyPressClr, radius, stroke, strokeColor, pressEffect);
    }

    private static void setButtonGradientOrientation(GradientDrawable gd) {
        switch (getIntOrDefault(SettingMap.SET_KEY_GRADIENT_ORIENTATION)) {
            case ThemeUtils.KEY_BG_ORIENTATION_TB:
                gd.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
                break;
            case ThemeUtils.KEY_BG_ORIENTATION_BT:
                gd.setOrientation(GradientDrawable.Orientation.BOTTOM_TOP);
                break;
            case ThemeUtils.KEY_BG_ORIENTATION_LR:
                gd.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
                break;
            case ThemeUtils.KEY_BG_ORIENTATION_RL:
                gd.setOrientation(GradientDrawable.Orientation.RIGHT_LEFT);
                break;
            case ThemeUtils.KEY_BG_ORIENTATION_TL_BR:
                gd.setOrientation(GradientDrawable.Orientation.TL_BR);
                break;
            case ThemeUtils.KEY_BG_ORIENTATION_TR_BL:
                gd.setOrientation(GradientDrawable.Orientation.TR_BL);
                break;
            case ThemeUtils.KEY_BG_ORIENTATION_BL_TR:
                gd.setOrientation(GradientDrawable.Orientation.BL_TR);
                break;
            case ThemeUtils.KEY_BG_ORIENTATION_BR_TL:
                gd.setOrientation(GradientDrawable.Orientation.BR_TL);
                break;
        }
    }

    public static Drawable getButtonBackground(int clr, int pressClr, int radius, int stroke, int strokeColor, boolean pressEffect) {
        GradientDrawable gd = new GradientDrawable();
        gd.setCornerRadius(radius);

        boolean isGrad = getIntOrDefault(SettingMap.SET_KEY_BG_TYPE) != ThemeUtils.KEY_BG_TYPE_FLAT;
        if (isGrad) {
            gd.setColors(new int[]{clr, pressClr});
            setButtonGradientOrientation(gd);
        } else {
            gd.setColor(clr);
        }

        if (stroke > 0) {
            gd.setStroke(stroke, strokeColor);
        }

        if (pressEffect) {
            StateListDrawable d = new StateListDrawable();
            GradientDrawable pd = new GradientDrawable();
            pd.setCornerRadius(radius);

            if (isGrad) {
                pd.setColors(new int[]{pressClr, clr});
                setButtonGradientOrientation(pd);
            } else {
                pd.setColor(pressClr);
            }

            if (stroke > 0) {
                pd.setStroke(stroke, strokeColor);
            }

            d.addState(new int[]{android.R.attr.state_selected}, pd);
            d.addState(new int[]{android.R.attr.state_pressed}, pd);
            d.addState(new int[]{}, gd);
            return d;
        }

        return gd;
    }

    public static Drawable getSelectableItemBg(int textColor) {
        return getSelectableItemBg(textColor, false);
    }

    public static Drawable getSelectableItemBg(int textColor, boolean darker) {
        return getSelectableItemBg(textColor, darker, false);
    }

    public static Drawable getTransSelectableItemBg(Context context, int textColor) {
        return getTransSelectableItemBg(context, textColor, false);
    }

    public static Drawable getTransSelectableItemBg(Context context, int textColor, boolean forceSquare) {
        if (!forceSquare) {
            return new RippleDrawable(
                    ColorStateList.valueOf(setAlphaForColor(0x88, textColor)),
                    null,
                    new ShapeDrawable(new OvalShape())
            );
        }

        TypedArray array = context.getTheme().obtainStyledAttributes(
                new int[]{android.R.attr.selectableItemBackground}
        );
        int resId = array.getResourceId(0, 0);
        int color = setAlphaForColor(0x88, textColor);

        Drawable d = getTintedDrawable(resId, color);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            array.close();
        }
        return d;
    }

    public static Drawable getSelectableItemBg(int textColor, boolean darker, boolean transparent) {
        GradientDrawable content = new GradientDrawable();
        int accent = transparent ? 0 : getAccentColor();
        if (darker && !transparent) {
            accent = getDarkerColor(accent);
        }
        content.setColor(accent);
        int padding = dpInt(16);
        content.setCornerRadius(padding);

        return new RippleDrawable(
                ColorStateList.valueOf(textColor - 0x88000000),
                content,
                transparent ? new ColorDrawable(textColor - 0x88000000) : null
        );
    }
}
