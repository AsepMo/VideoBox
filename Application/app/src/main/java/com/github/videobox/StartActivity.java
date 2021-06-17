package com.github.videobox;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.VideoView;
import android.widget.Toast;

import java.util.Locale;

import com.github.videobox.app.permissions.PermissionsManager;
import com.github.videobox.app.permissions.PermissionsResultAction;
import com.github.videobox.utils.VideoFolder;
import com.github.videobox.utils.SharedPref;

public class StartActivity extends Activity {

    private static final String TAG = StartActivity.class.getSimpleName();
    private VideoView mVideoView;
    private int position = 0;
    private SharedPref sharedPref;
    private Handler mHandler = new Handler(); 
    private Runnable mFirstTimeRunner = new Runnable()
    {
        @Override 
        public void run() {

            // Requesting all the permissions in the manifest
            PermissionsManager.getInstance().requestAllManifestPermissionsIfNecessary(StartActivity.this, new PermissionsResultAction() {
                    @Override
                    public void onGranted() {
                        VideoFolder.initFolder(new VideoFolder.Builder(getApplicationContext())
                                               .setDefaultFolder(VideoFolder.ZFOLDER)
                                               .setFolderApk(true) 
                                               .setFolderImage(true)
                                               .setFolderAudio(true)
                                               .setFolderAudio_Converted(true)
                                               .setFolderArchive(true)
                                               .setFolderEbook(true)
                                               .setFolderImage(true)
                                               .setFolderVideo(true)
                                               .setFolderVideoConverted(true)
                                               .setFolderYoutube_Analytics(true)
                                               .setFolderScriptMe(true)
                                               .build()); 
                        Intent intent = new Intent(StartActivity.this, VideoBoxActivity.class);
                        startActivity(intent);
                        StartActivity.this.finish();
                        //Toast.makeText(StartActivity.this, R.string.message_granted, Toast.LENGTH_SHORT).show();                   
                    }

                    @Override
                    public void onDenied(String permission) {
                        String message = String.format(Locale.getDefault(), getString(R.string.message_denied), permission);
                        Toast.makeText(StartActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
        }
	}; 

    private Runnable mSecondTimeRunner = new Runnable()
    {
        @Override 
        public void run() {

            // Requesting all the permissions in the manifest
            PermissionsManager.getInstance().requestAllManifestPermissionsIfNecessary(StartActivity.this, new PermissionsResultAction() {
                    @Override
                    public void onGranted() {

                        Intent intent = new Intent(StartActivity.this, VideoBoxActivity.class);
                        startActivity(intent);
                        StartActivity.this.finish();
                        //Toast.makeText(StartActivity.this, R.string.message_granted, Toast.LENGTH_SHORT).show();                   
                    }

                    @Override
                    public void onDenied(String permission) {
                        String message = String.format(Locale.getDefault(), getString(R.string.message_denied), permission);
                        Toast.makeText(StartActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
        }
    }; 


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_starter);
        sharedPref = new SharedPref(this);
        final boolean isFirtsTime = sharedPref.isFirstTimeLaunch();
        
        mVideoView = (VideoView)findViewById(R.id.video_loading);
        mVideoView.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.loading_sound_effects);
        mVideoView.requestFocus();
        mVideoView.setOnPreparedListener(new OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mVideoView.seekTo(position);
                    if (position == 0) {
                        mVideoView.start();              
                    } else {
                        mVideoView.pause();
                    }
                }
            });
        mVideoView.setOnCompletionListener(new OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mVideoView.pause();
                   if (isFirtsTime) {
                        mHandler.postDelayed(mFirstTimeRunner, 500); 
                    } else {
                        mHandler.postDelayed(mSecondTimeRunner, 500); 
                    }
                }
            });

        
        boolean hasPermission = PermissionsManager.getInstance().hasPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        Log.d(TAG, "Has " + Manifest.permission.READ_EXTERNAL_STORAGE + " permission: " + hasPermission);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(TAG, "Activity-onRequestPermissionsResult() PermissionsManager.notifyPermissionsChange()");
        PermissionsManager.getInstance().notifyPermissionsChange(permissions, grantResults);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("Position", mVideoView.getCurrentPosition());
        mVideoView.pause();
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        position = savedInstanceState.getInt("Position");
        mVideoView.seekTo(position);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mVideoView != null && mVideoView.isPlaying()) {
            mVideoView.pause();
        }
        sharedPref.setSeekTo(mVideoView.getCurrentPosition());
        mHandler.removeCallbacks(mFirstTimeRunner);
        mHandler.removeCallbacks(mSecondTimeRunner);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVideoView.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVideoView != null) {
            mVideoView.stopPlayback();
        }
        mHandler.removeCallbacks(mFirstTimeRunner);
        mHandler.removeCallbacks(mSecondTimeRunner);
    }

}
