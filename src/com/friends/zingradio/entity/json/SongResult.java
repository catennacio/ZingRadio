package com.friends.zingradio.entity.json;

import java.util.ArrayList;

public class SongResult implements IJsonEntity
{
    ArrayList<SongSearchResult> list = new ArrayList<SongSearchResult>();
    String title;

    public ArrayList<SongSearchResult> getSongList()
    {
        return list;
    }
    
    public String getTitle()
    {
        return (title != null)?title:"";
    }

    @Override
    public void free()
    {
        for(SongSearchResult ssr : list)
        {
            ssr.free();
        }
        title = null;
    }
}
