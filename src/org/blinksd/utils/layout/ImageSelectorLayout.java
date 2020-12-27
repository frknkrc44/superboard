package org.blinksd.utils.layout;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.*;
import android.os.*;
import android.provider.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import android.widget.TabHost.*;
import java.io.*;
import java.util.*;
import org.blinksd.*;
import org.blinksd.board.*;
import org.blinksd.utils.image.*;
import org.blinksd.sdb.*;

public class ImageSelectorLayout {
	
	private ImageSelectorLayout(){}
	
	private static SuperMiniDB db;
	private static TabWidget widget;
	private static ImageView prev;
	private static Bitmap temp;
	private static TreeMap<Integer,Integer> colorList;
	
	static {
		db = SuperBoardApplication.getApplicationDatabase();
	}
	
	public static View getImageSelectorLayout(final Dialog win, final AppSettingsV2 ctx, String key){
		LinearLayout main = LayoutCreator.createFilledVerticalLayout(FrameLayout.class,ctx);

		widget = new TabWidget(ctx);
		widget.setId(android.R.id.tabs);

		final TabHost host = new TabHost(ctx);
		host.setLayoutParams(LayoutCreator.createLayoutParams(LinearLayout.class,-1,-2));
		FrameLayout fl = new FrameLayout(ctx);
		fl.setLayoutParams(LayoutCreator.createLayoutParams(LinearLayout.class,-1,-1));
		fl.setId(android.R.id.tabcontent);
		LinearLayout holder = LayoutCreator.createFilledVerticalLayout(LinearLayout.class,ctx);
		holder.setGravity(Gravity.CENTER);
		holder.addView(widget);
		prev = new ImageView(ctx){
			@Override
			public void setImageBitmap(Bitmap b){
				super.setImageBitmap(b);
				temp = b;
			}
		};
		prev.setId(android.R.id.custom);
		int dp = DensityUtils.hpInt(25);
		prev.setLayoutParams(new LinearLayout.LayoutParams(-1,dp,0));
		prev.setScaleType(ImageView.ScaleType.FIT_CENTER);
		prev.setAdjustViewBounds(true);
		holder.addView(prev);
		holder.addView(fl);
		host.addView(holder);
		host.setOnTabChangedListener(new TabHost.OnTabChangeListener(){

				@Override
				public void onTabChanged(String p1){
					if(host.getCurrentTab() == 1 && colorList.size() < 1){
						gradientAddColorListener.onClick(host);
					}
				}

		});
		main.addView(host);

		final String[] stra = {
			"image_selector_photo",
			"image_selector_gradient"
		};

		for(int i = 0;i < stra.length;i++){
			stra[i] = ctx.getTranslation(stra[i]);
		}

		host.setup();

		for(int i = 0;i < stra.length;i++){
			TabSpec ts = host.newTabSpec(stra[i]);
			TextView tv = (TextView) LayoutInflater.from(ctx).inflate(android.R.layout.simple_list_item_1,widget,false);
			LinearLayout.LayoutParams pr = (LinearLayout.LayoutParams) LayoutCreator.createLayoutParams(LinearLayout.class,-1,DensityUtils.dpInt(48));
			pr.weight = 0.33f;
			tv.setLayoutParams(pr);
			tv.setText(stra[i]);
			tv.setBackgroundResource(R.drawable.tab_indicator_material);
			tv.getBackground().setColorFilter(0xFFDEDEDE,PorterDuff.Mode.SRC_ATOP);
			tv.setGravity(Gravity.CENTER);
			tv.setPadding(0,0,0,0);
			tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP,16);
			ts.setIndicator(tv);
			final View v = getView(win,ctx,i);
			ts.setContent(new TabContentFactory(){
				@Override
				public View createTabContent(String p1){
					return v;
				}
			});
			host.addTab(ts);
		}
		
