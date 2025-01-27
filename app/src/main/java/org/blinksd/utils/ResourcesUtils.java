package org.blinksd.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.util.TypedValue;

import org.blinksd.board.SuperBoardApplication;

@SuppressWarnings("deprecation")
public class ResourcesUtils {
    private ResourcesUtils() {}

    public static Drawable getDrawable(int resId) {
        Resources res = SuperBoardApplication.getApplication().getResources();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            return res.getDrawable(resId, SuperBoardApplication.getApplication().getTheme());
        }

        return res.getDrawable(resId);
    }

    public static Drawable getTintedDrawable(int resId, Integer tintColor) {
        Drawable drawable = ResourcesUtils.getDrawable(resId);
        if (tintColor != null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                ColorUtils.setColorFilter(drawable, tintColor);
            } else {
                drawable.setTint(tintColor);
            }
        }

        return drawable;
    }

    public static int getColor(int resId) {
        Resources res = SuperBoardApplication.getApplication().getResources();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return res.getColor(resId, SuperBoardApplication.getApplication().getTheme());
        }

        return res.getColor(resId);
    }

    public static float getListPreferredItemHeight(Context context) {
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.listPreferredItemHeight, value, true);
        return TypedValue.complexToDimension(value.data, context.getResources().getDisplayMetrics());
    }

    public static Drawable getKeyBg(int clr, int pressClr, boolean pressEffect) {
        int radius = DensityUtils.mpInt(DensityUtils.getFloatNumberFromInt(SuperDBHelper.getIntOrDefault(SettingMap.SET_KEY_RADIUS)));
        int stroke = DensityUtils.mpInt(DensityUtils.getFloatNumberFromInt(SuperDBHelper.getIntOrDefault(SettingMap.SET_KEY_PADDING)));
        return getButtonBackground(clr, pressClr, radius, stroke, pressEffect);
    }

    public static Drawable getCircleButtonBackground(boolean pressEffect) {
        return getButtonBackground(64, 2, pressEffect);
    }

    public static Drawable getButtonBackground(int radius, int stroke, boolean pressEffect) {
        int keyClr = ColorUtils.getAccentColor();
        int keyPressClr = ColorUtils.getDarkerColor(keyClr);
        return getButtonBackground(keyClr, keyPressClr, radius, stroke, pressEffect);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private static void setButtonGradientOrientation(GradientDrawable gd) {
        switch (SuperDBHelper.getIntOrDefault(SettingMap.SET_KEY_GRADIENT_ORIENTATION)) {
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

    public static Drawable getButtonBackground(int clr, int pressClr, int radius, int stroke, boolean pressEffect) {
        GradientDrawable gd = new GradientDrawable();

        boolean isGrad = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN &&
                SuperDBHelper.getIntOrDefault(SettingMap.SET_KEY_BG_TYPE) != ThemeUtils.KEY_BG_TYPE_FLAT;
        if (isGrad) {
            gd.setColors(new int[]{clr, pressClr});
            setButtonGradientOrientation(gd);
        } else {
            gd.setColor(clr);
        }

        gd.setCornerRadius(radius);
        gd.setStroke(stroke, 0);

        if (pressEffect) {
            StateListDrawable d = new StateListDrawable();
            GradientDrawable pd = new GradientDrawable();

            if (isGrad) {
                pd.setColors(new int[]{pressClr, clr});
                setButtonGradientOrientation(pd);
            } else {
                pd.setColor(pressClr);
            }

            pd.setCornerRadius(radius);
            pd.setStroke(stroke, 0);
            d.addState(new int[]{android.R.attr.state_selected}, pd);
            d.addState(new int[]{android.R.attr.state_pressed}, pd);
            d.addState(new int[]{}, gd);
            return d;
        }

        return gd;
    }

    public static Drawable getSelectableItemBg(Context context, int textColor) {
        return getSelectableItemBg(context, textColor, false);
    }

    public static Drawable getSelectableItemBg(Context context, int textColor, boolean darker) {
        return getSelectableItemBg(context, textColor, darker, false);
    }

    public static Drawable getTransSelectableItemBg(Context context, int textColor) {
        TypedArray array = context.getTheme().obtainStyledAttributes(
                new int[]{android.R.attr.selectableItemBackground}
        );
        int resId = array.getResourceId(0, 0);
        int color = textColor - 0x88000000;
        Drawable d = ResourcesUtils.getTintedDrawable(resId, color);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            array.close();
        }
        return d;
    }

    public static Drawable getSelectableItemBg(
            Context context, int textColor, boolean darker, boolean transparent) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return getTransSelectableItemBg(context, textColor);
        }

        GradientDrawable content = new GradientDrawable();
        int accent = transparent ? 0 : ColorUtils.getAccentColor();
        if (darker && !transparent) {
            accent = ColorUtils.getDarkerColor(accent);
        }
        content.setColor(accent);
        int padding = DensityUtils.dpInt(16);
        content.setCornerRadius(padding);

        return new RippleDrawable(
                ColorStateList.valueOf(textColor - 0x88000000),
                content,
                transparent ? new ColorDrawable(textColor - 0x88000000) : null
        );
    }
}
