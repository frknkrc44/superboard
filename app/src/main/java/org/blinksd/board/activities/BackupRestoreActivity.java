package org.blinksd.board.activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import org.blinksd.board.R;
import org.blinksd.board.SuperBoardApplication;
import org.blinksd.board.views.CustomRadioButton;
import org.blinksd.board.views.SettingsCategorizedListAdapter;
import org.blinksd.utils.DensityUtils;
import org.blinksd.utils.LayoutCreator;
import org.blinksd.utils.SettingMap;
import org.blinksd.utils.SuperDBHelper;
import org.blinksd.utils.ThemeUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class BackupRestoreActivity extends Activity {
    private LinearLayout main;
    private TabHost host;
    private Uri importedZipUri;
    private File dataFile;

    private final int INCLUDE_OTHER = 0x01;
    private final int INCLUDE_THEME = 0x02;
    private final int INCLUDE_ALL = INCLUDE_OTHER | INCLUDE_THEME;

    private final int
            REQUEST_RESTORE_SELECT_FILE = 1,
            REQUEST_BACKUP_SAVE_TO_DIRECTORY = 2;

    private final String
            THEME_JSON = "theme.json",
            SETTINGS_JSON = "settings.json",
            BACKGROUND_IMAGE = "bg.png";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        main = LayoutCreator.createFilledVerticalLayout(FrameLayout.class, this);

        if (Build.VERSION.SDK_INT >= 31) {
            getWindow().getDecorView().setFitsSystemWindows(true);
            main.setFitsSystemWindows(false);
            getWindow().setNavigationBarColor(0);
            getWindow().setStatusBarColor(0);
            ColorDrawable colorDrawable = new ColorDrawable(getColor(android.R.color.system_neutral1_900));
            getWindow().setBackgroundDrawable(colorDrawable);
            getActionBar().setBackgroundDrawable(colorDrawable.getConstantState().newDrawable());
        }

        try {
            createMainView();
        } catch (Throwable e) {
            Log.e("MainView", "Error:", e);
        }

        setContentView(main);
    }

    private void createMainView() {
        TabWidget widget = new TabWidget(this);
        widget.setId(android.R.id.tabs);

        host = new TabHost(this);
        host.setLayoutParams(LayoutCreator.createLayoutParams(LinearLayout.class, -1, -2));
        FrameLayout fl = new FrameLayout(this);
        fl.setLayoutParams(LayoutCreator.createLayoutParams(LinearLayout.class, -1, -1));
        fl.setId(android.R.id.tabcontent);
        LinearLayout holder = LayoutCreator.createFilledVerticalLayout(LinearLayout.class, this);
        holder.setGravity(Gravity.CENTER);
        holder.addView(widget);
        holder.addView(fl);
        host.addView(holder);
        main.addView(host);

        final String[] tabTitles = {
                "backup_menu_backup",
                "backup_menu_restore"
        };

        for (int i = 0; i < tabTitles.length; i++) {
            tabTitles[i] = SettingsCategorizedListAdapter.getTranslation(this, tabTitles[i]);
        }

        host.setup();

        for (int i = 0; i < tabTitles.length; i++) {
            TabHost.TabSpec ts = host.newTabSpec(tabTitles[i]);
            TextView tv = (TextView) getLayoutInflater().inflate(android.R.layout.simple_list_item_1, widget, false);
            LinearLayout.LayoutParams pr = (LinearLayout.LayoutParams) LayoutCreator.createLayoutParams(LinearLayout.class, -1, DensityUtils.dpInt(48));
            pr.weight = tabTitles.length;
            tv.setLayoutParams(pr);
            tv.setText(tabTitles[i]);
            tv.setBackgroundResource(R.drawable.tab_indicator_material);
            tv.getBackground().setColorFilter(0xFFDEDEDE, PorterDuff.Mode.SRC_ATOP);
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(0, 0, 0, 0);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            ts.setIndicator(tv);
            final View v = getView(i);
            ts.setContent(p1 -> v);
            host.addTab(ts);
        }
    }

    private View getView(int i) {
        switch (i) {
            case 0:
                return getBackupView();
            case 1:
                return getRestoreView();
        }
        return null;
    }

    private View getBackupView() {
        RadioGroup radioGroup = new RadioGroup(this);
        radioGroup.setId(android.R.id.selectedIcon);
        radioGroup.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        int padding = DensityUtils.dpInt(16);
        radioGroup.setPadding(padding, padding, padding, padding);

        int[] choices = {
                R.string.settings_backup_type_all,
                R.string.settings_backup_type_theme,
                R.string.settings_backup_type_other,
        };

        for (int choice : choices) {
            CustomRadioButton radioButton = new CustomRadioButton(this);
            radioButton.setId(choice);
            radioButton.setText(choice);
            radioGroup.addView(radioButton);
        }

        radioGroup.check(choices[0]);
        return radioGroup;
    }

    private View getRestoreView() {
        LinearLayout linearLayout = LayoutCreator.createFilledVerticalLayout(FrameLayout.class, this);
        linearLayout.setGravity(LinearLayout.VERTICAL);
        int padding = DensityUtils.dpInt(16);
        linearLayout.setPadding(padding, padding, padding, padding);

        Button button = LayoutCreator.createButton(this);
        button.setText(R.string.settings_select_file);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/zip");
            startActivityForResult(Intent.createChooser(intent, null), REQUEST_RESTORE_SELECT_FILE);
        });
        linearLayout.addView(button);

        return linearLayout;
    }

    private File createZipFile() throws Throwable {
        int backupMode = getSelectedBackupMode();
        long millis = System.currentTimeMillis();
        File file = new File(getExternalCacheDir(), String.format("export_%s.zip", millis));
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
        byte[] buf = new byte[4096];
        int count;

        if ((backupMode & INCLUDE_THEME) != 0) {
            // backup the current theme as json
            ZipEntry themeJsonEntry = new ZipEntry(THEME_JSON);
            zipOutputStream.putNextEntry(themeJsonEntry);

            JSONObject exportedTheme = ThemeUtils.getCurrentThemeJSON();
            byte[] data = exportedTheme.toString().getBytes();
            zipOutputStream.write(data, 0, data.length);
            zipOutputStream.closeEntry();

            // backup the current background image as file
            File bgImageFile = SuperBoardApplication.getBackgroundImageFile();
            if (bgImageFile.exists()) {
                ZipEntry bgImageEntry = new ZipEntry(BACKGROUND_IMAGE);
                zipOutputStream.putNextEntry(bgImageEntry);

                FileInputStream fileInputStream = new FileInputStream(bgImageFile);
                while ((count = fileInputStream.read(buf, 0,  buf.length)) > 0) {
                    zipOutputStream.write(buf, 0, count);
                }

                zipOutputStream.closeEntry();
            }
        }

        if ((backupMode & INCLUDE_OTHER) != 0) {
            // export all settings as json (except theme props)
            ZipEntry settingsJsonEntry = new ZipEntry(SETTINGS_JSON);
            zipOutputStream.putNextEntry(settingsJsonEntry);

            Map<String, String> settingsMap = SuperDBHelper.exportAllExceptTheme();
            JSONObject exportedSettings = new JSONObject(settingsMap);
            byte[] data = exportedSettings.toString().getBytes();
            zipOutputStream.write(data, 0, data.length);
            zipOutputStream.closeEntry();
        }

        zipOutputStream.close();
        return file;
    }

    private void createAndShareZipFile() throws Throwable {
        dataFile = createZipFile();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            startActivityForResult(intent, REQUEST_BACKUP_SAVE_TO_DIRECTORY);
        } else {
            Uri fileUri = Uri.fromFile(dataFile);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setDataAndType(fileUri, "*/*");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(Intent.EXTRA_STREAM, fileUri);
            startActivity(Intent.createChooser(intent, getString(R.string.app_name)));
        }
    }

    @SuppressLint("ResourceType")
    private int getSelectedBackupMode() {
        RadioGroup group = findViewById(android.R.id.selectedIcon);

        if (R.string.settings_backup_type_theme == group.getCheckedRadioButtonId()) {
            return INCLUDE_THEME;
        } else if (R.string.settings_backup_type_other == group.getCheckedRadioButtonId()) {
            return INCLUDE_OTHER;
        }

        return INCLUDE_ALL;
    }

    private void extractAndApplyZipFile() throws IOException, JSONException {
        InputStream inputStream = getContentResolver().openInputStream(importedZipUri);
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

        ZipEntry entry;
        byte[] buf = new byte[4096];
        int count;
        while ((entry = zipInputStream.getNextEntry()) != null) {
            switch (entry.getName()) {
                case THEME_JSON: {
                    byteStream.reset();

                    while ((count = zipInputStream.read(buf, 0, buf.length)) > 0) {
                        byteStream.write(buf, 0, count);
                    }

                    new ThemeUtils.ThemeHolder(byteStream.toString()).applyTheme();
                    break;
                }
                case BACKGROUND_IMAGE: {
                    File file = SuperBoardApplication.getBackgroundImageFile();
                    FileOutputStream fileOutputStream = new FileOutputStream(file);

                    while ((count = zipInputStream.read(buf, 0, buf.length)) > 0) {
                        fileOutputStream.write(buf, 0, count);
                    }

                    fileOutputStream.close();
                    break;
                }
                case SETTINGS_JSON: {
                    byteStream.reset();

                    while ((count = zipInputStream.read(buf, 0, buf.length)) > 0) {
                        byteStream.write(buf, 0, count);
                    }

                    SuperDBHelper.importAllFromJSON(new JSONObject(byteStream.toString()));
                    break;
                }
            }
        }

        zipInputStream.close();
        byteStream.close();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void saveToDirectory(Uri treeUri) {
        String documentId = DocumentsContract.getTreeDocumentId(treeUri);
        if (DocumentsContract.isDocumentUri(this, treeUri)) {
            documentId = DocumentsContract.getDocumentId(treeUri);
        }

        treeUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId);
        String[] dataFileNameSplit = dataFile.toString().split("/");
        String dataBaseName = dataFileNameSplit[dataFileNameSplit.length - 1];
        String dataBaseExt = dataBaseName.substring(dataBaseName.lastIndexOf('.'));
        String dataMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(dataBaseExt);

        if (dataMimeType == null) {
            dataMimeType = "application/octet-stream";
        }

        try {
            Uri documentFile = DocumentsContract.createDocument(
                    getContentResolver(), treeUri, dataMimeType, dataBaseName);

            FileInputStream fileInputStream = new FileInputStream(dataFile);
            OutputStream saveStream = getContentResolver().openOutputStream(documentFile);
            byte[] buf = new byte[4096];
            int count;

            while ((count = fileInputStream.read(buf, 0, buf.length)) > 0) {
                saveStream.write(buf, 0, count);
            }

            saveStream.flush();
            saveStream.close();
            fileInputStream.close();
        } catch (Throwable ignored) {}
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_RESTORE_SELECT_FILE && resultCode == RESULT_OK && intent.getData() != null) {
            importedZipUri = intent.getData();
        }

        if (requestCode == REQUEST_BACKUP_SAVE_TO_DIRECTORY && resultCode == RESULT_OK && intent.getData() != null) {
            saveToDirectory(intent.getData());
        }

        super.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Drawable doneIcon = getResources().getDrawable(R.drawable.sym_board_return);
        doneIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        MenuItem done = menu.add(android.R.string.ok).setIcon(doneIcon);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            done.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (host.getCurrentTab()) {
            case 0: // backup
                try {
                    createAndShareZipFile();
                } catch (Throwable ignored) {
                    return false;
                }
                break;
            case 1: // restore
                if (importedZipUri != null) {
                    try {
                        extractAndApplyZipFile();
                    } catch (Throwable ignored) {
                        return false;
                    }
                } else {
                    return false;
                }
                break;
            default:
                return false;
        }

        Toast.makeText(this, android.R.string.ok, Toast.LENGTH_SHORT).show();
        return true;
    }
}
