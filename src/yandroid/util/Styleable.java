package yandroid.util;
/**
 * A little hack to access hidden
 * styleable class to get XML values
 * starts with "android"
 *
 * @author frknkrc44
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Styleable {
	
	private Styleable(){}
	
	/**
	 * Get int array to get style indexes
	 *
	 * If requested name is unavailable at
	 * namespace or not int array, returns
	 * empty int array
	 *
	 * @author frknkrc44
	 */
	public static int[] getStyleable(String requestedName){
		Object o = getObject(requestedName);
		if(o != null && o instanceof int[]){
			return (int[]) o;
		}
		return new int[]{};
	}
	
	/**
	 * Get a key to get style value from
	 * attributes
	 *
	 * If requested name is unavailable at
	 * namespace, returns zero
	 *
	 * @author frknkrc44
	 */
	public static int getKey(String requestedName){
		Object o = getObject(requestedName);
		if(o != null){
			return (int) o;
		}
		return 0;
	}
	
	/**
	 * Get requested field from hidden class
	 *
	 * If any error occurred or requested
	 * field name is empty, returns null
	 *
	 * @author frknkrc44
	 */
	private static Object getObject(String requestedName){
		requestedName = requestedName.trim();
		tryToBypassRestrictions();
		try {
			Class<?> CLASS = Class.forName("android.R$styleable");
			if(requestedName.length() > 1){
				Field f = CLASS.getDeclaredField(requestedName);
				f.setAccessible(true);
				return f.get(null);
			}
		} catch(Throwable t){}
		return null;
	}
	
	/**
	 * Try to bypass reflection restrictions
	 * Thanks to XDA
	 *
	 * @author frknkrc44
	 */
	public static void tryToBypassRestrictions(){
        try {
            Method forName = Class.class.getDeclaredMethod("forName", String.class);
            Method getDeclaredMethod = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);
            Class<?> vmRuntimeClass = (Class<?>) forName.invoke(null, "dalvik.system.VMRuntime");
            Method getRuntime = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "getRuntime", null);
            Method setHiddenApiExemptions = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "setHiddenApiExemptions", new Class[]{String[].class});
            assert getRuntime != null;
            Object vmRuntime = getRuntime.invoke(null);
            assert setHiddenApiExemptions != null;
            setHiddenApiExemptions.invoke(vmRuntime, (Object) new String[]{"L"});
        } catch(Throwable t){}
	}
}
