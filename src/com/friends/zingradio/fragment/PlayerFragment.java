package com.friends.zingradio.fragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import com.actionbarsherlock.view.MenuItem;
import com.friends.zingradio.R;
import com.friends.zingradio.activity.MainActivity;
import com.friends.zingradio.activity.SettingsActivity;
import com.friends.zingradio.adapter.SongAdapter;
import com.friends.zingradio.adapter.SongPlaylistAdapter;
import com.friends.zingradio.async.GetPerformerCoverAsync;
import com.friends.zingradio.async.GetStationAudioListAsync;
import com.friends.zingradio.async.GetPerformerCoverAsyncComplete;
import com.friends.zingradio.async.GetStationAudioListAsyncComplete;
import com.friends.zingradio.async.GetZingPlaylistDetailAsync;
import com.friends.zingradio.async.GetZingPlaylistDetailAsyncComplete;
import com.friends.zingradio.data.ZingRadioDatabaseHelper;
import com.friends.zingradio.entity.AudioItem;
import com.friends.zingradio.entity.Playlist;
import com.friends.zingradio.entity.Station;
import com.friends.zingradio.media.MusicService;
import com.friends.zingradio.media.MusicServiceEventListener;
import com.friends.zingradio.ui.PlayerFragmentVolumeGestureListener;
import com.friends.zingradio.util.Constants;
import com.friends.zingradio.util.GAUtils;
import com.friends.zingradio.util.NetworkConnectivityListener;
import com.friends.zingradio.util.StationSuggestion;
import com.friends.zingradio.util.download.FileDownloadManager;
import com.friends.zingradio.util.download.IFileDownloadManagerListener;
import com.friends.zingradio.util.Utilities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.Keyframe;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.AsyncTask.Status;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SlidingDrawer;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class PlayerFragment extends Fragment implements GetPerformerCoverAsyncComplete, GetStationAudioListAsyncComplete, GetZingPlaylistDetailAsyncComplete, SeekBar.OnSeekBarChangeListener,
        IFileDownloadManagerListener, MusicServiceEventListener
{
    public static final String TAG = PlayerFragment.class.getSimpleName();
    public static final Uri URI = new Uri.Builder().scheme(TAG).authority("").build();
    public static final int TIME_TO_RELEASE_BUTTON = 3000;// 3 seconds
    public static final int NOTIFICATION_ID = 2;
    public static final String REPEAT_SONG_KEY = "REPEAT_SONG_KEY";
    public static final int VOLUME_DELAY = 3000;
    public static final int LYRICS_DELAY = 2000;

    private View viewRoot;
    private ImageButton mPlayPauseButton;
    private ImageButton mNextButton;
    private ImageButton mPrevButton;
    private SeekBar mSeekbar;
    private TextView mSongCurrentDurationLabel;
    private TextView mSongTotalDurationLabel;
    private TextView mSongName;
    //private ImageButton mStarButton;
    private ProgressBar mLoadingIndicator;
    private ImageButton mHeartButton;
    private ImageButton mLyricsButton;
    //private TextView mStationName;
    private SeekBar mVolumeSeekbar;
    private ImageButton mVolumeUpButton;
    private ImageButton mVolumeDownButton;
    private ImageView mVolumeHandler;
    private CountDownTimer mVolumeTimer;
    private CountDownTimer mLyricsTimer;
    private ImageView mArtistImageView;
    //    private WeakReference<ImageView> mArtistImageViewRef;
    private RelativeLayout mMainLayout;
    private ImageButton mDownloadButton;
    private ImageButton mRepeatSongButton;
    private SlidingDrawer mLyricsSlidingDrawer;
    //private SlidingDrawer mVolumeSlidingDrawer;
    private ImageView mLyricsHandle;
    private WebView mLyricsWebView;
    private ImageButton mVolumeHandlerOutside;
    private FileDownloadManager mFileDownloadManager = null;
    private AudioItem mDownloadItem;
    private GetStationAudioListAsync mGetStationAudioListAsync;
    private GetZingPlaylistDetailAsync mGetZingPlaylistDetailAsync;
    private boolean mRepeatSong;
    private ImageButton mFavChannelButton;
    private TextView mChannelNameTextView;
    private ListView mSonglistListView;
    private LayoutTransition mTransitioner;
    private LinearLayout mSonglistLayout;
    private ImageButton mCloseSonglistButton;
    private ArrayList<AudioItem> mSonglist;
    private SongPlaylistAdapter mSongAdapter;
    private TextView mSongPlaylistInfo;
    private LinearLayout mVolumeLayout;
    private ImageButton mSettingsButton;

    public MusicService.State getState()
    {
        return mMusicService.getState();
    }

    private Station mStation;
    private GetPerformerCoverAsync mGetPerformerCoverAsync;

    private MusicService mMusicService;

    public MusicService getMusicService()
    {
        return mMusicService;
    }

    public void setMusicService(MusicService mMusicService)
    {
        this.mMusicService = mMusicService;
    }

    public Station getStation()
    {
        return mStation;
    }

    public void setStation(Station mStation)
    {
        this.mStation = mStation;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        //Log.d(TAG, "onCreate() - savedInstanceState=" + savedInstanceState);
        super.onCreate(savedInstanceState);
        //this.setRetainInstance(true);
        mFileDownloadManager = new FileDownloadManager(this);
        mFileDownloadManager.init();
    }

    @SuppressLint("NewApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        //Log.d(TAG, "onCreateView() - savedInstanceState=" + savedInstanceState);
        GAUtils.writeViewFragement(this.getActivity(), TAG);
        super.onCreateView(inflater, container, savedInstanceState);
        mLyricsTimer = new CountDownTimer(LYRICS_DELAY, 1000)
        {
            public void onFinish()
            {
                if (!mLyricsSlidingDrawer.isOpened() && !mLyricsSlidingDrawer.isMoving())
                {
                    mLyricsSlidingDrawer.setVisibility(View.GONE);
                }
                else
                {
                    mLyricsSlidingDrawer.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTick(long l)
            {
            }
        };

        viewRoot = inflater.inflate(R.layout.player, container, false);
        // viewRoot.setBackground(R.drawable.app_background);

        viewRoot.setOnKeyListener(new OnKeyListener()
        {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (keyCode == KeyEvent.KEYCODE_BACK)
                {
                    return true;
                }
                return false;
            }
        });

        mArtistImageView = (ImageView) viewRoot.findViewById(R.id.imageViewPerformerCover);
        mSonglistLayout = (LinearLayout) viewRoot.findViewById(R.id.lo_songlist);
        mSonglist = new ArrayList<AudioItem>();
        mSongAdapter = new SongPlaylistAdapter(getActivity(), mSonglist);
        mSonglistListView = (ListView) viewRoot.findViewById(R.id.lv_songlist);
        mSonglistListView.setAdapter(mSongAdapter);
        mSonglistListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long flags)
            {
                if (Utilities.isNetworkOnline(getActivity()))
                {
                    PlayerFragment.this.playAt(position);
                }
                else
                {
                    Toast.makeText(viewRoot.getContext(), getString(R.string.msg_err_connection_error), Toast.LENGTH_SHORT).show();
                }
            }
        });

        mSongPlaylistInfo = (TextView) viewRoot.findViewById(R.id.tv_songlist_info);
        mSongPlaylistInfo.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showHideSonglist(View.GONE);
            }
        });
        mCloseSonglistButton = (ImageButton) viewRoot.findViewById(R.id.btn_close_songlist);
        mCloseSonglistButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showHideSonglist(View.GONE);
            }
        });

        mArtistImageView.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showHideSonglist(View.VISIBLE);
                loadSongList();
            }
        });

        //mTransitioner = new LayoutTransition();

        //mArtistImageViewRef = new WeakReference<ImageView>(mArtistImageView);
        mLyricsSlidingDrawer = (SlidingDrawer) viewRoot.findViewById(R.id.sliding_lyrics);
        mLyricsHandle = (ImageView) viewRoot.findViewById(R.id.sliding_handle);
        mLyricsWebView = (WebView) viewRoot.findViewById(R.id.sliding_content);
        //disable hardware accelerator to solve flicks when scrolling lyrics
        if (Build.VERSION.SDK_INT > 11) mLyricsWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mLyricsWebView.setBackgroundColor(0x00000000);

        mLyricsButton = (ImageButton) viewRoot.findViewById(R.id.btn_toggle_lyric);
        mLyricsButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mLyricsSlidingDrawer.setVisibility(View.VISIBLE);
                mLyricsSlidingDrawer.animateOpen();
            }
        });

        mLyricsSlidingDrawer.setOnDrawerCloseListener(new OnDrawerCloseListener()
        {
            @Override
            public void onDrawerClosed()
            {
                mLyricsSlidingDrawer.setVisibility(View.GONE);
            }
        });

        mRepeatSongButton = (ImageButton) viewRoot.findViewById(R.id.btn_repeat_song);
        mRepeatSongButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                PlayerFragment.this.setRepeatSong(!mRepeatSong);
            }
        });

        mPlayPauseButton = (ImageButton) viewRoot.findViewById(R.id.button_playpause);

        mPlayPauseButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                onButtonClick(mPlayPauseButton);
            }
        });

        mNextButton = (ImageButton) viewRoot.findViewById(R.id.button_next);
        mNextButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                onButtonClick(mNextButton);
            }
        });

        mPrevButton = (ImageButton) viewRoot.findViewById(R.id.button_prev);
        mPrevButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                onButtonClick(mPrevButton);
            }
        });

        mHeartButton = (ImageButton) viewRoot.findViewById(R.id.btn_toggle_song_fav);

        mHeartButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                ZingRadioDatabaseHelper db = ZingRadioDatabaseHelper.getInstance(getActivity());

                long newRowId = db.toggleFavoriteSong(mMusicService.mPlayingItem);
                // Log.d(TAG, "newrowId=" + newRowId);
                if (newRowId == -1)
                {
                    Toast.makeText(getActivity(), getActivity().getString(R.string.msg_err_insert_song), Toast.LENGTH_SHORT).show();
                }
                else if (newRowId == -99)
                {
                    Toast.makeText(getActivity(), getActivity().getString(R.string.msg_removed_from_my_songs), Toast.LENGTH_SHORT).show();
                    mHeartButton.setSelected(false);
                }
                else
                {
                    Toast.makeText(getActivity(), getActivity().getString(R.string.msg_added_to_my_songs), Toast.LENGTH_SHORT).show();
                    mHeartButton.setSelected(true);

                    // need to check for preference if user wants to download
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(PlayerFragment.this.getActivity());
                    boolean autoDownload = sharedPref.getBoolean(SettingsActivity.SETTINGS_AUTO_DOWNLOAD, true);

                    // only download if auto download is true and file not
                    // exists
                    if (autoDownload && Utilities.checkMp3FileExists(mMusicService.mPlayingItem.getId()) == null)
                    {
                        mDownloadItem = mMusicService.mPlayingItem;
                        mFileDownloadManager.startDownload(mMusicService.mPlayingItem);
                    }
                }
            }
        });

        mSeekbar = (SeekBar) viewRoot.findViewById(R.id.songProgressBar);
        mSeekbar.setOnSeekBarChangeListener(this); // Important
        mSeekbar.setProgress(0);
        // mSongProgressBar.setEnabled(false);
        mSeekbar.setClickable(false);
        // mSongProgressBar.setFocusable(false);

        mLoadingIndicator = (ProgressBar) viewRoot.findViewById(R.id.progressBar1);

        mVolumeTimer = new CountDownTimer(VOLUME_DELAY, 1000)
        {
            public void onFinish()
            {
                //mVolumeFrameLayout.setVisibility(View.GONE);
                showHideVolume(false);
            }

            @Override
            public void onTick(long l)
            {
            }
        };

        /*
         * mVolumeSlidingDrawer =
         * (SlidingDrawer)viewRoot.findViewById(R.id.sliding_volume);
         * mVolumeSlidingDrawer.setOnDrawerOpenListener(new
         * OnDrawerOpenListener() {
         * 
         * @Override public void onDrawerOpened() { int max =
         * mMusicService.getAudioManager
         * ().getStreamMaxVolume(AudioManager.STREAM_MUSIC); int currVol =
         * mMusicService
         * .getAudioManager().getStreamVolume(AudioManager.STREAM_MUSIC);
         * mVolumeSeekbar.setMax(max); mVolumeSeekbar.setProgress(currVol);
         * mVolumeTimer.start(); } });
         * 
         * mVolumeSlidingDrawer.setOnDrawerCloseListener(new
         * OnDrawerCloseListener() {
         * 
         * @Override public void onDrawerClosed() { mVolumeTimer.cancel(); } });
         */

        mVolumeLayout = (LinearLayout) viewRoot.findViewById(R.id.lo_volume);

        mVolumeHandlerOutside = (ImageButton) viewRoot.findViewById(R.id.sliding_volume_handle_outside);
        mVolumeHandlerOutside.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showHideVolume(true);
                /*
                 * Animation animation = new AlphaAnimation(0.0f, 1.0f);
                 * mVolumeFrameLayout.startAnimation(animation);
                 * mVolumeFrameLayout.setVisibility(View.VISIBLE);
                 * mVolumeTimer.start();
                 */
            }
        });

        mVolumeHandlerOutside.setOnTouchListener(new OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (mVolumeLayout.getVisibility() == View.GONE)
                {
                    showHideVolume(true);
                }
                return true;
            }
        });

        mVolumeHandler = (ImageView) viewRoot.findViewById(R.id.sliding_volume_handle);
        mVolumeHandler.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showHideVolume(false);
                /*
                 * Animation animation = new AlphaAnimation(1.0f, 0.0f);
                 * mVolumeFrameLayout.startAnimation(animation);
                 * mVolumeFrameLayout.setVisibility(View.GONE);
                 * mVolumeTimer.cancel();
                 */
            }
        });

        mVolumeSeekbar = (SeekBar) viewRoot.findViewById(R.id.seekbar_volume);
        if (mMusicService != null)
        {
            int max = mMusicService.getAudioManager().getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int currVol = mMusicService.getAudioManager().getStreamVolume(AudioManager.STREAM_MUSIC);
            mVolumeSeekbar.setMax(max);
            setVolume(currVol);
        }

        //mVolumeSeekbar.setProgress(currVol);
        mVolumeSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
        {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
                showHideVolume(false);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                if (fromUser)
                {
                    setVolume(progress);
                }
                //mVolumeTimer.start();
            }
        });

        /*
         * mVolumeUpButton =
         * (ImageButton)viewRoot.findViewById(R.id.sliding_volume_inscrease);
         * mVolumeUpButton.setOnClickListener(new OnClickListener() {
         * 
         * @Override public void onClick(View v) {
         * 
         * int max =
         * mMusicService.getAudioManager().getStreamMaxVolume(AudioManager
         * .STREAM_MUSIC); int currVol =
         * mMusicService.getAudioManager().getStreamVolume
         * (AudioManager.STREAM_MUSIC); if(currVol < max) {
         * setVolume(++currVol); mVolumeTimer.start(); } } });
         * 
         * mVolumeDownButton =
         * (ImageButton)viewRoot.findViewById(R.id.sliding_volume_descrease);
         * mVolumeDownButton.setOnClickListener(new OnClickListener() {
         * 
         * @Override public void onClick(View v) { int currVol =
         * mMusicService.getAudioManager
         * ().getStreamVolume(AudioManager.STREAM_MUSIC); if(currVol > 0) {
         * setVolume(--currVol); mVolumeTimer.start(); } } });
         */
        mSongCurrentDurationLabel = (TextView) viewRoot.findViewById(R.id.songCurrentDurationLabel);
        mSongTotalDurationLabel = (TextView) viewRoot.findViewById(R.id.songTotalDurationLabel);

        mSongName = (TextView) viewRoot.findViewById(R.id.tv_song_title);
        mSongName.setSelected(true);

        mMusicService = ((MainActivity) viewRoot.getContext()).mMusicService;

        if (savedInstanceState != null)
        {
            //Log.d(TAG, "mRepeatSong=" + mRepeatSong);
            this.setRepeatSong(savedInstanceState.getBoolean(REPEAT_SONG_KEY));
            //Log.d(TAG, "mRepeatSong after=" + mRepeatSong);
        }

        MainActivity ma = (MainActivity) this.getActivity();
        mFavChannelButton = ma.getFavChannelButton();
        mChannelNameTextView = ma.getChannelNameTextView();

        mSettingsButton = (ImageButton) viewRoot.findViewById(R.id.button_settings);
        mSettingsButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                MainActivity ma = (MainActivity) PlayerFragment.this.getActivity();
                Intent settingsIntent = new Intent(ma, SettingsActivity.class);
                ma.startActivity(settingsIntent);
                ma.overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            }
        });

        return viewRoot;
    }

    public void setVolume(int vol)
    {
        if (mVolumeSeekbar != null && mMusicService != null && mVolumeHandler != null)
        {
            int max = mMusicService.getAudioManager().getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            mVolumeSeekbar.setMax(max);
            mVolumeSeekbar.setProgress(vol);
            float f = (float) vol / max;
            //Log.d(TAG, "vol=" + vol + " max=" + mVolumeSeekbar.getMax() + " f=" + f);
            mMusicService.getAudioManager().setStreamVolume(AudioManager.STREAM_MUSIC, vol, AudioManager.FLAG_VIBRATE);

            if (f == 0)
            {
                mVolumeHandler.setImageLevel(0);
                mVolumeHandlerOutside.setImageLevel(0);
            }
            else if (f <= 0.4)
            {
                mVolumeHandler.setImageLevel(1);
                mVolumeHandlerOutside.setImageLevel(1);
            }
            else if (f < 0.7)
            {
                mVolumeHandler.setImageLevel(2);
                mVolumeHandlerOutside.setImageLevel(2);
            }
            else
            {
                mVolumeHandler.setImageLevel(3);
                mVolumeHandlerOutside.setImageLevel(3);
            }
        }
    }

    private Handler mSeekbarHandler = new Handler();;

    public void updateProgressBar()
    {
        mSeekbarHandler.postDelayed(mUpdateTimeTask, 100);
    }

    public void updatePlayPauseButton(MusicService.State state)
    {
        if (state == MusicService.State.Playing)
        {
            mPlayPauseButton.setSelected(true);
        }
        else
        {
            mPlayPauseButton.setSelected(false);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        //Log.d(TAG, "onViewCreated() - savedInstanceState=" + savedInstanceState);
        super.onViewCreated(view, savedInstanceState);
        updateProgressBar();
    }

    /**
     * Background Runnable thread
     * */
    private Runnable mUpdateTimeTask = new Runnable()
    {
        public void run()
        {
            if (mMusicService != null)
            {
                MediaPlayer mp = mMusicService.getPlayer();
                if (mp != null && mp.isPlaying())
                {
                    long totalDuration = mp.getDuration();
                    long currentDuration = mp.getCurrentPosition();

                    // Displaying Total Duration time
                    mSongTotalDurationLabel.setText("" + Utilities.milliSecondsToTimerMinute(totalDuration));
                    // Displaying time completed playing
                    mSongCurrentDurationLabel.setText("" + Utilities.milliSecondsToTimerMinute(currentDuration));

                    // Updating progress bar
                    int progress = (int) (Utilities.getProgressPercentage(currentDuration, totalDuration));
                    //Log.d("Progress=", + progress + " Secondary progress=" + lastPercent);
                    mSeekbar.setProgress(progress);

                    // Running this thread after 100 milliseconds
                    mSeekbarHandler.postDelayed(this, 100);
                }
            }
        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        //Log.d(TAG, "onSaveInstanceState() - mRepeatSong=" + mRepeatSong);
        outState.putBoolean(REPEAT_SONG_KEY, mRepeatSong);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause()
    {
        //Log.d(TAG, "onPause()");
        super.onPause();
    }

    @Override
    public void onResume()
    {
        //Log.d(TAG, "onResume()");
        super.onResume();

        if (mMusicService != null)
        {
            // Log.d(TAG, "onResume() 1");
            if (mMusicService.getState() == MusicService.State.Playing)// playing,
                                                                       // update
                                                                       // current
                                                                       // view
            {
                // Log.d(TAG, "onResume() 2");
                mStation = mMusicService.getStation();
                updateView(mMusicService.mPlayingItem, false);
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
                this.setRepeatSong(sharedPref.getBoolean(REPEAT_SONG_KEY, false));
            }
            // Log.d(TAG, "onResume() 3");
        }
    }

    @Override
    public void onStop()
    {
        // Log.d(TAG, "onStop()");
        super.onStop();
    }

    @Override
    public void onDestroy()
    {
        // Log.d(TAG, "onDestroy()");
        super.onDestroy();
        if (mFileDownloadManager != null)
        {
            mFileDownloadManager.close();
        }

        /*
         * Drawable drawable = mArtistImageView.getDrawable(); if (drawable
         * instanceof BitmapDrawable) { BitmapDrawable bitmapDrawable =
         * (BitmapDrawable) drawable; Bitmap bitmap =
         * bitmapDrawable.getBitmap(); bitmap.recycle(); }
         */
    }

    @Override
    public void onGetPerformerCoverAsyncCompleted(Bitmap b)
    {
        if (this.isAdded())
        {
            if (b == null)
            {
                b = BitmapFactory.decodeResource(getResources(), R.drawable.img_song_loading_thumbnail);
            }

            /*
             * //free current bitmap memory Drawable drawable =
             * mArtistImageView.getDrawable(); if (drawable instanceof
             * BitmapDrawable) { BitmapDrawable bitmapDrawable =
             * (BitmapDrawable) drawable; Bitmap bitmap =
             * bitmapDrawable.getBitmap(); bitmap.recycle(); }
             */
            mArtistImageView.setImageDrawable(null);
            mArtistImageView.setImageBitmap(b);
            
            /*
            mArtistImageView.setVisibility(View.INVISIBLE);
            
            Animation fadeInAnimation = new AlphaAnimation(0.00f, 1.00f);
            fadeInAnimation.setDuration(300);
            fadeInAnimation.setAnimationListener(new AnimationListener()
            {

                public void onAnimationStart(Animation animation)
                {
                }

                public void onAnimationRepeat(Animation animation)
                {
                }

                public void onAnimationEnd(Animation animation)
                {
                    mArtistImageView.setVisibility(View.VISIBLE);

                    /*
                    RotateAnimation anim = new RotateAnimation(0f, 350f, (float) (mArtistImageView.getWidth() / 2), (float) (mArtistImageView.getHeight() / 2));
                    //RotateAnimation anim = new RotateAnimation(ROTATE_FROM, ROTATE_TO, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    anim.setInterpolator(new LinearInterpolator());
                    anim.setRepeatCount(Animation.INFINITE);
                    anim.setDuration(10000);
                    mArtistImageView.startAnimation(anim);
                    
                } 
                
            });
            
            mArtistImageView.startAnimation(fadeInAnimation);
            */

            
            showProgressBar(false);
        }
    }

    // private CountDownTimer timer = null;
    public void onButtonClick(View target)
    {
        if (mMusicService != null)
        {
            MainActivity ma = (MainActivity) this.getActivity();
            // Log.d(TAG, "onButtonClick() - Network State=" +
            // ma.getNetworkState());

            Intent i = null;

            if (ma.getNetworkState() == NetworkConnectivityListener.State.CONNECTED)
            {
                // Send the correct intent to the MusicService, according to the
                // button that was clicked

                if (target == mPlayPauseButton)
                {
                    i = new Intent(MusicService.ACTION_TOGGLE_PLAYBACK);
                    // mMusicService.setStation(mStation);
                }
                else if (target == mNextButton)
                {
                    // Log.d(TAG, "Disable buttons for " +
                    // TIME_TO_RELEASE_BUTTON);
                    enableButtons(false);
                    this.setRepeatSong(false);

                    CountDownTimer timer = new CountDownTimer(TIME_TO_RELEASE_BUTTON, 1000)
                    {
                        public void onFinish()
                        {
                            // Log.d(TAG, TIME_TO_RELEASE_BUTTON +
                            // " seconds passed. Re-enable buttons");
                            enableButtons(true);
                        }

                        @Override
                        public void onTick(long l)
                        {
                        }
                    };
                    timer.start();

                    i = new Intent(MusicService.ACTION_SKIP);
                    i.putExtra(MusicService.FROM_USER_TAG, true);
                }
                else if (target == mPrevButton)
                {
                    enableButtons(false);

                    CountDownTimer timer = new CountDownTimer(TIME_TO_RELEASE_BUTTON, 1000)
                    {
                        public void onFinish()
                        {
                            enableButtons(true);
                        }

                        @Override
                        public void onTick(long l)
                        {
                        }
                    };
                    timer.start();

                    i = new Intent(MusicService.ACTION_PREV);
                }

                ma.startMusicService(i);
            }
            else
            {
                if (target == mPlayPauseButton && mMusicService.getState() == MusicService.State.Playing)
                {
                    i = new Intent(MusicService.ACTION_TOGGLE_PLAYBACK);
                    ma.startMusicService(i);
                }
                else
                {
                    Toast.makeText(this.getActivity(), getString(R.string.msg_err_connection_error), Toast.LENGTH_SHORT).show();
                }
            }
        }
        else
        {
            // wait for music service to connect, do nothing.
        }
    }

    public void updateView(AudioItem ai, boolean updateBitmap)
    {
        if (ai != null && this.isAdded())
        {
            // Log.d(TAG, "updateView() - updateBitmap=" + updateBitmap);
            mStation = mMusicService.getStation();
            MainActivity ma = (MainActivity) this.getActivity();
            ZingRadioDatabaseHelper db = ZingRadioDatabaseHelper.getInstance(ma);

            if (mStation.getId().equals(Station.MY_SONGS_ID))// Favorite station
            {
                mFavChannelButton.setVisibility(View.GONE);
            }
            else if (mStation.getId().equals(Station.SEARCH_ID))// Favorite
                                                                // station
            {
                mFavChannelButton.setVisibility(View.GONE);
            }
            else if (db.isStationFavorite(mStation.getId()))// Station is
                                                            // favorited
            {
                mFavChannelButton.setVisibility(View.VISIBLE);
                mFavChannelButton.setSelected(true);
                mFavChannelButton.setEnabled(true);
            }
            else
            {
                mFavChannelButton.setVisibility(View.VISIBLE);
                mFavChannelButton.setSelected(false);
                mFavChannelButton.setEnabled(true);
            }

            if (db.isSongFavorite(ai.getId()))
            {
                //mHeartButton.setImageResource(R.drawable.img_btn_heart_on);
                mHeartButton.setSelected(true);
            }
            else
            {
                //mHeartButton.setImageResource(R.drawable.img_btn_heart_off);
                mHeartButton.setSelected(false);
            }

            if (updateBitmap && ma.getNetworkState() == NetworkConnectivityListener.State.CONNECTED)
            {
                if (ai.getThumbnail() != null)
                {
                    mGetPerformerCoverAsync = new GetPerformerCoverAsync(this);
                    mGetPerformerCoverAsync.execute(ai.getThumbnail());
                    showProgressBar(true);
                }
                else
                {
                    Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.img_song_loading_thumbnail);
                    final ImageView iv = (ImageView) viewRoot.findViewById(R.id.imageViewPerformerCover);
                    iv.setImageBitmap(b);
                }
            }

            mChannelNameTextView.setText(this.mStation.getName());

            TextView tv_song_title = (TextView) viewRoot.findViewById(R.id.tv_song_title);
            tv_song_title.setText(ai.getTitle());

            TextView tv_song_performer = (TextView) viewRoot.findViewById(R.id.tv_song_performer);
            tv_song_performer.setText(ai.getPerformer());

            if (mMusicService.getState() == MusicService.State.Playing || mMusicService.getState() == MusicService.State.Preparing)// if
                                                                                                                                   // playing,
                                                                                                                                   // pause
                                                                                                                                   // it
            {
                // mPlayPauseButton.setImageBitmap(BitmapFactory.decodeResource(getResources(),
                // R.drawable.btn_pause));
                //mPlayPauseButton.setImageResource(R.drawable.btn_pause);
                mPlayPauseButton.setSelected(true);
            }
            else
            // if not playing
            {
                // mPlayPauseButton.setImageBitmap(BitmapFactory.decodeResource(getResources(),
                // R.drawable.btn_play));
                //mPlayPauseButton.setImageResource(R.drawable.btn_play);
                mPlayPauseButton.setSelected(false);
            }

            /*
             * if(!mDownloading && !mDownloaded) { if(ai.isStreaming()) {
             * mDownloadButton.setVisibility(View.VISIBLE); } else {
             * mDownloadButton.setVisibility(View.GONE); } }
             */

            //ma.updatePlayPauseButtonImage(mMusicService.getState());

            //mLyricsTextView.setText(ai.getLyrics());
            String l = ai.getLyrics();
            String lineSep = System.getProperty("line.separator");

            String head = Constants.WEB_VIEW_CSS;
            if (l == null || l.equals("-2") || l.isEmpty())
            {
                l = "<center>" + getString(R.string.msg_lyrics_not_available) + "</center>";
            }
            else
            {
                l = l.replaceAll(lineSep, "<br>");
            }

            l = "<html><head><style type='text/css'>" + head + "</style></head><body>" + l + "<br></body></html>";

            mLyricsWebView.getSettings().setDefaultTextEncodingName("utf-8");
            mLyricsWebView.loadDataWithBaseURL("file:///android_asset/", l, "text/html", "utf-8", null);

            //Log.d(TAG, "updateview() - 1");
            if (mSonglistLayout.getVisibility() == View.VISIBLE)
            {
                loadSongList();
            }

            if(ai.getUrlType() == AudioItem.URL_SDCARD || !ai.isStreaming()) this.setSongSeekbarSecondaryProgress(100);
        }
    }

    public void playAt(int pos)
    {

        //Log.d(TAG, "playAt() - isLoadindSong=" + isLoadindSong);
        //isLoadindSong = true;
        MainActivity ma = (MainActivity) this.getActivity();
        if (mStation.getAudioItems() != null)
        {
            Log.d(TAG, "playAt() - Pos=" + pos + " Count=" + mStation.getAudioItems().size());
            ma.mMusicService.setStation(mStation);
            // mMusicService.mPlayingItem = mStation.getAudioItems().get(pos);
            this.setRepeatSong(false);
            Intent i = new Intent(MusicService.ACTION_PLAY_AT);
            i.putExtra("Pos", pos);
            i.putExtra(MusicService.FROM_USER_TAG, true);
            i.putExtra("Station", mStation);
            ma.mMusicService.setPlayerFragment(this);
            ma.startMusicService(i);
            // mMusicService.startPlaylist();
        }
        else
        {
            Log.d(TAG, "playAt() - Play suggested.");
            playStation(StationSuggestion.getSuggestedStation(getActivity()));
        }
    }

    //private boolean isLoadindSong = false;

    public void enableButtons(boolean enable)
    {
        mPlayPauseButton.setClickable(enable);
        mNextButton.setClickable(enable);
        mSeekbar.setEnabled(enable);
        if (mFavChannelButton != null) mFavChannelButton.setClickable(enable);
        mHeartButton.setClickable(enable);
        mSonglistListView.setClickable(enable);
        mSonglistListView.setEnabled(enable);
        mArtistImageView.setClickable(enable);
        // mDownloadButton.setClickable(enable);
        showProgressBar(!enable);
    }

    /*
     * public void setDownloadFlags(boolean enable) { mDownloaded = enable;
     * mDownloading = enable; }
     */

    public void enableSeekbar(boolean enable)
    {
        mSeekbar.setEnabled(enable);
    }

    @Override
    public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2)
    {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekbar)
    {
        // remove message Handler from updating progress bar
        mSeekbarHandler.removeCallbacks(mUpdateTimeTask);

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekbar)
    {
        MainActivity ma = (MainActivity) this.getActivity();
        if (ma.getNetworkConnectivityListener().getState() == NetworkConnectivityListener.State.CONNECTED)
        {
            showProgressBar(true);
            if (mMusicService != null && mMusicService.getPlayer() != null)
            {
                mSeekbarHandler.removeCallbacks(mUpdateTimeTask);
                int totalDuration = mMusicService.getPlayer().getDuration();
                int currentPosition = Utilities.progressToTimer(seekbar.getProgress(), totalDuration);

                // forward or backward to certain seconds
                mMusicService.getPlayer().seekTo(currentPosition);

                // update timer progress again
                updateProgressBar();
            }
        }
        else
        {
            Toast.makeText(this.getActivity(), getString(R.string.msg_err_connection_error), Toast.LENGTH_SHORT).show();
        }
    }

    public void showProgressBar(boolean show)
    {
        int vis = show ? View.VISIBLE : View.INVISIBLE;
        mLoadingIndicator.setVisibility(vis);
    }

    public void playStation(Station st)
    {
        // Log.d(TAG, "playStation() - Station=" + st.getId() + " " + st.getName());
        enableButtons(false);
        if(mSonglistLayout != null) mSonglistLayout.setVisibility(View.GONE);
        mSonglist = null;
        if (mSongAdapter != null) mSongAdapter.notifyDataSetChanged();
        mStation = st;

        switch (mStation.getType())
        {
            case Radio:
            {
                if (mGetStationAudioListAsync != null)
                {
                    if (mGetStationAudioListAsync.getStatus() != Status.FINISHED)
                    {
                        mGetStationAudioListAsync.cancel(true);
                    }
                }

                mGetStationAudioListAsync = new GetStationAudioListAsync(this);
                mGetStationAudioListAsync.execute(mStation.getServerId());

                break;
            }
            case Album:
            {
                if (mGetZingPlaylistDetailAsync != null)
                {
                    if (mGetZingPlaylistDetailAsync.getStatus() != Status.FINISHED)
                    {
                        mGetZingPlaylistDetailAsync.cancel(true);
                    }
                }

                mGetZingPlaylistDetailAsync = new GetZingPlaylistDetailAsync(this);
                mGetZingPlaylistDetailAsync.execute(mStation.getId());

                break;
            }
        }
    }

    public void playStationSearchedItems(String tag, Station st)
    {
        //Log.d(TAG, "playStationSearchedItem()");
        //Log.d(TAG, "mStation=" + mStation + " st=" + st);
        mStation = st;
        mSonglistListView.setAdapter(mSongAdapter);
        if (this.isAdded())
        {
            MainActivity ma = (MainActivity) getActivity();
            ma.mMusicService.setStation(mStation);
            ma.mMusicService.setPlayerFragment(this);
            //mMusicService.setPlayerFragment(this);
            setRepeatSong(false);
            Intent i = new Intent(MusicService.ACTION_START_PLAYLIST);
            i.putExtra(MusicService.FROM_FRAGMENT_TAG, tag);
            i.putExtra("Station", mStation);
            ma.startMusicService(i);
            mSonglist = mStation.getAudioItems();
            mSongAdapter.setSongs(mSonglist);
        }
        else
        {
            Log.e(TAG, "Fragment not attached to activity");
        }
    }

    @Override
    public void onGetStationAudioListAsyncComplete(ArrayList<AudioItem> al)
    {
        mStation.setAudioItems(al);
        playStationSearchedItems(PlayerFragment.TAG, mStation);

        // Log.d(TAG, "onGetStationAudioListAsyncCompleted() - Station name=" +
        // mStation.getName() + " Count=" + mStation.getAudioItems().size());
    }

    @Override
    public void onGetStationAudioListAsyncError(int errCode, String errMsg)
    {
        if (this.isAdded()) Toast.makeText(this.getActivity(), getString(R.string.msg_err_play_list_empty), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void OnDownloadComplete(AudioItem ai, long downloadId)
    {
        mDownloadItem = ai;
        if (mDownloadItem == null) return;

        // Prepare intent which is triggered if the notification is selected
        Intent i = new Intent(getActivity(), MainActivity.class);

        int statusCode = mFileDownloadManager.getStatusCode(downloadId);
        String statusMsg = mFileDownloadManager.getStatus(downloadId);
        if (statusCode == DownloadManager.STATUS_SUCCESSFUL)
        {
            Log.d(TAG, "msg=" + statusMsg);
            i.putExtra(MainActivity.INTENT, MainActivity.DOWNLOAD_INTENT);
            MainActivity ma = (MainActivity) this.getActivity();
            ma.mAlreadyShowMySongs = false;
        }

        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pi = PendingIntent.getActivity(getActivity(), 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notif = new Notification();
        notif.tickerText = statusMsg;// getString(R.string.download_complete);
        notif.icon = R.drawable.img_btn_download;
        notif.flags |= Notification.FLAG_AUTO_CANCEL;
        notif.setLatestEventInfo(this.getActivity(), mDownloadItem.getTitle() + " - " + mDownloadItem.getPerformer(), statusMsg, pi);
        notif.contentIntent = pi;

        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

        // Hide the notification after its selected
        notificationManager.notify((int) downloadId, notif);
    }

    @Override
    public void OnDownloadNotificationClick(AudioItem ai, long downloadId)
    {
        // getActivity().startActivity(new
        // Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
        Toast.makeText(this.getActivity(), getString(R.string.download_notification_click), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onServiceConnected(MusicService service)
    {
        //this.mMusicService = service;
        if (mMusicService != null)
        {
            int currVol = mMusicService.getAudioManager().getStreamVolume(AudioManager.STREAM_MUSIC);
            setVolume(currVol);
        }
    }

    @Override
    public void onServiceDisconnected()
    {
    }

    @Override
    public void onNext(AudioItem nextAudioItem)
    {
    }

    @Override
    public void onPrev(AudioItem nextAudioItem)
    {
    }

    @Override
    public void onPlay(AudioItem audioItem, boolean updateBitmap)
    {
        if (!this.isAdded()) return;
        this.updateView(audioItem, false);
    }

    @Override
    public void onPause(AudioItem audioItem)
    {
    }

    @Override
    public void onStop(AudioItem lastAudioItem)
    {
    }

    @Override
    public void onError(int errCode)
    {
        if (!this.isAdded()) return;
        switch (errCode)
        {
            case MusicService.ERROR_CODE_CONNECTION_ERROR:
            {
                // Toast.makeText(this.getActivity(),
                // Constants.ERR_CONNECTION_ERROR, Toast.LENGTH_SHORT).show();
                break;
            }
            case MusicService.ERROR_CODE_BAD_SONG_URL:
            {
                // Toast.makeText(this.getActivity(),
                // getString(R.string.msg_err_song_bad_link),
                // Toast.LENGTH_SHORT).show();
                enableButtons(false);
                showProgressBar(false);
                enableSeekbar(false);
                //mHeartButton.setImageResource(R.drawable.img_btn_heart_off);
                mHeartButton.setSelected(true);
                mPlayPauseButton.setEnabled(true);
                mPlayPauseButton.setImageResource(R.drawable.btn_pause);
                break;
            }
            case MusicService.ERROR_CODE_SONG_DELETED_ON_SERVER:
            {
                // Toast.makeText(this.getActivity(),
                // getString(R.string.msg_err_song_deleted_on_server),
                // Toast.LENGTH_SHORT).show();
                enableButtons(false);
                showProgressBar(false);
                enableSeekbar(false);
                //mHeartButton.setImageResource(R.drawable.img_btn_heart_off);
                mHeartButton.setSelected(true);
                mPlayPauseButton.setEnabled(true);
                mPlayPauseButton.setImageResource(R.drawable.btn_pause);
                break;
            }
            case MusicService.ERROR_CODE_SLOW_CONNECTION:
            {
                // Toast.makeText(this.getActivity(),
                // getString(R.string.msg_err_slow_connection),
                // Toast.LENGTH_SHORT).show();
                break;
            }
            case MusicService.ERROR_CODE_PLAY_LIST_EMPTY:
            {
                // Toast.makeText(this.getActivity(),
                // getString(R.string.msg_err_play_list_empty),
                // Toast.LENGTH_SHORT).show();
                break;
            }
            case MusicService.ERROR_CODE_SONG_DROPPED:
            {
                this.enableButtons(true);
                // Toast.makeText(this.getActivity(),
                // getString(R.string.msg_err_song_dropped),
                // Toast.LENGTH_SHORT).show();
                break;
            }
            case MusicService.ERROR_CODE_MEDIA_INFO_NOT_SEEKABLE:
            {
                // Toast.makeText(this.getActivity(),
                // getString(R.string.msg_err_media_not_seekable),
                // Toast.LENGTH_LONG).show();
                break;
            }
            case MusicService.ERROR_CODE_MEDIA_INFO_NETWORK_BANDWIDTH:
            {
                // Toast.makeText(this.getActivity(),
                // getString(R.string.msg_err_media_info_network_bandwidth),
                // Toast.LENGTH_LONG).show();
                break;
            }
        }
    }

    public boolean isRepeatSong()
    {
        return mRepeatSong;
    }

    public void setRepeatSong(boolean mRepeatSong)
    {
        this.mRepeatSong = mRepeatSong;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        Editor editor = sharedPref.edit();
        editor.putBoolean(REPEAT_SONG_KEY, mRepeatSong);
        editor.commit();
        if (mRepeatSongButton != null)
        //mRepeatSongButton.setImageResource(mRepeatSong?R.drawable.img_btn_repeat_song_on:R.drawable.img_btn_repeat_song_off);
        mRepeatSongButton.setSelected(mRepeatSong ? true : false);
    }

    @Override
    public void OnGetZingPlaylistDetailAsyncComplete(Playlist pl)
    {
        if (pl.getAudioItems() != null && pl.getAudioItems().size() > 0)
        {
            mStation.setAudioItems(pl.getAudioItems());
            mStation.setName(pl.getTitle());
            //mStation.setName(pl.getTitle() + " - " + pl.getArtistName());
            mStation.setCategoryName(Station.ALBUM_NAME);
            mStation.setId(pl.getId());
            mStation.setType(Station.Type.Album);
            playStationSearchedItems(PlayerFragment.TAG, mStation);
        }
    }

    @Override
    public void OnGetZingPlaylistDetailAsyncError(int errCode, String errMsg)
    {
        Log.e(TAG, "errCode=" + errCode + " errMsg=" + errMsg);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupCustomAnimations()
    {
        // Changing while Adding
        PropertyValuesHolder pvhLeft = PropertyValuesHolder.ofInt("left", 0, 1);
        PropertyValuesHolder pvhTop = PropertyValuesHolder.ofInt("top", 0, 1);
        PropertyValuesHolder pvhRight = PropertyValuesHolder.ofInt("right", 0, 1);
        PropertyValuesHolder pvhBottom = PropertyValuesHolder.ofInt("bottom", 0, 1);
        PropertyValuesHolder pvhScaleX = PropertyValuesHolder.ofFloat("scaleX", 1f, 0f, 1f);
        PropertyValuesHolder pvhScaleY = PropertyValuesHolder.ofFloat("scaleY", 1f, 0f, 1f);
        final ObjectAnimator changeIn = ObjectAnimator.ofPropertyValuesHolder(this, pvhLeft, pvhTop, pvhRight, pvhBottom, pvhScaleX, pvhScaleY).setDuration(
                mTransitioner.getDuration(LayoutTransition.CHANGE_APPEARING));
        mTransitioner.setAnimator(LayoutTransition.CHANGE_APPEARING, changeIn);
        changeIn.addListener(new AnimatorListenerAdapter()
        {
            public void onAnimationEnd(Animator anim)
            {
                View view = (View) ((ObjectAnimator) anim).getTarget();
                view.setScaleX(1f);
                view.setScaleY(1f);
            }
        });

        // Changing while Removing
        Keyframe kf0 = Keyframe.ofFloat(0f, 0f);
        Keyframe kf1 = Keyframe.ofFloat(.9999f, 360f);
        Keyframe kf2 = Keyframe.ofFloat(1f, 0f);
        PropertyValuesHolder pvhRotation = PropertyValuesHolder.ofKeyframe("rotation", kf0, kf1, kf2);
        final ObjectAnimator changeOut = ObjectAnimator.ofPropertyValuesHolder(this, pvhLeft, pvhTop, pvhRight, pvhBottom, pvhRotation).setDuration(
                mTransitioner.getDuration(LayoutTransition.CHANGE_DISAPPEARING));
        mTransitioner.setAnimator(LayoutTransition.CHANGE_DISAPPEARING, changeOut);
        changeOut.addListener(new AnimatorListenerAdapter()
        {
            public void onAnimationEnd(Animator anim)
            {
                View view = (View) ((ObjectAnimator) anim).getTarget();
                view.setRotation(0f);
            }
        });

        // Adding
        ObjectAnimator animIn = ObjectAnimator.ofFloat(null, "rotationY", 90f, 0f).setDuration(mTransitioner.getDuration(LayoutTransition.APPEARING));
        mTransitioner.setAnimator(LayoutTransition.APPEARING, animIn);
        animIn.addListener(new AnimatorListenerAdapter()
        {
            public void onAnimationEnd(Animator anim)
            {
                View view = (View) ((ObjectAnimator) anim).getTarget();
                view.setRotationY(0f);
            }
        });

        // Removing
        ObjectAnimator animOut = ObjectAnimator.ofFloat(null, "rotationX", 0f, 90f).setDuration(mTransitioner.getDuration(LayoutTransition.DISAPPEARING));
        mTransitioner.setAnimator(LayoutTransition.DISAPPEARING, animOut);
        animOut.addListener(new AnimatorListenerAdapter()
        {
            public void onAnimationEnd(Animator anim)
            {
                View view = (View) ((ObjectAnimator) anim).getTarget();
                view.setRotationX(0f);
            }
        });
    }

    private void showHideSonglist(int view)
    {
        //int view = (mSonglistLayout.getVisibility() == View.VISIBLE)?View.GONE:View.VISIBLE;
        mSonglistLayout.setVisibility(view);
    }

    private void loadSongList()
    {
        Log.d(TAG, "loadSongList() - start");
        final MainActivity ma = (MainActivity) getActivity();
        if (ma.getPlayingItem() != null)
        {
            ma.runOnUiThread(new Runnable()
            {
                public void run()
                {
                    mSonglist = mMusicService.getStation().getAudioItems();
                    mSongAdapter.setSongs(mSonglist);
                    mSongAdapter.mPlayingItem = mMusicService.mPlayingItem;
                    mSongAdapter.notifyDataSetChanged();
                    int index = mMusicService.mPlayingIndex + 1;
                    mSongPlaylistInfo.setText(("Playing " + index) + "/" + mSonglist.size());
                    scrolltoSong(mSongAdapter.mPlayingItem.getId());
                }
            });
        }
    }

    private void scrolltoSong(String id)
    {
        if (mSonglist != null)
        {
            int p = -1;

            for (int i = 0; i < mSonglist.size(); i++)
            {
                AudioItem ai = (AudioItem) mSonglist.get(i);
                if (ai.getId().equals(id))
                {
                    p = i;
                    break;
                }
            }

            if (p >= 0)
            {
                final int pos = p;
                mSonglistListView.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        mSonglistListView.smoothScrollToPosition(pos);
                    }
                });
            }
        }
    }

    private void showHideVolume(boolean show)
    {
        if (show)
        {
            Animation animation = new AlphaAnimation(0.0f, 1.0f);
            animation.setDuration(200);
            mVolumeLayout.startAnimation(animation);
            mVolumeLayout.setVisibility(View.VISIBLE);
            mVolumeTimer.start();
        }
        else
        {
            Animation animation = new AlphaAnimation(1.0f, 0.0f);
            animation.setDuration(200);
            mVolumeLayout.startAnimation(animation);
            mVolumeLayout.setVisibility(View.GONE);
            mVolumeTimer.cancel();
        }
    }

    //private int lastPercent = 0;

    public void setSongSeekbarSecondaryProgress(int percent)
    {
        //Log.d(TAG, "loading " + percent + " lastPercent=" + lastPercent);
        mSeekbar.setSecondaryProgress(percent);

        /*
        if (isAdded() && lastPercent != percent)
        {
            //Log.d(TAG, "do update");
            lastPercent = percent;
            mSeekbar.setSecondaryProgress(lastPercent);
        }
        */
    }
}
