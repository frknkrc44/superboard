package org.blinksd.board.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.blinksd.utils.ColorUtils;
import org.blinksd.utils.DensityUtils;

@SuppressLint("ViewConstructor")
public class SuperTab extends LinearLayout {
    public static final int DEFVALUE = 0x98, DEF_BG_COLOR = 0, DEF_ICON_COLOR = 0xFFDEDEDE;
    private Context mContext;
    private int currentSelection = 0;
    private final float disabled = 0.6f;
    private ViewGroup root;
    private OnTabChangedListener mOnTabChangedListener;

    public SuperTab(Context context, ViewGroup rootView) {
        this(context, rootView, DEFVALUE);
    }

    public SuperTab(Context context, ViewGroup rootView, int height) {
        this(context, rootView, DEFVALUE, height);
    }

    public SuperTab(Context context, ViewGroup rootView, int width, int height) {
        this(context, rootView, width, height, DEFVALUE);
    }

    public SuperTab(Context context, ViewGroup rootView, int width, int height, int bgColor) {
        super(context);
        if (width == DEFVALUE) width = getBarInfo(WIDTH);
        if (height == DEFVALUE) height = getBarInfo(HEIGHT);
        if (bgColor == DEFVALUE) bgColor = getBarInfo(BAR_BG_COLOR);
        createNewBar(context, rootView, width, height, bgColor);
    }

    private void createNewBar(Context context, ViewGroup rootView, int width, int height, int bgColor) {
        mContext = context;
        setLayoutParams(new LinearLayout.LayoutParams(width, height, 0));
        setBackgroundColor(bgColor);
        root = rootView;
    }

    public int getSelected() {
        return currentSelection;
    }

    public void setSelected(int selection) {
        if (root != null) {
            if (getChildCount() - 1 < currentSelection)
                currentSelection = selection = getChildCount() - 1;
            if (currentSelection != selection)
                getChildAt(currentSelection).animate().scaleX(disabled).scaleY(disabled).setInterpolator(new OvershootInterpolator());
            getChildAt(selection).animate().scaleX(1).scaleY(1).setInterpolator(new OvershootInterpolator());
            for (int i = 0; i != root.getChildCount(); i++)
                root.getChildAt(i).setVisibility(i == selection ? View.VISIBLE : View.GONE);

            if (mOnTabChangedListener != null)
                mOnTabChangedListener.onTabChanged(selection);
        }
        currentSelection = selection;
    }

    public void setSelected(boolean next) {
        if (next) {
            if (getSelected() == (getChildCount() - 1))
                return;
            setSelected(getSelected() + 1);
        } else {
            if (getSelected() == 0)
                return;
            setSelected(getSelected() - 1);
        }
    }

    public void setOnTabChangedListener(OnTabChangedListener listener) {
        mOnTabChangedListener = listener;
    }

    public void addButton(int resId) {
        addButtonView().setImageResource(resId);
    }

    public void hideButton(int index) {
        getChildAt(index).setVisibility(GONE);
    }

    public void toggleButton(boolean show, int index) {
        if (show) {
            showButton(index);
        } else {
            hideButton(index);
        }
    }

    public void showButton(int index) {
        getChildAt(index).setVisibility(VISIBLE);
    }

    private ImageView addButtonView() {
        ImageView iv = new ImageView(mContext);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        lp.weight = 1;
        iv.setLayoutParams(lp);
        iv.setTag(getChildCount());
        iv.setScaleX(disabled);
        iv.setScaleY(iv.getScaleX());
        int p = DensityUtils.mpInt(4);
        iv.setPadding(p, p, p, p);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        iv.setOnClickListener(v -> setSelected((int) v.getTag()));
        ColorUtils.setColorFilter(iv, DEF_ICON_COLOR);
        addView(iv);
        if (iv.getTag().equals(currentSelection)) setSelected(currentSelection);
        return iv;
    }

    public static final int WIDTH = 0,
            HEIGHT = 1,
            BAR_SELECTION = 2,
            BAR_BG_COLOR = 3;

    public int getBarInfo(int req) {
        switch (req) {
            case WIDTH:
                return ViewGroup.LayoutParams.MATCH_PARENT;
            case HEIGHT:
                return ViewGroup.LayoutParams.WRAP_CONTENT;
            case BAR_SELECTION:
                return 0;
            case BAR_BG_COLOR:
                return DEF_BG_COLOR;
            default:
                return -1;
        }
    }

    public interface OnTabChangedListener {
        void onTabChanged(int index);
    }
}
