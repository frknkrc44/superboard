package org.blinksd.utils.layout;

import android.content.*;
import android.view.*;
import android.widget.*;

import java.util.*;

public class RadioSelectorLayout {
	
	private RadioSelectorLayout(){}
	
	public static final View getRadioSelectorLayout(Context ctx, int selection, String[] items){
		List<String> itemOut = new ArrayList<String>();
		for(String item : items){
			itemOut.add(item);
		}
		return getRadioSelectorLayout(ctx, selection, itemOut);
	}
	
	public static final View getRadioSelectorLayout(Context ctx, int selection, List<String> items){
		final RadioGroup rg = new RadioGroup(ctx);
		int i = DensityUtils.dpInt(8);
		rg.setPadding(i,i,i,i);
		rg.setTag(selection);
		rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId){
				rg.setTag(checkedId);
			}
		});
		i = 0;
		for(String key : items){
			CustomRadioButton rb = new CustomRadioButton(ctx);
			rb.setId(i);
			rb.setChecked(i == selection);
			rb.setText(key);
			rg.addView(rb);
			i++;
		}
		return rg;
	}
	
}
