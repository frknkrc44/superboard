package org.blinksd.board.activities;

import static org.blinksd.board.SuperBoardApplication.getAppDB;
import static org.blinksd.board.SuperBoardApplication.getSettings;
import static org.blinksd.utils.SuperDBHelper.getBooleanOrDefault;
import static org.blinksd.utils.SuperDBHelper.getFloatPercentOrDefault;
import static org.blinksd.utils.SuperDBHelper.getIntOrDefault;
import static org.blinksd.utils.SuperDBHelper.setColorsFromBitmap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import org.blinksd.board.R;
import org.blinksd.board.SuperBoardApplication;
import org.blinksd.board.services.KeyboardThemeApi;
import org.blinksd.board.views.ColorSelectorLayout;
import org.blinksd.board.views.CustomActionBar;
import org.blinksd.board.views.ImageSelectorLayout;
import org.blinksd.board.views.NumberSelectorLayout;
import org.blinksd.board.views.RadioSelectorLayout;
import org.blinksd.board.views.SuperBoard;
import org.blinksd.board.views.SuperTab;
import org.blinksd.utils.ColorUtils;
import org.blinksd.utils.DensityUtils;
import org.blinksd.utils.IconThemeUtils;
import org.blinksd.utils.ImageUtils;
import org.blinksd.utils.LayoutCreator;
import org.blinksd.utils.LayoutUtils;
import org.blinksd.utils.LocalIconTheme;
import org.blinksd.utils.ResourcesUtils;
import org.blinksd.utils.SettingCategory;
import org.blinksd.utils.SettingItem;
import org.blinksd.utils.SettingMap;
import org.blinksd.utils.SettingType;
import org.blinksd.utils.SuperDBHelper;
import org.blinksd.utils.ThemeUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

public class AppSettingsV3 extends BaseActivity {
    private static final int TAG1 = R.id.key_np, TAG2 = R.id.key_lp;
    private LinearLayout main;
    private FrameLayout mTabsHolder;
    private CustomActionBar actionBar;
    private SuperTab superTab;
    public SuperBoard kbdPreview;
    private ImageView backgroundImageView;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        createMainView();
        setKeyPrefs();
        setContentView(main);
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

