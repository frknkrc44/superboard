package org.blinksd.utils.layout;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import java.lang.reflect.Field;

public class CustomRadioButton extends RadioButton {
	
	public CustomRadioButton(Context c){
		super(c);
		int i = DensityUtils.dpInt(8);
		setPadding(i,0,i,0);
		if(Build.VERSION.SDK_INT < 21){
			Drawable drw = getButtonDrawable();
			if(drw != null){
				drw.setColorFilter(0xFFDEDEDE, PorterDuff.Mode.SRC_IN);
			}

			if(Build.VERSION.SDK_INT < 16){
				setPadding(i + drw.getIntrinsicWidth(),0,i,0);
			}
		} else {
			setPadding(i,0,i,0);
			int color = 0xFFDEDEDE;
			if(Build.VERSION.SDK_INT >= 31) {
				color = getResources().getColor(android.R.color.system_accent1_200);
			}
			setButtonTintList(ColorStateList.valueOf(color));
			setButtonTintMode(PorterDuff.Mode.SRC_IN);
			setBackgroundDrawable(null);
		}
		
		//setRadioButton();
	}

	public Drawable getButtonDrawable() {
		if(Build.VERSION.SDK_INT >= 23) {
			return super.getButtonDrawable();
		}

		try {
			Field field = CompoundButton.class.getDeclaredField("mButtonDrawable");
			field.setAccessible(true);
			return (Drawable) field.get(this);
		} catch(Throwable t) {
			return null;
		}
	}
	
	/*void setRadioButton(){
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
	}*/
	
}
