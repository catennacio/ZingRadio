package com.friends.zingradio.async.social;

public interface PostLiveFeedAsyncComplete
{
    public void onPostLiveFeedAsyncComplete(String objectId);
    public void onPostLiveFeedAsyncError(int errCode, String errMsg);
}
