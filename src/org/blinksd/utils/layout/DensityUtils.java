package org.blinksd.utils.layout;

import android.content.res.Resources;
import android.util.DisplayMetrics;

public class DensityUtils {
	
	public static final float dp(float px){
		return getDisplayMetrics().density * px;
	}
	
	public static final int dpInt(float px){
		return (int) dp(px);
	}
	
	public static final float wp(float px){
		return (getScreenWidth() / 100f) * px;
	}

	public static final int wpInt(float px){
		return (int) wp(px);
	}
	
	public static final float hp(float px){
		return (getScreenHeight() / 100f) * px;
	}

	public static final int hpInt(float px){
		return (int) hp(px);
	}

	public static float mp(float percent){
		float width = wp(percent), height = hp(percent);
		return width < height ? width : height;
	}
	
	public static int mpInt(float percent){
		return (int) mp(percent);
	}
	
	public static DisplayMetrics getDisplayMetrics(){
		return Resources.getSystem().getDisplayMetrics();
	}
	
	public static int getScreenWidth(){
		return getDisplayMetrics().widthPixels;
	}

	public static int getScreenHeight(){
		return getDisplayMetrics().heightPixels;
	}
	
}
