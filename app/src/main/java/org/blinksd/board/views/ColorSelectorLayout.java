package org.blinksd.board.views;

import static org.blinksd.utils.ColorUtils.setColorFilter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.inputmethodservice.Keyboard;
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
import android.widget.TabWidget;
import android.widget.TextView;

import org.blinksd.board.R;
import org.blinksd.board.activities.settings.SettingsBaseActivity;
import org.blinksd.utils.ColorUtils;
import org.blinksd.utils.Defaults;
import org.blinksd.utils.DensityUtils;
import org.blinksd.utils.HSVColorUtils;
import org.blinksd.utils.LayoutCreator;
import org.blinksd.utils.ResourcesUtils;
import org.blinksd.utils.SettingMap;
import org.blinksd.utils.SuperDBHelper;

@SuppressLint("ViewConstructor")
@SuppressWarnings({"deprecation", "all"})
public final class ColorSelectorLayout extends LinearLayout {
    private final TextView prev;
    private final CustomSeekBar a;
    private CustomSeekBar r, g, b, h, s, v;
    private EditText hexIn;
    public int currentColorValue;

    public ColorSelectorLayout(Context ctx, int colorValue) {
        super(ctx);
        setOrientation(VERTICAL);
        TabWidget widget = new TabWidget(ctx);
        widget.setId(android.R.id.tabs);
        this.currentColorValue = colorValue;

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
        a.setProgress(Color.alpha(currentColorValue));
        holder.addView(a);
        holder.addView(fl);
        host.addView(holder);
        addView(host);

        final String[] tabTitles = { "rgb", "hsv", "hex" };
        host.setOnTabChangedListener(p1 -> {
            a.setVisibility(p1.equals(tabTitles[2]) ? View.GONE : View.VISIBLE);
            a.setProgress(Color.alpha(currentColorValue));
            switch (host.getCurrentTab()) {
                case 0:
                    r.setProgress(Color.red(currentColorValue));
                    g.setProgress(Color.green(currentColorValue));
                    b.setProgress(Color.blue(currentColorValue));
                    break;
                case 1:
                    int[] hsv = getHSV(currentColorValue);
                    h.setProgress(hsv[0]);
                    s.setProgress(hsv[1]);
                    v.setProgress(hsv[2]);
                    break;
                case 2:
                    hexIn.setText(ColorUtils.colorIntToString(
                            a.getProgress(),
                            r.getProgress(),
                            g.getProgress(),
                            b.getProgress(),
                            false
                    ));
                    break;
            }
        });

        host.setup();

        for (int i = 0; i < tabTitles.length; i++) {
            TabHost.TabSpec ts = host.newTabSpec(tabTitles[i]);
            TextView tv = (TextView) LayoutInflater.from(ctx).inflate(android.R.layout.simple_list_item_1, widget, false);
            LinearLayout.LayoutParams pr = (LinearLayout.LayoutParams) LayoutCreator.createLayoutParams(LinearLayout.class, -1, DensityUtils.dpInt(48));
            pr.weight = tabTitles.length;
            tv.setLayoutParams(pr);
            tv.setText(getColorSelectorTranslation(tabTitles[i]));
            tv.setBackgroundResource(R.drawable.tab_indicator_material);
            setColorFilter(tv.getBackground(), 0xFFDEDEDE);
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
        return switch (i) {
            case 0 -> getRGBSelector(ctx);
            case 1 -> getHSVSelector(ctx);
            case 2 -> getHexSelector(ctx);
            default -> null;
        };
    }

    private View getRGBSelector(final Context ctx) {
        LinearLayout ll = new LinearLayout(ctx);
        ll.setLayoutParams(new LinearLayout.LayoutParams(-1, -1, 1));
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setGravity(Gravity.CENTER);

        r = new CustomSeekBar(ctx);
        g = new CustomSeekBar(ctx);
        b = new CustomSeekBar(ctx);

        r.setProgressColor(ColorUtils.rgb(0xDE, 0, 0));
        g.setProgressColor(ColorUtils.rgb(0, 0xDE, 0));
        b.setProgressColor(ColorUtils.rgb(0, 0, 0xDE));

        setPreview(prev);

        SeekBar.OnSeekBarChangeListener opc = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar s, int i, boolean c) {
                currentColorValue = ColorUtils.argb(a.getProgress(), r.getProgress(), g.getProgress(), b.getProgress());
                setPreview(prev);
            }

            @Override
            public void onStartTrackingTouch(SeekBar s) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar s) {
            }
        };

        r.setProgress(Color.red(currentColorValue));
        g.setProgress(Color.green(currentColorValue));
        b.setProgress(Color.blue(currentColorValue));

        for (CustomSeekBar v : new CustomSeekBar[]{a, r, g, b}) {
            v.setOnSeekBarChangeListener(opc);
            int pad = DensityUtils.dpInt(8);
            v.setPadding(pad * 2, pad, pad * 2, pad);
            if (a != v) {
                v.setMax(255);
                ll.addView(v);
            }
        }

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

        int[] hsv = getHSV(currentColorValue);
        h.setProgress(hsv[0]);
        s.setProgress(hsv[1]);
        v.setProgress(hsv[2]);

        SeekBar.OnSeekBarChangeListener opc = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar z, int i, boolean c) {
                int color = HSVColorUtils.getColorFromHSVInt(h.getProgress(), s.getProgress(), v.getProgress());
                color = ColorUtils.argb(a.getProgress(), Color.red(color), Color.green(color), Color.blue(color));
                currentColorValue = color;
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
            int pad = DensityUtils.dpInt(8);
            y.setPadding(pad * 2, pad, pad * 2, pad);
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
                    currentColorValue = Color.parseColor("#" + p1.toString());
                    setPreview(prev);
                } catch (Throwable ignored) {}
            }

        });

        SuperBoard sb = new SuperBoard(ctx) {
            @SuppressLint("SetTextI18n")
            @Override
            protected void sendKeyboardEvent(Key v) {
                Pair<Integer, Boolean> pair = v.getNormalPressEvent();

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

                    hexIn.setText(hexIn.getText() + v.getText().toString());
                    playSound(0);
                }
            }

            @Override
            public void playSound(int event) {
                if (!SuperDBHelper.getBooleanOrDefault(SettingMap.SET_PLAY_SND_PRESS)) return;
                super.playSound(event);
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
        sb.setKeysPadding(DensityUtils.mpInt(0.5f));
        sb.setKeyDrawable(0, 1, -1, R.drawable.sym_keyboard_delete);
        sb.setPressEventForKey(0, 1, -1, Keyboard.KEYCODE_DELETE);
        sb.setKeyDrawable(0, 0, -1, R.drawable.delete);
        sb.setPressEventForKey(0, 0, -1, Keyboard.KEYCODE_CANCEL);
        sb.setKeysBackground(ResourcesUtils.getKeyBg(Defaults.KEY_BACKGROUND_COLOR, Defaults.KEY_PRESS_BACKGROUND_COLOR, true));
        sb.setIconSizeMultiplier(SuperDBHelper.getIntOrDefault(SettingMap.SET_KEY_ICON_SIZE_MULTIPLIER));
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
        x.setTextColor(ColorUtils.satisfiesTextContrast(currentColorValue) ? 0xFF212121 : 0XFFDEDEDE);
        x.setBackgroundColor(currentColorValue);
    }

    private String getColorString(boolean showColorInt) {
        return getColorString(
                Color.alpha(currentColorValue), Color.red(currentColorValue), Color.green(currentColorValue), Color.blue(currentColorValue),
                showColorInt
        );
    }

    private String getColorString(int a, int r, int g, int b, boolean showColorInt) {
        String hexColor = ColorUtils.colorIntToString(a, r, g, b);

        return showColorInt
                ? String.format("%s\n(%s, %s, %s, %s)", hexColor, a, r, g, b)
                : hexColor;
    }

    private String getColorSelectorTranslation(String key) {
        return SettingsBaseActivity.getTranslation("color_selector_" + key);
    }
}
