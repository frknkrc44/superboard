package org.blinksd.utils.layout;

import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.widget.*;

public class CustomRadioButton extends RadioButton {
	
	CustomRadioButton(Context c){
		super(c);
		int i = DensityUtils.dpInt(8);
		setPadding(i,0,i,0);
		setRadioButton();
	}
	
	void setRadioButton(){
		StateListDrawable sld = new StateListDrawable();
		int i = 64;
		int g = DensityUtils.dpInt(i);
		Bitmap b = Bitmap.createBitmap(g,g,Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(b);
		Paint p = new Paint();
		i = g = DensityUtils.dpInt(4);
		p.setStyle(Paint.Style.STROKE);
		p.setStrokeWidth(i);
		i *= 3;
		p.setColor(0xFFDEDEDE);
		c.drawRoundRect(i,i,b.getWidth()-i,b.getHeight()-i,g,g,p);
		BitmapDrawable bdn = new BitmapDrawable(b);
		b = Bitmap.createBitmap(b);
		c = new Canvas(b);
		p.setStyle(Paint.Style.FILL);
		p.setStrokeWidth(0);
		i *= 2;
		c.drawRoundRect(i,i,b.getWidth()-i,b.getHeight()-i,g,g,p);
		BitmapDrawable bdc = new BitmapDrawable(b);
		sld.addState(new int[]{android.R.attr.state_checked},bdc);
		sld.addState(new int[]{},bdn);
		setButtonDrawable(sld);
	}
	
}
