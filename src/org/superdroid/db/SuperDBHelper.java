package org.superdroid.db;

import android.content.*;
import org.blinksd.*;
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
	
}
