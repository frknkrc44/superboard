package org.blinksd.utils;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;

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
}
