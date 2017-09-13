package com.friends.zingradio.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParserException;

import com.friends.zingradio.entity.AudioItem;

public class AlbumXmlParser extends XmlParser
{
    public static String ROOT_TAG = "data";
    public static String AUDIO_ITEM_TAG = "item";
    public static String AUDIO_ITEM_ID_TAG = "id";
    public static String AUDIO_ITEM_TYPE_TAG = "type";
    public static String AUDIO_ITEM_TITLE_TAG = "title";
    
    public static ArrayList<AudioItem> parse(InputStream in) throws XmlPullParserException, IOException
    {
        ArrayList<AudioItem> ret = new ArrayList<AudioItem>();
        return ret;
    }
}
