package org.blinksd.utils.icon;

import org.blinksd.*;
import org.blinksd.board.*;
import org.blinksd.utils.layout.*;
import org.superdroid.db.*;

public class SpaceBarThemeUtils extends BaseMap<String,Integer> {
	public static final int SPACEBAR_DEFAULT = 0, SPACEBAR_TEXT = 1, SPACEBAR_HIDE = 2;
	
	public SpaceBarThemeUtils(){
		put("theme", SPACEBAR_DEFAULT);
		put("text", SPACEBAR_TEXT);
		put("daisy", org.blinksd.board.R.drawable.sym_keyboard_daisy);
		put("spacebar", org.blinksd.board.R.drawable.sym_keyboard_spacebar);
		put("hide", SPACEBAR_HIDE);
	}
	
	public int getIconResource(){
		String key = SuperDBHelper.getValueOrDefault(SettingMap.SET_KEYBOARD_SPACETYPE_SELECT);
		return getIconResource(key);
	}

	public int getIconResource(String themeKey){
		return get(containsKey(themeKey) ? themeKey : Defaults.KEYBOARD_SPACETYPE);
	}
}
