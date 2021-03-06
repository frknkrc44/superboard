package org.blinksd.board;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.inputmethodservice.*;
import android.media.*;
import android.net.*;
import android.os.*;
import android.provider.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import org.blinksd.*;
import org.blinksd.utils.color.*;
import org.blinksd.utils.color.ThemeUtils.*;
import org.blinksd.utils.image.*;
import org.blinksd.utils.layout.*;
import org.blinksd.sdb.*;
import org.superdroid.db.*;
import org.superdroid.db.*;
import yandroid.widget.*;

import static android.media.AudioManager.*;
import org.blinksd.utils.toolbar.*;
import org.blinksd.utils.icon.*;

public class AppSettingsV2 extends Activity {
	
	private ScrollView scroller;
	private LinearLayout main, sets;
	private SuperMiniDB sdb;
	private static View dialogView;
	private SettingMap sMap;
	private SuperBoard sb;
	private static ImageView iv;
	
	private static final int TAG1 = R.string.app_name, TAG2 = R.string.hello_world;

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
		sMap = SuperBoardApplication.getSettings();
		main = LayoutCreator.createFilledVerticalLayout(FrameLayout.class,this);
		SuperToolbar toolbar = new SuperToolbar(this);
		if(BuildParams.DEBUG) {
			toolbar.addMenuItem(getResources().getDrawable(R.drawable.sym_keyboard_backup), new View.OnClickListener(){

					@Override
					public void onClick(View p1){
						startActivity(new Intent(AppSettingsV2.this, BackupRestoreActivity.class));
					}

			});
		}
		toolbar.addMenuItem(getResources().getDrawable(R.drawable.sym_keyboard_close), new View.OnClickListener(){

				@Override
				public void onClick(View p1){
					sdb.removeDB();
					File bgFile = getBackgroundImageFile();
					bgFile.delete();
					recreate();
				}

		});
		toolbar.setTextColor(0xFFFFFFFF);
		main.addView(toolbar);
		sets = LayoutCreator.createFilledVerticalLayout(LinearLayout.class,this);
		int dp = DensityUtils.dpInt(16);
		sets.setPadding(dp,dp,dp,dp);
		try {
			createMainView();
		} catch(Throwable e){
			Log.e("MainView","Error:",e);
		}
		setKeyPrefs();
		scroller = new ScrollView(this);
		scroller.setLayoutParams(new FrameLayout.LayoutParams(-1,-1));
		scroller.addView(sets);
		main.addView(scroller);
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
		
