package org.blinksd.board.views;

import static android.media.AudioManager.FX_KEYPRESS_DELETE;
import static android.media.AudioManager.FX_KEYPRESS_RETURN;
import static android.media.AudioManager.FX_KEYPRESS_SPACEBAR;
import static android.media.AudioManager.FX_KEYPRESS_STANDARD;
import static android.view.Gravity.CENTER;
import static android.view.View.OnTouchListener;
import static org.blinksd.board.SuperBoardApplication.mainHandler;
import static org.blinksd.utils.ColorUtils.setAlphaForColor;
import static org.blinksd.utils.ColorUtils.setColorFilter;
import static org.blinksd.utils.DensityUtils.dpInt;
import static org.blinksd.utils.DensityUtils.hpInt;
import static org.blinksd.utils.DensityUtils.minP;
import static org.blinksd.utils.DensityUtils.minPInt;
import static org.blinksd.utils.DensityUtils.wpInt;
import static org.blinksd.utils.SystemUtils.getMultipliedTextSize;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
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
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.blinksd.board.R;
import org.blinksd.utils.ListedMap;
import org.blinksd.utils.ResourcesUtils;
import org.blinksd.utils.TextUtilsCompat;
import org.blinksd.utils.superboard.KeyboardType;
import org.blinksd.utils.superboard.OnModifierChangedListener;
import org.blinksd.utils.superboard.TextType;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@SuppressWarnings({"deprecation"})
public class SuperBoard extends FrameLayout implements OnTouchListener {
    public static final int KEYCODE_CLOSE_KEYBOARD = -100;
    public static final int KEYCODE_SWITCH_LANGUAGE = -101;
    public static final int KEYCODE_OPEN_EMOJI_LAYOUT = -102;
    public static final int KEYCODE_TOGGLE_CTRL = -103;
    public static final int KEYCODE_TOGGLE_ALT = -104;
    public static final int KEYCODE_SETTINGS = -105;
    public static final int SHIFT_OFF = 0;
    public static final int SHIFT_ON = 1;
    public static final int SHIFT_LOCKED = 2;
    protected static final int
            TAG_LONG_PRESS = R.id.key_long_press,
            TAG_NORMAL_PRESS = R.id.key_normal_press,
            TAG_KEY_WIDTH = R.id.key_width,
            TAG_DISABLE_MODIFIER = R.id.disable_type_modifier,
            TAG_KEY_REPEAT = R.id.key_repeat;
    private static Locale caseLocale = new Locale("tr", "TR");
    private final MyHandler mHandler = new MyHandler();
    private final Vibrator vibrator;
    private float textSize = minP(1.25f);
    private float landSizeIncreaser = 1f;
    protected Drawable keyBackground = null;
    private int selected = 0;
    private float heightPercent = 40;
    private int widthPercent = 100;
    protected int shadowRadius = 0;
    private int keyTextColor = 0xFFDEDEDE;
    protected int shadowColor = keyTextColor;
    protected int textStyle = 0;
    private int vibrateDuration = 0;
    private int longPressMultiplier = 1;
    protected int iconSizeMultiplier = 1;
    private int currentEditorAction = 0;
    private float keyIndicatorHeight = 0.5f;
    private boolean disablePopup = false;
    private boolean popupPreview = false;
    private boolean isRepeat = true;
    private boolean shiftDetect = true;
    private boolean enforcedShiftDetect = true;
    private boolean enforcedEditorAction = true;
    private boolean longPressFastDelete = false;
    private boolean insertSpaceAfterPunc = false;
    private boolean disableSuggestionsTemporarily = false;
    private final ListedMap<String, String> specialCases = new ListedMap<>();
    private final List<Integer> enforcedShiftRestrictedEvents = Arrays.asList(
            KEYCODE_TOGGLE_CTRL,
            KEYCODE_TOGGLE_ALT,
            Keyboard.KEYCODE_SHIFT
    );
    private final List<Key> extraKeyViews = new ArrayList<>();
    private final List<CharSequence> SUPPORTED_PUNCTUATION_TYPES = Arrays.asList(
            ".", ",", ";", ":",
            "...", "…", "?", "!"
    );
    private final OnModifierChangedListener onModifierChangedListener;
    private Configuration recentConfiguration;

