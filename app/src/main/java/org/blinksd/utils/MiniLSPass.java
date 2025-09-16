package org.blinksd.utils;

import android.util.Log;
import android.util.Property;
import android.view.Window;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A minimal port of LSPass, because I don't want to include the complete HiddenApiBypass library and its dependencies.<br>
 * Big thanks to the <a href="https://github.com/LSPosed">LSPosed</a> Team.<br><br>
 *
 * Source: <a href="https://github.com/LSPosed/AndroidHiddenApiBypass/blob/01ab3d8d2ef5496a78ff0db15f73cc30e6f5861e/library/src/main/java/org/lsposed/hiddenapibypass/LSPass.java">LSPass</a>
 */
@SuppressWarnings("rawtypes")
public final class MiniLSPass {
    private static final String TAG = "MiniLSPass";
    private static final Property<Class, Method[]> methods = Property.of(Class.class, Method[].class, "DeclaredMethods");

    /**
     * invoke a restrict method named {@code methodName} of the given class {@code clazz} with this object {@code thiz} and arguments {@code args}
     *
     * @param clazz      the class call the method on (this parameter is required because this method cannot call inherit method)
     * @param thisObj    this object, which can be {@code null} if the target method is static
     * @param methodName the method name
     * @param args       arguments to call the method with name {@code methodName}
     * @return the return value of the method
     * @see Method#invoke(Object, Object...)
     */
    private static Object invoke(Class<?> clazz, Object thisObj, String methodName, Object... args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        for (var method : methods.get(clazz)) {
            if (!method.getName().equals(methodName)) continue;
            method.setAccessible(true);
            return method.invoke(thisObj, args);
        }
        throw new NoSuchMethodException("Cannot find matching method");
    }

    /**
     * disable Google's Edge2Edge enforcement.
     *
     * @param window The current activity/service window
     */
    public static void disableEdgeToEdge(Window window) {
        try {
            Field field = window.getClass().getDeclaredField("mEdgeToEdgeEnforced");
            field.setAccessible(true);
            field.set(window, false);
        } catch (Throwable e) {
            Log.w(TAG, "setField", e);
        }
    }

    /**
     * allow an app to execute the hidden API methods without restrictions.
     */
    public static void allowHiddenApi() {
        try {
            var runtimeClazz = Class.forName("dalvik.system.VMRuntime");
            var runtime = invoke(runtimeClazz, null, "getRuntime");
            invoke(runtimeClazz, runtime, "setHiddenApiExemptions", (Object) new String[] {"L"});
        } catch (Throwable e) {
            Log.w(TAG, "allowHiddenApi", e);
        }
    }
}