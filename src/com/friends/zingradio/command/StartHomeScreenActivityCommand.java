package com.friends.zingradio.command;

import android.content.Context;
import android.content.Intent;

public class StartHomeScreenActivityCommand implements ICommand
{

    Context mContext;
    
    public StartHomeScreenActivityCommand(Context ctx)
    {
        mContext = ctx;
    }

    @Override
    public void execute()
    {
        Intent setIntent = new Intent(Intent.ACTION_MAIN);
        setIntent.addCategory(Intent.CATEGORY_HOME);
        setIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mContext.startActivity(setIntent);
    }

}
