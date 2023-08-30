public class MainTRF {
	public static void main(String[] args){
		String[][] layout = {
			{"1","2","3","4","5","6","7","8","9","0"},
			{"f","g","ğ","ı","o","d","r","n","h","p","q","w"},
			{"u","i","e","a","ü","t","k","m","l","y","ş"},
			{"CAPS","j","ö","v","c","ç","z","s","b","x","DEL"},
			{"SYM",",","SPACE",".","ENTER"}
		};
		String[][] popup = {
			{"¹①½⅓¼⅛","²②⅔","³③¾⅜","⁴④","⑤⅝","⑥","⑦⅞","⑧","⑨","⓪⊕⊖⊗⊘⊙⊚⊛⊜⊝ø"},
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
		CreatorBase.create(
				"trf",
				"Türkçe F",
				true,
				8,
				true,
				"blinksd",
				"tr_TR_F",
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
