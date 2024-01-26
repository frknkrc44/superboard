package org.blinksd.board.views;

import static android.view.Gravity.CENTER;
import static android.view.View.OnTouchListener;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.text.InputType;
import android.util.Log;
import android.util.Pair;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.blinksd.board.R;
import org.blinksd.utils.ColorUtils;
import org.blinksd.utils.DensityUtils;
import org.blinksd.utils.TextUtilsCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings({"deprecation", "unused"})
public class SuperBoard extends FrameLayout implements OnTouchListener {

    public static final int KEYCODE_CLOSE_KEYBOARD = -100;
    public static final int KEYCODE_SWITCH_LANGUAGE = -101;
    public static final int KEYCODE_OPEN_EMOJI_LAYOUT = -102;
    public static final int KEYCODE_TOGGLE_CTRL = -103;
    public static final int KEYCODE_TOGGLE_ALT = -104;
    public static final int SHIFT_OFF = 0;
    public static final int SHIFT_ON = 1;
    public static final int SHIFT_LOCKED = 2;
    protected static final int
            TAG_LP = R.id.key_lp,
            TAG_NP = R.id.key_np,
            TAG_DISABLE_MODIFIER = R.id.disable_type_modifier,
            TAG_KEY_REPEAT = R.id.key_repeat;
    private static Locale loc = new Locale("tr", "TR");
    private final MyHandler mHandler = new MyHandler();
    private final Vibrator vb;
    private int keyclr = -1;
    private float txtsze = -1;
    protected Drawable keybg = null;
    InputMethodService curr = null;
    int action = 0;
    private int selected = 0, shift = 0, hp = 40, wp = 100, y, shrad = 0,
            shclr = -1, txtclr = Color.WHITE, txts = 0, vib = 0, mult = 1,
            act = MotionEvent.ACTION_UP, iconmulti = 1, ctrl = 0, alt = 0;
    private boolean clear = false;
    private boolean lng = false;
    private boolean dpopup = false;
    private boolean ppreview = false;
    private Typeface cFont = Typeface.DEFAULT;
    private boolean isRepeat = true;
    private boolean shiftDetect = true;
    private final Map<String, String> specialCases = new HashMap<>();

    public SuperBoard(Context c) {
        super(c);
        if (c instanceof InputMethodService) {
            curr = (InputMethodService) c;
        }
        if (Build.VERSION.SDK_INT < 31)
            vb = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
        else {
            VibratorManager vm = (VibratorManager) c.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            vb = vm.getDefaultVibrator();
        }
        // trigSystemSuggestions();
        setLayoutParams(new LayoutParams(-1, -1));
        setBackgroundColor(0xFF212121);
        createEmptyLayout();
        setKeyboardHeight(hp);
    }

    public static void setTypefaceFromTextType(TextView label, TextType style, Typeface customFont) {
        if (style == null) {
            style = TextType.regular;
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
                label.setTypeface(customFont);
                break;
        }
    }

    /** @noinspection EmptyMethod*/
    public void beforeKeyboardEvent(View v) {}

    public void onKeyboardEvent(View v) {

    }

    public void afterKeyboardEvent() {

    }

    public void onPopupEvent() {

    }

    public void afterPopupEvent() {
        mHandler.removeAndSendEmptyMessage(0);
    }

    public void switchLanguage() {

    }

    public void openEmojiLayout() {

    }

    public void setPadding(int p) {
        setPadding(p, p, p, p);
    }

    public void setCustomFont(Typeface type) {
        cFont = type;
    }

    public void setSpecialCases(Map<String, String> items) {
        specialCases.clear();
        specialCases.putAll(items);
    }

    public int getKeyboardHeight() {
        return getLayoutParams().height;
    }

    public void setKeyboardHeight(int percent) {
        //if(percent > 19 && percent < 81){
        hp = percent;
        getLayoutParams().height = DensityUtils.hpInt(percent);
        if (getChildCount() > 0) {
            for (int i = 0; i < getChildCount(); i++) {
                getChildAt(i).getLayoutParams().height = getLayoutParams().height;
            }
        }
        int x = selected;

        // this line added because after
        // resizing layout, it needs recreation
        setEnabledLayout(findNumberKeyboardIndex());
        setEnabledLayout(x);
        //} else throw new RuntimeException("Invalid keyboard height");
    }

    public int getKeyboardHeightPercent() {
        return hp;
    }

    public int getKeyboardWidthPercent() {
        return wp;
    }

    public void fixHeight() {
        setKeyboardHeight(hp);
        for (int i = 0; i < getChildCount(); i++) {
            for (int g = 0; g < getKeyboard(i).getChildCount(); g++) {
                getRow(i, g).setKeyWidths();
            }
        }
    }

    public void setBackground(Drawable background) {
        setBackgroundDrawable(background);
    }

    public void setBackgroundDrawable(Drawable background) {
        super.setBackgroundDrawable(Objects.requireNonNull(background.getConstantState()).newDrawable());
    }

    public void setKeyVibrateDuration(int dur) {
        vib = dur;
    }

    public void clear() {
        if (clear) {
            for (int i = 0; i < getChildCount(); i++) {
                ViewGroup k = getKeyboard(i);
                for (int g = 0; g < k.getChildCount(); g++)
                    getRow(i, g).removeAllViewsInLayout();
                k.removeAllViewsInLayout();
            }
            removeAllViewsInLayout();
            createEmptyLayout();
            clear = false;
        }
    }

    public void setKeyTintColor(Key k, int normalColor, int pressColor) {
        Drawable d = k.getBackground();
        try {
            if (Build.VERSION.SDK_INT > 21) {
                d.setTintList(getTintListWithStates(normalColor, pressColor));
            } else {
                d.setColorFilter(normalColor, PorterDuff.Mode.SRC_ATOP);
            }
        } catch (Exception e) {
            d.setColorFilter(normalColor, PorterDuff.Mode.SRC_ATOP);
        }
    }

    public void setKeyTintColor(int keyboardIndex, int rowIndex, int keyIndex, int normalColor, int pressColor) {
        setKeyTintColor(getKey(keyboardIndex, rowIndex, keyIndex), normalColor, pressColor);
    }

    public void setKeyBackground(int keyboardIndex, int rowIndex, int keyIndex, Drawable background) {
        getKey(keyboardIndex, rowIndex, keyIndex).setBackground(background);
    }

    public ColorStateList getTintListWithStates(int normalColor, int pressColor) {
        return new ColorStateList(new int[][]{
                {android.R.attr.state_selected}, {}
        }, new int[]{pressColor, normalColor});
    }

    public void setKeyRepeat(int keyboardIndex, int rowIndex, int keyIndex) {
        setKeyRepeat(keyboardIndex, rowIndex, keyIndex, true);
    }

