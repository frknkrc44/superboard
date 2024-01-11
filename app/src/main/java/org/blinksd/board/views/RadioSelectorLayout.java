package org.blinksd.board.views;

import android.content.Context;
import android.view.View;
import android.widget.RadioGroup;

import org.blinksd.utils.DensityUtils;

import java.util.List;

public class RadioSelectorLayout {
    private RadioSelectorLayout() {
    }

    public static View getRadioSelectorLayout(Context ctx, int selection, List<String> items) {
        final RadioGroup rg = new RadioGroup(ctx);
        int i = DensityUtils.dpInt(8);
        rg.setPadding(i, i, i, i);
        rg.setTag(selection);
        rg.setOnCheckedChangeListener((group, checkedId) -> rg.setTag(checkedId));
        i = 0;
        for (String key : items) {
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
