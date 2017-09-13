package com.friends.zingradio.entity.json;

public class ArtistInfo
{
    String ArtistName;
    String ArtistAvatar;
    String ZmeAcc;
    String Biography;
    
    public String getArtistName()
    {
        return (ArtistName != null)?ArtistName:"";
    }
    public String getArtistAvatar()
    {
        return (ArtistAvatar != null)?ArtistAvatar:"";
    }
    public String getZmeAcc()
    {
        return (ZmeAcc!= null)?ZmeAcc:"";
    }
    public String getBiography()
    {
        return (Biography != null)?Biography:"";
    }
}
