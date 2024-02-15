package org.blinksd.utils;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;

import org.blinksd.board.SuperBoardApplication;
import org.blinksd.board.activities.BackupRestoreActivity;
import org.blinksd.board.activities.FontSelector;
import org.blinksd.board.activities.KeyboardLayoutSelector;
import org.blinksd.board.dictionary.DictionaryImportActivity;

import java.util.ArrayList;
import java.util.List;

public class SettingMap extends ListedMap<String, SettingItem> {

    public static final String SET_KEYBOARD_LANG_SELECT = "keyboard_lang_select",
            SET_KEYBOARD_TEXTTYPE_SELECT = "keyboard_texttype_select",
            SET_KEYBOARD_SPACETYPE_SELECT = "keyboard_spacetype_select",
            SET_KEYBOARD_BGIMG = "keyboard_bgimg",
            SET_KEYBOARD_BGBLUR = "keyboard_bgblur",
            SET_KEYBOARD_HEIGHT = "keyboard_height",
            SET_KEYBOARD_BGCLR = "keyboard_bgclr",
            SET_KEYBOARD_SHOW_POPUP = "keyboard_show_popup",
            SET_KEYBOARD_LC_ON_EMOJI = "keyboard_lc_on_emoji",
            SET_PLAY_SND_PRESS = "play_snd_press",
            SET_KEY_BGCLR = "key_bgclr",
            SET_KEY_PRESS_BGCLR = "key_press_bgclr",
            SET_KEY_BG_TYPE = "key_bg_type",
            SET_KEY2_BGCLR = "key2_bgclr",
            SET_KEY2_PRESS_BGCLR = "key2_press_bgclr",
            SET_ENTER_BGCLR = "enter_bgclr",
            SET_ENTER_PRESS_BGCLR = "enter_press_bgclr",
            SET_KEY_GRADIENT_ORIENTATION = "key_gradient_orientation",
            SET_KEY_SHADOWCLR = "key_shadowclr",
            SET_KEY_PADDING = "key_padding",
            SET_KEY_RADIUS = "key_radius",
            SET_KEY_TEXTSIZE = "key_textsize",
            SET_KEY_SHADOWSIZE = "key_shadowsize",
            SET_KEY_VIBRATE_DURATION = "key_vibrate_duration",
            SET_KEY_LONGPRESS_DURATION = "key_longpress_duration",
            SET_KEY_TEXTCLR = "key_textclr",
            SET_COLORIZE_NAVBAR = "colorize_navbar",
            SET_DETECT_CAPSLOCK = "detect_capslock",
            SET_COLORIZE_NAVBAR_ALT = "colorize_navbar_alt",
            SET_DISABLE_POPUP = "disable_popup",
            SET_DISABLE_REPEAT = "disable_repeat",
            SET_DISABLE_SUGGESTIONS = "disable_suggestions",
            SET_USE_MONET = "use_monet",
            SET_ENABLE_POPUP_PREVIEW = "enable_popup_preview",
            SET_ICON_THEME = "keyboard_icon_theme",
            SET_KILL_BACKGROUND = "keyboard_kill_background",
            SET_THEME_PRESET = "keyboard_theme_preset",
            SET_KEY_ICON_SIZE_MULTIPLIER = "key_icon_size_multi",
            SET_IMPORT_DICT_PACK = "import_dict_pack",
            SET_DISABLE_TOP_BAR = "disable_top_bar",
            SET_DISABLE_NUMBER_ROW = "disable_number_row",
            SET_USE_FIRST_POPUP_CHARACTER = "use_first_popup_character",
            SET_CLIPBOARD_HISTORY = "clipboard_history",
            SET_HIDE_TOP_BAR_FN_BUTTONS = "hide_top_bar_fn_buttons",
            SET_ENABLE_CLIPBOARD = "enable_clipboard",
            SET_BACKUP_RESTORE = "backup_menu";