    // key states
    private int ctrl = 0;
    private int alt = 0;
    private int shift = 0;

    public SuperBoard(Context context) {
        this(context, null);
    }

    public SuperBoard(Context context, OnModifierChangedListener onModifierChangedListener) {
        super(context);
        this.recentConfiguration = context.getResources().getConfiguration();
        this.onModifierChangedListener = onModifierChangedListener;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        else {
            VibratorManager vm = (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            vibrator = vm.getDefaultVibrator();
        }

        setLayoutParams(new LayoutParams(-1, -1));
        createEmptyLayout(KeyboardType.TEXT);
        setForegroundGravity(CENTER);
    }

    /** @noinspection EmptyMethod*/
    public final void beforeKeyboardEvent() {}

    public void onKeyboardEvent(View view) {}

    public void afterKeyboardEvent() {}

    public void onPopupEvent() {}

    public void afterPopupEvent() {
        stopAllKeyEvents();
    }

    public void switchLanguage() {}

    public void openEmojiLayout() {}

    public final void setSpecialCases(Map<String, String> items) {
        specialCases.clear();
        specialCases.putAll(items);
    }

    public final int getKeyboardHeight() {
        return getLayoutParams().height;
    }

    public void setRecentConfiguration(Configuration newConfig) {
        recentConfiguration = newConfig;
    }

    public final void setLandscapeHeightIncreaser(float amount) {
        landSizeIncreaser = amount;
    }

    public final void setKeyboardHeight(float percent) {
        heightPercent = percent;
        getLayoutParams().height = hpInt(percent *
                (recentConfiguration.orientation == Configuration.ORIENTATION_LANDSCAPE ? landSizeIncreaser : 1f));
        final int childCount = getChildCount();
        if (childCount > 0) {
            for (int i = 0; i < childCount; i++) {
                getChildAt(i).getLayoutParams().height = getLayoutParams().height;
            }
        }

        int x = selected;
        setEnabledLayout(findNumberKeyboardIndex());
        setEnabledLayout(x);
    }

    public final float getKeyboardHeightPercent() {
        return heightPercent;
    }

    public final int getKeyboardWidthPercent() {
        return widthPercent;
    }

    public final void fixHeight() {
        setKeyboardHeight(getKeyboardHeightPercent());

        final int mainChildCount = getChildCount();
        for (int i = 0; i < mainChildCount; i++) {
            final int kbdChildCount = getKeyboard(i).getChildCount();

            for (int g = 0; g < kbdChildCount; g++) {
                getRow(i, g).setKeyWidths();
            }
        }
    }

    public final void setKeyVibrateDuration(int dur) {
        vibrateDuration = dur;
    }

    public void clear() {
        removeAllViewsInLayout();
        createEmptyLayout(KeyboardType.TEXT);
    }

    public final void setKeyBackgroundAndItemColor(int keyboardIndex, int rowIndex, int keyIndex, Drawable background, int itemColor) {
        Key key = getKey(keyboardIndex, rowIndex, keyIndex);
        key.setBackground(background);
        key.setKeyItemColor(itemColor);
    }

    public final void setKeyRepeat(int keyboardIndex, int rowIndex, int keyIndex) {
        setKeyRepeat(keyboardIndex, rowIndex, keyIndex, true);
    }

    public final void setKeyRepeat(int keyboardIndex, int rowIndex, int keyIndex, boolean repeat) {
        setKeyRepeat(getKey(keyboardIndex, rowIndex, keyIndex), repeat);
    }

    public final void setKeyRepeat(SuperBoard.Key key, boolean repeat) {
        key.setRepeat(repeat);
    }

    public final void setKeyWidthPercent(int keyboardIndex, int rowIndex, int keyIndex, int percent) {
        getKey(keyboardIndex, rowIndex, keyIndex).setKeyWidthPercent(percent);
    }

    public final void setLongPressMultiplier(int multi) {
        longPressMultiplier = multi;
    }

    public final void setIconSizeMultiplier(int multi) {
        iconSizeMultiplier = multi;
        applyIconMultiply();
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
                final int vChildCount = v.getChildCount();

                assert (vChildCount == chars.length)
                        : "Row count != Popup row count";

                for (int i = 0; i < vChildCount; i++) {
                    Row r = getRow(keyboardIndex, i);
                    final int rChildCount = r.getChildCount();

                    assert (rChildCount == chars[i].length)
                            : "Row key count != Popup row key count";

                    for (int g = 0; g < rChildCount; g++)
                        setPopupForKey(keyboardIndex, i, g, chars[i][g]);
                }
            } else throw new RuntimeException("Invalid keyboard index number");
        }
    }

    public final void setKeysPadding(final int padding) {
        applyToAllKeys(key -> {
            Row.LayoutParams l = (Row.LayoutParams) key.getLayoutParams();
            l.bottomMargin = l.topMargin = l.leftMargin = l.rightMargin = padding;
        });
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

    public void addExtraKey(Key key) {
        extraKeyViews.add(key);
    }

    public final float getKeysTextSize() {
        return textSize;
    }

    public final void setKeysTextSize(final float size) {
        setKeysTextSize(size, false);
    }

    public final void setKeysTextSize(final float size, boolean force) {
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

    public final void setKeyboardIndicatorHeight(final float height) {
        if (height != keyIndicatorHeight) {
            keyIndicatorHeight = height;
            applyToAllKeys(Key::applyIndicatorHeight);
        }
    }

    public final void applyToAllKeys(ApplyToKeyRunnable runnable) {
        final int mainChildCount = getChildCount();
        for (int j = 0; j < mainChildCount; j++) {
            final int kbdChildCount = getKeyboard(j).getChildCount();
            for (int i = 0; i < kbdChildCount; i++) {
                final int rowChildCount = getRow(j, i).getChildCount();
                for (int g = 0; g < rowChildCount; g++) {
                    runnable.run(getKey(j, i, g));
                }
            }
        }

        for (int i = 0; i < extraKeyViews.size(); i++) {
            runnable.run(extraKeyViews.get(i));
        }
    }

    public final void setKeyboardWidth(int percent) {
        widthPercent = percent;
        getLayoutParams().width = wpInt(percent);
        final int childCount = getChildCount();
        if (childCount > 0) {
            for (int i = 0; i < childCount; i++) {
                getChildAt(i).getLayoutParams().width = getLayoutParams().width;
            }
        }
        int x = selected;
        setEnabledLayout(findNumberKeyboardIndex());
        setEnabledLayout(x);
    }

    public final void setKeyDrawable(int keyboardIndex, int rowIndex, int keyIndex, int resId) {
        setKeyDrawable(keyboardIndex, rowIndex, keyIndex, ResourcesUtils.getDrawable(resId));
    }

    public final void setKeyDrawable(int keyboardIndex, int rowIndex, int keyIndex, Drawable d) {
        setColorFilter(d, keyTextColor);
        Key t = getKey(keyboardIndex, rowIndex, keyIndex);
        ((LinearLayout.LayoutParams) t.getLayoutParams()).gravity = CENTER;
        t.setKeyIcon(d);
    }

    public int getLayoutRowCount(int layoutIndex) {
        return ((ViewGroup) getChildAt(layoutIndex)).getChildCount();
    }

    public int getEnabledLayoutIndex() {
        return selected;
    }

    public final void setEnabledLayout(int keyboardIndex) {
        final int childCount = getChildCount();
        if (keyboardIndex < 0) keyboardIndex += childCount;
        if (keyboardIndex < childCount && keyboardIndex >= 0) {
            if (childCount == 1 || keyboardIndex == selected) return;
            getChildAt(selected).setVisibility(GONE);
            selected = keyboardIndex;
            getChildAt(selected).setVisibility(VISIBLE);
        } else throw new RuntimeException("Invalid keyboard index number");
    }

    public final void setLayoutType(int keyboardIndex, KeyboardType type) {
        getKeyboard(keyboardIndex).setTag(type);
    }

    public final void createLayoutWithRows(String[][] keys, KeyboardType type) {
        createEmptyLayout(type);
        addRows(getChildCount() - 1, keys);
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

    protected void sendKeyboardEvent(Key v) {
        defaultKeyboardEvent(v);
    }

    private void defaultKeyboardEvent(Key v) {
        beforeKeyboardEvent();

        if (v.hasNormalPressEvent()) {
            Pair<Integer, Boolean> currentKey = v.getNormalPressEvent();
            switch (currentKey.first) {
                case KEYCODE_TOGGLE_CTRL:
                    toggleCtrlState();
                    break;
                case KEYCODE_TOGGLE_ALT:
                    toggleAltState();
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

                    if (!getEnforcedShiftDetection()) {
                        updateKeyState();
                    }
                    break;
            }
            playSound(currentKey.first);

            if (getEnforcedShiftDetection() &&
                    !enforcedShiftRestrictedEvents.contains(currentKey.first)) {
                updateKeyState();
            }
        } else {
            commitText(v.getText().toString());
            updateKeyState();

            playSound(0);
        }

        vibrate();
        onKeyboardEvent(v);
    }

    @SuppressLint("MissingPermission")
    public final void vibrate() {
        if (vibrateDuration > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(vibrateDuration, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(vibrateDuration);
            }
        }
    }

    private InputMethodService getCurrentIMService() {
        return ((InputMethodService) getContext());
    }

    public InputConnection getCurrentInputConnection() {
        return getCurrentIMService().getCurrentInputConnection();
    }

    @SuppressLint("InlinedApi")
    public void sendKeyEvent(int code) {
        switch (code) {
            case KEYCODE_CLOSE_KEYBOARD:
                getCurrentIMService().requestHideSelf(InputMethodManager.HIDE_NOT_ALWAYS);
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
                sendKeyDownUp(code);

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
        sendKeyAction(
                KeyEvent.KEYCODE_CTRL_LEFT,
                down ? KeyEvent.ACTION_DOWN : KeyEvent.ACTION_UP,
                KeyEvent.META_CTRL_ON | KeyEvent.META_CTRL_LEFT_ON
        );
    }

    private void sendAlt(boolean down) {
        sendKeyAction(
                KeyEvent.KEYCODE_ALT_LEFT,
                down ? KeyEvent.ACTION_DOWN : KeyEvent.ACTION_UP,
                KeyEvent.META_ALT_ON | KeyEvent.META_ALT_LEFT_ON
        );
    }

    private void sendKeyDownUp(int code) {
        sendKeyAction(code, KeyEvent.ACTION_DOWN);
        sendKeyAction(code, KeyEvent.ACTION_UP);
    }

    private void sendKeyAction(int code, int action) {
        int metaState = 0;

        if (!isDisabledModifierForKeyboard(selected)) {
            if (getCtrlState() > 0) {
                metaState |= KeyEvent.META_CTRL_LEFT_ON | KeyEvent.META_CTRL_ON;
            }

            if (getAltState() > 0) {
                metaState |= KeyEvent.META_ALT_LEFT_ON | KeyEvent.META_ALT_ON;
            }
        }

        sendKeyAction(code, action, metaState);
    }

    private void sendKeyAction(int code, int action, int metaState) {
        KeyEvent event = new KeyEvent(
                0,
                0,
                action,
                code,
                0,
                metaState,
                0,
                0,
                KeyEvent.FLAG_CANCELED | KeyEvent.FLAG_KEEP_TOUCH_MODE | KeyEvent.FLAG_TRACKING,
                InputDevice.SOURCE_TOUCHSCREEN
        );
        getCurrentInputConnection().sendKeyEvent(event);
    }

    public boolean isDisabledSuggestionsTemporarily() {
        return disableSuggestionsTemporarily;
    }

    private boolean performEditorAction() {
        boolean performedAction = false;

        if ((currentEditorAction > EditorInfo.IME_ACTION_NONE &&
                currentEditorAction <= EditorInfo.IME_ACTION_PREVIOUS) || enforcedEditorAction) {
            performedAction = getCurrentInputConnection().performEditorAction(currentEditorAction);
        }

        currentEditorAction = 0;
        return performedAction;
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

        if (modifiersEnabled && StandardCharsets.US_ASCII.newEncoder().canEncode(text)) {
            // Copied from https://stackoverflow.com/a/31625638
            KeyCharacterMap charMap;
            charMap = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD);

            KeyEvent[] events = charMap.getEvents(text.toCharArray());
            for (KeyEvent event : events) {
                sendKeyAction(event.getKeyCode(), event.getAction());
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
        if (insertSpaceAfterPunc &&
                !TextUtils.isEmpty(text) &&
                SUPPORTED_PUNCTUATION_TYPES.contains(text.substring(text.length() - 1))) {
            text += " ";
        }

        getCurrentInputConnection().commitText(text, text.length());
    }

    public void setLongPressFastDelete(boolean value) {
        longPressFastDelete = value;
    }

    public void setInsertSpaceAfterPunc(boolean value) {
        insertSpaceAfterPunc = value;
    }

    public int getCtrlState() {
        return ctrl;
    }

    public final void setCtrlState(int state) {
        if (state >= 2 || state < 0) {
            state = 0;
        }

        ctrl = state;

        if (onModifierChangedListener != null) {
            onModifierChangedListener.onModifierChanged(KEYCODE_TOGGLE_CTRL, state);
        }
    }

    protected void toggleCtrlState() {
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

        if (onModifierChangedListener != null) {
            onModifierChangedListener.onModifierChanged(KEYCODE_TOGGLE_ALT, state);
        }
    }

    protected void toggleAltState() {
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
            if (state != SHIFT_LOCKED && !t.isKeyIconSet() && !t.hasEvent()) {
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

    public final boolean getEnforcedShiftDetection() {
        return shiftDetect && enforcedShiftDetect;
    }

    public final void setEnforcedShiftDetection(boolean enabled) {
        enforcedShiftDetect = enabled;
    }

    public final void setEnforcedEditorAction(boolean enabled) {
        enforcedEditorAction = enabled;
    }

    public final void updateKeyState() {
        setCtrlState(0);
        setAltState(0);

        if (isCurrentFNKeyboard() || isCurrentSymbolKeyboard() || isCurrentMathKeyboard()) {
            return;
        }

        EditorInfo ei = getCurrentIMService().getCurrentInputEditorInfo();

        currentEditorAction = ei.imeOptions & (EditorInfo.IME_MASK_ACTION | EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        disableSuggestionsTemporarily = false;

        switch (ei.inputType & InputType.TYPE_MASK_VARIATION) {
            case InputType.TYPE_NUMBER_VARIATION_PASSWORD:
            case InputType.TYPE_TEXT_VARIATION_PASSWORD:
            case InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD:
            case InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD:
                disableSuggestionsTemporarily = true;
                break;
        }

        if ((ei.inputType & InputType.TYPE_MASK_FLAGS) == InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS) {
            disableSuggestionsTemporarily = true;
        }

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
        recentConfiguration = newConfig;
        fixHeight();
    }

    public final void setRowPadding(int keyboardIndex, int rowIndex, int padding) {
        getRow(keyboardIndex, rowIndex).setPadding(padding, 0, padding, 0);
    }

    public final boolean isDisabledModifierForKeyboard(int keyboardIndex) {
        Object tag = getKeyboard(keyboardIndex).getTag(TAG_DISABLE_MODIFIER);
        return tag != null && (boolean) tag;
    }

    @SuppressWarnings("unused")
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
        key.setNormalPressEvent(keyCode, isEvent);
    }

    public final void setLongPressEventForKey(int keyboardIndex, int rowIndex, int keyIndex, int keyCode) {
        setLongPressEventForKey(keyboardIndex, rowIndex, keyIndex, keyCode, true);
    }

    public final void setLongPressEventForKey(int keyboardIndex, int rowIndex, int keyIndex, int keyCode, boolean isEvent) {
        setLongPressEventForKey(getKey(keyboardIndex, rowIndex, keyIndex), keyCode, isEvent);
    }

    public final void setLongPressEventForKey(Key key, int keyCode, boolean isEvent) {
        key.setLongPressEvent(keyCode, isEvent);
    }

    public final void setDisablePopup(boolean val) {
        disablePopup = val;
    }

    public final List<Integer> findKeyboardIndexes(KeyboardType type) {
        List<Integer> indexes = new ArrayList<>();
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (type.equals(getChildAt(i).getTag())) {
                indexes.add(i);
            }
        }

        return indexes;
    }

    public final int findKeyboardIndex(KeyboardType type) {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (type.equals(getChildAt(i).getTag())) {
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

    public final boolean isCurrentMathKeyboard() {
        return getCurrentKeyboardType() == KeyboardType.MATH;
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
        return getKeyboardType(selected);
    }

    public final KeyboardType getKeyboardType(int index) {
        return (KeyboardType) getKeyboard(index).getTag();
    }

    public final int findMathKeyboardIndex() {
        return findKeyboardIndex(KeyboardType.MATH);
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
        Key key = (Key) v;

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

        if (key.isKeyRepeat() || key.hasPopup() || key.hasLongPressEvent()) {
            if (key.hasPopup() && disablePopup) {
                normalPress(v, m);
                return true;
            }
            switch (m.getAction()) {
                case MotionEvent.ACTION_UP:
                    key.currentMotionEventAction = MotionEvent.ACTION_UP;
                    if (mHandler.hasMessages(1)) {
                        mHandler.removeMessages(1);
                        sendKeyboardEvent(key);
                    }
                    mHandler.removeAndSendMessage(0, key);
                    break;
                case MotionEvent.ACTION_DOWN:
                    key.currentMotionEventAction = MotionEvent.ACTION_DOWN;
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
                mHandler.removeAndSendMessage(0, v);
                break;
            case MotionEvent.ACTION_DOWN:
                sendKeyboardEvent((Key) v);
                break;
        }
    }

    public void stopAllKeyEvents() {
        applyToAllKeys(key -> {
            key.longPressed = false;
            key.longPressEventCounter = 0;
            key.currentMotionEventAction = KeyEvent.ACTION_UP;
        });
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
                final int lenThreads = threads.size();

                for (int i = 0; i < lenThreads; i++) {
                    try {
                        threads.get(i).interrupt();
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
                final var thread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(time);
                            mainHandler.post(() -> sendMessage(what, v));
                        } catch (InterruptedException ignored) {}

                        try {
                            threads.remove(this);
                        } catch (Throwable ignored) {}
                    }
                };
                threads.add(thread);
                thread.start();
            } catch (Throwable ignored) {}
        }

        public boolean hasMessages(int what) {
            return messageIds.containsKey(what);
        }

        private void handleMessage(int what) {
            Key v = (Key) messageIds.get(what);
            if (v == null && what != 0) {
                removeAndSendEmptyMessage(0);
                return;
            }

            switch (what) {
                case 0: // after
                    if (v != null) {
                        v.longPressed = false;
                        v.longPressEventCounter = 0;
                        v.currentMotionEventAction = MotionEvent.ACTION_UP;
                    }

                    removeMessages(0);
                    afterKeyboardEvent();
                    break;
                case 1: // long continue
                    removeMessages(1);

                    switch (v.currentMotionEventAction) {
                        case MotionEvent.ACTION_UP:
                            removeAndSendMessage(0, v);
                            break;
                        case MotionEvent.ACTION_DOWN:
                            if (v.hasLongPressEvent()) {
                                Pair<Integer, Boolean> a = v.getLongPressEvent();
                                if (a.second) {
                                    sendKeyEvent(a.first);
                                } else {
                                    commitText(String.valueOf((char) a.first.intValue()));
                                }
                                playSound(a.first);
                                removeAndSendMessage(0, v);
                            } else if (v.hasPopup()) {
                                onPopupEvent();
                                removeAndSendMessage(0, v);
                            } else {
                                if (getContext() instanceof InputMethodService &&
                                        !((InputMethodService) getContext()).isInputViewShown()) {
                                    v.currentMotionEventAction = MotionEvent.ACTION_UP;
                                }
                                removeAndSendMessage(2, v);
                            }
                            break;
                    }
                    break;
                case 2: // normal or long start
                    if (v.currentMotionEventAction == MotionEvent.ACTION_UP) {
                        if (longPressFastDelete && v.getNormalPressEvent().first == Keyboard.KEYCODE_DELETE) {
                            setCtrlState(0);
                            sendCtrl(false);
                        }

                        removeAndSendMessage(0, v);
                    } else {
                        sendKeyboardEvent(v);
                        if (isRepeat) {
                            long delay = (20L * longPressMultiplier) * (v.longPressed ? 1 : 20);
                            removeAndSendMessageDelayed(1, v, delay);
                            if (!v.longPressed) v.longPressed = true;
                            else if (longPressFastDelete && v.getNormalPressEvent().first == Keyboard.KEYCODE_DELETE) {
                                setCtrlState(0);
                                sendCtrl(false);

                                if (v.longPressEventCounter++ > 10) {
                                    setCtrlState(1);
                                    sendCtrl(true);
                                }
                            }
                        } else {
                            removeAndSendMessage(0, v);
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
            }
        }

        @SuppressLint("ResourceType")
        void setKeyWidths() {
            final int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                Key k = (Key) getChildAt(i);
                k.setKeyWidthPercent(
                        k.getKeyWidthPercent() < 1
                                ? 100 / childCount
                                : k.getKeyWidthPercent()
                );
            }
        }
    }

    public final class Key extends RelativeLayout {
        private final TextView label, subLabel;
        private final ImageView icon;
        private View state;
        private int stateCount = 1, currentState = 0;
        private CharSequence[] popupCharacters;
        private int currentMotionEventAction = MotionEvent.ACTION_UP;
        private int longPressEventCounter = 0;
        private boolean longPressed = false;

        Key(Context context) {
            super(context);
            setFocusable(false);
            setLayoutParams(new LinearLayout.LayoutParams(-1, -1, 1));

            label = new TextView(context);
            subLabel = new TextView(context);
            setLabelParams();
            setSubLabelParams();

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

        private void setLabelParams() {
            label.setLayoutParams(new LayoutParams(-1, -1));
            label.setTextColor(keyTextColor);
            label.setSingleLine();
            label.setGravity(CENTER);
            label.setHintTextColor(0);
        }

        private void setSubLabelParams() {
            LayoutParams subParams = new LayoutParams(-1, -1);
            subParams.addRule(ALIGN_PARENT_TOP, TRUE);
            subParams.addRule(CENTER_HORIZONTAL, TRUE);
            subParams.topMargin = minPInt(0.5f);
            subLabel.setLayoutParams(subParams);
            subLabel.setGravity(Gravity.CENTER_HORIZONTAL);
            subLabel.setTextColor(setAlphaForColor(0x66, keyTextColor));
            label.setSingleLine();
            label.setGravity(CENTER);
            label.setHintTextColor(0);
        }

        public boolean isKeyIconSet() {
            return icon.getDrawable() != null;
        }

        public boolean hasNormalPressEvent() {
            return getTag(TAG_NORMAL_PRESS) != null;
        }

        public void setNormalPressEvent(int keyCode, boolean isEvent) {
            setTag(TAG_NORMAL_PRESS, new Pair<>(keyCode, isEvent));
        }

        public boolean hasLongPressEvent() {
            return getTag(TAG_LONG_PRESS) != null;
        }

        public void setLongPressEvent(int keyCode, boolean isEvent) {
            setTag(TAG_LONG_PRESS, new Pair<>(keyCode, isEvent));
        }

        public boolean isKeyRepeat() {
            if (!isRepeat) return false;
            Object tag = getTag(TAG_KEY_REPEAT);
            return tag != null && (boolean) tag;
        }

        private void setRepeat(boolean repeat) {
            setTag(TAG_KEY_REPEAT, repeat);
        }

        private boolean hasPopup() {
            return popupCharacters != null && popupCharacters.length > 0 && !isKeyRepeat();
        }

        @SuppressWarnings("unchecked")
        public Pair<Integer, Boolean> getNormalPressEvent() {
            return (Pair<Integer, Boolean>) getTag(TAG_NORMAL_PRESS);
        }

        @SuppressWarnings("unchecked")
        public Pair<Integer, Boolean> getLongPressEvent() {
            return (Pair<Integer, Boolean>) getTag(TAG_LONG_PRESS);
        }

        public boolean hasEvent() {
            return isKeyRepeat() || hasLongPressEvent() || hasNormalPressEvent();
        }

        public void setStateCount(int stateCount) {
            if (stateCount < 1) {
                return;
            }

            if (state == null) {
                RelativeLayout.LayoutParams stateParams =
                        new RelativeLayout.LayoutParams(
                                minPInt(4), minPInt(keyIndicatorHeight));
                stateParams.bottomMargin = minPInt(2);
                stateParams.addRule(ALIGN_PARENT_BOTTOM, TRUE);
                stateParams.addRule(CENTER_HORIZONTAL, TRUE);
                state = new View(getContext());
                state.setLayoutParams(stateParams);
                GradientDrawable stateDrawable = new GradientDrawable();
                stateDrawable.setColor(keyTextColor);
                stateDrawable.setAlpha(0);
                stateDrawable.setCornerRadius(dpInt(16));
                state.setBackgroundDrawable(stateDrawable);
                addView(state);
            }

            this.stateCount = stateCount;
            if (currentState >= stateCount) {
                currentState = stateCount - 1;
            }
            changeState(currentState);
        }

        public void applyIndicatorHeight() {
            if (state != null) {
                state.getLayoutParams().height = minPInt(keyIndicatorHeight);
            }
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
            subLabel.setTextColor(setAlphaForColor(0x66, color));
            if (isKeyIconSet()) {
                setColorFilter(getKeyIcon(), color);
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
            setKeyImageVisible(dr != null);
            setKeyItemColor(keyTextColor);
        }

        public void setKeyIcon(int iconRes) {
            setKeyIcon(ResourcesUtils.getDrawable(iconRes));
        }

        public int getKeyWidthPercent() {
            Object width = getTag(TAG_KEY_WIDTH);
            return width == null ? 0 : (int) width;
        }

        public void setKeyWidthPercent(int percent) {
            getLayoutParams().width = wpInt(percent);
            setTag(TAG_KEY_WIDTH, percent);
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
            label.setTextSize(TypedValue.COMPLEX_UNIT_PX, getMultipliedTextSize(size));
            subLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, getMultipliedTextSize(size / 1.5f));
            applyIconMultiply();
        }

        private void applyIconMultiply() {
            ViewGroup.LayoutParams vp = icon.getLayoutParams();
            vp.width = -1;
            vp.height = (int) (getMultipliedTextSize(textSize) * iconSizeMultiplier * 0.75f);
        }

        public void setKeyShadow(int radius, int color) {
            label.setShadowLayer(radius, 0, 0, color);
            subLabel.setShadowLayer(radius, 0, 0, color);
        }

        public void setKeyTextStyle(int style) {
            TextUtilsCompat.setTypefaceFromTextType(label, TextType.getFromIndex(style));
            subLabel.setTypeface(label.getTypeface());
        }

        public void clone(Key k) {
            if (getBackground() != null) {
                Rect r = getBackground().getBounds();
                k.getLayoutParams().width = r.right;
                k.getLayoutParams().height = r.bottom;
            } else {
                k.getLayoutParams().width = getMeasuredWidth();
                k.getLayoutParams().height = getMeasuredHeight();
            }

            k.setBackgroundDrawable(getBackground());
            k.getLayoutParams().width = getLayoutParams().width;
            k.setPopupCharacters(getPopupCharacters());
            k.setKeyShadow(shadowRadius, shadowColor);
            k.setKeyItemColor(keyTextColor);
            k.setKeyTextSize(label.getTextSize() / 2.5f);
            k.setKeyTextStyle(textStyle);
            k.setText(getText());
            k.setSubText(getSubText());
            k.setPopupCharacters(popupCharacters);

            k.setKeyIcon(getKeyIcon());
            k.setKeyImageVisible(isKeyIconSet());
        }
    }
}
