package com.learnwithvel.shutdownreceiver;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

public class HomeActivity extends AppCompatActivity {

    private ApplicationService service;
    private boolean isServiceBound;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ApplicationService.ShutdownServiceBinder binder = (ApplicationService.ShutdownServiceBinder) service;
            HomeActivity.this.service = binder.getServiceInstance();

            isServiceBound = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            HomeActivity.this.service = null;
            isServiceBound = false;
        }
    };

    //    boolean startedByService = false;
//
//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        if (!hasFocus && startedByService) {
//            Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
//            sendBroadcast(closeDialog);
//            finish();
//        }
//    }
    private static int CODE_AUTHENTICATION_VERIFICATION = 241;

    private void openThroughPowerButton() {
        Intent intent = getIntent();
        if (intent != null) {
            boolean startedThroughService = intent
                    .getBooleanExtra("boot_action", false);
            if (startedThroughService) {
//                startedByService = true;
                KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
                if (km.isKeyguardSecure()) {

                    Intent i = km.createConfirmDeviceCredentialIntent("Authentication required", "password");
                    startActivityForResult(i, CODE_AUTHENTICATION_VERIFICATION);
                } else
                    Toast.makeText(this, "No any security setup done by user(pattern or password or pin or fingerprint", Toast.LENGTH_SHORT).show();

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == CODE_AUTHENTICATION_VERIFICATION) {
            Toast.makeText(this, "Success: Verified user's identity", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failure: Unable to verify user's identity", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(this, ApplicationService.class));
        openThroughPowerButton();
    }


    @Override
    protected void onStart() {
        super.onStart();
        bindLocationUpdateService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindLocationUpdateService();
    }

    private void bindLocationUpdateService() {
        bindService(new Intent(this, ApplicationService.class),
                connection, Context.BIND_AUTO_CREATE);
    }

    private void unbindLocationUpdateService() {
        if (isServiceBound) {
            unbindService(connection);
            isServiceBound = false;
        }
    }

}