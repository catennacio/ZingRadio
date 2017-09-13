/*   
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.friends.zingradio.media;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.RemoteControlClient;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;

import com.friends.zingradio.IObservarable;
import com.friends.zingradio.R;
import com.friends.zingradio.ZingRadioApplication;
import com.friends.zingradio.activity.MainActivity;
import com.friends.zingradio.activity.SettingsActivity;
import com.friends.zingradio.async.GetStationAudioListAsync;
import com.friends.zingradio.async.GetStationAudioListAsyncComplete;
import com.friends.zingradio.async.GetZingSongDetailAsync;
import com.friends.zingradio.async.GetZingSongDetailAsyncComplete;
import com.friends.zingradio.entity.AudioItem;
import com.friends.zingradio.entity.Radio;
import com.friends.zingradio.entity.Station;
import com.friends.zingradio.entity.json.Song;
import com.friends.zingradio.fragment.PlayerFragment;
import com.friends.zingradio.fragment.SearchFragment;
import com.friends.zingradio.util.Constants;
import com.friends.zingradio.util.GAUtils;
import com.friends.zingradio.util.NetworkConnectivityListener;
import com.friends.zingradio.util.StationSuggestion;
import com.friends.zingradio.util.Utilities;
import com.friends.zingradio.util.timer.Timer;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;

/**
 * Service that handles media playback. This is the Service through which we
 * perform all the media handling in our application. Upon initialization, it
 * starts a {@link MusicRetriever} to scan the user's media. Then, it waits for
 * Intents (which come from our main activity, {@link MainActivity}, which
 * signal the service to perform specific operations: Play, Pause, Rewind, Skip,
 * etc.
 */
