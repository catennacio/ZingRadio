package com.friends.zingradio.entity;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class Category implements Parcelable
{
    private String mId;
    private String mName;
    private String mIcon;
    private String mUrl;
    private ArrayList<Station> mStations = new ArrayList<Station>();
    
    public Category() {}
    
    public Category(String id, String name)
    {
        this.mId = id;
        this.mName = name;
    }
    
    public Category(Parcel in)
    {
        readFromParcel(in);
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
    
    public String getIcon()
    {
        return mIcon;
    }

    public void setIcon(String mIcon)
    {
        this.mIcon = mIcon;
    }

    public String getUrl()
    {
        return mUrl;
    }

    public void setUrl(String mUrl)
    {
        this.mUrl = mUrl;
    }

    public ArrayList<Station> getStations()
    {
        return mStations;
    }

    public void setStations(ArrayList<Station> mStations)
    {
        this.mStations = mStations;
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
        dest.writeString(mName);
        dest.writeString(mIcon);
        dest.writeString(mUrl);
        dest.writeTypedList(mStations);
    }
    
    private void readFromParcel(Parcel in)
    {
        mId = in.readString();
        mName = in.readString();
        mIcon = in.readString();
        mUrl = in.readString();
        in.readTypedList(mStations, Station.CREATOR);
        //in.readList(mStations, Station.class.getClassLoader());
    }
    
    public static final Parcelable.Creator<Category> CREATOR = new Parcelable.Creator<Category>()
    {
        public Category createFromParcel(Parcel in)
        {
            return new Category(in);
        }

        public Category[] newArray(int size)
        {
            return new Category[size];
        }
    };
    
    public Category clone()
    {
        Category c = new Category();
        c.setId(this.mId);
        c.setName(this.mName);
        c.setIcon(this.mIcon);
        c.setUrl(this.mUrl);
        c.setStations((ArrayList<Station>) this.mStations.clone());//potential memory leak. need to loop through each object and clone
        return c;
    }
}
