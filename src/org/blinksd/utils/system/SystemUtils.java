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
		if(!isGesturesEnabled()) return 0;
		try {
			if(SDK_INT >= 29) {
				if(SDK_INT > 30) {
					/*
					Point appUsableScreenSize = new Point();
					Point realScreenSize = new Point();
					Display defaultDisplay = ((WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
					defaultDisplay.getSize(appUsableScreenSize);
					defaultDisplay.getRealSize(realScreenSize);
					return realScreenSize.y - appUsableScreenSize.y;
					*/
					
					WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
					return wm.getCurrentWindowMetrics()
						.getWindowInsets()
						.getInsets(WindowInsets.Type.navigationBars())
						.bottom;
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
			int gestureHeight = findGestureHeight(ctx);
			if(gestureHeight > 0) return gestureHeight;
			Resources res = ctx.getResources();
			int resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android");
			return (int) (resourceId > 0 ? res.getDimensionPixelSize(resourceId) : 0);
		}
		return 0;
	}
	
	public static int statusBarH(Context ctx){
		if(isColorized(ctx)){
			int resourceId = ctx.getResources().getIdentifier("status_bar_height", "dimen", "android");
			return (int) (resourceId > 0 ? (SDK_INT > 30 ? 1 : 1) * ctx.getResources().getDimensionPixelSize(resourceId) : 0);
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
