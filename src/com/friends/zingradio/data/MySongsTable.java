package com.friends.zingradio.data;

import java.util.ArrayList;

import com.friends.zingradio.entity.AudioItem;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

public class MySongsTable
{
    public static final String TAG = MySongsTable.class.getSimpleName();
    
    public static final String SQL_CREATE_TABLE = "CREATE TABLE " + MySongEntry.TABLE_NAME + " (" +
            MySongsTable.MySongEntry._ID + " INTEGER PRIMARY KEY," +
            MySongsTable.MySongEntry.COLUMN_NAME_SONG_ID + ZingRadioDatabaseHelper.TEXT_TYPE + ZingRadioDatabaseHelper.COMMA_SEP +
            MySongsTable.MySongEntry.COLUMN_NAME_SONG_TITLE + ZingRadioDatabaseHelper.TEXT_TYPE + ZingRadioDatabaseHelper.COMMA_SEP +
            MySongsTable.MySongEntry.COLUMN_NAME_SONG_ARTIST + ZingRadioDatabaseHelper.TEXT_TYPE + ZingRadioDatabaseHelper.COMMA_SEP +
            MySongsTable.MySongEntry.COLUMN_NAME_SONG_THUMBNAIL_URL + ZingRadioDatabaseHelper.TEXT_TYPE + ZingRadioDatabaseHelper.COMMA_SEP +
            MySongsTable.MySongEntry.COLUMN_NAME_SONG_URL + ZingRadioDatabaseHelper.TEXT_TYPE + " )";
    
    public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + MySongEntry.TABLE_NAME;
    
    private MySongsTable(){}
    
    public abstract class MySongEntry implements BaseColumns
    {
        public static final String TABLE_NAME = "MySongs";
        public static final String COLUMN_NAME_SONG_ID = "id";
        public static final String COLUMN_NAME_SONG_TITLE = "title";
        public static final String COLUMN_NAME_SONG_ARTIST = "artist";
        public static final String COLUMN_NAME_SONG_THUMBNAIL_URL = "thumbnail";
        public static final String COLUMN_NAME_SONG_URL = "url";
    }
}
