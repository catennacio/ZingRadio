package com.friends.zingradio.data;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.friends.zingradio.entity.AudioItem;
import com.friends.zingradio.entity.Station;
import com.friends.zingradio.entity.Station.Type;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

public class ZingRadioDatabaseHelper extends SQLiteOpenHelper
{
    private static String TAG = ZingRadioDatabaseHelper.class.getSimpleName();
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "ZingRadio.db";
    public static final String DATABASE_PATH = Environment.getDataDirectory() + "/data/YOUR_PACKAGE/databases/";
    
    public static final String TEXT_TYPE = " TEXT";
    public static final String COMMA_SEP = ",";

    private SQLiteDatabase mWritableDatabase;
    private SQLiteDatabase mReadableDatabase;
    private static Context mContext;
    
    private static ZingRadioDatabaseHelper mInstance = null;
    
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock r = rwl.readLock();
    private final Lock w = rwl.writeLock();
    
    public static ZingRadioDatabaseHelper getInstance(Context context)
    {
        //Log.d(TAG, "getInstance() - 1");
        if(mInstance == null)
        {
            //Log.d(TAG, "getInstance() - 2");
            mInstance = new ZingRadioDatabaseHelper(context);
            mContext = context;
        }
        
        //Log.d(TAG, "getInstance() - 3");
        return mInstance;
    }
    
