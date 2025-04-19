package org.blinksd.board.views;

import static android.view.Gravity.BOTTOM;
import static android.view.Gravity.CENTER_VERTICAL;
import static android.view.Gravity.LEFT;
import static android.view.Gravity.RIGHT;
import static android.view.Gravity.TOP;

import static org.blinksd.utils.ResourcesUtils.getCircleButtonBackground;
import static org.blinksd.utils.ResourcesUtils.getTransSelectableItemBg;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;

import org.blinksd.board.R;
import org.blinksd.utils.ColorUtils;
import org.blinksd.utils.DensityUtils;
import org.blinksd.utils.ResourcesUtils;
import org.blinksd.utils.SettingMap;
import org.blinksd.utils.SimpleAnimatorListener;
import org.blinksd.utils.SuperDBHelper;
import org.blinksd.utils.ViewUtils;

public class FABView extends LinearLayout {
    private static int BUTTON_SIZE = 64, DEFAULT_ICON_COLOR = 0xFFFFFFFF, iconColor = DEFAULT_ICON_COLOR;
    private static boolean REVERSE = false, EXCEPTION = false, OLD_REVERSE = false;
    private LinearLayout buttonLayouts = null;
    private ImageView main = null;
    private ViewGroup sv = null;
    private Space bugFixLayout = null;
    private Orientation oldOri = null;
    private final OnButtonClickListener onButtonClickListener;
    private final OnButtonClickInternalListener onButtonClickInternalListener;

    public FABView(Context context, OnButtonClickListener onButtonClickListener){
        super(context);
        this.onButtonClickListener = onButtonClickListener;
        this.onButtonClickInternalListener = new OnButtonClickInternalListener();
        setOrientation(Orientation.BRV);
        addButton(new SubButton(android.R.drawable.ic_input_add));
    }

    @SuppressLint("WrongConstant")
    @Override
    public int getOrientation(){
        if(EXCEPTION){
            setOrientation(-100);
            return -1;
        }

        EXCEPTION = true;
        return super.getOrientation();
    }

    public Orientation getFBOrientation(){
        return oldOri;
    }

    public void setOrientation(Orientation ori){
        if(ori != oldOri){
            EXCEPTION = false;
            REVERSE = false;
            switch(ori){
                case BLV:
                    setGravity(BOTTOM | LEFT);
                    setOrientation(VERTICAL);
                    REVERSE = true;
                    break;
                case BLH:
                    setGravity(BOTTOM | LEFT);
                    setOrientation(HORIZONTAL);
                    break;
                case BRV:
                    setGravity(BOTTOM | RIGHT);
                    setOrientation(VERTICAL);
                    REVERSE = true;
                    break;
                case BRH:
                    setGravity(BOTTOM | RIGHT);
                    setOrientation(HORIZONTAL);
                    REVERSE = true;
                    break;
                case TLV:
                    setGravity(TOP | LEFT);
                    setOrientation(VERTICAL);
                    break;
                case TLH:
                    setGravity(TOP | LEFT);
                    setOrientation(HORIZONTAL);
                    break;
                case TRV:
                    setGravity(TOP | RIGHT);
                    setOrientation(VERTICAL);
                    break;
                case TRH:
                    setGravity(TOP | RIGHT);
                    setOrientation(HORIZONTAL);
                    REVERSE = true;
                    break;
            }

            if(buttonLayouts != null){
                buttonLayouts.setOrientation(getOrientation());
                View[] buttonsArray = new View[buttonLayouts.getChildCount()];

                for(int i = 0; i < buttonsArray.length; i++){
                    buttonsArray[i] = buttonLayouts.getChildAt(i);
                }

                setScrollView();
                for (View view : buttonsArray) {
                    add(buttonLayouts, view, OLD_REVERSE);
                }

                if(REVERSE){
                    addView(bugFixLayout);
                    addView(sv);
                    addView(main);
                } else {
                    addView(main);
                    addView(sv);
                    addView(bugFixLayout);
                }
            }
            EXCEPTION = true;
            OLD_REVERSE = REVERSE;
            oldOri = ori;
        }
    }

    public void addButton(SubButton sb){
        ImageView buttonItem = new ImageView(getContext());
        buttonItem.setImageDrawable(sb.buttonImage);
        int p = DensityUtils.dpInt(16);
        buttonItem.setPadding(p,p,p,p);
        if (sb.keyCode == null) {
            ViewUtils.setBackground(buttonItem, getTransSelectableItemBg(getContext(), iconColor));
        } else {
            ViewUtils.setBackground(buttonItem, getCircleButtonBackground(iconColor, true));
        }
        buttonItem.setScaleType(ImageView.ScaleType.FIT_CENTER);
        buttonItem.setTag(R.id.key_normal_press, sb.keyCode);
        buttonItem.setOnClickListener(onButtonClickInternalListener);
        if(getChildCount() != 0){
            buttonItem.setScaleX(0);
            buttonItem.setScaleY(0);
        }
        int btnSize = DensityUtils.dpInt(BUTTON_SIZE);
        if(buttonLayouts == null){
            addView(main = buttonItem);
            buttonItem.setTag(getChildCount());
            var params = new LayoutParams((int) (btnSize * 0.85f),(int) (btnSize * 0.85f),0);
            params.gravity = CENTER_VERTICAL;
            params.rightMargin = p / 2;
            buttonItem.setLayoutParams(params);
            buttonLayouts = new LinearLayout(getContext());
            EXCEPTION = false;
            buttonLayouts.setOrientation(getOrientation());
            buttonLayouts.setGravity(CENTER_VERTICAL);
            buttonLayouts.setLayoutParams(new ScrollView.LayoutParams(-2,-1));
            setScrollView();
            add(this, sv);
            // BUG FIX ITEM, DON'T DELETE IT
            bugFixLayout = new Space(getContext());
            bugFixLayout.setLayoutParams(new LayoutParams(btnSize,btnSize,0));
            bugFixLayout.setVisibility(GONE);
            add(this, bugFixLayout);
        } else {
            var subBtnSize = (int) (btnSize * 0.75f);
            var params = new LinearLayout.LayoutParams(subBtnSize, subBtnSize);
            params.rightMargin = p / 2;
            buttonItem.setLayoutParams(params);
            buttonItem.setTag(buttonLayouts.getChildCount());
            add(buttonLayouts, buttonItem);
        }
    }

