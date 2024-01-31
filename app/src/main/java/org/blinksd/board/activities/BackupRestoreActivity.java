package org.blinksd.board.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import org.blinksd.board.R;
import org.blinksd.board.views.CustomRadioButton;
import org.blinksd.board.views.SettingsCategorizedListAdapter;
import org.blinksd.utils.DensityUtils;
import org.blinksd.utils.LayoutCreator;

public class BackupRestoreActivity extends Activity {
    private LinearLayout main;
    private TabHost host;
    private Uri data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        main = LayoutCreator.createFilledVerticalLayout(FrameLayout.class, this);

        if (Build.VERSION.SDK_INT >= 31) {
            getWindow().getDecorView().setFitsSystemWindows(true);
            main.setFitsSystemWindows(false);
            getWindow().setNavigationBarColor(0);
            getWindow().setStatusBarColor(0);
            ColorDrawable colorDrawable = new ColorDrawable(getColor(android.R.color.system_neutral1_900));
            getWindow().setBackgroundDrawable(colorDrawable);
            getActionBar().setBackgroundDrawable(colorDrawable.getConstantState().newDrawable());
        }

        try {
            createMainView();
        } catch (Throwable e) {
            Log.e("MainView", "Error:", e);
        }

        setContentView(main);
    }

    private void createMainView() {
        TabWidget widget = new TabWidget(this);
        widget.setId(android.R.id.tabs);

        host = new TabHost(this);
        host.setLayoutParams(LayoutCreator.createLayoutParams(LinearLayout.class, -1, -2));
        FrameLayout fl = new FrameLayout(this);
        fl.setLayoutParams(LayoutCreator.createLayoutParams(LinearLayout.class, -1, -1));
        fl.setId(android.R.id.tabcontent);
        LinearLayout holder = LayoutCreator.createFilledVerticalLayout(LinearLayout.class, this);
        holder.setGravity(Gravity.CENTER);
        holder.addView(widget);
        holder.addView(fl);
        host.addView(holder);
        main.addView(host);

        final String[] tabTitles = {
                "backup_menu_backup",
                "backup_menu_restore"
        };

        for (int i = 0; i < tabTitles.length; i++) {
            tabTitles[i] = SettingsCategorizedListAdapter.getTranslation(this, tabTitles[i]);
        }

        host.setup();

        for (int i = 0; i < tabTitles.length; i++) {
            TabHost.TabSpec ts = host.newTabSpec(tabTitles[i]);
            TextView tv = (TextView) getLayoutInflater().inflate(android.R.layout.simple_list_item_1, widget, false);
            LinearLayout.LayoutParams pr = (LinearLayout.LayoutParams) LayoutCreator.createLayoutParams(LinearLayout.class, -1, DensityUtils.dpInt(48));
            pr.weight = tabTitles.length;
            tv.setLayoutParams(pr);
            tv.setText(tabTitles[i]);
            tv.setBackgroundResource(R.drawable.tab_indicator_material);
            tv.getBackground().setColorFilter(0xFFDEDEDE, PorterDuff.Mode.SRC_ATOP);
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(0, 0, 0, 0);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            ts.setIndicator(tv);
            final View v = getView(i);
            ts.setContent(p1 -> v);
            host.addTab(ts);
        }
    }

    private View getView(int i) {
        switch (i) {
            case 0:
                return getBackupView();
            case 1:
                return getRestoreView();
        }
        return null;
    }

    private View getBackupView() {
        RadioGroup radioGroup = new RadioGroup(this);
        radioGroup.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        int padding = DensityUtils.dpInt(16);
        radioGroup.setPadding(padding, padding, padding, padding);

        int[] choices = {
                /*
                R.string.settings_backup_type_all,
                */
                R.string.settings_backup_type_theme,
                /*
                R.string.settings_backup_type_other,
                */
        };

        for (int choice : choices) {
            CustomRadioButton radioButton = new CustomRadioButton(this);
            radioButton.setId(choice);
            radioButton.setText(choice);
            radioGroup.addView(radioButton);
        }

        radioGroup.check(choices[0]);
        return radioGroup;
    }

    private View getRestoreView() {
        LinearLayout linearLayout = LayoutCreator.createFilledVerticalLayout(FrameLayout.class, this);
        linearLayout.setGravity(LinearLayout.VERTICAL);
        int padding = DensityUtils.dpInt(16);
        linearLayout.setPadding(padding, padding, padding, padding);

        Button button = LayoutCreator.createButton(this);
        button.setText(R.string.settings_select_file);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/zip");
            startActivityForResult(Intent.createChooser(intent, null), 1);
        });
        linearLayout.addView(button);

        return linearLayout;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 1 && resultCode == RESULT_OK && intent.getData() != null) {
            data = intent.getData();
        }

        super.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem done = menu.add(0,0,0,android.R.string.ok)
                .setIcon(R.drawable.sym_board_return);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            done.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (item.getItemId() == 0) {
            switch (host.getCurrentTab()) {
                case 0: // backup
                    // TODO: Create ZIP file
                    break;
                case 1: // restore
                    if (data != null) {
                        // TODO: Open ZIP file and extract
                    }
                    break;
            }
        }

        return super.onMenuItemSelected(featureId, item);
    }
}
