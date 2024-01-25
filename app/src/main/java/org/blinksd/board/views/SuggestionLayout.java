package org.blinksd.board.views;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.ExtractedText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.blinksd.board.R;
import org.blinksd.board.SuperBoardApplication;
import org.blinksd.utils.ColorUtils;
import org.blinksd.utils.DensityUtils;
import org.blinksd.utils.LayoutUtils;
import org.blinksd.utils.SettingMap;
import org.blinksd.utils.SuperDBHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SuppressLint("ViewConstructor")
public class SuggestionLayout extends FrameLayout implements View.OnClickListener {
    private final LinearLayout mCompletionsLayout, mCompletionsLayoutRoot;
    private final ImageView mReturnToQuickMenu;
    private final List<LoadDictTask> mLoadDictTasks = new ArrayList<>();
    private final ExecutorService mThreadPool = Executors.newFixedThreadPool(64);
    private final LinearLayout mQuickMenuLayout;
    private OnSuggestionSelectedListener mOnSuggestionSelectedListener;
    private String mLastText, mCompleteText;
    private final SuperBoard superBoard;

    public SuggestionLayout(SuperBoard superBoard) {
        super(superBoard.getContext());
        this.superBoard = superBoard;

        mCompletionsLayoutRoot = new LinearLayout(getContext());
        mCompletionsLayoutRoot.setLayoutParams(new LayoutParams(-1, -1));

        mReturnToQuickMenu = new ImageButton(getContext());
        int size = DensityUtils.dpInt(56);
        LinearLayout.LayoutParams returnToQMParams =
                new LinearLayout.LayoutParams(size, -1, 0);
        returnToQMParams.rightMargin = returnToQMParams.leftMargin =
                returnToQMParams.bottomMargin = returnToQMParams.topMargin = DensityUtils.dpInt(8);

        mReturnToQuickMenu.setPadding(
                returnToQMParams.leftMargin * 2,
                returnToQMParams.topMargin * 2,
                returnToQMParams.leftMargin * 2,
                returnToQMParams.topMargin * 2
        );
        mReturnToQuickMenu.setLayoutParams(returnToQMParams);
        mReturnToQuickMenu.setImageResource(R.drawable.sym_keyboard_close);
        mReturnToQuickMenu.setScaleType(ImageView.ScaleType.FIT_CENTER);
        mReturnToQuickMenu.setId(android.R.id.button1);
        mReturnToQuickMenu.setOnClickListener(v -> toggleQuickMenu(true));
        mCompletionsLayoutRoot.addView(mReturnToQuickMenu);

        mCompletionsLayout = new LinearLayout(getContext());
        mCompletionsLayout.setLayoutParams(new LinearLayout.LayoutParams(-1, -1, 1));

        HorizontalScrollView scroller = new HorizontalScrollView(getContext());
        scroller.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        scroller.addView(mCompletionsLayout);
        mCompletionsLayoutRoot.addView(scroller);
        addView(mCompletionsLayoutRoot);

        // Add quick menu layout
        mQuickMenuLayout = new LinearLayout(getContext());
        mQuickMenuLayout.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        mQuickMenuLayout.setVisibility(GONE);
        mQuickMenuLayout.setGravity(Gravity.CENTER);
        addView(mQuickMenuLayout);
        fillQuickMenu();
    }

    public void setOnSuggestionSelectedListener(OnSuggestionSelectedListener listener) {
        mOnSuggestionSelectedListener = listener;
    }

    public void setCompletion(ExtractedText text, String lang) {
        setCompletionText(text == null ? "" : text.text, lang);
    }

