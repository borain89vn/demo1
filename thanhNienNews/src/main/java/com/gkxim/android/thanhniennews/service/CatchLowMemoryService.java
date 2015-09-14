package com.gkxim.android.thanhniennews.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.widget.Toast;

import com.gkxim.android.thanhniennews.SplashActivity;
import com.gkxim.android.thanhniennews.ThanhNienNewsApp;

public class CatchLowMemoryService extends Service {
    public CatchLowMemoryService() {
    }
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);


    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.

        boolean isStartservice= ThanhNienNewsApp.preferences.getBoolean(ThanhNienNewsApp.KEY_START_SERVICE,false);
        if(isStartservice){

            Toast.makeText(this, "Stop Service kill", Toast.LENGTH_LONG).show();

            //android.os.Process.killProcess(android.os.Process.myPid());
            // System.exit(0);
            Intent intentSplash = new Intent(getApplicationContext(), SplashActivity.class);


            intentSplash .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            intentSplash.putExtra(ThanhNienNewsApp.KEY_CATCH_LOW_MEMORY, true);
            startActivity(intentSplash);

            SharedPreferences.Editor editor =ThanhNienNewsApp.preferences.edit();
            editor.putBoolean(ThanhNienNewsApp.KEY_START_SERVICE, false);
            editor.commit();

        }else{

           Toast.makeText(this, "Service Start", Toast.LENGTH_LONG).show();

            SharedPreferences.Editor editor =ThanhNienNewsApp.preferences.edit();
            editor.putBoolean(ThanhNienNewsApp.KEY_START_SERVICE,true);
            editor.commit();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }
}
