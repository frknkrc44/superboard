package org.blinksd.utils.keys;

import java.util.ArrayList;
import java.util.Arrays;
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
    private final List<MapKey> keyMaps = new ArrayList<>();

    record KeyCombination(int bitValue, String code) {
        private static final int BIT_CTRL  = 0x01;
        private static final int BIT_RALT  = 0x02;
        private static final int BIT_LALT  = 0x04;
        private static final int BIT_CAPS  = 0x08;
        private static final int BIT_SHIFT = 0x10;

        public boolean ctrl() {
            return (bitValue & BIT_CTRL) != 0;
        }

        public boolean rAlt() {
            return (bitValue & BIT_RALT) != 0;
        }

        public boolean lAlt() {
            return (bitValue & BIT_LALT) != 0;
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
                    ", rAlt=" + rAlt() +
                    ", lAlt=" + lAlt() +
                    ", capsLock=" + capsLock() +
                    ", shift=" + shift() +
                    ", code='" + code + '\'' +
                    '}';
        }
    }

    static class Key {
        final String name;
        final String label;
        final List<KeyCombination> combinations = new ArrayList<>();

        private Key(String name, String label) {
            this.name = name;
            this.label = label;
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

    private record MapKey(int keyCode, String keyName) {
        @Override
        public String toString() {
            return "MapKey{" +
                    "keyCode=" + keyCode +
                    ", keyName='" + keyName + '\'' +
                    '}';
        }
    }

    private Key findByKeyName(String keyName) {
        for (var item : keys) {
            if (keyName.equals(item.name)) {
                return item;
            }
        }

        return null;
    }

    Key findByKeyCode(int keyCode) {
        for (var item : keyMaps) {
            if (item.keyCode() == keyCode) {
                return findByKeyName(item.keyName());
            }
        }

        return null;
    }

    static KCMParser parseFileContent(String fileContent) {
        var keyMapper = new KCMParser();

        var beginKeyRead = false;
        String tempKeyName = null;
        Key tempKey = null;
        for (var line : fileContent.split("\\n")) {
            if (line.startsWith("#")) {
                continue;
            }

            if (beginKeyRead) {
                // ralt+capslock+shift: 'i'
                // capslock, shift: 'V'
                if (tempKey != null) {
                    if (line.contains(":")) {
                        var splitColon = line.split(":");
                        for (var comb : splitColon[0].split(",")) {
                            var combSplit = Arrays.asList(comb.trim().split("\\+"));

                            tempKey.combinations.add(new KeyCombination(
                                    getCombFlags(combSplit),
                                    unescapeUnicode(splitColon[1].substring(
                                            splitColon[1].indexOf("'") + 1,
                                            splitColon[1].lastIndexOf("'")
                                    ))
                            ));
                        }
                    }

                    if (line.startsWith("}")) {
                        beginKeyRead = false;
                        keyMapper.keys.add(tempKey);
                        tempKey = null;
                    }
                }

                // label: 'u'
                if (line.contains("label:")) {
                    tempKey = new Key(
                            tempKeyName,
                            line.substring(line.indexOf("'") + 1, line.lastIndexOf("'"))
                    );
                    tempKeyName = null;
                }
            }

            // key I {
            if (line.startsWith("key")) {
                beginKeyRead = true;
                tempKeyName = line.substring(line.indexOf(" "), line.indexOf("{")).trim();
            }

            // map key 95 NUMPAD_COMMA
            if (line.startsWith("map")) {
                var splitMaps = line.split(" ");
                var mapKey = new MapKey(
                        Integer.parseInt(splitMaps[2]),

                        // CRLF -> LF to fix my mental health
                        splitMaps[3].replaceAll("\r", "")
                );

                keyMapper.keyMaps.add(mapKey);
            }
        }

        return keyMapper;
    }

    private static int getCombFlags(List<String> combSplit) {
        var combFlags = 0;

        if (combSplit.contains("ctrl"))
            combFlags |= KeyCombination.BIT_CTRL;

        if (combSplit.contains("ralt"))
            combFlags |= KeyCombination.BIT_RALT;

        if (combSplit.contains("lalt"))
            combFlags |= KeyCombination.BIT_LALT;

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