    public void setKeyRepeat(int keyboardIndex, int rowIndex, int keyIndex, boolean repeat) {
        setKeyRepeat(getKey(keyboardIndex, rowIndex, keyIndex), repeat);
    }

    public void setKeyRepeat(SuperBoard.Key key) {
        setKeyRepeat(key, true);
    }

    public void setKeyRepeat(SuperBoard.Key key, boolean repeat) {
        key.setTag(TAG_KEY_REPEAT, repeat);
    }

    public boolean isKeyRepeat(int keyboardIndex, int rowIndex, int keyIndex) {
        return isKeyRepeat(getKey(keyboardIndex, rowIndex, keyIndex));
    }

    public boolean isKeyHasEvent(int keyboardIndex, int rowIndex, int keyIndex) {
        return isKeyHasEvent(getKey(keyboardIndex, rowIndex, keyIndex));
    }

    public boolean isKeyHasEvent(Key k) {
        return isKeyRepeat(k) || k.getTag(TAG_LP) != null || k.getTag(TAG_NP) != null;
    }

    private boolean isKeyRepeat(View v) {
        if (!isRepeat) return false;
        Object tag = v.getTag(TAG_KEY_REPEAT);
        return tag != null && (boolean) tag;
    }

    public void setKeyWidthPercent(int keyboardIndex, int rowIndex, int keyIndex, int percent) {
        Key k = getKey(keyboardIndex, rowIndex, keyIndex);
        k.getLayoutParams().width = DensityUtils.wpInt(percent);
        k.setId(percent);
    }

    public final void setLongPressMultiplier(int multi) {
        mult = multi;
    }

    public final void setIconSizeMultiplier(int multi) {
        iconmulti = multi;
        applyIconMultiply();
    }

    private boolean isHasPopup(View v) {
        CharSequence cs = ((Key) v).getHint();
        if (cs == null) return false;
        return (!isKeyRepeat(v)) && (cs.length() > 0);
    }

    public void setPopupForKey(int keyboardIndex, int rowIndex, int keyIndex, String chars) {
        StringBuilder cs = new StringBuilder();
        for (String x : chars.split("")) {
            if (!cs.toString().contains(x)) {
                cs.append(x);
            }
        }
        Key key = getKey(keyboardIndex, rowIndex, keyIndex);
        key.setHint(cs.toString());
        if (cs.length() > 0)
            key.setSubText(String.valueOf(cs.charAt(0)));
    }

    public void setLayoutPopup(int keyboardIndex, String[][] chars) {
        if (chars != null) {
            if (keyboardIndex < getChildCount() && keyboardIndex >= 0) {
                ViewGroup v = getKeyboard(keyboardIndex);

                assert (v.getChildCount() == chars.length)
                        : "Row count != Popup row count";

                for (int i = 0; i < v.getChildCount(); i++) {
                    Row r = getRow(keyboardIndex, i);

                    assert (r.getChildCount() == chars[i].length)
                            : "Row key count != Popup row key count";

                    for (int g = 0; g < r.getChildCount(); g++)
                        setPopupForKey(keyboardIndex, i, g, chars[i][g]);
                }
            } else throw new RuntimeException("Invalid keyboard index number");
        }
    }

    public void setKeysPadding(final int padding) {
        if (getReferenceKeyMargin() != padding)
            applyToAllKeys(new ApplyToKeyRunnable() {
                public void run(Key key) {
                    Row.LayoutParams l = (Row.LayoutParams) key.getLayoutParams();
                    l.bottomMargin = l.topMargin = l.leftMargin = l.rightMargin = padding;
                }
            });
    }

    private int getReferenceKeyMargin() {
        try {
            Key key = getKey(0, 0, 0);
            Row.LayoutParams l = (Row.LayoutParams) key.getLayoutParams();
            return l.bottomMargin;
        } catch (Throwable ignored) {
        }
        return -1;
    }

    public int getKeysTextColor() {
        return keyclr;
    }

    public void setKeysTextColor(final int color) {
        if (keyclr != color)
            applyToAllKeys(new ApplyToKeyRunnable() {
                public void run(Key key) {
                    key.setKeyItemColor(color);
                }
            });
        keyclr = color;
    }

    private void applyIconMultiply() {
        applyToAllKeys(new ApplyToKeyRunnable() {
            public void run(Key key) {
                key.applyIconMultiply();
            }
        });
    }

    protected float getKeysTextSize() {
        return txtsze;
    }

    public void setKeysTextSize(final int size) {
        setKeysTextSize(size, false);
    }

    public void setKeysTextSize(final int size, boolean force) {
        if (txtsze != size || force)
            applyToAllKeys(new ApplyToKeyRunnable() {
                public void run(Key key) {
                    key.setKeyTextSize(size);
                }
            });
        txtsze = size;
    }

    public void setKeysBackground(final Drawable d) {
        if (keybg != d)
            applyToAllKeys(new ApplyToKeyRunnable() {
                public void run(Key key) {
                    key.setBackground(d);
                }
            });
        keybg = d;
    }

    public void setKeysShadow(final int radius, final int color) {
        if (shrad != radius || shclr != color)
            applyToAllKeys(new ApplyToKeyRunnable() {
                public void run(Key key) {
                    key.setKeyShadow(radius, color);
                }
            });
        shrad = radius;
        shclr = color;
    }

    public void setKeysPopupPreviewEnabled(final boolean enabled) {
        if (enabled != ppreview)
            applyToAllKeys(new ApplyToKeyRunnable() {
                public void run(Key key) {
                    ppreview = enabled;
                    key.setKeyImageVisible(key.isKeyIconSet());
                }
            });
    }

    public void setKeysTextType(final int style) {
        if (txts != style)
            applyToAllKeys(new ApplyToKeyRunnable() {
                public void run(Key key) {
                    key.setKeyTextStyle(style);
                }
            });
        txts = style;
    }

    public void setKeysTextType(final TextType style) {
        if (style == null) {
            setKeysTextType(0);
            return;
        }
        int i = 0;
        for (TextType type : TextType.values()) {
            if (style.name().equals(type.name())) {
                setKeysTextType(i);
                break;
            }
            i++;
        }
    }

    public void applyToAllKeys(ApplyToKeyRunnable runnable) {
        for (int j = 0; j < getChildCount(); j++) {
            for (int i = 0; i < getKeyboard(j).getChildCount(); i++) {
                for (int g = 0; g < getRow(j, i).getChildCount(); g++) {
                    runnable.run(getKey(j, i, g));
                }
            }
        }
    }

