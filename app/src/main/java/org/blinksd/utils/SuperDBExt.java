package org.blinksd.utils;

import org.frknkrc44.minidb.LogProvider;
import org.frknkrc44.minidb.SuperMiniDB;

import java.io.File;

public class SuperDBExt extends SuperMiniDB {
    private OnSettingsApplyListener mOnSettingsApplyListener;

    public SuperDBExt(String dbName, File path, boolean notRead) {
        super(dbName, path, notRead);
    }

    public SuperDBExt(String dbName, File path, boolean notRead, LogProvider provider) {
        super(dbName, path, notRead, provider);
    }

    public SuperDBExt(String dbName, File path, boolean notRead, boolean useAes) {
        super(dbName, path, notRead, useAes);
    }

    public SuperDBExt(String dbName, File path, boolean notRead, boolean useAes, LogProvider provider) {
        super(dbName, path, notRead, useAes, provider);
    }

    public SuperDBExt(String dbName, File path, Runnable onDBLoadFinished) {
        super(dbName, path, onDBLoadFinished);
    }

    public SuperDBExt(String dbName, File path, Runnable onDBLoadFinished, LogProvider provider) {
        super(dbName, path, onDBLoadFinished, provider);
    }

    public SuperDBExt(String dbName, File path, Runnable onDBLoadFinished, boolean useAes) {
        super(dbName, path, onDBLoadFinished, useAes);
    }

    public SuperDBExt(String dbName, File path, Runnable onDBLoadFinished, boolean useAes, LogProvider provider) {
        super(dbName, path, onDBLoadFinished, useAes, provider);
    }

    public void setOnSettingsApplyListener(OnSettingsApplyListener listener) {
        mOnSettingsApplyListener = listener;
    }

    public void triggerApplyListeners() {
        if (mOnSettingsApplyListener != null) {
            mOnSettingsApplyListener.onSettingsApply();
        }
    }

    public interface OnSettingsApplyListener {
        void onSettingsApply();
    }
}
