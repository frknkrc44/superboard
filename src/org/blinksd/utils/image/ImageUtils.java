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
			if(a > 512){
				float f = 512.0f/a;
				return getScaledBitmap(b,f);
			}
			return b;
		}
		return null;
	}
	
	public static Bitmap fastblur(Bitmap sentBitmap, float scale, int radius){
		if(sentBitmap == null) return null;
		try{
			int width = Math.round(sentBitmap.getWidth() * scale);
			int height = Math.round(sentBitmap.getHeight() * scale);
			if(scale != 1) sentBitmap = Bitmap.createScaledBitmap(sentBitmap, width, height, false);
			Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
			if (radius < 1) return null;
			int w = bitmap.getWidth(), h = bitmap.getHeight(), pix[] = new int[w * h];
			bitmap.getPixels(pix, 0, w, 0, 0, w, h); 
			int wm = w - 1, hm = h - 1, wh = w * h, div = (radius * 2) + 1, 
				r[] = new int[wh], g[] = new int[wh], b[] = new int[wh],
				rsum, gsum, bsum, x, y, i, p, yp, yi, yw, 
				vmin[] = new int[Math.max(w, h)], divsum = (div + 1) >> 1;
			divsum *= divsum;
			int dv[] = new int[256 * divsum];
			for (i = 0; i < 256 * divsum; i++) dv[i] = (i / divsum);
			yw = yi = 0;
			int stack[][] = new int[div][3],stackpointer,stackstart,
				rbs, sir[], r1 = radius + 1, routsum, goutsum, boutsum,
				rinsum, ginsum, binsum;
			for (y = 0; y < h; y++){
				rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
				for (i = -radius; i <= radius; i++){
					p = pix[yi + Math.min(wm, Math.max(i, 0))];
					sir = stack[i + radius];
					sir[0] = (p & 0xff0000) >> 16;
					sir[1] = (p & 0x00ff00) >> 8;
					sir[2] = (p & 0x0000ff);
					rbs = r1 - Math.abs(i);
					rsum += sir[0] * rbs;
					gsum += sir[1] * rbs;
					bsum += sir[2] * rbs;
					if (i > 0){
						rinsum += sir[0];
						ginsum += sir[1];
						binsum += sir[2];
					} else {
						routsum += sir[0];
						goutsum += sir[1];
						boutsum += sir[2];
					}
				} stackpointer = radius;
				for (x = 0; x < w; x++){
					r[yi] = dv[rsum]; 
					g[yi] = dv[gsum];
					b[yi] = dv[bsum];
					rsum -= routsum;
					gsum -= goutsum;
					bsum -= boutsum;
					stackstart = stackpointer - radius + div;
					sir = stack[stackstart % div];
					routsum -= sir[0];
					goutsum -= sir[1];
					boutsum -= sir[2];
					if (y == 0) vmin[x] = Math.min(x + radius + 1, wm);
					p = pix[yw + vmin[x]];
					sir[0] = (p & 0xff0000) >> 16;
					sir[1] = (p & 0x00ff00) >> 8;
					sir[2] = (p & 0x0000ff);
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
					rsum += rinsum;
					gsum += ginsum;
					bsum += binsum;
					stackpointer = (stackpointer + 1) % div;
					sir = stack[stackpointer % div];
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
					rinsum -= sir[0];
					ginsum -= sir[1];
					binsum -= sir[2];
					yi++;
				} yw += w;
			}

			for (x = 0; x < w; x++) {
				rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
				yp = -radius * w;
				for (i = -radius; i <= radius; i++){
					yi = Math.max(0, yp) + x;
					sir = stack[i + radius];
					sir[0] = r[yi];
					sir[1] = g[yi];
					sir[2] = b[yi];
					rbs = r1 - Math.abs(i);
					rsum += r[yi] * rbs;
					gsum += g[yi] * rbs;
					bsum += b[yi] * rbs;
					if (i > 0){
						rinsum += sir[0];
						ginsum += sir[1];
						binsum += sir[2];
					} else {
						routsum += sir[0];
						goutsum += sir[1];
						boutsum += sir[2];
					}
					if (i < hm) yp += w;
				} yi = x;
				stackpointer = radius;
				for (y = 0; y < h; y++){
					pix[yi] = ( 0xff000000 & pix[yi] ) | ( dv[rsum] << 16 ) | ( dv[gsum] << 8 ) | dv[bsum];
					rsum -= routsum;
					gsum -= goutsum;
					bsum -= boutsum;
					stackstart = stackpointer - radius + div;
					sir = stack[stackstart % div];
					routsum -= sir[0];
					goutsum -= sir[1];
					boutsum -= sir[2];
					if (x == 0) vmin[y] = Math.min(y + r1, hm) * w;
					p = x + vmin[y];
					sir[0] = r[p];
					sir[1] = g[p];
					sir[2] = b[p];
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
					rsum += rinsum;
					gsum += ginsum;
					bsum += binsum;
					stackpointer = (stackpointer + 1) % div;
					sir = stack[stackpointer];
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
					rinsum -= sir[0];
					ginsum -= sir[1];
					binsum -= sir[2];
					yi += w;
				}
			} bitmap.setPixels(pix, 0, w, 0, 0, w, h);
			return bitmap;
		} catch(Exception e){
			return sentBitmap;
		}
	}
}
