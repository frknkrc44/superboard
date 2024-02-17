package org.blinksd.board.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.RadioGroup;

import org.blinksd.utils.DensityUtils;

import java.util.List;

@SuppressLint("ViewConstructor")
final class RadioSelectorLayout extends RadioGroup {
    RadioSelectorLayout(Context context, int selection, List<String> items) {
        super(context);
        int i = DensityUtils.dpInt(8);
        setPadding(i, i, i, i);
        setTag(selection);
        setOnCheckedChangeListener((group, checkedId) -> setTag(checkedId));
        i = 0;
        for (String key : items) {
            CustomRadioButton rb = new CustomRadioButton(context);
            rb.setId(i);
            rb.setChecked(i == selection);
            rb.setText(key);
            addView(rb);
            i++;
        }
    }
    
}
