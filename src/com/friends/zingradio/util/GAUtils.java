package com.friends.zingradio.util;

import com.friends.zingradio.activity.SettingsActivity;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.ModelFields;
import com.google.analytics.tracking.android.Tracker;

import android.content.Context;

public class GAUtils
{
   //Google Analytics
    public static final String GA_EVENT_CATEGORY_TRACKUSER = "TrackUser";
    public static final String GA_EVENT_CATEGORY_SERVICE = "ServiceAction";
    public static final String GA_EVENT_CATEGORY_UI = "UIAction";
    public static final String GA_EVENT_CATEGORY_APPFLOW = "AppFlow";
    
    public static final String GA_EVENT_ACTION_TRACKUSER = "TrackUser";
    public static final String GA_EVENT_ACTION_MUSICSERVICE = "MusicService";
    public static final String GA_EVENT_ACTION_VIEW_FRAGMENT = "ViewFragment";
    public static final String GA_EVENT_ACTION_SIGNIN = "SignIn";
    
    public static final String GA_EVENT_LABEL_TRACKUSER = "ActiveUser";
    public static final String GA_EVENT_LABEL_PLAYSONG = "Play";
    public static final String GA_EVENT_LABEL_SEARCH = "Search";
    public static final String GA_EVENT_LABEL_SIGNIN = "Sign in Parse via Facebook";
    
    
    public static void writeTrackUserEvent(Context ctx)
    {
        GoogleAnalytics ga = GoogleAnalytics.getInstance(ctx);
        //Tracker gaTracker = ga.getDefaultTracker();
        Tracker gaTracker = ga.getTracker(Constants.GOOGLE_ANALYTICS_TRACK_ID);
        if(gaTracker != null) gaTracker.sendEvent(GA_EVENT_CATEGORY_TRACKUSER, GA_EVENT_ACTION_TRACKUSER, GA_EVENT_LABEL_TRACKUSER, null);
    }
    
    public static void writePlayEvent(Context ctx)
    {
        GoogleAnalytics ga = GoogleAnalytics.getInstance(ctx);
        //Tracker gaTracker = ga.getDefaultTracker();
        Tracker gaTracker = ga.getTracker(Constants.GOOGLE_ANALYTICS_TRACK_ID);
        if(gaTracker != null) gaTracker.sendEvent(GA_EVENT_CATEGORY_SERVICE, GA_EVENT_ACTION_MUSICSERVICE, GA_EVENT_LABEL_PLAYSONG, null);
    }

    public static void writeSearchEvent(Context ctx)
    {
        GoogleAnalytics ga = GoogleAnalytics.getInstance(ctx);
        //Tracker gaTracker = ga.getDefaultTracker();
        Tracker gaTracker = ga.getTracker(Constants.GOOGLE_ANALYTICS_TRACK_ID);
        if(gaTracker != null) gaTracker.sendEvent(GA_EVENT_CATEGORY_UI, GA_EVENT_ACTION_VIEW_FRAGMENT, GA_EVENT_LABEL_SEARCH, null);   
    }
    
    public static void writeLoginSession(Context ctx)
    {
        GoogleAnalytics ga = GoogleAnalytics.getInstance(ctx);
        //Tracker gaTracker = ga.getDefaultTracker();
        Tracker gaTracker = ga.getTracker(Constants.GOOGLE_ANALYTICS_TRACK_ID);
        if(gaTracker != null)
        {
            gaTracker.setStartSession(true);
            gaTracker.sendEvent(GA_EVENT_CATEGORY_APPFLOW, GA_EVENT_ACTION_SIGNIN, GA_EVENT_LABEL_SIGNIN, null);    
        }
    }
    
    public static void writeViewFragement(Context ctx, String tag)
    {
        GoogleAnalytics ga = GoogleAnalytics.getInstance(ctx);
        Tracker gaTracker = ga.getTracker(Constants.GOOGLE_ANALYTICS_TRACK_ID);
        //Tracker gaTracker = ga.getDefaultTracker();
        if(gaTracker != null)
        {
            gaTracker.sendEvent(GA_EVENT_CATEGORY_APPFLOW, GA_EVENT_ACTION_VIEW_FRAGMENT, tag, null);
        }
    }
}
