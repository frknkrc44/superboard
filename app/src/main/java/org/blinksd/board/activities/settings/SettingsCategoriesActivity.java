package org.blinksd.board.activities.settings;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static org.blinksd.board.SuperBoardApplication.getSettings;
import static org.blinksd.board.SuperBoardApplication.getThemesCache;
import static org.blinksd.board.SuperBoardApplication.isWatchDevice;
import static org.blinksd.utils.LayoutCreator.createFilledVerticalLayout;
import static org.blinksd.utils.ResourcesUtils.getTransSelectableItemBg;
import static org.blinksd.utils.ThemeUtils.getThemeNames;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.view.RoundedCorner;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;

import org.blinksd.board.R;
import org.blinksd.utils.DensityUtils;
import org.blinksd.utils.LayoutCreator;
import org.blinksd.utils.SettingCategory;
import org.blinksd.utils.SettingMap;
import org.blinksd.utils.SettingType;
import org.blinksd.utils.SimpleAnimatorListener;
import org.blinksd.utils.SuperDBHelper;

import java.util.List;

public abstract class SettingsCategoriesActivity extends SettingsSelectorsActivity {
    void addCategories() {
        for (int i = 0; i < categoryList.size(); i++) {
            addCategoryChildren(i);
        }
    }

    void toggleCategory(SettingCategory category) {
        int currentIndex = currentCategory == null ? categoryList.size() : categoryList.indexOf(currentCategory);
        currentCategory = category;
        int newIndex = currentCategory == null ? categoryList.size() : categoryList.indexOf(currentCategory);

        if (currentIndex != newIndex) {
            actionBar.toggleBackButton(currentCategory != null);

            final var currentChild = mTabsHolder.getChildAt(currentIndex);
            final var newChild = mTabsHolder.getChildAt(newIndex);

            if (currentCategory != null && isWatchDevice() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                var insets = getWindow().getDecorView().getRootWindowInsets();
                var topLeft = insets.getRoundedCorner(RoundedCorner.POSITION_BOTTOM_LEFT);
                int pad = (int) (topLeft.getRadius() * Math.sin(Math.toRadians(45))) / 8;
                newChild.setPadding(pad, 0, pad, pad * 5);
            }

            final int animSpeed = 200;
            final TimeInterpolator interpolator = new AccelerateInterpolator();
            final SimpleAnimatorListener animatorListener = new SimpleAnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    newChild.setVisibility(VISIBLE);
                    newChild.animate()
                            .alpha(1)
                            .translationX(0)
                            .setDuration(animSpeed)
                            .setInterpolator(interpolator)
                            .setListener(null)
                            .start();
                    newChild.setScrollX(0);
                    newChild.setScrollY(0);
                    newChild.requestFocus();
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    currentChild.setVisibility(GONE);
                }
            };

            currentChild.animate()
                    .alpha(0)
                    .translationX((currentCategory == null ? 1 : -1) * displayWidth)
                    .setDuration(animSpeed)
                    .setListener(animatorListener)
                    .setInterpolator(interpolator)
                    .start();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                OnBackInvokedDispatcher dispatcher = getOnBackInvokedDispatcher();

                if (onBackAnimationCallback == null) {
                    onBackAnimationCallback = (OnBackInvokedCallback) () -> toggleCategory(null);
                }

