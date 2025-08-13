package org.blinksd.board.activities;

import static org.blinksd.board.SuperBoardApplication.getAppDB;
import static org.blinksd.board.SuperBoardApplication.getCurrentKeyboardLanguage;
import static org.blinksd.board.SuperBoardApplication.getDictDB;
import static org.blinksd.board.SuperBoardApplication.getKeyboardLanguageList;
import static org.blinksd.board.activities.settings.SettingsBaseActivity.doHacksAndShow;
import static org.blinksd.utils.DensityUtils.dpInt;
import static org.blinksd.utils.ResourcesUtils.getTransSelectableItemBg;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import org.blinksd.board.R;
import org.blinksd.board.activities.settings.SettingsBaseActivity;
import org.blinksd.utils.ColorUtils;
import org.blinksd.utils.LayoutCreator;

import java.util.Locale;

public class DictionaryManageActivity extends BaseActivity {
    private Intent mDictImportIntent;
    private LinearLayout mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDictImportIntent = new Intent(this, DictionaryImportActivity.class);

        ScrollView scroller = new ScrollView(this);
        scroller.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));

        mainLayout = new LinearLayout(this);
        mainLayout.setLayoutParams(new ViewGroup.LayoutParams(-1, -2));
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        scroller.addView(mainLayout);

        setContentView(scroller);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mainLayout.removeAllViews();

        addImportView();

        final var languageCodes = getDictDB().getSavedLanguageCodes();
        Log.d(getClass().getSimpleName(), String.join(", ", languageCodes));

        var currentLangCode = getCurrentKeyboardLanguage().language.split("_")[0];
        for (var code : languageCodes) {
            addLanguageItemView(currentLangCode, code);
        }
    }

    private void addImportView() {
        View childView = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, mainLayout, false);
        TextView text1 = childView.findViewById(android.R.id.text1);
        text1.setText(R.string.settings_import_dict_pack);

        childView.setBackground(getTransSelectableItemBg(this, text1.getCurrentTextColor(), true));
        childView.setOnClickListener(v -> startActivity(mDictImportIntent));

        mainLayout.addView(childView);
    }

    @SuppressWarnings({"deprecation", "all"})
    private void addLanguageItemView(String currentLangCode, String languageCode) {
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

        int tableLength = getDictDB().getTableLength(languageCode);
        TextView text2 = childTextsView.findViewById(android.R.id.text2);
        text2.setText(getResources().getQuantityString(R.plurals.settings_word_count, tableLength, tableLength));

        final int iconSize = dpInt(48);
        final int iconPadding = iconSize / 8;

        Switch enabledForSuggestions = LayoutCreator.createSwitch(
                this, SettingsBaseActivity.getTranslation("suggestions"),
                false, new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                getAppDB().putBoolean(String.format("LANG_%s_sug", languageCode), isChecked, true);
            }
        });

        boolean isSelectedLocale = currentLangCode.equals(languageCode);
        enabledForSuggestions.setEnabled(!isSelectedLocale && tableLength > 0);
        enabledForSuggestions.setChecked(isSelectedLocale || getAppDB().getBoolean(String.format("LANG_%s_sug", languageCode), false));

        var enabledForSuggestionsParams = new LinearLayout.LayoutParams(-2, iconSize, 0);
        enabledForSuggestionsParams.rightMargin = iconPadding;
        enabledForSuggestions.setLayoutParams(enabledForSuggestionsParams);

        enabledForSuggestions.setSwitchPadding(iconPadding);
        childView.addView(enabledForSuggestions);

        final String finalDisplayStr = displayStr;
        ImageView deleteIcon = new ImageView(this);
        deleteIcon.setEnabled(tableLength > 0);
        deleteIcon.setLayoutParams(new LinearLayout.LayoutParams(iconSize, iconSize, 0));
        deleteIcon.setOnClickListener(v -> showDeleteTableDialog(finalDisplayStr, languageCode));
        deleteIcon.setBackground(getTransSelectableItemBg(this, text1.getCurrentTextColor()));
        deleteIcon.setImageResource(R.drawable.delete);
        deleteIcon.setPadding(iconPadding, iconPadding, iconPadding, iconPadding);
        ColorUtils.setColorFilter(deleteIcon, text1.getTextColors().getColorForState(
                deleteIcon.isEnabled()
                        ? new int[] { android.R.attr.state_enabled }
                        : new int[] { },
                text1.getCurrentTextColor()
        ));
        childView.addView(deleteIcon);

        childView.setBackground(getTransSelectableItemBg(this, text1.getCurrentTextColor(), true));

        mainLayout.addView(childView);
    }

    void showDeleteTableDialog(String languageName, String languageCode) {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.settings_delete_dictionary_title)
                .setMessage(getString(R.string.settings_delete_dictionary_desc, languageName))
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    getDictDB().dropDatabase(languageCode);
                    dialog.dismiss();
                    recreate();
                })
                .create();

        doHacksAndShow(alertDialog);
    }
}
