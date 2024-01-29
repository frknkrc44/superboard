package org.blinksd.board.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.blinksd.board.R;
import org.blinksd.board.SuperBoardApplication;
import org.blinksd.board.views.SuperBoard;
import org.blinksd.utils.DensityUtils;
import org.blinksd.utils.LayoutCreator;
import org.blinksd.utils.LayoutUtils;
import org.blinksd.utils.SettingMap;
import org.blinksd.utils.SuperDBHelper;
import org.blinksd.utils.TextUtilsCompat;

public class FontSelector extends Activity implements View.OnClickListener {
    public static final int FONT_SELECTOR_RESULT = 0xFF;
    private String[] fontTypeTranslations;
    private int currentFont;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fontTypeTranslations = getResources().getStringArray(R.array.settings_keyboard_texttype_select);
        currentFont = SuperDBHelper.getIntOrDefault(SettingMap.SET_KEYBOARD_TEXTTYPE_SELECT);
        View main = createMainLayout();

        if (Build.VERSION.SDK_INT >= 31) {
            getWindow().getDecorView().setFitsSystemWindows(true);
            main.setFitsSystemWindows(false);
            getWindow().setNavigationBarColor(0);
            getWindow().setStatusBarColor(0);
            getWindow().setBackgroundDrawableResource(android.R.color.system_neutral1_900);
        }

        setContentView(main);
    }

    private View createMainLayout() {
        ScrollView scroller = new ScrollView(this);
        scroller.setLayoutParams(LayoutCreator.createLayoutParams(FrameLayout.class, -1, -1));
        SuperBoard.TextType[] values = SuperBoard.TextType.values();
        int rowCount = 3;
        int columnCount = values.length / rowCount;
        LinearLayout main = LayoutCreator.createVerticalLayout(this);
        for (int i = 0; i < columnCount; i++) {
            LinearLayout hor = LayoutCreator.createHorizontalLayout(this);
            for (int j = 0; j < rowCount; j++) {
                hor.addView(createFontItemLayout(
                        values[(i * rowCount) + j], i, j, rowCount, columnCount));
            }
            main.addView(hor);
        }
        scroller.addView(main);
        return scroller;
    }

    @SuppressLint("SetTextI18n")
    private View createFontItemLayout(SuperBoard.TextType value, int i, int j, int rowCount, int columnCount) {
        int currentIndex = (i * rowCount) + j;
        LinearLayout btn = new LinearLayout(this);
        btn.setOrientation(LinearLayout.VERTICAL);
        int size = DensityUtils.wpInt(33);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size, 1);
        int m = DensityUtils.dpInt(8);
        lp.setMargins(m, m, j == rowCount - 1 ? m : 0, i == columnCount - 1 ? m : 0);
        btn.setPadding(m, m, m, m);
        btn.setLayoutParams(lp);
        TextView textView = new TextView(this);
        textView.setLayoutParams(new LinearLayout.LayoutParams(-1, -1, 1));
        textView.setGravity(Gravity.CENTER);
        TextUtilsCompat.setTypefaceFromTextType(textView, value);
        textView.setText("A1#");
        textView.setTextSize(DensityUtils.mp(4));
        btn.setGravity(Gravity.CENTER);
        btn.addView(textView);
        TextView description = new TextView(this);
        description.setGravity(Gravity.CENTER);
        description.setLayoutParams(new LinearLayout.LayoutParams(-1, -2, 0));
        description.setText(fontTypeTranslations[currentIndex]);
        description.setTextSize(DensityUtils.mp(1.5f));
        description.setSingleLine();
        description.setEllipsize(TextUtils.TruncateAt.END);
        btn.addView(description);
        btn.setBackgroundDrawable(LayoutUtils.getSelectableItemBg(
                this,
                textView.getCurrentTextColor(),
                currentFont == currentIndex
        ));
        btn.setTag(currentIndex);
        btn.setOnClickListener(this);
        return btn;
    }

    @Override
    public void onClick(View v) {
        SuperBoardApplication.getAppDB().putInteger(
                SettingMap.SET_KEYBOARD_TEXTTYPE_SELECT, (int) v.getTag(), true);
        setResult(FONT_SELECTOR_RESULT);
        finish();
    }
}
