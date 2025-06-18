package org.blinksd.board.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.RadioGroup;

import org.blinksd.utils.DensityUtils;

import java.util.List;

@SuppressLint("ViewConstructor")
public final class RadioSelectorLayout extends RadioGroup {
    public RadioSelectorLayout(Context context, int selection, List<String> items) {
        super(context);

        int padding = DensityUtils.dpInt(8);
        setPadding(padding, padding, padding, padding);
        setTag(selection);
        setOnCheckedChangeListener((group, checkedId) -> setTag(checkedId));

        final var itemsSize = items.size();
        for (int i = 0; i < itemsSize; i++) {
            CustomRadioButton rb = new CustomRadioButton(context);
            rb.setId(i);
            rb.setChecked(i == selection);
            rb.setText(items.get(i));
            addView(rb);
        }
    }
    
}
