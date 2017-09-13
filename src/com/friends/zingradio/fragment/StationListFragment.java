package com.friends.zingradio.fragment;

import java.util.ArrayList;

import com.friends.zingradio.R;
import com.friends.zingradio.ZingRadioApplication;
import com.friends.zingradio.activity.MainActivity;
import com.friends.zingradio.adapter.StationAdapter;
import com.friends.zingradio.async.GetStationAudioListAsync;
import com.friends.zingradio.async.GetStationAudioListAsyncComplete;
import com.friends.zingradio.entity.*;
import com.friends.zingradio.ui.MenuItemGestureDetector;
import com.friends.zingradio.util.Constants;
import com.friends.zingradio.util.GAUtils;
import com.friends.zingradio.util.NetworkConnectivityListener;
import com.friends.zingradio.util.Utilities;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class StationListFragment extends Fragment
{
    public static final String TAG = StationListFragment.class.getSimpleName();
    public static final Uri URI = new Uri.Builder().scheme(TAG).authority("").build();
    public static final int CAT_THELOAI_ID = 0;
    public static final int CAT_CHUDE_ID = 1;
    public static final int CAT_ELECTRONIC_ID = 2;
    public static final int CAT_NGHESI_ID = 3;
    private View viewRoot;
    private StationAdapter mSA;;
    
    private PlayerFragment mPlayerFragment;
    private Station mSelectedStation;
    private TextView mHeaderText;
    private Category mCategory;
    private ListView mListView;
    private ImageButton mBackButton;
    
    public Station getSelectedStation()
    {
        return mSelectedStation;
    }
    
    public Category getCategory()
    {
        return mCategory;
    }
    
    int a;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    { 
        //Log.d(TAG, "onCreateView()");
        GAUtils.writeViewFragement(this.getActivity(), TAG);
        viewRoot = inflater.inflate(R.layout.station_list, container, false);
        mBackButton = (ImageButton)viewRoot.findViewById(R.id.button_back);
        mBackButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                MainActivity ma = (MainActivity) StationListFragment.this.getActivity();
                ma.updateLeftFrame(LeftMenuFragment.URI, null);
            }
        });
        
        mHeaderText = (TextView) viewRoot.findViewById(R.id.station_list_header);
        mHeaderText.setOnClickListener(new OnClickListener()
        {
            
            @Override
            public void onClick(View v)
            {
                MainActivity ma = (MainActivity) StationListFragment.this.getActivity();
                ma.updateLeftFrame(LeftMenuFragment.URI, null); 
            }
        });

        mListView = (ListView) viewRoot.findViewById(R.id.station_listview);

        /*
        if(!this.isAdded())
        {
            Log.d(TAG, "1");
            Bundle b = this.getArguments();
            mCategory = b.getParcelable("Cat");
        }
        
        Log.d(TAG, "2");
        */
        
        Bundle b = this.getArguments();
        if(b != null && !b.isEmpty())
        {
            String catId = b.getString("CatId");
            ZingRadioApplication app = ((ZingRadioApplication) getActivity().getApplication());
            app.loadAll();
            mCategory = app.getRadio().getCategory(catId);
            //mCategory = b.getParcelable("Cat");
        }
        
        //Log.d(TAG, "************* cat_id=" + mCategory.getId());

        final GestureDetector gestureDetector = new GestureDetector(this.getActivity(), new MenuItemGestureDetector(this.getActivity()));
        View.OnTouchListener gestureListener = new View.OnTouchListener()
        {
            public boolean onTouch(View v, MotionEvent event)
            {
                return gestureDetector.onTouchEvent(event);
            }
        };
        mListView.setOnTouchListener(gestureListener);
        mSA = new StationAdapter(getActivity(), mCategory.getStations());
        mHeaderText.setText(mCategory.getName());
        mListView.setAdapter(mSA);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long flags)
            {
                if (getNetworkState() == NetworkConnectivityListener.State.CONNECTED)
                {
                    mSelectedStation = (Station) mCategory.getStations().get(position);
                    // Log.d(TAG, "Selected station=" + mSelectedStation.getId()
                    // + " ServerID=" + mSelectedStation.getServerId());
                    Toast.makeText(viewRoot.getContext(), "Loading " + mSelectedStation.getName() + "...", Toast.LENGTH_SHORT).show();

                    MainActivity ma = (MainActivity) StationListFragment.this.getActivity();
                    mPlayerFragment = (PlayerFragment) ma.updateContent(PlayerFragment.URI);
                    mPlayerFragment.setRepeatSong(false);
                    mPlayerFragment.playStation(mSelectedStation);
                    ma.updateLeftFrame(LeftMenuFragment.URI, null);
                }
                else
                {
                    Toast.makeText(viewRoot.getContext(), getString(R.string.msg_err_connection_error), Toast.LENGTH_LONG).show();
                }
            }
        });

        return viewRoot;
    }
    
    

    private NetworkConnectivityListener.State getNetworkState()
    {
        return ((MainActivity)this.getActivity()).getNetworkConnectivityListener().getState();
    }
    
    public void populateStations(Category cat)
    {
        mCategory = cat;
        //Log.d(TAG, "populateStations() - Cat id=" + mCategory.getId() + " name=" + mCategory.getName() + " Station count=" + mCategory.getStations().size());
        mHeaderText.setText(mCategory.getName());
        mSA = new StationAdapter(getActivity(), mCategory.getStations());
        mHeaderText.setText(mCategory.getName());
        mListView.setAdapter(mSA);
    }
    
    @Override
    public void onResume()
    {
        super.onResume();
        //this.populateStations();
    }
}
