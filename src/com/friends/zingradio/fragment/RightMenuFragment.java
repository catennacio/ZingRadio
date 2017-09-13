package com.friends.zingradio.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.friends.zingradio.R;
import com.friends.zingradio.activity.MainActivity;
import com.friends.zingradio.adapter.ActionsAdapter;
import com.friends.zingradio.util.Constants;
import com.friends.zingradio.util.GAUtils;
import com.friends.zingradio.util.NetworkConnectivityListener;

public class RightMenuFragment extends Fragment
{
    public static final String TAG = RightMenuFragment.class.getSimpleName();
    public static final Uri URI = new Uri.Builder().scheme(TAG).authority("").build();
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        GAUtils.writeViewFragement(this.getActivity(), TAG);
        return inflater.inflate(R.layout.actions_right, null);
    }

    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }
}
