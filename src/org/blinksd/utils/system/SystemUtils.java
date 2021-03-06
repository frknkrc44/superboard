package org.blinksd.utils.system;

import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.os.*;
import android.util.*;
import android.view.*;
import org.blinksd.board.*;
import org.blinksd.utils.color.*;
import org.superdroid.db.*;
import java.lang.reflect.Method;

import static android.os.Build.VERSION.SDK_INT;

public class SystemUtils {
	
	public static final boolean isNotColorizeNavbar(){
		return getSystemProp("ro.build.version.emui").length() > 1 || SDK_INT > 29;
	}
	
	public static final String getSystemProp(String key){
		try {
			Class<?> propClass = Class.forName("android.os.SystemProperties");
			Method getMethod = propClass.getMethod("get", String.class);
			return getMethod.invoke(null, key).toString();
		} catch(Throwable t){}
		return "";
	}

	public static boolean detectNavbar(InputService inputService){
		if(SDK_INT >= 14){
			try {
				Class<?> serviceManager = Class.forName("android.os.ServiceManager");
				IBinder serviceBinder = (IBinder)serviceManager.getMethod("getService", String.class).invoke(serviceManager, "window");
				Class<?> stub = Class.forName("android.view.IWindowManager$Stub");
				Object windowManagerService = stub.getMethod("asInterface", IBinder.class).invoke(stub, serviceBinder);
				Method hasNavigationBar = null;
				if(SDK_INT < 29){
					hasNavigationBar = windowManagerService.getClass().getMethod("hasNavigationBar");
					return (boolean) hasNavigationBar.invoke(windowManagerService);
				}
				hasNavigationBar = windowManagerService.getClass().getMethod("hasNavigationBar",int.class);
				WindowManager wm = inputService.getWindow().getWindow().getWindowManager();
				Display dsp = wm.getDefaultDisplay();
				return (boolean) hasNavigationBar.invoke(windowManagerService,dsp.getDisplayId());
			} catch(Exception e){
				Log.e("Navbar","Navbar detection failed by internal system APIs because ...",e);
			}
		}
		return (!(KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK) && 
			KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME)));
	}

	public static View createNavbarLayout(Context ctx, int gestureHeight, int color){
		View v = new View(ctx);
		v.setLayoutParams(new ViewGroup.LayoutParams(-1,isColorized(ctx) ? navbarH(ctx, gestureHeight) : -1));
		boolean isLight = ColorUtils.satisfiesTextContrast(Color.rgb(Color.red(color),Color.green(color),Color.blue(color)));
		if(isLight)
			color = AppSettingsV2.getDarkerColor(color);
		v.setBackgroundColor(color);
		return v;
	}
	
	public static int navbarH(Context ctx, int gestureHeight){
		if(isColorized(ctx)){
			if(gestureHeight > 0) return gestureHeight;
			int resourceId = ctx.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
			return resourceId > 0 ? ctx.getResources().getDimensionPixelSize(resourceId) : 0;
		}
		return 0;
	}
	
	public static boolean isColorized(Context ctx){
		if(isNotColorizeNavbar() || !SuperDBHelper.getBooleanValueOrDefault(SettingMap.SET_COLORIZE_NAVBAR)){
			return false;
		}
		return !isLand(ctx) || isTablet(ctx);
	}
	
	private static boolean isTablet(Context ctx){
		return ctx.getResources().getConfiguration().smallestScreenWidthDp >= 600;
	}
	
	private static boolean isLand(Context ctx){
		return ctx.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
	}
	
}
