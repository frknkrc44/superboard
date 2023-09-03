package org.blinksd.sdb;

public class YACipher {
    private final byte[] mKey;
    private final char mSplitKey = 0x1B;

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
            return toHex(input);
        }

        return input;
    }

    public byte[] decode(byte[] input) {
        return encode(fromHex(input), true);
    }

    private byte[] fromHex(byte[] input) {
        String str = new String(input);
        String[] split = str.split(String.valueOf(mSplitKey));

        byte[] output = new byte[split.length];
        for(int i = 0; i < split.length; i++) {
            output[i] = (byte) Integer.parseInt(split[i], 16);
        }

        return output;
    }

    private byte[] toHex(byte[] input) {
        StringBuilder stringBuilder = new StringBuilder();

        for (byte item : input) {
            stringBuilder.append(Integer.toHexString(item));
            stringBuilder.append(mSplitKey);
        }

        stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
        return stringBuilder.toString().getBytes();
    }

    public String encodeStr(String input) {
        return new String(encode(input.getBytes()));
    }

    public String decodeStr(String input) {
        return new String(decode(input.getBytes()));
    }
}
