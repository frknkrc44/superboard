package org.blinksd.utils.layout;

import android.content.*;
import android.graphics.drawable.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import org.blinksd.board.*;
import android.graphics.*;

public class ColorSelectorItemLayout extends LinearLayout {
	
	private TreeMap<Integer,Integer> colorList;
	private ImageView img;
	
	public ColorSelectorItemLayout(AppSettingsV2 ctx, int index, TreeMap<Integer,Integer> colors, View.OnClickListener gradientAddColorListener, View.OnClickListener gradientDelColorListener, View.OnClickListener colorSelectorListener){
		super(ctx);
		setLayoutParams(new LayoutParams(-1,-2));
		img = LayoutCreator.createImageView(ctx);
		int height = (int) getListPreferredItemHeight();
		img.setLayoutParams(LayoutCreator.createLayoutParams(LinearLayout.class,height,height));
		img.setScaleType(ImageView.ScaleType.FIT_CENTER);
		int pad = height / 4;
		img.setPadding(pad,pad,pad,pad);
		TextView btn = LayoutCreator.createTextView(ctx);
		LayoutParams lp = new LayoutParams(-1,-1,1);
		btn.setLayoutParams(lp);
		btn.setId(android.R.id.text1);
		btn.setGravity(Gravity.CENTER_VERTICAL);
		btn.setTextColor(0xFFFFFFFF);
		btn.setMinHeight(height);
		addView(img);
		addView(btn);
		setMinimumHeight(height);
		setId(index);
		switch(index){
			case -1:
				img.setImageResource(android.R.drawable.ic_input_add);
				img.setColorFilter(0xFFFFFFFF,PorterDuff.Mode.SRC_ATOP);
				btn.setText(ctx.getTranslation("image_selector_gradient_add_item"));
				setOnClickListener(gradientAddColorListener);
				return;
			case -2:
				img.setImageResource(android.R.drawable.ic_media_next);
				img.setColorFilter(0xFFFFFFFF,PorterDuff.Mode.SRC_ATOP);
				btn.setText(ctx.getTranslation("image_selector_gradient_change_orientation"));
				setOnClickListener(gradientAddColorListener);
				return;
		}
		colorList = colors;
		int color = 0xFF000000;
		updateColorView(color);
		btn.setText(ctx.getTranslation("image_selector_gradient_item"));
		ImageView del = LayoutCreator.createImageView(ctx);
		lp = new LayoutParams(height,height,0);
		del.setLayoutParams(lp);
		del.setScaleType(img.getScaleType());
		del.setImageResource(R.drawable.sym_keyboard_close);
		del.setColorFilter(0xFFFFFFFF,PorterDuff.Mode.SRC_ATOP);
		del.setPadding(pad,pad,0,pad);
		del.setOnClickListener(gradientDelColorListener);
		del.setId(index);
		setTag(color);
		setOnClickListener(colorSelectorListener);
		addView(del,0);
	}
	
	private void updateColorView(int color){
		GradientDrawable gd = new GradientDrawable();
		gd.setColor(color);
		gd.setCornerRadius(1000);
		img.setImageDrawable(gd);
	}

	@Override
	public void setTag(Object tag){
		super.setTag(tag);
		colorList.put(getId(),(int)tag);
		int color = (int) tag;
		updateColorView(color);
	}
	
	private final float getListPreferredItemHeight(){
		TypedValue value = new TypedValue();
		getContext().getTheme().resolveAttribute(android.R.attr.listPreferredItemHeight, value, true);	
		return TypedValue.complexToDimension(value.data, getResources().getDisplayMetrics());
	}
	
}
