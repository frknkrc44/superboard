package org.blinksd.utils.color;

import android.graphics.*;

public class HSVColorUtils {
	
	private HSVColorUtils(){}
	
	public static int getColorFromHSVInt(float hue, int sat, int val){
		if(hue > 360){
			hue = 360;
		} else if(hue < 0){
			hue = 0;
		}
		return getColorFromHSV(hue, 1f - (sat / 100f), val / 100f);
	}
	
	public static int getColorFromHSV(float hue, float sat, float val){
		return Color.HSVToColor(new float[]{hue,sat,val});
	}
	
}
