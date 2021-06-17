package com.github.videobox;

import android.support.annotation.Nullable;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.VideoView;
import android.widget.MediaController;
import android.widget.Toast;

import java.io.File;

import com.github.videobox.utils.VideoFolder;

public class VideoRecorderActivity extends Activity implements View.OnClickListener {


    public static void start(Context c) {
        Intent intent = new Intent(c, VideoRecorderActivity.class);
        c.startActivity(intent);
    }


    private static final String TAG = VideoRecorderActivity.class.getSimpleName();
    private static final int REQUEST_VIDEO_TRIMMER = 0x01;
    private static final String EXTRA_FILENAME= "com.github.videobox.EXTRA_FILENAME";
    private static final String FILENAME="VideoBox.mp4";
    public static final String EXTRA_VIDEO_PATH = "EXTRA_VIDEO_PATH";
    private File output = null;
    private FrameLayout mFlVideo;
    private VideoView mVideoView;
    private ImageView mIvThumb;
    private ImageView mIvPlay;

    public static void startActivity(Context context, String videoPath, String videoThumb) {
        Intent intent = new Intent(context, VideoRecorderActivity.class);
        intent.putExtra("path", videoPath);
        intent.putExtra("thumb", videoThumb);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_recorder);

        initView(savedInstanceState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(EXTRA_FILENAME, output);
    }
    
    private void initView(Bundle savedInstanceState) {
        Intent videoCapture = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        
        if (savedInstanceState == null) {
            File dir = new File(VideoFolder.ZFOLDER_VIDEO_RECORDER);

            dir.mkdirs();
            output = new File(dir, FILENAME);
        } else {
            output = (File)savedInstanceState.getSerializable(EXTRA_FILENAME);
        }

        if (output.exists()) {
            output.delete();
        }

        videoCapture.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(output));

        startActivityForResult(videoCapture, REQUEST_VIDEO_TRIMMER);

        mFlVideo = findViewById(R.id.fl_video);
        mVideoView = findViewById(R.id.view_video);
        mIvThumb = findViewById(R.id.iv_thumb);
        mIvPlay = findViewById(R.id.iv_play);
        mIvPlay.setOnClickListener(this);

        mVideoView.setMediaController(new MediaController(VideoRecorderActivity.this));
        
        mVideoView.setOnPreparedListener(new OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    ViewGroup.LayoutParams lp = mVideoView.getLayoutParams();
                    int videoWidth = mp.getVideoWidth();
                    int videoHeight = mp.getVideoHeight();
                    float videoProportion = (float) videoWidth / (float) videoHeight;
                    int screenWidth = mFlVideo.getWidth();
                    int screenHeight = mFlVideo.getHeight();
                    float screenProportion = (float) screenWidth / (float) screenHeight;
                    if (videoProportion > screenProportion) {
                        lp.width = screenWidth;
                        lp.height = (int) ((float) screenWidth / videoProportion);
                    } else {
                        lp.width = (int) (videoProportion * (float) screenHeight);
                        lp.height = screenHeight;
                    }
                    mVideoView.setLayoutParams(lp);

                    Log.d(TAG, "videoWidth:" + videoWidth + ", videoHeight:" + videoHeight);
                }
            });
        mVideoView.setOnCompletionListener(new OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mIvPlay.setVisibility(View.VISIBLE);
                    mIvThumb.setVisibility(View.VISIBLE);                    
                }
            });
        videoStart();
    }

    private void videoStart() {
        mVideoView.start();
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_play:
                mIvThumb.setVisibility(View.GONE);
                mIvPlay.setVisibility(View.GONE);
                videoStart();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_VIDEO_TRIMMER) {
                /*final Uri selectedUri = data.getData();
                if (selectedUri != null) {
                    
                    mVideoView.setVideoURI(selectedUri);
                } else {
                    Toast.makeText(VideoRecorderActivity.this, "Videonya Tidak Bisa Dibuka", Toast.LENGTH_SHORT).show();
                }*/
                mVideoView.setVideoPath(output.getAbsolutePath());
            }
        }
    }
}
