package org.blinksd.utils;

import static org.blinksd.board.SuperBoardApplication.getAppFilesDir;
import static org.blinksd.board.SuperBoardApplication.getIconThemes;
import static org.blinksd.board.SuperBoardApplication.getKeyboardLanguageList;
import static org.blinksd.board.SuperBoardApplication.getSBApplication;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;

import org.blinksd.board.R;
import org.blinksd.board.views.SuperBoard;
import org.blinksd.utils.superboard.KeyOptions;
import org.blinksd.utils.superboard.Language;
import org.blinksd.utils.superboard.RowOptions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

@SuppressWarnings("deprecation")
public class LayoutUtils {
    public static final Language emptyLanguage;

    static {
        emptyLanguage = new Language();
        emptyLanguage.layout = emptyLanguage.popup = new ArrayList<>();
    }

    private LayoutUtils() {}

    private static List<RowOptions> createLayoutFromJSON(JSONArray jsonData) throws JSONException {
        List<RowOptions> rowList = new ArrayList<>();

        for (int i = 0; i < jsonData.length(); i++) {
            JSONObject row = jsonData.getJSONObject(i);

            boolean enablePadding = false;
            try {
                enablePadding = row.getBoolean("cpad");
            } catch(Throwable ignored) {}

            rowList.add(RowOptions.createEmpty(enablePadding));
            JSONArray keys = row.getJSONArray("keys");

            for (int j = 0; j < keys.length(); j++) {
                JSONObject key = keys.getJSONObject(j);

                KeyOptions ko = new KeyOptions();
                ko.key = key.getString("key");
                try {
                    ko.width = key.getInt("width");
                } catch (Throwable e) {
                    ko.width = 0;
                }

                try {
                    ko.pressKeyCode = key.getInt("pkc");
                } catch (Throwable e) {
                    ko.pressKeyCode = 0;
                }

                try {
                    ko.longPressKeyCode = key.getInt("lpkc");
                } catch (Throwable e) {
                    ko.longPressKeyCode = 0;
                }

                try {
                    ko.repeat = key.getBoolean("rep");
                } catch (Throwable e) {
                    ko.repeat = false;
                }

                try {
                    ko.pressIsNotEvent = key.getBoolean("pine");
                } catch (Throwable e) {
                    ko.pressIsNotEvent = false;
                }

                try {
                    ko.longPressIsNotEvent = key.getBoolean("lpine");
                } catch (Throwable e) {
                    ko.longPressIsNotEvent = false;
                }

                try {
                    ko.darkerKeyTint = key.getBoolean("dkt");
                } catch (Throwable e) {
                    ko.darkerKeyTint = false;
                }

                rowList.get(i).keys.add(ko);
            }
        }

        return rowList;
    }

    public static String[][] getLayoutKeys(List<RowOptions> list) {
        if (list != null) {
            String[][] xout = new String[list.size()][];
            for (int i = 0; i < list.size(); i++) {
                RowOptions subList = list.get(i);
                String[] out = new String[subList.keys.size()];
                for (int g = 0; g < subList.keys.size(); g++) {
                    KeyOptions opts = subList.keys.get(g);
                    out[g] = opts.key;
                }
                xout[i] = out;
            }
            return xout;
        }

        return new String[0][];
    }

    public static Language createLanguage(String fileData, boolean userLanguage) throws JSONException {
        Language l = new Language();
        JSONObject main = new JSONObject(fileData);
        l.name = main.getString("name");
        l.label = main.getString("label");
        l.enabled = main.getBoolean("enabled");
        l.enabledSdk = main.getInt("enabledSdk");
        l.author = main.getString("author");
        l.language = main.getString("language");
        l.layout = createLayoutFromJSON(main.getJSONArray("layout"));
        l.popup = createLayoutFromJSON(main.getJSONArray("popup"));
        l.userLanguage = userLanguage;
        return l;
    }

    /** @noinspection ResultOfMethodCallIgnored*/
    public static File getUserLanguageFilesDir() {
        File file = new File(getAppFilesDir(), "langpacks");
        if (!file.exists())
            file.mkdirs();
        return file;
    }

    public static Map<String, String> getSpecialCases() {
        Map<String, String> out = new HashMap<>();

        try {
            AssetManager assets = getSBApplication().getAssets();
            Scanner sc = new Scanner(assets.open("special_cases.json"));
            StringBuilder s = new StringBuilder();
            while (sc.hasNext()) s.append(sc.nextLine());
            sc.close();
            JSONObject obj = new JSONObject(s.toString());

            for (Iterator<String> it = obj.keys(); it.hasNext(); ) {
                String key = it.next();
                out.put(key, obj.getString(key));
            }
        } catch (Throwable ignored) {}

        return out;
    }

