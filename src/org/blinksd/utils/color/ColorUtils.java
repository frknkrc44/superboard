package org.blinksd.utils.color;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import org.blinksd.Defaults;
import org.blinksd.SuperBoardApplication;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

// Copied from support library
// Android Open Source Project

public class ColorUtils {
	private static final double XYZ_WHITE_REFERENCE_X = 95.047;
	private static final double XYZ_WHITE_REFERENCE_Y = 100;
	private static final double XYZ_WHITE_REFERENCE_Z = 108.883;
	private static final double XYZ_EPSILON = 0.008856;
	private static final double XYZ_KAPPA = 903.3;

	private static final ThreadLocal<double[]> TEMP_ARRAY = new ThreadLocal<>();

	private ColorUtils() {}

	public static int compositeColors(@ColorInt int foreground, @ColorInt int background) {
		int bgAlpha = Color.alpha(background);
		int fgAlpha = Color.alpha(foreground);
		int a = compositeAlpha(fgAlpha, bgAlpha);
		int r = compositeComponent(Color.red(foreground), fgAlpha,
								   Color.red(background), bgAlpha, a);
		int g = compositeComponent(Color.green(foreground), fgAlpha,
								   Color.green(background), bgAlpha, a);
		int b = compositeComponent(Color.blue(foreground), fgAlpha,
								   Color.blue(background), bgAlpha, a);
		return Color.argb(a, r, g, b);
	}

	private static int compositeAlpha(int foregroundAlpha, int backgroundAlpha) {
		return 0xFF - (((0xFF - backgroundAlpha) * (0xFF - foregroundAlpha)) / 0xFF);
	}

	private static int compositeComponent(int fgC, int fgA, int bgC, int bgA, int a) {
		if (a == 0) return 0;
		return ((0xFF * fgC * fgA) + (bgC * bgA * (0xFF - fgA))) / (a * 0xFF);
	}

	@FloatRange(from = 0.0, to = 1.0)
	public static double calculateLuminance(@ColorInt int color) {
		if(Build.VERSION.SDK_INT >= 24)
			return Color.luminance(color);
		
		final double[] result = getTempDouble3Array();
		colorToXYZ(color, result);
		// Luminance is the Y component
		return result[1] / 100;
	}

	public static double[] getTempDouble3Array() {
		double[] result = TEMP_ARRAY.get();
		if (result == null) {
			result = new double[3];
			TEMP_ARRAY.set(result);
		}
		return result;
	}

	public static double calculateContrast(@ColorInt int foreground, @ColorInt int background) {
		if (Color.alpha(background) != 255) {
			Log.wtf("ColorUtils", "background can not be translucent: #"
					+ Integer.toHexString(background));
		}
		if (Color.alpha(foreground) < 255) {
			// If the foreground is translucent, composite the foreground over the background
			foreground = compositeColors(foreground, background);
		}

		final double luminance1 = calculateLuminance(foreground) + 0.05;
		final double luminance2 = calculateLuminance(background) + 0.05;

		// Now return the lighter luminance divided by the darker luminance
		return Math.max(luminance1, luminance2) / Math.min(luminance1, luminance2);
	}

	public static void colorToLAB(@ColorInt int color, @NonNull double[] outLab) {
		RGBToLAB(Color.red(color), Color.green(color), Color.blue(color), outLab);
	}

	public static void RGBToLAB(@IntRange(from = 0x0, to = 0xFF) int r,
								@IntRange(from = 0x0, to = 0xFF) int g, @IntRange(from = 0x0, to = 0xFF) int b,
								@NonNull double[] outLab) {
		// First we convert RGB to XYZ
		RGBToXYZ(r, g, b, outLab);
		// outLab now contains XYZ
		XYZToLAB(outLab[0], outLab[1], outLab[2], outLab);
		// outLab now contains LAB representation
	}

	public static void colorToXYZ(@ColorInt int color, @NonNull double[] outXyz) {
		RGBToXYZ(Color.red(color), Color.green(color), Color.blue(color), outXyz);
	}

	public static void RGBToXYZ(@IntRange(from = 0x0, to = 0xFF) int r,
								@IntRange(from = 0x0, to = 0xFF) int g,
								@IntRange(from = 0x0, to = 0xFF) int b,
								@NonNull double[] outXyz) {
		if (outXyz.length != 3) {
			throw new IllegalArgumentException("outXyz must have a length of 3.");
		}

		double sr = r / 255.0;
		sr = sr < 0.04045 ? sr / 12.92 : Math.pow((sr + 0.055) / 1.055, 2.4);
		double sg = g / 255.0;
		sg = sg < 0.04045 ? sg / 12.92 : Math.pow((sg + 0.055) / 1.055, 2.4);
		double sb = b / 255.0;
		sb = sb < 0.04045 ? sb / 12.92 : Math.pow((sb + 0.055) / 1.055, 2.4);

		outXyz[0] = 100 * (sr * 0.4124 + sg * 0.3576 + sb * 0.1805);
		outXyz[1] = 100 * (sr * 0.2126 + sg * 0.7152 + sb * 0.0722);
		outXyz[2] = 100 * (sr * 0.0193 + sg * 0.1192 + sb * 0.9505);
	}

