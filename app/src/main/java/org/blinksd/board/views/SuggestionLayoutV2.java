package org.blinksd.board.views;

import static android.os.Build.VERSION.SDK_INT;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
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
import org.blinksd.board.SuperBoardApplication;
import org.blinksd.utils.ColorUtils;
import org.blinksd.utils.DensityUtils;
import org.blinksd.utils.SettingMap;
import org.blinksd.utils.SuperDBHelper;
import org.blinksd.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SuppressLint("ViewConstructor")
public class SuggestionLayoutV2 extends RelativeLayout implements View.OnClickListener {
    private final LinearLayout mCompletionsLayout;
    private final List<LoadDictTask> mLoadDictTasks = new ArrayList<>();
    private final ExecutorService mThreadPool = Executors.newFixedThreadPool(64);
    private OnSuggestionSelectedListener mOnSuggestionSelectedListener;
    private String mLastText, mCompleteText;
    private final SuperBoard superBoard;
    private final FABView fabView;

    public SuggestionLayoutV2(SuperBoard superBoard) {
        super(superBoard.getContext());
        this.superBoard = superBoard;

        int p = DensityUtils.dpInt(8);
        setPadding(p, 0, p, 0);

        mCompletionsLayout = new LinearLayout(getContext());
        mCompletionsLayout.setLayoutParams(new HorizontalScrollView.LayoutParams(-1, -1));

        fabView = new FABView(superBoard.getContext(), superBoard::sendKeyEvent);
        fabView.setLayoutParams(new LayoutParams(-2, -1));
        fabView.setOrientation(FABView.Orientation.TLH);
        fabView.addButton(new FABView.SubButton(R.drawable.arrow_left, KeyEvent.KEYCODE_DPAD_LEFT));
        fabView.addButton(new FABView.SubButton(R.drawable.more_control, KeyEvent.KEYCODE_HENKAN));
        if (SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            fabView.addButton(new FABView.SubButton(R.drawable.sym_board_emoji, KeyEvent.KEYCODE_KANA));
        }
        fabView.addButton(new FABView.SubButton(R.drawable.clipboard, KeyEvent.KEYCODE_EISU));
        fabView.addButton(new FABView.SubButton(R.drawable.arrow_right, KeyEvent.KEYCODE_DPAD_RIGHT));

        boolean topBarDisabled = SuperDBHelper.getBooleanOrDefault(SettingMap.SET_DISABLE_TOP_BAR);
        fabView.setVisibility(topBarDisabled ? GONE : VISIBLE);

        HorizontalScrollView scroller = new HorizontalScrollView(getContext());
        var params = new LayoutParams(-1, -1);

        fabView.getChildAt(0).addOnLayoutChangeListener((v, left, top, right, bottom, leftWas, topWas, rightWas, bottomWas) -> {
            params.leftMargin = (int) (v.getMeasuredWidth() * 1.15f);
            scroller.setLayoutParams(params);
        });

        scroller.setLayoutParams(params);
        scroller.addView(mCompletionsLayout);
        addView(scroller);

        addView(fabView);
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
        mCompletionsLayout.addView(tv);
    }

    private Drawable getSuggestionItemBackground() {
        int color = SuperDBHelper.getIntOrDefault(SettingMap.SET_KEY_TEXTCLR);
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(ColorUtils.getColorWithAlpha(color, 70));
        gd.setCornerRadius(16);
        return gd;
    }

    public void reTheme() {
        int color = SuperDBHelper.getIntOrDefault(SettingMap.SET_KEY_TEXTCLR);
        for (int i = 0; i < mCompletionsLayout.getChildCount(); i++) {
            TextView tv = (TextView) mCompletionsLayout.getChildAt(i);
            tv.setTextColor(color);
            float textSize = DensityUtils.mpInt(SuperDBHelper.getFloatedIntOrDefault(SettingMap.SET_KEY_TEXTSIZE));
            tv.setTextSize(textSize);
            ViewUtils.setBackground(tv, getSuggestionItemBackground());
        }

        fabView.reTheme(color);
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

            ((HorizontalScrollView) mCompletionsLayout.getParent()).scrollTo(0, 0);
            mCompletionsLayout.removeAllViews();

            for (String item : result)
                addCompletionView(item);

            mLoadDictTasks.remove(this);
        }
    }
}
