package com.friends.zingradio.entity;

public class LiveFeed
{
    String mUserId;
    String mUsername;
    String mSongId;
    String mSongname;
    String mArtistname;
    String mMessage;
    String mUserFullname;

    public String getUserFullname()
    {
        return mUserFullname;
    }

    public void setUserFullname(String mUserFullname)
    {
        this.mUserFullname = mUserFullname;
    }

    public String getUserId()
    {
        return mUserId;
    }
    public void setUserId(String mUserId)
    {
        this.mUserId = mUserId;
    }
    public String getUsername()
    {
        return mUsername;
    }
    public void setUsername(String mUsername)
    {
        this.mUsername = mUsername;
    }
    public String getSongId()
    {
        return mSongId;
    }
    public void setSongId(String mSongId)
    {
        this.mSongId = mSongId;
    }
    public String getArtistname()
    {
        return mArtistname;
    }
    public void setArtistname(String mArtistname)
    {
        this.mArtistname = mArtistname;
    }
    public String getMessage()
    {
        return mMessage;
    }
    public void setMessage(String mMessage)
    {
        this.mMessage = mMessage;
    }
    
    public String getSongname()
    {
        return mSongname;
    }
    public void setSongname(String mSongname)
    {
        this.mSongname = mSongname;
    }
}
