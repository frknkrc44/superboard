package org.blinksd.board;

import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.inputmethodservice.*;
import android.os.*;
import android.text.*;
import android.util.*;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;
import java.util.*;

import static android.view.View.*;
import static android.view.Gravity.*;

public class SuperBoard extends FrameLayout {

	private int selected = 0, shift = 0, keyclr = -1, /*ori = 0,*/ hp = 40, y, TAG_LP = R.string.app_name, TAG_NP = R.string.hello_world;
	private boolean block = false, clear = false, lng = false;
	private Drawable keybg = null, kbdbg = null;
	private String KEY_REPEAT = "10RePeAt01", x[];
	public static final int KEYCODE_CLOSE_KEYBOARD = -100;
	private Handler h = new Handler(){
		@Override
		public void handleMessage(Message msg){
			block = false;
			removeMessages(0);
		}
	};
	
	//PopupView pv = null;

	public SuperBoard(Context c){
		super(c);
		//ori = getResources().getConfiguration().orientation;
		disableSystemSuggestions();
		setLayoutParams(new LayoutParams(-1,-1));
		setBackgroundColor(0xFF212121);
		createEmptyLayout();
		setKeyboardHeight(hp);
	}
	
	private void disableSystemSuggestions(){
		Locale loc = new Locale("tr","TR");
		Locale.setDefault(loc);
		Configuration c = new Configuration();
		c.locale = loc;
		getResources().updateConfiguration(c,null);
	}
	
	public void setPadding(int p){
		setPadding(p,p,p,p);
	}

	public static int dp(int px){
		return (int)(Resources.getSystem().getDisplayMetrics().density * px);
	}
	
	public int getKeyboardHeight(){
		return getLayoutParams().height;
	}
	
	public int getKeyboardHeightPercent(){
		return hp;
	}
	
	public void fixHeight(){
		setKeyboardHeight(hp);
		for(int i = 0;i < getChildCount();i++){
			for(int g = 0;g < getKeyboard(i).getChildCount();g++){
				getRow(i,g).setKeyWidths();
			}
		}
	}

	public void setBackground(Drawable background){
		setBackgroundDrawable(background);
	}

	public void setBackgroundDrawable(Drawable background){
		super.setBackgroundDrawable(kbdbg = background.getConstantState().newDrawable());
	}

	public void setBackgroundColor(int color){
		super.setBackgroundColor(color);
		kbdbg = getBackground();
	}

	public void setBackgroundResource(int resid){
		super.setBackgroundResource(resid);
		kbdbg = getBackground();
	}

	public void setBackgroundTintMode(PorterDuff.Mode tintMode){
		super.setBackgroundTintMode(tintMode);
		if(kbdbg == null) kbdbg = getBackground();
		kbdbg.setTintMode(tintMode);
	}

	public void setBackgroundTintList(ColorStateList tint){
		super.setBackgroundTintList(tint);
		if(kbdbg == null) kbdbg = getBackground();
		kbdbg.setTintList(tint);
	}

	public void clear(){
		if(clear){
			for(int i = 0;i < getChildCount();i++){
				ViewGroup k = getKeyboard(i);
				for(int g = 0;g < k.getChildCount();g++)
					getRow(i,g).removeAllViewsInLayout();
				k.removeAllViewsInLayout();
			}
			removeAllViewsInLayout();
			createEmptyLayout();
			clear = false;
		}
	}
	
	public void setKeyTintColor(int keyboardIndex, int rowIndex, int keyIndex, int color){
		getKey(keyboardIndex, rowIndex, keyIndex).getBackground().setColorFilter(color,PorterDuff.Mode.SRC_ATOP);
	}

	public void setKeyRepeat(int keyboardIndex, int rowIndex, int keyIndex){
		setKeyRepeat(keyboardIndex, rowIndex, keyIndex, true);
	}

	public void setKeyRepeat(int keyboardIndex, int rowIndex, int keyIndex, boolean repeat){
		getKey(keyboardIndex, rowIndex, keyIndex).setHint(repeat ? KEY_REPEAT : "");
	}

