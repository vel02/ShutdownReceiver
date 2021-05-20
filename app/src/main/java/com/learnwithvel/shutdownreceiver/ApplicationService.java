package com.learnwithvel.shutdownreceiver;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static com.learnwithvel.shutdownreceiver.App.CHANNEL_ID;

public class ApplicationService extends Service {

    private static final String TAG = "ApplicationService";

    private final BroadcastReceiver LongPress = new BroadcastReceiver() {

        private boolean isTriggered = false;
        long count;


        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                if (count == 10 && isTriggered) {
                    isTriggered = false;
                    Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                    sendBroadcast(closeDialog);
                }
            }

            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                isTriggered = true;
                count = (System.currentTimeMillis() / 1000);
            }

        }
    };

    private final BroadcastReceiver BootReceiver = new BroadcastReceiver() {
        private final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(ACTION_BOOT)) {
                //my stuff
                Log.d("Power", "Boot Complete");

                intent = new Intent(ApplicationService.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("boot_action", true);
                startActivity(intent);
            }
        }
    };

    private final BroadcastReceiver ShutDownReceiver = new BroadcastReceiver() {
        private final String ACTION_SHUTDOWN = "android.intent.action.ACTION_SHUTDOWN";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_SHUTDOWN)) {
                //my stuff
                Log.d("Power", "Shutdown Complete");
            }
        }
    };

    private final IBinder binder = new ShutdownServiceBinder();

    public class ShutdownServiceBinder extends Binder {
        ApplicationService getServiceInstance() {
            return ApplicationService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //only required with bound services.
        return binder;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(BootReceiver, new IntentFilter(Intent.ACTION_BOOT_COMPLETED));
        registerReceiver(ShutDownReceiver, new IntentFilter(Intent.ACTION_SHUTDOWN));

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(LongPress, filter);


    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String input = intent.getStringExtra(getString(R.string.INPUT_DATA_EXTRA));

        Intent notifIntent = new Intent(this, HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notifIntent, 0);

        //Create Notification
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Application Service")
                .setContentText(input)
                .setSmallIcon(R.drawable.ic_android)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        //  Warning: Please do heavy work on the background thread. (IntentService,
        // JobIntentService, WorkerManager) are the alternative that create their
        // background thread.

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(BootReceiver);
        unregisterReceiver(ShutDownReceiver);
        super.onDestroy();
    }
}
