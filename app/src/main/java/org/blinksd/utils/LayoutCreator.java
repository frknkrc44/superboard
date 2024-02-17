package org.blinksd.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import org.blinksd.board.R;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;

@SuppressWarnings("unused")
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

    public static View getVFilledView(Class<?> clazz, Class<?> rootViewClass, Context ctx) {
        View v = getFilledView(clazz, rootViewClass, ctx);
        v.getLayoutParams().width = -2;
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

    public static LinearLayout createGridBox(Context ctx, int rowCount, int columnCount) {
        LinearLayout main = createVerticalLayout(ctx);
        for (int i = 0; i < columnCount; i++) {
            LinearLayout hor = createHorizontalLayout(ctx);
            for (int j = 0; j < rowCount; j++) {
                Button btn = createButton(ctx);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2, 1);
                int m = DensityUtils.dpInt(8);
                lp.setMargins(m, m, j == rowCount - 1 ? m : 0, i == columnCount - 1 ? m : 0);
                btn.setLayoutParams(lp);
                btn.setTag((i * rowCount) + j);
                hor.addView(btn);
            }
            main.addView(hor);
        }
        return main;
    }

    public static LinearLayout createFilledGridBox(Class<?> rootViewClass, Context ctx, int rowCount, int columnCount) {
        LinearLayout main = createGridBox(ctx, rowCount, columnCount);
        main.setLayoutParams(createLayoutParams(rootViewClass, -1, -1));
        return main;
    }

    public static ScrollView createScrollableGridBox(Context ctx, int rowCount, int columnCount) {
        ScrollView main = new ScrollView(ctx);
        main.addView(createGridBox(ctx, rowCount, columnCount));
        return main;
    }

    public static ScrollView createScrollableFilledGridBox(Class<?> rootViewClass, Context ctx, int rowCount, int columnCount) {
        ScrollView main = createScrollableGridBox(ctx, rowCount, columnCount);
        main.setLayoutParams(createLayoutParams(rootViewClass, -1, -1));
        return main;
    }

    public static Button createButton(Context ctx) {
        return (Button) getView(Button.class, ctx);
    }

    public static Button getButtonFromGridBox(ViewGroup box, int row, int column) {
        ViewGroup group = (ViewGroup) box.getChildAt(row);
        return (Button) group.getChildAt(column);
    }

    public static ArrayList<Button> getButtonsFromGridBox(ViewGroup box) {
        ArrayList<Button> btnList = new ArrayList<>();
        for (int i = 0; i < box.getChildCount(); i++) {
            ViewGroup group = (ViewGroup) box.getChildAt(i);
            for (int j = 0; j < group.getChildCount(); j++) {
                Button btn = (Button) group.getChildAt(j);
                btnList.add(btn);
            }
        }
        return btnList;
    }

    public static Button getButtonFromScrollableGridBox(ViewGroup box, int row, int column) {
        ViewGroup group = (ViewGroup) box.getChildAt(0);
        return getButtonFromGridBox(group, row, column);
    }

    public static ArrayList<Button> getButtonsFromScrollableGridBox(ViewGroup box) {
        ViewGroup group = (ViewGroup) box.getChildAt(0);
        return getButtonsFromGridBox(group);
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

        return new ViewGroup.LayoutParams(-1, -1);
    }

    public static ViewGroup.LayoutParams createLayoutParams(Class<?> rootViewClass, int width, int height, int weight) {
        try {
            if (rootViewClass == null) {
                rootViewClass = ViewGroup.class;
            }
            Class<?> c = Class.forName(rootViewClass.getName() + "$LayoutParams");
            Constructor<?> cs = c.getConstructor(int.class, int.class, int.class);
            cs.setAccessible(true);
            return (ViewGroup.LayoutParams) cs.newInstance(width, height, weight);
        } catch (Throwable t) {
            return createLayoutParams(rootViewClass, width, height);
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static Switch createSwitch(Context ctx, String text, boolean on, CompoundButton.OnCheckedChangeListener listener) {
        Switch sw = (Switch) getView(Switch.class, ctx);
        sw.setText(text);
        sw.setChecked(on);
        sw.setOnCheckedChangeListener(listener);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            sw.setThumbResource(R.drawable.switch_thumb);
            sw.setTrackResource(R.drawable.switch_track);
        }

        int tint = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                ? ctx.getResources().getColor(android.R.color.system_accent1_200, ctx.getTheme())
                : ColorUtils.getAccentColor();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            sw.getThumbDrawable().setTint(tint);
            sw.getTrackDrawable().setTint(tint);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            sw.getThumbDrawable().setColorFilter(tint, PorterDuff.Mode.SRC_ATOP);
            sw.getTrackDrawable().setColorFilter(tint, PorterDuff.Mode.SRC_ATOP);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            setSwitchThumbAPI14(sw, tint);
        }

        return sw;
    }

    /** @noinspection JavaReflectionMemberAccess*/
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @SuppressLint("DiscouragedPrivateApi")
    private static void setSwitchThumbAPI14(Switch switchWidget, int tint) {
        try {
            Field thumbField = Switch.class.getDeclaredField("mThumbDrawable");
            Drawable thumbDrawable = switchWidget.getResources().getDrawable(R.drawable.switch_thumb);
            thumbDrawable.setColorFilter(tint, PorterDuff.Mode.SRC_ATOP);
            thumbField.set(switchWidget, thumbDrawable);

            Field trackField = Switch.class.getDeclaredField("mTrackDrawable");
            Drawable trackDrawable = switchWidget.getResources().getDrawable(R.drawable.switch_track);
            trackDrawable.setColorFilter(tint, PorterDuff.Mode.SRC_ATOP);
            trackField.set(switchWidget, trackDrawable);
        } catch (Throwable ignored) {}
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
