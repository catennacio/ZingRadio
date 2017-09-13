package com.friends.zingradio.entity.json;

import java.util.ArrayList;

public class SearchZingSongResult implements IJsonEntity
{
    ArrayList<Song> Data;// = new ArrayList<Song>();
    String ResultCount;
    
    public ArrayList<Song> getSongList()
    {
        return Data;
    }
    
    public String getResultCount()
    {
        return ResultCount;
    }

    @Override
    public void free()
    {
        if(Data != null)
        {
            for(Song s : Data)
            {
                s = null;
            }
            Data.clear();
            Data = null;
        }
    }
}
