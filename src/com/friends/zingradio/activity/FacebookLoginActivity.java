package com.friends.zingradio.activity;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.friends.zingradio.R;
import com.friends.zingradio.util.Utilities;
import com.google.analytics.tracking.android.EasyTracker;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

public class FacebookLoginActivity extends Activity
{
    public static final int FACEBOOK_AUTHORIZE_ACTIVITY_RESULT_CODE_OK = 0;
    public static final int FACEBOOK_AUTHORIZE_ACTIVITY_RESULT_CODE_ERROR = -1;
    
    public static final String FACEBOOK_USER_ID_KEY = "UserId";
    public static final String FACEBOOK_USER_NAME_KEY = "UserName";
    public static final String FACEBOOK_USER_FULLNAME_KEY = "UserFullname";
    public static final String FACEBOOK_ACCESSTOKEN_KEY = "access_token";
    public static final String FACEBOOK_ACCESSTOKENEXPIRES_KEY = "expires_in";
    public static final String FACEBOOK_ENABLED_KEY = "FACEBOOK_ENABLED_KEY";
    
    protected static final String TAG = FacebookLoginActivity.class.getSimpleName();
    
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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.facebooklogin);
        
        mUiHelper = new UiLifecycleHelper(this, mCallback);
        mUiHelper.onCreate(savedInstanceState);

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        mUiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mUiHelper.onDestroy();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        mUiHelper.onPause();
        mIsResumed = false;

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        mUiHelper.onResume();
        mIsResumed = true;
    }
    
    Session mSession;
    
    private void onSessionStateChange(final Session session, SessionState state, Exception exception)
    {
        if (mIsResumed)
        {
            mSession = session;
            if(session.isOpened())
            {
                Request request = Request.newMeRequest(session, new Request.GraphUserCallback()
                {
                    @Override
                    public void onCompleted(GraphUser user, Response response)
                    {
                        // If the response is successful
                        if (mSession == Session.getActiveSession())
                        {
                            if (user != null)
                            {
                                Utilities.writeFBSession(FacebookLoginActivity.this, user, session);
                                FacebookLoginActivity.this.setResult(FACEBOOK_AUTHORIZE_ACTIVITY_RESULT_CODE_OK);
                            }
                        }
                        if (response.getError() != null)
                        {
                            // Handle errors, will do so later.
                        }
                        FacebookLoginActivity.this.finish();
                    }
                });
                request.executeAsync();
            }
            else
            {
                this.setResult(FACEBOOK_AUTHORIZE_ACTIVITY_RESULT_CODE_ERROR);
                FacebookLoginActivity.this.finish();
            }

            //this.finishActivity(SettingsActivity.FACEBOOK_AUTHORIZE_ACTIVITY_REQUEST_CODE);
            
        }
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
        super.onStop();
        EasyTracker.getInstance().activityStop(this);
    }
}
