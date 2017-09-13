package com.friends.zingradio.fragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import com.friends.zingradio.R;
import com.friends.zingradio.activity.MainActivity;
import com.friends.zingradio.activity.SettingsActivity;
import com.friends.zingradio.activity.SplashScreenActivity;
import com.friends.zingradio.adapter.SongAdapter;
import com.friends.zingradio.adapter.StationAdapter;
import com.friends.zingradio.async.GetAlbumServerIdAsync;
import com.friends.zingradio.async.GetAlbumServerIdAsyncComplete;
import com.friends.zingradio.async.GetStationServerIdAsync;
import com.friends.zingradio.async.GetStationServerIdAsyncComplete;
import com.friends.zingradio.async.GetUrlResponseMimeTypeAsync;
import com.friends.zingradio.async.GetUrlResponseMimeTypeAsyncComplete;
import com.friends.zingradio.async.GetZingPlaylistDetailAsync;
import com.friends.zingradio.async.GetZingPlaylistDetailAsyncComplete;
import com.friends.zingradio.async.GetZingSongDetailAsync;
import com.friends.zingradio.async.GetZingSongDetailAsyncComplete;
import com.friends.zingradio.async.PerformSearchAsync;
import com.friends.zingradio.async.PerformSearchAsyncComplete;
import com.friends.zingradio.async.SearchZingSongAsync;
import com.friends.zingradio.async.SearchZingSongAsyncComplete;
import com.friends.zingradio.command.StartMainActivityCommand;
import com.friends.zingradio.entity.AudioItem;
import com.friends.zingradio.entity.Station;
import com.friends.zingradio.entity.json.AlbumResult;
import com.friends.zingradio.entity.json.AlbumSearchResult;
import com.friends.zingradio.entity.json.ArtistSearchResult;
import com.friends.zingradio.entity.json.SearchResult;
import com.friends.zingradio.entity.json.Song;
import com.friends.zingradio.entity.json.SongSearchResult;
import com.friends.zingradio.ui.LoadMoreSongResultEndlessScrollListener;
import com.friends.zingradio.util.Constants;
import com.friends.zingradio.util.GAUtils;
import com.friends.zingradio.util.Utilities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;

