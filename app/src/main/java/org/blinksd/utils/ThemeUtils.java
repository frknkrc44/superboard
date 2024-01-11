package org.blinksd.utils;

import android.content.res.AssetManager;
import android.graphics.Color;

import org.blinksd.board.SuperBoardApplication;
import org.frknkrc44.minidb.SuperMiniDB;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

@SuppressWarnings("unused")
public class ThemeUtils {
    public static final int KEY_BG_TYPE_FLAT     = 0,
                            KEY_BG_TYPE_GRADIENT = 1;

    public static final int KEY_BG_ORIENTATION_TB   = 0, // top center - bottom center
                            KEY_BG_ORIENTATION_BT   = 1, // bottom center - top center
                            KEY_BG_ORIENTATION_LR   = 2, // left center - right center
                            KEY_BG_ORIENTATION_RL   = 3, // right center - left center
                            KEY_BG_ORIENTATION_TL_BR = 4, // top left - bottom right
                            KEY_BG_ORIENTATION_TR_BL = 5, // top right - bottom left
                            KEY_BG_ORIENTATION_BL_TR = 6, // bottom left - top right
                            KEY_BG_ORIENTATION_BR_TL = 7; // bottom right - top left

    private ThemeUtils() {}

    public static List<String> getKeyBgTypes() {
        List<String> out = new ArrayList<>();
        out.add(Integer.toString(KEY_BG_TYPE_FLAT));
        out.add(Integer.toString(KEY_BG_TYPE_GRADIENT));
        return out;
    }

    public static List<String> getKeyBgOrientationTypes() {
        List<String> out = new ArrayList<>();
        out.add(Integer.toString(KEY_BG_ORIENTATION_TB));
        out.add(Integer.toString(KEY_BG_ORIENTATION_BT));
        out.add(Integer.toString(KEY_BG_ORIENTATION_LR));
        out.add(Integer.toString(KEY_BG_ORIENTATION_RL));
        out.add(Integer.toString(KEY_BG_ORIENTATION_TL_BR));
        out.add(Integer.toString(KEY_BG_ORIENTATION_TR_BL));
        out.add(Integer.toString(KEY_BG_ORIENTATION_BL_TR));
        out.add(Integer.toString(KEY_BG_ORIENTATION_BR_TL));
        return out;
    }

    public static ThemeHolder getUserThemeFromCodeName(List<ThemeHolder> themes, String name) {
        for (ThemeHolder holder : themes) {
            if (holder.codeName.equals(name) && holder.isUserTheme) {
                return holder;
            }
        }
        return null;
    }

    public static ThemeHolder getThemeFromCodeName(List<ThemeHolder> themes, String name) {
        for (ThemeHolder holder : themes) {
            if (holder.codeName.equals(name)) {
                return holder;
            }
        }
        return null;
    }

    public static int getThemeIndexFromCodeName(List<ThemeHolder> themes, String name) {
        for (int i = 0; i < themes.size(); i++) {
            if (themes.get(i).codeName.equals(name)) {
                return i;
            }
        }
        return -1;
    }

    public static List<String> getThemeNames() throws IOException, JSONException {
        return getThemeNames(getThemes());
    }

    public static List<String> getThemeNames(List<ThemeHolder> themes) {
        List<String> themeNames = new ArrayList<>();
        for (ThemeHolder holder : themes)
            themeNames.add(holder.name);
        return themeNames;
    }

    /** @noinspection ResultOfMethodCallIgnored*/
    public static File getUserThemesDir() {
        File themesDir = new File(SuperBoardApplication.getApplication().getFilesDir() + "/themes");

        if (!themesDir.exists()) {
            themesDir.mkdirs();
        }

        return themesDir;
    }

    public static List<ThemeHolder> getThemes() throws IOException, JSONException {
        List<ThemeHolder> holders = new ArrayList<>();

        // Import default themes
        AssetManager assets = SuperBoardApplication.getApplication().getAssets();
        String subdir = "themes";
        String[] items = assets.list(subdir);
        assert items != null;
        Arrays.sort(items);
        for (String str : items) {
            if (str.endsWith(".json")) {
                Scanner sc = new Scanner(assets.open(subdir + "/" + str));
                StringBuilder s = new StringBuilder();
                while (sc.hasNext()) s.append(sc.nextLine());
                sc.close();
                holders.add(new ThemeHolder(s.toString(), false));
            }
        }

        // Import themes by using keyboard theme api
        File themesDir = getUserThemesDir();

        for (String file : Objects.requireNonNull(themesDir.list())) {
            Scanner sc = new Scanner(new File(themesDir + "/" + file));
            StringBuilder s = new StringBuilder();
            while (sc.hasNext()) s.append(sc.nextLine());
            sc.close();
            holders.add(new ThemeHolder(s.toString(), true));
        }

        return holders;
    }

    public static class ThemeHolder {
        public final String name, codeName, fontType, iconTheme,
                backgroundColor, primaryColor, secondaryColor,
                enterColor, textShadowColor, textColor,
                primaryPressColor, secondaryPressColor, enterPressColor;
        public final int keyPadding, keyRadius, textSize, textShadow,
                keyBgType, key2BgType, enterBgType, keyBgGradientOrientation;
        public final boolean isUserTheme;

        public ThemeHolder() throws JSONException {
            this(true);
        }

        public ThemeHolder(String str) throws JSONException {
            this(new JSONObject(str), true);
        }

        public ThemeHolder(JSONObject json) {
            this(json, true);
        }

        private ThemeHolder(boolean userTheme) throws JSONException {
            this("{}", userTheme);
        }

