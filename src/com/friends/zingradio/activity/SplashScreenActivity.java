package com.friends.zingradio.activity;

import android.app.Activity;
import android.content.*;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.friends.zingradio.R;
import com.friends.zingradio.ZingRadioApplication;
import com.friends.zingradio.command.StartMainActivityCommand;
import com.friends.zingradio.entity.User;
import com.friends.zingradio.media.MusicService;
import com.friends.zingradio.util.Constants;
import com.friends.zingradio.util.Utilities;
import com.google.analytics.tracking.android.EasyTracker;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

public class SplashScreenActivity extends Activity
{
    private static final int SPLASH_LOADDATA_TIME = 0;
    private static final int SPLASH_CONNECTING_TIME = 0;
    private static final int SPLASH_DISPLAY_TIME = 200;
    private static final String FACE_HAPPY = "";
    private static final String FACE_SAD = ">.<";
    private static final String FACE_THINKING = "";

    public static final String TAG = SplashScreenActivity.class.getSimpleName();
    boolean mIsOnline = false;
    boolean mIsShowingDialog = false;

    //private ImageButton mRetryButton;
    private TextView mMessageTextView;
    //private ViewFlipper mFlipper;
    private Button mRetryButton;
    //private Animation mTextBlinkingAnimation;
    private TextView mFaceTextView;
    private ProgressBar mProgressBar;

    public void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        mProgressBar = (ProgressBar)this.findViewById(R.id.progressbar);
        mProgressBar.setVisibility(View.GONE);
/*
        mFlipper = ((ViewFlipper)findViewById(R.id.tv_flipper));
        mFlipper.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadein));
        mFlipper.setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadeout));
  */    
        /*
        mTextBlinkingAnimation = new AlphaAnimation(1, 0); // Change alpha from fully visible to invisible
        mTextBlinkingAnimation.setDuration(500); // duration
        mTextBlinkingAnimation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
        mTextBlinkingAnimation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
        mTextBlinkingAnimation.setRepeatMode(Animation.REVERSE); // Reverse animation at the end so the button will fade back in
        */
        mFaceTextView = (TextView) this.findViewById(R.id.tv_face);
        mFaceTextView.setVisibility(View.INVISIBLE);

        mMessageTextView = (TextView) this.findViewById(R.id.tv_message);
        //mMessageTextView.setAnimation(mTextBlinkingAnimation);

