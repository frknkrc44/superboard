package org.blinksd.utils;

import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.util.Pair;
import android.widget.TextView;

import org.blinksd.board.SuperBoardApplication;
import org.blinksd.board.views.SuperBoard;

import java.nio.charset.Charset;

public class TextUtilsCompat {
    // U+DFFFD which is very end of unassigned plane.
    private static final String TOFU_STRING = "\uDB3F\uDFFD";
    private static final String EM_STRING = "m";
    private static final ThreadLocal<Pair<Rect, Rect>> sRectThreadLocal = new ThreadLocal<>();
    private final Paint paint = new Paint();

    // THIS PART IS COPIED FROM
    // https://android.googlesource.com/platform/frameworks/support/+/refs/heads/androidx-main/core/core/src/main/java/androidx/core/graphics/PaintCompat.java

    public TextUtilsCompat() {
    }

    private static Pair<Rect, Rect> obtainEmptyRects() {
        Pair<Rect, Rect> rects = sRectThreadLocal.get();
        if (rects == null) {
            rects = new Pair<>(new Rect(), new Rect());
            sRectThreadLocal.set(rects);
        } else {
            rects.first.setEmpty();
            rects.second.setEmpty();
        }
        return rects;
    }

    public Rect getTextBounds(float textSize, String text) {
        Rect bounds = new Rect();
        paint.setTextSize(textSize);
        paint.getTextBounds(text, 0, text.length(), bounds);
        return bounds;
    }

    public boolean hasGlyph(String text) {
        text = text.trim();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            return paint.hasGlyph(text);
        }

        return hasGlyph(paint, text);
    }

    /**
     * Determine whether the typeface set on the paint has a glyph supporting the
     * string in a backwards compatible way.
     *
     * @param paint  the paint instance to check
     * @param string the string to test whether there is glyph support
     * @return true if the typeface set on the given paint has a glyph for the string
     */
    private boolean hasGlyph(Paint paint, String string) {
        final int length = string.length();
        if (length == 1 && Character.isWhitespace(string.charAt(0))) {
            // measureText + getTextBounds skips whitespace so we need to special case it here
            return true;
        }
        final float missingGlyphWidth = paint.measureText(TOFU_STRING);
        final float emGlyphWidth = paint.measureText(EM_STRING);
        final float width = paint.measureText(string);
        if (width == 0f) {
            // If the string width is 0, it can't be rendered
            return false;
        }
        if (string.codePointCount(0, string.length()) > 1) {
            // Heuristic to detect fallback glyphs for ligatures like flags and ZWJ sequences
            // Return false if string is rendered too widely
            if (width > 2 * emGlyphWidth) {
                return false;
            }
            // Heuristic to detect fallback glyphs for ligatures like flags and ZWJ sequences (2).
            // If width is greater than or equal to the sum of width of each code point, it is very
            // likely that the system is using fallback fonts to draw {@code string} in two or more
            // glyphs instead of a single ligature glyph. (hasGlyph returns false in this case.)
            // False detections are possible (the ligature glyph may happen to have the same width
            // as the sum width), but there are no good way to avoid them.
            // NOTE: This heuristic does not work with proportional glyphs.
            // NOTE: This heuristic does not work when a ZWJ sequence is partially combined.
            // E.g. If system has a glyph for "A ZWJ B" and not for "A ZWJ B ZWJ C", this heuristic
            // returns true for "A ZWJ B ZWJ C".
            float sumWidth = 0;
            int i = 0;
            while (i < length) {
                int charCount = Character.charCount(string.codePointAt(i));
                sumWidth += paint.measureText(string, i, i + charCount);
                i += charCount;
            }
            if (width >= sumWidth) {
                return false;
            }
        }
        if (width != missingGlyphWidth) {
            // If the widths are different then its not tofu
            return true;
        }
        // If the widths are the same, lets check the bounds. The chance of them being
        // different chars with the same bounds is extremely small
        final Pair<Rect, Rect> rects = obtainEmptyRects();
        paint.getTextBounds(TOFU_STRING, 0, TOFU_STRING.length(), rects.first);
        paint.getTextBounds(string, 0, length, rects.second);
        return !rects.first.equals(rects.second);
    }

    public static Charset getCharset(String name) {
        return Charset.forName(name);
    }

    public static void setTypefaceFromTextType(TextView label, SuperBoard.TextType style) {
        if (style == null) {
            style = SuperBoard.TextType.regular;
        }
        switch (style) {
            case regular:
                label.setTypeface(Typeface.DEFAULT);
                break;
            case bold:
                label.setTypeface(Typeface.DEFAULT_BOLD);
                break;
            case italic:
                label.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
                break;
            case bold_italic:
                label.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC));
                break;
            case condensed:
                label.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));
                break;
            case condensed_bold:
                label.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
                break;
            case condensed_italic:
                label.setTypeface(Typeface.create("sans-serif-condensed", Typeface.ITALIC));
                break;
            case condensed_bold_italic:
                label.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD_ITALIC));
                break;
            case serif:
                label.setTypeface(Typeface.SERIF);
                break;
            case serif_bold:
                label.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
                break;
            case serif_italic:
                label.setTypeface(Typeface.create(Typeface.SERIF, Typeface.ITALIC));
                break;
            case serif_bold_italic:
                label.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD_ITALIC));
                break;
            case monospace:
                label.setTypeface(Typeface.MONOSPACE);
                break;
            case monospace_bold:
                label.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
                break;
            case monospace_italic:
                label.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.ITALIC));
                break;
            case monospace_bold_italic:
                label.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD_ITALIC));
                break;
            case serif_monospace:
                label.setTypeface(Typeface.create("serif-monospace", Typeface.NORMAL));
                break;
            case serif_monospace_bold:
                label.setTypeface(Typeface.create("serif-monospace", Typeface.BOLD));
                break;
            case serif_monospace_italic:
                label.setTypeface(Typeface.create("serif-monospace", Typeface.ITALIC));
                break;
            case serif_monospace_bold_italic:
                label.setTypeface(Typeface.create("serif-monospace", Typeface.BOLD_ITALIC));
                break;
            case custom:
                // Contains a system problem about custom font files,
                // Custom fonts applying too slowly and I can't fix it!
                label.setTypeface(SuperBoardApplication.getCustomFont());
                break;
        }
    }
}
