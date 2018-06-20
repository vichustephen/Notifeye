package com.seven.zion.blinknotifier;

import android.animation.ObjectAnimator;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;

import java.util.concurrent.TimeUnit;

public class BackgroundService extends Service {

    int NOTIFY_ID = 200;
    cancelNotifyBroadcast cancelNotifyBroadcast;
    CountDownTimer timer,timer2;
    ProgressBar bar;
    private View exerciseView;
    WindowManager windowManager;

    public BackgroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
       return  null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        cancelNotifyBroadcast = new cancelNotifyBroadcast();
        exerciseView = LayoutInflater.from(this).inflate(R.layout.eye_exercise_normal,null);
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        windowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
        registerReceiver(cancelNotifyBroadcast,new IntentFilter("stopService"));
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext()
        ,0,intent,0);
        PendingIntent broadcast = PendingIntent.getBroadcast(getApplicationContext(),0,
                new Intent("stopService"),0);
        Notification.Action action = new Notification.Action(R.drawable.ic_launcher_background,"STOP",broadcast);
        final Notification.Builder notification = new Notification.Builder(getApplicationContext())
                .setAutoCancel(true).setSmallIcon(R.drawable.ic_launcher_background)
                .addAction(action);
        final NotificationManagerCompat managerCompat = NotificationManagerCompat.from(getApplicationContext());
        managerCompat.notify(NOTIFY_ID, notification.build());

        timer = new CountDownTimer(1000*8,1000) {
            @Override
            public void onTick(long l) {
                long minutes = TimeUnit.MILLISECONDS.toMinutes(l) - TimeUnit.HOURS.toMinutes(
                        TimeUnit.MILLISECONDS.toHours(l));
                long seconds = TimeUnit.MILLISECONDS.toSeconds(l) - TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(l));
                notification.setContentText("Next Blink Notification in " + minutes + " Minute(s) " +
                        seconds + " Second(s) " );
                managerCompat.notify(NOTIFY_ID,notification.build());
            }

            @Override
            public void onFinish() {

              //  windowManager.addView(exerciseView,params);
                timer2.start();
                startAnimate();
                startActivity( new Intent(BackgroundService.this,LivePreviewActivity.class));
                managerCompat.cancel(NOTIFY_ID);
            }
        };
        timer.start();


         timer2 = new CountDownTimer(10000,1000) {
            @Override
            public void onTick(long l) {
               // progressBar.setProgress(l/1000);
            }

            @Override
            public void onFinish() {
             //   windowManager.removeView(exerciseView);
                timer.start();
            }
        };
        bar = new ProgressBar(this);

    }

    private void startAnimate() {

    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Toast.makeText(getApplicationContext(),"Stopped",Toast.LENGTH_SHORT).show();
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("Task","Removed");
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(getApplicationContext());
        managerCompat.cancel(NOTIFY_ID);
    }

    protected class cancelNotifyBroadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.i("Broadcast "," Cancelled");
            timer.cancel();
            timer2.cancel();
            try {
                if (exerciseView != null)
                    windowManager.removeView(exerciseView);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(getApplicationContext());
            managerCompat.cancel(NOTIFY_ID);
            unregisterReceiver(cancelNotifyBroadcast);
            stopSelf();
        }
    }
}