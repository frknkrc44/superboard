package org.blinksd.board.api;

interface IKeyboardThemeApi {
    /* Theme */
    int importTheme(String jsonStr);
    int importThemeForced(String jsonStr);
    boolean isThemeImported(String name);

    /* Background image */
    int importImage(String path);
    int importImageBytes(in byte[] bytes);
}