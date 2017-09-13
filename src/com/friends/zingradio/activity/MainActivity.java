package com.friends.zingradio.activity;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.*;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.*;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.facebook.*;
import com.facebook.model.GraphUser;
import com.friends.zingradio.R;
import com.friends.zingradio.ZingRadioApplication;
import com.friends.zingradio.data.ZingRadioDatabaseHelper;
import com.friends.zingradio.entity.AudioItem;
import com.friends.zingradio.entity.Radio;
import com.friends.zingradio.entity.Station;
import com.friends.zingradio.fragment.*;
import com.friends.zingradio.media.MusicService;
import com.friends.zingradio.media.MusicService.MusicServiceBinder;
import com.friends.zingradio.util.Constants;
import com.friends.zingradio.util.NetworkConnectivityListener;
import com.friends.zingradio.util.StationSuggestion;
import com.friends.zingradio.util.Utilities;
import com.parse.ParseAnalytics;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

public class MainActivity extends BaseActivity
{
    public static final String TAG = MainActivity.class.getSimpleName();
    public static final String INTENT = "INTENT";
    public static final String DOWNLOAD_INTENT = "DOWNLOAD_INTENT";    
    public Uri currentUri;
    private ParseUser mUser;
    private String currentFragmentTag;
    private static final String STATE_URI = "state:uri";    
    public static final int TIME_TO_RELEASE_BUTTON = 3000;//3 seconds
    private PlayerFragment mPlayerFragment;
    private StationListFragment mStationListFragment;
    public MusicService mMusicService;
    private NetworkConnectivityListener.State mNetworkState;
    private Radio mRadio = null;
    private FavoriteSongListFragment mFavoriteSongListFragment;
    private FavoriteStationListFragment mFavoriteStationListFragment;
    private SearchFragment mSearchFragment;
    private Station mStation;
    private ImageButton mFavChannelButton;
    public ImageButton getFavChannelButton()
    {
        return mFavChannelButton;
    }
    private TextView mChannelNameTextView;
    public TextView getChannelNameTextView()
    {
        return mChannelNameTextView;
    }
    
    private ImageButton mChannelListButton;
    
    public NetworkConnectivityListener.State getNetworkState()
    {
        return mNetworkState;
    }

    public void setNetworkState(NetworkConnectivityListener.State mNetworkState)
    {
        this.mNetworkState = mNetworkState;
    }
    
    private Handler mNetWorkHandler = null;
    private View mActionBarView;
    
    //facebook objects
    private boolean mIsResumed = false;
    private UiLifecycleHelper mUiHelper;
    private Session.StatusCallback mCallback = new Session.StatusCallback()
    {
        @Override
        public void call(Session session, SessionState state, Exception exception)
        {
            onSessionStateChange(session, state, exception);
        }
    };

    private Session mFBSession;
    public void setSession(Session mFBSession)
    {
        this.mFBSession = mFBSession;
    }

    public Session getSession()
    {
        return mFBSession;
    }
    
    private FacebookLoginFragment mFacebookLoginFragment;
    private LiveFeedFragment mLiveFeedFragment;
    private String currentRightFragmentTag = FacebookLoginFragment.TAG;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        ZingRadioApplication app = (ZingRadioApplication)this.getApplication();
        app.loadAll();
        mRadio = app.getRadio();
        
        ParseAnalytics.trackAppOpened(getIntent());
        
        //SharedPreferences savedFBSession = getSharedPreferences(Constants.FACEBOOK_SESSION_PREF, this.MODE_PRIVATE);
        mFBSession = Session.getActiveSession();
        
        mUiHelper = new UiLifecycleHelper(this, mCallback);
        mUiHelper.onCreate(savedInstanceState);
        
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mActionBarView = this.getLayoutInflater().inflate(R.layout.main_action_bar, null);
        mActionBar.setCustomView(mActionBarView);
        mFavChannelButton = (ImageButton)mActionBarView.findViewById(R.id.btn_add_channel_fav);
        mChannelNameTextView = (TextView)mActionBarView.findViewById(R.id.textViewStationName);
        mChannelNameTextView.setOnClickListener(new OnClickListener()
        {
            
            @Override
            public void onClick(View v)
            {
                mSlidingMenu.showMenu();
            }
        });
        mChannelListButton = (ImageButton)mActionBarView.findViewById(R.id.btn_channel_list);
        mChannelListButton.setOnClickListener(new OnClickListener()
        {
            
            @Override
            public void onClick(View v)
            {
                mSlidingMenu.showMenu();
            }
        });
        
        mFavChannelButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                if (mMusicService != null)
                {
                    mStation = mMusicService.getStation();
                }

                if (mStation == null) return;

                ZingRadioDatabaseHelper db = ZingRadioDatabaseHelper.getInstance(MainActivity.this);

                // long newRowId =
                // db.toggleFavoriteSong(mMusicService.mPlayingItem);
                long newRowId = db.toggleFavoriteStation(mStation);
                // Log.d(TAG, "newrowId=" + newRowId);
                if (newRowId == -1)
                {
                    Toast.makeText(MainActivity.this, getString(R.string.msg_err_insert_channel), Toast.LENGTH_SHORT).show();
                }
                else if (newRowId == -99)
                {
                    Toast.makeText(MainActivity.this, getString(R.string.msg_removed_from_my_channels), Toast.LENGTH_SHORT).show();
                    mFavChannelButton.setSelected(false);
                    mFavChannelButton.setSelected(false);
                }
                else
                {
                    Toast.makeText(MainActivity.this,getString(R.string.msg_added_to_my_channels), Toast.LENGTH_SHORT).show();
                    mFavChannelButton.setSelected(true);
                    mFavChannelButton.setSelected(true);
                }
            }
        });

        setContentView(R.layout.content_frame);
        
        if (savedInstanceState == null)
        {
            mPlayerFragment = new PlayerFragment();
            getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.content_frame, mPlayerFragment, PlayerFragment.TAG)
            .commit();
        }
        else
        {
            mPlayerFragment = (PlayerFragment) this.getFragment(this.getSupportFragmentManager(), PlayerFragment.TAG);
        }

        mNetworkListener =  new NetworkConnectivityListener();
        mNetworkListener.startListening(this);

        mNetWorkHandler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                mNetworkState = mNetworkListener.getState();
                //Log.d(TAG, "handleMessage() - " + mNetworkState);
                
                if(mMusicService != null)
                {
                    ZingRadioApplication app = (ZingRadioApplication)MainActivity.this.getApplication();
                    app.loadAll();
                    MainActivity.this.mRadio = app.getRadio();
                    mMusicService.setRadio(mRadio);
                    
                    if(mNetworkState == NetworkConnectivityListener.State.CONNECTED)
                    {
                        //Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                        //mPlayerFragment.enableButtons(true);

                        if(mPlayerFragment != null)
                        {
                            mPlayerFragment.enableSeekbar(true);
                            mPlayerFragment.enableButtons(true);
                            mPlayerFragment.updateView(mMusicService.mPlayingItem, true);
                            //Log.d(TAG, "handleMessage() - Music server state=" + mMusicService.getState());
                            mMusicService.setNetworkState(NetworkConnectivityListener.State.CONNECTED);
                            
                            if(mMusicService.getState() == MusicService.State.Stopped)
                            {
                                if(mPlayerFragment.getStation() == null)
                                {
                                    //Log.d(TAG, "Play suggested station");
                                    mPlayerFragment.playStation(StationSuggestion.getSuggestedStation(MainActivity.this));
                                    //MainActivity.this.playSuggestedStation();
                                }
                                else//playing or pause
                                {
                                    if(mMusicService.mPlayingItem != null)
                                    {
                                        mMusicService.resumePlay();    
                                    }
                                    else
                                    {
                                        mPlayerFragment.playStation(StationSuggestion.getSuggestedStation(MainActivity.this));
                                    }
                                }
                            }
                        }
                        else
                        {
                            Log.d(TAG, "Fragment not created. Do nothing");
                        }
                    }
                    //disconnected
                    else //if(mNetworkListener.getState() == NetworkConnectivityListener.State.NOT_CONNECTED || mNetworkListener.getState() == NetworkConnectivityListener.State.UNKNOWN)
                    {
                        //Log.d(TAG, Constants.ERR_CONNECTION_ERROR + " " + mNetworkListener.getState());
                        Toast.makeText(MainActivity.this, getString(R.string.msg_err_connection_error), Toast.LENGTH_LONG).show();
                        mPlayerFragment.enableSeekbar(false);
                        mMusicService.setNetworkState(NetworkConnectivityListener.State.NOT_CONNECTED);
                    }
                }
                else //if music service null
                {
                    //Toast.makeText(MainActivity.this, "Loading songs...", Toast.LENGTH_LONG).show();
                    //Log.d(TAG, "Music service not started. Do nothing");   
                }
            }
        };
        
        mNetworkListener.registerHandler(mNetWorkHandler, 1);
        
        try
        {
            if(savedInstanceState != null)
            {
                if(savedInstanceState.getString(STATE_URI) != null)
                {
                    currentUri = Uri.parse(savedInstanceState.getString(STATE_URI));
                    //Log.d(TAG, "currentUri=" + currentUri.toString());
                }
            }
            else currentUri = PlayerFragment.URI;
        }
        catch(Exception e)
        {
            Log.e(TAG, "Error parsing savedInstanceState - " + e.getMessage());
            currentUri = PlayerFragment.URI;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        /*
        mFavChannelMenuItem = menu.add("FavChannel"); 
        mFavChannelMenuItem.setIcon(R.drawable.img_btn_star_off);
        mFavChannelMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);*/
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        return super.onOptionsItemSelected(item);
    }

    public void showAboutDialog()
    {
        //Log.i(TAG, "showAboutDialog()");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String s = Utilities.getAboutHtml(this);
        //builder.setMessage(Html.fromHtml(message)).setTitle(R.string.app_name);
        String ver = null;
        try
        {
            PackageManager manager = this.getPackageManager();
            PackageInfo pInfo = manager.getPackageInfo(this.getPackageName(), 0);
            ver = pInfo.versionName;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.e(TAG, "showAboutDialog() - Error=" + e.getMessage());
        }

        builder.setMessage(Html.fromHtml(s)).setTitle(getString(R.string.app_name) + " " + ver);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    
    private NetworkConnectivityListener mNetworkListener = null;
    
    public NetworkConnectivityListener getNetworkConnectivityListener()
    {
        return mNetworkListener;
    }
    
    @Override
    protected void onDestroy()
    {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();
        //System.runFinalizersOnExit(true);
        mNetworkListener.unregisterHandler(mNetWorkHandler);
        mNetworkListener.stopListening();
        mUiHelper.onDestroy();
    }

    
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
      //Log.i(TAG, "onSaveInstanceState()");
        super.onSaveInstanceState(outState);
        outState.putString(STATE_URI, currentUri.toString());
        mUiHelper.onSaveInstanceState(outState);
    }

    private ServiceConnection mMusicServiceConnection = new ServiceConnection()
    {
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            Log.d(TAG, "Music service connected.");
            MusicServiceBinder binder = (MusicServiceBinder)service;
            mMusicService = (MusicService)binder.getService();
            ZingRadioApplication app = (ZingRadioApplication)MainActivity.this.getApplication();
            app.loadAll();
            app.setMusicService((MusicService)binder.getService());
            if(mRadio == null)
            {
                mRadio = app.getRadio();
            }
            mMusicService.setRadio(mRadio);
            
            if(mPlayerFragment != null)
            {
                mMusicService.setPlayerFragment(mPlayerFragment);
                mMusicService.addListener(mPlayerFragment);
                mPlayerFragment.setMusicService(mMusicService);
                mPlayerFragment.onServiceConnected(mMusicService);
            }
            
            if(mNetworkState == NetworkConnectivityListener.State.CONNECTED)
            {
               // Log.d(TAG, "onServiceConnected() 1 ");
                mMusicService.setNetworkState(mNetworkState);
                if(mMusicService.getState() == MusicService.State.Stopped)
                {
                    //Log.d(TAG, "onServiceConnected() 2 ");
                    mPlayerFragment.playStation(StationSuggestion.getSuggestedStation(MainActivity.this));
                    //mPlayPauseButton.startAnimation(mActionButtonAnimation);
                }
                else
                {
                    //Log.d(TAG, "onServiceConnected() 3 ");

                    AudioItem ai = mMusicService.mPlayingItem;
                    if(ai != null)
                    {
                        //Log.d(TAG, "onServiceConnected() 4 ");
                        if(mPlayerFragment != null && mPlayerFragment.isVisible())
                        {
                            mPlayerFragment.updateView(ai, true);    
                        }
                    }
                    //mPlayerFragment.setStation(mMusicService.getStation());
                }
            }
            else
            {
                //network not connected
                //Toast.makeText(MainActivity.this, getString(R.string.msg_err_connection_error), Toast.LENGTH_SHORT).show();
                //Toast.makeText(MainActivity.this, "4", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0)
        {
            Log.d(TAG, "Service disconnected.");
        }
    };
    
    public void playSuggestedStation()
    {
        mPlayerFragment.playStation(StationSuggestion.getSuggestedStation(this));
    }
    
    public void startMusicService(Intent i)
    {
        //Log.d(TAG, "startMusicService() - Intent action=" + i.getAction());
        if(mMusicService != null) mMusicService.startService(i);
        else
        {
            Intent newServiceIntent = new Intent(this, MusicService.class);
            this.bindService(newServiceIntent, mMusicServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onStart()
    {
        Log.d(TAG, "onStart()");
        super.onStart();
        ZingRadioApplication app = (ZingRadioApplication)this.getApplication();
        app.loadAll();
        
        Intent i = new Intent(this, MusicService.class);
        this.bindService(i, mMusicServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop()
    {
        Log.d(TAG, "onStop()");
        if(mMusicServiceConnection != null) this.unbindService(mMusicServiceConnection);
        /*
        FragmentManager fm = getSupportFragmentManager();
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        */
        super.onStop();
    }
    
    public static final int SCREEN_PLAYER = 0;
    public static final int SCREEN_FAVORITE_SONG_LIST = 1;
    
    @Override
    protected void onPause()
    {
        Log.d(TAG, "onPause()");
        super.onPause();
        mUiHelper.onPause();
        mIsResumed = false;
        //Log.d(TAG, "onPause() - currentUri=" + currentUri.toString() );//+ " fragment=" + currentContentFragmentTag + " music state=" + mMusicService.getState().toString());
    }

    @Override
    protected void onResume()
    {
        Log.d(TAG, "onResume()");
        super.onResume();
        mUiHelper.onResume();
        mIsResumed = true;
    }
    
    @Override
    protected void onResumeFragments()
    {
        super.onResumeFragments();
        Session session = Session.getActiveSession();

        /*
        if (session != null && session.isOpened())
        {
            updateRightFrame(LiveFeedFragment.URI);
        }
        else
        {
            updateRightFrame(FacebookLoginFragment.URI);
        }
        */
    }
    
    public boolean mAlreadyShowMySongs = true;
    //this onPostResume() has to be here otherwise will get IllegalStateException: can not perform this operation after onsaveInstance
    @Override
    protected void onPostResume()
    {
        //Log.d(TAG, "onPostResume() - fragment=" + currentUri.getScheme());
        super.onPostResume();
        updateContent(currentUri);

        //onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        Log.d(TAG, "onNewIntent()");
        super.onNewIntent(intent);
        
        String val = intent.getStringExtra(MainActivity.INTENT);

        if(mAlreadyShowMySongs == false)
        {
            if(val != null)
            {
                //Log.i(TAG, "3");
                if(val.equals(MainActivity.DOWNLOAD_INTENT))
                {
                    //Log.i(TAG, "4");
                    currentUri = PlayerFragment.URI;
                    updateContent(currentUri);
                    updateLeftFrame(FavoriteSongListFragment.URI, null);
                    mSlidingMenu.showMenu();
                    mAlreadyShowMySongs = true;
                }
                //Log.i(TAG, "5");
                
            }
        }
    }
    
    //private ArrayList<Fragment> mFragmentList = new ArrayList<Fragment>();

    public Fragment updateContent(Uri uri)
    {
        Fragment fragment = null;
        if(uri != null)
        {
            Log.d(TAG, "updateContent() - Current uri = " + currentUri.toString() + " New Uri = " + uri.toString());
            String schema = uri.getScheme();
            final FragmentManager fm = getSupportFragmentManager();
            final FragmentTransaction tr = fm.beginTransaction();

            if (currentFragmentTag != null && !currentFragmentTag.isEmpty())
            {
                //Log.d(TAG, "updateContent() - Hide currentContentFragmentTag = " + fragmentName);
                final Fragment currentFragment = fm.findFragmentByTag(currentFragmentTag);
                if (currentFragment != null)
                {
                    tr.hide(currentFragment);
                }
            }

            //String authority = uri.getAuthority();
            //Log.d(TAG, "updateContent() - Schema=" + schema + " Authority=" + authority);

            if(schema.equals(PlayerFragment.TAG))
            {
                mPlayerFragment = (PlayerFragment)getFragment(fm, PlayerFragment.TAG);
                fragment = mPlayerFragment;
            }
            else if(schema.equals(StationListFragment.TAG))//other type of item, like Settings, About etc
            {
                //Log.d(TAG,"updateContent() - StationListFragment.TAG");
                mStationListFragment = (StationListFragment)getFragment(fm, StationListFragment.TAG);
                fragment = mStationListFragment;
            }
            else if(schema.equals(FavoriteStationListFragment.TAG))//other type of item, like Settings, About etc
            {
                mFavoriteStationListFragment = (FavoriteStationListFragment)getFragment(fm, FavoriteStationListFragment.TAG);
                fragment = mFavoriteStationListFragment;
            }
            else if(schema.equals(FavoriteSongListFragment.TAG))//other type of item, like Settings, About etc
            {                
                mFavoriteSongListFragment = (FavoriteSongListFragment)getFragment(fm, FavoriteSongListFragment.TAG);
                if(mMusicService != null)
                {
                    mFavoriteSongListFragment.mPlayingItem = this.getPlayingItem();
                    mMusicService.addListener(mFavoriteSongListFragment);
                }

                fragment = mFavoriteSongListFragment;
            }
            else if(schema.equals(SearchFragment.TAG))
            {
                mSearchFragment = (SearchFragment)getFragment(fm, SearchFragment.TAG);
                fragment = mSearchFragment;
            }
            else if(schema.equals(TestSearchZingSongFragment.TAG))
            {
                fragment  = (TestSearchZingSongFragment)getFragment(fm, TestSearchZingSongFragment.TAG);
            }

            //Log.d(TAG, "fragment=" + schema);
            if(fragment.isAdded())
            {
                tr.show(fragment);
            }
            else
            {
                //tr.addToBackStack(null);
                tr.add(R.id.content_frame, fragment, schema);
            }
            tr.commit();
            
            /*
            Log.d(TAG, "currentFragmentTag=" + currentFragmentTag);
            Log.d(TAG, "schema=" + schema);
            */
            //if(!currentFragmentTag.equals(schema)) mFragmentList.add(fragment);
            //Log.d(TAG, "fragmentList count=" + mFragmentList.size());

            mSlidingMenu.showContent();
            currentFragmentTag = schema;
            currentUri = uri;
        }

        //Log.d(TAG, "updateContent() - Here = " + currentUri.toString() + " New Uri = " + uri.toString());
        return fragment;
    }
    
    private String currentLeftFragmentTag = LeftMenuFragment.TAG;
    public Fragment updateLeftFrame(Uri uri, Bundle b)
    {
        Fragment fragment = null;
        if(uri != null)
        {
            //Log.d(TAG, "updateMenu() - Current uri = " + currentLeftFragmentTag.toString() + " New Uri = " + uri.toString());
            
            final FragmentManager fm = getSupportFragmentManager();
            final FragmentTransaction tr = fm.beginTransaction();
            final Fragment currentLeftFragment = fm.findFragmentByTag(currentLeftFragmentTag);
            if (currentLeftFragment != null)
            {
                tr.hide(currentLeftFragment);
            }
            
            String schema = uri.getScheme();
            if(schema.equals(LeftMenuFragment.TAG))//other type of item, like Settings, About etc
            {
                mLeftMenuFragment = (LeftMenuFragment)getFragment(fm, LeftMenuFragment.TAG);
                fragment = mLeftMenuFragment;
            }
            else if(schema.equals(StationListFragment.TAG))//other type of item, like Settings, About etc
            {
                mStationListFragment = (StationListFragment)getFragment(fm, StationListFragment.TAG);
                mStationListFragment.setArguments(b);
                fragment = mStationListFragment;
            }
            else if(schema.equals(FavoriteStationListFragment.TAG))//other type of item, like Settings, About etc
            {
                mFavoriteStationListFragment = (FavoriteStationListFragment)getFragment(fm, FavoriteStationListFragment.TAG);
                fragment = mFavoriteStationListFragment;
            }
            else if(schema.equals(FavoriteSongListFragment.TAG))//other type of item, like Settings, About etc
            {                
                mFavoriteSongListFragment = (FavoriteSongListFragment)getFragment(fm, FavoriteSongListFragment.TAG);
                if(mMusicService != null)
                {
                    mFavoriteSongListFragment.mPlayingItem = this.getPlayingItem();
                    mMusicService.addListener(mFavoriteSongListFragment);
                }

                fragment = mFavoriteSongListFragment;
            }
            else if(schema.equals(SearchFragment.TAG))
            {
                mSearchFragment = (SearchFragment)getFragment(fm, SearchFragment.TAG);
                fragment = mSearchFragment;
            }
            else if(schema.equals(TestSearchZingSongFragment.TAG))
            {
                fragment = (TestSearchZingSongFragment)getFragment(fm, TestSearchZingSongFragment.TAG);
            }
            
            //Log.d(TAG, "fragment=" + fragment + " schema=" + schema);

            if(fragment != null && fragment.isAdded())
            {
                tr.show(fragment);
            }
            else
            {
                if(currentLeftFragmentTag.equals(LeftMenuFragment.TAG) && !schema.equals(SearchFragment.TAG))
                {
                    tr.setCustomAnimations(R.anim.slide_out_left, R.anim.slide_in_right);
                }
                else if(currentLeftFragmentTag.equals(LeftMenuFragment.TAG) && schema.equals(SearchFragment.TAG))
                {
                    tr.setCustomAnimations(0, R.anim.fadeout);
                }
                else if(currentLeftFragmentTag.equals(SearchFragment.TAG) && schema.equals(LeftMenuFragment.TAG))
                {
                    tr.setCustomAnimations(0, R.anim.fadeout);
                }
                else
                {
                    tr.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
                }
                tr.replace(R.id.menu_frame_left, fragment, schema);
            }
            
            tr.commit();
            currentLeftFragmentTag = schema;
        }

        return fragment;
    }
    
    public Fragment updateRightFrame(Uri uri)
    {
        Fragment fragment = null;
        if(uri != null)
        {
            //Log.d(TAG, "updateMenu() - Current uri = " + currentLeftFragmentTag.toString() + " New Uri = " + uri.toString());
            
            final FragmentManager fm = getSupportFragmentManager();
            final FragmentTransaction tr = fm.beginTransaction();
            final Fragment currentRightFragment = fm.findFragmentByTag(currentRightFragmentTag);
            if (currentRightFragment != null)
            {
                tr.hide(currentRightFragment);
            }
            
            String schema = uri.getScheme();
            if(schema.equals(FacebookLoginFragment.TAG))
            {
                mFacebookLoginFragment = (FacebookLoginFragment)getFragment(fm, FacebookLoginFragment.TAG);
                fragment = mFacebookLoginFragment;
            }
            else if(schema.equals(LiveFeedFragment.TAG))
            {
                mLiveFeedFragment = (LiveFeedFragment)getFragment(fm, LiveFeedFragment.TAG);
                fragment = mLiveFeedFragment;
            }
            
            //Log.d(TAG ,"Right fragment=" + schema);

            if(fragment != null && fragment.isAdded())
            {
                tr.show(fragment);
            }
            else
            {
                tr.setCustomAnimations(0, R.anim.fadeout);
                tr.replace(R.id.menu_frame_right, fragment, schema);
            }
            
            tr.commit();
            currentRightFragmentTag = schema;
        }

        return fragment;
    }

    private Fragment getFragment(FragmentManager fm, String tag)
    {
        //Log.d(TAG,"getFragment() - tag=" + tag);
        Fragment f = fm.findFragmentByTag(tag);

        if (f == null)
        {
            //Log.d(TAG,"NOT Found fragment, create one");
            if(tag.equals(LeftMenuFragment.TAG))
            {
                f = new LeftMenuFragment();
            }
            else if(tag.equals(PlayerFragment.TAG))
            {
                f = new PlayerFragment();
            }
            else if(tag.equals(StationListFragment.TAG))
            {
                f = new StationListFragment();
            }
            else if(tag.equals(FavoriteSongListFragment.TAG))
            {
                f = new FavoriteSongListFragment();
            }
            else if(tag.equals(FavoriteStationListFragment.TAG))
            {
                f = new FavoriteStationListFragment();
            }
            else if(tag.equals(SearchFragment.TAG))
            {
                f = new SearchFragment();
            }
            else if(tag.equals(TestSearchZingSongFragment.TAG))
            {
                f = new TestSearchZingSongFragment();
            }
            else if(tag.equals(FacebookLoginFragment.TAG))
            {
                f = new FacebookLoginFragment();
            }
            else if(tag.equals(LiveFeedFragment.TAG))
            {
                f = new LiveFeedFragment(); 
            }
        }
        else
        {
            //Log.d(TAG,"Found fragment, return as is");
        }
        
        return f;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
        boolean handled = false;
        String tag = currentUri.getScheme();
        Log.i(TAG, "onKeyUp() 1 - Fragment=" + tag);
        switch(keyCode)
        {
            case KeyEvent.KEYCODE_BACK:
            {
                if(mSlidingMenu.isMenuShowing())
                {
                    Log.i(TAG, "menu is shown, current fragment " + tag);
                    handled = true;
                    if(currentLeftFragmentTag.equals(LeftMenuFragment.TAG))
                    {
                        Log.i(TAG, "1");
                        mSlidingMenu.toggle(true);
                        updateContent(PlayerFragment.URI);
                    }
                    else
                    {
                        Log.i(TAG, "2");
                        updateLeftFrame(LeftMenuFragment.URI, null);    
                    }
                }
                else//slide menu not shown
                {
                    //Log.i(TAG, "menu is NOT shown, current fragment " + tag);
                    if(tag.equals(StationListFragment.TAG) || 
                       tag.equals(FavoriteSongListFragment.TAG) || 
                       tag.equals(FavoriteStationListFragment.TAG) ||
                       tag.equals(SearchFragment.TAG))
                    {
                        Log.i(TAG, "3");
                        //Log.i(TAG, "menu is NOT shown, current fragment = list");
                        handled = true;
                        mSlidingMenu.toggle(true);
                        //updateContent(PlayerFragment.URI);
                        //viewActionsContentView.showActions();
                    }
                    else if(tag.equals(PlayerFragment.TAG))
                    {
                        Log.i(TAG, "4");
                        //Log.i(TAG, "menu is NOT shown, current fragment = player");
                        backTimes++;
                        if(backTimes == 2)
                        {
                            if(mMusicService != null)
                            {
                                this.mMusicService.stopSelf();
                            }
                            else
                            {
                                Intent i = new Intent(MusicService.ACTION_PAUSE);
                                this.startMusicService(i);    
                            }
                            //Toast.makeText(this, getString(R.string.msg_good_bye), Toast.LENGTH_SHORT).show();
                            try
                            {
                                //StartHomeScreenActivityCommand startHomeScreenActivityCmd = new StartHomeScreenActivityCommand(this);
                                //startHomeScreenActivityCmd.execute();    
                            }
                            catch(SecurityException e)
                            {
                                Toast.makeText(this, getString(R.string.msg_err_start_home), Toast.LENGTH_SHORT).show();
                                //Log.e(TAG, e.getMessage());
                            }
                            catch(Exception e)
                            {
                                //Log.e(TAG, e.getMessage());
                            }
                            backTimes = 0;
                            this.finish();
                        }
                        else
                        {
                            Toast.makeText(this, getString(R.string.msg_back_to_exit), Toast.LENGTH_SHORT).show();
                            mBackTimer.start();
                        }
                    }
                }
                break;
            }
            default:
                break;
        }

        return handled;
    }
    
    private int backTimes = 0;
    private CountDownTimer mBackTimer = new CountDownTimer(2500, 1000)
    {
        public void onFinish()
        {
            backTimes = 0;
        }

        @Override
        public void onTick(long l)
        {
        }
    };
    
    public void onRightMenuButtonClick(View view)
    {
        if(mSlidingMenu.isSecondaryMenuShowing())
        {
            mSlidingMenu.showContent();
        }
        else
        {
            mSlidingMenu.showSecondaryMenu(true);
        }
    }
    
    public AudioItem getPlayingItem()
    {
        if(mMusicService != null) return mMusicService.mPlayingItem;
        else return new AudioItem();
    }
    
    private void onSessionStateChange(final Session session, SessionState state, Exception exception)
    {
        /*
        // Only make changes if the activity is visible
        if (mIsResumed)
        {
            if (state.isOpened())
            {
                mFBSession = session;
                updateRightFrame(LiveFeedFragment.URI);
                
                Request request = Request.newMeRequest(session, new Request.GraphUserCallback()
                {
                    @Override
                    public void onCompleted(GraphUser user, Response response)
                    {
                        // If the response is successful
                        if (mFBSession == Session.getActiveSession())
                        {
                            if (user != null)
                            {
                                Utilities.writeFBSession(MainActivity.this, user, session);
                            }
                        }
                        if (response.getError() != null)
                        {
                             Log.e(TAG, response.getError().getErrorMessage());
                        }
                    }
                });
                request.executeAsync();
            }
            else if (state.isClosed())
            {
                mFBSession = null;
                updateRightFrame(FacebookLoginFragment.URI);
            }
        }
        */
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        mUiHelper.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
    }

    public ParseUser getUser()
    {
        return mUser;
    }

    public void setUser(ParseUser mUser)
    {
        this.mUser = mUser;
    }
    
    private void makeMeRequest(final Session session)
    {
        Request request = Request.newMeRequest(session, new Request.GraphUserCallback()
        {
            @Override
            public void onCompleted(GraphUser user, Response response)
            {
                // If the response is successful
                if (session == Session.getActiveSession())
                {
                    if (user != null)
                    {
                        String facebookId = user.getId();
                    }
                }
                if (response.getError() != null)
                {
                    // Handle error 
                }
            }
        });
        request.executeAsync();
    }

    private ProgressDialog loginProgress;
    private void login()
    {
        loginProgress = ProgressDialog.show(this, getString(R.string.msg_settings_logging_in_fb), getString(R.string.msg_settings_logging_in_fb_pls_wait), true);
    }

    public ImageButton getChannelListButton()
    {
        return mChannelListButton;
    }

    public void setChannelListButton(ImageButton mChannelListButton)
    {
        this.mChannelListButton = mChannelListButton;
    }

    public Fragment getCurrentLeftFragment()
    {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction tr = fm.beginTransaction();
        Fragment f = fm.findFragmentByTag(currentLeftFragmentTag);
        return f;
    }
    
    public Fragment getCurrentRightFragment()
    {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction tr = fm.beginTransaction();
        Fragment f = fm.findFragmentByTag(currentRightFragmentTag);
        return f;
    }
    
    public Fragment getCurrentCenterFragment()
    {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction tr = fm.beginTransaction();
        Fragment f = fm.findFragmentByTag(currentFragmentTag);
        return f;
    }
}
