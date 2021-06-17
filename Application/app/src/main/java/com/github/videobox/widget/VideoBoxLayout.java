package com.github.videobox.widget;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.annotation.TargetApi;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Build;
import android.os.Handler;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.Locale;

import com.github.videobox.R;
import com.github.videobox.VideoVRActivity;
import com.github.videobox.VideoRecorderActivity;
import com.github.videobox.VideoPreviewActivity;
import com.github.videobox.VideoEditorActivity;
import com.github.videobox.VideoTrimmerActivity;
import com.github.videobox.YouTubePreviewActivity;
import com.github.videobox.app.dialogs.NormalProgressDialog;
import com.github.videobox.models.VideoModel;
import com.github.videobox.models.ActionItem;
import com.github.videobox.utils.VideoInfo;
import com.github.videobox.utils.VideoUtils;
import com.github.videobox.utils.ExtractVideoInfoUtil;
import com.github.videobox.utils.IntentUtils;

import com.martin.ads.vrlib.ui.Pano360ConfigBundle;
import com.martin.ads.vrlib.ui.PanoPlayerActivity;
import java.io.IOException;
import java.io.FileNotFoundException;
import com.github.videobox.utils.AnimationUtils;
import com.github.videobox.VideoSelectorActivity;
import com.github.videobox.VideoHistoryActivity;
import com.github.videobox.utils.SharedPref;

public class VideoBoxLayout extends RelativeLayout {
    private static final String TAG = VideoBoxLayout.class.getSimpleName();
    public static final int SELECT_FILE_CODE = 121;
    public static final int SELECT_FOLDER_CODE = 122;

    private Context mContext;
    private LayoutInflater inflater;
    private View mVideoFrame;
    private RelativeLayout mVideoBoxLayout;
    private VideoBox mVideoBox;

    public static final int VIDEO_LOCAL = 1;
    public static final int VIDEO_PATH = 2;
    public static final int VIDEO_STREAMMING = 3;

    private final int ID_INFO    = 1;
    private final int ID_VIDEO   = 2;
    private final int ID_VIDEO_PLAYLIST  = 3;
    private final int ID_VIDEO_FOLDER  = 4;
    private final int ID_VIDEO_CINEMA   = 5;
    private final int ID_VIDEO_RECORDER   = 6;
    private final int ID_VIDEO_3D   = 7;
    private final int ID_VIDEO_TRIMMER   = 8;
    private final int ID_EDITOR   = 9;
    private final int ID_CONVERT_TO_MP3   = 10;
    private final int ID_SHARE   = 11;
    private final int ID_MANAGE    = 12;

    private String filePath;
    private String videoTitle;
    private String videoPath;
    private String videoHotspotPath;

    public enum VideoType {
        VIDEO_LOADING, VIDEO_ERROR, VIDEO_COMPLETE, VIDEO_PLAYER, VIDEO_INFO, VIDEO_CONVERT
        }

    public VideoBoxLayout(Context context) {
        super(context);
        init(context, null);
    }

    public VideoBoxLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public VideoBoxLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressWarnings("unused")
    public VideoBoxLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        setBackgroundColor(Color.TRANSPARENT);  

    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        setKeepScreenOn(true);

        // Instantiate and add TextureView for rendering
        inflater = LayoutInflater.from(getContext());
        mVideoFrame = inflater.inflate(R.layout.videobox_frame_layout, this, false);
        mVideoFrame.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        addView(mVideoFrame);

