package org.blinksd;

import android.app.Application;
import java.util.ArrayList;
import java.util.HashMap;
import org.superdroid.db.SuperDB;
import org.superdroid.db.SuperDBHelper;
import org.blinksd.board.LayoutUtils;
import org.blinksd.board.LayoutUtils.Language;
import org.blinksd.board.AppSettings.Key;

public class SuperBoardApplication extends Application {
	
	static HashMap<String,Language> langs = null;
	static SuperDB appDB = null;
	static SuperBoardApplication app = null;
	
	@Override
	public void onCreate(){
		app = this;
		appDB = SuperDBHelper.getDefault(getApplicationContext());
		try {
			langs = LayoutUtils.getLanguageList(getApplicationContext());
		} catch(Throwable t){
			langs = new HashMap<String,LayoutUtils.Language>();
		}
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
	
	public static Language getKeyboardLanguage(String name){
		return langs.containsKey(name) ? langs.get(name) : LayoutUtils.getEmptyLanguage();
	}
	
	public static Language getNextLanguage(){
		ArrayList<String> ll = LayoutUtils.getKeyListFromLanguageList(langs);
		String key = Key.keyboard_lang_select.name();
		String sel = appDB.getString(key,"");
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
	
}
