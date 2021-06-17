package com.github.videobox.utils;

import android.content.Context;
import android.provider.MediaStore;
import android.database.Cursor;
import android.net.Uri;
import android.media.MediaPlayer;
import android.util.Log;
import android.text.Spanned;
import android.widget.VideoView;
import android.widget.MediaController;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;
import java.util.ArrayList;

import com.example.library.banner.BannerLayout;
import com.github.videobox.adapters.VideoAdapter;
import com.github.videobox.models.VideoModel;

public class VideoLocal {

    private Context mContext;
    private ArrayList<VideoModel> mVideoList = new ArrayList<VideoModel>();
    private BannerLayout mBannerLayout;
    private OnVideoLocalListener mItemClickListener;
    private VideoInfo mVideoInfo;
    public VideoLocal(Context context) {
        mContext = context; 
    }
    
    public VideoLocal(Context context, BannerLayout bannerLayout, ArrayList<VideoModel> videoList) {
        mContext = context; 
        mBannerLayout = bannerLayout;
        mVideoList = videoList;
        mVideoInfo = new VideoInfo(context);
        getAllVideoFromGallery();
    }

    public void initVideoView(final VideoView mVideoView) {
        final int position = 0;
        try {
            mVideoView.setMediaController(new MediaController(mContext));
            mVideoView.setVideoURI(Uri.parse(getVideoPath()));
            mVideoView.requestFocus();
            mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    // Close the progress bar and play the video
                    public void onPrepared(MediaPlayer mp) {
                        //progressDialog.dismiss();
                        mVideoView.seekTo(position);
                        if (position == 0) {
                            mVideoView.start();              
                        } else {
                            mVideoView.pause();

                        }
                    }
                });
            
            VideoModel video = new VideoModel();
            video.setVideoTitle(getVideoTitle());
            video.setVideoPath(getVideoPath());
            video.setVideoThumb(getVideoThumbnail());
            mVideoInfo.initialise(video);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
    }

    public void getAllVideoFromGallery() {
        Uri uri;
        Cursor mCursor;
        int COLUMN_INDEX_DATA, COLUMN_INDEX_NAME, COLUMN_ID, COLUMN_THUMB;
        String absolutePathOfFile = null;
        uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Video.Media.BUCKET_DISPLAY_NAME, MediaStore.Video.Media._ID, MediaStore.Video.Thumbnails.DATA};

        final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
        mCursor = mContext.getContentResolver().query(uri, projection, null, null, orderBy + " DESC");
        COLUMN_INDEX_DATA = mCursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        COLUMN_INDEX_NAME = mCursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
        COLUMN_ID = mCursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
        COLUMN_THUMB = mCursor.getColumnIndexOrThrow(MediaStore.Video.Thumbnails.DATA);

        while (mCursor.moveToNext()) {
            absolutePathOfFile = mCursor.getString(COLUMN_INDEX_DATA);
            File videoTitle = new File(absolutePathOfFile);
            Log.e("Column", absolutePathOfFile);
            Log.e("Folder", mCursor.getString(COLUMN_INDEX_NAME));
            Log.e("column_id", mCursor.getString(COLUMN_ID));
            Log.e("thum", mCursor.getString(COLUMN_THUMB));
            VideoModel mVideo = new VideoModel();
            mVideo.setSelected(false);
            mVideo.setVideoTitle(videoTitle.getName());
            mVideo.setVideoPath(absolutePathOfFile);
            mVideo.setVideoThumb(mCursor.getString(COLUMN_THUMB));
            //mVideo.setVideoSize(VideoUtils.formatVideoSize(videoTitle));
            mVideoList.add(mVideo);
        }

        VideoAdapter webBannerAdapter = new VideoAdapter(mContext, mVideoList);
        webBannerAdapter.setOnBannerItemClickListener(new BannerLayout.OnBannerItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    VideoModel video = mVideoList.get(position);
                    mVideoInfo.initialise(video);
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemClick(video.getVideoPath(), video.getVideoThumb());
                    }
                }
            });

        mBannerLayout.setAdapter(webBannerAdapter);
    }

    public String getVideoTitle() {
        return mVideoList.get(0).getVideoTitle();
    }

    public String getVideoPath() {
        return mVideoList.get(0).getVideoPath();
    }
    
    public String getVideoThumbnail() {
        return mVideoList.get(0).getVideoThumb();
    }

    public Spanned getVideoInfo() {
        return mVideoInfo.getVideoInfo();
    }

   
    public void setOnVideoLocalClickListener(OnVideoLocalListener listener) {
        mItemClickListener = listener;
    }

    /**
     * Listener for item click
     *
     */
    public interface OnVideoLocalListener {
        public abstract void onItemClick(String videoPath, String videoThumb);
    }
}
