package com.friends.zingradio;

import android.app.Application;
import android.content.res.AssetManager;
import android.util.Log;
import android.widget.Toast;

import com.crittercism.app.Crittercism;
import com.friends.zingradio.entity.Radio;
import com.friends.zingradio.entity.User;
import com.friends.zingradio.media.MusicService;
import com.friends.zingradio.util.Constants;
import com.friends.zingradio.util.DataXmlParser;
import com.friends.zingradio.util.StationSuggestion;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import org.xmlpull.v1.XmlPullParserException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ZingRadioApplication extends Application
{
    public static String TAG = ZingRadioApplication.class.getSimpleName();
    
    public static String DATA_FILE_NAME = "data.xml";
    public static final int DATA_FILE_LOAD_ERROR = -1;
    public static final int DATA_FILE_LOAD_OK = 0;
    public static final int DATA_FILE_NOT_LOADED = 1;    
    private Radio mRadio = null;
    private User mUser;
    private MusicService mMusicService;
    
    public Radio getRadio()
    {
        return mRadio;
    }

    public void setmRadio(Radio mRadio)
    {
        this.mRadio = mRadio;
    }
    
    @Override
    public void onCreate()
    {
        super.onCreate();
        Parse.initialize(this, Constants.PARSE_APP_ID, Constants.PARSE_CLIENT_KEY);
        ParseFacebookUtils.initialize(getString(R.string.fb_app_id));
        Crittercism.init(getApplicationContext(), Constants.CRITTERCISM_APP_ID);
        //Crittercism.setUsername("dev");
    }
    
    public synchronized int loadAll()
    {
        StationSuggestion.buildSuggestedStation();
        
        int ret = DATA_FILE_NOT_LOADED;
        if(mRadio == null)
        {
            ret = loadDataXml();    
        }
        
        switch(ret)
        {
            case DATA_FILE_NOT_LOADED:
            {
                //Toast.makeText(this, "Data already loaded.", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Data already loaded. Station count=" + mRadio.getStationCount());
                break;
            }
            case DATA_FILE_LOAD_ERROR:
            {
                Log.i(TAG, "Error loading data. Please restart the app.");
                Toast.makeText(this, "Error loading data. Please restart the app.", Toast.LENGTH_LONG).show();
                break;
            }
            case DATA_FILE_LOAD_OK:
            {
                Log.i(TAG, "Data loaded. Station count=" + mRadio.getStationCount());
                //Toast.makeText(this, "Data loaded.", Toast.LENGTH_SHORT).show();
                break;
            }
        }
        
        return ret;
    }
    
    private int loadDataXml()
    {
        AssetManager assetManager = getAssets();
        
        try
        {
            InputStream in = assetManager.open(DATA_FILE_NAME);
            if(in != null)
            {
                mRadio = new Radio();
                mRadio = DataXmlParser.parseDataXml(in);
                in.close();
                //Log.i(TAG, "Load radio done - Category count=" + mRadio.getCategories().size());    
            }
            else
            {
                return DATA_FILE_LOAD_ERROR;
            }
            
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (XmlPullParserException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        return DATA_FILE_LOAD_OK;
    }

    public User getUser()
    {
        return mUser;
    }

    public void setUser(User mUser)
    {
        this.mUser = mUser;
    }

    public MusicService getMusicService()
    {
        return mMusicService;
    }

    public void setMusicService(MusicService s)
    {
        this.mMusicService = s;
    }
}