    public void setKeyboardWidth(int percent) {
        //if(percent > 11 && percent < 101){
        wp = percent;
        getLayoutParams().width = DensityUtils.wpInt(percent);
        if (getChildCount() > 0) {
            for (int i = 0; i < getChildCount(); i++) {
                getChildAt(i).getLayoutParams().width = getLayoutParams().width;
            }
        }
        int x = selected;
        setEnabledLayout(findNumberKeyboardIndex());
        setEnabledLayout(x);
        //} else throw new RuntimeException("Invalid keyboard width");
    }

    public void setKeyLongClickEvent(int keyboardIndex, int rowIndex, int keyIndex, OnLongClickListener event) {
        getKey(keyboardIndex, rowIndex, keyIndex).setOnLongClickListener(event);
    }

    public void setKeyDrawable(int keyboardIndex, int rowIndex, int keyIndex, int resId) {
        setKeyDrawable(keyboardIndex, rowIndex, keyIndex, getResources().getDrawable(resId));
    }

    public void setKeyDrawable(int keyboardIndex, int rowIndex, int keyIndex, Drawable d) {
        d.setColorFilter(keyclr, PorterDuff.Mode.SRC_ATOP);
        Key t = getKey(keyboardIndex, rowIndex, keyIndex);
        ((LinearLayout.LayoutParams) t.getLayoutParams()).gravity = CENTER;
        t.setKeyIcon(d);
    }

    public int getEnabledLayoutIndex() {
        return selected;
    }

    public void setEnabledLayout(KeyboardType type) {
        setEnabledLayout(findKeyboardIndex(type));
    }

    public void setEnabledLayout(int keyboardIndex) {
        if (keyboardIndex < 0) keyboardIndex += getChildCount();
        if (keyboardIndex < getChildCount() && keyboardIndex >= 0) {
            if (getChildCount() == 1 || keyboardIndex == selected) return;
            getChildAt(selected).setVisibility(GONE);
            selected = keyboardIndex;
            getChildAt(selected).setVisibility(VISIBLE);
        } else throw new RuntimeException("Invalid keyboard index number");
    }

    public void resetToNormalLayout() {
        setEnabledLayout(KeyboardType.TEXT);
    }

    public void setLayoutType(int keyboardIndex, KeyboardType type) {
        getKeyboard(keyboardIndex).setTag(type);
    }

    public void createLayoutWithRows(String[][] keys, KeyboardType type) {
        createEmptyLayout(type);
        addRows(getChildCount() - 1, keys);
    }

    public void createLayoutWithRows(String[][] keys) {
        createLayoutWithRows(keys, KeyboardType.TEXT);
    }

    public void createEmptyLayout() {
        createEmptyLayout(KeyboardType.TEXT);
    }

    public void createEmptyLayout(KeyboardType type) {
        LinearLayout ll = new LinearLayout(getContext());
        ll.setLayoutParams(new LayoutParams(-1, getLayoutParams().height));
        ll.setOrientation(LinearLayout.VERTICAL);
        addView(ll);
        setLayoutType(getChildCount() - 1, type);
        if (getChildCount() != 1) {
            ll.setVisibility(GONE);
        }
    }

    public int getCurrentKeyboardIndex() {
        return selected;
    }

    public ViewGroup getCurrentKeyboard() {
        return getKeyboard(selected);
    }

    public ViewGroup getKeyboard(int keyboardIndex) {
        if (keyboardIndex < 0) keyboardIndex += getChildCount();
        return (ViewGroup) getChildAt(keyboardIndex);
    }

    public void replaceNormalKeyboard(String[][] newKeyboard) {
        ViewGroup vg = getKeyboard(findNormalKeyboardIndex());
        vg.removeAllViewsInLayout();
        addRows(findNormalKeyboardIndex(), newKeyboard);
    }

    public void replaceRowFromKeyboard(int keyboardIndex, int rowIndex, String[] chars) {
        getRow(keyboardIndex, rowIndex).removeAllViewsInLayout();
        for (String aChar : chars) {
            addKeyToRow(keyboardIndex, rowIndex, aChar);
        }
    }

    public void removeRowFromKeyboard(int keyboardIndex, int rowIndex) {
        getRow(keyboardIndex, rowIndex).removeAllViewsInLayout();
        getKeyboard(keyboardIndex).removeViewAt(rowIndex);
    }

    public void removeKeyFromRow(int keyboardIndex, int rowIndex, int keyIndex) {
        getRow(keyboardIndex, rowIndex).removeViewAt(keyIndex);
    }

    public Row getRow(int keyboardIndex, int rowIndex) {
        if (rowIndex < 0) rowIndex += getKeyboard(keyboardIndex).getChildCount();
        return (Row) getKeyboard(keyboardIndex).getChildAt(rowIndex);
    }

    public Key getKey(int keyboardIndex, int rowIndex, int keyIndex) {
        if (keyIndex < 0) keyIndex += getRow(keyboardIndex, rowIndex).getChildCount();
        return (Key) getRow(keyboardIndex, rowIndex).getChildAt(keyIndex);
    }

    public void setKeyPopup(int keyboardIndex, int rowIndex, int keyIndex, String chars) {
        getKey(keyboardIndex, rowIndex, keyIndex).setHint(chars);
    }

    public void addRows(int keyboardIndex, String[][] keys) {
        if (keys != null) {
            for (String[] key : keys) {
                addRow(keyboardIndex, key);
            }
        }
    }

    public void addKeyToRow(int keyboardIndex, int rowIndex, String key) {
        addKeyToRow(keyboardIndex, rowIndex, key, "");
    }

    public void addKeyToRow(int keyboardIndex, int rowIndex, String key, String subKey) {
        Row r = getRow(keyboardIndex, rowIndex);
        Key k = createKey(key, subKey);
        r.addKey(k);
        r.setKeyWidths();
    }

    public Key createKey(String key, String subKey) {
        Key k = new Key(getContext());
        k.setText(key);
        k.setSubText(subKey);
        return k;
    }

    public void addRow(int keyboardIndex, String[] keys) {
        addRow(keyboardIndex, null, keys);
    }

    public void addRow(int keyboardIndex, Key template, String[] keys) {
        clear = true;
        Row r = new Row(getContext());
        if (keys.length > 0) {
            for (String key : keys) {
                Key k = new Key(getContext());
                if (template != null) {
                    template.clone(k);
                }
                k.setText(key);
                r.addKey(k);
            }
            r.setKeyWidths();
        }
        getKeyboard(keyboardIndex).addView(r);
    }

    protected void sendDefaultKeyboardEvent(View v) {
        defaultKeyboardEvent((Key) v);
    }

