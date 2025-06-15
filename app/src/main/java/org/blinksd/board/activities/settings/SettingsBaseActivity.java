package org.blinksd.board.activities.settings;

import static org.blinksd.board.SuperBoardApplication.getSBApplication;
import static org.blinksd.board.SuperBoardApplication.getSettings;
import static org.blinksd.board.SuperBoardApplication.isWatchDevice;
import static org.blinksd.board.SuperBoardApplication.mainHandler;
import static org.blinksd.utils.DensityUtils.dp;
import static org.blinksd.utils.DensityUtils.dpInt;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.blinksd.board.R;
import org.blinksd.board.activities.BaseActivity;
import org.blinksd.board.services.KeyboardThemeApi;
import org.blinksd.board.views.CustomActionBar;
import org.blinksd.utils.ColorUtils;
import org.blinksd.utils.DensityUtils;
import org.blinksd.utils.ImageUtils;
import org.blinksd.utils.ResourcesUtils;
import org.blinksd.utils.SettingCategory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public abstract class SettingsBaseActivity extends BaseActivity {
    LinearLayout main;
    // FrameLayout mPreviewHolder;
    MainTabListAdapter mTabListAdapter;
    FrameLayout mTabsHolder;
    CustomActionBar actionBar;
    // SuperBoard kbdPreview;
    ImageView backgroundImageView;
    View dialogView;
    SettingCategory currentCategory;
    static final List<SettingCategory> categoryList = Arrays.asList(SettingCategory.values());
    Object onBackAnimationCallback;
    static final int displayWidth = DensityUtils.wpInt(100);

    public static String getTranslation(String key) {
        Context context = getSBApplication();
        String requestedKey = "settings_" + key;
        try {
            int id = context.getResources().getIdentifier(requestedKey, "string", context.getPackageName());
            return context.getString(id);
        } catch (Throwable ignored) {}
        return requestedKey;
    }

    @SuppressLint("DiscouragedApi")
    List<String> getArrayAsList(String key) {
        Resources res = getResources();

        int id;
        try {
            id = res.getIdentifier("settings_" + key, "array", this.getPackageName());
        } catch (Throwable t) {
            id = 0;
        }

        if (id > 0) {
            try {
                String[] arr = res.getStringArray(id);
                return new ArrayList<>(Arrays.asList(arr));
            } catch (Throwable ignored) {}
        }
        return getSettings().getSelector(key);
    }

    public abstract void setKeyPrefs();

    public void restartKeyboard() {
        dialogView = null;
        setKeyPrefs();
        KeyboardThemeApi.restartKeyboard();
    }

    public static void doHacksAndShow(AlertDialog dialog) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            GradientDrawable gradientDrawable = new GradientDrawable();
            int color = ResourcesUtils.getColor(android.R.color.system_neutral1_900);
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
                ? ResourcesUtils.getColor(android.R.color.system_accent1_200)
                : ColorUtils.getAccentColor();

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            new ImageTask().execute(getContentResolver(), data.getData());
        } /* else if (requestCode == 2 && resultCode == RESULT_OK) {
            recreate();
        } */
    }

    private class ImageTask {
        private static final Executor executor = Executors.newSingleThreadExecutor();

        public void execute(Object... args) {
            executor.execute(() -> {
                Bitmap bmp = doInBackground(args);
                mainHandler.post(() -> onPostExecute(bmp));
            });
        }

        @SuppressWarnings({"deprecation", "all"})
        protected Bitmap doInBackground(Object[] p1) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ImageDecoder.Source decoder = ImageDecoder.createSource((ContentResolver) p1[0], (Uri) p1[1]);
                    return ImageDecoder.decodeBitmap(decoder);
                }

                return MediaStore.Images.Media.getBitmap((ContentResolver) p1[0], (Uri) p1[1]);
            } catch (Throwable ignored) {}

            return null;
        }

        protected void onPostExecute(Bitmap result) {
            if (result != null && dialogView != null) {
                ImageView imageView = dialogView.findViewById(R.id.dialog_image_preview);
                if (imageView != null) {
                    imageView.setImageBitmap(ImageUtils.getMinimizedBitmap(result));
                }
            }
        }
    }
}
