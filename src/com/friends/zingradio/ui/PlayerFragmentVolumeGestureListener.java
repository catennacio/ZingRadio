package com.friends.zingradio.ui;

import com.friends.zingradio.fragment.PlayerFragment;
import com.friends.zingradio.ui.verticalseekbar.VerticalSeekBar;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.widget.Scroller;

public class PlayerFragmentVolumeGestureListener extends SimpleOnGestureListener
{
    public static final String TAG = PlayerFragmentVolumeGestureListener.class.getSimpleName();

    private static final int SWIPE_MIN_DISTANCE = 20;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    private VerticalSeekBar mVolumnSeekBar;
    private PlayerFragment mPlayerFragment;

    private PlayerFragmentVolumeGestureListener(){}
    
    public PlayerFragmentVolumeGestureListener(PlayerFragment f, VerticalSeekBar seekbar)
    {
        mVolumnSeekBar = seekbar;
        mPlayerFragment = f;
    }

    @Override
    public boolean onDown(MotionEvent event)
    {
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
    {
        //if (Math.abs(distanceY) > 1)
        {
            // Log.d(TAG,"e1getX=" + e1getX + " e1getY=" + e1getY + " e2getX=" +
            // e2getX + " e2getY=" + e2getY);
            float e1getY = e1.getY();
            float e2getY = e2.getY();
            float distance = e2getY - e1getY;
            int distanceInt = Math.abs((int) distance);// /
                                                       // SWIPE_PIXEL_PER_UNIT;//distance
                                                       // of first and last
                                                       // point
                                                       // when swipe

            int height = mVolumnSeekBar.getHeight();// pixel
            int pixelPerVolUnit = height / mVolumnSeekBar.getMax();

            int volUnitIncreaseOrDecrease = Math.abs(distanceInt / pixelPerVolUnit);
            int currentProgress = mVolumnSeekBar.getProgress();
            //Log.d(TAG, "distanceInt=" + distanceInt + "\ncurrent progress=" + currentProgress + "\npixelPerVolUnit=" + pixelPerVolUnit + "\nvolUnitIncreaseOrDecrease=" + volUnitIncreaseOrDecrease);

            int newProgress = mVolumnSeekBar.getProgress();

            if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE)// scroll up
            {
                //newProgress = currentProgress + volUnitIncreaseOrDecrease;
                newProgress = currentProgress + 1;
            }
            else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE)// scroll down
            {
                //newProgress = currentProgress - volUnitIncreaseOrDecrease;
                newProgress = currentProgress - 1;
            }

            if (newProgress > mVolumnSeekBar.getMax()) newProgress = mVolumnSeekBar.getMax();
            if (newProgress < 0) newProgress = 0; 
            mPlayerFragment.setVolume(newProgress);
            
            
            try
            {
                Thread.sleep(25);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
    {
        /*
         * if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY)
         * > SWIPE_THRESHOLD_VELOCITY) { Log.d("gesture", "scroll up"); return
         * true; // Bottom to top } else if (e2.getY() - e1.getY() >
         * SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY)
         * { Log.d("gesture", "scroll down"); return true; // Top to bottom }
         */
        return true;
    }
}