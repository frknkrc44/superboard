package org.blinksd.board;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.media.*;
import android.net.*;
import android.os.*;
import android.provider.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import org.blinksd.*;
import org.blinksd.board.dictionary.*;
import org.blinksd.sdb.*;
import org.blinksd.utils.icon.*;
import org.blinksd.utils.image.*;
import org.blinksd.utils.layout.*;
import org.superdroid.db.*;

import static android.media.AudioManager.*;

public class AppSettingsV2 extends Activity {
	private LinearLayout main;
	private SuperMiniDB sdb;
	private SuperBoard sb;
	private static ImageView iv;
	private SettingsCategorizedListView mSettView;
	

	public void recreate() {
		if(Build.VERSION.SDK_INT >= 11) {
			super.recreate();
			return;
		}
		
		onCreate(getIntent().getExtras());
	}
	
	@Override
	protected void onCreate(Bundle b){
		super.onCreate(b);
		sdb = SuperBoardApplication.getApplicationDatabase();
		main = LayoutCreator.createFilledVerticalLayout(FrameLayout.class,this);
		
		if(Build.VERSION.SDK_INT >= 31){
			getWindow().getDecorView().setFitsSystemWindows(true);
			main.setFitsSystemWindows(false);
			getWindow().setNavigationBarColor(0);
			getWindow().setStatusBarColor(0);
			getWindow().setBackgroundDrawableResource(android.R.color.system_neutral1_900);
		}
		
		try {
			createMainView();
		} catch(Throwable e){
			Log.e("MainView","Error:",e);
		}
		main.addView(mSettView);
		setKeyPrefs();
		setContentView(main);
	}
	
	private void createPreviewView(){
		FrameLayout ll = (FrameLayout) LayoutCreator.getHFilledView(FrameLayout.class, LinearLayout.class, this);
		sb = new SuperBoard(this){
			@Override
			public void sendDefaultKeyboardEvent(View v){
				playSound(v.getId());
			}
			
			@Override
			public void playSound(int event){
				if(!SuperDBHelper.getBooleanValueOrDefault(SettingMap.SET_PLAY_SND_PRESS)) return;
				AudioManager audMgr = (AudioManager) getSystemService(AUDIO_SERVICE);
				switch(event){
					case 3:
						audMgr.playSoundEffect(FX_KEYPRESS_SPACEBAR);
						break;
					case 2:
						audMgr.playSoundEffect(FX_KEYPRESS_RETURN);
						break;
					case 1:
						audMgr.playSoundEffect(FX_KEYPRESS_DELETE);
						break;
					default:
						audMgr.playSoundEffect(FX_KEYPRESS_STANDARD);
						break;
				}
			}
		};
		
		int ph = 12;
		sb.addRow(0,new String[]{"1","2","3","4"});
		sb.getKey(0,0,0).setSubText("½");
		for(int i = 0;i < 4;i++) sb.getKey(0,0,i).setId(i);
		sb.createEmptyLayout();
		sb.setEnabledLayout(0);
		sb.setKeyboardHeight(ph);
		sb.setKeysPadding(DensityUtils.mpInt(1));
		iv = new ImageView(this);
		iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
		iv.setLayoutParams(new RelativeLayout.LayoutParams(-1,DensityUtils.hpInt(ph)));
		ll.addView(iv);
		ll.addView(sb);
		main.addView(ll);
	}
	
	private void createMainView() throws Throwable {
		createPreviewView();
		mSettView = new SettingsCategorizedListView(this);
	}
	
