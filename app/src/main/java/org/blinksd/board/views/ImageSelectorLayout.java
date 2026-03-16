package org.blinksd.board.views;

import static org.blinksd.board.SuperBoardApplication.getBackgroundImageFile;
import static org.blinksd.utils.ColorUtils.setColorFilter;
import static org.blinksd.utils.DialogUtils.doHacksAndShow;
import static org.blinksd.utils.LayoutCreator.createButton;
import static org.blinksd.utils.LayoutCreator.createFilledVerticalLayout;
import static org.blinksd.utils.LayoutCreator.createLayoutParams;
import static org.blinksd.utils.ResourcesUtils.getDefaultTextColor;
import static org.blinksd.utils.SuperDBHelper.calculateColorsForScheme;
import static org.blinksd.utils.SystemUtils.isDocumentsUiAvailable;
import static org.blinksd.utils.SystemUtils.isPermGranted;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.blinksd.board.R;
import org.blinksd.board.activities.settings.SettingsBaseActivity;
import org.blinksd.utils.DensityUtils;
import org.blinksd.utils.ImageUtils;
import org.blinksd.utils.ResourcesUtils;

import java.io.File;
import java.util.TreeMap;

import thirdparty.android.widget.TabHost;
import thirdparty.android.widget.TabWidget;

@SuppressLint("ViewConstructor")
public final class ImageSelectorLayout extends LinearLayout {
    private byte indexNum = 0, gradientType = 0;
    private final ImageView prev;
    private final SuperBoard superBoard;
    private final TabHost host;
    private TreeMap<Integer, Integer> colorList;
    private final GradientDrawable.Orientation[] gradientOrientations = GradientDrawable.Orientation.values();
    private final View.OnClickListener colorSelectorListener = new View.OnClickListener() {

        @Override
        public void onClick(final View view) {
            Context ctx = view.getContext();
            int tag = (int) view.getTag();
            final ColorSelectorLayout px = new ColorSelectorLayout(ctx, tag);
            px.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
            AlertDialog.Builder build = new AlertDialog.Builder(view.getContext());
            build.setTitle(((TextView) view.findViewById(android.R.id.text1)).getText());
            build.setView(px);
            build.setOnCancelListener(p -> prev.setImageBitmap(convertGradientToBitmap()));
            build.setNegativeButton(android.R.string.cancel, (p0, p1) -> p0.dismiss());
            build.setPositiveButton(android.R.string.ok, (p0, p1) -> {
                view.setTag(px.currentColorValue);
                prev.setImageBitmap(convertGradientToBitmap());
                p0.dismiss();
            });
            doHacksAndShow(build.create());
        }

    };
    private final View.OnClickListener gradientDelColorListener = new View.OnClickListener() {

        @Override
        public void onClick(View p1) {
            if (colorList.size() < 2) {
                Context ctx = p1.getContext();
                String out = String.format(getImageSelectorTranslation("gradient_remove_item_error"), colorList.size());
                Toast.makeText(ctx, out, Toast.LENGTH_SHORT).show();
                return;
            }

            // get delete button's parent's parent
            // ImageView del > ColorSelectorItemLayout > color selector item layout's parent
            ViewGroup gradientSel = (ViewGroup) ((View) p1.getParent()).getParent();
            int num = p1.getId();
            colorList.remove(num);
            gradientSel.removeView(gradientSel.findViewById(num));
            prev.setImageBitmap(convertGradientToBitmap());
        }

    };

