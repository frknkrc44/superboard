package org.blinksd.board;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.inputmethodservice.InputMethodService;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import org.blinksd.utils.layout.DensityUtils;
import org.blinksd.utils.layout.LayoutUtils;
import org.superdroid.db.SuperDBHelper;

public class BoardPopup extends SuperBoard {

	private final ViewGroup mRoot;
	private final Key mKey;
	private static final int[] pos = new int[2];
	private static int khp = 0;
	private final View popupFilter;

	@SuppressLint("ClickableViewAccessibility")
	public BoardPopup(ViewGroup root){
		super(root.getContext());
		mRoot = root;
		setKeyboardHeight(10);
		pos[0] = pos[1] = 0;
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

	public void setKeyboardPrefs(){
		khp = getIntOrDefault(SettingMap.SET_KEYBOARD_HEIGHT);
		int a = getIntOrDefault(SettingMap.SET_KEYBOARD_BGCLR);
		int ap = getIntOrDefault(SettingMap.SET_KEY_PRESS_BGCLR);
		a = Color.argb(0xCC,Color.red(a),Color.green(a),Color.blue(a));
		ap = Color.argb(0xCC,Color.red(ap),Color.green(ap),Color.blue(ap));
		setBackgroundDrawable(LayoutUtils.getKeyBg(a,ap,true));
		popupFilter.setBackgroundColor(a-0x33000000);
		mKey.setVisibility(GONE);
	}

	public void setKey(Key key){
		setIconSizeMultiplier(getIntOrDefault(SettingMap.SET_KEY_ICON_SIZE_MULTIPLIER));
		key.getLocationInWindow(pos);
		key.clone(mKey);
		setKeysTextColor(key.getTextColor());
		setKeysTextType(key.txtst);
		setKeysShadow(key.shr,key.shc);
		mKey.setX(pos[0]);
		int a = mKey.getLayoutParams().height;
		mKey.setY(pos[1] - (pos[1] >= a ? a : 0));
		setKeyboardPrefs();
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
		setVisibility(visible && str.length() > 0 ?VISIBLE:GONE);
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
		int h, c = 6;
		setKeyboardWidth(a.length < c ? 11*a.length : 11*c);
		setX(DensityUtils.wpInt(50-(getKeyboardWidthPercent()/2f)));
		h = a.length / c;
		h = h > 0 ? h : 1;
		h += ((a.length > (c - 1)) && (a.length) % c > 0) ? 1 :0;
		setKeyboardHeight(10*h);
		setY(DensityUtils.hpInt((khp-getKeyboardHeightPercent())/2f));
		String[] x;
		for(int i = 0,k = 0;i < h;i++){
			x = new String[Math.min(a.length, c)];
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

	public int getIntOrDefault(String key){
		return SuperDBHelper.getIntValueOrDefault(key);
	}
}
