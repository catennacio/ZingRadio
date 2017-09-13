package com.friends.zingradio.activity;

import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.friends.zingradio.R;
import com.friends.zingradio.fragment.LeftMenuFragment;
import com.friends.zingradio.fragment.RightMenuFragment;
import com.google.analytics.tracking.android.EasyTracker;
import com.slidingmenu.lib.*;
import com.slidingmenu.lib.SlidingMenu.OnOpenListener;
import com.slidingmenu.lib.app.SlidingFragmentActivity;

public class BaseActivity extends SlidingFragmentActivity
{
    private static final String TAG = BaseActivity.class.getSimpleName();
    protected ActionBar mActionBar;
    protected SlidingMenu mSlidingMenu;
    protected LeftMenuFragment mLeftMenuFragment;
    protected RightMenuFragment mRightMenuFragment;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setBehindContentView(R.layout.menu_frame_left);
        
        if(mLeftMenuFragment != null) mLeftMenuFragment.onDestroy();
        mLeftMenuFragment = new LeftMenuFragment();
        getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.menu_frame_left, mLeftMenuFragment, LeftMenuFragment.TAG)
        .commit();

        if(mRightMenuFragment != null) mRightMenuFragment.onDestroy();
        mRightMenuFragment = new RightMenuFragment();
        this.getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.menu_frame_right, mRightMenuFragment, RightMenuFragment.TAG)
        .commit();

        /*
        if (savedInstanceState == null)
        {
            mLeftMenuFragment = new LeftMenuFragment();
            getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.menu_frame_left, mLeftMenuFragment, LeftMenuFragment.TAG)
            .commit();

            mRightMenuFragment = new RightMenuFragment();
            this.getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.menu_frame_right, mRightMenuFragment, RightMenuFragment.TAG)
            .commit();
        }
        else
        {
            try
            {
                //mLeftMenuFragment = (LeftMenuFragment) this.getSupportFragmentManager().findFragmentById(R.id.menu_frame_left);    
            }
            catch(ClassCastException e)
            {
                //Log.e(TAG, e.getMessage());
            }
        }
        */

        mSlidingMenu = this.getSlidingMenu();
        mSlidingMenu.setFadeEnabled(true);
        mSlidingMenu.setFadeDegree(0.7f);
        mSlidingMenu.setSelectorEnabled(true);
        mSlidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        mSlidingMenu.setMode(SlidingMenu.LEFT_RIGHT);
        mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        mSlidingMenu.setSecondaryMenu(R.layout.menu_frame_right);
        mSlidingMenu.setSecondaryShadowDrawable(R.drawable.shadowright);
        mSlidingMenu.setShadowDrawable(R.drawable.shadowleft);
        mSlidingMenu.setShadowWidthRes(R.dimen.shadow_width);

        mSlidingMenu.setOnOpenListener(new OnOpenListener()
        {
            @Override
            public void onOpen()
            {
                mSlidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
            }
        });

        mActionBar = getSupportActionBar();
        //mActionBar.setLogo(R.drawable.button_channellist);
        //mActionBar.setIcon(R.drawable.button_channellist);
        mActionBar.setDisplayHomeAsUpEnabled(false);
        mActionBar.setHomeButtonEnabled(true);
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case android.R.id.home:
            {
                mSlidingMenu.showMenu(true);
                return true;
            }
            default:
            {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        EasyTracker.getInstance().activityStart(this);
    }
    
    @Override
    protected void onStop()
    {
        super.onStop();
        EasyTracker.getInstance().activityStop(this);
    }
}
