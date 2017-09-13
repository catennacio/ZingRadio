package com.friends.zingradio.util;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.friends.zingradio.ZingRadioApplication;
import com.friends.zingradio.activity.FacebookLoginActivity;
import com.friends.zingradio.activity.SettingsActivity;
import com.friends.zingradio.entity.AudioItem;
import com.friends.zingradio.entity.Radio;
import com.friends.zingradio.entity.User;
import com.friends.zingradio.entity.json.Song;
import com.friends.zingradio.ui.OffTimerDialogPreference;
import com.friends.zingradio.ui.OnTimerDialogPreference;
import com.friends.zingradio.util.download.FileDownloadManager;
import com.parse.LogInCallback;
import com.parse.ParseFacebookUtils;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.GeneralSecurityException;

public class Utilities
{

    private static final String TAG = Utilities.class.getSimpleName();

    /**
     * Function to convert milliseconds time to Timer Format
     * Hours:Minutes:Seconds
     * */
    public static String milliSecondsToTimerHour(long milliseconds)
    {
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there
        if (hours > 0)
        {
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10)
        {
            secondsString = "0" + seconds;
        }
        else
        {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }
    
    public static String milliSecondsToTimerMinute(long milliseconds)
    {
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int minutes = (int) (milliseconds / (1000 * 60 ));
        //int seconds = (int) ((milliseconds % (1000 * 60 * 60 ) / 1000));
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10)
        {
            secondsString = "0" + seconds;
        }
        else
        {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    /**
     * Function to get Progress percentage
     * 
     * @param currentDuration
     * @param totalDuration
     * */
    public static int getProgressPercentage(long currentDuration, long totalDuration)
    {
        Double percentage = (double) 0;

        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);

        // calculating percentage
        percentage = (((double) currentSeconds) / totalSeconds) * 100;

        // return percentage
        return percentage.intValue();
    }

    /**
     * Function to change progress to timer
     * 
     * @param progress
     *            -
     * @param totalDuration
     *            returns current duration in milliseconds
     * */
    public static int progressToTimer(int progress, int totalDuration)
    {
        int currentDuration = 0;
        totalDuration = (int) (totalDuration / 1000);
        currentDuration = (int) ((((double) progress) / 100) * totalDuration);

        // return current duration in milliseconds
        return currentDuration * 1000;
    }

    public static Radio getApplicationRadio(Activity act)
    {
        if (act != null)
        {
            ZingRadioApplication app = ((ZingRadioApplication) act.getApplication());
            Radio radio = app.getRadio();
            return radio;
        }
        else
            return null;
    }

    public static String getAboutHtml(Activity act)
    {
        String s = "";
        AssetManager assetManager = act.getAssets();

        try
        {
            InputStream in = assetManager.open(Constants.ABOUT_FILE_NAME);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String s1 = reader.readLine();
            while (s1 != null)
            {
                s += s1;
                s1 = reader.readLine();
            }
            reader.close();
            in.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return s;
    }

    public static boolean isNetworkOnline(Context ctx)
    {
        boolean status = false;
        try
        {
            ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getNetworkInfo(0);
            if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED)
            {
                status = true;
            }
            else
            {
                netInfo = cm.getNetworkInfo(1);
                if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) status = true;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
        return status;

    }

    public static Dialog createDialog(Context context, String title, String str)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(str).setTitle(title);
        builder.setCancelable(true);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        return dialog;
    }

    public static String checkMp3FileExists(String songId)
    {
        FilenameFilter af = new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                if (name.endsWith(".mp3") || name.endsWith(".MP3"))
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
        };
        
        File rootFile = new File(Environment.getExternalStorageDirectory(), FileDownloadManager.DOWNLOAD_DIR);
        File[] files = (rootFile).listFiles(af);
        
        /*
        for(File f1 : mFiles)
        {
            Log.d(TAG, "file=" + f1.getName());
        }
        Log.d(TAG, "count=" + mFiles.length);
         */      
        
        String ret = null;

        if (files != null && Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
        {
            for(File f : files)
            {
                String filename = f.getName();
                String path = f.getAbsolutePath();
                //Log.d(TAG, "filename=" + filename);
                String[] fileNameTokens = filename.split("\\.(?=[^\\.]+$)");//split name and extension
                String name = fileNameTokens[0];
                //Log.d(TAG, "name=" + name);
                String[] nameTokens = name.split(FileDownloadManager.ID_SEPARATOR);//split name and id with separator (___)
                //Log.d(TAG, "nameTokens count=" + nameTokens.length);
                
                if(nameTokens.length >= 2)
                {
                    String id = nameTokens[nameTokens.length -1];//get the last token as id, in case in filename there are separator too!
                    //Log.d(TAG, "id=" + id);
                    //try to split one more time in case there are many versions of the mp3
                    String[] idTokens = id.split("-");
                    String realId = "";
                    if(idTokens.length == 2)//has many versions
                    {
                        //Log.d(TAG, "token0=" + idTokens[0] + " token1=" + idTokens[1]);
                        realId = idTokens[0];//readlId is the first token
                    }
                    else if(idTokens.length == 1)//only one version
                    {
                        realId = id;
                    }
                    
                    if(realId.equals(songId))
                    {
                        //Log.d(TAG, "Found song realId=" + realId + " songID=" + songId + " name=" + filename);
                        ret = path;
                        break;
                    }
                }
            }
        }

        //Log.i(TAG, "id=" + songId + " ret=" + ret);
        return ret;
    }
    
    public static String checkMp3FileExists(String songId, File[] files)
    {
        String ret = null;

        if (files != null && Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
        {
            for(File f : files)
            {
                String filename = f.getName();
                String path = f.getAbsolutePath();
                //Log.d(TAG, "filename=" + filename);
                String[] fileNameTokens = filename.split("\\.(?=[^\\.]+$)");//split name and extension
                String name = fileNameTokens[0];
                //Log.d(TAG, "name=" + name);
                String[] nameTokens = name.split(FileDownloadManager.ID_SEPARATOR);//split name and id with separator (___)
                //Log.d(TAG, "nameTokens count=" + nameTokens.length);
                
                if(nameTokens.length >= 2)
                {
                    String id = nameTokens[nameTokens.length -1];//get the last token as id, in case in filename there are separator too!
                    //Log.d(TAG, "id=" + id);
                    //try to split one more time in case there are many versions of the mp3
                    String[] idTokens = id.split("-");
                    String realId = "";
                    if(idTokens.length == 2)//has many versions
                    {
                        //Log.d(TAG, "token0=" + idTokens[0] + " token1=" + idTokens[1]);
                        realId = idTokens[0];//readlId is the first token
                    }
                    else if(idTokens.length == 1)//only one version
                    {
                        realId = id;
                    }
                    
                    if(realId.equals(songId))
                    {
                        //Log.d(TAG, "Found song realId=" + realId + " songID=" + songId + " name=" + filename);
                        ret = path;
                        break;
                    }
                }
            }
        }

        return ret;
    }
    
    public static String computeSignature(String baseString, String keyString) throws GeneralSecurityException, UnsupportedEncodingException
    {
        SecretKeySpec keySpec = new SecretKeySpec(keyString.getBytes(), "HmacMD5");

        Mac mac = Mac.getInstance("HmacMD5");
        mac.init(keySpec);
        byte[] rawHmac = mac.doFinal(baseString.getBytes());

        return new String(Hex.encodeHex(rawHmac));
    }
    
    public static AudioItem fromSong(Song s)
    {
        AudioItem a = new AudioItem();
        a.setAlbum(s.getAlbumId());
        a.setDownloadLink(s.getLinkDownload128());
        a.setId(s.getID());
        a.setLink(s.getLink());
        a.setPerformer(s.getArtist());
        a.setPlink(s.getArtistAvatar());
        a.setSource(s.getLinkPlay128());
        a.setThumbnail(s.getArtistAvatar());
        a.setTitle(s.getTitle());
        return a;
    }
    
    public static boolean fileExistance(String fname)
    {
        boolean res = false;
        File file = new File(fname);
        if (file.exists())
        {
            res = true;
        }
        return res;
    }
    
    public static Intent getOpenFacebookIntent(Context context)
    {
        try
        {
            context.getPackageManager().getPackageInfo("com.facebook.katana", 0);
            return new Intent(Intent.ACTION_VIEW, Uri.parse("fb://profile/" + Constants.FACEBOOK_PAGE_ID));
        }
        catch (Exception e)
        {
            return new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/" + Constants.FACEBOOK_PAGE_USERNAME));
        }
    }
    
    public static void writeFBSession(Context ctx, GraphUser user, Session session)
    {
        if (user != null)
        {
            String userId = user.getId();
            String fullname = user.getName();
            String userName = user.getUsername();

            SharedPreferences sharedPref = ctx.getSharedPreferences(Constants.FACEBOOK_SESSION_PREF, Activity.MODE_PRIVATE);
            Editor editor = sharedPref.edit();
            editor.putString(FacebookLoginActivity.FACEBOOK_USER_ID_KEY, userId);
            editor.putString(FacebookLoginActivity.FACEBOOK_USER_NAME_KEY, userName);
            editor.putString(FacebookLoginActivity.FACEBOOK_USER_FULLNAME_KEY, fullname);
            editor.putString(FacebookLoginActivity.FACEBOOK_ACCESSTOKEN_KEY, session.getAccessToken());
            editor.putString(FacebookLoginActivity.FACEBOOK_ACCESSTOKENEXPIRES_KEY, session.getExpirationDate().toString());
            editor.commit();
        }
    }
    
    public static void writeFBEnabled(Context ctx, boolean enabled)
    {
        SharedPreferences sharedPref = ctx.getSharedPreferences(Constants.FACEBOOK_SESSION_PREF, Activity.MODE_PRIVATE);
        Editor editor = sharedPref.edit();
        editor.putBoolean(FacebookLoginActivity.FACEBOOK_ENABLED_KEY, enabled);
        editor.commit();
    }

    public static User getCurrentUser(Activity a)
    {
        ZingRadioApplication app = (ZingRadioApplication)a.getApplication();
        return app.getUser();
    }

    /*
    public static void setCurrentUser(Activity a, User user)
    {
        ZingRadioApplication app = (ZingRadioApplication)a.getApplication();
        app.setUser(user);
    }
*/
    public static void setCurrentUser(Activity a, User user)
    {
        ZingRadioApplication app = (ZingRadioApplication)a.getApplication();
        SharedPreferences fbPref = a.getSharedPreferences(Constants.FACEBOOK_SESSION_PREF, Activity.MODE_PRIVATE);
        user.setUserId(fbPref.getString(FacebookLoginActivity.FACEBOOK_USER_ID_KEY, null));
        user.setFullname(fbPref.getString(FacebookLoginActivity.FACEBOOK_USER_FULLNAME_KEY, null));
        user.setUsername(fbPref.getString(FacebookLoginActivity.FACEBOOK_USER_NAME_KEY, null));
        app.setUser(user);
    }

    public static void login(Activity a, Session session, LogInCallback logInCallback)
    {
        Utilities.writeFBEnabled(a, true);

        ParseFacebookUtils.logIn(a, logInCallback);
    }
    
    public static void reWriteTimerPrefValues(Editor editor, Context ctx)
    {
        /*
        SharedPreferences sharedPref = ctx.getSharedPreferences(SettingsActivity.SHARED_PREF_USE_TIMER, Context.MODE_MULTI_PROCESS);
        boolean useOffTimer = sharedPref.getBoolean(SettingsActivity.SHARED_PREF_USE_TIMER_OFF_KEY, false);
        int offHours = sharedPref.getInt(OffTimerDialogPreference.TIMER_OFF_HOURS_KEY, 0);
        int offMinutes = sharedPref.getInt(OffTimerDialogPreference.TIMER_OFF_MINUTES_KEY, 0);
        
        boolean useOnTimer = sharedPref.getBoolean(SettingsActivity.SHARED_PREF_USE_TIMER_ON_KEY, false);
        int onHours = sharedPref.getInt(OnTimerDialogPreference.TIMER_ON_HOURS_KEY, 0);
        int onMinutes = sharedPref.getInt(OnTimerDialogPreference.TIMER_ON_MINUTES_KEY, 0);
        
        //Editor editor = sharedPref.edit();
        //editor.clear();
        editor.putBoolean(SettingsActivity.SHARED_PREF_USE_TIMER_OFF_KEY, useOffTimer);
        editor.putInt(OffTimerDialogPreference.TIMER_OFF_HOURS_KEY, offHours);
        editor.putInt(OffTimerDialogPreference.TIMER_OFF_MINUTES_KEY, offMinutes);
        
        editor.putBoolean(SettingsActivity.SHARED_PREF_USE_TIMER_ON_KEY, useOnTimer);
        editor.putInt(OnTimerDialogPreference.TIMER_ON_HOURS_KEY, onHours);
        editor.putInt(OnTimerDialogPreference.TIMER_ON_MINUTES_KEY, onMinutes);
        
        //editor.apply();
         * */
    }
}
