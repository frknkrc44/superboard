package yandroid.graphics;
/**
 * A little hack to get required hidden
 * methods for Switch and CompoundButton
 *
 * @author frknkrc44
 */

import android.graphics.drawable.Drawable;
import java.lang.reflect.Method;

public class DrawableUtils {
	
	/**
	 * Access to getOpticalInsets method of Drawable
	 * and adapt to custom Insets class
	 *
	 * If any error occurred, returns NONE
	 *
	 * @author frknkrc44
	 */
	public static Insets getOpticalInsets(Drawable drw){
		try {
			Method m = Drawable.class.getDeclaredMethod("getOpticalInsets");
			m.setAccessible(true);
			Object o = m.invoke(drw);
			return new Insets(o);
		} catch(Throwable t){}
		return Insets.NONE;
	}
	
}
