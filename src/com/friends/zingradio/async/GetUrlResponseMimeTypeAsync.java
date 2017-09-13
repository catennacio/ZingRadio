package com.friends.zingradio.async;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.validator.routines.UrlValidator;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.util.Log;

public class GetUrlResponseMimeTypeAsync extends AsyncTask<String, Void, String>
{
    public static final String TAG = GetUrlResponseMimeTypeAsync.class.getSimpleName();
    private String mId;
    
    private GetUrlResponseMimeTypeAsyncComplete mGetUrlResponseMimeTypeAsyncComplete;
    
    public GetUrlResponseMimeTypeAsync(GetUrlResponseMimeTypeAsyncComplete lis)
    {
        mGetUrlResponseMimeTypeAsyncComplete = lis;
    }
    
    @Override
    protected String doInBackground(String... arg0)
    {
        String mimeType = null;
        mId = arg0[0];
        String url = arg0[1];
        UrlValidator urlValidator = new UrlValidator();
        //Log.d(TAG, "url=" + url);
        if(!urlValidator.isValid(url))
        {
            return mimeType;
        }
        else
        {
            try
            {
                HttpClient httpclient = new DefaultHttpClient();
                HttpGet g = new HttpGet();
                g.setURI(new URI(url));
                HttpResponse response = httpclient.execute(g);
                Header contentType = response.getEntity().getContentType();
                if(contentType != null)
                {
                    //Log.d(TAG, "contentType=" + contentType);
                    mimeType = contentType.getValue().split(";")[0].trim();
                }

            }
            catch (URISyntaxException e)
            {
                e.printStackTrace();
            }
            catch (ClientProtocolException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return mimeType;
    }

    @Override
    protected void onPostExecute(String result)
    {
        mGetUrlResponseMimeTypeAsyncComplete.onGetUrlResponseMimeTypeAsyncComplete(mId, result);
    }
}
