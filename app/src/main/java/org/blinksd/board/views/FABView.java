package org.blinksd.board.views;

import static android.view.Gravity.BOTTOM;
import static android.view.Gravity.CENTER_VERTICAL;
import static android.view.Gravity.LEFT;
import static android.view.Gravity.RIGHT;
import static android.view.Gravity.TOP;
import static org.blinksd.utils.ColorUtils.calculateContrast;
import static org.blinksd.utils.ColorUtils.convertARGBtoRGB;
import static org.blinksd.utils.ColorUtils.invertColor;
import static org.blinksd.utils.ColorUtils.satisfiesTextContrast;
import static org.blinksd.utils.ColorUtils.setColorFilter;
import static org.blinksd.utils.ResourcesUtils.getCircleButtonBackground;
import static org.blinksd.utils.ResourcesUtils.getTransSelectableItemBg;

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
    private static boolean REVERSE = false, EXCEPTION = false;
    private LinearLayout buttonLayouts = null;
    private ImageView main = null;
    private ViewGroup sv = null;
    private Space bugFixLayout = null;
    private Orientation oldOri = null;
    private final OnButtonClickListener onButtonClickListener;
    private final OnButtonClickInternalListener onButtonClickInternalListener;
    private final OnStateChangedListener onStateChangedListener;
    private final OnLongClickListener onButtonLongClickListener;
    public final List<Integer> disabledKeycodes;

    public FABView(Context context, OnButtonClickListener onButtonClickListener, OnStateChangedListener onStateChangedListener, OnLongClickListener onButtonLongClickListener){
        super(context);
        this.disabledKeycodes = new ArrayList<>();
        this.onButtonClickListener = onButtonClickListener;
        this.onButtonClickInternalListener = new OnButtonClickInternalListener();
        this.onStateChangedListener = onStateChangedListener;
        this.onButtonLongClickListener = onButtonLongClickListener;
        setOrientation(Orientation.BRV);
        addButton(android.R.drawable.ic_input_add, null);
    }

    public static int getButtonSize() {
        return DensityUtils.mpInt(BUTTON_SIZE);
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
        if(ori == oldOri){
            return;
        }

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
            setScrollView();

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
        oldOri = ori;
    }

    public View findButtonByKeyCode(int keyCode) {
        final int childCount = buttonLayouts.getChildCount();
        for (int i = 0; i < childCount; i++) {
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
        int btnSize = getButtonSize();
        if(buttonLayouts == null){
            addView(main = buttonItem);
            buttonItem.setOnLongClickListener(onButtonLongClickListener);
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
            add(buttonLayouts, buttonItem, false);
        }
    }

    private void setScrollView(){
        if(sv != null){
            sv.removeAllViews();
            removeAllViews();
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

    private void add(ViewGroup g, View v, boolean insertToBeginning){
        if(insertToBeginning){
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
        main.setBackground(getTransSelectableItemBg(getContext(), textColor));
        setColorFilter(main, textColor);

        final var childCount = buttonLayouts.getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (buttonLayouts.getChildAt(i) instanceof StatefulImageView child) {
                if ((boolean) child.getTag(R.id.key_long_press)) {
                    var stateListDrawable = new StateListDrawable();
                    stateListDrawable.addState(new int[]{android.R.attr.state_selected}, getCircleButtonBackground(keyColor, textColor, true));
                    stateListDrawable.addState(new int[]{}, getCircleButtonBackground(keyColor, textColor, false));
                    child.setSelected(false);
                    child.setBackground(stateListDrawable);

                    var colorStates = new ColorStateList(new int[][]{
                            new int[] {android.R.attr.state_selected},
                            new int[]{},
                    }, new int[] {convertARGBtoRGB(keyColor), textColor});
                    child.saveState(colorStates);
                } else {
                    child.setBackground(getCircleButtonBackground(keyColor, textColor, false));
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

    public interface OnButtonClickListener {
        void onClick(int keyCode);
    }

    private class OnButtonClickInternalListener implements OnClickListener {
        private static final int baseDelay = 50;
        private static final int degree = 225;

        private int calculateDelay(boolean collapsed, int totalAnimatedButtons, int currentIndex) {
            return (REVERSE && collapsed) || (!REVERSE && !collapsed)
                    ? (totalAnimatedButtons - (currentIndex + 1)) * baseDelay
                    : (currentIndex + 1) * baseDelay;
        }

        @Override
        public void onClick(View v){
            if (v.getTag(R.id.key_normal_press) != null) {
                onButtonClickListener.onClick((int) v.getTag(R.id.key_normal_press));
                return;
            }

            if(getChildCount() != 1){
                final boolean collapsed = main.getRotation() == 0;
                if(collapsed) sv.setVisibility(VISIBLE);

                final int childCount = buttonLayouts.getChildCount();

                final var totalAnimatedButtons = childCount - disabledKeycodes.size();
                final var animDuration = baseDelay * totalAnimatedButtons * 2;
                main.animate().setDuration(animDuration).rotation(collapsed ? (REVERSE ? -degree : degree) : 0);
                onStateChangedListener.onStateChanged(
                        collapsed ? 0 : 1,
                        collapsed ? 0 : animDuration
                );

                int disabledCount = 0;
                for(int i = 0; i < childCount; i++){
                    final var child = buttonLayouts.getChildAt(i);

                    if (disabledKeycodes.contains((int) child.getTag(R.id.key_normal_press))) {
                        disabledCount++;
                        continue;
                    }

                    final int currentIndex = i - disabledCount;
                    final int delay = calculateDelay(collapsed, totalAnimatedButtons, currentIndex);
                    final int btnSize = getButtonSize();
                    final var isFirstOrLastOneGone = currentIndex == (REVERSE ? (totalAnimatedButtons - 1) : 0);
                    if(collapsed){
                        child.animate().scaleX(1).scaleY(1).setStartDelay(delay).setListener(new SimpleAnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                child.setVisibility(View.VISIBLE);

                                if(isFirstOrLastOneGone){
                                    if(REVERSE){
                                        sv.setScrollX(totalAnimatedButtons * btnSize);
                                        sv.setScrollY(totalAnimatedButtons * btnSize);
                                    } else {
                                        sv.setScrollX(0);
                                        sv.setScrollY(0);
                                    }
                                }
                            }
                        });
                    } else {
                        child.animate().scaleX(0).scaleY(0).setStartDelay(delay).setListener(new SimpleAnimatorListener() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                child.setVisibility(View.GONE);

                                if(isFirstOrLastOneGone){
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
