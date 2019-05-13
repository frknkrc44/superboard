package org.blinksd.board;

import org.json.*;

public class LayoutUtils {
	
	private LayoutUtils(){}
	
	public static String[][] createFromJSON(String jsonData) throws JSONException {
		JSONArray ja = new JSONArray(jsonData),jx;
		String[][] out = new String[ja.length()][];
		for(int i = 0;i < ja.length();i++){
			jx = ja.getJSONArray(i);
			for(int g = 0;g < jx.length();g++){
				out[i][g] = jx.getString(g).trim();
			}
		}
		return out;
	}
}
