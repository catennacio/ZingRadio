package com.friends.zingradio.media;

import com.friends.zingradio.IObservarable;
import com.friends.zingradio.entity.AudioItem;

public interface IMusicServiceObservarable extends IObservarable
{
    public void notifyOnNext();
    public void notifyOnPlay(AudioItem ai, boolean updateBitmap);
    public void notifyOnStop();
    public void notifyOnPause();
    public void notifyOnError(int errCode);
}