    private void defaultKeyboardEvent(Key v) {
        beforeKeyboardEvent(v);

        if (v.getTag(TAG_NP) != null) {
            Pair<Integer, Boolean> currentKey = v.getNormalPressEvent();
            switch (y = currentKey.first) {
                case KEYCODE_TOGGLE_CTRL:
                    setCtrlState();
                    break;
                case KEYCODE_TOGGLE_ALT:
                    setAltState();
                    break;
                case Keyboard.KEYCODE_SHIFT:
                    setShiftState();
                    break;
                case Keyboard.KEYCODE_CANCEL:
                    setEnabledLayout((selected - 1) >= 0 ? selected - 1 : findSymbolKeyboardIndex());
                    break;
                case Keyboard.KEYCODE_MODE_CHANGE:
                    setEnabledLayout(selected == 0 ? findSymbolKeyboardIndex() : findNormalKeyboardIndex());
                    setCtrlState(0);
                    setAltState(0);
                    break;
                case Keyboard.KEYCODE_ALT:
                    setEnabledLayout((selected + 1) % getChildCount());
                    break;
                case Keyboard.KEYCODE_DELETE:
                    sendKeyEvent(KeyEvent.KEYCODE_DEL);
                    break;
                case Keyboard.KEYCODE_DONE:
                    switch (action) {
                        case EditorInfo.IME_ACTION_DONE:
                        case EditorInfo.IME_ACTION_GO:
                        case EditorInfo.IME_ACTION_SEARCH:
                        case EditorInfo.IME_ACTION_SEND:
                        case EditorInfo.IME_ACTION_NEXT:
                        case EditorInfo.IME_ACTION_PREVIOUS:
                            performEditorAction(action);
                            break;
                        default:
                            sendKeyEvent(KeyEvent.KEYCODE_ENTER);
                            break;
                    }
                    break;
                default:
                    if (currentKey.second) {
                        sendKeyEvent(y);
                    } else {
                        commitText(String.valueOf((char) y));
                    }

                    updateKeyState();
                    break;
            }
            playSound(y);
        } else {
            commitText(v.getText().toString());
            updateKeyState();

            playSound(0);
        }

        vibrate();
        onKeyboardEvent(v);
    }

    public void vibrate() {
        vibrateInternal(vib);
    }

