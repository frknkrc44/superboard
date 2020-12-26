package org.blinksd.board.backup;

import android.database.DataSetObserver;
import android.view.*;
import android.widget.*;
import java.util.*;
import org.blinksd.board.*;

public class BackupRestoreAdapter implements ExpandableListAdapter {

    private List<String> titles;

    public BackupRestoreAdapter(List<String> titles) {
        if(titles == null) {
            throw new RuntimeException("titles cannot be null");
        }
        this.titles = titles;
    }

    @Override
	public void registerDataSetObserver(DataSetObserver p1){}

	@Override
	public void unregisterDataSetObserver(DataSetObserver p1){}

	@Override
	public int getGroupCount(){
		return titles.size();
	}

	@Override
	public int getChildrenCount(int p1){
		return 1;
	}

	@Override
	public Object getGroup(int p1){
		return titles.get(p1);
	}

	@Override
	public Object getChild(int p1, int p2){
		return getGroup(p1);
	}

	@Override
	public long getGroupId(int p1){
		return p1;
	}

	@Override
	public long getChildId(int p1, int p2){
		return getGroupId(p1);
	}

	@Override
	public boolean hasStableIds(){
		return false;
	}

	@Override
	public View getGroupView(int p1, boolean p2, View p3, ViewGroup p4){
		View group = LayoutInflater.from(p4.getContext()).inflate(android.R.layout.simple_expandable_list_item_1, p4, false);
		TextView text = group.findViewById(android.R.id.text1);
		text.setText(titles.get(p1));
		return group;
	}

	@Override
	public View getChildView(int p1, int p2, boolean p3, View p4, ViewGroup p5){
		return new BackupOptionsSelectorLayout((BackupRestoreActivity) p5.getContext(), (String) getGroup(p1));
	}

	@Override
	public boolean isChildSelectable(int p1, int p2){
		return false;
	}

	@Override
	public boolean areAllItemsEnabled(){
		return false;
	}

	@Override
	public boolean isEmpty(){
		return titles.size() < 1;
	}

	@Override
	public void onGroupExpanded(int p1){}

	@Override
	public void onGroupCollapsed(int p1){}

	@Override
	public long getCombinedChildId(long p1, long p2){
		return getChildId((int) p1, (int) p2);
	}

	@Override
	public long getCombinedGroupId(long p1){
		return getGroupId((int) p1);
	}


}