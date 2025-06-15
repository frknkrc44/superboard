package org.blinksd.board.services;

import static org.blinksd.board.SuperBoardApplication.clearLanguageCache;
import static org.blinksd.board.SuperBoardApplication.clearThemeCache;
import static org.blinksd.board.SuperBoardApplication.getAppDB;
import static org.blinksd.board.SuperBoardApplication.getBackgroundImageFile;
import static org.blinksd.board.SuperBoardApplication.getIconThemes;
import static org.blinksd.board.SuperBoardApplication.getKeyboardLanguage;
import static org.blinksd.board.SuperBoardApplication.getThemesCache;
import static org.blinksd.utils.LayoutUtils.createLanguage;
import static org.blinksd.utils.LayoutUtils.getUserLanguageFilesDir;
import static org.blinksd.utils.ThemeUtils.getUserThemeFromCodeName;
import static org.blinksd.utils.ThemeUtils.getUserThemesDir;

import android.graphics.Bitmap;
import android.os.Build;

import org.blinksd.board.services.parcelables.IconThemeParcel;
import org.blinksd.utils.LocalIconTheme;
import org.blinksd.utils.SettingMap;
import org.blinksd.utils.SuperDBHelper;
import org.blinksd.utils.ThemeUtils;
import org.blinksd.utils.superboard.Language;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

@SuppressWarnings("unused")
public final class KeyboardThemeApi extends IKeyboardThemeApi.Stub {
    public static final int THEME_IMPORT_SUCCESS = 0,
            THEME_IMPORT_FAILED_MISSING_KEYS = 1,
            THEME_IMPORT_FAILED_EXISTS = 2,
            THEME_IMPORT_FAILED_INVALID_JSON = 3,
            THEME_IMPORT_FAILED_UNKNOWN = -1;

    public static final int IMAGE_IMPORT_SUCCESS = 0,
            IMAGE_IMPORT_FAILED = 1;

    public static final int ICON_THEME_IMPORT_SUCCESS = 0,
            ICON_THEME_IMPORT_FAILED_EXISTS = 1,
            ICON_THEME_IMPORT_FAILED_UNKNOWN = -1;

    public static final int LANG_PKG_IMPORT_SUCCESS = 0,
            LANG_PKG_IMPORT_FAILED_EXISTS = 1,
            LANG_PKG_IMPORT_FAILED_SDK = 2,
            LANG_PKG_IMPORT_FAILED_NOT_ENABLED = 3,
            LANG_PKG_IMPORT_FAILED_UNKNOWN = -1;

    @Override
    public int importTheme(String jsonStr) {
        try {
            JSONObject obj = new JSONObject(jsonStr);
            themeCheckMandatoryKeys(obj);
            if (!isThemeImported(obj.getString("code"))) {
                importThemeInternal(obj);
                clearThemeCache();
                return THEME_IMPORT_SUCCESS;
            }
            return THEME_IMPORT_FAILED_EXISTS;
        } catch (JSONException e) {
            return THEME_IMPORT_FAILED_INVALID_JSON;
        } catch (MissingKeysException x) {
            return THEME_IMPORT_FAILED_MISSING_KEYS;
        } catch (RuntimeException y) {
            return THEME_IMPORT_FAILED_UNKNOWN;
        }
    }

    private void importThemeInternal(JSONObject obj) {
        String themeStr = obj.toString();
        try {
            File file = new File(getUserThemesDir(), "user_" + obj.getString("code") + Integer.toHexString(themeStr.hashCode()) + ".json");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(themeStr.getBytes());
            fos.flush();
            fos.close();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private void themeCheckMandatoryKeys(JSONObject obj) {
        if (!obj.has("name") && obj.has("code")) {
            throw new MissingKeysException();
        }
    }

    @Override
    public int importThemeForced(String jsonStr) {
        try {
            JSONObject obj = new JSONObject(jsonStr);
            themeCheckMandatoryKeys(obj);
            importThemeInternal(obj);
            clearThemeCache();
            return THEME_IMPORT_SUCCESS;
        } catch (JSONException e) {
            // do nothing
        } catch (MissingKeysException x) {
            return THEME_IMPORT_FAILED_MISSING_KEYS;
        }
        return THEME_IMPORT_FAILED_UNKNOWN;
    }

    @Override
    public boolean isThemeImported(String name) {
        List<ThemeUtils.ThemeHolder> themes = getThemesCache();
        ThemeUtils.ThemeHolder theme = getUserThemeFromCodeName(themes, name);
        // System.out.println("NAME: " + name + " OBJ: " + theme);
        return theme != null;
    }

    @Override
    public int importBgImage(Bitmap bmp) {
        FileOutputStream outputStream = null;
        try {
            File file = getBackgroundImageFile();
            outputStream = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            SuperDBHelper.setColorsFromBitmap(bmp);

            // disable monet because we're imported a background image
            // and pulled colors from it
            getAppDB().putBoolean(SettingMap.SET_USE_MONET, false, true);

            restartKeyboard();
            return IMAGE_IMPORT_SUCCESS;
        } catch (Throwable e) {
            return IMAGE_IMPORT_FAILED;
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Throwable ignored) {}
        }
    }

    public static void restartKeyboard() {
        try {
            getAppDB().triggerApplyListeners();
        } catch (Throwable e) {
            // do nothing
        }
    }

    @Override
    public int importIconTheme(IconThemeParcel icons) {
        if (isIconThemeImported(icons.mThemeName)) {
            return ICON_THEME_IMPORT_FAILED_EXISTS;
        }

        return importIconThemeForced(icons);
    }

    @Override
    public int importIconThemeForced(IconThemeParcel icons) {
        try {
            String name = icons.mThemeName;
            getIconThemes().importIconTheme(name, new LocalIconTheme(icons));
        } catch (Throwable t) {
            return ICON_THEME_IMPORT_FAILED_UNKNOWN;
        }

        return ICON_THEME_IMPORT_SUCCESS;
    }

    @Override
    public boolean isIconThemeImported(String name) {
        return getIconThemes().isThemeExists(name);
    }

    @Override
    public int importLangPkg(String jsonStr) {
        return importLangPkgInternal(jsonStr, false);
    }

    @Override
    public int importLangPkgForced(String jsonStr) {
        return importLangPkgInternal(jsonStr, true);
    }

    private int importLangPkgInternal(String langPkgStr, boolean forced) {
        try {
            Language lang = createLanguage(langPkgStr, true);
            if (!forced && isLangPkgImported(lang.name))
                return LANG_PKG_IMPORT_FAILED_EXISTS;
            else if (lang.enabledSdk > Build.VERSION.SDK_INT)
                return LANG_PKG_IMPORT_FAILED_SDK;
            else if (!lang.enabled)
                return LANG_PKG_IMPORT_FAILED_NOT_ENABLED;
            File file = new File(getUserLanguageFilesDir(), "user_" + lang.name + Integer.toHexString(langPkgStr.hashCode()) + ".json");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(langPkgStr.getBytes());
            fos.flush();
            fos.close();
            clearLanguageCache();
            return LANG_PKG_IMPORT_SUCCESS;
        } catch (Throwable t) {
            // do nothing
        }
        return LANG_PKG_IMPORT_FAILED_UNKNOWN;
    }

    @Override
    public boolean isLangPkgImported(String name) {
        return getKeyboardLanguage(name, true).name.equals(name);
    }

    private static final class MissingKeysException extends RuntimeException {}
}