	public boolean isKeyRepeat(int keyboardIndex, int rowIndex, int keyIndex){
		return isKeyRepeat(getKey(keyboardIndex, rowIndex, keyIndex));
	}

	private boolean isKeyRepeat(View v){
		CharSequence cs = ((Key)v).getHint();
		if(cs == null) return false;
		return cs.toString().equals(KEY_REPEAT);
	}

	public void setKeyWidthPercent(int keyboardIndex, int rowIndex, int keyIndex, int percent){
		Key k = getKey(keyboardIndex,rowIndex,keyIndex);
		k.getLayoutParams().width = wp(percent);
		k.setId(percent);
	}
	
	private int getScreenWidth(){
		return getResources().getDisplayMetrics().widthPixels;
	}
	
	private int getScreenHeight(){
		return getResources().getDisplayMetrics().heightPixels;
	}
	
	public int wp(int percent){
		return (int)((getScreenWidth() / 100f) * percent);
	}
	
	public int hp(int percent){
		return (int)((getScreenHeight() / 100f) * percent);
	}
	
	public int mp(int percent){
		return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? hp(percent) : wp(percent);
	}
	
	/*private boolean isHasPopup(View v){
		CharSequence cs = ((TextView)v).getHint();
		if(cs == null) return false;
		return !isKeyRepeat(v);
	}
	
	public void setPopupForKey(int keyboardIndex, int rowIndex, int keyIndex, String chars){
		for(String s : chars.split(""))
			if(!tmp.contains(s))
				tmp += s;
		getKey(keyboardIndex, rowIndex, keyIndex).setHint(tmp);
		tmp = "";
		if(pv == null){
			pv = new PopupView(getContext());
		}
	}

	public void setLayoutPopup(int keyboardIndex,String[][] chars){
		if(keyboardIndex < getChildCount() && keyboardIndex >= 0){
			ViewGroup v = getKeyboard(keyboardIndex), r = null;
			for(int i = 0;i < v.getChildCount();i++){
				r = getRow(keyboardIndex,i);
				for(int g = 0;g < r.getChildCount();g++)
					setPopupForKey(keyboardIndex,i,g,chars[i][g]);
			}
		} else throw new RuntimeException("Invalid keyboard index number");
	}*/

	public void setKeyTextColor(int color){
		ViewGroup k = null,r = null;
		keyclr = color;
		for(int j = 0;j < getChildCount();j++){
			k = getKeyboard(j);
			for(int i = 0;i < k.getChildCount();i++){
				r = getRow(j,i);
				for(int g = 0;g < r.getChildCount();g++){
					Key t = (Key)r.getChildAt(g);
					if(t.isKeyIconSet()){
						t.getKeyIcon().setColorFilter(color,PorterDuff.Mode.SRC_ATOP);
					} else {
						t.setTextColor(color);
					}
				}
			}
		}
	}

	public void setKeyBackground(Drawable d){
		ViewGroup k = null,r = null;
		keybg = d;
		for(int j = 0;j < getChildCount();j++){
			k = getKeyboard(j);
			for(int i = 0;i < k.getChildCount();i++){
				r = getRow(j,i);
				for(int g = 0;g < r.getChildCount();g++){
					((Key)r.getChildAt(g)).setBackgroundDrawable(d);
				}
			}
		}
	}

	public void setKeyboardHeight(int percent){
		if(percent > 0 && percent < 80){
			hp = percent;
			getLayoutParams().height = hp(percent);
			if(getChildCount() > 0){
				for(int i = 0;i < getChildCount();i++){
					getChildAt(i).getLayoutParams().height = getLayoutParams().height;
				}
			}
		} else throw new RuntimeException("Invalid keyboard height");
	}

	public void setKeyLongClickEvent(int keyboardIndex, int rowIndex, int keyIndex, OnLongClickListener event){
		getKey(keyboardIndex, rowIndex, keyIndex).setOnLongClickListener(event);
	}
	
