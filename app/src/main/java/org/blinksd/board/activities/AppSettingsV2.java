package org.blinksd.board.activities;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.blinksd.board.SuperBoardApplication;
import org.blinksd.board.services.KeyboardThemeApi;
import org.blinksd.board.views.SettingsCategorizedListView;
import org.blinksd.board.views.SuperBoard;
import org.blinksd.utils.DensityUtils;
import org.blinksd.utils.IconThemeUtils;
import org.blinksd.utils.ImageUtils;
import org.blinksd.utils.LayoutCreator;
import org.blinksd.utils.LayoutUtils;
import org.blinksd.utils.LocalIconTheme;
import org.blinksd.utils.SettingMap;
import org.blinksd.utils.SuperDBHelper;

import java.io.File;
import java.util.concurrent.Executors;

public class AppSettingsV2 extends Activity {
    private LinearLayout main;
    public SuperBoard kbdPreview;
    private ImageView backgroundImageView;
    private SettingsCategorizedListView mSettView;

    @Override
    protected void onResume() {
        super.onResume();
        setKeyPrefs();
    }

    public void recreate() {
        if (Build.VERSION.SDK_INT >= 11) {
            super.recreate();
            return;
        }

        onCreate(getIntent().getExtras());
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        main = LayoutCreator.createFilledVerticalLayout(FrameLayout.class, this);

        if (Build.VERSION.SDK_INT >= 31) {
            getWindow().getDecorView().setFitsSystemWindows(true);
            main.setFitsSystemWindows(false);
            getWindow().setNavigationBarColor(0);
            getWindow().setStatusBarColor(0);
            getWindow().setBackgroundDrawableResource(android.R.color.system_neutral1_900);
        }

        try {
            createMainView();
        } catch (Throwable e) {
            Log.e("MainView", "Error:", e);
        }

        main.addView(mSettView);
        setKeyPrefs();

        setContentView(main);
    }

    private void createPreviewView() {
        FrameLayout ll = (FrameLayout) LayoutCreator.getHFilledView(FrameLayout.class, LinearLayout.class, this);
        kbdPreview = new PreviewBoard(this);
        int popupHeight = 12;
        kbdPreview.addRow(0, new String[]{"1", "2", "3", "4"});
        kbdPreview.getKey(0, 0, 0).setSubText("½");
        for (int i = 0; i < 4; i++) kbdPreview.getKey(0, 0, i).setId(i);
        kbdPreview.createEmptyLayout();
        kbdPreview.setEnabledLayout(0);
        kbdPreview.setKeyboardHeight(popupHeight);
        kbdPreview.setKeysPadding(DensityUtils.mpInt(1));
        backgroundImageView = new ImageView(this);
        backgroundImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        backgroundImageView.setLayoutParams(new FrameLayout.LayoutParams(-1, DensityUtils.hpInt(popupHeight)));
        ll.addView(backgroundImageView);
        ll.addView(kbdPreview);
        main.addView(ll);
    }

    private void createMainView() {
        createPreviewView();
        mSettView = new SettingsCategorizedListView(this);
    }

