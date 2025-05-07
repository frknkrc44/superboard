package org.blinksd.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import org.blinksd.board.R;

import java.lang.reflect.Constructor;

public final class LayoutCreator {

    public static View getView(Class<?> clazz, Context ctx) {
        try {
            Constructor<?> cs = clazz.getConstructor(Context.class);
            cs.setAccessible(true);
            return (View) cs.newInstance(ctx);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        // return null;
    }

    public static View getFilledView(Class<?> clazz, Class<?> rootViewClass, Context ctx) {
        View v = getView(clazz, ctx);
        v.setLayoutParams(createLayoutParams(rootViewClass, -1, -1));
        return v;
    }

    public static View getHFilledView(Class<?> clazz, Class<?> rootViewClass, Context ctx) {
        View v = getFilledView(clazz, rootViewClass, ctx);
        v.getLayoutParams().height = -2;
        return v;
    }

    public static LinearLayout createHorizontalLayout(Context ctx) {
        return (LinearLayout) getView(LinearLayout.class, ctx);
    }

    public static LinearLayout createVerticalLayout(Context ctx) {
        LinearLayout ll = createHorizontalLayout(ctx);
        ll.setOrientation(LinearLayout.VERTICAL);
        return ll;
    }

    public static LinearLayout createFilledHorizontalLayout(Class<?> rootViewClass, Context ctx) {
        LinearLayout ll = createHorizontalLayout(ctx);
        ll.setLayoutParams(createLayoutParams(rootViewClass, -1, -1));
        return ll;
    }

    public static LinearLayout createFilledVerticalLayout(Class<?> rootViewClass, Context ctx) {
        LinearLayout ll = createFilledHorizontalLayout(rootViewClass, ctx);
        ll.setOrientation(LinearLayout.VERTICAL);
        return ll;
    }

    public static Button createButton(Context ctx) {
        return (Button) getView(Button.class, ctx);
    }

    public static ViewGroup.LayoutParams createLayoutParams(Class<?> rootViewClass, int width, int height) {
        try {
            if (rootViewClass == null) {
                rootViewClass = ViewGroup.class;
            }
            Class<?> c = Class.forName(rootViewClass.getName() + "$LayoutParams");
            Constructor<?> cs = c.getConstructor(int.class, int.class);
            cs.setAccessible(true);
            return (ViewGroup.LayoutParams) cs.newInstance(width, height);
        } catch (Throwable ignored) {}

        return new ViewGroup.LayoutParams(width, height);
    }

    public static ViewGroup.LayoutParams createLayoutParams(Class<?> rootViewClass, int width, int height, float weight) {
        try {
            if (rootViewClass == null) {
                rootViewClass = ViewGroup.class;
            }
            Class<?> c = Class.forName(rootViewClass.getName() + "$LayoutParams");
            Constructor<?> cs = c.getConstructor(int.class, int.class, float.class);
            cs.setAccessible(true);
            return (ViewGroup.LayoutParams) cs.newInstance(width, height, weight);
        } catch (Throwable ignored) {}

        return createLayoutParams(rootViewClass, width, height);
    }

    public static Switch createSwitch(Context ctx, String text, boolean on, CompoundButton.OnCheckedChangeListener listener) {
        Switch sw = (Switch) getView(Switch.class, ctx);
        sw.setText(text);
        sw.setChecked(on);
        sw.setOnCheckedChangeListener(listener);
        sw.setTextOff("");
        sw.setTextOn("");

        sw.setThumbResource(R.drawable.switch_thumb);
        sw.setTrackResource(R.drawable.switch_track);

        return sw;
    }

    public static Switch createFilledSwitch(Class<?> rootViewClass, Context ctx, String text, boolean on, CompoundButton.OnCheckedChangeListener listener) {
        Switch view = createSwitch(ctx, text, on, listener);
        view.setLayoutParams(createLayoutParams(rootViewClass, -1, -2));
        return view;
    }

    public static TextView createTextView(Context ctx) {
        return (TextView) getView(TextView.class, ctx);
    }

    public static ImageView createImageView(Context ctx) {
        return (ImageView) getView(ImageView.class, ctx);
    }

}
