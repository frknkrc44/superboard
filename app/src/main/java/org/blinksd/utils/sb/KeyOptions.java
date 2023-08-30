package org.blinksd.utils.sb;

public class KeyOptions {
    public String key;
    public int width = 0;
    public int pressKeyCode = 0;
    public int longPressKeyCode = 0;
    public boolean repeat;
    public boolean pressIsNotEvent;
    public boolean longPressIsNotEvent;
    public boolean darkerKeyTint;

    @Override
    public String toString() {
        return "KeyOptions{" +
                "key='" + key + '\''/* +
                ", width=" + width +
                ", pressKeyCode=" + pressKeyCode +
                ", longPressKeyCode=" + longPressKeyCode +
                ", repeat=" + repeat +
                ", pressIsNotEvent=" + pressIsNotEvent +
                ", longPressIsNotEvent=" + longPressIsNotEvent +
                ", darkerKeyTint=" + darkerKeyTint*/ +
                '}';
    }
}
