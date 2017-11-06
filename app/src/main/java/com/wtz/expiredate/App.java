package com.wtz.expiredate;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by WTZ on 2017/11/1.
 */

public class App extends Application {
    private final static String TAG = App.class.getSimpleName();

    private static final long CHECK_INTERVAL = 6 * 3600 * 1000;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();

        startService(new Intent(this, MyService.class));

        PendingIntent mAlarmSender = PendingIntent.getService(this, 0, new Intent(this, MyService.class), PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 10 * 1000, CHECK_INTERVAL, mAlarmSender);
    }

}