	public void setKeyDrawable(int keyboardIndex, int rowIndex, int keyIndex, int resId){
		setKeyDrawable(keyboardIndex, rowIndex, keyIndex, getResources().getDrawable(resId));
	}

	public void setKeyDrawable(int keyboardIndex, int rowIndex, int keyIndex, Drawable d){
		d.setColorFilter(keyclr,PorterDuff.Mode.SRC_ATOP);
		Key t = getKey(keyboardIndex, rowIndex, keyIndex);
		((LinearLayout.LayoutParams)t.getLayoutParams()).gravity = CENTER;
		t.setKeyIcon(d);
	}

	public int getEnabledLayoutIndex(){
		return selected;
	}

	public void setEnabledLayout(int keyboardIndex){
		if(keyboardIndex < 0) keyboardIndex += getChildCount();
		if(keyboardIndex < getChildCount() && keyboardIndex >= 0){
			if(getChildCount() == 1 || keyboardIndex == selected) return;
			selected = keyboardIndex;
			for(int i = 0;i < getChildCount();i++){
				getChildAt(i).setVisibility(i == keyboardIndex ? VISIBLE : GONE);
			}
		} else throw new RuntimeException("Invalid keyboard index number");
	}
	
	public void createLayoutWithRows(String[][] keys, KeyboardType type){
		createEmptyLayout(type);
		addRows(getChildCount()-1,keys);
	}

	public void createLayoutWithRows(String[][] keys){
		createLayoutWithRows(keys,KeyboardType.TEXT);
	}
	
	public void createEmptyLayout(){
		createEmptyLayout(KeyboardType.TEXT);
	}

	public void createEmptyLayout(KeyboardType type){
		LinearLayout ll = new LinearLayout(getContext());
		ll.setLayoutParams(new LayoutParams(-1,getLayoutParams().height));
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setTag(type);
		addView(ll);
		if(getChildCount() != 1){
			ll.setVisibility(GONE);
		}
	}

	public ViewGroup getCurrentKeyboard(){
		return getKeyboard(selected);
	}

	public ViewGroup getKeyboard(int keyboardIndex){
		return (ViewGroup)getChildAt(keyboardIndex);
	}
	
	/*private void replaceRowFromKeyboard(int keyboardIndex, int rowIndex, Row r){
		if(keyboardIndex < 0) keyboardIndex += getChildCount();
		if(rowIndex < 0) rowIndex += getKeyboard(keyboardIndex).getChildCount();
		getKeyboard(keyboardIndex).removeViewAt(rowIndex);
		getKeyboard(keyboardIndex).addView(r,rowIndex);
	}*/
	
	public void replaceRowFromKeyboard(int keyboardIndex, int rowIndex, String[] chars){
		getRow(keyboardIndex, rowIndex).removeAllViewsInLayout();
		for(String chr : chars){
			addKeyToRow(keyboardIndex, rowIndex, chr);
		}
	}
	
	public void removeRowFromKeyboard(int keyboardIndex, int rowIndex){
		if(keyboardIndex < 0) keyboardIndex += getChildCount();
		if(rowIndex < 0) rowIndex += getKeyboard(keyboardIndex).getChildCount();
		getRow(keyboardIndex, rowIndex).removeAllViewsInLayout();
		getKeyboard(keyboardIndex).removeViewAt(rowIndex);
	}
	
	public void removeKeyFromRow(int keyboardIndex, int rowIndex, int keyIndex){
		if(keyboardIndex < 0) keyboardIndex += getChildCount();
		if(rowIndex < 0) rowIndex += getKeyboard(keyboardIndex).getChildCount();
		if(keyIndex < 0) keyIndex += getRow(keyboardIndex,rowIndex).getChildCount();
		getRow(keyboardIndex,rowIndex).removeViewAt(keyIndex);
	}

	public Row getRow(int keyboardIndex, int rowIndex){
		if(rowIndex < 0) rowIndex += getKeyboard(keyboardIndex).getChildCount();
		return (Row)getKeyboard(keyboardIndex).getChildAt(rowIndex);
	}

