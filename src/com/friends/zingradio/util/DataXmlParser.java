package com.friends.zingradio.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.provider.ContactsContract.Contacts.Data;
import android.util.Log;

import com.friends.zingradio.entity.Category;
import com.friends.zingradio.entity.Radio;
import com.friends.zingradio.entity.Station;

public class DataXmlParser extends XmlParser
{
    public static String XML_DATA_ROOT_NODE = "zingradio";
    public static String XML_DATA_CATEGORIES_NODE = "categories";
    public static String XML_DATA_CATEGORY_NODE = "category";
    public static String XML_DATA_CATEGORY_NAME_NODE = "name";
    public static String XML_DATA_CATEGORY_ICON_NODE = "icon";
    public static String XML_DATA_CATEGORY_ID_NODE = "id";
    public static String XML_DATA_CATEGORY_URL_NODE = "url";
    public static String XML_DATA_STATIONS_NODE = "stations";
    public static String XML_DATA_STATION_NODE = "station";
    public static String XML_DATA_STATION_NAME_NODE = "name";
    public static String XML_DATA_STATION_ICON_NODE = "icon";
    public static String XML_DATA_STATION_ID_NODE = "id";
    public static String XML_DATA_STATION_SERVERID_NODE = "serverId";
    public static String XML_DATA_STATION_URL_NODE = "url";
    
    public static String TAG = DataXmlParser.class.getSimpleName();
    
    private static Radio mRadio = new Radio();
    private static Category mCat = new Category();
    private static Station mSta = new Station();
    
    public static Radio parseDataXml(InputStream in) throws XmlPullParserException, IOException
    {
        ArrayList<Category> categories = null;
        
        try
        {
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            parser.require(XmlPullParser.START_TAG, ns, XML_DATA_ROOT_NODE);
            
            while (parser.next() != XmlPullParser.END_TAG)
            {
                if (parser.getEventType() != XmlPullParser.START_TAG)
                {
                    continue;
                }
                
                String name = parser.getName();
                //Log.i(TAG, "parseDataXml() - tagname=" + name);
                
                // Starts by looking for the categories tag
                if (name.equals(XML_DATA_CATEGORIES_NODE))
                {
                    categories = readCategories(in);
                }
                else
                {
                    skip(parser);
                }
            } 
        }
        catch(XmlPullParserException ex)
        {
            ex.printStackTrace();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            in.close();
        }
        
        mRadio.setCategories(categories);
        
        return mRadio;
    }
    
    private static ArrayList<Category> readCategories(InputStream in) throws XmlPullParserException, IOException
    {
        ArrayList<Category> al = new ArrayList<Category>();
        while (parser.next() != XmlPullParser.END_TAG)
        {
            if (parser.getEventType() != XmlPullParser.START_TAG)
            {
                continue;
            }
            
            String name = parser.getName();
            //Log.i(TAG, "readCategories() - tagname=" + name);
            if (name.equals(XML_DATA_CATEGORY_NODE))
            {
                Category cat = readCategory(parser);
                al.add(cat);
                mCat = new Category();
            }
            else
            {
                skip(parser);
            }
        }
        return al;
    }
    
    private static Category readCategory(XmlPullParser parser) throws XmlPullParserException, IOException
    {
        parser.require(XmlPullParser.START_TAG, ns, XML_DATA_CATEGORY_NODE);
        
        String name, icon, id, url;
        
        while (parser.next() != XmlPullParser.END_TAG)
        {
            if (parser.getEventType() != XmlPullParser.START_TAG)
            {
                continue;
            }
            String tag = parser.getName();
            //Log.i(TAG, "readCategory() - tagname=" + tag);
            if (tag.equals(XML_DATA_CATEGORY_NAME_NODE))
            {
                name = readText(parser);
                //Log.i(TAG, "readCategory() - name=" + name);
                mCat.setName(name);
                //Log.i(TAG, "readCategory() - After setname=" + mCat.getName());
            }
            else if (tag.equals(XML_DATA_CATEGORY_ICON_NODE))
            {
                icon = readText(parser);
                //Log.i(TAG, "readCategory() - icon=" + icon);
                mCat.setIcon(icon);
                //Log.i(TAG, "readCategory() - After set icon=" + mCat.getIcon());
            }
            else if (tag.equals(XML_DATA_CATEGORY_ID_NODE))
            {
                id = readText(parser);
                //Log.i(TAG, "readCategory() - id=" + id);
                mCat.setId(id);
                //Log.i(TAG, "readCategory() - After set id=" + mCat.getId());
            }
            else if (tag.equals(XML_DATA_CATEGORY_URL_NODE))
            {
                url = readText(parser);
                //Log.i(TAG, "readCategory() - url=" + url);
                mCat.setUrl(url);
                //Log.i(TAG, "readCategory() - After set url=" + mCat.getUrl());
            }
            else if (tag.equals(XML_DATA_STATIONS_NODE))
            {
                //Log.i(TAG, "readCategory() - now read stations");
                ArrayList<Station> stations = readStations(parser);
                mCat.setStations(stations);
            }
            else
            {
                skip(parser);
            }
        }
        return mCat;
    }
    
