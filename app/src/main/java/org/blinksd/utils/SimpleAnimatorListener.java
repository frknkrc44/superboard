package org.blinksd.utils;

import android.animation.Animator;

/**
 * Animator listener but as class
 * It's created for deleting the empty method bodies
 */
public class SimpleAnimatorListener implements Animator.AnimatorListener {
    @Override
    public void onAnimationStart(Animator animation) {}

    @Override
    public void onAnimationEnd(Animator animation) {}

    @Override
    public void onAnimationCancel(Animator animation) {}

    @Override
    public void onAnimationRepeat(Animator animation) {}
}
