package org.blinksd.board;

import static android.os.Build.VERSION.SDK_INT;
import static org.blinksd.board.SuperBoardApplication.clearCustomFont;
import static org.blinksd.board.SuperBoardApplication.getAppDB;
import static org.blinksd.board.SuperBoardApplication.getBackgroundImageFile;
import static org.blinksd.board.SuperBoardApplication.getCustomFont;
import static org.blinksd.board.SuperBoardApplication.getDictDB;
import static org.blinksd.board.SuperBoardApplication.getIconThemes;
import static org.blinksd.board.SuperBoardApplication.getKeyboardLanguage;
import static org.blinksd.board.SuperBoardApplication.getMonetColors;
import static org.blinksd.board.SuperBoardApplication.getNextLanguage;
import static org.blinksd.board.SuperBoardApplication.getResConfiguration;
import static org.blinksd.board.SuperBoardApplication.isDictDBReady;
import static org.blinksd.board.SuperBoardApplication.isWatchDevice;
import static org.blinksd.utils.ColorUtils.convertARGBtoRGB;
import static org.blinksd.utils.DensityUtils.getFloatNumberFromInt;
import static org.blinksd.utils.DensityUtils.hp;
import static org.blinksd.utils.DensityUtils.minPInt;
import static org.blinksd.utils.LayoutUtils.getLayoutKeys;
import static org.blinksd.utils.LayoutUtils.getSpecialCases;
import static org.blinksd.utils.LayoutUtils.setKeyOpts;
import static org.blinksd.utils.LayoutUtils.setSpaceBarViewPrefs;
import static org.blinksd.utils.SuperDBHelper.getBooleanOrDefault;
import static org.blinksd.utils.SuperDBHelper.getBooleanOrDefaultResolved;
import static org.blinksd.utils.SuperDBHelper.getFloatPercentOrDefault;
import static org.blinksd.utils.SuperDBHelper.getFloatedIntOrDefault;
import static org.blinksd.utils.SuperDBHelper.getIntOrDefault;
import static org.blinksd.utils.SuperDBHelper.getStringOrDefault;
import static org.blinksd.utils.SuperDBHelper.removeKeyFromDB;
import static org.blinksd.utils.SystemUtils.createNavbarLayout;
import static org.blinksd.utils.SystemUtils.detectNavbar;
import static org.blinksd.utils.SystemUtils.disableEdgeToEdge;
import static org.blinksd.utils.SystemUtils.isColorized;
import static org.blinksd.utils.SystemUtils.isLand;
import static org.blinksd.utils.SystemUtils.navbarH;
import static org.blinksd.utils.WindowManagerServiceUtils.navbarAndroid9ModeEnabled;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.os.Build;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.blinksd.board.activities.SetupActivityV2;
import org.blinksd.board.views.BoardPopup;
import org.blinksd.board.views.BottomKeyboardBarView;
import org.blinksd.board.views.ClipboardView;
import org.blinksd.board.views.SuggestionLayoutV2;
import org.blinksd.board.views.SuperBoard;
import org.blinksd.board.views.emoji.EmojiCategoryViewV2;
import org.blinksd.board.views.emoji.EmojiViewV2;
import org.blinksd.utils.ColorUtils;
import org.blinksd.utils.DensityUtils;
import org.blinksd.utils.IconThemeUtils;
import org.blinksd.utils.ImageUtils;
import org.blinksd.utils.LocalIconTheme;
import org.blinksd.utils.ResourcesUtils;
import org.blinksd.utils.SettingMap;
import org.blinksd.utils.keys.KeyRemapper;
import org.blinksd.utils.superboard.KeyOptions;
import org.blinksd.utils.superboard.KeyboardType;
import org.blinksd.utils.superboard.Language;
import org.blinksd.utils.superboard.OnModifierChangedListener;
import org.blinksd.utils.superboard.RowOptions;

import java.io.File;
import java.util.List;

