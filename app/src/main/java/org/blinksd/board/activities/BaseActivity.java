package org.blinksd.board.activities;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;

@SuppressWarnings("deprecation")
public class BaseActivity extends Activity {
    @Override
    public void setContentView(View main) {
        super.setContentView(main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getWindow().getDecorView().setFitsSystemWindows(true);
            getWindow().setBackgroundDrawableResource(android.R.color.system_neutral1_900);
            main.setFitsSystemWindows(false);

            getWindow().getAttributes().layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;

            main.setOnApplyWindowInsetsListener((v, insets) -> {
                main.setPadding(
                        insets.getSystemWindowInsetLeft(),
                        insets.getSystemWindowInsetTop(),
                        insets.getSystemWindowInsetRight(),
                        insets.getSystemWindowInsetBottom()
                );
                return insets;
            });
        }
    }
}
