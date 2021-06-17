package com.github.videobox.widget;

import android.support.annotation.RequiresApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.provider.MediaStore;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import com.example.library.banner.BannerLayout;
import com.github.videobox.R;
import com.github.videobox.models.VideoModel;
import com.github.videobox.adapters.VideoAdapter;

public class VideoBanner extends RelativeLayout {

    private static final int DISMISS_ON_COMPLETE_DELAY = 1000;
    private ArrayList<VideoModel> mVideoList;
    private BannerLayout mVideoBanner;
    private OnVideoBannerClickListener onVideoBannerClickListener;
    
    public interface OnVideoBannerClickListener
    {
        void OnVideoBannerClick(int position);
    }
    /**
     * Current status of status view
     */
    private Status currentStatus;

    /**
     * Automatically hide when status changed to complete
     */
    private boolean hideOnComplete;

    /**
     * Views for each status
     */
    private View completeView;
    private View errorView;
    private View loadingview;

    /**
     * Fade in out animations
     */
    private Animation slideOut;
    private Animation slideIn;

    /**
     * layout inflater
     */
    private LayoutInflater inflater;

    /**
     * Handler
     */
    //private Handler handler;

    /**
     * Auto dismiss on complete
     */
    /*private Runnable autoDismissOnComplete = new Runnable() {
        @Override
        public void run() {
            exitAnimation(getCurrentView(currentStatus));
            handler.removeCallbacks(autoDismissOnComplete);
        }
    };*/

    public VideoBanner(Context context) {
        super(context);
        init(context, null, 0, 0, 0);
    }

