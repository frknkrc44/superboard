package org.blinksd.board.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.widget.ImageView;

public class StatefulImageView extends ImageView {
    private ColorStateList iconStates;

    public StatefulImageView(Context context) {
        super(context);
    }

    public void saveState(ColorStateList iconStates) {
        this.iconStates = iconStates;
        setSelected(isSelected());
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        if (iconStates != null) {
            setColorFilter(iconStates.getColorForState(
                    selected
                            ? new int[]{android.R.attr.state_selected}
                            : new int[]{},
                    0)
            );
        }
    }
}
