package com.friends.zingradio.fragment;

import java.util.ArrayList;

import com.friends.zingradio.R;
import com.friends.zingradio.activity.MainActivity;
import com.friends.zingradio.adapter.SongAdapter;
import com.friends.zingradio.async.SearchZingSongAsync;
import com.friends.zingradio.async.SearchZingSongAsyncComplete;
import com.friends.zingradio.entity.AudioItem;
import com.friends.zingradio.util.Utilities;

import android.app.Service;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TestSearchZingSongFragment extends Fragment implements SearchZingSongAsyncComplete
{
    public static final String TAG = TestSearchZingSongFragment.class.getSimpleName();
    private View viewRoot;
    private EditText mEditText;
    private ListView mSongResultListView;
    private SongAdapter mSongAdapter;
    private ArrayList<AudioItem> mSongList;
    private int currentPage = 0;
    private boolean isloading = false;
    private SearchZingSongAsync mLoadZingSongTask;
    private boolean endOfResult = false;
    private LinearLayout mSongResultLayout;

    private Button mSearchButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        viewRoot = inflater.inflate(R.layout.search, container, false);
        //viewRoot = inflater.inflate(R.layout.test_list_view, container, false);
        /*
        mEditText = (EditText) viewRoot.findViewById(R.id.editSearch);
        mEditText.setText("dam vinh hung");
        mSearchButton = (Button)viewRoot.findViewById(R.id.btn_back);
        mSearchButton.setText("Search");
        mSearchButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                loadMoreSongs();
            }
        });
         */
        mSongResultLayout = (LinearLayout)viewRoot.findViewById(R.id.search_fragment_layout_search_song_result);
        mSongResultLayout.setVisibility(View.VISIBLE);
        mSongResultListView = (ListView)viewRoot.findViewById(R.id.lv_songsearchresult);
        mSongList = new ArrayList<AudioItem>();
        /*
        for(int i = 0; i < 3; i++)
        {
            AudioItem ai = new AudioItem();
            ai.setTitle("Title " + i);
            ai.setPerformer("Artist " + i);
            mSongList.add(new AudioItem());
        }
        */
        loadMoreSongs();
        mSongAdapter = new SongAdapter(this.getActivity(), mSongList);
        mSongResultListView.setAdapter(mSongAdapter);

        return viewRoot;
    }
    
    private void loadMoreSongs()
    {
        Log.d(TAG ,"loadMoreSongs()");
        isloading = true;
        mLoadZingSongTask = new SearchZingSongAsync(this);
        //mLoadZingSongTask.execute(mEditText.getText().toString(), String.valueOf(SearchZingSongAsync.DEFAULT_RESULT_ROW_COUNT), String.valueOf(currentPage));
        mLoadZingSongTask.execute("dam vinh hung", String.valueOf(SearchZingSongAsync.DEFAULT_RESULT_ROW_COUNT), String.valueOf(currentPage));
        currentPage ++;
    }

    @Override
    public void onSearchZingSongAsyncComplete(ArrayList<AudioItem> al, int totalResults)
    {
        Log.d(TAG ,"onSearchZingSongAsyncComplete() - count=" + al.size());
        endOfResult = (al.size() == 0)?true:false;
        mSongList.addAll(al);
        //mSongAdapter.addAll(al);
        Log.d(TAG ,"song list count=" + mSongAdapter.getSongs().size());
        mSongAdapter.notifyDataSetChanged();
        isloading = false;
    }

    @Override
    public void onSearchZingSongAsyncError(int errCode, String errMsg)
    {
        Log.d(TAG ,"onSearchZingSongAsyncError()");
    }
}