public class MusicService extends Service implements    IMusicServiceObservarable,
                                                        OnCompletionListener, 
                                                        OnPreparedListener, 
                                                        OnErrorListener, 
                                                        MusicFocusable,
                                                        OnBufferingUpdateListener, 
                                                        OnSeekCompleteListener,
                                                        GetZingSongDetailAsyncComplete,
                                                        OnInfoListener,
                                                        GetStationAudioListAsyncComplete
{

    // The tag we put on debug messages
    final static String TAG = MusicService.class.getSimpleName();

    // These are the Intent actions that we are prepared to handle. Notice that
    // the fact these
    // constants exist in our class is a mere convenience: what really defines
    // the actions our
    // service can handle are the <action> tags in the <intent-filters> tag for
    // our service in
    // AndroidManifest.xml.

    public static final String ACTION_TOGGLE_PLAYBACK = "ACTION_TOGGLE_PLAYBACK";
    public static final String ACTION_PLAY = "ACTION_PLAY";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String ACTION_SKIP = "ACTION_SKIP";
    public static final String ACTION_PREV = "ACTION_PREV";
    public static final String ACTION_REWIND = "ACTION_REWIND";
    public static final String ACTION_START_PLAYLIST = "ACTION_START_PLAYLIST";
    public static final String ACTION_PLAY_AT = "ACTION_PLAY_AT";
    public static final String ACTION_PLAY_SUGGESTED = "ACTION_PLAY_SUGGESTED";
    public static final String ACTION_PLAY_ON_TIMER = "ACTION_PLAY_ON_TIMER";
    
    public static final String FROM_USER_TAG = "FROM_USER_TAG";
    public static final String FROM_FRAGMENT_TAG = "FROM_FRAGMENT_TAG";
    
    public static final int MESSAGE_DONE_SONG = 0;
    public static final int MESSAGE_REGISTER_CLIENT = 1;
    
    public static final int ERROR_CODE_CONNECTION_ERROR = -1;
    public static final int ERROR_CODE_BAD_SONG_URL = -2;
    public static final int ERROR_CODE_SONG_DELETED_ON_SERVER = -3;
    public static final int ERROR_CODE_SLOW_CONNECTION = -4;
    public static final int ERROR_CODE_PLAY_LIST_EMPTY = -5;
    public static final int ERROR_CODE_SONG_DROPPED = -6;
    public static final int ERROR_CODE_MEDIA_INFO_NOT_SEEKABLE = -7;
    public static final int ERROR_CODE_MEDIA_INFO_NETWORK_BANDWIDTH = -8;
    

    // The volume we set the media player to when we lose audio focus, but are
    // allowed to reduce
    // the volume instead of stopping playback.
    public static final float DUCK_VOLUME = 0.1f;
    //private ArrayList<Messenger> mClients = new ArrayList<Messenger>();
    private ArrayList<MusicServiceEventListener> mListeners = new ArrayList<MusicServiceEventListener>();

    // our media player
    MediaPlayer mPlayer = null;

    public MediaPlayer getPlayer()
    {
        return mPlayer;
    }

    // our AudioFocusHelper object, if it's available (it's available on SDK
    // level >= 8)
    // If not available, this will be null. Always check for null before using!
    AudioFocusHelper mAudioFocusHelper = null;

    // indicates the state our service:
    public enum State
    {
        Stopped, // media player is stopped and not prepared to play
        Preparing, // media player is preparing...
        Playing, // playback active (media player ready!). (but the media player
                 // may actually be
                 // paused in this state if we don't have audio focus. But we
                 // stay in this state
                 // so that we know we have to resume playback once we get focus
                 // back)
        Paused // playback paused (media player ready!)
    };
    
    /*
    public enum Action
    {
        ACTION_TOGGLE_PLAYBACK,
        ACTION_PLAY,
        ACTION_PAUSE,
        ACTION_STOP,
        ACTION_SKIP,
        ACTION_REWIND,
        ACTION_START_PLAYLIST,
        ACTION_PLAY_AT
    }
     */
    
    State mState = State.Stopped;

    // if in Retrieving mode, this flag indicates whether we should start
    // playing immediately
    // when we are ready or not.
    //boolean mStartPlayingAfterRetrieve = false;

    // if mStartPlayingAfterRetrieve is true, this variable indicates the URL
    // that we should
    // start playing when we are ready. If null, we should play a random song
    // from the device
    //Uri mWhatToPlayAfterRetrieve = null;

    public State getState()
    {
        return mState;
    }

    public void setState(State mState)
    {
        this.mState = mState;
    }

    enum PauseReason
    {
        UserRequest, // paused by user request
        FocusLoss, // paused because of audio focus loss
    };

    // why did we pause? (only relevant if mState == State.Paused)
    PauseReason mPauseReason = PauseReason.UserRequest;

    // do we have audio focus?
    enum AudioFocus
    {
        NoFocusNoDuck, // we don't have audio focus, and can't duck
        NoFocusCanDuck, // we don't have focus, but can play at a low volume
                        // ("ducking")
        Focused // we have full audio focus
    }

    AudioFocus mAudioFocus = AudioFocus.NoFocusNoDuck;

    // title of the song we are currently playing
    String mNotificationSubText = "";

    // whether the song we are playing is streaming from the network
    boolean mIsStreaming = false;

    // Wifi lock that we hold when streaming files from the internet, in order
    // to prevent the
    // device from shutting off the Wifi radio
    WifiLock mWifiLock;

    // The ID we use for the notification (the onscreen alert that appears at
    // the notification
    // area at the top of the screen as an icon -- and as text as well if the
    // user expands the
    // notification area).
    final int NOTIFICATION_ID = 1;

    // Our instance of our MusicRetriever, which handles scanning for media and
    // providing titles and URIs as we need.

    // our RemoteControlClient object, which will use remote control APIs
    // available in
    // SDK level >= 14, if they're available.
    RemoteControlClientCompat mRemoteControlClientCompat1;

    // Dummy album art we will pass to the remote control (if the APIs are
    // available).
    //Bitmap mDummyAlbumArt;

    // The component name of MusicIntentReceiver, for use with media button and
    // remote control
    // APIs
    ComponentName mMediaButtonReceiverComponent;

    AudioManager mAudioManager;
    public AudioManager getAudioManager()
    {
        return mAudioManager;
    }
    NotificationManager mNotificationManager;

    Notification mNotification = null;
    
    /*private ArrayList<AudioItem> mAudioItems;
    
    public ArrayList<AudioItem> getAudioItems()
    {
        return mAudioItems;
    }

    public void setAudioItems(ArrayList<AudioItem> mAudioItems)
    {
        this.mAudioItems = mAudioItems;
    }
    */
    public int mPlayingIndex;

    //Binder given to clients
    private final IBinder mBinder = new MusicServiceBinder();
    private PlayerFragment mPlayerFragment;
    private Station mStation;
    public AudioItem mPlayingItem;
    private NetworkConnectivityListener.State mNetworkState;
    private Radio mRadio;
    private boolean mPlayFromChannel = true;
    private Timer mTimer;
    private NetworkConnectivityListener mNetworkListener = null;
    private Handler mNetWorkHandler = null;

    public Radio getRadio()
    {
        return mRadio;
    }

    public void setRadio(Radio mRadio)
    {
        this.mRadio = mRadio;
    }

    public NetworkConnectivityListener.State getNetworkState()
    {
        return mNetworkState;
    }

    public void setNetworkState(NetworkConnectivityListener.State mNetworkState)
    {
        this.mNetworkState = mNetworkState;
        /*
        if(mNetworkState == NetworkConnectivityListener.State.CONNECTED)
        {
            mState = State.Playing;
        }
        else
        {
            mState = State.Paused;
        }
        */
    }
    
    public Station getStation()
    {
        return mStation;
    }

    public void setStation(Station mStation)
    {
        this.mStation = mStation;
    }

    public PlayerFragment getPlayerFragment()
    {
        return mPlayerFragment;
    }

    public void setPlayerFragment(PlayerFragment mPlayerFragment)
    {
        this.mPlayerFragment = mPlayerFragment;
    }

    public int getPlayingIndex()
    {
        return mPlayingIndex;
    }

    public void setPlayingIndex(int pi)
    {
        this.mPlayingIndex = pi;
    }
    
    private boolean mSkipFromUser = false;
    private boolean mRepeatSong = false;

    /**
     * Makes sure the media player exists and has been reset. This will create
     * the media player if needed, or reset the existing media player if one
     * already exists.
     */
    void createMediaPlayerIfNeeded()
    {
        //Log.d(TAG, "createMediaPlayerIfNeeded() - mPlayer=" + mPlayer);
        if (mPlayer == null)
        {
            //Log.d(TAG, "createMediaPlayerIfNeeded() 1");
            mPlayer = new MediaPlayer();

            // Make sure the media player will acquire a wake-lock while
            // playing. If we don't do
            // that, the CPU might go to sleep while the song is playing,
            // causing playback to stop.
            //
            // Remember that to use this, we have to declare the
            // android.permission.WAKE_LOCK
            // permission in AndroidManifest.xml.
            mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

            // we want the media player to notify us when it's ready preparing,
            // and when it's done
            // playing:
            
            //Log.d(TAG, "createMediaPlayerIfNeeded() 2");
            mPlayer.setOnPreparedListener(this);
            mPlayer.setOnCompletionListener(this);
            mPlayer.setOnErrorListener(this);
            mPlayer.setOnSeekCompleteListener(this);
            mPlayer.setOnBufferingUpdateListener(this);
            mPlayer.setOnInfoListener(this);
        }
        else
        {
            //Log.d(TAG, "createMediaPlayerIfNeeded() 3");
            mPlayer.reset();
        }
    }

    @Override
    public void onCreate()
    {
        Log.d(TAG, "debug: Creating service");
        
        mTimer = new Timer();
        mTimer.setTimer(getApplicationContext());

        //mState = State.Stopped; Create the Wifi lock (this does not acquire the lock, this just creates it)
        mWifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        // create the Audio Focus Helper, if the Audio Focus feature is
        // available (SDK 8 or above)
        if (android.os.Build.VERSION.SDK_INT >= 8) mAudioFocusHelper = new AudioFocusHelper(getApplicationContext(), this);
        else
            mAudioFocus = AudioFocus.Focused; // no focus feature, so we always
                                              // "have" audio focus

        //mDummyAlbumArt = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);

        mMediaButtonReceiverComponent = new ComponentName(this, MusicIntentReceiver.class);
        
        mNetworkListener =  new NetworkConnectivityListener();
        mNetworkListener.startListening(this);

        mNetWorkHandler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                mNetworkState = mNetworkListener.getState();
            }
        };
        
        mNetworkListener.registerHandler(mNetWorkHandler, 1);
    }

    /**
     * Called when we receive an Intent. When we receive an intent sent to us
     * via startService(), this is the method that gets called. So here we react
     * appropriately depending on the Intent's action, which specifies what is
     * being requested of us.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        GAUtils.writeTrackUserEvent(getApplicationContext());
        String action = intent.getAction();
        Log.i(TAG, "onStartCommand() - action=" + action);
        if (action.equals(ACTION_TOGGLE_PLAYBACK)) processTogglePlaybackRequest();
        else if (action.equals(ACTION_PLAY)) processPlayRequest();
        else if (action.equals(ACTION_PAUSE)) processPauseRequest();
        else if (action.equals(ACTION_PREV)) processPlayPrev();
        else if (action.equals(ACTION_STOP)) processStopRequest();
        else if (action.equals(ACTION_SKIP))
        {
            mSkipFromUser = intent.getBooleanExtra(FROM_USER_TAG, false);
            processPlayNext();
        }
        else if (action.equals(ACTION_START_PLAYLIST))
        {
            String tag = intent.getStringExtra(FROM_FRAGMENT_TAG);
            //Log.d(TAG, "Tag=" + tag);
            if(tag != null && tag.equals(SearchFragment.TAG)) mPlayFromChannel = false;
            else mPlayFromChannel = true;
            Station st = intent.getParcelableExtra("Station");
            if(st != null)
            {
                //Log.d(TAG,"here");
                if(mStation != null) mStation.freeAudioItems();
                mStation = st;
            }
            processPlaylist();
        }
        else if (action.equals(ACTION_PLAY_AT))
        {
            int pos = intent.getIntExtra("Pos", 0);
            mSkipFromUser = intent.getBooleanExtra(FROM_USER_TAG,  false);
            Station st = intent.getParcelableExtra("Station");
            if(st != null)
            {
                if(mStation != null) mStation.freeAudioItems();
                mStation = st;
            }
            Log.d(TAG, "Play at pos=" + pos + " fromuser=" + mSkipFromUser);
            processPlayAt(pos);
        }
        else if (action.equals(ACTION_PLAY_SUGGESTED))
        {
            processPlaySuggested();
        }
        else if (action.equals(ACTION_PLAY_ON_TIMER))
        {
            Log.d(TAG, "State=" + mState);
            if(mState == State.Stopped)
            {
                Log.d(TAG, "Play suggest");
                processPlaySuggested();
            }
            else
            {
                Log.d(TAG, "Resume play");
                processPlayRequest();
            }
        }

        return START_NOT_STICKY; // Means we started the service, but don't want it to restart in case it's killed.
    }

    void processTogglePlaybackRequest()
    {
        //Log.d(TAG, "processTogglePlaybackRequest() - Current state=" + mState + "mPlayer=" + mPlayer);
        //createMediaPlayerIfNeeded();
        if (mState == State.Paused || mState == State.Stopped)
        {
            //Log.d(TAG, "processTogglePlaybackRequest() 1");
            processPlayRequest();
        }
        else if(mState == State.Playing)
        {
            //Log.d(TAG, "processTogglePlaybackRequest() 2");
            processPauseRequest();
        }
    }

    void processPlayRequest()
    {
        //Log.d(TAG, "processPlayRequest() - mState=" + mState);
        tryToGetAudioFocus();

        // actually play the song
        //createMediaPlayerIfNeeded();
        
        if (mState == State.Stopped)
        {
            // If we're stopped, just go ahead to the next song and start
            // playing
            mState = State.Playing;
            setUpAsForeground(mNotificationSubText);
        }
        else if (mState == State.Paused)
        {
            //Log.d(TAG, "processPlayRequest() 2");
            // If we're paused, just continue playback and restore the
            // 'foreground service' state.
            mState = State.Playing;
            setUpAsForeground(mNotificationSubText);
        }
        
        configAndStartMediaPlayer();

        // Tell any remote controls that our playback state is 'playing'.
        /*
        if (mRemoteControlClientCompat != null)
        {
            mRemoteControlClientCompat.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
        }
        */
        
        /*
        if(mPlayerFragment != null)
        {
            mPlayerFragment.updateView(mPlayingItem, false);    
        }
        */
        notifyOnPlay(mPlayingItem, false);
    }

    void processPauseRequest()
    {
        //Log.d(TAG, "processPauseRequest() - mState=" + mState);
        if (mState == State.Playing)
        {
            // Pause media player and cancel the 'foreground service' state.
            mState = State.Paused;
            mPlayer.pause();
            relaxResources(false); // while paused, we always retain the
                                   // MediaPlayer
                                //do not give up audio focus
            
            //setUpAsForeground("(Paused) " + mNotificationSubText);
            
            if(mPlayerFragment != null)
            {
                mPlayerFragment.updateView(mPlayingItem, false);
                mPlayerFragment.showProgressBar(false);    
            }
            notifyOnPause();
        }

        /*
        // Tell any remote controls that our playback state is 'paused'.
        if (mRemoteControlClientCompat != null)
        {
            mRemoteControlClientCompat.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
        }
        */
    }
    
    void processStopRequest()
    {
        try
        {
            if (mPlayer != null && mPlayer.isPlaying())
            {
                mState = State.Stopped;
                mPlayer.stop();
                relaxResources(true);
                
                if(mPlayerFragment != null)
                {
                    mPlayerFragment.updateView(mPlayingItem, false);
                    mPlayerFragment.showProgressBar(false);    
                }
                notifyOnStop();
            }    
        }
        catch(Exception e)
        {
            Log.d(TAG, e.getMessage());
        }
    }

    /**
     * Releases resources used by the service for playback. This includes the
     * "foreground service" status and notification, the wake locks and possibly
     * the MediaPlayer.
     * 
     * @param releaseMediaPlayer
     *            Indicates whether the Media Player should also be released or
     *            not
     */
    void relaxResources(boolean releaseMediaPlayer)
    {
        // stop being a foreground service
        stopForeground(true);

        //Log.d(TAG, "relaxResources() mPlayer=" + mPlayer + " releaseMediaPlayer=" + releaseMediaPlayer);
        // stop and release the Media Player, if it's available
        if (releaseMediaPlayer && mPlayer != null)
        {
            //Log.d(TAG, "relaxResources() 2");
            mPlayer.reset();
            mPlayer.release();
            mPlayer = null;
        }

        //Log.d(TAG, "relaxResources() 3");
        // we can also release the Wifi lock, if we're holding it
        if (mWifiLock.isHeld()) mWifiLock.release();
    }

    void giveUpAudioFocus()
    {
        if (mAudioFocus == AudioFocus.Focused && mAudioFocusHelper != null && mAudioFocusHelper.abandonFocus()) mAudioFocus = AudioFocus.NoFocusNoDuck;
    }

    /**
     * Reconfigures MediaPlayer according to audio focus settings and
     * starts/restarts it. This method starts/restarts the MediaPlayer
     * respecting the current audio focus state. So if we have focus, it will
     * play normally; if we don't have focus, it will either leave the
     * MediaPlayer paused or set it to a low volume, depending on what is
     * allowed by the current focus settings. This method assumes mPlayer !=
     * null, so if you are calling it, you have to do so from a context where
     * you are sure this is the case.
     */
    void configAndStartMediaPlayer()
    {
        if(mPlayer == null)
        {
            createMediaPlayerIfNeeded();
        }
        
        //Log.d(TAG, "configAndStartMediaPlayer() 1 - mState=" + mState);
        if (mAudioFocus == AudioFocus.NoFocusNoDuck)
        {
            // If we don't have audio focus and can't duck, we have to pause,
            // even if mState
            // is State.Playing. But we stay in the Playing state so that we
            // know we have to resume
            // playback once we get the focus back.
            if (mPlayer.isPlaying())
            {
                //Log.d(TAG, "configAndStartMediaPlayer() 1.1");
                mPlayer.pause();
                mState = State.Paused;
                if(mPlayerFragment != null) mPlayerFragment.updatePlayPauseButton(mState);
                notifyOnPause();
            }
            else
            {
                //Log.d(TAG, "configAndStartMediaPlayer() 1.2");
                mPlayer.start();
                mState = State.Playing;
                if(mPlayerFragment != null)
                {
                    mPlayerFragment.updateProgressBar();
                    mPlayerFragment.updatePlayPauseButton(mState);    
                }
            }

            return;
        }
        else if (mAudioFocus == AudioFocus.NoFocusCanDuck)
        {
            //Log.d(TAG, "configAndStartMediaPlayer() 2");
            mPlayer.setVolume(DUCK_VOLUME, DUCK_VOLUME); // we'll be relatively quiet 
        }
        else
        {
            //Log.d(TAG, "configAndStartMediaPlayer() 3");
            mPlayer.setVolume(1.0f, 1.0f); // we can be loud
            
        }

        if (!mPlayer.isPlaying())
        {
            //Log.d(TAG, "configAndStartMediaPlayer() 4");
            mPlayer.start();
            mState = State.Playing;
            if(mPlayerFragment != null)
            {
                mPlayerFragment.updatePlayPauseButton(mState);
                mPlayerFragment.updateProgressBar();
            }
        }
        
        //Log.d(TAG, "configAndStartMediaPlayer() 5");
    }

    void processPlaylist()
    {
        //Log.d(TAG, "processPlaylist()");
        if(mStation == null && mPlayerFragment != null)
        {
            mStation = mPlayerFragment.getStation();
        }
        else if(mStation != null)
        {
            if(mStation.getAudioItems() != null && mStation.getAudioItems().size() > 0)
            {
                tryToGetAudioFocus();
                mPlayingIndex = -1;
                processPlayNext();
            }
        }
    }
    
    void processPlayAt(int pos)
    {
        if(mStation == null && mPlayerFragment != null)
        {
            mStation = mPlayerFragment.getStation();    
        }
        else if(mStation != null)
        {
            if(mStation.getAudioItems() != null && mStation.getAudioItems().size() > 0)
            {
                tryToGetAudioFocus();
                mPlayingIndex = pos - 1;//minus one for the top row
                //Log.d(TAG, "processPlayAt() - pos=" + pos + " playingindex=" + mPlayingIndex);
                processPlayNext();
            }    
        }
    }
    
    void processPlaySuggested()
    {
        if(mStation != null)
        {
            mStation.freeAudioItems();
        }
        mStation = StationSuggestion.getSuggestedStation(this.getApplicationContext());
        new GetStationAudioListAsync(this).execute(mStation.getServerId());
    }

    void tryToGetAudioFocus()
    {
        //Log.d(TAG, "tryToGetAudioFocus()() - mAudioFocus before=" + mAudioFocus);
        if (mAudioFocus != AudioFocus.Focused && mAudioFocusHelper != null && mAudioFocusHelper.requestFocus()) mAudioFocus = AudioFocus.Focused;
        //Log.d(TAG, "tryToGetAudioFocus()() - mAudioFocus after=" + mAudioFocus);
    }

    /**
     * Starts playing the next song. If manualUrl is null, the next song will be
     * randomly selected from our Media Retriever (that is, it will be a random
     * song in the user's device). If manualUrl is non-null, then it specifies
     * the URL or path to the song that will be played next.
     */
    void playNextSong(AudioItem ai)
    {
        mState = State.Stopped;
        relaxResources(false); // release everything except MediaPlayer
        int index = mPlayingIndex + 1;
        Log.d(TAG, "playNextSong() - Playing " + index + "/" + mStation.getAudioItems().size() + " " + ai.getTitle());

        GAUtils.writeTrackUserEvent(getApplicationContext());
        GAUtils.writePlayEvent(getApplicationContext());

        //Log.d(TAG, "Source=" + ai.getSource());
        try
        {
            createMediaPlayerIfNeeded();
            
            //MainActivity ma = (MainActivity)mPlayerFragment.getActivity();
            if(mNetworkState != NetworkConnectivityListener.State.CONNECTED)
            {
                //Log.d(TAG, "playNextSong() - " + Constants.ERR_CONNECTION_ERROR);
                
                //notifyOnError(ERROR_CODE_CONNECTION_ERROR);
                Toast.makeText(getApplicationContext(), getString(R.string.msg_err_connection_error), Toast.LENGTH_SHORT).show();
                mState = State.Paused;
                //notify
                return;
            }

            //Log.d(TAG, "2");
            String path = Utilities.checkMp3FileExists(ai.getId());
            if(path != null)//tim thay tren sdcard
            {
                //Log.d(TAG, "2.1");
                ai.setSource(path);
            }
            
            //Log.d(TAG, "3");
            mIsStreaming = ai.isStreaming();
            
            mNotificationSubText = ai.getTitle() + " - " + ai.getPerformer();// + " - " + ai.getSource();
            mState = State.Preparing;
            //Log.d(TAG, "5 - mIsStreaming=" + mIsStreaming);

            //Log.d(TAG, "source=" + ai.getSource());
            if(mIsStreaming)
            {
                //Log.d(TAG, "6");
                mPlayer.setDataSource(ai.getSource());
                Log.d(TAG, "Streaming from " + ai.getSource());
                mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mPlayer.prepareAsync();
            }
            else if(!Utilities.fileExistance(ai.getSource()))//if source NOT exists then return
            {
                //Log.d(TAG, "7");
                this.notifyOnError(ERROR_CODE_BAD_SONG_URL);
                Toast.makeText(getApplicationContext(), getString(R.string.msg_err_song_bad_link), Toast.LENGTH_SHORT).show();
                return;
            }
            else
            {
                //Log.d(TAG, "8");
                mPlayer.setDataSource(ai.getSource());
                Log.d(TAG, "Playing from sdcard" + ai.getSource());
                //Toast.makeText(getApplicationContext(), "Playing from sdcard...", Toast.LENGTH_SHORT).show();
                mPlayer.prepare();
            }
            
            String s = mIsStreaming?"Streaming " : "Playing ";
            setUpAsForeground(s + mNotificationSubText);//add to notification bar
            updateWidget();

            //Log.d(TAG, "9");

            // If we are streaming from the internet, we want to hold a Wifi
            // lock, which prevents
            // the Wifi radio from going to sleep while the song is playing. If,
            // on the other hand,
            // we are *not* streaming, we want to release the lock if we were
            // holding it before.
            if (mIsStreaming) mWifiLock.acquire();
            else if (mWifiLock.isHeld()) mWifiLock.release();
        }
        catch (IOException ex)
        {
            //Log.e(TAG, "IOException playing next song: " + ex.getMessage());
            ex.printStackTrace();
            if(mPlayerFragment != null) mPlayerFragment.enableButtons(true);
        }
        catch(IllegalStateException ex)
        {
            Log.e(TAG, "MediaPlayer already prepared. Play next song now.");
            //ex.printStackTrace();
            createMediaPlayerIfNeeded();
            processPlayNext();
        }
        catch(Exception ex)
        {
            Log.e(TAG, "Something wrong. Resetting player...");
            ex.printStackTrace();
            mState = State.Stopped;
            relaxResources(true);
            giveUpAudioFocus();
            if(mPlayerFragment != null) 
            {
                mPlayerFragment.enableButtons(true);
                mPlayerFragment.updatePlayPauseButton(mState);
            }
        }
    }

    /** Called when media player is done playing current song. */
    public void onCompletion(MediaPlayer player)
    {
        processPlayNext();
    }

    /** Called when media player is done preparing. */
    public void onPrepared(MediaPlayer player)
    {
        //Log.d(TAG, "7");
        // The media player is done preparing. That means we can start playing!
        mState = State.Playing;
        updateNotification(mNotificationSubText);
        //mPlayerFragment.updateProgressBar();
        configAndStartMediaPlayer();
        if(mPlayerFragment != null)
        {
            mPlayerFragment.enableButtons(true);
            mPlayerFragment.updateView(mPlayingItem, true);
        }
    }

    /** Updates the notification. */
    public void updateNotification(String text)
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean show = sharedPref.getBoolean(SettingsActivity.SETTINGS_SHOW_NOTIFICATION_WHEN_PLAY, true);
        if(show)
        {
            PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(getApplicationContext(), MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
            mNotification.setLatestEventInfo(getApplicationContext(), mStation.getName(), text, pi);
            mNotificationManager.notify(NOTIFICATION_ID, mNotification);    
        }
    }
    
    public void showNotification(String text)
    {
        mNotification = new Notification();
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(getApplicationContext(), MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        mNotification.tickerText = text;
        mNotification.icon = R.drawable.ic_launcher;
        mNotification.flags |= Notification.FLAG_ONGOING_EVENT;
        if(mStation !=  null)//playing
        {
            mNotification.setLatestEventInfo(getApplicationContext(), mStation.getName(), text, pi); 
        }
        else
        {
            mNotification.setLatestEventInfo(getApplicationContext(), "", text, pi);
        }
        startForeground(NOTIFICATION_ID, mNotification);
    }

    public void hideNotification()
    {
        mNotification = new Notification();
        mNotification.setLatestEventInfo(getApplicationContext(), "", "", null);
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    }

    /**
     * Configures service as a foreground service. A foreground service is a
     * service that's doing something the user is actively aware of (such as
     * playing music), and must appear to the user as a notification. That's why
     * we create the notification here.
     */
    public void setUpAsForeground(String text)
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean show = sharedPref.getBoolean(SettingsActivity.SETTINGS_SHOW_NOTIFICATION_WHEN_PLAY, true);
        mNotification = new Notification();
        if(show)
        {
            PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(getApplicationContext(), MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
            mNotification.tickerText = text;
            mNotification.icon = R.drawable.ic_launcher;
            mNotification.flags |= Notification.FLAG_ONGOING_EVENT;
            if(mStation !=  null)//playing
            {
                mNotification.setLatestEventInfo(getApplicationContext(), mStation.getName(), text, pi); 
            }
            else
            {
                mNotification.setLatestEventInfo(getApplicationContext(), "", text, pi);
            }
        }
        startForeground(NOTIFICATION_ID, mNotification);
    }

    /**
     * Called when there's an error playing media. When this happens, the media
     * player goes to the Error state. We warn the user about the error and
     * reset the media player.
     */
    public boolean onError(MediaPlayer mp, int what, int extra)
    {
        //Toast.makeText(getApplicationContext(), "Media player error! Resetting.", Toast.LENGTH_SHORT).show();
        Log.e(TAG, "Media player error! Resetting. Error: what=" + String.valueOf(what) + ", extra=" + String.valueOf(extra));

        mState = State.Stopped;
        relaxResources(true);
        giveUpAudioFocus();
        
        if(extra == -1004 || extra== -2147483648)//I/O error. can't play the path
        {
            this.notifyOnError(ERROR_CODE_SONG_DELETED_ON_SERVER);
            Toast.makeText(getApplicationContext(), getString(R.string.msg_err_song_deleted_on_server), Toast.LENGTH_SHORT).show();
            processPlayNext();
        }
        else
        {
            this.notifyOnError(ERROR_CODE_SLOW_CONNECTION);
            Toast.makeText(getApplicationContext(), getString(R.string.msg_err_slow_connection), Toast.LENGTH_SHORT).show();
            processPlayNext();    
        }
        
        return true;// true indicates we handled the error
    }

    public void onGainedAudioFocus()
    {
        //Toast.makeText(getApplicationContext(), "Gained audio focus.", Toast.LENGTH_SHORT).show();
        mAudioFocus = AudioFocus.Focused;

        // restart media player with new focus settings
        if (mState == State.Playing)
        {
            configAndStartMediaPlayer();
        }
    }

    public void onLostAudioFocus(boolean canDuck)
    {
        //Toast.makeText(getApplicationContext(), "Lost audio focus. " + (canDuck ? "can duck" : "no duck"), Toast.LENGTH_SHORT).show();
        mAudioFocus = canDuck ? AudioFocus.NoFocusCanDuck : AudioFocus.NoFocusNoDuck;
        //Log.d(TAG, "onLostAudioFocus() - Audio focus=" + mAudioFocus);

        // start/restart/pause media player with new focus settings
        if ((mPlayer != null && mPlayer.isPlaying() ))
        {
            //tryToGetAudioFocus();
            
            configAndStartMediaPlayer();
        }
    }

    @Override
    public void onDestroy()
    {
        // Service is being killed, so make sure we release our resources
        mNetworkListener.unregisterHandler(mNetWorkHandler);
        mNetworkListener.stopListening();
        mState = State.Stopped;
        relaxResources(true);
        giveUpAudioFocus();
    }

    @Override
    public IBinder onBind(Intent arg0)
    {
        return mBinder;
    }
    
    private void calculateNewPlayingIndex()
    {
        //play list has only 1 song or last song already, play from beginning (or notify client to load new playlist - future)
        if(mStation.getAudioItems().size() == 1 || mPlayingIndex == mStation.getAudioItems().size() - 1)
        {
            //Log.d(TAG, "processPlayNext() - 1");
            mPlayingIndex=0;
        }
        else
        {
            //Log.d(TAG, "processPlayNext() - 3");
            mPlayingIndex++;
        }
    }
 
    private void processPlayNext()
    {
        //Log.d(TAG, "processPlayNext() - fromUser=" + mSkipFromUser + " isrepeat=" + mPlayerFragment.isRepeatSong() );
        if(mStation != null && mStation.getAudioItems() != null && mStation.getAudioItems().size() > 0)
        {
            if(mPlayerFragment != null)
            {
                mPlayerFragment.enableButtons(false);    
            }
            if(mSkipFromUser)
            {
                calculateNewPlayingIndex();
                mSkipFromUser = false;
            }
            else//if not skip from user (media player advance to next song itself)
            {
                if(mPlayerFragment != null) mRepeatSong = mPlayerFragment.isRepeatSong();
                if(!mRepeatSong)//if not repeat, increase index
                {
                    calculateNewPlayingIndex();
                }
                
                if(mPlayingIndex < 0 || mPlayingIndex >= mStation.getAudioItems().size() - 1) mPlayingIndex = 0;//for first time play the first song
            }

            //Log.d(TAG, "processPlayNext() - mPlayingIndex=" + mPlayingIndex);
            mPlayingItem = mStation.getAudioItems().get(mPlayingIndex);
            mIsStreaming = mPlayingItem.isStreaming();//.getSource().startsWith("http:") || mPlayingItem.getSource().startsWith("https:");
            if(mPlayingItem.getId() != null && !mPlayingItem.getId().equals("")) new GetZingSongDetailAsync(this).execute(mPlayingItem.getId());
            else processPlayNext();
            /*
            if(mIsStreaming == false)//file available on sd card
            {
                //String path = mPlayingItem.getSource();
                playNextSong(mPlayingItem);
            }
            else//file not found on sd card
            {
                new GetZingSongDetailAsync(this).execute(mPlayingItem.getId());    
            }*/
        }
        else//no songs in the station
        {
            this.notifyOnError(ERROR_CODE_PLAY_LIST_EMPTY);
            Toast.makeText(getApplicationContext(), getString(R.string.msg_err_play_list_empty), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void calculatePrevIndex()
    {
        Log.d(TAG, "currentIndex before=" + mPlayingIndex);
        if(mStation.getAudioItems().size() == 1)
        {
            //Log.d(TAG, "processPlayNext() - 1");
            mPlayingIndex=0;
        }
        else if(mPlayingIndex == 0)
        {
            mPlayingIndex = mStation.getAudioItems().size() - 1;
        }
        else
        {
            //Log.d(TAG, "processPlayNext() - 3");
            mPlayingIndex--;
        }
        Log.d(TAG, "currentIndex after=" + mPlayingIndex);
    }
    
    private void processPlayPrev()
    {
        if(mStation != null && mStation.getAudioItems() != null && mStation.getAudioItems().size() > 0)
        {
            if(mPlayerFragment != null)
            {
                mPlayerFragment.enableButtons(false);    
            }
            
            calculatePrevIndex();
            mPlayingItem = mStation.getAudioItems().get(mPlayingIndex);
            mIsStreaming = mPlayingItem.isStreaming();//.getSource().startsWith("http:") || mPlayingItem.getSource().startsWith("https:");
            new GetZingSongDetailAsync(this).execute(mPlayingItem.getId());
        }
        else//no songs in the station
        {
            this.notifyOnError(ERROR_CODE_PLAY_LIST_EMPTY);
            Toast.makeText(getApplicationContext(), getString(R.string.msg_err_play_list_empty), Toast.LENGTH_SHORT).show();
        }
    }
    
    public void resumePlay()
    {
        //Log.d(TAG, "resumePlay()");
        playNextSong(mPlayingItem);
    }
    
    public class MusicServiceBinder extends Binder
    {
        public MusicService getService()
        {
            return MusicService.this;
        }
    }

    @Override
    public void onSeekComplete(MediaPlayer arg0)
    {
        mPlayerFragment.showProgressBar(false);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent)
    {
        // //set percent downloaded in progressbar here
        if(mPlayerFragment != null)
        {
            mPlayerFragment.setSongSeekbarSecondaryProgress(percent);
        }
    }

    @Override
    public void onGetZingSongDetailAsyncComplete(Song s)
    {
        //Log.d(TAG, "onGetZingSongDetailAsyncComplete()");
        if(s != null)
        {
            mPlayingItem.setAlbum(s.getAlbumId());
            mPlayingItem.setDownloadLink(s.getLinkDownload128());
            mPlayingItem.setId(s.getID());
            mPlayingItem.setLink(s.getLink());
            mPlayingItem.setPerformer(s.getArtist());
            mPlayingItem.setPlink(s.getArtistAvatar());
            if(Utilities.checkMp3FileExists(mPlayingItem.getId()) == null)
            {
                mPlayingItem.setSource(s.getLinkPlay128());    
            }
            mPlayingItem.setLyrics(s.getLyrics());
            //Log.d(TAG, "lyrics=" + mPlayingItem.getLyrics());
            if(!mPlayFromChannel)
            {
                mPlayingItem.setThumbnail(s.getArtistAvatar());  
            }
            mPlayingItem.setTitle(s.getTitle());

            //mPlayerFragment.enableButtons(true);
            this.playNextSong(mPlayingItem);
        }
        else
        {
            //Log.d(TAG, "Song is null");
            this.notifyOnError(ERROR_CODE_SONG_DROPPED);
            Toast.makeText(getApplicationContext(), getString(R.string.msg_err_song_dropped), Toast.LENGTH_SHORT).show();
            processPlayNext();
        }
    }
    
    @Override
    public void onGetZingSongDetailAsyncError(int errCode, String errMsg)
    {
        Log.e(TAG, "onGetZingSongDetailAsyncError() - errCode=" + errCode + " errMsg=" + errMsg);
        mState = State.Stopped;
        Toast.makeText(getApplicationContext(), getString(R.string.msg_err_song_dropped), Toast.LENGTH_SHORT).show();
        /*
        if(mPlayerFragment != null)
        {
            mPlayerFragment.enableButtons(true);
        }
        */
        mState = State.Stopped;
        this.notifyOnError(ERROR_CODE_SONG_DROPPED);
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra)
    {
        switch(what)
        {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
            {
                if(mPlayerFragment != null) mPlayerFragment.showProgressBar(true);
                break;
            }
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
            {
                if(mPlayerFragment != null) mPlayerFragment.showProgressBar(false);
                break;
            }
            case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
            {
                this.notifyOnError(ERROR_CODE_MEDIA_INFO_NOT_SEEKABLE);
                Toast.makeText(getApplicationContext(), getString(R.string.msg_err_media_not_seekable), Toast.LENGTH_LONG).show();
                break;
            }
            case 703://MEDIA_INFO_NETWORK_BANDWIDTH:
            {
                this.notifyOnError(ERROR_CODE_MEDIA_INFO_NETWORK_BANDWIDTH);
                Toast.makeText(getApplicationContext(), getString(R.string.msg_err_media_info_network_bandwidth), Toast.LENGTH_LONG).show();
                break;
            }
            default:
            {
                Log.d(TAG, "onInfo() - what=" + what);
                break;
            }
        }
        
        return false;
    }

    @Override
    public void onRebind(Intent intent)
    {
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        return super.onUnbind(intent);
    }

    public ArrayList<MusicServiceEventListener> getListeners()
    {
        return mListeners;
    }

    public void setListeners(ArrayList<MusicServiceEventListener> mListeners)
    {
        this.mListeners = mListeners;
    }
    
    public void addListener(MusicServiceEventListener lis)
    {
        this.mListeners.add(lis);
    }
    
    public void removeListener(MusicServiceEventListener lis)
    {
        this.mListeners.remove(lis);
    }

    @Override
    public void notifyOnNext()
    {
        for(MusicServiceEventListener lis : this.mListeners)
        {
            if(lis != null) lis.onNext(mPlayingItem);
        }

        updateWidget();
    }

    @Override
    public void notifyOnPlay(AudioItem ai, boolean updateBitmap)
    {
        for(MusicServiceEventListener lis : this.mListeners)
        {
            if(lis != null) lis.onPlay(ai, updateBitmap);
        }
        updateWidget();
    }

    @Override
    public void notifyOnStop()
    {
        for(MusicServiceEventListener lis : this.mListeners)
        {
            if(lis != null) lis.onStop(mPlayingItem);
        }
        updateWidget();
    }

    @Override
    public void notifyOnPause()
    {
        for(MusicServiceEventListener lis : this.mListeners)
        {
            if(lis != null) lis.onPause(mPlayingItem);
        }
        updateWidget();
    }

    @Override
    public void notifyOnError(int errCode)
    {
        for(MusicServiceEventListener lis : this.mListeners)
        {
            if(lis != null) lis.onError(errCode);
        }
        updateWidget();
    }

    @Override
    public void onGetStationAudioListAsyncComplete(ArrayList<AudioItem> al)
    {
        mStation.setAudioItems(al);
        processPlaylist();
    }

    @Override
    public void onGetStationAudioListAsyncError(int errCode, String errMsg)
    {
        Toast.makeText(getApplicationContext(), errMsg, Toast.LENGTH_SHORT).show();
        Log.e(TAG, "onGetStationAudioListAsyncError() - errMsg=" + errMsg);
        mState = State.Stopped;
    }
    
    private void updateWidget()
    {
        /*
        Log.d(TAG, "updateWidget()");
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.widget);
        remoteViews.setTextViewText(R.id.stationName, mStation.getName());
        remoteViews.setTextViewText(R.id.songName, mPlayingItem.getTitle());
        */
    }
}
