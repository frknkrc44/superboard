import java.io.*;

public class MainOTK {
	public static void main(String[] args){
		String[][] layout = {
			{"1","2","3","4","5","6","7","8","9","0"},
			{"𐰱","𐰪","𐰀","𐰺","𐰼","𐱃","𐱅","𐰖","𐰘","𐰇","𐰃","𐰆","𐰯"},
			{"𐰸","𐰽","𐰾","𐰑","𐰓","𐰶","𐰍","𐰏","𐰴","𐰚","𐰞","𐰠","𐰡"},
			{"𐰔","𐰨","𐰲","𐰦","𐰉","𐰋","𐰣","𐰤","𐰢","𐰭","𐱁","𐰜"},
			{"SYM","LC",":","DEL","ENTER"}
		};
		String[][] popup = {
			{"¹①½⅓¼⅛","²②⅔","³③¾⅜","⁴④","⑤⅝","⑥","⑦⅞","⑧","⑨","⓪⊕⊖⊗⊘⊙⊚⊛⊜⊝ø"},
			{"","","","","","","","","","","","",""},
			{"","","","","","","","","","","","",""},
			{"","","","","","","","","","","",""},
			{"","","","",""},
		};
		int[][] keyWidths = {
			{0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0,0},
			{15,15,40,15,15},
		};
		int[][] pressKeyCodes = {
			{0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0,0},
			{-2,-102,0,-5,-4},
		};
		int[][] longPressKeyCodes = {
			{0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0,0},
			{-100,-101,0,0,0},
		};
		boolean[][] repeats = {
			new boolean[10],
			new boolean[13],
			new boolean[13],
			new boolean[12],
			new boolean[5]
		};
		boolean[][] pressIsNotEvents = {
			new boolean[10],
			new boolean[13],
			new boolean[13],
			new boolean[12],
			new boolean[5]
		};
		boolean[][] longPressIsNotEvents = {
			new boolean[10],
			new boolean[13],
			new boolean[13],
			new boolean[12],
			new boolean[5]
		};
		boolean[][] darkerKeyTints = {
			new boolean[10],
			new boolean[13],
			new boolean[13],
			new boolean[12],
			new boolean[5]
		};
		repeats[4][3] = true;
		for(int i = 0;i < 4;i++)
			if(i != 2)
				darkerKeyTints[4][i] = true;
		String y = create("otk","𐱅𐰇𐰼𐰜",true,26,false,"blinksd","otk_TR",layout,popup,keyWidths,pressKeyCodes,longPressKeyCodes,repeats,pressIsNotEvents,longPressIsNotEvents,darkerKeyTints);
		try {
			FileWriter fw = new FileWriter("otk.json");
			fw.write(y);
			fw.flush();
			fw.close();
		} catch(Throwable t){
			t.printStackTrace();
		}
	}
	
	static String create(String name, String label, boolean enabled, int enabledSdk, boolean midPadding, String author, String language, String[][] layout, String[][] popup, int[][] keyWidth, int[][] pkc, int[][] lpkc, boolean[][] rpt, boolean[][] pine, boolean[][] lpine, boolean[][] dkt){
		String out = "{";
		out += keyValueOut("name",name)+",";
		out += keyValueOut("label",label)+",";
		out += keyValueOut("enabled",enabled)+",";
		out += keyValueOut("enabledSdk",enabledSdk)+",";
		out += keyValueOut("midPadding",midPadding)+",";
		out += keyValueOut("author",author)+",";
		out += keyValueOut("language",language)+",";
		out += keyOut("layout")+arrayToString(layout,keyWidth,pkc,lpkc,rpt,pine,lpine,dkt)+",";
		out += keyOut("popup")+arrayToString(popup);
		out += "}";
		return out;
	}
	
	static String arrayToString(String[][] arr){
		return arrayToString(arr,null,null,null,null,null,null,null);
	}
	
	static String arrayToString(String[][] arr,int[][] kw,int[][] pkc,int[][] lpkc, boolean[][] rpt, boolean[][] pine, boolean[][] lpine, boolean[][] dkt){
		String x = "[";
		if(arr != null){
			for(int i = 0;i < arr.length;i++){
				x += "{"+keyOut("row")+"[";
				for(int g = 0;g < arr[i].length;g++){
					x += keyArrayItem(arr[i][g],kw != null ? kw[i][g] : 0, 
							pkc != null ? pkc[i][g] : 0, lpkc != null ? lpkc[i][g] : 0, 
							rpt != null ? rpt[i][g] : false, pine != null ? pine[i][g] : false,
							lpine != null ? lpine[i][g] : false, dkt != null ? dkt[i][g] : false);
				}
				x = x.substring(0,x.length()-1)+"]},";
			}
			x = x.substring(0,x.length()-1);
		}
		x += "]";
		return x;
	}
	
	static String keyArrayItem(String item, int width, int pressKeyCode, int longPressKeyCode, boolean repeat, boolean pressIsNotEvent, boolean longPressIsNotEvent, boolean darkerKeyTint){
		return "{"+keyValueOut("key",item)+(width > 0 ? ","+keyValueOut("width",width) : "")+
				(pressKeyCode != 0 ? ","+keyValueOut("pkc",pressKeyCode) : "")+
				(longPressKeyCode != 0 ? ","+keyValueOut("lpkc",longPressKeyCode) : "")+
				(repeat ? ","+keyValueOut("rep",repeat) : "")+
				(pressIsNotEvent ? ","+keyValueOut("pine",pressIsNotEvent) : "")+
				(longPressIsNotEvent ? ","+keyValueOut("lpine",longPressIsNotEvent) : "")+
				(darkerKeyTint ? ","+keyValueOut("dkt",darkerKeyTint) : "")+
				"},";
	}
	
	static String keyValueOut(String key, Object value){
		return keyOut(key)+"\""+value+"\"";
	}
	
	static String keyOut(String key){
		return "\""+key+"\":";
	}
	
}