        mRetryButton = (Button) this.findViewById(R.id.bt_retry1);
        mRetryButton.setVisibility(Button.INVISIBLE);
        mRetryButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                doCheckOnline();
            }
        });
    }

    boolean mIsCheckingOnline = false;

    private void doCheckOnline()
    {
        Log.d(TAG, "doCheckOnline()");

        if (mIsCheckingOnline) return;
        mIsCheckingOnline = true;
        //mFlipper.startFlipping();
        mRetryButton.setVisibility(ImageButton.INVISIBLE);
        mFaceTextView.setVisibility(View.VISIBLE);
        mMessageTextView.setText(getString(R.string.msg_connecting));
        mFaceTextView.setText(FACE_THINKING);
        //mMessageTextView.startAnimation(mTextBlinkingAnimation);

        //Log.d(TAG ,"2");
        CountDownTimer timer = new CountDownTimer(SPLASH_CONNECTING_TIME, 1000)
        {
            public void onFinish()
            {
                mIsOnline = Utilities.isNetworkOnline(SplashScreenActivity.this);
                mFaceTextView.setVisibility(View.VISIBLE);
                //Log.d(TAG ,"3");
                if (! mIsOnline)//not online
                    {
                        //mFlipper.stopFlipping();

                        mFaceTextView.setText(FACE_SAD);
                        mMessageTextView.setText(getString(R.string.splash_msg_no_connection));
                        mMessageTextView.clearAnimation();
                        mRetryButton.setVisibility(ImageButton.VISIBLE);
                        mIsCheckingOnline = false;
                    } else
                    {
                        mFaceTextView.setText(FACE_HAPPY);
                        mRetryButton.setVisibility(ImageButton.INVISIBLE);
                        mMessageTextView.setText("Connecting...");
                        //mMessageTextView.startAnimation(mTextBlinkingAnimation);

                        CountDownTimer timer = new CountDownTimer(SPLASH_DISPLAY_TIME, 1000)
                        {
                            public void onFinish()
                            {
                                PreferenceManager.setDefaultValues(SplashScreenActivity.this, R.xml.settings, false);                                
                                SharedPreferences sharedPref = SplashScreenActivity.this.getSharedPreferences(Constants.FACEBOOK_SESSION_PREF, FacebookLoginActivity.MODE_PRIVATE);
                                boolean isFBEnabled = sharedPref.getBoolean(FacebookLoginActivity.FACEBOOK_ENABLED_KEY, false);
                                Log.d(TAG, "isFBEnabled()=" + isFBEnabled);
                                //getFBUser();
                                //startMainActivity();

                                if(isFBEnabled)
                                {
                                    login();
                                }
                                else
                                {
                                    startMainActivity();
                                }
                            }

                            @Override
                            public void onTick(long l) {}
                        }.start();
                    }
            }

            @Override
            public void onTick(long l) {}
        }.start();
    }

    @Override
    protected void onStart()
    {
        Log.d(TAG, "onStart()");
        super.onStart();
        Intent i = new Intent(this, MusicService.class);
        this.bindService(i, mMusicServiceConnection, Context.BIND_AUTO_CREATE);
        EasyTracker.getInstance().activityStart(this);
    }

    @Override
    protected void onResume()
    {
        Log.d(TAG, "onResume()");
        super.onResume();
        mFaceTextView.setVisibility(View.VISIBLE);
        mFaceTextView.setText(FACE_THINKING);
        ZingRadioApplication app = (ZingRadioApplication) this.getApplication();
        app.loadAll();
        doCheckOnline();
    }

    @Override
    protected void onStop()
    {
        Log.d(TAG, "onStop()");
        if (mMusicServiceConnection != null) this.unbindService(mMusicServiceConnection);
        EasyTracker.getInstance().activityStop(this);
        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();
    }

    private ServiceConnection mMusicServiceConnection = new ServiceConnection()
    {
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            //doCheckOnline();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {}
    };

    private Session mSession;

    private void login()
    {
        mProgressBar.setVisibility(View.VISIBLE);
        mRetryButton.setVisibility(View.GONE);
        mMessageTextView.setText(getString(R.string.msg_logging_in));
        Log.d(TAG,"login() - session=" + mSession);
        ParseFacebookUtils.logIn(this, new LogInCallback()
        {
            private ParseUser pUser;

            @Override
            public void done(ParseUser user, ParseException err)
            {
                this.pUser = user;
                if (user == null)
                {
                    Toast.makeText(SplashScreenActivity.this, SplashScreenActivity.this.getString(R.string.msg_login_cancel), Toast.LENGTH_SHORT).show();
                    Log.d(SplashScreenActivity.TAG, "Uh oh. The user cancelled the Facebook login.");
                    SplashScreenActivity.this.finish();
                }
                else
                {
                    mSession = Session.openActiveSession(SplashScreenActivity.this, false, null);
                    Request request = Request.newMeRequest(mSession, new Request.GraphUserCallback()
                    {
                        @Override
                        public void onCompleted(GraphUser graphUser, Response response)
                        {
                            if (graphUser != null)
                            {
                                Utilities.writeFBSession(SplashScreenActivity.this, graphUser, mSession);
                                User zUser = new User(pUser);
                                Utilities.setCurrentUser(SplashScreenActivity.this, zUser);
                                Log.d(TAG, "UserId=" + graphUser.getId());
                                mProgressBar.setVisibility(View.GONE);
                                String msg;
                                if (pUser.isNew())
                                {
                                    msg = SplashScreenActivity.this.getString(R.string.msg_welcome) + ", "+ zUser.getFullname() + "!";
                                    Log.d(SplashScreenActivity.TAG, "User signed up and logged in through Facebook!");
                                }
                                else
                                {
                                    msg = SplashScreenActivity.this.getString(R.string.msg_welcome_back) + ", "+ zUser.getFullname() + "!";
                                    Log.d(SplashScreenActivity.TAG, "User logged in through Facebook!");
                                }

                                Toast.makeText(SplashScreenActivity.this, msg, Toast.LENGTH_SHORT).show();
                                startMainActivity();
                            }
                            else
                            {
                                Log.d(TAG, "Cannot get Graph User");
                                Toast.makeText(SplashScreenActivity.this, "Error login!", Toast.LENGTH_SHORT).show();
                                SplashScreenActivity.this.finish();
                            }

                            if (response.getError() != null)
                            {
                                //Toast.makeText(SplashScreenActivity.this, response.getError().getErrorMessage(), Toast.LENGTH_SHORT).show();
                                Log.e(TAG, response.getError().getErrorMessage());
                                SplashScreenActivity.this.finish();
                            }
                        }
                    });
                    request.executeAsync();
                }
            }
        });
    }

    private void startMainActivity()
    {
        StartMainActivityCommand startMainActivityCmd = new StartMainActivityCommand(SplashScreenActivity.this);
        startMainActivityCmd.execute(false);
        this.finish();
    }
}
