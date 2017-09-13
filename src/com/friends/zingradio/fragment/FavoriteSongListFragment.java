package com.friends.zingradio.fragment;

import java.util.ArrayList;
import java.util.Hashtable;

import com.friends.zingradio.R;
import com.friends.zingradio.data.ZingRadioDatabaseHelper;
import com.friends.zingradio.entity.*;
import com.friends.zingradio.entity.json.Song;
import com.friends.zingradio.media.MusicService;
import com.friends.zingradio.media.MusicServiceEventListener;
import com.friends.zingradio.ui.MenuItemGestureDetector;
import com.friends.zingradio.util.Constants;
import com.friends.zingradio.util.GAUtils;
import com.friends.zingradio.util.NetworkConnectivityListener;
import com.friends.zingradio.util.Utilities;
import com.friends.zingradio.util.download.FileDownloadManager;
import com.friends.zingradio.util.download.IFileDownloadManagerListener;
import com.friends.zingradio.activity.MainActivity;
import com.friends.zingradio.adapter.SongAdapter;
import com.friends.zingradio.adapter.StationAdapter;
import com.friends.zingradio.async.GetStationAudioListAsync;
import com.friends.zingradio.async.GetZingSongDetailAsync;
import com.friends.zingradio.async.GetZingSongDetailAsyncComplete;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.AsyncTask.Status;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnClickListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FavoriteSongListFragment extends Fragment implements MusicServiceEventListener, IFileDownloadManagerListener, GetZingSongDetailAsyncComplete
{
    public static final String TAG = FavoriteSongListFragment.class.getSimpleName();
    public static final Uri URI = new Uri.Builder().scheme(TAG).authority("").build();

    // private Hashtable<Long, AudioItem> mDownloadTable = new Hashtable<Long,
    // AudioItem>();

    private View viewRoot;
    private SongAdapter mSongAdapter;
    private ArrayList<AudioItem> mItems;
    private ListView mListView;
    private TextView mTextView;
    private FileDownloadManager mFileDownloadManager;
    private AudioItem mSelectedItem;
    private Hashtable<Long, AudioItem> mDownloadTable = new Hashtable<Long, AudioItem>();
    private ImageButton mBackButton;
    public AudioItem mPlayingItem;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mFileDownloadManager = new FileDownloadManager(this);
        mFileDownloadManager.init();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreateView()");
        GAUtils.writeViewFragement(this.getActivity(), TAG);

        viewRoot = inflater.inflate(R.layout.song_list, container, false);

        mBackButton = (ImageButton) viewRoot.findViewById(R.id.button_back);
        mBackButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                MainActivity ma = (MainActivity) FavoriteSongListFragment.this.getActivity();
                ma.updateLeftFrame(LeftMenuFragment.URI, null);
            }
        });

        mTextView = (TextView) viewRoot.findViewById(R.id.song_list_header);
        mTextView.setText(getString(R.string.menu_baihatyeuthich));
        mTextView.setOnClickListener(new OnClickListener()
        {
            
            @Override
            public void onClick(View v)
            {
                MainActivity ma = (MainActivity) FavoriteSongListFragment.this.getActivity();
                ma.updateLeftFrame(LeftMenuFragment.URI, null);
            }
        });
        mListView = (ListView) viewRoot.findViewById(R.id.song_listview);
        final GestureDetector gestureDetector = new GestureDetector(this.getActivity(), new MenuItemGestureDetector(this.getActivity()));
        View.OnTouchListener gestureListener = new View.OnTouchListener()
        {
            public boolean onTouch(View v, MotionEvent event)
            {
                return gestureDetector.onTouchEvent(event);
            }
        };
        mListView.setOnTouchListener(gestureListener);

        mItems = ZingRadioDatabaseHelper.getInstance(getActivity()).getFavoriteSongs();
        if (mItems != null)
        {
            /*
             * View header =
             * (View)inflater.inflate(R.layout.station_list_header,null);
             * mListView.addHeaderView(header, null, false); TextView tv_header
             * = (TextView) viewRoot.findViewById(R.id.tv_station_list_header);
             * tv_header.setText("Favorites");
             */

            mSongAdapter = new SongAdapter(getActivity(), mItems);
            MainActivity ma = (MainActivity) getActivity();
            mSongAdapter.mPlayingItem = ma.getPlayingItem();
            mListView.setAdapter(mSongAdapter);
            AudioItem playingSong = mSongAdapter.mPlayingItem;
            if (playingSong != null) scrolltoSong(playingSong.getId());

            mListView.setLongClickable(true);

            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> adapter, View v, int position, long flags)
                {
                    if (Utilities.isNetworkOnline(getActivity()))
                    {
                        // position = position - 1;

                        AudioItem selectedSong = mItems.get(position);
                        Log.d(TAG, "Selected song=" + selectedSong.getTitle() + " Id=" + selectedSong.getId());

                        MainActivity ma = (MainActivity) getActivity();
                        PlayerFragment pf = (PlayerFragment) ma.updateContent(PlayerFragment.URI);
                        ma.updateLeftFrame(LeftMenuFragment.URI, null);

                        Station st = new Station();
                        st.setId(Station.MY_SONGS_ID);
                        st.setName(getString(R.string.station_favorite_name));
                        st.setAudioItems(mItems);
                        pf.setStation(st);
                        pf.playAt(position);
                        pf.enableButtons(false);

                        // Toast.makeText(viewRoot.getContext(), "Loading " +
                        // selectedSong.getTitle() + "...",
                        // Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(viewRoot.getContext(), getString(R.string.msg_err_connection_error), Toast.LENGTH_SHORT).show();
                    }
                }
            });

            registerForContextMenu(mListView);
        }
        else
        {
            Log.e(TAG, "Favorite items not set!");
        }

        return viewRoot;
    }
    
    @Override
    public void onDestroy()
    {
        if (mFileDownloadManager != null)
        {
            mFileDownloadManager.close();
        }

        /*
        Drawable d = mBackButton.getDrawable();
        if (d instanceof BitmapDrawable)
        {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) d;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            bitmap.recycle();
        }
         */
        if (mItems != null)
        {
            for (AudioItem a : mItems)
            {
                a = null;
            }
        }

        super.onDestroy();
    }

    @Override
    public void onHiddenChanged(boolean hidden)
    {
        super.onHiddenChanged(hidden);
        if (!hidden)
        {
            updateView(true);// don't scroll
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);

        if (v.getId() == R.id.song_listview)
        {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            AudioItem ai = (AudioItem) mListView.getAdapter().getItem(info.position);

            menu.setHeaderTitle(ai.getTitle());
            MenuInflater inflater = new MenuInflater(getActivity());
            inflater.inflate(R.menu.song_item, menu);

            if (Utilities.checkMp3FileExists(ai.getId()) != null)
            {
                menu.removeItem(R.id.menu_song_item_download);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        if (mItems != null && mItems.size() > 0)
        {
            AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

            mSelectedItem = (AudioItem) mListView.getAdapter().getItem(info.position);

            switch (item.getItemId())
            {
                case R.id.menu_song_item_download:
                {
                    new GetZingSongDetailAsync(FavoriteSongListFragment.this).execute(mSelectedItem.getId());
                    return true;
                }
                case R.id.menu_song_item_delete:
                {
                    ZingRadioDatabaseHelper db = ZingRadioDatabaseHelper.getInstance(getActivity());
                    db.deleteFavoriteSong(mSelectedItem.getId());
                    updateView(false);// don't scroll
                    return true;
                }
                default:
                {
                    return super.onContextItemSelected(item);
                }
            }
        }
        else
            return false;
    }

    private void scrolltoSong(String id)
    {
        int p = -1;

        for (int i = 0; i < mItems.size(); i++)
        {
            AudioItem ai = (AudioItem) mItems.get(i);
            if (ai.getId().equals(id))
            {
                p = i;
                break;
            }
        }

        if (p >= 0)
        {
            final int pos = p;
            mListView.post(new Runnable()
            {
                @Override
                public void run()
                {
                    mListView.smoothScrollToPosition(pos);
                }
            });
        }
    }

    private void updateView(boolean scrollToPlayingItemIfAvailable)
    {
        mItems = ZingRadioDatabaseHelper.getInstance(getActivity()).getFavoriteSongs();
        mSongAdapter.setSongs(mItems);
        MainActivity ma = (MainActivity) getActivity();
        if (ma != null && ma.getPlayingItem() != null)
        {
            mSongAdapter.mPlayingItem = ma.getPlayingItem();
            mSongAdapter.notifyDataSetChanged();
            if (scrollToPlayingItemIfAvailable)
            {
                scrolltoSong(mSongAdapter.mPlayingItem.getId());
            }
        }
    }

    @Override
    public void onNext(AudioItem nextAudioItem)
    {
    }

    @Override
    public void onPlay(AudioItem audioItem, boolean updateBitmap)
    {
        // Log.d(TAG, "Receive onPlay notification - pos=" + pos);
        updateView(true);
    }

    @Override
    public void onPause(AudioItem audioItem)
    {
    }

    @Override
    public void onStop(AudioItem lastAudioItem)
    {
    }

    @Override
    public void onError(int errCode)
    {
    }

    @Override
    public void onGetZingSongDetailAsyncComplete(Song song)
    {
        // need to know which song is done, in case multiple downloads
        mSelectedItem.setDownloadLink(song.getLinkDownload128());
        long downloadId = mFileDownloadManager.startDownload(mSelectedItem);
        mDownloadTable.put(downloadId, mSelectedItem);
    }

    @Override
    public void onGetZingSongDetailAsyncError(int errCode, String errMsg)
    {
        Toast.makeText(this.getActivity(), getString(R.string.msg_err_song_dropped), Toast.LENGTH_SHORT).show();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void OnDownloadComplete(AudioItem ai, long downloadId)
    {
        if (ai == null) return;
        // Prepare intent which is triggered if the notification is selected
        Intent i = new Intent(getActivity(), MainActivity.class);
        // AudioItem ai = mDownloadTable.get(downloadId);
        // if(ai == null) return;
        // mDownloadTable.remove(downloadId);

        int statusCode = mFileDownloadManager.getStatusCode(downloadId);
        String statusMsg = mFileDownloadManager.getStatus(downloadId);
        if (statusCode == DownloadManager.STATUS_SUCCESSFUL)
        {
            Log.d(TAG, "msg=" + statusMsg);
            i.putExtra(MainActivity.INTENT, MainActivity.DOWNLOAD_INTENT);
            MainActivity ma = (MainActivity) this.getActivity();
            ma.mAlreadyShowMySongs = false;
        }

        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pi = PendingIntent.getActivity(getActivity(), 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notif = new Notification();
        notif.tickerText = statusMsg;// getString(R.string.download_complete);
        notif.icon = R.drawable.img_btn_download;
        notif.flags |= Notification.FLAG_AUTO_CANCEL;
        notif.setLatestEventInfo(getActivity(), ai.getTitle() + " - " + ai.getPerformer(), statusMsg, pi);
        notif.contentIntent = pi;

        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        // Hide the notification after its selected
        notificationManager.notify((int) downloadId, notif);
        updateView(false);// don't scroll
    }

    @Override
    public void OnDownloadNotificationClick(AudioItem ai, long downloadId)
    {
        Toast.makeText(this.getActivity(), getString(R.string.download_notification_click), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onServiceConnected(MusicService service)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void onServiceDisconnected()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPrev(AudioItem nextAudioItem)
    {
        // TODO Auto-generated method stub

    }
}
