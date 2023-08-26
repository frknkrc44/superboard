package org.blinksd.sdb;

/*
 ----------------------------
  This DB project is started
     by Furkan Karcıoğlu
        25.08.2017 Fri
 ----------------------------
  Minimized version started
      at 23.06.2019 Sun
 ----------------------------
*/

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class SuperMiniDB {

    public static final String QUERY_RULE_STARTSWITH = "SW",
            QUERY_RULE_ENDSWITH = "EW",
            QUERY_RULE_EQUALS = "EQ",
            QUERY_RULE_CONTAINS = "CT";
    private final HashMap<String, String> hm1 = new HashMap<String, String>();
    private String su, sv, sq;
    private boolean dbRemoved = false, clean = true;
    private File folder, tf;
    private FileOutputStream os = null;

    public SuperMiniDB(String dbName, File path, boolean dontRead) {
        init(dbName, path);
        if (!dontRead) {
            read();
        }
    }

    public SuperMiniDB(String dbName, File path) {
        this(dbName, path, false);
    }

    public SuperMiniDB(String dbName, File path, String key) {
        init(dbName, path);
        readKey(key);
    }

    private void init(String dbName, File path) {
        folder = new File(path + File.separator + "superminidb" + File.separator + dbName);
        if (!folder.exists()) folder.mkdirs();
    }

    public final int getLength() {
        return hm1.size();
    }

    public final boolean isDBContainsKey(String key) {
        return hm1.containsKey(key);
    }

    public final String getString(String key, String def) {
        return isDBContainsKey(key) ? decode(hm1.get(key)) : def;
    }

    public final long getLong(String key, long def) {
        String s = getString(key, def + "");
        try {
            return s.equals(def + "") ? def : Long.parseLong(s);
        } catch (Throwable t) {
            return def;
        }
    }

    public final byte getByte(String key, byte def) {
        String s = getString(key, def + "");
        try {
            return s.equals(def + "") ? def : Byte.parseByte(s);
        } catch (Throwable t) {
            return def;
        }
    }

    public final int getInteger(String key, int def) {
        String s = getString(key, def + "");
        try {
            return s.equals(def + "") ? def : Integer.parseInt(s);
        } catch (Throwable t) {
            return def;
        }
    }

    public final float getFloat(String key, float def) {
        String s = getString(key, def + "");
        try {
            return s.equals(def + "") ? def : Float.parseFloat(s);
        } catch (Throwable t) {
            return def;
        }
    }

    public final double getDouble(String key, double def) {
        String s = getString(key, def + "");
        try {
            return s.equals(def + "") ? def : Double.parseDouble(s);
        } catch (Throwable t) {
            return def;
        }
    }

    public final boolean getBoolean(String key, boolean def) {
        String s = getString(key, def + "");
        try {
            return s.equals(def + "") ? def : Boolean.parseBoolean(s);
        } catch (Throwable t) {
            return def;
        }
    }

    public final void putString(String key, String value) {
        putString(key, value, false);
    }

    public final void putString(String key, String value, boolean permanent) {
        hm1.put(key, encode(value));
        if (permanent) {
            writeKey(key);
            readKey(key);
        }
    }

    public final void putLong(String key, long value) {
        putLong(key, value, false);
    }

    public final void putLong(String key, long value, boolean permanent) {
        putString(key, value + "", permanent);
    }

    public final void putByte(String key, byte value) {
        putByte(key, value, false);
    }

    public final void putByte(String key, byte value, boolean permanent) {
        putString(key, value + "", permanent);
    }

    public final void putInteger(String key, int value) {
        putInteger(key, value, false);
    }

    public final void putInteger(String key, int value, boolean permanent) {
        putString(key, value + "", permanent);
    }

    public final void putFloat(String key, float value) {
        putFloat(key, value, false);
    }

    public final void putFloat(String key, float value, boolean permanent) {
        putString(key, value + "", permanent);
    }

    public final void putDouble(String key, double value) {
        putDouble(key, value, false);
    }

    public final void putDouble(String key, double value, boolean permanent) {
        putString(key, value + "", permanent);
    }

    public final void putBoolean(String key, boolean value) {
        putBoolean(key, value, false);
    }

    public final void putBoolean(String key, boolean value, boolean permanent) {
        putString(key, value + "", permanent);
    }

    public final Map getDatabaseDump() {
        return hm1;
    }

    public final void putDatabaseDump(Map dump) {
        hm1.clear();
        hm1.putAll(dump);
    }

    public final void removeKeyFromDB(String key) {
        hm1.remove(key);
        tf = new File(folder + File.separator + key);
        tf.delete();
        tf = null;
    }

    public final void removeDB() {
        hm1.clear();
        removeRecursive(folder);
        dbRemoved = true;
    }

    private final void removeRecursive(File f) {
        if (f.isDirectory()) {
            if (f.listFiles().length > 0) {
                for (File g : f.listFiles()) {
                    removeRecursive(g);
                }
            }
        }
        f.delete();
    }

    public final void clearRAM() {
        clean = true;
        hm1.clear();
    }

    public final boolean isRAMClean() {
        return clean;
    }

    public final void exportToDir(File dir) {
        if (dir.isDirectory()) {
            writeAll(dir);
        } else {
            throw new RuntimeException(dir + " is not a directory");
        }
    }

    public final void refresh() {
        writeAll();
        read();
    }

    public final void onlyRead() {
        read();
    }

    public final void onlyWrite() {
        writeAll();
    }

    public final void writeKey(Object key) {
        write(key);
    }

    private void write(Object key) {
        write(folder, key);
    }

    private void write(File dir, Object key) {
        try {
            if (((String) key).length() > 0) {
                if (os != null) os.close();
                tf = new File(dir + File.separator + key);
                os = new FileOutputStream(tf);
                os.write(hm1.get(key).getBytes());
                os.flush();
            }
        } catch (Exception e) {
            // do nothing
        } finally {
            try {
                os.close();
                os = null;
            } catch (Exception e) {
            }
        }
    }

    private void writeAll() {
        writeAll(folder);
    }

    private void writeAll(File dir) {
        for (Object key : getKeys(false)) {
            write(dir, key);
        }
    }

    public final void readKey(String key) {
        try {
            parseValues(new File(folder + File.separator + key));
        } catch (FileNotFoundException e) {
        }
    }

    public final void refreshKey(String key) {
        writeKey(key);
        readKey(key);
    }

    private void read() {
        hm1.clear();
        clean = false;
        if (!dbRemoved) {
            try {
                if (folder.exists()) {
                    for (File f : folder.listFiles()) {
                        parseValues(f);
                    }
                } else folder.mkdirs();
            } catch (Exception e) {
            }
        }
    }

    private void parseValues(File f) throws FileNotFoundException {
        sq = "";
        Scanner sc = new Scanner(f);
        while (sc.hasNext()) append(0, sc.nextLine());
        hm1.put(f.getName(), sq);
        sq = "";
        sc = null;
    }

    private final String encode(String in) {
        return Crypt.encode(folder.getAbsolutePath().hashCode(), in);
    }

    private final String decode(String in) {
        return Crypt.decode(folder.getAbsolutePath().hashCode(), in);
    }

    private String append(int i, String c) {
        if (i == 0) sq += c;
        else if (i == 1) su += c;
        else sv += c;
        return i == 0 ? sq : i == 1 ? su : sv;
    }

    public Object[] getKeys(boolean descending) {
        return getKeys(false, descending);
    }

    public Object[] getKeys(boolean sort, boolean descending) {
        Set<String> s = hm1.keySet();
        if (!sort) {
            return s.toArray();
        }
        TreeSet<String> x = new TreeSet<String>(s);
        if (descending) {
            s = x.descendingSet();
            return s.toArray();
        }
        return x.toArray();
    }

    /*	DB QUERY
     *
     * 	SW=xxx key starts with
     * 	EW=xxx key ends with
     * 	EQ=xxx key equals to
     * 	CT=xxx key contains
     */
    public Map<String, String> query(String rule) {
        if (rule == null || !rule.contains("="))
            throw new RuntimeException("rule must be non-null and must contain = as delimiter");
        String[] ruleArr = rule.split("=");
        if (ruleArr.length < 2 || ruleArr[0].trim().length() < 1 || ruleArr[1].trim().length() < 1) {
            throw new RuntimeException("invalid rule");
        }
        List<String> keys = new ArrayList<String>(hm1.keySet());
        Map<String, String> out = new HashMap<String, String>();
        switch (ruleArr[0].trim()) {
            case QUERY_RULE_STARTSWITH:
                for (String key : keys) {
                    if (key.startsWith(ruleArr[1].trim())) {
                        out.put(key, getString(key, ""));
                    }
                }
                break;
            case QUERY_RULE_ENDSWITH:
                for (String key : keys) {
                    if (key.endsWith(ruleArr[1].trim())) {
                        out.put(key, getString(key, ""));
                    }
                }
                break;
            case QUERY_RULE_EQUALS:
                for (String key : keys) {
                    if (key.equals(ruleArr[1].trim())) {
                        out.put(key, getString(key, ""));
                    }
                }
                break;
            case QUERY_RULE_CONTAINS:
                for (String key : keys) {
                    if (key.contains(ruleArr[1].trim())) {
                        out.put(key, getString(key, ""));
                    }
                }
                break;
            default:
                throw new RuntimeException("invalid rule");
        }
        return out;
    }

    private static class Crypt {

        private static SecretKeySpec secretKey;
        private static byte[] keyBuf;
        private static Cipher cipher;

        public static void setKey(int key) {
            try {
                cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                MessageDigest sha = MessageDigest.getInstance("SHA-1");
                keyBuf = Integer.toString(key).getBytes("UTF-8");
                byte[] newBuf = new byte[16];
                System.arraycopy(sha.digest(keyBuf), 0, newBuf, 0, 16);
                // keyBuf = Arrays.copyOf(sha.digest(keyBuf), 16);
                secretKey = new SecretKeySpec(newBuf, "AES");
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        static String encode(int ck, String s) {
            try {
                setKey(ck);
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                return new String(Base64EXT.encode(cipher.doFinal(s.getBytes("UTF-8")), Base64EXT.DEFAULT));
            } catch (Throwable e) {
                return s;
            }
        }

        static String decode(int ck, String s) {
            try {
                setKey(ck);
                cipher.init(Cipher.DECRYPT_MODE, secretKey);
                return new String(cipher.doFinal(Base64EXT.decode(s, Base64EXT.DEFAULT)));
            } catch (Throwable e) {
                return s;
            }
        }
    }
}
