package yandroid.graphics;

import android.annotation.TargetApi;
import android.util.FloatProperty;
import android.util.Property;

import yandroid.widget.YSwitch;

public class PropertyUtils {
    private PropertyUtils() {}

    @TargetApi(24)
    public static final FloatProperty<YSwitch> THUMB_POS = new FloatProperty<YSwitch>("thumbPos") {
        @Override
        public Float get(YSwitch object) {
            return object.getThumbPosition();
        }

        @Override
        public void setValue(YSwitch object, float value) {
            object.setThumbPosition(value);
        }
    };

    @TargetApi(14)
    public static final Property<YSwitch, Float> THUMB_POS_COMPAT = new Property<YSwitch, Float>(Float.class, "thumbPos") {
        @Override
        public Float get(YSwitch object) {
            return object.getThumbPosition();
        }

        @Override
        public void set(YSwitch object, Float value) {
            super.set(object, value);
            object.setThumbPosition(value);
        }
    };
}
