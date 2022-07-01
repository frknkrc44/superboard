package org.blinksd.utils.layout;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import org.blinksd.*;
import org.blinksd.board.*;
import org.blinksd.board.AppSettingsV2.*;
import org.blinksd.sdb.*;
import org.blinksd.utils.color.*;
import org.blinksd.utils.color.ThemeUtils.*;
import org.superdroid.db.*;
import yandroid.widget.*;
import yandroid.util.*;

public class SettingsCategorizedListAdapter extends BaseExpandableListAdapter{
	
	public SettingsCategorizedListAdapter(AppSettingsV2 context){
		mContext = context;
	}
	
	private final AppSettingsV2 mContext;
	public static View dialogView;
	private static final int TAG1 = R.string.app_name, TAG2 = R.string.hello_world;
	
	private static SuperMiniDB getAppDB(){
		return SuperBoardApplication.getApplicationDatabase();
	}
	
	private static SettingMap getSettings(){
		return SuperBoardApplication.getSettings();
	}
	
	@Override
	public int getGroupCount(){
		return SettingCategory.values().length;
	}

	@Override
	public int getChildrenCount(int p1){
		return getSettings().getChildrenCount((SettingCategory) getGroup(p1));
	}

	@Override
	public Object getGroup(int p1){
		return SettingCategory.values()[p1];
	}

	@Override
	public Object getChild(int p1, int p2){
		return getSettings().getChildKey((SettingCategory) getGroup(p1), p2);
	}

	@Override
	public long getGroupId(int p1){
		return p1 << 8;
	}

	@Override
	public long getChildId(int p1, int p2){
		return getGroupId(p1) | p2;
	}

	@Override
	public boolean hasStableIds(){
		return false;
	}

	@Override
	public View getGroupView(int p1, boolean p2, View p3, ViewGroup p4){
		if(SuperDBHelper.getBooleanValueOrDefault(SettingMap.SET_USE_MONET) && getGroup(p1) == SettingCategory.THEMING_ADVANCED) {
			// Disable advanced theming for monet mode
			return new Space(p4.getContext());
		}
		TextView view = (TextView) LayoutInflater.from(p4.getContext()).inflate(android.R.layout.simple_expandable_list_item_1, p4, false);
		view.setText(mContext.getResources().getStringArray(R.array.settings_categories)[p1]);
		return view;
	}

	@Override
	public View getChildView(int p1, int p2, boolean p3, View p4, ViewGroup p5){
		String key = (String) getChild(p1, p2);
		SettingType z = SuperBoardApplication.getSettings().get(key).type;
		switch(z){
			case REDIRECT:
				return createRedirect(key);
			case BOOL:
				return createBoolSelector(key);
			case IMAGE:
				return createImageSelector(key);
			case THEME_SELECTOR:
				List<String> themeKeys = ThemeUtils.getThemeNames(SuperBoardApplication.getThemes());
				return createRadioSelector(key,themeKeys);
			case COLOR_SELECTOR:
				return createColorSelector(key);
			case STR_SELECTOR:
			case SELECTOR:
				if(SettingMap.SET_KEYBOARD_LANG_SELECT.equals(key)){
					List<String> keySet = SuperBoardApplication.getLanguageHRNames();
					return createRadioSelector(key,keySet);
				}
				
				List<String> selectorKeys = getArrayAsList(key);
				return createRadioSelector(key,selectorKeys);
			case DECIMAL_NUMBER:
			case MM_DECIMAL_NUMBER:
			case FLOAT_NUMBER:
				return createNumberSelector(key,z == SettingType.FLOAT_NUMBER);
		}
		
		return null;
	}

	@Override
	public boolean isChildSelectable(int p1, int p2){
		return false;
	}

