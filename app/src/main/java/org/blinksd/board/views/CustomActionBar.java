package org.blinksd.board.views;

import static org.blinksd.board.SuperBoardApplication.isWatchDevice;
import static org.blinksd.utils.ColorUtils.setColorFilter;
import static org.blinksd.utils.ResourcesUtils.getTransSelectableItemBg;
import static org.blinksd.utils.ViewUtils.setTextAppearance;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.blinksd.board.R;
import org.blinksd.utils.DensityUtils;

@SuppressLint("ViewConstructor")
public class CustomActionBar extends LinearLayout {
    private final ImageView mBackButton;
    private final TextView mTitle;
    private final LinearLayout mActions;
    private static final int barPadding = DensityUtils.dpInt(16);

    public CustomActionBar(Context context, OnClickListener onBackButtonClick) {
        super(context);
        final var isWatch = isWatchDevice();

        setLayoutParams(new LinearLayout.LayoutParams(-1, DensityUtils.dpInt(isWatch ? 48 : 56), 0));
        setGravity(Gravity.CENTER_VERTICAL);

        mBackButton = new ImageView(context);
        final int pad = DensityUtils.dpInt(8);
        var mBackButtonParams = new LayoutParams(-2, -2, 0);
        mBackButtonParams.setMargins(pad, 0, pad, 0);
        mBackButton.setLayoutParams(mBackButtonParams);
        mBackButton.setImageResource(R.drawable.arrow_left);
        mBackButton.setOnClickListener(onBackButtonClick);

        mBackButton.setPadding(0, pad, 0, pad);
        addView(mBackButton);
        toggleBackButton(false);

        mTitle = new TextView(context);
        mTitle.setLayoutParams(new LayoutParams(-1, -1, 1));
        setTextAppearance(mTitle, isWatch
                ? android.R.style.TextAppearance_Small
                : android.R.style.TextAppearance_Medium);
        setColorFilter(mBackButton, mTitle.getCurrentTextColor());
        mBackButton.setBackground(getTransSelectableItemBg(context, mTitle.getCurrentTextColor()));
        mTitle.setGravity(isWatch ? Gravity.CENTER : Gravity.CENTER_VERTICAL);

        addView(mTitle);

        mActions = new LinearLayout(context);
        mActions.setLayoutParams(new LayoutParams(-2, -2, 0));
        addView(mActions);
    }

    public void toggleBackButton(boolean show) {
        show = !isWatchDevice() && show;

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
        buttonImage.setBackground(getTransSelectableItemBg(getContext(), mTitle.getCurrentTextColor()));
        setColorFilter(buttonImage, Color.WHITE);
        mActions.addView(buttonImage);
    }
}
