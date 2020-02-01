package org.blinksd.board;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.content.res.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.provider.*;
import android.text.*;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;
import java.lang.reflect.*;

public class SetupActivity extends Activity {

	private SetupResources sr;
	private LinearLayout ml,ll;
	private int tempClr;
	private Dots dot;
	
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
		if(isInputMethodEnabled() && isInputMethodSelected()){
			startActivity(new Intent(this,AppSettingsV2.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
			finish();
			return;
		}
        sr = new SetupResources();
        setContentView(sr.mainView());
		h.sendEmptyMessage(0);
    }

	Handler h = new Handler(){
		@Override
		public void handleMessage(Message msg){
			this.removeMessages(0);
			if(sr != null) sr.skip(sr.SEEK_NEXT);
			this.sendEmptyMessageDelayed(0,250);
			super.handleMessage(msg);
		}
	};
	
	private class SetupResources {

		private int lastScroll = 0, lscr = 0,textSize;
		private ViewGroup scroll,bottom;
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
				ml.setLayoutParams(new LinearLayout.LayoutParams(-1,-1));
				ml.setOrientation(LinearLayout.VERTICAL);
				LinearLayout ll = new LinearLayout(SetupActivity.this);
				ll.setLayoutParams(new LinearLayout.LayoutParams(getWidth() * tabs.length,-1,1));
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
				bottom = ll;
			}
			return ll;
		}
		
		public Drawable getSelectableItemBg(){
			return csibg(getResources().getDrawable(
							 getTheme().obtainStyledAttributes(new int[]{android.R.attr.selectableItemBackground}
																 ).getResourceId(0,0)),false);
		}

		private Drawable csibg(Drawable d,boolean b){
			int color = textColor - 0x88000000;
			Method m = null;
			try {
				if(d.getClass().getName().contains("RippleDrawable")){
					m = d.getClass().getDeclaredMethod("setColor",new Class[]{ColorStateList.class});
					m.setAccessible(true);
					m.invoke(d,new ColorStateList(new int[][]{new int[]{android.R.attr.state_enabled}},new int[]{color}));
				} else {
					m = d.getClass().getDeclaredMethod("setColorFilter",new Class[]{int.class,PorterDuff.Mode.class});
					m.setAccessible(true);
					m.invoke(d,color,PorterDuff.Mode.SRC_ATOP);
				}
			} catch(Exception | Error e){}
			return d;
		}

		private View.OnClickListener onClk = new View.OnClickListener(){
			@Override
			public void onClick(View view){
				navigateViews((int)view.getTag());
			}
		};

		private int navigateViews(int seek){
			switch(seek){
				case SEEK_BACK:
					if(seek(false)){
						lastScroll = (getWidth() + lastScroll);
						if(Build.VERSION.SDK_INT > 14)
							scroll.animate().translationX(lastScroll).setDuration(200);
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
			return 0;
		}

		private boolean seek(boolean next){
			return next ? (lscr < (scroll.getChildCount() - 1)) : (lscr > 0);
		}

		public int skip(int seek){
			if((lscr == 1 && isInputMethodEnabled()) || (lscr == 2 && isInputMethodSelected()))
				navigateViews(seek);
			return 0;
		}

		
		public int setLauncherIconVisibility(PackageManager pm, boolean visible){
			pm.setComponentEnabledSetting(getComponentName(), 
										  visible ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : 
										  PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
			return 0;
		}

		public Bitmap drawPrev(){
			int d = 3;
			Bitmap b = Bitmap.createBitmap(dpInt(100)/d,dpInt(100)/d,Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(b);
			Paint p = new Paint();
			p.setStrokeWidth(dpInt(10)/d);
			p.setStrokeCap(Paint.Cap.ROUND);
			p.setStrokeJoin(Paint.Join.ROUND);
			p.setColor(SetupResources.textColor);
			c.drawLine(dpInt(60)/d,dpInt(10)/d,dpInt(30)/d,dpInt(44)/d,p);
			c.drawLine(dpInt(30)/d,dpInt(46)/d,dpInt(60)/d,dpInt(80)/d,p);
			c.drawPoint(dpInt(29)/d,dpInt(45)/d,p);
			return b;
		}
		
		public Bitmap drawNext(){
			int d = 3;
			Bitmap b = Bitmap.createBitmap(dpInt(100)/d,dpInt(100)/d,Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(b);
			Paint p = new Paint();
			p.setStrokeWidth(dpInt(10)/d);
			p.setStrokeCap(Paint.Cap.ROUND);
			p.setStrokeJoin(Paint.Join.ROUND);
			p.setColor(SetupResources.textColor);
			c.drawLine(dpInt(30)/d,dpInt(10)/d,dpInt(60)/d,dpInt(44)/d,p);
			c.drawLine(dpInt(60)/d,dpInt(46)/d,dpInt(30)/d,dpInt(80)/d,p);
			c.drawPoint(dpInt(61)/d,dpInt(45)/d,p);
			return b;
		}

		public static final int SEEK_BACK = 1, SEEK_NEXT = 2, ENABLE_INPUT = 3,
		SELECT_INPUT = 4, KEYBOARD_SETTINGS = 5, WIZARD_DONE = 6, textColor = 0xFFDEDEDE;
	}
	
	public class Dots {
		
		private LinearLayout dotView;
		private int dot;
		
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
		
		public int setSelection(int index){
			View tempView;
			for(int i = 0;i < dot;i++){
				tempView = dotView.getChildAt(i);
				tempClr = (SetupResources.textColor - (tempView.getId() == index ? 0 : 0xAA000000));
				tempView.setBackgroundDrawable(getBg(tempClr,100));
			}
			return 0;
		}
	}
	
	private static GradientDrawable getBg(int color, float radius){
		GradientDrawable gd = new GradientDrawable();
		gd.setColor(color);
		gd.setCornerRadius(radius);
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
