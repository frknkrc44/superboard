package org.blinksd.board.views;

import static org.blinksd.utils.ColorUtils.getAccentColor;
import static org.blinksd.utils.SystemUtils.isDarkThemeEnabled;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import org.blinksd.utils.DensityUtils;
import org.blinksd.utils.ResourcesUtils;

import java.lang.reflect.Field;

public final class CustomRadioButton extends RadioButton {

    public CustomRadioButton(Context c) {
        super(c);
        int i = DensityUtils.dpInt(8);
        setPadding(i, 0, i, 0);
        setPadding(i, 0, i, 0);
        final var darkTheme = isDarkThemeEnabled();
        int color = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                ? ResourcesUtils.getColor(darkTheme
                    ? android.R.color.system_accent1_200
                    : android.R.color.system_accent1_600)
                : getAccentColor();
        setButtonTintList(ColorStateList.valueOf(color));
        setButtonTintMode(PorterDuff.Mode.SRC_IN);

        setBackground(null);
    }

    /** @noinspection JavaReflectionMemberAccess*/
    @SuppressLint("DiscouragedPrivateApi")
    public Drawable getButtonDrawable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return super.getButtonDrawable();
        }

        try {
            Field field = CompoundButton.class.getDeclaredField("mButtonDrawable");
            field.setAccessible(true);
            return (Drawable) field.get(this);
        } catch (Throwable t) {
            return null;
        }
    }
}
