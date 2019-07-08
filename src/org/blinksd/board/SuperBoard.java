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
import org.blinksd.utils.image.*;
import android.view.InputDevice.*;

public class SuperBoard extends FrameLayout {

	protected int selected = 0, shift = 0, keyclr = -1, hp = 40, wp = 100, y, shrad = 0, shclr = 0, vib = 0, mult = 1, act = MotionEvent.ACTION_UP;
	protected float txtsze = -1;
	private static final int TAG_LP = R.string.app_name, TAG_NP = R.string.hello_world;
	private boolean clear = false, lng = false, lock = false;
	private Drawable keybg = null;
	private String KEY_REPEAT = "10RePeAt01", x[];
	public static final int KEYCODE_CLOSE_KEYBOARD = -100;
	public static final int KEYCODE_SWITCH_LANGUAGE = -101;
	protected Handler h = new Handler(){
		@Override
		public void handleMessage(Message msg){
			View v = null;
			if(msg.obj != null && msg.obj instanceof View){
				v = (View) msg.obj;
			}
			switch(msg.what){
				case 0:
					removeMessages(3);
					sendEmptyMessage(3);
					sendEmptyMessage(4);
					break;
				case 1:
					removeMessages(1);
					switch(act){
						case MotionEvent.ACTION_UP:
							removeMessages(3);
							sendEmptyMessage(3);
							break;
						case MotionEvent.ACTION_DOWN:
							if(isHasPopup(v)){
								onPopupEvent();
								removeMessages(3);
								sendEmptyMessage(3);
							} else if(isHasLongPressEvent(v)){
								String[] a = v.getTag(TAG_LP).toString().split(":");
								y = Integer.parseInt(a[0]);
								if(Boolean.parseBoolean(a[1])){
									sendKeyEvent(y);
								} else {
									commitText((char)y+"");
								}
								removeMessages(3);
								sendEmptyMessage(3);
							} else {
								Message m = obtainMessage(2,msg.obj);
								removeMessages(2);
								sendMessage(m);
							}
							break;
					}
					break;
				case 2:
					if(act == MotionEvent.ACTION_UP){
						removeMessages(3);
						sendEmptyMessage(3);
					} else {
						Message n = obtainMessage(1,msg.obj);
						sendMessageDelayed(n,((mult>1?15:20)*mult)*(lng?1:20));
						if(!lng) lng = true;
						sendDefaultKeyboardEvent(v);
					}
					break;
				case 3:
					removeMessages(3);
					lock = lng = false;
					afterKeyboardEvent();
					break;
				case 4:
					for(int i = 0;i <= 4;i++){
						if(i != 3){
							removeMessages(i);
						}
					}
					break;
				case 5:
					setEnabled(false);
					break;
				case 6:
					setEnabled(true);
					break;
			}
		}
	};
	
	Vibrator vb = null;

	public SuperBoard(Context c){
		super(c);
		if(c instanceof InputMethodService){
			curr = (InputMethodService) c;
		}
		vb = (Vibrator) c.getSystemService(c.VIBRATOR_SERVICE);
		trigSystemSuggestions();
		setLayoutParams(new LayoutParams(-1,-1));
		setBackgroundColor(0xFF212121);
		createEmptyLayout();
		setKeyboardHeight(hp);
	}
	
	public void onKeyboardEvent(View v){
		
	}
	
	public void afterKeyboardEvent(){
		
	}
	
	public void onPopupEvent(){
		
	}
	
	public void afterPopupEvent(){
		h.removeMessages(0);
		h.sendEmptyMessage(0);
	}
	
	public void switchLanguage(){
		
	}
	
