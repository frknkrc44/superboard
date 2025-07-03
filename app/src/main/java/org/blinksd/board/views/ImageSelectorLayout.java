package org.blinksd.board.views;

import static org.blinksd.board.SuperBoardApplication.getBackgroundImageFile;
import static org.blinksd.utils.ColorUtils.setColorFilter;
import static org.blinksd.utils.ResourcesUtils.getDefaultTextColor;
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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import org.blinksd.board.R;
import org.blinksd.board.activities.settings.AppSettingsV3;
import org.blinksd.board.activities.settings.SettingsBaseActivity;
import org.blinksd.utils.DensityUtils;
import org.blinksd.utils.ImageUtils;
import org.blinksd.utils.LayoutCreator;
import org.blinksd.utils.ResourcesUtils;

import java.io.File;
import java.util.TreeMap;

@SuppressLint("ViewConstructor")
public final class ImageSelectorLayout extends LinearLayout {
    private byte indexNum = 0, gradientType = 0;
    private final ImageView prev;
    private TreeMap<Integer, Integer> colorList;
    GradientDrawable.Orientation[] gradientOrientations = GradientDrawable.Orientation.values();
    private final View.OnClickListener colorSelectorListener = new View.OnClickListener() {

        @Override
        public void onClick(final View p1) {
            Context ctx = p1.getContext();
            int tag = (int) p1.getTag();
            final ColorSelectorLayout px = new ColorSelectorLayout(ctx, tag);
            px.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
            AlertDialog.Builder build = new AlertDialog.Builder(p1.getContext());
            build.setTitle(((TextView) p1.findViewById(android.R.id.text1)).getText());
            build.setView(px);
            build.setOnCancelListener(p11 -> {
                prev.setImageBitmap(convertGradientToBitmap());
                System.gc();
            });
            build.setNegativeButton(android.R.string.cancel, (p112, p2) -> p112.dismiss());
            build.setPositiveButton(android.R.string.ok, (p0, p2) -> {
                p1.setTag(px.currentColorValue);
                prev.setImageBitmap(convertGradientToBitmap());
                System.gc();
                p0.dismiss();
            });
            AppSettingsV3.doHacksAndShow(build.create());
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

    @SuppressWarnings("deprecation")
    public ImageSelectorLayout(final Dialog win, final Runnable onImageSelectPressed, final Runnable onRestartKeyboard) {
        super(win.getContext());
        setOrientation(VERTICAL);

        TabWidget widget = new TabWidget(win.getContext());
        widget.setId(android.R.id.tabs);

        final TabHost host = new TabHost(win.getContext());
        host.setLayoutParams(LayoutCreator.createLayoutParams(LinearLayout.class, -1, -2));
        FrameLayout fl = new FrameLayout(win.getContext());
        fl.setLayoutParams(LayoutCreator.createLayoutParams(LinearLayout.class, -1, -1));
        fl.setId(android.R.id.tabcontent);
        LinearLayout holder = LayoutCreator.createFilledVerticalLayout(LinearLayout.class, win.getContext());
        holder.setGravity(Gravity.CENTER);
        holder.addView(widget);
        prev = new ImageView(win.getContext());
        prev.setId(R.id.dialog_image_preview);
        int gradientPadding = DensityUtils.dpInt(2);
        int frameMargin = DensityUtils.dpInt(8);
        prev.setPadding(gradientPadding, gradientPadding, gradientPadding, gradientPadding);
        int dp = DensityUtils.hpInt(25);
        LinearLayout.LayoutParams imagePreviewParams =
                new LinearLayout.LayoutParams(-1, dp, 0);
        imagePreviewParams.setMargins(frameMargin, frameMargin, frameMargin, frameMargin);
        prev.setLayoutParams(imagePreviewParams);
        prev.setScaleType(ImageView.ScaleType.CENTER_CROP);
        holder.addView(prev);
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
                    gradientAddColorListener.onClick(host);
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
                    LayoutCreator.createLayoutParams(LinearLayout.class, -1, DensityUtils.dpInt(48));
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

    /** @noinspection ResultOfMethodCallIgnored*/
    @SuppressLint("MissingPermission")
    private View getPhotoSelector(final Dialog win, final Runnable onImageSelectPressed,
                                         final Runnable onRestartKeyboard) {
        Context ctx = win.getContext();
        int margin = DensityUtils.dpInt(8);
        LinearLayout l = LayoutCreator.createFilledVerticalLayout(LinearLayout.class, ctx);
        l.setPadding(margin, margin, margin, margin);
        Button s = LayoutCreator.createButton(ctx);
        s.setBackground(ResourcesUtils.getSelectableItemBg(s.getCurrentTextColor()));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2, 0);
        params.bottomMargin = margin;
        s.setLayoutParams(params);
        s.setText(getImageSelectorTranslation("select"));
        s.setOnClickListener(p1 -> onImageSelectPressed.run());
        l.addView(s);

        if (isDocumentsUiAvailable()) {
            Button w = LayoutCreator.createButton(ctx);
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

        Button rb = LayoutCreator.createButton(ctx);
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

        LinearLayout gradientSel = LayoutCreator.createFilledVerticalLayout(LinearLayout.class, ctx);
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
        for (int i = 0; i < size; i++) {
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
            System.gc();
        }

    };

    private String getImageSelectorTranslation(String key) {
        return SettingsBaseActivity.getTranslation("image_selector_" + key);
    }
}
