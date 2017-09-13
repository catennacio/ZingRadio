package com.friends.zingradio.async;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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

import com.friends.zingradio.entity.json.SearchResult;
import com.google.gson.Gson;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

public class PerformSearchAsync extends AsyncTask<String, Void, SearchResult>
{
    public static final String TAG = PerformSearchAsync.class.getSimpleName(); 
    public static final String SEARCH_URL = "http://mp3.zing.vn/suggest/search?term=";
    private PerformSearchAsyncComplete mPerformSearchListener;
    private int errCode = 0;
    private String errMsg;
    
    public PerformSearchAsync(PerformSearchAsyncComplete lis)
    {
        mPerformSearchListener = lis;
    }

    @Override
    protected SearchResult doInBackground(String... arg0)
    {
        SearchResult result = new SearchResult();
        HttpClient httpclient = new DefaultHttpClient();
        try
        {
            String s = arg0[0];
            //String s1 = new String(Base64.encode(s.getBytes(), Base64.URL_SAFE));
            String query = URLEncoder.encode(s, "UTF-8");
            String url = SEARCH_URL + query;
            //Log.d(TAG, "Url=" + url);

            HttpGet httpGet = new HttpGet(url);
            
            HttpResponse response = httpclient.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK)
            {
                HttpEntity entity = response.getEntity();
                ByteArrayOutputStream out = new ByteArrayOutputStream(); 
                entity.writeTo(out);
                String resultString  = out.toString();
                out.close();
                //Log.d(TAG, "result=" + resultString);
                resultString = resultString.trim();
                
                if(resultString != null && !resultString.isEmpty())
                {
                    Gson gson = new Gson();
                    result = gson.fromJson(resultString, SearchResult.class);
                }
            }
            else
            {
                Log.e(TAG, "FAIL - Code=" + statusLine.getStatusCode() + "\nReason=" + statusLine.getReasonPhrase());
                response.getEntity().getContent().close();
                errCode = statusLine.getStatusCode();
                errMsg = statusLine.getReasonPhrase();
            }
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
            errCode = -1;
            errMsg = e.getMessage();
        }
        catch (ClientProtocolException e)
        {
            e.printStackTrace();
            errCode = -1;
            errMsg = e.getMessage();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            errCode = -1;
            errMsg = e.getMessage();
        }
        catch(IllegalStateException e)
        {
            e.printStackTrace();
            errCode = -1;
            errMsg = e.getMessage();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            errCode = -1;
            errMsg = e.getMessage();
        }
        finally
        {
            if(httpclient != null)
            {
                httpclient.getConnectionManager().shutdown();    
            }
        }
        
        return result;
    }

    protected void onPostExecute(SearchResult result)
    {
        if(errCode == 0)
        {
            mPerformSearchListener.onPerformSearchComplete(result);    
        }
        else
        {
            mPerformSearchListener.onPerformSearchError(errCode, errMsg);
        }
    }
}
