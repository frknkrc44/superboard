package org.blinksd.utils;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.TextView;

@SuppressWarnings("deprecation")
public class ViewUtils {
    private ViewUtils() {}

    public static void setBackground(View view, Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(drawable);
        } else {
            view.setBackgroundDrawable(drawable);
        }
    }

    public static void setTextAppearance(TextView textView, int resId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            textView.setTextAppearance(resId);
        } else {
            textView.setTextAppearance(textView.getContext(), resId);
        }
    }
}
