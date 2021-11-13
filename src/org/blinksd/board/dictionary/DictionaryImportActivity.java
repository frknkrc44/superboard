package org.blinksd.board.dictionary;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import org.blinksd.*;
import org.blinksd.board.*;
import org.blinksd.utils.dictionary.*;
import org.blinksd.utils.layout.*;

public class DictionaryImportActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Intent i = new Intent();
		i.setType("application/octet-stream");
		i.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(Intent.createChooser(i,""),1);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
			new DictLoadTask().execute(data.getData());
		} else finish();
	}
	
	private void prepareProgressView(){
		LinearLayout ll = new LinearLayout(this);
		ll.setLayoutParams(new FrameLayout.LayoutParams(-1,-1));
		ll.setGravity(Gravity.CENTER);
		ll.setOrientation(LinearLayout.VERTICAL);
		int pad = DensityUtils.dpInt(16);
		ll.setPadding(pad,pad,pad,pad);
		ProgressBar pb = new ProgressBar(this);
		pb.setIndeterminate(true);
		ll.addView(pb);
		TextView tv = new TextView(this);
		tv.setLayoutParams(new FrameLayout.LayoutParams(-1,-2));
		tv.setGravity(Gravity.CENTER);
		tv.setText(R.string.settings_dict_importing);
		tv.setTextAppearance(android.R.style.TextAppearance_DeviceDefault_Medium);
		ll.addView(tv);
		TextView tv2 = new TextView(this);
		tv2.setId(android.R.id.text1);
		tv2.setLayoutParams(new FrameLayout.LayoutParams(-1,-2));
		tv2.setGravity(Gravity.CENTER);
		tv2.setTextAppearance(android.R.style.TextAppearance_DeviceDefault_Medium);
		ll.addView(tv2);
		setContentView(ll);
	}

	@Override
	public void onBackPressed(){}
	
	private class DictLoadTask extends AsyncTask<Uri, Integer, Void> implements DictionaryDB.OnSaveProgressListener {

		@Override
		public void onProgress(int current, int state){
			if(current % 1000 == 0 || state == DictionaryDB.OnSaveProgressListener.STATE_DELETE_DUPLICATES)
				publishProgress(current, state);
		}

		@Override
		protected void onProgressUpdate(Integer[] values){
			super.onProgressUpdate(values);
			int current = values[0];
			int state = values[1];
			TextView tv = findViewById(android.R.id.text1);
			switch(state){
				case DictionaryDB.OnSaveProgressListener.STATE_IMPORT:
					tv.setText(String.format(getString(R.string.settings_dict_import_count), current));
					break;
				case DictionaryDB.OnSaveProgressListener.STATE_DELETE_DUPLICATES:
					tv.setText(getString(R.string.settings_dict_import_delete_duplicates));
					break;
			}
		}


		@Override
		protected void onPreExecute(){
			super.onPreExecute();
			prepareProgressView();
		}

		@Override
		protected Void doInBackground(Uri[] p1){
			try {
				Uri uri = p1[0];
				InputStream pfd = getContentResolver().openInputStream(uri);
				String name = uri.getLastPathSegment();
				if(name.contains("/"))
					name = name.substring(name.lastIndexOf('/')+1);
				assert name.endsWith(".fbd") : "Name is " + name;
				name = name.substring(0, name.lastIndexOf("."));
				
				SuperBoardApplication.getDictDB().saveToDB(name, pfd, this);
			} catch (IOException e){
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result){
			super.onPostExecute(result);
			finish();
		}
	}
}
