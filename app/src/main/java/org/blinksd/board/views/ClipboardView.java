package org.blinksd.board.views;

import static org.blinksd.board.SuperBoardApplication.getAppDB;
import static org.blinksd.utils.ColorUtils.convertARGBtoRGB;
import static org.blinksd.utils.ColorUtils.setColorFilter;
import static org.blinksd.utils.ResourcesUtils.getTransSelectableItemBg;
import static org.blinksd.utils.ViewUtils.setViewBackground;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.text.SpannableString;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.blinksd.board.R;
import org.blinksd.utils.DensityUtils;
import org.blinksd.utils.SettingMap;
import org.blinksd.utils.SuperDBHelper;
import org.frknkrc44.minidb.SuperMiniDB;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@SuppressLint("ViewConstructor")
public final class ClipboardView extends LinearLayout
        implements ClipboardManager.OnPrimaryClipChangedListener {
    private LinearLayout listView;
    private ClipboardManager clipboardManager;
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US);

    private final List<SpannableString> clipboardHistory = new ArrayList<>();

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

        int buttonSize = DensityUtils.dpInt(48);
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
            SpannableString spannableString = new SpannableString(str);
            clipboardHistory.add(spannableString);
            addClipView(spannableString, false);
        }

        clipboardManager = (ClipboardManager) getContext()
                .getSystemService(Context.CLIPBOARD_SERVICE);
        onPrimaryClipChanged();
        clipboardManager.addPrimaryClipChangedListener(this);
    }

    private void addClipView(SpannableString text, boolean addToHistory) {
        int buttonSize = DensityUtils.dpInt(48);
        int buttonPadding = buttonSize / 4;

        LinearLayout clipLayout = new LinearLayout(getContext());
        LinearLayout.LayoutParams clipLayoutParams =
                new LinearLayout.LayoutParams(-1, -2);
        clipLayoutParams.rightMargin = buttonPadding;
        clipLayout.setLayoutParams(clipLayoutParams);
        clipLayout.setTag(text);
        listView.addView(clipLayout, 0);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View textHolder = inflater.inflate(android.R.layout.simple_list_item_2, clipLayout, false);
        textHolder.setLayoutParams(new LinearLayout.LayoutParams(-1, -2, 1));
        clipLayout.addView(textHolder);

        TextView textView1 = textHolder.findViewById(android.R.id.text1);
        textView1.setText(text);

        TextView textView2 = textHolder.findViewById(android.R.id.text2);
        textView2.setText(dateFormat.format(Calendar.getInstance().getTime()));

        ImageButton pasteButton = new ImageButton(getContext());
        pasteButton.setLayoutParams(new LinearLayout.LayoutParams(buttonSize, buttonSize, 0));
        pasteButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
        pasteButton.setOnClickListener(v -> selectAndUseClipItem(v, false));
        pasteButton.setOnLongClickListener(v -> selectAndUseClipItem(v, true));
        pasteButton.setImageResource(R.drawable.clipboard);
        pasteButton.setId(android.R.id.button1);
        pasteButton.setPadding(buttonPadding, buttonPadding, buttonPadding, buttonPadding);
        clipLayout.addView(pasteButton);

        ImageButton deleteButton = new ImageButton(getContext());
        deleteButton.setLayoutParams(new LinearLayout.LayoutParams(buttonSize, buttonSize, 0));
        deleteButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
        deleteButton.setOnClickListener(v -> removeClipView(v, true));
        deleteButton.setImageResource(R.drawable.delete);
        deleteButton.setId(android.R.id.button2);
        deleteButton.setPadding(buttonPadding, buttonPadding, buttonPadding, buttonPadding);
        clipLayout.addView(deleteButton);

        if (addToHistory) {
            clipboardHistory.add(text);
            syncClipboardCache();
        }
    }

    /** @noinspection SameReturnValue*/
    private boolean selectAndUseClipItem(View view, boolean useCtrlToPaste) {
        selectClipItem(view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !useCtrlToPaste) {
            superBoard.sendKeyEvent(KeyEvent.KEYCODE_PASTE);
        } else {
            superBoard.setCtrlState(1);
            superBoard.sendKeyEvent(KeyEvent.KEYCODE_V);
        }
        return true;
    }

    private void selectClipItem(View view) {
        SpannableString item = (SpannableString) ((View) view.getParent()).getTag();

        List<SpannableString> texts = getLastPrimaryClipTexts();
        if (texts.isEmpty() || texts.contains(item)) {
            return;
        }

        clipboardManager.setPrimaryClip(ClipData.newPlainText(item, item));
        removeClipView(view, false);
        addClipView(item, true);
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
        listView.removeView((View) view.getParent());
        clipboardHistory.remove((SpannableString) ((View) view.getParent()).getTag());
        if (sync) syncClipboardCache();
    }

    private void syncClipboardCache() {
        SuperMiniDB db = getAppDB();
        if (clipboardHistory.isEmpty()) {
            db.removeKeyFromDB(SettingMap.SET_CLIPBOARD_HISTORY);
        } else {
            String[] outArray = new String[clipboardHistory.size()];
            for (int i = 0; i < clipboardHistory.size(); i++) {
                outArray[i] = clipboardHistory.get(i).toString();
            }

            db.putStringArray(
                    SettingMap.SET_CLIPBOARD_HISTORY,
                    outArray,
                    true
            );
        }
    }

    public void reTheme() {
        getLayoutParams().height = superBoard.getHeight();

        int textColor = SuperDBHelper.getIntOrDefault(SettingMap.SET_KEY_TEXTCLR);
        textColor = convertARGBtoRGB(textColor);

        setColorFilter(clearAllButton, textColor);
        setViewBackground(clearAllButton, getTransSelectableItemBg(getContext(), textColor));

        setColorFilter(backButton, textColor);
        setViewBackground(backButton, getTransSelectableItemBg(getContext(), textColor));

        for (int i = 0; i < listView.getChildCount(); i++) {
            View child = listView.getChildAt(i);

            TextView textView1 = child.findViewById(android.R.id.text1);
            textView1.setTextColor(textColor);

            TextView textView2 = child.findViewById(android.R.id.text2);
            textView2.setTextColor(textColor);

            ImageButton button1 = child.findViewById(android.R.id.button1);
            setColorFilter(button1, textColor);
            setViewBackground(button1, getTransSelectableItemBg(getContext(), textColor));

            ImageButton button2 = child.findViewById(android.R.id.button2);
            setColorFilter(button2, textColor);
            setViewBackground(button2, getTransSelectableItemBg(getContext(), textColor));
        }
    }

    private List<SpannableString> getLastPrimaryClipTexts() {
        List<SpannableString> texts = new ArrayList<>();

        if (clipboardManager.hasPrimaryClip()) {
            ClipData data = Objects.requireNonNull(clipboardManager.getPrimaryClip());

            for (int i = 0; i < data.getItemCount(); i++) {
                CharSequence text = data.getItemAt(i).getText();
                if (text instanceof SpannableString) {
                    texts.add((SpannableString) text);
                } else if (text instanceof String) {
                    texts.add(new SpannableString(text));
                }
            }
        }

        return texts;
    }

    @Override
    public void onPrimaryClipChanged() {
        List<SpannableString> texts = getLastPrimaryClipTexts();

        for (int i = 0; i < texts.size(); i++) {
            SpannableString primaryText = texts.get(i);

            if (listView.findViewWithTag(primaryText) == null) {
                addClipView(primaryText, true);
            }
        }
    }
}