    public void setCompletionText(CharSequence text, String lang) {
        mCompletionsLayout.removeAllViews();

        if (text == null)
            text = "";

        if (lang == null)
            lang = this.superBoard.getKeyboardLanguage().getLanguage();

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

    public void toggleQuickMenu(boolean show) {
        boolean topBarDisabled = SuperDBHelper.getBooleanOrDefault(SettingMap.SET_DISABLE_TOP_BAR);

        if (topBarDisabled) {
            show = false;
        } else if (mOnSuggestionSelectedListener == null) {
            show = true;
        }

        mReturnToQuickMenu.setVisibility(topBarDisabled ? View.GONE : View.VISIBLE);
        mQuickMenuLayout.setVisibility(show ? VISIBLE : GONE);
        mCompletionsLayoutRoot.setVisibility(show ? GONE : VISIBLE);
    }

    @SuppressLint("InlinedApi")
    private void fillQuickMenu() {
        addQMItem(KeyEvent.KEYCODE_DPAD_LEFT, R.drawable.arrow_left, true);
        addQMItemStateful(SuperBoard.KEYCODE_TOGGLE_CTRL, "ctrl");
        addQMItem(KeyEvent.KEYCODE_HENKAN, R.drawable.more_control, false);
        addQMItem(KeyEvent.KEYCODE_NUM, R.drawable.number, false);
        addQMItem(KeyEvent.KEYCODE_KANA, R.drawable.sym_board_emoji, false);
        addQMItem(KeyEvent.KEYCODE_EISU, R.drawable.clipboard, false);
        addQMItemStateful(SuperBoard.KEYCODE_TOGGLE_ALT, "alt");
        addQMItem(KeyEvent.KEYCODE_DPAD_RIGHT, R.drawable.arrow_right, true);
    }

    private void addQMItemStateful(int tag, String text) {
        SuperBoard.Key key = superBoard.createKey(text, null);
        superBoard.setPressEventForKey(key, tag, true);
        key.setLayoutParams(new LinearLayout.LayoutParams(DensityUtils.mpInt(16), -1));
        key.setStateCount(2);
        mQuickMenuLayout.addView(key);
    }

    private void addQMItem(int tag, int drawableRes, boolean repeat) {
        SuperBoard.Key key = superBoard.createKey("", null);
        key.setKeyIcon(drawableRes);
        superBoard.setKeyRepeat(key, repeat);
        superBoard.setPressEventForKey(key, tag, true);
        key.setLayoutParams(new LinearLayout.LayoutParams(DensityUtils.mpInt(16), -1));
        mQuickMenuLayout.addView(key);
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
        tv.setPadding(pad, pad, pad, pad);
        tv.setEllipsize(TextUtils.TruncateAt.END);
        tv.setText(text);
        tv.setOnClickListener(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            tv.setBackground(getSuggestionItemBackground());
        } else {
            tv.setBackgroundDrawable(getSuggestionItemBackground());
        }
        mCompletionsLayout.addView(tv);
    }

    private Drawable getSuggestionItemBackground() {
        int color = SuperDBHelper.getIntOrDefault(SettingMap.SET_KEY_TEXTCLR);
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(ColorUtils.getColorWithAlpha(color, 70));
        gd.setCornerRadius(16);
        return gd;
    }

    private void setBackground(View view, Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(drawable);
        } else {
            view.setBackgroundDrawable(drawable);
        }
    }

