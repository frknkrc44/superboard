package org.blinksd.board.views;

import static android.media.AudioManager.FX_KEYPRESS_DELETE;
import static android.media.AudioManager.FX_KEYPRESS_STANDARD;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.inputmethodservice.Keyboard;
import android.media.AudioManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.TextView;

import org.blinksd.board.R;
import org.blinksd.utils.ColorUtils;
import org.blinksd.utils.Defaults;
import org.blinksd.utils.DensityUtils;
import org.blinksd.utils.HSVColorUtils;
import org.blinksd.utils.LayoutCreator;
import org.blinksd.utils.LayoutUtils;
import org.blinksd.utils.SettingMap;
import org.blinksd.utils.SuperDBHelper;

/** @noinspection unused*/
@SuppressLint("ViewConstructor")
@SuppressWarnings("deprecation")
public class ColorSelectorLayout extends LinearLayout {
    private final TextView prev;
    private final CustomSeekBar a;
    private CustomSeekBar r, g, b, h, s, v;
    private EditText hexIn;
    public int colorValue;

    public ColorSelectorLayout(Context ctx, int colorValue) {
        super(ctx);
        setOrientation(VERTICAL);
        TabWidget widget = new TabWidget(ctx);
        widget.setId(android.R.id.tabs);
        this.colorValue = colorValue;

        final TabHost host = new TabHost(ctx);
        host.setLayoutParams(LayoutCreator.createLayoutParams(LinearLayout.class, -1, -2));
        FrameLayout fl = new FrameLayout(ctx);
        fl.setLayoutParams(LayoutCreator.createLayoutParams(LinearLayout.class, -1, -1));
        fl.setId(android.R.id.tabcontent);
        LinearLayout holder = LayoutCreator.createFilledVerticalLayout(LinearLayout.class, ctx);
        holder.setGravity(Gravity.CENTER);
        holder.addView(widget);
        prev = new TextView(ctx);
        prev.setLayoutParams(new LinearLayout.LayoutParams(-1, -1, 1));
        prev.setGravity(Gravity.CENTER);
        int dp = DensityUtils.dpInt(16);
        prev.setPadding(0, dp, 0, dp);
        holder.addView(prev);
        a = new CustomSeekBar(ctx);
        a.setMax(255);
        a.setProgress(Color.alpha(colorValue));
        holder.addView(a);
        holder.addView(fl);
        host.addView(holder);
        addView(host);

        final String[] stra = {
                "color_selector_rgb",
                "color_selector_hsv",
                "color_selector_hex"
        };

        for (int i = 0; i < stra.length; i++) {
            stra[i] = SettingsCategorizedListAdapter.getTranslation(ctx, stra[i]);
        }

        host.setOnTabChangedListener(p1 -> {
            a.setVisibility(p1.equals(stra[2]) ? View.GONE : View.VISIBLE);
            switch (host.getCurrentTab()) {
                case 0:
                    r.setProgress(Color.red(colorValue));
                    g.setProgress(Color.green(colorValue));
                    b.setProgress(Color.blue(colorValue));
                    break;
                case 1:
                    int[] hsv = getHSV(colorValue);
                    h.setProgress(hsv[0]);
                    s.setProgress(hsv[1]);
                    v.setProgress(hsv[2]);
                    break;
                case 2:
                    hexIn.setText(getColorString(false));
                    break;
            }
        });

        host.setup();

        for (int i = 0; i < stra.length; i++) {
            TabSpec ts = host.newTabSpec(stra[i]);
            TextView tv = (TextView) LayoutInflater.from(ctx).inflate(android.R.layout.simple_list_item_1, widget, false);
            LinearLayout.LayoutParams pr = (LinearLayout.LayoutParams) LayoutCreator.createLayoutParams(LinearLayout.class, -1, DensityUtils.dpInt(48));
            pr.weight = 0.33f;
            tv.setLayoutParams(pr);
            tv.setText(stra[i]);
            tv.setBackgroundResource(R.drawable.tab_indicator_material);
            tv.getBackground().setColorFilter(0xFFDEDEDE, PorterDuff.Mode.SRC_ATOP);
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(0, 0, 0, 0);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            ts.setIndicator(tv);
            final View v = getView(ctx, i);
            ts.setContent(p1 -> v);
            host.addTab(ts);
        }
    }

