package com.seven.zion.blinknotifier;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class snoozeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (isMyServiceRunning(BackgroundService.class,context))
            context.stopService(new Intent(context,BackgroundService.class));

        context.startService(new Intent(context,BackgroundService.class));
        SharedPreferences sharedPreferences = context.getSharedPreferences("notifeye",0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("snoozeSettings",context.getString(R.string.snooze_disabled));
        editor.putBoolean("Activation",true);
        editor.commit();
    }
    private boolean isMyServiceRunning(Class<?> serviceClass,Context context) {
        ActivityManager manager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
