package org.blinksd;

import android.app.Application;
import java.util.HashMap;
import org.superdroid.db.SuperDB;
import org.superdroid.db.SuperDBHelper;
import org.blinksd.board.LayoutUtils;
import org.blinksd.board.LayoutUtils.Language;

public class SuperBoardApplication extends Application {
	
	static HashMap<String,Language> langs = null;
	static SuperDB appDB = null;
	
	@Override
	public void onCreate(){
		appDB = SuperDBHelper.getDefault(getApplicationContext());
		try {
			langs = LayoutUtils.getLanguageList(getApplicationContext());
		} catch(Throwable t){
			langs = new HashMap<String,LayoutUtils.Language>();
		}
	}
	
	public static SuperDB getApplicationDatabase(){
		return appDB;
	}
	
	public static HashMap<String,Language> getKeyboardLanguageList(){
		return langs;
	}
	
	public static Language getKeyboardLanguage(String name){
		return langs.containsKey(name) ? langs.get(name) : LayoutUtils.getEmptyLanguage();
	}
	
}
