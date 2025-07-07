package org.blinksd.board.views.emoji;

import static org.blinksd.utils.ColorUtils.setColorFilter;
import static org.blinksd.utils.ResourcesUtils.getTransSelectableItemBg;
import static org.blinksd.utils.SuperDBHelper.getBooleanOrDefault;
import static org.blinksd.utils.SuperDBHelper.getFloatPercentOrDefault;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.blinksd.board.R;
import org.blinksd.board.views.SuperBoard;
import org.blinksd.utils.SettingMap;

import java.util.Map;

import thirdparty.android.widget.TabHost;
import thirdparty.android.widget.TabWidget;

@SuppressWarnings("deprecation")
@SuppressLint("ViewConstructor")
public class EmojiViewV2 extends LinearLayout {
    private final Map<String, EmojiList> emojiListMap;
    private final String[] emojiListKeyArray;
    private final EmojiCategoryViewV2.OnEmojiClickListener onEmojiClickListener;
    private final View.OnClickListener onActionsClickListener;

    private final TabHost tabHost;
    private final TabWidget tabWidget;
    private final FrameLayout tabContentHolder;
    private final LinearLayout tabWidgetHolder;

    private int currentTab = 0;
    private int keyTextColor;

    public EmojiViewV2(
            Context context,
            EmojiCategoryViewV2.OnEmojiClickListener onEmojiClickListener,
            View.OnClickListener onActionsClickListener
    ) {
        super(context);
        keyTextColor = Color.WHITE;
        this.onEmojiClickListener = onEmojiClickListener;
        this.onActionsClickListener = onActionsClickListener;
        setOrientation(VERTICAL);

        tabHost = new TabHost(getContext());
        tabWidget = new TabWidget(getContext());
        tabContentHolder = new FrameLayout(getContext());
        tabWidgetHolder = new LinearLayout(getContext());

        emojiListMap = EmojiListParser.parseEmojiJson();
        emojiListKeyArray = emojiListMap.keySet().toArray(new String[0]);

        final int padding = getFloatPercentOrDefault(SettingMap.SET_KEYBOARD_PADDING);
        setPadding(padding, 0, padding, 0);

        setupTabs();
    }

    public void applyTheme(SuperBoard sb) {
        keyTextColor = sb.getKeysTextColor();

        final int tabWidgetHolderChildCount = tabWidgetHolder.getChildCount();
        for (int i = 0; i < tabWidgetHolderChildCount; i++) {
            final var child = tabWidgetHolder.getChildAt(i);
            if (child instanceof ImageButton imageButton) {
                setColorFilter(imageButton, keyTextColor);
            }
        }

        final int tabWidgetChildCount = tabWidget.getChildCount();
        for (int i = 0; i < tabWidgetChildCount; i++) {
            final var indicatorView = (TextView) tabWidget.getChildTabViewAt(i);
            indicatorView.setTextColor(keyTextColor);
            setColorFilter(indicatorView.getBackground(), keyTextColor);

            final var emojiCategoryView = (EmojiCategoryViewV2) tabHost.getTabView(i);
            final var scrollVerticalPortrait = getBooleanOrDefault(SettingMap.SET_EMOJI_USE_VERTICAL_SCROLL_PORTRAIT);
            final var scrollVerticalLandscape = getBooleanOrDefault(SettingMap.SET_EMOJI_USE_VERTICAL_SCROLL_LANDSCAPE);
            emojiCategoryView.setScrollDirectionPortrait(scrollVerticalPortrait ? SCROLL_AXIS_VERTICAL : SCROLL_AXIS_HORIZONTAL);
            emojiCategoryView.setScrollDirectionLandscape(scrollVerticalLandscape ? SCROLL_AXIS_VERTICAL : SCROLL_AXIS_HORIZONTAL);
            emojiCategoryView.setVisibility(i == tabHost.getCurrentTab() ? View.VISIBLE : View.GONE);

            final var emojiChildCount = emojiCategoryView.getChildCount();
            for (int j = 0; j < emojiChildCount; j++) {
                final var itemView = (TextView) emojiCategoryView.getChildAt(j);
                itemView.setTextColor(keyTextColor);
                itemView.setBackground(getTransSelectableItemBg(getContext(), keyTextColor));
            }
        }
    }

