package com.seven.zion.blinknotifier;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,dialogBox.optionListener{
    ImageView onOff,infoView;
    TextView Ntype,typeOption,noOfBlinks,noOfBlinksOption,duration,durationOption,snooze,snoozeOption,activate;
    Switch showNotification;
    public static final int DRAW_OVER_OTHER_APPS = 101;
    private boolean on_off = false;
    SharedPreferences sharedPreferences;
    serviceStoppedBroadcast stoppedBroadcast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        stoppedBroadcast = new serviceStoppedBroadcast();
        registerReceiver(stoppedBroadcast,new IntentFilter("serviceStopped"));
        onOff = (ImageView) findViewById(R.id.OnOffbutton);
        Ntype = (TextView)findViewById(R.id.type);
        typeOption = (TextView)findViewById(R.id.type_option);
        noOfBlinks = (TextView)findViewById(R.id.no_of_blinks);
        noOfBlinksOption = (TextView)findViewById(R.id.blinks_option);
        duration = (TextView)findViewById(R.id.set_duration);
        durationOption = (TextView)findViewById(R.id.duration_option);
        snooze = (TextView)findViewById(R.id.snooze_text);
        snoozeOption = (TextView)findViewById(R.id.snooze_option);
        infoView = (ImageView)findViewById(R.id.info_view);
        showNotification = (Switch)findViewById(R.id.show_notification);
        activate = (TextView)findViewById(R.id.activation_text);
        sharedPreferences = getApplicationContext().getSharedPreferences("notifeye", Context.MODE_PRIVATE);
        on_off = sharedPreferences.getBoolean("Activation",false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            requestPerms();
        }
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.M ) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED)
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 101);
        }
        if(on_off)
        {
            activate.setText(R.string.activated);
            activate.setTextColor(Color.GREEN);
            onOff.setImageResource(R.drawable.buttonon3);

        }
        onOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (on_off)
                {
                  off();
                  on_off = false;
                    try {
                        sendBroadcast(new Intent("stopService").putExtra("real", false));
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                }
                else
                {
                    on();
                    on_off = true;
                    startService(new Intent(getApplicationContext(),BackgroundService.class));
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("Activation",true);
                    editor.commit();
                }
            }
        });
        Initialize();
        noOfBlinks.setOnClickListener(this);
        Ntype.setOnClickListener(this);
        duration.setOnClickListener(this);
        infoView.setOnClickListener(this);
        snooze.setOnClickListener(this);
        showNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (b) {
                    editor.putBoolean("switch",true);
                    editor.commit();
                    sendBroadcast(new Intent("notifyCast").putExtra("notifyOption",true));
                }
                else
                {
                    editor.putBoolean("switch",false);
                    editor.commit();
                    sendBroadcast(new Intent("notifyCast").putExtra("notifyOption",false));
                }
            }
        });
    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    private void Initialize() {
        showNotification.setChecked(sharedPreferences.getBoolean("switch",true));
        noOfBlinksOption.setText(sharedPreferences.getString("noOfBlinks",getString(R.string.tenB)));
        durationOption.setText(sharedPreferences.getString("duration",getString(R.string.twenty)));
        typeOption.setText(sharedPreferences.getString("notifeye","Normal"));
    }

    private void on() {

        onOff.setImageResource(R.drawable.buttonon3);
        activate.setText(R.string.activated);
        activate.setTextColor(Color.GREEN);
    }

    private void off() {
        onOff.setImageResource(R.drawable.buttonoff);
        activate.setText(R.string.tap_to_activate_notifeye);
        activate.setTextColor(Color.GRAY);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101)
        {
            if (!(grantResults[0]== PackageManager.PERMISSION_GRANTED))
            {
                Toast.makeText(getApplicationContext(),"Need Camera permission for Real time blink detection"
                        ,Toast.LENGTH_LONG).show();
               MainActivity.this.finish();
            }

        }
    }

    private void requestPerms() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permission Required").setMessage("Please grant the overlay permission for the Notifeye Guard")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, DRAW_OVER_OTHER_APPS);
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this, "Permission Needed for Notifeye Guard", Toast.LENGTH_LONG).show();
                MainActivity.this.finish();
            }
        }).create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DRAW_OVER_OTHER_APPS)
        {
            if (resultCode == RESULT_OK)
                Toast.makeText(this,"Permission Granted",Toast.LENGTH_LONG).show();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getApplicationContext()))
        {
            Toast.makeText(this,"Permission Required",Toast.LENGTH_LONG).show();
            MainActivity.this.finish();
        }
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        on_off = sharedPreferences.getBoolean("Activation",false);
        if (on_off)
            on();
        else
            off();
    }

    @Override
    protected void onPause() {
        super.onPause();
      //  on_off = sharedPreferences.getBoolean("Activation",false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag("OptionsDialog");
        if (fragment !=null)
            fragmentManager.beginTransaction().remove(fragment).commit();
        dialogBox box = new dialogBox();
        Bundle data = new Bundle();
        switch (view.getId())
        {
            case R.id.no_of_blinks:
                data.putString("Type","noOfBlinks");
                box.setArguments(data);
                box.show(fragmentManager,"OptionsDialog");
                break;

            case R.id.set_duration:
                data.putString("Type","duration");
                box.setArguments(data);
                box.show(fragmentManager,"OptionsDialog");
                break;

            case R.id.type:
                data.putString("Type","notifeye");
                box.setArguments(data);
                box.show(fragmentManager,"OptionsDialog");
                break;

            case R.id.info_view:
                startActivity(new Intent(MainActivity.this,InfoActivity.class));
                break;

            case R.id.snooze_text:
                pickTime();
                break;

        }
    }

    private void pickTime() {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY),minute = c.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this,
                TimePickerDialog.THEME_DEVICE_DEFAULT_LIGHT,new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String m;
                if (minute<=9)
                   m = "0"+String.format(Locale.getDefault(),"%d",minute);
                else
                    m =String.format(Locale.getDefault(),"%d",minute);

                String s = "Snoozing until "+ String.format(Locale.getDefault(),"%d",hourOfDay)
                        +" : "+ m;
                snoozeOption.setText(s);
                AlarmManager alarmMgr = (AlarmManager)getSystemService(ALARM_SERVICE);
            }
        },hour,minute,false);
        timePickerDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                snoozeOption.setText(getString(R.string.snooze_disabled));
            }
        });
        timePickerDialog.show();
    }

    @Override
    public void onOptionChanged(String tag) {

        Initialize();
        if (tag.equals("notifeye")|| tag.equals("noOfBlinks")|| tag.equals("duration"))
        {
            if (isMyServiceRunning(BackgroundService.class))
            {
                stopService(new Intent(MainActivity.this,BackgroundService.class));
                startService(new Intent(MainActivity.this,BackgroundService.class));
            }
        }
    }

    public class serviceStoppedBroadcast extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context context, Intent intent) {
            off();
        }
    }

}
