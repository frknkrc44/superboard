package org.blinksd.utils.layout;

import android.annotation.SuppressLint;
import android.content.Context;
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

import org.blinksd.SuperBoardApplication;
import org.blinksd.board.R;
import org.blinksd.board.SettingMap;
import org.blinksd.board.SuperBoard;
import org.blinksd.utils.color.ColorUtils;
import org.blinksd.sdb.SuperDBHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SuppressLint("ViewConstructor")
public class SuggestionLayout extends FrameLayout implements View.OnClickListener {
    private final LinearLayout mCompletionsLayout;
    private final List<LoadDictTask> mLoadDictTasks = new ArrayList<>();
    private final ExecutorService mThreadPool = Executors.newFixedThreadPool(64);
    private final LinearLayout mQuickMenuLayout;
    private OnSuggestionSelectedListener mOnSuggestionSelectedListener;
    private OnQuickMenuItemClickListener mOnQuickMenuItemClickListener;
    private String mLastText, mCompleteText;
    private SuperBoard superBoard;
    private final OnClickListener mOnQMClickListener = (v) -> {
        assert (mOnQuickMenuItemClickListener != null)
                : "OnQuickMenuItemClickListener is not specified";

        switch ((int) v.getTag()) {
            case 1:
                mOnQuickMenuItemClickListener.onQuickMenuItemClick(KeyEvent.KEYCODE_DPAD_LEFT);
                break;
            case 2:
                mOnQuickMenuItemClickListener.onQuickMenuItemClick(KeyEvent.KEYCODE_DPAD_RIGHT);
                break;
            case 3:
                mOnQuickMenuItemClickListener.onQuickMenuItemClick(KeyEvent.KEYCODE_NUM);
                break;
            case 4:
                superBoard.setEnabledLayout(
                        superBoard.isNumberKeyboard(superBoard.getCurrentKeyboardIndex())
                                ? SuperBoard.KeyboardType.TEXT
                                : SuperBoard.KeyboardType.NUMBER
                );
                break;
        }
    };

    public SuggestionLayout(SuperBoard superBoard) {
        super(superBoard.getContext());
        Context context = superBoard.getContext();
        mCompletionsLayout = new LinearLayout(context);
        mCompletionsLayout.setLayoutParams(new HorizontalScrollView.LayoutParams(-1, -1));
        HorizontalScrollView scroller = new HorizontalScrollView(context);
        scroller.setLayoutParams(new LayoutParams(-1, -1));
        scroller.addView(mCompletionsLayout);
        addView(scroller);

        // Add quick menu layout
        mQuickMenuLayout = new LinearLayout(context);
        mQuickMenuLayout.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        mQuickMenuLayout.setVisibility(GONE);
        mQuickMenuLayout.setGravity(Gravity.CENTER);
        addView(mQuickMenuLayout);
        this.superBoard = superBoard;
        fillQuickMenu();
    }

    public void setOnSuggestionSelectedListener(OnSuggestionSelectedListener listener) {
        mOnSuggestionSelectedListener = listener;
    }

    public void setOnQuickMenuItemClickListener(OnQuickMenuItemClickListener listener) {
        mOnQuickMenuItemClickListener = listener;
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

        if (str.length() < 1 || str.charAt(str.length() - 1) == ' ') {
            toggleQuickMenu(true);
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
        if (mOnQuickMenuItemClickListener == null) {
            show = false;
        } else if (mOnSuggestionSelectedListener == null) {
            show = true;
        }

        mQuickMenuLayout.setVisibility(show ? VISIBLE : GONE);
        mCompletionsLayout.setVisibility(show ? GONE : VISIBLE);
    }

    private void fillQuickMenu() {
        addQMItem(1, R.drawable.arrow_left);
        addQMItemStateful(SuperBoard.KEYCODE_TOGGLE_CTRL, "ctrl");
        addQMItem(3, R.drawable.more_control);
        addQMItem(4, R.drawable.number);
        addQMItemStateful(SuperBoard.KEYCODE_TOGGLE_ALT, "alt");
        addQMItem(2, R.drawable.arrow_right);
    }

    private void addQMItemStateful(int tag, String text) {
        SuperBoard.Key key = superBoard.createKey(text, null);
        superBoard.setPressEventForKey(key, tag, true);
        key.setLayoutParams(new LinearLayout.LayoutParams(DensityUtils.mpInt(16), -1));
        key.setStateCount(2);
        mQuickMenuLayout.addView(key);
    }

    private void addQMItem(int tag, int drawableRes) {
        ImageButton btn = new ImageButton(getContext());
        btn.setLayoutParams(new LinearLayout.LayoutParams(-2, -1));
        btn.setImageResource(drawableRes);
        btn.setTag(tag);
        btn.setOnClickListener(mOnQMClickListener);
        int color = SuperDBHelper.getIntValueOrDefault(SettingMap.SET_KEY_TEXTCLR);
        int pad = DensityUtils.dpInt(16);
        btn.setScaleType(ImageView.ScaleType.FIT_CENTER);
        btn.setPadding(pad / 2, pad, pad / 2, pad);
        int keyClr = SuperDBHelper.getIntValueOrDefault(SettingMap.SET_KEY_BGCLR);
        int keyPressClr = SuperDBHelper.getIntValueOrDefault(SettingMap.SET_KEY_PRESS_BGCLR);
        Drawable keybg = LayoutUtils.getKeyBg(keyClr, keyPressClr, true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            btn.setBackground(keybg);
        } else {
            btn.setBackgroundDrawable(keybg);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            btn.setImageTintList(ColorStateList.valueOf(color));
            btn.setBackgroundTintList(new ColorStateList(new int[][]{
                    {android.R.attr.state_enabled, android.R.attr.state_pressed},
                    {}
            }, new int[]{
                    keyPressClr,
                    keyClr
            }));
        } else {
            btn.getDrawable().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            btn.setColorFilter(ColorUtils.getColorWithAlpha(color, 70), PorterDuff.Mode.SRC_ATOP);
        }
        mQuickMenuLayout.addView(btn);
    }

    private void addCompletionView(final CharSequence text) {
        TextView tv = new TextView(getContext());
        tv.setGravity(Gravity.CENTER);
        int color = SuperDBHelper.getIntValueOrDefault(SettingMap.SET_KEY_TEXTCLR);
        tv.setTextColor(color);
        float textSize = DensityUtils.mpInt(SuperDBHelper.getFloatedIntValueOrDefault(SettingMap.SET_KEY_TEXTSIZE));
        int pad = DensityUtils.dpInt(8);
        tv.setTextSize(textSize);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -1);
        params.rightMargin = params.topMargin = params.bottomMargin = pad;
        if (mCompletionsLayout.getChildCount() < 1) {
            params.leftMargin = pad;
        }
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
        int color = SuperDBHelper.getIntValueOrDefault(SettingMap.SET_KEY_TEXTCLR);
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(ColorUtils.getColorWithAlpha(color, 70));
        gd.setCornerRadius(16);
        return gd;
    }

