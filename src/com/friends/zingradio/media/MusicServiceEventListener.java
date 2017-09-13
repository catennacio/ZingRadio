package com.friends.zingradio.media;

import com.friends.zingradio.entity.AudioItem;

public interface MusicServiceEventListener
{
    public void onServiceConnected(MusicService service);
    public void onServiceDisconnected();
    public void onPlay(AudioItem audioItem, boolean updateBitmap);
    public void onPause(AudioItem audioItem);
    public void onNext(AudioItem nextAudioItem);
    public void onPrev(AudioItem nextAudioItem);
    public void onStop(AudioItem lastAudioItem);
    public void onError(int errCode);
}
