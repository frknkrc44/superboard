package org.blinksd.board;

import static android.os.Build.VERSION.SDK_INT;
import static org.blinksd.utils.DensityUtils.mpInt;
import static org.blinksd.utils.SystemUtils.createNavbarLayout;
import static org.blinksd.utils.SystemUtils.detectNavbar;
import static org.blinksd.utils.SystemUtils.isColorized;
import static org.blinksd.utils.SystemUtils.navbarH;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.os.Build;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.blinksd.board.views.BoardPopup;
import org.blinksd.board.views.ClipboardView;
import org.blinksd.board.views.EmojiView;
import org.blinksd.board.views.SuggestionLayout;
import org.blinksd.board.views.SuperBoard;
import org.blinksd.utils.ColorUtils;
import org.blinksd.utils.DensityUtils;
import org.blinksd.utils.IconThemeUtils;
import org.blinksd.utils.ImageUtils;
import org.blinksd.utils.LayoutUtils;
import org.blinksd.utils.LocalIconTheme;
import org.blinksd.utils.SettingMap;
import org.blinksd.utils.SuperDBHelper;
import org.blinksd.utils.superboard.KeyOptions;
import org.blinksd.utils.superboard.KeyboardType;
import org.blinksd.utils.superboard.Language;
import org.blinksd.utils.superboard.RowOptions;

import java.io.File;
import java.util.List;

