package com.friends.zingradio.async;

import java.text.Normalizer;
import java.util.Scanner;

import com.friends.zingradio.entity.json.ArtistInfo;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

public class GetStationServerIdAsync extends AsyncTask<String, Void, String> implements GetZingArtistDetailAsyncComplete,
                                                                                        HttpRequestAsyncComplete
{
    public static final String TAG = GetStationServerIdAsync.class.getSimpleName();
    public static final String RADIO_URL = "http://mp3.zing.vn/radio/";
    
    private GetStationServerIdAsyncComplete mGetStationServerIdListener;
    
    public GetStationServerIdAsync(GetStationServerIdAsyncComplete lis)
    {
        mGetStationServerIdListener = lis;
    }
    
    @Override
    protected String doInBackground(String... arg0)
    {
        String artistId = arg0[0];
        
      //get artist name to build Url
        new GetZingArtistDetailAsync(this).execute(artistId);
        return null;
    }

    @Override
    protected void onPostExecute(String result) {}

    @Override
    public void onGetZingArtistDetailComplete(ArtistInfo artistInfo)
    {
        if(artistInfo != null)
        {
            String name = artistInfo.getArtistName();
            //Log.d(TAG, "name=" + name);
            if(name == null || name.isEmpty())
            {
                mGetStationServerIdListener.onGetStationServerIdError(-1, "Artist name is null or empty");
            }
            else
            {
                name = name.trim();
                String nameLatin= Normalizer.normalize(name, Normalizer.Form.NFC);

                nameLatin = nameLatin.replace(" ", "-");
                String url = RADIO_URL + nameLatin;
                //Log.d(TAG, "name=" + nameLatin);
                //Log.d(TAG, "url=" + url);
                new HttpRequestAsync(this).execute(url);
            }
        }

    }
    
    @Override
    public void onGetZingArtistDetailError(int errCode, String errMsg)
    {
        mGetStationServerIdListener.onGetStationServerIdError(errCode, errMsg);
    }
    
    @Override
    public void onHttpRequestAsyncListenerComplete(String result)
    {
        if(result == null || result.isEmpty())
        {
            mGetStationServerIdListener.onGetStationServerIdError(GetStationServerIdAsyncComplete.ERR_CODE_RESPONSE_MALFORMED, "Response Malformed.");
        }
        else
        {
            Scanner scanner = new Scanner(result);
            String line = "";
            boolean found = false;
            while(scanner.hasNextLine() && !found)
            {
                line = scanner.nextLine();
                //Log.d(TAG, "line=" + line);
                if (line.toLowerCase().contains("xmlURL:".toLowerCase()))
                {
                    //Log.d(TAG, "************* here ***************");
                    found = true;
                }
            }

            scanner.close();
            
            if(!found)
            {
                mGetStationServerIdListener.onGetStationServerIdError(GetStationServerIdAsyncComplete.ERR_CODE_RESPONSE_MALFORMED, "Response Malformed.");
            }
            else
            {
                String[] lineTokens = line.split("xmlURL:");
                //Log.d(TAG, "lineTokens length=" + lineTokens.length);
                if(lineTokens.length == 2)
                {
                    String url = lineTokens[1];
                    url = url.substring(1, url.length() - 1);
                    //Log.d(TAG, "url=" + url);
                    Uri uri = Uri.parse(url);
                    String path = uri.getPath();
                    String[] pathTokens = path.split("/");
                    String id = pathTokens[pathTokens.length - 1];
                    //Log.d(TAG, "id=" + id);
                    mGetStationServerIdListener.onGetStationServerIdComplete(id);
                }
                else
                {
                    mGetStationServerIdListener.onGetStationServerIdError(GetStationServerIdAsyncComplete.ERR_CODE_RESPONSE_MALFORMED, "Response Malformed.");
                }    
            }
        }
    }

    @Override
    public void onHttpRequestAsyncListenerError(int errCode, String errMsg)
    {
        mGetStationServerIdListener.onGetStationServerIdError(errCode, errMsg);
    }
}