	public Key getKey(int keyboardIndex, int rowIndex, int keyIndex){
		if(keyboardIndex < 0) keyboardIndex += getChildCount();
		if(rowIndex < 0) rowIndex += getKeyboard(keyboardIndex).getChildCount();
		if(keyIndex < 0) keyIndex += getRow(keyboardIndex,rowIndex).getChildCount();
		return (Key)getRow(keyboardIndex,rowIndex).getChildAt(keyIndex);
	}

	public void setKeyPopup(int keyboardIndex, int rowIndex, int keyIndex, String chars){
		getKey(keyboardIndex, rowIndex, keyIndex).setHint(chars);
	}

	public void addRows(int keyboardIndex,String[][] keys){
		for(String[] key : keys){
			addRow(keyboardIndex,key);
		}
	}
	
	public void addKeyToRow(int keyboardIndex, int rowIndex, String key){
		Row r = (Row) getRow(keyboardIndex, rowIndex);
		Key k = new Key(getContext());
		k.setText(key);
		r.addKey(k);
		r.setKeyWidths();
	}

	public void addRow(int keyboardIndex,String... keys){
		clear = true;
		Row r = new Row(getContext());
		if(keys.length > 0){
			for(String key : keys){
				Key k = new Key(getContext());
				k.setText(key);
				k.getLayoutParams().width = wp(100 / keys.length);
				r.addKey(k);
			}
		}
		getKeyboard(keyboardIndex).addView(r);
	}
	
	public void sendDefaultKeyboardEvent(View v){
		defaultKeyboardEvent((Key)v);
	}

	private void defaultKeyboardEvent(Key v){
		if(v.getTag(TAG_NP) != null){
			x = v.getTag(TAG_NP).toString().split(":");
			switch(y = Integer.parseInt(x[0])){
				case Keyboard.KEYCODE_SHIFT:
					setShiftState();
					break;
				case Keyboard.KEYCODE_CANCEL:
					setEnabledLayout((selected - 1) >= 0 ? selected - 1 : findSymbolKeyboardIndex());
					break;
				case Keyboard.KEYCODE_MODE_CHANGE:
					setEnabledLayout(selected==0?findSymbolKeyboardIndex():findNormalKeyboardIndex());
					if(getEnabledLayoutIndex() == findNormalKeyboardIndex() && shift != 2)
						updateKeyState();
					break;
				case Keyboard.KEYCODE_ALT:
					setEnabledLayout((selected + 1) % getChildCount());
					break;
				case Keyboard.KEYCODE_DELETE:
					sendKeyEvent(KeyEvent.KEYCODE_DEL);
					if(getEnabledLayoutIndex() == findNormalKeyboardIndex() && shift != 2)
						updateKeyState();
					break;
				case Keyboard.KEYCODE_DONE:
					sendKeyEvent(KeyEvent.KEYCODE_ENTER);
					break;
				default:
					if(Boolean.parseBoolean(x[1])){
						sendKeyEvent(y);
					} else {
						commitText(x[0]);
					}
					break;
			}
		} else {
			commitText(v.getText().toString());
			if(getEnabledLayoutIndex() == findNormalKeyboardIndex() && shift != 2)
				updateKeyState();
		}
	}
	
	private InputMethodService getServiceContext(){
		return curr;
	}
	
	private InputConnection getCurrentIC(){
		return getServiceContext().getCurrentInputConnection();
	}

	private void sendKeyEvent(int code){
		switch(code){
			case KEYCODE_CLOSE_KEYBOARD:
				closeKeyboard();
				break;
			default:
				getCurrentIC().sendKeyEvent(new KeyEvent(System.currentTimeMillis(),System.currentTimeMillis(),KeyEvent.ACTION_DOWN,code,0,0,0,0));
		}
	}

	private void commitText(String text){
		if(text == null) return;
		getCurrentIC().commitText(text,text.length());
	}

	private void setShiftState(){
		setShiftState(shift = (shift+1) % 3);
	}
	
