package org.superdroid.db;

import android.content.*;
import org.blinksd.*;

public class SuperDBHelper {
	
	private SuperDBHelper(){}
	
	public static SuperDB getDefault(Context c){
		return new SuperDB(c.getPackageName(),c.getFilesDir());
	}

	public static SuperDB getDefault(Context c, String key){
		return new SuperDB(c.getPackageName(),c.getFilesDir(),key);
	}
	
	public static String getValueOrDefault(SuperDB db, String key){
		if(db.isRAMClean()) db.onlyRead();
		String ret = "";
		if(!db.isDBContainsKey(key)){
			db.putString(key,""+SuperBoardApplication.getSettings().getDefaults(key));
			db.writeKey(key);
		}
		return db.getString(key,ret);
	}
	
	public static int getIntValueOrDefault(SuperDB db, String key){
		return Integer.valueOf(getValueOrDefault(db, key));
	}
	
	public static boolean getBooleanValueOrDefault(SuperDB db, String key){
		return Boolean.valueOf(getValueOrDefault(db, key));
	}
	
	public static long getLongValueOrDefault(SuperDB db, String key){
		return Long.valueOf(getValueOrDefault(db, key));
	}
	
	public static float getFloatValueOrDefault(SuperDB db, String key){
		return Float.valueOf(getValueOrDefault(db, key));
	}
	
	public static double getDoubleValueOrDefault(SuperDB db, String key){
		return Double.valueOf(getValueOrDefault(db, key));
	}
	
	public static byte getByteValueOrDefault(SuperDB db, String key){
		return Byte.valueOf(getValueOrDefault(db, key));
	}
	
}
