package org.blinksd.board.activities.settings;

import static org.blinksd.utils.SuperDBHelper.getFloatPercentOrDefault;
import static org.blinksd.utils.SuperDBHelper.getIntOrDefault;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.blinksd.board.SuperBoardApplication;
import org.blinksd.board.views.CustomActionBar;
import org.blinksd.board.views.SuperTab;
import org.blinksd.utils.DensityUtils;
import org.blinksd.utils.IconThemeUtils;
import org.blinksd.utils.ImageUtils;
import org.blinksd.utils.LayoutCreator;
import org.blinksd.utils.LayoutUtils;
import org.blinksd.utils.LocalIconTheme;
import org.blinksd.utils.ResourcesUtils;
import org.blinksd.utils.SettingMap;
import org.blinksd.utils.SuperDBHelper;

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

    private void createAppBarView() {
        actionBar = new CustomActionBar(this);
        onTabChangedListener.onTabChanged(0);
        main.addView(actionBar);
    }

    private void createTabBarView() {
        mTabsHolder = new FrameLayout(this);
        mTabsHolder.setLayoutParams(new LinearLayout.LayoutParams(-1, -1, 1));
        superTab = new SuperTab(this, mTabsHolder);
        superTab.setBackgroundColor(0);
        superTab.setOnTabChangedListener(onTabChangedListener);
        main.addView(mTabsHolder);
        main.addView(superTab);
        addCategories();
    }

    @Override
    public void setKeyPrefs() {
        boolean useMonet = SuperDBHelper.getBooleanOrDefault(SettingMap.SET_USE_MONET);
        superTab.toggleButton(superTab.getChildCount() - 1, !useMonet);

        File img = SuperBoardApplication.getBackgroundImageFile();
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
        IconThemeUtils iconThemes = SuperBoardApplication.getIconThemes();
        kbdPreview.setKeyDrawable(0, 0, 2,
                iconThemes.getIconResource(LocalIconTheme.SYM_TYPE_DELETE));
        LayoutUtils.setSpaceBarViewPrefs(iconThemes,
                kbdPreview.getKey(0, 0, 1),
                SuperBoardApplication.getCurrentKeyboardLanguage().name);
        kbdPreview.setKeyDrawable(0, 0, -1,
                iconThemes.getIconResource(LocalIconTheme.SYM_TYPE_ENTER));
        kbdPreview.setKeyVibrateDuration(getIntOrDefault(SettingMap.SET_KEY_VIBRATE_DURATION));
        kbdPreview.setKeysTextColor(getIntOrDefault(SettingMap.SET_KEY_TEXTCLR));
        try {
            SuperBoardApplication.clearCustomFont();
            SuperBoardApplication.getCustomFont();
        } catch (Throwable ignored) {}
    }
}