	/*private void fixKeyChanges(Configuration c){
		ViewGroup k = getCurrentKeyboard(),r = null;
		Key t = null;
		for(int i = 0;i < k.getChildCount();i++){
			r = getRow(selected,i);
			for(int g = 0;g < r.getChildCount();g++){
				t = (Key) r.getChildAt(g);
				t.setKeyIconPadding(mp(4));
				//t.onConfigurationChanged(c);
			}
		}
	}*/

	public void setShiftState(int state){
		shift = state;
		ViewGroup k = getCurrentKeyboard(),r = null;
		Key t = null;
		for(int i = 0;i < k.getChildCount();i++){
			r = getRow(selected,i);
			for(int g = 0;g < r.getChildCount();g++){
				t = (Key) r.getChildAt(g);
				if(t.getTag(TAG_NP) == null && t.getTag(TAG_LP) == null){
					if(t.getText() != null){
						if(state > 0){
							t.setText(t.getText().toString().toUpperCase(new Locale("tr","TR")));
						} else {
							t.setText(t.getText().toString().toLowerCase(new Locale("tr","TR")));
						}
					}
				}
			}
		}
	}
	
	InputMethodService curr = null;
	
	private void updateKeyState(){
		updateKeyState(curr);
	}

	public void updateKeyState(InputMethodService s){
		if(!s.equals(curr)){
			curr = s;
		}
		EditorInfo ei = s.getCurrentInputEditorInfo();
		/*Row t = null;
		Key k = null;*/
		switch (ei.inputType & InputType.TYPE_MASK_CLASS){
			case InputType.TYPE_CLASS_NUMBER:
            case InputType.TYPE_CLASS_PHONE:
				setEnabledLayout(findNumberKeyboardIndex());
				break;
			//case /*InputType.TYPE_MASK_VARIATION & */InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS:
			//case /*InputType.TYPE_MASK_VARIATION & */InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
				//setEnabledLayout(0);
				/*t = (Row) getRow(0,-1);
				k = null;
				for(int i = 0;i < t.getChildCount();i++){
					if((k = (Key)t.getChildAt(i)).getText().toString().equals(",")){
						k.setText("@");
					}
				}*/
				//break;
			default:
				setEnabledLayout(findNormalKeyboardIndex());
				/*t = (Row) getRow(0,-1);
				k = null;
				for(int i = 0;i < t.getChildCount();i++){
					if((k = (Key)t.getChildAt(i)).getText().toString().equals("@")){
						k.setText(",");
					}
				}*/
				int caps = ei.inputType != InputType.TYPE_NULL 
					? s.getCurrentInputConnection().getCursorCapsMode(ei.inputType)
					: 0;
				setShiftState(caps==0?0:1);
				break;
		}
    }

	@Override
	protected void onConfigurationChanged(Configuration newConfig){
		/*if(ori != newConfig.orientation){
			ori = newConfig.orientation;
		}*/
		fixHeight();
	}
	
	public void setRowPadding(int keyboardIndex, int rowIndex, int padding){
		getRow(keyboardIndex, rowIndex).setPadding(padding,0,padding,0);
	}
	
	private boolean isHasLongPressEvent(View v){
		return v.getTag(TAG_LP) != null;
	}
	
	public void setPressEventForKey(int keyboardIndex, int rowIndex, int keyIndex, int keyCode){
		setPressEventForKey(keyboardIndex, rowIndex, keyIndex, keyCode, true);
	}
	
	public void setPressEventForKey(int keyboardIndex, int rowIndex, int keyIndex, int keyCode, boolean isEvent){
		getKey(keyboardIndex, rowIndex, keyIndex).setTag(TAG_NP,keyCode+":"+isEvent);
	}
	
	public void setLongPressEventForKey(int keyboardIndex, int rowIndex, int keyIndex, int keyCode){
		setLongPressEventForKey(keyboardIndex, rowIndex, keyIndex, keyCode, true);
	}
	
