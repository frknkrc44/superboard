package org.blinksd.board.views.emoji;

import static org.blinksd.board.SuperBoardApplication.getSBApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An emoji list parser
 * <p>
 * This parser uses emoji_list.json file and parses it.
 * If you want to add new emojis, just execute a Python script
 * and the script will handle it for you.
 */
public class EmojiListParser {
    // this parser SHOULD be used as static
    private EmojiListParser() {}

    /**
     * Parse the emoji list JSON file
     *
     * @return mapped emoji list with skin tones
     */
    public static Map<String, EmojiList> parseEmojiJson() {
        final var output = new LinkedHashMap<String, EmojiList>();
        final JSONObject input;
        try {
            input = convertEmojiFileToJson();
        } catch (Throwable ignored) {
            return output;
        }

        for (Iterator<String> it = input.keys(); it.hasNext(); ) {
            try {
                final var name = it.next();
                final var value = input.getJSONArray(name);

                output.put(name, parseCategory(value));
            } catch (Throwable ignored) {}
        }

        try {
            output.put("Characters", parseCategory(new JSONArray(new String[]{
                    "🇦 ", "🇧 ", "🇨 ", "🇩 ", "🇪 ", "🇫 ", "🇬 ",
                    "🇭 ", "🇮 ", "🇯 ", "🇰 ", "🇱 ", "🇲 ", "🇳 ",
                    "🇴 ", "🇵 ", "🇶 ", "🇷 ", "🇸 ", "🇹 ", "🇺 ",
                    "🇻 ", "🇼 ", "🇽 ", "🇾 ", "🇿 "
            })));
        } catch (Throwable ignored) {}

        return output;
    }

    /**
     * Read the emoji list and convert it to a JSONObject
     *
     * @return The emoji list with categories
     * @throws IOException If the file is not found to process
     * @throws JSONException If the invalid JSON file structure processed from the file
     */
    private static JSONObject convertEmojiFileToJson() throws IOException, JSONException {
        try (final var inputStream = getSBApplication().getAssets().open("emoji_list.json");
             final var outputStream = new ByteArrayOutputStream()) {
            byte[] buf = new byte[4096];
            int count;
            while ((count = inputStream.read(buf, 0, buf.length)) > 0) {
                outputStream.write(buf, 0, count);
            }

            return new JSONObject(outputStream.toString());
        }
    }

    /**
     * Parse an emoji category, such as "Flags"
     *
     * @param input JSON array input for per-category
     * @return an emoji list to add in the category map
     */
    private static EmojiList parseCategory(JSONArray input) {
        final var output = new EmojiList();
        final var length = input.length();
        for (int i = 0; i < length; i++) {
            try {
                output.addEmoji(input.getString(i));
            } catch (Throwable ignored) {}
        }

        return output;
    }
}
