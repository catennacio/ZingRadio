package com.friends.zingradio.fragment;

import java.util.ArrayList;

import com.friends.zingradio.R;
import com.friends.zingradio.data.ZingRadioDatabaseHelper;
import com.friends.zingradio.entity.*;
import com.friends.zingradio.ui.MenuItemGestureDetector;
import com.friends.zingradio.util.Constants;
import com.friends.zingradio.util.GAUtils;
import com.friends.zingradio.util.NetworkConnectivityListener;
import com.friends.zingradio.util.Utilities;
import com.friends.zingradio.activity.MainActivity;
import com.friends.zingradio.adapter.SongAdapter;
import com.friends.zingradio.adapter.StationAdapter;
import com.friends.zingradio.async.GetStationAudioListAsync;
import com.friends.zingradio.async.GetStationAudioListAsyncComplete;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.AsyncTask.Status;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;


public class FavoriteStationListFragment extends Fragment implements GetStationAudioListAsyncComplete
{
    public static final String TAG = FavoriteStationListFragment.class.getSimpleName();
    public static final Uri URI = new Uri.Builder().scheme(TAG).authority("").build();

    private View viewRoot;
    private StationAdapter mStationAdapter;
    private ArrayList<Station> mItems;
    private ListView mListView;
    private TextView mTextView;
    private GetStationAudioListAsync mGetStationAudioListAsync;
    private FavoriteStationListFragment mListener;
    private PlayerFragment mPlayerFragment;
    private Station mSelectedStation;
    private ImageButton mBackButton;

    public FavoriteStationListFragment() {}
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        GAUtils.writeViewFragement(this.getActivity(), TAG);
        //Log.d(TAG, "onCreateView()");
        viewRoot= inflater.inflate(R.layout.station_list, container, false);
        
        mBackButton = (ImageButton)viewRoot.findViewById(R.id.button_back);
        mBackButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                MainActivity ma = (MainActivity) FavoriteStationListFragment.this.getActivity();
                ma.updateLeftFrame(LeftMenuFragment.URI, null);
            }
        });
        
        mTextView = (TextView)viewRoot.findViewById(R.id.station_list_header);
        mTextView.setText(getString(R.string.my_channels));
        mTextView.setOnClickListener(new OnClickListener()
        {
            
            @Override
            public void onClick(View v)
            {
                MainActivity ma = (MainActivity) FavoriteStationListFragment.this.getActivity();
                ma.updateLeftFrame(LeftMenuFragment.URI, null);
            }
        });
        
        final GestureDetector gestureDetector = new GestureDetector(this.getActivity(), new MenuItemGestureDetector(this.getActivity()));
        View.OnTouchListener gestureListener = new View.OnTouchListener()
        {
            public boolean onTouch(View v, MotionEvent event)
            {
                return gestureDetector.onTouchEvent(event);
            }
        };
        
        mListView = (ListView) viewRoot.findViewById(R.id.station_listview);
        mListView.setOnTouchListener(gestureListener);
        registerForContextMenu(mListView);
        
        //mListener = this;
        
        mItems = ZingRadioDatabaseHelper.getInstance(getActivity()).getFavoriteStations();
        
        if(mItems != null)
        {
            mStationAdapter = new StationAdapter(getActivity(), mItems);
            mListView.setAdapter(mStationAdapter);
            
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> adapter, View v, int position, long flags)
                {
                    if(Utilities.isNetworkOnline(getActivity()))
                    {
                        mSelectedStation = mItems.get(position);
                        Log.d(TAG, "Selected channel=" + mSelectedStation.getName()+ "Id=" + mSelectedStation.getId() + " ServerId=" + mSelectedStation.getServerId());
                        
                        MainActivity ma = (MainActivity)getActivity();
                        PlayerFragment f = (PlayerFragment) ma.updateContent(PlayerFragment.URI);
                        f.setRepeatSong(false);
                        f.playStation(mSelectedStation);
                        ma.updateLeftFrame(LeftMenuFragment.URI, null);
                        /*
                        
                        if(mGetStationAudioListAsync != null)
                        {
                            if(mGetStationAudioListAsync.getStatus() != Status.FINISHED)
                            {
                                mGetStationAudioListAsync.cancel(true);
                            }
                        }

                        mGetStationAudioListAsync = new GetStationAudioListAsync(FavoriteStationListFragment.this);
                        mGetStationAudioListAsync.execute(mSelectedStation.getServerId());

                        MainActivity ma = (MainActivity)getActivity();
                        mPlayerFragment = (PlayerFragment)ma.updateContent(PlayerFragment.URI);
                        mPlayerFragment.enableButtons(false);
                        */
                        Toast.makeText(viewRoot.getContext(), "Loading " + mSelectedStation.getName() + "...", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(viewRoot.getContext(), getString(R.string.msg_err_connection_error), Toast.LENGTH_LONG).show();
                        //Toast.makeText(viewRoot.getContext(), "5", Toast.LENGTH_LONG).show();
                    }
                }
            });
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
        /*
        Drawable d = mBackButton.getDrawable();
        if (d instanceof BitmapDrawable)
        {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) d;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            bitmap.recycle();
        }
        */
        if(mItems != null)
        {
           for(Station s: mItems)
           {
               s.freeAudioItems();
           }
        }
        
        if(mSelectedStation != null)
        {
            mSelectedStation.freeAudioItems();
        }
        
        super.onDestroy();
    }

    @Override
    public void onHiddenChanged(boolean hidden)
    {
        super.onHiddenChanged(hidden);
        if(!hidden)
        {
            mItems = ZingRadioDatabaseHelper.getInstance(getActivity()).getFavoriteStations();
            mStationAdapter.setStations(mItems);
            mStationAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onGetStationAudioListAsyncComplete(ArrayList<AudioItem> al)
    {
        mSelectedStation.setAudioItems(al);
        mPlayerFragment.setStation(mSelectedStation);
        mPlayerFragment.setRepeatSong(false);
        mPlayerFragment.playStation(mSelectedStation);
    }
    
    @Override
    public void onGetStationAudioListAsyncError(int errCode, String errMsg)
    {
        Toast.makeText(this.getActivity(), getString(R.string.msg_err_play_list_empty), Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        
        if (v.getId() == R.id.station_listview)
        {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            Station st = (Station)mListView.getAdapter().getItem(info.position);
            
            menu.setHeaderTitle("Remove " + st.getName() + "?");
            /*
            menu.add("Yes");
            menu.add("No");
            */
            
            MenuInflater inflater = new MenuInflater(getActivity());
            inflater.inflate(R.menu.delete_confirm, menu);
        }
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        
        switch (item.getItemId())
        {
            case R.id.menu_delete_confirm_yes:
            {
                Station st = (Station)mListView.getAdapter().getItem(info.position);
                ZingRadioDatabaseHelper db = ZingRadioDatabaseHelper.getInstance(getActivity());
                db.deleteFavoriteStation(st.getId());

                mItems.remove(info.position);
                mStationAdapter.setStations(mItems);
                mStationAdapter.notifyDataSetChanged();
                
                return true;
            }
            case R.id.menu_delete_confirm_no:
            {
                return true;
            }
            default:
                return super.onContextItemSelected(item);
        }
    }
}
