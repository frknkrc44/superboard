package org.blinksd.board.api;

import java.io.*;
import java.util.*;
import org.blinksd.*;
import org.blinksd.utils.color.ThemeUtils;
import org.json.*;

public class KeyboardThemeApi extends IKeyboardThemeApi.Stub {
    public static final int THEME_IMPORT_SUCCESS = 0,
                            THEME_IMPORT_FAILED_MISSING_KEYS = 1,
                            THEME_IMPORT_FAILED_EXISTS = 2,
                            THEME_IMPORT_FAILED_UNKNOWN = -1;

    public static final int IMAGE_IMPORT_SUCCESS = 0,
                            IMAGE_IMPORT_FAILED_INVALID = 1,
                            IMAGE_IMPORT_FAILED_UNKNOWN = -1;

	@Override
    public int importTheme(String jsonStr){
        try {
            JSONObject obj = new JSONObject(jsonStr);
            checkMandatoryKeys(obj);
            if(!isThemeImported(obj.getString("code"))) {
                importThemeInternal(obj);
                SuperBoardApplication.reloadThemeCache();
                return THEME_IMPORT_SUCCESS;
            }
            return THEME_IMPORT_FAILED_EXISTS;
        } catch(JSONException e) {
            // do nothing
        } catch(MissingKeysException x) {
            return THEME_IMPORT_FAILED_MISSING_KEYS;
        }
        return THEME_IMPORT_FAILED_UNKNOWN;
	}

    private void importThemeInternal(JSONObject obj) throws JSONException {
        String themeStr = obj.toString();
        try {
            File file = new File(ThemeUtils.getUserThemesDir() + "/user_" + obj.getString("code") + Integer.toHexString(themeStr.hashCode()) + ".json");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(themeStr.getBytes());
            fos.flush();
            fos.close();
        } catch(Throwable t) {
            throw new JSONException(t);
        }
    }

    private void checkMandatoryKeys(JSONObject obj) {
        if(!obj.has("name") && obj.has("code")) {
            throw new MissingKeysException();
        }
    }

	@Override
    public int importThemeForced(String jsonStr){
        try {
            JSONObject obj = new JSONObject(jsonStr);
            checkMandatoryKeys(obj);
            importThemeInternal(obj);
            SuperBoardApplication.reloadThemeCache();
            return THEME_IMPORT_SUCCESS;
        } catch(JSONException e) {
            // do nothing
        } catch(MissingKeysException x) {
            return THEME_IMPORT_FAILED_MISSING_KEYS;
        }
        return THEME_IMPORT_FAILED_UNKNOWN;
	}

	@Override
    public boolean isThemeImported(String name){
		List<ThemeUtils.ThemeHolder> themes = SuperBoardApplication.getApplication().getThemes();
        ThemeUtils.ThemeHolder theme = ThemeUtils.getUserThemeFromCodeName(themes, name);
        // System.out.println("NAME: " + name + " OBJ: " + theme);
        return theme != null;
	}
    	
	@Override
	public int importImage(String path){
        return IMAGE_IMPORT_FAILED_UNKNOWN;
	}

	@Override
    public int importImageBytes(byte[] bytes){
        return IMAGE_IMPORT_FAILED_UNKNOWN;
	}

    private class MissingKeysException extends RuntimeException {}
}