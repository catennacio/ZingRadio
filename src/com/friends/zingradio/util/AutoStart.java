package com.friends.zingradio.util;

import com.friends.zingradio.activity.SplashScreenActivity;
import com.friends.zingradio.media.MusicService;
import com.friends.zingradio.util.timer.Timer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class AutoStart extends BroadcastReceiver
{
    public static final String TAG = AutoStart.class.getSimpleName();
    
    public AutoStart()
    {
        super();
    }
    
    Timer timer = new Timer();

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
            timer.setTimer(context);
            Log.d(TAG, "BOOT_COMPLETED");
            //Toast.makeText(context, "Boot completed!!!!!!!!!", Toast.LENGTH_LONG).show();
            //Intent mainIntent = new Intent(context, SplashScreenActivity.class);
            //context.startActivity(mainIntent);
            //Intent musicService = new Intent(context, MusicService.class);
            //musicService.setAction(MusicService.ACTION_STOP);
            //context.startService(musicService);
        }
    }

}