    public VideoBanner(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0, 0);
    }

    public VideoBanner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, 0, 0, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public VideoBanner(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public VideoBanner(Context context, int completeLayout, int errorLayout, int loadingLayout) {
        super(context);
        init(context, null, completeLayout, errorLayout, loadingLayout);
    }

    public VideoBanner(Context context, AttributeSet attrs, int completeLayout, int errorLayout, int loadingLayout) {
        super(context, attrs);
        init(context, attrs, completeLayout, errorLayout, loadingLayout);
    }

    public VideoBanner(Context context, AttributeSet attrs, int defStyleAttr, int completeLayout, int errorLayout, int loadingLayout) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, completeLayout, errorLayout, loadingLayout);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public VideoBanner(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, int completeLayout, int errorLayout, int loadingLayout) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, completeLayout, errorLayout, loadingLayout);
    }

    private void init(Context context, AttributeSet attrs, int completeLayout, int errorLayout, int loadingLayout) {

        /**
         * Load initial values
         */
        currentStatus = Status.IDLE;
        hideOnComplete = true;
        slideIn = AnimationUtils.loadAnimation(context, R.anim.sv_slide_in);
        slideOut = AnimationUtils.loadAnimation(context, R.anim.sv_slide_out);
        inflater = LayoutInflater.from(context);
        //handler = new Handler();

 
        /**
         * inflate layouts
         */
        if (completeLayout == 0) {
            completeView = inflater.inflate(R.layout.videobanner_layout_complete, null);
            errorView = inflater.inflate(R.layout.videobanner_layout_error, null);
            loadingview = inflater.inflate(R.layout.videobanner_layout_loading, null);
        } else {
            completeView = inflater.inflate(R.layout.videobanner_layout_complete, null);
            errorView = inflater.inflate(R.layout.videobanner_layout_error, null);
            loadingview = inflater.inflate(R.layout.videobanner_layout_loading, null);
        }

        /**
         * Default layout params
         */
        completeView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        errorView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        loadingview.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        /**
         * Add layout to root
         */
        addView(completeView);
        addView(errorView);
        addView(loadingview);

        /**
         * set visibilities of childs
         */
        completeView.setVisibility(View.INVISIBLE);
        errorView.setVisibility(View.INVISIBLE);
        loadingview.setVisibility(View.INVISIBLE);

        mVideoBanner = (BannerLayout)findViewById(R.id.video_banner);

        mVideoList = new ArrayList<VideoModel>();
        getAllVideoFromGallery(context);
    }

    public void getAllVideoFromGallery(final Context c) {
        Uri uri;
        Cursor mCursor;
        int COLUMN_INDEX_DATA, COLUMN_INDEX_NAME, COLUMN_ID, COLUMN_THUMB;
        String absolutePathOfFile = null;
        uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Video.Media.BUCKET_DISPLAY_NAME, MediaStore.Video.Media._ID, MediaStore.Video.Thumbnails.DATA};

        final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
        mCursor = c.getApplicationContext().getContentResolver().query(uri, projection, null, null, orderBy + " DESC");
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

        VideoAdapter webBannerAdapter=new VideoAdapter(c, mVideoList);
        webBannerAdapter.setOnBannerItemClickListener(new BannerLayout.OnBannerItemClickListener() {
                @Override
                public void onItemClick(int position) {

                    if(onVideoBannerClickListener == null){
                        onVideoBannerClickListener.OnVideoBannerClick(position);
                    }

                    Toast.makeText(c, "Video  " + mVideoList.get(position).getVideoTitle(), Toast.LENGTH_SHORT).show();
                }
            });

        mVideoBanner.setAdapter(webBannerAdapter);
        setStatus(Status.COMPLETE);
    }
    
    public void setOnVideoBannerClickListener(OnVideoBannerClickListener onVideoBannerClickListener){
        this.onVideoBannerClickListener = onVideoBannerClickListener;
    }
    
    public void setOnErrorClickListener(OnClickListener onErrorClickListener) {
        errorView.setOnClickListener(onErrorClickListener);
    }

    public void setOnLoadingClickListener(OnClickListener onLoadingClickListener) {
        loadingview.setOnClickListener(onLoadingClickListener);
    }

    public void setOnCompleteClickListener(OnClickListener onCompleteClickListener){
        completeView.setOnClickListener(onCompleteClickListener);
    }

    public View getErrorView() {
        return errorView;
    }

    public View getCompleteView() {
        return completeView;
    }

    public View getLoadingView() {
        return loadingview;
    }

    public void setStatus(final Status status) {
        if (currentStatus == Status.IDLE) {
            currentStatus = status;
            enterAnimation(getCurrentView(currentStatus));
        } else if (status != Status.IDLE) {
            switchAnimation(getCurrentView(currentStatus), getCurrentView(status));
            currentStatus = status;
        } else {
            exitAnimation(getCurrentView(currentStatus));
        }
    }
    /**
     * 
     * @return Status object 
     */
    public Status getStatus(){
        return this.currentStatus;
    }

    private View getCurrentView(Status status) {
        if (status == Status.IDLE)
            return null;
        else if (status == Status.COMPLETE)
            return completeView;
        else if (status == Status.ERROR)
            return errorView;
        else if (status == Status.LOADING)
            return loadingview;
        return null;
    }

    private void switchAnimation(final View exitView, final View enterView) {
        clearAnimation();
        exitView.setVisibility(View.VISIBLE);
        exitView.startAnimation(slideOut);
        slideOut.setAnimationListener(new SimpleAnimListener() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    slideOut.setAnimationListener(null);
                    exitView.setVisibility(View.INVISIBLE);
                    enterView.setVisibility(View.VISIBLE);
                    enterView.startAnimation(slideIn);
                }
            });
    }

    private void enterAnimation(View enterView) {
        if (enterView == null)
            return;

        enterView.setVisibility(VISIBLE);
        enterView.startAnimation(slideIn);
    }

    private void exitAnimation(final View exitView) {
        if (exitView == null)
            return;

        exitView.startAnimation(slideOut);
        slideOut.setAnimationListener(new SimpleAnimListener() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    currentStatus = Status.IDLE;
                    exitView.setVisibility(INVISIBLE);
                    slideOut.setAnimationListener(null);
                }
            });
    }
}
