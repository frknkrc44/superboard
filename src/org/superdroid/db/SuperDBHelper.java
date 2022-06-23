package org.superdroid.db;

import android.content.*;
import android.content.res.*;
import android.os.*;
import org.blinksd.*;
import org.blinksd.board.*;
import org.blinksd.sdb.*;

public class SuperDBHelper {
	
	private SuperDBHelper(){}
	
	public static SuperMiniDB getDefault(Context c){
		return new SuperMiniDB(c.getPackageName(),c.getFilesDir());
	}

	public static SuperMiniDB getDefault(Context c, String key){
		return new SuperMiniDB(c.getPackageName(),c.getFilesDir(),key);
	}
	
	public static String getValueOrDefault(String key){
		SuperMiniDB db = SuperBoardApplication.getApplicationDatabase();
		String ret = "";
		if(!db.isDBContainsKey(key)){
			db.putString(key,""+SuperBoardApplication.getSettings().getDefaults(key));
			db.refreshKey(key);
		}
		return db.getString(key,ret);
	}
	
	public static int getIntValueOrDefault(String key){
		if(Build.VERSION.SDK_INT >= 31 && getBooleanValueOrDefault(SettingMap.SET_USE_MONET)){
			return getMonetColorValue(key);
		}
		
		return Integer.valueOf(getValueOrDefault(key));
	}
	
	public static boolean getBooleanValueOrDefault(String key){
		return Boolean.valueOf(getValueOrDefault(key));
	}
	
	public static long getLongValueOrDefault(String key){
		return Long.valueOf(getValueOrDefault(key));
	}
	
	public static float getFloatValueOrDefault(String key){
		return Float.valueOf(getValueOrDefault(key));
	}
	
	public static double getDoubleValueOrDefault(String key){
		return Double.valueOf(getValueOrDefault(key));
	}
	
	public static byte getByteValueOrDefault(String key){
		return Byte.valueOf(getValueOrDefault(key));
	}
	
	private static int getMonetColorValue(String key){
		Resources res = SuperBoardApplication.getApplication().getResources();
		Configuration conf = res.getConfiguration();
		boolean dark = (conf.uiMode & Configuration.UI_MODE_NIGHT_MASK)  == Configuration.UI_MODE_NIGHT_YES;
		switch(key){
			case SettingMap.SET_ENTER_BGCLR:
				return res.getColor(dark ? android.R.color.system_accent1_500 : android.R.color.system_accent1_300);
			case SettingMap.SET_ENTER_PRESS_BGCLR:
				return res.getColor(dark ? android.R.color.system_accent1_400 : android.R.color.system_accent1_400);
			case SettingMap.SET_KEY_BGCLR:
				return res.getColor(dark ? android.R.color.system_neutral1_600 : android.R.color.system_neutral1_100);
			case SettingMap.SET_KEY_PRESS_BGCLR:
				return res.getColor(dark ? android.R.color.system_neutral1_500 : android.R.color.system_neutral1_200);
			case SettingMap.SET_KEY2_BGCLR:
				return res.getColor(dark ? android.R.color.system_neutral1_700 : android.R.color.system_neutral1_200);
			case SettingMap.SET_KEY2_PRESS_BGCLR:
				return res.getColor(dark ? android.R.color.system_neutral1_600 : android.R.color.system_neutral1_300);
			case SettingMap.SET_KEYBOARD_BGCLR:
				return res.getColor(dark ? android.R.color.system_neutral1_800 : android.R.color.system_neutral1_50);
			case SettingMap.SET_KEY_TEXTCLR:
				return res.getColor(dark ? android.R.color.system_neutral1_100 : android.R.color.system_neutral1_900);
		}
		
		return Integer.valueOf(getValueOrDefault(key));
	}
}