    private void vibrateInternal(int duration) {
        if (duration > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vb.vibrate(VibrationEffect.createOneShot(duration, 255));
            } else {
                vb.vibrate(duration);
            }
        }
    }

    /**
     * Use fake keyboard event instead of real one
     *
     * @param v - Input key for play sound
     */
    public void fakeKeyboardEvent(Key v) {
        if (v.getTag(TAG_NP) != null) {
            Pair<Integer, Boolean> currentKey = v.getNormalPressEvent();
            y = currentKey.first;
            playSound(y);
            return;
        }
        playSound(0);
        vibrate();
    }

    public InputMethodService getServiceContext() {
        return curr;
    }

    public InputConnection getCurrentIC() {
        return getServiceContext().getCurrentInputConnection();
    }

    @SuppressLint("InlinedApi")
    public void sendKeyEvent(int code) {
        switch (code) {
            case KEYCODE_CLOSE_KEYBOARD:
                closeKeyboard();
                break;
            case KEYCODE_SWITCH_LANGUAGE:
                switchLanguage();
                break;
            case KEYCODE_OPEN_EMOJI_LAYOUT:
                openEmojiLayout();
                break;
            case KEYCODE_TOGGLE_CTRL:
            case KEYCODE_TOGGLE_ALT:
                break;
            default:
                int metaState = 0;

                if (!isDisabledModifierForKeyboard(selected)) {
                    if (getCtrlState() > 0) {
                        metaState |= KeyEvent.META_CTRL_LEFT_ON | KeyEvent.META_CTRL_ON;
                        sendCtrl(true);
                    }

                    if (getAltState() > 0) {
                        metaState |= KeyEvent.META_ALT_LEFT_ON | KeyEvent.META_ALT_ON;
                        sendAlt(true);
                    }
                }

                sendKeyUpDown(code, metaState);

                if (!isDisabledModifierForKeyboard(selected)) {
                    if (getCtrlState() > 0) {
                        sendCtrl(false);
                    }

                    if (getAltState() > 0) {
                        sendAlt(false);
                    }
                }
        }
    }

    @SuppressLint("InlinedApi")
    private void sendCtrl(boolean down) {
        int metaState = KeyEvent.META_CTRL_ON | KeyEvent.META_CTRL_LEFT_ON;
        if (down) {
            sendKeyDown(KeyEvent.KEYCODE_CTRL_LEFT, metaState);
        } else {
            sendKeyUp(KeyEvent.KEYCODE_CTRL_LEFT, metaState);
        }
    }

    private void sendAlt(boolean down) {
        int metaState = KeyEvent.META_ALT_ON | KeyEvent.META_ALT_LEFT_ON;
        if (down) {
            sendKeyDown(KeyEvent.KEYCODE_ALT_LEFT, metaState);
        } else {
            sendKeyUp(KeyEvent.KEYCODE_ALT_LEFT, metaState);
        }
    }

    @SuppressLint("InlinedApi")
    private void sendKeyUpDown(int code) {
        int metaState = 0;

        if (!isDisabledModifierForKeyboard(selected)) {
            if (getCtrlState() > 0) {
                metaState |= KeyEvent.META_CTRL_LEFT_ON | KeyEvent.META_CTRL_ON;
            }

            if (getAltState() > 0) {
                metaState |= KeyEvent.META_ALT_LEFT_ON | KeyEvent.META_ALT_ON;
            }
        }

        sendKeyUpDown(code, metaState);
    }

    private void sendKeyUpDown(int code, int metaState) {
        sendKeyUp(code, metaState);
        sendKeyDown(code, metaState);
    }

    private void sendKeyUp(int code, int metaState) {
        sendKeyAction(code, KeyEvent.ACTION_UP, metaState);
    }

    private void sendKeyDown(int code, int metaState) {
        sendKeyAction(code, KeyEvent.ACTION_DOWN, metaState);
    }

    private void sendKeyAction(int code, int action, int metaState) {
        KeyEvent event = new KeyEvent(0, 0, action, code, 0, metaState);
        getCurrentIC().sendKeyEvent(event);
    }

    private void performEditorAction(int action) {
        getCurrentIC().performEditorAction(action);
    }

    public final void commitText(String text) {
        if (text == null) return;
        boolean modifierDisabledForKeyboard = isDisabledModifierForKeyboard(selected);
        boolean modifiersEnabled = false;

        if (!modifierDisabledForKeyboard) {
            if (getCtrlState() > 0) {
                sendCtrl(true);
                modifiersEnabled = true;
            }

            if (getAltState() > 0) {
                sendAlt(true);
                modifiersEnabled = true;
            }
        }

        if (modifiersEnabled) {
            if (text.length() < 2 && TextUtilsCompat.getCharset("US-ASCII").newEncoder().canEncode(text)) {
                // Copied from https://stackoverflow.com/a/31625638
                KeyCharacterMap charMap;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                    charMap = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD);
                else
                    charMap = KeyCharacterMap.load(KeyCharacterMap.ALPHA);

                sendKeyUpDown(charMap.getEvents(new char[]{text.charAt(0)})[0].getKeyCode());
            } else {
                sendText(text);
            }
        } else {
            sendText(text);
        }

        if (!modifierDisabledForKeyboard) {
            if (getCtrlState() > 0) {
                sendCtrl(false);
            }

            if (getAltState() > 0) {
                sendAlt(false);
            }
        }
    }

    private void sendText(String text) {
        getCurrentIC().commitText(text, text.length());
        getCurrentIC().finishComposingText();
    }

    public int getCtrlState() {
        return ctrl;
    }

    public void setCtrlState(int state) {
        if (state >= 2 || state < 0) {
            state = 0;
        }

        ctrl = state;
    }

    private void setCtrlState() {
        setCtrlState((ctrl + 1) % 2);
    }

    public int getAltState() {
        return alt;
    }

    public void setAltState(int state) {
        if (state >= 2 || state < 0) {
            state = 0;
        }

        alt = state;
    }

    private void setAltState() {
        setAltState((alt + 1) % 2);
    }

    public int getShiftState() {
        return shift;
    }

    public String getCase(String character, boolean upper) {
        character = character.toLowerCase(loc);

        if(specialCases.containsKey(character)) {
            return upper ? specialCases.get(character) : character;
        }

        return upper ? character.toUpperCase(loc) : character;
    }

    public void setShiftState(int state) {
        if (state == shift) {
            return;
        }

        shift = state;

        ViewGroup k = getCurrentKeyboard();
        for (int i = 0; i < k.getChildCount(); i++) {
            Row r = getRow(selected, i);
            for (int g = 0; g < r.getChildCount(); g++) {
                Key t = (Key) r.getChildAt(g);
                if (!isKeyHasEvent(t) && t.getText() != null) {
                    String tText = t.getText().toString();
                    String sText = t.getSubText().toString();
                    t.setText(getCase(tText, state > 0));
                    t.setSubText(getCase(sText, state > 0));
                    t.setSelected(false);
                } else {
                    if (t.getTag(TAG_NP) != null) {
                        Pair<Integer, Boolean> values = t.getNormalPressEvent();
                        int keyEvent = values.first;
                        if (keyEvent == Keyboard.KEYCODE_SHIFT) {
                            t.changeState(state);
                        }
                    }
                }
            }
        }
    }

    private void setShiftState() {
        setShiftState((shift + 1) % 3);
    }

    public Locale getKeyboardLanguage() {
        return loc;
    }

    public void setKeyboardLanguage(String lang) {
        if (lang != null) {
            String[] la = lang.split("_");
            loc = la.length > 1 ? new Locale(la[0], la[1]) : new Locale(la[0].toLowerCase(), la[0].toUpperCase());
            // trigSystemSuggestions();
        }
    }

    public void setRepeating(boolean repeat) {
        isRepeat = repeat;
    }

    public void setShiftDetection(boolean detect) {
        shiftDetect = detect;
    }

    private void updateKeyState() {
        updateKeyState(curr);
    }

    public void updateKeyState(InputMethodService s) {
        setCtrlState(0);
        setAltState(0);

        if (isCurrentFNKeyboard() || isCurrentSymbolKeyboard()) {
            return;
        }

        EditorInfo ei = s.getCurrentInputEditorInfo();

        action = ei.imeOptions & (EditorInfo.IME_MASK_ACTION | EditorInfo.IME_FLAG_NO_ENTER_ACTION);

        switch (ei.inputType & InputType.TYPE_MASK_CLASS) {
            case InputType.TYPE_CLASS_NUMBER:
            case InputType.TYPE_CLASS_PHONE:
                setEnabledLayout(findNumberKeyboardIndex());
                break;
            default:
                setEnabledLayout(findNormalKeyboardIndex());
                if (getShiftState() != SHIFT_LOCKED) {
                    if (shiftDetect) {
                        int caps = ei.inputType != InputType.TYPE_NULL
                                ? s.getCurrentInputConnection().getCursorCapsMode(ei.inputType)
                                : 0;
                        setShiftState(caps == SHIFT_OFF ? SHIFT_OFF : SHIFT_ON);
                    } else setShiftState(0);
                }
                break;
        }

        Key k;
        switch (ei.inputType & InputType.TYPE_MASK_VARIATION) {
            case InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
            case InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS:
                k = findKeyByLabel(0, ",");
                if (k != null) {
                    k.setText("@");
                    // k.setSubText("→");
                }
                break;
            default:
                k = findKeyByLabel(0, "@");
                if (k != null) {
                    k.setText(",");
                    // k.setSubText("→");
                }
                break;
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        fixHeight();
    }

    public Key findKeyByLabel(int keyboard, String label) {
        ViewGroup k = getKeyboard(keyboard);
        for (int i = 0; i < k.getChildCount(); i++) {
            Row r = (Row) k.getChildAt(i);
            for (int g = 0; g < r.getChildCount(); g++) {
                Key t = (Key) r.getChildAt(g);
                if (t.getText() != null && t.getText().equals(label)) {
                    return t;
                }
            }
        }
        return null;
    }

    public Key findKey(int keyboard, int keyAction) {
        ViewGroup k = getKeyboard(keyboard);
        for (int i = 0; i < k.getChildCount(); i++) {
            Row r = (Row) k.getChildAt(i);
            for (int g = 0; g < r.getChildCount(); g++) {
                Key t = (Key) r.getChildAt(g);
                if ((t.getText() != null && t.getText().charAt(0) == keyAction) ||
                        (t.getTag(TAG_NP) != null && t.getNormalPressEvent().first == keyAction)) {
                    return t;
                }
            }
        }
        return null;
    }

    public void setRowPadding(int keyboardIndex, int rowIndex, int padding) {
        getRow(keyboardIndex, rowIndex).setPadding(padding, 0, padding, 0);
    }

    private boolean isHasLongPressEvent(View v) {
        return v != null && v.getTag(TAG_LP) != null;
    }

    public boolean isDisabledModifierForKeyboard(int keyboardIndex) {
        Object tag = getKeyboard(keyboardIndex).getTag(TAG_DISABLE_MODIFIER);
        return tag != null && (boolean) tag;
    }

    public void setDisableModifierForKeyboard(int keyboardIndex, boolean value) {
        getKeyboard(keyboardIndex).setTag(TAG_DISABLE_MODIFIER, value);
    }

    public void setPressEventForKey(int keyboardIndex, int rowIndex, int keyIndex, int keyCode) {
        setPressEventForKey(keyboardIndex, rowIndex, keyIndex, keyCode, true);
    }

    public void setPressEventForKey(int keyboardIndex, int rowIndex, int keyIndex, int keyCode, boolean isEvent) {
        setPressEventForKey(getKey(keyboardIndex, rowIndex, keyIndex), keyCode, isEvent);
    }

    public void setPressEventForKey(Key key, int keyCode, boolean isEvent) {
        key.setTag(TAG_NP, new Pair<>(keyCode, isEvent));
    }

    public void setLongPressEventForKey(int keyboardIndex, int rowIndex, int keyIndex, int keyCode) {
        setLongPressEventForKey(keyboardIndex, rowIndex, keyIndex, keyCode, true);
    }

    public void setLongPressEventForKey(int keyboardIndex, int rowIndex, int keyIndex, int keyCode, boolean isEvent) {
        setLongPressEventForKey(getKey(keyboardIndex, rowIndex, keyIndex), keyCode, isEvent);
    }

    public void setLongPressEventForKey(Key key, int keyCode, boolean isEvent) {
        key.setTag(TAG_LP, new Pair<>(keyCode, isEvent));
    }

    public void setDisablePopup(boolean val) {
        dpopup = val;
    }

    public void closeKeyboard() {
        getServiceContext().requestHideSelf(0);
    }

    public List<Integer> findKeyboardIndexes(KeyboardType type) {
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i).getTag() != null &&
                    getChildAt(i).getTag().equals(type)) {
                indexes.add(i);
            }
        }

        return indexes;
    }

    public int findKeyboardIndex(KeyboardType type) {
        for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i).getTag() != null &&
                    getChildAt(i).getTag().equals(type)) {
                return i;
            }
        }

        if (type != KeyboardType.TEXT) {
            Log.e(getClass().getSimpleName(),
                    String.format("No keyboard set for %s, falling back to normal keyboard ...", type));
            return findKeyboardIndex(KeyboardType.TEXT);
        }

        Log.e(getClass().getSimpleName(), "No normal keyboard set, crashing ...");
        throw new RuntimeException("You must set a normal keyboard for input");
    }

    public boolean isFNKeyboard(int keyboardIndex) {
        return getKeyboard(keyboardIndex).getTag() == KeyboardType.FN;
    }

    public boolean isSymbolKeyboard(int keyboardIndex) {
        return getKeyboard(keyboardIndex).getTag() == KeyboardType.SYMBOL;
    }

    public boolean isNumberKeyboard(int keyboardIndex) {
        return getKeyboard(keyboardIndex).getTag() == KeyboardType.NUMBER;
    }

    public boolean isNormalKeyboard(int keyboardIndex) {
        return getKeyboard(keyboardIndex).getTag() == KeyboardType.TEXT;
    }

    public boolean isCurrentFNKeyboard() {
        return getCurrentKeyboardType() == KeyboardType.FN;
    }

    public boolean isCurrentSymbolKeyboard() {
        return getCurrentKeyboardType() == KeyboardType.SYMBOL;
    }

    public boolean isCurrentNumberKeyboard() {
        return getCurrentKeyboardType() == KeyboardType.NUMBER;
    }

    public boolean isCurrentTextKeyboard() {
        return getCurrentKeyboardType() == KeyboardType.TEXT;
    }

    public KeyboardType getCurrentKeyboardType() {
        return (KeyboardType) getKeyboard(selected).getTag();
    }

    public int findSymbolKeyboardIndex() {
        return findKeyboardIndex(KeyboardType.SYMBOL);
    }

    public int findNormalKeyboardIndex() {
        return findKeyboardIndex(KeyboardType.TEXT);
    }

    public int findNumberKeyboardIndex() {
        return findKeyboardIndex(KeyboardType.NUMBER);
    }

    public void playSound(int event) {

    }

    public float getX() {
        if (Build.VERSION.SDK_INT >= 11) {
            return super.getX();
        }

        return getTranslationX() + getLeft();
    }

    public void setX(float x) {
        if (Build.VERSION.SDK_INT >= 11) {
            super.setX(x);
            return;
        }

        setTranslationX(x - getLeft());
    }

    public float getY() {
        if (Build.VERSION.SDK_INT >= 11) {
            return super.getY();
        }

        return getTranslationY() + getTop();
    }

    public void setY(float y) {
        if (Build.VERSION.SDK_INT >= 11) {
            super.setY(y);
            return;
        }

        setTranslationY(y - getTop());
    }

    public float getTranslationY() {
        if (Build.VERSION.SDK_INT >= 11) {
            super.getTranslationY();
        }

        return 0;
    }

    public void setTranslationY(float y) {
        if (Build.VERSION.SDK_INT >= 11) {
            super.setTranslationY(y);
        }
    }

    public float getTranslationX() {
        if (Build.VERSION.SDK_INT >= 11) {
            super.getTranslationX();
        }

        return 0;
    }

    public void setTranslationX(float x) {
        if (Build.VERSION.SDK_INT >= 11) {
            super.setTranslationX(x);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent m) {
        v.setSelected(m.getAction() != MotionEvent.ACTION_UP);

        switch (m.getAction()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_SCROLL:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
                v.setSelected(false);
                mHandler.removeMessages(3);
                break;
        }

        if (isKeyRepeat(v) || isHasPopup(v) || isHasLongPressEvent(v)) {
            if (isHasPopup(v) && dpopup) {
                normalPress(v, m);
                return true;
            }
            switch (m.getAction()) {
                case MotionEvent.ACTION_UP:
                    act = MotionEvent.ACTION_UP;
                    if (mHandler.hasMessages(1)) {
                        mHandler.removeMessages(1);
                        sendDefaultKeyboardEvent(v);
                    }
                    mHandler.removeAndSendEmptyMessage(3);
                    break;
                case MotionEvent.ACTION_DOWN:
                    act = MotionEvent.ACTION_DOWN;
                    mHandler.obtainAndSendMessageDelayed(1, v, 250L * mult);
                    onKeyboardEvent(v);
                    break;
            }
        } else {
            normalPress(v, m);
        }
        return true;
    }

    private void normalPress(View v, MotionEvent m) {
        switch (m.getAction()) {
            case MotionEvent.ACTION_UP:
                mHandler.removeAndSendEmptyMessage(3);
                break;
            case MotionEvent.ACTION_DOWN:
                sendDefaultKeyboardEvent(v);
                break;
        }
    }

    public enum KeyboardType {TEXT, SYMBOL, NUMBER, FN}

    public enum TextType {
        regular,
        bold,
        italic,
        bold_italic,
        condensed,
        condensed_bold,
        condensed_italic,
        condensed_bold_italic,
        serif,
        serif_bold,
        serif_italic,
        serif_bold_italic,
        monospace,
        monospace_bold,
        monospace_italic,
        monospace_bold_italic,
        serif_monospace,
        serif_monospace_bold,
        serif_monospace_italic,
        serif_monospace_bold_italic,
        custom
    }

    public static abstract class ApplyToKeyRunnable {
        public abstract void run(Key key);
    }

    @SuppressLint("HandlerLeak")
    private class MyHandler extends Handler {
        private MyHandler() {
            super(Looper.getMainLooper());
        }

        public void removeAndSendEmptyMessage(int msgId) {
            removeMessages(msgId);
            sendEmptyMessage(msgId);
        }

        public void removeAndSendMessage(Message msg) {
            removeMessages(msg.what);
            sendMessage(msg);
        }

        public void removeAndSendMessageDelayed(Message msg, long delay) {
            removeMessages(msg.what);
            sendMessageDelayed(msg, delay);
        }

        public void obtainAndSendMessage(int msgId, Object obj) {
            removeAndSendMessage(obtainMessage(msgId, obj));
        }

        public void obtainAndSendMessageDelayed(int msgId, View obj, long delay) {
            removeAndSendMessageDelayed(obtainMessage(msgId, obj), delay);
        }

        @Override
        public void handleMessage(Message msg) {
            View v = null;
            if (msg.obj instanceof View) {
                v = (View) msg.obj;
            }

            switch (msg.what) {
                case 0:
                    removeAndSendEmptyMessage(3);
                    break;
                case 1:
                    removeMessages(1);
                    switch (act) {
                        case MotionEvent.ACTION_UP:
                            removeAndSendEmptyMessage(3);
                            break;
                        case MotionEvent.ACTION_DOWN:
                            if (isHasPopup(v)) {
                                onPopupEvent();
                                removeAndSendEmptyMessage(3);
                            } else if (isHasLongPressEvent(v)) {
                                Pair<Integer, Boolean> a = ((Key) v).getLongPressEvent();
                                y = a.first;
                                if (a.second) {
                                    sendKeyEvent(y);
                                } else {
                                    commitText(String.valueOf((char) y));
                                }
                                playSound(y);
                                removeAndSendEmptyMessage(3);
                            } else {
                                if (!((InputMethodService) getContext()).isInputViewShown()) {
                                    act = MotionEvent.ACTION_UP;
                                }
                                obtainAndSendMessage(2, msg.obj);
                            }
                            break;
                    }
                    break;
                case 2:
                    if (act == MotionEvent.ACTION_UP) {
                        removeAndSendEmptyMessage(3);
                    } else {
                        sendDefaultKeyboardEvent(v);
                        if (isRepeat) {
                            Message n = obtainMessage(1, msg.obj);
                            sendMessageDelayed(n, ((long) (mult > 1 ? 15 : 20) * mult) * (lng ? 1 : 20));
                            if (!lng) lng = true;
                        } else {
                            removeAndSendEmptyMessage(3);
                        }
                    }
                    break;
                case 3:
                    lng = false;
                    removeMessages(3);
                    afterKeyboardEvent();
                    break;
                case 5:
                    setEnabled(false);
                    break;
                case 6:
                    setEnabled(true);
                    break;
            }
        }
    }

    public class Row extends LinearLayout {

        public Row(Context c) {
            super(c);
            setLayoutParams(new LinearLayout.LayoutParams(-1, -1, 1));
        }

        void addKey(Key k) {
            if (k != null) {
                addView(k);
            } else {
                throw new NullPointerException("Key is not be null");
            }
        }

        @SuppressLint("ResourceType")
        void setKeyWidths() {
            for (int i = 0; i < getChildCount(); i++) {
                Key k = (Key) getChildAt(i);
                if (k.getId() < 1)
                    k.setId(100 / getChildCount());
                k.getLayoutParams().width = DensityUtils.wpInt(k.getId());
            }
        }
    }

    public class Key extends RelativeLayout {

        protected int shr = 0, shc = 0, txtst = 0;
        private final TextView label, subLabel;
        private final ImageView icon;
        private View state;
        private int stateCount = 1, currentState = 0;

        protected Key(Context context) {
            super(context);
            setLayoutParams(new LinearLayout.LayoutParams(-1, -1, 1));
            label = new TextView(context);
            label.setLayoutParams(new LayoutParams(-1, -1));
            subLabel = new TextView(context);
            LayoutParams subParams = new LayoutParams(-2, -2);
            subParams.addRule(ALIGN_PARENT_RIGHT, TRUE);
            subParams.addRule(ALIGN_PARENT_TOP, TRUE);
            int margin = DensityUtils.mpInt(1.5f);
            subParams.rightMargin = subParams.topMargin = margin;
            subLabel.setLayoutParams(subParams);
            icon = new ImageView(context);
            LayoutParams iconParams = new LayoutParams(-1, -1);
            iconParams.addRule(CENTER_IN_PARENT, TRUE);
            icon.setLayoutParams(iconParams);
            addView(label);
            addView(subLabel);
            addView(icon);
            icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            setKeyImageVisible(false);
            label.setTextColor(keyclr != -1 ? keyclr : (keyclr = 0xFFDEDEDE));
            label.setSingleLine();
            label.setGravity(CENTER);
            label.setHintTextColor(0);
            setKeyShadow(shrad, shclr != -1 ? shclr : (shclr = keyclr));
            setKeyTextSize(txtsze != 1 ? txtsze : (txtsze = DensityUtils.mp(1.25f)));
            setBackground(keybg);
            setKeyTextStyle(txts);
            setKeyItemColor(txtclr);
            setOnTouchListener(SuperBoard.this);
        }

        public boolean isKeyIconSet() {
            return icon.getDrawable() != null;
        }

        protected int getTextColor() {
            return keyclr;
        }

        protected float getTextSize() {
            return getTextView().getTextSize();
        }

        public boolean hasNormalPressEvent() {
            return getTag(TAG_NP) != null;
        }

        public boolean hasLongPressEvent() {
            return getTag(TAG_LP) != null;
        }

        @SuppressWarnings("unchecked")
        public Pair<Integer, Boolean> getNormalPressEvent() {
            return (Pair<Integer, Boolean>) getTag(TAG_NP);
        }

        @SuppressWarnings("unchecked")
        public Pair<Integer, Boolean> getLongPressEvent() {
            return (Pair<Integer, Boolean>) getTag(TAG_LP);
        }

        public void setStateCount(int stateCount) {
            if (stateCount < 1) {
                return;
            }

            if (state == null) {
                RelativeLayout.LayoutParams stateParams =
                        new RelativeLayout.LayoutParams(
                                DensityUtils.mpInt(4), DensityUtils.mpInt(1));
                stateParams.bottomMargin = DensityUtils.mpInt(2);
                stateParams.addRule(ALIGN_PARENT_BOTTOM, TRUE);
                stateParams.addRule(CENTER_HORIZONTAL, TRUE);
                state = new View(getContext());
                state.setLayoutParams(stateParams);
                GradientDrawable stateDrawable = new GradientDrawable();
                stateDrawable.setColor(txtclr);
                stateDrawable.setAlpha(0);
                stateDrawable.setCornerRadius(DensityUtils.dpInt(16));
                state.setBackgroundDrawable(stateDrawable);
                addView(state);
            }

            this.stateCount = stateCount;
            if (currentState >= stateCount) {
                currentState = stateCount - 1;
            }
            changeState(currentState);
        }

        public void changeState(int newState) {
            if (state == null) {
                return;
            }

            if (newState >= stateCount) {
                return;
            }

            currentState = newState;

            GradientDrawable gradientDrawable = (GradientDrawable) state.getBackground();
            gradientDrawable.setAlpha((int) (255 * (float) currentState / Math.max(1, stateCount - 1)));
            gradientDrawable.setColor(txtclr);
        }

        public void setBackground(Drawable b) {
            setBackgroundDrawable(b);
        }

        @Override
        public void setBackgroundDrawable(Drawable b) {
            super.setBackgroundDrawable(b == null ? null : Objects.requireNonNull(b.getConstantState()).newDrawable());
        }

        public void setKeyItemColor(int color) {
            label.setTextColor(txtclr = color);
            subLabel.setTextColor(ColorUtils.convertARGBtoRGB(color) - 0x66000000);
            if (isKeyIconSet()) {
                getKeyIcon().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            }
            changeState(currentState);
        }

        public CharSequence getText() {
            return label.getText();
        }

        public void setText(CharSequence text) {
            setKeyImageVisible(false);
            label.setText(text);
        }

        public CharSequence getSubText() {
            return subLabel.getText();
        }

        public void setSubText(CharSequence text) {
            setKeyImageVisible(false);
            subLabel.setText(text);
        }

        public Drawable getKeyIcon() {
            return icon.getDrawable();
        }

        public void setKeyIcon(Drawable dr) {
            setKeyImageVisible(true);
            icon.setImageDrawable(dr);
            setKeyItemColor(txtclr);
        }

        public void setKeyIcon(int iconRes) {
            setKeyIcon(getContext().getResources().getDrawable(iconRes));
        }

        protected CharSequence getHint() {
            return label.getHint();
        }

        protected void setHint(CharSequence cs) {
            label.setHint(cs);
        }

        public void setKeyImageVisible(boolean visible) {
            icon.setVisibility(visible ? VISIBLE : GONE);
            label.setVisibility(visible ? GONE : VISIBLE);
            subLabel.setVisibility(ppreview && !visible ? VISIBLE : GONE);
        }

        public void setKeyTextSize(float size) {
            label.setTextSize(txtsze = size);
            subLabel.setTextSize(label.getTextSize() / 3);
            applyIconMultiply();
        }

        public void applyIconMultiply() {
            ViewGroup.LayoutParams vp = icon.getLayoutParams();
            vp.width = -1;
            vp.height = (int) (txtsze * iconmulti);
        }

        public void setKeyShadow(int radius, int color) {
            label.setShadowLayer(shr = radius, 0, 0, shc = color);
        }

        public void setKeyTextStyle(int style) {
            TextType[] arr = TextType.values();
            setKeyTextStyle(arr[(arr.length - 1) < style ? 0 : style]);
            txtst = style;
        }

        public void setKeyTextStyle(TextType style) {
            setTypefaceFromTextType(label, style, cFont);
            subLabel.setTypeface(label.getTypeface());
        }

        protected TextView getTextView() {
            return label;
        }

        protected TextView getSubTextView() {
            return subLabel;
        }

        protected ImageView getImageView() {
            return icon;
        }

        @Override
        public Key clone() {
            return clone(false);
        }

        public Key clone(boolean disableTouchEvent) {
            return clone(new Key(getContext()), disableTouchEvent);
        }

        public void clone(Key k) {
            clone(k, false);
        }

        @SuppressLint("ClickableViewAccessibility")
        public Key clone(Key k, boolean disableTouchEvent) {
            k.setBackgroundDrawable(getBackground());
            Rect r = getBackground().getBounds();
            k.getLayoutParams().width = r.right;
            k.getLayoutParams().height = r.bottom;
            k.setHint(getHint());
            k.setKeyShadow(shr, shc);
            k.setKeyItemColor(keyclr);
            k.getTextView().setSingleLine();
            k.getSubTextView().setSingleLine();
            k.setId(getId());
            k.setKeyTextSize(label.getTextSize() / 2.5f);
            k.setKeyTextStyle(txts);
            if (disableTouchEvent) k.setOnTouchListener(null);
            if (isKeyIconSet()) {
                k.setKeyIcon(getKeyIcon());
            } else {
                k.setText(getText());
                k.setSubText(getSubText());
            }
            return k;
        }

        public float getX() {
            if (Build.VERSION.SDK_INT >= 11) {
                return super.getX();
            }

            return getTranslationX() + getLeft();
        }

        public void setX(float x) {
            if (Build.VERSION.SDK_INT >= 11) {
                super.setX(x);
                return;
            }

            setTranslationX(x - getLeft());
        }

        public float getY() {
            if (Build.VERSION.SDK_INT >= 11) {
                return super.getY();
            }

            return getTranslationY() + getTop();
        }

        public void setY(float y) {
            if (Build.VERSION.SDK_INT >= 11) {
                super.setY(y);
                return;
            }

            setTranslationY(y - getTop());
        }

        public float getTranslationY() {
            if (Build.VERSION.SDK_INT >= 11) {
                super.getTranslationY();
            }

            return 0;
        }

        public void setTranslationY(float y) {
            if (Build.VERSION.SDK_INT >= 11) {
                super.setTranslationY(y);
            }
        }

        public float getTranslationX() {
            if (Build.VERSION.SDK_INT >= 11) {
                super.getTranslationX();
            }

            return 0;
        }

        public void setTranslationX(float x) {
            if (Build.VERSION.SDK_INT >= 11) {
                super.setTranslationX(x);
            }
        }
    }
}