	private final View createNumberSelector(String key, boolean isFloat){
		int num = mContext.getIntOrDefault(key);
		LinearLayout numSelector = LayoutCreator.createFilledHorizontalLayout(LinearLayout.class,mContext);
		numSelector.getLayoutParams().height = -2;
		TextView img = LayoutCreator.createTextView(mContext);
		img.setId(android.R.id.text1);
		int height = (int) getListPreferredItemHeight();
		img.setGravity(Gravity.CENTER);
		img.setTextColor(0xFFFFFFFF);
		img.setText(isFloat ? DensityUtils.getFloatNumberFromInt(num)+"" : num+"");
		img.setLayoutParams(LayoutCreator.createLayoutParams(LinearLayout.class,height,height));
		int pad = height / 4;
		img.setPadding(pad,pad,pad,pad);
		TextView btn = LayoutCreator.createTextView(mContext);
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
		int color = mContext.getIntOrDefault(key);
		LinearLayout colSelector = LayoutCreator.createFilledHorizontalLayout(LinearLayout.class,mContext);
		colSelector.getLayoutParams().height = -2;
		ImageView img = LayoutCreator.createImageView(mContext);
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
		TextView btn = LayoutCreator.createTextView(mContext);
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
		TextView btn = LayoutCreator.createTextView(mContext);
		btn.setGravity(Gravity.CENTER_VERTICAL);
		btn.setTextColor(0xFFFFFFFF);
		btn.setMinHeight((int) getListPreferredItemHeight());
		btn.setText(getTranslation(key));
		btn.setTag(key);
		btn.setOnClickListener(imageSelectorListener);
		int pad = (int)(getListPreferredItemHeight() / 4);
		btn.setPadding(pad,0,pad,0);
		return btn;
	}

