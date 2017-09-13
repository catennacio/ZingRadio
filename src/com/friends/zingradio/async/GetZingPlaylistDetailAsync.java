package com.friends.zingradio.async;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.friends.zingradio.entity.AudioItem;
import com.friends.zingradio.entity.Playlist;
import com.friends.zingradio.entity.json.Song;
import com.friends.zingradio.entity.json.ZingPlaylistSearchResult;
import com.friends.zingradio.util.Constants;
import com.friends.zingradio.util.Utilities;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

public class GetZingPlaylistDetailAsync extends AsyncTask<String, Void, Playlist>
{
    public static final String TAG = GetZingPlaylistDetailAsync.class.getSimpleName();
    
    private GetZingPlaylistDetailAsyncComplete mListener;
    private int errCode;
    private String errMsg;

    public GetZingPlaylistDetailAsync(GetZingPlaylistDetailAsyncComplete lis)
    {
        mListener = lis;
    }
    
    @Override
    protected Playlist doInBackground(String... arg0)
    {
        Playlist pl = new Playlist();
        try
        {
            String id = arg0[0];
            String url = "http://api.mp3.zing.vn/api/detail?jsondata=" + getZingPlaylistDetail(id);
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
                if(responseString == null || responseString.isEmpty())
                {
                    return pl;
                }
                else
                {
                    Gson gson = new Gson();
                    ZingPlaylistSearchResult r = gson.fromJson(responseString, ZingPlaylistSearchResult.class);
                    if(r != null)
                    {
                        ArrayList<Song> songList = r.getSongList();
                        //Log.d(TAG, "songList count=" + songList.size());
                        for(Song s : songList)
                        {
                            AudioItem ai = Utilities.fromSong(s);
                            pl.getAudioItems().add(ai);
                        }
                        
                        pl.setArtistName(r.getInfo().getArtist());
                        pl.setCreatedDate(r.getInfo().getCreatedDate());
                        pl.setHit(Integer.parseInt(r.getInfo().getHit()));
                        pl.setId(r.getInfo().getID());
                        pl.setOfficial(r.getInfo().getOfficial());
                        pl.setOwnerAcc(r.getInfo().getOwnerAcc());
                        pl.setPictureURL(r.getInfo().getPictureURL());
                        pl.setTitle(r.getInfo().getTitle());
                        pl.setTotalListen(Integer.parseInt(r.getInfo().getTotalListen()));
                    }
                }
            }
            else
            {
                Log.e(TAG, "FAIL - Code=" + statusLine.getStatusCode() + "\nReason=" + statusLine.getReasonPhrase());
                errCode = statusLine.getStatusCode();
                errMsg = statusLine.getReasonPhrase();
                response.getEntity().getContent().close();
            }
        }
        catch (JsonSyntaxException e)
        {
            errCode = -1;
            errMsg = e.getMessage();
            e.printStackTrace();
        }
        catch (IOException e)
        {
            errCode = -2;
            errMsg = e.getMessage();
            e.printStackTrace();
        }
        catch (Exception e)
        {
            errCode = -3;
            errMsg = e.getMessage();
            e.printStackTrace();
        }
        
        return pl;
    }

    @Override
    protected void onPostExecute(Playlist pl)
    {
        if(errCode == 0)
        {
            mListener.OnGetZingPlaylistDetailAsyncComplete(pl);
        }
        else
        {
            mListener.OnGetZingPlaylistDetailAsyncError(errCode, errMsg);
        }
    }
    
    public static String getZingPlaylistDetail(String id)
    {
        Map<String, String> map = new HashMap<String, String>();
        map.put("t", "playlist");
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
