package org.blinksd.utils;

public class SettingItem {
    public final SettingCategory category;
    public final SettingType type;
    public final String dependency;
    public final Object dependencyEnabled;

    public SettingItem(SettingCategory category, SettingType type, String dependency, Object dependencyEnabled) {
        this.category = category;
        this.type = type;
        this.dependency = dependency;
        this.dependencyEnabled = dependencyEnabled;
    }
}