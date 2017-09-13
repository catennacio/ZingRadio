package com.friends.zingradio.async;

import java.util.ArrayList;

import com.friends.zingradio.entity.AudioItem;

public interface SearchZingSongAsyncComplete
{
    public void onSearchZingSongAsyncComplete(ArrayList<AudioItem> al, int totalResults);
    public void onSearchZingSongAsyncError(int errCode, String errMsg);
}
