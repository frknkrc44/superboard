import java.io.FileWriter;
public class CreatorBase {
    private CreatorBase() {}

    // empty main method to avoid java errors
    public static void main(String[] args){}

    private static void writeToFile(String data, String path){
        try {
            FileWriter fw = new FileWriter(path);
            fw.write(data);
            fw.flush();
            fw.close();
        } catch(Throwable t){
            t.printStackTrace();
        }
    }

    static void create(String name, String label, boolean enabled, int enabledSdk,
                       boolean midPadding, String author, String language, String[][] layout,
                       String[][] popup, int[][] keyWidth, int[][] pkc, int[][] lpkc,
                       boolean[][] rpt, boolean[][] pine, boolean[][] lpine, boolean[][] dkt){
        String out = "{";
        out += keyValueOut("name",name)+",";
        out += keyValueOut("label",label)+",";
        out += keyValueOut("enabled",enabled)+",";
        out += keyValueOut("enabledSdk",enabledSdk)+",";
        out += keyValueOut("author",author)+",";
        out += keyValueOut("language",language)+",";
        out += keyOut("layout")+arrayToString(layout,keyWidth,pkc,lpkc,rpt,pine,lpine,dkt,midPadding)+",";
        out += keyOut("popup")+arrayToString(popup);
        out += "}";
        writeToFile(out, String.format("%s.json", name));
    }

    static String arrayToString(String[][] arr){
        return arrayToString(arr,null,null,null,null,null,null,null,false);
    }

    static String arrayToString(String[][] arr,int[][] kw,int[][] pkc,int[][] lpkc, boolean[][] rpt, boolean[][] pine, boolean[][] lpine, boolean[][] dkt, boolean midPadding){
        String x = "[";
        if(arr != null){
            for(int i = 0;i < arr.length;i++){
                x += "{"+keyOut("keys")+"[";
                for(int g = 0;g < arr[i].length;g++){
                    x += keyArrayItem(arr[i][g],kw != null ? kw[i][g] : 0,
                            pkc != null ? pkc[i][g] : 0, lpkc != null ? lpkc[i][g] : 0,
                            rpt != null ? rpt[i][g] : false, pine != null ? pine[i][g] : false,
                            lpine != null ? lpine[i][g] : false, dkt != null ? dkt[i][g] : false);
                }

                x = x.substring(0, x.length() - 1) + "]";
                if (midPadding && (arr.length / 2) == i) {
                    x += "," + keyValueOut("cpad", true);
                }
                x += "},";
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