package org.blinksd.board.backup;

import android.content.*;
import android.widget.*;
import org.blinksd.board.*;
import org.blinksd.utils.layout.*;

public class BackupOptionsSelectorLayout extends RadioGroup {

    String translation;

    public BackupOptionsSelectorLayout(BackupRestoreActivity context, String translation) {
        super(context);
        this.translation = translation;
        setOrientation(VERTICAL);
        createMain();
    }

    public void createMain(){
        int dp = DensityUtils.dpInt(8);
        setPadding(dp*2, dp, dp*2, dp);
        BackupRestoreMap map = new BackupRestoreMap();
        for(String item : map.keySet()) {
            CustomRadioButton box = new CustomRadioButton(getContext());
            box.setLayoutParams(new LayoutParams(-1, -2));
            box.setText(BackupRestoreActivity.getTranslation(getContext(), item));
            box.setTag(map.get(item));
            addView(box);
        }
    }

    public static enum BackupOptionType {
		ALL,
        THEME,
        OTHER,
	}

}