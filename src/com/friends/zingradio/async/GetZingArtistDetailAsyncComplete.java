package com.friends.zingradio.async;

import com.friends.zingradio.entity.json.ArtistInfo;

public interface GetZingArtistDetailAsyncComplete
{
    public void onGetZingArtistDetailComplete(ArtistInfo artistInfo);
    public void onGetZingArtistDetailError(int errCode, String errMsg);
}
