package org.blinksd.board;

import android.content.*;
import android.content.res.*;
import android.graphics.drawable.*;
import android.inputmethodservice.*;
import android.os.*;
import android.util.*;
import android.view.*;
import java.util.*;
import org.blinksd.board.*;
import org.json.*;
import org.superdroid.db.*;

public class LayoutUtils {
	
	private LayoutUtils(){}
	
	private static List<List<KeyOptions>> createLayoutFromJSON(JSONArray jsonData) throws JSONException {
		List<List<KeyOptions>> jsonList = new ArrayList<List<KeyOptions>>();
		for(int i = 0;i < jsonData.length();i++){
			jsonList.add(new ArrayList<KeyOptions>());
			JSONObject jo = jsonData.getJSONObject(i);
			for(int g = 0;g < jo.length();g++){
				JSONArray ja = jo.getJSONArray("row");
				for(int j = 0;j < ja.length();j++){
					JSONObject jox = ja.getJSONObject(j);
					KeyOptions ko = new KeyOptions();
					ko.key = jox.getString("key");
					try {
						ko.width = jox.getInt("width");
					} catch(Throwable e){
						ko.width = 0;
					}
					
					try {
						ko.pressKeyCode = jox.getInt("pkc");
					} catch(Throwable e){
						ko.pressKeyCode = 0;
					}
					
					try {
						ko.longPressKeyCode = jox.getInt("lpkc");
					} catch(Throwable e){
						ko.longPressKeyCode = 0;
					}
					
					try {
						ko.repeat = jox.getBoolean("rep");
					} catch(Throwable e){
						ko.repeat = false;
					}
					
					try {
						ko.pressIsNotEvent = jox.getBoolean("pine");
					} catch(Throwable e){
						ko.pressIsNotEvent = false;
					}
					
					try {
						ko.longPressIsNotEvent = jox.getBoolean("lpine");
					} catch(Throwable e){
						ko.longPressIsNotEvent = false;
					}
					
					try {
						ko.longPressIsNotEvent = jox.getBoolean("lpine");
					} catch(Throwable e){
						ko.longPressIsNotEvent = false;
					}
					
					try {
						ko.darkerKeyTint = jox.getBoolean("dkt");
					} catch(Throwable e){
						ko.darkerKeyTint = false;
					}
					
					jsonList.get(i).add(ko);
				}
			}
		}
		return jsonList;
	}
	
	public static String[][] getLayoutKeysFromList(List<List<KeyOptions>> list){
		if(list != null){
			String[][] out = new String[list.size()][];
			for(int i = 0;i < list.size();i++){
				List<KeyOptions> subList = list.get(i);
				String[] subOut = new String[subList.size()];
				for(int g = 0;g < subList.size();g++){
					subOut[g] = subList.get(g).key;
				}
				out[i] = subOut;
			}
			return out;
		}
		return null;
	}
	
	private static Language getLanguage(String fileData) throws JSONException {
		Language l = new Language();
		JSONObject main = new JSONObject(fileData);
		l.name = main.getString("name");
		l.label = main.getString("label");
		l.enabled = main.getBoolean("enabled");
		l.enabledSdk = main.getInt("enabledSdk");
		l.midPadding = main.getBoolean("midPadding");
		l.author = main.getString("author");
		l.language = main.getString("language");
		l.layout = createLayoutFromJSON(main.getJSONArray("layout"));
		l.popup = createLayoutFromJSON(main.getJSONArray("popup"));
		return l;
	}
	
	public static Language getLanguage(Context ctx, String name){
		try {
			HashMap<String,Language> llist = getLanguageList(ctx);
			if(llist.containsKey(name)){
				return llist.get(name);
			}
		} catch(Throwable t){}
		return getEmptyLanguage();
	}
	
	public static Language getEmptyLanguage(){
		Language l = new Language();
		l.layout = l.popup = new ArrayList<List<KeyOptions>>();
		return l;
	}
	
	public static HashMap<String,Language> getLanguageList(Context ctx) throws Throwable {
		HashMap<String,Language> langs = new HashMap<String,Language>();
		AssetManager assets = ctx.getAssets();
		String[] items = assets.list("");
		Arrays.sort(items);
		for(String str : items){
			if(str.endsWith(".json")){
				Scanner sc = new Scanner(assets.open(str));
				String s = "";
				while(sc.hasNext()) s += sc.nextLine();
				Language l = getLanguage(s);
				if(l.enabled && Build.VERSION.SDK_INT >= l.enabledSdk){
					langs.put(l.language,l);
				}
			}
		}
		return langs;
	}
	
