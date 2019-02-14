package org.superdroid.db;

import android.content.*;

public class SuperDBHelper {
	
	public static SuperDB getDefault(Context c){
		return new SuperDB(c.getPackageName(),c.getFilesDir());
	}
	
	public static String getValueAndSetItToDefaultIsNotSet(SuperDB db, String key, String def){
		if(!db.isDBContainsKey(key)){
			db.putString(key,def);
			db.refresh();
		}
		return db.getString(key,def);
	}
	
	public static int getIntValueAndSetItToDefaultIsNotSet(SuperDB db, String key, int def){
		return Integer.valueOf(getValueAndSetItToDefaultIsNotSet(db, key, def+""));
	}
	
	public static boolean getBooleanValueAndSetItToDefaultIsNotSet(SuperDB db, String key, boolean def){
		return Boolean.valueOf(getValueAndSetItToDefaultIsNotSet(db, key, def+""));
	}
	
	public static long getLongValueAndSetItToDefaultIsNotSet(SuperDB db, String key, long def){
		return Long.valueOf(getValueAndSetItToDefaultIsNotSet(db, key, def+""));
	}
	
	public static float getFloatValueAndSetItToDefaultIsNotSet(SuperDB db, String key, float def){
		return Float.valueOf(getValueAndSetItToDefaultIsNotSet(db, key, def+""));
	}
	
	public static double getDoubleValueAndSetItToDefaultIsNotSet(SuperDB db, String key, double def){
		return Double.valueOf(getValueAndSetItToDefaultIsNotSet(db, key, def+""));
	}
	
	public static byte getByteValueAndSetItToDefaultIsNotSet(SuperDB db, String key, byte def){
		return Byte.valueOf(getValueAndSetItToDefaultIsNotSet(db, key, def+""));
	}
	
}
