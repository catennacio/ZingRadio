package com.friends.zingradio.entity.json;

public class ArtistSearchResult  implements IJsonEntity
{
    String avatar;
    String name;
    String object_id;

    public String getAvatarUrl()
    {
        return (avatar!= null)?avatar:"";
    }
    
    public String getName()
    {
        return (name != null)?name:"";
    }

    public String getId()
    {
        return (object_id != null)?object_id:"";
    }
    
    public boolean isValid()
    {
        if (name != null && !name.isEmpty() 
               && avatar != null && !avatar.isEmpty() 
               && !avatar.toLowerCase().endsWith(("noavatar.gif")))
            return true;
        else return false;
    }

    @Override
    public void free()
    {
        avatar = null;
        name = null;
        object_id = null;
    }
}
