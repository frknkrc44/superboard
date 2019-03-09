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
import org.blinksd.utils.color.*;
import org.blinksd.utils.toolbar.*;
import org.superdroid.db.*;

public class Settings extends Activity {
	
	private ListView lv = null;
	private SuperToolbar st = null;
	private static SuperDB sd = null;
	private SuperBoard sb = null;
	private ImageView iv = null;
	private ArrayAdapter<Key> aa = null;
	private boolean first = true;
	
	@Override
	public void onCreate(Bundle b){
		super.onCreate(b);
		sd = SuperDBHelper.getDefault(this);
		sb = new SuperBoard(this){
			@Override
			public void sendDefaultKeyboardEvent(View v){}
		};
		iv = new ImageView(this);
		iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
		iv.setLayoutParams(new RelativeLayout.LayoutParams(-1,sb.hp(20)));
		sb.addRow(0,new String[]{"1","2","3"});
		sb.createEmptyLayout(SuperBoard.KeyboardType.NUMBER);
		sb.setKeyboardHeight(20);
		sb.setKeysPadding(sb.mp(4));
		st = new SuperToolbar(this);
		st.setTextColor(0xFFFFFFFF);
		Drawable d = getResources().getDrawable(R.drawable.sym_keyboard_close);
		d.setColorFilter(0xFFFFFFFF,PorterDuff.Mode.SRC_ATOP);
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
				i.putExtra("type",a.endsWith("clr") ? 0 : a.endsWith("img") ? 2 : 1);
				String s = sd.getString(a,"def");
				i.putExtra("value",s.equals("def")?s:Integer.valueOf(s)+"");
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
			lv.removeAllViewsInLayout();
			setAdapter();
			setKeyPrefs();
		} else first = false;
	}
	
	
	public static File getBackgroundImageFile(Context c){
		return new File(c.getFilesDir()+"/bg");
	}
	
	private void setKeyPrefs(){
		File img = getBackgroundImageFile(this);
		iv.setImageDrawable(img.exists()?Drawable.createFromPath(img.getAbsolutePath()):new ColorDrawable());
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
		sb.setKeyTintColor(0,0,1,sd.getInteger(Key.key2_bgclr.name(),0));
		sb.setKeyTintColor(0,0,2,sd.getInteger(Key.enter_bgclr.name(),0));
		sb.setBackgroundColor(sd.getInteger(Key.keyboard_bgclr.name(),0));
		sb.setKeysTextColor(sd.getInteger(Settings.Key.key_textclr.name(),0));
		sb.setKeysTextSize(sb.mp(a(sd.getInteger(Settings.Key.key_textsize.name(),0))));
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
		aa = new ArrayAdapter<Key>(this,android.R.layout.simple_list_item_2,android.R.id.text1,Key.$VALUES){
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
							  : (getItem(p).name().endsWith("clr") 
							  ? SetActivity.getColorString(Integer.valueOf(s),false) 
							  : (getItem(p).equals(Key.keyboard_height) 
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
		keyboard_bgimg,
		keyboard_bgclr,
		key_bgclr,
		key2_bgclr,
		enter_bgclr,
		key_textclr,
		key_padding,
		key_radius,
		key_textsize,
		keyboard_height
	}
	
	private enum Type { color, num, image }
	
	public static class SetActivity extends Activity {
		private Key act;
		private Type type;
		private String val;
		private int set;
		private Bitmap temp;

		@Override
		protected void onCreate(Bundle b){
			super.onCreate(b);
			act = Key.valueOf(getIntent().getExtras().getString("action"));
			type = Type.$VALUES[getIntent().getExtras().getInt("type")];
			val = getIntent().getExtras().getString("value");
			setTitle(act.name());
			if(!val.equals("def") || type.equals(Type.image)){
				LinearLayout ll = new LinearLayout(this);
				ll.setLayoutParams(new LinearLayout.LayoutParams(-1,-1));
				ll.setPadding(0,SuperBoard.dp(16),0,0);
				ll.setOrientation(LinearLayout.VERTICAL);
				ll.addView(s());
				ll.addView(b(new View.OnClickListener(){
									@Override
									public void onClick(View v){
										if(v.getId() == 1){
											if(!type.equals(Type.image)){
												sd.putInteger(act.name(),set);
												sd.onlyWrite();
											} else {
												try {
													if(temp != null){
														temp.compress(Bitmap.CompressFormat.PNG,85,new FileOutputStream(getBackgroundImageFile(v.getContext())));
													}
												} catch(Exception e){}
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
		
		private View s(){
			switch(type){
				case color:
					LinearLayout ll = new LinearLayout(this);
					ll.setLayoutParams(new LinearLayout.LayoutParams(-1,-1,1));
					ll.setOrientation(LinearLayout.VERTICAL);
					ll.setGravity(Gravity.CENTER);
					final TextView x = new TextView(this);
					x.setLayoutParams(new LinearLayout.LayoutParams(-1,-1,1));
					x.setGravity(Gravity.CENTER);
					
					ll.addView(x);
					final SeekBar a = new SeekBar(this),
					r = new SeekBar(this),
					g = new SeekBar(this),
					b = new SeekBar(this);
					SeekBar.OnSeekBarChangeListener opc = new SeekBar.OnSeekBarChangeListener(){
						@Override
						public void onProgressChanged(SeekBar s, int i, boolean c){
							int clr = set = Color.argb(a.getProgress(),r.getProgress(),g.getProgress(),b.getProgress());
							x.setText(getColorString(clr,true));
							x.setTextColor(ColorUtils.satisfiesTextContrast(clr) ? 0xFF212121 : 0XFFDEDEDE);
							x.setBackgroundColor(clr);
							changeSeekBarColor(r,Color.rgb(r.getProgress(),0,0));
							changeSeekBarColor(g,Color.rgb(0,g.getProgress(),0));
							changeSeekBarColor(b,Color.rgb(0,0,b.getProgress()));
						}

						@Override
						public void onStartTrackingTouch(SeekBar s){}

						@Override
						public void onStopTrackingTouch(SeekBar s){}
					};
					for(SeekBar v : new SeekBar[]{a,r,g,b}){
						v.setMax(255);
						v.setOnSeekBarChangeListener(opc);
						changeSeekBarColor(v,0xFFFFFFFF);
						v.setLayoutParams(new LinearLayout.LayoutParams(-1,SuperBoard.dp(36),0));
						v.setPadding(v.getLayoutParams().height,v.getPaddingTop(),v.getLayoutParams().height,v.getPaddingBottom());
						ll.addView(v);
					}
					int clr = Integer.valueOf(val);
					a.setProgress(Color.alpha(clr));
					r.setProgress(Color.red(clr));
					g.setProgress(Color.green(clr));
					b.setProgress(Color.blue(clr));
					x.setBackgroundColor(clr);
					return ll;
				case num:
					SeekBar sb = new SeekBar(this);
					int min = 0;
					if(act.name().endsWith("padding")){
						sb.setMax(40);
					} else if(act.name().endsWith("radius")){
						sb.setMax(100);
					} else if(act.name().endsWith("textsize")){
						min = 6;
						sb.setMax(60-min);
					} else if(act.equals(Key.keyboard_height)){
						min = 20;
						sb.setMax(80-min);
					}
					sb.setProgress((set = Integer.valueOf(val))-min);
					setTitle(act.name()+" ("+(act.equals(Key.keyboard_height)  ? set+"" : Settings.a(set)+"")+")");
					final int m = min;
					sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
							@Override
							public void onProgressChanged(SeekBar p1,int p2,boolean p3){
								set = p2 + m;
								setTitle(act.name()+" ("+(act.equals(Key.keyboard_height)  ? set+"" : Settings.a(set)+"")+")");
							}

							@Override
							public void onStartTrackingTouch(SeekBar p1){}

							@Override
							public void onStopTrackingTouch(SeekBar p1){}
					});
					sb.getProgressDrawable().setColorFilter(0xFFFFFFFF,PorterDuff.Mode.SRC_ATOP);
					sb.getThumb().setColorFilter(0xFFFFFFFF,PorterDuff.Mode.SRC_ATOP);
					sb.setLayoutParams(new LinearLayout.LayoutParams(-1,SuperBoard.dp(36)));
					sb.setPadding(sb.getLayoutParams().height,sb.getPaddingTop(),sb.getLayoutParams().height,sb.getPaddingBottom());
					return sb;
				case image:
					LinearLayout l = new LinearLayout(this);
					l.setLayoutParams(new LinearLayout.LayoutParams(-1,-1,1));
					l.setOrientation(LinearLayout.VERTICAL);
					Button s = new Button(this);
					s.setLayoutParams(new LinearLayout.LayoutParams(-1,-2));
					s.setText("Select image");
					l.addView(s);
					ImageView iv = new ImageView(this);
					iv.setId(22);
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
					iv.setLayoutParams(new LinearLayout.LayoutParams(-1,-1));
					iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
					iv.setAdjustViewBounds(true);
					final File f = getBackgroundImageFile(this);
					if(f.exists()){
						iv.setImageBitmap(temp = BitmapFactory.decodeFile(f.getAbsolutePath()));
					}
					s.setOnLongClickListener(new View.OnLongClickListener(){
						@Override
						public boolean onLongClick(View p1){
							f.delete();
							findImageView().setImageDrawable(null);
							finish();
							return false;
						}
					});
					return l;
				default:
					return null;
			}
		}
		
		private void changeSeekBarColor(SeekBar s, int c){
			s.getThumb().setColorFilter(c,PorterDuff.Mode.SRC_ATOP);
			s.getProgressDrawable().setColorFilter(c,PorterDuff.Mode.SRC_ATOP);
		}

		@Override
		protected void onActivityResult(int requestCode,int resultCode,Intent data){
			super.onActivityResult(requestCode,resultCode,data);
			if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
				Uri uri = data.getData();
				try {
					temp = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
					findImageView().setImageBitmap(temp);
				} catch(Exception e){}
			}
		}
		
		private ImageView findImageView(){
			ViewGroup v = (ViewGroup) findViewById(android.R.id.content);
			v = (ViewGroup) v.getChildAt(0);
			v = (ViewGroup) v.getChildAt(0);
			return (ImageView) v.findViewById(22);
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
}
