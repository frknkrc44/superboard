package org.blinksd.board.activities.settings;

import static org.blinksd.board.SuperBoardApplication.getSettings;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;

import org.blinksd.board.R;
import org.blinksd.board.SuperBoardApplication;
import org.blinksd.board.views.SuperTab;
import org.blinksd.utils.LayoutCreator;
import org.blinksd.utils.LayoutUtils;
import org.blinksd.utils.SettingCategory;
import org.blinksd.utils.SettingMap;
import org.blinksd.utils.SettingType;
import org.blinksd.utils.SuperDBHelper;
import org.blinksd.utils.ThemeUtils;
import org.blinksd.utils.ViewUtils;

import java.util.List;

public abstract class SettingsCategoriesActivity extends SettingsSelectorsActivity {
    void addCategories() {
        for (int i = 0; i < SettingCategory.values().length; i++) {
            addCategoryChildren(i);
            superTab.addButton(getCategoryIconResource(i));
        }
    }

    private int getCategoryIconResource(int categoryIndex) {
        switch (categoryIndex) {
            case 0:
                return R.drawable.more_control;
            case 1:
                return R.drawable.keyboard;
            case 2:
                return R.drawable.view_compact_alt;
            case 3:
                return R.drawable.web_asset;
            case 4:
                return R.drawable.web_asset_reversed;
            case 5:
                return R.drawable.brush;
            case 6:
                return R.drawable.format_paint;
        }

        return R.drawable.arrow_right;
    }

    private void addCategoryChildren(int categoryIndex) {
        SettingCategory category = SettingCategory.values()[categoryIndex];
        ViewGroup categoryView = getCategoryView(categoryIndex);

        SettingMap.ChildIterator categoryItemIterator = (key, item) -> {
            switch (item.type) {
                case REDIRECT:
                    categoryView.addView(createRedirect(key));
                    break;
                case BOOL:
                    categoryView.addView(createBoolSelector(key));
                    break;
                case IMAGE:
                    categoryView.addView(createImageSelector(key));
                    break;
                case THEME_SELECTOR:
                    List<String> themeKeys = ThemeUtils.getThemeNames(SuperBoardApplication.getThemes());
                    categoryView.addView(createRadioSelector(key, themeKeys));
                    break;
                case COLOR_SELECTOR:
                    categoryView.addView(createColorSelector(key));
                    break;
                case STR_SELECTOR:
                case SELECTOR:
                    if (SettingMap.SET_KEYBOARD_LANG_SELECT.equals(key)) {
                        List<String> keySet = SuperBoardApplication.getLanguageHRNames();
                        categoryView.addView(createRadioSelector(key, keySet));
                        return;
                    }

                    List<String> selectorKeys = getArrayAsList(key);
                    categoryView.addView(createRadioSelector(key, selectorKeys));
                    break;
                case DECIMAL_NUMBER:
                case MM_DECIMAL_NUMBER:
                case FLOAT_NUMBER:
                    categoryView.addView(createNumberSelector(key, item.type == SettingType.FLOAT_NUMBER));
                    break;
            }

            ViewUtils.setBackground(
                    categoryView.getChildAt(categoryView.getChildCount() - 1),
                    LayoutUtils.getTransSelectableItemBg(
                            categoryView.getContext(), 0xFFDEDEDE)
            );
        };

        getSettings().iterChild(category, categoryItemIterator);
    }

    final SuperTab.OnTabChangedListener onTabChangedListener =
            index -> actionBar.setTitle(getArrayAsList("categories").get(index));

    private ViewGroup getCategoryView(int categoryIndex) {
        if (mTabsHolder.getChildCount() - 1 < categoryIndex) {
            LinearLayout categoryLayout = LayoutCreator.createFilledVerticalLayout(ScrollView.class, this);
            ScrollView scrollView = new ScrollView(this);
            scrollView.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
            scrollView.addView(categoryLayout);
            mTabsHolder.addView(scrollView);
            return categoryLayout;
        }

        return (ViewGroup) mTabsHolder.getChildAt(categoryIndex);
    }

    @Override
    public void restartKeyboard() {
        super.restartKeyboard();

        // Re-apply switch dependencies
        for (int i = 0; i < mTabsHolder.getChildCount(); i++) {
            ViewGroup categoryView = (ViewGroup) getCategoryView(i).getChildAt(0);

            for (int g = 0; g < categoryView.getChildCount(); g++) {
                View item = categoryView.getChildAt(g);

                if (item instanceof Switch) {
                    Switch swtch = (Switch) item;
                    String key = (String) swtch.getTag();

                    boolean enabled = getSettings().getSwitchEnabledFromDependency(key);
                    boolean val = enabled && SuperDBHelper.getBooleanOrDefault(key);
                    swtch.setEnabled(enabled);
                    swtch.setOnCheckedChangeListener(null);
                    swtch.setChecked(val);
                    swtch.setOnCheckedChangeListener(switchListener);
                }
            }
        }
    }
}
