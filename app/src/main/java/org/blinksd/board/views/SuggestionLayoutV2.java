package org.blinksd.board.views;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.ExtractedText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
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
public class SuggestionLayoutV2 extends LinearLayout implements View.OnClickListener {
    private final LinearLayout mCompletionsLayout;
    private final List<LoadDictTask> mLoadDictTasks = new ArrayList<>();
    private final ExecutorService mThreadPool = Executors.newFixedThreadPool(64);
    private OnSuggestionSelectedListener mOnSuggestionSelectedListener;
    private String mLastText, mCompleteText;
    private final SuperBoard superBoard;

    public SuggestionLayoutV2(SuperBoard superBoard) {
        super(superBoard.getContext());
        this.superBoard = superBoard;

        mCompletionsLayout = new LinearLayout(getContext());
        mCompletionsLayout.setLayoutParams(new HorizontalScrollView.LayoutParams(-1, -1));

        FABView fabView = new FABView(superBoard.getContext());
        fabView.setLayoutParams(new LayoutParams(-2, -1, 0));
        fabView.setOrientation(FABView.Orientation.TLH);
        fabView.addButton(new FABView.SubButton(R.drawable.arrow_left, null));
        fabView.addButton(new FABView.SubButton(R.drawable.arrow_right, null));

        boolean topBarDisabled = SuperDBHelper.getBooleanOrDefault(SettingMap.SET_DISABLE_TOP_BAR);
        fabView.setVisibility(topBarDisabled ? GONE : VISIBLE);

        addView(fabView);

        HorizontalScrollView scroller = new HorizontalScrollView(getContext());
        scroller.setLayoutParams(new LayoutParams(-1, -1, 1));
        scroller.addView(mCompletionsLayout);
        addView(scroller);
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

    public void toggleQuickMenu(boolean show) {
        boolean topBarDisabled = SuperDBHelper.getBooleanOrDefault(SettingMap.SET_DISABLE_TOP_BAR);

        if (topBarDisabled) {
            show = false;
        } else if (mOnSuggestionSelectedListener == null) {
            show = true;
        }

        // TODO: Show or hide
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

            mLoadDictTasks.remove(this);
        }
    }
}
