package org.blinksd.utils.image;

import android.graphics.*;

public class ImageUtils {
	private ImageUtils(){}
	
	public static final int getShortDimensionOfPicture(Bitmap b){
		int x = b.getWidth(),y = b.getHeight();
		return x > y ? y : x;
	}
	
	public static final int getLongDimensionOfPicture(Bitmap b){
		int x = b.getWidth(),y = b.getHeight();
		return x > y ? x : y;
	}
	
	public static final Bitmap getScaledBitmap(Bitmap b,float scale){
		Bitmap x = b.copy(Bitmap.Config.ARGB_8888,true);
		int a = x.getWidth(), c = x.getHeight();
		x = Bitmap.createScaledBitmap(x,(int)(a*scale),(int)(c*scale),true);
		return x;
	}
	
	public static final Bitmap get512pxBitmap(Bitmap b){
		int a = getLongDimensionOfPicture(b);
		float f = 512.0f/a;
		return getScaledBitmap(b,f);
	}
}
