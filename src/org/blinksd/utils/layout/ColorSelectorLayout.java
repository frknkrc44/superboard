package org.blinksd.utils.layout;

import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.media.*;
import android.text.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import android.widget.TabHost.*;
import org.blinksd.*;
import org.blinksd.board.*;
import org.blinksd.utils.color.*;
import org.blinksd.sdb.*;
import org.superdroid.db.*;

import static android.media.AudioManager.*;
import android.inputmethodservice.*;

public class ColorSelectorLayout {
	
	private ColorSelectorLayout(){}
	
	private static SuperMiniDB db = SuperBoardApplication.getApplicationDatabase();
	private static TabWidget widget;
	private static TextView prev;
	private static CustomSeekBar a,r,g,b,h,s,v;
	private static EditText hexIn;
	
	public static View getColorSelectorLayout(final AppSettingsV2 ctx, int val){
		LinearLayout main = LayoutCreator.createFilledVerticalLayout(FrameLayout.class,ctx);

		widget = new TabWidget(ctx);
		widget.setId(android.R.id.tabs);
		widget.setTag(val);

		final TabHost host = new TabHost(ctx);
		host.setLayoutParams(LayoutCreator.createLayoutParams(LinearLayout.class,-1,-2));
		FrameLayout fl = new FrameLayout(ctx);
		fl.setLayoutParams(LayoutCreator.createLayoutParams(LinearLayout.class,-1,-1));
		fl.setId(android.R.id.tabcontent);
		LinearLayout holder = LayoutCreator.createFilledVerticalLayout(LinearLayout.class,ctx);
		holder.setGravity(Gravity.CENTER);
		holder.addView(widget);
		prev = new TextView(ctx);
		prev.setLayoutParams(new LinearLayout.LayoutParams(-1,-1,1));
		prev.setGravity(Gravity.CENTER);
		int dp = DensityUtils.dpInt(16);
		prev.setPadding(0,dp,0,dp);
		holder.addView(prev);
		a = new CustomSeekBar(ctx);
		a.setMax(255);
		a.setProgress(Color.alpha(val));
		holder.addView(a);
		holder.addView(fl);
		host.addView(holder);
		main.addView(host);

		final String[] stra = {
			"color_selector_rgb",
			"color_selector_hsv",
			"color_selector_hex"
		};

		for(int i = 0;i < stra.length;i++){
			stra[i] = SettingsCategorizedListAdapter.getTranslation(ctx, stra[i]);
		}

		host.setOnTabChangedListener(new TabHost.OnTabChangeListener(){

				@Override
				public void onTabChanged(String p1){
					a.setVisibility(p1.equals(stra[2]) ? View.GONE : View.VISIBLE);
					int tag = (int) widget.getTag();
					switch(host.getCurrentTab()){
						case 0:
							r.setProgress(Color.red(tag));
							g.setProgress(Color.green(tag));
							b.setProgress(Color.blue(tag));
							break;
						case 1:
							int[] hsv = getHSV(tag);
							h.setProgress(hsv[0]);
							s.setProgress(hsv[1]);
							v.setProgress(hsv[2]);
							break;
						case 2:
							hexIn.setText(getColorString(tag,false,true));
							break;
					}
				}

			});

		host.setup();

		for(int i = 0;i < stra.length;i++){
			TabSpec ts = host.newTabSpec(stra[i]);
			TextView tv = (TextView) LayoutInflater.from(ctx).inflate(android.R.layout.simple_list_item_1,widget,false);
			LinearLayout.LayoutParams pr = (LinearLayout.LayoutParams) LayoutCreator.createLayoutParams(LinearLayout.class,-1,DensityUtils.dpInt(48));
			pr.weight = 0.33f;
			tv.setLayoutParams(pr);
			tv.setText(stra[i]);
			tv.setBackgroundResource(R.drawable.tab_indicator_material);
			tv.getBackground().setColorFilter(0xFFDEDEDE,PorterDuff.Mode.SRC_ATOP);
			tv.setGravity(Gravity.CENTER);
			tv.setPadding(0,0,0,0);
			tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP,16);
			ts.setIndicator(tv);
			final View v = getView(ctx,i);
			ts.setContent(new TabContentFactory(){
					@Override
					public View createTabContent(String p1){
						return v;
					}
				});
			host.addTab(ts);
		}
		return main;
	}
	
	public static View getColorSelectorLayout(final AppSettingsV2 ctx, String key){
		int val = db.getInteger(key, (int) SuperBoardApplication.getSettings().getDefaults(key));
		return getColorSelectorLayout(ctx, val);
	}
	
	private static View getView(AppSettingsV2 ctx, int i){
		switch(i){
			case 0: return getRGBSelector(ctx);
			case 1: return getHSVSelector(ctx);
			case 2: return getHexSelector(ctx);
		}
		return null;
	}
	
	private static View getRGBSelector(final AppSettingsV2 ctx){
		LinearLayout ll = new LinearLayout(ctx);
		ll.setLayoutParams(new LinearLayout.LayoutParams(-1,-1,1));
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setGravity(Gravity.CENTER);
		
		r = new CustomSeekBar(ctx);
		g = new CustomSeekBar(ctx);
		b = new CustomSeekBar(ctx);
		
		changeSeekBarColor(r,Color.rgb(0xDE,0,0));
		changeSeekBarColor(g,Color.rgb(0,0xDE,0));
		changeSeekBarColor(b,Color.rgb(0,0,0xDE));
		
		int set = (int) widget.getTag();
		q(prev,set);
		
		for(CustomSeekBar v : new CustomSeekBar[]{r,g,b}){
			v.setMax(255);
		}
		
		r.setProgress(Color.red(set));
		g.setProgress(Color.green(set));
		b.setProgress(Color.blue(set));
		
		SeekBar.OnSeekBarChangeListener opc = new SeekBar.OnSeekBarChangeListener(){
			@Override
			public void onProgressChanged(SeekBar s, int i, boolean c){
				int color = Color.argb(a.getProgress(),r.getProgress(),g.getProgress(),b.getProgress());
				widget.setTag(color);
				q(prev,color);
			}

			@Override
			public void onStartTrackingTouch(SeekBar s){}

			@Override
			public void onStopTrackingTouch(SeekBar s){}
		};
		
		for(CustomSeekBar v : new CustomSeekBar[]{r,g,b}){
			v.setOnSeekBarChangeListener(opc);
			ll.addView(v);
		}
		
		a.setOnSeekBarChangeListener(opc);
		return ll;
	}
	
	private static View getHSVSelector(AppSettingsV2 ctx){
		LinearLayout ll = new LinearLayout(ctx);
		ll.setLayoutParams(new LinearLayout.LayoutParams(-1,-1,1));
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setGravity(Gravity.CENTER);
		
		h = new CustomSeekBar(ctx);
		s = new CustomSeekBar(ctx);
		v = new CustomSeekBar(ctx);

		int set = (int) widget.getTag();
		q(prev,set);
		
		h.setMax(360);
		s.setMax(100);
		v.setMax(100);
		
		int[] hsv = getHSV(set);
	
		h.setProgress(hsv[0]);
		s.setProgress(hsv[1]);
		v.setProgress(hsv[2]);
		
		SeekBar.OnSeekBarChangeListener opc = new SeekBar.OnSeekBarChangeListener(){
			@Override
			public void onProgressChanged(SeekBar z, int i, boolean c){
				int color = HSVColorUtils.getColorFromHSVInt(h.getProgress(),s.getProgress(),v.getProgress());
				color = Color.argb(a.getProgress(),Color.red(color),Color.green(color),Color.blue(color));
				widget.setTag(color);
				q(prev,color);
			}

			@Override
			public void onStartTrackingTouch(SeekBar s){}

			@Override
			public void onStopTrackingTouch(SeekBar s){}
		};
		for(CustomSeekBar y : new CustomSeekBar[]{h,s,v}){
			y.setOnSeekBarChangeListener(opc);
			ll.addView(y);
		}
		return ll;
	}
	
	private static View getHexSelector(final AppSettingsV2 ctx){
		LinearLayout ll = LayoutCreator.createFilledVerticalLayout(FrameLayout.class,ctx);
		hexIn = new EditText(ctx);
		hexIn.setLayoutParams(new LinearLayout.LayoutParams(-1,-2));
		hexIn.setEnabled(false);
		hexIn.setGravity(Gravity.CENTER);
		hexIn.addTextChangedListener(new TextWatcher(){

				@Override
				public void beforeTextChanged(CharSequence p1, int start, int count, int after){}

				@Override
				public void onTextChanged(CharSequence p1, int start, int before, int count){}

				@Override
				public void afterTextChanged(Editable p1){
					try {
						int color = Color.parseColor("#"+p1.toString());
						widget.setTag(color);
						q(prev,color);
					} catch(Throwable t){}
					if(p1.length() > 8){
						hexIn.setText(getColorString((int)widget.getTag(),false,true));
					}
				}
			
		});
		
		SuperBoard sb = new SuperBoard(ctx){
			@Override
			public void sendDefaultKeyboardEvent(View v){
				if(v.getTag(TAG_NP) != null){
					String[] x = v.getTag(TAG_NP).toString().split(":");
					switch(Integer.parseInt(x[0])){
						case Keyboard.KEYCODE_DELETE:
							String s = hexIn.getText().toString();
							hexIn.setText(s.substring(0,s.length()-1));
							break;
						case Keyboard.KEYCODE_CANCEL:
							hexIn.setText("");
							break;
					}
					playSound(1);
				} else {
					hexIn.setText(hexIn.getText()+((SuperBoard.Key)v).getText().toString());
					playSound(0);
				}
			}

			@Override
			public void playSound(int event){
				if(!SuperDBHelper.getBooleanValueOrDefault(SettingMap.SET_PLAY_SND_PRESS)) return;
				AudioManager audMgr = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
				switch(event){
					case 1:
						audMgr.playSoundEffect(FX_KEYPRESS_DELETE);
						break;
					case 0:
						audMgr.playSoundEffect(FX_KEYPRESS_STANDARD);
						break;
				}
				
			}
		};
		sb.addRows(0, new String[][]{
			{"1","2","3","4","5",""},
			{"6","7","8","9","0",""},
			{"A","B","C","D","E","F"}
		});
		sb.setKeyboardHeight(20);
		sb.setKeysTextSize(20);
		sb.setKeysPadding(DensityUtils.dpInt(4));
		sb.setKeyDrawable(0,1,-1,R.drawable.sym_keyboard_delete);
		sb.setPressEventForKey(0,1,-1,Keyboard.KEYCODE_DELETE);
		sb.setKeyDrawable(0,0,-1,R.drawable.sym_keyboard_close);
		sb.setPressEventForKey(0,0,-1,Keyboard.KEYCODE_CANCEL);
		sb.setKeysBackground(LayoutUtils.getKeyBg(Defaults.KEY_BACKGROUND_COLOR,Defaults.KEY_PRESS_BACKGROUND_COLOR,true));
		ll.addView(hexIn);
		ll.addView(sb);
		return ll;
	}
	
	private static int[] getHSV(int color){
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		
		int[] out = new int[3];
		
		out[0] = (int) hsv[0];
		out[1] = (int) ((1f - hsv[1]) * 100);
		out[2] = (int) (hsv[2] * 100);

		return out;
	}
	
	private static void q(TextView x,int color){
		x.setText(getColorString(color,true,false));
		x.setTextColor(ColorUtils.satisfiesTextContrast(color) ? 0xFF212121 : 0XFFDEDEDE);
		x.setBackgroundColor(color);
	}
	
	private static String getColorString(int color, boolean l, boolean oc){
		return getColorString(Color.alpha(color),Color.red(color),Color.green(color),Color.blue(color),l,oc);
	}

	private static String getColorString(int a, int r, int g, int b, boolean l, boolean oc){
		return ((oc?"":"#")+z(a)+z(r)+z(g)+z(b)+(l?"\n("+a+", "+r+", "+g+", "+b+")":"")).toUpperCase();
	}
	
	private static String z(int x){
		if(x == 0) return "00";
		String s = Integer.toHexString(x);
		return x < 16 ? "0"+s : s;
	}
	
	private static void changeSeekBarColor(CustomSeekBar s, int c){
		s.getThumb().setColorFilter(c,PorterDuff.Mode.SRC_ATOP);
		s.getProgressDrawable().setColorFilter(c,PorterDuff.Mode.SRC_ATOP);
	}
	
}