@SuppressWarnings({"deprecation", "InlinedApi"})
public final class InputService extends InputMethodService implements
        SuggestionLayout.OnSuggestionSelectedListener {

    public static final String RESTART_KEYBOARD = "org.blinksd.board.KILL";
    private SuperBoard superBoardView = null;
    private BoardPopup boardPopup = null;
    private String[][][] predefinedLayouts = null;
    private String appName;
    private LinearLayout keyboardLayoutHolder = null;
    private SuggestionLayout suggestionLayout = null;
    private RelativeLayout keyboardBackgroundHolder = null;
    private ImageView keyboardBackground = null;
    private Language currentLanguageCache;
    private EmojiView emojiView = null;
    private ClipboardView clipboardView = null;
    private final BroadcastReceiver restartKeyboardReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context p1, Intent p2) {
            setPrefs();
        }
    };
    private Configuration recentConfiguration;

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

        SuperBoardApplication.getDictDB()
                .increaseUsageCount(
                        currentLanguageCache.language.split("_")[0],
                        suggestion.toString().trim());
    }

    @Override
    public View onCreateInputView() {
        setLayout();
        return keyboardBackgroundHolder;
    }

    @Override
    public void setInputView(View view) {
        if (view.getParent() != null) {
            if (recentConfiguration == null) {
                recentConfiguration = getResources().getConfiguration();
            }

            if (recentConfiguration.orientation != getResources().getConfiguration().orientation) {
                System.exit(0);
            }

            ((ViewGroup) view.getParent()).removeView(view);
        }

        super.setInputView(view);
    }

    @Override
    public void onWindowHidden() {
        if (SuperDBHelper.getBooleanOrDefault(SettingMap.SET_KILL_BACKGROUND)) {
            System.exit(0);
        }
        onFinishInput();
        super.onWindowHidden();
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
            superBoardView.resetToNormalLayout();
        }

        if (boardPopup != null) {
            boardPopup.showPopup(false);
            boardPopup.clear();
        }

        showEmojiView(false);
        showClipboardView(false);

        if (suggestionLayout != null)
            suggestionLayout.setCompletion(null, null);

        System.gc();
    }

    public void sendCompletionRequest() {
        boolean sugDisabled = suggestionLayout == null ||
                !SuperBoardApplication.isDictDBReady() ||
                SuperDBHelper.getBooleanOrDefault(SettingMap.SET_DISABLE_SUGGESTIONS);
        if (superBoardView == null) return;
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        CharSequence text = ic.getTextBeforeCursor(Integer.MAX_VALUE, 0);
        if (sugDisabled) suggestionLayout.toggleQuickMenu(true);
        if (text != null && !sugDisabled) suggestionLayout.setCompletionText(text, currentLanguageCache.language);
    }

    @SuppressLint({"ResourceType", "UnspecifiedRegisterReceiverFlag"})
    private void setLayout() {
        if (superBoardView == null) {
            superBoardView = new SuperBoardImpl(this);
            superBoardView.setFocusable(false);
            if (SDK_INT >= Build.VERSION_CODES.O) {
                registerReceiver(restartKeyboardReceiver,
                        new IntentFilter(RESTART_KEYBOARD), Context.RECEIVER_NOT_EXPORTED);
            } else {
                registerReceiver(restartKeyboardReceiver, new IntentFilter(RESTART_KEYBOARD));
            }
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
            }, kbdNums = {
                    {"-", ".", ",", "ABC"},
                    {"1", "2", "3", "+"},
                    {"4", "5", "6", ";"},
                    {"7", "8", "9", ""},
                    {"*", "0", "#", ""}
            };

            predefinedLayouts = new String[][][]{kbdSym1, kbdSym2, kbdSym3, kbdNums};

            loadKeyboardLayout();

            superBoardView.createLayoutWithRows(predefinedLayouts[0], KeyboardType.SYMBOL);
            superBoardView.createLayoutWithRows(predefinedLayouts[1], KeyboardType.SYMBOL);
            superBoardView.createLayoutWithRows(predefinedLayouts[2], KeyboardType.FN);
            superBoardView.createLayoutWithRows(predefinedLayouts[3], KeyboardType.NUMBER);

            superBoardView.setPressEventForKey(1, 3, 0, Keyboard.KEYCODE_ALT);

            superBoardView.setPressEventForKey(-1, 0, -1, Keyboard.KEYCODE_ALT);
            superBoardView.setPressEventForKey(-1, -2, -1, Keyboard.KEYCODE_DELETE);
            superBoardView.setKeyRepeat(-1, -2, -1);
            superBoardView.setPressEventForKey(-1, -1, -1, Keyboard.KEYCODE_DONE);

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

            superBoardView.setDisableModifierForKeyboard(3, true);

            // set Fx buttons
            for (int i = 4; i < 6; i++) {
                for (int g = 0; g < 6; g++) {
                    superBoardView.setPressEventForKey(3, i, g, KeyEvent.KEYCODE_F1 + (g + (i * 6)));
                }
            }

            for (int i = 1; i < 3; i++) {
                superBoardView.setRowPadding(i, 2, DensityUtils.wpInt(2));
                superBoardView.setKeyRepeat(i, 3, -1);
                superBoardView.setKeyRepeat(i, 4, 2);
                superBoardView.setPressEventForKey(i, 3, -1, Keyboard.KEYCODE_DELETE);
                superBoardView.setPressEventForKey(i, 4, 0, Keyboard.KEYCODE_MODE_CHANGE);
                superBoardView.setPressEventForKey(i, 4, 2, KeyEvent.KEYCODE_SPACE);
                superBoardView.setPressEventForKey(i, 4, -1, Keyboard.KEYCODE_DONE);
                superBoardView.setLongPressEventForKey(i, 4, 0, SuperBoard.KEYCODE_CLOSE_KEYBOARD);
                superBoardView.setKeyWidthPercent(i, 3, 0, 15);
                superBoardView.setKeyWidthPercent(i, 3, -1, 15);
                superBoardView.setKeyWidthPercent(i, 4, 0, 20);
                superBoardView.setKeyWidthPercent(i, 4, 1, 15);
                superBoardView.setKeyWidthPercent(i, 4, 2, 50);
                superBoardView.setKeyWidthPercent(i, 4, 3, 15);
                superBoardView.setKeyWidthPercent(i, 4, -1, 20);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && emojiView == null) {
            emojiView = new EmojiView(superBoardView, emojiClick);
            emojiView.setFocusable(false);
            emojiView.setVisibility(View.GONE);

            if (SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                emojiView.setBackground(superBoardView.getBackground());
            } else {
                emojiView.setBackgroundDrawable(superBoardView.getBackground());
            }

        }

        if (keyboardLayoutHolder == null) {
            keyboardLayoutHolder = new LinearLayout(this);
            keyboardLayoutHolder.setFocusable(false);
            keyboardLayoutHolder.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
            keyboardLayoutHolder.setOrientation(LinearLayout.VERTICAL);
            suggestionLayout = new SuggestionLayout(superBoardView);
            suggestionLayout.setFocusable(false);
            suggestionLayout.setLayoutParams(new FrameLayout.LayoutParams(-1, mpInt(12)));
            suggestionLayout.setId(android.R.attr.shape);
            keyboardLayoutHolder.addView(suggestionLayout);
            keyboardLayoutHolder.addView(superBoardView);
            if (emojiView != null) {
                keyboardLayoutHolder.addView(emojiView);
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
    }

    public void setPrefs() {
        if (superBoardView != null) {
            superBoardView.fixHeight();

            LayoutUtils.setKeyOpts(currentLanguageCache, superBoardView);
            IconThemeUtils icons = SuperBoardApplication.getIconThemes();
            superBoardView.setKeyDrawable(-1, -2, -1, icons.getIconResource(LocalIconTheme.SYM_TYPE_DELETE));
            superBoardView.setKeyDrawable(-1, -1, -1, icons.getIconResource(LocalIconTheme.SYM_TYPE_ENTER));
            superBoardView.setKeyDrawable(3, 1, 2, icons.getIconResource(LocalIconTheme.SYM_TYPE_ENTER));
            List<Integer> indexes = superBoardView.findKeyboardIndexes(KeyboardType.SYMBOL);
            for (int i : indexes) {
                superBoardView.setKeyDrawable(i, 3, -1, icons.getIconResource(LocalIconTheme.SYM_TYPE_DELETE));
                superBoardView.setKeyDrawable(i, 4, -1, icons.getIconResource(LocalIconTheme.SYM_TYPE_ENTER));
                LayoutUtils.setSpaceBarViewPrefs(icons, superBoardView.getKey(i, 4, 2), appName);
            }
            superBoardView.setShiftDetection(SuperDBHelper.getBooleanOrDefault(SettingMap.SET_DETECT_CAPSLOCK));
            superBoardView.setRepeating(!SuperDBHelper.getBooleanOrDefault(SettingMap.SET_DISABLE_REPEAT));
            superBoardView.updateKeyState();
            superBoardView.setKeyboardHeight(SuperDBHelper.getIntOrDefault(SettingMap.SET_KEYBOARD_HEIGHT));
            File img;
            int c = SuperDBHelper.getIntOrDefault(SettingMap.SET_KEYBOARD_BGCLR);
            if (SuperDBHelper.getBooleanOrDefault(SettingMap.SET_USE_MONET)) {
                if (keyboardBackgroundHolder != null) {
                    keyboardBackground.setImageBitmap(null);
                }
            } else {
                img = SuperBoardApplication.getBackgroundImageFile();
                if (keyboardBackgroundHolder != null) {
                    if (img.exists()) {
                        int blur = SuperDBHelper.getIntOrDefault(SettingMap.SET_KEYBOARD_BGBLUR);
                        Bitmap b = BitmapFactory.decodeFile(img.getAbsolutePath());
                        keyboardBackground.setImageBitmap(blur > 0 ? ImageUtils.getBlur(b, blur) : b);
                    } else {
                        keyboardBackground.setImageBitmap(null);
                        c = ColorUtils.convertARGBtoRGB(c);
                    }
                }
            }

            keyboardLayoutHolder.setBackgroundColor(c);
            superBoardView.setBackgroundColor(Color.TRANSPARENT);

            if (suggestionLayout != null) {
                suggestionLayout.reTheme();
            }

            int keyClr = SuperDBHelper.getIntOrDefault(SettingMap.SET_KEY_BGCLR);
            int keyPressClr = SuperDBHelper.getIntOrDefault(SettingMap.SET_KEY_PRESS_BGCLR);
            superBoardView.setKeysBackground(LayoutUtils.getKeyBg(keyClr, keyPressClr, true));
            int shr = SuperDBHelper.getIntOrDefault(SettingMap.SET_KEY_SHADOWSIZE),
                    shc = SuperDBHelper.getIntOrDefault(SettingMap.SET_KEY_SHADOWCLR);
            superBoardView.setKeysShadow(shr, shc);
            superBoardView.setLongPressMultiplier(SuperDBHelper.getIntOrDefault(SettingMap.SET_KEY_LONGPRESS_DURATION));
            superBoardView.setKeyVibrateDuration(SuperDBHelper.getIntOrDefault(SettingMap.SET_KEY_VIBRATE_DURATION));
            superBoardView.setKeysTextColor(SuperDBHelper.getIntOrDefault(SettingMap.SET_KEY_TEXTCLR));
            superBoardView.setKeysTextSize(mpInt(DensityUtils.getFloatNumberFromInt(SuperDBHelper.getIntOrDefault(SettingMap.SET_KEY_TEXTSIZE))));
            superBoardView.setKeysTextType(SuperDBHelper.getIntOrDefault(SettingMap.SET_KEYBOARD_TEXTTYPE_SELECT));
            superBoardView.setIconSizeMultiplier(SuperDBHelper.getIntOrDefault(SettingMap.SET_KEY_ICON_SIZE_MULTIPLIER));
            superBoardView.setKeysPopupPreviewEnabled(SuperDBHelper.getBooleanOrDefault(SettingMap.SET_ENABLE_POPUP_PREVIEW));
            int y = SuperDBHelper.getIntOrDefault(SettingMap.SET_KEY2_BGCLR);
            int yp = SuperDBHelper.getIntOrDefault(SettingMap.SET_KEY2_PRESS_BGCLR);
            int z = SuperDBHelper.getIntOrDefault(SettingMap.SET_ENTER_BGCLR);
            int zp = SuperDBHelper.getIntOrDefault(SettingMap.SET_ENTER_PRESS_BGCLR);
            Drawable key2Bg = LayoutUtils.getKeyBg(y, yp, true);
            Drawable enterBg = LayoutUtils.getKeyBg(z, zp, true);
            for (int i = 0; i < predefinedLayouts.length; i++) {
                if (i != 0) {
                    if (i < 3) {
                        superBoardView.setKeyBackground(i, 3, 0, key2Bg);
                        superBoardView.setKeyBackground(i, 3, -1, key2Bg);
                        for (int h = 3; h < 5; h++) superBoardView.setKeyBackground(i, h, 0, key2Bg);
                        superBoardView.setKeyBackground(i, 4, 1, key2Bg);
                        superBoardView.setKeyBackground(i, 4, 3, key2Bg);
                    }
                    if (i != 3) superBoardView.setKeyBackground(i, -1, -1, enterBg);
                }
            }
            superBoardView.setDisablePopup(SuperDBHelper.getBooleanOrDefault(SettingMap.SET_DISABLE_POPUP));
            boolean isDBEmpty = SuperBoardApplication.getDictDB()
                    .getTableLength(currentLanguageCache.language.split("_")[0]) < 1;
            boolean sugDisabled = SuperDBHelper.getBooleanOrDefault(SettingMap.SET_DISABLE_SUGGESTIONS) || isDBEmpty;
            boolean topBarDisabled = SuperDBHelper.getBooleanOrDefault(SettingMap.SET_DISABLE_TOP_BAR);
            boolean numDisabled = SuperDBHelper.getBooleanOrDefault(SettingMap.SET_DISABLE_NUMBER_ROW);
            superBoardView.setPressEventForKey(2, 3, 0,
                    topBarDisabled ? Keyboard.KEYCODE_ALT : Keyboard.KEYCODE_CANCEL);
            superBoardView.getKey(2, 3, 0).setText(topBarDisabled ? "S3" : "S1");
            suggestionLayout.setVisibility(sugDisabled && topBarDisabled ? View.GONE : View.VISIBLE);
            suggestionLayout.setOnSuggestionSelectedListener(sugDisabled ? null : this);
            suggestionLayout.toggleQuickMenu(topBarDisabled);
            String lang = SuperDBHelper.getStringOrDefault(SettingMap.SET_KEYBOARD_LANG_SELECT);
            if (!lang.equals(currentLanguageCache.language)) {
                loadKeyboardLayout();
            }
            List<RowOptions> kOpt = currentLanguageCache.layout;
            for (int i = 0; i < kOpt.size(); i++) {
                RowOptions subKOpt = kOpt.get(i);
                for (int g = 0; g < subKOpt.keys.size(); g++) {
                    KeyOptions ko = subKOpt.keys.get(g);
                    if (ko.darkerKeyTint) {
                        superBoardView.setKeyBackground(0, i, g, key2Bg);
                    }

                    if (ko.pressKeyCode == Keyboard.KEYCODE_DONE) {
                        superBoardView.setKeyBackground(0, i, g, enterBg);
                    }
                }
            }

            superBoardView.getRow(0, 0).setVisibility(numDisabled ? View.GONE : View.VISIBLE);

            superBoardView.setKeyboardLanguage(currentLanguageCache.language);
            adjustNavbar(c);
            if (emojiView != null) {
                emojiView.applyTheme(superBoardView);
                emojiView.getLayoutParams().height = superBoardView.getKeyboardHeight();
            }
            SuperBoardApplication.clearCustomFont();
            SuperBoardApplication.getCustomFont();

            if (SDK_INT >= 11) {
                boolean enableClipboard = SuperDBHelper.getBooleanOrDefaultResolved(
                        SettingMap.SET_ENABLE_CLIPBOARD);

                if (enableClipboard && clipboardView == null) {
                    clipboardView = new ClipboardView(superBoardView);
                    clipboardView.setVisibility(View.GONE);
                    keyboardLayoutHolder.addView(clipboardView);
                } else if (!enableClipboard && clipboardView != null) {
                    clipboardView.clearClipboard();
                    clipboardView.deInit();
                    keyboardLayoutHolder.removeView(clipboardView);
                    clipboardView = null;
                    System.gc();
                }

                if (clipboardView != null) {
                    clipboardView.onPrimaryClipChanged();
                    clipboardView.reTheme();
                }
            }
        }

        sendCompletionRequest();
    }

    private void loadKeyboardLayout() {
        String lang = SuperDBHelper.getStringOrDefault(SettingMap.SET_KEYBOARD_LANG_SELECT);
        int keyboardIndex = superBoardView.findTextKeyboardIndex();
        Language language = SuperBoardApplication.getKeyboardLanguage(lang);
        if (!language.language.equals(lang)) {
            throw new RuntimeException("Where is the layout JSON file (in assets)?");
        }
        String[][] lkeys = LayoutUtils.getLayoutKeys(language.layout);
        superBoardView.replaceTextKeyboard(lkeys);
        superBoardView.setLayoutPopup(keyboardIndex, LayoutUtils.getLayoutKeys(language.popup));
        for (int i = 0; i < language.layout.size(); i++) {
            RowOptions opts = language.layout.get(i);
            if (opts.enablePadding) {
                superBoardView.setRowPadding(0, i, DensityUtils.wpInt(2));
            }
        }
        superBoardView.setKeyboardLanguage(language.language);
        LayoutUtils.setKeyOpts(language, superBoardView);
        currentLanguageCache = language;
    }

    private void adjustNavbar(int c) {
        int baseHeight = superBoardView.getKeyboardHeight();
        if (suggestionLayout.getVisibility() == View.VISIBLE) {
            baseHeight += suggestionLayout.getLayoutParams().height;
        }

        if (SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window w = getWindow().getWindow();
            assert w != null : "Window returned null";

            if (detectNavbar(this)) {
                @SuppressLint("ResourceType") View navbarView = keyboardLayoutHolder.findViewById(android.R.attr.gravity);
                if (navbarView != null)
                    keyboardLayoutHolder.removeView(navbarView);

                if (SDK_INT >= 28 && SuperDBHelper.getBooleanOrDefault(SettingMap.SET_COLORIZE_NAVBAR_ALT) && !isColorized()) {
                    w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                    w.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                    keyboardBackground.setLayoutParams(new RelativeLayout.LayoutParams(-1, baseHeight));
                    int color = Color.rgb(Color.red(c), Color.green(c), Color.blue(c));
                    w.setNavigationBarColor(color);
                    w.getDecorView().setSystemUiVisibility(ColorUtils.satisfiesTextContrast(color)
                            ? View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                            : View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                } else if (isColorized()) {
                    // I found a bug at SDK 30 (Android R)
                    // FLAG_LAYOUT_NO_LIMITS not working
                    // set FLAG_TRANSLUCENT_NAVIGATION for this SDK only
                    if (SDK_INT == Build.VERSION_CODES.R)
                        w.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                    else w.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                    w.setNavigationBarColor(0);
                    keyboardBackground.setLayoutParams(new RelativeLayout.LayoutParams(-1, baseHeight + navbarH(this)));
                    keyboardLayoutHolder.addView(createNavbarLayout(this, c));
                } else {
                    w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                    w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                    w.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                    keyboardBackground.setLayoutParams(new RelativeLayout.LayoutParams(-1, baseHeight));
                }
            } else {
                keyboardBackground.setLayoutParams(new RelativeLayout.LayoutParams(-1, baseHeight));
            }
        } else {
            keyboardBackground.setLayoutParams(new RelativeLayout.LayoutParams(-1, baseHeight));
        }

        keyboardLayoutHolder.getLayoutParams().height = keyboardBackground.getLayoutParams().height;
        boardPopup.setFilterHeight(keyboardLayoutHolder.getLayoutParams().height);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (boardPopup != null && boardPopup.isShown()) {
            boardPopup.showPopup(false);
        }
        showEmojiView(false);
        return super.onKeyDown(keyCode, event);
    }

    public void onEmojiText(String text) {
        superBoardView.commitText(text);
    }

    private void showEmojiView(boolean value) {
        if (SDK_INT < 16 || emojiView == null) {
            return;
        }
        if (emojiView.isShown() != value) {
            showClipboardView(false);
            emojiView.setVisibility(value ? View.VISIBLE : View.GONE);
            superBoardView.setVisibility(value ? View.GONE : View.VISIBLE);
        }
    }

    private void showClipboardView(boolean value) {
        if (SDK_INT < 11 || clipboardView == null) {
            return;
        }
        if (clipboardView.isShown() != value) {
            showEmojiView(false);

            if (value) {
                clipboardView.reTheme();
            }

            clipboardView.setVisibility(value ? View.VISIBLE : View.GONE);
            superBoardView.setVisibility(value ? View.GONE : View.VISIBLE);
        }
    }

    /** @noinspection unused*/
    private class SuperBoardImpl extends SuperBoard {
        private boolean shown = false;
        private SuperBoardImpl(Context context) {
            super(context);
            setSpecialCases(LayoutUtils.getSpecialCases());
        }

        @Override
        public void onKeyboardEvent(View v) {
            if (suggestionLayout != null) suggestionLayout.setAllKeyLockStatus();

            if (SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && emojiView != null && emojiView.isShown()) {
                showEmojiView(false);
            }

            if (SDK_INT >= Build.VERSION_CODES.HONEYCOMB && clipboardView != null && clipboardView.isShown()) {
                showClipboardView(false);
            }

            shown = boardPopup.isShown();
            if (shown) {
                boardPopup.showPopup(false);
                boardPopup.clear();
                return;
            }

            boolean showPopup = SuperDBHelper.getBooleanOrDefault(SettingMap.SET_KEYBOARD_SHOW_POPUP);
            boolean disablePopup = SuperDBHelper.getBooleanOrDefault(SettingMap.SET_DISABLE_POPUP);

            if (showPopup || !disablePopup)
                boardPopup.setKey(superBoardView, (SuperBoard.Key) v);

            if (showPopup)
                boardPopup.showCharacter();
        }

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

            if (SuperDBHelper.getBooleanOrDefault(SettingMap.SET_KEYBOARD_SHOW_POPUP)) {
                boardPopup.hideCharacter();
            }

            // sendCompletionRequest();
        }

        private boolean isClipboardViewShown() {
            return SDK_INT >= Build.VERSION_CODES.HONEYCOMB && clipboardView != null && clipboardView.isShown();
        }

        private boolean isEmojiViewShown() {
            return SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && emojiView != null && emojiView.isShown();
        }

        @Override
        public void sendDefaultKeyboardEvent(View v) {
            Key key = (Key) v;

            if (key.hasNormalPressEvent()) {
                if (key.getNormalPressEvent().first != KeyEvent.KEYCODE_EISU && isClipboardViewShown()) {
                    showClipboardView(false);
                }

                if (key.getNormalPressEvent().first != KeyEvent.KEYCODE_KANA && isEmojiViewShown()) {
                    showEmojiView(false);
                }

                switch (key.getNormalPressEvent().first) {
                    case KeyEvent.KEYCODE_HENKAN: // symbol menu
                        int fnIndex = findFNKeyboardIndex();
                        setEnabledLayout(
                                getEnabledLayoutIndex() != fnIndex
                                        ? fnIndex
                                        : findTextKeyboardIndex()
                        );
                        return;
                    case KeyEvent.KEYCODE_NUM:    // number menu
                        int numIndex = findNumberKeyboardIndex();
                        setEnabledLayout(
                                getEnabledLayoutIndex() != numIndex
                                        ? numIndex
                                        : findTextKeyboardIndex()
                        );
                        return;
                    case KeyEvent.KEYCODE_EISU:   // clipboard menu
                        showClipboardView(!clipboardView.isShown());
                        return;
                    case KeyEvent.KEYCODE_KANA:   // emoji menu
                        showEmojiView(!emojiView.isShown());
                        return;
                }
            }

            if (!shown) super.sendDefaultKeyboardEvent(v);
            else shown = false;
        }

        @Override
        public void switchLanguage() {
            if (SuperDBHelper.getBooleanOrDefault(SettingMap.SET_KEYBOARD_LC_ON_EMOJI)) {
                SuperBoardApplication.getNextLanguage();
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
            if (!SuperDBHelper.getBooleanOrDefault(SettingMap.SET_PLAY_SND_PRESS)) return;
            super.playSound(event);
        }
    }

    /** @noinspection unused*/
    private class BoardPopupImpl extends BoardPopup {
        public BoardPopupImpl(ViewGroup root) {
            super(root);
            setSpecialCases(LayoutUtils.getSpecialCases());
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
