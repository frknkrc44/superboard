package org.blinksd.utils;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.P;

public final class Defaults {
    static final int KEYBOARD_HEIGHT = 36;
    static final int KEYBOARD_BACKGROUND_BLUR = 0;
    static final int KEYBOARD_PADDING = 8;
    static final int COMPAT_MONET_MAX_COLORS = 12;

    static final int KEYBOARD_BACKGROUND_COLOR = 0xFF282D31;
    static final int KEY_BACKGROUND_COLOR = 0xFF474B4C;
    static final int KEY2_BACKGROUND_COLOR = 0xFF373C40;
    static final int ENTER_BACKGROUND_COLOR = 0xFF5F97F6;
    static final int KEY_PRESS_BACKGROUND_COLOR = 0xFF474B4C;
    static final int KEY2_PRESS_BACKGROUND_COLOR = 0xFF373C40;
    static final int ENTER_PRESS_BACKGROUND_COLOR = 0xFF5F97F6;
    static final int KEY_BACKGROUND_TYPE = ThemeUtils.KEY_BG_TYPE_FLAT;
    static final int KEY_BACKGROUND_ORIENTATION_TYPE = ThemeUtils.KEY_BG_ORIENTATION_TB;
    static final int KEY_TEXT_COLOR = 0xFFDDE1E2;
    static final int KEY_TEXT_SHADOW_COLOR = KEY_TEXT_COLOR;
    static final int KEY_STROKE_COLOR = 0;
    static final int KEY_PADDING = 10;
    static final int KEY_RADIUS = 24;

    static final int KEY_TEXT_SIZE = 23;
    static final int KEY_TEXT_SHADOW_SIZE = 0;
    static final int KEY_LONGPRESS_DURATION = 1;
    static final int KEY_VIBRATE_DURATION = 0;
    static final int KEY_FONT_TYPE = 0;
    static final int ICON_SIZE_MULTIPLIER = 2;
    static final int DICTIONARY_ALGORITHM = 0;
    static final int DICTIONARY_LIMIT = 10;
    static final int KEY_INDICATOR_HEIGHT = 5;
    static final int LANDSCAPE_HEIGHT_INCREASER = 15;
    static final int KEY_STROKE_WIDTH = 0;
    static final int KEYBOARD_ROUND = 0;

    public static final String KEYBOARD_LANGUAGE_KEY = "en_US";
    public static final String KEYBOARD_SPACETYPE = "theme";
    public static final String ICON_THEME = "theme_default";
    public static final String THEME_PRESET = MonetColors.COLOR_SCHEME_DEFAULT;

    static final boolean KEYBOARD_BGBLUR_USE_ALT = false;
    static final boolean KEYBOARD_SHOW_POPUP = true;
    static final boolean KEYBOARD_LC_ON_EMOJI = false;
    static final boolean KEYBOARD_TOUCH_SOUND = true;
    static final boolean COLORIZE_NAVBAR = SDK_INT < P ;
    static final boolean COLORIZE_NAVBAR_ALT = SDK_INT >= P;
    static final boolean COLORIZE_NAVBAR_ALWAYS_TRANS = COLORIZE_NAVBAR;
    static final boolean DETECT_CAPSLOCK = true;
    static final boolean ENFORCE_DETECT_CAPSLOCK = true;
    static final boolean ENFORCE_EDITOR_ACTION = false;
    static final boolean PREVENT_KBD_CLOSE = false;
    static final boolean FORCE_SHOW_KEYBOARD_PHYSICAL = false;
    static final boolean DISABLE_POPUP = false;
    static final boolean DISABLE_REPEAT = false;
    static final boolean DISABLE_SUGGESTIONS = true;
    static final boolean USE_MONET = false;
    static final boolean ENABLE_POPUP_PREVIEW = false;
    static final boolean KILL_BACKGROUND = false;
    static final boolean DISABLE_TOP_BAR = false;
    static final boolean HIDE_TOP_BAR_FN_BUTTONS = true;
    static final boolean SHOW_FAB_RIGHT = false;
    static final boolean ENABLE_CLIPBOARD = true;
    static final boolean DISABLE_NUMBER_ROW = false;
    static final boolean USE_FIRST_POPUP_CHARACTER = false;
    static final boolean SHOW_BOTTOM_BAR = false;
    static final boolean LONG_PRESS_FAST_DELETE = false;
    static final boolean INSERT_SPACE_AFTER_PUNC = false;
    static final boolean SHOW_FULLSCREEN_KEYBOARD = false;
    static final boolean EMOJI_USE_VERTICAL_SCROLL_PORTRAIT = true;
    static final boolean EMOJI_USE_VERTICAL_SCROLL_LANDSCAPE = false;
    static final boolean SHOW_MATH_LAYOUT = false;
    static final boolean REPLACE_PHYSICAL_KEYS = false;

    static final class MinMaxValues {
        static final int MIN_LONG_PRESS_DURATION = 1;
        static final int MAX_LONG_PRESS_DURATION = 3;
        static final int MAX_VIBRATION_DURATION = 100;
        static final int MAX_RADIUS = 100;
        static final int MIN_TEXT_SIZE = 6;
        static final int MAX_TEXT_SIZE = 60;
        static final int MIN_KEYBOARD_HEIGHT = 10;
        static final int MAX_KEYBOARD_HEIGHT = 80;
        static final int MAX_OTHER_VAL = 40;
        static final int MAX_KEYBOARD_PADDING = 120;
        static final int MIN_ICON_MULTI = 1;
        static final int MAX_ICON_MULTI = 10;
        static final int MIN_DICT_LIMIT = 3;
        static final int MAX_DICT_LIMIT = 50;
        static final int MAX_INDICATOR_HEIGHT = 20;
        static final int MIN_COMPAT_MONET_COLOR = 6;
        static final int MAX_COMPAT_MONET_COLOR = 128;
        static final int MIN_LANDSCAPE_HEIGHT_INCREASER = 10;
        static final int MAX_LANDSCAPE_HEIGHT_INCREASER = 30;
        static final int MAX_STROKE_WIDTH = 20;
        static final int MAX_KEYBOARD_ROUND = 80;
    }
}
