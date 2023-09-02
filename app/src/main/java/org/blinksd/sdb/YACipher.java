package org.blinksd.sdb;

public class YACipher {
    private final byte[] mKey;

    public YACipher(byte[] key) {
        mKey = key;
    }

    public byte[] encode(byte[] input) {
        return encode(input, false);
    }

    private byte[] encode(byte[] input, boolean raw) {
        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < mKey.length; j++) {
                input[i] ^= mKey[j] ^ i ^ j;
            }
        }

        if (!raw) {
            return BasicBase64.encode(input);
        }

        return input;
    }

    public byte[] decode(byte[] input) {
        return encode(BasicBase64.decode(input), true);
    }

    public String encodeStr(String input) {
        return new String(encode(input.getBytes()));
    }

    public String decodeStr(String input) {
        return new String(decode(input.getBytes()));
    }
}
