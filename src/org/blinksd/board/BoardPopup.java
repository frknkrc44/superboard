package org.blinksd.board;

import android.graphics.*;
import android.inputmethodservice.*;
import android.view.*;
import android.widget.*;
import org.superdroid.db.*;

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
		popupFilter.setVisibility(View.INVISIBLE);
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
	
	public void setKey(Key key, SuperDB sdb){
		key.clone(mKey);
		setKeysTextColor(key.getTextColor());
		khp = sdb.getInteger(Settings.Key.keyboard_height.name(),hp(10));
		int a = sdb.getInteger(Settings.Key.keyboard_bgclr.name(),0xFF000000);
		a = Color.argb(0xCC,Color.red(a),Color.green(a),Color.blue(a));
		setBackground(InputService.setKeyBg(sdb,this,a,false));
		setKeysBackground(InputService.setKeyBg(sdb,this,a,true));
		popupFilter.setBackgroundColor(a-0x33000000);
		mKey.setVisibility(INVISIBLE);
		key.getLocationInWindow(pos);
		mKey.setX(pos[0]);
		mKey.setY(pos[1] - (pos[1] >= (a = mKey.getLayoutParams().height) ? a : 0));
	}

	public void showCharacter(){
		mKey.setVisibility(VISIBLE);
	}
	
	public void hideCharacter(){
		mKey.setVisibility(INVISIBLE);
	}
	
	public void showPopup(){
		showPopup(true);
	}
	
	public void showPopup(boolean visible){
		hideCharacter();
		setVisibility(visible && mKey.getHint() != null && mKey.getHint().length() > 0?VISIBLE:GONE);
		popupFilter.setVisibility(getVisibility());
		if(isShown()){
			setCharacters(mKey.getHint());
		}
	}

	@Override
	public void setVisibility(int visibility){
		super.setVisibility(visibility);
		mRoot.setFocusable(visibility != VISIBLE);
	}
	
	private void setCharacters(CharSequence chr){
		clear();
		String[] u = chr.toString().split(""),y = new String[u.length-1];
		for(int i = 1;i < u.length;i++){
			y[i-1] = u[i];
		}
		createPopup(y);
		setKeysShadow(mKey.shr,mKey.shc);
	}
	
	private void createPopup(String[] a){
		int h = getChildAt(0).getLayoutParams().width, c = 6;
		setKeyboardWidth(a.length < c ? 11*a.length : 11*c);
		setX(wp(50-(getKeyboardWidthPercent()/2)));
		h = a.length / c;
		h = h > 0 ? h : 1;
		h += ((a.length > (c - 1)) && a.length % c == (c - 1)) ? 1 :0;
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
}
