package yandroid.graphics;

import static android.os.Build.VERSION_CODES.N;

import android.annotation.TargetApi;
import android.util.FloatProperty;

import yandroid.widget.YSwitch;

public class PropertyUtils {
    private PropertyUtils() {}

    @TargetApi(N)
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
}
