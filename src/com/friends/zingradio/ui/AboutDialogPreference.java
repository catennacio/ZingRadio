package com.friends.zingradio.ui;

import com.friends.zingradio.R;
import com.friends.zingradio.activity.SettingsActivity;
import com.friends.zingradio.util.Constants;
import com.friends.zingradio.util.Utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.preference.CheckBoxPreference;
import android.preference.DialogPreference;
import android.preference.PreferenceActivity;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class AboutDialogPreference extends DialogPreference
{
    public static final String TAG = AboutDialogPreference.class.getSimpleName(); 
    //private Context mContext;
    
    public AboutDialogPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        //mContext = context;
        setDialogLayoutResource(R.layout.about);
    }

    @Override
    protected void onBindDialogView(View view)
    {
        Log.d(TAG, "onBindDialogView()");
        super.onBindDialogView(view);
        
        String ver = null;
        try
        {
            PackageManager manager = getContext().getPackageManager();
            PackageInfo pInfo = manager.getPackageInfo(getContext().getPackageName(), 0);
            ver = pInfo.versionName;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        TextView tv = (TextView)view.findViewById(R.id.about_tv_tittle);
        tv.setText(getContext().getString(R.string.app_name) + " " + ver);
        
        String s = Utilities.getAboutHtml((SettingsActivity)getContext());
        //Log.d(TAG, "s=" + s);
        WebView wv = (WebView)view.findViewById(R.id.about_wv_body);
        wv.getSettings().setDefaultFontSize(15);
        wv.setWebViewClient(new WebViewClient()
        {
            @Override
            public boolean shouldOverrideUrlLoading(WebView wView, String url)
            {
                try
                {
                    Uri uri = Uri.parse(url);
                    String schema = uri.getScheme();
                    Intent i = null;
                    if(schema.equals("fb"))
                    {
                        i = Utilities.getOpenFacebookIntent(getContext());
                    }
                    else
                    {
                        i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    }
                    ((Activity)getContext()).startActivity(i);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
                return true;
            }
        });
        wv.loadData(s, "text/html", null);
        
        //String title = mContext.getString(R.string.app_name) + " " + ver;
        //aboutPref.setDialogTitle(title);
        //aboutPref.setDialogMessage(Html.fromHtml(s));
    }
}
