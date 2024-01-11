package org.blinksd.board.views;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Build;
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
import org.blinksd.board.SuperBoardApplication;
import org.blinksd.utils.ColorUtils;
import org.blinksd.utils.DensityUtils;
import org.blinksd.utils.LayoutUtils;
import org.blinksd.utils.SettingMap;
import org.blinksd.utils.SuperDBHelper;
import org.frknkrc44.minidb.SuperMiniDB;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@SuppressLint("ViewConstructor")
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ClipboardView extends LinearLayout
        implements ClipboardManager.OnPrimaryClipChangedListener {
    private LinearLayout listView;
    private ClipboardManager clipboardManager;
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US);

    private List<String> clipboardHistory;

    private final SuperBoard superBoard;

    public ClipboardView(SuperBoard superBoard) {
        super(superBoard.getContext());
        this.superBoard = superBoard;
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
        setOrientation(LinearLayout.VERTICAL);
        setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        setGravity(Gravity.CENTER_HORIZONTAL);

        int buttonSize = DensityUtils.dpInt(48);
        int buttonPadding = buttonSize / 4;

        int textColor = SuperDBHelper.getIntOrDefault(SettingMap.SET_KEY_TEXTCLR);
        textColor = ColorUtils.convertARGBtoRGB(textColor);

        ImageButton button = new ImageButton(getContext());
        button.setBackgroundDrawable(LayoutUtils.getTransSelectableItemBg(
                getContext(), textColor, false));
        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(buttonSize, buttonSize, 0);
        buttonParams.rightMargin = buttonPadding;
        button.setLayoutParams(buttonParams);
        button.setScaleType(ImageView.ScaleType.FIT_CENTER);
        button.setOnClickListener(V -> clearClipboard());
        button.setImageResource(R.drawable.sym_keyboard_close);
        button.setColorFilter(textColor, PorterDuff.Mode.SRC_ATOP);
        button.setPadding(buttonPadding, buttonPadding, buttonPadding, buttonPadding);
        addView(button);

        listView = new LinearLayout(getContext());
        listView.setOrientation(LinearLayout.VERTICAL);
        listView.setLayoutParams(new ScrollView.LayoutParams(-1, -1));

        ScrollView scrollView = new ScrollView(getContext());
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        scrollView.addView(listView);

        addView(scrollView);

        String[] clipboardHistoryArray = SuperBoardApplication.getAppDB()
                .getStringArray(SettingMap.SET_CLIPBOARD_HISTORY, new String[]{});
        clipboardHistory = new ArrayList<>();
        clipboardHistory.addAll(Arrays.asList(clipboardHistoryArray));

        for (String text: clipboardHistory) {
            addClipView(text, false);
        }

        clipboardManager = (ClipboardManager) getContext()
                .getSystemService(Context.CLIPBOARD_SERVICE);
        onPrimaryClipChanged();
        clipboardManager.addPrimaryClipChangedListener(this);
    }

    private void addClipView(String text, boolean addToHistory) {
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
        textHolder.setOnClickListener(this::selectClipItem);
        textHolder.setOnLongClickListener(this::selectAndUseClipItem);
        clipLayout.addView(textHolder);

        TextView textView1 = textHolder.findViewById(android.R.id.text1);
        textView1.setText(text);

        int textColor = SuperDBHelper.getIntOrDefault(SettingMap.SET_KEY_TEXTCLR);
        textColor = ColorUtils.convertARGBtoRGB(textColor);
        textView1.setTextColor(textColor);

        TextView textView2 = textHolder.findViewById(android.R.id.text2);
        textView2.setText(dateFormat.format(Calendar.getInstance().getTime()));
        textView2.setTextColor(ColorUtils.setAlphaForColor(0x88, textColor));

        ImageButton button = new ImageButton(getContext());
        button.setBackgroundDrawable(LayoutUtils.getTransSelectableItemBg(
                getContext(), textColor, false));
        button.setLayoutParams(new LinearLayout.LayoutParams(buttonSize, buttonSize, 0));
        button.setScaleType(ImageView.ScaleType.FIT_CENTER);
        button.setOnClickListener(v -> removeClipView(v, true));
        button.setImageResource(R.drawable.sym_keyboard_close);
        button.setColorFilter(textColor, PorterDuff.Mode.SRC_ATOP);
        button.setPadding(buttonPadding, buttonPadding, buttonPadding, buttonPadding);
        clipLayout.addView(button);

        if (addToHistory) {
            clipboardHistory.add(text);
            syncClipboardCache();
        }
    }

    private boolean selectAndUseClipItem(View view) {
        selectClipItem(view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            superBoard.sendKeyEvent(KeyEvent.KEYCODE_PASTE);
        } else {
            superBoard.setCtrlState(1);
            superBoard.sendKeyEvent(KeyEvent.KEYCODE_V);
        }
        return true;
    }

    private void selectClipItem(View view) {
        String item = (String) ((View) view.getParent()).getTag();

        CharSequence text = Objects.requireNonNull(clipboardManager.getPrimaryClip())
                .getItemAt(0).getText();
        if (text != null && text.toString().equals(item)) {
            return;
        }

        clipboardManager.setPrimaryClip(ClipData.newPlainText(item, item));
        removeClipView(view, false);
        addClipView(item, true);
    }

    public void clearClipboard() {
        listView.removeAllViews();
        clipboardHistory.clear();
        syncClipboardCache();
    }

    private void removeClipView(View view, boolean sync) {
        listView.removeView((View) view.getParent());
        clipboardHistory.remove((String) ((View) view.getParent()).getTag());
        if (sync) syncClipboardCache();
    }

    private void syncClipboardCache() {
        SuperMiniDB db = SuperBoardApplication.getAppDB();
        if (clipboardHistory.isEmpty()) {
            db.removeKeyFromDB(SettingMap.SET_CLIPBOARD_HISTORY);
        } else {
            db.putStringArray(
                    SettingMap.SET_CLIPBOARD_HISTORY,
                    clipboardHistory.toArray(new String[0]),
                    true
            );
        }
    }

    @Override
    public void onPrimaryClipChanged() {
        if (clipboardManager.hasPrimaryClip()) {
            ClipData data = Objects.requireNonNull(clipboardManager.getPrimaryClip());

            for (int i = 0; i < data.getItemCount(); i++) {
                CharSequence text = data.getItemAt(i).getText();
                if (text == null) {
                    return;
                }

                String primaryText = text.toString();
                if (listView.findViewWithTag(primaryText) == null) {
                    addClipView(primaryText, true);
                }
            }
        }
    }
}
