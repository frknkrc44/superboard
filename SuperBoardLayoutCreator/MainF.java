import java.io.*;

public class MainF {
	public static void main(String[] args){
		String[][] layout = {
			{"1","2","3","4","5","6","7","8","9","0"},
			{"f","g","ğ","ı","o","d","r","n","h","p","q","w"},
			{"u","i","e","a","ü","t","k","m","l","y","ş"},
			{"CAPS","j","ö","v","c","ç","z","s","b","x","DEL"},
			{"SYM",",","SPACE",".","ENTER"}
		};
		String[][] popup = {
			{"①¹½⅓¼⅛","②²⅔","③³¾⅜","④⁴","⑤⅝","⑥","⑦⅞","⑧","⑨","⓪⊕⊖⊗⊘⊙⊚⊛⊜⊝ø"},
			{"ɟꜰⒻ","ƃɢⒼ","ƃɢⒼ","ɪⒾīìíïîį","Ⓞōõóòœô","pᴅⒹ","ɹʀⓇ","uɴⓃñň","ɥʜⒽ","dᴘⓅ","bǫⓆ","ʍᴡⓌ"},
			{"nᴜⓊūùúû","ɪⒾīìíïîį","ǝᴇⒺèéëēėęê","ɐᴀⒶâäàáæåāã","nᴜⓊūùúû","ʇᴛⓉ","ʞᴋⓀ","ɯᴍⓂ","ʟⓁ","ʎʏⓎý","Ⓢßśš"},
			{"","ɾᴊⒿ","Ⓞōõóòœô","^Ⓥ","ɔⒸćč","ɔⒸćč","Ⓩž","Ⓢßśš","qʙⒷ","Ⓧ",""},
			{"","","","?!*&@/\\\\:;-+=",""}
		};
		int[][] keyWidths = {
			{0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0},
			{15,0,0,0,0,0,0,0,0,0,15},
			{20,15,50,15,20}
		};
		int[][] pressKeyCodes = {
			{0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0},
			{-1,0,0,0,0,0,0,0,0,0,-5},
			{-2,0,62,0,-4}
		};
		int[][] longPressKeyCodes = {
			{0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0},
			{-100,9,0,0,-102}
		};
		boolean[][] repeats = {
			{false,false,false,false,false,false,false,false,false,false},
			{false,false,false,false,false,false,false,false,false,false,false,false},
			{false,false,false,false,false,false,false,false,false,false,false},
			{false,false,false,false,false,false,false,false,false,false,true},
			{false,false,true,false,false}
		};
		boolean[][] pressIsNotEvents = {
			{false,false,false,false,false,false,false,false,false,false},
			{false,false,false,false,false,false,false,false,false,false,false,false},
			{false,false,false,false,false,false,false,false,false,false,false},
			{false,false,false,false,false,false,false,false,false,false,false},
			{false,false,false,false,false}
		};
		boolean[][] longPressIsNotEvents = {
			{false,false,false,false,false,false,false,false,false,false},
			{false,false,false,false,false,false,false,false,false,false,false,false},
			{false,false,false,false,false,false,false,false,false,false,false},
			{false,false,false,false,false,false,false,false,false,false,false},
			{false,true,false,false,false}
		};
		boolean[][] darkerKeyTints = {
			{false,false,false,false,false,false,false,false,false,false},
			{false,false,false,false,false,false,false,false,false,false,false,false},
			{false,false,false,false,false,false,false,false,false,false,false},
			{true,false,false,false,false,false,false,false,false,false,true},
			{true,true,false,true,false}
		};
		String y = create("trf","Türkçe F",true,8,true,"blinksd","tr_TR_F",layout,popup,keyWidths,pressKeyCodes,longPressKeyCodes,repeats,pressIsNotEvents,longPressIsNotEvents,darkerKeyTints);
		try {
			FileWriter fw = new FileWriter("trf.json");
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