	public static void setKeyOpts(List<List<KeyOptions>> langPack, SuperBoard sb){
		for(int i = 0;i < langPack.size();i++){
			List<KeyOptions> subList = langPack.get(i);
			for(int g = 0;g < subList.size();g++){
				KeyOptions ko = subList.get(g);
				if(ko.width > 0){
					sb.setKeyWidthPercent(0,i,g,ko.width);
				}
				if(ko.pressKeyCode != 0){
					sb.setPressEventForKey(0,i,g,ko.pressKeyCode,!ko.pressIsNotEvent);
				}
				if(ko.longPressKeyCode != 0){
					sb.setLongPressEventForKey(0,i,g,ko.longPressKeyCode,!ko.longPressIsNotEvent);
				}
				if(ko.repeat){
					sb.setKeyRepeat(0,i,g);
				}
				switch(ko.pressKeyCode){
					case Keyboard.KEYCODE_SHIFT:
						sb.setKeyDrawable(0,i,g,R.drawable.sym_keyboard_shift);
						break;
					case Keyboard.KEYCODE_DELETE:
						sb.setKeyDrawable(0,i,g,R.drawable.sym_keyboard_delete);
						break;
					case Keyboard.KEYCODE_MODE_CHANGE:
						sb.getKey(0,i,g).setText("!?#");
						break;
					case KeyEvent.KEYCODE_SPACE:
						Context ctx = sb.getContext();
						sb.getKey(0,i,g).setText(ctx.getApplicationInfo().loadLabel(ctx.getPackageManager()));
						break;
					case Keyboard.KEYCODE_DONE:
						sb.setKeyDrawable(0,i,g,R.drawable.sym_keyboard_return);
						break;
					case SuperBoard.KEYCODE_SWITCH_LANGUAGE:
						sb.setKeyDrawable(0,i,g,R.drawable.sym_keyboard_language);
				}
			}
		}
	}
	
	public static ArrayList<String> getKeyListFromLanguageList(HashMap<String,Language> list){
		ArrayList<String> a = new ArrayList<String>(list.keySet());
		return a;
	}
	
	public static Drawable getKeyBg(SuperDB sd,SuperBoard sb,int clr,boolean pressEffect){
		GradientDrawable gd = new GradientDrawable();
		gd.setColor(sb.getColorWithState(clr,false));
		gd.setCornerRadius(sb.mp(AppSettings.a(SuperDBHelper.getIntValueAndSetItToDefaultIsNotSet(sd,AppSettings.Key.key_radius.name(),10))));
		gd.setStroke(sb.mp(AppSettings.a(SuperDBHelper.getIntValueAndSetItToDefaultIsNotSet(sd,AppSettings.Key.key_padding.name(),10))),0);
		if(pressEffect){
			StateListDrawable d = new StateListDrawable();
			GradientDrawable pd = new GradientDrawable();
			pd.setColor(sb.getColorWithState(clr,true));
			pd.setCornerRadius(sb.mp(AppSettings.a(SuperDBHelper.getIntValueAndSetItToDefaultIsNotSet(sd,AppSettings.Key.key_radius.name(),10))));
			pd.setStroke(sb.mp(AppSettings.a(SuperDBHelper.getIntValueAndSetItToDefaultIsNotSet(sd,AppSettings.Key.key_padding.name(),10))),0);
			d.addState(new int[]{android.R.attr.state_selected},pd);
			d.addState(new int[]{},gd);
			return d;
		}
		return gd;
	}
	
	public static class Language {
		public String name = "";
		public String label = "";
		public boolean enabled;
		public int enabledSdk = 1;
		public boolean midPadding;
		public String author = "";
		public String language = "";
		public List<List<KeyOptions>> layout;
		public List<List<KeyOptions>> popup;
	}
	
	public static class KeyOptions {
		public String key;
		public int width = 0;
		public int pressKeyCode = 0;
		public int longPressKeyCode = 0;
		public boolean repeat;
		public boolean pressIsNotEvent;
		public boolean longPressIsNotEvent;
		public boolean darkerKeyTint;
	}
	
}
