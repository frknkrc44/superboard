package org.superdroid.db;

/*
 ----------------------------
  This DB project is started
     by Furkan Karcıoğlu
        25.08.2017 Fri
 ----------------------------
  Last Edit: 12.06.2019 Wed
 ----------------------------
*/

import java.io.*;
import java.util.*;

public class SuperDB {

	// Split characters for arrays
	private String components[] = { "_","—" };
	private String su,sv,s0[],s1[],out,sq,sr[],se,sd = "g",es = "=";
	private boolean clean = true;
	private File folder,tf;
	private int x,y;
	private Scanner sc = null;
	private FileOutputStream os = null;
	
	private Object[] hex = { '0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f' };
	private List<Object> l = Arrays.asList(hex);
	private Random r = new Random();
	
	private HashMap<String,String> hm1 = new HashMap<String,String>();

	public SuperDB(String dbName, File path){
		init(dbName, path);
		read();
	}
	
	public SuperDB(String dbName, File path, String key){
		init(dbName, path);
		readKey(key);
	}
	
	private void init(String dbName, File path){
		folder = new File(path+File.separator+"superdb"+File.separator+dbName);
		if(!folder.exists()) folder.mkdirs();
	}

	public final int getLength(){
		return hm1.size();
	}
	
	public final boolean isDBContainsKey(String key){
		if(clean) readKey(key);
		return hm1.containsKey(key);
	}
	
	public final String[][] getStringArrayArray(String key, String[][] def){
		if(isDBContainsKey(key)){
			x = 0;
			s0 = hm1.get(key).split(components[1]);
			String[][] xx = new String[s0.length][s0.length];
			for(String ss : s0){
				s1 = ss.split(components[0]);
				y = 0;
				for(String su : s1){
					s1[y] = decode(su);
					y++;
				}
				xx[x] = s1;
				x++;
			} return xx;
		} return def;
	}
	
	public final long[][] getLongArrayArray(String key, long[][] def){
		if(isDBContainsKey(key)){
			x = 0;
			s0 = hm1.get(key).split(components[1]);
			long[][] xx = new long[s0.length][s0.length];
			long[] yy = null;
			for(String ss : s0){
				s1 = ss.split(components[0]);
				y = 0;
				yy = new long[s1.length];
				for(String su : s1){
					yy[y] = Long.parseLong(decode(su));
					y++;
				}
				xx[x] = yy;
				x++;
			} return xx;
		} return def;
	}

	public final byte[][] getByteArrayArray(String key, byte[][] def){
		if(isDBContainsKey(key)){
			x = 0;
			s0 = hm1.get(key).split(components[1]);
			byte[][] xx = new byte[s0.length][s0.length];
			byte[] yy = null;
			for(String ss : s0){
				s1 = ss.split(components[0]);
				y = 0;
				yy = new byte[s1.length];
				for(String su : s1){
					yy[y] = Byte.parseByte(decode(su));
					y++;
				}
				xx[x] = yy;
				x++;
			} return xx;
		} return def;
	}
	
	public final int[][] getIntegerArrayArray(String key, int[][] def){
		if(isDBContainsKey(key)){
			x = 0;
			s0 = hm1.get(key).split(components[1]);
			int[][] xx = new int[s0.length][s0.length];
			int[] yy = null;
			for(String ss : s0){
				s1 = ss.split(components[0]);
				y = 0;
				yy = new int[s1.length];
				for(String su : s1){
					yy[y] = Integer.parseInt(decode(su));
					y++;
				}
				xx[x] = yy;
				x++;
			} return xx;
		} return def;
	}
	
	public final float[][] getFloatArrayArray(String key, float[]... def){
		if(isDBContainsKey(key)){
			x = 0;
			s0 = hm1.get(key).split(components[1]);
			float[][] xx = new float[s0.length][s0.length];
			float[] yy = null;
			for(String ss : s0){
				s1 = ss.split(components[0]);
				y = 0;
				yy = new float[s1.length];
				for(String su : s1){
					yy[y] = Float.parseFloat(decode(su));
					y++;
				}
				xx[x] = yy;
				x++;
			} return xx;
		} return def;
	}
	
	public final double[][] getDoubleArrayArray(String key, double[]... def){
		if(isDBContainsKey(key)){
			x = 0;
			s0 = hm1.get(key).split(components[1]);
			double[][] xx = new double[s0.length][s0.length];
			double[] yy = null;
			for(String ss : s1){
				s1 = ss.split(components[0]);
				y = 0;
				yy = new double[s1.length];
				for(String su : s1){
					yy[y] = Double.parseDouble(decode(su));
					y++;
				}
				xx[x] = yy;
				x++;
			} return xx;
		} return def;
	}
	
