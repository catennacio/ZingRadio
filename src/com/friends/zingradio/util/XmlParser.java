package com.friends.zingradio.util;

import org.kxml2.io.*;
import org.kxml2.kdom.*;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

public abstract class XmlParser
{
    //protected static XmlPullParser parser = Xml.newPullParser();
    protected static XmlPullParser parser = new KXmlParser();
    //protected KXmlParser parser = null;
    protected static final String ns = null;
    
    /*
    protected XmlParser getInstance()
    {
        
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        parser = factory.newPullParser();
        return parser;
    }*/
    
    protected static void skip(XmlPullParser parser) throws XmlPullParserException, IOException
    {
        if (parser.getEventType() != XmlPullParser.START_TAG)
        {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0)
        {
            switch (parser.next())
            {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
