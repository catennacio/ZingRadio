package com.friends.zingradio.async;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.friends.zingradio.entity.json.ArtistInfo;
import com.friends.zingradio.util.Constants;
import com.friends.zingradio.util.Utilities;
import com.google.gson.Gson;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

public class GetAlbumServerIdAsync extends AsyncTask<String, Void, String> implements HttpRequestAsyncComplete
{
    public static final String TAG = GetAlbumServerIdAsync.class.getSimpleName();
    public static final String ZING_API_URL = "http://api.mp3.zing.vn/api/xml-mini?jsondata=";
    
    private int errCode = 0;
    private String errMsg;
    private GetAlbumServerIdAsyncComplete mGetAlbumServerIdAsyncComplete;
    protected GetAlbumServerIdAsync(){}
    
    public GetAlbumServerIdAsync(GetAlbumServerIdAsyncComplete lis)
    {
        mGetAlbumServerIdAsyncComplete = lis;
    }
    
    @Override
    protected String doInBackground(String... params)
    {
        String ret = "";
        try
        {
            String id = params[0];
            String name = params[1];
            
            //Log.d(TAG, "name before=" + name);
            name = name.replace(" ", "-");
            name = name.replace("+", "-");
            //Log.d(TAG, "name after=" + name);
            String url = "http://mp3.zing.vn/album/" + name + "/" + id + ".html";
            //Log.d(TAG, "url=" + url);
            
            if(id != null && !id.isEmpty() && name!= null && !name.isEmpty())
            {
                new HttpRequestAsync(this).execute(url);
            }
            else
            {
                errCode = -1;
                errMsg = "Id and/or name is null or empty";
                Log.e(TAG, "Id and/or name is null or empty");
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
        
        return ret;
    }
    
    @Override
    protected void onPostExecute(String result){}
    
    @Override
    public void onHttpRequestAsyncListenerComplete(String result)
    {
        if(result == null || result.isEmpty())
        {
            mGetAlbumServerIdAsyncComplete.OnGetAlbumServerIdAsyncError(GetAlbumServerIdAsyncComplete.ERR_CODE_RESPONSE_MALFORMED, "Response Malformed.");
        }
        else
        {
            
            try
            {
                Scanner scanner = new Scanner(result);
                String line = "";
                boolean found = false;
                while(scanner.hasNextLine() && !found)
                {
                    line = scanner.nextLine();
                    //Log.d(TAG, "line=" + line);
                    if (line.toLowerCase().contains("xmlURL=".toLowerCase()))
                    {
                        //Log.d(TAG, "************* here *************** line=" + line);
                        found = true;
                    }
                }

                scanner.close();
                
                if(!found)
                {
                    mGetAlbumServerIdAsyncComplete.OnGetAlbumServerIdAsyncError(GetAlbumServerIdAsyncComplete.ERR_CODE_RESPONSE_MALFORMED, "No line with xmlURL.");
                }
                else
                {
                    String[] lineTokens = line.split(";");
                    //Log.d(TAG, "lineTokens count=" + lineTokens.length);
                    if(lineTokens.length == 4)
                    {
                        String xmlLine = lineTokens[2];
                        //Log.d(TAG, "xmlLine=" + xmlLine);
                        String[] xmlLineTokens = xmlLine.split("=");
                        if(xmlLineTokens.length == 2)
                        {
                            String urlLine = xmlLineTokens[1];
                            //Log.d(TAG, "urlLine=" + urlLine);
                            String[] urlLineTokens = urlLine.split("&");
                            if(urlLineTokens.length == 2)
                            {
                                String serverUrl = urlLineTokens[0];
                                //Log.d(TAG, "serverUrl=" + serverUrl);
                                Uri uri = Uri.parse(serverUrl);
                                //Log.d(TAG, "uri=" + uri.toString());
                                //Log.d(TAG, "query=" + uri.getQuery() + " fragment=" + uri.getFragment() + " path=" + uri.getPath());
                                List<String> pathSegments = uri.getPathSegments();
                                String serId = pathSegments.get(pathSegments.size() - 1);
                                //Log.d(TAG, "serID=" + serId);
                                mGetAlbumServerIdAsyncComplete.OnGetAlbumServerIdAsyncComplete(serId);
                            }
                            else
                            {
                                mGetAlbumServerIdAsyncComplete.OnGetAlbumServerIdAsyncError(GetAlbumServerIdAsyncComplete.ERR_CODE_RESPONSE_MALFORMED, "UrlLine Malformed. (token length=" + urlLineTokens.length + ") instead of 2");
                            }
                        }
                        else
                        {
                            mGetAlbumServerIdAsyncComplete.OnGetAlbumServerIdAsyncError(GetAlbumServerIdAsyncComplete.ERR_CODE_RESPONSE_MALFORMED, "XmlLine Malformed. (token length=" + xmlLineTokens.length + ") instead of 2");
                        }
                    }                
                    else
                    {
                        mGetAlbumServerIdAsyncComplete.OnGetAlbumServerIdAsyncError(GetAlbumServerIdAsyncComplete.ERR_CODE_RESPONSE_MALFORMED, "Line Malformed. (token length=" + lineTokens.length + ") instead of 4");
                    }
                }
            }
            catch (Exception e)
            {
                Log.e(TAG, "Error parsing response. Msg=" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onHttpRequestAsyncListenerError(int errCode, String errMsg)
    {
        Log.d(TAG, "errCode=" + errCode + " errMsg=" + errMsg);
        mGetAlbumServerIdAsyncComplete.OnGetAlbumServerIdAsyncError(errCode, errMsg);
    }
}
