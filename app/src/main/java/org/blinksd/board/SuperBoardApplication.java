package org.blinksd.board;

import static org.blinksd.utils.LayoutUtils.getKeyListFromLanguageList;
import static org.blinksd.utils.LayoutUtils.getLanguageList;
import static org.blinksd.utils.MiniLSPass.setHiddenApiExemptions;
import static org.blinksd.utils.ThemeUtils.getThemes;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import org.blinksd.utils.DictionaryDB;
import org.blinksd.utils.IconThemeUtils;
import org.blinksd.utils.LayoutUtils;
import org.blinksd.utils.MonetColors;
import org.blinksd.utils.SettingMap;
import org.blinksd.utils.SpaceBarThemeUtils;
import org.blinksd.utils.SuperDBHelper;
import org.blinksd.utils.TextUtilsCompat;
import org.blinksd.utils.ThemeUtils.ThemeHolder;
import org.blinksd.utils.superboard.Language;
import org.frknkrc44.minidb.SuperMiniDB;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@SuppressLint("StaticFieldLeak")
public final class SuperBoardApplication extends Application {
    public static final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static HashMap<String, Language> languageCache;
    private static SuperMiniDB appDB;
    private static SuperBoardApplication appContext;
    private static SettingMap settingMap;
    private static Typeface customFont;
    private static File fontFile;
    private static File bgFile;
    private static IconThemeUtils icons;
    private static SpaceBarThemeUtils spaceBars;
    private static TextUtilsCompat emojiUtils;
    private static List<ThemeHolder> themes;
    private static DictionaryDB dictDB;
    private static MonetColors monetColors;
    private static Boolean isWatchDevice = null;

    public synchronized static DictionaryDB getDictDB() {
        if (dictDB == null) {
            dictDB = new DictionaryDB(getSBApplication());
        }

        return dictDB;
    }

    public synchronized static boolean isDictDBReady() {
        return getDictDB().isReady;
    }

    public synchronized static SuperBoardApplication getSBApplication() {
        return appContext;
    }

    public synchronized static Resources getAppResources() {
        return getSBApplication().getResources();
    }

    public static boolean isWatchDevice() {
        if (isWatchDevice == null) {
            isWatchDevice = (getResConfiguration().uiMode & Configuration.UI_MODE_TYPE_MASK) == Configuration.UI_MODE_TYPE_WATCH;
        }

        return isWatchDevice;
    }

    public synchronized static Configuration getResConfiguration() {
        return getAppResources().getConfiguration();
    }

    public synchronized static SuperMiniDB getAppDB() {
        if (appDB == null) {
            appDB = SuperDBHelper.getDefault(getSBApplication());
        }

        return appDB;
    }

    public synchronized static File getAppFilesDir() {
        return getSBApplication().getFilesDir();
    }

    public synchronized static void clearLanguageCache() {
        languageCache = null;
    }

    public synchronized static HashMap<String, Language> getKeyboardLanguageList() {
        if (languageCache == null) {
            try {
                languageCache = getLanguageList(getSBApplication());
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        return languageCache;
    }

    public synchronized static void clearThemeCache() {
        themes = null;
    }

    public synchronized static List<ThemeHolder> getThemesCache() {
        if (themes == null) {
            try {
                themes = getThemes();
            } catch (Throwable t) {
                themes = new ArrayList<>();
            }
        }

        return themes;
    }

    public synchronized static MonetColors getMonetColors() {
        if (monetColors == null) {
            monetColors = new MonetColors();
        }

        return monetColors;
    }

    public synchronized static IconThemeUtils getIconThemes() {
        if (icons == null) {
            icons = new IconThemeUtils(getSBApplication());
        }

        return icons;
    }

    public synchronized static SpaceBarThemeUtils getSpaceBarStyles() {
        if (spaceBars == null) {
            spaceBars = new SpaceBarThemeUtils();
        }

        return spaceBars;
    }

    public synchronized static TextUtilsCompat getTextUtils() {
        if (emojiUtils == null) {
            emojiUtils = new TextUtilsCompat();
        }

        return emojiUtils;
    }

    public synchronized static Typeface getCustomFont() {
        if (fontFile == null) {
            fontFile = new File(getSBApplication().getExternalFilesDir(null),"font.ttf");
        }

        if (customFont == null) {
            try {
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

    public synchronized static void clearCustomFont() {
        customFont = null;
    }

    public synchronized static Language getCurrentKeyboardLanguage() {
        return getCurrentKeyboardLanguage(false);
    }

    public synchronized static Language getCurrentKeyboardLanguage(boolean onlyUser) {
        return getKeyboardLanguage(
                SuperDBHelper.getStringOrDefault(SettingMap.SET_KEYBOARD_LANG_SELECT), onlyUser);
    }

    public synchronized static Language getKeyboardLanguage(String name) {
        return getKeyboardLanguage(name, false);
    }

    public synchronized static Language getKeyboardLanguage(String name, boolean onlyUser) {
        Language ret = getKeyboardLanguageList().containsKey(name)
                ? Objects.requireNonNull(getKeyboardLanguageList().get(name))
                : LayoutUtils.emptyLanguage;

        if ((!ret.userLanguage) && onlyUser) {
            return LayoutUtils.emptyLanguage;
        }

        return ret;
    }

    public synchronized static void getNextLanguage() {
        ArrayList<String> ll = getKeyListFromLanguageList(getKeyboardLanguageList());
        String key = SettingMap.SET_KEYBOARD_LANG_SELECT;
        String sel = getAppDB().getString(key, (String) getSettings().getDefaults(key));
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
                Language l = getKeyboardLanguageList().get(ll.get(index));
                if (l == null) {
                    return;
                }
                getAppDB().putString(key, l.language);
                getAppDB().writeAll();
            }
        }
    }

    public synchronized static List<String> getLanguageTypes() {
        List<String> langKeys = new ArrayList<>(getKeyboardLanguageList().keySet());
        List<String> out = new ArrayList<>();
        for (String key : langKeys) {
            Language lang = getKeyboardLanguageList().get(key);
            if (lang != null) {
                String name = lang.language.split("_")[0].toLowerCase();
                if (!out.contains(name))
                    out.add(name);
            }
        }
        return out;
    }

    public synchronized static SettingMap getSettings() {
        if (settingMap == null) {
            settingMap = new SettingMap();
        }

        return settingMap;
    }

    public synchronized static File getBackgroundImageFile() {
        if (bgFile == null) {
            bgFile = new File(getSBApplication().getFilesDir(), "bg");
        }

        return bgFile;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            setHiddenApiExemptions(true);
        }

        getCustomFont();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (dictDB != null) dictDB.close();
    }
}
