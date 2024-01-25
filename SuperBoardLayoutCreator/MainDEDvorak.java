public class MainDEDvorak {
    public static void main(String[] args){
        String[][] layout = {
                {"1","2","3","4","5","6","7","8","9","0"},
                {"/",",",".","p","y","f","g","c","r","l"},
                {"a","o","e","u","i","d","h","t","n","s"},
                {"CAPS","j","k","x","b","m","w","v","DEL"},
                {"SYM","q","SPACE","z","ENTER"}
        };
        String[][] popup = {
                {"¹①½⅓¼⅛","²②⅔","³③¾⅜","⁴④","⑤⅝","⑥","⑦⅞","⑧","⑨","⓪⊕⊖⊗⊘⊙⊚⊛⊜⊝ø"},
                {"\\\"","<",">","","","","","","",""},
                {"âɐᴀⒶäàáæåāã","","","","","","","","",""},
                {"","","","","","","","",""},
                {"","","","",""}
        };
        int[][] keyWidths = {
                {0,0,0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0,0,0},
                {15,0,0,0,0,0,0,0,15},
                {20,15,50,15,20}
        };
        int[][] pressKeyCodes = {
                {0,0,0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0,0,0},
                {-1,0,0,0,0,0,0,0,-5},
                {-2,0,62,0,-4}
        };
        int[][] longPressKeyCodes = {
                {0,0,0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0,0},
                {-100,0,0,0,-102}
        };
        boolean[][] repeats = {
                {false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,true},
                {false,false,true,false,false}
        };
        boolean[][] pressIsNotEvents = {
                {false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false}
        };
        boolean[][] longPressIsNotEvents = {
                {false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false}
        };
        boolean[][] darkerKeyTints = {
                {false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false},
                {false,false,false,false,false,false,false,false,false,false},
                {true,false,false,false,false,false,false,false,true},
                {true,true,false,true,false}
        };
        CreatorBase.create(
                "de",
                "German (Dvorak)",
                true,
                1,
                false,
                "blinksd",
                "de_DE_DVORAK",
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
