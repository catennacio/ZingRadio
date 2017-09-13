package com.friends.zingradio.fragment;

import java.lang.ref.WeakReference;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.friends.zingradio.R;
import com.friends.zingradio.activity.MainActivity;
import com.friends.zingradio.entity.User;
import com.friends.zingradio.util.Utilities;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

public class FacebookLoginFragment extends Fragment
{
    public static final String TAG = FacebookLoginFragment.class.getSimpleName();
    public static final Uri URI = new Uri.Builder().scheme(TAG).authority("").build();
    private View viewRoot;
    private WeakReference<MainActivity> mActivity;
    private ProgressDialog loginProgress;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mActivity = new WeakReference<MainActivity>((MainActivity)getActivity());
        viewRoot = inflater.inflate(R.layout.facebooklogin, null);
        Button facebookBtn = (Button)viewRoot.findViewById(R.id.btn_fb);
        facebookBtn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                login();
            }
        });
        
        return viewRoot;
    }

    private void login()
    {
        Utilities.writeFBEnabled(mActivity.get(), true);
        loginProgress = ProgressDialog.show(mActivity.get(), /*getString(R.string.msg_settings_logging_in_fb)*/null, getString(R.string.msg_settings_logging_in_fb_pls_wait), true);
        ParseFacebookUtils.logIn(mActivity.get(), new LogInCallback()
        {
            private ParseUser pUser;

            @Override
            public void done(ParseUser user, ParseException err)
            {
                loginProgress.dismiss();
                this.pUser = user;
                if (user == null)
                {
                    Toast.makeText(FacebookLoginFragment.this.getActivity(), FacebookLoginFragment.this.getString(R.string.msg_login_cancel), Toast.LENGTH_SHORT).show();
                    Log.d(FacebookLoginFragment.TAG, "Uh oh. The user cancelled the Facebook login.");
                }
                else
                {
                    Session.openActiveSession(mActivity.get(), false, null);
                    mActivity.get().setSession(Session.getActiveSession());
                    Request request = Request.newMeRequest(mActivity.get().getSession(), new Request.GraphUserCallback()
                    {
                        @Override
                        public void onCompleted(GraphUser graphUser, Response response)
                        {
                            // If the response is successful
                            if (graphUser != null)
                            {
                                Utilities.writeFBSession(mActivity.get(), graphUser, mActivity.get().getSession());
                                User zUser = new User(pUser);
                                Utilities.setCurrentUser(mActivity.get(), zUser);
                                Log.d(TAG, "UserId=" + graphUser.getId());

                                String msg;
                                if (pUser.isNew())
                                {
                                    msg = FacebookLoginFragment.this.getString(R.string.msg_welcome) + ", "+ zUser.getFullname() + "!";
                                    Log.d(FacebookLoginFragment.TAG, "User signed up and logged in through Facebook!");
                                }
                                else
                                {
                                    msg = FacebookLoginFragment.this.getString(R.string.msg_welcome_back) + ", "+ zUser.getFullname() + "!";
                                    Log.d(FacebookLoginFragment.TAG, "User logged in through Facebook!");
                                }

                                Toast.makeText(mActivity.get(), msg, Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                Log.d(TAG, "Cannot get Graph User");
                                Toast.makeText(mActivity.get(), "Error login!", Toast.LENGTH_SHORT).show();
                            }

                            if (response.getError() != null)
                            {
                                //Toast.makeText(mActivity, response.getError().getErrorMessage(), Toast.LENGTH_SHORT).show();
                                Log.e(TAG, response.getError().getErrorMessage());
                            }
                        }
                    });
                    request.executeAsync();
                }
            }
        });
    }
}
