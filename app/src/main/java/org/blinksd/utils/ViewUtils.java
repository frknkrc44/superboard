package org.blinksd.utils;

import android.os.Build;
import android.widget.TextView;

public class ViewUtils {
    private ViewUtils() {}

    public static void setTextAppearance(TextView textView, int resId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            textView.setTextAppearance(resId);
        } else {
            textView.setTextAppearance(textView.getContext(), resId);
        }
    }
}
