package com.friends.zingradio.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.friends.zingradio.entity.AudioItem;

public class RadioXmlParser extends XmlParser
{   
    public static String ROOT_TAG = "data";
    public static String AUDIO_ITEM_TAG = "item";
    public static String AUDIO_ITEM_ID_TAG = "id";
    public static String AUDIO_ITEM_TYPE_TAG = "type";
    public static String AUDIO_ITEM_TITLE_TAG = "title";
    public static String AUDIO_ITEM_PERFORMER_TAG = "performer";
    public static String AUDIO_ITEM_SOURCE_TAG = "source";
    public static String AUDIO_ITEM_THUMBNAIL_TAG = "thumbnail";
    public static String AUDIO_ITEM_LINK_TAG = "link";
    public static String AUDIO_ITEM_PLINK_TAG = "plink";
    public static String AUDIO_ITEM_DURATION_TAG = "duration";
    public static String AUDIO_ITEM_ALBUM_TAG = "album";
    
    public static ArrayList<AudioItem> parseRadioXml(InputStream in) throws XmlPullParserException, IOException
    {
        ArrayList<AudioItem> al = new ArrayList<AudioItem>();
        try
        {
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            
            parser.require(XmlPullParser.START_TAG, ns, ROOT_TAG);
            
            while (parser.next() != XmlPullParser.END_TAG)
            {
                if (parser.getEventType() != XmlPullParser.START_TAG)
                {
                    continue;
                }
                
                String name = parser.getName();
                
                // Starts by looking for the entry tag
                if (name.equals(AUDIO_ITEM_TAG))
                {
                    AudioItem ai = readItem(parser);
                    al.add(ai);
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
            if(in != null)
            {
                in.close();    
            }
        }
        return al;
    }
    
    private static AudioItem readItem(XmlPullParser parser) throws XmlPullParserException, IOException
    {
        AudioItem ai = new AudioItem();
        
        parser.require(XmlPullParser.START_TAG, ns, AUDIO_ITEM_TAG);
        String id;
        String title;
        String performer;
        String source;
        String thumbnail;
        String link;
        String plink;
        int duration;
        String album;
        
        while (parser.next() != XmlPullParser.END_TAG)
        {
            if (parser.getEventType() != XmlPullParser.START_TAG)
            {
                continue;
            }
            String name = parser.getName();
            if (name.equals(AUDIO_ITEM_ID_TAG))
            {
                //parser.require(XmlPullParser.START_TAG, ns, AUDIO_ITEM_ID_TAG);
                id = readTagValue(parser, name);
                ai.setId(id);
            }
            else if (name.equals(AUDIO_ITEM_TITLE_TAG))
            {
                title = readTagValue(parser, name);
                ai.setTitle(title);
            }
            else if (name.equals(AUDIO_ITEM_PERFORMER_TAG))
            {
                performer = readTagValue(parser, name);
                ai.setPerformer(performer);
            }
            else if (name.equals(AUDIO_ITEM_SOURCE_TAG))
            {
                source = readTagValue(parser, name);
                ai.setSource(source);
            }
            else if (name.equals(AUDIO_ITEM_THUMBNAIL_TAG))
            {
                thumbnail = readTagValue(parser, name);
                ai.setThumbnail(thumbnail);
            }
            else if (name.equals(AUDIO_ITEM_LINK_TAG))
            {
                link = readTagValue(parser, name);
                ai.setLink(link);
            }
            else if (name.equals(AUDIO_ITEM_PLINK_TAG))
            {
                plink = readTagValue(parser, name);
                ai.setPlink(plink);
            }
            else if (name.equals(AUDIO_ITEM_DURATION_TAG))
            {
                duration = Integer.parseInt(readTagValue(parser, name));
                ai.setDuration(duration);
            }
            else if (name.equals(AUDIO_ITEM_ALBUM_TAG))
            {
                album = readTagValue(parser, name);
                ai.setAlbum(album);
            }
            else
            {
                skip(parser);
            }
        }

        return ai;
    }
    
    private static String readTagValue(XmlPullParser parser, String tagName) throws XmlPullParserException, IOException
    {
        //parser.require(XmlPullParser.COMMENT, ns, "CDATA");
        parser.nextToken();
        String result = parser.getText();
        parser.nextTag();
        
        /*if (parser.next() == XmlPullParser.TEXT)
        {
            
        }*/
        return result;
    }
}
