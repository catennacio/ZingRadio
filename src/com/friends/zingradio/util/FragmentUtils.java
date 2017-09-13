package com.friends.zingradio.util;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.friends.zingradio.fragment.PlayerFragment;
import com.friends.zingradio.fragment.StationListFragment;

public class FragmentUtils
{
    public static String TAG = FragmentUtils.class.getSimpleName();
    
    public static Fragment getFragment(FragmentManager fm, String tag)
    {
        //Log.d(TAG,"getFragment() - tag=" + tag);
        return fm.findFragmentByTag(tag);
    }
}
