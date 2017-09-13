package com.friends.zingradio.widget;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.friends.zingradio.R;
import com.friends.zingradio.activity.SplashScreenActivity;
import com.friends.zingradio.media.MusicService;

public class WidgetActivity extends AppWidgetProvider
{
    public static final String TAG = WidgetActivity.class.getSimpleName();

    public static WidgetActivity Widget = null;
    public static Context context;
    public static AppWidgetManager appWidgetManager;
    public static int appWidgetIds[];
    //private MusicService mMusicService;

    /*
    private ServiceConnection mConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            Log.d(TAG, "onServiceConnected()");
            mMusicService = (MusicService)service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
        }
    };
    */
    
    @Override
    public void onEnabled(Context context)
    {
        super.onEnabled(context);
        //Intent newServiceIntent = new Intent(context, MusicService.class);
        //context.getApplicationContext().bindService(newServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDisabled(Context context)
    {
        //if(mConnection != null) context.unbindService(mConnection);
        super.onDisabled(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        Log.d(TAG, "onUpdate()");
        if (null == context) context = WidgetActivity.context;
        if (null == appWidgetManager) appWidgetManager = WidgetActivity.appWidgetManager;
        if (null == appWidgetIds) appWidgetIds = WidgetActivity.appWidgetIds;

        WidgetActivity.Widget = this;
        WidgetActivity.context = context;
        WidgetActivity.appWidgetManager = appWidgetManager;
        WidgetActivity.appWidgetIds = appWidgetIds;

        for (int i = 0; i < appWidgetIds.length; i++)
        {
            int appWidgetId = appWidgetIds[i];

            updateAppWidget(context, appWidgetManager, appWidgetId);
        }

    }

    void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId)
    {
        Intent intentPlay = new Intent(context, MusicService.class);
        intentPlay.setAction(MusicService.ACTION_TOGGLE_PLAYBACK);
        
        Intent intentBack = new Intent(context, MusicService.class);
        intentBack.setAction(MusicService.ACTION_PREV);
        
        Intent intentForward = new Intent(context, MusicService.class);
        intentForward.setAction(MusicService.ACTION_SKIP);
        
        Intent intentList = new Intent(context, SplashScreenActivity.class);

        PendingIntent pendingIntentPlay = PendingIntent.getService(context, 0, intentPlay, 0);
        PendingIntent pendingIntentBack = PendingIntent.getService(context, 0, intentBack, 0);
        PendingIntent pendingIntentForward = PendingIntent.getService(context, 0, intentForward, 0);
        PendingIntent pendingIntentList = PendingIntent.getService(context, 0, intentList, 0);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);

        remoteViews.setOnClickPendingIntent(R.id.btnWidgetPlay, pendingIntentPlay);
        remoteViews.setOnClickPendingIntent(R.id.btnWidgetBack, pendingIntentBack);
        remoteViews.setOnClickPendingIntent(R.id.btnWidgetForward, pendingIntentForward);
        remoteViews.setOnClickPendingIntent(R.id.ln_stationDetail, pendingIntentList);

     // Tell the widget manager
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        super.onReceive(context, intent);
        /*
        Log.d(TAG, "onReceive() - mMusicService=" + mMusicService);
        if(mMusicService != null)
        {
            Log.d(TAG, "update text");
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
            if(mMusicService.getStation() != null) remoteViews.setTextViewText(R.id.stationName, mMusicService.getStation().getName());
            if(mMusicService.mPlayingItem != null) remoteViews.setTextViewText(R.id.songName, mMusicService.mPlayingItem.getTitle());
        }
        */
    }
    
    /*
     * public static class ListMusic extends Service {
     * 
     * @Override public void onStart(Intent intent, int startId) {
     * Toast.makeText(context, "Click Music List", Toast.LENGTH_SHORT).show(); }
     * 
     * @Override public IBinder onBind(Intent arg0) { return null; } }
     * 
     * public static class PlayMusic extends Service {
     * 
     * @Override public void onStart(Intent intent, int startId) {
     * Toast.makeText(context, "Click Play", Toast.LENGTH_SHORT).show(); }
     * 
     * @Override public IBinder onBind(Intent arg0) { return null; } }
     * 
     * public static class BackMusic extends Service {
     * 
     * @Override public void onStart(Intent intent, int startId) {
     * Toast.makeText(context, "Click Back", Toast.LENGTH_SHORT).show(); }
     * 
     * @Override public IBinder onBind(Intent arg0) { return null; } }
     * 
     * public static class ForwardMusic extends Service {
     * 
     * @Override public void onStart(Intent intent, int startId) {
     * Toast.makeText(context, "Click Forward", Toast.LENGTH_SHORT).show(); }
     * 
     * @Override public IBinder onBind(Intent arg0) { return null; } }
     */
}