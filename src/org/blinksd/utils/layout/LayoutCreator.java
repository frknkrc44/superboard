package org.blinksd.utils.layout;

import android.content.*;
import android.view.*;
import android.widget.*;
import java.lang.reflect.*;
import java.util.*;
import yandroid.widget.*;

public class LayoutCreator {
	
	public static final View getView(Class<?> clazz, Context ctx){
		try {
			Constructor cs = clazz.getConstructor(Context.class);
			cs.setAccessible(true);
			return (View) cs.newInstance(ctx);
		} catch(Throwable t){}
		return null;
	}
	
	public static final LinearLayout createHorizontalLayout(Context ctx){
		return (LinearLayout) getView(LinearLayout.class,ctx);
	}
	
	public static final LinearLayout createVerticalLayout(Context ctx){
		LinearLayout ll = createHorizontalLayout(ctx);
		ll.setOrientation(LinearLayout.VERTICAL);
		return ll;
	}
	
	public static final LinearLayout createFilledHorizontalLayout(Class<?> rootViewClass, Context ctx){
		LinearLayout ll = createHorizontalLayout(ctx);
		ll.setLayoutParams(createLayoutParams(rootViewClass, -1, -1));
		return ll;
	}
	
	public static final LinearLayout createFilledVerticalLayout(Class<?> rootViewClass, Context ctx){
		LinearLayout ll = createFilledHorizontalLayout(rootViewClass, ctx);
		ll.setOrientation(LinearLayout.VERTICAL);
		return ll;
	}
	
	public static final LinearLayout createGridBox(Context ctx, int rowCount, int columnCount){
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
	
	public static final LinearLayout createFilledGridBox(Class<?> rootViewClass, Context ctx, int rowCount, int columnCount){
		LinearLayout main = createGridBox(ctx, rowCount, columnCount);
		main.setLayoutParams(createLayoutParams(rootViewClass, -1, -1));
		return main;
	}
	
	public static final ScrollView createScrollableGridBox(Context ctx, int rowCount, int columnCount){
		ScrollView main = new ScrollView(ctx);
		main.addView(createGridBox(ctx, rowCount, columnCount));
		return main;
	}
	
	public static final ScrollView createScrollableFilledGridBox(Class<?> rootViewClass, Context ctx, int rowCount, int columnCount){
		ScrollView main = createScrollableGridBox(ctx, rowCount, columnCount);
		main.setLayoutParams(createLayoutParams(rootViewClass, -1, -1));
		return main;
	}
	
	public static final Button createButton(Context ctx){
		return (Button) getView(Button.class, ctx);
	}
	
	public static final Button getButtonFromGridBox(ViewGroup box, int row, int column){
		ViewGroup group = (ViewGroup) box.getChildAt(row);
		return (Button) group.getChildAt(column);
	}
	
	public static final ArrayList<Button> getButtonsFromGridBox(ViewGroup box){
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
	
	public static final Button getButtonFromScrollableGridBox(ViewGroup box, int row, int column){
		ViewGroup group = (ViewGroup) box.getChildAt(0);
		return getButtonFromGridBox(group, row, column);
	}
	
	public static final ArrayList<Button> getButtonsFromScrollableGridBox(ViewGroup box){
		ViewGroup group = (ViewGroup) box.getChildAt(0);
		return getButtonsFromGridBox(group);
	}
	
	public static final ViewGroup.LayoutParams createLayoutParams(Class<?> rootViewClass, int width, int height){
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
	
	public static final YSwitch createYSwitch(Context ctx, String text, boolean on, YCompoundButton.OnCheckedChangeListener listener){
		YSwitch sw = (YSwitch) getView(YSwitch.class, ctx);
		sw.setText(text);
		sw.setChecked(on);
		sw.setOnCheckedChangeListener(listener);
		return sw;
	}
	
	public static final YSwitch createFilledYSwitch(Class<?> rootViewClass, Context ctx, String text, boolean on, YCompoundButton.OnCheckedChangeListener listener){
		YSwitch view = createYSwitch(ctx, text, on, listener);
		view.setLayoutParams(createLayoutParams(rootViewClass, -1, -2));
		return view;
	}
	
	public static final TextView createTextView(Context ctx){
		return (TextView) getView(TextView.class, ctx);
	}
	
	public static final ImageView createImageView(Context ctx){
		return (ImageView) getView(ImageView.class, ctx);
	}
	
}
