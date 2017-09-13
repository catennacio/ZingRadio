package com.friends.zingradio.util.download;

import com.friends.zingradio.entity.AudioItem;

public interface IFileDownloadManagerListener
{
    public void OnDownloadComplete(AudioItem ai, long downloadId);
    public void OnDownloadNotificationClick(AudioItem ai, long downloadId);
}
