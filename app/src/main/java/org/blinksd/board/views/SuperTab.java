package org.blinksd.board.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.blinksd.utils.ColorUtils;
import org.blinksd.utils.DensityUtils;

@SuppressLint("ViewConstructor")
public class SuperTab extends LinearLayout {
    public static final int DEF_ICON_COLOR = 0xFFDEDEDE;
    private int currentSelection = -1;
    private final float disabledScale = 0.6f;
    private ViewGroup root;
    private OnTabChangedListener mOnTabChangedListener;

    public SuperTab(Context context, ViewGroup rootView) {
        this(context, rootView, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public SuperTab(Context context, ViewGroup rootView, int height) {
        this(context, rootView, ViewGroup.LayoutParams.MATCH_PARENT, height);
    }

    public SuperTab(Context context, ViewGroup rootView, int width, int height) {
        super(context);
        createNewBar(rootView, width, height);
    }

    private void createNewBar(ViewGroup rootView, int width, int height) {
        setLayoutParams(new LinearLayout.LayoutParams(width, height, 0));
        root = rootView;
        root.setOnHierarchyChangeListener(new OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                child.setVisibility(getChildCount() == currentSelection ? VISIBLE : GONE);
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {}
        });
    }

    public int getSelected() {
        return currentSelection;
    }

    public void setSelected(int selection) {
        if (getSelected() == selection) {
            return;
        }

        if (root != null) {
            if (getChildCount() - 1 < getSelected()) {
                currentSelection = selection = getChildCount() - 1;
            }

            if (getSelected() >= 0) {
                getChildAt(getSelected()).animate()
                        .scaleX(disabledScale)
                        .scaleY(disabledScale)
                        .setInterpolator(new OvershootInterpolator());
                root.getChildAt(getSelected()).setVisibility(GONE);
            }

            getChildAt(selection).animate()
                    .scaleX(1)
                    .scaleY(1)
                    .setInterpolator(new OvershootInterpolator());
            root.getChildAt(selection).setVisibility(VISIBLE);

            if (mOnTabChangedListener != null) {
                mOnTabChangedListener.onTabChanged(selection);
            }
        }

        currentSelection = selection;

        if (mOnTabChangedListener != null) {
            mOnTabChangedListener.onTabChanged(selection);
        }
    }

    public void setOnTabChangedListener(OnTabChangedListener listener) {
        mOnTabChangedListener = listener;
    }

    public void addButton(int resId) {
        addButtonView().setImageResource(resId);
    }

    public void toggleButton(int index, boolean show) {
        getChildAt(index).setVisibility(show ? VISIBLE : GONE);
    }

    private ImageView addButtonView() {
        ImageView buttonView = new ImageView(getContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1);
        buttonView.setLayoutParams(lp);
        buttonView.setTag(getChildCount());
        buttonView.setScaleX(disabledScale);
        buttonView.setScaleY(buttonView.getScaleX());
        int padding = DensityUtils.mpInt(4);
        buttonView.setPadding(padding, padding, padding, padding);
        buttonView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        buttonView.setOnClickListener(v -> setSelected((int) v.getTag()));
        ColorUtils.setColorFilter(buttonView, DEF_ICON_COLOR);
        addView(buttonView);

        if (getSelected() < 0) {
            setSelected(0);
        }

        return buttonView;
    }

    public interface OnTabChangedListener {
        void onTabChanged(int index);
    }
}