    private void createMainView() {
        main = LayoutCreator.createFilledVerticalLayout(FrameLayout.class, this);
        createAppBarView();
        createPreviewView();
        createTabBarView();
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

    private void addCategories() {
        for (int i = 0; i < SettingCategory.values().length; i++) {
            addCategoryChildren(i);
            superTab.addButton(getCategoryIconResource(i));
        }
    }

    private int getCategoryIconResource(int categoryIndex) {
        switch (categoryIndex) {
            case 0:
                return R.drawable.more_control;
            case 1:
                return R.drawable.keyboard;
            case 2:
                return R.drawable.view_compact_alt;
            case 3:
                return R.drawable.web_asset;
            case 4:
                return R.drawable.brush;
            case 5:
                return R.drawable.format_paint;
        }

        return R.drawable.arrow_right;
    }

    private void addCategoryChildren(int categoryIndex) {
        SettingCategory category = SettingCategory.values()[categoryIndex];
        ViewGroup categoryView = getCategoryView(categoryIndex);

        SettingMap.ChildIterator categoryItemIterator = (key, item) -> {
            switch (item.type) {
                case REDIRECT:
                    categoryView.addView(createRedirect(key));
                    break;
                case BOOL:
                    categoryView.addView(createBoolSelector(key));
                    break;
                case IMAGE:
                    categoryView.addView(createImageSelector(key));
                    break;
                case THEME_SELECTOR:
                    List<String> themeKeys = ThemeUtils.getThemeNames(SuperBoardApplication.getThemes());
                    categoryView.addView(createRadioSelector(key, themeKeys));
                    break;
                case COLOR_SELECTOR:
                    categoryView.addView(createColorSelector(key));
                    break;
                case STR_SELECTOR:
                case SELECTOR:
                    if (SettingMap.SET_KEYBOARD_LANG_SELECT.equals(key)) {
                        List<String> keySet = SuperBoardApplication.getLanguageHRNames();
                        categoryView.addView(createRadioSelector(key, keySet));
                        return;
                    }

                    List<String> selectorKeys = getArrayAsList(key);
                    categoryView.addView(createRadioSelector(key, selectorKeys));
                    break;
                case DECIMAL_NUMBER:
                case MM_DECIMAL_NUMBER:
                case FLOAT_NUMBER:
                    categoryView.addView(createNumberSelector(key, item.type == SettingType.FLOAT_NUMBER));
                    break;
            }
        };

        getSettings().iterChild(category, categoryItemIterator);
    }

    private final SuperTab.OnTabChangedListener onTabChangedListener =
            index -> actionBar.setTitle(getArrayAsList("categories").get(index));

    private ViewGroup getCategoryView(int categoryIndex) {
        if (mTabsHolder.getChildCount() - 1 < categoryIndex) {
            LinearLayout categoryLayout = LayoutCreator.createFilledVerticalLayout(ScrollView.class, this);
            ScrollView scrollView = new ScrollView(this);
            scrollView.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
            scrollView.addView(categoryLayout);
            mTabsHolder.addView(scrollView);
            return categoryLayout;
        }

        return (ViewGroup) mTabsHolder.getChildAt(categoryIndex);
    }

    private View createNumberSelector(String key, boolean isFloat) {
        int num = SuperDBHelper.getIntOrDefault(key);
        LinearLayout numSelector = LayoutCreator.createFilledHorizontalLayout(AbsListView.class, this);
        numSelector.getLayoutParams().height = -2;
        TextView img = LayoutCreator.createTextView(this);
        img.setId(android.R.id.text1);
        int size = (int) getListPreferredItemHeight();
        img.setGravity(Gravity.CENTER);
        img.setTextColor(Color.WHITE);
        img.setText(isFloat
                ? String.valueOf(DensityUtils.getFloatNumberFromInt(num))
                : String.valueOf(num));
        img.setLayoutParams(LayoutCreator.createLayoutParams(LinearLayout.class, size, size));
        int pad = size / 4;
        img.setPadding(pad, pad, pad, pad);
        TextView btn = LayoutCreator.createTextView(this);
        btn.setGravity(Gravity.CENTER_VERTICAL);
        btn.setTextColor(Color.WHITE);
        btn.setMinHeight(size);
        btn.setText(getTranslation(key));
        numSelector.setTag(key);
        numSelector.setMinimumHeight(size);
        numSelector.setOnClickListener(numberSelectorListener);
        numSelector.addView(img);
        numSelector.addView(btn);
        return numSelector;
    }

    private View createColorSelector(String key) {
        int color = SuperDBHelper.getIntOrDefault(key);
        LinearLayout colSelector = LayoutCreator.createFilledHorizontalLayout(AbsListView.class, this);
        colSelector.getLayoutParams().height = -2;
        ImageView img = LayoutCreator.createImageView(this);
        img.setId(android.R.id.icon);
        int size = (int) getListPreferredItemHeight();
        img.setLayoutParams(LayoutCreator.createLayoutParams(LinearLayout.class, size, size));
        img.setScaleType(ImageView.ScaleType.FIT_CENTER);
        int pad = size / 4;
        img.setPadding(pad, pad, pad, pad);
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(color);
        gd.setCornerRadius(1000);
        img.setImageDrawable(gd);
        TextView btn = LayoutCreator.createTextView(this);
        btn.setGravity(Gravity.CENTER_VERTICAL);
        btn.setTextColor(Color.WHITE);
        btn.setMinHeight(size);
        btn.setText(getTranslation(key));
        colSelector.setTag(key);
        colSelector.setMinimumHeight(size);
        colSelector.setOnClickListener(colorSelectorListener);
        colSelector.addView(img);
        colSelector.addView(btn);
        return colSelector;
    }

    private View createImageSelector(String key) {
        TextView btn = LayoutCreator.createTextView(this);
        btn.setGravity(Gravity.CENTER_VERTICAL);
        btn.setTextColor(Color.WHITE);
        btn.setMinHeight((int) getListPreferredItemHeight());
        btn.setText(getTranslation(key));
        btn.setTag(key);
        btn.setOnClickListener(imageSelectorListener);
        int pad = (int) (getListPreferredItemHeight() / 4);
        btn.setPadding(pad, 0, pad, 0);
        return btn;
    }

    private View createBoolSelector(String key) {
        int pad = (int) (getListPreferredItemHeight() / 4);

        boolean enabled = getSettings().getSwitchEnabledFromDependency(key);
        boolean val = enabled && SuperDBHelper.getBooleanOrDefault(key);

        Switch swtch = LayoutCreator.createFilledSwitch(AbsListView.class, this, getTranslation(key), val, switchListener);
        swtch.setEnabled(enabled);
        swtch.setMinHeight((int) getListPreferredItemHeight());
        swtch.setTag(key);
        swtch.setPadding(pad, 0, pad, 0);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            int minW = DensityUtils.dpInt(32);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                swtch.setSwitchMinWidth(minW);
            } else {
                setSwitchMinWidthOldAndroids(swtch, minW);
            }
        }

