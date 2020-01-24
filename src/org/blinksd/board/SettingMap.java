package org.blinksd.board;

import android.os.*;
import java.util.*;
import org.blinksd.*;
import org.blinksd.board.AppSettingsV2.*;
import android.content.res.*;
import android.util.*;

public class SettingMap extends LinkedHashMap<String,SettingType> {

	public static final String SET_KEYBOARD_LANG_SELECT = "keyboard_lang_select",
	SET_KEYBOARD_TEXTTYPE_SELECT = "keyboard_texttype_select",
	SET_KEYBOARD_BGIMG = "keyboard_bgimg",
	SET_KEYBOARD_BGBLUR = "keyboard_bgblur",
	SET_KEYBOARD_HEIGHT = "keyboard_height",
	SET_KEYBOARD_BGCLR = "keyboard_bgclr",
	SET_KEYBOARD_SHOW_POPUP = "keyboard_show_popup",
	SET_KEYBOARD_LC_ON_EMOJI = "keyboard_lc_on_emoji",
	SET_PLAY_SND_PRESS = "play_snd_press",
	SET_KEY_BGCLR = "key_bgclr",
	SET_KEY2_BGCLR = "key2_bgclr",
	SET_ENTER_BGCLR = "enter_bgclr",
	SET_KEY_SHADOWCLR = "key_shadowclr",
	SET_KEY_PADDING = "key_padding",
	SET_KEY_RADIUS = "key_radius",
	SET_KEY_TEXTSIZE = "key_textsize",
	SET_KEY_SHADOWSIZE = "key_shadowsize",
	SET_KEY_VIBRATE_DURATION = "key_vibrate_duration",
	SET_KEY_LONGPRESS_DURATION = "key_longpress_duration",
	SET_KEY_TEXTCLR = "key_textclr";

	public SettingMap(){
		put(SET_KEYBOARD_LANG_SELECT,SettingType.LANG_SELECTOR);
		put(SET_KEYBOARD_TEXTTYPE_SELECT,SettingType.SELECTOR);
		put(SET_KEYBOARD_BGIMG,SettingType.IMAGE);
		put(SET_KEYBOARD_SHOW_POPUP,SettingType.BOOL);
		put(SET_PLAY_SND_PRESS,SettingType.BOOL);
		put(SET_KEYBOARD_LC_ON_EMOJI,SettingType.BOOL);
		put(SET_KEYBOARD_BGBLUR,SettingType.DECIMAL_NUMBER);
		put(SET_KEYBOARD_HEIGHT,SettingType.MM_DECIMAL_NUMBER);
		put(SET_KEY_VIBRATE_DURATION,SettingType.DECIMAL_NUMBER);
		put(SET_KEY_LONGPRESS_DURATION,SettingType.MM_DECIMAL_NUMBER);
		put(SET_KEYBOARD_BGCLR,SettingType.COLOR_SELECTOR);
		put(SET_KEY_BGCLR,SettingType.COLOR_SELECTOR);
		put(SET_KEY2_BGCLR,SettingType.COLOR_SELECTOR);
		put(SET_ENTER_BGCLR,SettingType.COLOR_SELECTOR);
		put(SET_KEY_SHADOWCLR,SettingType.COLOR_SELECTOR);
		put(SET_KEY_TEXTCLR,SettingType.COLOR_SELECTOR);
		put(SET_KEY_PADDING,SettingType.FLOAT_NUMBER);
		put(SET_KEY_RADIUS,SettingType.FLOAT_NUMBER);
		put(SET_KEY_TEXTSIZE,SettingType.FLOAT_NUMBER);
		put(SET_KEY_SHADOWSIZE,SettingType.FLOAT_NUMBER);
	}

	public ArrayList<String> getSelector(final String key) throws Throwable {
		switch(key){
			case SET_KEYBOARD_LANG_SELECT:
				return LayoutUtils.getKeyListFromLanguageList();
			case SET_KEYBOARD_TEXTTYPE_SELECT:
				ArrayList<String> textTypes = new ArrayList<String>();
				for(SuperBoard.TextType type : SuperBoard.TextType.values())
					textTypes.add(type.name());
				return textTypes;
		}
		return new ArrayList<String>();
	}

