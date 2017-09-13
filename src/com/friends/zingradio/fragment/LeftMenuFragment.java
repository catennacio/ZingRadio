package com.friends.zingradio.fragment;

import com.friends.zingradio.R;
import com.friends.zingradio.ZingRadioApplication;
import com.friends.zingradio.activity.MainActivity;
import com.friends.zingradio.activity.SettingsActivity;
import com.friends.zingradio.adapter.ActionsAdapter;
import com.friends.zingradio.entity.Category;
import com.friends.zingradio.entity.Radio;
import com.friends.zingradio.util.Constants;
import com.friends.zingradio.util.NetworkConnectivityListener;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;
import android.util.Log;

public class LeftMenuFragment extends Fragment
{
    public static final String TAG = LeftMenuFragment.class.getSimpleName();
    public static final Uri URI = new Uri.Builder().scheme(TAG).authority("").build();
    
    public enum MenuId
    {
        Theloai(0),
        Chude(1),
        Electronic(2),
        Nghesi(3),
        Suggest(4),
        FavChannels(5),
        FavSongs(6);
    
        private int value;
        private MenuId(int value)
        {
            this.value = value;
        }
        public int getValue()
        {
            return this.value;
        }
    }
    
    public static final int MENU_THELOAI_ID = 0;
    public static final int MENU_CHUDE_ID = 1;
    public static final int MENU_ELECTRONIC_ID = 2;
    public static final int MENU_NGHESI_ID = 3;
    public static final int MENU_ELECTRONICID = 4;
    public static final int MENU_FAVORITE_ID = 4;
    private View viewRoot;
    
    private EditText mSearchBoxEditText;
    private ListView mViewActionsList;
    
    private Button mTheloaiButton;
    private Button mChudeButton;
    private Button mNghesiButton;
    private Button mElectronicButton;
    private Button mSuggestButton;
    private Button mFavChannelsButton;
    private Button mFavSongsButton;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        viewRoot =  inflater.inflate(R.layout.actions, null);
        mSearchBoxEditText = (EditText)viewRoot.findViewById(R.id.actions_list_searchbox);
        mTheloaiButton = (Button)viewRoot.findViewById(R.id.button_channel_theloai);
        mChudeButton = (Button)viewRoot.findViewById(R.id.button_channel_chude);
        mNghesiButton = (Button)viewRoot.findViewById(R.id.button_channel_nghesi);
        mElectronicButton = (Button)viewRoot.findViewById(R.id.button_channel_electronic);
        mSuggestButton = (Button)viewRoot.findViewById(R.id.button_channel_suggest);
        mFavChannelsButton = (Button)viewRoot.findViewById(R.id.button_channel_favchannels);
        mFavSongsButton = (Button)viewRoot.findViewById(R.id.button_channel_favsongs);

