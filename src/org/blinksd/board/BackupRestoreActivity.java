package org.blinksd.board;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import org.blinksd.utils.layout.LayoutCreator;

public class BackupRestoreActivity extends Activity {

    @Override
    public void onCreate(Bundle bundle){
        super.onRestart();
        LinearLayout main = LayoutCreator.createVerticalLayout(this);
        TextView text = LayoutCreator.createTextView(this);
        text.setText("In progress");
        text.setLayoutParams(LayoutCreator.createLayoutParams(main.getClass(), -1, -1));
        text.setGravity(Gravity.CENTER);
        main.addView(text);
        setContentView(main);
    }

}