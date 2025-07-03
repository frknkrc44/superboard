package org.blinksd.board.views;

import static org.blinksd.utils.ColorUtils.setColorFilter;
import static org.blinksd.utils.ResourcesUtils.getCircleBackground;
import static org.blinksd.utils.ResourcesUtils.getDefaultTextColor;
import static org.blinksd.utils.ResourcesUtils.getListPreferredItemHeight;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.blinksd.board.R;
import org.blinksd.board.activities.settings.SettingsBaseActivity;
import org.blinksd.utils.LayoutCreator;

import java.util.TreeMap;

@SuppressLint("ViewConstructor")
public final class ColorSelectorItemLayout extends LinearLayout {

    private final ImageView img;
    private TreeMap<Integer, Integer> colorList;

    public ColorSelectorItemLayout(Context ctx, int index, TreeMap<Integer, Integer> colors, View.OnClickListener gradientAddColorListener, View.OnClickListener gradientDelColorListener, View.OnClickListener colorSelectorListener) {
        super(ctx);
        int textColor = getDefaultTextColor();
        setLayoutParams(new LayoutParams(-1, -2));
        img = LayoutCreator.createImageView(ctx);
        int size = (int) getListPreferredItemHeight(ctx);
        img.setLayoutParams(LayoutCreator.createLayoutParams(LinearLayout.class, size, size));
        img.setScaleType(ImageView.ScaleType.FIT_CENTER);
        int pad = size / 4;
        img.setPadding(pad, pad, pad, pad);
        TextView btn = LayoutCreator.createTextView(ctx);
        LayoutParams lp = new LayoutParams(-1, -1, 1);
        btn.setLayoutParams(lp);
        btn.setId(android.R.id.text1);
        btn.setGravity(Gravity.CENTER_VERTICAL);
        btn.setTextColor(textColor);
        btn.setMinHeight(size);
        addView(img);
        addView(btn);
        setMinimumHeight(size);
        setId(index);
        switch (index) {
            case -1:
                img.setImageResource(android.R.drawable.ic_input_add);
                setColorFilter(img, textColor);
                btn.setText(SettingsBaseActivity.getTranslation("image_selector_gradient_add_item"));
                setOnClickListener(gradientAddColorListener);
                return;
            case -2:
                img.setImageResource(android.R.drawable.ic_media_next);
                setColorFilter(img, textColor);
                btn.setText(SettingsBaseActivity.getTranslation("image_selector_gradient_change_orientation"));
                setOnClickListener(gradientAddColorListener);
                return;
        }
        colorList = colors;
        int color = 0xFF000000;
        updateColorView(color);
        btn.setText(SettingsBaseActivity.getTranslation("image_selector_gradient_item"));
        ImageView del = LayoutCreator.createImageView(ctx);
        lp = new LayoutParams(size, size, 0);
        del.setLayoutParams(lp);
        del.setScaleType(img.getScaleType());
        del.setImageResource(R.drawable.delete);
        setColorFilter(del, textColor);
        pad = (int) (pad * 1.5f);
        del.setPadding(pad, pad, pad, pad);
        del.setOnClickListener(gradientDelColorListener);
        del.setId(index);
        setTag(color);
        setOnClickListener(colorSelectorListener);
        addView(del, 0);
    }

    private void updateColorView(int color) {
        img.setImageDrawable(getCircleBackground(color));
    }

    @Override
    public void setTag(Object tag) {
        super.setTag(tag);
        colorList.put(getId(), (int) tag);
        int color = (int) tag;
        updateColorView(color);
    }

}