    public SettingMap() {
        putGeneral(SET_BACKUP_RESTORE, SettingType.REDIRECT);
        putGeneral(SET_KEYBOARD_LANG_SELECT,  SettingType.REDIRECT);
        putGeneral(SET_IMPORT_DICT_PACK,  SettingType.REDIRECT);
        putTheming(SET_KEYBOARD_TEXTTYPE_SELECT,  SettingType.REDIRECT);
        putTheming(SET_KEYBOARD_SPACETYPE_SELECT, SettingType.STR_SELECTOR);
        putThemingAdvanced(SET_THEME_PRESET, SettingType.THEME_SELECTOR);
        putTheming(SET_ICON_THEME, SettingType.STR_SELECTOR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            putTheming(SET_KEY_BG_TYPE, SettingType.SELECTOR);
            putTheming(SET_KEY_GRADIENT_ORIENTATION, SettingType.SELECTOR);
        }
        putThemingAdvanced(SET_KEYBOARD_BGIMG, SettingType.IMAGE);
        putGeneral(SET_KEYBOARD_SHOW_POPUP, SettingType.BOOL);
        putGeneral(SET_PLAY_SND_PRESS, SettingType.BOOL);
        putGeneral(SET_KEYBOARD_LC_ON_EMOJI, SettingType.BOOL);
        if (!SystemUtils.isNotColorizeNavbar())
            putTheming(SET_COLORIZE_NAVBAR, SettingType.BOOL, SET_COLORIZE_NAVBAR_ALT, false);
        if (Build.VERSION.SDK_INT >= 28)
            putTheming(SET_COLORIZE_NAVBAR_ALT, SettingType.BOOL, SET_COLORIZE_NAVBAR, false);
        putGeneral(SET_DISABLE_POPUP, SettingType.BOOL);
        putGeneral(SET_USE_FIRST_POPUP_CHARACTER, SettingType.BOOL, SET_DISABLE_POPUP, false);
        putGeneral(SET_DISABLE_REPEAT, SettingType.BOOL);
        putGeneral(SET_DISABLE_TOP_BAR, SettingType.BOOL, SET_DISABLE_NUMBER_ROW, false);
        putGeneral(SET_HIDE_TOP_BAR_FN_BUTTONS, SettingType.BOOL, SET_DISABLE_TOP_BAR, false);
        putGeneral(SET_ENABLE_CLIPBOARD, SettingType.BOOL, SET_DISABLE_TOP_BAR, false);
        putGeneral(SET_DISABLE_SUGGESTIONS, SettingType.BOOL);
        putGeneral(SET_DISABLE_NUMBER_ROW, SettingType.BOOL, SET_DISABLE_TOP_BAR, false);
        if (Build.VERSION.SDK_INT >= 31)
            putTheming(SET_USE_MONET, SettingType.BOOL);
        putTheming(SET_ENABLE_POPUP_PREVIEW, SettingType.BOOL);
        putGeneral(SET_DETECT_CAPSLOCK, SettingType.BOOL);
        putGeneral(SET_KILL_BACKGROUND, SettingType.BOOL);
        putThemingAdvanced(SET_KEYBOARD_BGBLUR, SettingType.DECIMAL_NUMBER);
        putGeneral(SET_KEYBOARD_HEIGHT, SettingType.MM_DECIMAL_NUMBER);
        putGeneral(SET_KEY_VIBRATE_DURATION, SettingType.DECIMAL_NUMBER);
        putGeneral(SET_KEY_LONGPRESS_DURATION, SettingType.MM_DECIMAL_NUMBER);
        putThemingAdvanced(SET_KEYBOARD_BGCLR, SettingType.COLOR_SELECTOR);
        putThemingAdvanced(SET_KEY_BGCLR, SettingType.COLOR_SELECTOR);
        putThemingAdvanced(SET_KEY2_BGCLR, SettingType.COLOR_SELECTOR);
        putThemingAdvanced(SET_ENTER_BGCLR, SettingType.COLOR_SELECTOR);
        putThemingAdvanced(SET_KEY_PRESS_BGCLR, SettingType.COLOR_SELECTOR);
        putThemingAdvanced(SET_KEY2_PRESS_BGCLR, SettingType.COLOR_SELECTOR);
        putThemingAdvanced(SET_ENTER_PRESS_BGCLR, SettingType.COLOR_SELECTOR);
        putThemingAdvanced(SET_KEY_SHADOWCLR, SettingType.COLOR_SELECTOR);
        putThemingAdvanced(SET_KEY_TEXTCLR, SettingType.COLOR_SELECTOR);
        putTheming(SET_KEY_PADDING, SettingType.FLOAT_NUMBER);
        putTheming(SET_KEY_RADIUS, SettingType.FLOAT_NUMBER);
        putTheming(SET_KEY_TEXTSIZE, SettingType.FLOAT_NUMBER);
        putTheming(SET_KEY_SHADOWSIZE, SettingType.FLOAT_NUMBER);
        putTheming(SET_KEY_ICON_SIZE_MULTIPLIER, SettingType.MM_DECIMAL_NUMBER);
    }

    private void putGeneral(String name, SettingType type) {
        putGeneral(name, type, null, null);
    }
    private void putGeneral(String name, SettingType type, String dependency, Object dependencyEnabled) {
        put(name, new SettingItem(SettingCategory.GENERAL, type, dependency, dependencyEnabled));
    }

    private void putTheming(String name, SettingType type) {
        putTheming(name, type, null, null);
    }

