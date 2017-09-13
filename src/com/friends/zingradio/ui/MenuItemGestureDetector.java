package com.friends.zingradio.ui;

import com.friends.zingradio.activity.MainActivity;
import com.friends.zingradio.fragment.LeftMenuFragment;
import com.friends.zingradio.fragment.PlayerFragment;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

public class MenuItemGestureDetector extends SimpleOnGestureListener
{
    public final String TAG = MenuItemGestureDetector.class.getSimpleName();
    private static final int SWIPE_MIN_DISTANCE = 100;
    private static final int SWIPE_MAX_OFF_PATH = 400;
    private static final int SWIPE_THRESHOLD_VELOCITY = 100;

    private Context mContext;
    
    public MenuItemGestureDetector(Context ctx)
    {
        mContext = ctx;
    }
    
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
    {
        try
        {
          //return true to tell the caller we already handle the event. This is to overwrite the list item not to handle the swipe to cause acidentially onlick on the list ime
            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) return false;
            // right to left swipe
            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)
            {
                MainActivity ma = (MainActivity) mContext;
                ma.updateContent(PlayerFragment.URI);
            }
            else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)
            {
                MainActivity ma = (MainActivity) mContext;
                ma.updateLeftFrame(LeftMenuFragment.URI, null);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
        return false;
    }
}