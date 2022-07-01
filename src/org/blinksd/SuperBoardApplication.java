package org.blinksd;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.util.*;
import java.io.*;
import java.util.*;
import org.blinksd.board.*;
import org.blinksd.board.LayoutUtils.*;
import org.blinksd.sdb.*;
import org.superdroid.db.*;
import org.blinksd.utils.color.ThemeUtils;
import org.blinksd.utils.color.ThemeUtils.*;
import org.blinksd.utils.icon.*;
import yandroid.util.*;
import org.blinksd.utils.dictionary.*;
import android.os.*;

public class SuperBoardApplication extends Application {
	
	private static HashMap<String,Language> languageCache = null;
	private static SuperMiniDB appDB = null;
	private static SuperBoardApplication appContext = null;
	private static SettingMap settingMap = null;
	private static Typeface customFont = null;
	private static File fontFile = null;
	private static String fontPath = null;
	private static IconThemeUtils icons;
	private static SpaceBarThemeUtils spaceBars;
	private static List<ThemeHolder> themes;
	private static DictionaryDB dictDB;
	
	
	@Override
	public void onCreate(){
		Styleable.tryToBypassRestrictions();
		appContext = this;
		appDB = SuperDBHelper.getDefault(getApplicationContext());
		icons = new IconThemeUtils();
		spaceBars = new SpaceBarThemeUtils();
		settingMap = new SettingMap();
		fontPath = getApplication().getExternalCacheDir()+"/font.ttf";
		
		try {
			languageCache = LayoutUtils.getLanguageList(getApplicationContext());
		} catch(Throwable t){
			throw new RuntimeException(t);
		}
		
		reloadThemeCache();
		
		new Thread() {
			public void run(){
				dictDB = new DictionaryDB(appContext);
				// dictDB.fillDB();
			}
		}.start();
	}
	
	public static DictionaryDB getDictDB(){
		return dictDB;
	}

	public static boolean isDictsReady(){
		return dictDB != null && dictDB.isReady;
	}
	
	public static SuperBoardApplication getApplication(){
		return appContext;
	}
	
	public static SuperMiniDB getApplicationDatabase(){
		return appDB;
	}
	
	public static HashMap<String,Language> getKeyboardLanguageList(){
		return languageCache;
	}

	public static List<ThemeHolder> getThemes(){
		return themes;
	}

	public static void reloadThemeCache(){
		try {
			themes = ThemeUtils.getThemes();
		} catch(Throwable t){
			throw new RuntimeException(t);
			// themes = new ArrayList<>();
		}
	}
	
	public static IconThemeUtils getIconThemes(){
		return icons;
	}
	
	public static SpaceBarThemeUtils getSpaceBarStyles(){
		return spaceBars;
	}
	
	public static Typeface getCustomFont(){
		if(customFont == null){
			try {
				if(fontFile == null) fontFile = new File(fontPath);
				if(fontFile.exists()) customFont = Typeface.createFromFile(fontFile);
				else throw new Throwable();
			} catch(Throwable t){
				// Log.e("SuperBoardApplication","Exception: ",t);
				fontFile = null;
				customFont = null;
				return Typeface.DEFAULT;
			}
		}
		return customFont;
	}
	
	public static void clearCustomFont(){
		File newFile = new File(fontPath);
		if(fontFile == null || fontFile.lastModified() != newFile.lastModified()){
			customFont = null;
		}
	}
	
	public static Language getCurrentKeyboardLanguage(){
		String key = SettingMap.SET_KEYBOARD_LANG_SELECT;
		return getKeyboardLanguage(appDB.getString(key,(String)settingMap.getDefaults(key)));
	}
	
	public static Language getKeyboardLanguage(String name){
		return languageCache.containsKey(name) ? languageCache.get(name) : LayoutUtils.getEmptyLanguage();
	}
	
	public static Language getNextLanguage(){
		ArrayList<String> ll = LayoutUtils.getKeyListFromLanguageList(languageCache);
		String key = SettingMap.SET_KEYBOARD_LANG_SELECT;
		String sel = appDB.getString(key,(String)settingMap.getDefaults(key));
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
				Language l = languageCache.get(ll.get(index));
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
		List<String> langKeys = new ArrayList<String>(languageCache.keySet());
		List<String> out = new ArrayList<String>();
		for(String key : langKeys){
			Language lang = languageCache.get(key);
			out.add(lang.label);
		}
		return out;
	}
	
	public static List<String> getLanguageTypes(){
		List<String> langKeys = new ArrayList<String>(languageCache.keySet());
		List<String> out = new ArrayList<String>();
		for(String key : langKeys){
			Language lang = languageCache.get(key);
			String name = lang.language.split("_")[0].toLowerCase();
			if(!out.contains(name))
				out.add(name);
		}
		return out;
	}
	
	public static SettingMap getSettings(){
		return settingMap;
	}

	@Override
	public void onTerminate(){
		super.onTerminate();
		if(dictDB != null) dictDB.close();
	}
	
	
}
