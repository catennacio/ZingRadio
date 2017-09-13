package com.friends.zingradio.async;

import com.friends.zingradio.entity.json.SearchResult;

public interface PerformSearchAsyncComplete
{
    public void onPerformSearchComplete(SearchResult result);
    public void onPerformSearchError(int errCode, String errMsg);
}
