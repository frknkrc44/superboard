package org.blinksd.board.api;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;

public class KeyboardThemeService extends Service {
    private KeyboardThemeApi api = new KeyboardThemeApi();

    public Binder onBind(Intent intent) {
        return api;
    }
}
