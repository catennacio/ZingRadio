package com.friends.zingradio.fragment;

import java.lang.ref.WeakReference;

import android.app.Service;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.facebook.Session;
import com.friends.zingradio.R;
import com.friends.zingradio.activity.MainActivity;
import com.friends.zingradio.entity.AudioItem;
import com.friends.zingradio.entity.LiveFeed;
import com.friends.zingradio.entity.User;
import com.friends.zingradio.util.Constants;
import com.friends.zingradio.util.GAUtils;
import com.friends.zingradio.util.Utilities;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

public class LiveFeedFragment extends Fragment
{
    public static final String TAG = LiveFeedFragment.class.getSimpleName();
    public static final Uri URI = new Uri.Builder().scheme(TAG).authority("").build();

    private View viewRoot;
    private ListView mListView;
    private EditText mEditText;
    private Button mPostButton;
    private ProgressBar mProgressBar;
    private WeakReference<MainActivity> mActivity;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        GAUtils.writeViewFragement(this.getActivity(), TAG);
        mActivity = new WeakReference<MainActivity>((MainActivity)this.getActivity());
        viewRoot = inflater.inflate(R.layout.livefeed, null);
        mListView = (ListView)viewRoot.findViewById(R.id.lv_feed);
        mProgressBar = (ProgressBar)viewRoot.findViewById(R.id.progressbar);
        mPostButton = (Button)viewRoot.findViewById(R.id.btn_post);
        mPostButton.requestFocus();
        mPostButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(!mEditText.getText().toString().equals(""))
                {
                    postMessage();    
                }
                
                showHideSoftKeyboard(false);
            }
        });

        mEditText = (EditText)viewRoot.findViewById(R.id.et_message);
        /*
        mEditText.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                InputMethodManager imm = (InputMethodManager)LiveFeedFragment.this.getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
                if (hasFocus)
                {
                    imm.showSoftInput(mEditText, InputMethodManager.SHOW_FORCED);
                }
                else
                {
                    imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
                }
            }
        });*/
        
        mEditText.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showHideSoftKeyboard(true);
            }
        });

        showHideSoftKeyboard(false);
        return viewRoot;
    }
    
    
    @Override
    public void onResume()
    {
        super.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        showHideSoftKeyboard(false);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }
    
    private void postMessage()
    {
        Session session = Session.getActiveSession();

        if(session != null && session.isOpened())
        {
            mProgressBar.setVisibility(View.VISIBLE);
            LiveFeed lf = new LiveFeed();
            AudioItem ai = mActivity.get().getPlayingItem();
            User user = Utilities.getCurrentUser(mActivity.get());
            lf.setSongId(ai.getId());
            lf.setArtistname(ai.getPerformer());
            lf.setMessage(mEditText.getText().toString());
            lf.setSongname(ai.getTitle());
            lf.setUserId(user.getUserId());
            lf.setUsername(user.getUsername());
            lf.setUserFullname(user.getFullname());
            doParsePostMessage(lf);
        }
    }

    private void doParsePostMessage(LiveFeed lf)
    {
        ParseObject feed = new ParseObject(Constants.PARSE_CLASS_LIVEFEED);
        feed.put(Constants.PARSE_CLASS_LIVEFEED_ARTISTNAME, lf.getArtistname());
        feed.put(Constants.PARSE_CLASS_LIVEFEED_MESSAGE, lf.getMessage());
        feed.put(Constants.PARSE_CLASS_LIVEFEED_SONGID, lf.getSongId());
        feed.put(Constants.PARSE_CLASS_LIVEFEED_SONGNAME, lf.getSongname());
        feed.put(Constants.PARSE_CLASS_LIVEFEED_USERID, lf.getUserId());
        feed.put(Constants.PARSE_CLASS_LIVEFEED_USERNAME, lf.getUsername());
        feed.put(Constants.PARSE_CLASS_LIVEFEED_USERFULLNAME, lf.getUserFullname());
        feed.saveInBackground(new SaveCallback()
        {
            @Override
            public void done(ParseException e)
            {
                mProgressBar.setVisibility(View.GONE);
                Toast.makeText(mActivity.get(), "Message posted!", Toast.LENGTH_SHORT).show();
                mEditText.setText("");
            }
        });
    }

    private void showHideSoftKeyboard(boolean show)
    {
        InputMethodManager imm = (InputMethodManager)LiveFeedFragment.this.getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
        if(show)
        {
            imm.showSoftInput(mEditText, InputMethodManager.SHOW_FORCED);
        }
        else
        {
            imm.hideSoftInputFromWindow(mEditText.getApplicationWindowToken(), 0);
        }
    }
}