		return main;
	}
	
	private static View getView(Dialog win, AppSettingsV2 ctx, int index){
		switch(index){
			case 0: return getPhotoSelector(win,ctx);
			case 1: return getGradientSelector(ctx);
		}
		return null;
	}
	
	private static View getPhotoSelector(final Dialog win, final AppSettingsV2 ctx){
		LinearLayout l = LayoutCreator.createFilledVerticalLayout(LinearLayout.class,ctx);
		Button s = LayoutCreator.createButton(ctx);
		s.setLayoutParams(new LinearLayout.LayoutParams(-1,-2,0));
		s.setText(ctx.getTranslation("image_selector_select"));
		l.addView(s);
		Button w = LayoutCreator.createButton(ctx);
		w.setLayoutParams(new LinearLayout.LayoutParams(-1,-2,0));
		w.setText(ctx.getTranslation("image_selector_wp"));
		l.addView(w);
		s.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View p1){
					Intent i = new Intent();
					i.setType("image/*");
					i.setAction(Intent.ACTION_GET_CONTENT);
					ctx.startActivityForResult(Intent.createChooser(i,""),1);
				}
			});
		w.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View p1){
					int pm = ctx.checkCallingOrSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE);
					if(Build.VERSION.SDK_INT < 23 || pm == PackageManager.PERMISSION_GRANTED){
						WallpaperManager wm = (WallpaperManager) ctx.getSystemService(Context.WALLPAPER_SERVICE);
						Drawable d;
						if(wm.getWallpaperInfo() != null){
							Toast.makeText(p1.getContext(),"You're using live wallpaper, loading thumbnail ...",Toast.LENGTH_SHORT).show();
							d = wm.getWallpaperInfo().loadThumbnail(ctx.getPackageManager());
						} else {
							d = wm.getDrawable();
						}
						
						if(d instanceof BitmapDrawable){
							Bitmap b = ((BitmapDrawable) d).getBitmap();
							b = ImageUtils.getMinimizedBitmap(b);
							prev.setImageBitmap(b);
						}
					} else {
						Toast.makeText(p1.getContext(),"Enable storage access for get system wallpaper",Toast.LENGTH_LONG).show();
						ctx.startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:"+ctx.getPackageName())));
					}
				}
			});
		
		final File f = ctx.getBackgroundImageFile();
		if(f.exists()){
			prev.setImageBitmap(temp = BitmapFactory.decodeFile(f.getAbsolutePath()));
		}
		Button rb = LayoutCreator.createButton(ctx);
		rb.setLayoutParams(new LinearLayout.LayoutParams(-1,-2,0));
		l.addView(rb);
		rb.setText(ctx.getTranslation("image_selector_rotate"));
		rb.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View p1){
					if(temp == null){
						return;
					}
					Matrix matrix = new Matrix();
					matrix.postRotate(90);
					if(!temp.isMutable()){
						temp = temp.copy(Bitmap.Config.ARGB_8888,true);
					}
					temp = Bitmap.createBitmap(temp, 0, 0, temp.getWidth(), temp.getHeight(), matrix, true);
					prev.setImageBitmap(temp);
				}
			});
		s.setOnLongClickListener(new View.OnLongClickListener(){
				@Override
				public boolean onLongClick(View p1){
					f.delete();
					prev.setImageDrawable(null);
					win.dismiss();
					ctx.restartKeyboard();
					return false;
				}
			});
		return l;
	}
	
	private static LinearLayout gradientSel;
	
	private static View getGradientSelector(final AppSettingsV2 ctx){
		if(colorList == null){
			colorList = new TreeMap<Integer,Integer>();
		} else {
			colorList.clear();
		}
		
		gradientSel = LayoutCreator.createFilledVerticalLayout(LinearLayout.class,ctx);
		gradientSel.addView(getColorSelectorItem(ctx,-1));
		gradientSel.addView(getColorSelectorItem(ctx,-2));
		ScrollView gradientSelScr = new ScrollView(ctx);
		gradientSelScr.setLayoutParams(new LinearLayout.LayoutParams(-1,-1));
		gradientSelScr.addView(gradientSel);
		return gradientSelScr;
	}
	
	public static View getColorSelectorItem(AppSettingsV2 ctx, int index){
		return new ColorSelectorItemLayout(ctx, index, colorList, gradientAddColorListener, gradientDelColorListener, colorSelectorListener);
	}
	
	private static int indexNum = 0, gradientType = 0;
	
	private static int getNextEmptyItemIndex(){
		return indexNum++;
	}
	
	private static GradientDrawable.Orientation getGradientOrientation(){
		return GradientOrientation.getFromIndex(gradientType);
	}
	
	private static int[] getGradientColors(){
		if(colorList.size() < 1) return new int[]{0,0};
		Object[] ar = colorList.values().toArray();
		int size = ar.length == 1 ? 2 : ar.length;
		int[] out = new int[size];
		for(int i = 0;i < ar.length;i++){
			out[i] = (int) ar[i];
		}
		if(ar.length == 1){
			out[1] = out[0];
		}
		return out;
	}
	
	private static Bitmap convertGradientToBitmap(){
		int size = (int) ImageUtils.minSize;
		GradientDrawable gd = new GradientDrawable();
		gd.setColors(getGradientColors());
		gd.setBounds(0,0,size,size);
		gd.setOrientation(getGradientOrientation());
		Bitmap out = Bitmap.createBitmap(size,size,Bitmap.Config.ARGB_8888);
		Canvas drw = new Canvas(out);
		gd.draw(drw);
		return out;
	}
	
	private static final View.OnClickListener colorSelectorListener = new View.OnClickListener(){

		@Override
		public void onClick(final View p1){
			AppSettingsV2 ctx = (AppSettingsV2) p1.getContext();
			int tag = (int) p1.getTag();
			final View px = ColorSelectorLayout.getColorSelectorLayout(ctx,tag);
			AlertDialog.Builder build = new AlertDialog.Builder(p1.getContext());
			build.setTitle(((TextView) p1.findViewById(android.R.id.text1)).getText());
			build.setView(px);
			build.setOnCancelListener(new DialogInterface.OnCancelListener(){

					@Override
					public void onCancel(DialogInterface p1){
						prev.setImageBitmap(convertGradientToBitmap());
						System.gc();
					}

				});
			build.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface p1, int p2){
						p1.dismiss();
					}

				});
			build.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface p0, int p2){
						p1.setTag(px.findViewById(android.R.id.tabs).getTag());
						p0.dismiss();
					}

				});
			build.show();
		}

	};
	
	private static final View.OnClickListener gradientAddColorListener = new View.OnClickListener(){

		@Override
		public void onClick(View p1){
			AppSettingsV2 ctx = (AppSettingsV2) p1.getContext();
			if(p1.getId() == -2){
				gradientType++;
				prev.setImageBitmap(convertGradientToBitmap());
				System.gc();
			} else {
				int index = getNextEmptyItemIndex();
				View v = getColorSelectorItem(ctx,index);
				int count = gradientSel.getChildCount();
				gradientSel.addView(v,count - 2);
				colorSelectorListener.onClick(v);
			}
		}

	};
	
	private static final View.OnClickListener gradientDelColorListener = new View.OnClickListener(){

		@Override
		public void onClick(View p1){
			if(colorList.size() < 2){
				AppSettingsV2 ctx = (AppSettingsV2) p1.getContext();
				String out = String.format(ctx.getTranslation("image_selector_gradient_remove_item_error"),colorList.size());
				Toast.makeText(ctx,out,Toast.LENGTH_SHORT).show();
				return;
			}
			int num = p1.getId();
			colorList.remove(num);
			gradientSel.removeView((ViewGroup) p1.getParent());
			prev.setImageBitmap(convertGradientToBitmap());
		}

	};
	
	private static class GradientOrientation {
		
		private GradientOrientation(){}
		
		public static GradientDrawable.Orientation getFromIndex(int index){
			return getAll()[index % getCount()];
		}
		
		public static int getCount(){
			return getAll().length;
		}
		
		public static String getNameFromIndex(int index){
			return getFromIndex(index).name();
		}
		
		public static GradientDrawable.Orientation[] getAll(){
			return GradientDrawable.Orientation.values();
		}
		
	}
	
}
