package org.blinksd.board;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.Executors;

public class SetupActivity extends Activity {
	private SetupResources sr;
	private LinearLayout ml,ll;
	private int tempClr;
	private Dots dot;

	@Override
    protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		if(Build.VERSION.SDK_INT <= 11) {
			if(!isInputMethodEnabled()) {
				startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS));
				h.post(new ImeEnabledCheckerRunnable());
			} else if(!isInputMethodSelected()) {
				h.post(new ImeSelectedCheckerRunnable());
			} else {
				startSettings();
			}
			return;
		} else if(isInputMethodEnabled() && isInputMethodSelected()){
			startSettings();
			return;
		}
        sr = new SetupResources();
        setContentView(sr.mainView());
		h.sendEmptyMessage(0);
    }

	private void startSettings() {
		startActivity(new Intent(this,AppSettingsV2.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		finish();
	}

	Handler h = new Handler(Looper.getMainLooper()){
		@Override
		public void handleMessage(Message msg){
			this.removeMessages(0);
			if(sr != null) sr.skip(SetupResources.SEEK_NEXT);
			this.sendEmptyMessageDelayed(0,250);
			super.handleMessage(msg);
		}
	};

	private class ImeEnabledCheckerRunnable implements Runnable {
		@Override
		public void run() {
			if(!isInputMethodEnabled()) {
				h.postDelayed(this, 250);
				return;
			}

			h.post(new ImeSelectedCheckerRunnable());
		}
	}

	private class ImeSelectedCheckerRunnable implements Runnable {
		@Override
		public void run() {
			if(!isInputMethodSelected()) {
				((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).showInputMethodPicker();
				h.postDelayed(this, 2000);
				return;
			}

			startSettings();
		}
	}
	
	private class SetupResources {

		private int lastScroll = 0;
		private int lscr = 0;
		private final int textSize;
		private ViewGroup scroll;
		private ImageView tv,tn;

		public SetupResources(){
			textSize = dpInt(12);
		}

		private int dpInt(int px){
			return (int)dp(px);
		}

		private float dp(int px){
			return Resources.getSystem().getDisplayMetrics().density * px;
		}

		private int getWidth(){
			return Resources.getSystem().getDisplayMetrics().widthPixels;
		}

		public View mainView(){
			if(scroll == null){
				View[] tabs = new View[]{
					newTab(tab1()),
					newTab(tab2()),
					newTab(tab3()),
					newTab(tab4()),
					newTab(tab5())
				};
				dot = new Dots(tabs.length);
				ml = new LinearLayout(SetupActivity.this);
				ml.setLayoutParams(new LinearLayout.LayoutParams(-1,-1,0));
				ml.setOrientation(LinearLayout.VERTICAL);
				LinearLayout ll = new LinearLayout(SetupActivity.this);
				ll.setLayoutParams(new LinearLayout.LayoutParams(getWidth() * tabs.length,-1, 1));
				for(View v : tabs) ll.addView(v);
				ml.addView(ll);
				ml.addView(bottomBar());
				scroll = ll;
			}
			return ml;
		}

		private LinearLayout newTab(View slideItem){
			LinearLayout ll = new LinearLayout(slideItem.getContext());
			ll.setLayoutParams(new LinearLayout.LayoutParams(-1,-1,1));
			ll.setGravity(Gravity.CENTER);
			ll.addView(slideItem);
			return ll;
		}

		private View tab1(){
			LinearLayout ll = new LinearLayout(SetupActivity.this);
			ll.setLayoutParams(new LinearLayout.LayoutParams(-1,-2));
			ll.setGravity(Gravity.CENTER);
			ll.setOrientation(LinearLayout.VERTICAL);
			ImageView iv = new ImageView(SetupActivity.this);
			int imgSize = dpInt(128);
			iv.setLayoutParams(new LinearLayout.LayoutParams(imgSize,imgSize));
			iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
			String label = "";
			try {
				ApplicationInfo info = getPackageManager().getApplicationInfo(getPackageName(),0);
				Drawable d = info.loadIcon(getPackageManager());
				iv.setImageDrawable(d);
				label = info.loadLabel(getPackageManager()).toString();
			} catch(Throwable t){}
			TextView tv = new TextView(SetupActivity.this);
			tv.setLayoutParams(new LinearLayout.LayoutParams(-2,-2));
			tv.setText(String.format(getResources().getString(R.string.wizard_welcome),label));
			tv.setTextColor(textColor);
			tv.setGravity(Gravity.CENTER);
			tv.setTextSize(textSize);
			int pad = dpInt(8);
			tv.setPadding(pad,pad,pad,pad);
			ll.addView(iv);
			View s = new View(SetupActivity.this);
			s.setLayoutParams(new LinearLayout.LayoutParams(-1,textSize * 2));
			ll.addView(s);
			ll.addView(tv);
			return ll;
		}

		private View tab2(){
			return textViewAndBtn(R.string.wizard_enable,R.string.wizard_enablebtn,ENABLE_INPUT);
		}

		private View tab3(){
			return textViewAndBtn(R.string.wizard_select,R.string.wizard_selectbtn,SELECT_INPUT,true);
		}

		private View tab4(){
			return textViewAndBtn(R.string.wizard_settings,R.string.wizard_settingsbtn,KEYBOARD_SETTINGS);
		}

		private View tab5(){
			return textViewAndBtn(R.string.wizard_finish,R.string.wizard_finishbtn,WIZARD_DONE);
		}
		
		private View textViewAndBtn(int s1, int s2, int ocl){
			return textViewAndBtn(s1, s2, ocl, false);
		}

		private View textViewAndBtn(int s1, int s2, int ocl, boolean fwan){
			LinearLayout ll = new LinearLayout(SetupActivity.this);
			ll.setLayoutParams(new LinearLayout.LayoutParams(-1,-2));
			ll.setOrientation(LinearLayout.VERTICAL);
			ll.setGravity(Gravity.CENTER);
			TextView tv = new TextView(SetupActivity.this);
			tv.setLayoutParams(new LinearLayout.LayoutParams(-1,-2));
			if(fwan){
				String label = "";
				try {
					label = getPackageManager().getApplicationInfo(getPackageName(),0).loadLabel(getPackageManager()).toString();
				} catch(Throwable t){}
				tv.setText(String.format(getResources().getString(s1),label));
			} else {
				tv.setText(s1);
			}
			tv.setTextColor(textColor);
			tv.setGravity(Gravity.CENTER);
			tv.setTextSize(textSize);
			int pad = dpInt(8);
			tv.setPadding(pad,pad,pad,pad);
			Button bt = new Button(SetupActivity.this);
			bt.setLayoutParams(new LinearLayout.LayoutParams(-2,dpInt(64)));
			bt.setText(s2);
			bt.setTextColor(textColor);
			bt.setTextSize(textSize / 1.5f);
			bt.setTag(ocl);
			bt.setBackgroundDrawable(getSelectableItemBg());
			bt.setOnClickListener(onClk);
			ll.addView(tv);
			ll.addView(bt);
			return ll;
		}

		private View bottomBar(){
			if(ll == null){
				ll = new LinearLayout(SetupActivity.this);
				ll.setLayoutParams(new LinearLayout.LayoutParams(-1,-2,0));
				tv = new ImageView(SetupActivity.this);
				tv.setLayoutParams(new LinearLayout.LayoutParams(-1,-2,0.66f));
				tv.setImageBitmap(drawPrev());
				tv.setTag(SEEK_BACK);
				tv.setOnClickListener(onClk);
				tv.setVisibility(View.INVISIBLE);
				int p = dpInt(16);
				tv.setPadding(p,p,p,p);
				tv.setBackgroundDrawable(getSelectableItemBg());
				tn = new ImageView(SetupActivity.this);
				tn.setLayoutParams(tv.getLayoutParams());
				tn.setTag(SEEK_NEXT);
				tn.setOnClickListener(onClk);
				tn.setImageBitmap(drawNext());
				tn.setBackgroundDrawable(getSelectableItemBg());
				tn.setPadding(p,p,p,p);
				ll.addView(tv);
				ll.addView(dot.getDotView());
				ll.addView(tn);
				ViewGroup bottom = ll;
			}
			return ll;
		}
		
		public Drawable getSelectableItemBg(){
			if(Build.VERSION.SDK_INT < 14)
				return null;
		
			return csibg(getResources().getDrawable(
							 getTheme().obtainStyledAttributes(new int[]{android.R.attr.selectableItemBackground}
																 ).getResourceId(0,0)),false);
		}

		private Drawable csibg(Drawable d,boolean b){
			int color = textColor - 0x88000000;
			try {
				if(d.getClass().getName().contains("RippleDrawable")){
					Method m = d.getClass().getDeclaredMethod("setColor",new Class[]{ColorStateList.class});
					m.setAccessible(true);
					m.invoke(d,new ColorStateList(new int[][]{new int[]{android.R.attr.state_enabled}},new int[]{color}));
				} else {
					d.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
				}
			} catch(Throwable ignored) {}
			return d;
		}

		private final View.OnClickListener onClk = view -> navigateViews((int)view.getTag());

		private void navigateViews(int seek){
			switch(seek){
				case SEEK_BACK:
					if(seek(false)){
						lastScroll = (getWidth() + lastScroll);
						if(Build.VERSION.SDK_INT > 14)
							scroll.animate().translationX(lastScroll).setDuration(200);
						else if (Build.VERSION.SDK_INT < 11)
							setScrollXAPI8(scroll, lastScroll);
						else scroll.setTranslationX(lastScroll);
						lscr--;
						skip(seek);
					}
					break;
				case SEEK_NEXT:
					if(seek(true)){
						lastScroll = (-getWidth() + lastScroll);
						if(Build.VERSION.SDK_INT > 14)
							scroll.animate().translationX(lastScroll).setDuration(200);
						else if (Build.VERSION.SDK_INT < 11)
							setScrollXAPI8(scroll, lastScroll);
						else scroll.setTranslationX(lastScroll);
						lscr++;
						skip(seek);
					}
					break;
				case ENABLE_INPUT:
					startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS));
					break;
				case SELECT_INPUT:
					((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).showInputMethodPicker();
					break;
				case KEYBOARD_SETTINGS:
					startActivity(new Intent(SetupActivity.this,AppSettingsV2.class));
					break;
				case WIZARD_DONE:
					// bottom.setVisibility(View.INVISIBLE);
					// setLauncherIconVisibility(c.getPackageManager(),false);
					finish();
					lscr = lastScroll = 0;
					break;
				default:
					break;
			}
			dot.setSelection(lscr);
			tv.setVisibility(seek(false) ? View.VISIBLE : View.INVISIBLE);
			tn.setVisibility((lscr == 0) || (lscr == (scroll.getChildCount() - 2)) ? View.VISIBLE : View.INVISIBLE);
		}

		@SuppressLint("SoonBlockedPrivateApi")
		private void setScrollXAPI8(View view, int scroll) {
			try {
				Field scrollX = View.class.getDeclaredField("mScrollX");
				scrollX.setAccessible(true);
				scrollX.set(view, scroll);
				view.invalidate();
			} catch(Throwable ignored) {}
		}

		private boolean seek(boolean next){
			return next ? (lscr < (scroll.getChildCount() - 1)) : (lscr > 0);
		}

		public void skip(int seek){
			if((lscr == 1 && isInputMethodEnabled()) || (lscr == 2 && isInputMethodSelected()))
				navigateViews(seek);
		}


		public Bitmap drawPrev(){
			int d = 3;
			Bitmap b = Bitmap.createBitmap(dpInt(100)/d,dpInt(100)/d,Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(b);
			Paint p = new Paint();
			p.setStrokeWidth(dp(10)/d);
			p.setStrokeCap(Paint.Cap.ROUND);
			p.setStrokeJoin(Paint.Join.ROUND);
			p.setColor(SetupResources.textColor);
			c.drawLine(dp(60)/d,dp(10)/d,dp(30)/d,dp(44)/d,p);
			c.drawLine(dp(30)/d,dp(46)/d,dp(60)/d,dp(80)/d,p);
			c.drawPoint(dp(29)/d,dp(45)/d,p);
			return b;
		}
		
		public Bitmap drawNext(){
			int d = 3;
			Bitmap b = Bitmap.createBitmap(dpInt(100)/d,dpInt(100)/d,Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(b);
			Paint p = new Paint();
			p.setStrokeWidth(dp(10)/d);
			p.setStrokeCap(Paint.Cap.ROUND);
			p.setStrokeJoin(Paint.Join.ROUND);
			p.setColor(SetupResources.textColor);
			c.drawLine(dp(30)/d,dp(10)/d,dp(60)/d,dp(44)/d,p);
			c.drawLine(dp(60)/d,dp(46)/d,dp(30)/d,dp(80)/d,p);
			c.drawPoint(dp(61)/d,dp(45)/d,p);
			return b;
		}

		public static final int SEEK_BACK = 1, SEEK_NEXT = 2, ENABLE_INPUT = 3,
		SELECT_INPUT = 4, KEYBOARD_SETTINGS = 5, WIZARD_DONE = 6, textColor = 0xFFDEDEDE;
	}
	
	public class Dots {
		
		private LinearLayout dotView;
		private final int dot;
		
		public Dots(int dots){
			dot = dots;
		}
		
		public View getDotView(){
			return getDotView(false);
		}
		
		public View getDotView(boolean first){
			dotView = new LinearLayout(getBaseContext());
			dotView.setLayoutParams(new LinearLayout.LayoutParams(-1,-1,0.33f));
			dotView.setGravity(Gravity.CENTER);
			for(int i = 0;i < dot;i++){
				View v = new View(getBaseContext());
				v.setLayoutParams(new LinearLayout.LayoutParams(sr.textSize,sr.textSize));
				v.setId(i);
				LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) v.getLayoutParams();
				lp.leftMargin = lp.rightMargin = sr.dpInt(2);
				dotView.addView(v);
			}
			if(!first) setSelection(0);
			return dotView;
		}
		
		public void setSelection(int index){
			View tempView;
			for(int i = 0;i < dot;i++){
				tempView = dotView.getChildAt(i);
				tempClr = (SetupResources.textColor - (tempView.getId() == index ? 0 : 0xAA000000));
				tempView.setBackgroundDrawable(getBg(tempClr));
			}
		}
	}
	
	private static GradientDrawable getBg(int color){
		GradientDrawable gd = new GradientDrawable();
		gd.setColor(color);
		gd.setCornerRadius(100f);
		return gd;
	}
	
	private boolean isInputMethodEnabled(){
		String defaultIME = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_INPUT_METHODS);
		return !TextUtils.isEmpty(defaultIME) && defaultIME.contains(getPackageName());
	}

	private boolean isInputMethodSelected(){
		String defaultIME = Settings.Secure.getString(getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
		return !TextUtils.isEmpty(defaultIME) && defaultIME.contains(getPackageName());
	}

	@Override
	public void onBackPressed(){
		System.exit(0);
	}
}
