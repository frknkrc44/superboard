package org.blinksd.utils.layout;

import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.widget.*;
import java.lang.reflect.*;
import org.blinksd.board.*;

class CustomSeekBar extends SeekBar {
	
	private int mMinValue = 0;
	
	CustomSeekBar(Context c){
		super(c);
		setLayoutParams(new LinearLayout.LayoutParams(SuperBoard.mp(50),-2,0));
		int p = SuperBoard.dp(4);
		setPadding(p*4,p,p*4,p);
		if(Build.VERSION.SDK_INT >= 21)
			setSplitTrack(false);
		drawSeekBar();
	}

	void drawSeekBar(){
		Bitmap b = Bitmap.createBitmap(SuperBoard.dp(36),SuperBoard.dp(36),Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(b);
		Paint p = new Paint();
		p.setStyle(Paint.Style.FILL);
		p.setColor(0xFFDEDEDE);
		RectF r = new RectF(0,0,b.getWidth(),b.getHeight());
		c.drawOval(r,p);
		setThumb(new BitmapDrawable(b));
		Drawable ld = getResources().getDrawable(R.drawable.pbar);
		ld.setColorFilter(p.getColor(),PorterDuff.Mode.SRC_ATOP);
		setProgressDrawable(ld);
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