    private void setScrollView(){
        if(sv != null){
            buttonLayouts.removeAllViewsInLayout();
            sv.removeAllViewsInLayout();
            removeAllViewsInLayout();
        }
        sv = (buttonLayouts.getOrientation() == VERTICAL)
                ? new ScrollView(getContext())
                : new HorizontalScrollView(getContext());
        sv.setLayoutParams(new LayoutParams(-2,buttonLayouts.getOrientation() == VERTICAL ? -2 : -1,0));
        sv.setVisibility(View.GONE);
        sv.addView(buttonLayouts);
        sv.setHorizontalScrollBarEnabled(false);
        sv.setVerticalScrollBarEnabled(false);
    }

    @Override
    public void setOrientation(int orientation){
        if(EXCEPTION){
            throw new RuntimeException("Incompatible method for "+getClass().getSimpleName());
        } else {
            super.setOrientation(orientation);
        }
    }

    private void add(ViewGroup g, View v){
        add(g,v,REVERSE);
    }

    private void add(ViewGroup g, View v, boolean force){
        if(force){
            g.addView(v,0);
        } else {
            g.addView(v);
        }
    }

    void reTheme(int textColor) {
        ColorUtils.setColorFilter(main, textColor);
    }

    /*
    void expand() {
        if (main.getRotation() != 135) {
            onButtonClickInternalListener.onClick(main);
        }
    }

    void collapse() {
        if (main.getRotation() != 0) {
            onButtonClickInternalListener.onClick(main);
        }
    }

    void toggle(boolean expand) {
        if (expand) {
            expand();
        } else {
            collapse();
        }
    }
     */

    public interface OnButtonClickListener {
        void onClick(int keyCode);
    }

    private class OnButtonClickInternalListener implements OnClickListener {
        int baseDelay = 25;

        @Override
        public void onClick(View v){
            if (v.getTag(R.id.key_normal_press) != null) {
                onButtonClickListener.onClick((int) v.getTag(R.id.key_normal_press));
                return;
            }

            if(getChildCount() != 1){
                if(buttonLayouts.getChildAt(0).getScaleX() == 0){
                    sv.setVisibility(VISIBLE);
                    main.animate().rotation(135);
                } else {
                    main.animate().rotation(0);
                }
                for(int i = 0; i < buttonLayouts.getChildCount(); i++){
                    final int g = i,d1 = Math.abs((buttonLayouts.getChildCount()-1)-(i+1))*baseDelay,d2 = (i+1)*baseDelay;
                    if(buttonLayouts.getChildAt(0).getScaleX() == 0){
                        buttonLayouts.getChildAt(i).animate().scaleX(1).scaleY(1).setStartDelay(REVERSE ? d1 : d2).setListener(new SimpleAnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                buttonLayouts.getChildAt(g).setVisibility(View.VISIBLE);
                                if(g == (REVERSE ? (buttonLayouts.getChildCount()-1) : 0)){
                                    if(REVERSE){
                                        sv.setScrollX(buttonLayouts.getChildCount()*DensityUtils.dpInt(BUTTON_SIZE));
                                        sv.setScrollY(buttonLayouts.getChildCount()*DensityUtils.dpInt(BUTTON_SIZE));
                                    } else {
                                        sv.setScrollX(0);
                                        sv.setScrollY(0);
                                    }
                                }
                            }
                        });
                    } else {
                        buttonLayouts.getChildAt(i).animate().scaleX(0).scaleY(0).setStartDelay(REVERSE ? d2 : d1).setListener(new SimpleAnimatorListener() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                buttonLayouts.getChildAt(g).setVisibility(View.GONE);
                                if(g == (REVERSE ? (buttonLayouts.getChildCount()-1) : 0)){
                                    sv.setVisibility(GONE);
                                }
                            }
                        });
                    }
                }
            }
        }
    }

    public static class SubButton {
        private final Drawable buttonImage;
        private final Integer keyCode;

        private SubButton(int resource) {
            this(resource, null);
        }

        SubButton(Drawable img, int keyCode) {
            buttonImage = img;
            this.keyCode = keyCode;
            ColorUtils.setColorFilter(buttonImage, iconColor);
        }

        SubButton(int resource, Integer keyCode) {
            buttonImage = ResourcesUtils.getDrawable(resource);
            this.keyCode = keyCode;
            ColorUtils.setColorFilter(buttonImage, iconColor);
        }
    }

    public enum Orientation { BRH, BRV, BLH, BLV, TRH, TRV, TLH, TLV }
}
