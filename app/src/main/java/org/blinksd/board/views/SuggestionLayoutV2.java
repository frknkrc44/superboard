package org.blinksd.board.views;

import static org.blinksd.board.SuperBoardApplication.getAppDB;
import static org.blinksd.board.SuperBoardApplication.getDictDB;
import static org.blinksd.board.SuperBoardApplication.mainHandler;
import static org.blinksd.utils.SuperDBHelper.getBooleanOrDefault;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.ExtractedText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.blinksd.board.R;
import org.blinksd.utils.ColorUtils;
import org.blinksd.utils.DensityUtils;
import org.blinksd.utils.SettingMap;
import org.blinksd.utils.SuperDBHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SuppressLint({"ViewConstructor", "InlinedApi"})
public class SuggestionLayoutV2 extends RelativeLayout implements View.OnClickListener {
    private final HorizontalScrollView scroller;
    private final LinearLayout mCompletionsLayout;
    private final List<LoadDictTask> mLoadDictTasks = new ArrayList<>();
    private final ExecutorService mThreadPool = Executors.newFixedThreadPool(64);
    private OnSuggestionSelectedListener mOnSuggestionSelectedListener;
    private String mLastText, mCompleteText;
    private FABView fabView;
    private boolean oldReversed = false;

    public SuggestionLayoutV2(SuperBoard superBoard) {
        super(superBoard.getContext());

        int p = DensityUtils.dpInt(8);
        setPadding(p, 0, p, 0);

        mCompletionsLayout = new LinearLayout(getContext());
        mCompletionsLayout.setLayoutParams(new HorizontalScrollView.LayoutParams(-1, -1));
        mCompletionsLayout.setFocusable(false);

        fabView = new FABView(
                superBoard.getContext(),
                superBoard::sendKeyEvent,
                (alpha, delay) -> mCompletionsLayout.animate().setStartDelay(delay).alpha(alpha),
                v -> {
                    fabView.collapse();
                    getAppDB().putBoolean(SettingMap.SET_SHOW_FAB_RIGHT, !oldReversed, true);
                    setReversed(!oldReversed);
                    return true;
                }
        );
        fabView.setLayoutParams(new LayoutParams(-2, -1));
        fabView.setFocusable(false);

        fabView.addButton(R.drawable.arrow_left, KeyEvent.KEYCODE_DPAD_LEFT);
        fabView.addButton(R.drawable.sym_board_emoji, KeyEvent.KEYCODE_KANA, true);
        fabView.addButton(R.drawable.ctrl, SuperBoard.KEYCODE_TOGGLE_CTRL, true);
        fabView.addButton(R.drawable.superscript, KeyEvent.KEYCODE_KATAKANA_HIRAGANA);
        fabView.addButton(R.drawable.more_control, KeyEvent.KEYCODE_HENKAN);
        fabView.addButton(R.drawable.settings, SuperBoard.KEYCODE_SETTINGS);
        fabView.addButton(R.drawable.alt, SuperBoard.KEYCODE_TOGGLE_ALT, true);
        fabView.addButton(R.drawable.number, KeyEvent.KEYCODE_NUM);
        fabView.addButton(R.drawable.clipboard, KeyEvent.KEYCODE_EISU, true);
        fabView.addButton(R.drawable.arrow_right, KeyEvent.KEYCODE_DPAD_RIGHT);

        boolean topBarDisabled = getBooleanOrDefault(SettingMap.SET_DISABLE_TOP_BAR);
        fabView.setVisibility(topBarDisabled ? GONE : VISIBLE);

        scroller = new HorizontalScrollView(getContext());
        var params = new LayoutParams(-1, -1);

        scroller.setLayoutParams(params);
        scroller.addView(mCompletionsLayout);
        addView(scroller);
        addView(fabView);
    }

    public final void changeFABKeyState(int keyCode, boolean state) {
        fabView.changeKeyState(keyCode, state);
    }

    public void setOnSuggestionSelectedListener(OnSuggestionSelectedListener listener) {
        mOnSuggestionSelectedListener = listener;
    }

    public void setCompletion(SuperBoard superBoard, ExtractedText text, String lang) {
        setCompletionText(superBoard, text == null ? "" : text.text, lang);
    }

    public void setReversed(boolean reversed) {
        var fabParams = (LayoutParams) fabView.getLayoutParams();
        var scrollerParams = (LayoutParams) scroller.getLayoutParams();
        var childWidth = FABView.getButtonSize();

        if (reversed) {
            if (!oldReversed) {
                fabParams.addRule(ALIGN_PARENT_RIGHT);
            }

            fabView.setOrientation(FABView.Orientation.TRH);
            scrollerParams.leftMargin = (int) (childWidth * 0.15f);
            scrollerParams.rightMargin = childWidth;
        } else {
            if (oldReversed) {
                fabParams.removeRule(ALIGN_PARENT_RIGHT);
            }

            fabView.setOrientation(FABView.Orientation.TLH);
            scrollerParams.leftMargin = childWidth;
            scrollerParams.rightMargin = (int) (childWidth * 0.15f);
        }

        oldReversed = reversed;
    }

