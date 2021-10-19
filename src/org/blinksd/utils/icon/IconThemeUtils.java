package org.blinksd.utils.icon;

import java.util.*;
import org.blinksd.board.*;
import org.blinksd.*;
import org.blinksd.sdb.*;
import org.superdroid.db.*;
import java.util.Map.*;

public class IconThemeUtils extends LinkedHashMap<String, int[]> {

	public IconThemeUtils(){
		put("theme_default", new int[]{
				R.drawable.sym_keyboard_shift,
				R.drawable.sym_keyboard_emoji,
				android.R.color.transparent,
				R.drawable.sym_keyboard_return,
				R.drawable.sym_keyboard_delete
			});
		put("theme_daisy", new int[]{
				R.drawable.sym_keyboard_shift,
				R.drawable.sym_keyboard_emoji,
				R.drawable.sym_keyboard_daisy,
				R.drawable.sym_keyboard_return,
				R.drawable.sym_keyboard_delete
			});
		put("theme_board", new int[]{
				R.drawable.sym_board_shift,
				R.drawable.sym_board_emoji,
				android.R.color.transparent,
				R.drawable.sym_board_return,
				R.drawable.sym_board_delete
			});
		put("theme_ay", new int[]{
				R.drawable.sym_ay_shift,
				R.drawable.sym_board_emoji,
				android.R.color.transparent,
				R.drawable.sym_board_return,
				R.drawable.sym_ay_delete
			});
	}
	
	public ArrayList<String> getThemeList(){
		return new ArrayList<String>(keySet());
	}
	
	public int indexOf(String theme){
		return getThemeList().indexOf(theme);
	}
	
	public String getFromIndex(int index){
		List<String> keys = getThemeList();
		return keys.get(keys.size() > index ? index : 0);
	}
	
	public int getIconResource(int type){
		String key = SuperDBHelper.getValueOrDefault(SettingMap.SET_ICON_THEME);
		return getIconResource(key, type);
	}

	public int getIconResource(String themeKey, int type){
		int[] theme = get(containsKey(themeKey) ? themeKey : Defaults.ICON_THEME);
		return theme.length > type ? theme[type] : android.R.color.transparent;
	}

	public static final int SYM_TYPE_SHIFT = 0, SYM_TYPE_EMOJI = 1,
							SYM_TYPE_SPACE = 2, SYM_TYPE_ENTER = 3,
							SYM_TYPE_DELETE = 4;
}
