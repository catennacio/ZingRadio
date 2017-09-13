package com.friends.zingradio.ui;

import java.util.ArrayList;

import com.friends.zingradio.R;
import com.friends.zingradio.adapter.SongAdapter;
import com.friends.zingradio.async.SearchZingSongAsync;
import com.friends.zingradio.async.SearchZingSongAsyncComplete;
import com.friends.zingradio.entity.AudioItem;
import com.friends.zingradio.entity.json.SearchZingSongResult;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;

public class LoadMoreSongResultEndlessScrollListener implements OnScrollListener,
                                                                SearchZingSongAsyncComplete
{
    public static final String TAG = LoadMoreSongResultEndlessScrollListener.class.getSimpleName();
    
    private int visibleThreshold = 3;
    private int currentPage = 0;
    private int previousTotal = 0;
    private boolean loading = false;
    private ListView mListView;
    private Context mContext;
    //private ProgressDialog mProgressDialog;
    private String mKeyword;
    private int mRowCount;
    
    private SearchZingSongAsync mLoadZingSongTask;

    public LoadMoreSongResultEndlessScrollListener(Context ctx, ListView listview, String keyword, int rowCount)
    {
        mContext = ctx;
        mListView = listview;
        mKeyword = keyword;
        mRowCount = rowCount;
    }
    
    public void setKeyWord(String kw)
    {
        mKeyword = kw;
    }

    public LoadMoreSongResultEndlessScrollListener(Context ctx, ListView listview, String keyword, int rowCount, int visibleThreshold)
    {
        mContext = ctx;
        mListView = listview;
        this.visibleThreshold = visibleThreshold;
        mKeyword = keyword;
        mRowCount = rowCount;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
    {
        int loadedItems = firstVisibleItem + visibleItemCount;
        if ((loadedItems == totalItemCount) && !loading)
        {
            loading = true;
            mLoadZingSongTask = new SearchZingSongAsync(this);
            mLoadZingSongTask.execute(mKeyword, String.valueOf(SearchZingSongAsync.DEFAULT_RESULT_ROW_COUNT), String.valueOf(currentPage));
            currentPage ++;
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState){}

    @Override
    public void onSearchZingSongAsyncComplete(ArrayList<AudioItem> list, int totalResults)
    {
        Log.d(TAG, "onSearchZingSongAsyncComplete() page=" + currentPage);
        //if(mProgressDialog != null) mProgressDialog.dismiss();
        SongAdapter adapter = (SongAdapter)mListView.getAdapter();
        ArrayList<AudioItem> currentList = adapter.getSongs();
        currentList.addAll(list);
        adapter.notifyDataSetChanged();
        loading = false;
    }

    @Override
    public void onSearchZingSongAsyncError(int errCode, String errMsg)
    {
        //if(mProgressDialog != null) mProgressDialog.dismiss();
        Toast.makeText(mContext, "errCode=" + errCode + " errMsg=" + errMsg, Toast.LENGTH_SHORT).show();
        loading = false;
    }
}
