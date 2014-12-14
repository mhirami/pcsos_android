package epusp.pcs.os.appemergencia;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootBroadcastReceiver extends BroadcastReceiver {     
    static final String ACTION = "android.intent.action.BOOT_COMPLETED";   
    @Override   
    public void onReceive(Context context, Intent intent) {   
        // BOOT_COMPLETED” start Service    
        if (intent.getAction().equals(ACTION)) {   
        	//android.os.Debug.waitForDebugger();
            //Service
            Intent serviceIntent = new Intent(context, BackgroundService.class);
            context.startService(serviceIntent);
            Log.i("Autostart", "started");
        }   
    }    
} 