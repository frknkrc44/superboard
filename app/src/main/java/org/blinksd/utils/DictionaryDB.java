package org.blinksd.utils;

import static org.blinksd.board.SuperBoardApplication.getAppDB;
import static org.blinksd.board.SuperBoardApplication.getCurrentKeyboardLanguage;
import static org.blinksd.board.SuperBoardApplication.getLanguageTypes;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public final class DictionaryDB extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "dicts.db";
    private static final int DATABASE_VERSION = 2;
    public boolean isReady = true;
    private SQLiteDatabase mReadDatabase;

    public static final int QUERY_ALGORITHM_LEN_WORD_THEN_USAGE = 0,
                            QUERY_ALGORITHM_USAGE_THEN_LEN_WORD = 1,
                            QUERY_ALGORITHM_ONLY_LEN_WORD = 2,
                            QUERY_ALGORITHM_ONLY_USAGE = 3;

    public DictionaryDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static String escapeString(String str) {
        if (str != null && !str.isEmpty()) {
            return str.replace("\\", "\\\\")
                    .replace("'", "''")
                    .replace("\0", "\\0")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\"", "\\\"")
                    .replace("\\x1a", "\\Z");
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

    public int getTableLength(String language) {
        try {
            Cursor cursor = getReadableDatabase()
                    .rawQuery("SELECT COUNT(*) FROM LANG_" + language, null);
            cursor.moveToFirst();
            int ret = cursor.getInt(0);
            cursor.close();
            return ret;
        } catch (Throwable ignored) {
            return 0;
        }
    }

    public String[] getSavedLanguageCodes() {
        List<String> returnedCodes = new ArrayList<>();
        Cursor cursor = getReadableDatabase()
                .rawQuery("SELECT name FROM sqlite_schema WHERE type ='table' AND name LIKE 'LANG_%'", null);
        cursor.moveToFirst();
        while (cursor.moveToNext()) {
            String ret = cursor.getString(0);
            returnedCodes.add(ret.substring(ret.indexOf('_')+1));
        }
        cursor.close();
        Collections.sort(returnedCodes, String::compareToIgnoreCase);
        return returnedCodes.toArray(new String[0]);
    }

    @Override
    public void onCreate(SQLiteDatabase p1) {
        // do nothing
    }

    public void onKeyboardInit() {
        SQLiteDatabase p1 = getWritableDatabase();
        List<String> types = getLanguageTypes();
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

    public void saveToDB(String file, String lang, InputStream fd, OnSaveProgressListener listener) {
        try {
            if (file.endsWith(".gz")) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(fd)));
                saveToDBGZ(reader, listener);
            } else {
                BufferedReader reader = new BufferedReader(new InputStreamReader(fd));
                saveToDBFBD(lang, reader, listener);
            }
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    private String getLanguageCode(String rawCode) {
        if (rawCode.contains("_")) {
            return rawCode.substring(0, rawCode.indexOf("_"));
        }

        return rawCode;
    }

    public void dropDatabase(String lang) {
        isReady = false;

        SQLiteDatabase db = getWritableDatabase();
        db.delete("LANG_" + escapeString(lang), null, null);

        isReady = true;
    }

    private void saveToDBGZ(BufferedReader reader, OnSaveProgressListener listener) throws IOException {
        isReady = false;

        SQLiteDatabase db = getWritableDatabase();
        StringBuilder sb = new StringBuilder();
        String table = "";
        Map<String, String> pairs = new LinkedHashMap<>();

        int count = 0;
        for (String line; (line = reader.readLine()) != null; ) {
            pairs.clear();
            String[] commaSplit = line.split(",");

            for (String item : commaSplit) {
                String[] equalSplit = item.split("=");
                if (equalSplit.length != 2) continue;
                pairs.put(equalSplit[0].trim(), equalSplit[1].trim());
            }

            if (pairs.containsKey("locale")) {
                // noinspection ConstantConditions
                table = "LANG_" + escapeString(getLanguageCode(pairs.get("locale")));

                sb.append("INSERT OR IGNORE INTO ")
                        .append(table)
                        .append("(word,usage_count)")
                        .append(" VALUES ");
            } else if(pairs.containsKey("word")) {
                sb.append("('").append(escapeString(pairs.get("word"))).append("',").append(pairs.get("f")).append("),");
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

    private void saveToDBFBD(String lang, BufferedReader reader, OnSaveProgressListener listener) throws IOException {
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
                escapeString(word) +
                "'";
        db.execSQL(sb);
    }

    @SuppressWarnings("all")
    Iterable<String> mergeDictionaries(Iterable<LinkedHashMap<String, Integer>> in) {
        LinkedHashMap<String, Integer> proc1 = new LinkedHashMap<>();

        // put every word in the same hashmap
        for (var map : in) {
            for (var key : map.keySet()) {
                proc1.put(key, map.get(key));
            }
        }

        // Create a list from elements of HashMap
        List<Map.Entry<String, Integer>> list = new LinkedList<>(proc1.entrySet());

        switch (SuperDBHelper.getIntOrDefault(SettingMap.SET_DICTIONARY_ALGORITHM)) {
            case QUERY_ALGORITHM_ONLY_LEN_WORD:
            case QUERY_ALGORITHM_USAGE_THEN_LEN_WORD:
                // Sort the list
                Collections.sort(list, new Comparator<>() {
                    public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                        return Integer.compare(o1.getKey().length(), o2.getKey().length());
                    }
                });
                break;
            case QUERY_ALGORITHM_ONLY_USAGE:
            case QUERY_ALGORITHM_LEN_WORD_THEN_USAGE:
            default:
                // Sort the list
                Collections.sort(list, new Comparator<>() {
                    public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                        return (o1.getValue()).compareTo(o2.getValue());
                    }
                });

                // Reverse the list
                Collections.reverse(list);
                break;
        }

        List<String> out = new ArrayList<>();
        for (Map.Entry<String, Integer> aa : list) {
            out.add(aa.getKey());
        }

        return out;
    }

    public Iterable<String> getQuery(String prefix) {
        if (!isReady || TextUtils.isEmpty(prefix))
            return new ArrayList<>();

        var enabledLanguageCodes = new ArrayList<String>();
        var currentLangCode = getCurrentKeyboardLanguage().language.split("_")[0];
        for (var code : getSavedLanguageCodes()) {
            if (getTableLength(code) < 1)
                continue;

            if (getAppDB().getBoolean(String.format("LANG_%s_sug", code), false) && !currentLangCode.equals(code))
                enabledLanguageCodes.add(code);
        }

        enabledLanguageCodes.add(currentLangCode);

        if (enabledLanguageCodes.size() < 2)
            return getQuery(enabledLanguageCodes.get(0), prefix);

        List<LinkedHashMap<String, Integer>> out = new ArrayList<>();

        try {
            SQLiteDatabase db = getReadableDatabase();
            final int totalWordCount = SuperDBHelper.getIntOrDefault(SettingMap.SET_DICTIONARY_LIMIT);
            final int enabledLangCount = enabledLanguageCodes.size();
            int perLanguageLimit = totalWordCount / enabledLangCount;
            int consumedWordCount = 0;

            for (int i = 0; i < enabledLanguageCodes.size(); i++) {
                String lang = enabledLanguageCodes.get(i);
                LinkedHashMap<String, Integer> langOut = new LinkedHashMap<>();
                out.add(langOut);

                if (lang.contains("_"))
                    lang = lang.split("_")[0];

                lang = escapeString(lang.trim().toLowerCase());

                StringBuilder sb = new StringBuilder();
                sb.append("SELECT * FROM LANG_")
                        .append(lang);

                sb.append(" WHERE word")
                        .append(" LIKE ")
                        .append("'")
                        .append(escapeString(prefix))
                        .append("%'");

                switch (SuperDBHelper.getIntOrDefault(SettingMap.SET_DICTIONARY_ALGORITHM)) {
                    case QUERY_ALGORITHM_ONLY_LEN_WORD:
                        sb.append(" ORDER BY LENGTH(word) ASC");
                        break;
                    case QUERY_ALGORITHM_ONLY_USAGE:
                        sb.append(" ORDER BY usage_count DESC");
                        break;
                    case QUERY_ALGORITHM_USAGE_THEN_LEN_WORD:
                        sb.append(" ORDER BY usage_count DESC, LENGTH(word) ASC");
                        break;
                    case QUERY_ALGORITHM_LEN_WORD_THEN_USAGE:
                    default:
                        sb.append(" ORDER BY LENGTH(word) ASC, usage_count DESC");
                        break;
                }

                sb.append(" LIMIT ");

                if (currentLangCode.equals(lang) && (consumedWordCount / (i + 1) != perLanguageLimit)) {
                    perLanguageLimit = (totalWordCount - consumedWordCount);
                }

                sb.append(perLanguageLimit);
                Cursor cursor = db.rawQuery(sb.toString(), null);

                if (cursor.moveToFirst()) {
                    do {
                        String word = cursor.getString(1);
                        int usageCount = cursor.getInt(2);
                        langOut.put(word, usageCount);
                        consumedWordCount++;
                    } while (cursor.moveToNext());
                }

                cursor.close();
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

        return mergeDictionaries(out);
    }

    private Iterable<String> getQuery(String lang, String prefix) {
        List<String> out = new ArrayList<>();

        if (!isReady) return out;

        if (TextUtils.isEmpty(prefix))
            return out;

        try {
            SQLiteDatabase db = getReadableDatabase();

            if (lang.contains("_")) {
                lang = lang.split("_")[0].toLowerCase();
            }

            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM LANG_")
                    .append(escapeString(lang.trim().toLowerCase()));

            sb.append(" WHERE word")
                    .append(" LIKE ")
                    .append("'")
                    .append(escapeString(prefix))
                    .append("%'");

            switch (SuperDBHelper.getIntOrDefault(SettingMap.SET_DICTIONARY_ALGORITHM)) {
                case QUERY_ALGORITHM_ONLY_LEN_WORD:
                    sb.append(" ORDER BY LENGTH(word) ASC");
                    break;
                case QUERY_ALGORITHM_ONLY_USAGE:
                    sb.append(" ORDER BY usage_count DESC");
                    break;
                case QUERY_ALGORITHM_USAGE_THEN_LEN_WORD:
                    sb.append(" ORDER BY usage_count DESC, LENGTH(word) ASC");
                    break;
                case QUERY_ALGORITHM_LEN_WORD_THEN_USAGE:
                default:
                    sb.append(" ORDER BY LENGTH(word) ASC, usage_count DESC");
                    break;
            }

            sb.append(" LIMIT ");
            sb.append(SuperDBHelper.getIntOrDefault(SettingMap.SET_DICTIONARY_LIMIT));
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

    /** @noinspection UnusedAssignment*/
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
                String sb = "ALTER TABLE " + table +
                        " ADD COLUMN usage_count INTEGER DEFAULT 0";
                p1.execSQL(sb);
            }
            isReady = true;
            cursor.close();
            current++;
        }
    }

    public interface OnSaveProgressListener {
        int STATE_IMPORT = 0;
        int STATE_DELETE_DUPLICATES = 1;

        void onProgress(int current, int state);
    }
}