    public void setCompletionText(SuperBoard superBoard, CharSequence text, String lang) {
        // mCompletionsLayout.removeAllViews();

        if (text == null)
            text = "";

        if (lang == null)
            lang = superBoard.getKeyboardLanguage().getLanguage();

        String str = text.toString();
        mCompleteText = str;

        if (str.isEmpty() || str.charAt(str.length() - 1) == ' ') {
            LoadDictTask task = new LoadDictTask();
            mLoadDictTasks.add(task);
            task.execute(lang, "");
            return;
        }

        str = str.trim();
        str = str.substring(str.lastIndexOf(' ') + 1);
        str = str.substring(str.lastIndexOf('\n') + 1);
        mLastText = str;
        LoadDictTask task = new LoadDictTask();
        mLoadDictTasks.add(task);
        task.execute(lang, str);
    }

    private void addCompletionView(final CharSequence text) {
        TextView tv = new TextView(getContext());
        tv.setGravity(Gravity.CENTER);
        int color = SuperDBHelper.getIntOrDefault(SettingMap.SET_KEY_TEXTCLR);
        tv.setTextColor(color);
        float textSize = DensityUtils.mpInt(SuperDBHelper.getFloatedIntOrDefault(SettingMap.SET_KEY_TEXTSIZE));
        int pad = DensityUtils.dpInt(8);
        tv.setTextSize(textSize);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -1);
        params.rightMargin = params.topMargin = params.bottomMargin = pad;
        tv.setLayoutParams(params);
        tv.setEllipsize(TextUtils.TruncateAt.END);
        tv.setText(text);
        tv.setOnClickListener(this);
        tv.setFocusable(false);
        mCompletionsLayout.addView(tv);
    }

    private Drawable getSuggestionItemBackground() {
        int color = SuperDBHelper.getIntOrDefault(SettingMap.SET_KEY_TEXTCLR);
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(ColorUtils.setAlphaForColor(70, color));
        gd.setCornerRadius(16);
        return gd;
    }

    public void reTheme() {
        final int keyColor = SuperDBHelper.getIntOrDefault(SettingMap.SET_KEY2_BGCLR);
        final int textColor = SuperDBHelper.getIntOrDefault(SettingMap.SET_KEY_TEXTCLR);
        final int childCount = mCompletionsLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            TextView tv = (TextView) mCompletionsLayout.getChildAt(i);
            tv.setTextColor(textColor);
            float textSize = DensityUtils.mpInt(SuperDBHelper.getFloatedIntOrDefault(SettingMap.SET_KEY_TEXTSIZE));
            tv.setTextSize(textSize);
            tv.setBackground(getSuggestionItemBackground());
        }

        final var clipboardDisabled = getBooleanOrDefault(SettingMap.SET_SHOW_BOTTOM_BAR) ||
                                !getBooleanOrDefault(SettingMap.SET_ENABLE_CLIPBOARD);
        final var fnButtonsDisabled = getBooleanOrDefault(SettingMap.SET_HIDE_TOP_BAR_FN_BUTTONS);
        final var numberRowDisabled = getBooleanOrDefault(SettingMap.SET_DISABLE_NUMBER_ROW);

        toggleButtonVisibility(KeyEvent.KEYCODE_EISU, clipboardDisabled);
        toggleButtonVisibility(SuperBoard.KEYCODE_TOGGLE_CTRL, fnButtonsDisabled);
        toggleButtonVisibility(SuperBoard.KEYCODE_TOGGLE_ALT, fnButtonsDisabled);
        toggleButtonVisibility(KeyEvent.KEYCODE_HENKAN, fnButtonsDisabled);
        toggleButtonVisibility(KeyEvent.KEYCODE_NUM, !numberRowDisabled);

        fabView.reTheme(keyColor, textColor);
    }

    private void toggleButtonVisibility(int keyCode, boolean disabled) {
        if (fabView.disabledKeycodes.contains(keyCode) && !disabled) {
            fabView.disabledKeycodes.remove((Integer) keyCode);
        } else if (!fabView.disabledKeycodes.contains(keyCode) && disabled) {
            fabView.disabledKeycodes.add(keyCode);
        }
    }

    @Override
    public void onClick(View p1) {
        if (mOnSuggestionSelectedListener != null) {
            mOnSuggestionSelectedListener.onSuggestionSelected(mCompleteText, mLastText, ((TextView) p1).getText());
        }
    }

    public interface OnSuggestionSelectedListener {
        void onSuggestionSelected(CharSequence text, CharSequence oldText, CharSequence suggestion);
    }

    private class LoadDictTask {
        public void execute(String... args) {
            onPreExecute();
            mThreadPool.execute(() -> {
                List<String> out = doInBackground(args);
                mainHandler.post(() -> onPostExecute(out));
            });
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        private void cancel() {
            try {
                mThreadPool.awaitTermination(1, TimeUnit.MILLISECONDS);
            } catch (Throwable ignored) {}
        }

        protected void onPreExecute() {
            try {
                for (LoadDictTask task : mLoadDictTasks) {
                    if (task != this) {
                        task.cancel();
                        mLoadDictTasks.remove(task);
                    }
                }
            } catch (Throwable ignored) {
            }
        }

        protected List<String> doInBackground(String[] p1) {
            String prefix = p1[1].toLowerCase();

            if (TextUtils.isEmpty(prefix)) {
                return new ArrayList<>();
            }

            String lang = p1[0].toLowerCase();
            return getDictDB().getQuery(lang, prefix);
        }

        protected void onPostExecute(final List<String> result) {
            if (!mLoadDictTasks.contains(this)) {
                return;
            }

            ((HorizontalScrollView) mCompletionsLayout.getParent()).scrollTo(0, 0);
            mCompletionsLayout.removeAllViews();

            for (String item : result)
                addCompletionView(item);

            mLoadDictTasks.remove(this);
        }
    }
}
