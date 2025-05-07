package org.blinksd.board.activities.settings;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static org.blinksd.board.SuperBoardApplication.clearCustomFont;
import static org.blinksd.board.SuperBoardApplication.getBackgroundImageFile;
import static org.blinksd.board.SuperBoardApplication.getCurrentKeyboardLanguage;
import static org.blinksd.board.SuperBoardApplication.getCustomFont;
import static org.blinksd.board.SuperBoardApplication.getIconThemes;
import static org.blinksd.board.SuperBoardApplication.getMonetColors;
import static org.blinksd.board.SuperBoardApplication.isWatchDevice;
import static org.blinksd.utils.DensityUtils.mpInt;
import static org.blinksd.utils.LayoutUtils.setSpaceBarViewPrefs;
import static org.blinksd.utils.SuperDBHelper.getFloatPercentOrDefault;
import static org.blinksd.utils.SuperDBHelper.getIntOrDefault;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.blinksd.board.views.CustomActionBar;
import org.blinksd.utils.DensityUtils;
import org.blinksd.utils.IconThemeUtils;
import org.blinksd.utils.ImageUtils;
import org.blinksd.utils.LayoutCreator;
import org.blinksd.utils.LocalIconTheme;
import org.blinksd.utils.ResourcesUtils;
import org.blinksd.utils.SettingMap;
import org.blinksd.utils.SuperDBHelper;
import org.blinksd.utils.superboard.KeyboardType;

import java.io.File;