    private void setKeyPrefs() {
        File img = SuperBoardApplication.getBackgroundImageFile();
        if (img.exists()) {
            int blur = getIntOrDefault(SettingMap.SET_KEYBOARD_BGBLUR);
            Bitmap b = BitmapFactory.decodeFile(img.getAbsolutePath());
            backgroundImageView.setImageBitmap(blur > 0 ? ImageUtils.getBlur(b, blur) : b);
        } else {
            backgroundImageView.setImageBitmap(null);
        }
        int keyClr = SuperDBHelper.getIntOrDefault(SettingMap.SET_KEY_BGCLR);
        int keyPressClr = SuperDBHelper.getIntOrDefault(SettingMap.SET_KEY_PRESS_BGCLR);
        kbdPreview.setKeysBackground(LayoutUtils.getKeyBg(keyClr, keyPressClr, true));
        Drawable key2Bg = LayoutUtils.getKeyBg(
                getIntOrDefault(SettingMap.SET_KEY2_BGCLR),
                getIntOrDefault(SettingMap.SET_KEY2_PRESS_BGCLR), true);
        Drawable enterBg = LayoutUtils.getKeyBg(
                getIntOrDefault(SettingMap.SET_ENTER_BGCLR),
                getIntOrDefault(SettingMap.SET_ENTER_PRESS_BGCLR), true);
        kbdPreview.setKeysShadow(getIntOrDefault(SettingMap.SET_KEY_SHADOWSIZE),
                getIntOrDefault(SettingMap.SET_KEY_SHADOWCLR));
        kbdPreview.setKeyBackground(0, 0, 2, key2Bg);
        kbdPreview.setKeyBackground(0, 0, -1, enterBg);
        kbdPreview.setBackgroundColor(getIntOrDefault(SettingMap.SET_KEYBOARD_BGCLR));
        kbdPreview.setKeysTextColor(getIntOrDefault(SettingMap.SET_KEY_TEXTCLR));
        kbdPreview.setKeysTextSize(getFloatPercentOrDefault(SettingMap.SET_KEY_TEXTSIZE));
        kbdPreview.setIconSizeMultiplier(getIntOrDefault(SettingMap.SET_KEY_ICON_SIZE_MULTIPLIER));
        kbdPreview.setKeysTextType(getIntOrDefault(SettingMap.SET_KEYBOARD_TEXTTYPE_SELECT));
        IconThemeUtils iconThemes = SuperBoardApplication.getIconThemes();
        kbdPreview.setKeyDrawable(0, 0, 2,
                iconThemes.getIconResource(LocalIconTheme.SYM_TYPE_DELETE));
        LayoutUtils.setSpaceBarViewPrefs(iconThemes,
                kbdPreview.getKey(0, 0, 1),
                SuperBoardApplication.getCurrentKeyboardLanguage().name);
        kbdPreview.setKeyDrawable(0, 0, -1,
                iconThemes.getIconResource(LocalIconTheme.SYM_TYPE_ENTER));
        kbdPreview.setKeyVibrateDuration(
                SuperDBHelper.getIntOrDefault(SettingMap.SET_KEY_VIBRATE_DURATION));
        try {
            SuperBoardApplication.clearCustomFont();
            SuperBoardApplication.getCustomFont();
        } catch (Throwable ignored) {}
    }

    public int getFloatPercentOrDefault(String key) {
        return DensityUtils.mpInt(DensityUtils.getFloatNumberFromInt(getIntOrDefault(key)));
    }

    public int getIntOrDefault(String key) {
        return SuperDBHelper.getIntOrDefault(key);
    }

    public void restartKeyboard() {
        setKeyPrefs();
        KeyboardThemeApi.restartKeyboard();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            new ImageTask().execute(getContentResolver(), uri);
        }
    }

    public enum SettingCategory {
        GENERAL,
        THEMING,
        THEMING_ADVANCED,
    }

    public enum SettingType {
        BOOL,
        THEME_SELECTOR,
        COLOR_SELECTOR,
        STR_SELECTOR,
        SELECTOR,
        DECIMAL_NUMBER,
        FLOAT_NUMBER,
        MM_DECIMAL_NUMBER,
        IMAGE,
        REDIRECT,
    }

    public static class SettingItem {
        public final SettingCategory category;
        public final SettingType type;
        public final String dependency;
        public final Object dependencyEnabled;

        public SettingItem(SettingCategory category, SettingType type, String dependency, Object dependencyEnabled) {
            this.category = category;
            this.type = type;
            this.dependency = dependency;
            this.dependencyEnabled = dependencyEnabled;
        }
    }

    private class ImageTask {
        public void execute(Object... args) {
            Executors.newSingleThreadExecutor().execute(() -> {
                Bitmap bmp = doInBackground(args);
                SuperBoardApplication.mainHandler.post(() -> onPostExecute(bmp));
            });
        }

        protected Bitmap doInBackground(Object[] p1) {
            try {
                return MediaStore.Images.Media.getBitmap((ContentResolver) p1[0], (Uri) p1[1]);
            } catch (Throwable ignored) {
            }
            return null;
        }

        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                result = ImageUtils.getMinimizedBitmap(result);
                ImageView img = mSettView.mAdapter.dialogView.findViewById(android.R.id.custom);
                img.setImageBitmap(result);
            }
        }

    }

    /** @noinspection unused*/
    private class PreviewBoard extends SuperBoard {
        public PreviewBoard(Context c) {
            super(c);
        }

        @Override
        protected void sendDefaultKeyboardEvent(View v) {
            fakeKeyboardEvent((Key) v);
            kbdPreview.vibrate();
        }

        @Override
        public void playSound(int event) {
            if (!SuperDBHelper.getBooleanOrDefault(SettingMap.SET_PLAY_SND_PRESS)) return;
            super.playSound(event);
        }
    }
}
