package org.blinksd.utils.layout;

import android.app.*;
import android.graphics.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import android.widget.TabHost.*;
import org.blinksd.*;
import org.blinksd.board.*;
import org.blinksd.utils.color.*;
import org.superdroid.db.*;

public class ColorSelectorLayout {
	
	private ColorSelectorLayout(){}
	
	private static SuperDB db;
	private static TabWidget widget;
	private static TextView prev;
	private static CustomSeekBar a,r,g,b,h,s,v;
	
	static {
		db = SuperBoardApplication.getApplicationDatabase();
	}
	
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
			stra[i] = ctx.getTranslation(stra[i]);
		}

		host.setOnTabChangedListener(new TabHost.OnTabChangeListener(){

				@Override
				public void onTabChanged(String p1){
					a.setVisibility(p1.equals(stra[2]) ? View.GONE : View.VISIBLE);
					int tag = widget.getTag();
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
		int val = db.getInteger(key,0);
		
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
	
	private static View getHexSelector(AppSettingsV2 ctx){
		return LayoutCreator.createVerticalLayout(ctx);
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
		x.setText(getColorString(color,true));
		x.setTextColor(ColorUtils.satisfiesTextContrast(color) ? 0xFF212121 : 0XFFDEDEDE);
		x.setBackgroundColor(color);
	}
	
	private static String getColorString(int color, boolean l){
		return getColorString(Color.alpha(color),Color.red(color),Color.green(color),Color.blue(color),l);
	}

	private static String getColorString(int a, int r, int g, int b, boolean l){
		return ("#"+z(a)+z(r)+z(g)+z(b)+(l?"\n("+a+", "+r+", "+g+", "+b+")":"")).toUpperCase();
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