    private static ArrayList<Station> readStations(XmlPullParser parser) throws XmlPullParserException, IOException
    {
        ArrayList<Station> stations = new ArrayList<Station>();
        parser.require(XmlPullParser.START_TAG, ns, XML_DATA_STATIONS_NODE);
        Station st;
        
        while (parser.next() != XmlPullParser.END_TAG)
        {
            st = new Station();
            
            if (parser.getEventType() != XmlPullParser.START_TAG)
            {
                continue;
            }
            
            String tag = parser.getName();
            //Log.i(TAG, "readStations() - tagname=" + tag);
            if (tag.equals(XML_DATA_STATION_NODE))
            {
                //Log.i(TAG, "readStations() - now read station");
                st = readStation(parser);
                st.setCategoryId(mCat.getId());
                st.setCategoryName(mCat.getName());
                //Log.d(TAG, "After readStation() id=" + st.getId() + " name=" + st.getName() + " icon=" + st.getIcon() + " serverId=" + st.getServerId() + " url=" + st.getUrl() );
            }
            else
            {
                skip(parser);
            }
            
            stations.add(st);
            mSta = new Station();
        }
        
        return stations;
    }
    
    private static Station readStation(XmlPullParser parser) throws XmlPullParserException, IOException
    {
        String name, icon, id, serverId, url;
        
        while (parser.next() != XmlPullParser.END_TAG)
        {
            if (parser.getEventType() != XmlPullParser.START_TAG)
            {
                continue;
            }
            
            String tag = parser.getName();
            //Log.i(TAG, "readStation() - tagname=" + tag);
            if (tag.equals(XML_DATA_STATION_NAME_NODE))
            {
                name = readText(parser);
                //Log.i(TAG, "readStation() - name=" + name);
                mSta.setName(name);
                //Log.i(TAG, "readStation() - After set name=" + mSta.getName());
                //mSta.printData();
            }
            else if (tag.equals(XML_DATA_STATION_ICON_NODE))
            {
                icon = readText(parser);
                //Log.i(TAG, "readStation() - icon=" + icon);
                //mSta.setIcon(icon);
                mSta.setIcon(null);//to reduce memory consumption
                //Log.i(TAG, "readStation() - After set icon=" + mSta.getIcon());
                //mSta.printData();
            }
            else if (tag.equals(XML_DATA_STATION_ID_NODE))
            {
                id = readText(parser);
                //Log.i(TAG, "readStation() - id=" + id);
                mSta.setId(id);
                //Log.i(TAG, "readStation() - After set id=" + mSta.getId());
                //mSta.printData();
            }
            else if (tag.equals(XML_DATA_STATION_SERVERID_NODE))
            {
                serverId = readText(parser);
                //Log.i(TAG, "readStation() - serverId=" + serverId);
                mSta.setServerId(serverId);
                //Log.i(TAG, "readStation() - After set serverId=" + mSta.getServerId());
                //mSta.printData();
            }
            else if (tag.equals(XML_DATA_STATION_URL_NODE))
            {
                url = readText(parser);
                //Log.i(TAG, "readStation() - url=" + url);
                mSta.setUrl(url);
                //Log.i(TAG, "readStation() - After set url=" + mSta.getUrl());
                //mSta.printData();
            }
            else
            {
                //mSta.printData();
                skip(parser);
            }
        }
        
        //st.printData();
        mSta.setType(Station.Type.Radio);
        return mSta;
    }
    
    private static String readText(XmlPullParser parser) throws XmlPullParserException, IOException
    {
        parser.next();
        String t = parser.getText();
        parser.nextTag();
        return t;
    }
    
    public static void printData()
    {
        //Log.i(TAG, "printData()");
        for(Category c : mRadio.getCategories())
        {
            //Log.i(TAG, "Category id=" + c.getId() + " name=" + c.getName() + " icon=" + c.getIcon()  + " url=" + c.getUrl());
            for(Station s : c.getStations())
            {
                //Log.d(TAG, "Station id=" + s.getId() + " name=" + s.getName() + " icon=" + s.getIcon() + " serverId=" + s.getServerId() + " url=" + s.getUrl() );                
            }
        }
    }
}
