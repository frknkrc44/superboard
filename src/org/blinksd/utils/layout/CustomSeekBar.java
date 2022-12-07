package org.blinksd.utils.layout;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.widget.AbsSeekBar;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import org.blinksd.board.R;
import org.blinksd.board.SuperBoard;

import java.lang.reflect.Field;

class CustomSeekBar extends SeekBar {
	
	private int mMinValue = 0;
	
	CustomSeekBar(Context c){
		super(c);
		setLayoutParams(new LinearLayout.LayoutParams(DensityUtils.mpInt(50),-2,0));
		int p = SuperBoard.dp(4);
		setPadding(p*4,p,p*4,p);
		if(Build.VERSION.SDK_INT >= 21)
			setSplitTrack(false);
		drawSeekBar();
	}

	void drawSeekBar(){
		if(Build.VERSION.SDK_INT < 21){
			Bitmap b = Bitmap.createBitmap(SuperBoard.dp(36),SuperBoard.dp(36),Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(b);
			Paint p = new Paint();
			int color = 0xFFDEDEDE;
			p.setStyle(Paint.Style.FILL);
			p.setColor(color);
			RectF r = new RectF(0,0,b.getWidth(),b.getHeight());
			c.drawOval(r,p);
			setThumb(new BitmapDrawable(b));
			Drawable ld = getResources().getDrawable(R.drawable.pbar);
			ld.setColorFilter(p.getColor(),PorterDuff.Mode.SRC_ATOP);
			setProgressDrawable(ld);
		} else {
			int color = Build.VERSION.SDK_INT >= 31 
				? getResources().getColor(android.R.color.system_accent1_200)
				: 0xFFDEDEDE;
			setThumbTintList(ColorStateList.valueOf(color));
			setBackgroundTintList(ColorStateList.valueOf(color));
			setProgressTintList(ColorStateList.valueOf(color));
		}
	}

	public Drawable getThumb() {
		if(Build.VERSION.SDK_INT >= 16)
			return super.getThumb();
		
		try {
			Field thumb = AbsSeekBar.class.getDeclaredField("mThumb");
			thumb.setAccessible(true);
			return (Drawable) thumb.get(this);
		} catch(Throwable t) {}

		return null;
	}
	
}
