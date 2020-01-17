package org.blinksd.utils.database;

import android.content.*;
import android.net.*;
import android.os.*;
import android.util.*;
import org.blinksd.*;
import org.blinksd.sdbcenter.services.*;
import android.content.pm.*;
import org.blinksd.board.*;

public class SuperDBCenterConnector {
	
	public SuperDBCenterConnector(Context context){
		ctx = context;
	}
	
	private ISuperDBConnection sdbc = null;
	private Context ctx = null;
	
	public final void connectToSuperDB(){
		checkPermissions();
		ctx.bindService(getSuperDBConnectorIntent(),sdbconn,Context.BIND_AUTO_CREATE);
	}
	
	public final ISuperDBConnection getSuperDBConnection(){
		return sdbc;
	}
	
	private final void checkPermissions(){
		String perm = "org.blinksd.sdbcenter.permission.MANAGE_DATABASE";
		if(ctx.checkCallingOrSelfPermission(perm) != PackageManager.PERMISSION_GRANTED){
			ctx.startActivity(new Intent(ctx, CheckPermissionsActivity.class).putExtra("perm",perm));
		}
	}
	
	private final Intent getSuperDBConnectorIntent(){
		String conn = getConnectorPackageName();
		Class<?> clazz = ISuperDBConnection.class;
		Intent out = new Intent(clazz.getName());
		ComponentName name = new ComponentName(conn,clazz.getName().replace("IS","S"));
		out.setComponent(name);
		return out;
	}
	
	private final String getConnectorPackageName(){
		return "org.blinksd.sdbcenter";
	}
	
	private final ServiceConnection sdbconn = new ServiceConnection(){

		@Override
		public void onServiceConnected(ComponentName p1, IBinder p2){
			Log.d("SuperDBCenterConnector","Connected");
			sdbc = ISuperDBConnection.Stub.asInterface(p2);
		}

		@Override
		public void onServiceDisconnected(ComponentName p1){
			sdbc = null;
			connectToSuperDB();
		}
		
	};
	
}
