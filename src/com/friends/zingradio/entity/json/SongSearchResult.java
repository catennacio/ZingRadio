package com.friends.zingradio.entity.json;

public class SongSearchResult implements IJsonEntity
{
    String artist;
    String name;
    String object_id;
    
    public String getArtistName()
    {
        return (artist!= null)?artist:"";
    }
    
    public String getName()
    {
        return (name != null)?name:"";
    }
    
    public String getId()
    {
        return (object_id!= null)?object_id:"";
    }

    @Override
    public void free()
    {
        artist = null;
        name = null;
        object_id = null;
    }
}
