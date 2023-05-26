package org.blinksd.utils.layout;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;

import org.blinksd.board.R;
import org.blinksd.utils.color.ColorUtils;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import yandroid.widget.YCompoundButton;
import yandroid.widget.YSwitch;

public class LayoutCreator {
	
	public static View getView(Class<?> clazz, Context ctx){
		try {
			Constructor<?> cs = clazz.getConstructor(Context.class);
			cs.setAccessible(true);
			return (View) cs.newInstance(ctx);
		} catch(Throwable t){
			throw new RuntimeException(t);
		}
		// return null;
	}
	
	public static View getFilledView(Class<?> clazz, Class<?> rootViewClass, Context ctx){
		View v = getView(clazz, ctx);
		v.setLayoutParams(createLayoutParams(rootViewClass, -1, -1));
		return v;
	}
	
	public static View getHFilledView(Class<?> clazz, Class<?> rootViewClass, Context ctx){
		View v = getFilledView(clazz, rootViewClass, ctx);
		v.getLayoutParams().height = -2;
		return v;
	}
	
	public static View getVFilledView(Class<?> clazz, Class<?> rootViewClass, Context ctx){
		View v = getFilledView(clazz, rootViewClass, ctx);
		v.getLayoutParams().width = -2;
		return v;
	}
	
	public static LinearLayout createHorizontalLayout(Context ctx){
		return (LinearLayout) getView(LinearLayout.class,ctx);
	}
	
	public static LinearLayout createVerticalLayout(Context ctx){
		LinearLayout ll = createHorizontalLayout(ctx);
		ll.setOrientation(LinearLayout.VERTICAL);
		return ll;
	}
	
	public static LinearLayout createFilledHorizontalLayout(Class<?> rootViewClass, Context ctx){
		LinearLayout ll = createHorizontalLayout(ctx);
		ll.setLayoutParams(createLayoutParams(rootViewClass, -1, -1));
		return ll;
	}
	
	public static LinearLayout createFilledVerticalLayout(Class<?> rootViewClass, Context ctx){
		LinearLayout ll = createFilledHorizontalLayout(rootViewClass, ctx);
		ll.setOrientation(LinearLayout.VERTICAL);
		return ll;
	}
	
	public static LinearLayout createGridBox(Context ctx, int rowCount, int columnCount){
		LinearLayout main = createVerticalLayout(ctx);
		for(int i = 0;i < rowCount;i++){
			LinearLayout hor = createHorizontalLayout(ctx);
			for(int j = 0;j < columnCount;j++){
				Button btn = createButton(ctx);
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1,-2,1);
				int m = DensityUtils.dpInt(8);
				lp.setMargins(m,m,j == columnCount - 1 ? m : 0,i == rowCount - 1 ? m : 0);
				btn.setLayoutParams(lp);
				btn.setTag((i*columnCount)+j);
				hor.addView(btn);
			}
			main.addView(hor);
		}
		return main;
	}
	
	public static LinearLayout createFilledGridBox(Class<?> rootViewClass, Context ctx, int rowCount, int columnCount){
		LinearLayout main = createGridBox(ctx, rowCount, columnCount);
		main.setLayoutParams(createLayoutParams(rootViewClass, -1, -1));
		return main;
	}
	
	public static ScrollView createScrollableGridBox(Context ctx, int rowCount, int columnCount){
		ScrollView main = new ScrollView(ctx);
		main.addView(createGridBox(ctx, rowCount, columnCount));
		return main;
	}
	
	public static ScrollView createScrollableFilledGridBox(Class<?> rootViewClass, Context ctx, int rowCount, int columnCount){
		ScrollView main = createScrollableGridBox(ctx, rowCount, columnCount);
		main.setLayoutParams(createLayoutParams(rootViewClass, -1, -1));
		return main;
	}
	
	public static Button createButton(Context ctx){
		return (Button) getView(Button.class, ctx);
	}
	
	public static Button getButtonFromGridBox(ViewGroup box, int row, int column){
		ViewGroup group = (ViewGroup) box.getChildAt(row);
		return (Button) group.getChildAt(column);
	}
	
	public static ArrayList<Button> getButtonsFromGridBox(ViewGroup box){
		ArrayList<Button> btnList = new ArrayList<Button>();
		for(int i = 0;i < box.getChildCount();i++){
			ViewGroup group = (ViewGroup) box.getChildAt(i);
			for(int j = 0;j < group.getChildCount();j++){
				Button btn = (Button) group.getChildAt(j);
				btnList.add(btn);
			}
		}
		return btnList;
	}
	
	public static Button getButtonFromScrollableGridBox(ViewGroup box, int row, int column){
		ViewGroup group = (ViewGroup) box.getChildAt(0);
		return getButtonFromGridBox(group, row, column);
	}
	
	public static ArrayList<Button> getButtonsFromScrollableGridBox(ViewGroup box){
		ViewGroup group = (ViewGroup) box.getChildAt(0);
		return getButtonsFromGridBox(group);
	}

	public static ViewGroup.LayoutParams createLayoutParams(Class<?> rootViewClass, int width, int height){
		try {
			if(rootViewClass == null){
				rootViewClass = ViewGroup.class;
			}
			Class<?> c = Class.forName(rootViewClass.getName() + "$LayoutParams");
			Constructor cs = c.getConstructor(int.class,int.class);
			cs.setAccessible(true);
			ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) cs.newInstance(width,height);
			return params;
		} catch(Throwable t){}
		return null;
	}
	
	public static ViewGroup.LayoutParams createLayoutParams(Class<?> rootViewClass, int width, int height, int weight){
		try {
			if(rootViewClass == null){
				rootViewClass = ViewGroup.class;
			}
			Class<?> c = Class.forName(rootViewClass.getName() + "$LayoutParams");
			Constructor<?> cs = c.getConstructor(int.class,int.class,int.class);
			cs.setAccessible(true);
			ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) cs.newInstance(width,height,weight);
			return params;
		} catch(Throwable t){
			return createLayoutParams(rootViewClass, width, height);
		}
	}
	
	public static YSwitch createYSwitch(Context ctx, String text, boolean on, YCompoundButton.OnCheckedChangeListener listener){
		YSwitch sw = (YSwitch) getView(YSwitch.class, ctx);
		sw.setText(text);
		sw.setChecked(on);
		sw.setOnCheckedChangeListener(listener);
		sw.setThumbResource(R.drawable.switch_thumb);
		sw.setTrackResource(R.drawable.switch_track);
		int tint = Build.VERSION.SDK_INT >= 31 
					? ctx.getResources().getColor(android.R.color.system_accent1_200)
					: ColorUtils.getAccentColor();
		if (Build.VERSION.SDK_INT >= 21) {
			sw.getThumbDrawable().setTint(tint);
			sw.getTrackDrawable().setTint(tint);
		} else {
			sw.getThumbDrawable().setColorFilter(tint, PorterDuff.Mode.SRC_ATOP);
			sw.getThumbDrawable().setColorFilter(tint, PorterDuff.Mode.SRC_ATOP);
		}

		return sw;
	}
	
	public static YSwitch createFilledYSwitch(Class<?> rootViewClass, Context ctx, String text, boolean on, YCompoundButton.OnCheckedChangeListener listener){
		YSwitch view = createYSwitch(ctx, text, on, listener);
		view.setLayoutParams(createLayoutParams(rootViewClass, -1, -2));
		return view;
	}
	
	public static TextView createTextView(Context ctx){
		return (TextView) getView(TextView.class, ctx);
	}
	
	public static ImageView createImageView(Context ctx){
		return (ImageView) getView(ImageView.class, ctx);
	}
	
}
