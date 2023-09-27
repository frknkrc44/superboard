public class MainTRQ {
	public static void main(String[] args){
		String[][] layout = {
			{"1","2","3","4","5","6","7","8","9","0"},
			{"q","w","e","r","t","y","u","ı","o","p","ğ","ü"},
			{"a","s","d","f","g","h","j","k","l","ş","i"},
			{"CAPS","z","x","c","v","b","n","m","ö","ç","DEL"},
			{"SYM",",","SPACE",".","ENTER"}
		};
		String[][] popup = {
			{"¹①½⅓¼⅛","²②⅔","³③¾⅜","⁴④","⑤⅝","⑥","⑦⅞","⑧","⑨","⓪⊕⊖⊗⊘⊙⊚⊛⊜⊝ø"},
			{"bǫⓆ","ʍᴡⓌ","ǝᴇⒺèéëēėęê","ɹʀⓇ","ʇᴛⓉ","ʎʏⓎý","nᴜⓊūùúû","ɪⒾīìíïîį","Ⓞōõóòœô","dᴘⓅ","ƃɢⒼ","nᴜⓊūùúû"},
			{"âɐᴀⒶäàáæåāã","Ⓢßśš","pᴅⒹ","ɟꜰⒻ","ƃɢⒼ","ɥʜⒽ","ɾᴊⒿ","ʞᴋⓀ","ʟⓁ","Ⓢßśš","ɪⒾīìíïîį"},
			{"","Ⓩž","Ⓧ","ɔⒸćč","^Ⓥ","qʙⒷ","uɴⓃñň","ɯᴍⓂ","Ⓞōõóòœô","ɔⒸćč",""},
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
		CreatorBase.create(
				"trq",
				"Türkçe Q",
				true,
				8,
				true,
				"blinksd",
				"tr_TR_Q",
				layout,
				popup,
				keyWidths,
				pressKeyCodes,
				longPressKeyCodes,
				repeats,
				pressIsNotEvents,
				longPressIsNotEvents,
				darkerKeyTints
		);
	}
}
