package com.friends.zingradio.adapter;

import java.util.ArrayList;

import com.friends.zingradio.R;
import com.friends.zingradio.entity.*;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class StationAdapter extends BaseAdapter
{
    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_STATIONS = 1;
    
    public static String TAG = StationAdapter.class.getSimpleName();
    private final LayoutInflater mInflater;
    private ArrayList<Station> mStations;

    public ArrayList<Station> getStations()
    {
        return mStations;
    }

    public void setStations(ArrayList<Station> st)
    {
        this.mStations = st;
    }

    Context mContext;
    
    public StationAdapter(Context context)
    {
        mInflater = LayoutInflater.from(context);
        mContext = context;
    }
    
    public StationAdapter(Context context, ArrayList<Station> items)
    {
        mInflater = LayoutInflater.from(context);
        mContext = context;
        mStations = items;
    }

    @Override
    public int getCount()
    {
        return this.mStations.size();
    }

    @Override
    public Object getItem(int position)
    {
        return mStations.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }
    
    /*
    @Override
    public int getItemViewType(int position)
    {
        
        Object o = getItem(position);
        if(o instanceof Station || o != null)
        {
            return VIEW_TYPE_STATIONS;
        }
        else
        {
            return VIEW_TYPE_HEADER;
        }
        /*
        if(position == 0) return VIEW_TYPE_HEADER;
        else return VIEW_TYPE_STATIONS;
        *
        
        return position;
    }
    
    
    
    @Override
    public boolean isEnabled(int position)
    {
        return getItemViewType(position) != VIEW_TYPE_HEADER;
    }
    */
    

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        //final int type = getItemViewType(position);
        
        final ViewHolder holder;
        if (convertView == null)
        {
            convertView = mInflater.inflate(R.layout.station_list_item, parent, false);
            
            /*if (type == VIEW_TYPE_HEADER)
            {
                convertView = mInflater.inflate(R.layout.station_list_header, parent, false);
            }
            else
            {
                convertView = mInflater.inflate(R.layout.station_list_item, parent, false);    
            }
*/
            holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(android.R.id.text1);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        Station st = (Station)mStations.get(position);
        holder.text.setText(st.getName());
  
        /*
        final Resources res = mContext.getResources();
        Drawable drawable = res.getDrawable(R.drawable.ic_station);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        holder.text.setCompoundDrawables(drawable, null, null, null);
        */
        
        return convertView;
    }

    private static class ViewHolder
    {
        TextView text;
    }
}
