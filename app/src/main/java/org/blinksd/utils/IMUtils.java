package org.blinksd.utils;

import android.view.inputmethod.InputConnection;

public class IMUtils {
    private IMUtils() {}

    public static String getFullText(InputConnection inputConnection) {
        var before = inputConnection.getTextBeforeCursor(Integer.MAX_VALUE, 0);
        var after = inputConnection.getTextAfterCursor(Integer.MAX_VALUE, 0);
        if (before == null && after == null) return null;

        return (before != null ? before.toString() : "") + (after != null ? after.toString() : "");
    }
}
