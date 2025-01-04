package org.blinksd.board.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.blinksd.utils.DensityUtils;
import org.blinksd.utils.ViewUtils;

public class CustomActionBar extends LinearLayout {
    private final TextView mTitle;
    private final LinearLayout mActions;

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

        mActions = new LinearLayout(context);
        mActions.setLayoutParams(new LayoutParams(-2, -2, 0));
        addView(mActions);
    }

    public void setTitle(CharSequence title) {
        mTitle.setText(title);
    }

    public void addAction(int iconResId, OnClickListener onClick) {
        ImageView buttonImage = new ImageView(getContext());
        int size = DensityUtils.dpInt(48);
        buttonImage.setLayoutParams(new LinearLayout.LayoutParams(size, size));
        buttonImage.setImageResource(iconResId);
        buttonImage.setOnClickListener(onClick);
        buttonImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        buttonImage.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        mActions.addView(buttonImage);
    }
}
