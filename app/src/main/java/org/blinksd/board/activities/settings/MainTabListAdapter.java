package org.blinksd.board.activities.settings;

import android.annotation.SuppressLint;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.blinksd.board.R;
import org.blinksd.utils.ColorUtils;
import org.blinksd.utils.DensityUtils;
import org.blinksd.utils.LayoutCreator;
import org.blinksd.utils.ResourcesUtils;
import org.blinksd.utils.SettingCategory;
import org.blinksd.utils.ViewUtils;

import java.util.List;

class MainTabListAdapter extends BaseAdapter {
    final List<String> categoryTranslations;
    final View.OnClickListener itemOnClickListener;
    final int pad = DensityUtils.dpInt(16);

    MainTabListAdapter(SettingsBaseActivity activity, View.OnClickListener listener) {
        categoryTranslations = activity.getArrayAsList("categories");
        itemOnClickListener = listener;
    }

    @Override
    public int getCount() {
        return SettingsBaseActivity.categoryList.size();
    }

    @Override
    public Object getItem(int position) {
        return SettingsBaseActivity.categoryList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final SettingCategory category = SettingsBaseActivity.categoryList.get(position);

        LinearLayout item = LayoutCreator.createFilledHorizontalLayout(
                parent.getClass(), parent.getContext());
        item.setTag(category);
        item.setOnClickListener(itemOnClickListener);
        item.setGravity(Gravity.CENTER_VERTICAL);
        item.setPadding(0, 0, pad, 0);
        item.getLayoutParams().height =
                (int) ResourcesUtils.getListPreferredItemHeight(parent.getContext());

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(ColorUtils.getAccentColor());

        float softCorner = DensityUtils.dp(24);
        float squareCorner = DensityUtils.dp(8);

        if (position == 0) {
            gradientDrawable.setCornerRadii(new float[]{ softCorner, softCorner, softCorner, softCorner, squareCorner, squareCorner, squareCorner, squareCorner });
        } else if (position == (SettingsBaseActivity.categoryList.size() - 1)) {
            gradientDrawable.setCornerRadii(new float[]{ squareCorner, squareCorner, squareCorner, squareCorner, softCorner, softCorner, softCorner, softCorner });
        } else {
            gradientDrawable.setCornerRadii(new float[]{ squareCorner, squareCorner, squareCorner, squareCorner, squareCorner, squareCorner, squareCorner, squareCorner });
        }

        ViewUtils.setBackground(item, gradientDrawable);

        TextView title = (TextView) LayoutInflater.from(parent.getContext()).inflate(
                android.R.layout.simple_list_item_1, item, false);
        ((LinearLayout.LayoutParams) title.getLayoutParams()).weight = 1;
        title.setText(categoryTranslations.get(position));
        item.addView(title);

        ImageView arrowView = new ImageView(parent.getContext());
        int ivSize = item.getLayoutParams().height / 2;
        arrowView.setLayoutParams(new LinearLayout.LayoutParams(ivSize, ivSize, 0));
        arrowView.setImageResource(R.drawable.arrow_right);
        ColorUtils.setColorFilter(arrowView, title.getCurrentTextColor());

        final int aPad = pad / 4;
        arrowView.setPadding(aPad, aPad, aPad, aPad);

        GradientDrawable imageViewBg = new GradientDrawable();
        imageViewBg.setColor(0x44000000);
        imageViewBg.setCornerRadius(96);
        ViewUtils.setBackground(arrowView, imageViewBg);
        item.addView(arrowView);

        return item;
    }
}
