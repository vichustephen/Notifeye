package com.seven.zion.blinknotifier;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    ImageView onOff;
    TextView counts;
    public static final int DRAW_OVER_OTHER_APPS = 101;
    private boolean on_off = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        onOff = (ImageView) findViewById(R.id.OnOffbutton);
        final RotateAnimation animation = new RotateAnimation(0.0f,360.0f, Animation.RELATIVE_TO_SELF,0.5f
        ,Animation.RELATIVE_TO_SELF,0.5f);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(0);
        animation.setDuration(400);
        onOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (on_off)
                {
                    on_off = false;
                 //   onOff.startAnimation(animation);
                    onOff.setImageResource(R.drawable.buttonoff);
                }
                else
                {
                    on_off = true;
                   // onOff.startAnimation(animation);
                    onOff.setImageResource(R.drawable.buttonon3);
                   // onOff.setAnimation(null);
                }
                //startActivity(new Intent(MainActivity.this,LivePreviewActivity.class)
            //            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
             //   MainActivity.this.finish();
             //  startService(new Intent(getApplicationContext(),BackgroundService.class));
                //AlarmManager alarmManager = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);

            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
           requestPerms();
        }
    }

    private void requestPerms() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permission Required").setMessage("Please grant the overlay permission for the Eye Guard")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, DRAW_OVER_OTHER_APPS);
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this, "Permission Needed for EYE Guard", Toast.LENGTH_LONG).show();
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
        else
        {
            Toast.makeText(this,"Permission Required",Toast.LENGTH_LONG).show();
        }
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
