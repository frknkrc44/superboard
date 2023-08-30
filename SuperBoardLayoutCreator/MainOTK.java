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

		CreatorBase.create(
				"otk",
				"𐱅𐰇𐰼𐰜",
				true,
				26,
				false,
				"blinksd",
				"otk_TR",
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
