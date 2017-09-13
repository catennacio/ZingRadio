package com.friends.zingradio.util.download;

import android.app.DownloadManager;

public abstract class DownloadManagerBase
{
    protected DownloadManager mDownloadManager = null;
    protected long mLastDownload = -1L;

    public DownloadManagerBase()
    {

    }
}
