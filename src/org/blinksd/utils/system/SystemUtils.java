package org.blinksd.utils.system;

import java.lang.reflect.Method;

public class SystemUtils {
	
	public static final boolean isNotColorizeNavbar(){
		return getSystemProp("ro.build.version.emui").length() > 1;
	}
	
	private static final String getSystemProp(String key){
		try {
			Class<?> propClass = Class.forName("android.os.SystemProperties");
			Method getMethod = propClass.getMethod("get", String.class);
			return getMethod.invoke(null, key).toString();
		} catch(Throwable t){}
		return "";
	}
	
}
