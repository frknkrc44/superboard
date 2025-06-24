package org.blinksd.board.views;

import static org.blinksd.board.SuperBoardApplication.getAppDB;
import static org.blinksd.utils.ColorUtils.convertARGBtoRGB;
import static org.blinksd.utils.ColorUtils.setColorFilter;
import static org.blinksd.utils.DensityUtils.dpInt;
import static org.blinksd.utils.ResourcesUtils.getTransSelectableItemBg;
import static org.blinksd.utils.SystemUtils.getMultipliedTextSize;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.blinksd.board.R;
import org.blinksd.utils.SettingMap;
import org.blinksd.utils.SuperDBHelper;
import org.blinksd.utils.ViewUtils;
import org.frknkrc44.minidb.SuperMiniDB;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressLint("ViewConstructor")
public final class ClipboardView extends LinearLayout
        implements ClipboardManager.OnPrimaryClipChangedListener {
    private LinearLayout listView;
    private ClipboardManager clipboardManager;

    private final List<String> clipboardHistory = new ArrayList<>();

    private final SuperBoard superBoard;
    private ImageButton clearAllButton;
    private ImageButton backButton;
    private final View.OnClickListener onCloseListener;

    public ClipboardView(SuperBoard superBoard, View.OnClickListener onCloseListener) {
        super(superBoard.getContext());
        this.superBoard = superBoard;
        this.onCloseListener = onCloseListener;
        init();
    }

    public void deInit() {
        if (clipboardManager != null) {
            try {
                clipboardManager.removePrimaryClipChangedListener(this);
            } catch(Throwable ignored) {}
        }
    }

    private void init() {
        setOrientation(VERTICAL);
        setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        setGravity(Gravity.CENTER_HORIZONTAL);

        int buttonSize = dpInt(48);
        int buttonPadding = buttonSize / 4;

        backButton = new ImageButton(getContext());
        LinearLayout.LayoutParams backButtonParams =
                new LinearLayout.LayoutParams(buttonSize, buttonSize);
        backButtonParams.rightMargin = buttonPadding;
        backButton.setLayoutParams(backButtonParams);
        backButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
        backButton.setOnClickListener(onCloseListener);
        backButton.setImageResource(R.drawable.arrow_left);
        backButton.setPadding(buttonPadding, buttonPadding, buttonPadding, buttonPadding);

        clearAllButton = new ImageButton(getContext());
        LinearLayout.LayoutParams clearAllButtonParams =
                new LinearLayout.LayoutParams(buttonSize, buttonSize);
        clearAllButtonParams.rightMargin = buttonPadding;
        clearAllButton.setLayoutParams(clearAllButtonParams);
        clearAllButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
        clearAllButton.setOnClickListener(v -> clearClipboard(false));
        clearAllButton.setOnLongClickListener(v -> clearClipboard(true));
        clearAllButton.setImageResource(R.drawable.delete);
        clearAllButton.setPadding(buttonPadding, buttonPadding, buttonPadding, buttonPadding);

        LinearLayout buttonsHolder = new LinearLayout(getContext());
        buttonsHolder.setLayoutParams(new LinearLayout.LayoutParams(-2, -2, 0));
        buttonsHolder.addView(backButton);
        buttonsHolder.addView(clearAllButton);
        addView(buttonsHolder);

        listView = new LinearLayout(getContext());
        listView.setOrientation(LinearLayout.VERTICAL);
        listView.setLayoutParams(new ScrollView.LayoutParams(-1, -1));

        ScrollView scrollView = new ScrollView(getContext());
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        scrollView.addView(listView);

        addView(scrollView);

        String[] clipboardHistoryArray = getAppDB()
                .getStringArray(SettingMap.SET_CLIPBOARD_HISTORY, new String[]{});
        for (String str : clipboardHistoryArray) {
            clipboardHistory.add(str);
            addClipView(str, false);
        }

        clipboardManager = (ClipboardManager) getContext()
                .getSystemService(Context.CLIPBOARD_SERVICE);
        onPrimaryClipChanged();
        clipboardManager.addPrimaryClipChangedListener(this);
    }

    private void addClipView(String text, boolean addToHistory) {
        if (isViewAvailableByText(text)) {
            return;
        }

        int buttonSize = dpInt(48);
        int buttonPadding = buttonSize / 4;

        LinearLayout clipLayout = new LinearLayout(getContext());
        LinearLayout.LayoutParams clipLayoutParams =
                new LinearLayout.LayoutParams(-1, -2);
        clipLayoutParams.rightMargin = buttonPadding;
        clipLayoutParams.bottomMargin = buttonPadding;
        clipLayout.setLayoutParams(clipLayoutParams);
        clipLayout.setGravity(Gravity.CENTER_VERTICAL);
        listView.addView(clipLayout, 0);

        clipLayout.addView(createClipLayoutView(text));

        addClipLayoutButton(
                clipLayout,
                android.R.id.button1,
                R.drawable.clipboard,
                this::selectAndUseClipItem
        );

        addClipLayoutButton(
                clipLayout,
                android.R.id.button2,
                R.drawable.delete,
                v -> removeClipView(clipLayout, true)
        );

        if (addToHistory) {
            clipboardHistory.add(text);
            syncClipboardCache();
        }
    }

    private View createClipLayoutView(String text) {
        final int pad = dpInt(8);

        ExpandableTextView mainTextView = new ExpandableTextView(getContext());
        mainTextView.setLayoutParams(new LinearLayout.LayoutParams(-1, -2, 1));
        ViewUtils.setTextAppearance(mainTextView, android.R.style.TextAppearance_Small);

        mainTextView.setPaddingRelative(pad, 0, pad, 0);
        mainTextView.setId(android.R.id.text1);
        mainTextView.setText(text);

        return mainTextView;
    }

    private void addClipLayoutButton(
            ViewGroup clipLayout,
            int id,
            int iconId,
            View.OnClickListener onClickListener
    ) {
        int buttonSize = dpInt(48);
        int buttonPadding = buttonSize / 4;

        ImageButton clButton = new ImageButton(getContext());
        clButton.setLayoutParams(new LinearLayout.LayoutParams(buttonSize, buttonSize, 0));
        clButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
        clButton.setId(id);
        clButton.setImageResource(iconId);
        clButton.setOnClickListener(onClickListener);
        clButton.setPadding(buttonPadding, buttonPadding, buttonPadding, buttonPadding);
        clipLayout.addView(clButton);
    }

    private void selectAndUseClipItem(View view) {
        final var parentView = (View) view.getParent();

        String item = getTextFromView(parentView);

        List<String> texts = getLastPrimaryClipTexts();
        if (texts.isEmpty() || texts.contains(item)) {
            return;
        }

        clipboardManager.setPrimaryClip(ClipData.newPlainText(item, item));
        removeClipView(parentView, false);
        addClipView(item, true);

        superBoard.commitText(item);
    }

    private String getTextFromView(View view) {
        return ((TextView) view.findViewById(android.R.id.text1)).getText().toString();
    }

    /** @noinspection SameReturnValue*/
    public boolean clearClipboard(boolean clearSystem) {
        listView.removeAllViews();
        clipboardHistory.clear();

        if (clearSystem) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                clipboardManager.clearPrimaryClip();
            } else {
                clipboardManager.setPrimaryClip(ClipData.newPlainText(null, null));
            }
        }

        syncClipboardCache();
        return true;
    }

    private void removeClipView(View view, boolean sync) {
        listView.removeView(view);
        String item = getTextFromView(view);
        clipboardHistory.remove(item);
        if (sync) syncClipboardCache();
    }

    private void syncClipboardCache() {
        SuperMiniDB db = getAppDB();
        if (clipboardHistory.isEmpty()) {
            db.removeKeyFromDB(SettingMap.SET_CLIPBOARD_HISTORY);
        } else {
            String[] outArray = new String[clipboardHistory.size()];
            for (int i = 0; i < clipboardHistory.size(); i++) {
                outArray[i] = clipboardHistory.get(i);
            }

            db.putStringArray(
                    SettingMap.SET_CLIPBOARD_HISTORY,
                    outArray,
                    true
            );
        }
    }

    private List<String> getLastPrimaryClipTexts() {
        final var texts = new ArrayList<String>();

        if (clipboardManager.hasPrimaryClip()) {
            final var data = Objects.requireNonNull(clipboardManager.getPrimaryClip());
            final var itemCount = data.getItemCount();

            for (int i = 0; i < itemCount; i++) {
                final var text = data.getItemAt(i).getText();
                if (text instanceof String) {
                    texts.add((String) text);
                } else if (text != null) {
                    texts.add(text.toString());
                }
            }
        }

        return texts;
    }

    @Override
    public void onPrimaryClipChanged() {
        final var texts = getLastPrimaryClipTexts();
        final var textsSize = texts.size();
        for (int i = 0; i < textsSize; i++) {
            final var primaryText = texts.get(i);

            addClipView(primaryText, true);
        }
    }

    private boolean isViewAvailableByText(String text) {
        final int childCount = listView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = listView.getChildAt(i);

            TextView textView1 = child.findViewById(android.R.id.text1);
            if (text.contentEquals(textView1.getText())) {
                return true;
            }
        }

        return false;
    }

    public void reTheme() {
        getLayoutParams().height = superBoard.getHeight();

        int textColor = SuperDBHelper.getIntOrDefault(SettingMap.SET_KEY_TEXTCLR);
        textColor = convertARGBtoRGB(textColor);

        setColorFilter(clearAllButton, textColor);
        clearAllButton.setBackground(getTransSelectableItemBg(getContext(), textColor));

        setColorFilter(backButton, textColor);
        backButton.setBackground(getTransSelectableItemBg(getContext(), textColor));

        final int childCount = listView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = listView.getChildAt(i);

            ExpandableTextView textView1 = child.findViewById(android.R.id.text1);
            textView1.setTextColor(textColor);
            textView1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getMultipliedTextSize(superBoard.getKeysTextSize()) * 0.75f);

            ImageButton button1 = child.findViewById(android.R.id.button1);
            setColorFilter(button1, textColor);
            button1.setBackground(getTransSelectableItemBg(getContext(), textColor));

            ImageButton button2 = child.findViewById(android.R.id.button2);
            setColorFilter(button2, textColor);
            button2.setBackground(getTransSelectableItemBg(getContext(), textColor));
        }
    }
}
