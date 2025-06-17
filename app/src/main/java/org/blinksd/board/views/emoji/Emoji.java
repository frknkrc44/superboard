package org.blinksd.board.views.emoji;

import java.util.ArrayList;
import java.util.List;

/**
 * An emoji holder
 */
public class Emoji {
    /**
     * The emoji character
     */
    public final String emoji;

    /**
     * The emoji character list with the skin tones (if available)
     */
    public final List<String> skinTones = new ArrayList<>();

    // we don't need to create an instance outside of package
    Emoji(String emoji) {
        this.emoji = emoji;
    }
}