import android.app.Service;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class SearchFragment extends Fragment implements PerformSearchAsyncComplete,
                                                        GetStationServerIdAsyncComplete,
                                                        GetZingSongDetailAsyncComplete,
                                                        SearchZingSongAsyncComplete,
                                                        GetUrlResponseMimeTypeAsyncComplete,
                                                        GetAlbumServerIdAsyncComplete
                                                        //OnScrollListener
{
    public static final String TAG = SearchFragment.class.getSimpleName();
    public static final Uri URI = new Uri.Builder().scheme(TAG).authority("").build();
    private static final int TRIGGER_SEARCH = 1;
    private static final long SEARCH_TRIGGER_DELAY_IN_MS = 700;
    
    private View viewRoot;
    private EditText mEditText;
    private ImageButton mBackButton;
    private ListView mArtistResultListView;
    private ListView mAlbumResultListView;
    private ListView mSongResultListView;
    private TextView mEmptyTextView;
    private LinearLayout mArtistResultLayout;
    private LinearLayout mSongResultLayout;
    private LinearLayout mAlbumResultLayout;
    
    private ArrayList<Station> mStationList;// = new ArrayList<Station>();
    private ArrayList<Station> mAlbumList;// = new ArrayList<Station>();
    private ArrayList<AudioItem> mSongList;// = new ArrayList<AudioItem>();
    
    private Station mSelectedStation;
    private Station mSelectedAlbum;
    private AudioItem mSelectedSong;
    //private ProgressDialog mProgressDialog;
    private ProgressBar mProgressBar;
    private ProgressBar mSonglistProgressBar;

    private SongAdapter mSongAdapter;
    private StationAdapter mStationAdapter;
    private StationAdapter mAlbumAdapter;
    private boolean mInstantSearchEnable;
    private LoadMoreSongResultEndlessScrollListener mLoadMore;
    
    private PerformSearchAsync mPerformSearchAsync;
    private GetUrlResponseMimeTypeAsync mGetUrlResponseMimeTypeAsync;
    private GetStationServerIdAsync mGetStationServerIdAsync;
    private GetZingSongDetailAsync mGetZingSongDetailAsync;
    private GetAlbumServerIdAsync mGetAlbumServerIdAsync;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        GAUtils.writeViewFragement(this.getActivity(), TAG);
        viewRoot = inflater.inflate(R.layout.search, container, false);
        mEditText = (EditText)viewRoot.findViewById(R.id.editSearch);
        
        //mEditText.setTextColor(Color.WHITE);
        //mEditText.setBackgroundColor(getResources().getColor(R.color.actions_bg));
        //mEditText.setText("dam vinh hung");
        Button clearText = (Button)viewRoot.findViewById(R.id.btn_delete_text);
        clearText.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                mEditText.setText("");
                InputMethodManager imm = (InputMethodManager)SearchFragment.this.getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mEditText, InputMethodManager.SHOW_FORCED);
            }
        });

        mBackButton = (ImageButton)viewRoot.findViewById(R.id.btn_back);
        mBackButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                MainActivity ma = (MainActivity)SearchFragment.this.getActivity();
                ma.updateLeftFrame(LeftMenuFragment.URI, null);
            }
        });
        
        mProgressBar = (ProgressBar)viewRoot.findViewById(R.id.search_fragment_progress_bar);
        showProgressBar(false);
        
        mSonglistProgressBar = (ProgressBar)viewRoot.findViewById(R.id.song_list_progressbar);
        
        mEmptyTextView = (TextView)viewRoot.findViewById(R.id.tv_empty);
        mEmptyTextView.setText(R.string.msg_search_fragment_empty);
        
        mArtistResultLayout = (LinearLayout)viewRoot.findViewById(R.id.search_fragment_layout_search_artist_result);
        mStationList = new ArrayList<Station>();
        mArtistResultListView = (ListView)viewRoot.findViewById(R.id.lv_artistsearchresult);
        mStationAdapter = new StationAdapter(this.getActivity(), mStationList);
        mArtistResultListView.setAdapter(mStationAdapter);
        mArtistResultListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View view,int pos, long id)
            {
                mSelectedStation = mStationList.get(pos);
                mSelectedStation.setType(Station.Type.Radio);
                //Log.d(TAG, mSelectedStation.getName() + " id=" + mSelectedStation.getId() + " selected.");
                performGetStationServerId(mSelectedStation.getId());
            }
        });
        
        mAlbumResultLayout = (LinearLayout)viewRoot.findViewById(R.id.search_fragment_layout_search_album_result);
        mAlbumList = new ArrayList<Station>();
        mAlbumResultListView = (ListView)viewRoot.findViewById(R.id.lv_albumsearchresult);
        //Log.d(TAG, "mAlbumResultListView=" + mAlbumResultListView);
        mAlbumAdapter = new StationAdapter(this.getActivity(), mAlbumList);
        mAlbumResultListView.setAdapter(mAlbumAdapter);
        mAlbumResultListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View view,int pos, long id)
            {
                mSelectedAlbum = mAlbumList.get(pos);
                mSelectedAlbum.setType(Station.Type.Album);
                //Log.d(TAG, mSelectedAlbum.getName() + " id=" + mSelectedAlbum.getId() + " selected.");
                performGetAlbumServerId(mSelectedAlbum.getId(), mSelectedAlbum.getName());
            }
        });

        mSongResultLayout = (LinearLayout)viewRoot.findViewById(R.id.search_fragment_layout_search_song_result);
        mSongList = new ArrayList<AudioItem>();
        mSongResultListView = (ListView)viewRoot.findViewById(R.id.lv_songsearchresult);
        mSongAdapter = new SongAdapter(this.getActivity(), mSongList);
        mSongResultListView.setAdapter(mSongAdapter);
        //mSongResultListView.setOnScrollListener(this);
        mSongResultListView.setOnScrollListener(new OnScrollListener()
        {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState)
            {
            }
            
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
            {
                //Log.d(TAG, "1 - firstVisibleItem=" + firstVisibleItem + " visibleItemCount=" + visibleItemCount + " totalItemCount" + totalItemCount);
                
                int loadedItems = firstVisibleItem + visibleItemCount;
                if ((loadedItems == totalItemCount) && !isloading)
                {
                    if (mLoadZingSongTask != null && (mLoadZingSongTask.getStatus() == SearchZingSongAsync.Status.FINISHED) && !endOfResult)
                    {
                        loadMoreSongs();
                    }
                }
            }
        });
        
        //mLoadMore = new LoadMoreSongResultEndlessScrollListener(this.getActivity(), mSongResultListView, mEditText.getText().toString(), 10);
        //mSongResultListView.setOnScrollListener(mLoadMore);

        mSongResultListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View view,int pos, long id)
            {
                mSelectedSong = mSongList.get(pos);
                //Log.d(TAG, mSelectedSong.getTitle() + "Selected song id=" + mSelectedSong.getId());
                perFormGetZingSongDetail(mSelectedSong.getId());
            }
        });
        
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(SearchFragment.this.getActivity());
        mInstantSearchEnable = sharedPref.getBoolean(SettingsActivity.SETTINGS_INSTANT_SEARCH, false);

        mEditText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count){}
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}

            @Override
            public void afterTextChanged(final Editable s)
            {
                if(mInstantSearchEnable)
                {
                    if(mEditText.length() > 0)
                    {
                        handler.removeMessages(TRIGGER_SEARCH);
                        handler.sendEmptyMessageDelayed(TRIGGER_SEARCH, SEARCH_TRIGGER_DELAY_IN_MS);    
                    }
                    else
                    {
                        handler.removeMessages(TRIGGER_SEARCH);
                    }
                }
            }
        });

        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_SEARCH)
                {
                    MainActivity ma = (MainActivity)SearchFragment.this.getActivity();
                    if(!Utilities.isNetworkOnline(ma))
                    {
                        Toast.makeText(ma, ma.getString(R.string.msg_search_fragment_no_connection), Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        if(mEditText.length() > 0)
                        {
                            InputMethodManager imm = (InputMethodManager)SearchFragment.this.getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(mEditText.getApplicationWindowToken(), 0);

                            //mSongAdapter.setSongs(mSongList);
                            //mSongAdapter.notifyDataSetChanged();
                            mSongResultLayout.setVisibility(View.GONE);
                            mArtistResultLayout.setVisibility(View.GONE);
                            mAlbumResultLayout.setVisibility(View.GONE);
                            mEmptyTextView.setVisibility(View.GONE);
                            performSearch(mEditText.getText().toString());
                        }
                    }
                    return true;
                }
                return false;
            }
        });

        mEditText.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                InputMethodManager imm = (InputMethodManager)SearchFragment.this.getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
                if (hasFocus)
                {
                    imm.showSoftInput(mEditText, InputMethodManager.SHOW_FORCED);
                }
                else
                {
                    imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
                }
            }
        });

        return viewRoot;
    }
    
    
    @Override
    public void onDestroy()
    {
        if(mStationList != null)
        {
            for(Station s : mStationList)
            {
                s.freeAudioItems();
            }
            mStationList.clear();
        }
        mStationList = null;
        
        if(mAlbumList != null)
        {
            for(Station s : mAlbumList)
            {
                s.freeAudioItems();
            }
            mAlbumList.clear();
        }
        mAlbumList = null;
        
        if(mSongList != null)
        {
            for(AudioItem ai : mSongList)
            {
                ai = null;
            }
            mSongList.clear();
        }
        mSongList = null;
        
        mSongAdapter = null;
        mStationAdapter = null;
        mAlbumAdapter = null;
        
        cancelAsyncTasks();
        showProgressBar(false);
        mSonglistProgressBar.setVisibility(View.GONE);
        
        super.onDestroy();
    }
    
    private void cancelAsyncTasks()
    {
        if(mPerformSearchAsync != null)
        {
            if(mPerformSearchAsync.getStatus() != AsyncTask.Status.FINISHED)
            {
                mPerformSearchAsync.cancel(true);
            }
            mPerformSearchAsync = null;
        }

        if(mGetUrlResponseMimeTypeAsync != null)
        {
            mGetUrlResponseMimeTypeAsync.cancel(true);
            mGetUrlResponseMimeTypeAsync = null;
        }
        if(mGetStationServerIdAsync != null)
        {
            mGetStationServerIdAsync.cancel(true);
            mGetStationServerIdAsync = null;
        }
        if(mGetZingSongDetailAsync != null)
        {
            mGetZingSongDetailAsync.cancel(true);
            mGetZingSongDetailAsync = null;
        }
        if(mGetAlbumServerIdAsync != null)
        {
            mGetAlbumServerIdAsync.cancel(true);
            mGetAlbumServerIdAsync = null;
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            if (msg.what == TRIGGER_SEARCH)
            {
                if(Utilities.isNetworkOnline(SearchFragment.this.getActivity()))
                {
                    SearchFragment.this.performSearch(SearchFragment.this.mEditText.getText().toString());
                }
            }
        }
    };

    @Override
    public void onHiddenChanged(boolean hidden)
    {
        super.onHiddenChanged(hidden);
        if(!hidden)
        {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(SearchFragment.this.getActivity());
            mInstantSearchEnable = sharedPref.getBoolean(SettingsActivity.SETTINGS_INSTANT_SEARCH, false);    
        }
    }
    
    private void performSearch(String query)
    {
        currentPage = 1;
        endOfResult = false;
        
        mSongList.clear();
        mStationList.clear();
        mAlbumList.clear();
        
        mSongAdapter.notifyDataSetChanged();
        mStationAdapter.notifyDataSetChanged();
        mAlbumAdapter.notifyDataSetChanged();

        //mProgressDialog = ProgressDialog.show(this.getActivity(), "", getString(R.string.msg_search_fragment_progress_bar_searching), true);
        //Log.d(TAG, "1");
        //new PerformSearchAsync(this).execute(query);
        
        showProgressBar(true);
        mSonglistProgressBar.setVisibility(View.GONE);
        
        if(mPerformSearchAsync == null)
        {
            //Log.d(TAG, "2");
            mPerformSearchAsync = new PerformSearchAsync(this);
            mPerformSearchAsync.execute(query);
        }
        else if(mPerformSearchAsync.getStatus() != PerformSearchAsync.Status.FINISHED)
        {
            //Log.d(TAG, "3");
            mPerformSearchAsync.cancel(true);
            mPerformSearchAsync = null;
            mPerformSearchAsync = new PerformSearchAsync(this);
            mPerformSearchAsync.execute(query);
        }
        else
        {
            //Log.d(TAG, "4");
            mPerformSearchAsync = new PerformSearchAsync(this);
            mPerformSearchAsync.execute(query);
        }
    }

    private int mArtistCount;
    
    @Override
    public void onPerformSearchComplete(SearchResult result)
    {
        //Log.d(TAG, "onPerformSearchComplete() - artist=" + result.getArtistResult().getArtistList().size() + " songs=" + result.getSongResult().getSongList().size());
        mArtistCount = result.getArtistResult().getArtistList().size();
        //mStationList.clear();

        for(ArtistSearchResult a : result.getArtistResult().getArtistList())
        {
            //Log.d(TAG, "name=" + a.getName() + " id="+ a.getId() + " avaurl=" + a.getAvatarUrl());
            //if(a.isValid())
            {
                Station st = new Station();
                st.setCategoryId(Constants.CAT_NGHESI);
                st.setCategoryName("Nghe si");
                st.setId(a.getId());
                st.setName(a.getName());
                st.setServerId("");
                mStationList.add(st);    
            }
        }
        mStationAdapter.notifyDataSetChanged();
        
        if(mStationList.size() == 0)
        {
            mArtistResultLayout.setVisibility(View.GONE);
        }
        else
        {
            mArtistResultLayout.setVisibility(View.VISIBLE);
        }
        
        //mAlbumList.clear();
        //Log.d(TAG,"Album result count= " + result.getAlbumResult().getAlbumList().size());
        for(AlbumSearchResult ar : result.getAlbumResult().getAlbumList())
        {
            Station st = new Station();
            st.setCategoryId(Constants.CAT_NGHESI);
            st.setCategoryName(result.getAlbumResult().getTitle());
            st.setId(ar.getObject_id());
            st.setName(ar.getName());
            st.setServerId("");
            mAlbumList.add(st);
        }
        if(mAlbumList.size() == 0)
        {
            mAlbumResultLayout.setVisibility(View.GONE);
        }
        else
        {
            mAlbumAdapter.notifyDataSetChanged();
            mAlbumResultLayout.setVisibility(View.VISIBLE);
        }

        //mSongList.clear();
        for(SongSearchResult s : result.getSongResult().getSongList())
        {
            //Log.d(TAG, "name=" + s.getName() + " id="+ s.getId());
            AudioItem ai = new AudioItem();
            ai.setId(s.getId());
            ai.setTitle(s.getName());
            ai.setPerformer(s.getArtistName());
            mSongList.add(ai);
        }

        result.free();
        mSongAdapter.notifyDataSetChanged();
        //mSongResultLayout.setVisibility(View.VISIBLE);
        showProgressBar(false);
        loadMoreSongs();
    }
    
    @Override
    public void onPerformSearchError(int errCode, String errMsg)
    {
        Log.e(TAG, "onPerformSearchError() - errCode=" + errCode + " errMsg=" + errMsg);
        //if(mProgressDialog != null) mProgressDialog.dismiss();
        showProgressBar(false);
        if(this.isAdded()) Toast.makeText(this.getActivity(), R.string.msg_err_perform_search, Toast.LENGTH_SHORT).show();    
    }
    
    private void performGetStationServerId(String stationId)
    {
        //mProgressDialog = ProgressDialog.show(SearchFragment.this.getActivity(), "", getString(R.string.msg_search_fragment_progress_bar_get_station_id), true);
        showProgressBar(true);
        if(mGetStationServerIdAsync != null)
        {
            mGetStationServerIdAsync.cancel(true);
        }
        mGetStationServerIdAsync = new GetStationServerIdAsync(this);
        mGetStationServerIdAsync.execute(stationId);
        //new GetStationServerIdAsync(this).execute(stationId);
    }

    @Override
    public void onGetStationServerIdComplete(String serverId)
    {
        //Log.d(TAG, "Station server Id=" + serverId);
        //if(mProgressDialog != null) mProgressDialog.dismiss();
        if(this.isAdded() && mSelectedStation != null)
        {
            showProgressBar(false);
            mSelectedStation.setServerId(serverId);
            MainActivity ma = (MainActivity) this.getActivity();
            PlayerFragment f = (PlayerFragment) ma.updateContent(PlayerFragment.URI);
            f.setRepeatSong(false);
            f.playStation(mSelectedStation.clone());    
        }
        //Toast.makeText(this.getActivity(), "serverId=" + serverId, Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onGetStationServerIdError(int errCode, String errMsg)
    {
        Log.e(TAG, "onGetStationServerIdError() - errCode=" + errCode + " errMsg=" + errMsg);
        if(this.isAdded()) Toast.makeText(this.getActivity(), getString(R.string.msg_err_station_not_available), Toast.LENGTH_SHORT).show();
        //if(mProgressDialog != null) mProgressDialog.dismiss();
        showProgressBar(false);
    }

    private void perFormGetZingSongDetail(String id)
    {
        //mProgressDialog = ProgressDialog.show(SearchFragment.this.getActivity(), "", getString(R.string.msg_search_fragment_progress_bar_get_zing_song_detail), true);
        showProgressBar(true);
        if(mGetZingSongDetailAsync != null)
        {
            mGetZingSongDetailAsync.cancel(true);
        }
        mGetZingSongDetailAsync = new GetZingSongDetailAsync(this);
        mGetZingSongDetailAsync.execute(id);
        //new GetZingSongDetailAsync(this).execute(id);
    }

    @Override
    public void onGetZingSongDetailAsyncComplete(Song song)
    {
        //Log.d(TAG, "onGetZingSongDetailAsyncComplete() - song title=" + song.getTitle());
        //Log.d(TAG, "linkplay128=" + song.getLinkPlay128());
        //Log.d(TAG, "linkplay24=" + song.getLinkPlay24());
        //Log.d(TAG, "linkplay320=" + song.getLinkPlay320());
        //if(mProgressDialog != null) mProgressDialog.dismiss();
        showProgressBar(false);
        
        //Toast.makeText(this.getActivity(), "serverId=" + serverId, Toast.LENGTH_SHORT).show();
        mSelectedSong.setTitle(song.getTitle());
        mSelectedSong.setSource(song.getLinkPlay128());
        mSelectedSong.setDownloadLink(song.getLinkDownload128());
        mSelectedSong.setThumbnail(song.getArtistAvatar());

        if(!mSelectedSong.isStreaming() || Utilities.fileExistance(mSelectedSong.getSource()))
        {
            Toast.makeText(this.getActivity(), getString(R.string.msg_err_song_bad_link), Toast.LENGTH_SHORT).show();            
        }
        else
        {
            //check mimetype of url
            if(mGetUrlResponseMimeTypeAsync != null)
            {
                mGetUrlResponseMimeTypeAsync.cancel(true);
            }
            mGetUrlResponseMimeTypeAsync = new GetUrlResponseMimeTypeAsync(this);
            mGetUrlResponseMimeTypeAsync.execute(mSelectedSong.getId(), mSelectedSong.getSource());
        }
    }

    
    @Override
    public void onResume()
    {
        super.onResume();
        this.mEditText.requestFocus();
        //InputMethodManager imm = (InputMethodManager)SearchFragment.this.getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
        //imm.showSoftInput(mEditText, InputMethodManager.SHOW_FORCED);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        //InputMethodManager imm = (InputMethodManager)SearchFragment.this.getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
        //imm.hideSoftInputFromWindow(mEditText.getApplicationWindowToken(), 0);
    }
    
    @Override
    public void onStop()
    {
        if(mLoadZingSongTask != null)
        {
            mLoadZingSongTask.cancel(true);
        }
        super.onStop();
    }


    @Override
    public void onSearchZingSongAsyncError(int errCode, String errMsg)
    {
        //if(mProgressDialog != null) mProgressDialog.dismiss();
        showProgressBar(false);
        Log.e(TAG, "onSearchZingSongAsyncError() - errCode=" + errCode + " errMsg=" + errMsg);
        if(this.isAdded()) Toast.makeText(this.getActivity(), getString(R.string.msg_err_song_dropped), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onGetUrlResponseMimeTypeAsyncComplete(String id, String result)
    {
        if(!this.isAdded()) return;
        if(result == null && this.isAdded())
        {
            Toast.makeText(this.getActivity(), getString(R.string.msg_err_song_deleted_on_server), Toast.LENGTH_SHORT).show();
        }
        else if(result.equals("audio/mpeg"))
        {
            if(mSelectedStation != null) mSelectedStation.freeAudioItems();
            mSelectedStation = new Station();
            mSelectedStation.setId(Station.SEARCH_ID);
            mSelectedStation.setName(getString(R.string.station_search_name));
            ArrayList<AudioItem> playlist = new ArrayList<AudioItem>();
            playlist.add(mSelectedSong);
            mSelectedStation.setAudioItems(playlist);
            MainActivity ma = (MainActivity) this.getActivity();
            PlayerFragment f = (PlayerFragment) ma.updateContent(PlayerFragment.URI);
            f.playStationSearchedItems(SearchFragment.TAG, mSelectedStation.clone());
        }
        else if(result.equals("text/html"))
        {
            Toast.makeText(this.getActivity(), getString(R.string.msg_err_song_deleted_on_server), Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(this.getActivity(), getString(R.string.msg_err_song_general), Toast.LENGTH_SHORT).show();
        }
    }

    private int currentPage = 0;
    private boolean isloading = false;
    
    private SearchZingSongAsync mLoadZingSongTask;

    private void loadMoreSongs()
    {
        Log.d(TAG, "loadMoreSongs()");
        mSonglistProgressBar.setVisibility(View.VISIBLE);
        //mProgressDialog = ProgressDialog.show(this.getActivity(), "", getString(R.string.msg_search_fragment_progress_bar_searching), true);
        isloading = true;
        
        if(mLoadZingSongTask != null)
        {
            mLoadZingSongTask.cancel(true);
        }
        mLoadZingSongTask = new SearchZingSongAsync(this);
        mLoadZingSongTask.execute(mEditText.getText().toString(), String.valueOf(SearchZingSongAsync.DEFAULT_RESULT_ROW_COUNT), String.valueOf(currentPage));
        currentPage ++;
    }

    private boolean endOfResult = false;

    public void onSearchZingSongAsyncComplete(ArrayList<AudioItem> al, int totalResults)
    {
        //Log.d(TAG, "onSearchZingSongAsyncComplete() size=" + al.size() + " totalResults=" + totalResults);
        if(this.isAdded())
        {
            mSongList.addAll(al);
            
            if(al.size() == 0)//end of result
            {
                endOfResult = true;
            }
            else
            {
                endOfResult = false;
                /*
                for(AudioItem ai : al)
                {
                    if(ai.isStreaming())
                    {
                        mSongList.add(ai);
                    }
                }*/
            }
            
            mSongAdapter.notifyDataSetChanged();

            if(mSongList.size() == 0)
            {
                mSongResultLayout.setVisibility(View.GONE);
                if(mArtistCount == 0)
                {
                    mEmptyTextView.setVisibility(View.VISIBLE);
                }
                else
                {
                    mEmptyTextView.setVisibility(View.GONE);
                }
            }
            else
            {
                mSongResultLayout.setVisibility(View.VISIBLE);
            }
        }

        //if(mProgressDialog != null) mProgressDialog.dismiss();
        showProgressBar(false);
        mSonglistProgressBar.setVisibility(View.GONE);
        isloading = false;
    }
    
    public void showProgressBar(boolean show)
    {
        if(mProgressBar != null)
        {
            int visPB = show ? View.VISIBLE: View.GONE;
            int visIV = show ? View.GONE: View.VISIBLE;
            mProgressBar.setVisibility(visPB);    
        }
    }

    @Override
    public void onGetZingSongDetailAsyncError(int errCode, String errMsg)
    {
        //Log.e(TAG, "onGetZingSongDetailAsyncError() - errCode=" + errCode + " errMsg=" + errMsg);
        if(this.isAdded()) Toast.makeText(this.getActivity(), getString(R.string.msg_err_song_dropped), Toast.LENGTH_SHORT).show();
    }
    
    private void performGetAlbumServerId(String id, String name)
    {
        showProgressBar(true);
        if(mGetAlbumServerIdAsync != null)
        {
            mGetAlbumServerIdAsync.cancel(true);
        }
        mGetAlbumServerIdAsync = new GetAlbumServerIdAsync(this);
        mGetAlbumServerIdAsync.execute(id, name);
        //new GetAlbumServerIdAsync(this).execute(id, name);
    }

    @Override
    public void OnGetAlbumServerIdAsyncComplete(String serverId)
    {
        showProgressBar(false);
        if(mSelectedAlbum != null && this.isAdded())
        {
            mSelectedAlbum.setServerId(serverId);
            MainActivity ma = (MainActivity) this.getActivity();
            PlayerFragment f = (PlayerFragment) ma.updateContent(PlayerFragment.URI);
            f.setRepeatSong(false);
            f.playStation(mSelectedAlbum.clone());    
        }
    }

    @Override
    public void OnGetAlbumServerIdAsyncError(int errCode, String errMsg)
    {
        showProgressBar(false);
        Log.e(TAG, "errCode=" + errCode + " errMsg=" + errMsg);
        if(this.isAdded()) Toast.makeText(this.getActivity(), R.string.msg_err_station_not_available, Toast.LENGTH_SHORT).show();
    }
}
