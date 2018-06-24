package com.seven.zion.blinknotifier;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import java.util.concurrent.TimeUnit;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
public class BackgroundService extends Service {

    private static final String CHANNEL_ID ="notifeyeChannel" ;
    int NOTIFY_ID = 200;
    cancelNotifyBroadcast cancelNotifyBroadcast;
    CountDownTimer timer,timer2;
   // ProgressBar bar;
    SmoothProgressBar bar;
    private View exerciseView;
    WindowManager windowManager;
    private Button skip;
    public  boolean notificationShow;
    public int normalBlink = 0;
    SharedPreferences sharedPreferences;
    PendingIntent broadcast;
    Notification.Builder notification;
    NotificationManagerCompat managerCompat;
    notificationCast NotificationCast;
    WindowManager.LayoutParams params;
    int PARAM;

    public BackgroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
       return  null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = getApplicationContext().getSharedPreferences("notifeye",0);
        notificationShow = sharedPreferences.getBoolean("switch",true);
        if(sharedPreferences.getString("notifeye","Normal").equals("Normal"))
            normalBlink = 0;
        else if (sharedPreferences.getString("notifeye","Normal").equals("Real Time Detection(Beta)"))
            normalBlink = 1;
        else
            normalBlink = 2;

        cancelNotifyBroadcast = new cancelNotifyBroadcast();
        NotificationCast = new notificationCast();
        exerciseView = LayoutInflater.from(this).inflate(R.layout.eye_exercise_normal,null);
        skip = (Button)exerciseView.findViewById(R.id.skipNormal);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            PARAM = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        else
            PARAM = WindowManager.LayoutParams.TYPE_PHONE;
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                PARAM,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        windowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
        registerReceiver(cancelNotifyBroadcast,new IntentFilter("stopService"));
        registerReceiver(NotificationCast,new IntentFilter("notifyCast"));
        if (notificationShow)
             showNotification();
        if(normalBlink == 2)
            showNotification();
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                windowManager.removeView(exerciseView);
                timer.cancel();
                timer2.cancel();
                timer.start();
            }
        });
        bar = (SmoothProgressBar) exerciseView.findViewById(R.id.rcBar);
        String times = sharedPreferences.getString("duration","20 Minutes");
        int givenCount = Integer.parseInt(times.substring(0,2).trim());
        timer = new CountDownTimer(1000*6,1000) {
            @Override
            public void onTick(long l) {
                long minutes = TimeUnit.MILLISECONDS.toMinutes(l) - TimeUnit.HOURS.toMinutes(
                        TimeUnit.MILLISECONDS.toHours(l));
                long seconds = TimeUnit.MILLISECONDS.toSeconds(l) - TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(l));
                    if (notificationShow && normalBlink !=2) {
                        notification.setContentText("Next Blink Notification in " + minutes + " Minute(s) " +
                                seconds + " Second(s) ");
                        managerCompat.notify(NOTIFY_ID, notification.build());
                    }
            }

            @Override
            public void onFinish() {

                if (normalBlink==0) {
                     windowManager.addView(exerciseView,params);
                    Vibrator v = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        v.vibrate(VibrationEffect.createOneShot(1000,VibrationEffect.DEFAULT_AMPLITUDE));
                    else
                        v.vibrate(1000);
                  /*  ObjectAnimator progressAnimator = ObjectAnimator.ofInt(bar, "progress", 1000, 0);
                    progressAnimator.setDuration(15000);
                    progressAnimator.setInterpolator(new LinearInterpolator());
                    progressAnimator.start();
                     <ProgressBar
        android:id="@+id/rcBar"
        style="?android:attr/progressBarStyleHorizontal"
        android:progressDrawable="@drawable/custom_progress_bar"
        android:layout_width="270dp"
        android:layout_height="10dp"
        android:max="1000"
        android:layout_marginBottom="240dp"
        android:layout_alignParentBottom="true"
        android:layout_centerInParent="true"/>*/
                    timer2.start();
                }
                else if (normalBlink==1) {
                    startActivity(new Intent(BackgroundService.this, LivePreviewActivity.class));
                    Vibrator v = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        v.vibrate(VibrationEffect.createOneShot(1000,VibrationEffect.DEFAULT_AMPLITUDE));
                    else
                        v.vibrate(1000);
                }
                else {
                    notification.setContentText(getString(R.string.normal_info));
                    managerCompat.notify(NOTIFY_ID, notification.build());
                    Vibrator v = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        v.vibrate(VibrationEffect.createOneShot(3000,VibrationEffect.DEFAULT_AMPLITUDE));
                    else
                        v.vibrate(3000);
                    timer2.start();
                }
                if (notificationShow && normalBlink != 2)
                managerCompat.cancel(NOTIFY_ID);
            }
        };
        timer.start();


         timer2 = new CountDownTimer(23000,1000) {
            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                if (normalBlink !=2) {
                    Vibrator v = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        v.vibrate(VibrationEffect.createOneShot(1000,VibrationEffect.DEFAULT_AMPLITUDE));
                    else
                        v.vibrate(1000);
                    windowManager.removeView(exerciseView);
                }
                if (normalBlink == 2)
                    managerCompat.cancel(NOTIFY_ID);
                timer.start();
            }
        };

    }

    private void showNotification() {
        createNotificationChannel();
        broadcast = PendingIntent.getBroadcast(getApplicationContext(), 0,
                new Intent("stopService").putExtra("real", false), PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Action action = new Notification.Action(R.drawable.ic_launcher_background, "STOP", broadcast);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                    .setAutoCancel(true).setSmallIcon(R.drawable.icon)
                    .addAction(action);
        }
        else
        {
            notification = new Notification.Builder(getApplicationContext())
                    .setAutoCancel(true).setSmallIcon(R.drawable.icon)
                    .addAction(action);
        }
        managerCompat = NotificationManagerCompat.from(getApplicationContext());
        if (normalBlink !=2)
        managerCompat.notify(NOTIFY_ID, notification.build());
    }
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setVibrationPattern(null);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void startAnimate() {

    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
       // Toast.makeText(getApplicationContext(),"Stopped",Toast.LENGTH_SHORT).show();
      //  stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("Task","Removed");
        timer.cancel();
        timer2.cancel();
        if (notificationShow || normalBlink == 2) {
            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(getApplicationContext());
            managerCompat.cancel(NOTIFY_ID);
        }
        try {
          //  sendBroadcast(new Intent("serviceStopped"));
            unregisterReceiver(NotificationCast);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    protected class cancelNotifyBroadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.i("Broadcast "," Cancelled");
            if (intent.getExtras().getBoolean("real"))
            {
                timer.start();
                Log.i("LiveActivity","received");
            }
            else {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("Activation",false);
                editor.commit();
                timer.cancel();
                timer2.cancel();
                try {
                    if (exerciseView != null)
                        windowManager.removeView(exerciseView);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (notificationShow || normalBlink ==2) {
                    NotificationManagerCompat managerCompat = NotificationManagerCompat.from(getApplicationContext());
                    managerCompat.cancel(NOTIFY_ID);
                }
                unregisterReceiver(cancelNotifyBroadcast);
                sendBroadcast(new Intent("serviceStopped"));
                stopSelf();
            }
        }
    }
    protected class notificationCast extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras().getBoolean("notifyOption")) {
                notificationShow = true;
                showNotification();
            }
            else {
                notificationShow = false;
                NotificationManagerCompat managerCompat = NotificationManagerCompat.from(getApplicationContext());
                managerCompat.cancel(NOTIFY_ID);
            }
        }
    }
}