        private ThemeHolder(String str, boolean userTheme) throws JSONException {
            this(new JSONObject(str), userTheme);
        }

        /**
         * Initializes a theme from JSONObject
         * <p>
         * JSON parameters: Check <b>THEME_API.md</b> for details.
         */
        private ThemeHolder(JSONObject json, boolean userTheme) {
            this.name = getString(json, "name", userTheme ? " (U)" : "");
            this.codeName = getString(json, "code");
            this.fontType = getString(json, "fnTyp");
            this.iconTheme = getString(json, "icnThm");
            this.backgroundColor = getString(json, "bgClr");
            this.primaryColor = getString(json, "priClr");
            this.primaryPressColor = getString(json, "priPressClr");
            this.secondaryColor = getString(json, "secClr");
            this.secondaryPressColor = getString(json, "secPressClr");
            this.enterColor = getString(json, "enterClr");
            this.enterPressColor = getString(json, "enterPressClr");
            this.textShadowColor = getString(json, "tShdwClr");
            this.textColor = getString(json, "txtClr");
            this.keyPadding = getInt(json, "keyPad");
            this.keyRadius = getInt(json, "keyRad");
            this.textSize = getInt(json, "txtSize");
            this.textShadow = getInt(json, "txtShadow");

            this.keyBgType = getInt(json, "kBgType", KEY_BG_TYPE_FLAT, false);
            this.key2BgType = getInt(json, "k2BgType", KEY_BG_TYPE_FLAT, false);
            this.enterBgType = getInt(json, "eBgType", KEY_BG_TYPE_FLAT, false);
            this.keyBgGradientOrientation =
                    getInt(json, "kBGOri", KEY_BG_ORIENTATION_TB, false);
            this.isUserTheme = userTheme;
        }

        private String getString(JSONObject json, String key) {
            return getString(json, key, "");
        }

        private String getString(JSONObject json, String key, String userAttr) {
            try {
                return json.has(key) ? (json.getString(key) + userAttr) : "";
            } catch (Throwable t) {
                return "";
            }
        }

        private int getInt(JSONObject json, String key) {
            return getInt(json, key, -1, true);
        }

        private int getInt(JSONObject json, String key, int def, boolean multiply) {
            String str = getString(json, key);
            if (str.isEmpty()) {
                return def;
            }

            try {
                if (multiply) {
                    double convert = Double.parseDouble(str);
                    return (int) (10 * convert);
                }

                return Integer.parseInt(str);
            } catch (Throwable t) {
                return def;
            }
        }

        private void putControlledColor(String key, String color) {
            SuperMiniDB smdb = SuperBoardApplication.getAppDB();
            SettingMap sMap = SuperBoardApplication.getSettings();
            if (color.trim().isEmpty()) {
                smdb.putString(key, String.valueOf(sMap.getDefaults(key)), true);
                return;
            }
            smdb.putInteger(key, Color.parseColor(color), true);
        }

        private void putControlledString(String key, String value) {
            SuperMiniDB smdb = SuperBoardApplication.getAppDB();
            SettingMap sMap = SuperBoardApplication.getSettings();
            if (value.trim().isEmpty()) {
                smdb.putString(key, String.valueOf(sMap.getDefaults(key)), true);
                return;
            }
            smdb.putString(key, value, true);
        }

        private void putControlledInt(String key, int value) {
            SuperMiniDB smdb = SuperBoardApplication.getAppDB();
            SettingMap sMap = SuperBoardApplication.getSettings();
            if (value < 0) {
                smdb.putString(key, String.valueOf(sMap.getDefaults(key)), true);
                return;
            }
            smdb.putInteger(key, value, true);
        }

        public void applyTheme() {
            try {
                SettingMap sMap = SuperBoardApplication.getSettings();
                List<String> textTypes = sMap.getSelector(SettingMap.SET_KEYBOARD_TEXTTYPE_SELECT);
                putControlledInt(SettingMap.SET_KEYBOARD_TEXTTYPE_SELECT, textTypes.indexOf(fontType));
            } catch (Throwable t) {
                putControlledInt(SettingMap.SET_KEYBOARD_TEXTTYPE_SELECT, -1);
            }

            try {
                putControlledString(SettingMap.SET_ICON_THEME, iconTheme);
            } catch (Throwable t) {
                putControlledInt(SettingMap.SET_ICON_THEME, -1);
            }
            putControlledColor(SettingMap.SET_KEYBOARD_BGCLR, backgroundColor);
            putControlledColor(SettingMap.SET_KEY_BGCLR, primaryColor);
            putControlledColor(SettingMap.SET_KEY_PRESS_BGCLR, primaryPressColor);
            putControlledColor(SettingMap.SET_KEY2_BGCLR, secondaryColor);
            putControlledColor(SettingMap.SET_KEY2_PRESS_BGCLR, secondaryPressColor);
            putControlledColor(SettingMap.SET_ENTER_BGCLR, enterColor);
            putControlledColor(SettingMap.SET_ENTER_PRESS_BGCLR, enterPressColor);
            putControlledColor(SettingMap.SET_KEY_SHADOWCLR, textShadowColor);
            putControlledColor(SettingMap.SET_KEY_TEXTCLR, textColor);
            putControlledInt(SettingMap.SET_KEY_PADDING, keyPadding);
            putControlledInt(SettingMap.SET_KEY_RADIUS, keyRadius);
            putControlledInt(SettingMap.SET_KEY_TEXTSIZE, textSize);
            putControlledInt(SettingMap.SET_KEY_SHADOWSIZE, textShadow);
        }
    }
}