    public ImageSelectorLayout(final Dialog win, final Runnable onImageSelectPressed, final Runnable onRestartKeyboard) {
        super(win.getContext());
        setOrientation(VERTICAL);

        TabWidget widget = new TabWidget(win.getContext());
        widget.setId(android.R.id.tabs);

        host = new TabHost(win.getContext());
        host.setLayoutParams(createLayoutParams(LinearLayout.class, -1, -2));
        FrameLayout fl = new FrameLayout(win.getContext());
        fl.setLayoutParams(createLayoutParams(LinearLayout.class, -1, -1));
        fl.setId(android.R.id.tabcontent);
        LinearLayout holder = createFilledVerticalLayout(LinearLayout.class, win.getContext());
        holder.setGravity(Gravity.CENTER);
        holder.addView(widget);
        prev = new ImageView(win.getContext()) {
            @Override
            public void setImageDrawable(Drawable drawable) {
                super.setImageDrawable(drawable);

                if (prev.getTag(R.id.gradient_selector) instanceof ColorScheme colorScheme &&
                        drawable instanceof BitmapDrawable bitmapDrawable) {
                    applyColorPreview(bitmapDrawable.getBitmap(), colorScheme);
                }
            }
        };
        prev.setId(R.id.dialog_image_preview);
        int gradientPadding = DensityUtils.dpInt(2);
        int frameMargin = DensityUtils.dpInt(8);
        int dp = DensityUtils.hpInt(25);
        prev.setLayoutParams(new FrameLayout.LayoutParams(-1, dp));
        prev.setScaleType(ImageView.ScaleType.CENTER_CROP);
        superBoard = getSuperBoardView();
        FrameLayout prevContainer = new FrameLayout(win.getContext());
        var prevContainerParams = new LinearLayout.LayoutParams(-1, -2, 0);
        prevContainerParams.setMargins(frameMargin, frameMargin, frameMargin, frameMargin);
        prevContainer.setLayoutParams(prevContainerParams);
        prevContainer.setPadding(gradientPadding, gradientPadding, gradientPadding, gradientPadding);
        prevContainer.addView(prev);
        prevContainer.addView(superBoard);
        holder.addView(getColorSchemeSelector());
        holder.addView(prevContainer);
        holder.addView(fl);
        host.addView(holder);
        host.setOnTabChangedListener(p1 -> {
            switch (host.getCurrentTab()) {
                case 0:
                    if (colorList != null) {
                        gradientType = 0;
                        colorList.clear();
                        ViewGroup gradientSel = host.findViewById(R.id.gradient_selector);
                        if (gradientSel != null) {
                            gradientSel.removeAllViews();
                            gradientSel.addView(getColorSelectorItem(win.getContext(), -1));
                            gradientSel.addView(getColorSelectorItem(win.getContext(), -2));
                        }
                    }
                    setCurrentWallpaperPreview();
                    break;
                case 1:
                    showGradientColorPresetsDialog();
                    // gradientAddColorListener.onClick(host);
                    break;
            }

        });
        addView(host);
        host.setup();

        final String[] tabTitles = { "photo", "gradient" };
        for (int i = 0; i < tabTitles.length; i++) {
            TabHost.TabSpec ts = host.newTabSpec(tabTitles[i]);
            TextView tv = (TextView) LayoutInflater.from(win.getContext())
                    .inflate(android.R.layout.simple_list_item_1, widget, false);
            LinearLayout.LayoutParams pr = (LinearLayout.LayoutParams) 
                    createLayoutParams(LinearLayout.class, -1, DensityUtils.dpInt(48));
            pr.weight = 0.33f;
            tv.setLayoutParams(pr);
            tv.setText(getImageSelectorTranslation(tabTitles[i]));
            tv.setBackgroundResource(R.drawable.tab_indicator_material);
            setColorFilter(tv.getBackground(), getDefaultTextColor());
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(0, 0, 0, 0);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            ts.setIndicator(tv);

            switch (i) {
                case 0:
                    ts.setContent(p -> getPhotoSelector(win, onImageSelectPressed, onRestartKeyboard));
                    break;
                case 1:
                    ts.setContent(p -> getGradientSelector(win.getContext()));
                    break;
            }

            host.addTab(ts);
        }
    }

    private SuperBoard getSuperBoardView() {
        var superBoard = new SuperBoard(getContext()) {
            @Override
            protected void sendKeyboardEvent(Key v) {}
        };
        superBoard.setLayoutParams(new FrameLayout.LayoutParams(-1, -2));
        superBoard.addRow(0, new CharSequence[]{});
        superBoard.addRow(0, new CharSequence[]{ "a", "", "" });
        superBoard.addRow(0, new CharSequence[]{});
        superBoard.setKeysTextSize(DensityUtils.minP(2.5f));
        superBoard.setKeysPadding(10);
        superBoard.setIconSizeMultiplier(2);

        for (int i = 0; i <= 2; i += 2) {
            var row = superBoard.getRow(0, i);
            if (row != null) {
                row.setVisibility(View.INVISIBLE);
            }
        }

        var delKey = superBoard.getKey(0, 1, 1);
        if (delKey != null) {
            delKey.setKeyIcon(R.drawable.sym_keyboard_delete);
        }

        var enterKey = superBoard.getKey(0, 1, 2);
        if (enterKey != null) {
            enterKey.setKeyIcon(R.drawable.sym_keyboard_return);
        }

        return superBoard;
    }

