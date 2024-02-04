package org.blinksd.board.views;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.TextView;

import org.blinksd.board.InputService;
import org.blinksd.board.R;
import org.blinksd.board.SuperBoardApplication;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

@SuppressWarnings("deprecation")
public class EmojiView extends LinearLayout {

    private static String[][] emojis;
    private final Drawable emptyDrawable = new ColorDrawable();
    private float textSize;
    private int keyTextColor;
    private Drawable drw;
    private TabWidget tw;
    private TabHost th;
    private View.OnClickListener onclick;
    private int curTab = 0;

    public EmojiView(SuperBoard sb, View.OnClickListener ocl) {
        this(sb.getContext());
        getEmojis(sb);
        onclick = ocl;
        applyTheme(sb);
    }

    private EmojiView(Context c) {
        super(c);
    }

    private void getEmojis(SuperBoard sb) {
        try {
            InputStream stream = sb.getContext().getAssets().open("emoji_list.json");
            Scanner sc = new Scanner(stream);
            StringBuilder s = new StringBuilder();
            while (sc.hasNext()) s.append(sc.nextLine());
            sc.close();

            JSONObject jsonObject = new JSONObject(s.toString());
            List<String[]> emojiList = new ArrayList<>();
            for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
                JSONArray jsonArray = jsonObject.getJSONArray(it.next());
                List<String> category = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    String glyph = jsonArray.getString(i);
                    if (SuperBoardApplication.getTextUtils().hasGlyph(glyph)) {
                        category.add(glyph);
                    }
                }

                if (!category.isEmpty()) {
                    emojiList.add(category.toArray(new String[0]));
                }
            }

            if (SuperBoardApplication.getTextUtils().hasGlyph("🇦")) {
                emojiList.add(new String[]{
                        "🇦 ", "🇧 ", "🇨 ", "🇩 ", "🇪 ", "🇫 ", "🇬 ",
                        "🇭 ", "🇮 ", "🇯 ", "🇰 ", "🇱 ", "🇲 ", "🇳 ",
                        "🇴 ", "🇵 ", "🇶 ", "🇷 ", "🇸 ", "🇹 ", "🇺 ",
                        "🇻 ", "🇼 ", "🇽 ", "🇾 ", "🇿 "
                });
            }

