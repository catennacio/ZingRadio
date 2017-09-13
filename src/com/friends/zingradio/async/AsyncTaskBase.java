package com.friends.zingradio.async;

import android.os.AsyncTask;

public class AsyncTaskBase extends AsyncTask<Void, Void, Void>
{
    protected int errCode;
    protected String errMsg;
    
    protected int getErrCode()
    {
        return errCode;
    }
    
    protected String getErrMsg()
    {
        return errMsg;
    }

    @Override
    protected Void doInBackground(Void... arg0)
    {
        return null;
    }

}
