package org.blinksd.board.activities.settings;

import static org.blinksd.board.SuperBoardApplication.getAppDB;
import static org.blinksd.board.SuperBoardApplication.getSettings;
import static org.blinksd.utils.ResourcesUtils.getListPreferredItemHeight;
import static org.blinksd.utils.SuperDBHelper.setColorsFromBitmap;
import static org.blinksd.utils.SystemUtils.isPermGranted;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.blinksd.board.R;
import org.blinksd.board.SuperBoardApplication;
import org.blinksd.board.views.ColorSelectorLayout;
import org.blinksd.board.views.ImageSelectorLayout;
import org.blinksd.board.views.NumberSelectorLayout;
import org.blinksd.board.views.RadioSelectorLayout;
import org.blinksd.utils.DensityUtils;
import org.blinksd.utils.LayoutCreator;
import org.blinksd.utils.LayoutUtils;
import org.blinksd.utils.SettingItem;
import org.blinksd.utils.SettingMap;
import org.blinksd.utils.SettingType;
import org.blinksd.utils.SuperDBHelper;
import org.blinksd.utils.ThemeUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.List;

public abstract class SettingsSelectorsActivity extends SettingsBaseActivity {
    private static final int TAG1 = R.id.key_normal_press, TAG2 = R.id.key_long_press;

    View createNumberSelector(String key, boolean isFloat) {
        int num = SuperDBHelper.getIntOrDefault(key);
        LinearLayout numSelector = LayoutCreator.createFilledHorizontalLayout(AbsListView.class, this);
        numSelector.getLayoutParams().height = -2;
        TextView img = LayoutCreator.createTextView(this);
        img.setId(android.R.id.text1);
        final int size = (int) getListPreferredItemHeight(this);
        img.setGravity(Gravity.CENTER);
        img.setTextColor(Color.WHITE);
        // img.setTextSize(DensityUtils.dpInt(12));
        img.setText(isFloat
                ? String.valueOf(DensityUtils.getFloatNumberFromInt(num))
                : String.valueOf(num));
        img.setLayoutParams(LayoutCreator.createLayoutParams(LinearLayout.class, size, size, 0));
        int pad = size / 4;
        img.setPadding(pad, pad, pad, pad);
        TextView btn = LayoutCreator.createTextView(this);
        btn.setLayoutParams(LayoutCreator.createLayoutParams(LinearLayout.class, -1, -2, 1));
        btn.setPadding(pad, 0, 0, 0);
        btn.setGravity(Gravity.CENTER_VERTICAL);
        btn.setTextColor(Color.WHITE);
        btn.setMinHeight(size);
        btn.setText(getTranslation(key));
        numSelector.setTag(key);
        numSelector.setMinimumHeight(size);
        numSelector.setOnClickListener(numberSelectorListener);
        numSelector.addView(btn);
        numSelector.addView(img);
        return numSelector;
    }

    View createColorSelector(String key) {
        int color = SuperDBHelper.getIntOrDefault(key);
        LinearLayout colSelector = LayoutCreator.createFilledHorizontalLayout(AbsListView.class, this);
        colSelector.getLayoutParams().height = -2;
        ImageView img = LayoutCreator.createImageView(this);
        img.setId(android.R.id.icon);
        final int size = (int) getListPreferredItemHeight(this);
        img.setLayoutParams(LayoutCreator.createLayoutParams(LinearLayout.class, size, size, 0));
        img.setScaleType(ImageView.ScaleType.FIT_CENTER);
        final int pad = size / 4;
        img.setPadding(pad, pad, pad, pad);
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(color);
        gd.setCornerRadius(1000);
        img.setImageDrawable(gd);
        TextView btn = LayoutCreator.createTextView(this);
        btn.setLayoutParams(LayoutCreator.createLayoutParams(LinearLayout.class, -1, -2, 1));
        btn.setPadding(pad, 0, 0, 0);
        btn.setGravity(Gravity.CENTER_VERTICAL);
        btn.setTextColor(Color.WHITE);
        btn.setMinHeight(size);
        btn.setText(getTranslation(key));
        colSelector.setTag(key);
        colSelector.setMinimumHeight(size);
        colSelector.setOnClickListener(colorSelectorListener);
        colSelector.addView(btn);
        colSelector.addView(img);
        return colSelector;
    }

    View createImageSelector(String key) {
        final float listItemHeight = getListPreferredItemHeight(this);
        TextView btn = LayoutCreator.createTextView(this);
        btn.setGravity(Gravity.CENTER_VERTICAL);
        btn.setTextColor(Color.WHITE);
        btn.setMinHeight((int) listItemHeight);
        btn.setText(getTranslation(key));
        btn.setTag(key);
        btn.setOnClickListener(imageSelectorListener);
        int pad = (int) (listItemHeight / 4);
        btn.setPadding(pad, 0, pad, 0);
        return btn;
    }

