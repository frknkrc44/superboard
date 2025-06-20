package org.blinksd.board.views;

import static org.blinksd.utils.ColorUtils.setAlphaForColor;
import static org.blinksd.utils.SuperDBHelper.getIntOrDefault;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import org.blinksd.utils.DensityUtils;
import org.blinksd.utils.ResourcesUtils;
import org.blinksd.utils.SettingMap;
import org.blinksd.utils.SuperDBHelper;
import org.blinksd.utils.superboard.KeyboardType;

@SuppressLint("ViewConstructor")
public class BoardPopup extends SuperBoard {
    private static final int[] keyPosition = new int[]{0, 0};
    private static int mainKeyboardHeightPercent = 0;
    private final Key mKey;
    private final View mPopupFilter;

    @SuppressLint("ClickableViewAccessibility")
    public BoardPopup(ViewGroup root) {
        super(root.getContext());
        createEmptyLayout(KeyboardType.NUMBER);
        updateKeyState();
        mPopupFilter = new View(root.getContext());
        mPopupFilter.setLayoutParams(new RelativeLayout.LayoutParams(-1, -2));
        mPopupFilter.setFocusable(false);
        mKey = new Key(getContext());
        mKey.setOnTouchListener(null);
        root.addView(mPopupFilter);
        root.addView(mKey);
        mPopupFilter.setVisibility(View.GONE);
        mKey.setVisibility(GONE);
        setVisibility(GONE);
    }

    public void setFilterHeight(int h) {
        mPopupFilter.getLayoutParams().height = h;
    }

    private void setKeyboardPrefs(SuperBoard board) {
        setIconSizeMultiplier(board.iconSizeMultiplier);
        mainKeyboardHeightPercent = board.getKeyboardHeight();
        int keyboardColor = setAlphaForColor(0xCC, getIntOrDefault(SettingMap.SET_KEYBOARD_BGCLR));
        int ap = setAlphaForColor(0xCC, getIntOrDefault(SettingMap.SET_KEY_PRESS_BGCLR));
        setBackground(ResourcesUtils.getKeyBg(keyboardColor, ap, true));
        mPopupFilter.setBackgroundColor(setAlphaForColor(0x33, keyboardColor));
        mKey.setVisibility(GONE);
        final var keyHeight = mKey.getLayoutParams().height;

        final var params = (RelativeLayout.LayoutParams) mKey.getLayoutParams();
        params.leftMargin = keyPosition[0];
        params.topMargin = keyPosition[1] - (keyPosition[1] >= keyHeight ? keyHeight : 0);
    }

    public void setKey(SuperBoard board, Key key) {
        setShiftState(board.getShiftState());
        key.clone(mKey);
        key.getLocationInWindow(keyPosition);
        setKeysTextType(board.textStyle);
        setKeysShadow(board.shadowRadius, board.shadowColor);
        setKeysTextColor(board.getKeysTextColor());
        setKeysTextSize((int) board.getKeysTextSize());
        mKey.setKeyTextSize(board.getKeysTextSize());
        setKeyboardPrefs(board);
    }

    public void showCharacter() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            hideCharacter();
            return;
        }

        mKey.setVisibility(VISIBLE);
    }

    public void hideCharacter() {
        mKey.setVisibility(GONE);
    }

    public void showPopup(boolean visible) {
        hideCharacter();
        CharSequence[] popupCharacters = mKey.getPopupCharacters();
        boolean useFC = SuperDBHelper.getBooleanOrDefault(SettingMap.SET_USE_FIRST_POPUP_CHARACTER);
        visible = visible && popupCharacters != null;
        setVisibility(visible && !useFC ? VISIBLE : GONE);
        mPopupFilter.setVisibility(getVisibility());

        if (visible) {
            if (useFC) {
                CharSequence ret = popupCharacters[0];
                ret = getCase(ret, getShiftState() > SHIFT_OFF);

                commitText(ret.toString());

                if (getShiftState() == SHIFT_ON) {
                    setShiftState(SHIFT_OFF);
                }

                afterKeyboardEvent();
            } else {
                setCharacters(popupCharacters);
            }
        }
    }

    private void setCharacters(CharSequence[] chars) {
        clear();

        final var output = new CharSequence[chars.length];
        for (int i = 0; i < chars.length; i++) {
            output[i] = getCase(chars[i], getShiftState() != SHIFT_OFF);
        }

        createPopup(output);
    }

    private void createPopup(CharSequence[] input) {
        if (input.length < 1) {
            return;
        }

        final int charactersPerRow = 6;
        setKeyboardWidth(11 * Math.min(input.length, charactersPerRow));

        int rowCount = Math.max(1, input.length / charactersPerRow);
        if (input.length >= charactersPerRow && input.length % charactersPerRow > 0) {
            rowCount += 1;
        }

        setKeyboardHeight(10 * rowCount);
        setX(DensityUtils.wpInt(50 - (getKeyboardWidthPercent() / 2f)));
        setY(DensityUtils.hpInt((mainKeyboardHeightPercent - getKeyboardHeightPercent()) / 2f));

        for (int i = 0, k = 0; i < rowCount; i++) {
            final var charsPerRow = new CharSequence[Math.min(input.length, charactersPerRow)];

            for (int g = 0; g < charactersPerRow; g++) {
                k++;
                int j = (i * charactersPerRow) + g;
                if (j < input.length) {
                    charsPerRow[g] = input[j];
                    continue;
                }
                break;
            }

            // noinspection all
            if (k > 1 && charsPerRow[0] != null) addRow(0, charsPerRow);
        }

        fixHeight();
    }

    @Override
    protected void sendKeyboardEvent(Key v) {
        super.sendKeyboardEvent(v);
        showPopup(false);
        clear();
        System.gc();
    }

    @Override
    public void clear() {
        super.clear();
        mKey.setPopupCharacters(null);
    }
}
