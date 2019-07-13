package org.blinksd.board;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.*;
import android.os.*;
import android.provider.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import org.blinksd.*;
import org.blinksd.utils.color.*;
import org.blinksd.utils.image.*;
import org.blinksd.utils.toolbar.*;
import org.superdroid.db.*;
import android.view.View.*;
import java.util.*;

public class Settings extends Activity {
	
	private ListView lv = null;
	private SuperToolbar st = null;
	private static SuperDB sd = null;
	private SuperBoard sb = null;
	private static ImageView iv = null;
	private ArrayAdapter<Key> aa = null;
	private boolean first = true;
	private LayoutUtils.Language cl = null;
	
	@Override
	public void onCreate(Bundle b){
		super.onCreate(b);
		sd = SuperBoardApplication.getApplicationDatabase();
		sb = new SuperBoard(this){
			@Override
			public void sendDefaultKeyboardEvent(View v){}
		};
		iv = new ImageView(this);
		iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
		iv.setLayoutParams(new RelativeLayout.LayoutParams(-1,sb.hp(20)));
		sb.addRow(0,new String[]{"1","2","3"});
		sb.setKeyDrawable(0,0,1,R.drawable.sym_keyboard_delete);
		sb.setKeyDrawable(0,0,-1,R.drawable.sym_keyboard_return);
		sb.createEmptyLayout(SuperBoard.KeyboardType.NUMBER);
		sb.setKeyboardHeight(20);
		sb.setKeysPadding(sb.mp(4));
		st = new SuperToolbar(this);
		Drawable d = getResources().getDrawable(R.drawable.sym_keyboard_close);
		st.addMenuItem(d, new View.OnClickListener(){
			@Override
			public void onClick(View v){
				sd.removeDB();
				File img = getBackgroundImageFile(v.getContext());
				if(img.exists()) img.delete();
				restartKeyboard(Settings.this);
				finish();
				startActivity(new Intent(Settings.this,Settings.class));
			}
		});
		lv = new ListView(this);
		lv.setLayoutParams(new LinearLayout.LayoutParams(-1,-1));
		lv.setDivider(null);
		lv.setOnItemClickListener(new ListView.OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> p1,View p2,int p3,long p4){
				Intent i = new Intent(Settings.this,SetActivity.class);
				String a = ((Key)((ArrayAdapter)p1.getAdapter()).getItem(p3)).name();
				i.putExtra("action",a);
				i.putExtra("type",a.endsWith("clr") ? 0 : (a.endsWith("select") ? 3 : (a.endsWith("img") ? 2 : 1)));
				String s = sd.getString(a,"def");
				i.putExtra("value",a.endsWith("select") || s.equals("def")?s:Integer.valueOf(s)+"");
				startActivityForResult(i,RESULT_CANCELED);
			}
		});
		setContentView(m());
	}

	@Override
	protected void onResume(){
		resume();
		super.onResume();
	}

	@Override
	protected void onActivityResult(int requestCode,int resultCode,Intent data){
		super.onActivityResult(requestCode,resultCode,data);
		resume();
	}

	@Override
	public void setTitle(CharSequence title){
		st.setTitle(title.toString());
		super.setTitle(title);
	}
	
	private void resume(){
		if(!first){
			sd.onlyRead();
			setAdapter();
			setKeyPrefs();
		} else first = false;
	}
	
	
	public static File getBackgroundImageFile(Context c){
		return new File(c.getFilesDir()+"/bg");
	}
	
	private void setKeyPrefs(){
		File img = getBackgroundImageFile(this);
		int blur = SuperDBHelper.getIntValueAndSetItToDefaultIsNotSet(sd,Settings.Key.keyboard_bgblur.name(),0);
		Bitmap b = BitmapFactory.decodeFile(img.getAbsolutePath());
		iv.setImageBitmap(img.exists()?(blur > 0 ? ImageUtils.fastblur(b,1,blur) : b):null);
		StateListDrawable d = new StateListDrawable();
		GradientDrawable gd = new GradientDrawable();
		gd.setColor(sb.getColorWithState(sd.getInteger(Key.key_bgclr.name(),0),false));
		gd.setCornerRadius(sb.mp(a(sd.getInteger(Key.key_radius.name(),0))));
		gd.setStroke(sb.mp(a(sd.getInteger(Settings.Key.key_padding.name(),0))),0);
		GradientDrawable pd = new GradientDrawable();
		pd.setColor(sb.getColorWithState(sd.getInteger(Key.key_bgclr.name(),0),true));
		pd.setCornerRadius(sb.mp(a(sd.getInteger(Key.key_radius.name(),0))));
		pd.setStroke(sb.mp(a(sd.getInteger(Settings.Key.key_padding.name(),0))),0);
		d.addState(new int[]{android.R.attr.state_selected},pd);
		d.addState(new int[]{},gd);
		sb.setKeysBackground(d);
		sb.setKeysShadow(sd.getInteger(Key.key_shadowsize.name(),0),sd.getInteger(Key.key_shadowclr.name(),0));
		sb.setKeyTintColor(0,0,1,sd.getInteger(Key.key2_bgclr.name(),0));
		sb.setKeyTintColor(0,0,2,sd.getInteger(Key.enter_bgclr.name(),0));
		sb.setBackgroundColor(sd.getInteger(Key.keyboard_bgclr.name(),0));
		sb.setKeysTextColor(sd.getInteger(Key.key_textclr.name(),0));
		sb.setKeysTextSize(sb.mp(a(sd.getInteger(Key.key_textsize.name(),0))));
	}
	
	private View p(){
		if(sd.isDBContainsKey(Key.key_bgclr.name())){
			RelativeLayout rl = new RelativeLayout(this);
			rl.addView(iv);
			rl.addView(sb);
			rl.setLayoutParams(new LinearLayout.LayoutParams(-1,-2));
			return rl;
		} else {
			return ok(this);
		}
	}
	
	private View m(){
		LinearLayout m = new LinearLayout(this);
		m.setLayoutParams(new LinearLayout.LayoutParams(-1,-1));
		m.setOrientation(LinearLayout.VERTICAL);
		m.addView(st);
		m.addView(p());
		setAdapter();
		m.addView(lv);
		setKeyPrefs();
		return m;
	}
	
	private void setAdapter(){
		cl = SuperBoardApplication.getKeyboardLanguage(sd.getString(Key.keyboard_lang_select.name(),"def"));
		aa = new ArrayAdapter<Key>(this,android.R.layout.simple_list_item_2,android.R.id.text1,Key.values()){
			@Override
			public View getView(int p,View v,ViewGroup g){
				if(v == null) v = super.getView(p,v,g);
				TextView t = (TextView) v.findViewById(android.R.id.text1);
				t.setText(getItem(p).name());
				t = (TextView) v.findViewById(android.R.id.text2);
				if(getItem(p).name().endsWith("img")){
					t.setText("");
				} else {
					String s = sd.getString(getItem(p).name(),"def");
					t.setText(s.equals("def") 
							  ? "Varsayılan" 
							  : getItem(p).name().endsWith("select") 
							  ? cl.label
							  : (getItem(p).name().endsWith("clr") 
							  ? SetActivity.getColorString(Integer.valueOf(s),false) 
							  : (SetActivity.isNumberNotFloat(getItem(p))
							  ? s : a(Integer.valueOf(s))+"")));
					if((!s.equals("def")) && getItem(p).name().endsWith("clr")){
						int c = Integer.valueOf(s);
						t.setBackgroundColor(c);
						t.setTextColor(ColorUtils.satisfiesTextContrast(c) ? 0xFF212121 : 0xFFDEDEDE);
					}
				}
				
				return v;
			}
		};
		st.setTextColor(0xFFDEDEDE);
		lv.setAdapter(aa);
	}
	
	public static float a(int i){
		return i / 10.0f;
	}
	
	private static void restartKeyboard(Context c){
		c.sendBroadcast(new Intent(InputService.COLORIZE_KEYBOARD));
	}
	
	private static View ok(Context c){
		TextView tv = new TextView(c);
		int p = SuperBoard.dp(16);
		tv.setPadding(p,p,p,p);
		tv.setLayoutParams(new LinearLayout.LayoutParams(-1,-1));
		tv.setGravity(Gravity.CENTER);
		tv.setText("Firstly, open keyboard for get default value");
		return tv;
	}
	
	public enum Key {
		keyboard_lang_select,
		keyboard_bgimg,
		keyboard_bgblur,
		keyboard_height,
		keyboard_bgclr,
		key_bgclr,
		key2_bgclr,
		enter_bgclr,
		key_textclr,
		key_shadowclr,
		key_padding,
		key_radius,
		key_textsize,
		key_shadowsize,
		key_vibrate_duration,
		key_longpress_duration
	}
	
	private enum Type { color, num, image, selector }
	
	private enum Gradient {
		grad_color1,
		grad_color2,
		grad_orientation
	}
	
	public static class SetActivity extends Activity {
		private Key act;
		private Type type;
		private String val;
		private int set;
		private static Bitmap temp;
		private static ImageView iv;
		private static HashMap<String,LayoutUtils.Language> list = null;

		@Override
		protected void onCreate(Bundle b){
			super.onCreate(b);
			setTitleColor(0xFFDEDEDE);
			GradientDrawable gd = new GradientDrawable();
			gd.setColor(0xFF212121);
			gd.setCornerRadius(SuperBoard.dp(8));
			getWindow().setBackgroundDrawable(gd);
			act = Key.valueOf(getIntent().getExtras().getString("action"));
			type = Type.values()[getIntent().getExtras().getInt("type")];
			val = getIntent().getExtras().getString("value");
			setTitle(act.name());
			if(!val.equals("def") || type.equals(Type.image)){
				LinearLayout ll = new LinearLayout(this);
				ll.setLayoutParams(new LinearLayout.LayoutParams(-1,-1));
				ll.setOrientation(LinearLayout.VERTICAL);
				ll.setGravity(Gravity.CENTER_HORIZONTAL);
				ll.addView(s());
				ll.addView(b(new View.OnClickListener(){
									@Override
									public void onClick(View v){
										if(v.getId() == 1){
											switch(type){
												case color:
												case num:
													sd.putInteger(act.name(),set);
													sd.onlyWrite();
													break;
												case image:
													try {
														if(temp != null){
															temp.compress(Bitmap.CompressFormat.JPEG,85,new FileOutputStream(getBackgroundImageFile(v.getContext())));
															setColorsFromBitmap(temp);
														}
													} catch(Exception e){}
													break;
												case selector:
													List<String> lst = LayoutUtils.getKeyListFromLanguageList(list);
													sd.putString(act.name(),lst.get(set));
													sd.onlyWrite();
													break;
											}
											restartKeyboard(SetActivity.this);
										}
										finish();
									}
				},android.R.string.cancel,android.R.string.ok));
				setContentView(ll);
			} else {
				setContentView(ok(this));
			}
		}
		
		private void setColorsFromBitmap(Bitmap b){
			if(b == null) return;
			int c = ColorUtils.getBitmapColor(b);
			sd.putInteger(Key.keyboard_bgclr.name(),c-0xAA000000);
			sd.putInteger(Key.key_bgclr.name(),c-0xAA000000);
			sd.putInteger(Key.key2_bgclr.name(),SuperBoard.getColorWithState(c,true));
			sd.putInteger(Key.enter_bgclr.name(),ColorUtils.satisfiesTextContrast(c) ? SuperBoard.getColorWithState(sd.getInteger(Key.key2_bgclr.name(),0xFF212121),true) : 0xFFFFFFFF);
			sd.putInteger(Key.key_textclr.name(),ColorUtils.satisfiesTextContrast(c) ? 0xFF212121 : 0xFFDEDEDE);
			sd.putInteger(Key.key_shadowclr.name(),sd.getInteger(Key.key_textclr.name(),0xFFDEDEDE));
			sd.onlyWrite();
		}
		
		private View b(View.OnClickListener c, int... a){
			if(a.length < 1) return a(c);
			String[] x = new String[a.length];
			for(int b = 0;b < a.length;b++){
				x[b] = getResources().getString(a[b]);
			}
			return a(c,x);
		}
		
		private View a(View.OnClickListener c, String... a){
			LinearLayout ll = new LinearLayout(this);
			ll.setLayoutParams(new LinearLayout.LayoutParams(-1,-2,0));
			if(a.length < 1) return ll;
			ll.setGravity(Gravity.END);
			for(String s : a){
				TextView tv = new TextView(this);
				tv.setLayoutParams(new LinearLayout.LayoutParams(-2,-1));
				int p = SuperBoard.mp(2);
				tv.setPadding(p,p,p,p);
				tv.setText(s);
				tv.setOnClickListener(c);
				tv.setId(ll.getChildCount());
				ll.addView(tv);
			}
			return ll;
		}
		
		private void q(TextView x){
			x.setText(getColorString(set,true));
			x.setTextColor(ColorUtils.satisfiesTextContrast(set) ? 0xFF212121 : 0XFFDEDEDE);
			x.setBackgroundColor(set);
		}
		
		private View generateColorDialog(){
			LinearLayout ll = new LinearLayout(this);
			ll.setLayoutParams(new LinearLayout.LayoutParams(-1,-1,1));
			ll.setOrientation(LinearLayout.VERTICAL);
			ll.setGravity(Gravity.CENTER);
			final TextView x = new TextView(this);
			x.setLayoutParams(new LinearLayout.LayoutParams(-1,-1,1));
			x.setGravity(ll.getGravity());
			ll.addView(x);
			final CustomSeekBar a = new CustomSeekBar(this),
				r = new CustomSeekBar(this),
				g = new CustomSeekBar(this),
				b = new CustomSeekBar(this);
			changeSeekBarColor(r,Color.rgb(0xDE,0,0));
			changeSeekBarColor(g,Color.rgb(0,0xDE,0));
			changeSeekBarColor(b,Color.rgb(0,0,0xDE));
			set = Integer.valueOf(val);
			q(x);
			for(CustomSeekBar v : new CustomSeekBar[]{a,r,g,b}){
				v.setMax(255);
			}
			a.setProgress(Color.alpha(set));
			r.setProgress(Color.red(set));
			g.setProgress(Color.green(set));
			b.setProgress(Color.blue(set));
			SeekBar.OnSeekBarChangeListener opc = new SeekBar.OnSeekBarChangeListener(){
				@Override
				public void onProgressChanged(SeekBar s, int i, boolean c){
					set = Color.argb(a.getProgress(),r.getProgress(),g.getProgress(),b.getProgress());
					q(x);
				}

				@Override
				public void onStartTrackingTouch(SeekBar s){}

				@Override
				public void onStopTrackingTouch(SeekBar s){}
			};
			for(CustomSeekBar v : new CustomSeekBar[]{a,r,g,b}){
				v.setOnSeekBarChangeListener(opc);
				ll.addView(v);
			}
			return ll;
		}
		
		private View generateNumberSeekDialog(){
			CustomSeekBar sb = new CustomSeekBar(this);
			int min = 0;
			if(act.name().endsWith("radius") || act.equals(Key.key_vibrate_duration)){
				sb.setMax(100);
			} else if(act.name().endsWith("textsize")){
				min = 6;
				sb.setMax(60-min);
			} else if(act.equals(Key.key_longpress_duration)){
				min = 1;
				sb.setMax(3-min);
			} else if(act.equals(Key.keyboard_height)){
				min = 20;
				sb.setMax(80-min);
			} else {
				sb.setMax(40);
			}
			sb.setProgress((set = Integer.valueOf(val))-min);
			setTitle(act.name()+" ("+(isNumberNotFloat(act) ? set+"" : Settings.a(set)+"")+")");
			final int m = min;
			sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
					@Override
					public void onProgressChanged(SeekBar p1,int p2,boolean p3){
						set = p2 + m;
						setTitle(act.name()+" ("+(isNumberNotFloat(act) ? set+"" : Settings.a(set)+"")+")");
					}

					@Override
					public void onStartTrackingTouch(SeekBar p1){}

					@Override
					public void onStopTrackingTouch(SeekBar p1){}
				});
			return sb;
		}
		
		private View generateImageSelectorDialog(){
			LinearLayout l = new LinearLayout(this);
			l.setLayoutParams(new LinearLayout.LayoutParams(-1,-1,1));
			l.setOrientation(LinearLayout.VERTICAL);
			Button s = new Button(this);
			s.setLayoutParams(new LinearLayout.LayoutParams(-1,-2,0));
			s.setText("Select image");
			l.addView(s);
			iv = new ImageView(this){
				@Override
				public void setImageBitmap(Bitmap b){
					super.setImageBitmap(b);
					temp = b;
				}
			};
			l.addView(iv);
			s.setOnClickListener(new View.OnClickListener(){
					@Override
					public void onClick(View p1){
						Intent i = new Intent();
						i.setType("image/*");
						i.setAction(Intent.ACTION_GET_CONTENT);
						startActivityForResult(Intent.createChooser(i,""),1);
					}
				});
			iv.setLayoutParams(new LinearLayout.LayoutParams(-1,-1,1));
			iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
			iv.setAdjustViewBounds(true);
			final File f = getBackgroundImageFile(this);
			if(f.exists()){
				iv.setImageBitmap(temp = BitmapFactory.decodeFile(f.getAbsolutePath()));
			}
			Button rb = new Button(this);
			rb.setLayoutParams(new LinearLayout.LayoutParams(-1,-2,0));
			l.addView(rb);
			rb.setText("Rotate");
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
						iv.setImageBitmap(temp);
					}
				});
			s.setOnLongClickListener(new View.OnLongClickListener(){
					@Override
					public boolean onLongClick(View p1){
						f.delete();
						Settings.iv.setImageDrawable(null);
						finish();
						return false;
					}
				});
			return l;
		}
		
		private View generateGradientSelectorDialog(){
			LinearLayout l = new LinearLayout(this);
			l.setLayoutParams(new LinearLayout.LayoutParams(-1,-1,1));
			l.setOrientation(LinearLayout.VERTICAL);
			LinearLayout h = new LinearLayout(this);
			h.setLayoutParams(new LinearLayout.LayoutParams(-1,-2,1));
			for(Gradient g : Gradient.values()){
				Button b = new Button(this);
				b.setLayoutParams(new LinearLayout.LayoutParams(-2,-1,1));
				b.setText(g.name());
				h.addView(b);
			}
			HorizontalScrollView s = new HorizontalScrollView(this);
			s.setLayoutParams(new LinearLayout.LayoutParams(-1,-2,1));
			s.addView(h);
			ImageView v = new ImageView(this);
			v.setLayoutParams(new LinearLayout.LayoutParams(-1,-1,1));
			v.setScaleType(ImageView.ScaleType.FIT_CENTER);
			
			l.addView(s);
			return l;
		}
		
		private OnClickListener onButtonClick = new OnClickListener(){

			@Override
			public void onClick(View p1) {
				Intent i = new Intent(SetActivity.this,SetActivity.class);
				String a = ((Key)p1.getTag()).name();
				i.putExtra("action",a);
				i.putExtra("type",a.contains("color") ? 1 : 2);
				String s = sd.getString(a,"def");
				i.putExtra("value",s.equals("def")?s:Integer.valueOf(s)+"");
				startActivityForResult(i,RESULT_CANCELED);
			}

			
		};
		
		private View generateLanguageSelectorDialog(){
			try {
				list = SuperBoardApplication.getKeyboardLanguageList();
			} catch(Throwable t){
				list = new HashMap<String,LayoutUtils.Language>();
			}
			RadioGroup rg = new RadioGroup(this);
			int i = SuperBoard.dp(8);
			rg.setPadding(i,i,i,i);
			rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
				public void onCheckedChanged(RadioGroup group, int checkedId){
					set = checkedId;
				}
			});
			i = 0;
			LayoutUtils.Language sl = SuperBoardApplication.getKeyboardLanguage(val);
			for(String key : list.keySet()){
				CustomRadioButton rb = new CustomRadioButton(this);
				rb.setId(i);
				LayoutUtils.Language l = list.get(key);
				rb.setChecked(l.language.equals(sl.language));
				rb.setText(l.label);
				rg.addView(rb);
				i++;
			}
			return rg;
		}
		
		private View s(){
			switch(type){
				case color:
					return generateColorDialog();
				case num:
					return generateNumberSeekDialog();
				case image:
					return generateImageSelectorDialog();
				case selector:
					return generateLanguageSelectorDialog();
				default:
					return null;
			}
		}
		
		private static boolean isNumberNotFloat(Key k){
			return k.name().endsWith("height") || k.equals(Key.keyboard_bgblur) || k.name().endsWith("duration");
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
					result = ImageUtils.get512pxBitmap(result);
					iv.setImageBitmap(temp = result);
				}
			}

		}
		
		private void changeSeekBarColor(CustomSeekBar s, int c){
			s.getThumb().setColorFilter(c,PorterDuff.Mode.SRC_ATOP);
			s.getProgressDrawable().setColorFilter(c,PorterDuff.Mode.SRC_ATOP);
		}

		@Override
		protected void onActivityResult(int requestCode,int resultCode,Intent data){
			super.onActivityResult(requestCode,resultCode,data);
			if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
				final Uri uri = data.getData();
				new ImageTask().execute(getContentResolver(),uri);
			}
		}
		
		private static String getColorString(int color, boolean l){
			return getColorString(Color.alpha(color),Color.red(color),Color.green(color),Color.blue(color),l);
		}
		
		private static String getColorString(int a, int r, int g, int b, boolean l){
			return ("#"+z(a)+z(r)+z(g)+z(b)+(l?"\n("+a+", "+r+", "+g+", "+b+")":"")).toUpperCase();
		}
		
		private static String z(int x){
			if(x == 0) return "00";
			String s = Integer.toHexString(x);
			return x < 16 ? "0"+s : s;
		}
	}
	
	private static class CustomRadioButton extends RadioButton {
		CustomRadioButton(Context c){
			super(c);
			int i = SuperBoard.dp(8);
			setPadding(i,0,i,0);
			setRadioButton();
		}
		
		void setRadioButton(){
			StateListDrawable sld = new StateListDrawable();
			int i = 64;
			int g = SuperBoard.dp(i);
			Bitmap b = Bitmap.createBitmap(g,g,Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(b);
			Paint p = new Paint();
			i = g = SuperBoard.dp(4);
			p.setStyle(Paint.Style.STROKE);
			p.setStrokeWidth(i);
			i *= 3;
			p.setColor(0xFFDEDEDE);
			c.drawRoundRect(i,i,b.getWidth()-i,b.getHeight()-i,g,g,p);
			BitmapDrawable bdn = new BitmapDrawable(b);
			b = Bitmap.createBitmap(b);
			c = new Canvas(b);
			p.setStyle(Paint.Style.FILL);
			p.setStrokeWidth(0);
			i *= 2;
			c.drawRoundRect(i,i,b.getWidth()-i,b.getHeight()-i,g,g,p);
			BitmapDrawable bdc = new BitmapDrawable(b);
			sld.addState(new int[]{android.R.attr.state_checked},bdc);
			sld.addState(new int[]{},bdn);
			setButtonDrawable(sld);
		}
	}
	
	private static class CustomSeekBar extends SeekBar {
		CustomSeekBar(Context c){
			super(c);
			setLayoutParams(new LinearLayout.LayoutParams(SuperBoard.mp(50),-2,0));
			int p = SuperBoard.dp(4);
			setPadding(p*4,p,p*4,p);
			if(Build.VERSION.SDK_INT >= 21)
				setSplitTrack(false);
			drawSeekBar();
		}
		
		void drawSeekBar(){
			Bitmap b = Bitmap.createBitmap(SuperBoard.dp(48),SuperBoard.dp(48),Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(b);
			Paint p = new Paint();
			p.setStyle(Paint.Style.FILL);
			p.setColor(0xFFDEDEDE);
			c.drawOval(0,0,b.getWidth(),b.getHeight(),p);
			setThumb(new BitmapDrawable(b));
			Drawable ld = getResources().getDrawable(R.drawable.pbar);
			ld.setColorFilter(p.getColor(),PorterDuff.Mode.SRC_ATOP);
			setProgressDrawable(ld);
		}
	}
}
