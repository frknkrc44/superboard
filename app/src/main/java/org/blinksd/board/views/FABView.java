package org.blinksd.board.views;

import static android.view.Gravity.BOTTOM;
import static android.view.Gravity.CENTER_VERTICAL;
import static android.view.Gravity.LEFT;
import static android.view.Gravity.RIGHT;
import static android.view.Gravity.TOP;
import static org.blinksd.utils.ColorUtils.setColorFilter;
import static org.blinksd.utils.ResourcesUtils.getCircleButtonBackground;
import static org.blinksd.utils.ResourcesUtils.getTransSelectableItemBg;
import static org.blinksd.utils.ViewUtils.setViewBackground;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.StateListDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;

import org.blinksd.board.R;
import org.blinksd.utils.DensityUtils;
import org.blinksd.utils.SimpleAnimatorListener;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("ViewConstructor")
public class FABView extends LinearLayout {
    private static final int BUTTON_SIZE = 10;
    private static boolean REVERSE = false, EXCEPTION = false, OLD_REVERSE = false;
    private LinearLayout buttonLayouts = null;
    private ImageView main = null;
    private ViewGroup sv = null;
    private Space bugFixLayout = null;
    private Orientation oldOri = null;
    private final OnButtonClickListener onButtonClickListener;
    private final OnButtonClickInternalListener onButtonClickInternalListener;
    private final OnStateChangedListener onStateChangedListener;
    public final List<Integer> disabledKeycodes;

    public FABView(Context context, OnButtonClickListener onButtonClickListener, OnStateChangedListener onStateChangedListener){
        super(context);
        this.disabledKeycodes = new ArrayList<>();
        this.onButtonClickListener = onButtonClickListener;
        this.onButtonClickInternalListener = new OnButtonClickInternalListener();
        this.onStateChangedListener = onStateChangedListener;
        setOrientation(Orientation.BRV);
        addButton(android.R.drawable.ic_input_add, null);
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

    public View findButtonByKeyCode(int keyCode) {
        for (int i = 0; i < buttonLayouts.getChildCount(); i++) {
            var child = buttonLayouts.getChildAt(i);
            var np = child.getTag(R.id.key_normal_press);
            if (np != null && (int) np == keyCode) {
                return child;
            }
        }

        return null;
    }

    public void addButton(int resource, Integer keyCode) {
        addButton(resource, keyCode, false);
    }

    public void addButton(int resource, Integer keyCode, boolean stateful) {
        StatefulImageView buttonItem = new StatefulImageView(getContext());
        buttonItem.setImageResource(resource);
        int p = DensityUtils.mpInt(2);
        buttonItem.setPadding(p,p,p,p);
        buttonItem.setScaleType(ImageView.ScaleType.FIT_CENTER);
        buttonItem.setTag(R.id.key_normal_press, keyCode);
        buttonItem.setOnClickListener(onButtonClickInternalListener);
        if(getChildCount() != 0){
            buttonItem.setScaleX(0);
            buttonItem.setScaleY(0);
        }
        int btnSize = DensityUtils.mpInt(BUTTON_SIZE);
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
            buttonItem.setVisibility(GONE);
            buttonItem.setLayoutParams(params);
            buttonItem.setTag(buttonLayouts.getChildCount());
            buttonItem.setTag(R.id.key_long_press, stateful);
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

    void changeKeyState(int keyCode, boolean state) {
        var button = findButtonByKeyCode(keyCode);
        if (button != null)
            button.setSelected(state);
    }

    void reTheme(int keyColor, int textColor) {
        collapse();
        setViewBackground(main, getTransSelectableItemBg(getContext(), textColor));
        setColorFilter(main, textColor);

        for (int i = 0; i < buttonLayouts.getChildCount(); i++) {
            if (buttonLayouts.getChildAt(i) instanceof StatefulImageView child) {
                if ((boolean) child.getTag(R.id.key_long_press)) {
                    var stateListDrawable = new StateListDrawable();
                    stateListDrawable.addState(new int[]{android.R.attr.state_selected}, getCircleButtonBackground(keyColor, textColor, true));
                    stateListDrawable.addState(new int[]{}, getCircleButtonBackground(keyColor, textColor, false));
                    child.setSelected(false);

                    setViewBackground(child, stateListDrawable);

                    var colorStates = new ColorStateList(new int[][]{
                            new int[] {android.R.attr.state_selected},
                            new int[]{},
                    }, new int[] {keyColor, textColor});
                    child.saveState(colorStates);
                } else {
                    setViewBackground(child, getCircleButtonBackground(keyColor, textColor, false));
                    setColorFilter(child, textColor);
                }
            }
        }
    }

    void collapse() {
        if (main.getRotation() != 0) {
            onButtonClickInternalListener.onClick(main);
        }
    }

    /*
    void expand() {
        if (main.getRotation() != 135) {
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
                boolean collapsed = buttonLayouts.getChildAt(0).getScaleX() == 0;
                if(collapsed){
                    sv.setVisibility(VISIBLE);
                    main.animate().rotation(135);
                } else {
                    main.animate().rotation(0);
                }
                onStateChangedListener.onStateChanged(collapsed ? 0 : 1, collapsed ? 0 : baseDelay * (buttonLayouts.getChildCount() - disabledKeycodes.size()) * 2);
                for(int i = 0; i < buttonLayouts.getChildCount(); i++){
                    if (disabledKeycodes.contains(buttonLayouts.getChildAt(i).getTag(R.id.key_normal_press))) {
                        continue;
                    }

                    final int g = i,d1 = Math.abs((buttonLayouts.getChildCount()-1)-(i+1))*baseDelay,d2 = (i+1)*baseDelay;
                    if(collapsed){
                        buttonLayouts.getChildAt(i).animate().scaleX(1).scaleY(1).setStartDelay(REVERSE ? d1 : d2).setListener(new SimpleAnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                buttonLayouts.getChildAt(g).setVisibility(View.VISIBLE);
                                if(g == (REVERSE ? (buttonLayouts.getChildCount()-1) : 0)){
                                    if(REVERSE){
                                        sv.setScrollX(buttonLayouts.getChildCount()*DensityUtils.mpInt(BUTTON_SIZE));
                                        sv.setScrollY(buttonLayouts.getChildCount()*DensityUtils.mpInt(BUTTON_SIZE));
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

    public enum Orientation { BRH, BRV, BLH, BLV, TRH, TRV, TLH, TLV }

    public interface OnStateChangedListener {
        void onStateChanged(int alpha, int requiredDelay);
    }
}