	public static void XYZToLAB(@FloatRange(from = 0f, to = XYZ_WHITE_REFERENCE_X) double x,
								@FloatRange(from = 0f, to = XYZ_WHITE_REFERENCE_Y) double y,
								@FloatRange(from = 0f, to = XYZ_WHITE_REFERENCE_Z) double z,
								@NonNull double[] outLab) {
		if (outLab.length != 3) {
			throw new IllegalArgumentException("outLab must have a length of 3.");
		}
		x = pivotXyzComponent(x / XYZ_WHITE_REFERENCE_X);
		y = pivotXyzComponent(y / XYZ_WHITE_REFERENCE_Y);
		z = pivotXyzComponent(z / XYZ_WHITE_REFERENCE_Z);
		outLab[0] = Math.max(0, 116 * y - 16);
		outLab[1] = 500 * (x - y);
		outLab[2] = 200 * (y - z);
	}

	private static double pivotXyzComponent(double component) {
		return component > XYZ_EPSILON
			? Math.pow(component, 1 / 3.0)
			: (XYZ_KAPPA * component + 16) / 116;
	}
	
	public static boolean satisfiesTextContrast(int color){
		return satisfiesTextContrast(color,0xFF000000);
	}
	
	public static boolean satisfiesTextContrast(int backgroundColor, int foregroundColor) {
		if(Color.alpha(backgroundColor) > 0x88)
        	return calculateContrast(foregroundColor, backgroundColor) >= 10;
		return false;
    }
	
	public static int getBitmapColor(@NonNull Bitmap bitmap){
		if (bitmap == null) return 0xFF000000;
		bitmap = bitmap.createScaledBitmap(bitmap,64,64,false);
		int width = bitmap.getWidth(),height = bitmap.getHeight();
		int pixels[] = new int[width * height];
		bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
		int color,count = 0,r = 0,g = 0,b = 0,a = 0;
		for(int i = 0;i < pixels.length;i++){
			color = pixels[i];
			a = Color.alpha(color);
			if(a > 0){
				color = (a < 255) ? convertARGBtoRGB(color) : color;
				r += Color.red(color);
				g += Color.green(color);
				b += Color.blue(color);
				count++;
			}
		}
		if(r == g && g == b && r == 0){
			count = 1;
		}
		r /= count;
		g /= count;
		b /= count;
		r = (r << 16) & 0x00FF0000;
		g = (g << 8) & 0x0000FF00;
		b = b & 0x000000FF;
		color = 0xFF000000 | r | g | b;
		return color;
	}
	
	public static int convertARGBtoRGB(int color){
		return Color.rgb(Color.red(color),Color.green(color),Color.blue(color));
	}
	
	public static int getDarkerColor(int color) {
		int[] state = {Color.red(color),Color.green(color),Color.blue(color)};
		for(int i = 0;i < state.length;i++){
			state[i] /= 1.2;
		}
		return Color.argb(Color.alpha(color),state[0],state[1],state[2]);
	}
    
    public static int getColorWithAlpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }
	
	public static int getAccentColor(){
		TypedArray arr = SuperBoardApplication.getApplication().obtainStyledAttributes(0, new int[]{ android.R.attr.colorAccent });
		int color = arr.getColor(0, Defaults.ENTER_BACKGROUND_COLOR);
		arr.recycle();
		return color;
	}
	
	@Retention(SOURCE)
	@Target({PARAMETER,METHOD,LOCAL_VARIABLE,FIELD})
	public @interface ColorInt {}
	
	@Retention(SOURCE)
	@Target({METHOD,PARAMETER,FIELD,LOCAL_VARIABLE})
	public @interface FloatRange {
		double from() default Double.MIN_VALUE;
		double to() default Double.MAX_VALUE;
		boolean fromInclusive() default true;
		boolean toInclusive() default true;
	}
	
	@Retention(SOURCE)
	@Target({METHOD,PARAMETER,FIELD})
	public @interface NonNull {}
	
	@Retention(SOURCE)
	@Target({METHOD,PARAMETER,FIELD,LOCAL_VARIABLE,ANNOTATION_TYPE})
	public @interface IntRange {
		long from() default Long.MIN_VALUE;
		long to() default Long.MAX_VALUE;
	}
}
