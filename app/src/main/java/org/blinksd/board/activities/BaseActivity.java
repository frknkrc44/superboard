package org.blinksd.board.activities;

import static org.blinksd.board.SuperBoardApplication.isWatchDevice;
import static org.blinksd.utils.SystemUtils.isDarkThemeEnabled;

import android.app.Activity;
import android.app.UiModeManager;
import android.os.Build;
import android.view.View;
import android.view.WindowInsetsController;
import android.view.WindowManager;

@SuppressWarnings({"deprecation", "all"})
public class BaseActivity extends Activity {
    @Override
    public void setContentView(View main) {
        super.setContentView(main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (isWatchDevice()) {
                UiModeManager uiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);
                uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_YES);
            }

            final var darkTheme = isDarkThemeEnabled();

            getWindow().getDecorView().setFitsSystemWindows(true);
            getWindow().setBackgroundDrawableResource(darkTheme
                    ? android.R.color.system_neutral1_900
                    : android.R.color.system_neutral1_50
            );
            main.setFitsSystemWindows(false);

            getWindow().getAttributes().layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;

            final var insetsController = main.getWindowInsetsController();
            insetsController.setSystemBarsAppearance(
                    darkTheme ? 0 : WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            );
            insetsController.setSystemBarsAppearance(
                    darkTheme ? 0 : WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
            );

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
