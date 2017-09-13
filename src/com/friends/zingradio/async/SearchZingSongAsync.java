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
import org.json.JSONException;

import com.friends.zingradio.R;
import com.friends.zingradio.entity.AudioItem;
import com.friends.zingradio.entity.json.SearchZingSongResult;
import com.friends.zingradio.entity.json.Song;
import com.friends.zingradio.util.Constants;
import com.friends.zingradio.util.Utilities;
import com.google.gson.Gson;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;

public class SearchZingSongAsync extends AsyncTask<String, Void, ArrayList<AudioItem>> implements GetUrlResponseMimeTypeAsyncComplete
{
    public static final String TAG = SearchZingSongAsync.class.getSimpleName();
    public static final int DEFAULT_RESULT_ROW_COUNT = 10;
    private int errCode = 0;
    private String errMsg;
    private ArrayList<AudioItem> mResultList = null;
    private int mRowCount;
    private int mGetUrlAsyncTaskCount = 0;
    private int mTotalResults;

    private SearchZingSongAsyncComplete mSearchZingSongAsyncComplete;

    public SearchZingSongAsync(SearchZingSongAsyncComplete lis)
    {
        mSearchZingSongAsyncComplete = lis;
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
    }

    @Override
    protected ArrayList<AudioItem> doInBackground(String... arg0)
    {
        String keyword = arg0[0];
        int totalRowsRequested = Integer.parseInt(arg0[1]);
        //Log.d(TAG, "row requested=" + totalRowsRequested);
        String pageNumber = arg0[2];
        try
        {
            String url = "http://api.mp3.zing.vn/api/search?jsondata=" + getDataString(keyword, String.valueOf(totalRowsRequested), pageNumber);
            //Log.d(TAG, "keyword=" + keyword + " totalRowsRequested=" + totalRowsRequested + " pageNumber=" + pageNumber);
            // Log.d(TAG, "url=" + url);

            HttpClient httpclient = new DefaultHttpClient();

            HttpGet g = new HttpGet();
            g.setURI(new URI(url));
            HttpResponse response = httpclient.execute(g);
            StatusLine statusLine = response.getStatusLine();
            // Log.d(TAG, "Response status code=" +
            // response.getStatusLine().getStatusCode());
            if (statusLine.getStatusCode() == HttpStatus.SC_OK)
            {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                String responseString = out.toString();
                //Log.d(TAG, "responseString songSearchResult=" + responseString);
                if (responseString == null || responseString.isEmpty())
                {
                    return mResultList;
                }
                else
                {
                    Gson gson = new Gson();
                    SearchZingSongResult result = gson.fromJson(responseString, SearchZingSongResult.class);
                    if (mResultList == null) mResultList = new ArrayList<AudioItem>();
                    if (result.getResultCount() != null && !result.getResultCount().equals("0"))
                    {
                        mTotalResults = Integer.parseInt(result.getResultCount().toString());
                        if(result.getSongList() != null)
                        {
                            mRowCount = result.getSongList().size();
                            //if(mTotalResults < mRowCount) mTotalResults = mRowCount;
                            if(mRowCount > totalRowsRequested) mRowCount = totalRowsRequested;
                            //Log.d(TAG, "mRowCount=" + mRowCount);
                            //Log.d(TAG, "result.getSongList().size()=" + result.getSongList().size());
                            for (Song s : result.getSongList())
                            {
                                AudioItem a = Utilities.fromSong(s);
                                if(a.getId() != null) mResultList.add(a);
                            }
                            result.free();    
                        }
                    }
                }
            }
            else
            {
                Log.d(TAG, "FAIL - Code=" + response.getStatusLine().getStatusCode() + "\nReason=" + statusLine.getReasonPhrase());
                response.getEntity().getContent().close();
                errCode = statusLine.getStatusCode();
                errMsg = statusLine.getReasonPhrase();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
        catch (Exception e)
        {
            // Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }

        return mResultList;
    }

    @Override
    protected void onPostExecute(ArrayList<AudioItem> list)
    {
        if(list!= null && list.size() > 0)
        {
            for(AudioItem a : list)
            {
                new GetUrlResponseMimeTypeAsync(this).execute(a.getId(), a.getSource());    
            }    
        }
        else
        {
            list = new ArrayList<AudioItem>();
            mTotalResults = 0;
            mSearchZingSongAsyncComplete.onSearchZingSongAsyncComplete(list, mTotalResults);
        }
    }

    @Override
    public void onGetUrlResponseMimeTypeAsyncComplete(String id, String result)
    {
        //Log.d(TAG ,"id=" + id + " result=" + result);
        mGetUrlAsyncTaskCount++;
        //Log.d(TAG ,"mGetUrlAsyncTaskCount=" + mGetUrlAsyncTaskCount + " mRowCount=" + mRowCount);
        if(mGetUrlAsyncTaskCount < mRowCount)
        {
            if (result == null || !result.equals("audio/mpeg"))
            {
                //Log.d(TAG ,"mimtype not ok, look for item to remove");
                AudioItem foundItem = findItem(id);
                if(foundItem != null)
                {
                    //Log.d(TAG ,"found item, remove id=" + id);
                    mResultList.remove(foundItem);
                }
                else
                {
                    //Log.d(TAG ,"NOT found item - something wrong??");
                }
            }
            else
            {
                //Log.d(TAG ,"mimetype OK, do nothing");
            }
        }
        else
        {
            Log.d(TAG ,"Return set");
            if (errCode != 0)
            {
                mSearchZingSongAsyncComplete.onSearchZingSongAsyncError(errCode, errMsg);
            }
            else
            {
                mSearchZingSongAsyncComplete.onSearchZingSongAsyncComplete(mResultList, mTotalResults);
            }
        }
    }

    private AudioItem findItem(String id)
    {
        for (AudioItem a : mResultList)
        {
            if (a.getId() != null && a.getId().equals(id))
            {
                return a;
            }
        }
        return null;
    }
    
    private static String getDataString(String keyword, String rowCount, String pageNumber)
    {
        Map<String, String> map = new HashMap<String, String>();
        map.put("kw", keyword);
        map.put("t", "1");
        map.put("rc", rowCount);
        map.put("p", pageNumber);

        Gson gson = new Gson();
        String json = gson.toJson(map);

        String signature = "";
        String data = "";

        try
        {
            String s1 = new String(Base64.encode(json.getBytes(), Base64.DEFAULT));
            data = URLEncoder.encode(s1, "UTF-8");
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
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // Log.d(TAG, "signature " + signature);

        return data + "&publicKey=" + Constants.ZING_MP3_API_PUBLIC_KEY + "&signature=" + signature;
    }
}
