/*******************************************************************************
 * Copyright 2012 Steven Rudenko
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.friends.zingradio.adapter;

import java.util.ArrayList;

import com.friends.zingradio.R;
import com.friends.zingradio.ZingRadioApplication;
import com.friends.zingradio.activity.MainActivity;
import com.friends.zingradio.entity.Category;
import com.friends.zingradio.entity.Radio;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ActionsAdapter extends BaseAdapter
{

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_SETTINGS = 1;
    private static final int VIEW_TYPE_STATIONS = 2;
    private static final int VIEW_TYPES_COUNT = 3;
    
    private final LayoutInflater mInflater;
    
    ArrayList<Category> mCategories;
    
    public static String VIEW_TYPE_HEADER_STR = "header";
    public static String VIEW_TYPE_CATEGORY_STR = "category";
    
    /*
    private final String[] mTitles;
    //private final String[] mIds;
    */
    
    private final String[] mTitles;
    
    private final String[] mUrls;
    
    private final TypedArray mIcons;

    public ActionsAdapter(Context context)
    {
        mInflater = LayoutInflater.from(context);

        final Resources res = context.getResources();

        // load XML data here
        MainActivity ctx = (MainActivity)context;
        ZingRadioApplication app = ((ZingRadioApplication)ctx.getApplication());
        Radio radio = app.getRadio();
        mCategories = radio.getCategories();        
        mTitles = res.getStringArray(R.array.actions_names);
        //mIds = res.getStringArray(R.array.category_ids);
        mUrls = res.getStringArray(R.array.actions_links);
        mIcons = res.obtainTypedArray(R.array.actions_icons);
    }

    @Override
    public int getCount()
    {
        return mUrls.length;
    }

    @Override
    public Uri getItem(int position)
    {
        return Uri.parse(mUrls[position]);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        final int type = getItemViewType(position);

        final ViewHolder holder;
        if (convertView == null)
        {
            if (type == VIEW_TYPE_HEADER)
            {
                convertView = mInflater.inflate(R.layout.header_list_item, parent, false);
            }
            else
                convertView = mInflater.inflate(R.layout.action_list_item, parent, false);

            holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(android.R.id.text1);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.text.setText(mTitles[position]);
        if (type != VIEW_TYPE_HEADER)//draw icon if not header
        {
            final Drawable icon = mIcons.getDrawable(position);
            //icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
            icon.setBounds(0, 0, 30, 30);
            holder.text.setCompoundDrawables(icon, null, null, null);
        }

        return convertView;
    }

    @Override
    public int getViewTypeCount()
    {
        return VIEW_TYPES_COUNT;
    }

    @Override
    public int getItemViewType(int position)
    {
        final Uri uri = Uri.parse(mUrls[position]);
        final String scheme = uri.getScheme();
        if (VIEW_TYPE_HEADER_STR.equals(scheme)) return VIEW_TYPE_HEADER;
        else
            return VIEW_TYPE_STATIONS;
    }

    @Override
    public boolean isEnabled(int position)
    {
        return getItemViewType(position) != VIEW_TYPE_HEADER;
    }

    private static class ViewHolder
    {
        TextView text;
    }
}
