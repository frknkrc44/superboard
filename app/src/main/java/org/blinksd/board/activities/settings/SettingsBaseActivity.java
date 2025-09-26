package org.blinksd.board.activities.settings;

import static org.blinksd.board.SuperBoardApplication.getSBApplication;
import static org.blinksd.board.SuperBoardApplication.getSettings;
import static org.blinksd.board.SuperBoardApplication.mainHandler;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.blinksd.board.R;
import org.blinksd.board.activities.BaseActivity;
import org.blinksd.board.services.KeyboardThemeApi;
import org.blinksd.board.views.CustomActionBar;
import org.blinksd.utils.DensityUtils;
import org.blinksd.utils.ImageUtils;
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
    // ImageView backgroundImageView;
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
