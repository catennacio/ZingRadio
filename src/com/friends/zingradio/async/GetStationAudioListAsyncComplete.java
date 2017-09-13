package com.friends.zingradio.async;

import java.util.ArrayList;

import com.friends.zingradio.entity.AudioItem;

public interface GetStationAudioListAsyncComplete
{
    public void onGetStationAudioListAsyncComplete(ArrayList<AudioItem> ai);
    public void onGetStationAudioListAsyncError(int errCode, String errMsg);
}
