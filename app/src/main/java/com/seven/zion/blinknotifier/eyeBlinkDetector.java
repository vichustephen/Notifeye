package com.seven.zion.blinknotifier;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class eyeBlinkDetector extends Service {
    public eyeBlinkDetector() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
