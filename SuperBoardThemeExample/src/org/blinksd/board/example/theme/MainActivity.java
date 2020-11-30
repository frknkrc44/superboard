package org.blinksd.board.example.theme;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import org.blinksd.board.api.IKeyboardThemeApi;

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
            String exampleTheme = "{\"name\":\"Purpluos\",\"code\":\"purpluos\",\"fnTyp\":\"italic\",\"icnThm\":\"theme_board\",\"enterClr\":\"#FF7389D4\",\"keyPad\":\"1.0\",\"keyRad\":\"1.0\",\"txtSize\":\"1.6\",\"txtShadow\":\"0.0\"}";
            try {
                int out = api.importTheme(exampleTheme);
                System.out.println("API_IMPORT_RESULT: " + out);
                unbindService(serviceIntent);
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