package org.blinksd.utils.layout;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.blinksd.SuperBoardApplication;
import org.blinksd.board.AppSettingsV2;
import org.blinksd.sdb.SuperMiniDB;

public class NumberSelectorLayout {
	private NumberSelectorLayout(){}
	public static View getNumberSelectorLayout(final AppSettingsV2 ctx, final boolean isFloat, final int min, int max, int val){
		final LinearLayout main = LayoutCreator.createFilledVerticalLayout(FrameLayout.class,ctx);
		main.setGravity(Gravity.CENTER);
		main.setTag(val);
		
		final TextView text = LayoutCreator.createTextView(ctx);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2,-2);
		lp.bottomMargin = DensityUtils.dpInt(8);
		text.setLayoutParams(lp);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			text.setTextAppearance(ctx, android.R.style.TextAppearance_DeviceDefault_Medium);
		}
		text.setText(getProgressString(val,isFloat));
		main.addView(text);
		
		CustomSeekBar seek = new CustomSeekBar(ctx);
		seek.setLayoutParams(new LinearLayout.LayoutParams(DensityUtils.wpInt(50),-2));
		seek.setMax(max - min);
		seek.setProgress(val - min);
		seek.setOnSeekBarChangeListener(new CustomSeekBar.OnSeekBarChangeListener(){
				@Override
				public void onProgressChanged(SeekBar p1, int p2, boolean p3){
					int progress = p1.getProgress() + min;
					text.setText(getProgressString(progress,isFloat));
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
	
	private static String getProgressString(int val, boolean isFloat){
		return isFloat
				? String.valueOf(DensityUtils.getFloatNumberFromInt(val))
				: String.valueOf(val);
	}
	
}
