package org.blinksd.utils;

import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;

@SuppressWarnings("deprecation")
public class DrawableUtils {
    private DrawableUtils() {}

    public static void setColorFilter(Drawable drawable, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            drawable.setColorFilter(new BlendModeColorFilter(color, BlendMode.SRC_ATOP));
        } else {
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }
    }

    public static Drawable getTintedDrawable(int resId, Integer tintColor) {
        Drawable drawable = ResourcesUtils.getDrawable(resId);
        if (tintColor != null) {
            if (tintColor == 0) {
                GradientDrawable gradientDrawable = (GradientDrawable) drawable;
                gradientDrawable.setColor(0);
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                DrawableUtils.setColorFilter(drawable, tintColor);
            } else {
                drawable.setTint(tintColor);
            }
        }

        return drawable;
    }
}
