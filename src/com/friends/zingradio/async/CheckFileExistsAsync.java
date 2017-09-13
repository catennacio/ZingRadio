package com.friends.zingradio.async;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.ref.WeakReference;

import com.friends.zingradio.R;
import com.friends.zingradio.util.download.FileDownloadManager;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

public class CheckFileExistsAsync extends AsyncTask<String, Integer, String>
{
    private CheckFileExistsComplete mListener; 
    private static File[] mFiles = null;
    private WeakReference<ImageView> mImageViewReference;
    public static final String TAG = CheckFileExistsAsync.class.getSimpleName();
    
    public CheckFileExistsAsync(CheckFileExistsComplete lis, File[] files, ImageView imageView)
    {
        mListener = lis;
        mFiles = files;
        mImageViewReference = new WeakReference<ImageView>(imageView);
    }
    
    @Override
    protected String doInBackground(String... params)
    {
        String ret = null;
        String songId = params[0];
        
        final String state = Environment.getExternalStorageState();
        if(mFiles == null)
        {
            return null;
        }
        if (Environment.MEDIA_MOUNTED.equals(state))
        {
            ret = checkFile(songId);
        }
        //Log.i(TAG, "id=" + songId + " ret=" + ret);        
        return ret;
    }

    @Override
    protected void onPostExecute(String result)
    {
        if(isCancelled())
        {
            result = null;
            return;
        }
        //Log.i(TAG, "onPostExecute() - result=" + result);
        if(mImageViewReference != null)
        {
            ImageView imageView = mImageViewReference.get();
            if(imageView != null)
            {
                String id = (String)imageView.getTag();
                if(id != null && id.equals(result))
                {
                    imageView.setImageResource(R.drawable.img_btn_download_16x16);
                }
                else
                {
                    imageView.setImageResource(0);
                }
                mListener.onCheckFileExistsComplete(result);
            }
        }
    }
    
    private static String checkFile(String songId)
    {
        String ret = null;
        for(File f : mFiles)
        {
            String filename = f.getName();
            String path = f.getAbsolutePath();
            //Log.d(TAG, "filename=" + filename);
            String[] fileNameTokens = filename.split("\\.(?=[^\\.]+$)");//split name and extension
            String name = fileNameTokens[0];
            //Log.d(TAG, "name=" + name);
            String[] nameTokens = name.split(FileDownloadManager.ID_SEPARATOR);//split name and id with separator (___)
            //Log.d(TAG, "nameTokens count=" + nameTokens.length);
            
            if(nameTokens.length >= 2)
            {
                String id = nameTokens[nameTokens.length -1];//get the last token as id, in case in filename there are separator too!
                //Log.d(TAG, "id=" + id);
                //try to split one more time in case there are many versions of the mp3
                String[] idTokens = id.split("-");
                String realId = "";
                if(idTokens.length == 2)//has many versions
                {
                    realId = idTokens[idTokens.length -1];//take the lastest version
                }
                else if(idTokens.length == 1)//only one version
                {
                    realId = id;
                }

                if(realId.equals(songId))
                {
                    //Log.i(TAG, "Found song realId=" + realId + " songID=" + songId + " name=" + filename);
                    ret = songId;
                    break;
                }
            }
        }
        
        return ret;
    }
    
}
