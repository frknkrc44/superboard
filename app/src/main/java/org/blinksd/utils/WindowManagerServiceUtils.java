package org.blinksd.utils;

import static android.os.Build.VERSION.SDK_INT;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;

import java.lang.reflect.Method;

// https://stackoverflow.com/a/44170359/16290110
@SuppressLint("PrivateApi")
public final class WindowManagerServiceUtils {
    private WindowManagerServiceUtils() {}

    public static int getDisplayId(Context context) {
        try {
            Method getDisplay = Context.class.getMethod("getDisplay");
            Display display = (Display) getDisplay.invoke(context);
            return display.getDisplayId();
        } catch (Throwable ignored) {
            return Display.DEFAULT_DISPLAY;
        }
    }

    public static boolean hasNavigationBar(Context context) {
        Object windowManagerService = getService();
        if (windowManagerService == null) {
            return false;
        }

        try {
            Class<?> wmClass = windowManagerService.getClass();

            if (SDK_INT < 29) {
                Method hasNavigationBar = wmClass.getMethod("hasNavigationBar");
                return (boolean) hasNavigationBar.invoke(windowManagerService);
            }

            Method hasNavigationBar = wmClass.getMethod("hasNavigationBar", int.class);
            return (boolean) hasNavigationBar.invoke(windowManagerService, getDisplayId(context));
        } catch (Throwable e) {
            Log.e(WindowManagerServiceUtils.class.getSimpleName(), "[NAVBAR]", e);
        }

        return false;
    }

    private static Object getService() {
        try {
            Class<?> serviceManager = Class.forName("android.os.ServiceManager");
            IBinder serviceBinder = (IBinder) serviceManager.getMethod("getService", String.class).invoke(serviceManager, "window");
            Class<?> stub = Class.forName("android.view.IWindowManager$Stub");
            return stub.getMethod("asInterface", IBinder.class).invoke(stub, serviceBinder);
        } catch (Throwable e) {
            Log.e(WindowManagerServiceUtils.class.getSimpleName(), "[SERVICE]", e);
        }

        return null;
    }
}
