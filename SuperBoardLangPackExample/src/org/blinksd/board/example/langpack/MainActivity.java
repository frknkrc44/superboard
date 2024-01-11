package org.blinksd.board.example.langpack;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import java.util.Scanner;
import org.blinksd.board.services.IKeyboardThemeApi;

public class MainActivity extends Activity {

    private IKeyboardThemeApi api = null;
    Intent serviceIntent = new Intent()
                .setComponent(new ComponentName(
                        "org.blinksd.board",
                        "org.blinksd.board.api.KeyboardThemeService"));

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            api = IKeyboardThemeApi.Stub.asInterface(service);
            try {
                AssetManager assets = getAssets();
		        String[] items = assets.list("");
		        Scanner sc = new Scanner(assets.open(items[0]));
				String s = "";
				while(sc.hasNext()) s += sc.nextLine();
				sc.close();
                int out = api.importLangPkg(s);
                System.out.println("API_IMPORT_RESULT: " + out);
                unbindService(this);
            } catch(Throwable t) {
                throw new RuntimeException(t);
            }
            
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            api = null;
        }
    };

    @Override
    public void onCreate(Bundle bundle){
        super.onRestart();
        
        bindService(serviceIntent, conn, BIND_AUTO_CREATE);
    }

}