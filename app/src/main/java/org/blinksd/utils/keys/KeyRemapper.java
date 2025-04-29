package org.blinksd.utils.keys;

import android.view.KeyEvent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class KeyRemapper {
    private KCMParser keyMapper = null;
    public String currentLang;

    public KeyRemapper() {}

    public void setKeyMapFromFileContent(String fileContent) {
        if (fileContent == null) {
            keyMapper = null;
        } else {
            keyMapper = KCMParser.parseFileContent(fileContent);
        }
    }

    public void setKeyMapFromIS(InputStream inputStream) throws IOException {
        var byteArrayOutputStream = new ByteArrayOutputStream();

        try (inputStream) {
            var buf = new byte[4096];
            int c;
            while ((c = inputStream.read(buf, 0, buf.length)) > 0) {
                byteArrayOutputStream.write(buf, 0, c);
            }
        }

        setKeyMapFromFileContent(byteArrayOutputStream.toString());
    }

    public String convertKey(KeyEvent event) {
        if (keyMapper == null) {
            return null;
        }

        var key = keyMapper.findByKeyCode(event.getScanCode());
        if (key == null) {
            return null;
        }

        for (var combination : key.combinations) {
            if (combination.bitValue() == 0 && event.hasNoModifiers()) {
                return combination.code();
            }

            if (combination.ctrl() == event.isCtrlPressed() &&
                    combination.lAlt() == event.hasModifiers(KeyEvent.META_ALT_LEFT_ON) &&
                    combination.rAlt() == event.hasModifiers(KeyEvent.META_ALT_RIGHT_ON) &&
                    combination.capsLock() == event.isCapsLockOn() &&
                    combination.shift() == event.isShiftPressed()) {
                return combination.code();
            }
        }

        return null;
    }
}
