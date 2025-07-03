package org.blinksd.utils;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;
import static org.blinksd.board.SuperBoardApplication.getAppResources;
import static org.blinksd.board.SuperBoardApplication.getIconThemes;
import static org.blinksd.board.SuperBoardApplication.getSBApplication;
import static org.blinksd.board.SuperBoardApplication.getSpaceBarStyles;
import static org.blinksd.board.SuperBoardApplication.isWatchDevice;
import static org.blinksd.utils.SystemUtils.isDocumentsUiAvailable;
import static org.blinksd.utils.ThemeUtils.getKeyBgOrientationTypes;
import static org.blinksd.utils.ThemeUtils.getKeyBgTypes;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;

import org.blinksd.board.R;
import org.blinksd.board.activities.BackupRestoreActivity;
import org.blinksd.board.activities.DictionaryImportActivity;
import org.blinksd.board.activities.FontSelector;
import org.blinksd.board.activities.KeyboardLayoutSelector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingMap extends ListedMap<String, SettingItem> {

    public static final String SET_KEYBOARD_LANG_SELECT = "keyboard_lang_select",
            SET_KEYBOARD_TEXTTYPE_SELECT = "keyboard_texttype_select",
            SET_KEYBOARD_SPACETYPE_SELECT = "keyboard_spacetype_select",
            SET_KEYBOARD_BGIMG = "keyboard_bgimg",
            SET_KEYBOARD_BGBLUR = "keyboard_bgblur",
            SET_KEYBOARD_BGBLUR_USE_ALT = "keyboard_bgblur_use_alt",
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
            SET_KEY_INDICATOR_HEIGHT = "key_indicator_height",
            SET_KEY_TEXTSIZE = "key_textsize",
            SET_KEY_SHADOWSIZE = "key_shadowsize",
            SET_KEY_VIBRATE_DURATION = "key_vibrate_duration",
            SET_KEY_LONGPRESS_DURATION = "key_longpress_duration",
            SET_KEY_TEXTCLR = "key_textclr",
            SET_KEY2_TEXTCLR = "key2_textclr",
            SET_ENTER_TEXTCLR = "enter_textclr",
            SET_COLORIZE_NAVBAR = "colorize_navbar",
            SET_COLORIZE_NAVBAR_ALWAYS_TRANS = "colorize_navbar_always_trans",
            SET_COLORIZE_NAVBAR_ALT = "colorize_navbar_alt",
            SET_DETECT_CAPSLOCK = "detect_capslock",
            SET_ENFORCE_DETECT_CAPSLOCK = "enforce_detect_capslock",
            SET_ENFORCE_EDITOR_ACTION = "enforce_editor_action",
            SET_PREVENT_KBD_CLOSE = "prevent_keyboard_close",
            SET_DISABLE_POPUP = "disable_popup",
            SET_DISABLE_REPEAT = "disable_repeat",
            SET_DISABLE_SUGGESTIONS = "disable_suggestions",
            SET_USE_MONET = "use_monet",
            SET_USE_COMPAT_MONET = "use_compat_monet",
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
            SET_BACKUP_RESTORE = "backup_menu",
            SET_DICTIONARY_ALGORITHM = "dictionary_algorithm",
            SET_DICTIONARY_LIMIT = "dictionary_limit",
            SET_SHOW_BOTTOM_BAR = "show_bottom_bar",
            SET_ENABLE_LONG_PRESS_FAST_DELETE = "long_press_fast_delete",
            SET_INSERT_SPACE_AFTER_PUNC = "insert_space_after_punc",
            SET_MONET_COLOR_SCHEME = "monet_color_scheme",
            SET_KEYBOARD_PADDING = "keyboard_padding",
            SET_COMPAT_MONET_MAX_COLORS = "compat_monet_max_colors",
            SET_FORCE_SHOW_KEYBOARD_PHYSICAL = "force_show_keyboard_physical",
            SET_LANDSCAPE_HEIGHT_INCREASER = "land_height_increaser",
            SET_SHOW_FAB_RIGHT = "show_fab_right",
            SET_SHOW_FULLSCREEN_KEYBOARD = "show_fs_keyboard",
            SET_SHOW_FULLSCREEN_KEYBOARD_FORCED = "show_fs_keyboard_forced",
            SET_KEY_STROKE_WIDTH = "key_stroke_width",
            SET_KEY_STROKE_COLOR = "key_strokeclr";

    public SettingMap() {
        final var documentsUiAvailable = isDocumentsUiAvailable();
        if (documentsUiAvailable)
            putGeneral(SET_BACKUP_RESTORE, SettingType.REDIRECT);
        putKbdLayout(SET_KEYBOARD_LANG_SELECT,  SettingType.REDIRECT);
        if (documentsUiAvailable)
            putGeneral(SET_IMPORT_DICT_PACK,  SettingType.REDIRECT);
        putGeneral(SET_DICTIONARY_ALGORITHM, SettingType.SELECTOR);
        putGeneral(SET_DICTIONARY_LIMIT, SettingType.DECIMAL_NUMBER);
        putKbdLayout(SET_KEYBOARD_HEIGHT, SettingType.MM_DECIMAL_NUMBER);
        putKbdLayout(SET_LANDSCAPE_HEIGHT_INCREASER, SettingType.FLOAT_NUMBER);
        putKbdLayout(SET_KEYBOARD_PADDING, SettingType.MM_DECIMAL_NUMBER);
        putKbdLayout(SET_KEY_VIBRATE_DURATION, SettingType.DECIMAL_NUMBER);
        putKbdLayout(SET_KEY_LONGPRESS_DURATION, SettingType.MM_DECIMAL_NUMBER);
        putKbdLayout(SET_KEY_PADDING, SettingType.FLOAT_NUMBER);
        putKbdLayout(SET_KEY_STROKE_WIDTH, SettingType.FLOAT_NUMBER);
        putKbdLayout(SET_KEY_RADIUS, SettingType.FLOAT_NUMBER);
        putKbdLayout(SET_KEY_TEXTSIZE, SettingType.FLOAT_NUMBER);
        putKbdLayout(SET_KEY_SHADOWSIZE, SettingType.FLOAT_NUMBER);
        putKbdLayout(SET_KEY_INDICATOR_HEIGHT, SettingType.FLOAT_NUMBER);
        putKbdLayout(SET_KEY_ICON_SIZE_MULTIPLIER, SettingType.MM_DECIMAL_NUMBER);
        putTheming(SET_KEYBOARD_TEXTTYPE_SELECT,  SettingType.REDIRECT);
        putTheming(SET_KEYBOARD_SPACETYPE_SELECT, SettingType.STR_SELECTOR);
        putThemingAdvanced(SET_THEME_PRESET, SettingType.THEME_SELECTOR);
        putTheming(SET_ICON_THEME, SettingType.STR_SELECTOR);
        putTheming(SET_KEY_BG_TYPE, SettingType.SELECTOR);
        putTheming(SET_KEY_GRADIENT_ORIENTATION, SettingType.SELECTOR);
        putThemingAdvanced(SET_KEYBOARD_BGIMG, SettingType.IMAGE);
        putPopup(SET_KEYBOARD_SHOW_POPUP, SettingType.BOOL);
        putKbdLayout(SET_ENABLE_LONG_PRESS_FAST_DELETE, SettingType.BOOL);
        putKbdLayout(SET_INSERT_SPACE_AFTER_PUNC, SettingType.BOOL);
        putKbdLayout(SET_PLAY_SND_PRESS, SettingType.BOOL);
        putKbdLayout(SET_KEYBOARD_LC_ON_EMOJI, SettingType.BOOL);
        if (!SystemUtils.isNotColorizeNavbar() || SDK_INT >= VANILLA_ICE_CREAM) {
            putTheming(SET_COLORIZE_NAVBAR, SettingType.BOOL, SET_COLORIZE_NAVBAR_ALT, false);
            putTheming(SET_COLORIZE_NAVBAR_ALWAYS_TRANS, SettingType.BOOL, SET_COLORIZE_NAVBAR, true);
        }
        if (SDK_INT >= P)
            putTheming(SET_COLORIZE_NAVBAR_ALT, SettingType.BOOL, SET_COLORIZE_NAVBAR, false);
        putPopup(SET_DISABLE_POPUP, SettingType.BOOL);
        putPopup(SET_USE_FIRST_POPUP_CHARACTER, SettingType.BOOL, SET_DISABLE_POPUP, false);
        putKbdLayout(SET_DISABLE_REPEAT, SettingType.BOOL);
        putTopBar(SET_DISABLE_TOP_BAR, SettingType.BOOL, SET_DISABLE_NUMBER_ROW, false);
        putTopBar(SET_HIDE_TOP_BAR_FN_BUTTONS, SettingType.BOOL, SET_DISABLE_TOP_BAR, false);
        putTopBar(SET_SHOW_FAB_RIGHT, SettingType.BOOL, SET_DISABLE_TOP_BAR, false);
        putGeneral(SET_SHOW_FULLSCREEN_KEYBOARD, SettingType.BOOL, SET_SHOW_FULLSCREEN_KEYBOARD_FORCED, false);
        putGeneral(SET_SHOW_FULLSCREEN_KEYBOARD_FORCED, SettingType.BOOL, SET_SHOW_FULLSCREEN_KEYBOARD, false);
        putGeneral(SET_ENABLE_CLIPBOARD, SettingType.BOOL);
        putTopBar(SET_DISABLE_SUGGESTIONS, SettingType.BOOL, SET_DISABLE_TOP_BAR, false);
        putTopBar(SET_DISABLE_NUMBER_ROW, SettingType.BOOL, SET_DISABLE_TOP_BAR, false);
        putBottomBar(SET_SHOW_BOTTOM_BAR, SettingType.BOOL);
        if (SDK_INT >= S) {
            putTheming(SET_USE_MONET, SettingType.BOOL, SET_USE_COMPAT_MONET, false);
        }
        putTheming(SET_USE_COMPAT_MONET, SettingType.BOOL, SET_USE_MONET, false);
        putTheming(SET_COMPAT_MONET_MAX_COLORS, SettingType.MM_DECIMAL_NUMBER);
        putTheming(SET_MONET_COLOR_SCHEME, SettingType.THEME_SELECTOR);
        putKbdLayout(SET_ENABLE_POPUP_PREVIEW, SettingType.BOOL);
        putGeneral(SET_DETECT_CAPSLOCK, SettingType.BOOL);
        putGeneral(SET_ENFORCE_DETECT_CAPSLOCK, SettingType.BOOL, SET_DETECT_CAPSLOCK, true);
        putGeneral(SET_ENFORCE_EDITOR_ACTION, SettingType.BOOL);
        putGeneral(SET_PREVENT_KBD_CLOSE, SettingType.BOOL);
        putGeneral(SET_FORCE_SHOW_KEYBOARD_PHYSICAL, SettingType.BOOL);
        putGeneral(SET_KILL_BACKGROUND, SettingType.BOOL);
        putThemingAdvanced(SET_KEYBOARD_BGBLUR, SettingType.DECIMAL_NUMBER);
        putThemingAdvanced(SET_KEYBOARD_BGBLUR_USE_ALT, SettingType.BOOL);
        putThemingAdvanced(SET_KEYBOARD_BGCLR, SettingType.COLOR_SELECTOR);
        putThemingAdvanced(SET_KEY_BGCLR, SettingType.COLOR_SELECTOR);
        putThemingAdvanced(SET_KEY2_BGCLR, SettingType.COLOR_SELECTOR);
        putThemingAdvanced(SET_ENTER_BGCLR, SettingType.COLOR_SELECTOR);
        putThemingAdvanced(SET_KEY_PRESS_BGCLR, SettingType.COLOR_SELECTOR);
        putThemingAdvanced(SET_KEY2_PRESS_BGCLR, SettingType.COLOR_SELECTOR);
        putThemingAdvanced(SET_ENTER_PRESS_BGCLR, SettingType.COLOR_SELECTOR);
        putThemingAdvanced(SET_KEY_SHADOWCLR, SettingType.COLOR_SELECTOR);
        putThemingAdvanced(SET_KEY_TEXTCLR, SettingType.COLOR_SELECTOR);
        putThemingAdvanced(SET_KEY2_TEXTCLR, SettingType.COLOR_SELECTOR);
        putThemingAdvanced(SET_ENTER_TEXTCLR, SettingType.COLOR_SELECTOR);
        putThemingAdvanced(SET_KEY_STROKE_COLOR, SettingType.COLOR_SELECTOR);
    }

    private void putGeneral(String name, SettingType type) {
        putGeneral(name, type, null, null);
    }
    private void putGeneral(String name, SettingType type, String dependency, Boolean dependencyEnabled) {
        put(name, new SettingItem(SettingCategory.GENERAL, type, dependency, dependencyEnabled));
    }

    private void putKbdLayout(String name, SettingType type) {
        putKbdLayout(name, type, null, null);
    }
    private void putKbdLayout(String name, SettingType type, String dependency, Boolean dependencyEnabled) {
        put(name, new SettingItem(SettingCategory.KBD_LAYOUT, type, dependency, dependencyEnabled));
    }

    private void putPopup(String name, SettingType type) {
        putPopup(name, type, null, null);
    }
    private void putPopup(String name, SettingType type, String dependency, Boolean dependencyEnabled) {
        put(name, new SettingItem(SettingCategory.POPUP, type, dependency, dependencyEnabled));
    }

    private void putTopBar(String name, SettingType type, String dependency, Boolean dependencyEnabled) {
        put(name, new SettingItem(SettingCategory.TOP_BAR, type, dependency, dependencyEnabled));
    }

    private void putBottomBar(String name, SettingType type) {
        putBottomBar(name, type, null, null);
    }
    private void putBottomBar(String name, SettingType type, String dependency, Boolean dependencyEnabled) {
        put(name, new SettingItem(SettingCategory.BOTTOM_BAR, type, dependency, dependencyEnabled));
    }

    private void putTheming(String name, SettingType type) {
        putTheming(name, type, null, null);
    }

    private void putTheming(String name, SettingType type, String dependency, Boolean dependencyEnabled) {
        put(name, new SettingItem(SettingCategory.THEMING, type, dependency, dependencyEnabled));
    }

    private void putThemingAdvanced(String name, SettingType type) {
        put(name, new SettingItem(SettingCategory.THEMING_ADVANCED, type, null, null));
    }

    public void iterateChild(SettingCategory category, ChildIterator iterator) {
        for (String str : keyList()) {
            SettingItem item = get(str);
            // noinspection ConstantConditions
            if (item.category == category) {
                iterator.onIterate(str, item);
            }
        }
    }

    public Intent getRedirect(Context context, final String key) {
        return switch (key) {
            case SET_BACKUP_RESTORE ->
                    new Intent(context, BackupRestoreActivity.class).setData(Uri.EMPTY);
            case SET_IMPORT_DICT_PACK -> new Intent(context, DictionaryImportActivity.class);
            case SET_KEYBOARD_LANG_SELECT -> new Intent(context, KeyboardLayoutSelector.class);
            case SET_KEYBOARD_TEXTTYPE_SELECT -> new Intent(context, FontSelector.class);
            default -> null;
        };
    }

    public List<String> getSelector(final String key) {
        return switch (key) {
            case SET_DICTIONARY_ALGORITHM -> Arrays.asList(getAppResources()
                    .getStringArray(R.array.settings_dictionary_algorithms));
            case SET_KEY_BG_TYPE -> getKeyBgTypes();
            case SET_KEY_GRADIENT_ORIENTATION -> getKeyBgOrientationTypes();
            case SET_KEYBOARD_SPACETYPE_SELECT -> getSpaceBarStyles().keyList();
            case SET_ICON_THEME -> getIconThemes().keyList();
            default -> new ArrayList<>();
        };
    }

    public Object getDefaults(final String key) {
        switch (key) {
            case SET_KEYBOARD_BGBLUR:
                return Defaults.KEYBOARD_BACKGROUND_BLUR;
            case SET_KEYBOARD_BGBLUR_USE_ALT:
                return Defaults.KEYBOARD_BGBLUR_USE_ALT;
            case SET_KEYBOARD_PADDING:
                return Defaults.KEYBOARD_PADDING;
            case SET_KEY_VIBRATE_DURATION:
                return Defaults.KEY_VIBRATE_DURATION;
            case SET_KEYBOARD_HEIGHT:
                return Defaults.KEYBOARD_HEIGHT;
            case SET_LANDSCAPE_HEIGHT_INCREASER:
                return Defaults.LANDSCAPE_HEIGHT_INCREASER;
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
                TypedArray arr = getSBApplication().obtainStyledAttributes(0, new int[]{android.R.attr.colorAccent});
                int color = arr.getColor(0, Defaults.ENTER_BACKGROUND_COLOR);
                int pressColor = ColorUtils.getDarkerColor(color);
                arr.recycle();
                try {
                    if (SDK_INT >= S) {
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
            case SET_KEY2_TEXTCLR:
            case SET_ENTER_TEXTCLR:
                return Defaults.KEY_TEXT_COLOR;
            case SET_COLORIZE_NAVBAR:
                return Defaults.COLORIZE_NAVBAR;
            case SET_COLORIZE_NAVBAR_ALWAYS_TRANS:
                return Defaults.COLORIZE_NAVBAR_ALWAYS_TRANS;
            case SET_DETECT_CAPSLOCK:
                return Defaults.DETECT_CAPSLOCK;
            case SET_ENFORCE_DETECT_CAPSLOCK:
                return Defaults.ENFORCE_DETECT_CAPSLOCK;
            case SET_ENFORCE_EDITOR_ACTION:
                return Defaults.ENFORCE_EDITOR_ACTION;
            case SET_PREVENT_KBD_CLOSE:
                return Defaults.PREVENT_KBD_CLOSE;
            case SET_FORCE_SHOW_KEYBOARD_PHYSICAL:
                return Defaults.FORCE_SHOW_KEYBOARD_PHYSICAL;
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
            case SET_SHOW_FAB_RIGHT:
                return Defaults.SHOW_FAB_RIGHT;
            case SET_ENABLE_CLIPBOARD:
                return Defaults.ENABLE_CLIPBOARD;
            case SET_DISABLE_NUMBER_ROW:
                return Defaults.DISABLE_NUMBER_ROW;
            case SET_USE_FIRST_POPUP_CHARACTER:
                return Defaults.USE_FIRST_POPUP_CHARACTER;
            case SET_USE_MONET:
            case SET_USE_COMPAT_MONET:
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
            case SET_MONET_COLOR_SCHEME:
                return Defaults.THEME_PRESET;
            case SET_KEY_ICON_SIZE_MULTIPLIER:
                return Defaults.ICON_SIZE_MULTIPLIER;
            case SET_DICTIONARY_ALGORITHM:
                return Defaults.DICTIONARY_ALGORITHM;
            case SET_DICTIONARY_LIMIT:
                return Defaults.DICTIONARY_LIMIT;
            case SET_KEY_INDICATOR_HEIGHT:
                return Defaults.KEY_INDICATOR_HEIGHT;
            case SET_SHOW_BOTTOM_BAR:
                return Defaults.SHOW_BOTTOM_BAR;
            case SET_ENABLE_LONG_PRESS_FAST_DELETE:
                return Defaults.LONG_PRESS_FAST_DELETE;
            case SET_INSERT_SPACE_AFTER_PUNC:
                return Defaults.INSERT_SPACE_AFTER_PUNC;
            case SET_COMPAT_MONET_MAX_COLORS:
                return Defaults.COMPAT_MONET_MAX_COLORS;
            case SET_SHOW_FULLSCREEN_KEYBOARD:
                return Defaults.SHOW_FULLSCREEN_KEYBOARD;
            case SET_SHOW_FULLSCREEN_KEYBOARD_FORCED:
                return isWatchDevice();
            case SET_KEY_STROKE_WIDTH:
                return Defaults.KEY_STROKE_WIDTH;
            case SET_KEY_STROKE_COLOR:
                return Defaults.KEY_STROKE_COLOR;
            default:
                return null;
        }
    }

    public int[] getMinMaxNumbers(final String key) {
        int[] minMaxNumbers = new int[2];
        switch (key) {
            case SET_KEYBOARD_PADDING:
                minMaxNumbers[1] = Defaults.MinMaxValues.MAX_KEYBOARD_PADDING;
                break;
            case SET_KEYBOARD_BGBLUR:
            case SET_KEY_PADDING:
            case SET_KEY_SHADOWSIZE:
                minMaxNumbers[1] = Defaults.MinMaxValues.MAX_OTHER_VAL;
                break;
            case SET_KEY_VIBRATE_DURATION:
                minMaxNumbers[1] = Defaults.MinMaxValues.MAX_VIBRATION_DURATION;
                break;
            case SET_DICTIONARY_LIMIT:
                minMaxNumbers[0] = Defaults.MinMaxValues.MIN_DICT_LIMIT;
                minMaxNumbers[1] = Defaults.MinMaxValues.MAX_DICT_LIMIT;
                break;
            case SET_KEYBOARD_HEIGHT:
                minMaxNumbers[0] = Defaults.MinMaxValues.MIN_KEYBOARD_HEIGHT;
                minMaxNumbers[1] = Defaults.MinMaxValues.MAX_KEYBOARD_HEIGHT;
                break;
            case SET_KEY_LONGPRESS_DURATION:
                minMaxNumbers[0] = Defaults.MinMaxValues.MIN_LONG_PRESS_DURATION;
                minMaxNumbers[1] = Defaults.MinMaxValues.MAX_LONG_PRESS_DURATION;
                break;
            case SET_KEY_ICON_SIZE_MULTIPLIER:
                minMaxNumbers[0] = Defaults.MinMaxValues.MIN_ICON_MULTI;
                minMaxNumbers[1] = Defaults.MinMaxValues.MAX_ICON_MULTI;
                break;
            case SET_KEY_RADIUS:
                minMaxNumbers[1] = Defaults.MinMaxValues.MAX_RADIUS;
                break;
            case SET_KEY_TEXTSIZE:
                minMaxNumbers[0] = Defaults.MinMaxValues.MIN_TEXT_SIZE;
                minMaxNumbers[1] = Defaults.MinMaxValues.MAX_TEXT_SIZE;
                break;
            case SET_KEY_INDICATOR_HEIGHT:
                minMaxNumbers[1] = Defaults.MinMaxValues.MAX_INDICATOR_HEIGHT;
                break;
            case SET_COMPAT_MONET_MAX_COLORS:
                minMaxNumbers[0] = Defaults.MinMaxValues.MIN_COMPAT_MONET_COLOR;
                minMaxNumbers[1] = Defaults.MinMaxValues.MAX_COMPAT_MONET_COLOR;
                break;
            case SET_LANDSCAPE_HEIGHT_INCREASER:
                minMaxNumbers[0] = Defaults.MinMaxValues.MIN_LANDSCAPE_HEIGHT_INCREASER;
                minMaxNumbers[1] = Defaults.MinMaxValues.MAX_LANDSCAPE_HEIGHT_INCREASER;
                break;
            case SET_KEY_STROKE_WIDTH:
                minMaxNumbers[1] = Defaults.MinMaxValues.MAX_STROKE_WIDTH;
        }
        return minMaxNumbers;
    }

    public boolean getSwitchEnabledFromDependency(String settingName) {
        final var visitedDependencies = new ArrayList<>();

        SettingItem item = get(settingName);
        while (item != null && item.dependency != null && !visitedDependencies.contains(settingName)) {
            visitedDependencies.add(settingName);

            if (item.dependencyEnabled != SuperDBHelper.getBooleanOrDefault(item.dependency)) {
                return false;
            }

            item = get(item.dependency);
        }

        return true;
    }

    public interface ChildIterator {
        void onIterate(String key, SettingItem item);
    }
}
