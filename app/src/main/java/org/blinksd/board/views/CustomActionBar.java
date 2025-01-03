package org.blinksd.board.views;

import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.blinksd.utils.DensityUtils;
import org.blinksd.utils.ViewUtils;

public class CustomActionBar extends LinearLayout {
    private final TextView mTitle;

    public CustomActionBar(Context context) {
        super(context);
        setLayoutParams(new LinearLayout.LayoutParams(-1, DensityUtils.dpInt(56), 0));
        setGravity(Gravity.CENTER_VERTICAL);

        int barPadding = DensityUtils.dpInt(16);
        setPadding(barPadding, 0, barPadding, 0);

        mTitle = new TextView(context);
        mTitle.setLayoutParams(new LayoutParams(-1, -2, 1));
        ViewUtils.setTextAppearance(mTitle, android.R.style.TextAppearance_Medium);

        addView(mTitle);
    }

    public void setTitle(CharSequence title) {
        mTitle.setText(title);
    }
}
