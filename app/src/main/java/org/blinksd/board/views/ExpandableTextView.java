package org.blinksd.board.views;

import static android.view.View.MeasureSpec.makeMeasureSpec;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.text.TextUtils;
import android.widget.TextView;

import org.blinksd.utils.SimpleAnimatorListener;

// Inspired from https://stackoverflow.com/a/40197132
public class ExpandableTextView extends TextView {
    private static final int MAX_LINES = 1;
    private static final int ANIM_DURATION = 100;

    private int collapsedHeight = 0;
    private int expandedHeight;
    private int calculatedWidth = 0;

    public ExpandableTextView(Context context) {
        super(context);
        setMaxLines(MAX_LINES);
        setEllipsize(TextUtils.TruncateAt.MIDDLE);

        setOnClickListener(v -> {
            if (getMaxLines() != MAX_LINES) {
                createAndStartAnimation(expandedHeight, collapsedHeight, new SimpleAnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animator) {
                        setMaxLines(MAX_LINES);
                    }
                });
            } else {
                setMaxLines(Integer.MAX_VALUE);
                measure(
                        makeMeasureSpec(calculatedWidth, MeasureSpec.EXACTLY),
                        makeMeasureSpec(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
                );
                expandedHeight = getMeasuredHeight();
                createAndStartAnimation(collapsedHeight, expandedHeight, null);
            }
        });
    }

    void createAndStartAnimation(int sourceHeight, int targetHeight, SimpleAnimatorListener listener) {
        ObjectAnimator animation = ObjectAnimator.ofInt(this, "height", sourceHeight, targetHeight);
        if (listener != null) animation.addListener(listener);
        animation.setDuration(ANIM_DURATION).start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (collapsedHeight == 0) {
            collapsedHeight = getMeasuredHeight();
            calculatedWidth = getMeasuredWidth();
        }
    }
}