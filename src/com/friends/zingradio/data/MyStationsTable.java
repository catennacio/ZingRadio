package com.friends.zingradio.data;

import com.friends.zingradio.data.MySongsTable.MySongEntry;

import android.provider.BaseColumns;

public class MyStationsTable
{
    public static final String SQL_CREATE_TABLE = "CREATE TABLE " + MyStationEntry.TABLE_NAME + " (" +
            MyStationEntry._ID + " INTEGER PRIMARY KEY," +
            MyStationEntry.COLUMN_NAME_STATION_ID + ZingRadioDatabaseHelper.TEXT_TYPE + ZingRadioDatabaseHelper.COMMA_SEP +
            MyStationEntry.COLUMN_NAME_STATION_NAME + ZingRadioDatabaseHelper.TEXT_TYPE + ZingRadioDatabaseHelper.COMMA_SEP +
            MyStationEntry.COLUMN_NAME_STATION_CAT_ID + " INTEGER" + ZingRadioDatabaseHelper.COMMA_SEP +
            MyStationEntry.COLUMN_NAME_STATION_CAT_NAME + ZingRadioDatabaseHelper.TEXT_TYPE + ZingRadioDatabaseHelper.COMMA_SEP +
            MyStationEntry.COLUMN_NAME_STATION_SERVERID + ZingRadioDatabaseHelper.TEXT_TYPE + ZingRadioDatabaseHelper.COMMA_SEP +
            MyStationEntry.COLUMN_NAME_STATION_URL + ZingRadioDatabaseHelper.TEXT_TYPE + ZingRadioDatabaseHelper.COMMA_SEP +
            MyStationEntry.COLUMN_NAME_STATION_TYPE + ZingRadioDatabaseHelper.TEXT_TYPE + " )";
    
    public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + MyStationEntry.TABLE_NAME;

    public abstract class MyStationEntry implements BaseColumns
    {
        
        public static final String TABLE_NAME = "MyChannels";
        public static final String COLUMN_NAME_STATION_ID = "id";
        public static final String COLUMN_NAME_STATION_NAME = "name";
        public static final String COLUMN_NAME_STATION_CAT_ID = "category_id";
        public static final String COLUMN_NAME_STATION_CAT_NAME = "category_name";
        public static final String COLUMN_NAME_STATION_SERVERID = "server_id";
        public static final String COLUMN_NAME_STATION_URL = "url";
        public static final String COLUMN_NAME_STATION_TYPE = "type";
    }
}
