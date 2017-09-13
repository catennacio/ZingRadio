package com.friends.zingradio.entity.json;

public class GenreDetails
{
    private String GenreID;
    private String GenreName;
    
    public String getGenreID()
    {
        return (GenreID != null)?GenreID:"";
    }
    
    public String getGenreName()
    {
        return (GenreName != null)?GenreName:"";
    }
}
