package org.blinksd.utils.sb;

import java.util.ArrayList;
import java.util.List;

public class Language {
    public String name = "";
    public String label = "";
    public boolean enabled;
    public int enabledSdk = 1;
    public List<Integer> paddingEnabledIndexes = new ArrayList<>();
    public boolean userLanguage;
    public String author = "";
    public String language = "";
    public List<RowOptions> layout;
    public List<RowOptions> popup;
}