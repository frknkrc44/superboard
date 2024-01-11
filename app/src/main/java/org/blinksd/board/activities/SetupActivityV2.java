package org.blinksd.board.activities;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.blinksd.board.R;
import org.blinksd.utils.DensityUtils;
import org.blinksd.utils.LayoutUtils;

import java.util.ArrayList;

public class SetupActivityV2 extends Activity {

    private final ArrayList<PageContent> pageContents = new ArrayList<>();
    int currentPage = 0;
    boolean selectedCheckerRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isInputMethodDisabled() && !isInputMethodNotSelected()) {
            startActivity(new Intent(this, AppSettingsV2.class));
            finish();
            return;
        }

        pageContents.add(new PageContent(
                getAppIcon(),
                R.string.wizard_welcome,
                R.string.wizard_nextbtn,
                v -> {
                    if (isInputMethodDisabled()) {
                        changePage(currentPage + 1);
                    } else if (isInputMethodNotSelected()) {
                        changePage(currentPage + 2);
                    } else {
                        changePage(currentPage + 3);
                    }
                },
                false,
                false
        ));

        pageContents.add(new PageContent(
                LayoutUtils.getDrawableCompat(
                        this, R.drawable.sym_keyboard_language, Color.WHITE),
                R.string.wizard_enable,
                R.string.wizard_enablebtn,
                v -> {
                    startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS));
                    v.post(new ImeEnabledCheckerRunnable());
                },
                false,
                true
        ));

        pageContents.add(new PageContent(
                LayoutUtils.getDrawableCompat(
                        this, R.drawable.sym_keyboard_language, Color.WHITE),
                R.string.wizard_select,
                R.string.wizard_selectbtn,
                v -> {
                    ((InputMethodManager) getSystemService(
                            Context.INPUT_METHOD_SERVICE)).showInputMethodPicker();

                    if (!selectedCheckerRunning) {
                        v.post(new ImeSelectedCheckerRunnable());
                    }
                },
                false,
                true
        ));

        pageContents.add(new PageContent(
                LayoutUtils.getDrawableCompat(
                        this, R.drawable.sym_keyboard_language, Color.WHITE),
                R.string.wizard_settings,
                R.string.wizard_settingsbtn,
                v -> startActivity(new Intent(v.getContext(), AppSettingsV2.class)),
                true,
                true
        ));

        pageContents.add(new PageContent(
                LayoutUtils.getDrawableCompat(
                        this, R.drawable.sym_board_return, Color.WHITE),
                R.string.wizard_finish,
                R.string.wizard_finishbtn,
                v -> finish(),
                false,
                true
        ));

        changePage(0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getWindow().getDecorView().setFitsSystemWindows(true);
            ((ViewGroup) findViewById(android.R.id.content))
                    .getChildAt(0).setFitsSystemWindows(false);
            getWindow().setNavigationBarColor(0);
            getWindow().setStatusBarColor(0);
            getWindow().setBackgroundDrawableResource(android.R.color.system_neutral1_900);
        }
    }

    private boolean isInputMethodDisabled() {
        InputMethodManager inputMethodManager =
                (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        for (InputMethodInfo info : inputMethodManager.getEnabledInputMethodList()) {
            if (info.getPackageName().equals(getPackageName())) {
                return false;
            }
        }

        return true;
    }

    private boolean isInputMethodNotSelected() {
        try {
            String defaultIME = Settings.Secure.getString(
                    getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
            return TextUtils.isEmpty(defaultIME) || !defaultIME.contains(getPackageName());
        } catch (Throwable ignored) {}

        return false;
    }

    private void changePage(int page) {
        ViewGroup contentView = findViewById(android.R.id.content);
        PageContent content = pageContents.get(page);
        final int duration = 200;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH
                && contentView.getChildCount() > 0) {
            contentView.getChildAt(0)
                    .animate()
                    .alpha(0)
                    .setDuration(duration)
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            contentView.removeAllViews();
                            View view = new ImageTextAndButtonView(
                                    SetupActivityV2.this, content,
                                    content.extraNextButton, content.smallImage);
                            view.setAlpha(0);
                            contentView.addView(view);
                            view.animate().alpha(1).setDuration(duration).start();
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {
                        }
                    }).start();
        } else {
            contentView.removeAllViews();
            contentView.addView(new ImageTextAndButtonView(
                    this, content, content.extraNextButton, content.smallImage));
        }

        currentPage = page;
    }

    private Drawable getAppIcon() {
        try {
            PackageManager packageManager = getPackageManager();
            ApplicationInfo applicationInfo =
                    packageManager.getApplicationInfo(getPackageName(), 0);
            return applicationInfo.loadIcon(packageManager);
        } catch (Throwable ignored) {
        }
        return null;
    }

    private String getAppName() {
        try {
            PackageManager packageManager = getPackageManager();
            ApplicationInfo applicationInfo =
                    packageManager.getApplicationInfo(getPackageName(), 0);
            return applicationInfo.loadLabel(packageManager).toString();
        } catch (Throwable ignored) {
        }
        return getResources().getString(R.string.app_name);
    }

    private class ImeEnabledCheckerRunnable implements Runnable {
        @Override
        public void run() {
            if (isInputMethodDisabled()) {
                findViewById(android.R.id.content).postDelayed(this, 250);
                return;
            }

            changePage(currentPage + 1);
        }
    }

    private class ImeSelectedCheckerRunnable implements Runnable {
        @Override
        public void run() {
            if (isInputMethodNotSelected()) {
                selectedCheckerRunning = true;
                findViewById(android.R.id.content).postDelayed(this, 500);
                return;
            }

            changePage(currentPage + 1);
            selectedCheckerRunning = false;
        }
    }

    @SuppressWarnings("unused")
    private class PageContent {
        private final Drawable image;
        private final String text;
        private final String buttonText;
        private final View.OnClickListener onButtonClick;
        private final boolean extraNextButton;
        private final boolean smallImage;

        private PageContent(int textRes, int buttonTextRes, View.OnClickListener onButtonClick,
                            boolean extraNextButton, boolean smallImage) {
            this(null, textRes, buttonTextRes, onButtonClick, extraNextButton, smallImage);
        }

        private PageContent(Drawable image, int textRes, int buttonTextRes,
                            View.OnClickListener onButtonClick, boolean extraNextButton,
                            boolean smallImage) {
            this.image = image;
            this.text = getResources().getString(textRes);
            this.buttonText = getResources().getString(buttonTextRes);
            this.onButtonClick = onButtonClick;
            this.extraNextButton = extraNextButton;
            this.smallImage = smallImage;
        }
    }

    private class ImageTextAndButtonView extends LinearLayout {
        final ImageView imageView;
        final TextView textView;
        final Button buttonView;

        private ImageTextAndButtonView(Context context, PageContent content,
                                       boolean extraNextButton, boolean smallImage) {
            super(context);
            setLayoutParams(new LayoutParams(-1, -1));
            imageView = new ImageView(context);
            textView = new TextView(context);
            buttonView = new Button(context);
            setOrientation(LinearLayout.VERTICAL);
            addView(imageView);
            addView(textView);
            addView(buttonView);
            int imageSize = DensityUtils.dpInt(96);
            int imageLayoutSize = smallImage ? (int) (imageSize / 2f) : imageSize;
            imageView.setLayoutParams(new LayoutParams(imageLayoutSize, imageLayoutSize));
            buttonView.setLayoutParams(new LayoutParams(imageSize * 3, -2));
            setGravity(Gravity.CENTER);
            int padding = DensityUtils.dpInt(16);
            setPadding(padding, padding, padding, padding);
            LayoutParams params = (LayoutParams) textView.getLayoutParams();
            params.topMargin = padding;
            params.bottomMargin = padding;

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                textView.setTextAppearance(context, android.R.style.TextAppearance_Medium);
            } else {
                textView.setTextAppearance(android.R.style.TextAppearance_Medium);
            }
            textView.setGravity(Gravity.CENTER);

            buttonView.setBackgroundDrawable(
                    LayoutUtils.getSelectableItemBg(context, buttonView.getCurrentTextColor()));

            imageView.setImageDrawable(content.image);
            textView.setText(String.format(content.text, getAppName()));
            buttonView.setText(content.buttonText);
            buttonView.setOnClickListener(content.onButtonClick);

            if (extraNextButton) {
                Button nextButton = new Button(context);
                LayoutParams buttonParams = (LayoutParams) buttonView.getLayoutParams();
                buttonParams = new LayoutParams(buttonParams.width, buttonParams.height);
                buttonParams.topMargin = padding;
                nextButton.setLayoutParams(buttonParams);
                nextButton.setBackgroundDrawable(LayoutUtils.getSelectableItemBg(
                        context, buttonView.getCurrentTextColor()));
                nextButton.setOnClickListener(v -> changePage(currentPage + 1));
                nextButton.setText(R.string.wizard_nextbtn);
                addView(nextButton);
            }

            if (content.image == null) {
                hideImage();
            }
        }

        private void hideImage() {
            imageView.setVisibility(View.GONE);
        }
    }
}
