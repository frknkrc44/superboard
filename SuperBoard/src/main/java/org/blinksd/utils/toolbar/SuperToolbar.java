package org.blinksd.utils.toolbar;
import android.app.*;
import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.view.*;
import android.widget.*;

public class SuperToolbar extends LinearLayout {

	private TextView title;
	private LinearLayout menu;
	private ImageView nav;

	public SuperToolbar(Activity a){
		super(a);
		create();
	}

	private Activity getActivity(){
		return (Activity) getContext();
	}

	private void create(){
		setLayoutParams(new LayoutParams(-1,dp(56),0));
		setGravity(Gravity.CENTER_VERTICAL);
		int p = dp(8);
		setPadding(p,p,p,p);
		title = new TextView(getContext());
		title.setLayoutParams(new LayoutParams(-1,-1,1));
		title.setGravity(Gravity.CENTER_VERTICAL);
		title.setTextSize(dp(8.5f));
		h.sendEmptyMessage(0);
		nav = new ImageView(getContext());
		nav.setLayoutParams(new LayoutParams(-2,-1,0));
		nav.setScaleType(ImageView.ScaleType.FIT_CENTER);
		setDefaultIcon();
		getViewParams(nav).rightMargin = dp(8);
		menu = new LinearLayout(getContext());
		menu.setLayoutParams(new LayoutParams(-2,-1,0));
		addView(nav);
		addView(title);
		addView(menu);
	}

	public void resetIcon(){
		nav.setImageDrawable(null);
	}
	
	public void setDefaultIcon(){
		try {
			setIcon(getActivity().getPackageManager().getActivityIcon(getActivity().getComponentName()));
		} catch(Exception e){}
	}

	public void setIcon(Drawable d){
		nav.setImageDrawable(d);
	}

	public void setIcon(Bitmap b){
		nav.setImageBitmap(b);
	}

	public void setIcon(int res){
		nav.setImageResource(res);
	}

	public void setIconClickEvent(OnClickListener ocl){
		nav.setOnClickListener(ocl);
	}
	
	public void setTextGravity(int g){
		title.setGravity(g);
	}
	
	public void setTextColor(int color){
		title.setTextColor(color);
		
	}
	
	public void addMenuItem(MenuItem item){
		menu.addView(item.create(getContext()));
	}
	
	public void addMenuItem(Drawable icon, View.OnClickListener action){
		menu.addView(new MenuItem(icon,action).create(getContext()));
	}
	
	public void removeMenuItem(int index){
		menu.removeViewAt(index);
	}

	private LayoutParams getViewParams(View v){
		return (LayoutParams) v.getLayoutParams();
	}

	private static int dp(float px){
		return (int)(Resources.getSystem().getDisplayMetrics().density * px);
	}

	private Handler h = new Handler(){
		@Override
		public void handleMessage(Message msg){
			switch(msg.what){
				case 0:
					title.setText(getActivity().getTitle());
					removeMessages(0);
					sendEmptyMessageDelayed(0,100);
					break;
				default:
					removeMessages(msg.what);
					break;
			}
		}
	};
	
	public static class MenuItem {
		
		Drawable d = null;
		View.OnClickListener a = null;
		
		public MenuItem(Drawable icon, View.OnClickListener action){
			d = icon;
			a = action;
		}
		
		private View create(Context c){
			ImageView iv = new ImageView(c);
			iv.setLayoutParams(new LinearLayout.LayoutParams(dp(56),-1));
			iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
			int p = dp(8);
			iv.setPadding(p,p,p,p);
			iv.setImageDrawable(d);
			iv.setOnClickListener(a);
			return iv;
		}
	}
}
	
