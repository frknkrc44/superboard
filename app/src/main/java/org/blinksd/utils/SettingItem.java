package org.blinksd.utils;

public class SettingItem {
    public final SettingCategory category;
    public final SettingType type;
    public final String dependency;
    public final Boolean dependencyEnabled;

    public SettingItem(SettingCategory category, SettingType type, String dependency, Boolean dependencyEnabled) {
        this.category = category;
        this.type = type;
        this.dependency = dependency;
        this.dependencyEnabled = dependencyEnabled;
    }
}