        return viewRoot;
    }
    
    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        
        /*
        mSearchBoxEditText.setOnFocusChangeListener(new OnFocusChangeListener()
        {
            
            @Override
            public void onFocusChange(View v, boolean focus)
            {
                if(focus)
                {
                    Uri uri = new Uri.Builder().scheme(SearchFragment.TAG).authority("").build();
                    MainActivity ma = (MainActivity) getActivity();
                    ma.updateContent(uri);    
                }
            }
        });
        */
        
        mSearchBoxEditText.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Uri uri = SearchFragment.URI;
                //Uri uri = new Uri.Builder().scheme(TestSearchZingSongFragment.TAG).authority("").build();
                MainActivity ma = (MainActivity) getActivity();
                //ma.updateContent(uri);
                ma.updateLeftFrame(uri, null);
            }
        });

        mTheloaiButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                openStationList(MenuId.Theloai);
            }
        });
        
        mChudeButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                openStationList(MenuId.Chude);
            }
        });
        
        mNghesiButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.msg_search_artist_for_more), Toast.LENGTH_SHORT).show();
                openStationList(MenuId.Nghesi);
            }
        });
        
        mElectronicButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                openStationList(MenuId.Electronic);
            }
        });
        
        mSuggestButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                openStationList(MenuId.Suggest);
            }
        });
        
        mFavChannelsButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                openStationList(MenuId.FavChannels);
            }
        });
        
        mFavSongsButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                openStationList(MenuId.FavSongs);
            }
        });

        /*
        mViewActionsList = (ListView) viewRoot.findViewById(R.id.actions_list);

        //Log.d(TAG, "mViewActionsList=" + mViewActionsList);
        mViewActionsList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        final ActionsAdapter actionsAdapter = new ActionsAdapter(this.getActivity());
        mViewActionsList.setAdapter(actionsAdapter);

        mViewActionsList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            MainActivity ma = (MainActivity) getActivity();

            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long flags)
            {
                // Log.d(TAG, "Position=" + position);
                mViewActionsList.setItemChecked(position, true);

                final Uri uri = actionsAdapter.getItem(position);
                String schema = uri.getScheme();
                String au = uri.getAuthority();
                // Log.d(TAG, "Schema=" + schema + " Au=" + au);
                if (schema.equals(Constants.URI_SCHEMA_CATEGORY))
                {
                    int catId = Integer.parseInt(au);

                    switch (catId)
                    {
                        case 0: case 1: case 2: case 3:
                        {
                            Uri newUri = new Uri.Builder().scheme(StationListFragment.TAG).authority(au).build();
                            // Log.d(TAG, "onCreate()::Uri = " +
                            // newUri.toString());

                            Category cat = radio.getCategory(au);
                            cat.setId(au);
                            StationListFragment f = (StationListFragment) ma.updateContent(newUri);

                            if (f.isAdded())
                            {
                                //Log.d(TAG, "StationListFragment is already added.");
                                f.populateStations(cat);
                            }
                            else
                            // first time creating the fragment
                            {
                                //Log.d(TAG, "StationListFragment first time create.");
                                Bundle b = new Bundle();
                                b.putParcelable("Cat", cat);
                                f.setArguments(b);
                            }

                            break;
                        }
                        case 4:// fav channels
                        {
                            Uri newUri = new Uri.Builder().scheme(FavoriteStationListFragment.TAG).authority(au).build();
                            FavoriteStationListFragment f = (FavoriteStationListFragment) ma.updateContent(newUri);

                            break;
                        }
                    }
                }
                else if (schema.equals(Constants.URI_SCHEMA_APP))
                {
                    if (au.equals(Constants.URI_SCHEMA_APP_SETTINGS))
                    {
                        //ma.showAboutDialog();
                        Intent settingsIntent = new Intent(LeftMenuFragment.this.getActivity(), SettingsActivity.class);
                        ma.startActivity(settingsIntent);
                    }
                    else if (au.equals(Constants.URI_SCHEMA_APP_PLAY_SUGGEST))// play suggestion  menu item
                    {
                        if (ma.getNetworkState() == NetworkConnectivityListener.State.CONNECTED)
                        {
                            Toast.makeText(ma, "Suggesting...", Toast.LENGTH_LONG).show();
                            ma.updateContent(PlayerFragment.URI);
                            ma.playSuggestedStation();
                        }
                        else
                        {
                            Toast.makeText(ma, getString(R.string.msg_err_connection_error), Toast.LENGTH_SHORT).show();
                        }
                    }
                    else if (au.equals(Constants.URI_SCHEMA_APP_MYSONGS))// my
                                                                         // songs
                                                                         // items
                    {
                        Uri newUri = new Uri.Builder().scheme(FavoriteSongListFragment.TAG).authority(au).build();
                        FavoriteSongListFragment f = (FavoriteSongListFragment) ma.updateContent(newUri);
                    }
                }
            }
        });
        */
    }

    private void openStationList(MenuId menuId)
    {
        ZingRadioApplication app = ((ZingRadioApplication) LeftMenuFragment.this.getActivity().getApplication());
        app.loadAll();
        final Radio radio = app.getRadio();
        MainActivity ma = (MainActivity) getActivity();

        switch(menuId)
        {
            case Theloai: case Chude: case Nghesi: case Electronic:
            {
                String s = String.valueOf(menuId.getValue());
                Fragment f = ma.getCurrentLeftFragment();
                if(f instanceof StationListFragment && f.isAdded())
                {
                    StationListFragment sf = (StationListFragment)f;
                    Category cat = radio.getCategory(s);
                    sf.populateStations(cat);
                }
                else
                {
                    Bundle b = new Bundle();
                    b.putString("CatId", s);
                    f = ma.updateLeftFrame(StationListFragment.URI, b);
                }
                //StationListFragment f = (StationListFragment) ma.updateLeftFrame(StationListFragment.URI);
                /*
                if (f.isAdded())
                {
                    f.populateStations(cat);
                }
                else
                {
                    Bundle b = new Bundle();
                    b.putParcelable("Cat", cat);
                    f.setArguments(b);
                }*/
                
                break;
            }
            case Suggest:
            {
                if (ma.getNetworkState() == NetworkConnectivityListener.State.CONNECTED)
                {
                    Toast.makeText(ma, "Suggesting...", Toast.LENGTH_LONG).show();
                    ma.updateContent(PlayerFragment.URI);
                    ma.playSuggestedStation();
                }
                else
                {
                    Toast.makeText(ma, getString(R.string.msg_err_connection_error), Toast.LENGTH_SHORT).show();
                }
                
                break;
            }
            case FavChannels:
            {
                FavoriteStationListFragment f = (FavoriteStationListFragment) ma.updateLeftFrame(FavoriteStationListFragment.URI, null);
                break;
            }
            case FavSongs:
            {
                FavoriteSongListFragment f = (FavoriteSongListFragment) ma.updateLeftFrame(FavoriteSongListFragment.URI, null);
                break;
            }
        }
    }

}