    public ColorSelectorLayout(Context context, String key) {
        this(context, SuperDBHelper.getIntOrDefault(key));
    }

    private View getView(Context ctx, int i) {
        switch (i) {
            case 0:
                return getRGBSelector(ctx);
            case 1:
                return getHSVSelector(ctx);
            case 2:
                return getHexSelector(ctx);
        }
        return null;
    }

    private View getRGBSelector(final Context ctx) {
        LinearLayout ll = new LinearLayout(ctx);
        ll.setLayoutParams(new LinearLayout.LayoutParams(-1, -1, 1));
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setGravity(Gravity.CENTER);

        r = new CustomSeekBar(ctx);
        g = new CustomSeekBar(ctx);
        b = new CustomSeekBar(ctx);

        changeSeekBarColor(r, Color.rgb(0xDE, 0, 0));
        changeSeekBarColor(g, Color.rgb(0, 0xDE, 0));
        changeSeekBarColor(b, Color.rgb(0, 0, 0xDE));

        setPreview(prev);

        for (CustomSeekBar v : new CustomSeekBar[]{r, g, b}) {
            v.setMax(255);
        }

        r.setProgress(Color.red(colorValue));
        g.setProgress(Color.green(colorValue));
        b.setProgress(Color.blue(colorValue));

        SeekBar.OnSeekBarChangeListener opc = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar s, int i, boolean c) {
                colorValue = Color.argb(a.getProgress(), r.getProgress(), g.getProgress(), b.getProgress());
                setPreview(prev);
            }