    public void reTheme() {
        int color = SuperDBHelper.getIntOrDefault(SettingMap.SET_KEY_TEXTCLR);

        setBackground(mReturnToQuickMenu, getSuggestionItemBackground());
        mReturnToQuickMenu.getDrawable().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        for (int i = 0; i < mCompletionsLayout.getChildCount(); i++) {
            TextView tv = (TextView) mCompletionsLayout.getChildAt(i);
            tv.setTextColor(color);
            float textSize = DensityUtils.mpInt(SuperDBHelper.getFloatedIntOrDefault(SettingMap.SET_KEY_TEXTSIZE));
            tv.setTextSize(textSize);
            setBackground(tv, getSuggestionItemBackground());
        }

        for (int i = 0; i < mQuickMenuLayout.getChildCount(); i++) {
            View view = mQuickMenuLayout.getChildAt(i);
            int keyClr = SuperDBHelper.getIntOrDefault(SettingMap.SET_KEY2_BGCLR);
            int keyPressClr = SuperDBHelper.getIntOrDefault(SettingMap.SET_KEY2_PRESS_BGCLR);
            Drawable keyPressBg = LayoutUtils.getKeyBg(keyClr, keyPressClr, true);

            if (view instanceof SuperBoard.Key) {
                SuperBoard.Key key = (SuperBoard.Key) view;
                key.setKeyItemColor(color);

                setBackground(key, keyPressBg);

                int textSize = SuperDBHelper.getIntOrDefault(SettingMap.SET_KEY_TEXTSIZE);
                key.setKeyTextSize(textSize);

                int shr = SuperDBHelper.getIntOrDefault(SettingMap.SET_KEY_SHADOWSIZE),
                        shc = SuperDBHelper.getIntOrDefault(SettingMap.SET_KEY_SHADOWCLR);
                key.setKeyShadow(shr, shc);

                key.setKeyImageVisible(key.isKeyIconSet());

                switch(key.getNormalPressEvent().first) {
                    case KeyEvent.KEYCODE_EISU:
                        boolean enableClipboard = SuperDBHelper.getBooleanOrDefaultResolved(SettingMap.SET_ENABLE_CLIPBOARD);
                        key.setVisibility(enableClipboard ? View.VISIBLE : View.GONE);
                        break;
                    case KeyEvent.KEYCODE_NUM:
                        boolean numDisabled = SuperDBHelper.getBooleanOrDefault(SettingMap.SET_DISABLE_NUMBER_ROW);
                        key.setVisibility(numDisabled ? View.VISIBLE : View.GONE);
                        break;
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                    case SuperBoard.KEYCODE_TOGGLE_CTRL:
                    case SuperBoard.KEYCODE_TOGGLE_ALT:
                        boolean fnDisabled = SuperDBHelper.getBooleanOrDefault(SettingMap.SET_HIDE_TOP_BAR_FN_BUTTONS);
                        key.setVisibility(fnDisabled ? View.GONE : View.VISIBLE);
                        break;
                }

                setKeyLockStatus(key);
            } else if (view instanceof ImageButton) {
                ImageButton btn = (ImageButton) view;

                if ((int) btn.getTag() == 4) {
                    boolean numDisabled = SuperDBHelper.getBooleanOrDefault(SettingMap.SET_DISABLE_NUMBER_ROW);
                    btn.setVisibility(numDisabled ? View.VISIBLE : View.GONE);
                }

                setBackground(btn, keyPressBg);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    btn.setImageTintList(ColorStateList.valueOf(color));
                } else {
                    btn.getDrawable().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                }
            }
        }
    }

    public void setAllKeyLockStatus() {
        for (int i = 0; i < mQuickMenuLayout.getChildCount(); i++) {
            View v = mQuickMenuLayout.getChildAt(i);
            if (v instanceof SuperBoard.Key) {
                setKeyLockStatus((SuperBoard.Key) v);
            }
        }
    }

    private void setKeyLockStatus(SuperBoard.Key key) {
        if (key == null || key.getNormalPressEvent() == null) {
            return;
        }

        switch (key.getNormalPressEvent().first) {
            case SuperBoard.KEYCODE_TOGGLE_CTRL:
                key.changeState(superBoard.getCtrlState());
                break;
            case SuperBoard.KEYCODE_TOGGLE_ALT:
                key.changeState(superBoard.getAltState());
                break;
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
                SuperBoardApplication.mainHandler.post(() -> onPostExecute(out));
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
            return SuperBoardApplication.getDictDB().getQuery(lang, prefix);
        }

        protected void onPostExecute(final List<String> result) {
            if (!mLoadDictTasks.contains(this)) {
                return;
            }

            toggleQuickMenu(result.isEmpty());

            ((HorizontalScrollView) mCompletionsLayout.getParent()).scrollTo(0, 0);
            mCompletionsLayout.removeAllViews();

            for (String item : result)
                addCompletionView(item);

            // mLoadDictTask = null;
            mLoadDictTasks.remove(this);
        }
    }
}
