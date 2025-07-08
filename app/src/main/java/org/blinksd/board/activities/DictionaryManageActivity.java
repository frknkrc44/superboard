package org.blinksd.board.activities;

import static org.blinksd.board.SuperBoardApplication.getDictDB;
import static org.blinksd.board.SuperBoardApplication.getKeyboardLanguageList;
import static org.blinksd.utils.DensityUtils.dpInt;
import static org.blinksd.utils.ResourcesUtils.getTransSelectableItemBg;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.blinksd.board.R;
import org.blinksd.utils.ColorUtils;

import java.util.Locale;

public class DictionaryManageActivity extends BaseActivity {
    private Intent mDictImportIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDictImportIntent = new Intent(this, DictionaryImportActivity.class);

        ScrollView scroller = new ScrollView(this);
        scroller.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setLayoutParams(new ViewGroup.LayoutParams(-1, -2));
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        scroller.addView(mainLayout);

        createImportView(mainLayout);

        final var languageCodes = getDictDB().getSavedLanguageCodes();
        Log.d(getClass().getSimpleName(), String.join(", ", languageCodes));

        for (var code : languageCodes) {
            createChildView(mainLayout, code);
        }

        setContentView(scroller);
    }

    private void createImportView(ViewGroup rootView) {
        View childView = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, rootView, false);
        TextView text1 = childView.findViewById(android.R.id.text1);
        text1.setText(R.string.settings_import_dict_pack);

        childView.setBackground(getTransSelectableItemBg(this, text1.getCurrentTextColor(), true));
        childView.setOnClickListener(v -> startActivity(mDictImportIntent));

        rootView.addView(childView);
    }

    private void createChildView(ViewGroup rootView, String languageCode) {
        LinearLayout childView = new LinearLayout(this);
        childView.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        childView.setPadding(0, 0, dpInt(16), 0);

        View childTextsView = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, childView, false);
        ((LinearLayout.LayoutParams) childTextsView.getLayoutParams()).weight = 1;
        childView.addView(childTextsView);

        TextView text1 = childTextsView.findViewById(android.R.id.text1);
        text1.setTextDirection(View.TEXT_DIRECTION_LTR);
        text1.getLayoutParams().width = -2;

        Locale locale = new Locale(languageCode);
        String displayStr = locale.getDisplayLanguage(locale);
        if (displayStr.equals(languageCode)) {
            final var langTypes = getKeyboardLanguageList();
            for (var type : langTypes.values()) {
                if (type.name.equals(languageCode)) {
                    displayStr = type.label;
                    break;
                }
            }
        }

        text1.setText(displayStr);

        TextView text2 = childTextsView.findViewById(android.R.id.text2);
        text2.setText(String.valueOf(getDictDB().getTableLength(languageCode)));

        final int iconSize = dpInt(48);
        final int iconPadding = iconSize / 8;
        ImageView deleteIcon = new ImageView(this);
        deleteIcon.setLayoutParams(new LinearLayout.LayoutParams(iconSize, iconSize, 0));
        deleteIcon.setOnClickListener(v -> Toast.makeText(this, "Not implemented yet", Toast.LENGTH_SHORT).show());
        deleteIcon.setBackground(getTransSelectableItemBg(this, text1.getCurrentTextColor()));
        deleteIcon.setImageResource(R.drawable.delete);
        deleteIcon.setPadding(iconPadding, iconPadding, iconPadding, iconPadding);
        ColorUtils.setColorFilter(deleteIcon, text1.getCurrentTextColor());

        childView.addView(deleteIcon);

        childView.setBackground(getTransSelectableItemBg(this, text1.getCurrentTextColor(), true));

        rootView.addView(childView);
    }
}
