package com.friends.zingradio.util.download;

import java.util.Hashtable;

import com.friends.zingradio.R;
import com.friends.zingradio.entity.AudioItem;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

public class FileDownloadManager extends DownloadManagerBase
{
    public static final String TAG = FileDownloadManager.class.getSimpleName();
    public static final String DOWNLOAD_DIR = "ZingRadio";
    public static final String ID_SEPARATOR = "_";
    //public static final String DOWNLOAD_DIR = Environment.DIRECTORY_DOWNLOADS;
    
    private IFileDownloadManagerListener mListener;
    private AudioItem mAudioItem;
    private static FileDownloadManager mInstance = null;
    private Hashtable<Long, AudioItem> mDownloadTable = new Hashtable<Long, AudioItem>();
    
    private static FileDownloadManager getInstance(IFileDownloadManagerListener lis)
    {
        if(mInstance == null)
        {
            mInstance = new FileDownloadManager(lis);
        }
        return mInstance;
    }

    public FileDownloadManager(IFileDownloadManagerListener lis)
    {
        mListener = lis;
        mDownloadManager = (DownloadManager)((Fragment)mListener).getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
    }
    
    public void init()
    {
        ((Fragment)mListener).getActivity().registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        ((Fragment)mListener).getActivity().registerReceiver(onNotificationClick, new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));
    }

    public void close()
    {
        ((Fragment)mListener).getActivity().unregisterReceiver(onComplete);
        ((Fragment)mListener).getActivity().unregisterReceiver(onNotificationClick);
    }
    
    public long startDownload(AudioItem ai)
    //public long startDownload(String url, String notificationTitle, String notificationSubTitle, String saveDirectory, String filenameToBeSaved)
    {
        try
        {
            if(ai == null) return mLastDownload;
            mAudioItem = ai;
            
            //String dir = Environment.DIRECTORY_DOWNLOADS;
            //Log.d(TAG, "download dir=" + dir);
            
            String link = mAudioItem.getDownloadLink();
            //Log.d(TAG, "link=" + link);
            
            //real url to download file
            String[] linkTokens = link.split("\\?");                
            //Log.d(TAG, "tokens[0]="  + linkTokens[0]);
            //Log.d(TAG, "tokens[1]="  + linkTokens[1]);
            String url = linkTokens[0];
            //Log.d(TAG, "download link=" + url);

            //process the filename to be saved
            Uri filenameUri = Uri.parse(link);                
            String filename = filenameUri.getQueryParameter("filename");
            String[] tokens = filename.split("\\.(?=[^\\.]+$)");
            String fn1 = tokens[0] + ID_SEPARATOR + "Zing"+ ID_SEPARATOR + mAudioItem.getId();
            String fn2 = fn1 + "." + tokens[1];
            
            //Log.d(TAG, "url=" + url);
            //Log.d(TAG, "filename=" + fn2);

            Uri uri = Uri.parse(url);

            Environment.getExternalStoragePublicDirectory(DOWNLOAD_DIR).mkdirs();

            mLastDownload = mDownloadManager.enqueue(new DownloadManager.Request(uri)
                            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                            .setAllowedOverRoaming(false)
                            .setTitle(mAudioItem.getTitle() + " - " + mAudioItem.getPerformer()).setDescription("Downloading...")
                            .setDestinationInExternalPublicDir(DOWNLOAD_DIR, fn2));

            //Log.d(TAG, "mLastDownload=" + mLastDownload);
            
            mDownloadTable.put(mLastDownload, ai);
        }
        catch(IllegalStateException e)
        {
            Log.e(TAG, e.getMessage());
            Toast.makeText(((Fragment)mListener).getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            
        }
        catch(Exception e)
        {
            e.printStackTrace();
            //Log.e(TAG, e.getMessage());
            Toast.makeText(((Fragment)mListener).getActivity(), ((Fragment)mListener).getActivity().getString(R.string.msg_err_song_general), Toast.LENGTH_SHORT).show();
        }
        
        return mLastDownload;
    }

    public String getStatus(long downloadId)
    {
        Cursor c = mDownloadManager.query(new DownloadManager.Query().setFilterById(downloadId));
        String s = "";
        if (c == null)
        {
            s =  null;
        }
        else if (c.getCount() == 0)
        {
            s = "Cursor is 0";
        }
        else
        {
            c.moveToFirst();
            /*
            Log.d(getClass().getName(), "COLUMN_ID: " + c.getLong(c.getColumnIndex(DownloadManager.COLUMN_ID)));
            Log.d(getClass().getName(), "COLUMN_BYTES_DOWNLOADED_SO_FAR: " + c.getLong(c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)));
            Log.d(getClass().getName(), "COLUMN_LAST_MODIFIED_TIMESTAMP: " + c.getLong(c.getColumnIndex(DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP)));
            Log.d(getClass().getName(), "COLUMN_LOCAL_URI: " + c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)));
            Log.d(getClass().getName(), "COLUMN_STATUS: " + c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS)));
            Log.d(getClass().getName(), "COLUMN_REASON: " + c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON)));
             */
            s = statusMessage(c);
        }
        
        if(c != null)
        {
            c.close();
        }

        return s;
    }
    
    public int getStatusCode(long downloadId)
    {
        Cursor c = mDownloadManager.query(new DownloadManager.Query().setFilterById(downloadId));
        int s = DownloadManager.STATUS_FAILED;
        if(c != null && c.getCount() > 0)
        {
            c.moveToFirst();
            s = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
            c.close();
        }

        return s;
    }
    
    private String statusMessage(Cursor c)
    {
        String msg = "???";

        switch (c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS)))
        {
            case DownloadManager.STATUS_FAILED:
                msg = "Download failed! Please retry!";
                break;

            case DownloadManager.STATUS_PAUSED:
                msg = "Download paused!";
                break;

            case DownloadManager.STATUS_PENDING:
                msg = "Download pending!";
                break;

            case DownloadManager.STATUS_RUNNING:
                msg = "Download in progress!";
                break;

            case DownloadManager.STATUS_SUCCESSFUL:
                msg = "Download complete!";
                break;

            default:
                msg = "Download is nowhere in sight";
                break;
        }

        return (msg);
    }
    
    protected BroadcastReceiver onComplete = new BroadcastReceiver()
    {
        public void onReceive(Context ctxt, Intent intent)
        {
            String action = intent.getAction();

            long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
            AudioItem ai = mDownloadTable.get(downloadId);
            mDownloadTable.remove(downloadId);
            mListener.OnDownloadComplete(ai, downloadId);
        }
    };

    protected BroadcastReceiver onNotificationClick = new BroadcastReceiver()
    {
        public void onReceive(Context ctxt, Intent intent)
        {
            long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
            AudioItem ai = mDownloadTable.get(downloadId);
            mListener.OnDownloadNotificationClick(ai, downloadId);
        }
    };
}
