package org.blinksd.utils.layout;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.ExtractedText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.blinksd.SuperBoardApplication;
import org.blinksd.board.SettingMap;
import org.blinksd.utils.color.ColorUtils;
import org.superdroid.db.SuperDBHelper;

import java.util.ArrayList;
import java.util.List;

public class SuggestionLayout extends FrameLayout implements View.OnClickListener {
	private LinearLayout mCompletionsLayout;
	private OnSuggestionSelectedListener mOnSuggestionSelectedListener;
	private String mLastText, mCompleteText;
	private List<LoadDictTask> mLoadDictTasks = new ArrayList<>();
    private ExecutorService mThreadPool = Executors.newFixedThreadPool(64);
    // private LoadDictTask mLoadDictTask;
	
	public SuggestionLayout(Context context){
		super(context);
		mCompletionsLayout = new LinearLayout(context);
		mCompletionsLayout.setLayoutParams(new HorizontalScrollView.LayoutParams(-1, -1));
		HorizontalScrollView scroller = new HorizontalScrollView(context);
		scroller.setLayoutParams(new LayoutParams(-1, -1));
		scroller.addView(mCompletionsLayout);
		addView(scroller);
	}
	
	public void setOnSuggestionSelectedListener(OnSuggestionSelectedListener listener){
		mOnSuggestionSelectedListener = listener;
	}
	
	public void setCompletion(ExtractedText text, String lang){
		mCompletionsLayout.removeAllViews();

		if(text == null)
			return;
			
		setCompletionText(text.text, lang);
	}
	
	public void setCompletionText(CharSequence text, final String lang){
		mCompletionsLayout.removeAllViews();
		
		if(text == null)
			text = "";
            
        /*    
        if(text.length() < 1) {
            SuperBoardApplication.mainHandler.postDelayed(() -> {
                for(LoadDictTask task : mLoadDictTasks){
				    task.cancel(true);
				    mLoadDictTasks.remove(task);
		        }
                mCompletionsLayout.removeAllViews();
            }, 300);
            return;
        }
        */
        
		String str = text.toString();
		mCompleteText = str;
		str = str.substring(str.lastIndexOf(' ')+1);
		str = str.substring(str.lastIndexOf('\n')+1);
		mLastText = str;
		LoadDictTask task = new LoadDictTask();
        // mLoadDictTask = task;
		mLoadDictTasks.add(task);
		task.execute(lang, str);
	}
	
	private void addCompletionView(final CharSequence text){
		TextView tv = new TextView(getContext());
		tv.setGravity(Gravity.CENTER);
        int color = SuperDBHelper.getIntValueOrDefault(SettingMap.SET_KEY_TEXTCLR);
		tv.setTextColor(color);
		float textSize = DensityUtils.mpInt(DensityUtils.getFloatNumberFromInt(SuperDBHelper.getIntValueOrDefault(SettingMap.SET_KEY_TEXTSIZE)));
        int pad = DensityUtils.dpInt(8);
		tv.setTextSize(textSize);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -1);
        params.rightMargin = params.topMargin = params.bottomMargin = pad;
        if(mCompletionsLayout.getChildCount() < 1){
            params.leftMargin = pad;
        }
		tv.setLayoutParams(params);
		tv.setPadding(pad,pad,pad,pad);
		tv.setEllipsize(TextUtils.TruncateAt.END);
		tv.setText(text);
		tv.setOnClickListener(this);
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(ColorUtils.getColorWithAlpha(color, 70));
        gd.setCornerRadius(16);
        tv.setBackground(gd);
		mCompletionsLayout.addView(tv);
	}
	
	public void retheme(){
		for(int i = 0;i < mCompletionsLayout.getChildCount();i++){
			TextView tv = (TextView) mCompletionsLayout.getChildAt(i);
			int color = SuperDBHelper.getIntValueOrDefault(SettingMap.SET_KEY_TEXTCLR);
		    tv.setTextColor(color);
			float textSize = DensityUtils.mpInt(DensityUtils.getFloatNumberFromInt(SuperDBHelper.getIntValueOrDefault(SettingMap.SET_KEY_TEXTSIZE)));
			tv.setTextSize(textSize);
            GradientDrawable gd = new GradientDrawable();
            gd.setColor(ColorUtils.getColorWithAlpha(color, 50));
            gd.setCornerRadius(16);
            tv.setBackground(gd);
		}
	}
	
	@Override
	public void onClick(View p1){
		if(mOnSuggestionSelectedListener != null){
			mOnSuggestionSelectedListener.onSuggestionSelected(mCompleteText, mLastText, ((TextView) p1).getText());
		}
	}
	
	private class LoadDictTask {
        public void execute(String... args) {
            onPreExecute();
            SuperBoardApplication.mainHandler.postDelayed(() -> {
                mThreadPool.execute(() -> {
                    List<String> out = doInBackground(args);
                    SuperBoardApplication.mainHandler.post(() -> onPostExecute(out));
                });
            }, 100);
        }
        
        private void cancel(boolean terminate) {
            try {
                mThreadPool.awaitTermination(terminate ? 1 : 1000, TimeUnit.MILLISECONDS);
            } catch(Throwable ignored) {}
        }
        
		protected void onPreExecute(){
            /*
            try {
                if (mLoadDictTask != null) {
                    mLoadDictTask.cancel(true);
                    mLoadDictTask = this;
                }
            } catch (Throwable t) {}
            */
            
			try {
				for(LoadDictTask task : mLoadDictTasks){
                    if(task != this) {
					    task.cancel(true);
					    mLoadDictTasks.remove(task);
                    }
				}
			} catch(Throwable t){}
		}

		protected List<String> doInBackground(String[] p1){
			return SuperBoardApplication.getDictDB().getQuery(p1[0].toLowerCase(), p1[1].toLowerCase());
		}

		protected void onPostExecute(final List<String> result){
            if (!mLoadDictTasks.contains(this)) {
                return;
            }
            
			mCompletionsLayout.removeAllViews();
			
			for(String item : result)
				addCompletionView(item);
			
            // mLoadDictTask = null;
			mLoadDictTasks.remove(this);
		}
	}
	
	public interface OnSuggestionSelectedListener {
		void onSuggestionSelected(CharSequence text, CharSequence oldText, CharSequence suggestion);
	}
}
