package com.friends.zingradio.async.social;

import com.friends.zingradio.entity.LiveFeed;

import android.os.AsyncTask;

public class PostLiveFeedAsync extends AsyncTask<LiveFeed, Integer, String>
{
    private int errCode = 0;
    private String errMsg;
    private PostLiveFeedAsyncComplete mListener;
    
    public PostLiveFeedAsync(PostLiveFeedAsyncComplete lis)
    {
        mListener = lis;
    }
    
    @Override
    protected String doInBackground(LiveFeed... lfs)
    {
        LiveFeed lf = lfs[0];
        
        return null;
    }

    @Override
    protected void onPostExecute(String result)
    {
        super.onPostExecute(result);
    }
}
