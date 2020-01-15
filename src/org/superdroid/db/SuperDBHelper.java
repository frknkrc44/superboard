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
	
	public static String getValueAndSetItToDefaultIsNotSet(SuperDB db, String key){
		if(db.isRAMClean()) db.onlyRead();
		String ret = "";
		if(!db.isDBContainsKey(key)){
			db.putString(key,""+SuperBoardApplication.getSettings().getDefaults(key));
			db.writeKey(key);
		}
		return db.getString(key,ret);
	}
	
	public static int getIntValueAndSetItToDefaultIsNotSet(SuperDB db, String key){
		return Integer.valueOf(getValueAndSetItToDefaultIsNotSet(db, key));
	}
	
	public static boolean getBooleanValueAndSetItToDefaultIsNotSet(SuperDB db, String key){
		return Boolean.valueOf(getValueAndSetItToDefaultIsNotSet(db, key));
	}
	
	public static long getLongValueAndSetItToDefaultIsNotSet(SuperDB db, String key){
		return Long.valueOf(getValueAndSetItToDefaultIsNotSet(db, key));
	}
	
	public static float getFloatValueAndSetItToDefaultIsNotSet(SuperDB db, String key){
		return Float.valueOf(getValueAndSetItToDefaultIsNotSet(db, key));
	}
	
	public static double getDoubleValueAndSetItToDefaultIsNotSet(SuperDB db, String key){
		return Double.valueOf(getValueAndSetItToDefaultIsNotSet(db, key));
	}
	
	public static byte getByteValueAndSetItToDefaultIsNotSet(SuperDB db, String key){
		return Byte.valueOf(getValueAndSetItToDefaultIsNotSet(db, key));
	}
	
}