    private void applyColorPreview(Bitmap b, ColorScheme scheme) {
        superBoard.setKeyboardHeight(25);
        superBoard.fixHeight();

        var calculatedScheme = calculateColorsForScheme(b, scheme);
        superBoard.setBackgroundColor(calculatedScheme[0]);

        int keyClr          = calculatedScheme[ 1];
        int keyPressClr     = calculatedScheme[ 2];
        int key2Clr         = calculatedScheme[ 3];
        int key2PressClr    = calculatedScheme[ 4];
        int enterClr        = calculatedScheme[ 5];
        int enterPressClr   = calculatedScheme[ 6];
        int keyTextColor    = calculatedScheme[ 7];
        int key2TextColor   = calculatedScheme[ 8];
        int enterTextColor  = calculatedScheme[ 9];
        int textShadowColor = calculatedScheme[10];

        Drawable keyBg = ResourcesUtils.getKeyBg(keyClr, keyPressClr, true);
        Drawable key2Bg = ResourcesUtils.getKeyBg(key2Clr, key2PressClr, true);
        Drawable enterBg = ResourcesUtils.getKeyBg(enterClr, enterPressClr, true);

        superBoard.setKeysBackground(keyBg);
        superBoard.setKeysTextColor(keyTextColor);

        superBoard.setKeyBackgroundAndItemColor(0, 1, 1, key2Bg, key2TextColor);
        superBoard.setKeyBackgroundAndItemColor(0, 1, 2, enterBg, enterTextColor);
        superBoard.setKeysShadow(DensityUtils.minPInt(1), textShadowColor);
    }

