package com.friends.zingradio.entity.json;

import java.util.ArrayList;;

public class ArtistResult implements IJsonEntity
{
    ArrayList<ArtistSearchResult> list = new ArrayList<ArtistSearchResult>();
    String title;
    
    public ArrayList<ArtistSearchResult> getArtistList()
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
        for(ArtistSearchResult asr : list)
        {
            asr.free();
        }
        title = null;
    }
}
