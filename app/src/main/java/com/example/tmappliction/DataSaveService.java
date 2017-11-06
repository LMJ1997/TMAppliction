package com.example.tmappliction;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import org.litepal.LitePal;

import java.util.Calendar;
import java.util.List;

import static android.app.usage.UsageStatsManager.INTERVAL_BEST;

public class DataSaveService extends Service {
    public DataSaveService() {
    }

    @Override
    public void onCreate(){
        super.onCreate();
        Intent intent = new Intent(this,MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this,0,intent,0);
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("实时数据保存")
                .setStyle(new NotificationCompat.BigTextStyle().bigText("\"数据每半个小时自动保存一下，请不要关闭app和此信息\""))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher))
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pi)
                .build();;
        startForeground(1,notification);
        LitePal.initialize(this);
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent ,int flags, int startId){

        new Thread(new Runnable() {
            @Override
            public void run() {
                List<UsageStats> list = getUsageStatistics();
                TodayFragment.saveInDB(list,getTotalTime(list));
            }
        }).start();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int halfHour = 30 * 60 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + halfHour;
        Intent i = new Intent(this, DataSaveService.class);
        PendingIntent pi = PendingIntent.getService(this, 0 , i ,0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        return super.onStartCommand(intent ,flags,startId);
    }

    public List<UsageStats> getUsageStatistics() {
        Calendar cal = Calendar.getInstance();
//getSystemService没有用activity
        UsageStatsManager mUsageStatsManager = (UsageStatsManager)getSystemService(Context.USAGE_STATS_SERVICE);
        List<UsageStats> queryUsageStats = null;
                long endTime = cal.getTimeInMillis();
                cal.add(Calendar.DAY_OF_WEEK, -1);//DAY_OF_MONTH  和 DAY_OF_WEEK 都一样
                long startTime = cal.getTimeInMillis();
                queryUsageStats = mUsageStatsManager.queryUsageStats(INTERVAL_BEST, startTime, endTime);

        for (int i = queryUsageStats.size()-1;i>=0;i--){
            if(queryUsageStats.get(i).getTotalTimeInForeground()<60000)
                queryUsageStats.remove(i);
        }
        return queryUsageStats;
    }

    public int getTotalTime(List<UsageStats> usageStatsList){
        int totalTime = 0;
        for(UsageStats usageStats: usageStatsList) {
            totalTime += usageStats.getTotalTimeInForeground() / 60000;
        }
        return  totalTime;
    }

}