	public void setLongPressEventForKey(int keyboardIndex, int rowIndex, int keyIndex, int keyCode, boolean isEvent){
		getKey(keyboardIndex, rowIndex, keyIndex).setTag(TAG_LP,keyCode+":"+isEvent);
		//getKey(keyboardIndex, rowIndex, keyIndex).setId(keyCode);
	}
	
	public void closeKeyboard(){
		getServiceContext().requestHideSelf(0);
		//System.exit(0);
	}
	
	private int findSymbolKeyboardIndex(){
		for(int i = 0;i < getChildCount();i++){
			if(getChildAt(i).getTag() != null && 
			   getChildAt(i).getTag().equals(KeyboardType.SYMBOL)){
				return i;
			}
		}
		Log.e(getClass().getSimpleName(),"No number keyboard set, falling back to normal keyboard ...");
		return findNormalKeyboardIndex();
	}
	
	private int findNormalKeyboardIndex(){
		for(int i = 0;i < getChildCount();i++){
			if(getChildAt(i).getTag() == null || getChildAt(i).getTag().equals(KeyboardType.TEXT)){
				return i;
			}
		}
		Log.e(getClass().getSimpleName(),"No normal keyboard set, crashing ...");
		throw new RuntimeException("You must set a normal keyboard for input");
	}
	
	private int findNumberKeyboardIndex(){
		for(int i = 0;i < getChildCount();i++){
			if(getChildAt(i).getTag() != null && 
				getChildAt(i).getTag().equals(KeyboardType.NUMBER)){
				return i;
			}
		}
		Log.e(getClass().getSimpleName(),"No number keyboard set, falling back to normal keyboard ...");
		return findNormalKeyboardIndex();
	}
	
	public static enum KeyboardType { TEXT, SYMBOL, NUMBER }
	
	/*private class PopupView {
		
		SuperBoard sb = null;
		String pc = "";
		int x,y;
		
		PopupView(Context c){
			if(sb == null){
				sb = new SuperBoard(c);
				sb.setKeyboardHeight(hp);
				addView(sb);
			} else {
				sb.clear();
			}
			sb.setVisibility(View.GONE);
			sb.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v){
					sb.setVisibility(View.GONE);
				}
			});
		}
		
		void setChars(TextView key){
			pc = key.getHint().toString();
			x = (int) key.getX();
			y = (int) key.getY();
			for(int i = 0;i < pc.length();i++){
				if((i != 0) && (i % 6 == 0)){
					sb.createEmptyLayout();
					sb.addRow(0,pc.charAt(i)+"");
				} else {
					sb.addKeyToRow(0,-1,pc.charAt(i)+"");
				}
			}
		}
		
		public boolean isShowing(){
			return sb.isShown();
		}
		
		public void show(){
			sb.setVisibility(View.VISIBLE);
		}
		
		public void hide(){
			sb.setVisibility(View.GONE);
		}
	}*/

	private class Row extends LinearLayout {

		Row(Context c){
			super(c);
			setLayoutParams(new LinearLayout.LayoutParams(-1,-1,1));
		}

		void addKey(Key k){
			if(k != null){
				addView(k);
			} else {
				throw new NullPointerException("Key is not be null");
			}
		}
		
		void setKeyWidths(){
			for(int i = 0;i < getChildCount();i++){
				Key k = (Key) getChildAt(i);
				k.setId(k.getId() < 1 ? 100 / getChildCount() : k.getId());
				k.getLayoutParams().width = wp(k.getId());
			}
		}
	}

	private class Key extends LinearLayout {
		
		TextView t = null;
		ImageView i = null;
		Drawable d = null;
		
		public boolean isKeyIconSet(){
			return i.isShown();
		}
		
		public int getKeyWidth(){
			return getLayoutParams().width;
		}
		
		int ori = 0, pad = 0;

		@Override
		protected void onConfigurationChanged(Configuration newConfig){
			if(ori != newConfig.orientation){
				ori = newConfig.orientation;
				setKeyIconPadding();
			} else super.onConfigurationChanged(newConfig);
		}