        mVideoBoxLayout = (RelativeLayout)mVideoFrame.findViewById(R.id.video_box_iframe);  
        mVideoBoxLayout.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {                 
                    onActionMenu(v);
                }
            });

        mVideoBox = (VideoBox)mVideoFrame.findViewById(R.id.video_box); 
        mVideoBox.setOnVideoInfoClickListener(new VideoBox.OnVideoInfoClickListener(){
                @Override
                public void OnVideoInfoClick(View v) {                 
                    onActionMenu(v);
                }
            });
        mVideoBox.setOnVideoVRClickListener(new VideoBox.OnVideoVRClickListener(){
                @Override
                public void OnVideoVRClick(View v) {                 
                    onActionMenu(v);
                }
            });
    }

    public void setVideoType(VideoType typeVideo) {
        switch (typeVideo) {
            case VIDEO_LOADING:
                mVideoBox.setStatus(Status.LOADING);
                break;
            case VIDEO_ERROR:
                mVideoBox.setStatus(Status.ERROR);
                break;
            case VIDEO_COMPLETE:
                mVideoBox.setStatus(Status.COMPLETE);
                break;
            case VIDEO_PLAYER:
                mVideoBox.setStatus(Status.PLAYER);
                mVideoBox.getVideoView().pause();
                break;
            case VIDEO_INFO:
                mVideoBox.setStatus(Status.INFO);
                break; 
            case VIDEO_CONVERT:
                mVideoBox.setStatus(Status.CONVERT);
                break;     
        }
    }

    public void setVideoTitle(String title) {
        videoTitle = title;
    }

    public void setVideoPath(String path) {
        videoPath = path;

    }

    public void setVideoStreaming(String path) {
        videoPath = path;  
    }

    public void getVideoInfo() {
        setVideoType(VideoType.VIDEO_INFO);
    }

    public void getVideoLoading() {
        setVideoType(VideoType.VIDEO_LOADING);
    }

    public void getVideoPlayer() {
        setVideoType(VideoType.VIDEO_PLAYER);
    }

    public void getVideoPlaylist() {
        setVideoType(VideoType.VIDEO_COMPLETE);
    }

    public void getVideoFolder() {
        Intent intent = new Intent(mContext, VideoHistoryActivity.class);
        intent.setAction(VideoHistoryActivity.ACTION_SHOW_FOLDER);
        intent.putExtra(VideoHistoryActivity.FOLDER, SharedPref.SD_CARD_ROOT);
        mContext.startActivity(intent); 
    }

    public void getVideoCinema(boolean usingDefaultActivity) {
        filePath = "images/vr_cinema.jpg";
        videoHotspotPath = VideoInfo.getVideoPath();   
        Pano360ConfigBundle configBundle = Pano360ConfigBundle
            .newInstance()
            .setFilePath(filePath)
            .setImageModeEnabled(true)
            .setPlaneModeEnabled(false)
            .setRemoveHotspot(false)
            .setVideoHotspotPath(videoHotspotPath);
        if (usingDefaultActivity)
            configBundle.startEmbeddedActivity(mContext);
        else {
            Intent intent=new Intent(mContext, VideoVRActivity.class);
            intent.putExtra(PanoPlayerActivity.CONFIG_BUNDLE, configBundle);
            mContext.startActivity(intent);
        }

        Toast(videoHotspotPath);
    }

    public void getVideo3D(boolean usingDefaultActivity) {
        filePath = VideoInfo.getVideoPath();
        videoHotspotPath = null;   
        Pano360ConfigBundle configBundle = Pano360ConfigBundle
            .newInstance()
            .setFilePath(filePath)
            .setImageModeEnabled(false)
            .setPlaneModeEnabled(false)
            .setRemoveHotspot(false)
            .setVideoHotspotPath(videoHotspotPath);
        if (usingDefaultActivity)
            configBundle.startEmbeddedActivity(mContext);
        else {
            Intent intent=new Intent(mContext, VideoVRActivity.class);
            intent.putExtra(PanoPlayerActivity.CONFIG_BUNDLE, configBundle);
            mContext.startActivity(intent);
        }
    }

    public void getVideoRecorder() {
        VideoRecorderActivity.start(mContext);
        //Toast("Belum Tersedia...");
    }

    public void getVideoEditor() {
        VideoEditorActivity.start(mContext);
        //Toast("Belum Tersedia...");
    }

    public void getVideoConverter() {
        mVideoBox.setVideoConvert(mContext, VideoInfo.getVideoTitle(), VideoInfo.getConvertFolder(), "Convert To Mp3");
        //Toast("Belum Tersedia...");
    }

    public void getVideoDelete() {
        VideoTrimmerActivity.startTrimActivity(mContext, VideoInfo.getVideoPath());
        
        //Toast("Belum Tersedia...");
    }

    public void getVideoShare() {
        try {
            String path = VideoInfo.getVideoPath();
            VideoUtils.shareVideo(mContext, path);
        } catch (IOException | FileNotFoundException e) {
            String msg = e.getMessage();
            Toast(msg);
        }
    }

    public void getVideoManage() {
        Toast("Belum Tersedia...");
    }

    public void start() {
        mVideoBox.start();
    }

    public void pause() {
        if (mVideoBox != null && mVideoBox.isPlaying()) {
            mVideoBox.onPause();
        }
    }

    public void stopPlayback() {
        if (mVideoBox != null) {
            mVideoBox.onDestroy();
        }
    }

    public void seekTo(int msec) {
        mVideoBox.seekTo(msec);
    }

    public int getCurrentPosition() {
        return mVideoBox.getCurrentPosition();
    }

    public boolean isPlaying() {
        return mVideoBox.isPlaying();
    }

    public void onActionMenu(View view) {
        ActionItem infoItem     = new ActionItem(ID_INFO, "Info", getResources().getDrawable(R.drawable.ic_information_outline));
        ActionItem videoItem       = new ActionItem(ID_VIDEO, "Video", getResources().getDrawable(R.drawable.ic_video_player));
        ActionItem videoRecorderItem     = new ActionItem(ID_VIDEO_RECORDER, "Recorder", getResources().getDrawable(R.drawable.ic_video));
        ActionItem videoCinemaItem       = new ActionItem(ID_VIDEO_CINEMA, "Movie", getResources().getDrawable(R.drawable.ic_movie));
        ActionItem video3DItem       = new ActionItem(ID_VIDEO_3D, "3D", getResources().getDrawable(R.drawable.ic_video_3d));   
        ActionItem videoTrimmerItem     = new ActionItem(ID_VIDEO_TRIMMER, "Cut", getResources().getDrawable(R.drawable.ic_video_cut));     
        ActionItem shareItem       = new ActionItem(ID_SHARE, "Share", getResources().getDrawable(R.drawable.ic_share_variant));
        ActionItem manageItem       = new ActionItem(ID_MANAGE, "Manage", getResources().getDrawable(R.drawable.ic_settings));
        ActionItem editItem       = new ActionItem(ID_EDITOR, "Editor", getResources().getDrawable(R.drawable.ic_video_edit));
        ActionItem videoPlaylistItem     = new ActionItem(ID_VIDEO_PLAYLIST, "Library", getResources().getDrawable(R.drawable.ic_library_video));
        ActionItem convertToMp3Item       = new ActionItem(ID_CONVERT_TO_MP3, "Convert To Mp3", getResources().getDrawable(R.drawable.ic_convert_to_mp3));
        ActionItem videoFolderItem       = new ActionItem(ID_VIDEO_FOLDER, "Folder", getResources().getDrawable(R.drawable.ic_folder));


        final QuickAction quickAction = new QuickAction(mContext);

        //add action items into QuickAction
        quickAction.addActionItem(infoItem);  
        quickAction.addActionItem(videoItem);
        quickAction.addActionItem(videoPlaylistItem);
        quickAction.addActionItem(videoFolderItem);
        quickAction.addActionItem(videoCinemaItem);
        quickAction.addActionItem(video3DItem);
        quickAction.addActionItem(videoRecorderItem);
        quickAction.addActionItem(videoTrimmerItem);
        quickAction.addActionItem(editItem);  
        quickAction.addActionItem(convertToMp3Item);
        quickAction.addActionItem(shareItem);        
        quickAction.addActionItem(manageItem);

        //Set listener for action item clicked
        quickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {          
                @Override
                public void onItemClick(QuickAction source, int pos, int actionId) {
                    //here we can filter which action item was clicked with pos or actionId parameter
                    ActionItem actionItem = quickAction.getActionItem(pos);
                    switch (actionId) {

                        case ID_INFO:  
                            getVideoInfo();
                            break;
                        case ID_VIDEO:
                            getVideoPlayer();
                            break; 
                        case ID_VIDEO_PLAYLIST:
                            getVideoPlaylist();
                            break; 
                        case ID_VIDEO_FOLDER:
                            getVideoFolder();
                            break;     
                        case ID_VIDEO_CINEMA:
                            getVideoCinema(true);  
                            break;     
                        case ID_VIDEO_RECORDER:
                            getVideoRecorder();
                            break; 
                        case ID_VIDEO_3D:
                            getVideo3D(true);
                            break;      
                        case ID_EDITOR:
                            getVideoEditor();
                            break;   
                        case ID_CONVERT_TO_MP3:
                            getVideoConverter();
                            break;
                        case ID_VIDEO_TRIMMER:
                            getVideoDelete();
                            break;
                        case ID_SHARE:
                            getVideoShare();
                            break;    
                        case ID_MANAGE:
                            getVideoManage();
                            break;     
                        default:

                            Toast.makeText(mContext, actionItem.getTitle() + " selected", Toast.LENGTH_SHORT).show(); 
                            break;
                    }


                }

            });

        //set listnener for on dismiss event, this listener will be called only if QuickAction dialog was dismissed
        //by clicking the area outside the dialog.
        quickAction.setOnDismissListener(new QuickAction.OnDismissListener() {          
                @Override
                public void onDismiss() {

                }
            });
        quickAction.show(view); 
    }

    /**
     * 视频压缩
     */
    /* private void compressVideo(final String srcPath) {
     showCompressLoading();

     AsyncTask.execute(new Runnable() {
     @Override
     public void run() {
     try {

     File destDir = new File(VideoInfo.OutputDir);
     if (!destDir.exists() || !destDir.isDirectory()) {
     destDir.mkdirs();
     }
     String destDirPath = destDir.getAbsolutePath();
     String compressedFilePath = SiliCompressor.with(mContext).compressVideo(srcPath, destDirPath, 720, 480, 900000);
     Log.d(TAG, "视频压缩成功: " + compressedFilePath);

     //获取视频第一帧图片
     ExtractVideoInfoUtil extractVideoInfoUtil = new ExtractVideoInfoUtil(compressedFilePath);
     Bitmap bitmap = extractVideoInfoUtil.extractFrame();
     String firstFrameFilePath = VideoUtils.saveBitmap(destDirPath, bitmap);
     if (bitmap != null && !bitmap.isRecycled()) {
     bitmap.recycle();
     bitmap = null;
     }
     Log.d(TAG, "视频第一帧图片获取成功: " + firstFrameFilePath);

     dismissCompressLoading();

     VideoPreviewActivity.startActivity(mContext, VideoInfo.getVideoPath(), firstFrameFilePath);
     //finish();
     } catch (Exception e) {
     dismissCompressLoading();
     Log.e(TAG, "视频压缩失败: " + e.getMessage());
     }
     }
     });
     }

     private void showCompressLoading() {
     NormalProgressDialog.showLoading(mContext, "Please wait...", false);
     }

     private void dismissCompressLoading() {
     Handler mHandler = new Handler();
     mHandler.post(new Runnable() {
     @Override
     public void run() {
     NormalProgressDialog.stopLoading();
     }
     });
     }*/

    private void Toast(final String message) {
        Handler mHandler = new Handler();
        mHandler.post(new Runnable() {
                public void run() {
                    Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
                }
            });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.i("", "Attached to window");
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d("", "Detached from window");  
    }

}
