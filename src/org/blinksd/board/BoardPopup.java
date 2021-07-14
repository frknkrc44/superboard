package org.blinksd.board;

import android.graphics.*;
import android.inputmethodservice.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import org.blinksd.sdb.*;
import org.blinksd.*;

public class BoardPopup extends SuperBoard {
	
	private static ViewGroup mRoot;
	private Key mKey;
	private static int pos[] = new int[2], khp = 0;
	private View popupFilter = null;
	
	public BoardPopup(ViewGroup root){
		super((mRoot = root).getContext());
		setKeyboardHeight(10);
		updateKeyState((InputMethodService)root.getContext());
		popupFilter = new View(root.getContext());
		popupFilter.setLayoutParams(new RelativeLayout.LayoutParams(-1,getKeyboardHeight()));
		popupFilter.setVisibility(View.GONE);
		mKey = new Key(getContext());
		mKey.setOnTouchListener(null);
		root.addView(popupFilter);
		root.addView(mKey);
		mKey.setVisibility(GONE);
		setVisibility(GONE);
	}
	
	public void setFilterHeight(int h){
		popupFilter.getLayoutParams().height = h;
	}
	
	public void setKey(Key key){
		setIconSizeMultiplier(getIntOrDefault(SettingMap.SET_KEY_ICON_SIZE_MULTIPLIER));
		key.clone(mKey);
		setKeysTextColor(key.getTextColor());
		setKeysTextType(key.txtst);
		setKeysShadow(key.shr,key.shc);
		khp = getIntOrDefault(SettingMap.SET_KEYBOARD_HEIGHT);
		int a = getIntOrDefault(SettingMap.SET_KEYBOARD_BGCLR);
		int ap = getIntOrDefault(SettingMap.SET_KEY_PRESS_BGCLR);
		a = Color.argb(0xCC,Color.red(a),Color.green(a),Color.blue(a));
		ap = Color.argb(0xCC,Color.red(ap),Color.green(ap),Color.blue(ap));
		setBackgroundDrawable(LayoutUtils.getKeyBg(a,ap,true));
		popupFilter.setBackgroundColor(a-0x33000000);
		mKey.setVisibility(GONE);
		key.getLocationInWindow(pos);
		mKey.setX(pos[0]);
		mKey.setY(pos[1] - (pos[1] >= (a = mKey.getLayoutParams().height) ? a : 0));
	}

	public void showCharacter(){
		mKey.setVisibility(VISIBLE);
	}
	
	public void hideCharacter(){
		mKey.setVisibility(GONE);
	}
	
	public void showPopup(){
		showPopup(true);
	}
	
	public void showPopup(boolean visible){
		hideCharacter();
		CharSequence hint = mKey.getHint();
		String str = hint != null ? hint.toString().trim() : "";
		setVisibility(visible && str != null && str.length() > 0?VISIBLE:GONE);
		popupFilter.setVisibility(getVisibility());
		if(isShown()){
			setCharacters(str);
		}
	}

	@Override
	public void setVisibility(int visibility){
		super.setVisibility(visibility);
		mRoot.setFocusable(visibility != VISIBLE);
	}
	
	private void setCharacters(String chr){
		clear();
		String[] u = chr.split("");
		createPopup(u);
		setKeysShadow(mKey.shr,mKey.shc);
	}
	
	private void createPopup(String[] a){
		int h = getChildAt(0).getLayoutParams().width, c = 6;
		setKeyboardWidth(a.length < c ? 11*a.length : 11*c);
		setX(wp(50-(getKeyboardWidthPercent()/2)));
		h = a.length / c;
		h = h > 0 ? h : 1;
		h += ((a.length > (c - 1)) && (a.length) % c > 0) ? 1 :0;
		setKeyboardHeight(10*h);
		setY(hp((khp-getKeyboardHeightPercent())/2));
		String[] x = null;
		for(int i = 0,k = 0;i < h;i++){
			x = new String[a.length < c ? a.length : c];
			x[0] = "";
			for(int g = 0;g < c;g++){
				k++;
				int j = (i * c) + g;
				if(j < a.length){
					x[g] = a[j];
					continue;
				}
				break;
			}
			if(!x[0].equals("") && k > 1) addRow(0,x);
		}
		for(int i = 0;i < getKeyboard(0).getChildCount();i++){
			getRow(0,i).setKeyWidths();
		}
	}

	@Override
	public void sendDefaultKeyboardEvent(View v){
		super.sendDefaultKeyboardEvent(v);
		showPopup(false);
		clear();
		System.gc();
	}
	
	@Override
	public void clear(){
		super.clear();
		mKey.setHint(null);
	}
	
	private SuperMiniDB getDB(){
		return SuperBoardApplication.getApplicationDatabase();
	}
	
	public int getIntOrDefault(String key){
		return getDB().getInteger(key, (int) SuperBoardApplication.getSettings().getDefaults(key));
	}
}
