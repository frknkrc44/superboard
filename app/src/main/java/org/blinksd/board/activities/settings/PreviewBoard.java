package org.blinksd.board.activities.settings;

import static org.blinksd.utils.SuperDBHelper.getBooleanOrDefault;

import android.content.Context;

import org.blinksd.board.views.SuperBoard;
import org.blinksd.utils.SettingMap;

class PreviewBoard extends SuperBoard {
    public PreviewBoard(Context c) {
        super(c);
    }

    @Override
    protected void sendKeyboardEvent(Key v) {
        if (v.hasNormalPressEvent()) {
            playSound(v.getNormalPressEvent().first);
            return;
        }
        playSound(0);
        vibrate();
    }

    @Override
    public void playSound(int event) {
        if (!getBooleanOrDefault(SettingMap.SET_PLAY_SND_PRESS)) return;
        super.playSound(event);
    }
}