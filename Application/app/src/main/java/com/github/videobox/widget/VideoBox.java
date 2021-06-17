package com.github.videobox.widget;

import android.support.annotation.RequiresApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.provider.MediaStore;
import android.media.MediaPlayer;
import android.net.Uri;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.util.AttributeSet;
import android.text.TextUtils;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.TextureView;
import android.widget.RelativeLayout;
import android.widget.VideoView;
import android.widget.MediaController;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import com.example.library.banner.BannerLayout;
import com.github.videobox.R;
import com.github.videobox.models.VideoModel;
import com.github.videobox.adapters.VideoAdapter;
import com.github.videobox.utils.VideoLocal;
import com.github.videobox.utils.VideoInfo;
import com.github.videobox.VideoPreviewActivity;
import com.github.videobox.utils.VideoConvert;
import com.github.videobox.app.converter.callback.IConvertCallback;
import com.github.videobox.app.converter.AudioConverter;
import com.github.videobox.app.converter.model.AudioFormat;
import com.github.videobox.utils.VideoUtils;
import com.github.videobox.utils.IntentUtils;

public class VideoBox extends RelativeLayout {

    private static final int DISMISS_ON_COMPLETE_DELAY = 1000;
    private VideoView mVideoPlayer;
    private VideoLocal mVideoLocal;
    private VideoInfo mInfo;
    private VideoConvert mVideoConvert;
    private int position = 0;
    private ArrayList<VideoModel> mVideoList;
    private BannerLayout mVideoBanner;
    private OnVideoBannerClickListener onVideoBannerClickListener;
    private OnVideoInfoClickListener onVideoInfoClickListener;
    private OnVideoVRClickListener onVideoVRClickListener;

    private TextView mVideoInfo;

    public interface OnVideoBannerClickListener {
        void OnVideoBannerClick(int position);
    }

    public interface OnVideoInfoClickListener {
        void OnVideoInfoClick(View view);
    }

    public interface OnVideoVRClickListener {
        void OnVideoVRClick(View view);
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
    private View infoView;
    private View playerView;
    private View convertView;
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

    public VideoBox(Context context) {
        super(context);
        init(context, null, 0, 0, 0);
    }