    public void reTheme() {
        int color = SuperDBHelper.getIntValueOrDefault(SettingMap.SET_KEY_TEXTCLR);

        for (int i = 0; i < mCompletionsLayout.getChildCount(); i++) {
            TextView tv = (TextView) mCompletionsLayout.getChildAt(i);
            tv.setTextColor(color);
            float textSize = DensityUtils.mpInt(SuperDBHelper.getFloatedIntValueOrDefault(SettingMap.SET_KEY_TEXTSIZE));
            tv.setTextSize(textSize);
            GradientDrawable gd = new GradientDrawable();
            gd.setColor(ColorUtils.getColorWithAlpha(color, 70));
            gd.setCornerRadius(16);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                tv.setBackground(getSuggestionItemBackground());
            } else {
                tv.setBackgroundDrawable(getSuggestionItemBackground());
            }
        }

        for (int i = 0; i < mQuickMenuLayout.getChildCount(); i++) {
            View view = mQuickMenuLayout.getChildAt(i);
            int keyClr = SuperDBHelper.getIntValueOrDefault(SettingMap.SET_KEY2_BGCLR);
            int keyPressClr = SuperDBHelper.getIntValueOrDefault(SettingMap.SET_KEY2_PRESS_BGCLR);
            Drawable keyPressBg = LayoutUtils.getKeyBg(keyClr, keyPressClr, true);

            if (view instanceof SuperBoard.Key) {
                SuperBoard.Key key = (SuperBoard.Key) view;
                key.setKeyItemColor(color);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    key.setBackground(keyPressBg);
                } else {
                    key.setBackgroundDrawable(keyPressBg);
                }

                int textSize = SuperDBHelper.getIntValueOrDefault(SettingMap.SET_KEY_TEXTSIZE);
                key.setKeyTextSize(textSize);

                int shr = SuperDBHelper.getIntValueOrDefault(SettingMap.SET_KEY_SHADOWSIZE),
                        shc = SuperDBHelper.getIntValueOrDefault(SettingMap.SET_KEY_SHADOWCLR);
                key.setKeyShadow(shr, shc);

                key.setKeyImageVisible(key.isKeyIconSet());

                setKeyLockStatus(key);
            } else if (view instanceof ImageButton) {
                ImageButton btn = (ImageButton) view;

                if ((int) btn.getTag() == 4) {
                    boolean numDisabled = SuperDBHelper.getBooleanValueOrDefault(SettingMap.SET_DISABLE_NUMBER_ROW);
                    btn.setVisibility(numDisabled ? View.VISIBLE : View.GONE);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    btn.setBackground(keyPressBg);
                } else {
                    btn.setBackgroundDrawable(keyPressBg);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    btn.setImageTintList(ColorStateList.valueOf(color));
                    btn.setBackgroundTintList(new ColorStateList(new int[][]{
                            {android.R.attr.state_enabled, android.R.attr.state_pressed},
                            {}
                    }, new int[]{
                            keyPressClr,
                            keyClr
                    }));
                } else {
                    btn.getDrawable().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                    btn.setColorFilter(ColorUtils.getColorWithAlpha(color, 70), PorterDuff.Mode.SRC_ATOP);
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

    public interface OnQuickMenuItemClickListener {
        void onQuickMenuItemClick(int action);
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
            } catch (Throwable ignored) {
            }
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
            String lang = p1[0].toLowerCase();
            String prefix = p1[1].toLowerCase();
            return SuperBoardApplication.getDictDB().getQuery(lang, prefix);
        }

        protected void onPostExecute(final List<String> result) {
            if (!mLoadDictTasks.contains(this)) {
                return;
            }

            toggleQuickMenu(result.size() < 1);

            mCompletionsLayout.removeAllViews();

            for (String item : result)
                addCompletionView(item);

            // mLoadDictTask = null;
            mLoadDictTasks.remove(this);
        }
    }
}
