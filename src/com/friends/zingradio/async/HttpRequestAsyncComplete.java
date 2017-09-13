package com.friends.zingradio.async;

public interface HttpRequestAsyncComplete
{
    public void onHttpRequestAsyncListenerComplete(String result);
    public void onHttpRequestAsyncListenerError(int errCode, String errMsg);
}
