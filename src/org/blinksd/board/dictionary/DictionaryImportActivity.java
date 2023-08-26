package org.blinksd.board.dictionary;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.blinksd.SuperBoardApplication;
import org.blinksd.board.R;
import org.blinksd.utils.dictionary.DictionaryDB;
import org.blinksd.utils.layout.DensityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executors;

public class DictionaryImportActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = new Intent();
        i.setType("application/octet-stream");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, ""), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            new DictLoadTask().execute(data.getData());
        } else finish();
    }

    private void prepareProgressView() {
        LinearLayout ll = new LinearLayout(this);
        ll.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        ll.setGravity(Gravity.CENTER);
        ll.setOrientation(LinearLayout.VERTICAL);
        int pad = DensityUtils.dpInt(16);
        ll.setPadding(pad, pad, pad, pad);
        ProgressBar pb = new ProgressBar(this);
        pb.setIndeterminate(true);
        ll.addView(pb);
        TextView tv = new TextView(this);
        tv.setLayoutParams(new FrameLayout.LayoutParams(-1, -2));
        tv.setGravity(Gravity.CENTER);
        tv.setText(R.string.settings_dict_importing);
        tv.setTextAppearance(android.R.style.TextAppearance_DeviceDefault_Medium);
        ll.addView(tv);
        TextView tv2 = new TextView(this);
        tv2.setId(android.R.id.text1);
        tv2.setLayoutParams(new FrameLayout.LayoutParams(-1, -2));
        tv2.setGravity(Gravity.CENTER);
        tv2.setTextAppearance(android.R.style.TextAppearance_DeviceDefault_Medium);
        ll.addView(tv2);
        setContentView(ll);
    }

    @Override
    public void onBackPressed() {
    }

    private class DictLoadTask implements DictionaryDB.OnSaveProgressListener {

        @Override
        public void onProgress(int current, int state) {
            if (current % 1000 == 0 || state == DictionaryDB.OnSaveProgressListener.STATE_DELETE_DUPLICATES)
                publishProgress(current, state);
        }

        private void publishProgress(Integer... values) {
            onProgressUpdate(values);
        }

        protected void onProgressUpdate(Integer[] values) {
            int current = values[0];
            int state = values[1];
            TextView tv = findViewById(android.R.id.text1);
            switch (state) {
                case DictionaryDB.OnSaveProgressListener.STATE_IMPORT:
                    tv.setText(String.format(getString(R.string.settings_dict_import_count), current));
                    break;
                case DictionaryDB.OnSaveProgressListener.STATE_DELETE_DUPLICATES:
                    tv.setText(getString(R.string.settings_dict_import_delete_duplicates));
                    break;
            }
        }

        public void execute(Uri... args) {
            onPreExecute();
            Executors.newSingleThreadExecutor().execute(() -> {
                Void out = doInBackground(args);
                SuperBoardApplication.mainHandler.post(() -> onPostExecute(out));
            });
        }

        protected void onPreExecute() {
            prepareProgressView();
        }

        protected Void doInBackground(Uri[] p1) {
            try {
                Uri uri = p1[0];
                Cursor returnCursor = getContentResolver().query(uri, null, null, null, null);
                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                returnCursor.moveToFirst();

                String name = returnCursor.getString(nameIndex);
                assert name.endsWith(".fbd") : "Name is " + name;

                int idx = findDotIndex(name);
                name = name.substring(0, idx);

                InputStream pfd = getContentResolver().openInputStream(uri);

                SuperBoardApplication.getDictDB().saveToDB(name, pfd, this);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        private int findDotIndex(String txt) {
            char[] arr = txt.toCharArray();
            for (int i = 0; i < arr.length; i++) {
                char chr = arr[i];
                if (chr == '(' || chr == '.') {
                    return i;
                }
            }

            return 2;
        }

        protected void onPostExecute(Void result) {
            finish();
        }
    }
}