    public static HashMap<String, Language> getLanguageList(Context ctx) throws IOException, JSONException {
        HashMap<String, Language> langs = new HashMap<>();
        AssetManager assets = ctx.getAssets();
        String subdir = "langpacks";
        String[] items = assets.list(subdir);
        assert items != null;
        Arrays.sort(items);
        for (String str : items) {
            if (str.endsWith(".json")) {
                try {
                    Scanner sc = new Scanner(assets.open(subdir + "/" + str));
                    StringBuilder s = new StringBuilder();
                    while (sc.hasNext()) s.append(sc.nextLine());
                    sc.close();
                    Language l = createLanguage(s.toString(), false);
                    if (l.enabled && Build.VERSION.SDK_INT >= l.enabledSdk) {
                        langs.put(l.language, l);
                    }
                } catch (Throwable e) {
                    Log.d(
                            LayoutUtils.class.getSimpleName(),
                            "Language pack import failed for " + str,
                            e
                    );
                }
            }
        }

        File langFilesDir = getUserLanguageFilesDir();

        for (String file : Objects.requireNonNull(langFilesDir.list())) {
            Scanner sc = new Scanner(new File(langFilesDir, file));
            StringBuilder s = new StringBuilder();
            while (sc.hasNext()) s.append(sc.nextLine());
            sc.close();
            Language l = createLanguage(s.toString(), true);
            if (l.enabled && Build.VERSION.SDK_INT >= l.enabledSdk) {
                l.label += " (USER)";
                langs.put(l.language, l);
            }
        }
        return langs;
    }

    @SuppressWarnings("deprecation")
    public static void setKeyOpts(Language lang, SuperBoard sb) {
        List<RowOptions> langPack = lang.layout;
        for (int i = 0; i < langPack.size(); i++) {
            RowOptions subList = langPack.get(i);
            for (int g = 0; g < subList.keys.size(); g++) {
                KeyOptions ko = subList.keys.get(g);
                if (ko.width > 0) {
                    sb.setKeyWidthPercent(0, i, g, ko.width);
                }
                if (ko.pressKeyCode != 0) {
                    sb.setPressEventForKey(0, i, g, ko.pressKeyCode, !ko.pressIsNotEvent);
                }
                if (ko.longPressKeyCode != 0) {
                    sb.setLongPressEventForKey(0, i, g, ko.longPressKeyCode, !ko.longPressIsNotEvent);
                    if (ko.longPressIsNotEvent) {
                        SuperBoard.Key key = sb.getKey(0, i, g);
                        if (ko.longPressKeyCode == '\t') {
                            key.setSubText("→");
                        }
                    }
                }
                if (ko.repeat) {
                    sb.setKeyRepeat(0, i, g);
                }
                IconThemeUtils icons = getIconThemes();
                String theme = SuperDBHelper.getStringOrDefault(SettingMap.SET_ICON_THEME);
                switch (ko.pressKeyCode) {
                    case Keyboard.KEYCODE_SHIFT:
                        sb.setKeyDrawable(0, i, g, icons.getIconResource(theme, LocalIconTheme.SYM_TYPE_SHIFT));
                        sb.getKey(0, i, g).setStateCount(3);
                        break;
                    case Keyboard.KEYCODE_DELETE:
                        sb.setKeyDrawable(0, i, g, icons.getIconResource(theme, LocalIconTheme.SYM_TYPE_DELETE));
                        break;
                    case Keyboard.KEYCODE_MODE_CHANGE:
                        SuperBoard.Key kbdMChange = sb.getKey(0, i, g);
                        kbdMChange.setText("!?#");
                        kbdMChange.setSubText("↓");
                        break;
                    case KeyEvent.KEYCODE_SPACE:
                        setSpaceBarViewPrefs(icons, sb.getKey(0, i, g), lang.label);
                        break;
                    case Keyboard.KEYCODE_DONE:
                        sb.setKeyDrawable(0, i, g, icons.getIconResource(theme, LocalIconTheme.SYM_TYPE_ENTER));
                        break;
                    case SuperBoard.KEYCODE_SWITCH_LANGUAGE:
                        sb.setKeyDrawable(0, i, g, R.drawable.sym_keyboard_language);
                        break;
                    case SuperBoard.KEYCODE_OPEN_EMOJI_LAYOUT:
                        sb.setKeyDrawable(0, i, g, icons.getIconResource(theme, LocalIconTheme.SYM_TYPE_EMOJI));
                        break;
                }
            }
        }
        int iconmulti = SuperDBHelper.getIntOrDefault(SettingMap.SET_KEY_ICON_SIZE_MULTIPLIER);
        sb.setIconSizeMultiplier(iconmulti);
    }

    public static void setSpaceBarViewPrefs(IconThemeUtils icons, SuperBoard.Key space, String label) {
        if (icons == null) icons = getIconThemes();

        Drawable drawable = icons.getIconResource(LocalIconTheme.SYM_TYPE_SPACE);
        if (drawable == null) {
            space.setText(label);
        } else {
            space.setKeyIcon(drawable);
        }
    }

    public static ArrayList<String> getKeyListFromLanguageList() {
        return getKeyListFromLanguageList(getKeyboardLanguageList());
    }

    public static ArrayList<String> getKeyListFromLanguageList(HashMap<String, Language> list) {
        return new ArrayList<>(list.keySet());
    }
}
