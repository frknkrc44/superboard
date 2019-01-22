package org.blinksd.board;

import android.content.*;
import android.graphics.drawable.*;
import android.inputmethodservice.*;
import android.view.*;
import android.view.inputmethod.*;

import static org.blinksd.board.SuperBoard.*;

public class InputService extends InputMethodService {
	
	SuperBoard sb = null;

	@Override
	public View onCreateInputView(){
		setLayout();
		return sb;
	}

	@Override
	public void onStartInput(EditorInfo attribute, boolean restarting){
		super.onStartInput(attribute, restarting);
		setLayout();
	}

	@Override
	public void onFinishInput(){
		sb.setEnabledLayout(0);
		super.onFinishInput();
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		restart();
	}

	@Override
	public boolean onUnbind(Intent intent){
		restart();
		return super.onUnbind(intent);
	}
	
	public void restart(){
		startService(new Intent(this,InputService.class));
	}

	private void setKeyBg(int clr){
		GradientDrawable gd = new GradientDrawable();
		gd.setColor(clr);
		gd.setCornerRadius(sb.dp(8));
		gd.setStroke(sb.dp(6),0);
		sb.setKeyBackground(gd);
	}
	
	private void setLayout(){
		if(sb == null){
			sb = new SuperBoard(this);
			setKeyBg(0xFF363636);
			String[][][] kbd = {
				{
					{"1","2","3","4","5","6","7","8","9","0"},
					{"q","w","e","r","t","y","u","ı","o","p","ğ","ü"},
					{"a","s","d","f","g","h","j","k","l","ş","i"},
					{"UP","z","x","c","v","b","n","m","ö","ç","BS"},
					{"!?#",",","space",".","ENTER"}
				},{
					{"[","]","θ","÷","<",">","`","´","{","}"},
					{"©","£","€","+","®","¥","π","Ω","λ","β"},
					{"@","#","$","%","&","*","-","=","(",")"},
					{"S2","!","\"","'",":",";","/","?","BS"},
					{"ABC",",","space",".","ENTER"}
				},{
					{"√","ℕ","★","×","™","‰","∛","^","~","±"},
					{"♣","♠","♪","♥","♦","≈","Π","¶","§","∆"},
					{"←","↑","↓","→","∞","≠","_","℅","‘","’"},
					{"S3","¡","•","°","¢","|","\\","¿","BS"},
					{"ABC","₺","space","…","ENTER"}
				},{
					{"F1","F2","F3","F4","F5","F6","F7","F8"},
					{"F9","F10","F11","F12","P↑","P↓","INS","DEL"},
					{"TAB","HOME","END","ESC","PREV","PL/PA","STOP","NEXT"},
					{"","","","MH","S","","",""},
					{"ABC","🔇","←","↑","↓","→","🔉","🔊"}
				},{
					{"1","2","3","+"},
					{"4","5","6",";"},
					{"7","8","9","BS"},
					{"*","0","#","ENTER"}
				}
			};
			sb.addRows(0,kbd[0]);
			sb.createLayoutWithRows(kbd[1],KeyboardType.SYMBOL);
			sb.createLayoutWithRows(kbd[2],KeyboardType.SYMBOL);
			sb.createLayoutWithRows(kbd[3],KeyboardType.SYMBOL);
			sb.createLayoutWithRows(kbd[4],KeyboardType.NUMBER);
			
			sb.setPressEventForKey(0,3,0,Keyboard.KEYCODE_SHIFT);
			sb.setKeyDrawable(0,3,0,R.drawable.sym_keyboard_shift);
			sb.setPressEventForKey(1,3,0,Keyboard.KEYCODE_ALT);
			sb.setPressEventForKey(2,3,0,Keyboard.KEYCODE_ALT);
			sb.setPressEventForKey(3,-1,0,Keyboard.KEYCODE_MODE_CHANGE);
			
			sb.setPressEventForKey(-1,2,-1,Keyboard.KEYCODE_DELETE);
			sb.setKeyRepeat(-1,2,-1);
			sb.setKeyDrawable(-1,2,-1,R.drawable.sym_keyboard_delete);
			sb.setPressEventForKey(-1,3,-1,Keyboard.KEYCODE_DONE);
			sb.setKeyDrawable(-1,3,-1,R.drawable.sym_keyboard_return);
			
			sb.setPressEventForKey(3,1,4,KeyEvent.KEYCODE_PAGE_DOWN);
			sb.setPressEventForKey(3,1,5,KeyEvent.KEYCODE_PAGE_UP);
			sb.setPressEventForKey(3,1,6,KeyEvent.KEYCODE_INSERT);
			sb.setPressEventForKey(3,1,7,KeyEvent.KEYCODE_DEL);
			sb.setPressEventForKey(3,2,0,KeyEvent.KEYCODE_TAB);
			sb.setPressEventForKey(3,2,1,KeyEvent.KEYCODE_HOME);
			sb.setPressEventForKey(3,2,2,KeyEvent.KEYCODE_BREAK);
			sb.setPressEventForKey(3,2,3,KeyEvent.KEYCODE_ESCAPE);
			sb.setPressEventForKey(3,2,4,KeyEvent.KEYCODE_MEDIA_PREVIOUS);
			sb.setPressEventForKey(3,2,5,KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
			sb.setPressEventForKey(3,2,6,KeyEvent.KEYCODE_MEDIA_STOP);
			sb.setPressEventForKey(3,2,7,KeyEvent.KEYCODE_MEDIA_NEXT);
			
			//sb.setPressEventForKey(3,3,2,KeyEvent.KEYCODE_BRIGHTNESS_DOWN);
			sb.setPressEventForKey(3,3,3,KeyEvent.KEYCODE_MOVE_HOME);
			sb.setPressEventForKey(3,3,4,KeyEvent.KEYCODE_SEARCH);
			//sb.setPressEventForKey(3,3,5,KeyEvent.KEYCODE_DPAD_DOWN_RIGHT);
			
			sb.setPressEventForKey(3,-1,1,KeyEvent.KEYCODE_MUTE);
			sb.setPressEventForKey(3,-1,2,KeyEvent.KEYCODE_DPAD_LEFT);
			sb.setPressEventForKey(3,-1,3,KeyEvent.KEYCODE_DPAD_UP);
			sb.setPressEventForKey(3,-1,4,KeyEvent.KEYCODE_DPAD_DOWN);
			sb.setPressEventForKey(3,-1,5,KeyEvent.KEYCODE_DPAD_RIGHT);
			sb.setPressEventForKey(3,-1,6,KeyEvent.KEYCODE_VOLUME_DOWN);
			sb.setPressEventForKey(3,-1,7,KeyEvent.KEYCODE_VOLUME_UP);
			
			for(int i = 0;i < 2;i++){
				for(int g = 0;g < 8;g++){
					if(i >= 1 && g >= 4) break;
					sb.setPressEventForKey(3,i,g,KeyEvent.KEYCODE_F1+(g+(i*8)));
				}
			}
			//sb.setPopupForKey(0,0,0,"yep");
			
			for(int i = 0;i < kbd.length;i++){
				if(i < 3){
					sb.setRowPadding(i,2,sb.wp(2));
					sb.setKeyRepeat(i,3,-1);
					sb.setKeyRepeat(i,4,2);
					sb.setPressEventForKey(i,3,-1,Keyboard.KEYCODE_DELETE);
					sb.setKeyDrawable(i,3,-1,R.drawable.sym_keyboard_delete);
					sb.setPressEventForKey(i,4,0,Keyboard.KEYCODE_MODE_CHANGE);
					sb.setPressEventForKey(i,4,2,KeyEvent.KEYCODE_SPACE);
					sb.setPressEventForKey(i,4,-1,Keyboard.KEYCODE_DONE);
					sb.setKeyDrawable(i,4,-1,R.drawable.sym_keyboard_return);
					sb.setLongPressEventForKey(i,4,0,sb.KEYCODE_CLOSE_KEYBOARD);
					sb.setLongPressEventForKey(i,4,1,'\t',false);
				}
				if(i != 3) sb.setKeyTintColor(i,-1,-1,0xFFCC3434);
			}
		}
		
		for(int i = 0;i < 3;i++){
			sb.setKeyWidthPercent(i,3,0,15);
			sb.setKeyWidthPercent(i,3,-1,15);
			sb.setKeyWidthPercent(i,4,0,20);
			sb.setKeyWidthPercent(i,4,1,15);
			sb.setKeyWidthPercent(i,4,2,50);
			sb.setKeyWidthPercent(i,4,3,15);
			sb.setKeyWidthPercent(i,4,-1,20);
		}
		sb.updateKeyState(this);
	}
}