    private View getColorSchemeSelector() {
        prev.setTag(R.id.gradient_selector, ColorScheme.COLORFUL_V1);

        var spinner = new Spinner(getContext());
        spinner.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        spinner.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, ColorScheme.values()));
        spinner.setSelection(0);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                var selection = ColorScheme.values()[position];
                prev.setTag(R.id.gradient_selector, selection);

                if (prev.getDrawable() instanceof BitmapDrawable bitmapDrawable) {
                    applyColorPreview(bitmapDrawable.getBitmap(), selection);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        return spinner;
    }

    /** @noinspection ResultOfMethodCallIgnored*/
    @SuppressLint("MissingPermission")
    private View getPhotoSelector(final Dialog win, final Runnable onImageSelectPressed,
                                         final Runnable onRestartKeyboard) {
        Context ctx = win.getContext();
        int margin = DensityUtils.dpInt(8);
        LinearLayout l = createFilledVerticalLayout(LinearLayout.class, ctx);
        l.setPadding(margin, margin, margin, margin);
        Button s = createButton(ctx);
        s.setBackground(ResourcesUtils.getSelectableItemBg(s.getCurrentTextColor()));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2, 0);
        params.bottomMargin = margin;
        s.setLayoutParams(params);
        s.setText(getImageSelectorTranslation("select"));
        s.setOnClickListener(p1 -> onImageSelectPressed.run());
        l.addView(s);

        if (isDocumentsUiAvailable()) {
            Button w = createButton(ctx);
            w.setBackground(ResourcesUtils.getSelectableItemBg(w.getCurrentTextColor()));
            params = new LinearLayout.LayoutParams(-1, -2, 0);
            params.bottomMargin = margin;
            w.setLayoutParams(params);
            w.setText(getImageSelectorTranslation("wp"));
            l.addView(w);
            w.setOnClickListener(p1 -> {
                if (isPermGranted(ctx)) {
                    WallpaperManager wm = (WallpaperManager) ctx.getSystemService(Context.WALLPAPER_SERVICE);
                    Drawable d;
                    if (wm.getWallpaperInfo() != null) {
                        Toast.makeText(p1.getContext(), getImageSelectorTranslation("warning_live_wallpaper"), Toast.LENGTH_SHORT).show();
                        d = wm.getWallpaperInfo().loadThumbnail(ctx.getPackageManager());
                    } else {
                        d = wm.getDrawable();
                    }

                    if (d instanceof BitmapDrawable bd) {
                        Bitmap b = bd.getBitmap();
                        b = ImageUtils.getMinimizedBitmap(b);
                        prev.setImageBitmap(b);
                    }
                } else {
                    Toast.makeText(p1.getContext(),
                            getImageSelectorTranslation("warning_storage_access"),
                            Toast.LENGTH_LONG).show();
                    ctx.startActivity(new Intent(
                            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                                    ? Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                    : Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                            Uri.parse("package:" + ctx.getPackageName())));
                }
            });
        }

        Button rb = createButton(ctx);
        rb.setBackground(ResourcesUtils.getSelectableItemBg(rb.getCurrentTextColor()));
        params = new LinearLayout.LayoutParams(-1, -2, 0);
        params.bottomMargin = margin * 2;
        rb.setLayoutParams(params);
        l.addView(rb);
        rb.setText(getImageSelectorTranslation("rotate"));
        rb.setOnClickListener(p1 -> {
            if (prev.getDrawable() instanceof BitmapDrawable bmpDrawable) {
                var bitmap = bmpDrawable.getBitmap();
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                if (!bitmap.isMutable()) {
                    bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                }
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                prev.setImageBitmap(bitmap);
            }
        });
        s.setOnLongClickListener(p1 -> {
            getBackgroundImageFile().delete();
            prev.setImageDrawable(null);
            win.dismiss();
            onRestartKeyboard.run();
            return false;
        });
        return l;
    }

    private void setCurrentWallpaperPreview() {
        final File f = getBackgroundImageFile();
        if (f.exists()) {
            prev.setImageBitmap(BitmapFactory.decodeFile(f.getAbsolutePath()));
        }
    }

    private View getGradientSelector(final Context ctx) {
        if (colorList == null) {
            colorList = new TreeMap<>();
        }

        LinearLayout gradientSel = createFilledVerticalLayout(LinearLayout.class, ctx);
        gradientSel.setId(R.id.gradient_selector);
        gradientSel.addView(getColorSelectorItem(ctx, -1));
        gradientSel.addView(getColorSelectorItem(ctx, -2));
        ScrollView gradientSelScr = new ScrollView(ctx);
        gradientSelScr.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        gradientSelScr.addView(gradientSel);
        return gradientSelScr;
    }

    public View getColorSelectorItem(Context ctx, int index) {
        return new ColorSelectorItemLayout(ctx, index, colorList, gradientAddColorListener, gradientDelColorListener, colorSelectorListener);
    }

    private GradientDrawable.Orientation getGradientOrientation() {
        return gradientOrientations[gradientType];
    }

    private int[] getGradientColors() {
        if (colorList.isEmpty()) return new int[]{0, 0};
        Object[] ar = colorList.values().toArray();
        int size = ar.length == 1 ? 2 : ar.length;
        int[] out = new int[size];
        for (int i = 0; i < ar.length; i++) {
            out[i] = (int) ar[i];
        }
        if (ar.length == 1) {
            out[1] = out[0];
        }
        return out;
    }

    private Bitmap convertGradientToBitmap() {
        int size = (int) ImageUtils.minSize;
        GradientDrawable gd = new GradientDrawable(getGradientOrientation(), getGradientColors());
        gd.setBounds(0, 0, size, size);
        Bitmap out = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas drw = new Canvas(out);
        gd.draw(drw);
        return out;
    }

    private void showGradientColorPresetsDialog() {
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(getImageSelectorTranslation("gradient"))
                .create();

        final var view = new GradientColorPresetsView(getContext(), new GradientColorPresetsView.OnGradientItemClickListener() {
            @Override
            public void onColorPressed(int[] scheme) {
                ViewGroup gradientSel = host.findViewById(R.id.gradient_selector);

                for (int item : scheme) {
                    int index = indexNum++;
                    View v = getColorSelectorItem(getContext(), index);
                    int count = gradientSel.getChildCount();
                    gradientSel.addView(v, count - 2);
                    v.setTag(item);
                }

                prev.setImageBitmap(convertGradientToBitmap());
                dialog.dismiss();
            }

            @Override
            public void onAddPressed() {
                dialog.dismiss();
                gradientAddColorListener.onClick(host);
            }
        });

        dialog.setView(view);

        doHacksAndShow(dialog);
    }

    private final View.OnClickListener gradientAddColorListener = new View.OnClickListener() {

        @SuppressLint("ResourceType")
        @Override
        public void onClick(View p1) {
            if (p1.getId() == -2) {
                gradientType = (byte) ((gradientType + 1) % gradientOrientations.length);
            } else {
                ViewGroup gradientSel;
                if (p1 instanceof ColorSelectorItemLayout) {
                    // if executed by color selector item
                    gradientSel = (ViewGroup) p1.getParent();
                } else {
                    // if executed by tab host
                    gradientSel = p1.findViewById(R.id.gradient_selector);
                }

                int index = indexNum++;
                View v = getColorSelectorItem(p1.getContext(), index);
                int count = gradientSel.getChildCount();
                gradientSel.addView(v, count - 2);
                colorSelectorListener.onClick(v);
            }
            prev.setImageBitmap(convertGradientToBitmap());
        }

    };

    public enum ColorScheme {
        COLORFUL_V1(0),
        COLORFUL_V2(1);

        final int schemeId;

        ColorScheme(int schemeId) {
            this.schemeId = schemeId;
        }

        /** @noinspection all */
        @Override
        public String toString() {
            return getImageSelectorTranslation("cs_" + super.toString().toLowerCase());
        }
    }

    private static String getImageSelectorTranslation(String key) {
        return SettingsBaseActivity.getTranslation("image_selector_" + key);
    }
}
