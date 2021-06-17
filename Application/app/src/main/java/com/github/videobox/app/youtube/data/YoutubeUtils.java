package com.github.videobox.app.youtube.data;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.Context;
import android.os.Process;
import android.os.Vibrator;
import android.util.Log;
import android.util.SparseArray;
import android.net.Uri;
import android.webkit.WebView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;
import java.util.List;
import java.lang.reflect.Method;


import com.github.videobox.app.youtube.data.YoutubeVideo;
import com.github.videobox.app.youtube.data.YoutubeVideoDb;
import com.github.videobox.app.youtube.config.YoutubeConfig;
import com.github.videobox.YouTubePreviewActivity;
import com.github.videobox.utils.VideoFolder;

public class YoutubeUtils implements Serializable
{
	private Context mContext;
	private YoutubeVideoDb mDatabase;
	private Toast mToast;
    private static String TAG = YoutubeUtils.class.getSimpleName();
    
	public YoutubeUtils(Context context)
	{
		this.mContext = context;
		mDatabase = new YoutubeVideoDb(mContext);
	}
    
	public static void startActivity(Context c, Intent mClass)
	{
		Intent mApplication = new Intent(mClass);
		c.startActivity(mApplication);
	}

	public static void startActivity(Context c, Class<?> mClass)
	{
		Intent mApplication = new Intent(c, mClass);
		c.startActivity(mApplication);
	}

	public static void startPlayerActivity(Context c)
    {
        Intent mPlayer = new Intent(c, YouTubePreviewActivity.class);
        mPlayer.putExtra(YouTubePreviewActivity.TAG_URL, YoutubeData.getYouTubePlaying());
        c.startActivity(mPlayer);
	}
    
	public void setAddPlaylist(YoutubeData data)
	{
		YoutubeVideoDb mDatabase = new YoutubeVideoDb(mContext);
		try {
			mDatabase.addPlaylist(data);
		} catch (Exception e){
			android.util.Log.e(TAG, "exception", e);
		}
	}
	
	public static void setAddPlaylist(Context context, YoutubeData data)
	{
		YoutubeVideoDb mDatabase = new YoutubeVideoDb(context);
		try {
			mDatabase.addPlaylist(data);
		} catch (Exception e){
			android.util.Log.e(TAG, "exception", e);
		}
	}
	
	public void sendShortMessage(String message)
	{
        if (mToast != null)
		{
            mToast.cancel();
        }

        mToast = Toast.makeText(mContext, message, Toast.LENGTH_SHORT);
        mToast.show();
    }

    public void sendLongMessage(String message)
	{
        if (mToast != null)
		{
            mToast.cancel();
        }

        mToast = Toast.makeText(mContext, message, Toast.LENGTH_LONG);
        mToast.show();
    }

	public void setVibrator(long time)
	{
		final Vibrator mVibrator = (Vibrator)mContext.getSystemService(Context.VIBRATOR_SERVICE);
		mVibrator.vibrate(time);
	}

	
}

