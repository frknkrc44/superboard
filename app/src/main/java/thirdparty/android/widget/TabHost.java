/*
 * A modified version of the TabHost for recent Android versions
 *
 * Copyright 2025 frknkrc44
 */

/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package thirdparty.android.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.KeyEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Container for a tabbed window view. This object holds two children: a set of tab labels that the
 * user clicks to select a specific tab, and a FrameLayout object that displays the contents of that
 * page. The individual elements are typically controlled using this container object, rather than
 * setting values on the child elements themselves.
 */
public class TabHost extends FrameLayout implements ViewTreeObserver.OnTouchModeChangeListener {

    private static final int TABWIDGET_LOCATION_LEFT = 0;
    private static final int TABWIDGET_LOCATION_TOP = 1;
    private static final int TABWIDGET_LOCATION_RIGHT = 2;
    private static final int TABWIDGET_LOCATION_BOTTOM = 3;
    private TabWidget mTabWidget;
    private FrameLayout mTabContent;
    private final List<TabSpec> mTabSpecs = new ArrayList<TabSpec>(2);
    int mCurrentTab = -1;
    private View mCurrentView = null;
    private OnTabChangeListener mOnTabChangeListener;
    private OnKeyListener mTabKeyListener;

    public TabHost(Context context) {
        super(context);
        initTabHost();
    }

    private void initTabHost() {
        setFocusableInTouchMode(true);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);

