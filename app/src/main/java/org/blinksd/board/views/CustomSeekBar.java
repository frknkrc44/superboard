package org.blinksd.board.views;

import android.content.Context;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import org.blinksd.board.R;
import org.blinksd.utils.ColorUtils;
import org.blinksd.utils.DensityUtils;
import org.blinksd.utils.ResourcesUtils;

import java.lang.reflect.Field;

final class CustomSeekBar extends SeekBar {

    CustomSeekBar(Context c) {
        super(c);
        setLayoutParams(new LinearLayout.LayoutParams(DensityUtils.minPInt(75), -2, 0));
        setThumb(ResourcesUtils.getDrawable(R.drawable.seekbar_thumb));
        setProgressDrawable(ResourcesUtils.getDrawable(R.drawable.seekbar));
        setSplitTrack(false);

        try {
            setProgressColor(ResourcesUtils.getColor(R.color.seekbar_progress));
        } catch (Throwable ignored) {
            setProgressColor(ColorUtils.getAccentColor());
        }
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
                @SuppressWarnings("all")
                Field clipStateField = ClipDrawable.class.getDeclaredField("mClipState");
                clipStateField.setAccessible(true);
                Object clipState = clipStateField.get(progressClip);

                // noinspection ConstantConditions
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
        final var layerLength = layers.getNumberOfLayers();

        for (int i = 0; i < layerLength; i++) {
            if (layers.getId(i) == android.R.id.progress) {
                return layers.getDrawable(i);
            }
        }

        throw new RuntimeException("No progress layer found");
    }
}