@SuppressWarnings({"deprecation", "InlinedApi"})
public final class InputService extends InputMethodService implements
        SuggestionLayoutV2.OnSuggestionSelectedListener {

    private SuperBoard superBoardView = null;
    private BoardPopup boardPopup = null;
    private String appName;
    private LinearLayout keyboardLayoutHolder = null;
    private SuggestionLayoutV2 suggestionLayout = null;
    private RelativeLayout keyboardBackgroundHolder = null;
    private ImageView keyboardBackground = null;
    private Language currentLanguageCache;
    private EmojiViewV2 emojiView = null;
    private ClipboardView clipboardView = null;
    private BottomKeyboardBarView bottomKeyboardBarView = null;
    private KeyRemapper keyRemapper;
    private boolean hiddenBySelf = false;
    Intent settingsActivity = null;
    final Runnable onColorsLoadedListener = this::setPrefs;

    private final View.OnClickListener emojiClick = v -> {
        final int num = Integer.parseInt(v.getTag().toString());
        switch (num) {
            case -1:
                showEmojiView(false);
                break;
            case 10:
                superBoardView.sendKeyEvent(KeyEvent.KEYCODE_DEL);
                break;
        }
    };

    @Override
    public boolean onEvaluateInputViewShown() {
        boolean defValue = super.onEvaluateInputViewShown();
        return defValue || getBooleanOrDefault(SettingMap.SET_FORCE_SHOW_KEYBOARD_PHYSICAL);
    }

    @Override
    public boolean onEvaluateFullscreenMode() {
        return getBooleanOrDefault(SettingMap.SET_SHOW_FULLSCREEN_KEYBOARD_FORCED) ||
                (getBooleanOrDefault(SettingMap.SET_SHOW_FULLSCREEN_KEYBOARD) &&
                        super.onEvaluateFullscreenMode());
    }

    @Override
    public void onUpdateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd, int candidatesStart, int candidatesEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd);
        sendCompletionRequest();
    }

    @Override
    public void onSuggestionSelected(CharSequence text, CharSequence oldText, CharSequence suggestion) {
        if (superBoardView == null) return;
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;

        int state = superBoardView.getShiftState();
        if (state == SuperBoard.SHIFT_OFF && Character.isUpperCase(oldText.charAt(0))) {
            state = SuperBoard.SHIFT_ON;
        }

        switch (state) {
            case SuperBoard.SHIFT_OFF:
                suggestion = suggestion.toString().toLowerCase();
                break;
            case SuperBoard.SHIFT_LOCKED:
                suggestion = suggestion.toString().toUpperCase();
                break;
            case SuperBoard.SHIFT_ON:
                String first = String.valueOf(suggestion.charAt(0));
                String other = suggestion.toString().toLowerCase();
                other = other.substring(1);
                first = first.toUpperCase();
                suggestion = first + other;
                break;
        }

        ExtractedTextRequest req = new ExtractedTextRequest();
        ExtractedText exText = ic.getExtractedText(req, 0);
        String exTextStr = exText.text.toString();
        exTextStr = exTextStr.substring(text.length() - 1);

        ic.deleteSurroundingText(oldText.length(), exTextStr.indexOf(' '));
        suggestion += " ";
        ic.commitText(suggestion, suggestion.length());

        req = new ExtractedTextRequest();
        exText = ic.getExtractedText(req, 0);
        exTextStr = exText.text.toString();
        int pos = exTextStr.indexOf(' ', exText.selectionStart);
        ic.setSelection(pos, pos);

        superBoardView.afterKeyboardEvent();

        getDictDB().increaseUsageCount(
                        currentLanguageCache.language.split("_")[0],
                        suggestion.toString().trim());
    }

    @Override
    public void onCreate() {
        super.onCreate();

        getAppDB().setOnSettingsApplyListener(this::setPrefs);
        getMonetColors().registerOnColorsExtractedListener(onColorsLoadedListener);
    }

    @Override
    public void onDestroy() {
        getMonetColors().unregisterOnColorsExtractedListener(onColorsLoadedListener);
        super.onDestroy();
    }

    @Override
    public View onCreateInputView() {
        return setLayout();
    }

    @Override
    public void setInputView(View view) {
        if (view.getParent() != null) {
            ((ViewGroup) view.getParent()).removeView(view);
        }

        super.setInputView(view);
    }

    @Override
    public void onWindowHidden() {
        if (getBooleanOrDefault(SettingMap.SET_KILL_BACKGROUND)) {
            System.exit(0);
        }

        if (superBoardView != null) {
            superBoardView.stopAllKeyEvents();
            superBoardView.setEnabledLayout(0);
        }

        showEmojiView(false);
        showClipboardView(false);
        showLanguageSelectorView(false);

        onFinishInput();
        super.onWindowHidden();

        if (hiddenBySelf) {
            hiddenBySelf = false;
            return;
        }

        if (getBooleanOrDefault(SettingMap.SET_PREVENT_KBD_CLOSE)) {
            requestShowSelf(InputMethodManager.SHOW_IMPLICIT);
        }

        System.gc();
    }

    @Override
    public void requestHideSelf(int flags) {
        hiddenBySelf = true;
        super.requestHideSelf(flags);
    }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);

        if (superBoardView != null) {
            setPrefs();
            superBoardView.updateKeyState();
        }
    }

    @Override
    public void onFinishInput() {
        super.onFinishInput();
        if (superBoardView != null) {
            superBoardView.updateKeyState();
        }

        if (boardPopup != null) {
            boardPopup.showPopup(false);
            boardPopup.clear();
        }

        if (suggestionLayout != null)
            suggestionLayout.setCompletion(superBoardView, null, null);
    }

    public void sendCompletionRequest() {
        boolean sugDisabled = suggestionLayout == null ||
                !isDictDBReady() ||
                superBoardView.isDisabledSuggestionsTemporarily() ||
                getBooleanOrDefault(SettingMap.SET_DISABLE_TOP_BAR) ||
                getBooleanOrDefault(SettingMap.SET_DISABLE_SUGGESTIONS);
        if (superBoardView == null) return;
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        CharSequence text = ic.getTextBeforeCursor(Integer.MAX_VALUE, 0);
        if (text != null && !sugDisabled) suggestionLayout.setCompletionText(superBoardView, text, currentLanguageCache.language);
    }

    @SuppressLint("ResourceType")
    private View setLayout() {
        if (superBoardView == null) {
            superBoardView = new SuperBoardImpl(this,
                    (keyCode, modifierValue) -> suggestionLayout.changeFABKeyState(keyCode, modifierValue > 0));
            superBoardView.setFocusable(false);

            superBoardView.setLayoutParams(new LinearLayout.LayoutParams(-1, -1, 1));
            appName = getString(R.string.app_name);
            String abc = "ABC";
            String[][] kbdSym1 = {
                    {"[", "]", "θ", "÷", "<", ">", "`", "´", "{", "}"},
                    {"©", "£", "€", "+", "®", "¥", "π", "Ω", "λ", "β"},
                    {"@", "#", "$", "%", "&", "*", "-", "=", "(", ")"},
                    {"S2", "!", "\"", "'", ":", ";", "/", "?", ""},
                    {abc, ",", appName, ".", ""}
            }, kbdSym2 = {
                    {"√", "ℕ", "★", "×", "™", "‰", "∛", "^", "~", "±"},
                    {"♣", "♠", "♪", "♥", "♦", "≈", "Π", "¶", "§", "∆"},
                    {"←", "↑", "↓", "→", "∞", "≠", "_", "℅", "‘", "’"},
                    {"S1", "¡", "•", "°", "¢", "|", "\\", "¿", ""},
                    {abc, "₺", appName, "…", ""}
            }, kbdSym3 = {
                    {"INS", "HOME",  "↑",     "P↑",    "ESC"        },
                    {"BS",  "←",     "ENTER", "→",     "TAB"        },
                    {"DEL", "END",   "↓",     "P↓",    "MENU"       },
                    {"CUT", "COPY",  "PASTE",                       },
                    {"F1",  "F2",    "F3",    "F4",    "F5",   "F6" },
                    {"F7",  "F8",    "F9",    "F10",   "F11",  "F12"},
                    {abc,   "PREV",  "PLAY",  "PAUSE", "NEXT",      }
            }, kbdNum = {
                    {"-", ".", ",", abc},
                    {"1", "2", "3", "+"},
                    {"4", "5", "6", ";"},
                    {"7", "8", "9", ""},
                    {"*", "0", "#", ""}
            }, kbdMath1 = {
                    {"¹", "²", "³", "⁴", "⁵", "⁶", "⁷", "⁸", "⁹", "⁰"},
                    {"₁", "₂", "₃", "₄", "₅", "₆", "₇", "₈", "₉", "₀"},
                    {"⁺", "⁻", "⁼", "⁽", "⁾"},
                    {"₊", "₋", "₌", "₍", "₎"},
                    {abc,      "",        ""}
            };

            loadKeyboardLayout();

            superBoardView.createLayoutWithRows(kbdSym1,  KeyboardType.SYMBOL);
            superBoardView.createLayoutWithRows(kbdSym2,  KeyboardType.SYMBOL);
            superBoardView.createLayoutWithRows(kbdSym3,  KeyboardType.FN);
            superBoardView.createLayoutWithRows(kbdMath1, KeyboardType.MATH);
            superBoardView.createLayoutWithRows(kbdNum,   KeyboardType.NUMBER);

            superBoardView.setPressEventForKey(1, 3, 0, Keyboard.KEYCODE_ALT);

            superBoardView.setPressEventForKey(-1, 0, -1, Keyboard.KEYCODE_ALT);
            superBoardView.setPressEventForKey(-1, -2, -1, Keyboard.KEYCODE_DELETE);
            superBoardView.setKeyRepeat(-1, -2, -1);
            superBoardView.setPressEventForKey(-1, -1, -1, Keyboard.KEYCODE_DONE);

            superBoardView.setPressEventForKey(4, -1, 0, Keyboard.KEYCODE_ALT);
            superBoardView.setKeyRepeat(4, -1, -2);
            superBoardView.setPressEventForKey(4, -1, -2, Keyboard.KEYCODE_DELETE);
            superBoardView.setPressEventForKey(4, -1, -1, Keyboard.KEYCODE_DONE);

            superBoardView.setPressEventForKey(3, 0, 0, KeyEvent.KEYCODE_INSERT);
            superBoardView.setPressEventForKey(3, 0, 1, KeyEvent.KEYCODE_MOVE_HOME);
            superBoardView.setPressEventForKey(3, 0, 2, KeyEvent.KEYCODE_DPAD_UP);
            superBoardView.setKeyRepeat(3, 0, 2);
            superBoardView.setPressEventForKey(3, 0, 3, KeyEvent.KEYCODE_PAGE_UP);
            superBoardView.setKeyRepeat(3, 0, 3);
            superBoardView.setPressEventForKey(3, 0, 4, KeyEvent.KEYCODE_ESCAPE);

            superBoardView.setPressEventForKey(3, 1, 0, KeyEvent.KEYCODE_FORWARD_DEL);
            superBoardView.setPressEventForKey(3, 1, 1, KeyEvent.KEYCODE_DPAD_LEFT);
            superBoardView.setKeyRepeat(3, 1, 1);
            superBoardView.setPressEventForKey(3, 1, 2, '\n', false);
            superBoardView.setPressEventForKey(3, 1, 3, KeyEvent.KEYCODE_DPAD_RIGHT);
            superBoardView.setKeyRepeat(3, 1, 3);
            superBoardView.setPressEventForKey(3, 1, 4, KeyEvent.KEYCODE_TAB);

            superBoardView.setPressEventForKey(3, 2, 0, KeyEvent.KEYCODE_DEL);
            superBoardView.setPressEventForKey(3, 2, 1, KeyEvent.KEYCODE_MOVE_END);
            superBoardView.setPressEventForKey(3, 2, 2, KeyEvent.KEYCODE_DPAD_DOWN);
            superBoardView.setKeyRepeat(3, 2, 2);
            superBoardView.setPressEventForKey(3, 2, 3, KeyEvent.KEYCODE_PAGE_DOWN);
            superBoardView.setKeyRepeat(3, 2, 3);
            superBoardView.setPressEventForKey(3, 2, 4, KeyEvent.KEYCODE_MENU);

            superBoardView.setPressEventForKey(3, 3, 0, KeyEvent.KEYCODE_CUT);
            superBoardView.setPressEventForKey(3, 3, 1, KeyEvent.KEYCODE_COPY);
            superBoardView.setPressEventForKey(3, 3, 2, KeyEvent.KEYCODE_PASTE);

            superBoardView.setPressEventForKey(3, 6, 1, KeyEvent.KEYCODE_MEDIA_PREVIOUS);
            superBoardView.setKeyDrawable(3, 6, 1, android.R.drawable.ic_media_previous);

            superBoardView.setPressEventForKey(3, 6, 2, KeyEvent.KEYCODE_MEDIA_PLAY);
            superBoardView.setKeyDrawable(3, 6, 2, android.R.drawable.ic_media_play);

            superBoardView.setPressEventForKey(3, 6, 3, KeyEvent.KEYCODE_MEDIA_PAUSE);
            superBoardView.setKeyDrawable(3, 6, 3, android.R.drawable.ic_media_pause);

            superBoardView.setPressEventForKey(3, 6, 4, KeyEvent.KEYCODE_MEDIA_NEXT);
            superBoardView.setKeyDrawable(3, 6, 4, android.R.drawable.ic_media_next);

            superBoardView.setPressEventForKey(3, -1, 0, Keyboard.KEYCODE_MODE_CHANGE);

            // superBoardView.setDisableModifierForKeyboard(3, true);

            // set Fx buttons
            for (int i = 4; i < 6; i++) {
                for (int g = 0; g < 6; g++) {
                    final int fIdx = ((i - 4) * 6) + g;
                    superBoardView.setPressEventForKey(3, i, g, KeyEvent.KEYCODE_F1 + fIdx);
                }
            }

            for (int i = 1; i < 3; i++) {
                superBoardView.setRowPadding(i, 2, DensityUtils.wpInt(2));
                superBoardView.setKeyRepeat(i, 3, -1);
                superBoardView.setKeyRepeat(i, 4, 2);
                superBoardView.setPressEventForKey(i, 3, -1, Keyboard.KEYCODE_DELETE);
                superBoardView.setPressEventForKey(i, -1, 0, Keyboard.KEYCODE_MODE_CHANGE);
                superBoardView.setPressEventForKey(i, -1, 2, KeyEvent.KEYCODE_SPACE);
                superBoardView.setPressEventForKey(i, -1, -1, Keyboard.KEYCODE_DONE);
                superBoardView.setLongPressEventForKey(i, -1, 0, SuperBoard.KEYCODE_CLOSE_KEYBOARD);
                superBoardView.setKeyWidthPercent(i, 3, 0, 15);
                superBoardView.setKeyWidthPercent(i, 3, -1, 15);
                superBoardView.setKeyWidthPercent(i, -1, 0, 20);
                superBoardView.setKeyWidthPercent(i, -1, 1, 15);
                superBoardView.setKeyWidthPercent(i, -1, 2, 50);
                superBoardView.setKeyWidthPercent(i, -1, 3, 15);
                superBoardView.setKeyWidthPercent(i, -1, -1, 20);
            }
        }

        if (emojiView == null) {
            emojiView = new EmojiViewV2(this, new EmojiCategoryViewV2.OnEmojiClickListener() {
                @Override
                public void onEmojiClick(String emoji) {
                    superBoardView.commitText(emoji);
                }

                /*
                @Override
                public boolean onEmojiLongClick(Emoji emoji) {
                    if (!emoji.skinTones.isEmpty()) {
                        SuperBoard.Key key = superBoardView.createKey(emoji.emoji);
                        key.setPopupCharacters(emoji.skinTones.toArray(new String[0]));
                        boardPopup.setKey(superBoardView, key, true);
                        boardPopup.showPopup(true, true);

                        return true;
                    }

                    return false;
                }
                 */
            }, emojiClick);
            emojiView.setLayoutParams(new LinearLayout.LayoutParams(-1, -1, 1));
            emojiView.setFocusable(false);
            emojiView.setVisibility(View.GONE);

            emojiView.setBackground(superBoardView.getBackground());
        }

        if (bottomKeyboardBarView == null) {
            bottomKeyboardBarView = new BottomKeyboardBarView(superBoardView, lang -> {
                showLanguageSelectorView(false);

                if (!lang.equals(currentLanguageCache)) {
                    getAppDB().putString(SettingMap.SET_KEYBOARD_LANG_SELECT, lang.language, true);
                    setPrefs();
                }
            });
            bottomKeyboardBarView.setLayoutParams(new LinearLayout.LayoutParams(-1, -2, 0));
        }

        if (keyboardLayoutHolder == null) {
            keyboardLayoutHolder = new LinearLayout(this);
            keyboardLayoutHolder.setFocusable(false);
            keyboardLayoutHolder.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
            keyboardLayoutHolder.setOrientation(LinearLayout.VERTICAL);
            suggestionLayout = new SuggestionLayoutV2(superBoardView);
            suggestionLayout.setFocusable(false);
            suggestionLayout.setLayoutParams(new LinearLayout.LayoutParams(-1, -2, 0));
            keyboardLayoutHolder.addView(suggestionLayout);
            keyboardLayoutHolder.addView(superBoardView);
            if (emojiView != null) {
                keyboardLayoutHolder.addView(emojiView);
            }

            if (bottomKeyboardBarView != null) {
                keyboardLayoutHolder.addView(bottomKeyboardBarView.languageSelectorView);
                keyboardLayoutHolder.addView(bottomKeyboardBarView);
            }
        }

        if (keyboardBackgroundHolder == null) {
            keyboardBackgroundHolder = new RelativeLayout(this);
            keyboardBackgroundHolder.setFocusable(false);
            keyboardBackgroundHolder.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
            keyboardBackground = new ImageView(this);
            keyboardBackground.setFocusable(false);
            keyboardBackgroundHolder.addView(keyboardBackground);
            keyboardBackgroundHolder.addView(keyboardLayoutHolder);
            keyboardBackground.setScaleType(ImageView.ScaleType.CENTER_CROP);
            keyboardBackground.setAdjustViewBounds(false);
        }

        if (boardPopup == null) {
            boardPopup = new BoardPopupImpl(keyboardBackgroundHolder);
            boardPopup.setFocusable(false);
            keyboardBackgroundHolder.addView(boardPopup);
        }

        setPrefs();
        return keyboardBackgroundHolder;
    }

    private void loadKeyRemapper() {
        if (keyRemapper == null) {
            keyRemapper = new KeyRemapper();
        }

        try {
            String lang = getStringOrDefault(SettingMap.SET_KEYBOARD_LANG_SELECT);
            var processedLangCode = lang.split("_");
            if (processedLangCode.length > 2) {
                processedLangCode[2] = processedLangCode[2].substring(0, 1);
            }
            var finalLangCode = String.join("_", processedLangCode);

            if (finalLangCode.equals(keyRemapper.currentLang)) {
                return;
            }

            keyRemapper.setKeyMapFromIS(getAssets().open(String.format("keymaps/%s.kcm", finalLangCode)));
            keyRemapper.currentLang = finalLangCode;
            // Log.d(getClass().getSimpleName(), "Key remapper loaded for " + lang + " " + finalLangCode);
        } catch (Throwable e) {
            // Log.d(getClass().getSimpleName(), e.getMessage(), e);
            keyRemapper.setKeyMapFromFileContent(null);
            keyRemapper.currentLang = null;
        }
    }

    public void setPrefs() {
        if (superBoardView != null) {
            var heightIncreaser = getFloatedIntOrDefault(SettingMap.SET_LANDSCAPE_HEIGHT_INCREASER);
            superBoardView.setLandscapeHeightIncreaser(heightIncreaser);
            superBoardView.setRecentConfiguration(getResConfiguration());

            setKeyOpts(currentLanguageCache, superBoardView);
            IconThemeUtils icons = getIconThemes();
            superBoardView.setKeyDrawable(4, -1, -2, icons.getIconResource(LocalIconTheme.SYM_TYPE_DELETE));
            superBoardView.setKeyDrawable(4, -1, -1, icons.getIconResource(LocalIconTheme.SYM_TYPE_ENTER));
            superBoardView.setKeyDrawable(-1, -2, -1, icons.getIconResource(LocalIconTheme.SYM_TYPE_DELETE));
            superBoardView.setKeyDrawable(-1, -1, -1, icons.getIconResource(LocalIconTheme.SYM_TYPE_ENTER));
            superBoardView.setKeyDrawable(3, 1, 2, icons.getIconResource(LocalIconTheme.SYM_TYPE_ENTER));
            List<Integer> indexes = superBoardView.findKeyboardIndexes(KeyboardType.SYMBOL);
            for (int i : indexes) {
                superBoardView.setKeyDrawable(i, 3, -1, icons.getIconResource(LocalIconTheme.SYM_TYPE_DELETE));
                superBoardView.setKeyDrawable(i, 4, -1, icons.getIconResource(LocalIconTheme.SYM_TYPE_ENTER));
                setSpaceBarViewPrefs(icons, superBoardView.getKey(i, 4, 2), appName);
            }
            superBoardView.setShiftDetection(getBooleanOrDefault(SettingMap.SET_DETECT_CAPSLOCK));
            superBoardView.setEnforcedShiftDetection(getBooleanOrDefault(SettingMap.SET_ENFORCE_DETECT_CAPSLOCK));
            superBoardView.setEnforcedEditorAction(getBooleanOrDefault(SettingMap.SET_ENFORCE_EDITOR_ACTION));
            superBoardView.setRepeating(!getBooleanOrDefault(SettingMap.SET_DISABLE_REPEAT));
            superBoardView.updateKeyState();
            int kbdHeight = getIntOrDefault(SettingMap.SET_KEYBOARD_HEIGHT);
            superBoardView.setKeyboardHeight(kbdHeight);
            superBoardView.fixHeight();
            superBoardView.setKeysPadding(minPInt(getFloatNumberFromInt(getIntOrDefault(SettingMap.SET_KEY_PADDING))));
            File img;
            int c = getIntOrDefault(SettingMap.SET_KEYBOARD_BGCLR);
            if (getMonetColors().isMonetEnabled()) {
                if (keyboardBackgroundHolder != null) {
                    keyboardBackground.setImageBitmap(null);
                }
            } else {
                img = getBackgroundImageFile();
                if (keyboardBackgroundHolder != null) {
                    if (img.exists()) {
                        int blur = getIntOrDefault(SettingMap.SET_KEYBOARD_BGBLUR);
                        Bitmap b = BitmapFactory.decodeFile(img.getAbsolutePath());
                        keyboardBackground.setImageBitmap(blur > 0 ? ImageUtils.getBlur(b, blur) : b);
                    } else {
                        keyboardBackground.setImageBitmap(null);
                        c = convertARGBtoRGB(c);
                    }
                }
            }

            keyboardLayoutHolder.setBackgroundColor(c);
            superBoardView.setBackgroundColor(Color.TRANSPARENT);

            int keyClr = getIntOrDefault(SettingMap.SET_KEY_BGCLR);
            int keyPressClr = getIntOrDefault(SettingMap.SET_KEY_PRESS_BGCLR);
            superBoardView.setKeysBackground(ResourcesUtils.getKeyBg(keyClr, keyPressClr, true));
            int shr = getIntOrDefault(SettingMap.SET_KEY_SHADOWSIZE),
                    shc = getIntOrDefault(SettingMap.SET_KEY_SHADOWCLR);
            superBoardView.setKeysShadow(shr, shc);
            superBoardView.setInsertSpaceAfterPunc(getBooleanOrDefault(SettingMap.SET_INSERT_SPACE_AFTER_PUNC));
            superBoardView.setLongPressFastDelete(getBooleanOrDefault(SettingMap.SET_ENABLE_LONG_PRESS_FAST_DELETE));
            superBoardView.setLongPressMultiplier(getIntOrDefault(SettingMap.SET_KEY_LONGPRESS_DURATION));
            superBoardView.setKeyVibrateDuration(getIntOrDefault(SettingMap.SET_KEY_VIBRATE_DURATION));
            superBoardView.setKeysTextColor(getIntOrDefault(SettingMap.SET_KEY_TEXTCLR));
            superBoardView.setKeysTextSize(getFloatPercentOrDefault(SettingMap.SET_KEY_TEXTSIZE));
            superBoardView.setKeysTextType(getIntOrDefault(SettingMap.SET_KEYBOARD_TEXTTYPE_SELECT));
            superBoardView.setIconSizeMultiplier(getIntOrDefault(SettingMap.SET_KEY_ICON_SIZE_MULTIPLIER));
            superBoardView.setKeysPopupPreviewEnabled(getBooleanOrDefault(SettingMap.SET_ENABLE_POPUP_PREVIEW));
            superBoardView.setKeyboardIndicatorHeight(DensityUtils.getFloatNumberFromInt(getIntOrDefault(SettingMap.SET_KEY_INDICATOR_HEIGHT)));
            int key2BgClr = getIntOrDefault(SettingMap.SET_KEY2_BGCLR);
            int key2BgPressedClr = getIntOrDefault(SettingMap.SET_KEY2_PRESS_BGCLR);
            int key2TextClr = getIntOrDefault(SettingMap.SET_KEY2_TEXTCLR);
            int enterBgClr = getIntOrDefault(SettingMap.SET_ENTER_BGCLR);
            int enterBgPressClr = getIntOrDefault(SettingMap.SET_ENTER_PRESS_BGCLR);
            int enterTextClr = getIntOrDefault(SettingMap.SET_ENTER_TEXTCLR);
            Drawable key2Bg = ResourcesUtils.getKeyBg(key2BgClr, key2BgPressedClr, true);
            Drawable enterBg = ResourcesUtils.getKeyBg(enterBgClr, enterBgPressClr, true);
            for (int i = 1; i < superBoardView.getChildCount() - 1; i++) {
                if (i < 3) {
                    superBoardView.setKeyBackgroundAndItemColor(i, 3, 0, key2Bg, key2TextClr);
                    superBoardView.setKeyBackgroundAndItemColor(i, 3, -1, key2Bg, key2TextClr);
                    for (int h = 3; h < 5; h++) superBoardView.setKeyBackgroundAndItemColor(i, h, 0, key2Bg, key2TextClr);
                    superBoardView.setKeyBackgroundAndItemColor(i, 4, 1, key2Bg, key2TextClr);
                    superBoardView.setKeyBackgroundAndItemColor(i, 4, 3, key2Bg, key2TextClr);
                }
                if (i != 3) superBoardView.setKeyBackgroundAndItemColor(i, -1, -1, enterBg, enterTextClr);
            }
            superBoardView.setDisablePopup(getBooleanOrDefault(SettingMap.SET_DISABLE_POPUP));
            boolean isDBEmpty = getDictDB()
                    .getTableLength(currentLanguageCache.language.split("_")[0]) < 1;
            boolean topBarDisabled = getBooleanOrDefault(SettingMap.SET_DISABLE_TOP_BAR);
            boolean sugDisabled = topBarDisabled || getBooleanOrDefault(SettingMap.SET_DISABLE_SUGGESTIONS) || isDBEmpty;
            boolean fnDisabled = topBarDisabled || getBooleanOrDefault(SettingMap.SET_HIDE_TOP_BAR_FN_BUTTONS);
            boolean numDisabled = !topBarDisabled && getBooleanOrDefault(SettingMap.SET_DISABLE_NUMBER_ROW);
            boolean showFABRight = !topBarDisabled && getBooleanOrDefault(SettingMap.SET_SHOW_FAB_RIGHT);
            superBoardView.setPressEventForKey(2, 3, 0,
                    fnDisabled ? Keyboard.KEYCODE_ALT : Keyboard.KEYCODE_CANCEL);
            superBoardView.getKey(2, 3, 0).setText(topBarDisabled || fnDisabled ? "S3" : "S1");
            suggestionLayout.setVisibility(sugDisabled && topBarDisabled ? View.GONE : View.VISIBLE);
            suggestionLayout.setOnSuggestionSelectedListener(sugDisabled ? null : this);
            suggestionLayout.setReversed(showFABRight);
            String lang = getStringOrDefault(SettingMap.SET_KEYBOARD_LANG_SELECT);
            if (!lang.equals(currentLanguageCache.language)) {
                loadKeyboardLayout();
            }
            List<RowOptions> kOpt = currentLanguageCache.layout;
            for (int i = 0; i < kOpt.size(); i++) {
                RowOptions subKOpt = kOpt.get(i);
                for (int g = 0; g < subKOpt.keys.size(); g++) {
                    KeyOptions ko = subKOpt.keys.get(g);
                    if (ko.darkerKeyTint) {
                        superBoardView.setKeyBackgroundAndItemColor(0, i, g, key2Bg, key2TextClr);
                    }

                    if (ko.pressKeyCode == Keyboard.KEYCODE_DONE) {
                        superBoardView.setKeyBackgroundAndItemColor(0, i, g, enterBg, enterTextClr);
                    }
                }
            }

            superBoardView.getRow(0, 0).setVisibility(numDisabled ? View.GONE : View.VISIBLE);

            superBoardView.setKeyboardLanguage(currentLanguageCache.language);
            if (emojiView != null) {
                emojiView.applyTheme(superBoardView);
                emojiView.getLayoutParams().height = superBoardView.getKeyboardHeight();
            }
            clearCustomFont();
            getCustomFont();

            boolean enableClipboard = getBooleanOrDefaultResolved(SettingMap.SET_ENABLE_CLIPBOARD);

            if (enableClipboard && clipboardView == null) {
                clipboardView = new ClipboardView(superBoardView, v -> showClipboardView(false));
                clipboardView.setVisibility(View.GONE);

                if (bottomKeyboardBarView == null) {
                    keyboardLayoutHolder.addView(clipboardView);
                } else {
                    keyboardLayoutHolder.addView(clipboardView, keyboardLayoutHolder.indexOfChild(bottomKeyboardBarView) - 1);
                }
            } else if (!enableClipboard) {
                if (clipboardView != null) {
                    clipboardView.clearClipboard(false);
                    clipboardView.deInit();
                    keyboardLayoutHolder.removeView(clipboardView);
                    clipboardView = null;
                }

                removeKeyFromDB(SettingMap.SET_CLIPBOARD_HISTORY);
            }

            int kbdHeightInPixels = (int) (hp(kbdHeight) * (isLand() ? heightIncreaser : 1));
            int textKbdRowCount = superBoardView.getLayoutRowCount(superBoardView.findTextKeyboardIndex());
            int barHeight = kbdHeightInPixels / textKbdRowCount;

            if (clipboardView != null) {
                clipboardView.onPrimaryClipChanged();
                clipboardView.getLayoutParams().height = kbdHeightInPixels;
                clipboardView.reTheme();
            }

            if (bottomKeyboardBarView != null) {
                bottomKeyboardBarView.setBackground(superBoardView.getBackground());
                bottomKeyboardBarView.getLayoutParams().height = barHeight;
                bottomKeyboardBarView.languageSelectorView.getLayoutParams().height = kbdHeightInPixels;
                bottomKeyboardBarView.reTheme();
            }

            if (suggestionLayout != null) {
                suggestionLayout.getLayoutParams().height = barHeight;
                suggestionLayout.reTheme();
            }

            adjustNavbar(c);
        }

        sendCompletionRequest();
        updateInputViewShown();
    }

    private void loadKeyboardLayout() {
        String lang = getStringOrDefault(SettingMap.SET_KEYBOARD_LANG_SELECT);
        int keyboardIndex = superBoardView.findTextKeyboardIndex();
        Language language = getKeyboardLanguage(lang);
        if (!language.language.equals(lang)) {
            throw new RuntimeException("Where is the layout JSON file (in assets)?");
        }
        String[][] lkeys = getLayoutKeys(language.layout);
        superBoardView.replaceTextKeyboard(lkeys);
        superBoardView.setLayoutPopup(keyboardIndex, getLayoutKeys(language.popup));
        for (int i = 0; i < language.layout.size(); i++) {
            RowOptions opts = language.layout.get(i);
            if (opts.enablePadding) {
                superBoardView.setRowPadding(0, i, DensityUtils.wpInt(2));
            }
        }
        superBoardView.setKeyboardLanguage(language.language);
        setKeyOpts(language, superBoardView);
        currentLanguageCache = language;
    }

    @SuppressLint("ResourceType")
    private void adjustNavbar(int c) {
        int kbdPadding = getFloatPercentOrDefault(SettingMap.SET_KEYBOARD_PADDING);
        superBoardView.setPadding(kbdPadding, 0, kbdPadding, kbdPadding);

        int calculatedHeight = superBoardView.getKeyboardHeight() + kbdPadding;
        if (suggestionLayout.getVisibility() == View.VISIBLE) {
            calculatedHeight += suggestionLayout.getLayoutParams().height;
        }

        if (bottomKeyboardBarView != null && bottomKeyboardBarView.getVisibility() == View.VISIBLE) {
            calculatedHeight += bottomKeyboardBarView.getLayoutParams().height;
        }

        if (detectNavbar(this)) {
            Window w = getWindow().getWindow();
            assert w != null : "Window returned null";

            View navbarView = keyboardLayoutHolder.findViewById(android.R.attr.gravity);
            if (navbarView != null)
                keyboardLayoutHolder.removeView(navbarView);

            if (SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                disableEdgeToEdge(w);
                w.setDecorFitsSystemWindows(true);
            }

            if (navbarAndroid9ModeEnabled() && !isColorized()) {
                w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                w.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

                boolean monetEnabled = getMonetColors().isMonetEnabled();
                int color = monetEnabled
                        ? getMonetColors().getKeyboardColor()
                        : convertARGBtoRGB(c);
                w.setNavigationBarColor(color);
                w.getDecorView().setSystemUiVisibility(ColorUtils.satisfiesTextContrast(color)
                        ? View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                        : View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            } else if (isColorized()) {
                if (SDK_INT >= Build.VERSION_CODES.R)
                    w.setDecorFitsSystemWindows(false);

                // I found a bug at SDK 30 (Android R)
                // FLAG_LAYOUT_NO_LIMITS not working
                // set FLAG_TRANSLUCENT_NAVIGATION for this SDK only
                if (SDK_INT == Build.VERSION_CODES.R)
                    w.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                else w.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                w.setNavigationBarColor(0);

                calculatedHeight += navbarH(this);
                keyboardLayoutHolder.addView(createNavbarLayout(this, c));
            } else {
                w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                w.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                w.setNavigationBarColor(Color.BLACK);
            }
        }

        if (keyboardBackground.getLayoutParams().height != calculatedHeight) {
            keyboardBackground.setLayoutParams(new RelativeLayout.LayoutParams(-1, calculatedHeight));
            keyboardLayoutHolder.getLayoutParams().height = keyboardBackground.getLayoutParams().height;
            boardPopup.setFilterHeight(keyboardLayoutHolder.getLayoutParams().height);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (boardPopup != null && boardPopup.isShown()) {
            boardPopup.showPopup(false);
        }

        if (event.isFromSource(InputDevice.SOURCE_KEYBOARD)) {
            // Log.d(getClass().getSimpleName(), "Source = KEYBOARD " + event.getScanCode());

            loadKeyRemapper();

            String replacement = keyRemapper.convertKey(event);
            // Log.d(getClass().getSimpleName(), "Replacement of " + event.getScanCode() + ": " + replacement);
            if (replacement != null) {
                getCurrentInputConnection().commitText(replacement, replacement.length());
                return true;
            }
        }

        // showEmojiView(false);
        return super.onKeyDown(keyCode, event);
    }

    private void showEmojiView(boolean value) {
        if (emojiView == null) {
            return;
        }

        if (emojiView.isShown() != value) {
            showClipboardView(false);
            emojiView.setVisibility(value ? View.VISIBLE : View.GONE);
            superBoardView.setVisibility(value ? View.GONE : View.VISIBLE);
            suggestionLayout.changeFABKeyState(KeyEvent.KEYCODE_KANA, value);
        }
    }

    private void showClipboardView(boolean value) {
        if (clipboardView == null) {
            return;
        }
        if (clipboardView.isShown() != value) {
            showEmojiView(false);
            showLanguageSelectorView(false);

            if (value) {
                clipboardView.reTheme();
            }

            clipboardView.setVisibility(value ? View.VISIBLE : View.GONE);
            superBoardView.setVisibility(value ? View.GONE : View.VISIBLE);
            suggestionLayout.changeFABKeyState(KeyEvent.KEYCODE_EISU, value);
        }
    }

    private void showLanguageSelectorView(boolean value) {
        if (bottomKeyboardBarView == null) {
            return;
        }

        if (bottomKeyboardBarView.languageSelectorView.isShown() != value) {
            showEmojiView(false);
            showClipboardView(false);

            if (value) {
                bottomKeyboardBarView.reTheme();
            }

            bottomKeyboardBarView.languageSelectorView.setVisibility(value ? View.VISIBLE : View.GONE);
            superBoardView.setVisibility(value ? View.GONE : View.VISIBLE);
        }
    }

    private boolean isLanguageSelectorViewShown() {
        return bottomKeyboardBarView != null && bottomKeyboardBarView.languageSelectorView.isShown();
    }

    private final class SuperBoardImpl extends SuperBoard {
        private boolean shown = false;
        private SuperBoardImpl(Context context, OnModifierChangedListener listener) {
            super(context, listener);
            setSpecialCases(getSpecialCases());
        }

        @Override
        public void onKeyboardEvent(Key key) {
            if (emojiView != null && emojiView.isShown()) {
                showEmojiView(false);
            }

            if (clipboardView != null && clipboardView.isShown()) {
                showClipboardView(false);
            }

            shown = boardPopup.isShown();
            if (shown) {
                boardPopup.showPopup(false);
                boardPopup.clear();
                return;
            }

            boolean showPopup = !isWatchDevice() && getBooleanOrDefault(SettingMap.SET_KEYBOARD_SHOW_POPUP);
            boolean disablePopup = getBooleanOrDefault(SettingMap.SET_DISABLE_POPUP);

            if (showPopup || !disablePopup)
                boardPopup.setKey(superBoardView, key);

            if (showPopup)
                boardPopup.showCharacter();
        }

        @Override
        public void onPopupEvent() {
            boardPopup.setShiftState(getShiftState());
            boardPopup.showPopup(true);
            boardPopup.setShiftState(getShiftState());
        }

        @Override
        public void afterPopupEvent() {
            super.afterPopupEvent();
            setShiftState(boardPopup.getShiftState());
        }

        @Override
        public void afterKeyboardEvent() {
            super.afterKeyboardEvent();

            if (!isWatchDevice() && getBooleanOrDefault(SettingMap.SET_KEYBOARD_SHOW_POPUP)) {
                boardPopup.hideCharacter();
            }

            // sendCompletionRequest();
        }

        private boolean isClipboardViewShown() {
            return clipboardView != null && clipboardView.isShown();
        }

        private boolean isEmojiViewShown() {
            return emojiView != null && emojiView.isShown();
        }

        private boolean sendKeyEventImpl(int code) {
            if (code != KeyEvent.KEYCODE_EISU && isClipboardViewShown()) {
                showClipboardView(false);
            }

            if (code != KeyEvent.KEYCODE_KANA && code != KeyEvent.KEYCODE_DEL && isEmojiViewShown()) {
                showEmojiView(false);
            }

            if (code != KeyEvent.KEYCODE_3D_MODE && isLanguageSelectorViewShown()) {
                showLanguageSelectorView(false);
            }

            switch (code) {
                case SuperBoard.KEYCODE_TOGGLE_CTRL:
                    toggleCtrlState();
                    return true;
                case SuperBoard.KEYCODE_TOGGLE_ALT:
                    toggleAltState();
                    return true;
                case KeyEvent.KEYCODE_KATAKANA_HIRAGANA: // math menu 1
                    int mathIndex = findMathKeyboardIndex();
                    setEnabledLayout(
                            getEnabledLayoutIndex() != mathIndex
                                    ? mathIndex
                                    : findTextKeyboardIndex()
                    );
                    return true;
                case KeyEvent.KEYCODE_HENKAN:  // symbol menu
                    int fnIndex = findFNKeyboardIndex();
                    setEnabledLayout(
                            getEnabledLayoutIndex() != fnIndex
                                    ? fnIndex
                                    : findTextKeyboardIndex()
                    );
                    return true;
                case KeyEvent.KEYCODE_NUM:     // number menu
                    int numIndex = findNumberKeyboardIndex();
                    setEnabledLayout(
                            getEnabledLayoutIndex() != numIndex
                                    ? numIndex
                                    : findTextKeyboardIndex()
                    );
                    return true;
                case KeyEvent.KEYCODE_EISU:    // clipboard menu
                    showClipboardView(!clipboardView.isShown());
                    return true;
                case KeyEvent.KEYCODE_KANA:    // emoji menu
                    showEmojiView(!emojiView.isShown());
                    return true;
                case KeyEvent.KEYCODE_3D_MODE: // language selector menu
                    showLanguageSelectorView(!bottomKeyboardBarView.languageSelectorView.isShown());
                    return true;
                case SuperBoard.KEYCODE_SETTINGS:
                    if (settingsActivity == null) {
                        settingsActivity = new Intent(getContext(), SetupActivityV2.class);
                        settingsActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    }

                    startActivity(settingsActivity);
                    return true;
            }

            return false;
        }

        @Override
        public void sendKeyEvent(int code) {
            if (sendKeyEventImpl(code)) {
                return;
            }

            super.sendKeyEvent(code);
        }

        @Override
        public void sendKeyboardEvent(Key key) {
            if (!key.hasNormalPressEvent() || !sendKeyEventImpl(key.getNormalPressEvent().first)) {
                if (!shown) super.sendKeyboardEvent(key);
                else shown = false;
            }
        }

        @Override
        public void switchLanguage() {
            if (getBooleanOrDefault(SettingMap.SET_KEYBOARD_LC_ON_EMOJI)) {
                getNextLanguage();
                setPrefs();
            } else {
                openEmojiLayout();
            }
        }

        @Override
        public void openEmojiLayout() {
            showEmojiView(true);
        }

        @Override
        public void playSound(int event) {
            if (!getBooleanOrDefault(SettingMap.SET_PLAY_SND_PRESS)) return;
            super.playSound(event);
        }
    }

    private final class BoardPopupImpl extends BoardPopup {
        public BoardPopupImpl(ViewGroup root) {
            super(root);
            setSpecialCases(getSpecialCases());
        }

        @Override
        public void afterKeyboardEvent() {
            superBoardView.afterPopupEvent();
        }

        @Override
        public void playSound(int event) {
            superBoardView.playSound(event);
        }
    }
}