	private void setKeyPrefs(){
		File img = getBackgroundImageFile();
		if(img.exists()) {
			int blur = getIntOrDefault(SettingMap.SET_KEYBOARD_BGBLUR);
			Bitmap b = BitmapFactory.decodeFile(img.getAbsolutePath());
			iv.setImageBitmap(blur > 0 ? ImageUtils.getBlur(b,blur) : b);
		} else {
			iv.setImageBitmap(null);
		}
		int keyClr = SuperDBHelper.getIntValueOrDefault(SettingMap.SET_KEY_BGCLR);
		int keyPressClr = SuperDBHelper.getIntValueOrDefault(SettingMap.SET_KEY_PRESS_BGCLR);
		sb.setKeysBackground(LayoutUtils.getKeyBg(keyClr,keyPressClr,true));
		sb.setKeysShadow(getIntOrDefault(SettingMap.SET_KEY_SHADOWSIZE),getIntOrDefault(SettingMap.SET_KEY_SHADOWCLR));
		sb.setKeyTintColor(0,0,2,getIntOrDefault(SettingMap.SET_KEY2_BGCLR),getIntOrDefault(SettingMap.SET_KEY2_PRESS_BGCLR));
		sb.setKeyTintColor(0,0,-1,getIntOrDefault(SettingMap.SET_ENTER_BGCLR),getIntOrDefault(SettingMap.SET_ENTER_PRESS_BGCLR));
		sb.setBackgroundColor(getIntOrDefault(SettingMap.SET_KEYBOARD_BGCLR));
		sb.setKeysTextColor(getIntOrDefault(SettingMap.SET_KEY_TEXTCLR));
		sb.setKeysTextSize(getFloatPercentOrDefault(SettingMap.SET_KEY_TEXTSIZE));
		sb.setIconSizeMultiplier(getIntOrDefault(SettingMap.SET_KEY_ICON_SIZE_MULTIPLIER));
		sb.setKeysTextType(getIntOrDefault(SettingMap.SET_KEYBOARD_TEXTTYPE_SELECT));
		IconThemeUtils icons = SuperBoardApplication.getIconThemes();
		sb.setKeyDrawable(0,0,2,icons.getIconResource(IconThemeUtils.SYM_TYPE_DELETE));
		LayoutUtils.setSpaceBarViewPrefs(icons, sb.getKey(0,0,1), SuperBoardApplication.getCurrentKeyboardLanguage().name);
		sb.setKeyDrawable(0,0,-1,icons.getIconResource(IconThemeUtils.SYM_TYPE_ENTER));
		try {
			SuperBoardApplication.clearCustomFont();
			sb.setCustomFont(SuperBoardApplication.getCustomFont());
		} catch(Throwable t){}
	}
	
	public int getFloatPercentOrDefault(String key){
		return DensityUtils.mpInt(DensityUtils.getFloatNumberFromInt(getIntOrDefault(key)));
	}
	
	public int getIntOrDefault(String key){
		return SuperDBHelper.getIntValueOrDefault(key);
	}

	public void restartKeyboard(){
		setKeyPrefs();
		sendBroadcast(new Intent(InputService.COLORIZE_KEYBOARD));
	}
	
	public static File getBackgroundImageFile(){
		return new File(SuperBoardApplication.getApplication().getFilesDir()+"/bg");
	}
	
	@Override
	protected void onActivityResult(int requestCode,int resultCode,Intent data){
		super.onActivityResult(requestCode,resultCode,data);
		if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
			Uri uri = data.getData();
			new ImageTask().execute(getContentResolver(),uri);
		}
	}
	
	private class ImageTask extends AsyncTask<Object,Bitmap,Bitmap> {

		@Override
		protected Bitmap doInBackground(Object[] p1){
			try {
				return MediaStore.Images.Media.getBitmap((ContentResolver)p1[0],(Uri)p1[1]);
			} catch(Throwable e){}
			return null;
		}

		@Override
		protected void onPostExecute(Bitmap result){
			super.onPostExecute(result);
			if(result != null){
				result = ImageUtils.getMinimizedBitmap(result);
				ImageView img = mSettView.mAdapter.dialogView.findViewById(android.R.id.custom);
				img.setImageBitmap(result);
			}
		}

	}
	
	public static class SettingItem {
		public final SettingCategory category;
		public final SettingType type;
		
		public SettingItem(SettingCategory category, SettingType type){
			this.category = category;
			this.type = type;
		}
	}
	
	public static enum SettingCategory {
		GENERAL,
		THEMING,
		THEMING_ADVANCED,
	}
	
	public static enum SettingType {
		BOOL,
		THEME_SELECTOR,
		COLOR_SELECTOR,
		STR_SELECTOR,
		SELECTOR,
		DECIMAL_NUMBER,
		FLOAT_NUMBER,
		MM_DECIMAL_NUMBER,
		IMAGE,
		REDIRECT,
	}
	
}
