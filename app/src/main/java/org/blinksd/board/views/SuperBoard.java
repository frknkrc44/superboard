package org.blinksd.board.views;

import static android.media.AudioManager.FX_KEYPRESS_DELETE;
import static android.media.AudioManager.FX_KEYPRESS_RETURN;
import static android.media.AudioManager.FX_KEYPRESS_SPACEBAR;
import static android.media.AudioManager.FX_KEYPRESS_STANDARD;
import static android.view.Gravity.CENTER;
import static android.view.View.OnTouchListener;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.media.AudioManager;
import android.os.Build;
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
import org.blinksd.board.SuperBoardApplication;
import org.blinksd.utils.ColorUtils;
import org.blinksd.utils.DensityUtils;
import org.blinksd.utils.ListedMap;
import org.blinksd.utils.TextUtilsCompat;
import org.blinksd.utils.superboard.KeyboardType;
import org.blinksd.utils.superboard.TextType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@SuppressWarnings("deprecation")
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
    private static Locale caseLocale = new Locale("tr", "TR");
    private final MyHandler mHandler = new MyHandler();
    private final Vibrator vibrator;
    private float textSize = DensityUtils.mp(1.25f);
    protected Drawable keyBackground = null;
    private int selected = 0;
    private int heightPercent = 40;
    private int widthPercent = 100;
    protected int shadowRadius = 0;
    private int keyTextColor = 0xFFDEDEDE;
    protected int shadowColor = keyTextColor;
    protected int textStyle = 0;
    private int vibrateDuration = 0;
    private int longPressMultiplier = 1;
    private int currentMotionEventAction = MotionEvent.ACTION_UP;
    protected int iconSizeMultiplier = 1;
    private int currentEditorAction = 0;
    private boolean longPressed = false;
    private boolean disablePopup = false;
    private boolean popupPreview = false;
    private boolean isRepeat = true;
    private boolean shiftDetect = true;
    private final ListedMap<String, String> specialCases = new ListedMap<>();

    // key states
    private int ctrl = 0;
    private int alt = 0;
    private int shift = 0;

    public SuperBoard(Context context) {
        super(context);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        else {
            VibratorManager vm = (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            vibrator = vm.getDefaultVibrator();
        }

        setLayoutParams(new LayoutParams(-1, -1));
        createEmptyLayout();
        setKeyboardHeight(heightPercent);
    }

    /** @noinspection EmptyMethod*/
    public final void beforeKeyboardEvent() {}

    public void onKeyboardEvent(View view) {}

    public void afterKeyboardEvent() {}

    public void onPopupEvent() {}

    public void afterPopupEvent() {
        mHandler.removeAndSendEmptyMessage(0);
    }

    public void switchLanguage() {}

    public void openEmojiLayout() {}

    public final void setPadding(int p) {
        setPadding(p, p, p, p);
    }

    public final void setSpecialCases(Map<String, String> items) {
        specialCases.clear();
        specialCases.putAll(items);
    }

    public final int getKeyboardHeight() {
        return getLayoutParams().height;
    }

    public final void setKeyboardHeight(int percent) {
        heightPercent = percent;
        getLayoutParams().height = DensityUtils.hpInt(percent);
        if (getChildCount() > 0) {
            for (int i = 0; i < getChildCount(); i++) {
                getChildAt(i).getLayoutParams().height = getLayoutParams().height;
            }
        }

        int x = selected;
        setEnabledLayout(findNumberKeyboardIndex());
        setEnabledLayout(x);
    }

    public final int getKeyboardHeightPercent() {
        return heightPercent;
    }

    public final int getKeyboardWidthPercent() {
        return widthPercent;
    }

    public final void fixHeight() {
        setKeyboardHeight(heightPercent);
        for (int i = 0; i < getChildCount(); i++) {
            for (int g = 0; g < getKeyboard(i).getChildCount(); g++) {
                getRow(i, g).setKeyWidths();
            }
        }
    }

    public final void setBackground(Drawable background) {
        setBackgroundDrawable(background);
    }

    public final void setBackgroundDrawable(Drawable background) {
        super.setBackgroundDrawable(Objects.requireNonNull(background.getConstantState()).newDrawable());
    }

    public final void setKeyVibrateDuration(int dur) {
        vibrateDuration = dur;
    }

    public void clear() {
        for (int i = 0; i < getChildCount(); i++) {
            ViewGroup k = getKeyboard(i);
            for (int g = 0; g < k.getChildCount(); g++)
                getRow(i, g).removeAllViewsInLayout();
            k.removeAllViewsInLayout();
        }
        removeAllViewsInLayout();
        createEmptyLayout();
    }

    public final void setKeyBackground(int keyboardIndex, int rowIndex, int keyIndex, Drawable background) {
        getKey(keyboardIndex, rowIndex, keyIndex).setBackground(background);
    }

    public final void setKeyRepeat(int keyboardIndex, int rowIndex, int keyIndex) {
        setKeyRepeat(keyboardIndex, rowIndex, keyIndex, true);
    }

    public final void setKeyRepeat(int keyboardIndex, int rowIndex, int keyIndex, boolean repeat) {
        setKeyRepeat(getKey(keyboardIndex, rowIndex, keyIndex), repeat);
    }

    public final void setKeyRepeat(SuperBoard.Key key, boolean repeat) {
        key.setTag(TAG_KEY_REPEAT, repeat);
    }

    public final boolean isKeyHasEvent(Key k) {
        return isKeyRepeat(k) || k.hasLongPressEvent() || k.hasNormalPressEvent();
    }

    private boolean isKeyRepeat(View v) {
        if (!isRepeat) return false;
        Object tag = v.getTag(TAG_KEY_REPEAT);
        return tag != null && (boolean) tag;
    }

    public final void setKeyWidthPercent(int keyboardIndex, int rowIndex, int keyIndex, int percent) {
        Key k = getKey(keyboardIndex, rowIndex, keyIndex);
        k.getLayoutParams().width = DensityUtils.wpInt(percent);
        k.setId(percent);
    }

    public final void setLongPressMultiplier(int multi) {
        longPressMultiplier = multi;
    }

    public final void setIconSizeMultiplier(int multi) {
        iconSizeMultiplier = multi;
        applyIconMultiply();
    }

    private boolean isHasPopup(View v) {
        CharSequence cs = ((Key) v).getSubText();
        return (cs != null && cs.length() > 0) && !isKeyRepeat(v);
    }

    public final void setPopupForKey(int keyboardIndex, int rowIndex, int keyIndex, String chars) {
        Key key = getKey(keyboardIndex, rowIndex, keyIndex);
        Set<String> newSet = new LinkedHashSet<>(Arrays.asList(chars.split("")));
        key.setPopupCharacters(newSet.toArray(new String[0]));
    }

    public final void setLayoutPopup(int keyboardIndex, String[][] chars) {
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

    public final void setKeysPadding(final int padding) {
        if (getReferenceKeyMargin() != padding)
            applyToAllKeys(key -> {
                Row.LayoutParams l = (Row.LayoutParams) key.getLayoutParams();
                l.bottomMargin = l.topMargin = l.leftMargin = l.rightMargin = padding;
            });
    }

    private int getReferenceKeyMargin() {
        try {
            Key key = getKey(0, 0, 0);
            Row.LayoutParams l = (Row.LayoutParams) key.getLayoutParams();
            return l.bottomMargin;
        } catch (Throwable ignored) {}
        return -1;
    }

    public final int getKeysTextColor() {
        return keyTextColor;
    }

    public final void setKeysTextColor(final int color) {
        if (keyTextColor != color)
            applyToAllKeys(key -> key.setKeyItemColor(color));
        keyTextColor = color;
    }

    private void applyIconMultiply() {
        applyToAllKeys(Key::applyIconMultiply);
    }

    protected final float getKeysTextSize() {
        return textSize;
    }

    public final void setKeysTextSize(final int size) {
        setKeysTextSize(size, false);
    }

    public final void setKeysTextSize(final int size, boolean force) {
        if (textSize != size || force)
            applyToAllKeys(key -> key.setKeyTextSize(size));
        textSize = size;
    }

    public final void setKeysBackground(final Drawable d) {
        if (keyBackground != d)
            applyToAllKeys(key -> key.setBackground(d));
        keyBackground = d;
    }

    public final void setKeysShadow(final int radius, final int color) {
        if (shadowRadius != radius || shadowColor != color)
            applyToAllKeys(key -> key.setKeyShadow(radius, color));
        shadowRadius = radius;
        shadowColor = color;
    }

    public final void setKeysPopupPreviewEnabled(final boolean enabled) {
        if (enabled != popupPreview)
            applyToAllKeys(key -> {
                popupPreview = enabled;
                key.setKeyImageVisible(key.isKeyIconSet());
            });
    }

    public final void setKeysTextType(final int style) {
        if (textStyle != style)
            applyToAllKeys(key -> key.setKeyTextStyle(style));
        textStyle = style;
    }

    public final void applyToAllKeys(ApplyToKeyRunnable runnable) {
        for (int j = 0; j < getChildCount(); j++) {
            for (int i = 0; i < getKeyboard(j).getChildCount(); i++) {
                for (int g = 0; g < getRow(j, i).getChildCount(); g++) {
                    runnable.run(getKey(j, i, g));
                }
            }
        }
    }

    public final void setKeyboardWidth(int percent) {
        widthPercent = percent;
        getLayoutParams().width = DensityUtils.wpInt(percent);
        if (getChildCount() > 0) {
            for (int i = 0; i < getChildCount(); i++) {
                getChildAt(i).getLayoutParams().width = getLayoutParams().width;
            }
        }
        int x = selected;
        setEnabledLayout(findNumberKeyboardIndex());
        setEnabledLayout(x);
    }

    public final void setKeyDrawable(int keyboardIndex, int rowIndex, int keyIndex, int resId) {
        setKeyDrawable(keyboardIndex, rowIndex, keyIndex, getResources().getDrawable(resId));
    }

    public final void setKeyDrawable(int keyboardIndex, int rowIndex, int keyIndex, Drawable d) {
        d.setColorFilter(keyTextColor, PorterDuff.Mode.SRC_ATOP);
        Key t = getKey(keyboardIndex, rowIndex, keyIndex);
        ((LinearLayout.LayoutParams) t.getLayoutParams()).gravity = CENTER;
        t.setKeyIcon(d);
    }

    public int getEnabledLayoutIndex() {
        return selected;
    }

    public final void setEnabledLayout(KeyboardType type) {
        setEnabledLayout(findKeyboardIndex(type));
    }

    public final void setEnabledLayout(int keyboardIndex) {
        if (keyboardIndex < 0) keyboardIndex += getChildCount();
        if (keyboardIndex < getChildCount() && keyboardIndex >= 0) {
            if (getChildCount() == 1 || keyboardIndex == selected) return;
            getChildAt(selected).setVisibility(GONE);
            selected = keyboardIndex;
            getChildAt(selected).setVisibility(VISIBLE);
        } else throw new RuntimeException("Invalid keyboard index number");
    }

    public final void resetToNormalLayout() {
        setEnabledLayout(KeyboardType.TEXT);
    }

    public final void setLayoutType(int keyboardIndex, KeyboardType type) {
        getKeyboard(keyboardIndex).setTag(type);
    }

    public final void createLayoutWithRows(String[][] keys, KeyboardType type) {
        createEmptyLayout(type);
        addRows(getChildCount() - 1, keys);
    }

    public final void createEmptyLayout() {
        createEmptyLayout(KeyboardType.TEXT);
    }

    public final void createEmptyLayout(KeyboardType type) {
        LinearLayout ll = new LinearLayout(getContext());
        ll.setFocusable(false);
        ll.setLayoutParams(new LayoutParams(-1, getLayoutParams().height));
        ll.setOrientation(LinearLayout.VERTICAL);
        addView(ll);
        setLayoutType(getChildCount() - 1, type);
        if (getChildCount() != 1) {
            ll.setVisibility(GONE);
        }
    }

    public final ViewGroup getKeyboard(int keyboardIndex) {
        if (keyboardIndex < 0) keyboardIndex += getChildCount();
        return (ViewGroup) getChildAt(keyboardIndex);
    }

    public final void replaceTextKeyboard(String[][] newKeyboard) {
        ViewGroup vg = getKeyboard(findTextKeyboardIndex());
        vg.removeAllViewsInLayout();
        addRows(findTextKeyboardIndex(), newKeyboard);
    }

    public final Row getRow(int keyboardIndex, int rowIndex) {
        if (rowIndex < 0) rowIndex += getKeyboard(keyboardIndex).getChildCount();
        return (Row) getKeyboard(keyboardIndex).getChildAt(rowIndex);
    }

    public final Key getKey(int keyboardIndex, int rowIndex, int keyIndex) {
        if (keyIndex < 0) keyIndex += getRow(keyboardIndex, rowIndex).getChildCount();
        return (Key) getRow(keyboardIndex, rowIndex).getChildAt(keyIndex);
    }

    public void addRows(int keyboardIndex, CharSequence[][] keys) {
        if (keys != null) {
            for (CharSequence[] key : keys) {
                addRow(keyboardIndex, key);
            }
        }
    }

    public final Key createKey(String key) {
        Key k = new Key(getContext());
        k.setText(key);
        return k;
    }

    public final void addRow(int keyboardIndex, CharSequence[] keys) {
        addRow(keyboardIndex, null, keys);
    }

    public final void addRow(int keyboardIndex, Key template, CharSequence[] keys) {
        Row r = new Row(getContext());
        if (keys.length > 0) {
            for (CharSequence key : keys) {
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
        beforeKeyboardEvent();

        if (v.hasNormalPressEvent()) {
            Pair<Integer, Boolean> currentKey = v.getNormalPressEvent();
            switch (currentKey.first) {
                case KEYCODE_TOGGLE_CTRL:
                    setCtrlState();
                    break;
                case KEYCODE_TOGGLE_ALT:
                    setAltState();
                    break;
                case Keyboard.KEYCODE_SHIFT:
                    toggleShiftState();
                    break;
                case Keyboard.KEYCODE_CANCEL:
                    setEnabledLayout((selected - 1) >= 0 ? selected - 1 : findSymbolKeyboardIndex());
                    break;
                case Keyboard.KEYCODE_MODE_CHANGE:
                    setEnabledLayout(
                            isCurrentTextKeyboard()
                                    ? findSymbolKeyboardIndex()
                                    : findTextKeyboardIndex()
                    );
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
                    if (!performEditorAction()) {
                        sendKeyEvent(KeyEvent.KEYCODE_ENTER);
                    }
                    break;
                default:
                    if (currentKey.second) {
                        sendKeyEvent(currentKey.first);
                    } else {
                        commitText(String.valueOf((char) currentKey.first.intValue()));
                    }

                    updateKeyState();
                    break;
            }
            playSound(currentKey.first);
        } else {
            commitText(v.getText().toString());
            updateKeyState();

            playSound(0);
        }

        vibrate();
        onKeyboardEvent(v);
    }

    public final void vibrate() {
        vibrateInternal(vibrateDuration);
    }

    private void vibrateInternal(int duration) {
        if (duration > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(duration, 255));
            } else {
                vibrator.vibrate(duration);
            }
        }
    }

    /**
     * Use fake keyboard event instead of real one
     *
     * @param v - Input key for play sound
     */
    public final void fakeKeyboardEvent(Key v) {
        if (v.hasNormalPressEvent()) {
            playSound(v.getNormalPressEvent().first);
            return;
        }
        playSound(0);
        vibrate();
    }

    private InputMethodService getCurrentIMService() {
        return ((InputMethodService) getContext());
    }

    public InputConnection getCurrentInputConnection() {
        return getCurrentIMService().getCurrentInputConnection();
    }

    @SuppressLint("InlinedApi")
    public final void sendKeyEvent(int code) {
        switch (code) {
            case KEYCODE_CLOSE_KEYBOARD:
                getCurrentIMService().requestHideSelf(InputMethodService.BACK_DISPOSITION_DEFAULT);
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
        getCurrentInputConnection().sendKeyEvent(event);
    }

    private boolean performEditorAction() {
        boolean performedAction = false;

        if (currentEditorAction > EditorInfo.IME_ACTION_NONE &&
                currentEditorAction <= EditorInfo.IME_ACTION_PREVIOUS) {
            performedAction = getCurrentInputConnection().performEditorAction(currentEditorAction);
        }

        currentEditorAction = 0;
        return performedAction;
    }

    public final void commitText(CharSequence text) {
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

    private void sendText(CharSequence text) {
        getCurrentInputConnection().commitText(text, text.length());
        getCurrentInputConnection().finishComposingText();
    }

    public int getCtrlState() {
        return ctrl;
    }

    public final void setCtrlState(int state) {
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

    public final void setAltState(int state) {
        if (state >= 2 || state < 0) {
            state = 0;
        }

        alt = state;
    }

    private void setAltState() {
        setAltState((alt + 1) % 2);
    }

    public final int getShiftState() {
        return shift;
    }

    private boolean isUpperCase(String str) {
        for (char chr : str.toCharArray()) {
            if (!Character.isUpperCase(chr)) {
                return false;
            }
        }

        return true;
    }

    private boolean isLowerCase(String str) {
        for (char chr : str.toCharArray()) {
            if (!Character.isLowerCase(chr)) {
                return false;
            }
        }

        return true;
    }

    protected final CharSequence getCase(CharSequence character, boolean upper) {
        if (character == null) {
            return character;
        }

        String chrStr = character.toString();

        if (upper && isUpperCase(chrStr)) {
            return chrStr;
        }

        if (!upper && isLowerCase(chrStr)) {
            return chrStr;
        }

        if (!upper && isUpperCase(chrStr)) {
            chrStr = chrStr.toLowerCase(caseLocale);
        }

        if(upper && specialCases.containsKey(chrStr)) {
            return specialCases.get(chrStr);
        }

        if (!upper && specialCases.containsValue(chrStr)) {
            return specialCases.getKeyByValue(chrStr);
        }

        return upper ? chrStr.toUpperCase(caseLocale) : chrStr;
    }

    public final void setShiftState(int state) {
        if (state == shift) {
            return;
        }

        shift = state;

        applyToAllKeys(t -> {
            if (state != SHIFT_LOCKED && !t.isKeyIconSet() && !isKeyHasEvent(t)) {
                t.fixCase();
                t.setSelected(false);
            } else if (t.hasNormalPressEvent()) {
                Pair<Integer, Boolean> values = t.getNormalPressEvent();
                if (values.first == Keyboard.KEYCODE_SHIFT) {
                    t.changeState(state);
                }
            }
        });
    }

    private void toggleShiftState() {
        setShiftState((shift + 1) % 3);
    }

    public final Locale getKeyboardLanguage() {
        return caseLocale;
    }

    public final void setKeyboardLanguage(String lang) {
        if (lang != null) {
            String[] la = lang.split("_");
            caseLocale = la.length > 1 ? new Locale(la[0], la[1]) : new Locale(la[0].toLowerCase(), la[0].toUpperCase());
            // trigSystemSuggestions();
        }
    }

    public final void setRepeating(boolean repeat) {
        isRepeat = repeat;
    }

    public final void setShiftDetection(boolean detect) {
        shiftDetect = detect;
    }

    public final void updateKeyState() {
        setCtrlState(0);
        setAltState(0);

        if (isCurrentFNKeyboard() || isCurrentSymbolKeyboard()) {
            return;
        }

        EditorInfo ei = getCurrentIMService().getCurrentInputEditorInfo();

        currentEditorAction = ei.imeOptions & (EditorInfo.IME_MASK_ACTION | EditorInfo.IME_FLAG_NO_ENTER_ACTION);

        switch (ei.inputType & InputType.TYPE_MASK_CLASS) {
            case InputType.TYPE_CLASS_NUMBER:
            case InputType.TYPE_CLASS_PHONE:
            case InputType.TYPE_CLASS_DATETIME:
                if (!isCurrentNumberKeyboard()) {
                    setEnabledLayout(findNumberKeyboardIndex());
                }
                break;
            default:
                if (!isCurrentTextKeyboard()) {
                    setEnabledLayout(findTextKeyboardIndex());
                }

                if (getShiftState() != SHIFT_LOCKED) {
                    if (shiftDetect) {
                        int caps = ei.inputType != InputType.TYPE_NULL
                                ? getCurrentInputConnection().getCursorCapsMode(ei.inputType)
                                : 0;
                        setShiftState(caps == SHIFT_OFF ? SHIFT_OFF : SHIFT_ON);
                    } else setShiftState(SHIFT_OFF);
                }
                break;
        }
    }

    @Override
    protected final void onConfigurationChanged(Configuration newConfig) {
        fixHeight();
    }

    public final void setRowPadding(int keyboardIndex, int rowIndex, int padding) {
        getRow(keyboardIndex, rowIndex).setPadding(padding, 0, padding, 0);
    }

    private boolean isHasLongPressEvent(View v) {
        return v != null && v.getTag(TAG_LP) != null;
    }

    public final boolean isDisabledModifierForKeyboard(int keyboardIndex) {
        Object tag = getKeyboard(keyboardIndex).getTag(TAG_DISABLE_MODIFIER);
        return tag != null && (boolean) tag;
    }

    public final void setDisableModifierForKeyboard(int keyboardIndex, boolean value) {
        getKeyboard(keyboardIndex).setTag(TAG_DISABLE_MODIFIER, value);
    }

    public final void setPressEventForKey(int keyboardIndex, int rowIndex, int keyIndex, int keyCode) {
        setPressEventForKey(keyboardIndex, rowIndex, keyIndex, keyCode, true);
    }

    public final void setPressEventForKey(int keyboardIndex, int rowIndex, int keyIndex, int keyCode, boolean isEvent) {
        setPressEventForKey(getKey(keyboardIndex, rowIndex, keyIndex), keyCode, isEvent);
    }

    public final void setPressEventForKey(Key key, int keyCode, boolean isEvent) {
        key.setTag(TAG_NP, new Pair<>(keyCode, isEvent));
    }

    public final void setLongPressEventForKey(int keyboardIndex, int rowIndex, int keyIndex, int keyCode) {
        setLongPressEventForKey(keyboardIndex, rowIndex, keyIndex, keyCode, true);
    }

    public final void setLongPressEventForKey(int keyboardIndex, int rowIndex, int keyIndex, int keyCode, boolean isEvent) {
        setLongPressEventForKey(getKey(keyboardIndex, rowIndex, keyIndex), keyCode, isEvent);
    }

    public final void setLongPressEventForKey(Key key, int keyCode, boolean isEvent) {
        key.setTag(TAG_LP, new Pair<>(keyCode, isEvent));
    }

    public final void setDisablePopup(boolean val) {
        disablePopup = val;
    }

    public final List<Integer> findKeyboardIndexes(KeyboardType type) {
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i).getTag() != null &&
                    getChildAt(i).getTag().equals(type)) {
                indexes.add(i);
            }
        }

        return indexes;
    }

    public final int findKeyboardIndex(KeyboardType type) {
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

    public final boolean isCurrentFNKeyboard() {
        return getCurrentKeyboardType() == KeyboardType.FN;
    }

    public final boolean isCurrentSymbolKeyboard() {
        return getCurrentKeyboardType() == KeyboardType.SYMBOL;
    }

    public final boolean isCurrentNumberKeyboard() {
        return getCurrentKeyboardType() == KeyboardType.NUMBER;
    }

    public final boolean isCurrentTextKeyboard() {
        return getCurrentKeyboardType() == KeyboardType.TEXT;
    }

    public final KeyboardType getCurrentKeyboardType() {
        return (KeyboardType) getKeyboard(selected).getTag();
    }

    public final int findFNKeyboardIndex() {
        return findKeyboardIndex(KeyboardType.FN);
    }

    public final int findSymbolKeyboardIndex() {
        return findKeyboardIndex(KeyboardType.SYMBOL);
    }

    public final int findNumberKeyboardIndex() {
        return findKeyboardIndex(KeyboardType.NUMBER);
    }

    public final int findTextKeyboardIndex() {
        return findKeyboardIndex(KeyboardType.TEXT);
    }

    public void playSound(int event) {
        AudioManager audMgr = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        switch (event) {
            case Keyboard.KEYCODE_DONE:
                audMgr.playSoundEffect(FX_KEYPRESS_RETURN);
                break;
            case Keyboard.KEYCODE_DELETE:
                audMgr.playSoundEffect(FX_KEYPRESS_DELETE);
                break;
            case KeyEvent.KEYCODE_SPACE:
                audMgr.playSoundEffect(FX_KEYPRESS_SPACEBAR);
                break;
            default:
                audMgr.playSoundEffect(FX_KEYPRESS_STANDARD);
                break;
        }
    }

    @Override
    public final boolean onTouch(View v, MotionEvent m) {
        v.setSelected(m.getAction() != MotionEvent.ACTION_UP);

        switch (m.getAction()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_SCROLL:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
                v.setSelected(false);
                mHandler.removeMessages(0);
                break;
        }

        if (isKeyRepeat(v) || isHasPopup(v) || isHasLongPressEvent(v)) {
            if (isHasPopup(v) && disablePopup) {
                normalPress(v, m);
                return true;
            }
            switch (m.getAction()) {
                case MotionEvent.ACTION_UP:
                    currentMotionEventAction = MotionEvent.ACTION_UP;
                    if (mHandler.hasMessages(1)) {
                        mHandler.removeMessages(1);
                        sendDefaultKeyboardEvent(v);
                    }
                    mHandler.removeAndSendEmptyMessage(0);
                    break;
                case MotionEvent.ACTION_DOWN:
                    currentMotionEventAction = MotionEvent.ACTION_DOWN;
                    onKeyboardEvent(v);
                    mHandler.removeAndSendMessageDelayed(1, v, 250L * longPressMultiplier);
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
                mHandler.removeAndSendEmptyMessage(0);
                break;
            case MotionEvent.ACTION_DOWN:
                sendDefaultKeyboardEvent(v);
                break;
        }
    }

    public interface ApplyToKeyRunnable {
        void run(Key key);
    }

    private final class MyHandler {
        private final ListedMap<Integer, View> messageIds = new ListedMap<>();
        private final List<Thread> threads = new ArrayList<>();

        private MyHandler() {}

        public void removeAndSendEmptyMessage(int what) {
            removeMessages(what);
            sendEmptyMessage(what);
        }

        public void removeAndSendMessage(int what, View v) {
            removeMessages(what);
            sendMessage(what, v);
        }

        public void removeAndSendMessageDelayed(int what, View v, long delay) {
            removeMessages(what);
            sendMessageDelayed(what, v, delay);
        }

        public void removeMessages(int what) {
            messageIds.remove(what);

            if (what == 1) {
                for (Thread thread : threads) {
                    try {
                        thread.interrupt();
                    } catch (Throwable ignored) {}
                }
                threads.clear();
            }
        }

        public void sendEmptyMessage(int what) {
            sendMessage(what, null);
        }

        public void sendMessage(int what, View v) {
            if (!messageIds.containsKey(what)) {
                messageIds.put(what, v);
            }

            handleMessage(what);
        }

        public void sendMessageDelayed(int what, View v, long time) {
            messageIds.put(what, v);

            try {
                threads.add(new Thread() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(time);
                            SuperBoardApplication.mainHandler.post(() -> sendMessage(what, v));
                        } catch (InterruptedException ignored) {}
                    }
                });
                threads.get(threads.size() - 1).start();
            } catch (Throwable ignored) {}
        }

        public boolean hasMessages(int what) {
            return messageIds.containsKey(what);
        }

        private void handleMessage(int what) {
            View v = messageIds.get(what);

            switch (what) {
                case 0: // after
                    longPressed = false;
                    removeMessages(0);
                    afterKeyboardEvent();
                    break;
                case 1: // long continue
                    removeMessages(1);
                    switch (currentMotionEventAction) {
                        case MotionEvent.ACTION_UP:
                            removeAndSendEmptyMessage(0);
                            break;
                        case MotionEvent.ACTION_DOWN:
                            if (isHasPopup(v)) {
                                onPopupEvent();
                                removeAndSendEmptyMessage(0);
                            } else if (isHasLongPressEvent(v)) {
                                Pair<Integer, Boolean> a = ((Key) v).getLongPressEvent();
                                if (a.second) {
                                    sendKeyEvent(a.first);
                                } else {
                                    commitText(String.valueOf((char) a.first.intValue()));
                                }
                                playSound(a.first);
                                removeAndSendEmptyMessage(0);
                            } else {
                                if (!((InputMethodService) getContext()).isInputViewShown()) {
                                    currentMotionEventAction = MotionEvent.ACTION_UP;
                                }
                                removeAndSendMessage(2, v);
                            }
                            break;
                    }
                    break;
                case 2: // normal or long start
                    if (currentMotionEventAction == MotionEvent.ACTION_UP) {
                        removeAndSendEmptyMessage(0);
                    } else {
                        sendDefaultKeyboardEvent(v);
                        if (isRepeat) {
                            long delay = (20L * longPressMultiplier) * (longPressed ? 1 : 20);
                            removeAndSendMessageDelayed(1, v, delay);
                            if (!longPressed) longPressed = true;
                        } else {
                            removeAndSendEmptyMessage(0);
                        }
                    }
                    break;
            }
        }
    }

    public final class Row extends LinearLayout {

        public Row(Context c) {
            super(c);
            setFocusable(false);
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

    public final class Key extends RelativeLayout {
        public final TextView label, subLabel;
        private final ImageView icon;
        private View state;
        private int stateCount = 1, currentState = 0;
        private CharSequence[] popupCharacters;

        Key(Context context) {
            super(context);
            setFocusable(false);
            setLayoutParams(new LinearLayout.LayoutParams(-1, -1, 1));

            label = new TextView(context);
            setLabelParams(label);
            subLabel = new TextView(context);
            setSubLabelParams(subLabel);

            icon = new ImageView(context);
            LayoutParams iconParams = new LayoutParams(-1, -1);
            iconParams.addRule(CENTER_IN_PARENT, TRUE);
            icon.setLayoutParams(iconParams);
            icon.setScaleType(ImageView.ScaleType.FIT_CENTER);

            addView(label);
            addView(subLabel);
            addView(icon);

            setKeyImageVisible(false);
            setKeyShadow(shadowRadius, shadowColor);
            setKeyTextSize(textSize);
            setBackground(keyBackground);
            setKeyTextStyle(textStyle);
            setKeyItemColor(keyTextColor);
            setOnTouchListener(SuperBoard.this);
        }

        private void setLabelParams(TextView label) {
            label.setLayoutParams(new LayoutParams(-1, -1));
            label.setTextColor(keyTextColor);
            label.setSingleLine();
            label.setGravity(CENTER);
            label.setHintTextColor(0);
        }

        private void setSubLabelParams(TextView subLabel) {
            LayoutParams subParams = new LayoutParams(-2, -2);
            subParams.addRule(ALIGN_PARENT_RIGHT, TRUE);
            subParams.addRule(ALIGN_PARENT_TOP, TRUE);
            int margin = DensityUtils.mpInt(1.5f);
            subParams.rightMargin = subParams.topMargin = margin;
            subLabel.setLayoutParams(subParams);
        }

        public boolean isKeyIconSet() {
            return icon.getDrawable() != null;
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
                stateDrawable.setColor(keyTextColor);
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
            gradientDrawable.setColor(keyTextColor);
        }

        public void setBackground(Drawable b) {
            setBackgroundDrawable(b);
        }

        @Override
        public void setBackgroundDrawable(Drawable b) {
            super.setBackgroundDrawable(b == null ? null : Objects.requireNonNull(b.getConstantState()).newDrawable());
        }

        public void setKeyItemColor(int color) {
            label.setTextColor(color);
            subLabel.setTextColor(ColorUtils.setAlphaForColor(0x66, color));
            if (isKeyIconSet()) {
                getKeyIcon().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            }
            changeState(currentState);
        }

        CharSequence getText() {
            return label.getText();
        }

        public void setText(CharSequence text) {
            setKeyImageVisible(false);
            label.setText(text);
        }

        CharSequence getSubText() {
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
            icon.setImageDrawable(dr);
            setKeyImageVisible(true);
            setKeyItemColor(keyTextColor);
        }

        public void setKeyIcon(int iconRes) {
            setKeyIcon(getContext().getResources().getDrawable(iconRes));
        }

        CharSequence[] getPopupCharacters() {
            return popupCharacters;
        }

        void setPopupCharacters(CharSequence[] cs) {
            if (cs == null || cs.length < 1) {
                popupCharacters = null;
                setSubText("");
            } else {
                popupCharacters = cs;
                setSubText(popupCharacters[0]);
            }
        }

        public void setKeyImageVisible(boolean visible) {
            icon.setVisibility(visible ? VISIBLE : GONE);
            label.setVisibility(visible ? GONE : VISIBLE);
            subLabel.setVisibility(popupPreview && !visible ? VISIBLE : GONE);
        }

        public void fixCase() {
            boolean shiftOn = getShiftState() != SHIFT_OFF;

            label.setVisibility(isKeyIconSet() ? GONE : VISIBLE);
            subLabel.setVisibility(popupPreview && label.isShown() ? VISIBLE : GONE);

            if (!hasNormalPressEvent()) {
                label.setText(getCase(getText(), shiftOn));
                subLabel.setText(getCase(getSubText(), shiftOn));
            }
        }

        public void setKeyTextSize(float size) {
            label.setTextSize(size);
            subLabel.setTextSize(size / 2);
            applyIconMultiply();
        }

        public void applyIconMultiply() {
            ViewGroup.LayoutParams vp = icon.getLayoutParams();
            vp.width = -1;
            vp.height = (int) (textSize * iconSizeMultiplier);
        }

        public void setKeyShadow(int radius, int color) {
            label.setShadowLayer(radius, 0, 0, color);
            subLabel.setShadowLayer(radius, 0, 0, color);
        }

        public void setKeyTextStyle(int style) {
            TextType[] arr = TextType.values();
            setKeyTextStyle(arr[(arr.length - 1) < style ? 0 : style]);
        }

        public void setKeyTextStyle(TextType style) {
            TextUtilsCompat.setTypefaceFromTextType(label, style);
            subLabel.setTypeface(label.getTypeface());
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
            k.setPopupCharacters(getPopupCharacters());
            k.setKeyShadow(shadowRadius, shadowColor);
            k.setKeyItemColor(keyTextColor);
            k.setId(getId());
            k.setKeyTextSize(label.getTextSize() / 2.5f);
            k.setKeyTextStyle(textStyle);
            k.setText(label.getText());
            k.setSubText(subLabel.getText());

            k.setKeyIcon(getKeyIcon());
            k.setKeyImageVisible(isKeyIconSet());

            if (disableTouchEvent) {
                k.setOnTouchListener(null);
            }

            return k;
        }
    }
}
