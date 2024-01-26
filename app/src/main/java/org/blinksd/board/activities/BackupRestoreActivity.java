package org.blinksd.board.activities;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.blinksd.utils.LayoutCreator;

public class BackupRestoreActivity extends Activity {
    private LinearLayout main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        main = LayoutCreator.createFilledVerticalLayout(FrameLayout.class, this);

        if (Build.VERSION.SDK_INT >= 31) {
            getWindow().getDecorView().setFitsSystemWindows(true);
            main.setFitsSystemWindows(false);
            getWindow().setNavigationBarColor(0);
            getWindow().setStatusBarColor(0);
            getWindow().setBackgroundDrawableResource(android.R.color.system_neutral1_900);
        }

        try {
            createMainView();
        } catch (Throwable e) {
            Log.e("MainView", "Error:", e);
        }

        setContentView(main);
    }

    private void createMainView() {
        // stub
    }
}