    protected ZingRadioDatabaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        try
        {
            //Log.d(TAG, "ZingRadioDatabaseHelper() - 1");
            mWritableDatabase = getWritableDatabase();
            mReadableDatabase = getReadableDatabase();    
        }
        catch(Exception e)
        {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        //Log.d(TAG, "onCreate() - 1");
        //db.execSQL(MySongsTable.SQL_DELETE_TABLE);
        //db.execSQL(MyStationsTable.SQL_DELETE_TABLE);
        
        db.execSQL(MySongsTable.SQL_CREATE_TABLE);
        db.execSQL(MyStationsTable.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        if(oldVersion == 1 && newVersion == 2)
        {
            final String ALTER_MYCHANNELS_TABLE = "ALTER TABLE " + MyStationsTable.MyStationEntry.TABLE_NAME +
                    " ADD COLUMN " + MyStationsTable.MyStationEntry.COLUMN_NAME_STATION_TYPE + " " + ZingRadioDatabaseHelper.TEXT_TYPE;
            db.execSQL(ALTER_MYCHANNELS_TABLE);    
        }
    }

    /*
     * My songs table
     * 
     * **********************************************************************************************/
    
    public boolean isSongFavorite(String id)
    {
        boolean ret = false;
        r.lock();
        try
        {
            String selection = MySongsTable.MySongEntry.COLUMN_NAME_SONG_ID + "= ?";
            String[] selectionArgs = {id}; 
            Cursor c = mReadableDatabase.query(MySongsTable.MySongEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null);
            ret = c.getCount()>0 ? true : false;    
        }
        finally
        {
            r.unlock();
        }
        
        
        return ret;
    }
    
    public long toggleFavoriteSong(AudioItem ai)
    {
        long ret = -99;
        //Log.d(TAG, "toggleFavoriteSong() + Song=" + ai.getTitle());
        //String sql = "SELECT COUNT(*) FROM " + FavoriteEntryContract.FavoriteEntry.TABLE_NAME + 
        //        " WHERE " + FavoriteEntryContract.FavoriteEntry.COLUMN_NAME_SONG_ID + "='" + ai.getId() + "'";
        if(ai == null)
        {
            ret = -1;
        }
        else
        {
            if(!isSongFavorite(ai.getId()))
            {
                //Log.d(TAG, "Insert favorite.");
                ret = insertFavoriteSong(ai);
            }
            else
            {
                //Log.d(TAG, "Remove favorite.");
                deleteFavoriteSong(ai.getId());
            }    
        }
        
        return ret;
    }

    public long insertFavoriteSong(AudioItem ai)
    {
        //Log.d(TAG, "insertFavoriteSong() - 1");
        long newRowId;
        w.lock();
        try
        {
            ContentValues values = new ContentValues();
            values.put(MySongsTable.MySongEntry.COLUMN_NAME_SONG_ID, ai.getId());
            values.put(MySongsTable.MySongEntry.COLUMN_NAME_SONG_TITLE, ai.getTitle());
            values.put(MySongsTable.MySongEntry.COLUMN_NAME_SONG_ARTIST, ai.getPerformer());
            values.put(MySongsTable.MySongEntry.COLUMN_NAME_SONG_THUMBNAIL_URL, ai.getThumbnail());
            values.put(MySongsTable.MySongEntry.COLUMN_NAME_SONG_URL, ai.getSource());
            
            // Insert the new row, returning the primary key value of the new row
            
            newRowId = mWritableDatabase.insert(MySongsTable.MySongEntry.TABLE_NAME, null, values);    
        }
        finally
        {
            w.unlock();
        }
        
        //Log.d(TAG, "insertFavoriteStation() - 2 - newRowId=" + newRowId);
        return newRowId;
    }
    
    public void deleteFavoriteSong(String songId)
    {
        w.lock();
        try
        {
            String selection = MySongsTable.MySongEntry.COLUMN_NAME_SONG_ID + "=?";
            String[] selelectionArgs = { String.valueOf(songId) };
            mWritableDatabase.delete(MySongsTable.MySongEntry.TABLE_NAME, selection, selelectionArgs);    
        }
        finally
        {
            w.unlock();
        }
    }
    
    public ArrayList<AudioItem> getFavoriteSongs()
    {
        ArrayList<AudioItem> res = new ArrayList<AudioItem>();
        r.lock();
        try
        {
            String[] projection = {
                    MySongsTable.MySongEntry._ID,
                    MySongsTable.MySongEntry.COLUMN_NAME_SONG_ID,
                    MySongsTable.MySongEntry.COLUMN_NAME_SONG_TITLE,
                    MySongsTable.MySongEntry.COLUMN_NAME_SONG_ARTIST,
                    MySongsTable.MySongEntry.COLUMN_NAME_SONG_THUMBNAIL_URL,
                    MySongsTable.MySongEntry.COLUMN_NAME_SONG_URL,
                    };

            String sortOrder =
                    MySongsTable.MySongEntry.COLUMN_NAME_SONG_TITLE + " ASC";
            
            Cursor c = mReadableDatabase.query(
                    MySongsTable.MySongEntry.TABLE_NAME,  // The table to query
                    projection,                               // The columns to return
                    null,                                // The columns for the WHERE clause
                    null,                            // The values for the WHERE clause
                    null,                                     // don't group the rows
                    null,                                     // don't filter by row groups
                    sortOrder                                 // The sort order
                    );

            if(c.getCount() != 0)
            { 
                if (c.moveToFirst())
                {
                    do
                    {
                        AudioItem ai = new AudioItem();
                        ai.setId(c.getString(c.getColumnIndex(MySongsTable.MySongEntry.COLUMN_NAME_SONG_ID)));
                        ai.setTitle(c.getString(c.getColumnIndex(MySongsTable.MySongEntry.COLUMN_NAME_SONG_TITLE)));
                        ai.setPerformer(c.getString(c.getColumnIndex(MySongsTable.MySongEntry.COLUMN_NAME_SONG_ARTIST)));
                        ai.setThumbnail(c.getString(c.getColumnIndex(MySongsTable.MySongEntry.COLUMN_NAME_SONG_THUMBNAIL_URL)));
                        ai.setSource(c.getString(c.getColumnIndex(MySongsTable.MySongEntry.COLUMN_NAME_SONG_URL)));
                        
                        res.add(ai);
                    }
                    while(c.moveToNext());
                 }    
            }

            c.close();
        }
        finally
        {
            r.unlock();
        }
        
        return res;
    }
    
    /*
     * My Channels table
     * 
     * **********************************************************************************************/
    
    public boolean isStationFavorite(String id)
    {
        boolean ret = false;
        w.lock();
        
        try
        {
            String selection = MyStationsTable.MyStationEntry.COLUMN_NAME_STATION_ID + "= ?";
            String[] selectionArgs = {id}; 
            Cursor c = mReadableDatabase.query(MyStationsTable.MyStationEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null);
            ret = c.getCount() > 0 ? true : false;    
        }
        finally
        {
            w.unlock();
        }
        
        return ret;
    }
    
    public long toggleFavoriteStation(Station st)
    {
        long ret = -99;
        //Log.d(TAG, "toggleFavoriteStation() + Station=" + st.getName());
        
        if(!isStationFavorite(st.getId()))
        {
            //Log.d(TAG, "Insert favorite station.");
            ret = insertFavoriteStation(st);
        }
        else
        {
            //Log.d(TAG, "Remove favorite station.");
            deleteFavoriteStation(st.getId());
        }
        
        return ret;
    }
    
    public long insertFavoriteStation(Station st)
    {
      //Log.d(TAG, "insertFavoriteStation() - 1");
        long newRowId;
        w.lock();
        try
        {
            ContentValues values = new ContentValues();
            values.put(MyStationsTable.MyStationEntry.COLUMN_NAME_STATION_ID, st.getId());
            values.put(MyStationsTable.MyStationEntry.COLUMN_NAME_STATION_NAME, st.getName());
            values.put(MyStationsTable.MyStationEntry.COLUMN_NAME_STATION_CAT_ID, st.getCategoryId());
            values.put(MyStationsTable.MyStationEntry.COLUMN_NAME_STATION_CAT_NAME, st.getCategoryName());
            values.put(MyStationsTable.MyStationEntry.COLUMN_NAME_STATION_SERVERID, st.getServerId());
            values.put(MyStationsTable.MyStationEntry.COLUMN_NAME_STATION_URL, st.getUrl());
            values.put(MyStationsTable.MyStationEntry.COLUMN_NAME_STATION_TYPE, st.getType().name());
            
            // Insert the new row, returning the primary key value of the new row
            
            newRowId = mWritableDatabase.insert(MyStationsTable.MyStationEntry.TABLE_NAME, null, values);    
        }
        finally
        {
            w.unlock();
        }

        //Log.d(TAG, "insertFavoriteStation() - 2 - newRowId=" + newRowId);
        return newRowId;
    }
    
    public void deleteFavoriteStation(String id)
    {
        w.lock();
        
        try
        {
            String selection = MyStationsTable.MyStationEntry.COLUMN_NAME_STATION_ID + "=?";
            String[] selelectionArgs = { String.valueOf(id) };
            mWritableDatabase.delete(MyStationsTable.MyStationEntry.TABLE_NAME, selection, selelectionArgs);    
        }
        finally
        {
            w.unlock();
        }
    }
    
    public ArrayList<Station> getFavoriteStations()
    {
        ArrayList<Station> res = new ArrayList<Station>();
        r.lock();
        
        try
        {
            String[] projection = {
                    MyStationsTable.MyStationEntry._ID,
                    MyStationsTable.MyStationEntry.COLUMN_NAME_STATION_ID,
                    MyStationsTable.MyStationEntry.COLUMN_NAME_STATION_NAME,
                    MyStationsTable.MyStationEntry.COLUMN_NAME_STATION_CAT_ID,
                    MyStationsTable.MyStationEntry.COLUMN_NAME_STATION_CAT_NAME,
                    MyStationsTable.MyStationEntry.COLUMN_NAME_STATION_SERVERID,
                    MyStationsTable.MyStationEntry.COLUMN_NAME_STATION_URL,
                    MyStationsTable.MyStationEntry.COLUMN_NAME_STATION_TYPE,
                    };

            String sortOrder =
                    MyStationsTable.MyStationEntry.COLUMN_NAME_STATION_NAME + " ASC";
            
            Cursor c = mReadableDatabase.query(
                    MyStationsTable.MyStationEntry.TABLE_NAME,  // The table to query
                    projection,                               // The columns to return
                    null,                                // The columns for the WHERE clause
                    null,                            // The values for the WHERE clause
                    null,                                     // don't group the rows
                    null,                                     // don't filter by row groups
                    sortOrder                                 // The sort order
                    );

            if(c.getCount() != 0)
            { 
                if (c.moveToFirst())
                {
                    do
                    {
                        Station st = new Station();
                        st.setId(c.getString(c.getColumnIndex(MyStationsTable.MyStationEntry.COLUMN_NAME_STATION_ID)));
                        st.setName(c.getString(c.getColumnIndex(MyStationsTable.MyStationEntry.COLUMN_NAME_STATION_NAME)));
                        st.setCategoryId(c.getString(c.getColumnIndex(MyStationsTable.MyStationEntry.COLUMN_NAME_STATION_CAT_ID)));
                        st.setCategoryName(c.getString(c.getColumnIndex(MyStationsTable.MyStationEntry.COLUMN_NAME_STATION_CAT_NAME)));
                        st.setServerId(c.getString(c.getColumnIndex(MyStationsTable.MyStationEntry.COLUMN_NAME_STATION_SERVERID)));
                        st.setUrl(c.getString(c.getColumnIndex(MyStationsTable.MyStationEntry.COLUMN_NAME_STATION_URL)));
                        String strType = c.getString(c.getColumnIndex(MyStationsTable.MyStationEntry.COLUMN_NAME_STATION_TYPE));
                        if(strType == null || strType.isEmpty() || strType.equals(Station.Type.Radio.name()))
                        {
                            st.setType(Station.Type.Radio);
                        }
                        else
                        {
                            st.setType(Station.Type.Album);
                        }

                        res.add(st);
                        //Log.d(TAG, "name=" + st.getName() + " type=" + st.getType());
                    }
                    while(c.moveToNext());
                 }    
            }

            c.close();    
        }
        finally
        {
            r.unlock();
        }
        
        return res;
    }

}
