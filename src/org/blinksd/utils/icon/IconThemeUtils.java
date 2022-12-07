package org.blinksd.utils.icon;

import org.blinksd.Defaults;
import org.blinksd.SuperBoardApplication;
import org.blinksd.board.R;
import org.blinksd.board.SettingMap;
import org.blinksd.utils.layout.BaseMap;
import org.superdroid.db.SuperDBHelper;

public class IconThemeUtils extends BaseMap<String,int[]> {
	public IconThemeUtils(){
		put("theme_default", new int[]{
				R.drawable.sym_keyboard_shift,
				R.drawable.sym_keyboard_emoji,
				SpaceBarThemeUtils.SPACEBAR_DEFAULT,
				R.drawable.sym_keyboard_return,
				R.drawable.sym_keyboard_delete
			});
		put("theme_board", new int[]{
				R.drawable.sym_board_shift,
				R.drawable.sym_board_emoji,
				SpaceBarThemeUtils.SPACEBAR_DEFAULT,
				R.drawable.sym_board_return,
				R.drawable.sym_board_delete
			});
		put("theme_ay", new int[]{
				R.drawable.sym_ay_shift,
				R.drawable.sym_board_emoji,
				SpaceBarThemeUtils.SPACEBAR_DEFAULT,
				R.drawable.sym_board_return,
				R.drawable.sym_ay_delete
			});
	}
	
	public int getIconResource(int type){
		String key = SuperDBHelper.getValueOrDefault(SettingMap.SET_ICON_THEME);
		return getIconResource(key, type);
	}

	public int getIconResource(String themeKey, int type){
		if(type == SYM_TYPE_SPACE){
			int res = SuperBoardApplication.getSpaceBarStyles().getIconResource();
			if(res != SpaceBarThemeUtils.SPACEBAR_DEFAULT){
				return res;
			}
		}
		
		int[] theme = get(containsKey(themeKey) ? themeKey : Defaults.ICON_THEME);
		return theme.length > type ? theme[type] : SpaceBarThemeUtils.SPACEBAR_DEFAULT;
	}

	public static final int SYM_TYPE_SHIFT = 0, SYM_TYPE_EMOJI = 1,
							SYM_TYPE_SPACE = 2, SYM_TYPE_ENTER = 3,
							SYM_TYPE_DELETE = 4;
}
