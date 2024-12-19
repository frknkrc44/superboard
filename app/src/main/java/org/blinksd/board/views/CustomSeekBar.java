package org.blinksd.board.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Build;
import android.widget.AbsSeekBar;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import org.blinksd.board.R;
import org.blinksd.utils.DensityUtils;
import org.blinksd.utils.DrawableUtils;
import org.blinksd.utils.ResourcesUtils;

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
                Object clipState = ClipDrawable.class.getDeclaredField("mClipState").get(progressClip);
                progress = (GradientDrawable) clipState.getClass().getDeclaredField("mDrawable").get(clipState);
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
