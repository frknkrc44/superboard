package org.blinksd.utils.layout;

import android.content.*;
import android.os.*;
import android.text.*;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;
import java.util.*;
import org.blinksd.board.*;
import org.blinksd.utils.dictionary.*;
import org.superdroid.db.*;
import android.view.View.*;
import org.blinksd.*;

public class SuggestionLayout extends FrameLayout implements View.OnClickListener {
	private LinearLayout mCompletionsLayout;
	private OnSuggestionSelectedListener mOnSuggestionSelectedListener;
	private String mLastText, mCompleteText;
	private List<LoadDictTask> mLoadDictTasks = new ArrayList<>();
	
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
	
	public void setCompletionText(final CharSequence text, final String lang){
		mCompletionsLayout.removeAllViews();
		
		if(text == null)
			return;
		
		String str = text.toString();
		mCompleteText = str;
		str = str.substring(str.lastIndexOf(' ')+1);
		str = str.substring(str.lastIndexOf('\n')+1);
		mLastText = str;
		LoadDictTask task = new LoadDictTask();
		mLoadDictTasks.add(task);
		task.execute(lang, str);
	}
	
	private void addCompletionView(final CharSequence text){
		TextView tv = new TextView(getContext());
		tv.setGravity(Gravity.CENTER);
		tv.setTextColor(SuperDBHelper.getIntValueOrDefault(SettingMap.SET_KEY_TEXTCLR));
		float textSize = DensityUtils.mpInt(DensityUtils.getFloatNumberFromInt(SuperDBHelper.getIntValueOrDefault(SettingMap.SET_KEY_TEXTSIZE)));
		tv.setTextSize(textSize);
		tv.setLayoutParams(new LinearLayout.LayoutParams(-2, -1));
		int pad = DensityUtils.dpInt(8);
		tv.setPadding(pad,pad,pad,pad);
		tv.setEllipsize(TextUtils.TruncateAt.END);
		tv.setText(text);
		tv.setOnClickListener(this);
		mCompletionsLayout.addView(tv);
	}
	
	public void retheme(){
		for(int i = 0;i < mCompletionsLayout.getChildCount();i++){
			TextView tv = (TextView) mCompletionsLayout.getChildAt(i);
			tv.setTextColor(SuperDBHelper.getIntValueOrDefault(SettingMap.SET_KEY_TEXTCLR));
			float textSize = DensityUtils.mpInt(DensityUtils.getFloatNumberFromInt(SuperDBHelper.getIntValueOrDefault(SettingMap.SET_KEY_TEXTSIZE)));
			tv.setTextSize(textSize);
		}
	}
	
	@Override
	public void onClick(View p1){
		if(mOnSuggestionSelectedListener != null){
			mOnSuggestionSelectedListener.onSuggestionSelected(mCompleteText, mLastText, ((TextView) p1).getText());
		}
	}
	
	private class LoadDictTask extends AsyncTask<String,Void,List<String>>{

		@Override
		protected void onPreExecute(){
			try {
				for(LoadDictTask task : mLoadDictTasks){
					if(task != this){
						task.cancel(true);
						mLoadDictTasks.remove(task);
					}
				}
			} catch(Throwable t){}
			super.onPreExecute();
		}

		@Override
		protected List<String> doInBackground(String[] p1){
			return SuperBoardApplication.getDictDB().getQuery(p1[0].toLowerCase(), p1[1].toLowerCase());
		}

		@Override
		protected void onPostExecute(final List<String> result){
			mCompletionsLayout.removeAllViews();
			
			for(String item : result)
				addCompletionView(item);
			
			mLoadDictTasks.remove(this);
			super.onPostExecute(result);
		}
	}
	
	public interface OnSuggestionSelectedListener {
		void onSuggestionSelected(CharSequence text, CharSequence oldText, CharSequence suggestion);
	}
}
