package org.blinksd.board;

import android.content.*;
import android.content.res.*;
import android.graphics.drawable.*;
import android.inputmethodservice.*;
import android.os.*;
import android.view.*;
import android.view.inputmethod.*;
import java.lang.reflect.*;
import org.blinksd.utils.color.*;
import org.superdroid.db.*;

import static org.blinksd.board.SuperBoard.*;
import android.widget.*;

public class InputService extends InputMethodService {
	
	SuperBoard sb = null;
	SuperDB sd = null;
	public static final String COLORIZE_KEYBOARD = "org.blinksd.board.KILL";
	private String kbd[][][] = null;
	private LinearLayout ll = null;

	@Override
	public View onCreateInputView(){
		setLayout();
		return ll;
	}

	@Override
	public void onUnbindInput(){
		requestHideSelf(0);
		super.onUnbindInput();
	}

	@Override
	public boolean onKeyDown(int keyCode,KeyEvent event){
		if(isInputViewShown()){
			if(keyCode == event.KEYCODE_BACK){
				requestHideSelf(0);
				return true;
			}
		}
		return super.onKeyDown(keyCode,event);
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
		System.gc();
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
		Intent i = new Intent(this,InputService.class);
		stopService(i);
		startService(i);
	}

	private void setKeyBg(int clr){
		GradientDrawable gd = new GradientDrawable();
		gd.setColor(clr);
		gd.setCornerRadius(sb.mp(Settings.a(SuperDBHelper.getIntValueAndSetItToDefaultIsNotSet(sd,Settings.Key.key_radius.name(),10))));
		gd.setStroke(sb.mp(Settings.a(SuperDBHelper.getIntValueAndSetItToDefaultIsNotSet(sd,Settings.Key.key_padding.name(),10))),0);
		sb.setKeysBackground(gd);
	}
	
	private void setLayout(){
		if(sd == null){
			sd = SuperDBHelper.getDefault(this);
			registerReceiver(r,new IntentFilter(COLORIZE_KEYBOARD));
		}
		if(sb == null){
			sb = new SuperBoard(this);
			sb.setLayoutParams(new LinearLayout.LayoutParams(-1,-1,1));
			kbd = new String[][][]{
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
			
			sb.setPressEventForKey(3,3,3,KeyEvent.KEYCODE_MOVE_HOME);
			sb.setPressEventForKey(3,3,4,KeyEvent.KEYCODE_SEARCH);
			
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
		setPrefs();
		sb.updateKeyState(this);
		if(ll == null){
			ll = new LinearLayout(this);
			ll.setLayoutParams(new LinearLayout.LayoutParams(-1,-2));
			ll.setOrientation(LinearLayout.VERTICAL);
			ll.addView(sb);
		}
	}
	
	/*public int getThemeColor(){
		return Build.VERSION.SDK_INT>19?obtainStyledAttributes(new int[]{android.R.attr.colorAccent}).getInt(0,0xFF5F97F6):0xFF5F97F6;
	}*/

	@Override
	public void onConfigurationChanged(Configuration newConfig){
		setPrefs();
		// System.exit(0);
		// fixing crashes
	}
	
	public void setPrefs(){
		sb.setKeyboardHeight(SuperDBHelper.getIntValueAndSetItToDefaultIsNotSet(sd,Settings.Key.keyboard_height.name(),40));
		int c = SuperDBHelper.getIntValueAndSetItToDefaultIsNotSet(sd,Settings.Key.keyboard_bgclr.name(),0xFF282D31);
		sb.setBackgroundColor(c);
		setKeyBg(SuperDBHelper.getIntValueAndSetItToDefaultIsNotSet(sd,Settings.Key.key_bgclr.name(),0xFF474B4C));
		sb.setKeysTextColor(SuperDBHelper.getIntValueAndSetItToDefaultIsNotSet(sd,Settings.Key.key_textclr.name(),0xFFDDE1E2));
		sb.setKeysTextSize(sb.mp(Settings.a(SuperDBHelper.getIntValueAndSetItToDefaultIsNotSet(sd,Settings.Key.key_textsize.name(),10))));
		for(int i = 0;i < kbd.length;i++){
			if(i < 3){
				int y = SuperDBHelper.getIntValueAndSetItToDefaultIsNotSet(sd,Settings.Key.key2_bgclr.name(),0xFF373C40);
				sb.setKeyTintColor(i,3,-1,y);
				for(int h = 3;h < 5;h++) sb.setKeyTintColor(i,h,0,y);
				sb.setKeyTintColor(i,4,1,y);
				sb.setKeyTintColor(i,4,3,y);
			}
			
			if(i != 3) sb.setKeyTintColor(i,-1,-1,SuperDBHelper.getIntValueAndSetItToDefaultIsNotSet(sd,Settings.Key.enter_bgclr.name(),0xFF5F97F6));
		}
		try {
			if(Build.VERSION.SDK_INT > 20){
				Window w = getWindow().getWindow();
				if(Build.VERSION.SDK_INT > 27){
					w.setNavigationBarColor(c);
					w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | 
														   (ColorUtils.satisfiesTextContrast(c)
														   ? View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR 
														   : 0));
				} else {
					if(detectNavbar()){
						if(ll.getChildCount() > 1){
							ll.removeViewAt(1);
						}
						if(x()){
							w.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
							ll.addView(createNavbarLayout(c));
							ll.setOrientation(x() ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
						}
					}
				}
			}
		} catch(Exception e){}
	}
	
	private View createNavbarLayout(int color){
		View v = new View(this);
		v.setLayoutParams(new ViewGroup.LayoutParams(x() ? -1 : navbarH(),x() ? navbarH() : -1));
		v.setBackgroundColor(color);
		return v;
	}
	
	private int navbarH(){
		int resourceId = getResources().getIdentifier(x() ? "navigation_bar_height" : "navigation_bar_width", "dimen", "android");
		return resourceId > 0 ? getResources().getDimensionPixelSize(resourceId) : 0;
	}
	
	private boolean x(){
		return !isLand() || isTablet();
	}
	
	private boolean isTablet(){
		return isLand() && getResources().getConfiguration().smallestScreenWidthDp >= 600;
	}
	
	private boolean isLand(){
		return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
	}
	
	private boolean detectNavbar(){
		if(Build.VERSION.SDK_INT >= 14){
			try {
				Class<?> serviceManager = Class.forName("android.os.ServiceManager");
				IBinder serviceBinder = (IBinder)serviceManager.getMethod("getService", String.class).invoke(serviceManager, "window");
				Class<?> stub = Class.forName("android.view.IWindowManager$Stub");
				Object windowManagerService = stub.getMethod("asInterface", IBinder.class).invoke(stub, serviceBinder);
				Method hasNavigationBar = windowManagerService.getClass().getMethod("hasNavigationBar");
				return (boolean) hasNavigationBar.invoke(windowManagerService);
			} catch(Exception e){
				return ViewConfiguration.get(this).hasPermanentMenuKey();
			}
		}
		return (!(KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK) && 
			KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME)));
	}
	
	BroadcastReceiver r = new BroadcastReceiver(){

		@Override
		public void onReceive(Context p1,Intent p2){
			sd.onlyRead();
			setPrefs();
		}
		
	};
}
