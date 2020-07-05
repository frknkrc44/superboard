package org.blinksd;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.util.*;
import java.io.*;
import java.util.*;
import org.blinksd.board.*;
import org.blinksd.board.LayoutUtils.*;
import org.blinksd.utils.database.*;
import org.superdroid.db.*;

public class SuperBoardApplication extends Application {
	
	private static HashMap<String,Language> langs = null;
	private static SuperDB appDB = null;
	private static SuperBoardApplication app = null;
	private static SettingMap sMap = null;
	private static Typeface cFont = null;
	private static File fontFile = null;
	private static String fontPath = null;
	private static SuperDBCenterConnector dbSync;
	
	@Override
	public void onCreate(){
		app = this;
		appDB = SuperDBHelper.getDefault(getApplicationContext());
		sMap = new SettingMap();
		fontPath = app.getExternalCacheDir()+"/font.ttf";
		
		try {
			langs = LayoutUtils.getLanguageList(getApplicationContext());
		} catch(Throwable t){
			langs = new HashMap<String,LayoutUtils.Language>();
		}
		
		// initSyncServer();
	}
	
	private void initSyncServer(){
		try {
			if(dbSync == null)
				dbSync = new SuperDBCenterConnector(this);
			dbSync.connectToSuperDB();
		} catch(Throwable t){}
	}
	
	public static SuperBoardApplication getApplication(){
		return app;
	}
	
	public static SuperDB getApplicationDatabase(){
		return appDB;
	}
	
	public static HashMap<String,Language> getKeyboardLanguageList(){
		return langs;
	}
	
	public static Typeface getCustomFont(){
		if(cFont == null){
			try {
				if(fontFile == null) fontFile = new File(fontPath);
				if(fontFile.exists()) cFont = Typeface.createFromFile(fontFile);
				else throw new Throwable();
			} catch(Throwable t){
				Log.e("SuperBoardApplication","Exception: ",t);
				fontFile = null;
				cFont = null;
				return Typeface.DEFAULT;
			}
		}
		return cFont;
	}
	
	public static void clearCustomFont(){
		File newFile = new File(fontPath);
		if(fontFile == null || fontFile.lastModified() != newFile.lastModified()){
			cFont = null;
		}
	}
	
	public static Language getKeyboardLanguage(String name){
		return langs.containsKey(name) ? langs.get(name) : LayoutUtils.getEmptyLanguage();
	}
	
	public static Language getNextLanguage(){
		ArrayList<String> ll = LayoutUtils.getKeyListFromLanguageList(langs);
		String key = SettingMap.SET_KEYBOARD_LANG_SELECT;
		String sel = appDB.getString(key,(String)sMap.getDefaults(key));
		if(!sel.equals("")){
			int index = -1;
			for(int i = 0;i < ll.size();i++){
				if(ll.get(i).equals(sel)){
					index = i;
					break;
				}
			}
			if(index >= 0){
				index = (index + 1) % ll.size();
				Language l = langs.get(ll.get(index));
				appDB.putString(key,l.language);
				appDB.onlyWrite();
				return l;
			}
		}
		return LayoutUtils.getEmptyLanguage();
	}
	
	/**
	 * Returns human readable language name list
	 */
	public static List<String> getLanguageHRNames(){
		List<String> langKeys = new ArrayList<String>(langs.keySet());
		List<String> out = new ArrayList<String>();
		for(String key : langKeys){
			Language lang = langs.get(key);
			out.add(lang.label);
		}
		return out;
	}
	
	public static SettingMap getSettings(){
		return sMap;
	}
	
}
