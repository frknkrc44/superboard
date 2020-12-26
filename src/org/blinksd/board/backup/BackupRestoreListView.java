package org.blinksd.board.backup;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.*;
import java.util.*;
import org.blinksd.board.*;

public class BackupRestoreListView extends ExpandableListView implements OnGroupCollapseListener, OnGroupExpandListener {
    private BackupRestoreAdapter adapter;
    private int expandIndex = -1;

    public BackupRestoreListView(Context context) {
        super(context);
        List<String> titles = new ArrayList<String>();
        titles.add(BackupRestoreActivity.getTranslation(context, "menu_backup"));
        titles.add(BackupRestoreActivity.getTranslation(context, "menu_restore"));
        adapter = new BackupRestoreAdapter(titles);
        setAdapter(adapter);
        setOnGroupExpandListener(this);
		setOnGroupCollapseListener(this);
    }

    @Override
	public void onGroupCollapse(int p1){
		expandIndex = -1;
	}
	
	@Override
	public void onGroupExpand(int p1){
		if(expandIndex >= 0 && isGroupExpanded(expandIndex)){
			collapseGroup(expandIndex);
		}
		expandIndex = p1;
	}


}