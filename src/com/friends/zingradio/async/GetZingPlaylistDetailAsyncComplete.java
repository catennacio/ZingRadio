package com.friends.zingradio.async;

import java.util.ArrayList;

import com.friends.zingradio.entity.AudioItem;
import com.friends.zingradio.entity.Playlist;

public interface GetZingPlaylistDetailAsyncComplete
{
    public void OnGetZingPlaylistDetailAsyncComplete(Playlist pl);
    public void OnGetZingPlaylistDetailAsyncError(int errCode, String errMsg);
}
