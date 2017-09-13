package com.friends.zingradio.async;

public interface GetAlbumServerIdAsyncComplete
{
    public static final int ERR_CODE_RESPONSE_MALFORMED = -1;
    
    public void OnGetAlbumServerIdAsyncComplete(String result);
    public void OnGetAlbumServerIdAsyncError(int errCode, String errMsg);
}
