package org.blinksd.board.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;

public class KeyboardThemeService extends Service {
    private final KeyboardThemeApi api = new KeyboardThemeApi();

    public Binder onBind(Intent intent) {
        return api;
    }
}
