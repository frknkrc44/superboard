package org.blinksd.board.views;

import static org.blinksd.utils.SuperDBHelper.getIntOrDefault;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import org.blinksd.utils.ColorUtils;
import org.blinksd.utils.DensityUtils;
import org.blinksd.utils.LayoutUtils;
import org.blinksd.utils.SettingMap;
import org.blinksd.utils.SuperDBHelper;
import org.blinksd.utils.superboard.KeyboardType;

import java.util.ArrayList;
import java.util.List;

/** @noinspection unused*/
@SuppressLint("ViewConstructor")
public class BoardPopup extends SuperBoard {
    private static final int[] pos = new int[]{0, 0};
    private static int khp = 0;
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

    public void setKeyboardPrefs() {
        setIconSizeMultiplier(getIntOrDefault(SettingMap.SET_KEY_ICON_SIZE_MULTIPLIER));
        khp = getIntOrDefault(SettingMap.SET_KEYBOARD_HEIGHT);
        int a = getIntOrDefault(SettingMap.SET_KEYBOARD_BGCLR);
        int ap = getIntOrDefault(SettingMap.SET_KEY_PRESS_BGCLR);
        a = ColorUtils.setAlphaForColor(0xCC, a);
        ap = ColorUtils.setAlphaForColor(0xCC, ap);
        setBackgroundDrawable(LayoutUtils.getKeyBg(a, ap, true));
        mPopupFilter.setBackgroundColor(ColorUtils.setAlphaForColor(0x33, a));
        mKey.setVisibility(GONE);
        int h = mKey.getLayoutParams().height;
        setKeyLeftTop(pos[0], pos[1] - (pos[1] >= h ? h : 0));
    }

    private void setKeyLeftTop(int left, int top) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mKey.getLayoutParams();
        params.leftMargin = left;
        params.topMargin = top;
    }

    public void setKey(SuperBoard board, Key key) {
        setShiftState(board.getShiftState());
        key.clone(mKey);
        key.getLocationInWindow(pos);
        setKeysTextType(board.textStyle);
        setKeysShadow(board.shadowRadius, board.shadowColor);
        setKeysTextColor(board.getKeysTextColor());
        setKeysTextSize((int) board.getKeysTextSize());
        mKey.setKeyTextSize(board.getKeysTextSize());
        setKeyboardPrefs();
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

                commitText(ret);

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

        List<CharSequence> uppercaseCharacters = new ArrayList<>();
        for (CharSequence c : chars) {
            uppercaseCharacters.add(getCase(c, getShiftState() != SHIFT_OFF));
        }

        createPopup(uppercaseCharacters.toArray(chars));
    }

    private void createPopup(CharSequence[] a) {
        int h, c = 6;
        setKeyboardWidth(a.length < c ? 11 * a.length : 11 * c);
        h = a.length / c;
        h = h > 0 ? h : 1;
        h += ((a.length > (c - 1)) && (a.length) % c > 0) ? 1 : 0;
        setKeyboardHeight(10 * h);
        setXY(
                DensityUtils.wpInt(50 - (getKeyboardWidthPercent() / 2f)),
                DensityUtils.hpInt((khp - getKeyboardHeightPercent()) / 2f)
        );
        CharSequence[] x;
        for (int i = 0, k = 0; i < h; i++) {
            x = new CharSequence[Math.min(a.length, c)];
            x[0] = "";
            for (int g = 0; g < c; g++) {
                k++;
                int j = (i * c) + g;
                if (j < a.length) {
                    x[g] = a[j];
                    continue;
                }
                break;
            }
            if (x[0].length() > 0 && k > 1) addRow(0, x);
        }

        fixHeight();
    }

    public void setXY(float x, float y) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setX(x);
            setY(y);
            return;
        }

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
        params.leftMargin = (int) x;
        params.topMargin = (int) y;
    }

    @Override
    protected void sendDefaultKeyboardEvent(View v) {
        super.sendDefaultKeyboardEvent(v);
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
