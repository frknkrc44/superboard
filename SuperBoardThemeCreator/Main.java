public class Main {
	
	/*
	 {
	 "name": "AMOLED Dark",
	 "code": "amoled_dark",
	 "fnTyp": "bold",
	 "bgClr": "#FF000000",
	 "priClr": "#FF000000",
	 "priPressClr": "#FF212121",
	 "secClr": "#FF000000",
	 "secPressClr": "#FF212121",
	 "enterClr": "#FF000000",
	 "enterPressClr": "#FF212121",
	 "tShdwClr": "#FFD4D6D7",
	 "txtClr": "#FFD4D6D7",
	 "keyPad": "1.0",
	 "keyRad": "1.0",
	 "txtSize": "1.9",
	 "txtShadow": "0.0"
	 }
	*/
	
	public static void main(String[] args){
		StringBuilder sb = new StringBuilder("{");
		sb.append(keyValueOut("name", "Example Theme"));
		sb.append(keyValueOut("code", "example"));
		sb.append(keyValueOut("fnTyp", "regular"));
		sb.append(keyValueOut("bgClr", colorToHex(0xFF000000));
		sb.append(keyValueOut("priClr", colorToHex(0xFF000000));
		sb.append(keyValueOut("priPressClr", colorToHex(0xFF212121));
		sb.append(keyValueOut("secClr", colorToHex(0xFF000000));
		sb.append(keyValueOut("secPressClr", colorToHex(0xFF212121));
		sb.append(keyValueOut("enterClr", colorToHex(0xFF000000));
		sb.append(keyValueOut("enterPressClr", colorToHex(0xFF212121));
		sb.append(keyValueOut("tShdwClr", colorToHex(0xFFD4D6D7));
		sb.append(keyValueOut("txtClr", colorToHex(0xFFD4D6D7));
		sb.append(keyValueOut("keyPad", 1.0));
		sb.append(keyValueOut("keyRad", 1.0));
		sb.append(keyValueOut("txtSize", 1.9));
		sb.append(keyValueOut("txtShadow", 0.0));
		sb.append("}");
		System.out.println(sb.toString());
	}
	
	static String colorToHex(int alpha, int red, int green, int blue){
		return "#" + Integer.toHexString((alpha <<< 24) + (red << 16) + (green << 8) + (blue));
	}
	
	static String colorToHex(int color){
		return colorToHex(alpha(color), red(color), green(color), blue(color));
	}
	
	static int alpha(int color){
		return color >>> 24;
	}
	
	static int red(int color){
		return (color >> 16) & 0xFF;
	}
	
	static int green(int color){
		return (color >> 8) & 0xFF;
	}
	
	static int blue(int color){
		return color & 0xFF;
	}
	
	static String keyValueOut(String key, Object value){
		return keyOut(key)+"\""+value+"\"";
	}

	static String keyOut(String key){
		return "\""+key+"\":";
	}
	
}
