package com.agcy.vkproject.spy;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class LongPollService extends Service {
    public LongPollService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
