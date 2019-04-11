package org.blinksd.utils.image;

import android.graphics.*;

public class ImageUtils {
	private ImageUtils(){}
	
	public static final int getShortDimensionOfPicture(Bitmap b){
		if(b != null){
			int x = b.getWidth(),y = b.getHeight();
			return x > y ? y : x;
		}
		return 0;
	}
	
	public static final int getLongDimensionOfPicture(Bitmap b){
		if(b != null){
			int x = b.getWidth(),y = b.getHeight();
			return x > y ? x : y;
		}
		return 0;
	}
	
	public static final Bitmap getScaledBitmap(Bitmap b,float scale){
		if(b != null){
			Bitmap x = b.copy(Bitmap.Config.ARGB_8888,true);
			int a = x.getWidth(), c = x.getHeight();
			x = Bitmap.createScaledBitmap(x,(int)(a*scale),(int)(c*scale),true);
			return x;
		}
		return null;
	}
	
	public static final Bitmap get512pxBitmap(Bitmap b){
		if(b != null){
			int a = getLongDimensionOfPicture(b);
			float f = 512.0f/a;
			return getScaledBitmap(b,f);
		}
		return null;
	}
}
