package yandroid.graphics;
/**
 * A little hack to get required hidden
 * methods for Switch and CompoundButton
 *
 * @author frknkrc44
 */

import android.content.res.Resources;
import android.text.TextPaint;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class TextPaintUtils {
	
	/**
	 * Access to hidden setCompatibilityScaling method of TextPaint
	 * and invoke this with CompabilityInfo's applicationScale variable
	 *
	 * @author frknkrc44
	 */
	public static void setCompatibilityScaling(TextPaint utils, Resources res){
		try {
			Method m = TextPaint.class.getDeclaredMethod("setCompatibilityScaling", float.class);
			m.setAccessible(true);
			Object o = ResourcesUtils.getCompatibilityInfo(res);
			assert o != null;
			Field x = o.getClass().getDeclaredField("applicationScale");
			x.setAccessible(true);
			m.invoke(utils, x.getFloat(o));
		} catch(Throwable ignored){}
	}
	
}
