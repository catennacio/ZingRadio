package com.friends.zingradio.async;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.friends.zingradio.util.Constants;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

public class GetPerformerCoverAsync extends AsyncTask<String, Void, Bitmap>
{
    public static String TAG1 = GetPerformerCoverAsync.class.getSimpleName();
    private GetPerformerCoverAsyncComplete mOnGetPerformerCoverAsyncCompletedListener;
    private Bitmap mBitmapResult;

    public GetPerformerCoverAsync(GetPerformerCoverAsyncComplete listener)
    {
        mOnGetPerformerCoverAsyncCompletedListener = listener;
    }
    
    @Override
    protected Bitmap doInBackground(String... params)
    {
        String url = params[0];
        return downloadImage(url);
    }
    
    private Bitmap downloadImage(String url)
    {
        Bitmap bm = null;
        //Log.d(TAG, "doInBackground() - Bitmap URL=" + url );
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("Accept", "*/*");

        try
        {
            HttpResponse response = httpclient.execute(httpGet);
            
            HttpEntity entity = response.getEntity();
            BufferedHttpEntity buf = new BufferedHttpEntity(entity);
            
            InputStream is = buf.getContent();
            BufferedInputStream bis = new BufferedInputStream(is);
            bm = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();
        }
        catch (ClientProtocolException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return bm;
    }

    @Override
    protected void onPostExecute(Bitmap bm)
    {
        //Log.d(TAG,"onPostExecute - Bitmap=" + bm);
        mOnGetPerformerCoverAsyncCompletedListener.onGetPerformerCoverAsyncCompleted(bm);
    }
}
