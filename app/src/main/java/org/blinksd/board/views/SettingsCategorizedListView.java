package org.blinksd.board.views;

import android.content.Context;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;

import org.blinksd.board.activities.AppSettingsV2;

public class SettingsCategorizedListView extends ExpandableListView implements OnGroupCollapseListener, OnGroupExpandListener {
    public final SettingsCategorizedListAdapter mAdapter;
    private int expandIndex = -1;

    public SettingsCategorizedListView(Context context) {
        super(context);
        assert context instanceof AppSettingsV2 : "context must be AppSettingsV2";
        mAdapter = new SettingsCategorizedListAdapter((AppSettingsV2) context);
        setAdapter(mAdapter);
        setDivider(null);
        setOnGroupExpandListener(this);
        setOnGroupCollapseListener(this);
        expandGroup(0);
    }

    @Override
    public void onGroupCollapse(int p1) {
        expandIndex = -1;
    }

    @Override
    public void onGroupExpand(int p1) {
        if (expandIndex >= 0 && isGroupExpanded(expandIndex)) {
            collapseGroup(expandIndex);
        }
        expandIndex = p1;
    }
}
