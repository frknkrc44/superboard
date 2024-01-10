package org.blinksd;

import static android.os.Build.VERSION.SDK_INT;

import org.blinksd.utils.color.ThemeUtils;

public class Defaults {

    public static final int KEYBOARD_HEIGHT = 36;
    public static final int KEYBOARD_BACKGROUND_BLUR = 0;

    public static final int KEYBOARD_BACKGROUND_COLOR = 0xFF282D31;
    public static final int KEY_BACKGROUND_COLOR = 0xFF474B4C;
    public static final int KEY2_BACKGROUND_COLOR = 0xFF373C40;
    public static final int ENTER_BACKGROUND_COLOR = 0xFF5F97F6;
    public static final int KEY_PRESS_BACKGROUND_COLOR = 0xFF474B4C;
    public static final int KEY2_PRESS_BACKGROUND_COLOR = 0xFF373C40;
    public static final int ENTER_PRESS_BACKGROUND_COLOR = 0xFF5F97F6;
    public static final int KEY_BACKGROUND_TYPE = ThemeUtils.KEY_BG_TYPE_FLAT;
    public static final int KEY_BACKGROUND_ORIENTATION_TYPE = ThemeUtils.KEY_BG_ORIENTATION_TB;
    public static final int KEY_TEXT_COLOR = 0xFFDDE1E2;
    public static final int KEY_TEXT_SHADOW_COLOR = KEY_TEXT_COLOR;
    public static final int KEY_PADDING = 10;
    public static final int KEY_RADIUS = 10;

    public static final int KEY_TEXT_SIZE = 13;
    public static final int KEY_TEXT_SHADOW_SIZE = 0;
    public static final int KEY_LONGPRESS_DURATION = 1;
    public static final int KEY_VIBRATE_DURATION = 0;
    public static final int KEY_FONT_TYPE = 0;
    public static final int ICON_SIZE_MULTIPLIER = 3;

    public static final String KEYBOARD_LANGUAGE_KEY = "en_US";
    public static final String KEYBOARD_SPACETYPE = "theme";
    public static final String ICON_THEME = "theme_default";
    public static final String THEME_PRESET = "default";

    public static final boolean KEYBOARD_SHOW_POPUP = true;
    public static final boolean KEYBOARD_LC_ON_EMOJI = false;
    public static final boolean KEYBOARD_TOUCH_SOUND = true;
    public static final boolean COLORIZE_NAVBAR = true;
    public static final boolean DETECT_CAPSLOCK = true;
    public static final boolean COLORIZE_NAVBAR_ALT = false;
    public static final boolean DISABLE_POPUP = false;
    public static final boolean DISABLE_REPEAT = false;
    public static final boolean DISABLE_SUGGESTIONS = true;
    public static final boolean USE_MONET = false;
    public static final boolean ENABLE_POPUP_PREVIEW = false;
    public static final boolean KILL_BACKGROUND = false;
    public static final boolean DISABLE_TOP_BAR = false;
    public static final boolean HIDE_TOP_BAR_FN_BUTTONS = true;
    public static final boolean ENABLE_CLIPBOARD = SDK_INT >= 11;
    public static final boolean DISABLE_NUMBER_ROW = false;
    public static final boolean USE_FIRST_POPUP_CHARACTER = false;
}
