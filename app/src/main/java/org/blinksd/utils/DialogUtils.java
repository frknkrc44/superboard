package org.blinksd.utils;

import static org.blinksd.board.SuperBoardApplication.isWatchDevice;
import static org.blinksd.utils.DensityUtils.dp;
import static org.blinksd.utils.DensityUtils.dpInt;
import static org.blinksd.utils.SystemUtils.isDarkThemeEnabled;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;

public class DialogUtils {
    private DialogUtils() {}

    public static void doHacksAndShow(AlertDialog dialog) {
        final var darkTheme = isDarkThemeEnabled();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            GradientDrawable gradientDrawable = new GradientDrawable();
            int color = ResourcesUtils.getColor(
                    darkTheme
                            ? android.R.color.system_neutral1_900
                            : android.R.color.system_neutral1_50);
            gradientDrawable.setColor(color);
            gradientDrawable.setCornerRadius(dpInt(16));
            gradientDrawable.setTint(color);
            Window window = dialog.getWindow();
            if (window != null) {
                window.getDecorView().setBackground(gradientDrawable);
            }
        }

        dialog.show();

        int tint = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                ? ResourcesUtils.getColor(darkTheme
                ? android.R.color.system_accent1_200
                : android.R.color.system_accent1_600
        ) : ColorUtils.getAccentColor();

        Button btn1 = dialog.findViewById(android.R.id.button1);
        Button btn2 = dialog.findViewById(android.R.id.button2);
        Button btn3 = dialog.findViewById(android.R.id.button3);

        if (btn1 != null) {
            btn1.setTextColor(tint);
            btn1.setAllCaps(false);
            if (isWatchDevice()) {
                btn1.setTextColor(Color.BLACK);
                GradientDrawable elevatedButton = new GradientDrawable();
                elevatedButton.setColor(tint);
                elevatedButton.setCornerRadius(dp(96));

                btn1.setPadding(0, btn1.getPaddingTop(), 0, btn1.getPaddingBottom());
                btn1.setBackground(elevatedButton);
                btn1.setGravity(Gravity.CENTER);

                try {
                    var params = (LinearLayout.LayoutParams) btn1.getLayoutParams();
                    params.bottomMargin = dpInt(8);
                } catch (Throwable ignore) {}
            }
        }

        if (btn2 != null) {
            btn2.setTextColor(tint);
            btn2.setAllCaps(false);
            if (isWatchDevice()) {
                GradientDrawable outlinedButton = new GradientDrawable();
                outlinedButton.setColor(0);
                outlinedButton.setStroke(dpInt(1), tint);
                outlinedButton.setCornerRadius(dp(96));

                btn2.setPadding(0, btn2.getPaddingTop(), 0, btn2.getPaddingBottom());
                btn2.setBackground(outlinedButton);
                btn2.setGravity(Gravity.CENTER);

                try {
                    var params = (LinearLayout.LayoutParams) btn2.getLayoutParams();
                    params.bottomMargin = dpInt(8);
                } catch (Throwable ignore) {}
            }
        }

        if (btn3 != null) {
            btn3.setTextColor(tint);
            btn3.setAllCaps(false);

            if (isWatchDevice()) {
                GradientDrawable outlinedButton = new GradientDrawable();
                outlinedButton.setColor(0);
                outlinedButton.setStroke(dpInt(1), tint);
                outlinedButton.setCornerRadius(dp(96));

                btn3.setBackground(outlinedButton);
                btn3.setGravity(Gravity.CENTER);
                btn3.setPadding(0, btn3.getPaddingTop(), 0, btn3.getPaddingBottom());

                try {
                    var params = (LinearLayout.LayoutParams) btn3.getLayoutParams();
                    params.bottomMargin = dpInt(8);
                } catch (Throwable ignore) {}
            } else {
                btn3.setPadding(btn3.getPaddingLeft(), 0, btn3.getPaddingLeft(), 0);
            }
        }
    }
}