            @Override
            public void onStartTrackingTouch(SeekBar s) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar s) {
            }
        };

        for (CustomSeekBar v : new CustomSeekBar[]{r, g, b}) {
            v.setOnSeekBarChangeListener(opc);
            ll.addView(v);
        }

        a.setOnSeekBarChangeListener(opc);
        return ll;
    }

    private View getHSVSelector(Context ctx) {
        LinearLayout ll = new LinearLayout(ctx);
        ll.setLayoutParams(new LinearLayout.LayoutParams(-1, -1, 1));
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setGravity(Gravity.CENTER);

        h = new CustomSeekBar(ctx);
        s = new CustomSeekBar(ctx);
        v = new CustomSeekBar(ctx);

        setPreview(prev);

        h.setMax(360);
        s.setMax(100);
        v.setMax(100);

        int[] hsv = getHSV(colorValue);

        h.setProgress(hsv[0]);
        s.setProgress(hsv[1]);
        v.setProgress(hsv[2]);

        SeekBar.OnSeekBarChangeListener opc = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar z, int i, boolean c) {
                int color = HSVColorUtils.getColorFromHSVInt(h.getProgress(), s.getProgress(), v.getProgress());
                color = Color.argb(a.getProgress(), Color.red(color), Color.green(color), Color.blue(color));
                colorValue = color;
                setPreview(prev);
            }

            @Override
            public void onStartTrackingTouch(SeekBar s) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar s) {
            }
        };
        for (CustomSeekBar y : new CustomSeekBar[]{h, s, v}) {
            y.setOnSeekBarChangeListener(opc);
            ll.addView(y);
        }
        return ll;
    }

    private View getHexSelector(final Context ctx) {
        LinearLayout ll = LayoutCreator.createFilledVerticalLayout(FrameLayout.class, ctx);
        hexIn = new EditText(ctx);
        hexIn.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        hexIn.setEnabled(false);
        hexIn.setGravity(Gravity.CENTER);
        hexIn.setText(getColorString(false));
        hexIn.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence p1, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence p1, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable p1) {
                try {
                    colorValue = Color.parseColor("#" + p1.toString());
                    setPreview(prev);
                } catch (Throwable ignored) {}
            }

        });

        SuperBoard sb = new SuperBoard(ctx) {

            /** @noinspection unused*/
            @SuppressLint("SetTextI18n")
            @Override
            protected void sendDefaultKeyboardEvent(View v) {
                Pair<Integer, Boolean> pair = ((SuperBoard.Key) v).getNormalPressEvent();

                if (pair != null) {
                    switch (pair.first) {
                        case Keyboard.KEYCODE_DELETE:
                            String s = hexIn.getText().toString();
                            if (!s.isEmpty()) {
                                hexIn.setText(s.substring(0, s.length() - 1));
                            }
                            break;
                        case Keyboard.KEYCODE_CANCEL:
                            hexIn.setText("");
                            break;
                    }
                    playSound(1);
                } else {
                    if (hexIn.length() >= 8) {
                        return;
                    }

                    hexIn.setText(hexIn.getText() + ((SuperBoard.Key) v).getText().toString());
                    playSound(0);
                }
            }

            @Override
            public void playSound(int event) {
                if (!SuperDBHelper.getBooleanOrDefault(SettingMap.SET_PLAY_SND_PRESS)) return;
                AudioManager audMgr = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
                switch (event) {
                    case 1:
                        audMgr.playSoundEffect(FX_KEYPRESS_DELETE);
                        break;
                    case 0:
                        audMgr.playSoundEffect(FX_KEYPRESS_STANDARD);
                        break;
                }

            }
        };
        sb.addRows(0,
                new String[][]{
                        {"1", "2", "3", "4", "5", ""},
                        {"6", "7", "8", "9", "0", ""},
                        {"A", "B", "C", "D", "E", "F"}
                });
        sb.setKeyboardHeight(20);
        sb.setKeysTextSize(20);
        sb.setKeysPadding(DensityUtils.dpInt(4));
        sb.setKeyDrawable(0, 1, -1, R.drawable.sym_keyboard_delete);
        sb.setPressEventForKey(0, 1, -1, Keyboard.KEYCODE_DELETE);
        sb.setKeyDrawable(0, 0, -1, R.drawable.sym_keyboard_close);
        sb.setPressEventForKey(0, 0, -1, Keyboard.KEYCODE_CANCEL);
        sb.setKeysBackground(LayoutUtils.getKeyBg(Defaults.KEY_BACKGROUND_COLOR, Defaults.KEY_PRESS_BACKGROUND_COLOR, true));
        ll.addView(hexIn);
        ll.addView(sb);
        return ll;
    }

    private int[] getHSV(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);

        int[] out = new int[3];

        out[0] = (int) hsv[0];
        out[1] = (int) ((1f - hsv[1]) * 100);
        out[2] = (int) (hsv[2] * 100);

        return out;
    }

    private void setPreview(TextView x) {
        x.setText(getColorString(true));
        x.setTextColor(ColorUtils.satisfiesTextContrast(colorValue) ? 0xFF212121 : 0XFFDEDEDE);
        x.setBackgroundColor(colorValue);
    }

    private String getColorString(boolean showColorInt) {
        return getColorString(
                Color.alpha(colorValue), Color.red(colorValue), Color.green(colorValue), Color.blue(colorValue),
                showColorInt
        );
    }

    private String getColorString(int a, int r, int g, int b, boolean showColorInt) {
        String aColorStr = getHexColorString(a);
        String rColorStr = getHexColorString(r);
        String gColorStr = getHexColorString(g);
        String bColorStr = getHexColorString(b);

        String hexColor = String.format("%s%s%s%s", aColorStr, rColorStr, gColorStr, bColorStr).toUpperCase();

        return showColorInt
                ? String.format("%s\n(%s, %s, %s, %s)", hexColor, a, r, g, b)
                : hexColor;
    }

    private String getHexColorString(int x) {
        if (x == 0) return "00";
        String s = Integer.toHexString(x);
        return x < 16 ? "0" + s : s;
    }

    private void changeSeekBarColor(CustomSeekBar s, int c) {
        s.getThumb().setColorFilter(c, PorterDuff.Mode.SRC_ATOP);
        s.getProgressDrawable().setColorFilter(c, PorterDuff.Mode.SRC_ATOP);
    }

}
