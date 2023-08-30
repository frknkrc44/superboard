package org.blinksd.board.api;

import org.blinksd.board.api.parcelables.IconThemeParcel;

interface IKeyboardThemeApi {
    /* Theme */
    int importTheme(String jsonStr);
    int importThemeForced(String jsonStr);
    boolean isThemeImported(String name);

    /* Background image */
    int importBgImage(in Bitmap bmp);

    /* Icon theme */
    int importIconTheme(in IconThemeParcel icons);
    int importIconThemeForced(in IconThemeParcel icons);
    boolean isIconThemeImported(String name);

    /* Language pack */
    int importLangPkg(String jsonStr);
    int importLangPkgForced(String jsonStr);
    boolean isLangPkgImported(String name);
}