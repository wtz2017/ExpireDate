package com.wtz.expiredate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by WTZ on 2017/11/1.
 */

public class MyReceiver extends BroadcastReceiver {
    private final static String TAG = MyReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        // 会触发APP创建实例并启动service
        String action = intent.getAction();
        Log.d(TAG, "onReceive...action = " + action);
    }
}