	private void trigSystemSuggestions(){
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
	
	public int getKeyboardWidth(){
		return getLayoutParams().width;
	}
	
	public int getKeyboardWidthPercent(){
		return wp;
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
		super.setBackgroundDrawable(background.getConstantState().newDrawable());
	}
	
	public void setKeyVibrateDuration(int dur){
		vib = dur;
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
	
	public void setKeyTintColor(Key k, int color){
		Drawable d = k.getBackground();
		try {
			if(Build.VERSION.SDK_INT > 21){
				d.setTintList(getTintListWithStates(color));
			} else {
				d.setColorFilter(color,PorterDuff.Mode.SRC_ATOP);
			}
		} catch(Exception e){
			d.setColorFilter(color,PorterDuff.Mode.SRC_ATOP);
		}
	}
	
	public void setKeyTintColor(int keyboardIndex, int rowIndex, int keyIndex, int color){
		setKeyTintColor(getKey(keyboardIndex, rowIndex, keyIndex),color);
	}
	
	public ColorStateList getTintListWithStates(int color){
		return new ColorStateList(new int[][]{
			{android.R.attr.state_selected},{}
		},new int[]{getColorWithState(color,true),getColorWithState(color,false)});
	}
	
	public static int getColorWithState(int color, boolean selected){
		if(selected){
			int[] state = {Color.red(color),Color.green(color),Color.blue(color)};
			for(int i = 0;i < state.length;i++){
				state[i] /= 1.2;
			}
			return Color.argb(Color.alpha(color),state[0],state[1],state[2]);
		}
		return color;
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
	
	public boolean isKeyHasEvent(int keyboardIndex, int rowIndex, int keyIndex){
		return isKeyHasEvent(getKey(keyboardIndex,rowIndex,keyIndex));
	}
	
	public boolean isKeyHasEvent(Key k){
		return isKeyRepeat(k) || k.getTag(TAG_LP) != null || k.getTag(TAG_NP) != null || k.getText().toString().length() > 0;
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
	
	private static int getScreenWidth(){
		return Resources.getSystem().getDisplayMetrics().widthPixels;
	}
	
	private static int getScreenHeight(){
		return Resources.getSystem().getDisplayMetrics().heightPixels;
	}
	
	public static int wp(float percent){
		return (int)((getScreenWidth() / 100f) * percent);
	}
	
	public static int hp(float percent){
		return (int)((getScreenHeight() / 100f) * percent);
	}
	
	public static int mp(float percent){
		int x = wp(percent), y = hp(percent);
		return x < y ? x : y;
	}
	
	public final void setLongPressMultiplier(int multi){
		mult = multi;
	}
	
	private boolean isHasPopup(View v){
		CharSequence cs = ((Key)v).getHint();
		if(cs == null) return false;
		return (isKeyRepeat(v) == false) && (cs.length() > 0);
	}
	
	public void setPopupForKey(int keyboardIndex, int rowIndex, int keyIndex, String chars){
		String cs = "";
		for(String x : chars.split("")){
			if(!cs.contains(x)){
				cs += x;
			}
		}
		getKey(keyboardIndex, rowIndex, keyIndex).setHint(cs);
	}

	public void setLayoutPopup(int keyboardIndex,String[][] chars){
		if(chars != null){
			if(keyboardIndex < getChildCount() && keyboardIndex >= 0){
				ViewGroup v = getKeyboard(keyboardIndex), r = null;
				for(int i = 0;i < v.getChildCount();i++){
					r = getRow(keyboardIndex,i);
					for(int g = 0;g < r.getChildCount();g++)
						setPopupForKey(keyboardIndex,i,g,chars[i][g]);
				}
			} else throw new RuntimeException("Invalid keyboard index number");
		}
	}
	
	public void setKeysPadding(int p){
		for(int j = 0;j < getChildCount();j++){
			for(int i = 0;i < getKeyboard(j).getChildCount();i++){
				for(int g = 0;g < getRow(j,i).getChildCount();g++){
					Key k = getKey(j,i,g);
					Key.LayoutParams l = (Key.LayoutParams) k.getLayoutParams();
					l.bottomMargin = l.topMargin = l.leftMargin = l.rightMargin = p;
				}
			}
		}
	}

	public void setKeysTextColor(int color){
		keyclr = color;
		for(int j = 0;j < getChildCount();j++){
			for(int i = 0;i < getKeyboard(j).getChildCount();i++){
				for(int g = 0;g < getRow(j,i).getChildCount();g++){
					getKey(j,i,g).setKeyItemColor(color);
				}
			}
		}
	}
	
	public void setKeysTextSize(int size){
		txtsze = size;
		for(int j = 0;j < getChildCount();j++){
			for(int i = 0;i < getKeyboard(j).getChildCount();i++){
				for(int g = 0;g < getRow(j,i).getChildCount();g++){
					getKey(j,i,g).setKeyTextSize(size);
				}
			}
		}
	}

	public void setKeysBackground(Drawable d){
		keybg = d;
		for(int j = 0;j < getChildCount();j++){
			for(int i = 0;i < getKeyboard(j).getChildCount();i++){
				for(int g = 0;g < getRow(j,i).getChildCount();g++){
					getKey(j,i,g).setBackgroundDrawable(d);
				}
			}
		}
	}
	
	public void setKeysShadow(int radius, int color){
		shrad = radius;
		shclr = color;
		for(int j = 0;j < getChildCount();j++){
			for(int i = 0;i < getKeyboard(j).getChildCount();i++){
				for(int g = 0;g < getRow(j,i).getChildCount();g++){
					getKey(j,i,g).setKeyShadow(radius,color);
				}
			}
		}
	}

	public void setKeyboardHeight(int percent){
		//if(percent > 19 && percent < 81){
			hp = percent;
			getLayoutParams().height = hp(percent);
			if(getChildCount() > 0){
				for(int i = 0;i < getChildCount();i++){
					getChildAt(i).getLayoutParams().height = getLayoutParams().height;
				}
			}
			int x = selected;
			setEnabledLayout(findNumberKeyboardIndex());
			setEnabledLayout(x);
		//} else throw new RuntimeException("Invalid keyboard height");
	}
	
	public void setKeyboardWidth(int percent){
		//if(percent > 11 && percent < 101){
			wp = percent;
			getLayoutParams().width = wp(percent);
			if(getChildCount() > 0){
				for(int i = 0;i < getChildCount();i++){
					getChildAt(i).getLayoutParams().width = getLayoutParams().width;
				}
			}
			int x = selected;
			setEnabledLayout(findNumberKeyboardIndex());
			setEnabledLayout(x);
		//} else throw new RuntimeException("Invalid keyboard width");
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
	
	public void replaceNormalKeyboard(String[][] newKeyboard){
		ViewGroup vg = getKeyboard(findNormalKeyboardIndex());
		vg.removeAllViewsInLayout();
		addRows(findNormalKeyboardIndex(),newKeyboard);
	}
	
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
		if(keys != null){
			for(String[] key : keys){
				addRow(keyboardIndex,key);
			}
		}
	}
	
	public void addKeyToRow(int keyboardIndex, int rowIndex, String key){
		Row r = getRow(keyboardIndex, rowIndex);
		Key k = new Key(getContext());
		k.setText(key);
		r.addKey(k);
		r.setKeyWidths();
	}
	
	public void addRow(int keyboardIndex,String[] keys){
		addRow(keyboardIndex,null,keys);
	}

	public void addRow(int keyboardIndex,Key template,String[] keys){
		clear = true;
		Row r = new Row(getContext());
		if(keys.length > 0){
			for(String key : keys){
				Key k = new Key(getContext());
				if(template != null){
					template.clone(k);
				}
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
					switch(action){
						case EditorInfo.IME_ACTION_DONE:
						case EditorInfo.IME_ACTION_GO:
						case EditorInfo.IME_ACTION_SEARCH:
						case EditorInfo.IME_ACTION_SEND:
						case EditorInfo.IME_ACTION_NEXT:
						case EditorInfo.IME_ACTION_PREVIOUS:
							performEditorAction(action);
							break;
						default:
							sendKeyEvent(KeyEvent.KEYCODE_ENTER);
							break;
					}
					break;
				default:
					if(Boolean.parseBoolean(x[1])){
						sendKeyEvent(y);
					} else {
						commitText((char)y+"");
					}
					if(getEnabledLayoutIndex() == findNormalKeyboardIndex() && shift != 2)
						updateKeyState();
					break;
			}
		} else {
			commitText(v.getText().toString());
			if(getEnabledLayoutIndex() == findNormalKeyboardIndex() && shift != 2)
				updateKeyState();
		}
		if(vib > 0) vb.vibrate(vib);
	}
	
	protected InputMethodService getServiceContext(){
		return curr;
	}
	
	protected InputConnection getCurrentIC(){
		return getServiceContext().getCurrentInputConnection();
	}

	private void sendKeyEvent(int code){
		switch(code){
			case KEYCODE_CLOSE_KEYBOARD:
				closeKeyboard();
				break;
			case KEYCODE_SWITCH_LANGUAGE:
				switchLanguage();
				break;
			default:
				getCurrentIC().sendKeyEvent(new KeyEvent(System.currentTimeMillis(),System.currentTimeMillis(),KeyEvent.ACTION_DOWN,code,0,0,0,0));
		}
	}
	
	private void performEditorAction(int action){
		getCurrentIC().performEditorAction(action);
	}

	private void commitText(String text){
		if(text == null) return;
		getCurrentIC().commitText(text,text.length());
	}

	private void setShiftState(){
		setShiftState(shift = (shift+1) % 3);
	}

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
							t.setText(t.getText().toString().toUpperCase(loc));
						} else {
							t.setText(t.getText().toString().toLowerCase(loc));
						}
					}
				}
			}
		}
	}
	
	private static Locale loc = new Locale("tr","TR");
	
	public void setKeyboardLanguage(String lang){
		if(lang != null){
			String[] la = lang.split("_");
			loc = la.length > 1 ? new Locale(la[0],la[1]) : new Locale(la[0].toLowerCase(),la[0].toUpperCase());
			trigSystemSuggestions();
		}
	}
	
	InputMethodService curr = null;
	
	private void updateKeyState(){
		updateKeyState(curr);
	}
	
	int action = 0;

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
		
		action = ei.imeOptions & (EditorInfo.IME_MASK_ACTION | EditorInfo.IME_FLAG_NO_ENTER_ACTION);
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
	
	public int findSymbolKeyboardIndex(){
		for(int i = 0;i < getChildCount();i++){
			if(getChildAt(i).getTag() != null && 
			   getChildAt(i).getTag().equals(KeyboardType.SYMBOL)){
				return i;
			}
		}
		Log.e(getClass().getSimpleName(),"No number keyboard set, falling back to normal keyboard ...");
		return findNormalKeyboardIndex();
	}
	
	public int findNormalKeyboardIndex(){
		for(int i = 0;i < getChildCount();i++){
			if(getChildAt(i).getTag() == null || getChildAt(i).getTag().equals(KeyboardType.TEXT)){
				return i;
			}
		}
		Log.e(getClass().getSimpleName(),"No normal keyboard set, crashing ...");
		throw new RuntimeException("You must set a normal keyboard for input");
	}
	
	public int findNumberKeyboardIndex(){
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

	protected class Row extends LinearLayout {

		public Row(Context c){
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
				if(k.getId() < 1)
					k.setId(100 / getChildCount());
				k.getLayoutParams().width = wp(k.getId());
			}
		}
	}

	protected class Key extends LinearLayout {
		
		TextView t = null;
		ImageView i = null;
		protected int shr = 0, shc = 0;
		
		public boolean isKeyIconSet(){
			return i.getDrawable() != null;
		}
		
		public int getKeyWidth(){
			return getLayoutParams().width;
		}
		
		protected int getTextColor(){
			return keyclr;
		}

		Key(Context c){
			super(c);
			setLayoutParams(new LinearLayout.LayoutParams(-1,-1,1));
			t = new TextView(c);
			t.setLayoutParams(new LinearLayout.LayoutParams(-1,-1));
			i = new ImageView(c);
			i.setLayoutParams(new LinearLayout.LayoutParams(-1,-1));
			addView(t);
			addView(i);
			i.setScaleType(ImageView.ScaleType.FIT_CENTER);
			setKeyImageVisible(false);
			t.setTextColor(keyclr!=-1?keyclr:(keyclr=0xFFDEDEDE));
			t.setSingleLine();
			setGravity(CENTER);
			t.setGravity(CENTER);
			t.setHintTextColor(0);
			setKeyTextSize(txtsze!=1?txtsze:(txtsze=mp(1.25f)));
			setBackground(keybg);
			setOnTouchListener(new OnTouchListener(){
					@Override
					public boolean onTouch(View v, MotionEvent m){
						v.setSelected(m.getAction() != MotionEvent.ACTION_UP);
						if(isHasPopup(v) || isHasLongPressEvent(v) || isKeyRepeat(v)){
							switch(m.getAction()){
								case MotionEvent.ACTION_UP:
									act = MotionEvent.ACTION_UP;
									if(isKeyRepeat(v) == false && h.hasMessages(1)){
										sendDefaultKeyboardEvent(v);
									}
									h.removeMessages(3);
									h.sendEmptyMessage(3);
									break;
								case MotionEvent.ACTION_DOWN:
									act = MotionEvent.ACTION_DOWN;
									h.removeMessages(1);
									onKeyboardEvent(v);
									Message x = h.obtainMessage(1,v);
									if(isKeyRepeat(v)){
										h.sendMessage(x);
									} else {
										h.sendMessageDelayed(x,250*mult);
									}
									break;
							}
						} else {
							switch(m.getAction()){
								case MotionEvent.ACTION_UP:
									h.removeMessages(3);
									h.sendEmptyMessage(3);
									break;
								case MotionEvent.ACTION_DOWN:
									sendDefaultKeyboardEvent(v);
									onKeyboardEvent(v);
									break;
							}
						}
						return true;
					}
				});
		}

		public void setBackground(Drawable b){
			setBackgroundDrawable(b);
		}

		@Override
		public void setBackgroundDrawable(Drawable b){
			super.setBackgroundDrawable(b == null ? null : b.getConstantState().newDrawable());
		}
		
		public void setKeyIcon(Drawable dr){
			setKeyImageVisible(true);
			i.setImageDrawable(dr);
		}
		
		public void setKeyItemColor(int color){
			t.setTextColor(color);
			if(isKeyIconSet()){
				getKeyIcon().setColorFilter(color,PorterDuff.Mode.SRC_ATOP);
			}
		}
		
		public void setText(CharSequence cs){
			setKeyImageVisible(false);
			t.setText(cs);
		}
		
		public CharSequence getText(){
			return t.getText();
		}
		
		public Drawable getKeyIcon(){
			return i.getDrawable();
		}
		
		protected CharSequence getHint(){
			return t.getHint();
		}
		
		public void setKeyImageVisible(boolean visible){
			i.setVisibility(visible?VISIBLE:GONE);
			t.setVisibility(visible?GONE:VISIBLE);
		}
		
		protected void setHint(CharSequence cs){
			t.setHint(cs);
		}
		
		private void setKeyTextSize(float size){
			t.setTextSize(txtsze=size);
			ViewGroup.LayoutParams vp = i.getLayoutParams();
			vp.width = (int)(size*3);
			vp.height = (int)(size*3);
		}
		
		private void setKeyShadow(int radius, int color){
			t.setShadowLayer(shr=radius,0,0,shc=color);
			/*if(isKeyIconSet()){
				Bitmap b = Bitmap.createBitmap(128,128,Bitmap.Config.ARGB_8888);
				Canvas c = new Canvas(b);
				getKeyIcon().draw(c);
				//b = ImageUtils.fastblur(b,1,radius);
				setKeyIcon(new BitmapDrawable(b));
			}*/
		}
		
		/*private int gg(int a, int b){
			return a > b ? a : b;
		}*/
		
		protected TextView getTextView(){
			return t;
		}
		
		protected ImageView getImageView(){
			return i;
		}
		
		@Override
		public Key clone(){
			return clone(false);
		}
		
		public Key clone(boolean disableTouchEvent){
			return clone(new Key(getContext()),disableTouchEvent);
		}
		
		public Key clone(Key k){
			return clone(k,false);
		}
		
		public Key clone(Key k, boolean disableTouchEvent){
			Rect r = getBackground().getBounds();
			k.getLayoutParams().width = r.right;
			k.getLayoutParams().height = r.bottom;
			k.setBackgroundDrawable(getBackground());
			k.setHint(getHint());
			k.setKeyShadow(shr,shc);
			k.setKeyItemColor(keyclr);
			k.getTextView().setSingleLine();
			k.setId(getId());
			k.setKeyTextSize(t.getTextSize()/2.5f);
			if(disableTouchEvent) k.setOnTouchListener(null);
			if(isKeyIconSet()){
				k.setKeyIcon(getKeyIcon());
			} else {
				k.setText(getText());
			}
			return k;
		}
	}
}
