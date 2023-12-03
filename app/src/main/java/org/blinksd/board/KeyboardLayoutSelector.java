package org.blinksd.board;

import static org.blinksd.utils.layout.DensityUtils.mpInt;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.blinksd.SuperBoardApplication;
import org.blinksd.sdb.SuperDBHelper;
import org.blinksd.utils.layout.DensityUtils;
import org.blinksd.utils.layout.LayoutCreator;
import org.blinksd.utils.layout.LayoutUtils;
import org.blinksd.utils.sb.Language;
import org.blinksd.utils.sb.RowOptions;

import java.util.Map;
import java.util.Objects;

public class KeyboardLayoutSelector extends Activity implements View.OnClickListener {
    public static final int KEYBOARD_LAYOUT_SELECTOR_RESULT = 0xFF;
    private String currentLayout;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Map<String, Language> languageList = SuperBoardApplication.getKeyboardLanguageList();
        currentLayout = SuperDBHelper.getStringOrDefault(SettingMap.SET_KEYBOARD_LANG_SELECT);

        int m = DensityUtils.dpInt(8);

        ScrollView scroller = new ScrollView(this);
        scroller.setLayoutParams(LayoutCreator.createLayoutParams(FrameLayout.class, -1, -1));
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(m, m, m, m);
        layout.setLayoutParams(LayoutCreator.createLayoutParams(ViewGroup.class, -1, -1));
        scroller.addView(layout);

        for (String key : languageList.keySet()) {
            layout.addView(createItemLayout(Objects.requireNonNull(languageList.get(key))));
        }

        // insert the selected layout to the first index
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if (currentLayout.equals(child.getTag())) {
                layout.removeView(child);
                layout.addView(child, 0);
                break;
            }
        }

        if (Build.VERSION.SDK_INT >= 31) {
            getWindow().getDecorView().setFitsSystemWindows(true);
            scroller.setFitsSystemWindows(false);
            getWindow().setNavigationBarColor(0);
            getWindow().setStatusBarColor(0);
            getWindow().setBackgroundDrawableResource(android.R.color.system_neutral1_900);
        }

        setContentView(scroller);
    }

    private View createItemLayout(Language language) {
        FrameLayout layers = new FrameLayout(this);
        int m = DensityUtils.dpInt(8);
        FrameLayout.LayoutParams layerParams = new FrameLayout.LayoutParams(-1, -2);
        layerParams.setMargins(m, m, m, 0);
        layers.setLayoutParams(layerParams);
        layers.setTag(language.language);

        LinearLayout btn = new LinearLayout(this);
        btn.setOrientation(LinearLayout.VERTICAL);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-1, -2);
        btn.setPadding(m, m, m, m);
        btn.setLayoutParams(lp);
        layers.addView(btn);

        SuperBoardPreview preview = new SuperBoardPreview(this);
        preview.setLayoutParams(new LinearLayout.LayoutParams(-1, -1, 1));
        preview.addRows(0, LayoutUtils.getLayoutKeys(language.layout));
        LayoutUtils.setKeyOpts(language, preview);
        preview.setId(android.R.id.primary);
        preview.setKeyboardHeight(30);
        // preview.setLayoutPopup(0, LayoutUtils.getLayoutKeys(language.popup));

        for (int i = 0; i < language.layout.size(); i++) {
            RowOptions opts = language.layout.get(i);
            if (opts.enablePadding) {
                preview.setRowPadding(0, i, DensityUtils.wpInt(2));
            }
        }

        btn.setGravity(Gravity.CENTER);
        btn.addView(preview);

        TextView description = new TextView(this);
        description.setGravity(Gravity.CENTER);
        description.setLayoutParams(new LinearLayout.LayoutParams(-1, -2, 0));
        description.setText(String.format("%s - %s", language.label, language.author));
        description.setTextSize(DensityUtils.mp(1.5f));
        description.setSingleLine();
        description.setEllipsize(TextUtils.TruncateAt.END);
        btn.addView(description);

        View view = new View(this);
        view.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        view.setOnClickListener(this);
        view.setBackgroundDrawable(LayoutUtils.getSelectableItemBg(
                this,
                Color.WHITE,
                false,
                true
        ));

        boolean isSelected = currentLayout.equals(language.language);
        btn.setBackgroundDrawable(LayoutUtils.getSelectableItemBg(
                this,
                Color.WHITE,
                isSelected
        ));

        if (isSelected) {
            ImageView tick = new ImageView(this);
            int tickSize = DensityUtils.mpInt(16);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    tickSize, tickSize, Gravity.CENTER);
            params.bottomMargin = DensityUtils.dpInt(8);
            params.rightMargin = DensityUtils.dpInt(8);
            tick.setLayoutParams(params);
            int p = tickSize / 8;
            tick.setPadding(p, p, p, p);
            tick.setBackgroundDrawable(LayoutUtils.getCircleButtonBackground(false));
            tick.setScaleType(ImageView.ScaleType.FIT_CENTER);
            tick.setImageResource(R.drawable.sym_board_return);
            tick.getDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            layers.addView(tick);
        }

        layers.addView(view);
        return layers;
    }

    @Override
    public void onClick(View v) {
        String value = (String) ((View) v.getParent()).getTag();
        SuperBoardApplication.getAppDB()
                .putString(SettingMap.SET_KEYBOARD_LANG_SELECT, value, true);
        setResult(KEYBOARD_LAYOUT_SELECTOR_RESULT);
        finish();
    }

    private static class SuperBoardPreview extends SuperBoard {
        public SuperBoardPreview(Context c) {
            super(c);
            setClickable(false);
            setFocusable(false);
            setEnabled(false);
            setBackgroundColor(0);
            setKeysTextSize(mpInt(DensityUtils.getFloatNumberFromInt(SuperDBHelper.getIntOrDefault(SettingMap.SET_KEY_TEXTSIZE))));
            setKeysTextType(SuperDBHelper.getIntOrDefault(SettingMap.SET_KEYBOARD_TEXTTYPE_SELECT));
            setIconSizeMultiplier(SuperDBHelper.getIntOrDefault(SettingMap.SET_KEY_ICON_SIZE_MULTIPLIER));
            setKeysPopupPreviewEnabled(SuperDBHelper.getBooleanOrDefault(SettingMap.SET_ENABLE_POPUP_PREVIEW));
        }

        @Override
        public void sendDefaultKeyboardEvent(View v) {
            fakeKeyboardEvent((Key) v);
        }

        @Override
        public void addRows(int keyboardIndex, String[][] keys) {
            super.addRows(keyboardIndex, keys);
            setShiftState(SHIFT_ON);
        }
    }
}
