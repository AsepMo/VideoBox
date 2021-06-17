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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.VideoView;
import android.widget.TextView;
import android.widget.MediaController;

import com.bumptech.glide.Glide;

public class VideoPlayerActivity extends Activity {

    private static final String TAG = VideoPlayerActivity.class.getSimpleName();

    private LinearLayout mVideoLoading;
    private VideoView mVideoView;
    private TextView mTextLine1;
    private TextView mTextLine2;
    private TextView mLoadingText;
    private int position = 0;   
    private String mVideoPath;
    private String mVideoThumb;

    private Uri mUri;
    private long mMediaId = -1;
    public static void startActivity(Context context, String videoPath, String videoThumb) {
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        intent.putExtra("path", videoPath);
        intent.putExtra("thumb", videoThumb);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setTheme(R.style.AppTheme);
        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }
        mUri = intent.getData();
        if (mUri == null) {
            finish();
            return;
        }
        String scheme = mUri.getScheme();


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_preview);

        initView(scheme);
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

    public boolean isConnect() {
        ConnectivityManager cm =(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
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

    private void initView(String scheme) {
        // mVideoPath = getIntent().getStringExtra("path");
        //mVideoThumb = getIntent().getStringExtra("thumb");

        mVideoLoading = (LinearLayout)findViewById(R.id.layoutMessage);
        
        mLoadingText = (TextView) findViewById(R.id.tvMessage);
        mVideoView = (VideoView)findViewById(R.id.view_video);
        if (scheme.equals("http")) {
            String msg = getString(R.string.streamloadingtext, mUri.getHost());
            mLoadingText.setText(msg);
            mVideoLoading.setVisibility(View.VISIBLE);
            if (!isConnect()) {
                new AlertDialog.Builder(VideoPlayerActivity.this)
                    .setTitle("Tidak Ada Koneksi Internet!")
                    .setMessage("Dibutuhkan Akses Koneksi Internet Untuk Mengakses Fitur Ini.\n\nPastikan Perangkat Android Anda Terhubung Dengan Internet.\n\nSilahkan Coba Lagi!")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    }).show();
            }
        } else {
            mVideoLoading.setVisibility(View.GONE);
        }
     
        
        AsyncQueryHandler mAsyncQueryHandler = new AsyncQueryHandler(getContentResolver()) {
            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                if (cursor != null && cursor.moveToFirst()) {
                    int titleIdx = cursor.getColumnIndex(MediaStore.Video.Media.TITLE);
                    int artistIdx = cursor.getColumnIndex(MediaStore.Video.Media.ARTIST);
                    int idIdx = cursor.getColumnIndex(MediaStore.Video.Media._ID);
                    int displaynameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);

                    if (idIdx >= 0) {
                        mMediaId = cursor.getLong(idIdx);
                    }

                    if (titleIdx >= 0) {
                        String title = cursor.getString(titleIdx);
                        //mTextLine1.setText(title);
                        if (artistIdx >= 0) {
                            String artist = cursor.getString(artistIdx);
                            //mTextLine2.setText(artist);
                        }
                    } else if (displaynameIdx >= 0) {
                        String name = cursor.getString(displaynameIdx);
                        // mTextLine1.setText(name);
                    } else {
                        // Couldn't find anything to display, what to do now?
                        Log.w(TAG, "Cursor had no names for us");
                    }
                } else {
                    Log.w(TAG, "empty cursor");
                }

                if (cursor != null) {
                    cursor.close();
                }
                // setNames();
            }
        };

        if (scheme.equals(ContentResolver.SCHEME_CONTENT)) {
            if (mUri.getAuthority() == MediaStore.AUTHORITY) {
                // try to get title and artist from the media content provider
                mAsyncQueryHandler.startQuery(0, null, mUri,
                                              new String[] {MediaStore.Video.Media.TITLE, MediaStore.Video.Media.ARTIST},
                                              null, null, null);
            } else {
                // Try to get the display name from another content provider.
                // Don't specifically ask for the display name though, since the
                // provider might not actually support that column.
                mAsyncQueryHandler.startQuery(0, null, mUri, null, null, null, null);
            }
        } else if (scheme.equals("file")) {
            // check if this file is in the media database (clicking on a download
            // in the download manager might follow this path
            String path = mUri.getPath();
            mAsyncQueryHandler.startQuery(0, null, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, new String[] {MediaStore.Video.Media._ID, MediaStore.Video.Media.TITLE, MediaStore.Video.Media.ARTIST}, MediaStore.Video.Media.DATA + "=?", new String[] {path}, null);
        } 
        mVideoView.setMediaController(new MediaController(VideoPlayerActivity.this));
        mVideoView.setVideoURI(mUri);
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

    private void videoStart() {
        mVideoView.start();
        mVideoLoading.setVisibility(View.VISIBLE);
    }

    private void videoPause() {
        if (mVideoView != null && mVideoView.isPlaying()) {
            mVideoView.pause();
            mVideoLoading.setVisibility(View.GONE);
        }
    }

    private void videoDestroy() {
        if (mVideoView != null) {
            mVideoView.stopPlayback();
        }
    }
}
