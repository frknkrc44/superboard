package org.blinksd.board.views;

import static android.view.View.MeasureSpec.makeMeasureSpec;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.widget.TextView;

import org.blinksd.utils.SimpleAnimatorListener;

// Inspired from https://stackoverflow.com/a/40197132
public class ExpandableTextView extends TextView {
    private static final int MAX_LINES = 3;

    private int collapsedHeight = 0;
    private int expandedHeight;

    public ExpandableTextView(Context context) {
        super(context);
        setMaxLines(MAX_LINES);

        setOnClickListener(v -> {
            if (getLineCount() < MAX_LINES) {
                return;
            }

            if (getMaxLines() == MAX_LINES) {
                setMaxLines(Integer.MAX_VALUE);
                measure(
                        makeMeasureSpec(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED),
                        makeMeasureSpec(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
                );
                expandedHeight = getMeasuredHeight();
                ObjectAnimator animation = ObjectAnimator.ofInt(this, "height", collapsedHeight, expandedHeight);
                animation.setDuration(250).start();
            } else {
                ObjectAnimator animation = ObjectAnimator.ofInt(this, "height", expandedHeight, collapsedHeight);
                animation.addListener(new SimpleAnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animator) {
                        setMaxLines(MAX_LINES);
                    }
                });
                animation.setDuration(250).start();
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (collapsedHeight == 0) {
            collapsedHeight = getMeasuredHeight();
        }
    }
}