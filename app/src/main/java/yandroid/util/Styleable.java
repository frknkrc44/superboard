package yandroid.util;
/**
 * A little hack to access hidden
 * styleable class to get XML values
 * starts with "android"
 *
 * @author frknkrc44
 */

import android.annotation.SuppressLint;
import android.content.res.Resources;

import java.lang.reflect.Field;

public class Styleable {

    private Styleable() {
    }

    /**
     * Get int array to get style indexes
     * <p>
     * If requested name is unavailable at
     * namespace or not int array, returns
     * empty int array
     *
     * @author frknkrc44
     */
    public static int[] getStyleable(String requestedName) {
        Object o = getObject(requestedName);
        if (o instanceof int[]) {
            return (int[]) o;
        }
        return new int[]{};
    }

    /**
     * Get a key to get style value from
     * attributes
     * <p>
     * If requested name is unavailable at
     * namespace, returns zero
     *
     * @author frknkrc44
     */
    public static int getKey(String requestedName) {
        Object o = getObject(requestedName);
        if (o != null) {
            return (int) o;
        }
        return 0;
    }

    /**
     * Get requested field from hidden class
     * <p>
     * If any error occurred or requested
     * field name is empty, returns null
     *
     * @author frknkrc44
     */
    @SuppressLint("PrivateApi")
    private static Object getObject(String requestedName) {
        requestedName = requestedName.trim();

        try {
            Class<?> CLASS = Class.forName("android.R$styleable");
            if (requestedName.length() > 1) {
                Field f = CLASS.getDeclaredField(requestedName);
                f.setAccessible(true);
                return f.get(null);
            }
        } catch (Throwable t) {
            Resources res = Resources.getSystem();
            int resId = res.getIdentifier(requestedName, "styleable", "android");
            if (resId != 0) {
                return resId;
            }
        }
        return null;
    }
}