            emojis = emojiList.toArray(new String[0][]);
        } catch (Throwable ignored) {
        }
    }

    public void applyTheme(SuperBoard sb) {
        textSize = sb.getKeysTextSize();
        keyTextColor = sb.getKeysTextColor();
        drw = sb.keyBackground;
        if (drw == null) {
            drw = new ColorDrawable(0);
        }
        removeAllViewsInLayout();
        apply();
        System.gc();
    }

    private void apply() {
        th = new TabHost(getContext());
        th.setLayoutParams(new LayoutParams(-1, -2, 1));
        tw = new TabWidget(getContext());
        tw.setId(android.R.id.tabs);
        tw.setLayoutParams(new LayoutParams(-1, -1, 1));
        FrameLayout fl = new FrameLayout(getContext());
        fl.setLayoutParams(new LayoutParams(-1, -1));
        fl.setId(android.R.id.tabcontent);
        th.setOnTabChangedListener(p1 -> {
            if (tw != null) {
                View child = tw.getChildTabViewAt(curTab);
                if (child != null)
                    child.setSelected(false);
                child = tw.getChildTabViewAt(curTab = th.getCurrentTab());
                if (child != null)
                    child.setSelected(true);
            }
        });
        final LinearLayout ll = new LinearLayout(getContext());
        ll.setLayoutParams(new LayoutParams(-1, -1));
        ll.setOrientation(VERTICAL);
        final LinearLayout tl = new LinearLayout(getContext());
        int l = getResources().getDisplayMetrics().widthPixels / emojis.length;
        tl.setLayoutParams(new LayoutParams(-1, l));
        tl.addView(categoryItem(-1, l, onclick));
        tl.addView(tw);
        tl.addView(categoryItem(10, l, onclick));
        ll.addView(tl);
        ll.addView(fl);
        th.addView(ll);
        th.setup();
        for (int i = 0; i < emojis.length; i++) {
            TabSpec ts = th.newTabSpec(emojis[i][0]);
            TextView tv = (TextView) LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, tw, false);
            tv.setLayoutParams(new LayoutParams(-1, l, 1));
            tv.setText(emojis[i][0].trim());
            tv.setTextColor(keyTextColor);
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(0, 0, 0, 0);
            tv.setTextSize(textSize);
            ts.setIndicator(tv);
            tv.setBackgroundDrawable(Objects.requireNonNull(drw.getConstantState()).newDrawable());
            final int x = i;
            ts.setContent(p1 -> emojiList(x));
            th.addTab(ts);
        }
        addView(th);
        setOrientation(VERTICAL);
    }

    private View categoryItem(int num, int size, View.OnClickListener ocl) {
        if (num == -1) {
            Button tv = new Button(getContext());
            tv.setLayoutParams(new LayoutParams(size, -1, 0));
            tv.setTextColor(keyTextColor);
            tv.setBackgroundDrawable(Objects.requireNonNull(drw.getConstantState()).newDrawable());
            tv.setGravity(Gravity.CENTER);
            tv.setText("A");
            tv.setTag(num);
            tv.setTextSize(textSize);
            tv.setOnClickListener(ocl);
            return tv;
        } else {
            ImageButton iv = new ImageButton(getContext());
            iv.setBackgroundDrawable(Objects.requireNonNull(drw.getConstantState()).newDrawable());
            iv.setLayoutParams(new LayoutParams(size, size, 0));
            iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            iv.setAdjustViewBounds(true);
            int p = size / 4;
            iv.setPadding(p, p, p, p);
            iv.setImageResource(R.drawable.sym_keyboard_delete);
            iv.setColorFilter(keyTextColor, PorterDuff.Mode.SRC_ATOP);
            iv.setTag(num);
            iv.setOnClickListener(ocl);
            return iv;
        }
    }

    private GridView emojiList(final int index) {
        final GridView gv = new GridView(getContext());
        gv.setOverScrollMode(GridView.OVER_SCROLL_NEVER);
        gv.setLayoutParams(new LayoutParams(-1, -1));
        int columns = SuperBoardApplication.getTextUtils()
                .getTextBounds(textSize, emojis[0][0]).width() / 2;
        gv.setNumColumns(columns);
        gv.setGravity(Gravity.CENTER);
        gv.setSelector(emptyDrawable);
        gv.setOnItemClickListener((p1, p2, p3, p4) ->
                ((InputService) getContext()).onEmojiText(p1.getItemAtPosition(p3).toString()));
        EmojiItemAdapter adapter = new EmojiItemAdapter(index, columns);
        gv.setAdapter(adapter);
        return gv;
    }

    private class EmojiItemAdapter extends BaseAdapter {
        private final int categoryIndex;
        private final int columns;

        public EmojiItemAdapter(int categoryIndex, int columns) {
            this.categoryIndex = categoryIndex;
            this.columns = columns;
        }

        @Override
        public int getCount() {
            return emojis[categoryIndex].length;
        }

        @Override
        public String getItem(int position) {
            return emojis[categoryIndex][position];
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).hashCode() + position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView v = new TextView(EmojiView.this.getContext());
            v.setBackgroundDrawable(Objects.requireNonNull(drw.getConstantState()).newDrawable());
            v.setTextColor(keyTextColor);
            v.setGravity(Gravity.CENTER);
            v.setSingleLine();
            v.setWidth(getResources().getDisplayMetrics().widthPixels / columns);
            v.setHeight(getResources().getDisplayMetrics().widthPixels / columns);
            v.setTextSize(textSize);
            v.setText(getItem(position).trim());
            return v;
        }
    }
}
