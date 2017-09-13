package com.friends.zingradio.async;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParserException;

import com.friends.zingradio.entity.AudioItem;
import com.friends.zingradio.entity.Station;
import com.friends.zingradio.util.Constants;
import com.friends.zingradio.util.RadioXmlParser;

import android.os.AsyncTask;
import android.util.Log;

public class GetStationAudioListAsync extends AsyncTask<String, String, ArrayList<AudioItem>>
{
    public static String TAG = GetStationAudioListAsync.class.getSimpleName();
    private int errCode = 0;
    private String errMsg;
    
    //private ArrayList<AudioItem> mAudioList;
    private GetStationAudioListAsyncComplete mOnGetStationAudioListAsyncCompletedListener;
    
    public GetStationAudioListAsync(GetStationAudioListAsyncComplete listener)
    {
        this.mOnGetStationAudioListAsyncCompletedListener = listener;
    }
    
    @Override
    protected ArrayList<AudioItem> doInBackground(String... params)
    {
        String urlBegin = "";
        String url = Constants.ROOT_XML_RADIO_URL + params[0];
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        //httpGet.addHeader("Accept", "text/xml");

        ArrayList<AudioItem> list = null;// = new ArrayList<AudioItem>();
        try
        {
            HttpResponse response = httpclient.execute(httpGet);
            
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK)
            {
                HttpEntity entity = response.getEntity();
                
                /*
                ByteArrayOutputStream out = new ByteArrayOutputStream(); 
                entity.writeTo(out);
                out.close();
                String s  = out.toString();
                Log.d(TAG, "resultXml=" + s);
                 */
                
                BufferedHttpEntity buf = new BufferedHttpEntity(entity);
                InputStream in = buf.getContent();
                list = RadioXmlParser.parseRadioXml(in);
                in.close();
            }
            else
            {
                Log.d(TAG, "FAIL - Code=" + response.getStatusLine().getStatusCode() + "\nReason=" + statusLine.getReasonPhrase());
                errCode = response.getStatusLine().getStatusCode();
                errMsg = statusLine.getReasonPhrase();
                response.getEntity().getContent().close();
            }
            
        }
        catch (ClientProtocolException e)
        {
            errCode = -1;
            errMsg = e.getMessage();
            e.printStackTrace();
            Log.e(TAG, "" + e.getMessage());
        }
        catch (IOException e)
        {
            errCode = -1;
            errMsg = e.getMessage();
            e.printStackTrace();
            Log.e(TAG, "" + e.getMessage());
        }
        catch (XmlPullParserException e)
        {
            errCode = -1;
            errMsg = e.getMessage();
            e.printStackTrace();
            Log.e(TAG, "" + e.getMessage());
        }
        catch (RuntimeException e)
        {
            errCode = -1;
            errMsg = e.getMessage();
            e.printStackTrace();
            Log.e(TAG, "" + e.getMessage());
        }
        catch (Exception e)
        {
            errCode = -1;
            errMsg = e.getMessage();
            e.printStackTrace();
            Log.e(TAG, "" + e.getMessage());
        }
        
        return list;
    }

    @Override
    protected void onPostExecute(ArrayList<AudioItem> list)
    {
        if(errCode == 0)
        {
            mOnGetStationAudioListAsyncCompletedListener.onGetStationAudioListAsyncComplete(list);    
        }
        else
        {
            mOnGetStationAudioListAsyncCompletedListener.onGetStationAudioListAsyncError(errCode, errMsg);
        }
    }

}
