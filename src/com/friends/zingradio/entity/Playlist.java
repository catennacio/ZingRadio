package com.friends.zingradio.entity;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class Playlist implements Parcelable
{
    public static final String TAG = Playlist.class.getSimpleName();

    private String mArtistName;
    private String mCreatedDate;
    private int mHit;
    private String mId;
    private String mOfficial;
    private String mOwnerAcc;
    private String mPictureURL;
    private String mTitle;
    private int mTotalListen;
    private ArrayList<AudioItem> mAudioItems = new ArrayList<AudioItem>();
    
    public Playlist()
    {
    }
    
    public Playlist(Parcel in)
    {
        readFromParcel(in);
    }
    
    public ArrayList<AudioItem> getAudioItems()
    {
        return mAudioItems;
    }

    public void setAudioItems(ArrayList<AudioItem> mAudioItems)
    {
        this.mAudioItems = mAudioItems;
    }

    public String getArtistName()
    {
        return mArtistName;
    }

    public void setArtistName(String mArtistName)
    {
        this.mArtistName = mArtistName;
    }

    public String getCreatedDate()
    {
        return mCreatedDate;
    }

    public void setCreatedDate(String mCreatedDate)
    {
        this.mCreatedDate = mCreatedDate;
    }

    public int getHit()
    {
        return mHit;
    }

    public void setHit(int mHit)
    {
        this.mHit = mHit;
    }

    public String getId()
    {
        return mId;
    }

    public void setId(String mId)
    {
        this.mId = mId;
    }

    public String getOfficial()
    {
        return mOfficial;
    }

    public void setOfficial(String mOfficial)
    {
        this.mOfficial = mOfficial;
    }

    public String getOwnerAcc()
    {
        return mOwnerAcc;
    }

    public void setOwnerAcc(String mOwnerAcc)
    {
        this.mOwnerAcc = mOwnerAcc;
    }

    public String getPictureURL()
    {
        return mPictureURL;
    }

    public void setPictureURL(String mPictureURL)
    {
        this.mPictureURL = mPictureURL;
    }

    public String getTitle()
    {
        return mTitle;
    }

    public void setTitle(String mTitle)
    {
        this.mTitle = mTitle;
    }

    public int getTotalListen()
    {
        return mTotalListen;
    }

    public void setTotalListen(int mTotalListen)
    {
        this.mTotalListen = mTotalListen;
    }   

    @Override
    public int describeContents()
    {
        return 0;
    }

    private void readFromParcel(Parcel in)
    {
        mArtistName = in.readString();
        mCreatedDate = in.readString(); 
        mHit = in.readInt();
        mId = in.readString();
        mOfficial = in.readString();
        mOwnerAcc = in.readString();
        mPictureURL = in.readString();
        mTitle = in.readString();
        mTotalListen = in.readInt();
        in.readTypedList(mAudioItems, AudioItem.CREATOR);
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(mArtistName);
        dest.writeString(mCreatedDate);
        dest.writeInt(mHit);
        dest.writeString(mId);
        dest.writeString(mOfficial);
        dest.writeString(mOwnerAcc);
        dest.writeString(mPictureURL);
        dest.writeString(mTitle);
        dest.writeInt(mTotalListen);
        dest.writeTypedList(mAudioItems);
    }
    
    public static final Parcelable.Creator<Playlist> CREATOR = new Parcelable.Creator<Playlist>()
    {
        public Playlist createFromParcel(Parcel in)
        {
            return new Playlist(in);
        }

        public Playlist[] newArray(int size)
        {
            return new Playlist[size];
        }
    };
}
