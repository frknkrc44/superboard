package org.blinksd.board;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.*;
import android.os.*;
import android.provider.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.*;
import org.blinksd.*;
import org.blinksd.utils.image.*;
import org.blinksd.utils.layout.*;
import org.superdroid.db.*;
import yandroid.widget.*;

public class AppSettingsV2 extends Activity {
	
	LinearLayout main;
	SuperDB sdb;
	static View dialogView;
	SettingMap sMap;

	@Override
	protected void onCreate(Bundle b){
		super.onCreate(b);
		main = LayoutCreator.createFilledVerticalLayout(FrameLayout.class,this);
		int dp = DensityUtils.dpInt(16);
		main.setPadding(dp,dp,dp,dp);
		setContentView(main);
		sdb = SuperBoardApplication.getApplicationDatabase();
		sMap = new SettingMap();
		try {
			createMainView();
		} catch(Throwable e){
			// do nothing
		}
	}
	
	private void createMainView() throws Throwable {
		ArrayList<String> keys = new ArrayList<String>(sMap.keySet());
		for(String key : keys){
			SettingType z = sMap.get(key);
			switch(z){
				case BOOL:
					main.addView(createBoolSelector(key));
					break;
				case IMAGE:
					main.addView(createImageSelector(key));
					break;
				case COLOR_SELECTOR:
					main.addView(createColorSelector(key));
					break;
				case SELECTOR:
					main.addView(createRadioSelector(key));
					break;
				case DECIMAL_NUMBER:
				case MM_DECIMAL_NUMBER:
				case FLOAT_NUMBER:
					// main.addView(createNumberSelector(key));
					break;
			}
		}
	}
	
	private final View createColorSelector(String key){
		int color = sdb.getInteger(key,0xFF000000);
		LinearLayout colSelector = LayoutCreator.createFilledHorizontalLayout(LinearLayout.class,this);
		colSelector.getLayoutParams().height = -2;
		ImageView img = LayoutCreator.createImageView(this);
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
	
	private final YSwitch createBoolSelector(String key){
		boolean val = sdb.getBoolean(key,false);
		YSwitch swtch = LayoutCreator.createFilledYSwitch(LinearLayout.class,this,getTranslation(key),val,switchListener);
		swtch.setMinHeight((int) getListPreferredItemHeight());
		swtch.setTag(key);
		return swtch;
	}
	
	private final View createRadioSelector(String key) throws Throwable {
		View base = createImageSelector(key);
		base.setOnClickListener(radioSelectorListener);
		return base;
	}
	
	private final View.OnClickListener colorSelectorListener = new View.OnClickListener(){

		@Override
		public void onClick(View p1){
			AlertDialog.Builder build = new AlertDialog.Builder(p1.getContext());
			build.setTitle(getTranslation(p1.getTag().toString()));
			build.setView(ColorSelectorLayout.getColorSelectorLayout(AppSettingsV2.this,p1.getTag().toString()));
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
								Bitmap bmp = ((BitmapDrawable) d).getBitmap();
								FileOutputStream fos = new FileOutputStream(getBackgroundImageFile());
								bmp.compress(Bitmap.CompressFormat.PNG,100,fos);
							} catch(Throwable e){}
							restartKeyboard();
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
		}
		
	};
	
	private final View.OnClickListener radioSelectorListener = new View.OnClickListener(){

		@Override
		public void onClick(View p1){
			
		}
		
	};
	
	public final float getListPreferredItemHeight(){
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
	
	public static float a(int i){
		return i / 10.0f;
	}

	public static void restartKeyboard(){
		SuperBoardApplication.getApplication().sendBroadcast(new Intent(InputService.COLORIZE_KEYBOARD));
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
	
	private class SettingMap extends TreeMap<String,SettingType> {

		public SettingMap(){
			put("keyboard_lang_select",SettingType.SELECTOR);
			put("keyboard_texttype_select",SettingType.SELECTOR);
			put("keyboard_bgimg",SettingType.IMAGE);
			put("keyboard_bgblur",SettingType.DECIMAL_NUMBER);
			put("keyboard_height",SettingType.MM_DECIMAL_NUMBER);
			put("keyboard_bgclr",SettingType.COLOR_SELECTOR);
			put("keyboard_show_popup",SettingType.BOOL);
			put("keyboard_lc_on_emoji",SettingType.BOOL);
			put("play_snd_press",SettingType.BOOL);
			put("key_bgclr",SettingType.COLOR_SELECTOR);
			put("key2_bgclr",SettingType.COLOR_SELECTOR);
			put("enter_bgclr",SettingType.COLOR_SELECTOR);
			put("key_shadowclr",SettingType.COLOR_SELECTOR);
			put("key_padding",SettingType.FLOAT_NUMBER);
			put("key_radius",SettingType.FLOAT_NUMBER);
			put("key_textsize",SettingType.FLOAT_NUMBER);
			put("key_shadowsize",SettingType.FLOAT_NUMBER);
			put("key_vibrate_duration",SettingType.DECIMAL_NUMBER);
			put("key_longpress_duration",SettingType.MM_DECIMAL_NUMBER);
		}
		
		public ArrayList<String> getSelector(final String key) throws Throwable {
			switch(key){
				case "keyboard_lang_select":
					return new ArrayList<String>(LayoutUtils.getLanguageList(SuperBoardApplication.getApplication()).keySet());
				case "keyboard_texttype_select":
					ArrayList<String> textTypes = new ArrayList<String>();
					for(SuperBoard.TextType type : SuperBoard.TextType.values())
						textTypes.add(type.name());
					return textTypes;
			}
			return new ArrayList<String>();
		}
		
		public int[] getMinMaxNumbers(final String key){
			int[] nums = new int[2];
			if(containsKey(key)){
				switch(get(key)){
					case DECIMAL_NUMBER:
						nums[0] = 0;
						switch(key){
							case "keyboard_bgblur":
								nums[1] = 40;
								break;
							case "key_vibrate_duration":
								nums[1] = 100;
								break;
						}
						break;
					case MM_DECIMAL_NUMBER:
						switch(key){
							case "keyboard_height":
								nums[0] = 20;
								nums[1] = 80;
								break;
							case "key_longpress_duration":
								nums[0] = 1;
								nums[1] = 3;
								break;
						}
						break;
					case FLOAT_NUMBER:
						nums[0] = 0;
						switch(key){
							case "key_padding":
							case "key_shadowsize":
								nums[1] = 40;
								break;
							case "key_radius":
								nums[1] = 100;
								break;
							case "key_textsize":
								nums[0] = 6;
								nums[1] = 60;
								break;
						}
						break;
				}
			}
			return nums;
		}

	}
	
	public static enum SettingType {
		BOOL,
		COLOR_SELECTOR,
		SELECTOR,
		DECIMAL_NUMBER,
		FLOAT_NUMBER,
		MM_DECIMAL_NUMBER,
		IMAGE,
	}
	
}