        return swtch;
    }

    /** @noinspection JavaReflectionMemberAccess*/
    @SuppressLint("SoonBlockedPrivateApi")
    private void setSwitchMinWidthOldAndroids(Switch view, int minW) {
        try {
            Field minWidth = Switch.class.getDeclaredField("mSwitchMinWidth");
            minWidth.setAccessible(true);
            minWidth.set(view, minW);
        } catch (Throwable ignored) {}
    }

    private View createRadioSelector(String key, List<String> items) {
        View base = createImageSelector(key);
        base.setTag(TAG1, key);
        base.setTag(TAG2, items);
        base.setOnClickListener(radioSelectorListener);
        return base;
    }

    private View createRedirect(String key) {
        View base = createImageSelector(key);
        base.setOnClickListener(redirectListener);
        return base;
    }

    private float getListPreferredItemHeight() {
        TypedValue value = new TypedValue();
        this.getTheme().resolveAttribute(android.R.attr.listPreferredItemHeight, value, true);
        return TypedValue.complexToDimension(value.data, this.getResources().getDisplayMetrics());
    }

    private String getTranslation(String key) {
        return getTranslation(this, key);
    }

    @SuppressLint("DiscouragedApi")
    public static String getTranslation(Context context, String key) {
        String requestedKey = "settings_" + key;
        try {
            int id = context.getResources().getIdentifier(requestedKey, "string", context.getPackageName());
            return context.getString(id);
        } catch (Throwable ignored) {}
        return requestedKey;
    }

    @SuppressLint("DiscouragedApi")
    private List<String> getArrayAsList(String key) {
        int id;
        try {
            id = this.getResources().getIdentifier("settings_" + key, "array", this.getPackageName());
        } catch (Throwable t) {
            id = 0;
        }

        if (id > 0) {
            try {
                String[] arr = this.getResources().getStringArray(id);
                return new ArrayList<>(Arrays.asList(arr));
            } catch (Throwable ignored) {}
        }
        return getSettings().getSelector(key);
    }

    public static void doHacksAndShow(AlertDialog dialog) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            GradientDrawable gradientDrawable = new GradientDrawable();
            int color = ResourcesUtils.getColor(android.R.color.system_neutral1_900);
            gradientDrawable.setColor(color);
            gradientDrawable.setCornerRadius(DensityUtils.dpInt(16));
            gradientDrawable.setTint(color);
            Window window = dialog.getWindow();
            if (window != null) {
                window.getDecorView().setBackground(gradientDrawable);
            }
        }

        dialog.show();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int tint = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                    ? ResourcesUtils.getColor(android.R.color.system_accent1_200)
                    : ColorUtils.getAccentColor();

            Button btn1 = dialog.findViewById(android.R.id.button1);
            Button btn2 = dialog.findViewById(android.R.id.button2);
            Button btn3 = dialog.findViewById(android.R.id.button3);

            if (btn1 != null) {
                btn1.setTextColor(tint);
                btn1.setAllCaps(false);
            }

            if (btn2 != null) {
                btn2.setTextColor(tint);
                btn2.setAllCaps(false);
            }

            if (btn3 != null) {
                btn3.setTextColor(tint);
                btn3.setAllCaps(false);
            }
        }
    }

    public void restartKeyboard() {
        setKeyPrefs();
        KeyboardThemeApi.restartKeyboard();
    }

    private final View.OnClickListener redirectListener = p1 -> {
        Intent intent = getSettings().getRedirect(p1.getContext(), (String) p1.getTag());
        if (intent.getData() != null) {
            intent.setData(null);
            ((Activity) p1.getContext()).startActivityForResult(intent, 2);
            return;
        }

        p1.getContext().startActivity(intent);
    };
    private final Switch.OnCheckedChangeListener switchListener = (buttonView, isChecked) -> {
        String str = (String) buttonView.getTag();
        getAppDB().putBoolean(str, isChecked, true);
        if (SettingMap.SET_USE_MONET.equals(str)) {
            superTab.toggleButton(superTab.getChildCount() - 1, !isChecked);
        }
        restartKeyboard();
    };

    public View dialogView;
    private final View.OnClickListener colorSelectorListener = new View.OnClickListener() {

        @Override
        public void onClick(View p1) {
            AlertDialog.Builder build = new AlertDialog.Builder(p1.getContext());
            final String tag = p1.getTag().toString();
            build.setTitle(getTranslation(tag));
            final int val = SuperDBHelper.getIntOrDefault(tag);
            dialogView = new ColorSelectorLayout(AppSettingsV3.this, p1.getTag().toString());
            dialogView.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
            build.setView(dialogView);
            build.setNegativeButton(android.R.string.cancel, (p11, p2) -> p11.dismiss());
            build.setNeutralButton(R.string.settings_return_defaults, (d1, p2) -> {
                int tagVal = (int) getSettings().getDefaults(tag);
                getAppDB().putInteger(tag, tagVal, true);
                ImageView img = p1.findViewById(android.R.id.icon);
                GradientDrawable gd = new GradientDrawable();
                gd.setColor(tagVal);
                gd.setCornerRadius(1000);
                img.setImageDrawable(gd);
                restartKeyboard();
                d1.dismiss();
            });
            build.setPositiveButton(android.R.string.ok, (d1, p2) -> {
                int tagVal = ((ColorSelectorLayout) dialogView).colorValue;
                if (tagVal != val) {
                    getAppDB().putInteger(tag, tagVal, true);
                    ImageView img = p1.findViewById(android.R.id.icon);
                    GradientDrawable gd = new GradientDrawable();
                    gd.setColor(tagVal);
                    gd.setCornerRadius(1000);
                    img.setImageDrawable(gd);
                    restartKeyboard();
                }
                d1.dismiss();
            });

            doHacksAndShow(build.create());
        }

    };
    private final View.OnClickListener numberSelectorListener = new View.OnClickListener() {

        @Override
        public void onClick(final View p1) {
            AlertDialog.Builder build = new AlertDialog.Builder(p1.getContext());
            final String tag = p1.getTag().toString();
            build.setTitle(getTranslation(tag));
            AppSettingsV3 act = (AppSettingsV3) p1.getContext();
            final boolean isFloat = getSettings().get(tag).type == SettingType.FLOAT_NUMBER;
            int[] minMax = getSettings().getMinMaxNumbers(tag);
            final int val = SuperDBHelper.getIntOrDefault(tag);
            dialogView = NumberSelectorLayout.getNumberSelectorLayout(act, isFloat, minMax[0], minMax[1], val);
            build.setView(dialogView);
            build.setNegativeButton(android.R.string.cancel, (p11, p2) -> p11.dismiss());
            build.setNeutralButton(R.string.settings_return_defaults, (d1, p2) -> {
                int tagVal = (int) getSettings().getDefaults(tag);
                getAppDB().putInteger(tag, tagVal, true);
                TextView tv = p1.findViewById(android.R.id.text1);
                tv.setText(isFloat
                        ? String.valueOf(DensityUtils.getFloatNumberFromInt(tagVal))
                        : String.valueOf(tagVal));
                restartKeyboard();
                if (SettingMap.SET_KEY_ICON_SIZE_MULTIPLIER.equals(tag)) {
                    recreate();
                }
                d1.dismiss();
            });
            build.setPositiveButton(android.R.string.ok, (d1, p2) -> {
                int tagVal = (int) dialogView.getTag();
                if (tagVal != val) {
                    getAppDB().putInteger(tag, tagVal, true);
                    TextView tv = p1.findViewById(android.R.id.text1);
                    tv.setText(isFloat
                            ? String.valueOf(DensityUtils.getFloatNumberFromInt(tagVal))
                            : String.valueOf(tagVal));
                    restartKeyboard();
                    if (SettingMap.SET_KEY_ICON_SIZE_MULTIPLIER.equals(tag)) {
                        recreate();
                    }
                }
                d1.dismiss();
            });

            doHacksAndShow(build.create());
        }

    };
    private final View.OnClickListener imageSelectorListener = new View.OnClickListener() {

        @Override
        public void onClick(View p1) {
            AlertDialog.Builder build = new AlertDialog.Builder(p1.getContext());
            build.setTitle(getTranslation(p1.getTag().toString()));
            build.setNegativeButton(android.R.string.cancel, (p11, p2) -> p11.dismiss());
            build.setPositiveButton(android.R.string.ok, (p112, p2) -> {
                ImageView img = dialogView.findViewById(android.R.id.custom);
                Drawable d = img.getDrawable();
                if (d != null) {
                    try {
                        File bgFile = SuperBoardApplication.getBackgroundImageFile();
                        Bitmap bmp = ((BitmapDrawable) d).getBitmap();
                        setColorsFromBitmap(bmp);
                        FileOutputStream fos = new FileOutputStream(bgFile);
                        bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    } catch (Throwable ignored) {
                    }
                    restartKeyboard();
                    recreate();
                }
                p112.dismiss();
            });
            AlertDialog dialog = build.create();
            dialogView = new ImageSelectorLayout(dialog, () -> {
                Intent i = new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(i, ""), 1);
            }, AppSettingsV3.this::restartKeyboard);
            dialogView.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));

            dialog.setView(dialogView);
            doHacksAndShow(dialog);
        }

    };
    private final View.OnClickListener radioSelectorListener = new View.OnClickListener() {

        @Override
        @SuppressWarnings("unchecked")
        public void onClick(final View p1) {
            AlertDialog.Builder build = new AlertDialog.Builder(p1.getContext());
            final String tag = p1.getTag(TAG1).toString();
            int val;
            final SettingItem item = getSettings().get(tag);
            final boolean langSelector = item.type == SettingType.STR_SELECTOR && SettingMap.SET_KEYBOARD_LANG_SELECT.equals(tag);
            final boolean iconSelector = item.type == SettingType.STR_SELECTOR && SettingMap.SET_ICON_THEME.equals(tag);
            final boolean spaceSelector = item.type == SettingType.STR_SELECTOR && SettingMap.SET_KEYBOARD_SPACETYPE_SELECT.equals(tag);
            final boolean themeSelector = item.type == SettingType.THEME_SELECTOR;
            if (langSelector || iconSelector || spaceSelector) {
                String value = SuperDBHelper.getStringOrDefault(tag);
                if (langSelector)
                    val = LayoutUtils.getKeyListFromLanguageList().indexOf(value);
                else if (iconSelector)
                    val = SuperBoardApplication.getIconThemes().indexOfKey(value);
                else
                    val = SuperBoardApplication.getSpaceBarStyles().indexOfKey(value);
            } else if (themeSelector) {
                val = -1;
            } else {
                val = SuperDBHelper.getIntOrDefault(tag);
            }
            build.setTitle(getTranslation(tag));
            ScrollView dialogScroller = new ScrollView(p1.getContext());
            dialogView = new RadioSelectorLayout(AppSettingsV3.this, val, (List<String>) p1.getTag(TAG2));
            dialogScroller.addView(dialogView);
            build.setView(dialogScroller);
            build.setNegativeButton(android.R.string.cancel, (p11, p2) -> p11.dismiss());
            if (!themeSelector)
                build.setNeutralButton(R.string.settings_return_defaults, (p112, p2) -> {
                    if (langSelector || iconSelector || spaceSelector)
                        getAppDB().putString(tag, (String) getSettings().getDefaults(tag), true);
                    else getAppDB().putInteger(tag, (int) getSettings().getDefaults(tag), true);
                    restartKeyboard();
                    p112.dismiss();
                });
            final int xval = val;
            build.setPositiveButton(android.R.string.ok, (p113, p2) -> {
                int tagVal = (int) dialogView.getTag();
                if (tagVal != xval) {
                    if (langSelector) {
                        String index = LayoutUtils.getKeyListFromLanguageList().get(tagVal);
                        getAppDB().putString(tag, index, true);
                    } else if (iconSelector) {
                        String index = SuperBoardApplication.getIconThemes().getKeyByIndex(tagVal);
                        getAppDB().putString(tag, index, true);
                    } else if (spaceSelector) {
                        String index = SuperBoardApplication.getSpaceBarStyles().getKeyByIndex(tagVal);
                        getAppDB().putString(tag, index, true);
                    } else if (themeSelector) {
                        List<ThemeUtils.ThemeHolder> themes = SuperBoardApplication.getThemes();
                        ThemeUtils.ThemeHolder theme = themes.get(tagVal);
                        theme.applyTheme();
                        recreate();
                    } else getAppDB().putInteger(tag, tagVal, true);
                    restartKeyboard();
                }
                p113.dismiss();
            });

            doHacksAndShow(build.create());
        }
    };

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
                backgroundImageView.setImageBitmap(result);
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