	public final boolean[][] getBooleanArrayArray(String key, boolean[]... def){
		if(isDBContainsKey(key)){
			x = 0;
			s0 = hm1.get(key).split(components[1]);
			boolean[][] xx = new boolean[s0.length][s0.length];
			boolean[] yy = {};
			for(String ss : s0){
				s1 = ss.split(components[0]);
				y = 0;
				yy = new boolean[s1.length];
				for(String su : s1){
					yy[y] = Boolean.parseBoolean(decode(su));
					y++;
				}
				xx[x] = yy;
				x++;
			}
			return xx;
		} return def;
	}
	
	public final void putStringArrayArray(String key, String[][] value){
		sv = su = "";
		for(int i = 0;i != value.length;i++){
			for(int n = 0;n != value[i].length;n++)
				append(1,encode(value[i][n])+components[0]);
			append(2,su.substring(0,su.length()-1)+components[1]);
			su = "";
		}
		sv.substring(0,sv.length()-2);
		hm1.put(key,sv);
	}
	
	public final void putLongArrayArray(String key, long[][] value){
		sv = su = "";
		for(int i = 0;i != value.length;i++){
			for(int n = 0;n != value[i].length;n++)
				append(1,encode(value[i][n]+"")+components[0]);
			append(2,su.substring(0,su.length()-1)+components[1]);
			su = "";
		}
		sv.substring(0,sv.length()-2);
		hm1.put(key,sv);
	}

	public final void putByteArrayArray(String key, byte[][] value){
		sv = su = "";
		for(int i = 0;i != value.length;i++){
			for(int n = 0;n != value[i].length;n++)
				append(1,encode(value[i][n]+"")+components[0]);
			append(2,su.substring(0,su.length()-1)+components[1]);
			su = "";
		}
		sv.substring(0,sv.length()-2);
		hm1.put(key,sv);
	}
	
	public final void putIntegerArrayArray(String key, int[][] value){
		sv = su = "";
		for(int i = 0;i != value.length;i++){
			for(int n = 0;n != value[i].length;n++)
				append(1,encode(value[i][n]+"")+components[0]);
			append(2,su.substring(0,su.length()-1)+components[1]);
			su = "";
		}
		sv.substring(0,sv.length()-2);
		hm1.put(key,sv);
	}
	
	public final void putFloatArrayArray(String key, float[][] value){
		sv = su = "";
		for(int i = 0;i != value.length;i++){
			for(int n = 0;n != value[i].length;n++)
				append(1,encode(value[i][n]+"")+components[0]);
			append(2,su.substring(0,su.length()-1)+components[1]);
			su = "";
		}
		sv.substring(0,sv.length()-2);
		hm1.put(key,sv);
	}
	
	public final void putDoubleArrayArray(String key, double[][] value){
		sv = su = "";
		for(int i = 0;i != value.length;i++){
			for(int n = 0;n != value[i].length;n++)
				append(1,encode(value[i][n]+"")+components[0]);
			append(2,su.substring(0,su.length()-1)+components[1]);
			su = "";
		}
		sv.substring(0,sv.length()-2);
		hm1.put(key,sv);
	}
	
	public final void putBooleanArrayArray(String key, boolean[][] value){
		sv = su = "";
		for(int i = 0;i != value.length;i++){
			for(int n = 0;n != value[i].length;n++)
				append(1,encode(value[i][n]+"")+components[0]);
			append(2,su.substring(0,su.length()-1)+components[1]);
			su = "";
		}
		sv.substring(0,sv.length()-2);
		hm1.put(key,sv);
	}
	
	public final String[] getStringArray(String key, String[] def){
		if(isDBContainsKey(key)){
			su = "";
			for(String p : hm1.get(key).split(components[0]))
				append(1,decode(p)+components[0]);
			return su.split(components[0]);
		} return def;
	}
	
	public final long[] getLongArray(String key, long[] def){
		if(isDBContainsKey(key)){
			int i = 0;
			su = "";
			for(String p : hm1.get(key).split(components[0]))
				append(1,decode(p)+components[0]);
			long[] num = new long[su.length()];
			for(String x : su.split(components[0])){
				num[i] = Long.parseLong(x);
				i++;
			} return num;
		} return def;
	}

	public final byte[] getByteArray(String key, byte[] def){
		if(isDBContainsKey(key)){
			int i = 0;
			su = "";
			for(String p : hm1.get(key).split(components[0]))
				append(1,decode(p)+components[0]);
			byte[] num = new byte[su.length()];
			for(String x : su.split(components[0])){
				num[i] = Byte.parseByte(x);
				i++;
			} return num;
		} return def;
	}

	public final int[] getIntegerArray(String key, int[] def){
		if(isDBContainsKey(key)){
			int i = 0;
			su = "";
			for(String p : hm1.get(key).split(components[0]))
				append(1,decode(p)+components[0]);
			int[] num = new int[su.length()];
			for(String x : su.split(components[0])){
				num[i] = Integer.parseInt(x);
				i++;
			}
			return num;
		} return def;
	}

