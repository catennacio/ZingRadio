package com.friends.zingradio.entity;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Station implements Parcelable
{
    public static final String TAG = Station.class.getSimpleName();
    public enum Type { Radio, Album };
    
    public static final String MY_SONGS_ID = "MY_SONGS_ID";
    //public static final String MY_SONGS_NAME = "My songs";
    public static final String ALBUM_NAME = "Album";
    
    
    public static final String SEARCH_ID = "SEARCH_ID";
    //public static final String SEARCH_NAME = "My songs";

    //public static final String FAVORITE_CHANNEL1 = "My channels";
    
    private String mId;
    private String mIcon;
    private String mName;
    private String mUrl;
    private String mServerId;
    private ArrayList<AudioItem> mAudioItems = new ArrayList<AudioItem>();
    private String mCategoryId;
    private String mCategoryName;
    private Type mType;

    public Station()
    {
    }

    public Station(Parcel in)
    {
        readFromParcel(in);
    }

    public ArrayList<AudioItem> getAudioItems()
    {
        return mAudioItems;
    }

    public void setAudioItems(ArrayList<AudioItem> mAudioItems)
    {
        freeAudioItems();
        this.mAudioItems = mAudioItems;
    }

    public String getServerId()
    {
        return mServerId;
    }

    public void setServerId(String serverId)
    {
        this.mServerId = serverId;
    }
    
    public String getIcon()
    {
        return mIcon;
    }

    public void setIcon(String icon)
    {
        this.mIcon = icon;
    }

    public String getId()
    {
        return mId;
    }

    public void setId(String id)
    {
        this.mId = id;
    }

    public String getName()
    {
        return mName;
    }

    public void setName(String name)
    {
        this.mName = name;
    }
    
    public String getUrl()
    {
        return mUrl;
    }

    public void setUrl(String url)
    {
        this.mUrl = url;
    }
    
    public void printData()
    {
        //Log.d(TAG, "printData()() - Station id=" + this.getId() + " name=" + this.getName() + " icon=" + this.getIcon() + " serverId=" + this.getServerId() + " url=" + this.getUrl() );
    }

    /*
    public Category getCategory()
    {
        return mCategory;
    }

    public void setCategory(Category mCategory)
    {
        this.mCategory = mCategory;
    }
     */
    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(mId);
        dest.writeString(mIcon);
        dest.writeString(mName);
        dest.writeString(mUrl);
        dest.writeString(mServerId);
        if(mAudioItems != null) dest.writeTypedList(mAudioItems);
        dest.writeString(mCategoryId);
        dest.writeString(mCategoryName);
        dest.writeString(mType != null? mType.name() : "");
    }
    
    private void readFromParcel(Parcel in)
    {
        mId = in.readString();
        mIcon = in.readString();
        mName = in.readString();
        mUrl = in.readString();
        mServerId = in.readString();
        if(mAudioItems != null) in.readTypedList(mAudioItems, AudioItem.CREATOR);
        mCategoryId = in.readString();
        mCategoryName = in.readString();
        try
        {
            mType = Type.valueOf(in.readString());
        }
        catch(Exception e)
        {
            mType = Type.Radio;
        }
    }

    public String getCategoryName()
    {
        return mCategoryName;
    }

    public void setCategoryName(String mCategoryName)
    {
        this.mCategoryName = mCategoryName;
    }

    public String getCategoryId()
    {
        return mCategoryId;
    }

    public void setCategoryId(String mCategoryId)
    {
        this.mCategoryId = mCategoryId;
    }

    public Type getType()
    {
        return mType;
    }

    public void setType(Type mType)
    {
        this.mType = mType;
    }

    public static final Parcelable.Creator<Station> CREATOR = new Parcelable.Creator<Station>()
    {
        public Station createFromParcel(Parcel in)
        {
            return new Station(in);
        }

        public Station[] newArray(int size)
        {
            return new Station[size];
        }
    };

    public void freeAudioItems()
    {
        if(mAudioItems != null)
        {
            for(AudioItem ai : mAudioItems)
            {
                ai = null;
            }
            mAudioItems.clear();
            mAudioItems = null;
        }
    }
    
    public Station clone()
    {
        Station st = new Station();
        st.setCategoryId(this.getCategoryId());
        st.setCategoryName(this.getCategoryName());
        st.setIcon(this.getIcon());
        st.setId(this.getId());
        st.setName(this.getName());
        st.setServerId(this.getServerId());
        st.setType(this.getType());
        st.setUrl(this.getUrl());
        
        if(mAudioItems != null)
        {
            for(AudioItem ai : mAudioItems)
            {
                st.getAudioItems().add(ai.clone());
            }    
        }

        return st;
    }
}
