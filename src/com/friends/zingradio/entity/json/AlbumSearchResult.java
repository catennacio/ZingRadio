package com.friends.zingradio.entity.json;

public class AlbumSearchResult implements IJsonEntity
{
    String artist;
    String avatar;
    String object_id;
    String name;
    
    public String getArtist()
    {
        return (artist != null) ? artist: "";
    }
    public String getAvatar()
    {
        return (avatar != null) ? avatar : "";
    }
    public String getObject_id()
    {
        return (object_id != null) ? object_id : "";
    }
    
    public String getName()
    {
        return (name != null) ? name:"";
    }
    @Override
    public void free()
    {
        artist = null;
        avatar = null;
        object_id = null;
        name = null;
    }
}
