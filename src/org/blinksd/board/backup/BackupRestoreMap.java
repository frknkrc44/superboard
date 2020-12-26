package org.blinksd.board.backup;

import java.util.LinkedHashMap;
import org.blinksd.board.backup.BackupOptionsSelectorLayout.BackupOptionType;

public class BackupRestoreMap extends LinkedHashMap<String,BackupOptionType> {

    public static final String BKP_TYPE_ALL = "type_all",
    BKP_TYPE_THEME = "type_theme",
    BKP_TYPE_OTHER = "type_other";

    public BackupRestoreMap() {
        put(BKP_TYPE_ALL, BackupOptionType.ALL);
        put(BKP_TYPE_THEME, BackupOptionType.THEME);
        put(BKP_TYPE_OTHER, BackupOptionType.OTHER);
    }

}