package org.blinksd.utils;

import static org.blinksd.utils.LocalIconTheme.SYM_TYPE_SPACE;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import org.blinksd.board.R;
import org.blinksd.board.SuperBoardApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

public class IconThemeUtils extends BaseMap<String, LocalIconTheme> {
    public IconThemeUtils() {
        put("theme_default", new LocalIconTheme(new int[]{
                R.drawable.sym_keyboard_shift,
                R.drawable.sym_keyboard_emoji,
                SpaceBarThemeUtils.SPACEBAR_DEFAULT,
                R.drawable.sym_keyboard_return,
                R.drawable.sym_keyboard_delete
        }));
        put("theme_board", new LocalIconTheme(new int[]{
                R.drawable.sym_board_shift,
                R.drawable.sym_board_emoji,
                SpaceBarThemeUtils.SPACEBAR_DEFAULT,
                R.drawable.sym_board_return,
                R.drawable.sym_board_delete
        }));
        put("theme_ay", new LocalIconTheme(new int[]{
                R.drawable.sym_ay_shift,
                R.drawable.sym_board_emoji,
                SpaceBarThemeUtils.SPACEBAR_DEFAULT,
                R.drawable.sym_board_return,
                R.drawable.sym_ay_delete
        }));

        loadImportedIcons();
    }

    /** @noinspection ResultOfMethodCallIgnored*/
    private File getIconFolder() {
        File iconFolder = new File(SuperBoardApplication.getApplication().getFilesDir(), "icon_themes");
        if (!iconFolder.exists()) {
            iconFolder.mkdirs();
        }
        return iconFolder;
    }

    private void loadImportedIcons() {
        File mainFolder = getIconFolder();
        for (File themeFolder : Objects.requireNonNull(mainFolder.listFiles())) {
            String themeName = themeFolder.getName();
            Drawable shiftIcon = loadFromFile(new File(themeFolder, "shift"));
            Drawable emojiIcon = loadFromFile(new File(themeFolder, "emoji"));
            Drawable spaceIcon = loadFromFile(new File(themeFolder, "space"));
            Drawable returnIcon = loadFromFile(new File(themeFolder, "return"));
            Drawable deleteIcon = loadFromFile(new File(themeFolder, "delete"));
            put(themeName, new LocalIconTheme(
                    shiftIcon,
                    emojiIcon,
                    spaceIcon,
                    returnIcon,
                    deleteIcon
            ));
        }
    }

    public boolean isThemeExists(String themeName) {
        File themeFolder = new File(getIconFolder(), String.format("%s_(I)", themeName));
        return themeFolder.exists();
    }

    /** @noinspection ResultOfMethodCallIgnored*/
    public void importIconTheme(String themeName, LocalIconTheme iconTheme) {
        File mainFolder = getIconFolder();
        File themeFolder = new File(mainFolder, String.format("%s_(I)", themeName));
        themeFolder.mkdirs();
        writeIconToFile(new File(themeFolder, "shift"), iconTheme.shiftIcon);
        writeIconToFile(new File(themeFolder, "emoji"), iconTheme.emojiIcon);
        writeIconToFile(new File(themeFolder, "space"), iconTheme.spaceIcon);
        writeIconToFile(new File(themeFolder, "return"), iconTheme.returnIcon);
        writeIconToFile(new File(themeFolder, "delete"), iconTheme.deleteIcon);
        loadImportedIcons();
    }

    private void writeIconToFile(File file, Drawable drawable) {
        try (FileOutputStream stream = new FileOutputStream(file)) {
            if (drawable instanceof BitmapDrawable) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                Bitmap bmp = bitmapDrawable.getBitmap();
                bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            }
        } catch (Throwable ignored) {
        }
    }

    private Drawable loadFromFile(File file) {
        if (!file.exists()) {
            return null;
        }

        try (FileInputStream stream = new FileInputStream(file)) {
            return new BitmapDrawable(BitmapFactory.decodeStream(stream));
        } catch (IOException ignored) {
            return null;
        }
    }

    public Drawable getIconResource(int type) {
        String key = SuperDBHelper.getStringOrDefault(SettingMap.SET_ICON_THEME);
        return getIconResource(key, type);
    }

    public Drawable getIconResource(String themeKey, int type) {
        if (type == SYM_TYPE_SPACE) {
            int res = SuperBoardApplication.getSpaceBarStyles().getIconResource();
            if (res != SpaceBarThemeUtils.SPACEBAR_DEFAULT) {
                return getDrawable(res);
            }
        }

        LocalIconTheme theme = get(containsKey(themeKey) ? themeKey : Defaults.ICON_THEME);
        return theme.getIconByType(type);
    }

    private Drawable getDrawable(int res) {
        switch (res) {
            case SpaceBarThemeUtils.SPACEBAR_HIDE:
                return new ColorDrawable();

            case SpaceBarThemeUtils.SPACEBAR_TEXT:
            case SpaceBarThemeUtils.SPACEBAR_DEFAULT:
                return null;
        }

        return LayoutUtils.getDrawableCompat(
                SuperBoardApplication.getApplication(), res, null);
    }
}