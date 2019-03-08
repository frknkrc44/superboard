package org.blinksd.board;

import android.content.*;
import android.widget.*;
import android.view.*;
import android.content.res.*;

public class BoardPopup extends SuperBoard {
	
	private static ViewGroup mRoot;
	private static Key mKey;
	private static int[] pos = new int[2];
	
	public BoardPopup(ViewGroup root){
		super((mRoot = root).getContext());
		setVisibility(GONE);
		cc();
	}
	
	public void setKey(Key key){
		if(mKey != null) mRoot.removeView(mKey);
		mKey = key.clone(true);
		mKey.getTextView().setSingleLine();
		replacer();
		mRoot.addView(mKey);
		mKey.setVisibility(GONE);
		key.getLocationInWindow(pos);
		mKey.setX(pos[0]);
		mKey.setY(pos[1] - (pos[1] >= mKey.getLayoutParams().height ? mKey.getLayoutParams().height : 0));
		setCharacters(key.getHint());
	}

	private void replacer(){
		ViewGroup.LayoutParams p = mKey.getLayoutParams();
		if(p.width < 1) p.width = wp(mKey.getId() > 20 ? mKey.getId()/1.25f : 10);
		if(p.height < 1) p.height = hp(getKeyboardHeightPercent()/5);
	}
	
	public void showCharacter(){
		if(isKeyHasEvent(mKey) || mKey.isKeyIconSet())
			mKey.setVisibility(VISIBLE);
	}
	
	public void hideCharacter(){
		mRoot.removeView(mKey);
		mKey = null;
		System.gc();
	}
	
	public void showPopup(boolean visible){
		hideCharacter();
		setVisibility(visible?VISIBLE:GONE);
	}
	
	private void setCharacters(CharSequence chr){
		clear();
		if(chr != null) createPopup(chr.toString().split(""));
	}

	@Override
	protected void onConfigurationChanged(Configuration newConfig){
		cc();
	}
	
	public void cc(){
		setKeyboardHeight(20);
		setKeyboardWidth(40);
	}
	
	private void createPopup(String[] a){
		String[] x = null; 
		for(int i = 0;i < (a.length / 4) + 1;i++){
			x = new String[4];
			for(int g = 0;g < 4;g++){
				int j = (i * 4) + g;
				if(j < a.length){
					x[g] = a[j];
					continue;
				}
				break;
			}
			addRow(0,x);
		}
		for(int i = 0;i < getKeyboard(0).getChildCount();i++){
			getRow(0,i).setKeyWidths();
		}
		createEmptyLayout(KeyboardType.NUMBER);
	}
}
