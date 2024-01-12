package org.blinksd.board;

import android.app.Application;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;

import org.blinksd.board.dictionary.DictionaryDB;
import org.blinksd.utils.IconThemeUtils;
import org.blinksd.utils.LayoutUtils;
import org.blinksd.utils.SettingMap;
import org.blinksd.utils.SpaceBarThemeUtils;
import org.blinksd.utils.SuperDBHelper;
import org.blinksd.utils.TextUtilsCompat;
import org.blinksd.utils.ThemeUtils;
import org.blinksd.utils.ThemeUtils.ThemeHolder;
import org.blinksd.utils.superboard.Language;
import org.frknkrc44.minidb.SuperMiniDB;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class SuperBoardApplication extends Application {

    public static final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static HashMap<String, Language> languageCache = null;
    private static SuperMiniDB appDB = null;
    private static SuperBoardApplication appContext = null;
    private static SettingMap settingMap = null;
    private static Typeface customFont = null;
    private static File fontFile = null;
    private static String fontPath = null;
    private static IconThemeUtils icons;
    private static SpaceBarThemeUtils spaceBars;
    private static TextUtilsCompat emojiUtils;
    private static List<ThemeHolder> themes;
    private static DictionaryDB dictDB;

    public static DictionaryDB getDictDB() {
        return dictDB;
    }

    public static boolean isDictDBReady() {
        return dictDB != null && dictDB.isReady;
    }

    public static SuperBoardApplication getApplication() {
        return appContext;
    }

    public static SuperMiniDB getAppDB() {
        return appDB;
    }

    public static File getAppFilesDir() {
        return getApplication().getFilesDir();
    }

    public static HashMap<String, Language> getKeyboardLanguageList() {
        return languageCache;
    }

    public static List<ThemeHolder> getThemes() {
        return themes;
    }

    public static void reloadThemeCache() {
        try {
            themes = ThemeUtils.getThemes();
        } catch (Throwable t) {
            themes = new ArrayList<>();
        }
    }

    public static IconThemeUtils getIconThemes() {
        return icons;
    }

    public static SpaceBarThemeUtils getSpaceBarStyles() {
        return spaceBars;
    }

    public static TextUtilsCompat getTextUtils() {
        return emojiUtils;
    }

    public static Typeface getCustomFont() {
        if (customFont == null) {
            try {
                if (fontFile == null) fontFile = new File(fontPath);
                if (fontFile.exists()) customFont = Typeface.createFromFile(fontFile);
                else throw new Throwable();
            } catch (Throwable t) {
                // Log.e("SuperBoardApplication","Exception: ",t);
                fontFile = null;
                customFont = null;
                return Typeface.DEFAULT;
            }
        }
        return customFont;
    }

    public static void clearCustomFont() {
        File newFile = new File(fontPath);
        if (fontFile == null || fontFile.lastModified() != newFile.lastModified()) {
            customFont = null;
        }
    }

    public static Language getCurrentKeyboardLanguage() {
        return getCurrentKeyboardLanguage(false);
    }

    public static Language getCurrentKeyboardLanguage(boolean onlyUser) {
        String key = SettingMap.SET_KEYBOARD_LANG_SELECT;
        return getKeyboardLanguage(
                appDB.getString(key, (String) settingMap.getDefaults(key)), onlyUser);
    }

    public static Language getKeyboardLanguage(String name) {
        return getKeyboardLanguage(name, false);
    }

    public static Language getKeyboardLanguage(String name, boolean onlyUser) {
        Language ret = languageCache.containsKey(name)
                ? Objects.requireNonNull(languageCache.get(name))
                : LayoutUtils.emptyLanguage;

        if ((!ret.userLanguage) && onlyUser) {
            return LayoutUtils.emptyLanguage;
        }

        return ret;
    }

    public static void getNextLanguage() {
        ArrayList<String> ll = LayoutUtils.getKeyListFromLanguageList(languageCache);
        String key = SettingMap.SET_KEYBOARD_LANG_SELECT;
        String sel = appDB.getString(key, (String) settingMap.getDefaults(key));
        if (!sel.isEmpty()) {
            int index = -1;
            for (int i = 0; i < ll.size(); i++) {
                if (ll.get(i).equals(sel)) {
                    index = i;
                    break;
                }
            }
            if (index >= 0) {
                index = (index + 1) % ll.size();
                Language l = languageCache.get(ll.get(index));
                if (l == null) {
                    return;
                }
                appDB.putString(key, l.language);
                appDB.writeAll();
            }
        }
    }

    /**
     * Returns human readable language name list
     */
    public static List<String> getLanguageHRNames() {
        List<String> langKeys = new ArrayList<>(languageCache.keySet());
        List<String> out = new ArrayList<>();
        for (String key : langKeys) {
            Language lang = languageCache.get(key);
            if (lang != null) out.add(lang.label);
        }
        return out;
    }

    public static List<String> getLanguageTypes() {
        List<String> langKeys = new ArrayList<>(languageCache.keySet());
        List<String> out = new ArrayList<>();
        for (String key : langKeys) {
            Language lang = languageCache.get(key);
            if (lang != null) {
                String name = lang.language.split("_")[0].toLowerCase();
                if (!out.contains(name))
                    out.add(name);
            }
        }
        return out;
    }

    public static SettingMap getSettings() {
        return settingMap;
    }

    public static File getBackgroundImageFile() {
        return new File(getApplication().getFilesDir(), "bg");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
        settingMap = new SettingMap();
        appDB = SuperDBHelper.getDefault(this);

        fontPath = getApplication().getExternalCacheDir() + "/font.ttf";
        getCustomFont();
        reloadLanguageCache();

        dictDB = new DictionaryDB(this);
        icons = new IconThemeUtils();
        spaceBars = new SpaceBarThemeUtils();
        emojiUtils = new TextUtilsCompat();
        reloadThemeCache();
    }

    public static void reloadLanguageCache() {
        try {
            languageCache = LayoutUtils.getLanguageList(getApplication());
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (dictDB != null) dictDB.close();
    }
}