    private void setupTabs() {
        tabHost.setLayoutParams(new LayoutParams(-1, -2, 1));

        tabWidget.setId(android.R.id.tabs);
        tabWidget.setLayoutParams(new LayoutParams(-1, -1, 1));

        tabContentHolder.setLayoutParams(new LayoutParams(-1, -1));
        tabContentHolder.setId(android.R.id.tabcontent);

        tabHost.setOnTabChangedListener(tabId -> {
            tabContentHolder.findViewWithTag(tabId).scrollTo(0, 0);
            tabWidget.getChildTabViewAt(currentTab).setSelected(false);
            currentTab = tabHost.getCurrentTab();
            tabWidget.getChildTabViewAt(currentTab).setSelected(true);
        });

        int length = getResources().getDisplayMetrics().widthPixels / emojiListKeyArray.length;
        tabWidgetHolder.setLayoutParams(new LayoutParams(-1, -2));
        tabWidgetHolder.addView(categoryItem(-1, length));
        tabWidgetHolder.addView(tabWidget);
        tabWidgetHolder.addView(categoryItem(10, length));


        var mainHolder = new LinearLayout(getContext());
        mainHolder.setOrientation(VERTICAL);
        mainHolder.setLayoutParams(new LayoutParams(-1, -1));
        mainHolder.addView(tabWidgetHolder);
        mainHolder.addView(tabContentHolder);

        tabHost.addView(mainHolder);
        tabHost.setup();

        for (String emojiItem : emojiListKeyArray) {
            var emojiList = emojiListMap.get(emojiItem);

            // noinspection ConstantConditions
            if (emojiList.isEmpty()) {
                continue;
            }

            // noinspection ConstantConditions
            var tabSpec = tabHost.newTabSpec(emojiList.get(0).emoji);

            var indicatorView = (TextView) LayoutInflater.from(getContext())
                    .inflate(android.R.layout.simple_list_item_1, tabWidget, false);
            indicatorView.setLayoutParams(new LayoutParams(-1, -2, 1));
            indicatorView.setText(tabSpec.getTag().trim());
            indicatorView.setTextColor(keyTextColor);
            indicatorView.setGravity(Gravity.CENTER);
            indicatorView.setPadding(0, 0, 0, 0);
            indicatorView.setTextSize(TypedValue.COMPLEX_UNIT_PX, length / 2f);

            tabSpec.setIndicator(indicatorView);

            indicatorView.setBackgroundResource(R.drawable.tab_indicator_material);
            setColorFilter(indicatorView.getBackground(), keyTextColor);

            tabSpec.setContent(p1 -> {
                final var view = new EmojiCategoryViewV2(getContext(), emojiList, onEmojiClickListener);
                view.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
                view.setTag(p1);
                return view;
            });

            tabHost.addTab(tabSpec);
        }

        addView(tabHost);
    }

    private View categoryItem(int num, int size) {
        ImageButton imageButton = new ImageButton(getContext());
        imageButton.setBackgroundDrawable(getTransSelectableItemBg(getContext(), keyTextColor));
        imageButton.setLayoutParams(new LayoutParams(size, size, 0));
        imageButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageButton.setAdjustViewBounds(true);
        int p = size / 4;
        imageButton.setPadding(p, p, p, p);
        imageButton.setImageResource(num == -1 ? R.drawable.arrow_left : R.drawable.sym_keyboard_delete);
        setColorFilter(imageButton, keyTextColor);
        imageButton.setTag(num);
        imageButton.setOnClickListener(onActionsClickListener);
        return imageButton;
    }
}
