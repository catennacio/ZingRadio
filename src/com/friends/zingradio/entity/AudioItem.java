package com.friends.zingradio.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class AudioItem implements Parcelable
{
    public static final int URL_STREAM = 1;
    public static final int URL_SDCARD = 2;
    public static final int URL_INVALID = -1;
    
    private String mId;
    private String mType;
    private String mTitle;
    private String mPerformer;
    private String mSource;
    private String mThumbnail;
    private String mLink;
    private String mPlink;
    private int mDuration;
    private String mAlbum;
    private String mDownloadLink;
    private String mLyrics;

    public AudioItem(){}
    
    public AudioItem(Parcel in)
    {
        readFromParcel(in);
    }
    
    public String getType()
    {
        return mType;
    }
    public void setType(String type)
    {
        this.mType = type;
    }
    
    public String getId()
    {
        return mId;
    }
    public void setId(String mId)
    {
        this.mId = mId;
    }
    
    public String getTitle()
    {
        return mTitle;
    }
    public void setTitle(String mTitle)
    {
        this.mTitle = mTitle;
    }
    
    public String getPerformer()
    {
        return mPerformer;
    }
    public void setPerformer(String mPerformer)
    {
        this.mPerformer = mPerformer;
    }
    
    public String getSource()
    {
        return mSource;
    }
    public void setSource(String mSource)
    {
        this.mSource = mSource;
    }
    
    public String getThumbnail()
    {
        return mThumbnail;
    }
    public void setThumbnail(String mThumbnail)
    {
        this.mThumbnail = mThumbnail;
    }
    
    public String getLink()
    {
        return mLink;
    }
    public void setLink(String mLink)
    {
        this.mLink = mLink;
    }
    
    public String getPlink()
    {
        return mPlink;
    }
    public void setPlink(String mPlink)
    {
        this.mPlink = mPlink;
    }
    
    public int getDuration()
    {
        return mDuration;
    }
    public void setDuration(int mDuration)
    {
        this.mDuration = mDuration;
    }
    
    public String getAlbum()
    {
        return mAlbum;
    }
    public void setAlbum(String mAlbum)
    {
        this.mAlbum = mAlbum;
    }
    
    public String getDownloadLink()
    {
        return mDownloadLink;
    }

    public void setDownloadLink(String mDownloadLink)
    {
        this.mDownloadLink = mDownloadLink;
    }
    
    public String getLyrics()
    {
        return mLyrics;
    }

    public void setLyrics(String mLyrics)
    {
        this.mLyrics = mLyrics;
    }
    
    @Override
    public int describeContents()
    {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(mId);
        dest.writeString(mType);
        dest.writeString(mTitle);
        dest.writeString(mPerformer);
        dest.writeString(mSource);
        dest.writeString(mThumbnail);
        dest.writeString(mLink);
        dest.writeString(mPlink);
        dest.writeInt(mDuration);
        dest.writeString(mAlbum);
        dest.writeString(mDownloadLink);
        dest.writeString(mLyrics);
    }
    
    private void readFromParcel(Parcel in)
    {
        mId = in.readString();
        mType = in.readString();
        mTitle = in.readString();
        mPerformer = in.readString();
        mSource = in.readString();
        mThumbnail = in.readString();
        mLink = in.readString();
        mPlink = in.readString();
        mDuration = in.readInt();
        mAlbum = in.readString();
        mDownloadLink = in.readString();
        mLyrics = in.readString();
    }
    
    public static final Parcelable.Creator<AudioItem> CREATOR = new Parcelable.Creator<AudioItem>()
    {
        public AudioItem createFromParcel(Parcel in)
        {
            return new AudioItem(in);
        }

        public AudioItem[] newArray(int size)
        {
            return new AudioItem[size];
        }
    };

    public boolean isStreaming()
    {
        if(mSource == null) return false;
        else return mSource.startsWith("http:") || mSource.startsWith("https:");
    }
    
    public int getUrlType()
    {
        int res = URL_INVALID;
        if(isStreaming()) res = URL_STREAM;
        else if(mSource.startsWith("sdcard")) res = URL_SDCARD;
        return res;
    }

    public AudioItem clone()
    {
        AudioItem a = new AudioItem();
        a.setAlbum(this.getAlbum());
        a.setDownloadLink(this.getDownloadLink());
        a.setDuration(this.getDuration());
        a.setId(this.getId());
        a.setLink(this.getLink());
        a.setLyrics(this.getLyrics());
        a.setPerformer(this.getPerformer());
        a.setPlink(this.getPlink());
        a.setSource(this.getSource());
        a.setThumbnail(this.getThumbnail());
        a.setTitle(this.getTitle());
        a.setType(this.getType());
        return a;
    }
    
}