    private void putTheming(String name, SettingType type, String dependency, Object dependencyEnabled) {
        put(name, new SettingItem(SettingCategory.THEMING, type, dependency, dependencyEnabled));
    }

    private void putThemingAdvanced(String name, SettingType type) {
        put(name, new SettingItem(SettingCategory.THEMING_ADVANCED, type, null, null));
    }

    public int getChildrenCount(SettingCategory category) {
        int count = 0;
        for (String str : keyList()) {
            if (get(str).category == category)
                count++;
        }
        return count;
    }

    public String getChildKey(SettingCategory category, int idx) {
        int i = 0;
        for (String str : keyList()) {
            if (get(str).category == category) {
                if (i == idx) return str;
                i++;
            }
        }
        return null;
    }

    public Intent getRedirect(Context context, final String key) {
        switch (key) {
            case SET_BACKUP_RESTORE:
                return new Intent(context, BackupRestoreActivity.class).setData(Uri.EMPTY);
            case SET_IMPORT_DICT_PACK:
                return new Intent(context, DictionaryImportActivity.class);
            case SET_KEYBOARD_LANG_SELECT:
                return new Intent(context, KeyboardLayoutSelector.class);
            case SET_KEYBOARD_TEXTTYPE_SELECT:
                return new Intent(context, FontSelector.class);
        }
        return null;
    }

    public List<String> getSelector(final String key) {
        switch (key) {
            case SET_KEY_BG_TYPE:
                return ThemeUtils.getKeyBgTypes();
            case SET_KEY_GRADIENT_ORIENTATION:
                return ThemeUtils.getKeyBgOrientationTypes();
            case SET_KEYBOARD_SPACETYPE_SELECT:
                return SuperBoardApplication.getSpaceBarStyles().keyList();
            case SET_ICON_THEME:
                return SuperBoardApplication.getIconThemes().keyList();
        }
        return new ArrayList<>();
    }

