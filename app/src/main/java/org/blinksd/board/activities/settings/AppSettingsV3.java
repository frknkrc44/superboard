package org.blinksd.board.activities.settings;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static org.blinksd.board.SuperBoardApplication.clearCustomFont;
import static org.blinksd.board.SuperBoardApplication.getCustomFont;
import static org.blinksd.board.SuperBoardApplication.isWatchDevice;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.blinksd.board.R;
import org.blinksd.board.views.CustomActionBar;
import org.blinksd.utils.LayoutCreator;

public class AppSettingsV3 extends SettingsCategoriesActivity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        main = LayoutCreator.createFilledVerticalLayout(FrameLayout.class, this);
        createAppBarView();
        createTabBarView();

        setKeyPrefs();
        setContentView(main);

        actionBar.setTitle(getTitle());

        mTabsHolder.getChildAt(mTabsHolder.getChildCount() - 1).requestFocus();
    }

    @SuppressWarnings({"deprecation", "all"})
    private void createAppBarView() {
        actionBar = new CustomActionBar(this, (v) -> toggleCategory(null));
        if (!isWatchDevice()) {
            actionBar.addAction(
                    R.drawable.refresh,
                    v -> System.exit(0)
            );
            actionBar.addAction(
                    R.drawable.fboard_mono,
                    v -> {
                        var imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    }
            );
        }
        main.addView(actionBar);
    }

    private void createTabBarView() {
        mTabsHolder = new FrameLayout(this);
        mTabsHolder.setLayoutParams(new LinearLayout.LayoutParams(-1, -1, 1));
        mTabsHolder.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                child.setVisibility(child.getId() == android.R.id.tabs ? VISIBLE : GONE);
                child.setAlpha(child.getId() == android.R.id.tabs ? 1 : 0);
                child.setTranslationX(child.getId() == android.R.id.tabs ? 0 : displayWidth);
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {}
        });
        main.addView(mTabsHolder);
        addCategories();
        createMainTab();
    }

    @Override
    public void restartKeyboard() {
        super.restartKeyboard();

        mTabListAdapter.notifyDataSetChanged();
    }

    @Override
    public void setKeyPrefs() {
        try {
            clearCustomFont();
            getCustomFont();
        } catch (Throwable ignored) {}
    }
}
