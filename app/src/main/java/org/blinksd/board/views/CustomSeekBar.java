package org.blinksd.board.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.widget.AbsSeekBar;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import org.blinksd.board.R;
import org.blinksd.utils.DensityUtils;

import java.lang.reflect.Field;

final class CustomSeekBar extends SeekBar {
    CustomSeekBar(Context c) {
        super(c);
        setLayoutParams(new LinearLayout.LayoutParams(DensityUtils.mpInt(50), -2, 0));
        setThumb(c.getResources().getDrawable(R.drawable.seekbar_thumb));
        setProgressDrawable(c.getResources().getDrawable(R.drawable.seekbar));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setSplitTrack(false);
        }
        setProgressColor(getResources().getColor(R.color.seekbar_progress));
    }

    /** @noinspection JavaReflectionMemberAccess*/
    @SuppressLint("DiscouragedPrivateApi")
    public Drawable getThumb() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            return super.getThumb();

        try {
            Field thumb = AbsSeekBar.class.getDeclaredField("mThumb");
            thumb.setAccessible(true);
            return (Drawable) thumb.get(this);
        } catch (Throwable ignored) {}

        return null;
    }

    public void setProgressColor(int color) {
        setThumbStrokeColor(color);
        setTrackProgressColor(color);
    }

    private void setThumbStrokeColor(int color) {
        GradientDrawable thumb = (GradientDrawable) getThumb();
        thumb.setStroke(DensityUtils.dpInt(2), color);
    }

    private void setTrackProgressColor(int color) {
        LayerDrawable layers = (LayerDrawable) getProgressDrawable();
        ClipDrawable progressClip = (ClipDrawable) findProgressLayerById(layers);

        GradientDrawable progress = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            progress = (GradientDrawable) progressClip.getDrawable();
        } else {
            try {
                Field clipStateField = ClipDrawable.class.getDeclaredField("mClipState");
                clipStateField.setAccessible(true);
                Object clipState = clipStateField.get(progressClip);
                Field clipMDrawableField = clipState.getClass().getDeclaredField("mDrawable");
                clipMDrawableField.setAccessible(true);
                progress = (GradientDrawable) clipMDrawableField.get(clipState);
            } catch (Throwable ignore) {}
        }

        if (progress != null) {
            progress.setColor(color);
        }
    }

    private Drawable findProgressLayerById(LayerDrawable layers) {
        for (int i = 0; i < layers.getNumberOfLayers(); i++) {
            if (layers.getId(i) == android.R.id.progress) {
                return layers.getDrawable(i);
            }
        }

        throw new RuntimeException("No progress layer found");
    }
}
