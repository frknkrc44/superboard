package org.blinksd.utils.keys;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * This class parses the OVERLAY KCM format (not complete).<br>
 * <a href="https://source.android.com/docs/core/interaction/input/key-character-map-files">
 *     More info (OVERLAY format is not documented here but still useful)
 * </a>.<br><br>
 *
 * Example OVERLAY KCM file:
 * <pre>
 * type OVERLAY
 *
 * map key 2 1
 *
 * key 1 {
 *   label: '1'
 *   base: '1'
 *   shift: '\u0021'
 *   capslock+shift: '\u0021'
 *   ralt: '\u003e'
 * }
 * </pre>
 */
class KCMParser {
    private KCMParser() {}

    private final List<Key> keys = new ArrayList<>();

    record KeyCombination(int bitValue, String code) {
        private static final int BIT_CTRL  = 0x01;
        private static final int BIT_LALT  = 0x02;
        private static final int BIT_RALT  = 0x04;
        private static final int BIT_CAPS  = 0x08;
        private static final int BIT_SHIFT = 0x10;

        public boolean ctrl() {
            return (bitValue & BIT_CTRL) != 0;
        }

        public boolean lAlt() {
            return (bitValue & BIT_LALT) != 0;
        }

        public boolean rAlt() {
            return (bitValue & BIT_RALT) != 0;
        }

        public boolean capsLock() {
            return (bitValue & BIT_CAPS) != 0;
        }

        public boolean shift() {
            return (bitValue & BIT_SHIFT) != 0;
        }

        @Override
        public String toString() {
            return "KeyCombination{" +
                    "bitValue=" + bitValue +
                    ", ctrl=" + ctrl() +
                    ", lAlt=" + lAlt() +
                    ", rAlt=" + rAlt() +
                    ", capsLock=" + capsLock() +
                    ", shift=" + shift() +
                    ", code='" + code + '\'' +
                    '}';
        }
    }

    static class Key {
        final int keyCode;
        final String name;
        String label = null;
        final List<KeyCombination> combinations = new ArrayList<>();

        private Key(int keyCode, String name) {
            this.keyCode = keyCode;
            this.name = name;
        }

        @Override
        public String toString() {
            return "Key{" +
                    "name='" + name + '\'' +
                    ", label='" + label + '\'' +
                    ", combinations=" + combinations +
                    '}';
        }
    }

    Key findByKeyCode(int keyCode) {
        for (var item : keys) {
            if (item.keyCode == keyCode) {
                return item;
            }
        }

        return null;
    }

    static KCMParser parseFileContent(String fileContent) {
        final var keyMapper = new KCMParser();
        final var keyMap = new LinkedHashMap<Integer, String>();

        var beginKeyRead = false;
        Key tempKey = null;
        for (var line : fileContent.split("\\n")) {
            if (line.startsWith("#")) {
                continue;
            }

            // CRLF -> LF and trim all whitespaces
            line = line.replaceAll("\r", "").trim();

            // map key 95 NUMPAD_COMMA
            if (line.startsWith("map")) {
                var splitMaps = line.replaceAll("\\s+", " ").split(" ");
                if (splitMaps.length != 4) {
                    continue;
                }

                keyMap.put(Integer.parseInt(splitMaps[2]), splitMaps[3]);
            }

            // key I {
            if (line.endsWith("{")) {
                final var name = line.substring(line.indexOf(" "), line.indexOf("{")).trim();

                Integer keyCode = null;
                for (var item : keyMap.entrySet()) {
                    if (item.getValue().equals(name)) {
                        keyCode = item.getKey();
                        break;
                    }
                }

                beginKeyRead = keyCode != null;

                if (keyCode == null) {
                    continue;
                }

                tempKey = new Key(keyCode, name);
            }

            if (beginKeyRead) {
                if (line.startsWith("}")) {
                    beginKeyRead = false;
                    keyMapper.keys.add(tempKey);
                    tempKey = null;
                }

                if (line.contains(":")) {
                    assert tempKey != null: "The temporary key returned null";

                    var splitColon = line.split(":");
                    for (var comb : splitColon[0].split(",")) {
                        var combSplit = Arrays.asList(comb.trim().split("\\+"));

                        // label: 'u'
                        if (combSplit.contains("label")) {
                            tempKey.label = unescapeUnicode(splitColon[1].substring(
                                    splitColon[1].indexOf("'") + 1,
                                    splitColon[1].lastIndexOf("'")
                            ));

                            continue;
                        }

                        // ralt+capslock+shift: 'i'
                        // capslock, shift: 'V'
                        tempKey.combinations.add(new KeyCombination(
                                getCombFlags(combSplit),
                                unescapeUnicode(splitColon[1].substring(
                                        splitColon[1].indexOf("'") + 1,
                                        splitColon[1].lastIndexOf("'")
                                ))
                        ));
                    }
                }
            }
        }

        return keyMapper;
    }

    private static int getCombFlags(List<String> combSplit) {
        var combFlags = 0;

        if (combSplit.contains("ctrl"))
            combFlags |= KeyCombination.BIT_CTRL;

        if (combSplit.contains("lalt"))
            combFlags |= KeyCombination.BIT_LALT;

        if (combSplit.contains("ralt"))
            combFlags |= KeyCombination.BIT_RALT;

        if (combSplit.contains("capslock"))
            combFlags |= KeyCombination.BIT_CAPS;

        if (combSplit.contains("shift"))
            combFlags |= KeyCombination.BIT_SHIFT;

        return combFlags;
    }

    public static String unescapeUnicode(String unescaped) {
        try {
            if (unescaped.startsWith("\\u")) {
                unescaped = String.valueOf((char) Integer.parseInt(unescaped.substring(2), 16));
            }
        } catch (Throwable ignored) {}

        return unescaped;
    }
}
