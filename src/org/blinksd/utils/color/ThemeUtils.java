package org.blinksd.utils.color;

import android.content.res.*;
import android.graphics.*;

import java.io.*;
import java.util.*;

import org.blinksd.SuperBoardApplication;
import org.blinksd.board.*;
import org.blinksd.sdb.*;
import org.json.*;
import org.superdroid.db.*;

public class ThemeUtils {
    private ThemeUtils() {}

    public static ThemeHolder getUserThemeFromCodeName(List<ThemeHolder> themes, String name){
        for(ThemeHolder holder : themes){
            if(holder.codeName.equals(name) && holder.isUserTheme){
                return holder;
            }
        }
        return null;
    }

    public static ThemeHolder getThemeFromCodeName(List<ThemeHolder> themes, String name){
        for(ThemeHolder holder : themes){
            if(holder.codeName.equals(name)){
                return holder;
            }
        }
        return null;
    }

    public static int getThemeIndexFromCodeName(List<ThemeHolder> themes, String name){
        for(int i = 0;i < themes.size();i++){
            if(themes.get(i).codeName.equals(name)){
                return i;
            }
        }
        return -1;
    }

    public static List<String> getThemeNames() throws IOException, JSONException {
        return getThemeNames(getThemes());
    }

    public static List<String> getThemeNames(List<ThemeHolder> themes){
        List<String> themeNames = new ArrayList<>();
        for(ThemeHolder holder : themes)
            themeNames.add(holder.name);
        return themeNames;
    }

    public static File getUserThemesDir() {
        File themesDir = new File(SuperBoardApplication.getApplication().getFilesDir() + "/themes");

        if(!themesDir.exists()){
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
		Arrays.sort(items);
		for(String str : items){
			if(str.endsWith(".json")){
				Scanner sc = new Scanner(assets.open(subdir + "/" + str));
				String s = "";
				while(sc.hasNext()) s += sc.nextLine();
				sc.close();
                holders.add(new ThemeHolder(s, false));
            }
        }

        // Import themes by using keyboard theme api
        File themesDir = getUserThemesDir();

        for(String file : themesDir.list()) {
            Scanner sc = new Scanner(new File(themesDir + "/" + file));
			String s = "";
			while(sc.hasNext()) s += sc.nextLine();
			sc.close();
            holders.add(new ThemeHolder(s, true));
        }

        return holders;
    }

    public static class ThemeHolder {
        public final String name, codeName, fontType, iconTheme, backgroundColor, primaryColor, secondaryColor, enterColor, textShadowColor, textColor;
        public final int keyPadding, keyRadius, textSize, textShadow;
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

        private ThemeHolder(JSONObject json, boolean userTheme) {
            this.name = getString(json, "name", userTheme ? " (USER)" : "");
            this.codeName = getString(json, "code");
            this.fontType = getString(json, "fnTyp");
            this.iconTheme = getString(json, "icnThm");
            this.backgroundColor = getString(json, "bgClr");
            this.primaryColor = getString(json, "priClr");
            this.secondaryColor = getString(json, "secClr");
            this.enterColor = getString(json, "enterClr");
            this.textShadowColor = getString(json, "tShdwClr");
            this.textColor = getString(json, "txtClr");
            this.keyPadding = getInt(json, "keyPad");
            this.keyRadius = getInt(json, "keyRad");
            this.textSize = getInt(json, "txtSize");
            this.textShadow = getInt(json, "txtShadow");
            this.isUserTheme = userTheme;
        }

        private String getString(JSONObject json, String key) {
            return getString(json, key, "");
        }

        private String getString(JSONObject json, String key, String userAttr) {
            try {
                return json.has(key) ? (json.getString(key) + userAttr) : "";
            } catch(Throwable t) {
                return "";
            }
        }

        private int getInt(JSONObject json, String key) {
            String str = getString(json, key);
            if(str.length() < 1) {
                return -1;
            }

            try {
                double convert = Double.parseDouble(str);
                int convertInt = (int)(10 * convert);
                return convertInt;
            } catch(Throwable t) {
                return -1;
            }
        }

        private void putControlledColor(String key, String color) {
            SuperMiniDB smdb = SuperBoardApplication.getApplicationDatabase();
            SettingMap sMap = SuperBoardApplication.getSettings();
            if(color.trim().length() < 1) {
                smdb.putString(key, String.valueOf(sMap.getDefaults(key)), true);
                return;
            }
            smdb.putInteger(key, Color.parseColor(color), true);
        }

        private void putControlledString(String key, String value) {
            SuperMiniDB smdb = SuperBoardApplication.getApplicationDatabase();
            SettingMap sMap = SuperBoardApplication.getSettings();
            if(value.trim().length() < 1) {
                smdb.putString(key, String.valueOf(sMap.getDefaults(key)), true);
                return;
            }
            smdb.putString(key, value, true);
        }

        private void putControlledInt(String key, int value) {
            SuperMiniDB smdb = SuperBoardApplication.getApplicationDatabase();
            SettingMap sMap = SuperBoardApplication.getSettings();
            if(value < 0) {
                smdb.putString(key, String.valueOf(sMap.getDefaults(key)), true);
                return;
            }
            smdb.putInteger(key, value, true);
        }

        public void applyTheme() {
            try {
                SettingMap sMap = SuperBoardApplication.getSettings();
                List textTypes = sMap.getSelector(SettingMap.SET_KEYBOARD_TEXTTYPE_SELECT);
                putControlledInt(SettingMap.SET_KEYBOARD_TEXTTYPE_SELECT, textTypes.indexOf(fontType));
            } catch(Throwable t) {
                putControlledInt(SettingMap.SET_KEYBOARD_TEXTTYPE_SELECT, -1);
            }

            try {
                 putControlledString(SettingMap.SET_ICON_THEME, iconTheme);
            } catch(Throwable t) {
                putControlledInt(SettingMap.SET_ICON_THEME, -1);
            }
            putControlledColor(SettingMap.SET_KEYBOARD_BGCLR, backgroundColor);
            putControlledColor(SettingMap.SET_KEY_BGCLR, primaryColor);
            putControlledColor(SettingMap.SET_KEY2_BGCLR, secondaryColor);
            putControlledColor(SettingMap.SET_ENTER_BGCLR, enterColor);
            putControlledColor(SettingMap.SET_KEY_SHADOWCLR, textShadowColor);
            putControlledColor(SettingMap.SET_KEY_TEXTCLR, textColor);
            putControlledInt(SettingMap.SET_KEY_PADDING, keyPadding);
            putControlledInt(SettingMap.SET_KEY_RADIUS, keyRadius);
            putControlledInt(SettingMap.SET_KEY_TEXTSIZE, textSize);
            putControlledInt(SettingMap.SET_KEY_SHADOWSIZE, textShadow);
        }
    }
}