    public Object getDefaults(final String key) {
        if (containsKey(key)) {
            switch (key) {
                case SET_KEYBOARD_BGBLUR:
                    return Defaults.KEYBOARD_BACKGROUND_BLUR;
                case SET_KEY_VIBRATE_DURATION:
                    return Defaults.KEY_VIBRATE_DURATION;
                case SET_KEYBOARD_HEIGHT:
                    return Defaults.KEYBOARD_HEIGHT;
                case SET_KEY_LONGPRESS_DURATION:
                    return Defaults.KEY_LONGPRESS_DURATION;
                case SET_KEY_PADDING:
                    return Defaults.KEY_PADDING;
                case SET_KEY_SHADOWSIZE:
                    return Defaults.KEY_TEXT_SHADOW_SIZE;
                case SET_KEY_RADIUS:
                    return Defaults.KEY_RADIUS;
                case SET_KEY_TEXTSIZE:
                    return Defaults.KEY_TEXT_SIZE;
                case SET_KEYBOARD_LANG_SELECT:
                    return Defaults.KEYBOARD_LANGUAGE_KEY;
                case SET_KEYBOARD_TEXTTYPE_SELECT:
                    return Defaults.KEY_FONT_TYPE;
                case SET_KEYBOARD_BGCLR:
                    return Defaults.KEYBOARD_BACKGROUND_COLOR;
                case SET_KEYBOARD_SHOW_POPUP:
                    return Defaults.KEYBOARD_SHOW_POPUP;
                case SET_KEYBOARD_LC_ON_EMOJI:
                    return Defaults.KEYBOARD_LC_ON_EMOJI;
                case SET_PLAY_SND_PRESS:
                    return Defaults.KEYBOARD_TOUCH_SOUND;
                case SET_KEY_BGCLR:
                    return Defaults.KEY_BACKGROUND_COLOR;
                case SET_KEY2_BGCLR:
                    return Defaults.KEY2_BACKGROUND_COLOR;
                case SET_KEY_PRESS_BGCLR:
                    return Defaults.KEY_PRESS_BACKGROUND_COLOR;
                case SET_KEY2_PRESS_BGCLR:
                    return Defaults.KEY2_PRESS_BACKGROUND_COLOR;
                case SET_ENTER_BGCLR:
                case SET_ENTER_PRESS_BGCLR:
                    if (Build.VERSION.SDK_INT < 21) {
                        return key.equals(SET_ENTER_BGCLR) ? Defaults.ENTER_BACKGROUND_COLOR : Defaults.ENTER_PRESS_BACKGROUND_COLOR;
                    }
                    TypedArray arr = SuperBoardApplication.getApplication().obtainStyledAttributes(0, new int[]{android.R.attr.colorAccent});
                    int color = arr.getColor(0, Defaults.ENTER_BACKGROUND_COLOR);
                    int pressColor = ColorUtils.getDarkerColor(color);
                    arr.recycle();
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            arr.close();
                        }
                    } catch (Throwable ignored) {}
                    return key.equals(SET_ENTER_BGCLR) ? color : pressColor;
                case SET_KEY_BG_TYPE:
                    return Defaults.KEY_BACKGROUND_TYPE;
                case SET_KEY_GRADIENT_ORIENTATION:
                    return Defaults.KEY_BACKGROUND_ORIENTATION_TYPE;
                case SET_KEY_SHADOWCLR:
                    return Defaults.KEY_TEXT_SHADOW_COLOR;
                case SET_KEY_TEXTCLR:
                    return Defaults.KEY_TEXT_COLOR;
                case SET_COLORIZE_NAVBAR:
                    return Defaults.COLORIZE_NAVBAR;
                case SET_DETECT_CAPSLOCK:
                    return Defaults.DETECT_CAPSLOCK;
                case SET_COLORIZE_NAVBAR_ALT:
                    return Defaults.COLORIZE_NAVBAR_ALT;
                case SET_DISABLE_POPUP:
                    return Defaults.DISABLE_POPUP;
                case SET_DISABLE_REPEAT:
                    return Defaults.DISABLE_REPEAT;
                case SET_DISABLE_SUGGESTIONS:
                    return Defaults.DISABLE_SUGGESTIONS;
                case SET_DISABLE_TOP_BAR:
                    return Defaults.DISABLE_TOP_BAR;
                case SET_HIDE_TOP_BAR_FN_BUTTONS:
                    return Defaults.HIDE_TOP_BAR_FN_BUTTONS;
                case SET_ENABLE_CLIPBOARD:
                    return Defaults.ENABLE_CLIPBOARD;
                case SET_DISABLE_NUMBER_ROW:
                    return Defaults.DISABLE_NUMBER_ROW;
                case SET_USE_FIRST_POPUP_CHARACTER:
                    return Defaults.USE_FIRST_POPUP_CHARACTER;
                case SET_USE_MONET:
                    return Defaults.USE_MONET;
                case SET_ENABLE_POPUP_PREVIEW:
                    return Defaults.ENABLE_POPUP_PREVIEW;
                case SET_ICON_THEME:
                    return Defaults.ICON_THEME;
                case SET_KEYBOARD_SPACETYPE_SELECT:
                    return Defaults.KEYBOARD_SPACETYPE;
                case SET_KILL_BACKGROUND:
                    return Defaults.KILL_BACKGROUND;
                case SET_THEME_PRESET:
                    return Defaults.THEME_PRESET;
                case SET_KEY_ICON_SIZE_MULTIPLIER:
                    return Defaults.ICON_SIZE_MULTIPLIER;
            }
        }
        return null;
    }

    public int[] getMinMaxNumbers(final String key) {
        int[] nums = new int[2];
        if (containsKey(key)) {
            switch (get(key).type) {
                case DECIMAL_NUMBER:
                    switch (key) {
                        case SET_KEYBOARD_BGBLUR:
                            nums[1] = Constants.MAX_OTHER_VAL;
                            break;
                        case SET_KEY_VIBRATE_DURATION:
                            nums[1] = Constants.MAX_VIBR_DUR;
                            break;
                    }
                    break;
                case MM_DECIMAL_NUMBER:
                    switch (key) {
                        case SET_KEYBOARD_HEIGHT:
                            nums[0] = Constants.MIN_KEYBD_HGT;
                            nums[1] = Constants.MAX_KEYBD_HGT;
                            break;
                        case SET_KEY_LONGPRESS_DURATION:
                            nums[0] = Constants.MIN_LPRESS_DUR;
                            nums[1] = Constants.MAX_LPRESS_DUR;
                            break;
                        case SET_KEY_ICON_SIZE_MULTIPLIER:
                            nums[0] = Constants.MIN_ICON_MULTI;
                            nums[1] = Constants.MAX_ICON_MULTI;
                    }
                    break;
                case FLOAT_NUMBER:
                    switch (key) {
                        case SET_KEY_PADDING:
                        case SET_KEY_SHADOWSIZE:
                            nums[1] = Constants.MAX_OTHER_VAL;
                            break;
                        case SET_KEY_RADIUS:
                            nums[1] = Constants.MAX_RADS_DUR;
                            break;
                        case SET_KEY_TEXTSIZE:
                            nums[0] = Constants.MIN_TEXT_SIZE;
                            nums[1] = Constants.MAX_TEXT_SIZE;
                            break;
                    }
                    break;
            }
        }
        return nums;
    }
}
