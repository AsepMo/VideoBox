package com.github.videobox;
 
import android.support.annotation.Nullable;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Context;
import android.content.Intent;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.VideoView;
import android.widget.TextView;
import android.widget.MediaController;

import com.bumptech.glide.Glide;

public class VideoPreviewActivity extends Activity {

    private static final String TAG = VideoPreviewActivity.class.getSimpleName();

    private LinearLayout mVideoLoading;
    private VideoView mVideoView;
    
    private int position = 0;   
    private String mVideoPath;
    
    public static void startActivity(Context context, String videoPath, String videoThumb) {
        Intent intent = new Intent(context, VideoPreviewActivity.class);
        intent.putExtra("path", videoPath);
        //intent.putExtra("thumb", videoThumb);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setTheme(R.style.AppTheme);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_view);

        initView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //videoStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoDestroy();
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
    public void onBackPressed() {
        super.onBackPressed();
        videoDestroy();
        finish();
    }

    private void initView() {
        mVideoPath = getIntent().getStringExtra("path");
        //mVideoThumb = getIntent().getStringExtra("thumb");

        mVideoView = (VideoView)findViewById(R.id.view_video);
        
        mVideoView.setMediaController(new MediaController(VideoPreviewActivity.this));
        mVideoView.setVideoPath(mVideoPath);
        mVideoView.setOnPreparedListener(new OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mVideoLoading.setVisibility(View.GONE);
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
                    videoPause();
                    mVideoView.seekTo(0);                 
                }
            });
    }

    private void videoPause() {
        if (mVideoView != null && mVideoView.isPlaying()) {
            mVideoView.pause();
        }
    }

    private void videoDestroy() {
        if (mVideoView != null) {
            mVideoView.stopPlayback();
        }
    }
}
