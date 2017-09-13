package com.friends.zingradio.entity.json;

import java.util.ArrayList;

public class AlbumResult implements IJsonEntity
{
    ArrayList<AlbumSearchResult> list = new ArrayList<AlbumSearchResult>();
    String title;
    
    public ArrayList<AlbumSearchResult> getAlbumList()
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
        for(AlbumSearchResult asr : list)
        {
            asr.free();
        }
        
        title = null;
    }
}
