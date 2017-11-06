package com.wtz.expiredate;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.support.v4.app.NotificationCompat;

import com.wtz.expiredate.data.GoodsItem;
import com.wtz.expiredate.utils.DatabaseHelper;

import java.util.List;

public class MyService extends Service {
    private final static String TAG = MyService.class.getSimpleName();

    private NotificationManager mNotificationManager;

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();
        Notification notification = new Notification();
        startForeground(1, notification);

        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand...flags=" + flags + ", startId=" + startId);
        List<GoodsItem> list = new DatabaseHelper(this).queryList();
        int countExpired = 0;
        int countWillExpired = 0;
        for (GoodsItem item : list) {
            if (item.isExpired()) {
                countExpired++;
            } else if (item.isWillExpired()) {
                countWillExpired++;
            }
        }
        Log.d(TAG, "has expired item: " + countExpired);
        StringBuilder contentBuilder = new StringBuilder();
        if (countExpired > 0) {
            String format = getString(R.string.format_notify_expired);
            String result = String.format(format, countExpired);
            contentBuilder.append(result);
        }
        if (countWillExpired > 0) {
            String format = getString(R.string.format_notify_will_expired);
            String result = String.format(format, countWillExpired);
            contentBuilder.append(result);
        }
        if (!TextUtils.isEmpty(contentBuilder.toString())) {
            contentBuilder.append(getString(R.string.format_notify_end));
            postNotification(contentBuilder.toString());
        }
        return START_STICKY;
    }

    public void postNotification(String content) {
        Log.d(TAG, "postNotification: " + content);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        Intent intent = new Intent(this, MainActivity.class);  //需要跳转指定的页面
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        builder.setSmallIcon(R.mipmap.ic_launcher);// 设置图标
        builder.setContentTitle(getString(R.string.expire_tips));// 设置通知的标题
        builder.setContentText(content);// 设置通知的内容
        builder.setWhen(System.currentTimeMillis());// 设置通知来到的时间
        builder.setAutoCancel(true); //Setting this flag will make it so the notification is automatically canceled when the user clicks it in the panel.
        builder.setTicker("new message");// 第一次提示消失的时候显示在通知栏上的
        //Ongoing notifications do not have an 'X' close button, and are not affected by the "Clear all" button.
        builder.setOngoing(false);

        Notification notification = builder.build();
        //that should be set if the notification should not be canceled when the user clicks the Clear all button.
        //notification.flags = Notification.FLAG_NO_CLEAR;
        mNotificationManager.notify(0, notification);
    }
}