	private final View createBoolSelector(String key){
		int pad = (int)(getListPreferredItemHeight() / 4);
		boolean val = getAppDB().getBoolean(key,(boolean) getSettings().getDefaults(key));
		if(Build.VERSION.SDK_INT >= 21) {
			YSwitch swtch = LayoutCreator.createFilledYSwitch(LinearLayout.class,mContext,getTranslation(key),val,switchListener);
			swtch.setMinHeight((int) getListPreferredItemHeight());
			swtch.setTag(key);
			swtch.setPadding(pad,0,pad,0);
			return swtch;
		} else if(Build.VERSION.SDK_INT >= 14) {
			Switch swtch = new Switch(mContext);
			swtch.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
			swtch.setChecked(val);
			swtch.setText(getTranslation(key));
			swtch.setOnCheckedChangeListener(switchListenerAPI19);
			swtch.setTextOn("");
			swtch.setTextOff("");
			swtch.setPadding(pad,0,pad,0);
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

		LinearLayout ll = LayoutCreator.createHorizontalLayout(mContext);
		ll.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
		TextView tv = new TextView(mContext);
		tv.setLayoutParams(new LinearLayout.LayoutParams(-1, -2, 1));
		tv.setText(getTranslation(key));
		ll.addView(tv);
		ToggleButton tb = new ToggleButton(mContext);
		tb.setLayoutParams(new LinearLayout.LayoutParams(-2, -2, 0));
		tb.setChecked(val);
		tb.setPadding(pad,0,pad,0);
		tb.setOnCheckedChangeListener(switchListenerAPI19);
		ll.addView(tb);
		return ll;
	}

	private final View createRadioSelector(String key, List<String> items) {
		try {
			View base = createImageSelector(key);
			base.setTag(TAG1,key);
			base.setTag(TAG2,items);
			base.setOnClickListener(radioSelectorListener);
			return base;
		} catch(Throwable t){
			throw new RuntimeException(t);
		}
	}
	
	private final View createRedirect(String key){
		try {
			View base = createImageSelector(key);
			base.setOnClickListener(redirectListener);
			return base;
		} catch(Throwable t){
			throw new RuntimeException(t);
		}
	}
	
	private final View.OnClickListener redirectListener = new View.OnClickListener(){
		@Override
		public void onClick(View p1){
			Intent intent = getSettings().getRedirect(p1.getContext(), (String) p1.getTag());
			p1.getContext().startActivity(intent);
		}
	};

	private final View.OnClickListener colorSelectorListener = new View.OnClickListener(){

		@Override
		public void onClick(final View p1){
			AlertDialog.Builder build = new AlertDialog.Builder(p1.getContext());
			final String tag = p1.getTag().toString();
			build.setTitle(getTranslation(tag));
			final int val = mContext.getIntOrDefault(tag);
			dialogView = ColorSelectorLayout.getColorSelectorLayout(mContext ,p1.getTag().toString());
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
						int tagVal = (int) getSettings().getDefaults(tag);
						getAppDB().putInteger(tag,tagVal);
						getAppDB().onlyWrite();
						ImageView img = p1.findViewById(android.R.id.icon);
						GradientDrawable gd = new GradientDrawable();
						gd.setColor(tagVal);
						gd.setCornerRadius(1000);
						img.setImageDrawable(gd);
						mContext.restartKeyboard();
						d1.dismiss();
					}

				});
			build.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface d1, int p2){
						int tagVal = (int) dialogView.findViewById(android.R.id.tabs).getTag();
						if(tagVal != val){
							getAppDB().putInteger(tag,tagVal);
							getAppDB().onlyWrite();
							ImageView img = p1.findViewById(android.R.id.icon);
							GradientDrawable gd = new GradientDrawable();
							gd.setColor(tagVal);
							gd.setCornerRadius(1000);
							img.setImageDrawable(gd);
							mContext.restartKeyboard();
						}
						d1.dismiss();
					}

				});

			doHacksAndShow(build);
		}

	};

	private final View.OnClickListener numberSelectorListener = new View.OnClickListener(){

		@Override
		public void onClick(final View p1){
			AlertDialog.Builder build = new AlertDialog.Builder(p1.getContext());
			final String tag = p1.getTag().toString();
			build.setTitle(getTranslation(tag));
			AppSettingsV2 act = (AppSettingsV2) p1.getContext();
			final boolean isFloat = getSettings().get(tag).type == SettingType.FLOAT_NUMBER;
			int[] minMax = getSettings().getMinMaxNumbers(tag);
			final int val = mContext.getIntOrDefault(tag);
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
						int tagVal = (int) getSettings().getDefaults(tag);
						getAppDB().putInteger(tag,tagVal);
						getAppDB().onlyWrite();
						TextView tv = p1.findViewById(android.R.id.text1);
						tv.setText(isFloat ? DensityUtils.getFloatNumberFromInt(tagVal) + "" : tagVal + "");
						mContext.restartKeyboard();
						// TODO: add dynamic change for size multiplier
						if(SettingMap.SET_KEY_ICON_SIZE_MULTIPLIER.equals(tag))
							mContext.recreate();
						d1.dismiss();
					}

				});
			build.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface d1, int p2){
						int tagVal = (int) dialogView.getTag();
						if(tagVal != val){
							getAppDB().putInteger(tag,tagVal);
							getAppDB().refreshKey(tag);
							TextView tv = p1.findViewById(android.R.id.text1);
							tv.setText(isFloat ? DensityUtils.getFloatNumberFromInt(tagVal) + "" : tagVal + "");
							mContext.restartKeyboard();
							// TODO: add dynamic change for size multiplier
							if(SettingMap.SET_KEY_ICON_SIZE_MULTIPLIER.equals(tag))
								mContext.recreate();
						}
						d1.dismiss();
					}

				});
			
			doHacksAndShow(build);
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
								File bgFile = mContext.getBackgroundImageFile();
								Bitmap bmp = ((BitmapDrawable) d).getBitmap();
								setColorsFromBitmap(bmp);
								FileOutputStream fos = new FileOutputStream(bgFile);
								bmp.compress(Bitmap.CompressFormat.PNG,100,fos);
							} catch(Throwable e){}
							mContext.restartKeyboard();
							mContext.recreate();
						}
						p1.dismiss();
					}

				});
			AlertDialog dialog = build.create();
			dialogView = ImageSelectorLayout.getImageSelectorLayout(dialog,mContext,p1.getTag().toString());
			dialog.setView(dialogView);
			
			doHacksAndShow(build);
		}

	};

	private final YSwitch.OnCheckedChangeListener switchListener = new YSwitch.OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(YCompoundButton buttonView, boolean isChecked){
			String str = (String) buttonView.getTag();
			getAppDB().putBoolean(str,isChecked);
			getAppDB().onlyWrite();
			mContext.restartKeyboard();
		}

	};

	private final CompoundButton.OnCheckedChangeListener switchListenerAPI19 = new CompoundButton.OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
			String str = (String) buttonView.getTag();
			getAppDB().putBoolean(str,isChecked);
			getAppDB().writeKey(str);
			mContext.restartKeyboard();
		}

	};

	private final View.OnClickListener radioSelectorListener = new View.OnClickListener(){

		@Override
		public void onClick(final View p1){
			AlertDialog.Builder build = new AlertDialog.Builder(p1.getContext());
			final String tag = p1.getTag(TAG1).toString();
			int val;
			final SettingItem item = getSettings().get(tag);
			final boolean langSelector = item.type == SettingType.STR_SELECTOR && SettingMap.SET_KEYBOARD_LANG_SELECT.equals(tag);
			final boolean iconSelector = item.type == SettingType.STR_SELECTOR && SettingMap.SET_ICON_THEME.equals(tag);
			final boolean spaceSelector = item.type == SettingType.STR_SELECTOR && SettingMap.SET_KEYBOARD_SPACETYPE_SELECT.equals(tag);
			final boolean themeSelector = item.type == SettingType.THEME_SELECTOR;
			if(langSelector || iconSelector || spaceSelector){
				String value = SuperDBHelper.getValueOrDefault(tag);
				if(langSelector)
					val = LayoutUtils.getKeyListFromLanguageList().indexOf(value);
				else if(iconSelector)
					val = SuperBoardApplication.getIconThemes().indexOf(value);
				else
					val = SuperBoardApplication.getSpaceBarStyles().indexOf(value);
			} else if(themeSelector) {
				val = -1;
			} else {
				val = mContext.getIntOrDefault(tag);
			}
			build.setTitle(getTranslation(tag));
			ScrollView dialogScroller = new ScrollView(p1.getContext());
			dialogView = RadioSelectorLayout.getRadioSelectorLayout(mContext,val,(List<String>)p1.getTag(TAG2));
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
							if(langSelector || iconSelector || spaceSelector) getAppDB().putString(tag,(String)getSettings().getDefaults(tag));
							else getAppDB().putInteger(tag,(int)getSettings().getDefaults(tag));
							getAppDB().onlyWrite();
							mContext.restartKeyboard();
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
								getAppDB().putString(tag,index);
							} else if(iconSelector){
								String index = SuperBoardApplication.getIconThemes().getFromIndex(tagVal);
								getAppDB().putString(tag,index);
							} else if(spaceSelector) {
								String index = SuperBoardApplication.getSpaceBarStyles().getFromIndex(tagVal);
								getAppDB().putString(tag,index);
							} else if (themeSelector) {
								List<ThemeHolder> themes = SuperBoardApplication.getThemes();
								ThemeHolder theme = themes.get(tagVal);
								theme.applyTheme();
								mContext.recreate();
							} else getAppDB().putInteger(tag,tagVal);
							getAppDB().onlyWrite();
							mContext.restartKeyboard();
						}
						p1.dismiss();
					}

				});

			doHacksAndShow(build);
		}

	};
	
	private final float getListPreferredItemHeight(){
		TypedValue value = new TypedValue();
		mContext.getTheme().resolveAttribute(android.R.attr.listPreferredItemHeight, value, true);	
		return TypedValue.complexToDimension(value.data, mContext.getResources().getDisplayMetrics());
	}
	
	private String getTranslation(String key){
		return getTranslation(mContext, key);
	}
	
	public static String getTranslation(Context ctx, String key){
		String requestedKey = "settings_" + key;
		try {
			return ctx.getString(ctx.getResources().getIdentifier(requestedKey, "string", ctx.getPackageName()));
		} catch(Throwable t){}
		return requestedKey;
	}
	
	private void setColorsFromBitmap(Bitmap b){
		if(b == null) return;
		int c = ColorUtils.getBitmapColor(b);
		getAppDB().putInteger(SettingMap.SET_KEYBOARD_BGCLR,c-0xAA000000);
		int keyClr = c-0xAA000000;
		int keyPressClr = ColorUtils.getDarkerColor(keyClr);
		int keyPress2Clr = ColorUtils.getDarkerColor(keyPressClr);
		getAppDB().putInteger(SettingMap.SET_KEY_BGCLR,keyClr);
		getAppDB().putInteger(SettingMap.SET_KEY2_BGCLR,keyPressClr);
		getAppDB().putInteger(SettingMap.SET_KEY_PRESS_BGCLR,keyPressClr);
		getAppDB().putInteger(SettingMap.SET_KEY2_PRESS_BGCLR,keyPress2Clr);
		boolean isLight = ColorUtils.satisfiesTextContrast(c);
		getAppDB().putInteger(SettingMap.SET_ENTER_BGCLR,isLight ? keyPressClr : 0xFFFFFFFF);
		keyClr = isLight ? 0xFF212121 : 0xFFDEDEDE;
		getAppDB().putInteger(SettingMap.SET_KEY_TEXTCLR,keyClr);
		getAppDB().putInteger(SettingMap.SET_KEY_SHADOWCLR,keyClr ^ 0x00FFFFFF);
		getAppDB().onlyWrite();
	}
	
	private List<String> getArrayAsList(String key) {
		try {
			int id = mContext.getResources().getIdentifier("settings_" + key, "array", mContext.getPackageName());
			if(id > 0){
				String[] arr = mContext.getResources().getStringArray(id);
				List<String> out = new ArrayList<String>();
				for(String str : arr){
					out.add(str);
				}
				return out;
			}
			return getSettings().getSelector(key);
		} catch(Throwable t){
			return new ArrayList<String>();
		}
	}
	
	public void doHacksAndShow(AlertDialog.Builder builder){
		AlertDialog dialog = builder.create();
		
		if(Build.VERSION.SDK_INT >= 31) {
			Drawable dw = dialog.getWindow().getDecorView().getBackground(); 
			int color = mContext.getResources().getColor(android.R.color.system_neutral1_900);
			dw.setTint(color);
			/*
			GradientDrawable gd = new GradientDrawable();
			gd.setColor(color);
			gd.setCornerRadius(mContext.getResources().getDisplayMetrics().density * 16);
			dialog.getWindow().setBackgroundDrawable(gd);
			*/
		}
			
		
		dialog.show();

		try {
			Styleable.tryToBypassRestrictions();
			Field mAlert = AlertDialog.class.getDeclaredField("mAlert");
			mAlert.setAccessible(true);
			Object mController = mAlert.get(dialog);
			Field fPosBtn = mController.getClass().getDeclaredField("mButtonPositive");
			fPosBtn.setAccessible(true);
			Field fNeuBtn = mController.getClass().getDeclaredField("mButtonNeutral");
			fNeuBtn.setAccessible(true);
			Field fNegBtn = mController.getClass().getDeclaredField("mButtonNegative");
			fNegBtn.setAccessible(true);
			int tint = Build.VERSION.SDK_INT >= 31 
				? mContext.getResources().getColor(android.R.color.system_accent1_200)
				: ColorUtils.getAccentColor();
			Button mPositiveButton = (Button) fPosBtn.get(mController);
			Button mNeutralButron = (Button) fNeuBtn.get(mController);
			Button mNegativeButton = (Button) fNegBtn.get(mController);
			if(mPositiveButton != null) mPositiveButton.setTextColor(tint);
			if(mNeutralButron != null) mNeutralButron.setTextColor(tint);
			if(mNegativeButton != null) mNegativeButton.setTextColor(tint);
		} catch(Throwable t){}
	}
}
