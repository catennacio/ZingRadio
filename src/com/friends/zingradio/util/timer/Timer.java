package com.friends.zingradio.util.timer;

import java.util.Calendar;

import com.friends.zingradio.ZingRadioApplication;
import com.friends.zingradio.activity.MainActivity;
import com.friends.zingradio.activity.SettingsActivity;
import com.friends.zingradio.media.MusicService;
import com.friends.zingradio.ui.OffTimerDialogPreference;
import com.friends.zingradio.ui.OnTimerDialogPreference;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.audiofx.BassBoost.Settings;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class Timer extends BroadcastReceiver
{
    public static final long INTERVAL = 1000 * 3;
    public static final String TAG = Timer.class.getSimpleName();    
    public Timer(){super();}
    
    @Override
    public void onReceive(Context context, Intent intent)
    {
        //SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        SharedPreferences sharedTimerOffPref = context.getSharedPreferences(SettingsActivity.SHARED_PREF_USE_TIMER_OFF, Context.MODE_MULTI_PROCESS);
        boolean useOffTimer = sharedTimerOffPref.getBoolean(SettingsActivity.SHARED_PREF_USE_TIMER_OFF_KEY, false);
        //Log.d(TAG, "useOffTimer=" + useOffTimer);
        if(useOffTimer)
        {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
            wl.acquire();
            
            int hours = sharedTimerOffPref.getInt(OffTimerDialogPreference.TIMER_OFF_HOURS_KEY, 0);
            int minutes = sharedTimerOffPref.getInt(OffTimerDialogPreference.TIMER_OFF_MINUTES_KEY, 0);

            Calendar c = Calendar.getInstance();
            int currentHours = c.get(Calendar.HOUR_OF_DAY);
            int currentMinutes = c.get(Calendar.MINUTE);
            //Log.d(TAG,"SetHours=" + hours + " SetMinutes=" + minutes + " currentHours=" + currentHours + " currentMunites=" + currentMinutes);
            if(currentHours == hours && currentMinutes == minutes)
            {
                //Toast.makeText(context, "Tat nhac hen gio", Toast.LENGTH_SHORT).show();
                ZingRadioApplication app = (ZingRadioApplication)context.getApplicationContext();
                Intent i = new Intent(MusicService.ACTION_PAUSE);
                app.startService(i);
            }

            wl.release();
        }

        /*
        SharedPreferences sharedTimerOnPref = context.getSharedPreferences(SettingsActivity.SHARED_PREF_USE_TIMER_ON, Context.MODE_MULTI_PROCESS);
        boolean useOnTimer = sharedTimerOnPref.getBoolean(SettingsActivity.SHARED_PREF_USE_TIMER_ON_KEY, false);
        //Log.d(TAG, "useOnTimer=" + useOnTimer);
        if(useOnTimer)
        {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
            wl.acquire();
            
            int hours = sharedTimerOnPref.getInt(OnTimerDialogPreference.TIMER_ON_HOURS_KEY, 0);
            int minutes = sharedTimerOnPref.getInt(OnTimerDialogPreference.TIMER_ON_MINUTES_KEY, 0);

            Calendar c = Calendar.getInstance();
            int currentHours = c.get(Calendar.HOUR_OF_DAY);
            int currentMinutes = c.get(Calendar.MINUTE);
            //Log.d(TAG,"SetHours=" + hours + " SetMinutes=" + minutes + " currentHours=" + currentHours + " currentMunites=" + currentMinutes);
            if(currentHours == hours && currentMinutes == minutes)
            {
                //Toast.makeText(context, "Tat nhac hen gio", Toast.LENGTH_SHORT).show();
                ZingRadioApplication app = (ZingRadioApplication)context.getApplicationContext();
                Intent i = new Intent(MusicService.ACTION_PLAY_ON_TIMER);
                app.startService(i);
            }

            wl.release();
        }
        */
    }

    public void setTimer(Context context)
    {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, Timer.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), INTERVAL, pi); // Millisec * Second
    }

    public void cancelTimer(Context context)
    {
        Intent intent = new Intent(context, Timer.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}
