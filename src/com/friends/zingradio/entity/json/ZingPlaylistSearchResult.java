package com.friends.zingradio.entity.json;

import java.util.ArrayList;

public class ZingPlaylistSearchResult
{
    ArrayList<Song> Data = new ArrayList<Song>();
    ZingPlaylistInfoSearchResult Info;
    
    public ArrayList<Song> getSongList()
    {
        return Data;
    }
    
    public ZingPlaylistInfoSearchResult getInfo()
    {
        return Info;
    }
}
