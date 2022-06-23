package org.blinksd.utils.layout;

import android.content.*;
import android.widget.*;
import android.widget.ExpandableListView.*;
import org.blinksd.board.*;

public class SettingsCategorizedListView extends ExpandableListView implements OnGroupCollapseListener, OnGroupExpandListener {
	public final SettingsCategorizedListAdapter mAdapter;
	private int expandIndex = -1;
	
	public SettingsCategorizedListView(AppSettingsV2 context){
		super(context);
		mAdapter = new SettingsCategorizedListAdapter(context);
		setAdapter(mAdapter);
		setDivider(null);
		setOnGroupExpandListener(this);
		setOnGroupCollapseListener(this);
		expandGroup(0);
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