	public final float[] getFloatArray(String key, float[] def){
		if(isDBContainsKey(key)){
			int i = 0;
			su = "";
			for(String p : hm1.get(key).split(components[0]))
				append(1,decode(p)+components[0]);
			float[] num = new float[su.length()];
			for(String x : su.split(components[0])){
				num[i] = Float.parseFloat(x);
				i++;
			} return num;
		} return def;
	}

	public final double[] getDoubleArray(String key, double[] def){
		if(isDBContainsKey(key)){
			int i = 0;
			su = "";
			for(String p : hm1.get(key).split(components[0]))
				append(1,decode(p)+components[0]);
			double[] num = new double[su.length()];
			for(String x : su.split(components[0])){
				num[i] = Double.parseDouble(x);
				i++;
			} return num;
		} return def;
	}

	public final boolean[] getBooleanArray(String key, boolean[] def){
		if(isDBContainsKey(key)){
			int i = 0;
			su = "";
			for(String p : hm1.get(key).split(components[0]))
				append(1,decode(p)+components[0]);
			boolean[] num = new boolean[su.length()];
			for(String x : su.split(components[0])){
				num[i] = Boolean.parseBoolean(x);
				i++;
			} return num;
		} return def;
	}

	public final void putStringArray(String key, String[] value){
		su = "";
		for(int i = 0;i != value.length;i++)
			append(1,encode(value[i])+components[0]);
		su.substring(0,su.length()-1);
		hm1.put(key,su);
	}
	
	public final void putLongArray(String key, long[] value){
		su = "";
		for(int i = 0;i != value.length;i++)
			append(1,encode(value[i]+"")+components[0]);
		su.substring(0,su.length()-1);
		hm1.put(key,su);
	}

	public final void putByteArray(String key, byte[] value){
		su = "";
		for(int i = 0;i != value.length;i++)
			append(1,encode(value[i]+"")+components[0]);
		su.substring(0,su.length()-1);
		hm1.put(key,su);
	}

	public final void putIntegerArray(String key, int[] value){
		su = "";
		for(int i = 0;i != value.length;i++)
			append(1,encode(value[i]+"")+components[0]);
		su.substring(0,su.length()-1);
		hm1.put(key,su);
	}

	public final void putFloatArray(String key, float[] value){
		su = "";
		for(int i = 0;i != value.length;i++)
			append(1,encode(value[i]+"")+components[0]);
		su.substring(0,su.length()-1);
		hm1.put(key,su);
	}

	public final void putDoubleArray(String key, double[] value){
		su = "";
		for(int i = 0;i != value.length;i++)
			append(1,encode(value[i]+"")+components[0]);
		su.substring(0,su.length()-1);
		hm1.put(key,su);
	}

	public final void putBooleanArray(String key, boolean[] value){
		su = "";
		for(int i = 0;i != value.length;i++)
			append(1,encode(value[i]+"")+components[0]);
		su.substring(0,su.length()-1);
		hm1.put(key,su);
	}
	
	public final String getString(String key, String def){
		String s = hm1.get(key);
		s = isDBContainsKey(key) && s.length() > 0 ? decode(s) : "";
		return s.length() > 0 ? s : def;
	}
	
	public final long getLong(String key, long def){
		String s = getString(key,"");
		return s.length() > 0 ? Long.parseLong(s) : def;
	}

	public final byte getByte(String key, byte def){
		String s = getString(key,"");
		return s.length() > 0 ? Byte.parseByte(s) : def;
	}

	public final int getInteger(String key, int def){
		String s = getString(key,"");
		return s.length() > 0 ? Integer.parseInt(s) : def;
	}

	public final float getFloat(String key, float def){
		String s = getString(key,"");
		return s.length() > 0 ? Float.parseFloat(s) : def;
	}

	public final double getDouble(String key, double def){
		String s = getString(key,"");
		return s.length() > 0 ? Double.parseDouble(s) : def;
	}

	public final boolean getBoolean(String key, boolean def){
		String s = getString(key,"");
		return s.length() > 0 ? Boolean.parseBoolean(s) : def;
	}

	public final void putString(String key, String value){
		hm1.put(key,encode(value));
	}
	
	public final void putLong(String key, long value){
		hm1.put(key,encode(value+""));
	}

	public final void putByte(String key, byte value){
		hm1.put(key,encode(value+""));
	}

	public final void putInteger(String key, int value){
		hm1.put(key,encode(value+""));
	}

	public final void putFloat(String key, float value){
		hm1.put(key,encode(value+""));
	}

	public final void putDouble(String key, double value){
		hm1.put(key,encode(value+""));
	}

