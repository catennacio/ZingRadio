package com.friends.zingradio.async;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import android.os.AsyncTask;
import android.util.Log;

public class HttpRequestAsync extends AsyncTask<String, Void, String>
{
    public static final String TAG = HttpRequestAsync.class.getSimpleName();
    private HttpRequestAsyncComplete mHttpRequestAsyncListener;
    private int errCode = 0;
    private String errMsg;
    
    public static final int ERR_CODE_URL_NULL = -1;
    public static final String ERR_MSG_URL_NULL = "Url is null";
    public static final int ERR_CODE_UNSUPPORTED_ENCODING = -2;
    public static final int ERR_CODE_CLIENT_PROTOCOL = -3;
    public static final int ERR_CODE_IO_EXCEPTION = -4;
    public static final int ERR_CODE_EXCEPTION = -5;
    
    public HttpRequestAsync(HttpRequestAsyncComplete lis)
    {
        mHttpRequestAsyncListener = lis;
    }
    
    @Override
    protected String doInBackground(String... params)
    {
        String result = "";
        try
        {
            String s = params[0];
            if(s == null)
            {
                errCode = ERR_CODE_URL_NULL;
                errMsg = ERR_MSG_URL_NULL;
            }
            else
            {
              //String s1 = new String(Base64.encode(s.getBytes(), Base64.URL_SAFE));
                //String url = URLEncoder.encode(s, "UTF-8");
                Log.d(TAG, "Url=" + s);
                
                HttpClient httpclient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(s);            
                HttpResponse response = httpclient.execute(httpGet);
                
                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == HttpStatus.SC_OK)
                {
                    HttpEntity entity = response.getEntity();

                    ByteArrayOutputStream out = new ByteArrayOutputStream(); 
                    entity.writeTo(out);
                    result  = out.toString();
                    out.close();

                    //Log.d(TAG, "result=" + result);
                }
                else
                {
                    Log.e(TAG, "FAIL - Code=" + statusLine.getStatusCode() + "\nReason=" + statusLine.getReasonPhrase());
                    response.getEntity().getContent().close();
                    errCode = statusLine.getStatusCode();
                    errMsg = statusLine.getReasonPhrase();
                }
            }
        }
        catch (UnsupportedEncodingException e1)
        {
            errCode = ERR_CODE_UNSUPPORTED_ENCODING;
            errMsg = e1.getMessage();
            e1.printStackTrace();
        }
        catch (ClientProtocolException e)
        {
            errCode = ERR_CODE_CLIENT_PROTOCOL;
            errMsg = e.getMessage();
            e.printStackTrace();
        }
        catch (IOException e)
        {
            errCode = ERR_CODE_IO_EXCEPTION;
            errMsg = e.getMessage();
            e.printStackTrace();
        }
        catch (Exception e)
        {
            errCode = ERR_CODE_EXCEPTION;
            errMsg = e.getMessage();
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result)
    {
        if(errCode != 0)
        {
            mHttpRequestAsyncListener.onHttpRequestAsyncListenerError(errCode, errMsg);
        }
        else
        {
            mHttpRequestAsyncListener.onHttpRequestAsyncListenerComplete(result);    
        }
        
    }
}
