package org.blinksd.utils.sb;

import java.util.ArrayList;
import java.util.List;

public class RowOptions {
    public final List<KeyOptions> keys;
    public final boolean enablePadding;
    public RowOptions(List<KeyOptions> keys, boolean enablePadding) {
        this.keys = keys;
        this.enablePadding = enablePadding;
    }

    public static RowOptions createEmpty(boolean enablePadding) {
        return new RowOptions(new ArrayList<>(), enablePadding);
    }

    @Override
    public String toString() {
        return "RowOptions{" +
                "keys=" + keys +
                ", enablePadding=" + enablePadding +
                '}';
    }
}
