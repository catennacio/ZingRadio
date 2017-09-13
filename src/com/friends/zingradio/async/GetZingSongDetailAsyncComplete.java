package com.friends.zingradio.async;

import com.friends.zingradio.entity.json.Song;

public interface GetZingSongDetailAsyncComplete
{
    public void onGetZingSongDetailAsyncComplete(Song song);
    public void onGetZingSongDetailAsyncError(int errCode, String errMsg);
}