                if (newIndex == categoryList.size()) {
                    dispatcher.unregisterOnBackInvokedCallback((OnBackInvokedCallback) onBackAnimationCallback);
                } else if (currentIndex == categoryList.size()) {
                    dispatcher.registerOnBackInvokedCallback(1, (OnBackInvokedCallback) onBackAnimationCallback);
                }
            }
        }

        actionBar.setTitle(
                currentCategory == null
                        ? getTitle()
                        : getArrayAsList("categories").get(newIndex)
        );
    }

    void createMainTab() {
        ListView listView = new ListView(this);
        listView.setId(android.R.id.tabs);
        int pad = DensityUtils.dpInt(16);

        if (listView.getDivider() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{listView.getDivider()});
                layerDrawable.getDrawable(0).setAlpha(0);
                layerDrawable.setLayerHeight(0, pad / 8);
                listView.setDivider(layerDrawable);
            } else {
                listView.getDivider().setAlpha(0);
            }
        }

        if (mTabListAdapter == null) {
            mTabListAdapter = new MainTabListAdapter(this, v -> toggleCategory((SettingCategory) v.getTag()));
        }

        listView.setSelector(new ColorDrawable(0));
        listView.setLayoutParams(LayoutCreator.createLayoutParams(mTabsHolder.getClass(), -1, -1));
        listView.setAdapter(mTabListAdapter);
        mTabsHolder.addView(listView);
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
                    switch (key) {
                        case SettingMap.SET_MONET_COLOR_SCHEME:
                            List<String> colorSchemeKeys = getArrayAsList("monet_color_schemes");
                            categoryView.addView(createRadioSelector(key, colorSchemeKeys));
                            break;
                        case SettingMap.SET_THEME_PRESET:
                            List<String> themeKeys = getThemeNames(getThemesCache());
                            categoryView.addView(createRadioSelector(key, themeKeys));
                            break;
                    }
                    break;
                case COLOR_SELECTOR:
                    categoryView.addView(createColorSelector(key));
                    break;
                case STR_SELECTOR:
                case SELECTOR:
                    List<String> selectorKeys = getArrayAsList(key);
                    categoryView.addView(createRadioSelector(key, selectorKeys));
                    break;
                case DECIMAL_NUMBER:
                case MM_DECIMAL_NUMBER:
                case FLOAT_NUMBER:
                    categoryView.addView(createNumberSelector(key, item.type == SettingType.FLOAT_NUMBER));
                    break;
            }

            categoryView.getChildAt(categoryView.getChildCount() - 1).setBackground(
                    getTransSelectableItemBg(categoryView.getContext(), 0xFFDEDEDE, true)
            );
        };

        getSettings().iterateChild(category, categoryItemIterator);
    }

    private ViewGroup getCategoryView(int categoryIndex) {
        if (mTabsHolder.getChildCount() - 1 < categoryIndex) {
            LinearLayout categoryLayout = createFilledVerticalLayout(ScrollView.class, this);
            ScrollView scrollView = new ScrollView(this);
            scrollView.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
            scrollView.addView(categoryLayout);
            mTabsHolder.addView(scrollView);
            return categoryLayout;
        }

        return (ViewGroup) mTabsHolder.getChildAt(categoryIndex);
    }

    @SuppressWarnings({"deprecation", "all"})
    @Override
    public void onBackPressed() {
        if (currentCategory != null) {
            toggleCategory(null);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void restartKeyboard() {
        super.restartKeyboard();

        // Re-apply switch dependencies
        for (int i = 0; i < categoryList.size(); i++) {
            ViewGroup categoryView = (ViewGroup) getCategoryView(i).getChildAt(0);
            final int childCount = categoryView.getChildCount();

            for (int g = 0; g < childCount; g++) {
                View item = categoryView.getChildAt(g);

                if (item instanceof Switch switchItem) {
                    String key = (String) switchItem.getTag();

                    boolean enabled = getSettings().getSwitchEnabledFromDependency(key);
                    boolean val = enabled && SuperDBHelper.getBooleanOrDefault(key);
                    switchItem.setEnabled(enabled);
                    switchItem.setOnCheckedChangeListener(null);
                    switchItem.setChecked(val);
                    switchItem.setOnCheckedChangeListener(switchListener);
                }

                if (item instanceof ViewGroup) {
                    var key = (String) item.getTag();

                    var icon = item.findViewById(android.R.id.icon);
                    if (icon instanceof ImageView imageView) {
                        int color = SuperDBHelper.getIntOrDefault(key);
                        var gradient = (GradientDrawable) imageView.getDrawable();
                        gradient.setColor(color);
                    }

                    var text1 = item.findViewById(android.R.id.text1);
                    if (text1 instanceof TextView textView) {
                        int num = SuperDBHelper.getIntOrDefault(key);
                        var isFloat = (boolean) text1.getTag(R.id.key_normal_press);

                        textView.setText(isFloat
                                ? String.valueOf(DensityUtils.getFloatNumberFromInt(num))
                                : String.valueOf(num));
                    }
                }
            }
        }
    }
}
