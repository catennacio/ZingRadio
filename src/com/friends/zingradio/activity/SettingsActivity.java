package com.friends.zingradio.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.*;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.facebook.*;
import com.facebook.model.GraphUser;
import com.friends.zingradio.R;
import com.friends.zingradio.entity.User;
import com.friends.zingradio.fragment.PlayerFragment;
import com.friends.zingradio.media.MusicService;
import com.friends.zingradio.media.MusicService.MusicServiceBinder;
import com.friends.zingradio.ui.OffTimerDialogPreference;
import com.friends.zingradio.ui.OnTimerDialogPreference;
import com.friends.zingradio.util.Constants;
import com.friends.zingradio.util.GAUtils;
import com.friends.zingradio.util.Utilities;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends PreferenceActivity
{
    public static final String TAG = SettingsActivity.class.getSimpleName();
    public static final String SETTINGS_AUTO_DOWNLOAD = "auto_download";
    public static final String SETTINGS_INSTANT_SEARCH = "instant_search";
    public static final String SETTINGS_SHOW_NOTIFICATION_WHEN_PLAY = "show_notification_when_play";
    public static final String SETTINGS_MARKET = "show_market";
    public static final String SETTINGS_FACEBOOK = "facebook";
    public static final String SETTINGS_ABOUT = "settings_about";
    public static final String SETTINGS_TIMER_OFF_SELECT = "timer_off_select";
    public static final String SETTINGS_TIMER_OFF_DIALOG = "timer_off_dialog";
    
    public static final String SETTINGS_TIMER_ON_SELECT = "timer_on_select";
    public static final String SETTINGS_TIMER_ON_DIALOG = "timer_on_dialog";

    public static final String SHARED_PREF_USE_TIMER_OFF = "pref_timer_off";
    public static final String SHARED_PREF_USE_TIMER_ON = "pref_timer_on";
    public static final String SHARED_PREF_USE_TIMER_OFF_KEY = "SHARED_PREF_USE_TIMER_OFF_KEY";
    public static final String SHARED_PREF_USE_TIMER_ON_KEY = "SHARED_PREF_USE_TIMER_ON_KEY";
    
    public static final int FACEBOOK_AUTHORIZE_ACTIVITY_REQUEST_CODE = 0;
    
    public static final String PLAY_STORE_URL_WEB = "https://play.google.com/store/apps/details?id=com.friends.zingradio&referrer=utm_source%3Dgoogle%26utm_medium%3Dcpc%26utm_term%3Dinstall%252Bzing%252Bradio%252Bmp3%252Bdownload%26utm_campaign%3DShare%2520feelings%2520through%2520music";
    public static final String PLAY_STORE_URL = "market://details?id=com.friends.zingradio&referrer=utm_source%3Dgoogle%26utm_medium%3Dcpc%26utm_term%3Dinstall%252Bzing%252Bradio%252Bmp3%252Bdownload%26utm_campaign%3DShare%2520feelings%2520through%2520music";

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

    private Preference mFacebookPreference;
    private Session mFBSession;
    private Button mParseLoginButton;

    public static final List<String> FACEBOOK_PERMISSIONS = new ArrayList<String>()
    {
        {
            add("publish_actions");
            add("xmpp_login");
        }
    };

    private MusicService mMusicService;
    private ServiceConnection mMusicServiceConnection = new ServiceConnection()
    {
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            MusicServiceBinder binder = (MusicServiceBinder) service;
            mMusicService = (MusicService) binder.getService();
            @SuppressWarnings("deprecation")
            CheckBoxPreference checkboxPref = (CheckBoxPreference) findPreference(SETTINGS_SHOW_NOTIFICATION_WHEN_PLAY);
            checkboxPref.setEnabled(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0)
        {
        }
    };

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_settings);

        mUiHelper = new UiLifecycleHelper(this, mCallback);
        mUiHelper.onCreate(savedInstanceState);

        Intent i = new Intent(this, MusicService.class);
        this.bindService(i, mMusicServiceConnection, Context.BIND_AUTO_CREATE);

        SharedPreferences FBsharedPref = this.getSharedPreferences(Constants.FACEBOOK_SESSION_PREF, Activity.MODE_PRIVATE);
        addPreferencesFromResource(R.xml.settings);

        Preference marketPref = findPreference(SETTINGS_MARKET);
        marketPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            public boolean onPreferenceClick(Preference preference)
            {
                try
                {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_URL));
                    startActivity(intent);
                }
                catch (ActivityNotFoundException e)
                {
                    // e.printStackTrace();
                    Log.d(TAG, e.getMessage());
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_URL_WEB));
                    startActivity(intent);
                }
                return false;
            }
        });

        mFacebookPreference = findPreference(SETTINGS_FACEBOOK);

        mFBSession = Session.getActiveSession();
        if (mFBSession != null)
        {
            String userName = FBsharedPref.getString(FacebookLoginActivity.FACEBOOK_USER_FULLNAME_KEY, null);
            if (mFBSession.isOpened())
            {
                mFacebookPreference.setTitle(R.string.settings_logout_facebook);
                mFacebookPreference.setSummary(SettingsActivity.this.getString(R.string.msg_logout_from_facebook) + " " + userName + ". "
                        + SettingsActivity.this.getString(R.string.settings_logout_facebook_summary));
            }
        }
        else
        {
            mFacebookPreference.setTitle(R.string.settings_login_facebook);
            mFacebookPreference.setSummary(R.string.settings_login_facebook_summary);
        }

        mFacebookPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            public boolean onPreferenceClick(Preference preference)
            {
                // Toast.makeText(SettingsActivity.this, "Chức năng đang được implement sẽ có trong bản sắp tới!", Toast.LENGTH_SHORT).show();
                // return false;

                mFBSession = Session.getActiveSession();
                Log.d(TAG, "mFBSession=" + mFBSession);
                if (mFBSession == null || mFBSession.isClosed())
                {
                    login();
                }
                else if (mFBSession.isOpened())
                {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            switch (which)
                            {
                                case DialogInterface.BUTTON_POSITIVE:
                                {
                                    logout();
                                    break;
                                }
                                case DialogInterface.BUTTON_NEGATIVE:
                                {
                                    break;
                                }
                            }
                        }
                    };

                    SharedPreferences sharedPref = SettingsActivity.this.getSharedPreferences(Constants.FACEBOOK_SESSION_PREF, Activity.MODE_PRIVATE);
                    String fullname = sharedPref.getString(FacebookLoginActivity.FACEBOOK_USER_FULLNAME_KEY, null);
                    AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                    builder.setTitle(getString(R.string.msg_logout_from_facebook) + " " + fullname + "?");
                    builder.setMessage(getString(R.string.settings_logout_facebook_summary) + " " + getString(R.string.msg_are_you_sure));
                    builder.setPositiveButton("OK :(", dialogClickListener);
                    builder.setNegativeButton("NO!!!", dialogClickListener);
                    builder.show();
                }
                return false;
            }
        });

        CheckBoxPreference showNotificationCheckboxPref = (CheckBoxPreference) findPreference(SETTINGS_SHOW_NOTIFICATION_WHEN_PLAY);
        showNotificationCheckboxPref.setEnabled(false);
        showNotificationCheckboxPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
        {
            public boolean onPreferenceChange(Preference preference, Object newValue)
            {
                boolean show = ((Boolean) newValue).booleanValue();
                if (show)
                {
                    if (mMusicService != null && mMusicService.getState() == MusicService.State.Playing)
                    {
                        String text = mMusicService.mPlayingItem.getTitle() + " - " + mMusicService.mPlayingItem.getPerformer();
                        mMusicService.showNotification(text);
                    }
                }
                else
                {
                    mMusicService.hideNotification();
                }

                return true;
            }
        });

        SharedPreferences timerOffSharedPref = this.getSharedPreferences(SettingsActivity.SHARED_PREF_USE_TIMER_OFF, Context.MODE_MULTI_PROCESS);

        CheckBoxPreference timerOffPref = (CheckBoxPreference) findPreference(SETTINGS_TIMER_OFF_SELECT);
        final OffTimerDialogPreference timerOffDialogPref = (OffTimerDialogPreference)findPreference(SETTINGS_TIMER_OFF_DIALOG);
        boolean useOffTimer = timerOffSharedPref.getBoolean(SettingsActivity.SHARED_PREF_USE_TIMER_OFF_KEY, false);
        //Log.d(TAG,"onCreate() - useOffTimer=" + useOffTimer);
        timerOffPref.setChecked(useOffTimer);
        timerOffDialogPref.setEnabled(timerOffPref.isChecked());
        timerOffPref.setSummary(timerOffPref.getSummary());
        
        timerOffPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue)
            {
                boolean useTimer = ((Boolean) newValue).booleanValue();
                //Log.d(TAG,"OffTimer onPrefChanged() - useTimer=" + useTimer);
                SharedPreferences sharedPref = SettingsActivity.this.getSharedPreferences(SettingsActivity.SHARED_PREF_USE_TIMER_OFF, Context.MODE_MULTI_PROCESS);
                Editor editor = sharedPref.edit();
                editor.putBoolean(SHARED_PREF_USE_TIMER_OFF_KEY, useTimer);
                editor.apply();
                timerOffDialogPref.setEnabled(useTimer);
                return true;
            }
        });
        
        SharedPreferences timerOnSharedPref = this.getSharedPreferences(SettingsActivity.SHARED_PREF_USE_TIMER_ON, Context.MODE_MULTI_PROCESS);
        CheckBoxPreference timerOnPref = (CheckBoxPreference) findPreference(SETTINGS_TIMER_ON_SELECT);
        final OnTimerDialogPreference timerOnDialogPref = (OnTimerDialogPreference)findPreference(SETTINGS_TIMER_ON_DIALOG);
        boolean useOnTimer = timerOnSharedPref.getBoolean(SettingsActivity.SHARED_PREF_USE_TIMER_ON_KEY, false);
        //Log.d(TAG,"onCreate() - useOnTimer=" + useOnTimer);
        timerOnPref.setChecked(useOnTimer);
        timerOnDialogPref.setEnabled(timerOnPref.isChecked());
        timerOnPref.setSummary(timerOnPref.getSummary());
        
        timerOnPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue)
            {
                boolean useTimer = ((Boolean) newValue).booleanValue();
                //Log.d(TAG,"OnTimer onPrefChanged() - useTimer=" + useTimer);
                SharedPreferences sharedPref = SettingsActivity.this.getSharedPreferences(SettingsActivity.SHARED_PREF_USE_TIMER_ON, Context.MODE_MULTI_PROCESS);
                Editor editor = sharedPref.edit();
                editor.putBoolean(SHARED_PREF_USE_TIMER_ON_KEY, useTimer);
                editor.apply();
                timerOnDialogPref.setEnabled(useTimer);
                return true;
            }
        });

        PreferenceScreen screen = getPreferenceScreen();
        PreferenceCategory mCategory = (PreferenceCategory) findPreference("category_facebook");
        // screen.removePreference(mCategory);
        mCategory.removePreference(mFacebookPreference);
        PreferenceCategory options = (PreferenceCategory) findPreference("category_general");
        options.removePreference(timerOnPref);
        options.removePreference(timerOnDialogPref);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        mUiHelper.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        Session session = Session.getActiveSession();
        Session.saveSession(session, outState);
        mUiHelper.onSaveInstanceState(outState);
    }
    
    @Override
    protected void onResume()
    {
        super.onResume();
        mUiHelper.onResume();
        mIsResumed = true;
        /*
         * SharedPreferences sharedPref =
         * this.getSharedPreferences(Constants.FACEBOOK_SESSION_PREF,
         * Activity.MODE_PRIVATE); boolean isFBEnabled =
         * sharedPref.getBoolean(FacebookLoginActivity.FACEBOOK_ENABLED_KEY,
         * false); if (isFBEnabled) { String fullname =
         * sharedPref.getString(FacebookLoginActivity
         * .FACEBOOK_USER_FULLNAME_KEY, null);
         * mFacebookPreference.setTitle(R.string.settings_logout_facebook);
         * mFacebookPreference
         * .setSummary(SettingsActivity.this.getString(R.string
         * .msg_logout_from_facebook) + " " + fullname + ". " +
         * SettingsActivity.
         * this.getString(R.string.settings_logout_facebook_summary)); } else {
         * mFacebookPreference
         * .setTitle(getString(R.string.msg_settings_logging_in_fb));
         * mFacebookPreference
         * .setSummary(R.string.settings_logout_facebook_summary); }
         */
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        mUiHelper.onPause();
        mIsResumed = false;
    }

    @Override
    protected void onDestroy()
    {
        if (mMusicServiceConnection != null) this.unbindService(mMusicServiceConnection);
        mUiHelper.onDestroy();
        if (loginProgress != null)
        {
            loginProgress.dismiss();
        }
        super.onDestroy();
    }

    private void onSessionStateChange(final Session session, SessionState state, Exception exception)
    {
        Log.d(TAG, "onSessionStateChange() - session=" + session);
        if (mIsResumed)
        {
            mFBSession = session;
            if (session.isOpened())
            {
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
                                Utilities.writeFBSession(SettingsActivity.this, user, session);
                                mFacebookPreference.setTitle(R.string.settings_logout_facebook);
                                mFacebookPreference.setSummary(SettingsActivity.this.getString(R.string.msg_logout_from_facebook) + " " + user.getName() + ". "
                                        + SettingsActivity.this.getString(R.string.settings_logout_facebook_summary));
                            }
                        }
                        if (response.getError() != null)
                        {
                            Toast.makeText(SettingsActivity.this, response.getError().getErrorMessage(), Toast.LENGTH_SHORT).show();
                            Log.e(TAG, response.getError().getErrorMessage());
                        }
                    }
                });
                request.executeAsync();
            }
            else
            {
                mFacebookPreference.setTitle(R.string.settings_login_facebook);
                mFacebookPreference.setSummary(R.string.settings_login_facebook_summary);
            }
        }
    }

    private ProgressDialog loginProgress;

    private void login()
    {
        Utilities.writeFBEnabled(SettingsActivity.this, true);
        loginProgress = ProgressDialog.show(this, /*
                                                   * getString(R.string.
                                                   * msg_settings_logging_in_fb)
                                                   */null, getString(R.string.msg_settings_logging_in_fb_pls_wait), true);
        ParseFacebookUtils.logIn(this, new LogInCallback()
        {
            private ParseUser pUser;

            @Override
            public void done(ParseUser user, ParseException err)
            {
                this.pUser = user;
                loginProgress.dismiss();
                if (user == null)
                {
                    Toast.makeText(SettingsActivity.this, SettingsActivity.this.getString(R.string.msg_login_cancel), Toast.LENGTH_SHORT).show();
                    Log.d(SettingsActivity.TAG, "Uh oh. The user cancelled the Facebook login.");
                    mFacebookPreference.setTitle(R.string.settings_login_facebook);
                    mFacebookPreference.setSummary(R.string.settings_login_facebook_summary);
                }
                else
                {
                    Session.openActiveSession(SettingsActivity.this, false, null);
                    mFBSession = Session.getActiveSession();

                    Request request = Request.newMeRequest(mFBSession, new Request.GraphUserCallback()
                    {
                        @Override
                        public void onCompleted(GraphUser graphUser, Response response)
                        {
                            // If the response is successful
                            if (mFBSession == Session.getActiveSession())
                            {
                                if (graphUser != null)
                                {
                                    Utilities.writeFBSession(SettingsActivity.this, graphUser, mFBSession);
                                    User zUser = new User(pUser);
                                    Utilities.setCurrentUser(SettingsActivity.this, zUser);
                                    Log.d(TAG, "UserId=" + graphUser.getId());

                                    String msg;
                                    if (pUser.isNew())
                                    {
                                        msg = SettingsActivity.this.getString(R.string.msg_welcome) + ", " + zUser.getFullname() + "!";
                                        Log.d(SettingsActivity.TAG, "User signed up and logged in through Facebook!");

                                        GAUtils.writeLoginSession(SettingsActivity.this);
                                    }
                                    else
                                    {
                                        msg = SettingsActivity.this.getString(R.string.msg_welcome_back) + ", " + zUser.getFullname() + "!";
                                        Log.d(SettingsActivity.TAG, "User logged in through Facebook!");
                                    }

                                    Toast.makeText(SettingsActivity.this, msg, Toast.LENGTH_SHORT).show();
                                }
                            }

                            if (response.getError() != null)
                            {
                                Toast.makeText(SettingsActivity.this, response.getError().getErrorMessage(), Toast.LENGTH_SHORT).show();
                                Log.e(TAG, response.getError().getErrorMessage());
                            }
                        }
                    });
                    request.executeAsync();
                }
            }
        });
    }

    private void logout()
    {
        Utilities.writeFBEnabled(SettingsActivity.this, false);
        if (mFBSession != null)
        {
            mFBSession.closeAndClearTokenInformation();
            ParseUser.logOut();
        }

        mFacebookPreference.setTitle(R.string.settings_login_facebook);
        mFacebookPreference.setSummary(R.string.settings_login_facebook_summary);
    }

    @Override
    public void onBackPressed()
    {
        finish();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }
    
    @Override
    protected void onStart()
    {
        super.onStart();
        EasyTracker.getInstance().activityStart(this);    
    }
    
    @Override
    protected void onStop()
    {
        EasyTracker.getInstance().activityStop(this);
        super.onStop();
    }
}
