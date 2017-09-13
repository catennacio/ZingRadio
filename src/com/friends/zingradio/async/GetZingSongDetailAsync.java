package com.friends.zingradio.async;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.friends.zingradio.entity.json.Song;
import com.friends.zingradio.util.Constants;
import com.friends.zingradio.util.Utilities;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

public class GetZingSongDetailAsync extends AsyncTask<String, Integer, Song>
{
    public static final String TAG = GetZingSongDetailAsync.class.getSimpleName();
    
    private GetZingSongDetailAsyncComplete mListener;
    private int errCode = 0;
    private String errMsg;
    
    public GetZingSongDetailAsync(GetZingSongDetailAsyncComplete listener)
    {
        mListener = listener;
    }

    @Override
    protected Song doInBackground(String... arg0)
    {
        Song song = new Song();
        try
        {
            String id = arg0[0];
            Log.d(TAG, "Get song detail id=" + id);
            if(id == null || id.equals(""))
            {
                errCode = -5;
            }
            else
            {
                String url = "http://api.mp3.zing.vn/api/detail?jsondata=" + getZingSongDetail(id);
                //Log.d(TAG, "url=" + url);
                
                HttpClient httpclient = new DefaultHttpClient();

                HttpGet g = new HttpGet();
                g.setURI(new URI(url));
                HttpResponse response = httpclient.execute(g);
                StatusLine statusLine = response.getStatusLine();
                //Log.d(TAG, "Response status code=" + response.getStatusLine().getStatusCode());
                if (statusLine.getStatusCode() == HttpStatus.SC_OK)
                {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    String responseString = out.toString();
                    //Log.d(TAG, "Response=" + responseString);
                    
                    Gson gson = new Gson();
                    song = gson.fromJson(responseString, Song.class);
                }
                else
                {
                    Log.e(TAG, "FAIL - Code=" + statusLine.getStatusCode() + "\nReason=" + statusLine.getReasonPhrase());
                    errCode = statusLine.getStatusCode();
                    errMsg = statusLine.getReasonPhrase();
                    response.getEntity().getContent().close();
                }    
            }
        }
        catch (JsonSyntaxException e)
        {
            errCode = -1;
            errMsg = e.getMessage();
            e.printStackTrace();
            Log.e(TAG, "FAIL - Code=" + errCode + "\nReason=" + errMsg);
        }
        catch (UnknownHostException e)
        {
            errCode = -2;
            errMsg = e.getMessage();
            e.printStackTrace();
            Log.e(TAG, "FAIL - Code=" + errCode + "\nReason=" + errMsg);
        }
        catch (IOException e)
        {
            errCode = -3;
            errMsg = e.getMessage();
            e.printStackTrace();
            Log.e(TAG, "FAIL - Code=" + errCode + "\nReason=" + errMsg);
        }
        catch (Exception e)
        {
            errCode = -4;
            errMsg = e.getMessage();
            e.printStackTrace();
            Log.e(TAG, "FAIL - Code=" + errCode + "\nReason=" + errMsg);
        }

        return song;
    }

    @Override
    protected void onPostExecute(Song song)
    {
        if(errCode == 0)
        {
            mListener.onGetZingSongDetailAsyncComplete(song);
        }
        else
        {
            mListener.onGetZingSongDetailAsyncError(errCode, errMsg);
        }
    }

    public static String getZingSongDetail(String id)
    {
        Map<String, String> map = new HashMap<String, String>();
        map.put("t", "song");
        map.put("id", id);

        Gson gson = new Gson();
        String json = gson.toJson(map);

        String signature = "";
        String data = "";
        
        try
        {
            //Log.d(TAG, "Base64 " + new String(Base64.encode(json.getBytes(), Base64.URL_SAFE)));
            
            String s1 = new String( Base64.encode(json.getBytes(), Base64.DEFAULT));
            data = URLEncoder.encode(s1, "UTF-8");
            //String data = URLEncoder.encode(new String(Base64.encode(json.getBytes(), Base64.DEFAULT))).replace("%0A", "");
            //Log.d(TAG, "urlencode " + data);
            
            signature = Utilities.computeSignature(data, Constants.ZING_MP3_API_PRIVATE_KEY);
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        catch (GeneralSecurityException e)
        {
            e.printStackTrace();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        //Log.d(TAG, "signature " + signature);

        return data + "&publicKey=" + Constants.ZING_MP3_API_PUBLIC_KEY + "&signature=" + signature;
    }
}
