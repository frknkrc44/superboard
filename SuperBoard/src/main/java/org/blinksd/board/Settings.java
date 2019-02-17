package org.blinksd.board;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import org.blinksd.utils.color.*;
import org.blinksd.utils.toolbar.*;
import org.superdroid.db.*;

public class Settings extends Activity {
	
	private ListView lv = null;
	private SuperToolbar st = null;
	private static SuperDB sd = null;
	private ArrayAdapter<Key> aa = null;
	
	@Override
	public void onCreate(Bundle b){
		super.onCreate(b);
		sd = SuperDBHelper.getDefault(this);
		setContentView(m());
	}

	@Override
	protected void onResume(){
		sd.onlyRead();
		lv.removeAllViewsInLayout();
		setAdapter();
		super.onResume();
	}

	@Override
	protected void onActivityResult(int requestCode,int resultCode,Intent data){
		onResume();
		super.onActivityResult(requestCode,resultCode,data);
	}
	
	private View m(){
		LinearLayout m = new LinearLayout(this);
		m.setLayoutParams(new LinearLayout.LayoutParams(-1,-1));
		m.setOrientation(LinearLayout.VERTICAL);
		st = new SuperToolbar(this);
		st.resetIcon();
		st.setTextGravity(Gravity.CENTER);
		m.addView(st);
		lv = new ListView(this);
		lv.setLayoutParams(new LinearLayout.LayoutParams(-1,-1));
		lv.setDivider(null);
		setAdapter();
		lv.setOnItemClickListener(new ListView.OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> p1,View p2,int p3,long p4){
				Intent i = new Intent(Settings.this,SetActivity.class);
				String a = ((Key)((ArrayAdapter)p1.getAdapter()).getItem(p3)).name();
				i.putExtra("action",a);
				i.putExtra("type",a.endsWith("clr") ? 0 : 1);
				String s = sd.getString(a,"def");
				i.putExtra("value",s.equals("def")?s:/*(a.endsWith("clr")?s:unmp(*/Integer.valueOf(s)/*)*/+"")/*)*/;
				startActivityForResult(i,RESULT_CANCELED);
			}
		});
		m.addView(lv);
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
				String s = sd.getString(getItem(p).name(),"def");
				t.setText(s.equals("def") 
					? "Varsayılan" 
					: (getItem(p).name().endsWith("clr") 
						? SetActivity.getColorString(Integer.valueOf(s),false) 
						: /*unmp(*/a(Integer.valueOf(s))/*)*/+""));
				if((!s.equals("def")) && getItem(p).name().endsWith("clr")){
					int c = Integer.valueOf(s);
					t.setBackgroundColor(c);
					t.setTextColor(ColorUtils.satisfiesTextContrast(c) ? 0xFF212121 : 0xFFDEDEDE);
				}
				return v;
			}
		};
		lv.setAdapter(aa);
	}
	
	public static float a(int i){
		return i / 10.0f;
	}
	
	/*private int unmp(int mp){
		return (int)(mp / (float)SuperBoard.mp(1));
	}*/
	
	public enum Key {
		keyboard_bgclr,
		key_bgclr,
		key2_bgclr,
		enter_bgclr,
		key_textclr,
		key_padding,
		key_radius,
		key_textsize
	}
	
	private enum Type { color, num }
	
	public static class SetActivity extends Activity {
		
		Key act;
		Type type;
		String val;
		int set;

		@Override
		protected void onCreate(Bundle b){
			super.onCreate(b);
			act = Key.valueOf(getIntent().getExtras().getString("action"));
			type = Type.$VALUES[getIntent().getExtras().getInt("type")];
			val = getIntent().getExtras().getString("value");
			setTitle(act.name());
			if(!val.equals("def")){
				LinearLayout ll = new LinearLayout(this);
				ll.setLayoutParams(new LinearLayout.LayoutParams(-1,-1));
				ll.setPadding(0,SuperBoard.dp(16),0,0);
				ll.setOrientation(LinearLayout.VERTICAL);
				ll.addView(s());
				ll.addView(b(new View.OnClickListener(){
									@Override
									public void onClick(View v){
										if(v.getId() == 1){
											sd.putInteger(act.name(),/*act.name().endsWith("clr")?*/set/*:SuperBoard.mp(set)*/);
											sd.onlyWrite();
											sendBroadcast(new Intent(InputService.COLORIZE_KEYBOARD));
										}
										finish();
									}
				},android.R.string.cancel,android.R.string.ok));
				setContentView(ll);
			} else {
				TextView tv = new TextView(this);
				int p = SuperBoard.dp(16);
				tv.setPadding(p,p,p,p);
				tv.setLayoutParams(new LinearLayout.LayoutParams(-1,-1));
				tv.setGravity(Gravity.CENTER);
				tv.setText("Firstly, open keyboard for get default value");
				setContentView(tv);
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
						}

						@Override
						public void onStartTrackingTouch(SeekBar s){}

						@Override
						public void onStopTrackingTouch(SeekBar s){}

					};
					for(SeekBar v : new SeekBar[]{a,r,g,b}){
						v.setMax(255);
						v.setOnSeekBarChangeListener(opc);
						v.getProgressDrawable().setColorFilter(0xFFFFFFFF,PorterDuff.Mode.SRC_ATOP);
						v.getThumb().setColorFilter(0xFFFFFFFF,PorterDuff.Mode.SRC_ATOP);
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
					if(act.name().endsWith("padding")){
						sb.setMax(40);
					} else if(act.name().endsWith("radius")){
						sb.setMax(100);
					} else if(act.name().endsWith("textsize")){
						sb.setMin(6);
						sb.setMax(60);
					}
					sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

							@Override
							public void onProgressChanged(SeekBar p1,int p2,boolean p3){
								set = p2;
							}

							@Override
							public void onStartTrackingTouch(SeekBar p1){}

							@Override
							public void onStopTrackingTouch(SeekBar p1){}
						
					});
					sb.setProgress(Integer.valueOf(val));
					sb.getProgressDrawable().setColorFilter(0xFFFFFFFF,PorterDuff.Mode.SRC_ATOP);
					sb.getThumb().setColorFilter(0xFFFFFFFF,PorterDuff.Mode.SRC_ATOP);
					sb.setLayoutParams(new LinearLayout.LayoutParams(-1,SuperBoard.dp(36)));
					sb.setPadding(sb.getLayoutParams().height,sb.getPaddingTop(),sb.getLayoutParams().height,sb.getPaddingBottom());
					return sb;
				default:
					return null;
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
}
