package org.blinksd.utils.layout;

import android.content.res.Resources;

public class DensityUtils {
	
	public static final float dp(float px){
		return Resources.getSystem().getDisplayMetrics().density * px;
	}
	
	public static final int dpInt(float px){
		return (int) dp(px);
	}
	
	public static final float wp(float px){
		return (Resources.getSystem().getDisplayMetrics().widthPixels / 100f) * px;
	}

	public static final int wpInt(float px){
		return (int) wp(px);
	}
	
	public static final float hp(float px){
		return (Resources.getSystem().getDisplayMetrics().heightPixels / 100f) * px;
	}

	public static final int hpInt(float px){
		return (int) hp(px);
	}
	
}
