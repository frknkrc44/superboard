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

public class SuggestionLayout extends FrameLayout implements View.OnClickListener {
	private LinearLayout mCompletionsLayout;
	private OnSuggestionSelectedListener mOnSuggestionSelectedListener;
	private String mLastText;
	private ExtractedText mLastExtractedText;
	
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
			
		mLastExtractedText = text;
		setCompletionText(text.text, lang);
	}
	
	private void setCompletionText(final CharSequence text, final String lang){
		mCompletionsLayout.removeAllViews();
		
		if(text == null || text.length() < 1)
			return;
		
		String str = text.toString();
		str = str.substring(str.lastIndexOf(" ")+1);
		mLastText = str;
		new LoadDictTask().execute(str, lang);
	}
	
	private void addCompletionView(final CharSequence text){
		TextView tv = new TextView(getContext());
		tv.setGravity(Gravity.CENTER);
		tv.setTextColor(SuperDBHelper.getIntValueOrDefault(SettingMap.SET_KEY_TEXTCLR));
		float textSize = DensityUtils.mpInt(AppSettingsV2.getFloatNumberFromInt(SuperDBHelper.getIntValueOrDefault(SettingMap.SET_KEY_TEXTSIZE)));
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
			float textSize = DensityUtils.mpInt(AppSettingsV2.getFloatNumberFromInt(SuperDBHelper.getIntValueOrDefault(SettingMap.SET_KEY_TEXTSIZE)));
			tv.setTextSize(textSize);
		}
	}
	
	@Override
	public void onClick(View p1){
		if(mOnSuggestionSelectedListener != null){
			mOnSuggestionSelectedListener.onSuggestionSelected(mLastExtractedText, mLastText, ((TextView) p1).getText());
		}
	}
	
	private class LoadDictTask extends AsyncTask<String,Void,List<String>>{

		@Override
		protected List<String> doInBackground(String[] p1){
			return DictionaryProvider.getSuggestions(p1[0], p1[1]);
		}

		@Override
		protected void onPostExecute(final List<String> result){
			mCompletionsLayout.removeAllViews();
			for(String item : result) {
				addCompletionView(item);
			}
			super.onPostExecute(result);
		}
	}
	
	public interface OnSuggestionSelectedListener {
		void onSuggestionSelected(ExtractedText text, CharSequence oldText, CharSequence suggestion);
	}
}
