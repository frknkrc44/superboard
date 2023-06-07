package org.blinksd.board;

import static android.media.AudioManager.FX_KEYPRESS_DELETE;
import static android.media.AudioManager.FX_KEYPRESS_RETURN;
import static android.media.AudioManager.FX_KEYPRESS_SPACEBAR;
import static android.media.AudioManager.FX_KEYPRESS_STANDARD;
import static android.os.Build.VERSION.SDK_INT;
import static org.blinksd.board.SuperBoard.KeyboardType;;
import static org.blinksd.utils.layout.DensityUtils.mpInt;
import static org.blinksd.utils.system.SystemUtils.createNavbarLayout;
import static org.blinksd.utils.system.SystemUtils.detectNavbar;
import static org.blinksd.utils.system.SystemUtils.isColorized;
import static org.blinksd.utils.system.SystemUtils.navbarH;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.media.AudioManager;
import android.os.Build;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.blinksd.SuperBoardApplication;
import org.blinksd.sdb.SuperMiniDB;
import org.blinksd.utils.color.ColorUtils;
import org.blinksd.utils.icon.IconThemeUtils;
import org.blinksd.utils.icon.LocalIconTheme;
import org.blinksd.utils.image.ImageUtils;
import org.blinksd.utils.layout.DensityUtils;
import org.blinksd.utils.layout.LayoutUtils;
import org.blinksd.utils.layout.LayoutUtils.KeyOptions;
import org.blinksd.utils.layout.LayoutUtils.Language;
import org.blinksd.utils.layout.SuggestionLayout;
import org.superdroid.db.SuperDBHelper;

import java.io.File;
import java.util.List;

