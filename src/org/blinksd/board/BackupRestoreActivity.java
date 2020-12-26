package org.blinksd.board;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import org.blinksd.utils.layout.LayoutCreator;
import org.blinksd.utils.toolbar.*;
import org.blinksd.board.backup.*;
import java.util.*;

public class BackupRestoreActivity extends Activity {

    @Override
    public void onCreate(Bundle bundle){
        super.onRestart();
        LinearLayout main = LayoutCreator.createVerticalLayout(this);
        SuperToolbar toolbar = new SuperToolbar(this);
        toolbar.addMenuItem(getResources().getDrawable(R.drawable.sym_board_return), new View.OnClickListener(){

				@Override
				public void onClick(View p1){
					
				}

		});
        toolbar.setTextColor(0xFFFFFFFF);
        main.addView(toolbar);
        BackupRestoreListView expandable = new BackupRestoreListView(this);
        main.addView(expandable);
        setContentView(main);
    }

    private String getTranslation(String key) {
        return getTranslation(this, key);
    }

    public static String getTranslation(Context context, String key){
		String requestedKey = "settings_backup_" + key;
		try {
			return context.getString(context.getResources().getIdentifier(requestedKey, "string", context.getPackageName()));
		} catch(Throwable t){}
		return requestedKey;
	}

}