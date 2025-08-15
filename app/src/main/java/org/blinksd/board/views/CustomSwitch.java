package org.blinksd.board.views;

import android.content.Context;
import android.widget.Switch;

import org.blinksd.board.R;

public class CustomSwitch extends Switch {
    public CustomSwitch(Context context) {
        super(context);

        setTextOff("");
        setTextOn("");

        setThumbResource(R.drawable.switch_thumb);
        setTrackResource(R.drawable.switch_track);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        final int alpha = enabled ? 0xFF : 0x88;
        getThumbDrawable().setAlpha(alpha);
        getTrackDrawable().setAlpha(alpha);
    }
}
