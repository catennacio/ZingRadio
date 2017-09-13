package com.friends.zingradio.async;

public interface GetStationServerIdAsyncComplete
{
    public static final int ERR_CODE_RESPONSE_MALFORMED = -1;
    
    public void onGetStationServerIdComplete(String serverId);
    public void onGetStationServerIdError(int errCode, String errMsg);
}