public class AppSettingsV3 extends SettingsCategoriesActivity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        main = LayoutCreator.createFilledVerticalLayout(FrameLayout.class, this);
        createAppBarView();
        createPreviewView();
        createTabBarView();

        setKeyPrefs();
        setContentView(main);

        actionBar.setTitle(getTitle());

        mTabsHolder.getChildAt(mTabsHolder.getChildCount() - 1).requestFocus();
    }

    private void createPreviewView() {
        LinearLayout mainHolder = (LinearLayout) LayoutCreator.getHFilledView(LinearLayout.class, LinearLayout.class, this);
        mainHolder.setGravity(Gravity.CENTER);
        mPreviewHolder = (FrameLayout) LayoutCreator.getHFilledView(FrameLayout.class, LinearLayout.class, this);
        mPreviewHolder.setForegroundGravity(Gravity.CENTER);
        kbdPreview = new PreviewBoard(this);
        kbdPreview.addRow(0, new String[]{"1", "2", "3", "4"});
        kbdPreview.getKey(0, 0, 0).setSubText("½");
        for (int i = 0; i < 4; i++) kbdPreview.getKey(0, 0, i).setId(i);
        kbdPreview.createEmptyLayout(KeyboardType.TEXT);
        kbdPreview.setEnabledLayout(0);
        kbdPreview.setKeysPadding(mpInt(1));
        kbdPreview.setKeyboardHeight(12);
        backgroundImageView = new ImageView(this);
        backgroundImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        backgroundImageView.setLayoutParams(new FrameLayout.LayoutParams(-1, -2));
        mPreviewHolder.addView(backgroundImageView);
        mPreviewHolder.addView(kbdPreview);
        mainHolder.addView(mPreviewHolder);
        main.addView(mainHolder);

        if (isWatchDevice()) {
            mainHolder.setVisibility(GONE);
        }
    }

    private void createAppBarView() {
        actionBar = new CustomActionBar(this, (v) -> toggleCategory(null));
        main.addView(actionBar);
    }

    private void createTabBarView() {
        mTabsHolder = new FrameLayout(this);
        mTabsHolder.setLayoutParams(new LinearLayout.LayoutParams(-1, -1, 1));
        mTabsHolder.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                child.setVisibility(child.getId() == android.R.id.tabs ? VISIBLE : GONE);
                child.setAlpha(child.getId() == android.R.id.tabs ? 1 : 0);
                child.setTranslationX(child.getId() == android.R.id.tabs ? 0 : displayWidth);
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {}
        });
        main.addView(mTabsHolder);
        addCategories();
        createMainTab();
    }

    @Override
    public void restartKeyboard() {
        super.restartKeyboard();

        mTabListAdapter.notifyDataSetChanged();
    }

    @Override
    public void setKeyPrefs() {
        boolean useMonet = getMonetColors().isMonetEnabled();

        File img = getBackgroundImageFile();
        if (img.exists() && !useMonet) {
            int blur = getIntOrDefault(SettingMap.SET_KEYBOARD_BGBLUR);
            Bitmap b = BitmapFactory.decodeFile(img.getAbsolutePath());
            backgroundImageView.setImageBitmap(blur > 0 ? ImageUtils.getBlur(b, blur) : b);
        } else {
            backgroundImageView.setImageBitmap(null);
        }
        int keyClr = getIntOrDefault(SettingMap.SET_KEY_BGCLR);
        int keyPressClr = getIntOrDefault(SettingMap.SET_KEY_PRESS_BGCLR);
        kbdPreview.setKeysBackground(ResourcesUtils.getKeyBg(keyClr, keyPressClr, true));
        Drawable key2Bg = ResourcesUtils.getKeyBg(
                getIntOrDefault(SettingMap.SET_KEY2_BGCLR),
                getIntOrDefault(SettingMap.SET_KEY2_PRESS_BGCLR), true);
        Drawable enterBg = ResourcesUtils.getKeyBg(
                getIntOrDefault(SettingMap.SET_ENTER_BGCLR),
                getIntOrDefault(SettingMap.SET_ENTER_PRESS_BGCLR), true);
        kbdPreview.setKeysShadow(getIntOrDefault(SettingMap.SET_KEY_SHADOWSIZE),
                getIntOrDefault(SettingMap.SET_KEY_SHADOWCLR));
        kbdPreview.setKeyBackground(0, 0, 2, key2Bg);
        kbdPreview.setKeyBackground(0, 0, -1, enterBg);
        kbdPreview.setBackgroundColor(getIntOrDefault(SettingMap.SET_KEYBOARD_BGCLR));
        kbdPreview.setKeysTextSize(getFloatPercentOrDefault(SettingMap.SET_KEY_TEXTSIZE));
        kbdPreview.setIconSizeMultiplier(getIntOrDefault(SettingMap.SET_KEY_ICON_SIZE_MULTIPLIER));
        kbdPreview.setKeysTextType(getIntOrDefault(SettingMap.SET_KEYBOARD_TEXTTYPE_SELECT));
        IconThemeUtils iconThemes = getIconThemes();
        kbdPreview.setKeyDrawable(0, 0, 2,
                iconThemes.getIconResource(LocalIconTheme.SYM_TYPE_DELETE));
        setSpaceBarViewPrefs(iconThemes,
                kbdPreview.getKey(0, 0, 1),
                getCurrentKeyboardLanguage().name);
        kbdPreview.setKeyDrawable(0, 0, -1,
                iconThemes.getIconResource(LocalIconTheme.SYM_TYPE_ENTER));
        kbdPreview.setKeyVibrateDuration(getIntOrDefault(SettingMap.SET_KEY_VIBRATE_DURATION));
        kbdPreview.setKeysTextColor(getIntOrDefault(SettingMap.SET_KEY_TEXTCLR));

        float kbdPadPercent = SuperDBHelper.getFloatedIntOrDefault(SettingMap.SET_KEYBOARD_PADDING);
        float kbdHeightPercent = 12 + (kbdPadPercent * 2);

        ((View) mPreviewHolder.getParent()).getLayoutParams().height = DensityUtils.hpInt(kbdHeightPercent);
        kbdPreview.setKeyboardHeight(kbdHeightPercent - kbdPadPercent);
        kbdPreview.getLayoutParams().height = -1;

        int kbdPadding = mpInt(kbdPadPercent);
        kbdPreview.setPadding(kbdPadding, kbdPadding, kbdPadding, kbdPadding);

        try {
            clearCustomFont();
            getCustomFont();
        } catch (Throwable ignored) {}
    }
}