    View createBoolSelector(String key) {
        final float listItemHeight = getListPreferredItemHeight(this);
        int pad = (int) (listItemHeight / 4);

        boolean enabled = getSettings().getSwitchEnabledFromDependency(key);
        boolean val = enabled && SuperDBHelper.getBooleanOrDefault(key);

        Switch swtch = LayoutCreator.createFilledSwitch(AbsListView.class, this, getTranslation(key), val, switchListener);
        swtch.setEnabled(enabled);
        swtch.setMinHeight((int) listItemHeight);
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

    View createRadioSelector(String key, List<String> items) {
        View base = createImageSelector(key);
        base.setTag(TAG1, key);
        base.setTag(TAG2, items);
        base.setOnClickListener(radioSelectorListener);
        return base;
    }

    View createRedirect(String key) {
        View base = createImageSelector(key);
        base.setOnClickListener(redirectListener);
        return base;
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

    private final View.OnClickListener redirectListener = p1 -> {
        Intent intent = getSettings().getRedirect(p1.getContext(), (String) p1.getTag());
        if (intent.getData() != null) {
            intent.setData(null);
            ((Activity) p1.getContext()).startActivityForResult(intent, 2);
            return;
        }

        p1.getContext().startActivity(intent);
    };

    final Switch.OnCheckedChangeListener switchListener = (buttonView, isChecked) -> {
        String str = (String) buttonView.getTag();
        Context context = buttonView.getContext();

        if (isChecked && str.equals(SettingMap.SET_USE_COMPAT_MONET) && !isPermGranted(context)) {
            Toast.makeText(context,
                    getTranslation("image_selector_warning_storage_access"),
                    Toast.LENGTH_LONG).show();
            context.startActivity(new Intent(
                    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                            ? Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            : Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                    Uri.parse("package:" + context.getPackageName())));
            buttonView.setChecked(false);
            return;
        }

        getAppDB().putBoolean(str, isChecked, true);
        restartKeyboard();
    };

    private final View.OnClickListener colorSelectorListener = p1 -> {
        AlertDialog.Builder build = new AlertDialog.Builder(p1.getContext());
        final String tag = p1.getTag().toString();
        build.setTitle(getTranslation(tag));
        final int val = SuperDBHelper.getIntOrDefault(tag);
        dialogView = new ColorSelectorLayout(p1.getContext(), p1.getTag().toString());
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
            int tagVal = ((ColorSelectorLayout) dialogView).currentColorValue;
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
    };

    private final View.OnClickListener numberSelectorListener = p1 -> {
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
    };

    private final View.OnClickListener imageSelectorListener = p1 -> {
        AlertDialog.Builder build = new AlertDialog.Builder(p1.getContext());
        build.setTitle(getTranslation(p1.getTag().toString()));
        build.setNegativeButton(android.R.string.cancel, (p11, p2) -> p11.dismiss());
        build.setPositiveButton(android.R.string.ok, (p112, p2) -> {
            ImageView img = dialogView.findViewById(R.id.dialog_image_preview);
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
        }, this::restartKeyboard);
        dialogView.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));

        dialog.setView(dialogView);
        doHacksAndShow(dialog);
    };

    private final View.OnClickListener radioSelectorListener = p1 -> {
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
            val = SettingMap.SET_MONET_COLOR_SCHEME.equals(tag)
                    ? SuperBoardApplication.getMonetColors().getSelectedMonetThemeIndex()
                    : -1;
        } else {
            val = SuperDBHelper.getIntOrDefault(tag);
        }

        build.setTitle(getTranslation(tag));
        ScrollView dialogScroller = new ScrollView(p1.getContext());
        dialogView = new RadioSelectorLayout(p1.getContext(), val, (List<String>) p1.getTag(TAG2));
        dialogScroller.addView(dialogView);
        build.setView(dialogScroller);
        build.setNegativeButton(android.R.string.cancel, (p11, p2) -> p11.dismiss());
        if (!themeSelector) {
            build.setNeutralButton(R.string.settings_return_defaults, (p112, p2) -> {
                if (langSelector || iconSelector || spaceSelector)
                    getAppDB().putString(tag, (String) getSettings().getDefaults(tag), true);
                else getAppDB().putInteger(tag, (int) getSettings().getDefaults(tag), true);
                restartKeyboard();
                p112.dismiss();
            });
        }

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
                    switch (tag) {
                        case SettingMap.SET_MONET_COLOR_SCHEME:
                            String index = SuperBoardApplication.getMonetColors().getKeyByIndex(tagVal);
                            getAppDB().putString(tag, index, true);
                            break;
                        case SettingMap.SET_THEME_PRESET:
                            List<ThemeUtils.ThemeHolder> themes = SuperBoardApplication.getThemes();
                            ThemeUtils.ThemeHolder theme = themes.get(tagVal);
                            theme.applyTheme();
                            recreate();
                            break;
                    }
                } else getAppDB().putInteger(tag, tagVal, true);
                restartKeyboard();
            }
            p113.dismiss();
        });

        doHacksAndShow(build.create());
    };
}
