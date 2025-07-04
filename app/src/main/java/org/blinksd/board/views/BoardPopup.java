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

    private void setKeyLeftTop(int left, int top) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mKey.getLayoutParams();
        params.leftMargin = left;
        params.topMargin = top;
    }

    public void setKey(SuperBoard board, Key key) {
        setIconSizeMultiplier(getIntOrDefault(SettingMap.SET_KEY_ICON_SIZE_MULTIPLIER));
        key.getLocationInWindow(keyPosition);
        int keyHeight = mKey.getLayoutParams().height;
        setKeyLeftTop(keyPosition[0], keyPosition[1] - (keyPosition[1] >= keyHeight ? keyHeight : 0));

        setShiftState(board.getShiftState());
        key.clone(mKey);
        setKeysTextType(board.textStyle);
        setKeysShadow(board.shadowRadius, board.shadowColor);
        setKeysTextColor(board.getKeysTextColor());
        setKeysTextSize(board.getKeysTextSize());
        mKey.setKeyTextSize(board.getKeysTextSize());

        mainKeyboardHeightPercent = getIntOrDefault(SettingMap.SET_KEYBOARD_HEIGHT);
        int a = getIntOrDefault(SettingMap.SET_KEYBOARD_BGCLR);
        int ap = getIntOrDefault(SettingMap.SET_KEY_PRESS_BGCLR);
        a = setAlphaForColor(0xCC, a);
        ap = setAlphaForColor(0xCC, ap);
        setBackground(ResourcesUtils.getKeyBg(a, ap, true));
        mPopupFilter.setBackgroundColor(setAlphaForColor(0x33, a));
        mKey.setVisibility(GONE);
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

        final var input = new CharSequence[chars.length];
        for (int i = 0; i < chars.length; i++) {
            input[i] = getCase(chars[i], getShiftState() != SHIFT_OFF);
        }

        final int columnCount = 6;
        setKeyboardWidth(11 * Math.min(input.length, columnCount));
        int rowCount = input.length / columnCount;
        rowCount = rowCount > 0 ? rowCount : 1;
        rowCount += ((input.length > (columnCount - 1)) && (input.length) % columnCount > 0) ? 1 : 0;
        setKeyboardHeight(10 * rowCount);
        setX(DensityUtils.wp(50 - (getKeyboardWidthPercent() / 2f)));
        setY(DensityUtils.hp((mainKeyboardHeightPercent - getKeyboardHeightPercent()) / 2f));

        for (int i = 0, k = 0; i < rowCount; i++) {
            CharSequence[] charsPerRow = new CharSequence[Math.min(input.length, columnCount)];
            for (int g = 0; g < columnCount; g++) {
                k++;
                int j = (i * columnCount) + g;
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
    }

    @Override
    public void clear() {
        super.clear();
        mKey.setPopupCharacters(null);
    }
}
