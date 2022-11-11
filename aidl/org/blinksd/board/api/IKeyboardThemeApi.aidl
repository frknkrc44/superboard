package org.blinksd.board.api;

interface IKeyboardThemeApi {
    /* Theme */
    int importTheme(String jsonStr);
    int importThemeForced(String jsonStr);
    boolean isThemeImported(String name);

    /* TODO: Background image */
    int importBgImage(String path);
    int importBgImageBytes(in byte[] bytes);

    /* TODO: Icon theme */
    int importIconTheme(String jsonStr, in byte[] icons);
    int importIconThemeForced(String jsonStr, in byte[] icons);
    boolean isIconThemeImported(String name);

    /* Language pack */
    int importLangPkg(String jsonStr);
    int importLangPkgForced(String jsonStr);
    boolean isLangPkgImported(String name);
}