package org.blinksd.board.views;

import android.content.Context;
import android.text.TextUtils;
import android.widget.TextView;

// Inspired from https://stackoverflow.com/a/40197132
public class ExpandableTextView extends TextView {
    private static final int MAX_LINES = 1;

/*
    private static final int ANIM_DURATION = 100;

    private int collapsedHeight = 0;
    private int expandedHeight = 0;
    private StaticLayout staticLayout;
*/

    public ExpandableTextView(Context context) {
        super(context);
        setMaxLines(MAX_LINES);
        setEllipsize(TextUtils.TruncateAt.END);

        setOnClickListener(v -> {
            toggleNonAnimated();

            // toggleAnimated();
        });
    }

    private void toggleNonAnimated() {
        setMaxLines(getMaxLines() != MAX_LINES ? MAX_LINES : Integer.MAX_VALUE);
    }

/*
    private void toggleAnimated() {
        generateStaticLayout();

        if (calculateLineCount() <= MAX_LINES) {
            return;
        }

        if (getMaxLines() != MAX_LINES) {
            createAndStartAnimation(expandedHeight, collapsedHeight, new SimpleAnimatorListener() {
                @Override
                public void onAnimationEnd(Animator animator) {
                    setMaxLines(MAX_LINES);
                }
            });
        } else {
            setMaxLines(Integer.MAX_VALUE);
            expandedHeight = calculateHeight();
            createAndStartAnimation(collapsedHeight, expandedHeight, null);
        }
    }

    private void createAndStartAnimation(int sourceHeight, int targetHeight, SimpleAnimatorListener listener) {
        ObjectAnimator animation = ObjectAnimator.ofInt(this, "height", sourceHeight, targetHeight);
        if (listener != null) animation.addListener(listener);
        animation.setDuration(ANIM_DURATION).start();
    }

    @SuppressWarnings({"deprecation", "all"})
    private void generateStaticLayout() {
        staticLayout = new StaticLayout(getText(), getPaint(), getMeasuredWidth(),
                Layout.Alignment.ALIGN_CENTER, getLineSpacingMultiplier(), getLineSpacingExtra(),
                getIncludeFontPadding());
    }

    private int calculateHeight() {
        return staticLayout.getHeight() + (staticLayout.getHeight() / staticLayout.getLineCount());
    }

    private int calculateLineCount() {
        return staticLayout.getLineCount();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (collapsedHeight == 0) {
            collapsedHeight = getMeasuredHeight();
        }

        staticLayout = null;
    }
 */
}