	public final void putBoolean(String key, boolean value){
		hm1.put(key,encode(value+""));
	}
	
	public final void removeKeyFromDB(String key){
		hm1.remove(key);
		tf = new File(folder+File.separator+key);
		tf.delete();
		tf = null;
	}

	public final void removeDB(){
		hm1.clear();
		removeRecursive(folder);
		read();
	}
	
	private final void removeRecursive(File f){
		if(f.isDirectory()){
			if(f.listFiles().length > 0){
				for(File g : f.listFiles()){
					removeRecursive(g);
				}
			}
		}
		f.delete();
	}
	
	public final void clearRAM(){
		clean = true;
		hm1.clear();
	}
	
	public final boolean isRAMClean(){
		return clean;
	}
	
	public final void exportToDir(File dir){
		if(dir.isDirectory()){
			writeAll(dir);
		} else {
			throw new RuntimeException(dir+" is not a directory");
		}
	}

	public final void refresh(){
		writeAll();
		read();
	}
	
	public final void onlyRead(){
		read();
	}
	
	public final void onlyWrite(){
		writeAll();
	}
	
	public final void writeKey(String key){
		write(key);
	}
	
	private void write(String key){
		write(folder,key);
	}
	
	private void write(File dir, String key){
		try {
			if(key.length() > 0){
				if(os != null) os.close();
				tf = new File(dir+File.separator+key);
				os = new FileOutputStream(tf);
				os.write(hm1.get(key).getBytes());
				os.flush();
			}
		} catch(Exception e){
			// do nothing
		} finally {
			try {
				os.close();
				os = null;
			} catch(Exception e){}
		}
	}
	
	private void writeAll(){
		writeAll(folder);
	}
	
	private void writeAll(File dir){
		for(String key : getKeys()){
			write(dir,key);
		}
	}
	
	public final void readKey(String key){
		if(clean) clean = false;
		try {
			parseValues(new File(folder+File.separator+key));
		} catch(FileNotFoundException e){}
	}
	
	public final void refreshKey(String key){
		writeKey(key);
		readKey(key);
	}

	private void read(){
		hm1.clear();
		try{
			if(folder.exists()){
				for(File f : folder.listFiles()){
					parseValues(f);
				}
			} else folder.mkdirs();
		} catch(Exception e){}
	}
	
	private void parseValues(File f) throws FileNotFoundException {
		sq = "";
		sc = new Scanner(f);
		while(sc.hasNext()) append(0,sc.nextLine());
		hm1.put(f.getName(),sq);
		sq = "";
		sc = null;
	}
	
	public final String encode(String in){
		if(in.length() < 1) return in;
		out = "";
		se = sd;
		sr = in.split("");
		for(int i = (sr.length - 1);i > 0;i--){
			out += rev(Integer.toHexString(sr[i].charAt(0))) + se;
			se = se.charAt(0) < 'z' ? ""+(char)(se.charAt(0)+1) : sd;
		}
		out = rnd(out.substring(0,out.length()-1));
		out = updn(out);
		return out;
	}

	public final String decode(String in){
		in = in.toLowerCase().replaceAll(es,"");
		if(in.length() < 1) return in;
		out = sq = "";
		se = sd;
		for(int i = 0;i < in.length();i++){
			sq += (l.contains(in.charAt(i)) ? in.charAt(i)+"" : es);
		}
		sr = sq.split(es);
		for(int i = (sr.length - 1);i >= 0;i--){
			String s = rev(sr[i]);
			if(s.length() > 0){
				out += ((char)Integer.parseInt(s,16))+"";
			}
		}
		return out;
	}

	private String rnd(String s){
		return s.length() % 2 == 1 ? s+es+es : s;
	}

	private String rev(String s){
		sq = "";
		for(int i = (s.length() - 1);i >= 0;i--){
			append(0,s.charAt(i)+"");
		}
		return sq;
	}

	private String updn(String s){
		sq = "";
		for(int i = 0;i < s.length();i++){
			append(0,r.nextInt() % 2 == 0 
				? (s.charAt(i)+"").toUpperCase() 
				: (s.charAt(i)+"").toLowerCase());
		}
		return sq;
	}

	private String append(int i, String c){
		if(i == 0) sq += c;
		else if(i == 1) su += c;
		else sv += c;
		return i == 0 ? sq : i == 1 ? su : sv;
	}
	
	public final Set<String> getKeys(){
		return getKeys(false,false);
	}
	
	public final Set<String> getKeys(boolean descending){
		return getKeys(true,descending);
	}
	
	public final Set<String> getKeys(boolean sort, boolean descending){
		Set<String> s = hm1.keySet();
		if(sort){
			TreeSet<String> x = new TreeSet<String>(s);
			return descending ? x.descendingSet() : x;
		}
		return s;
	}
}
