package org.blinksd.board.views.emoji;

import static org.blinksd.board.SuperBoardApplication.getTextUtils;

import java.util.ArrayList;

/**
 * A basic emoji holder with emoji specific changes
 */
public class EmojiList extends ArrayList<Emoji> {
    // we don't need to create an instance outside of package
    EmojiList() {}

    /**
     * Store the skin tones for future use
     * <p>
     * There's only 5 skin tones available
     * <p>
     * - Light<br>
     * - Medium-Light<br>
     * - Medium<br>
     * - Medium-Dark<br>
     * - Dark<br>
     */
    private static final String[] skinTones = new String[]{
            "\uD83C\uDFFB",
            "\uD83C\uDFFC",
            "\uD83C\uDFFD",
            "\uD83C\uDFFE",
            "\uD83C\uDFFF"
    };

    /**
     * A single regex rule to delete all skin tones from emoji
     */
    private static final String skinToneRegex = String.format("(%s)", String.join("|", skinTones));

    /**
     * Try to find the similar emoji from the list
     *
     * @param emoji Emoji string to process
     * */
    private Emoji findEmoji(String emoji) {
        final var size = size();
        for (int i = 0; i < size; i++) {
            final var next = get(i);
            if (emoji.replaceAll(skinToneRegex, "").contains(next.emoji)) {
                return next;
            }
        }

        return null;
    }

    /**
     * Add a new emoji entry or skin tone to an existing emoji
     *
     * @param emojiStr Emoji string to process
     * */
    public void addEmoji(String emojiStr) {
        // Don't add unsupported emojis
        if (!getTextUtils().hasGlyph(emojiStr)) {
            return;
        }

        // there's only 5 skin tones, so i can hardcode it
        for (int i = 0; i < 5; i++) {
            if (emojiStr.contains(skinTones[i])) {
                final var emoji = findEmoji(emojiStr);
                if (emoji != null) {
                    emoji.skinTones.add(emojiStr);
                    return;
                }
            }
        }

        add(new Emoji(emojiStr));
    }
}
