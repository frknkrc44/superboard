package org.blinksd.utils.layout;

import android.content.*;
import android.view.*;
import android.widget.*;
import org.blinksd.*;
import org.blinksd.board.*;
import org.blinksd.sdb.*;

public class NumberSelectorLayout {

	private NumberSelectorLayout(){}
	
	private static SuperMiniDB db;
	
	static {
		db = SuperBoardApplication.getApplicationDatabase();
	}
	
	public static View getNumberSelectorLayout(final AppSettingsV2 ctx, final boolean isFloat, final int min, int max, int val){
		final LinearLayout main = LayoutCreator.createFilledVerticalLayout(FrameLayout.class,ctx);
		main.setGravity(Gravity.CENTER);
		main.setTag(val);
		
		final TextView text = LayoutCreator.createTextView(ctx);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2,-2);
		lp.bottomMargin = DensityUtils.dpInt(8);
		text.setLayoutParams(lp);
		text.setTextAppearance(ctx, android.R.style.TextAppearance_DeviceDefault_Medium);
		text.setText(getProgressString(ctx,val,isFloat));
		main.addView(text);
		
		CustomSeekBar seek = new CustomSeekBar(ctx);
		seek.setLayoutParams(new LinearLayout.LayoutParams(DensityUtils.wpInt(50),-2));
		seek.setMax(max - min);
		seek.setProgress(val - min);
		seek.setOnSeekBarChangeListener(new CustomSeekBar.OnSeekBarChangeListener(){
				@Override
				public void onProgressChanged(SeekBar p1, int p2, boolean p3){
					int progress = p1.getProgress() + min;
					text.setText(getProgressString(ctx,progress,isFloat));
					main.setTag(progress);
				}

				@Override
				public void onStartTrackingTouch(SeekBar p1){}

				@Override
				public void onStopTrackingTouch(SeekBar p1){}
		});
		main.addView(seek);
		
		return main;
	}
	
	private static String getProgressString(AppSettingsV2 ctx, int val, boolean isFloat){
		return isFloat ? ctx.getFloatNumberFromInt(val) + "" : val + "";
	}
	
}