    public VideoBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0, 0);
    }

    public VideoBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, 0, 0, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public VideoBox(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public VideoBox(Context context, int completeLayout, int errorLayout, int loadingLayout) {
        super(context);
        init(context, null, completeLayout, errorLayout, loadingLayout);
    }

    public VideoBox(Context context, AttributeSet attrs, int completeLayout, int errorLayout, int loadingLayout) {
        super(context, attrs);
        init(context, attrs, completeLayout, errorLayout, loadingLayout);
    }

    public VideoBox(Context context, AttributeSet attrs, int defStyleAttr, int completeLayout, int errorLayout, int loadingLayout) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, completeLayout, errorLayout, loadingLayout);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public VideoBox(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, int completeLayout, int errorLayout, int loadingLayout) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, completeLayout, errorLayout, loadingLayout);
    }

    private void init(final Context context, AttributeSet attrs, int completeLayout, int errorLayout, int loadingLayout) {

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
            completeView = inflater.inflate(R.layout.videobox_complete_layout, null);
            errorView = inflater.inflate(R.layout.videobox_error_layout, null);
            loadingview = inflater.inflate(R.layout.videobox_loading_layout, null);
            infoView = inflater.inflate(R.layout.videobox_info_layout, null);
            playerView = inflater.inflate(R.layout.videobox_player_layout, null);
            convertView = inflater.inflate(R.layout.videobox_convert_progress, null);

        } else {
            completeView = inflater.inflate(R.layout.videobox_complete_layout, null);
            errorView = inflater.inflate(R.layout.videobox_error_layout, null);
            loadingview = inflater.inflate(R.layout.videobox_loading_layout, null);
            infoView = inflater.inflate(R.layout.videobox_info_layout, null);
            playerView = inflater.inflate(R.layout.videobox_player_layout, null);   
            convertView = inflater.inflate(R.layout.videobox_convert_progress, null);

        }

        /**
         * Default layout params
         */
        completeView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        errorView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        loadingview.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        infoView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        playerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        convertView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        /**
         * Add layout to root
         */
        addView(completeView);
        addView(errorView);
        addView(loadingview);
        addView(infoView);
        addView(playerView);
        addView(convertView);
        /**
         * set visibilities of childs
         */
        completeView.setVisibility(View.INVISIBLE);
        errorView.setVisibility(View.INVISIBLE);
        loadingview.setVisibility(View.INVISIBLE);
        infoView.setVisibility(View.INVISIBLE);
        playerView.setVisibility(View.INVISIBLE);
        convertView.setVisibility(View.INVISIBLE);

        mVideoBanner = (BannerLayout)completeView.findViewById(R.id.video_banner);
        mVideoInfo = (TextView)infoView.findViewById(R.id.video_box_info);
        mVideoInfo.setMovementMethod(LinkMovementMethod.getInstance());
        mVideoPlayer = (VideoView)playerView.findViewById(R.id.video_box_player);
        mVideoPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
                @Override
                public void onCompletion(MediaPlayer md) {
                    mVideoPlayer.pause();
                    setStatus(Status.COMPLETE);
                    //getAllVideoFromGallery(context);
                }
            });
        mVideoList = new ArrayList<VideoModel>();
        mVideoLocal = new VideoLocal(getContext(), mVideoBanner, mVideoList);     
        mVideoLocal.initVideoView(mVideoPlayer);
        mInfo = new VideoInfo(context);
        mVideoInfo.setText(mVideoLocal.getVideoInfo());
        mVideoInfo.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {                 
                    if (onVideoInfoClickListener != null) {
                        onVideoInfoClickListener.OnVideoInfoClick(v);
                    }
                }
            });
        mVideoLocal.setOnVideoLocalClickListener(new VideoLocal.OnVideoLocalListener(){
                @Override
                public void onItemClick(String videoPath, String videoThumb) {    

                    try {
                        mVideoPlayer.setMediaController(new MediaController(getContext()));
                        mVideoPlayer.setVideoURI(Uri.parse(videoPath)); 
                        mVideoPlayer.requestFocus();
                        mVideoPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                // Close the progress bar and play the video
                                public void onPrepared(MediaPlayer mp) {
                                    //progressDialog.dismiss();
                                    mVideoPlayer.seekTo(position);
                                    if (position == 0) {
                                        mVideoPlayer.start();              
                                    } else {
                                        mVideoPlayer.pause();

                                    }
                                }
                            });
                        mVideoInfo.setText(mVideoLocal.getVideoInfo());                 
                    } catch (Exception e) {
                        Log.e("Error", e.getMessage());
                        e.printStackTrace();
                    }
                    mVideoPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
                            @Override
                            public void onCompletion(MediaPlayer md) {
                                mVideoPlayer.pause();
                                setStatus(Status.COMPLETE);
                            }
                        });

                    setStatus(Status.PLAYER);
                }
            }); 

        mVideoConvert = (VideoConvert)convertView.findViewById(R.id.video_box_convert);
    }


    public void setVideoPath(String video) {
        mVideoPlayer.setMediaController(null);
        mVideoPlayer.setVideoURI(Uri.parse(video));
        mVideoPlayer.requestFocus();
        mVideoPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
                @Override
                public void onCompletion(MediaPlayer md) {
                    mVideoPlayer.pause();
                    setStatus(Status.COMPLETE);
                    //getAllVideoFromGallery(context);
                }
            });
        mVideoPlayer.start();
        setStatus(Status.PLAYER);
    }

    public void start() {
        
        setStatus(Status.PLAYER);
    }

    public void onPause() {
        if (mVideoPlayer != null && mVideoPlayer.isPlaying()) {
            mVideoPlayer.pause();
        }
    }

    public void onDestroy() {
        if (mVideoPlayer != null) {
            mVideoPlayer.stopPlayback();
        }
    }

    public void seekTo(int msec) {
        mVideoPlayer.seekTo(msec);
    }

    public int getCurrentPosition() {
        return mVideoPlayer.getCurrentPosition();
    }

    public boolean isPlaying() {
        return mVideoPlayer.isPlaying();
    }

    public void setVideoConvert(final Context c, final String title, final String message, final String outPut)
    {
        /**
         *  Update with a valid audio file!
         *  Supported formats: {@link AndroidAudioConverter.AudioFormat}
         */
        File wavFile = new File(VideoInfo.getVideoPath());
        IConvertCallback callback = new IConvertCallback() {
            @Override
            public void onStart() {
                mVideoConvert.setTitle(title);
                mVideoConvert.setMessage(message);
                mVideoConvert.setStatus(outPut);
            }
            
            @Override
            public void onSuccess(File convertedFile) {
                //Toast.makeText(c, "SUCCESS: " + convertedFile.getPath(), Toast.LENGTH_LONG).show();
                mVideoConvert.setStatus(convertedFile.getPath());
                IntentUtils.openFile(c, convertedFile.getAbsoluteFile());
            }
            @Override
            public void onFailure(Exception error) {
                mVideoConvert.setMessage(error.getMessage());
                Toast.makeText(c, "ERROR: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
            
            @Override
            public void onFinish() {
                
            }
        };
        Toast.makeText(c, "Converting audio file...", Toast.LENGTH_SHORT).show();
        AudioConverter.with(c)
            .setFile(wavFile)
            .setFormat(AudioFormat.MP3)
            .setCallback(callback)
            .convert();
        
        setStatus(Status.CONVERT);
    }
    
    public void setMediaController(MediaController controller) {
        mVideoPlayer.setMediaController(controller);
    }

    public void setOnPreparedListener(MediaPlayer.OnPreparedListener listener) {
        mVideoPlayer.setOnPreparedListener(listener);
    }

    public void setOnVideoBannerClickListener(OnVideoBannerClickListener onVideoBannerClickListener) {
        this.onVideoBannerClickListener = onVideoBannerClickListener;
    }

    public void setOnErrorClickListener(OnClickListener onErrorClickListener) {
        errorView.setOnClickListener(onErrorClickListener);
    }

    public void setOnLoadingClickListener(OnClickListener onLoadingClickListener) {
        loadingview.setOnClickListener(onLoadingClickListener);
    }

    public void setOnCompleteClickListener(OnClickListener onCompleteClickListener) {
        completeView.setOnClickListener(onCompleteClickListener);
    }

    public void setOnVideoInfoClickListener(OnVideoInfoClickListener onVideoInfoClickListener) {
        this.onVideoInfoClickListener = onVideoInfoClickListener;
    }

    public void setOnVideoVRClickListener(OnVideoVRClickListener onVideoVRClickListener) {
        this.onVideoVRClickListener = onVideoVRClickListener;
    }

    public VideoView getVideoView() {
        return mVideoPlayer;
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
    public Status getStatus() {
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
        else if (status == Status.INFO)
            return infoView; 
        else if (status == Status.PLAYER)
            return playerView; 
        else if (status == Status.CONVERT)
            return convertView; 
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