        mCurrentTab = -1;
        mCurrentView = null;
    }

    /**
     * Creates a new {@link TabSpec} associated with this tab host.
     *
     * @param tag tag for the tab specification, must be non-null
     * @throws IllegalArgumentException If the passed tag is null
     */
    public TabSpec newTabSpec(String tag) {
        if (tag == null) {
            throw new IllegalArgumentException("tag must be non-null");
        }
        return new TabSpec(tag);
    }



    /**
      * <p>Call setup() before adding tabs if loading TabHost using findViewById().
      * <i><b>However</i></b>: You do not need to call setup() after getTabHost()
      * in {link android.app.TabActivity TabActivity}.
      * Example:</p>
<pre>mTabHost = (TabHost)findViewById(R.id.tabhost);
mTabHost.setup();
mTabHost.addTab(TAB_TAG_1, "Hello, world!", "Tab 1");
      */
    public void setup() {
        mTabWidget = findViewById(android.R.id.tabs);
        if (mTabWidget == null) {
            throw new RuntimeException(
                    "Your TabHost must have a TabWidget whose id attribute is 'android.R.id.tabs'");
        }

        // KeyListener to attach to all tabs. Detects non-navigation keys
        // and relays them to the tab content.
        mTabKeyListener = (v, keyCode, event) -> {
            if (KeyEvent.isModifierKey(keyCode)) {
                return false;
            }
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_DPAD_LEFT:
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                case KeyEvent.KEYCODE_DPAD_UP:
                case KeyEvent.KEYCODE_DPAD_DOWN:
                case KeyEvent.KEYCODE_TAB:
                case KeyEvent.KEYCODE_SPACE:
                case KeyEvent.KEYCODE_ENTER:
                    return false;

            }
            mTabContent.requestFocus(View.FOCUS_FORWARD);
            return mTabContent.dispatchKeyEvent(event);
        };

        mTabWidget.setTabSelectionListener((tabIndex, clicked) -> {
            setCurrentTab(tabIndex);
            if (clicked) {
                mTabContent.requestFocus(View.FOCUS_FORWARD);
            }
        });

        mTabContent = findViewById(android.R.id.tabcontent);
        if (mTabContent == null) {
            throw new RuntimeException(
                    "Your TabHost must have a FrameLayout whose id attribute is "
                            + "'android.R.id.tabcontent'");
        }
    }

    @Override
    public void onTouchModeChanged(boolean isInTouchMode) {
        // No longer used, but kept to maintain API compatibility.
    }

    /**
     * Add a tab.
     * @param tabSpec Specifies how to create the indicator and content.
     * @throws IllegalArgumentException If the passed tab spec has null indicator strategy and / or
     *      null content strategy.
     */
    public void addTab(TabSpec tabSpec) {

        if (tabSpec.mIndicatorStrategy == null) {
            throw new IllegalArgumentException("you must specify a way to create the tab indicator.");
        }

        if (tabSpec.mContentStrategy == null) {
            throw new IllegalArgumentException("you must specify a way to create the tab content");
        }
        View tabIndicator = tabSpec.mIndicatorStrategy.createIndicatorView();
        tabIndicator.setOnKeyListener(mTabKeyListener);

        // If this is a custom view, then do not draw the bottom strips for
        // the tab indicators.
        if (tabSpec.mIndicatorStrategy instanceof ViewIndicatorStrategy) {
            mTabWidget.setStripEnabled(false);
        }

        mTabWidget.addView(tabIndicator);
        mTabSpecs.add(tabSpec);

        if (mCurrentTab == -1) {
            setCurrentTab(0);
        }
    }


    /**
     * Removes all tabs from the tab widget associated with this tab host.
     */
    public void clearAllTabs() {
        mTabWidget.removeAllViews();
        initTabHost();
        mTabContent.removeAllViews();
        mTabSpecs.clear();
        requestLayout();
        invalidate();
    }

    public TabWidget getTabWidget() {
        return mTabWidget;
    }

    /**
     * Returns the current tab.
     *
     * @return the current tab, may be {@code null} if no tab is set as current
     */
    public int getCurrentTab() {
        return mCurrentTab;
    }

    /**
     * Returns the tag for the current tab.
     *
     * @return the tag for the current tab, may be {@code null} if no tab is
     *         set as current
     */
    public String getCurrentTabTag() {
        if (mCurrentTab >= 0 && mCurrentTab < mTabSpecs.size()) {
            return mTabSpecs.get(mCurrentTab).getTag();
        }
        return null;
    }

    /**
     * Returns the view for the current tab.
     *
     * @return the view for the current tab, may be {@code null} if no tab is
     *         set as current
     */
    public View getCurrentTabView() {
        if (mCurrentTab >= 0 && mCurrentTab < mTabSpecs.size()) {
            return mTabWidget.getChildTabViewAt(mCurrentTab);
        }
        return null;
    }

    public View getCurrentView() {
        return mCurrentView;
    }

    public View getViewByIndex(int index) {
        return mTabSpecs.get(index).mContentStrategy.getContentView();
    }

    /**
     * Sets the current tab based on its tag.
     *
     * @param tag the tag for the tab to set as current
     */
    public void setCurrentTabByTag(String tag) {
        for (int i = 0, count = mTabSpecs.size(); i < count; i++) {
            if (mTabSpecs.get(i).getTag().equals(tag)) {
                setCurrentTab(i);
                break;
            }
        }
    }

    /**
     * Get the FrameLayout which holds tab content
     */
    public FrameLayout getTabContentView() {
        return mTabContent;
    }

    /**
     * Get the location of the TabWidget.
     *
     * @return The TabWidget location.
     */
    @SuppressLint("SwitchIntDef")
    private int getTabWidgetLocation() {
        if (mTabWidget.getOrientation() == LinearLayout.VERTICAL) {
            return (mTabContent.getLeft() < mTabWidget.getLeft()) ? TABWIDGET_LOCATION_RIGHT
                    : TABWIDGET_LOCATION_LEFT;
        }

        return (mTabContent.getTop() < mTabWidget.getTop()) ? TABWIDGET_LOCATION_BOTTOM
                : TABWIDGET_LOCATION_TOP;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        final boolean handled = super.dispatchKeyEvent(event);

        // unhandled key events change focus to tab indicator for embedded
        // activities when there is nothing that will take focus from default
        // focus searching
        if (!handled
                && (event.getAction() == KeyEvent.ACTION_DOWN)
                && (mCurrentView != null)
                && (mCurrentView.hasFocus())) {
            int keyCodeShouldChangeFocus = KeyEvent.KEYCODE_DPAD_UP;
            int directionShouldChangeFocus = View.FOCUS_UP;
            int soundEffect = SoundEffectConstants.NAVIGATION_UP;

            switch (getTabWidgetLocation()) {
                case TABWIDGET_LOCATION_LEFT:
                    keyCodeShouldChangeFocus = KeyEvent.KEYCODE_DPAD_LEFT;
                    directionShouldChangeFocus = View.FOCUS_LEFT;
                    soundEffect = SoundEffectConstants.NAVIGATION_LEFT;
                    break;
                case TABWIDGET_LOCATION_RIGHT:
                    keyCodeShouldChangeFocus = KeyEvent.KEYCODE_DPAD_RIGHT;
                    directionShouldChangeFocus = View.FOCUS_RIGHT;
                    soundEffect = SoundEffectConstants.NAVIGATION_RIGHT;
                    break;
                case TABWIDGET_LOCATION_BOTTOM:
                    keyCodeShouldChangeFocus = KeyEvent.KEYCODE_DPAD_DOWN;
                    directionShouldChangeFocus = View.FOCUS_DOWN;
                    soundEffect = SoundEffectConstants.NAVIGATION_DOWN;
                    break;
                case TABWIDGET_LOCATION_TOP:
                default:
                    break;
            }
            if (event.getKeyCode() == keyCodeShouldChangeFocus
                    && mCurrentView.findFocus().focusSearch(directionShouldChangeFocus) == null) {
                mTabWidget.getChildTabViewAt(mCurrentTab).requestFocus();
                playSoundEffect(soundEffect);
                return true;
            }
        }
        return handled;
    }


    @Override
    public void dispatchWindowFocusChanged(boolean hasFocus) {
        if (mCurrentView != null){
            mCurrentView.dispatchWindowFocusChanged(hasFocus);
        }
    }

    @Override
    public CharSequence getAccessibilityClassName() {
        return TabHost.class.getName();
    }

    public void setCurrentTab(int index) {
        if (index < 0 || index >= mTabSpecs.size()) {
            return;
        }

        if (index == mCurrentTab) {
            return;
        }

        // notify old tab content
        if (mCurrentTab != -1) {
            mTabSpecs.get(mCurrentTab).mContentStrategy.tabClosed();
        }

        mCurrentTab = index;
        final TabHost.TabSpec spec = mTabSpecs.get(index);

        // Call the tab widget's focusCurrentTab(), instead of just
        // selecting the tab.
        mTabWidget.focusCurrentTab(mCurrentTab);

        // tab content
        mCurrentView = spec.mContentStrategy.getContentView();

        if (mCurrentView.getParent() == null) {
            mTabContent
                    .addView(
                            mCurrentView,
                            new ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT));
        }

        if (!mTabWidget.hasFocus()) {
            // if the tab widget didn't take focus (likely because we're in touch mode)
            // give the current tab content view a shot
            mCurrentView.requestFocus();
        }

        //mTabContent.requestFocus(View.FOCUS_FORWARD);
        invokeOnTabChangeListener();
    }

    /**
     * Register a callback to be invoked when the selected state of any of the items
     * in this list changes
     * @param l
     * The callback that will run
     */
    public void setOnTabChangedListener(OnTabChangeListener l) {
        mOnTabChangeListener = l;
    }

    private void invokeOnTabChangeListener() {
        if (mOnTabChangeListener != null) {
            mOnTabChangeListener.onTabChanged(getCurrentTabTag());
        }
    }

    /**
     * Interface definition for a callback to be invoked when tab changed
     */
    public interface OnTabChangeListener {
        void onTabChanged(String tabId);
    }


    /**
     * Makes the content of a tab when it is selected. Use this if your tab
     * content needs to be created on demand, i.e. you are not showing an
     * existing view or starting an activity.
     */
    public interface TabContentFactory {
        /**
         * Callback to make the tab contents
         *
         * @param tag
         *            Which tab was selected.
         * @return The view to display the contents of the selected tab.
         */
        View createTabContent(String tag);
    }


    /**
     * A tab has a tab indicator, content, and a tag that is used to keep
     * track of it.  This builder helps choose among these options.
     * <br>
     * For the tab indicator, your choices are:
     * 1) set a label
     * 2) set a label and an icon
     * <br>
     * For the tab content, your choices are:
     * 1) the id of a {@link View}
     * 2) a {@link TabContentFactory} that creates the {@link View} content.
     */
    public static class TabSpec {

        private final String mTag;
        private IndicatorStrategy mIndicatorStrategy;
        private ContentStrategy mContentStrategy;

        /**
         * Constructs a new tab specification with the specified tag.
         *
         * @param tag the tag for the tag specification, must be non-null
         */
        private TabSpec(String tag) {
            mTag = tag;
        }

        /**
         * Specify a view as the tab indicator.
         */
        public TabSpec setIndicator(View view) {
            mIndicatorStrategy = new ViewIndicatorStrategy(view);
            return this;
        }

        /**
         * Specify a {@link android.widget.TabHost.TabContentFactory} to use to
         * create the content of the tab.
         */
        public TabSpec setContent(TabContentFactory contentFactory) {
            mContentStrategy = new FactoryContentStrategy(mTag, contentFactory);
            return this;
        }

        /**
         * Returns the tag for this tab specification.
         *
         * @return the tag for this tab specification
         */
        public String getTag() {
            return mTag;
        }
    }

    /**
     * Specifies what you do to create a tab indicator.
     */
    private interface IndicatorStrategy {

        /**
         * Return the view for the indicator.
         */
        View createIndicatorView();
    }

    /**
     * Specifies what you do to manage the tab content.
     */
    private interface ContentStrategy {

        /**
         * Return the content view.  The view should may be cached locally.
         */
        View getContentView();

        /**
         * Perhaps do something when the tab associated with this content has
         * been closed (i.e make it invisible, or remove it).
         */
        void tabClosed();
    }

    /**
     * How to create a tab indicator by specifying a view.
     */
    private static class ViewIndicatorStrategy implements IndicatorStrategy {

        private final View mView;

        private ViewIndicatorStrategy(View view) {
            mView = view;
        }

        public View createIndicatorView() {
            return mView;
        }
    }

    /**
     * How tab content is managed using {@link TabContentFactory}.
     */
    private static class FactoryContentStrategy implements ContentStrategy {
        private View mTabContent;
        private final CharSequence mTag;
        private final TabContentFactory mFactory;

        public FactoryContentStrategy(CharSequence tag, TabContentFactory factory) {
            mTag = tag;
            mFactory = factory;
        }

        public View getContentView() {
            if (mTabContent == null) {
                mTabContent = mFactory.createTabContent(mTag.toString());
            }
            mTabContent.setVisibility(View.VISIBLE);
            return mTabContent;
        }

        public void tabClosed() {
            mTabContent.setVisibility(View.GONE);
        }
    }

}
