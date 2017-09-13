package com.friends.zingradio.entity;

import java.util.ArrayList;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Radio implements Parcelable
{
    public static String TAG = Radio.class.getSimpleName();
    
    private ArrayList<Category> mCategories;
    
    public Radio(){}
    
    public Radio(Parcel in)
    {
        readFromParcel(in);
    }

    public ArrayList<Category> getCategories()
    {
        return mCategories;
    }

    public void setCategories(ArrayList<Category> categories)
    {
        this.mCategories = categories;
    }
    
    public Category getCategory(String id)
    {
        Category cat = null;
        
        for(Category c : mCategories)
        {
            if(c.getId().equals(id))
            {
                cat = c;
                break;
            }
        }
        
        return cat.clone();
    }
    
    public Station getStation(String serverId)
    {
        Station st = null;
        for(Category c : mCategories)
        {
            for(Station s : c.getStations())
            {
                if(s.getServerId().equals(serverId))
                {
                    st = s;
                    break;
                }
            }
        }
        return st;
    }
   
    public void printData()
    {
        //Log.i(TAG, "printData()");
        for(Category c : this.getCategories())
        {
            //Log.i(TAG, "Category id=" + c.getId() + " name=" + c.getName() + " icon=" + c.getIcon()  + " url=" + c.getUrl());
            for(Station s : c.getStations())
            {
                s.printData();
            }
        }
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeTypedList(mCategories);
    }
    
    private void readFromParcel(Parcel in)
    {
        in.readTypedList(mCategories, Category.CREATOR);
    }
    
    public static final Parcelable.Creator<Radio> CREATOR = new Parcelable.Creator<Radio>()
    {
        public Radio createFromParcel(Parcel in)
        {
            return new Radio(in);
        }

        public Radio[] newArray(int size)
        {
            return new Radio[size];
        }
    };
    
    public int getStationCount()
    {
        int count = 0;
        for(Category c : mCategories)
        {
            count += c.getStations().size();
        }
        return count;
    }
}
