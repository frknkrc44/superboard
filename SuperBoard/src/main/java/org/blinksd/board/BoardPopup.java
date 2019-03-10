package org.blinksd.board;

import android.content.res.*;
import android.view.*;

public class BoardPopup extends SuperBoard {
	
	private static ViewGroup mRoot;
	private Key mKey;
	private static int[] pos = new int[2];
	
	public BoardPopup(ViewGroup root){
		super((mRoot = root).getContext());
		mKey = new Key(getContext());
		mKey.setOnTouchListener(null);
		root.addView(mKey);
		mKey.setVisibility(GONE);
		setVisibility(GONE);
		cc();
	}
	
	public void setKey(Key key){
		key.clone(mKey);
		mKey.setVisibility(GONE);
		key.getLocationInWindow(pos);
		mKey.setX(pos[0]);
		mKey.setY(pos[1] - (pos[1] >= mKey.getLayoutParams().height ? mKey.getLayoutParams().height : 0));
		setCharacters(key.getHint());
	}

	public void showCharacter(){
		mKey.setVisibility(VISIBLE);
	}
	
	public void hideCharacter(){
		mKey.setVisibility(GONE);
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