		sb.addRow(0,new String[]{"1","2","3"});
		for(int i = 0;i <= 2;i++) sb.getKey(0,0,i).setId(i);
		sb.createEmptyLayout();
		sb.setEnabledLayout(0);
		sb.setKeyboardHeight(20);
		sb.setKeysPadding(sb.mp(4));
		iv = new ImageView(this);
		iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
		iv.setLayoutParams(new RelativeLayout.LayoutParams(-1,sb.hp(20)));
		ll.addView(iv);
		ll.addView(sb);
		main.addView(ll);
	}
	
	private void createMainView() throws Throwable {
		createPreviewView();
		ArrayList<String> keys = new ArrayList<String>(sMap.keySet());
		for(String key : keys){
			SettingType z = sMap.get(key);
			switch(z){
				case BOOL:
					sets.addView(createBoolSelector(key));
					break;
				case IMAGE:
					sets.addView(createImageSelector(key));
					break;
				case THEME_SELECTOR:
					List<String> themeKeys = ThemeUtils.getThemeNames(SuperBoardApplication.getThemes());
					sets.addView(createRadioSelector(key,themeKeys));
					break;
				case COLOR_SELECTOR:
					sets.addView(createColorSelector(key));
					break;
				case LANG_SELECTOR:
					List<String> keySet = SuperBoardApplication.getLanguageHRNames();
					sets.addView(createRadioSelector(key,keySet));
					break;
				case ICON_SELECTOR:
				case SELECTOR:
					List<String> selectorKeys = getArrayAsList(key);
					sets.addView(createRadioSelector(key,selectorKeys));
					break;
				case DECIMAL_NUMBER:
				case MM_DECIMAL_NUMBER:
				case FLOAT_NUMBER:
					sets.addView(createNumberSelector(key,z == SettingType.FLOAT_NUMBER));
					break;
			}
		}
	}
	
	private final View createNumberSelector(String key, boolean isFloat){
		int num = getIntOrDefault(key);
		LinearLayout numSelector = LayoutCreator.createFilledHorizontalLayout(LinearLayout.class,this);
		numSelector.getLayoutParams().height = -2;
		TextView img = LayoutCreator.createTextView(this);
		img.setId(android.R.id.text1);
		int height = (int) getListPreferredItemHeight();
		img.setGravity(Gravity.CENTER);
		img.setTextColor(0xFFFFFFFF);
		img.setText(isFloat ? getFloatNumberFromInt(num)+"" : num+"");
		img.setLayoutParams(LayoutCreator.createLayoutParams(LinearLayout.class,height,height));
		int pad = height / 4;
		img.setPadding(pad,pad,pad,pad);
		TextView btn = LayoutCreator.createTextView(this);
		btn.setGravity(Gravity.CENTER_VERTICAL);
		btn.setTextColor(0xFFFFFFFF);
		btn.setMinHeight(height);
		btn.setText(getTranslation(key));
		numSelector.setTag(key);
		numSelector.setMinimumHeight(height);
		numSelector.setOnClickListener(numberSelectorListener);
		numSelector.addView(img);
		numSelector.addView(btn);
		return numSelector;
	}
	
	private final View createColorSelector(String key){
		int color = getIntOrDefault(key);
		LinearLayout colSelector = LayoutCreator.createFilledHorizontalLayout(LinearLayout.class,this);
		colSelector.getLayoutParams().height = -2;
		ImageView img = LayoutCreator.createImageView(this);
		img.setId(android.R.id.icon);
		int height = (int) getListPreferredItemHeight();
		img.setLayoutParams(LayoutCreator.createLayoutParams(LinearLayout.class,height,height));
		img.setScaleType(ImageView.ScaleType.FIT_CENTER);
		int pad = height / 4;
		img.setPadding(pad,pad,pad,pad);
		GradientDrawable gd = new GradientDrawable();
		gd.setColor(color);
		gd.setCornerRadius(1000);
		img.setImageDrawable(gd);
		TextView btn = LayoutCreator.createTextView(this);
		btn.setGravity(Gravity.CENTER_VERTICAL);
		btn.setTextColor(0xFFFFFFFF);
		btn.setMinHeight(height);
		btn.setText(getTranslation(key));
		colSelector.setTag(key);
		colSelector.setMinimumHeight(height);
		colSelector.setOnClickListener(colorSelectorListener);
		colSelector.addView(img);
		colSelector.addView(btn);
		return colSelector;
	}
	
	private final View createImageSelector(String key){
		TextView btn = LayoutCreator.createTextView(this);
		btn.setGravity(Gravity.CENTER_VERTICAL);
		btn.setTextColor(0xFFFFFFFF);
		btn.setMinHeight((int) getListPreferredItemHeight());
		btn.setText(getTranslation(key));
		btn.setTag(key);
		btn.setOnClickListener(imageSelectorListener);
		return btn;
	}
	
	private final View createBoolSelector(String key){
		boolean val = sdb.getBoolean(key,(boolean) sMap.getDefaults(key));
		if(Build.VERSION.SDK_INT >= 21 && Build.VERSION.SDK_INT < 30) {
			TextView swtch = LayoutCreator.createFilledYSwitch(LinearLayout.class,this,getTranslation(key),val,switchListener);
			swtch.setMinHeight((int) getListPreferredItemHeight());
			swtch.setTag(key);
			return swtch;
		} else if(Build.VERSION.SDK_INT >= 14) {
			Switch swtch = new Switch(this);
			swtch.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
			swtch.setChecked(val);
			swtch.setText(getTranslation(key));
			swtch.setOnCheckedChangeListener(switchListenerAPI19);
			swtch.setTextOn("");
			swtch.setTextOff("");
			if(Build.VERSION.SDK_INT < 30) {
				int minW = DensityUtils.dpInt(64);
				if(Build.VERSION.SDK_INT >= 16)
					swtch.setSwitchMinWidth(minW);
				else {
					try {
						Field minWidth = Switch.class.getDeclaredField("mSwitchMinWidth");
						minWidth.setAccessible(true);
						minWidth.set(swtch, minW);
					} catch(Throwable t) {}
				}
			}
			
			swtch.setMinHeight((int) getListPreferredItemHeight());
			swtch.setTag(key);
			return swtch;
		}
		
		LinearLayout ll = LayoutCreator.createHorizontalLayout(this);
		ll.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
		TextView tv = new TextView(this);
		tv.setLayoutParams(new LinearLayout.LayoutParams(-1, -2, 1));
		tv.setText(getTranslation(key));
		ll.addView(tv);
		ToggleButton tb = new ToggleButton(this);
		tb.setLayoutParams(new LinearLayout.LayoutParams(-2, -2, 0));
		tb.setChecked(val);
		tb.setOnCheckedChangeListener(switchListenerAPI19);
		ll.addView(tb);
		return ll;
	}
	
	private final View createRadioSelector(String key, List<String> items) throws Throwable {
		View base = createImageSelector(key);
		base.setTag(TAG1,key);
		base.setTag(TAG2,items);
		base.setOnClickListener(radioSelectorListener);
		return base;
	}
	
	private final View.OnClickListener colorSelectorListener = new View.OnClickListener(){

		@Override
		public void onClick(final View p1){
			AlertDialog.Builder build = new AlertDialog.Builder(p1.getContext());
			final String tag = p1.getTag().toString();
			build.setTitle(getTranslation(tag));
			final int val = getIntOrDefault(tag);
			dialogView = ColorSelectorLayout.getColorSelectorLayout(AppSettingsV2.this,p1.getTag().toString());
			build.setView(dialogView);
			build.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface p1, int p2){
						p1.dismiss();
					}

				});
			build.setNeutralButton(R.string.settings_return_defaults, new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface d1, int p2){
						int tagVal = (int) sMap.getDefaults(tag);
						sdb.putInteger(tag,tagVal);
						sdb.onlyWrite();
						ImageView img = p1.findViewById(android.R.id.icon);
						GradientDrawable gd = new GradientDrawable();
						gd.setColor(tagVal);
						gd.setCornerRadius(1000);
						img.setImageDrawable(gd);
						restartKeyboard();
						d1.dismiss();
					}
				
				});
			build.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface d1, int p2){
						int tagVal = (int) dialogView.findViewById(android.R.id.tabs).getTag();
						if(tagVal != val){
							sdb.putInteger(tag,tagVal);
							sdb.onlyWrite();
							ImageView img = p1.findViewById(android.R.id.icon);
							GradientDrawable gd = new GradientDrawable();
							gd.setColor(tagVal);
							gd.setCornerRadius(1000);
							img.setImageDrawable(gd);
							restartKeyboard();
						}
						d1.dismiss();
					}

				});
			build.show();
		}

	};
	
	private final View.OnClickListener numberSelectorListener = new View.OnClickListener(){

		@Override
		public void onClick(final View p1){
			AlertDialog.Builder build = new AlertDialog.Builder(p1.getContext());
			final String tag = p1.getTag().toString();
			build.setTitle(getTranslation(tag));
			AppSettingsV2 act = (AppSettingsV2) p1.getContext();
			final boolean isFloat = sMap.get(tag) == SettingType.FLOAT_NUMBER;
			int[] minMax = sMap.getMinMaxNumbers(tag);
			final int val = getIntOrDefault(tag);
			dialogView = NumberSelectorLayout.getNumberSelectorLayout(act,isFloat,minMax[0],minMax[1],val);
			build.setView(dialogView);
			build.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface p1, int p2){
						p1.dismiss();
					}

				});
			build.setNeutralButton(R.string.settings_return_defaults, new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface d1, int p2){
						int tagVal = (int) sMap.getDefaults(tag);
						sdb.putInteger(tag,tagVal);
						sdb.onlyWrite();
						TextView tv = p1.findViewById(android.R.id.text1);
						tv.setText(isFloat ? getFloatNumberFromInt(tagVal) + "" : tagVal + "");
						restartKeyboard();
						// TODO: add dynamic change for size multiplier
						if(SettingMap.SET_KEY_ICON_SIZE_MULTIPLIER.equals(tag))
							recreate();
						d1.dismiss();
					}

				});
			build.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface d1, int p2){
						int tagVal = (int) dialogView.getTag();
						if(tagVal != val){
							sdb.putInteger(tag,tagVal);
							sdb.refreshKey(tag);
							TextView tv = p1.findViewById(android.R.id.text1);
							tv.setText(isFloat ? getFloatNumberFromInt(tagVal) + "" : tagVal + "");
							restartKeyboard();
							// TODO: add dynamic change for size multiplier
							if(SettingMap.SET_KEY_ICON_SIZE_MULTIPLIER.equals(tag))
								recreate();
						}
						d1.dismiss();
					}

				});
			build.show();
		}

	};
	
	private final View.OnClickListener imageSelectorListener = new View.OnClickListener(){

		@Override
		public void onClick(View p1){
			AlertDialog.Builder build = new AlertDialog.Builder(p1.getContext());
			build.setTitle(getTranslation(p1.getTag().toString()));
			build.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface p1, int p2){
						p1.dismiss();
					}
				
				});
			build.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface p1, int p2){
						ImageView img = dialogView.findViewById(android.R.id.custom);
						Drawable d = img.getDrawable();
						if(d != null){
							try {
								File bgFile = getBackgroundImageFile();
								Bitmap bmp = ((BitmapDrawable) d).getBitmap();
								setColorsFromBitmap(bmp);
								FileOutputStream fos = new FileOutputStream(bgFile);
								bmp.compress(Bitmap.CompressFormat.PNG,100,fos);
							} catch(Throwable e){}
							restartKeyboard();
							recreate();
						}
						p1.dismiss();
					}

				});
			AlertDialog dialog = build.create();
			dialogView = ImageSelectorLayout.getImageSelectorLayout(dialog,AppSettingsV2.this,p1.getTag().toString());
			dialog.setView(dialogView);
			dialog.show();
		}
		
	};
	
	private final YSwitch.OnCheckedChangeListener switchListener = new YSwitch.OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(YCompoundButton buttonView, boolean isChecked){
			String str = (String) buttonView.getTag();
			sdb.putBoolean(str,isChecked);
			sdb.onlyWrite();
			restartKeyboard();
		}
		
	};

	private final CompoundButton.OnCheckedChangeListener switchListenerAPI19 = new CompoundButton.OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
			String str = (String) buttonView.getTag();
			sdb.putBoolean(str,isChecked);
			sdb.writeKey(str);
			restartKeyboard();
		}
		
	};
	
	private final View.OnClickListener radioSelectorListener = new View.OnClickListener(){

		@Override
		public void onClick(final View p1){
			AlertDialog.Builder build = new AlertDialog.Builder(p1.getContext());
			final String tag = p1.getTag(TAG1).toString();
			int val;
			final boolean langSelector = sMap.get(tag) == SettingType.LANG_SELECTOR;
			final boolean iconSelector = sMap.get(tag) == SettingType.ICON_SELECTOR;
			final boolean themeSelector = sMap.get(tag) == SettingType.THEME_SELECTOR;
			if(langSelector){
				String value = SuperDBHelper.getValueOrDefault(tag);
				val = LayoutUtils.getKeyListFromLanguageList().indexOf(value);
			} else if(iconSelector){
				String value = SuperDBHelper.getValueOrDefault(tag);
				val = SuperBoardApplication.getIconThemes().indexOf(value);
			} else if (themeSelector){
				val = -1;
			} else {
				val = getIntOrDefault(tag);
			}
			build.setTitle(getTranslation(tag));
			ScrollView dialogScroller = new ScrollView(p1.getContext());
			dialogView = RadioSelectorLayout.getRadioSelectorLayout(AppSettingsV2.this,val,(List<String>)p1.getTag(TAG2));
			dialogScroller.addView(dialogView);
			build.setView(dialogScroller);
			build.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface p1, int p2){
						p1.dismiss();
					}

				});
			if(!themeSelector)
				build.setNeutralButton(R.string.settings_return_defaults, new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface p1, int p2){
							if(langSelector || iconSelector || themeSelector) sdb.putString(tag,(String)sMap.getDefaults(tag));
							else sdb.putInteger(tag,(int)sMap.getDefaults(tag));
							sdb.onlyWrite();
							restartKeyboard();
							p1.dismiss();
						}

					});
			final int xval = val;
			build.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface p1, int p2){
						int tagVal = (int) dialogView.getTag();
						if(tagVal != xval){
							if(langSelector){
								String index = LayoutUtils.getKeyListFromLanguageList().get(tagVal);
								sdb.putString(tag,index);
							} else if(iconSelector){
								String index = SuperBoardApplication.getIconThemes().getFromIndex(tagVal);
								sdb.putString(tag,index);
							} else if (themeSelector) {
								List<ThemeHolder> themes = SuperBoardApplication.getThemes();
								ThemeHolder theme = themes.get(tagVal);
								theme.applyTheme();
								recreate();
							} else sdb.putInteger(tag,tagVal);
							sdb.onlyWrite();
							restartKeyboard();
						}
						p1.dismiss();
					}

				});
			build.show();
		}
		
	};
	
	private void setColorsFromBitmap(Bitmap b){
		if(b == null) return;
		int c = ColorUtils.getBitmapColor(b);
		sdb.putInteger(SettingMap.SET_KEYBOARD_BGCLR,c-0xAA000000);
		int keyClr = c-0xAA000000;
		int keyPressClr = getDarkerColor(keyClr);
		int keyPress2Clr = getDarkerColor(keyPressClr);
		sdb.putInteger(SettingMap.SET_KEY_BGCLR,keyClr);
		sdb.putInteger(SettingMap.SET_KEY2_BGCLR,keyPressClr);
		sdb.putInteger(SettingMap.SET_KEY_PRESS_BGCLR,keyPressClr);
		sdb.putInteger(SettingMap.SET_KEY2_PRESS_BGCLR,keyPress2Clr);
		boolean isLight = ColorUtils.satisfiesTextContrast(c);
		sdb.putInteger(SettingMap.SET_ENTER_BGCLR,isLight ? keyPressClr : 0xFFFFFFFF);
		keyClr = isLight ? 0xFF212121 : 0xFFDEDEDE;
		sdb.putInteger(SettingMap.SET_KEY_TEXTCLR,keyClr);
		sdb.putInteger(SettingMap.SET_KEY_SHADOWCLR,keyClr ^ 0x00FFFFFF);
		sdb.onlyWrite();
	}

	public static int getDarkerColor(int color) {
		int[] state = {Color.red(color),Color.green(color),Color.blue(color)};
		for(int i = 0;i < state.length;i++){
			state[i] /= 1.2;
		}
		return Color.argb(Color.alpha(color),state[0],state[1],state[2]);
	}
	
	private List<String> getArrayAsList(String key) throws Throwable {
		int id = getResources().getIdentifier("settings_" + key, "array", getPackageName());
		if(id > 0){
			String[] arr = getResources().getStringArray(id);
			List<String> out = new ArrayList<String>();
			for(String str : arr){
				out.add(str);
			}
			return out;
		}
		return sMap.getSelector(key);
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
		sb.setKeyTintColor(0,0,1,getIntOrDefault(SettingMap.SET_KEY2_BGCLR),getIntOrDefault(SettingMap.SET_KEY2_PRESS_BGCLR));
		sb.setKeyTintColor(0,0,2,getIntOrDefault(SettingMap.SET_ENTER_BGCLR),getIntOrDefault(SettingMap.SET_ENTER_PRESS_BGCLR));
		sb.setBackgroundColor(getIntOrDefault(SettingMap.SET_KEYBOARD_BGCLR));
		sb.setKeysTextColor(getIntOrDefault(SettingMap.SET_KEY_TEXTCLR));
		sb.setIconSizeMultiplier(getIntOrDefault(SettingMap.SET_KEY_ICON_SIZE_MULTIPLIER));
		sb.setKeysTextSize(getFloatPercentOrDefault(SettingMap.SET_KEY_TEXTSIZE));
		sb.setKeysTextType(getIntOrDefault(SettingMap.SET_KEYBOARD_TEXTTYPE_SELECT));
		IconThemeUtils icons = SuperBoardApplication.getIconThemes();
		sb.setKeyDrawable(0,0,1,icons.getIconResource(IconThemeUtils.SYM_TYPE_DELETE));
		sb.setKeyDrawable(0,0,-1,icons.getIconResource(IconThemeUtils.SYM_TYPE_ENTER));
		try {
			SuperBoardApplication.clearCustomFont();
			sb.setCustomFont(SuperBoardApplication.getCustomFont());
		} catch(Throwable t){}
	}
	
	private int getFloatPercentOrDefault(String key){
		return sb.mp(getFloatNumberFromInt(getIntOrDefault(key)));
	}
	
	private int getIntOrDefault(String key){
		return sdb.getInteger(key,(int)sMap.getDefaults(key));
	}
	
	private final float getListPreferredItemHeight(){
		TypedValue value = new TypedValue();
		getTheme().resolveAttribute(android.R.attr.listPreferredItemHeight, value, true);	
		return TypedValue.complexToDimension(value.data, getResources().getDisplayMetrics());
	}
	
	public String getTranslation(String key){
		String requestedKey = "settings_" + key;
		try {
			return getString(getResources().getIdentifier(requestedKey, "string", getPackageName()));
		} catch(Throwable t){}
		return requestedKey;
	}
	
	public static float getFloatNumberFromInt(int i){
		return i / 10.0f;
	}
	
	public static int getIntNumberFromFloat(float i){
		return (int)(i * 10);
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
	
	private static class ImageTask extends AsyncTask<Object,Bitmap,Bitmap> {

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
				ImageView img = dialogView.findViewById(android.R.id.custom);
				img.setImageBitmap(result);
			}
		}

	}
	
	public static enum SettingType {
		BOOL,
		THEME_SELECTOR,
		COLOR_SELECTOR,
		LANG_SELECTOR,
		ICON_SELECTOR,
		SELECTOR,
		DECIMAL_NUMBER,
		FLOAT_NUMBER,
		MM_DECIMAL_NUMBER,
		IMAGE,
	}
	
}