public class InputService extends InputMethodService implements
		SuggestionLayout.OnSuggestionSelectedListener,
		SuggestionLayout.OnQuickMenuItemClickListener {
	
	private SuperBoard sb = null;
	private BoardPopup po = null;
	private SuperMiniDB sd = null;
	public static final String COLORIZE_KEYBOARD = "org.blinksd.board.KILL";
	private String[][][] kbd = null;
	private String appname;
	private LinearLayout ll = null;
	private SuggestionLayout sl = null;
	private RelativeLayout fl = null;
	private ImageView iv = null;
	private Language cl;
	private EmojiView emoji = null;

	@Override
	public void onUpdateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd, int candidatesStart, int candidatesEnd){
		super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd);
		sendCompletionRequest();
	}
	
	@Override
	public void onSuggestionSelected(CharSequence text, CharSequence oldText, CharSequence suggestion){
		if(sb == null) return;
		InputConnection ic = sb.getCurrentIC();
		if(ic == null) ic = getCurrentInputConnection();
		if(ic == null) return;
		
		int state = sb.getShiftState();
		if(state == SuperBoard.SHIFT_OFF && Character.isUpperCase(oldText.charAt(0))){
			state = SuperBoard.SHIFT_ON;
		}
		
		switch(state){
			case SuperBoard.SHIFT_OFF:
				suggestion = suggestion.toString().toLowerCase();
				break;
			case SuperBoard.SHIFT_LOCKED:
				suggestion = suggestion.toString().toUpperCase();
				break;
			case SuperBoard.SHIFT_ON:
				String first = String.valueOf(suggestion.charAt(0));
				String other = suggestion.toString();
				other = other.substring(1);
				first = first.toUpperCase();
				suggestion = first + other;
				break;
		}
		
		ExtractedTextRequest req = new ExtractedTextRequest();
		ExtractedText exText = ic.getExtractedText(req, 0);
		String exTextStr = exText.text.toString();
		exTextStr = exTextStr.substring(text.length()-1);
		
		ic.deleteSurroundingText(oldText.length(), exTextStr.indexOf(' '));
		suggestion += " ";
		ic.commitText(suggestion, suggestion.length());
		
		req = new ExtractedTextRequest();
		exText = ic.getExtractedText(req, 0);
		exTextStr = exText.text.toString();
		int pos = exTextStr.indexOf(suggestion.toString()) + suggestion.length();
		ic.setSelection(pos, pos);
		
		sb.afterKeyboardEvent();

		SuperBoardApplication.getDictDB().increaseUsageCount(cl.language.split("_")[0], suggestion.toString().trim());
	}

	@Override
	public View onCreateInputView(){
		setLayout();
		return fl;
	}

	@Override
	public void onWindowHidden(){
		if(SuperDBHelper.getBooleanValueOrDefault(SettingMap.SET_KILL_BACKGROUND)){
			System.exit(0);
		}
		onFinishInput();
		super.onWindowHidden();
	}

	@Override
	public void onStartInput(EditorInfo attribute, boolean restarting){
		super.onStartInput(attribute, restarting);
		
		if(sb != null){
			setPrefs();
			sb.updateKeyState(this);
		}
	}

	@Override
	public void onFinishInput(){
		super.onFinishInput();
		if(sb != null)
			sb.updateKeyState(this);
		
		if(po != null){
			po.showPopup(false);
			po.clear();
		}
		
		if(emoji != null)
			showEmojiView(false);

		if(sl != null)
			sl.setCompletion(null, null);
			
		System.gc();
	}
	
	public void sendCompletionRequest(){
		if(sb == null) return;
		InputConnection ic = sb.getCurrentIC();
		if(ic == null) ic = getCurrentInputConnection();
		if(ic == null) return;
		CharSequence text = ic.getTextBeforeCursor(Integer.MAX_VALUE, 0);
		boolean sugDisabled = SuperDBHelper.getBooleanValueOrDefault(SettingMap.SET_DISABLE_SUGGESTIONS);
		if(text != null && sl != null) sl.setCompletionText(sugDisabled ? "" : text, cl.language);
	}
	
	@SuppressLint("ResourceType")
	private void setLayout(){
		if(sd == null){
			sd = SuperBoardApplication.getApplicationDatabase();
			registerReceiver(r,new IntentFilter(COLORIZE_KEYBOARD));
		}
		if(sb == null){
			sb = new SuperBoardImpl(this);
			sb.setLayoutParams(new LinearLayout.LayoutParams(-1,-1,1));
			appname = getString(R.string.app_name);
			String abc = "ABC";
			String[][] kbdSym1 = {
				{"[","]","θ","÷","<",">","`","´","{","}"},
				{"©","£","€","+","®","¥","π","Ω","λ","β"},
				{"@","#","$","%","&","*","-","=","(",")"},
				{"S2","!","\"","'",":",";","/","?",""},
				{abc,",",appname,".",""}
			}, kbdSym2 = {
				{"√","ℕ","★","×","™","‰","∛","^","~","±"},
				{"♣","♠","♪","♥","♦","≈","Π","¶","§","∆"},
				{"←","↑","↓","→","∞","≠","_","℅","‘","’"},
				{"S3","¡","•","°","¢","|","\\","¿",""},
				{abc,"₺",appname,"…",""}
			}, kbdSym3 = {
				{"F1","F2","F3","F4","F5","F6","F7","F8"},
				{"F9","F10","F11","F12","P↓","P↑","INS","DEL"},
				{"TAB","ENTER","HOME","ESC","PREV","PLAY","STOP","NEXT"},
				{"","","END","","BS","PAUSE","CUT","COPY"},
				{abc,"","←","↑","↓","→","","PASTE"}
			}, kbdNums = {
				{"-",".",",","ABC"},
				{"1","2","3","+"},
				{"4","5","6",";"},
				{"7","8","9",""},
				{"*","0","#",""}
			};
			
			kbd = new String[][][]{kbdSym1,kbdSym2,kbdSym3,kbdNums};

			loadKeyboardLayout();

			sb.createLayoutWithRows(kbd[0],KeyboardType.SYMBOL);
			sb.createLayoutWithRows(kbd[1],KeyboardType.SYMBOL);
			sb.createLayoutWithRows(kbd[2],KeyboardType.SYMBOL);		
			sb.createLayoutWithRows(kbd[3],KeyboardType.NUMBER);
			
			sb.setPressEventForKey(1,3,0,Keyboard.KEYCODE_ALT);
			sb.setPressEventForKey(2,3,0,Keyboard.KEYCODE_ALT);
			sb.setPressEventForKey(3,-1,0,Keyboard.KEYCODE_MODE_CHANGE);
			
			sb.setPressEventForKey(-1,0,-1,Keyboard.KEYCODE_ALT);
			sb.setPressEventForKey(-1,-2,-1,Keyboard.KEYCODE_DELETE);
			sb.setKeyRepeat(-1,-2,-1);
			sb.setPressEventForKey(-1,-1,-1,Keyboard.KEYCODE_DONE);
			
			sb.setPressEventForKey(3,1,4,KeyEvent.KEYCODE_PAGE_DOWN);
			sb.setPressEventForKey(3,1,5,KeyEvent.KEYCODE_PAGE_UP);
			sb.setPressEventForKey(3,1,6,KeyEvent.KEYCODE_INSERT);
			sb.setPressEventForKey(3,1,7,KeyEvent.KEYCODE_FORWARD_DEL);
			sb.setPressEventForKey(3,2,0,KeyEvent.KEYCODE_TAB);
			sb.setPressEventForKey(3,2,1,'\n',false);
			sb.setPressEventForKey(3,2,2,KeyEvent.KEYCODE_MOVE_HOME);
			sb.setPressEventForKey(3,2,3,KeyEvent.KEYCODE_ESCAPE);
			sb.setPressEventForKey(3,2,4,KeyEvent.KEYCODE_MEDIA_PREVIOUS);
			sb.setPressEventForKey(3,2,5,KeyEvent.KEYCODE_MEDIA_PLAY);
			sb.setPressEventForKey(3,2,6,KeyEvent.KEYCODE_MEDIA_STOP);
			sb.setPressEventForKey(3,2,7,KeyEvent.KEYCODE_MEDIA_NEXT);
			
			sb.setPressEventForKey(3,3,2,KeyEvent.KEYCODE_MOVE_END);
			sb.setPressEventForKey(3,3,4,KeyEvent.KEYCODE_DEL);
			sb.setPressEventForKey(3,3,5,KeyEvent.KEYCODE_MEDIA_PAUSE);
			sb.setPressEventForKey(3,3,6,KeyEvent.KEYCODE_CUT);
			sb.setPressEventForKey(3,3,7,KeyEvent.KEYCODE_COPY);
			
			sb.setPressEventForKey(3,-1,2,KeyEvent.KEYCODE_DPAD_LEFT);
			sb.setPressEventForKey(3,-1,3,KeyEvent.KEYCODE_DPAD_UP);
			sb.setPressEventForKey(3,-1,4,KeyEvent.KEYCODE_DPAD_DOWN);
			sb.setPressEventForKey(3,-1,5,KeyEvent.KEYCODE_DPAD_RIGHT);
			sb.setPressEventForKey(3,-1,7,KeyEvent.KEYCODE_PASTE);
			
			for(int i = 2;i < 6;i++)
				sb.setKeyRepeat(3,-1,i);
			
			for(int i = 0;i < 2;i++){
				for(int g = 0;g < 8;g++){
					if(i > 0 && g > 3) break;
					sb.setPressEventForKey(3,i,g,KeyEvent.KEYCODE_F1+(g+(i*8)));
				}
			}
			
			for(int i = 1;i < 3;i++){
				sb.setRowPadding(i,2,DensityUtils.wpInt(2));
				sb.setKeyRepeat(i,3,-1);
				sb.setKeyRepeat(i,4,2);
				sb.setPressEventForKey(i,3,-1,Keyboard.KEYCODE_DELETE);
				sb.setPressEventForKey(i,4,0,Keyboard.KEYCODE_MODE_CHANGE);
				sb.setPressEventForKey(i,4,2,KeyEvent.KEYCODE_SPACE);
				sb.setPressEventForKey(i,4,-1,Keyboard.KEYCODE_DONE);
				sb.setLongPressEventForKey(i,4,0, SuperBoard.KEYCODE_CLOSE_KEYBOARD);
				sb.setKeyWidthPercent(i,3,0,15);
				sb.setKeyWidthPercent(i,3,-1,15);
				sb.setKeyWidthPercent(i,4,0,20);
				sb.setKeyWidthPercent(i,4,1,15);
				sb.setKeyWidthPercent(i,4,2,50);
				sb.setKeyWidthPercent(i,4,3,15);
				sb.setKeyWidthPercent(i,4,-1,20);
			}
		}
		
		if(Build.VERSION.SDK_INT >= 16 && emoji == null){
			emoji = new EmojiView(sb,emojiClick);
			emoji.setVisibility(View.GONE);
			if(SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
				emoji.setBackground(sb.getBackground());
			} else {
				emoji.setBackgroundDrawable(sb.getBackground());
			}
		}
		
		if(ll == null){
			ll = new LinearLayout(this);
			ll.setLayoutParams(new LinearLayout.LayoutParams(-1,-2));
			ll.setOrientation(LinearLayout.VERTICAL);
			sl = new SuggestionLayout(sb);
			sl.setLayoutParams(new FrameLayout.LayoutParams(-1, mpInt(12)));
			sl.setId(android.R.attr.shape);
			// sl.setOnSuggestionSelectedListener(this);
			// setCandidatesView(sl);
			// setCandidatesViewShown(true);
			ll.addView(sl);
			ll.addView(sb);
			if(emoji != null){
				ll.addView(emoji);
			}
		}
		
		if(fl == null){
			fl = new RelativeLayout(this);
			fl.setLayoutParams(new LinearLayout.LayoutParams(-1,-2));
			iv = new ImageView(this);
			fl.addView(iv);
			fl.addView(ll);
			iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
			iv.setAdjustViewBounds(false);
		}
		if(po == null){
			po = new BoardPopup(fl){
				@Override
				public void afterKeyboardEvent(){
					sb.afterPopupEvent();
				}
				
				@Override
				public void playSound(int event){
					sb.playSound(event);
				}
			};
			fl.addView(po);
		}
		setPrefs();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig){
		try {
			setPrefs(newConfig);
		} catch(Throwable t){
			System.exit(0);
		}
	}
	
	public void setPrefs(){
		setPrefs(getResources().getConfiguration());
	}
	
	public void setPrefs(Configuration conf){
		if(sb != null && sd != null){
			LayoutUtils.setKeyOpts(cl,sb);
			IconThemeUtils icons = SuperBoardApplication.getIconThemes();
			sb.setKeyDrawable(-1,-2,-1,icons.getIconResource(LocalIconTheme.SYM_TYPE_DELETE));
			sb.setKeyDrawable(-1,-1,-1,icons.getIconResource(LocalIconTheme.SYM_TYPE_ENTER));
			for(int i = 1;i < 3;i++){
				sb.setKeyDrawable(i,3,-1,icons.getIconResource(LocalIconTheme.SYM_TYPE_DELETE));
				sb.setKeyDrawable(i,4,-1,icons.getIconResource(LocalIconTheme.SYM_TYPE_ENTER));
				LayoutUtils.setSpaceBarViewPrefs(icons, sb.getKey(i,4,2), appname);
			}
			sb.setShiftDetection(SuperDBHelper.getBooleanValueOrDefault(SettingMap.SET_DETECT_CAPSLOCK));
			sb.setRepeating(!SuperDBHelper.getBooleanValueOrDefault(SettingMap.SET_DISABLE_REPEAT));
			sb.updateKeyState(this);
			float ori = conf.orientation == Configuration.ORIENTATION_LANDSCAPE ? 1.3f : 1;
			sb.setKeyboardHeight((int)(SuperDBHelper.getIntValueOrDefault(SettingMap.SET_KEYBOARD_HEIGHT) * ori));
			File img;
			int c = SuperDBHelper.getIntValueOrDefault(SettingMap.SET_KEYBOARD_BGCLR);
			if(SuperDBHelper.getBooleanValueOrDefault(SettingMap.SET_USE_MONET)){
				if(fl != null){
					iv.setImageBitmap(null);
				}
			} else {
				img = SuperBoardApplication.getBackgroundImageFile();
				if(fl != null){
					if(img.exists()) {
						int blur = SuperDBHelper.getIntValueOrDefault(SettingMap.SET_KEYBOARD_BGBLUR);
						Bitmap b = BitmapFactory.decodeFile(img.getAbsolutePath());
						iv.setImageBitmap(blur > 0 ? ImageUtils.getBlur(b,blur) : b);
					} else {
						iv.setImageBitmap(null);
						c = ColorUtils.convertARGBtoRGB(c);
					}
				}
			}

			sb.setBackgroundColor(c);
			sl.setBackgroundColor(c);
			sl.retheme();
			int keyClr = SuperDBHelper.getIntValueOrDefault(SettingMap.SET_KEY_BGCLR);
			int keyPressClr = SuperDBHelper.getIntValueOrDefault(SettingMap.SET_KEY_PRESS_BGCLR);
			sb.setKeysBackground(LayoutUtils.getKeyBg(keyClr,keyPressClr,true));
			int shr = SuperDBHelper.getIntValueOrDefault(SettingMap.SET_KEY_SHADOWSIZE),
				shc = SuperDBHelper.getIntValueOrDefault(SettingMap.SET_KEY_SHADOWCLR);
			sb.setKeysShadow(shr,shc);
			sb.setLongPressMultiplier(SuperDBHelper.getIntValueOrDefault(SettingMap.SET_KEY_LONGPRESS_DURATION));
			sb.setKeyVibrateDuration(SuperDBHelper.getIntValueOrDefault(SettingMap.SET_KEY_VIBRATE_DURATION));
			sb.setKeysTextColor(SuperDBHelper.getIntValueOrDefault(SettingMap.SET_KEY_TEXTCLR));
			sb.setKeysTextSize(mpInt(DensityUtils.getFloatNumberFromInt(SuperDBHelper.getIntValueOrDefault(SettingMap.SET_KEY_TEXTSIZE))));
			sb.setKeysTextType(SuperDBHelper.getIntValueOrDefault(SettingMap.SET_KEYBOARD_TEXTTYPE_SELECT));
			sb.setIconSizeMultiplier(SuperDBHelper.getIntValueOrDefault(SettingMap.SET_KEY_ICON_SIZE_MULTIPLIER));
			sb.setKeysPopupPreviewEnabled(SuperDBHelper.getBooleanValueOrDefault(SettingMap.SET_ENABLE_POPUP_PREVIEW));
			int y = SuperDBHelper.getIntValueOrDefault(SettingMap.SET_KEY2_BGCLR);
			int yp = SuperDBHelper.getIntValueOrDefault(SettingMap.SET_KEY2_PRESS_BGCLR);
			int z = SuperDBHelper.getIntValueOrDefault(SettingMap.SET_ENTER_BGCLR);
			int zp = SuperDBHelper.getIntValueOrDefault(SettingMap.SET_ENTER_PRESS_BGCLR);
			for(int i = 0;i < kbd.length;i++){
				if(i != 0){
					if(i < 3){
						sb.setKeyTintColor(i,3,0,y,yp);
						sb.setKeyTintColor(i,3,-1,y,yp);
						for(int h = 3;h < 5;h++) sb.setKeyTintColor(i,h,0,y,yp);
						sb.setKeyTintColor(i,4,1,y,yp);
						sb.setKeyTintColor(i,4,3,y,yp);
					}
					if(i != 3) sb.setKeyTintColor(i,-1,-1,z,zp);
				}
			}
			sb.setDisablePopup(SuperDBHelper.getBooleanValueOrDefault(SettingMap.SET_DISABLE_POPUP));
			boolean sugDisabled = SuperDBHelper.getBooleanValueOrDefault(SettingMap.SET_DISABLE_SUGGESTIONS);
			boolean topBarDisabled = SuperDBHelper.getBooleanValueOrDefault(SettingMap.SET_DISABLE_TOP_BAR);
			sl.setVisibility(sugDisabled && topBarDisabled ? View.GONE : View.VISIBLE);
			sl.setOnSuggestionSelectedListener(sugDisabled ? null : this);
			sl.setOnQuickMenuItemClickListener(topBarDisabled ? null : this);
			String lang = SuperDBHelper.getValueOrDefault(SettingMap.SET_KEYBOARD_LANG_SELECT);
			if(!lang.equals(cl.language)){
				loadKeyboardLayout();
			}
			List<List<KeyOptions>> kOpt = cl.layout;
			for(int i = 0;i < kOpt.size();i++){
				List<KeyOptions> subKOpt = kOpt.get(i);
				for(int g = 0;g < subKOpt.size();g++){
					KeyOptions ko = subKOpt.get(g);
					if(ko.darkerKeyTint){
						sb.setKeyTintColor(sb.getKey(0,i,g),y,yp);
					}
					if(ko.pressKeyCode == Keyboard.KEYCODE_DONE){
						sb.setKeyTintColor(sb.getKey(0,i,g),z,zp);
					}
				}
			}
			sb.setKeyboardLanguage(cl.language);
			adjustNavbar(c);
			if(emoji != null){
				emoji.applyTheme(sb);
				emoji.getLayoutParams().height = sb.getKeyboardHeight();
			}
			SuperBoardApplication.clearCustomFont();
			sb.setCustomFont(SuperBoardApplication.getCustomFont());
		}
		
		sendCompletionRequest();
	}
	private void loadKeyboardLayout() {
		try {
			String lang = SuperDBHelper.getValueOrDefault(SettingMap.SET_KEYBOARD_LANG_SELECT);
			int keyboardIndex = sb.findNormalKeyboardIndex();
			Language l = SuperBoardApplication.getKeyboardLanguage(lang);
			if(!l.language.equals(lang)){
				throw new RuntimeException("Where is the layout JSON file (in assets)?");
			}
			String[][] lkeys = LayoutUtils.getLayoutKeys(l.layout);
			sb.replaceNormalKeyboard(lkeys);
			sb.setLayoutPopup(keyboardIndex, LayoutUtils.getLayoutKeys(l.popup));
			if(l.midPadding && lkeys != null){
				sb.setRowPadding(keyboardIndex,lkeys.length/2,DensityUtils.wpInt(2));
			}
			LayoutUtils.setKeyOpts(l,sb);
			cl = l;
		} catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	
	private void adjustNavbar(int c){
		int baseHeight = sb.getKeyboardHeight();
		if(sl.getVisibility() == View.VISIBLE){
			baseHeight += sl.getLayoutParams().height;
		}
		
		if(SDK_INT > 20){
			Window w = getWindow().getWindow();
			if(detectNavbar(this)){
				@SuppressLint("ResourceType") View navbarView = ll.findViewById(android.R.attr.gravity);
				if(navbarView != null)
					ll.removeView(navbarView);

				if(SDK_INT >= 28 && SuperDBHelper.getBooleanValueOrDefault(SettingMap.SET_COLORIZE_NAVBAR_ALT)){
					w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
					w.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
					iv.setLayoutParams(new RelativeLayout.LayoutParams(-1,baseHeight));
					int color = Color.rgb(Color.red(c),Color.green(c),Color.blue(c));
					w.setNavigationBarColor(color);
					w.getDecorView().setSystemUiVisibility(ColorUtils.satisfiesTextContrast(color)
																		? View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
																		: View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
				} else if(isColorized(this)){
					// I found a bug at SDK 30
					// FLAG_LAYOUT_NO_LIMITS not working
					// set FLAG_TRANSLUCENT_NAVIGATION for this SDK only
					if(SDK_INT == 30) w.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
					else w.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
					w.setNavigationBarColor(0);
					iv.setLayoutParams(new RelativeLayout.LayoutParams(-1,baseHeight+navbarH(this)));
					ll.addView(createNavbarLayout(this, c));
				} else {
					w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
					w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
					w.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
					iv.setLayoutParams(new RelativeLayout.LayoutParams(-1,baseHeight));
				}
			} else {
				iv.setLayoutParams(new RelativeLayout.LayoutParams(-1,baseHeight));
			}
		} else {
			iv.setLayoutParams(new RelativeLayout.LayoutParams(-1,baseHeight));
		}
		
		ll.getLayoutParams().height = iv.getLayoutParams().height;
		po.setFilterHeight(iv.getLayoutParams().height);
	}
	
	private final BroadcastReceiver r = new BroadcastReceiver(){
		@Override
		public void onReceive(Context p1,Intent p2){
			setPrefs();
		}
	};

	@Override
	public boolean onKeyDown(int keyCode,KeyEvent event){
		if(po != null && po.isShown()){
			po.showPopup(false);
		}
		showEmojiView(false);
		return super.onKeyDown(keyCode,event);
	}
	
	public void onEmojiText(String text){
		sb.commitText(text);
	}
	
	private boolean showEmoji = false;
	
	private void showEmojiView(boolean value){
		if(Build.VERSION.SDK_INT < 16){
			return;
		}
		if(showEmoji != value){
			emoji.setVisibility(value ? View.VISIBLE : View.GONE);
			sb.setVisibility(value ? View.GONE : View.VISIBLE);
			showEmoji = value;
		}
	}
	
	private final View.OnClickListener emojiClick = v -> {
		final int num = Integer.parseInt(v.getTag().toString());
		switch(num){
			case -1:
				showEmojiView(false);
				break;
			case 10:
				sb.sendKeyEvent(KeyEvent.KEYCODE_DEL);
				break;
		}
	};

	@Override
	public void onQuickMenuItemClick(int action) {
		if (sb != null) {
			switch (action) {
				case KeyEvent.KEYCODE_NUM:
					sb.setEnabledLayout(sb.getEnabledLayoutIndex() != 3 ? 3 : 0);
					break;
				default:
					sb.sendKeyEvent(action);
					break;
			}
		}
	}

	private class SuperBoardImpl extends SuperBoard {
		private SuperBoardImpl(Context context) {
			super(context);
		}

		private boolean shown = false;

		@Override
		public void beforeKeyboardEvent(View v) {
			boolean showPopup = SuperDBHelper.getBooleanValueOrDefault(SettingMap.SET_KEYBOARD_SHOW_POPUP);
			boolean disablePopup = SuperDBHelper.getBooleanValueOrDefault(SettingMap.SET_DISABLE_POPUP);

			if(showPopup || !disablePopup)
				po.setKey((SuperBoard.Key)v);
		}

		@Override
		public void onKeyboardEvent(View v){
			boolean showPopup = SuperDBHelper.getBooleanValueOrDefault(SettingMap.SET_KEYBOARD_SHOW_POPUP);
			shown = po.isShown();

			if(shown){
				po.showPopup(false);
				po.clear();
				return;
			}

			if(showPopup)
				po.showCharacter();
		}

		public void onPopupEvent(){
			po.showPopup();
			po.setShiftState(shift);
		}

		@Override
		public void afterKeyboardEvent(){
			super.afterKeyboardEvent();
			if(SuperDBHelper.getBooleanValueOrDefault(SettingMap.SET_KEYBOARD_SHOW_POPUP)){
				po.hideCharacter();
			}

			if (sl != null) {
				sl.retheme();
			}
			// sendCompletionRequest();
		}

		public void sendDefaultKeyboardEvent(View v){
			if(!shown) super.sendDefaultKeyboardEvent(v);
			else shown = false;
		}

		@Override
		public void switchLanguage(){
			if(SuperDBHelper.getBooleanValueOrDefault(SettingMap.SET_KEYBOARD_LC_ON_EMOJI)){
				SuperBoardApplication.getNextLanguage();
				setPrefs();
			} else {
				openEmojiLayout();
			}
		}

		@Override
		public void openEmojiLayout(){
			showEmojiView(true);
		}

		@Override
		public void playSound(int event){
			if(!SuperDBHelper.getBooleanValueOrDefault(SettingMap.SET_PLAY_SND_PRESS)) return;
			AudioManager audMgr = (AudioManager) getSystemService(AUDIO_SERVICE);
			switch(event){
				case Keyboard.KEYCODE_DONE:
					audMgr.playSoundEffect(FX_KEYPRESS_RETURN);
					break;
				case Keyboard.KEYCODE_DELETE:
					audMgr.playSoundEffect(FX_KEYPRESS_DELETE);
					break;
				case KeyEvent.KEYCODE_SPACE:
					audMgr.playSoundEffect(FX_KEYPRESS_SPACEBAR);
					break;
				default:
					audMgr.playSoundEffect(FX_KEYPRESS_STANDARD);
					break;
			}
		}
	}
}
