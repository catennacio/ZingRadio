package com.friends.zingradio.async;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.friends.zingradio.entity.json.ArtistInfo;
import com.friends.zingradio.util.Utilities;
import com.google.gson.Gson;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

public class GetZingArtistDetailAsync extends AsyncTask<String, Void, ArtistInfo>
{
    public static final String TAG = GetZingArtistDetailAsync.class.getSimpleName();
    public static final String ZING_MP3_API_PUBLIC_KEY = "5f40b0affbd5cfcd663b77dab510e1e0b2db6e7f";
    public static final String ZING_MP3_API_PRIVATE_KEY = "687de8dadf9e4a56d29795df00584c80";

    private GetZingArtistDetailAsyncComplete mGetZingArtistDetailListener;
    private int errCode = 0;
    private String errMsg;
    
    public GetZingArtistDetailAsync(GetZingArtistDetailAsyncComplete lis)
    {
        mGetZingArtistDetailListener = lis;
    }

    @Override
    protected ArtistInfo doInBackground(String... arg0)
    {
        ArtistInfo artInf = null;
        try
        {
            String id = arg0[0];    
            //Log.d(TAG, "id=" + id );
            String url = "http://api.mp3.zing.vn/api/singer-info?jsondata=" + getZingArtistDetail(id);
            //Log.d(TAG, "url=" + url);

            HttpClient httpclient = new DefaultHttpClient();
            HttpGet g = new HttpGet();
            g.setURI(new URI(url));
            //Log.d(TAG, "1");
            HttpResponse response = httpclient.execute(g);
            //Log.d(TAG, "2");
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK)
            {
                //Log.d(TAG, "3");
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                String resultString = out.toString();
                //Log.d(TAG, "Response=" + resultString);
                
                Gson gson = new Gson();
                artInf = gson.fromJson(resultString, ArtistInfo.class);
            }
            else
            {
                //Log.e(TAG, "FAIL - Reason=" + statusLine.getReasonPhrase());
                response.getEntity().getContent().close();
                errCode = statusLine.getStatusCode();
                errMsg = statusLine.getReasonPhrase();
            }            
        }
        catch (UnsupportedEncodingException e1)
        {
            e1.printStackTrace();
        }
        catch (ClientProtocolException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
        return artInf;
    }
    
    @Override
    protected void onPostExecute(ArtistInfo result)
    {
        if(errCode != 0)
        {
            mGetZingArtistDetailListener.onGetZingArtistDetailError(errCode, errMsg);;
        }
        else
        {
            mGetZingArtistDetailListener.onGetZingArtistDetailComplete(result);    
        }
    }
    
    public static String getZingArtistDetail(String id)
    {
        Map<String, String> map = new HashMap<String, String>();
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
            
            signature = Utilities.computeSignature(data, ZING_MP3_API_PRIVATE_KEY);
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

        return data + "&publicKey=" + ZING_MP3_API_PUBLIC_KEY + "&signature=" + signature;
    }
}
