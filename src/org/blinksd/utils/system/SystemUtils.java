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
import android.provider.*;
import org.blinksd.utils.layout.*;
import org.blinksd.*;

public class SystemUtils {
	
	public static final boolean isNotColorizeNavbar(){
		return getSystemProp("ro.build.version.emui").length() > 1;
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
				if(SDK_INT < 29){
					Method hasNavigationBar = windowManagerService.getClass().getMethod("hasNavigationBar");
					return (boolean) hasNavigationBar.invoke(windowManagerService);
				}
				Method hasNavigationBar = windowManagerService.getClass().getMethod("hasNavigationBar",int.class);
				Display dsp = inputService.getWindow().getWindow().getWindowManager().getDefaultDisplay();
				return (boolean) hasNavigationBar.invoke(windowManagerService,dsp.getDisplayId());
			} catch(Exception e){
				Log.e("Navbar","Navbar detection failed by internal system APIs because ...",e);
			}
		}
		return (!(KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK) && 
			KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME)));
	}

	public static View createNavbarLayout(Context ctx, int color){
		View v = new View(ctx);
		v.setLayoutParams(new ViewGroup.LayoutParams(-1,isColorized(ctx) ? navbarH(ctx) : -1));
		boolean isLight = ColorUtils.satisfiesTextContrast(Color.rgb(Color.red(color),Color.green(color),Color.blue(color)));
		if(isLight)
			color = AppSettingsV2.getDarkerColor(color);
		v.setBackgroundColor(color);
		return v;
	}
	
	public static int findGestureHeight(Context ctx) {
		try {
			if(SDK_INT >= 29) {
				if(SDK_INT > 30) {
					WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
					boolean gesturesEnabled = isGesturesEnabled();
					return (int) (wm.getCurrentWindowMetrics()
						.getWindowInsets()
						.getInsets(gesturesEnabled ? WindowInsets.Type.systemGestures() : WindowInsets.Type.navigationBars())
						.bottom * (gesturesEnabled ? 1.5 : 1));
				}
				
				// For SDK 30 or below, use old method
				// Because new method reports wrong size
				return DensityUtils.dpInt(48);
			}
		} catch(Throwable t){}
		return 0;
	}
	
	public static boolean isGesturesEnabled(){
		try {
			return Settings.Secure.getInt(SuperBoardApplication.getApplication().getContentResolver(),"navigation_mode") == 2;
		} catch(Throwable t){
			return false;
		}
	}
	
	public static int navbarH(Context ctx){
		if(isColorized(ctx)){
			if(!isGesturesEnabled() && isLand(ctx) && !isTablet(ctx)) return 0;
			int gestureHeight = findGestureHeight(ctx);
			if(gestureHeight > 0) return gestureHeight;
			Resources res = ctx.getResources();
			int resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android");
			return (int) (resourceId > 0 ? res.getDimensionPixelSize(resourceId) : 0);
		}
		return 0;
	}
	
	public static boolean isColorized(Context ctx){
		return !(isNotColorizeNavbar() || !SuperDBHelper.getBooleanValueOrDefault(SettingMap.SET_COLORIZE_NAVBAR));
	}

	private static boolean isTablet(Context ctx){
		return ctx.getResources().getConfiguration().smallestScreenWidthDp >= 600;
	}
	
	private static boolean isLand(Context ctx){
		return ctx.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
	}
	
}
