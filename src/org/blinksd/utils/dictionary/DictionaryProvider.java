package org.blinksd.utils.dictionary;

import java.io.*;
import java.util.*;
import org.blinksd.*;

public class DictionaryProvider {
	private DictionaryProvider(){}
	
	public static File getDictionaryDir(){
		return new File(SuperBoardApplication.getApplication().getDataDir() + "/dictionaries");
	}
	
	public static Map<String, List<String>> getAllDictionaries(){
		if(SuperBoardApplication.isDictsReady())
			return SuperBoardApplication.getDicts();
		
		Map<String, List<String>> out = new LinkedHashMap<>();
		try {
			File dictDir = getDictionaryDir();
			
			if(!dictDir.exists()) {
				dictDir.mkdirs();
				return out;
			}
			
			File[] files = dictDir.listFiles();
			
			if(files == null || files.length < 1)
				return out;
				
			for(File file : files){
				String name = file.getName();
				name = name.substring(0, name.lastIndexOf("."));
				System.out.println(name);
				out.put(name, getDictionary(file));
			}
		} catch(Throwable t){}
		return out;
	}
	
	public static List<String> getDictionary(File file){
		List<String> out = new ArrayList<>();
		try {
			FileInputStream fis = new FileInputStream(file);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buf = new byte[4096];
			int count;
			
			while((count = fis.read(buf)) > 0){
				baos.write(buf, 0, count);
			}
			
			baos.flush();
			
			for(String line : baos.toString().split("\\n")){
				line = line.trim().toLowerCase();
				
				if(line.length() > 2 && !line.contains(" ") && !line.contains("/") && !out.contains(line)) {
					out.add(line);
				}
			}
			
			fis.close();
			baos.close();
		} catch(Throwable t){
			t.printStackTrace();
		}
		return out;
	}
	
	public static List<String> getDictForLanguage(String lang){
		if(SuperBoardApplication.isDictsReady()){
			lang = lang.split("_")[0].toLowerCase();
			List<String> out = SuperBoardApplication.getDicts().get(lang);
			if(out != null) return out;
		}
		return new ArrayList<String>();
	}
	
	public static List<String> getSuggestions(String prefix, String lang){
		List<String> temp = new ArrayList<>();
		
		if(prefix == null || prefix.length() < 1)
			return temp;
		
		int count = 0;
		for(String item : getDictForLanguage(lang)){
			if(item.startsWith(prefix.toLowerCase())){
				temp.add(item);
				
				if(++count > 10)
					break;
			}
		}
		
		return temp;
	}
}