		Key(Context c){
			super(c);
			ori = getResources().getConfiguration().orientation;
			pad = (ori == Configuration.ORIENTATION_LANDSCAPE ? mp(2) : mp(4));
			setLayoutParams(new LinearLayout.LayoutParams(-1,-1,1));
			t = new TextView(c);
			t.setLayoutParams(new LinearLayout.LayoutParams(-1,-1));
			i = new ImageView(c);
			i.setLayoutParams(new LinearLayout.LayoutParams(-1,-1));
			addView(t);
			addView(i);
			i.setScaleType(ImageView.ScaleType.FIT_CENTER);
			setKeyIconPadding();
			i.setVisibility(View.GONE);
			//t.setVisibility(View.GONE);
			t.setTextColor(keyclr!=-1?keyclr:(keyclr=0xFFDEDEDE));
			setGravity(CENTER);
			t.setGravity(CENTER);
			t.setHintTextColor(0);
			t.setTextSize(TypedValue.COMPLEX_UNIT_DIP,18);
			setBackground(keybg);
			setOnTouchListener(new OnTouchListener(){
					@Override
					public boolean onTouch(View v, MotionEvent m){
						if(isHasLongPressEvent(v)){
							if(m.getAction() != m.ACTION_UP){
								if(!block){
									if(!lng){
										block = lng = true;
										h.removeMessages(0);
										h.sendEmptyMessageDelayed(0,500);
									} else {
										x = v.getTag(TAG_LP).toString().split(":");
										y = Integer.parseInt(x[0]);
										if(Boolean.parseBoolean(x[1])){
											sendKeyEvent(y);
										} else {
											commitText((char)y+"");
										}
										lng = false;
										block = !lng;
									}
								}
							} else {
								h.removeMessages(0);
								if(lng) sendDefaultKeyboardEvent(v);
								block = lng = false;
							}
						} else
						/*if(isHasPopup(v)){
							if(m.getAction() != m.ACTION_UP){
								if(!block){
									if(!lng){
										block = true;
										h.sendEmptyMessageDelayed(0,1000);
									} else {
										pv.setChars((TextView)v);
										pv.show();
									}
								}
							} else {
								block = lng = false;
								if(pv.isShowing())
									sendDefaultKeyboardEvent(v);
							}
						} else */if(isKeyRepeat(v)){
							if(m.getAction() != m.ACTION_UP){
								if(!block){
									//if(!lng){
										if(!h.hasMessages(0)){
											block = true;
											h.sendEmptyMessageDelayed(0,20*(lng?1:23));
											if(!lng) lng = true;
											sendDefaultKeyboardEvent(v);
										}
									//}
								}
							} else {
								h.removeMessages(0);
								block = lng = false;
							}
						} else {
							if(m.getAction() == m.ACTION_DOWN){
								sendDefaultKeyboardEvent(v);
							}
						}
						return true;
					}
				});
		}

		public void setBackground(Drawable b){
			setBackgroundDrawable(b);
		}

		public void setBackgroundDrawable(Drawable b){
			super.setBackgroundDrawable(b.getConstantState().newDrawable());
		}
		
		public void setKeyIcon(Drawable dr){
			t.setVisibility(View.GONE);
			i.setVisibility(View.VISIBLE);
			i.setImageDrawable(d = dr);
		}
		
		private void setKeyIconPadding(){
			pad = mp(ori == Configuration.ORIENTATION_LANDSCAPE ? 2 : 4);
			i.setPadding(pad,pad,pad,pad);
		}
		
		public void setTextColor(int color){
			t.setTextColor(color);
		}
		
		public void setText(CharSequence cs){
			i.setVisibility(View.GONE);
			t.setVisibility(View.VISIBLE);
			t.setText(cs);
		}
		
		public CharSequence getText(){
			return t.getText();
		}
		
		public Drawable getKeyIcon(){
			return d;
		}
		
		public CharSequence getHint(){
			return t.getHint();
		}
		
		public void setHint(CharSequence cs){
			t.setHint(cs);
		}
	}
}
