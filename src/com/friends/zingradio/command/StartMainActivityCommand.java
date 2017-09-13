package com.friends.zingradio.command;

import com.friends.zingradio.R;
import com.friends.zingradio.activity.MainActivity;
import com.friends.zingradio.activity.SplashScreenActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public class StartMainActivityCommand extends BaseCommand implements ICommand
{
    Context mContext;

    public StartMainActivityCommand(Context ctx)
    {
        mContext = ctx;
    }

    @Override
    public void execute()
    {
        Intent mainIntent = new Intent(mContext, MainActivity.class);
        mContext.startActivity(mainIntent);
        ((Activity) mContext).finish();
        ((Activity) mContext).overridePendingTransition(R.anim.mainfadein, R.anim.splashfadeout);
    }
    
    public void execute(boolean fade)
    {
        if(fade) execute();
        else
        {
            Intent mainIntent = new Intent(mContext, MainActivity.class);
            mContext.startActivity(mainIntent);
        }
    }
}
