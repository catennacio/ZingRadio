package com.friends.zingradio.adapter;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import com.friends.zingradio.R;
import com.friends.zingradio.async.CheckFileExistsAsync;
import com.friends.zingradio.async.CheckFileExistsComplete;
import com.friends.zingradio.entity.AudioItem;
import com.friends.zingradio.fragment.FavoriteSongListFragment;
import com.friends.zingradio.media.MusicService;
import com.friends.zingradio.media.MusicServiceEventListener;
import com.friends.zingradio.util.Utilities;
import com.friends.zingradio.util.download.FileDownloadManager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.provider.MediaStore.Files;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SongAdapter extends BaseAdapter implements CheckFileExistsComplete
{
    public static final String TAG = SongAdapter.class.getSimpleName();
    public static final int INDICATOR_PLAY = 0;
    public static final int INDICATOR_DOWNLOAD = 1;
    private final LayoutInflater mInflater;

    private static FilenameFilter af = new FilenameFilter()
    {
        public boolean accept(File dir, String name)
        {
            if (name.endsWith(".mp3") || name.endsWith(".MP3"))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
    };
    
    private static File rootFile = new File(Environment.getExternalStorageDirectory(), FileDownloadManager.DOWNLOAD_DIR);
    private static File[] files = (rootFile).listFiles(af);

    public AudioItem mPlayingItem;

    private ArrayList<AudioItem> mSongs;
    public ArrayList<AudioItem> getSongs()
    {
        return mSongs;
    }

    public void setSongs(ArrayList<AudioItem> items)
    {
        this.mSongs = items;
    }

    private Context mContext;
    
    public SongAdapter(Context context)
    {
        mInflater = LayoutInflater.from(context);
        mContext = context;
    }
    
    public SongAdapter(Context context, ArrayList<AudioItem> items)
    {
        mInflater = LayoutInflater.from(context);
        mContext = context;
        mSongs = items;
        //Log.d(TAG,"SongAdapter() - song count=" + mSongs.size());
    }
    
    @Override
    public int getCount()
    {
        return mSongs.size();
    }

    @Override
    public AudioItem getItem(int position)
    {
        return mSongs.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent)
    {
        if(mSongs == null || mSongs.size() == 0) return view;
        final ViewHolder holder;
        //Log.d(TAG,"getView() - song count=" + mSongs.size());
        
        if(view == null)
        {
            //LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = mInflater.inflate(R.layout.song_list_item, parent, false);
            
            holder = new ViewHolder();
            holder.songname = (TextView) view.findViewById(R.id.song_list_item_song_name);
            holder.artistname = (TextView) view.findViewById(R.id.song_list_item_song_artist);
            holder.indicator = (ImageView) view.findViewById(R.id.song_list_item_download);
            
            view.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)view.getTag();
        }
        
        AudioItem ai = mSongs.get(position);
        holder.songname.setText(ai.getTitle());
        holder.artistname.setText(ai.getPerformer());
        
        /*
        Drawable currentDrawable = holder.indicator.getDrawable();
        if (currentDrawable instanceof BitmapDrawable)
        {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) currentDrawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            bitmap.recycle();
        }
         */
        
        if(mPlayingItem != null && ai.getId().equals(mPlayingItem.getId()))
        {
            holder.indicator.setImageResource(R.drawable.img_btn_play_16x16);
            holder.songname.setTextColor(mContext.getResources().getColor(R.color.holo));
            holder.artistname.setTextColor(mContext.getResources().getColor(R.color.holo));
        }
        else
        {
           //scan download directory for song, if available, set icon download for row
            holder.songname.setTextColor(mContext.getResources().getColor(R.color.mainon_color));
            holder.artistname.setTextColor(mContext.getResources().getColor(R.color.mainon_color));
            //holder.indicator.setImageResource(0);
            holder.indicator.setTag(ai.getId());
            new CheckFileExistsAsync(this, files, holder.indicator).execute(ai.getId());
            /*
            String filepath = Utilities.checkMp3FileExists(ai.getId(), files);
            if(filepath != null)
            {
                holder.indicator.setImageResource(R.drawable.img_btn_download_16x16);
                ai.setSource(filepath);
            }
            else
            {
                holder.indicator.setImageDrawable(null);
            }
            */
        }
        
        return view;
    }
    
    @Override
    public void onCheckFileExistsComplete(String foundPath)
    {        
    }
    
    private static class ViewHolder
    {
        TextView songname;
        TextView artistname;
        ImageView indicator;
    }
}
