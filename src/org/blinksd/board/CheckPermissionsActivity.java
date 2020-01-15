package org.blinksd.board;

import android.app.*;
import android.os.*;
import android.content.pm.*;

public class CheckPermissionsActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		String perm = getIntent().getStringExtra("perm");
		while(checkCallingOrSelfPermission(perm) != PackageManager.PERMISSION_GRANTED){
			requestPermissions(new String[]{perm},1);
		}
		finish();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}
	
}