	public Object getDefaults(final String key){
		if(containsKey(key)){
			switch(key){
				case SET_KEYBOARD_BGBLUR:
					return Defaults.KEYBOARD_BACKGROUND_BLUR;
				case SET_KEY_VIBRATE_DURATION:
					return Defaults.KEY_VIBRATE_DURATION;
				case SET_KEYBOARD_HEIGHT:
					return Defaults.KEYBOARD_HEIGHT;
				case SET_KEY_LONGPRESS_DURATION:
					return Defaults.KEY_LONGPRESS_DURATION;
				case SET_KEY_PADDING:
					return Defaults.KEY_PADDING;
				case SET_KEY_SHADOWSIZE:
					return Defaults.KEY_TEXT_SHADOW_SIZE;
				case SET_KEY_RADIUS:
					return Defaults.KEY_RADIUS;
				case SET_KEY_TEXTSIZE:
					return Defaults.KEY_TEXT_SIZE;
				case SET_KEYBOARD_LANG_SELECT:
					return Defaults.KEYBOARD_LANGUAGE_KEY;
				case SET_KEYBOARD_TEXTTYPE_SELECT:
					return Defaults.KEY_FONT_TYPE;
				case SET_KEYBOARD_BGCLR:
					return Defaults.KEYBOARD_BACKGROUND_COLOR;
				case SET_KEYBOARD_SHOW_POPUP:
					return Defaults.KEYBOARD_SHOW_POPUP;
				case SET_KEYBOARD_LC_ON_EMOJI:
					return Defaults.KEYBOARD_LC_ON_EMOJI;
				case SET_PLAY_SND_PRESS:
					return Defaults.KEYBOARD_TOUCH_SOUND;
				case SET_KEY_BGCLR:
					return Defaults.KEY_BACKGROUND_COLOR;
				case SET_KEY2_BGCLR:
					return Defaults.KEY2_BACKGROUND_COLOR;
				case SET_ENTER_BGCLR:
					if(Build.VERSION.SDK_INT < 21)
						return Defaults.ENTER_BACKGROUND_COLOR;
					TypedArray arr = SuperBoardApplication.getApplication().obtainStyledAttributes(0, new int[]{ android.R.attr.colorAccent });
					int color = arr.getColor(0, Defaults.ENTER_BACKGROUND_COLOR);
					arr.recycle();
					return color;
				case SET_KEY_SHADOWCLR:
					return Defaults.KEY_TEXT_SHADOW_COLOR;
				case SET_KEY_TEXTCLR:
					return Defaults.KEY_TEXT_COLOR;
			}
		}
		return null;
	}

	public int[] getMinMaxNumbers(final String key){
		int[] nums = new int[2];
		if(containsKey(key)){
			switch(get(key)){
				case DECIMAL_NUMBER:
					nums[0] = 0;
					switch(key){
						case SET_KEYBOARD_BGBLUR:
							nums[1] = Constants.MAX_OTHER_VAL;
							break;
						case SET_KEY_VIBRATE_DURATION:
							nums[1] = Constants.MAX_VIBR_DUR;
							break;
					}
					break;
				case MM_DECIMAL_NUMBER:
					switch(key){
						case SET_KEYBOARD_HEIGHT:
							nums[0] = Constants.MIN_KEYBD_HGT;
							nums[1] = Constants.MAX_KEYBD_HGT;
							break;
						case SET_KEY_LONGPRESS_DURATION:
							nums[0] = Constants.MIN_LPRESS_DUR;
							nums[1] = Constants.MAX_LPRESS_DUR;
							break;
					}
					break;
				case FLOAT_NUMBER:
					nums[0] = 0;
					switch(key){
						case SET_KEY_PADDING:
						case SET_KEY_SHADOWSIZE:
							nums[1] = Constants.MAX_OTHER_VAL;
							break;
						case SET_KEY_RADIUS:
							nums[1] = Constants.MAX_RADS_DUR;
							break;
						case SET_KEY_TEXTSIZE:
							nums[0] = Constants.MIN_TEXT_SIZE;
							nums[1] = Constants.MAX_TEXT_SIZE;
							break;
					}
					break;
			}
		}
		return nums;
	}
	
}
