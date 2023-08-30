package org.blinksd.utils.dictionary;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.blinksd.SuperBoardApplication;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class DictionaryDB extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "dicts.db";
    private static final int DATABASE_VERSION = 2;
    public boolean isReady = true;
    private SQLiteDatabase mReadDatabase;

    public DictionaryDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static String escapeString(String str) {
        if (str != null && str.length() > 0) {
            str = str.replace("\\", "\\\\");
            str = str.replace("'", "`");
            str = str.replace("\0", "\\0");
            str = str.replace("\n", "\\n");
            str = str.replace("\r", "\\r");
            str = str.replace("\"", "\\\"");
            str = str.replace("\\x1a", "\\Z");
        }
        return str;
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        if (mReadDatabase == null || !mReadDatabase.isOpen()) {
            mReadDatabase = super.getReadableDatabase();
        }

        return mReadDatabase;
    }

    @Override
    public void onCreate(SQLiteDatabase p1) {
        List<String> types = SuperBoardApplication.getLanguageTypes();
        StringBuilder sb = new StringBuilder();
        isReady = false;

        for (String type : types) {
            sb.append("CREATE TABLE IF NOT EXISTS LANG_")
                    .append(escapeString(type))
                    .append(" (id INTEGER PRIMARY KEY AUTOINCREMENT,")
                    .append(" word TEXT")
                    .append(", usage_count INTEGER DEFAULT 0)");
            p1.execSQL(sb.toString());
            sb.setLength(0);
        }

        isReady = true;
    }

    public void saveToDB(String lang, File file, OnSaveProgressListener listener) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            saveToDB(lang, reader, listener);
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    public void saveToDB(String lang, InputStream fd, OnSaveProgressListener listener) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(fd));
            saveToDB(lang, reader, listener);
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    private void saveToDB(String lang, BufferedReader reader, OnSaveProgressListener listener) throws IOException {
        isReady = false;

        String table = "LANG_" + escapeString(lang);
        StringBuilder sb = new StringBuilder();
        SQLiteDatabase db = getWritableDatabase();
        sb.append("INSERT OR IGNORE INTO ")
                .append(table)
                .append("(word)")
                .append(" VALUES ");

        int count = 0;
        for (String line; (line = reader.readLine()) != null; ) {
            line = escapeString(line.trim().toLowerCase());
            if (line.length() > 1 && !line.contains(" ") && !line.contains("/")) {
                sb.append("('").append(line).append("'),");
                count++;
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        db.execSQL(sb.toString());
        sb.setLength(0);

        if (listener != null)
            listener.onProgress(count, OnSaveProgressListener.STATE_DELETE_DUPLICATES);

        sb.setLength(0);
        sb.append("DELETE FROM ")
                .append(table)
                .append(" WHERE id NOT IN (SELECT min(id) FROM ")
                .append(table)
                .append(" GROUP BY word)");
        db.execSQL(sb.toString());

        reader.close();
        db.close();

        isReady = true;
    }

    public void increaseUsageCount(String lang, String word) {
        SQLiteDatabase db = getWritableDatabase();
        String sb = "UPDATE LANG_" + escapeString(lang) +
                " SET usage_count = usage_count +1 WHERE word = '" +
                word +
                "'";
        db.execSQL(sb);
    }

    public List<String> getQuery(String lang, String prefix) {
        return getQuery(lang, prefix, false);
    }

    private List<String> getQuery(String lang, String prefix, boolean internalCheck) {
        List<String> out = new ArrayList<String>();

        if (!isReady && !internalCheck) return out;

        try {
            SQLiteDatabase db = getReadableDatabase();

            if (lang.contains("_")) {
                lang = lang.split("_")[0].toLowerCase();
            }

            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM LANG_")
                    .append(escapeString(lang.trim().toLowerCase()));

            if (!internalCheck) {
                if (prefix == null || prefix.length() < 1)
                    return out;

                sb.append(" WHERE word")
                        .append(" LIKE ")
                        .append("'")
                        .append(escapeString(prefix))
                        .append("%'")
                        .append(" ORDER BY LENGTH(word), usage_count DESC");
            }

            sb.append(" LIMIT 20");
            Cursor cursor = db.rawQuery(sb.toString(), null);

            if (cursor.moveToFirst()) {
                do {
                    String word = cursor.getString(1);
                    out.add(word);
                } while (cursor.moveToNext());
            }

            cursor.close();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

        return out;
    }

    @Override
    public void onUpgrade(SQLiteDatabase p1, int oldVersion, int newVersion) {
        int current = oldVersion;

        // add usage_count for words
        if (current == 1) {
            List<String> tables = new ArrayList<>();
            Cursor cursor = p1.rawQuery("SELECT name FROM sqlite_master WHERE type ='table' AND name LIKE 'LANG_%'", null);
            if (cursor.moveToFirst()) {
                do {
                    String table = cursor.getString(0);
                    System.out.println("TABLE: " + table);
                    tables.add(table);
                } while (cursor.moveToNext());
            }

            isReady = false;
            for (String table : tables) {
                StringBuilder sb = new StringBuilder("ALTER TABLE ");
                sb.append(table);
                sb.append(" ADD COLUMN usage_count INTEGER DEFAULT 0");
                p1.execSQL(sb.toString());
            }
            isReady = true;
            cursor.close();
            current++;
        }
    }

    public interface OnSaveProgressListener {
        public static final int STATE_IMPORT = 0;
        public static final int STATE_DELETE_DUPLICATES = 1;

        void onProgress(int current, int state);
    }
}
