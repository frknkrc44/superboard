package org.blinksd.board.activities;

import static org.blinksd.utils.SuperDBHelper.getBooleanOrDefault;
import static org.blinksd.utils.SuperDBHelper.getFloatPercentOrDefault;
import static org.blinksd.utils.SuperDBHelper.getIntOrDefault;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
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

import java.io.File;
import java.util.concurrent.Executors;

@SuppressWarnings("deprecation")
public final class AppSettingsV2 extends BaseActivity {
    private LinearLayout main;
    public SuperBoard kbdPreview;
    private ImageView backgroundImageView;
    private SettingsCategorizedListView mSettView;

    @Override
    protected void onResume() {
        super.onResume();
        setKeyPrefs();
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        main = LayoutCreator.createFilledVerticalLayout(FrameLayout.class, this);

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
        int keyClr = getIntOrDefault(SettingMap.SET_KEY_BGCLR);
        int keyPressClr = getIntOrDefault(SettingMap.SET_KEY_PRESS_BGCLR);
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
        kbdPreview.setKeyVibrateDuration(getIntOrDefault(SettingMap.SET_KEY_VIBRATE_DURATION));
        try {
            SuperBoardApplication.clearCustomFont();
            SuperBoardApplication.getCustomFont();
        } catch (Throwable ignored) {}
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
        } else if (requestCode == 2 && resultCode == RESULT_OK) {
            recreate();
        }
    }

    private class ImageTask {
        public void execute(Object... args) {
            Executors.newSingleThreadExecutor().execute(() -> {
                Bitmap bmp = doInBackground(args);
                SuperBoardApplication.mainHandler.post(() -> onPostExecute(bmp));
            });
        }

        @SuppressWarnings("deprecation")
        protected Bitmap doInBackground(Object[] p1) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ImageDecoder.Source decoder = ImageDecoder.createSource((ContentResolver) p1[0], (Uri) p1[1]);
                    return ImageDecoder.decodeBitmap(decoder);
                } else {
                    return MediaStore.Images.Media.getBitmap((ContentResolver) p1[0], (Uri) p1[1]);
                }
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

    private static class PreviewBoard extends SuperBoard {
        public PreviewBoard(Context c) {
            super(c);
        }

        @Override
        protected void sendDefaultKeyboardEvent(Key v) {
            if (v.hasNormalPressEvent()) {
                playSound(v.getNormalPressEvent().first);
                return;
            }
            playSound(0);
            vibrate();
        }

        @Override
        public void playSound(int event) {
            if (!getBooleanOrDefault(SettingMap.SET_PLAY_SND_PRESS)) return;
            super.playSound(event);
        }
    }
}
