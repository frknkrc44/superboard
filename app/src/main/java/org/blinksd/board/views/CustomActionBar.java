package org.blinksd.board.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.blinksd.board.R;
import org.blinksd.utils.ColorUtils;
import org.blinksd.utils.DensityUtils;
import org.blinksd.utils.ResourcesUtils;
import org.blinksd.utils.ViewUtils;

public class CustomActionBar extends LinearLayout {
    private final ImageView mBackButton;
    private final TextView mTitle;
    private final LinearLayout mActions;
    private static final int barPadding = DensityUtils.dpInt(16);

    public CustomActionBar(Context context, OnClickListener onBackButtonClick) {
        super(context);
        setLayoutParams(new LinearLayout.LayoutParams(-1, DensityUtils.dpInt(56), 0));
        setGravity(Gravity.CENTER_VERTICAL);

        mBackButton = new ImageView(context);
        mBackButton.setLayoutParams(new LayoutParams(-2, -2, 0));
        mBackButton.setImageResource(R.drawable.arrow_left);
        mBackButton.setOnClickListener(onBackButtonClick);
        addView(mBackButton);
        toggleBackButton(false);

        mTitle = new TextView(context);
        mTitle.setLayoutParams(new LayoutParams(-1, -2, 1));
        ViewUtils.setTextAppearance(mTitle, android.R.style.TextAppearance_Medium);
        ColorUtils.setColorFilter(mBackButton, mTitle.getCurrentTextColor());
        ViewUtils.setBackground(mBackButton,
                ResourcesUtils.getTransSelectableItemBg(context, mTitle.getCurrentTextColor()));
        addView(mTitle);

        mActions = new LinearLayout(context);
        mActions.setLayoutParams(new LayoutParams(-2, -2, 0));
        addView(mActions);
    }

    public void toggleBackButton(boolean show) {
        mBackButton.setVisibility(show ? VISIBLE : GONE);
        setPadding(show ? 0 : barPadding, 0, barPadding, 0);
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
