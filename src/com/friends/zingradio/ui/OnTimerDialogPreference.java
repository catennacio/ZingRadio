package com.friends.zingradio.ui;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.friends.zingradio.R;
import com.friends.zingradio.activity.SettingsActivity;
import com.friends.zingradio.util.Utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TimePicker;

public class OnTimerDialogPreference extends DialogPreference implements TimePicker.OnTimeChangedListener
{
    public static final String TAG = OffTimerDialogPreference.class.getSimpleName();
    public static final String TIMER_ON_HOURS_KEY = "TIMER_ON_HOUR_KEY";
    public static final String TIMER_ON_MINUTES_KEY = "TIMER_ON_MINUTES_KEY";
    private Calendar calendar;
    private TimePicker picker = null;
    private int mSelectedHours;
    private int mSelectedMinutes;

    public OnTimerDialogPreference(Context ctx, AttributeSet attrs)
    {
        super(ctx, attrs);
        setPositiveButtonText(ctx.getString(R.string.msg_settings_timer_dialog_set));
        setNegativeButtonText(ctx.getString(R.string.msg_settings_timer_dialog_cancel));
        calendar = new GregorianCalendar();
        setDialogLayoutResource(R.layout.dialog_timer);
        
        SharedPreferences sharedPref = this.getContext().getSharedPreferences(SettingsActivity.SHARED_PREF_USE_TIMER_ON, Context.MODE_MULTI_PROCESS);
        mSelectedHours = sharedPref.getInt(TIMER_ON_HOURS_KEY, 0);
        mSelectedMinutes = sharedPref.getInt(TIMER_ON_MINUTES_KEY, 0);
        //Log.d(TAG,"useTimer=" + useTimer + " mSelectedHours=" + mSelectedHours + " mSelectedMinutes=" + mSelectedMinutes);

        calendar.set(Calendar.HOUR_OF_DAY, mSelectedHours);
        calendar.set(Calendar.MINUTE, mSelectedMinutes);
    }

    /*
    @Override
    protected View onCreateDialogView()
    {
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService (Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.dialog_timer, null);
        picker = new TimePicker(getContext());
        return picker;
    }
     */
    
    @Override
    protected void onBindDialogView(View v)
    {
        super.onBindDialogView(v);
        //Log.d(TAG,"onBindDialogView()");
        
        picker = (TimePicker)v.findViewById(R.id.timer);
        picker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
        picker.setCurrentMinute(calendar.get(Calendar.MINUTE));
        picker.setOnTimeChangedListener(this);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult)
    {
        super.onDialogClosed(positiveResult);
        //Log.d(TAG, "onDialogClosed() - positiveResult=" + positiveResult);
        if (positiveResult)
        {
            //Log.d(TAG, "Hours=" + mSelectedHours + " Minutes=" + mSelectedMinutes);
            calendar.set(Calendar.HOUR_OF_DAY, mSelectedHours);
            calendar.set(Calendar.MINUTE, mSelectedMinutes);

            setSummary(getSummary());
            if (callChangeListener(calendar.getTimeInMillis()))
            {
                persistLong(calendar.getTimeInMillis());
                notifyChanged();
            }
            
            SharedPreferences sharedPref = this.getContext().getSharedPreferences(SettingsActivity.SHARED_PREF_USE_TIMER_ON, Context.MODE_MULTI_PROCESS);

            //Log.d(TAG,"useTimer=" + useTimer);
            boolean useOnTimer = sharedPref.getBoolean(SettingsActivity.SHARED_PREF_USE_TIMER_ON_KEY, false);
            
            Editor editor = sharedPref.edit();
            editor.clear();
            editor.putBoolean(SettingsActivity.SHARED_PREF_USE_TIMER_ON_KEY, useOnTimer);
            editor.putInt(TIMER_ON_HOURS_KEY, mSelectedHours);
            editor.putInt(TIMER_ON_MINUTES_KEY, mSelectedMinutes);
            editor.commit();
            Log.d(TAG, "WRITE SetHours=" + mSelectedHours + " SetMinutes=" + mSelectedMinutes);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index)
    {
        return (a.getString(index));
    }

    @Override
    public CharSequence getSummary()
    {
        if (calendar == null)
        {
            return null;
        }
        //Log.d(TAG, "calendar hours=" + mSelectedHours + " minutes=" + mSelectedMinutes + " timeinmillis=" + calendar.getTimeInMillis());
        return DateFormat.getTimeFormat(getContext()).format(new Date(calendar.getTimeInMillis()));
    }

    @Override
    public void onTimeChanged(TimePicker arg0, int hours, int minutes)
    {
        //SharedPreferences sharedPref = this.getContext().getSharedPreferences(SettingsActivity.SHARED_PREF_USE_TIMER, Context.MODE_MULTI_PROCESS);
        //Log.d(TAG,"onTimeChanged() - useTimer=" + sharedPref.getBoolean(SettingsActivity.SHARED_PREF_USE_TIMER_KEY, false));
        //Log.d(TAG, "onTimeChanged() - minutes=" + minutes);
        mSelectedHours = hours;
        mSelectedMinutes = minutes;
    }
}
