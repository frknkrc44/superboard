package org.blinksd.sdb;

import java.io.ByteArrayOutputStream;

public class BasicBase64 {
    private BasicBase64() {}

    private static final byte[] ENCODE_TABLE = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/',
    };

    private static final byte[] DECODE_TABLE = {
            0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F,
            0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F,
            0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F,
            0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x3E,
            0x7F, 0x7F, 0x7F, 0x3F, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3A,
            0x3B, 0x3C, 0x3D, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x00,
            0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B,
            0x0C, 0x0D, 0x0E, 0x0F, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16,
            0x17, 0x18, 0x19, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x1A, 0x1B,
            0x1C, 0x1D, 0x1E, 0x1F, 0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26,
            0x27, 0x28, 0x29, 0x2A, 0x2B, 0x2C, 0x2D, 0x2E, 0x2F, 0x30, 0x31,
            0x32, 0x33, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F,
            0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F,
            0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F,
            0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F,
            0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F,
            0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F,
            0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F,
            0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F,
            0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F,
            0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F,
            0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F,
            0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F,
            0x7F, 0x7F, 0x7F
    };

    private static boolean isValidChar(byte chr) {
        if (chr < 0) chr *= -1;
        return DECODE_TABLE[chr] != 0x7F;
    }

    private static boolean isValidArray(byte[] arr) {
        if (arr.length % 4 != 0) {
            return false;
        }

        for (int i = 0; i < arr.length - 2; i++) {
            if (!isValidChar(arr[i])) {
                return false;
            }
        }

        if (!isValidChar(arr[arr.length - 1])) {
            return arr[arr.length - 1] == '=';
        }

        return isValidChar(arr[arr.length - 1]);
    }

    private static byte[] encodeTriplet(byte[] bytes) {
        assert bytes.length == 3 : "encodeTriplet bytes.length != 3";

        int concatBits = bytes[0] << 16 | bytes[1] << 8 | bytes[2];
        return new byte[] {
                ENCODE_TABLE[(concatBits >> 18) & 0x3F],
                ENCODE_TABLE[(concatBits >> 12) & 0x3F],
                ENCODE_TABLE[(concatBits >> 6) & 0x3F],
                ENCODE_TABLE[concatBits & 0x3F]
        };
    }

    private static byte[] decodeQuad(byte[] bytes) {
        assert bytes.length == 4 : "decodeQuad bytes.length != 4";

        int concatBits = (DECODE_TABLE[bytes[0]] << 18) |
                (DECODE_TABLE[bytes[1]] << 12) |
                (DECODE_TABLE[bytes[2]] << 6) |
                (DECODE_TABLE[bytes[3]]);

        return new byte[] {
                (byte) ((concatBits >> 16) & 0xFF),
                (byte) ((concatBits >> 8) & 0xFF),
                (byte) (concatBits & 0xFF)
        };
    }

    public static byte[] encode(byte[] input) {
        int size = input.length;
        int triples = size / 3;
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        for (int i = 0; i < triples; ++i) {
            byte[] tmp = new byte[3];
            System.arraycopy(input, i * 3, tmp, 0, tmp.length);
            byte[] chars = encodeTriplet(tmp);
            out.write(chars, 0, chars.length);
        }

        int remainingChars = input.length % 3;
        if (remainingChars == 2) {
            byte[] chars = encodeTriplet(new byte[]{
                    input[input.length - 2],
                    input[input.length - 1],
                    0
            });
            chars[chars.length - 1] = '=';
            out.write(chars, 0, chars.length);
        } else if (remainingChars == 1) {
            byte[] chars = encodeTriplet(new byte[]{
                    input[input.length - 1],
                    0,
                    0
            });
            chars[chars.length - 2] = chars[chars.length - 1] = '=';
            out.write(chars, 0, chars.length);
        }

        try {
            out.close();
        } catch (Throwable ignored) {}

        return out.toByteArray();
    }

    public static byte[] decode(byte[] input) {
        if (input.length < 1) {
            return input;
        }

        assert isValidArray(input) : "Base64 is not valid";

        byte[] unPaddedBytes = new byte[indexOfEQ(input) + 1];
        System.arraycopy(input, 0, unPaddedBytes, 0, unPaddedBytes.length);

        int quads = unPaddedBytes.length / 4;
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        for (int i = 0; i < quads; ++i) {
            byte[] tmp = new byte[4];
            System.arraycopy(unPaddedBytes, i * 4, tmp, 0, tmp.length);
            byte[] quad = decodeQuad(tmp);
            out.write(quad, 0, quad.length);
        }

        int remainingQuads = unPaddedBytes.length % 4;
        if (remainingQuads == 0) {
            try {
                out.close();
            } catch (Throwable ignored) {}

            return out.toByteArray();
        } else if (remainingQuads == 2) {
            byte[] quad = decodeQuad(new byte[] {
                    unPaddedBytes[unPaddedBytes.length - 2],
                    unPaddedBytes[unPaddedBytes.length - 1],
                    'A',
                    'A'
            });
            out.write(quad, 0, 1);
        } else {
            byte[] quad = decodeQuad(new byte[] {
                    unPaddedBytes[unPaddedBytes.length - 3],
                    unPaddedBytes[unPaddedBytes.length - 2],
                    unPaddedBytes[unPaddedBytes.length - 1],
                    'A'
            });
            out.write(quad, 0, 2);
        }

        try {
            out.close();
        } catch (Throwable ignored) {}

        return out.toByteArray();
    }

    private static int indexOfEQ(byte[] arr) {
        for (int i = 0; i < arr.length; i++) {
            if ('=' == arr[i]) {
                return i;
            }
        }

        return arr.length - 1;
    }
}
