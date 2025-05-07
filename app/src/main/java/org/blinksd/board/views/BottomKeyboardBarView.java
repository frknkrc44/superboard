package org.blinksd.board.views;

import static org.blinksd.board.SuperBoardApplication.getCurrentKeyboardLanguage;
import static org.blinksd.board.SuperBoardApplication.getKeyboardLanguageList;
import static org.blinksd.utils.DensityUtils.mpInt;
import static org.blinksd.utils.ResourcesUtils.getTransSelectableItemBg;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Space;

import org.blinksd.board.R;
import org.blinksd.utils.SettingMap;
import org.blinksd.utils.SuperDBHelper;
import org.blinksd.utils.superboard.Language;

import java.util.ArrayList;
import java.util.List;

@SuppressLint({"ViewConstructor", "InlinedApi"})
public class BottomKeyboardBarView extends LinearLayout {
    public final LanguageSelectorView languageSelectorView;
    private final SuperBoard.Key langSelectorKey;
    private final SuperBoard.Key clipboardKey;

    public BottomKeyboardBarView(SuperBoard superBoard, LanguageSelectorView.OnLanguageChangedListener listener) {
        super(superBoard.getContext());
        languageSelectorView = new LanguageSelectorView(superBoard.getContext(), listener);
        languageSelectorView.setLayoutParams(new LinearLayout.LayoutParams(-1, -1, 1));

        final int keyWidth = mpInt(12);
        final int padding = keyWidth / 2;

        setPadding(padding, 0, padding, 0);

        langSelectorKey = superBoard.createKey("");
        langSelectorKey.setLayoutParams(new LayoutParams(keyWidth, -1, 0));
        langSelectorKey.setNormalPressEvent(KeyEvent.KEYCODE_3D_MODE, true);
        langSelectorKey.setKeyIcon(R.drawable.sym_keyboard_language);
        superBoard.addExtraKey(langSelectorKey);
        addView(langSelectorKey);

        Space space = new Space(superBoard.getContext());
        space.setLayoutParams(new LayoutParams(-1, -1, 1));
        addView(space);

        clipboardKey = superBoard.createKey("");
        clipboardKey.setLayoutParams(new LayoutParams(keyWidth, -1, 0));
        clipboardKey.setNormalPressEvent(KeyEvent.KEYCODE_EISU, true);
        clipboardKey.setKeyIcon(R.drawable.clipboard);
        superBoard.addExtraKey(clipboardKey);
        addView(clipboardKey);
    }

    public void reTheme() {
        int textColor = SuperDBHelper.getIntOrDefault(SettingMap.SET_KEY_TEXTCLR);

        langSelectorKey.setBackground(getTransSelectableItemBg(getContext(), textColor));
        clipboardKey.setBackground(getTransSelectableItemBg(getContext(), textColor));

        boolean clipboardEnabled = SuperDBHelper.getBooleanOrDefault(SettingMap.SET_ENABLE_CLIPBOARD);
        clipboardKey.setVisibility(clipboardEnabled ? View.VISIBLE : View.GONE);

        boolean showBottomBar = SuperDBHelper.getBooleanOrDefault(SettingMap.SET_SHOW_BOTTOM_BAR);
        setVisibility(showBottomBar ? View.VISIBLE : View.GONE);

        languageSelectorView.reTheme(textColor);
    }

    public static final class LanguageSelectorView extends LinearLayout {
        private final OnLanguageChangedListener onLanguageChangedListener;
        private final ScrollView languageListScroller;
        private final RadioGroup radioGroup;
        private final Button okButton;

        @Override
        public void setVisibility(int visibility) {
            super.setVisibility(visibility);

            if (visibility == VISIBLE) {
                languageListScroller.scrollTo(0, 0);
            }
        }

        public LanguageSelectorView(Context context, OnLanguageChangedListener listener) {
            super(context);
            onLanguageChangedListener = listener;
            setVisibility(GONE);
            setOrientation(VERTICAL);
            int textColor = SuperDBHelper.getIntOrDefault(SettingMap.SET_KEY_TEXTCLR);

            languageListScroller = new ScrollView(context);
            languageListScroller.setLayoutParams(new LinearLayout.LayoutParams(-1, -1, 1));
            radioGroup = new RadioGroup(context);
            radioGroup.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
            languageListScroller.addView(radioGroup);

            List<Language> languageList = new ArrayList<>(getKeyboardLanguageList().values());
            for (int i = 0; i < languageList.size(); i++) {
                Language language = languageList.get(i);
                boolean checked = language.equals(getCurrentKeyboardLanguage());

                CustomRadioButton customRadioButton = new CustomRadioButton(context);
                customRadioButton.setId(i);
                customRadioButton.setText(language.label);
                customRadioButton.setTextColor(textColor);
                radioGroup.addView(customRadioButton);

                if (checked) {
                    radioGroup.check(i);
                }
            }
            addView(languageListScroller);

            okButton = new Button(context);
            LinearLayout.LayoutParams okButtonParams = new LinearLayout.LayoutParams(-2, -2, 0);
            okButtonParams.gravity = Gravity.END;
            okButton.setLayoutParams(okButtonParams);
            okButton.setBackground(getTransSelectableItemBg(context, textColor));
            okButton.setText(android.R.string.ok);
            okButton.setTextColor(textColor);
            okButton.setOnClickListener(view -> {
                int index = radioGroup.getCheckedRadioButtonId();
                Language language = languageList.get(index);
                onLanguageChangedListener.onLanguageChange(language);
            });
            addView(okButton);
        }

        private void reTheme(final int textColor) {
            final int childCount = radioGroup.getChildCount();

            for (int i = 0; i < childCount; i++) {
                CustomRadioButton customRadioButton = (CustomRadioButton) radioGroup.getChildAt(i);
                customRadioButton.setTextColor(textColor);
            }

            okButton.setTextColor(textColor);
        }

        public interface OnLanguageChangedListener {
            void onLanguageChange(Language language);
        }
    }
}
