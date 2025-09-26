package org.blinksd.board.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;

import org.blinksd.utils.ResourcesUtils;

import java.util.ArrayList;

import thirdparty.two_way_grid_view.TwoWayGridView;

@SuppressLint("ViewConstructor")
public class GradientColorPresetsView extends TwoWayGridView {
    private final ArrayList<int[]> colorProfiles = new ArrayList<>();
    private final OnGradientItemClickListener onGradientItemClickListener;

    public GradientColorPresetsView(Context context, OnGradientItemClickListener listener) {
        super(context);

        setLayoutParams(new ViewGroup.LayoutParams(-1, -1));

        this.onGradientItemClickListener = listener;
        setScrollDirectionPortrait(SCROLL_AXIS_VERTICAL);
        setScrollDirectionLandscape(SCROLL_AXIS_VERTICAL);

        setNumColumns(3);

        // add color profiles
        colorProfiles.add(new int[] { 0xfffdb8ba, 0xffbddbff });
        colorProfiles.add(new int[] { 0xffe3e0d7, 0xffc7d5da });
        colorProfiles.add(new int[] { 0xffddc9ca, 0xffb3c4d0 });

        colorProfiles.add(new int[] { 0xffd3dae6, 0xffb1b1b1 });
        colorProfiles.add(new int[] { 0xfff7c1d3, 0xfff594b4 });
        colorProfiles.add(new int[] { 0xffffe2d6, 0xffb7e9fc });

        colorProfiles.add(new int[] { 0xffffe5a6, 0xffdcd9f5 });
        colorProfiles.add(new int[] { 0xffa8d299, 0xffe4c9d0 });
        colorProfiles.add(new int[] { 0xfff6ba90, 0xffffe5b3 });

        colorProfiles.add(new int[] { 0xff1c2159, 0xffac4e9d });
        colorProfiles.add(new int[] { 0xff54575c, 0xff000000 });
        colorProfiles.add(new int[] { 0xff554441, 0xff412723 });

        colorProfiles.add(new int[] { 0xff3c474e, 0xff2a363d });
        colorProfiles.add(new int[] { 0xff007b6d, 0xff005052 });
        colorProfiles.add(new int[] { 0xff274f80, 0xff0050a3 });

        colorProfiles.add(new int[] { 0xff335c7d, 0xff132530 });
        colorProfiles.add(new int[] { 0xff575a5f, 0xff2e3236 });
        colorProfiles.add(new int[] { 0xff635954, 0xff1c1515 });

        // add + button
        colorProfiles.add(null);

        setAdapter(new GradientColorAdapter());
    }

    public interface OnGradientItemClickListener {
        void onColorPressed(int[] scheme);
        void onAddPressed();
    }

    private class GradientColorAdapter extends BaseAdapter {
        int height = (int) (Resources.getSystem().getDisplayMetrics().density * 64);

        @Override
        public int getCount() {
            return colorProfiles.size();
        }

        @Override
        public Object getItem(int position) {
            return colorProfiles.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            int[] item = (int[]) getItem(position);
            int tintColor = ResourcesUtils.getDefaultTextColor();

            if (item == null) {
                ImageButton imageButton = new ImageButton(getContext());
                imageButton.setLayoutParams(new TwoWayGridView.LayoutParams(-1, height));
                imageButton.setBackground(ResourcesUtils.getTransSelectableItemBg(getContext(), tintColor, true));
                imageButton.setImageResource(android.R.drawable.ic_input_add);
                imageButton.setImageTintList(ColorStateList.valueOf(tintColor));
                imageButton.setOnClickListener(v -> onGradientItemClickListener.onAddPressed());
                return imageButton;
            }

            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setColors(item);
            gradientDrawable.setStroke((int) (getResources().getDisplayMetrics().density * 8), 0);
            gradientDrawable.setCornerRadius(16);
            View coloredView = new View(getContext());
            coloredView.setLayoutParams(new TwoWayGridView.LayoutParams(-1, height));
            coloredView.setBackground(gradientDrawable);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                coloredView.setForeground(ResourcesUtils.getTransSelectableItemBg(getContext(), tintColor, true));
            }

            coloredView.setOnClickListener(v -> onGradientItemClickListener.onColorPressed(item));
            return coloredView;
        }
    }
}
