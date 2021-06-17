package com.github.videobox;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewPropertyAnimator;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import com.google.android.youtube.player.YouTubePlayer.OnFullscreenListener;
import com.github.videobox.app.fragments.VideoFragment;
import com.github.videobox.app.youtube.tasks.YoutubeTask;
import com.github.videobox.utils.CheckerUrl;
import com.github.videobox.models.VideoModel;
import com.github.videobox.models.ActionItem;
import com.github.videobox.widget.QuickAction;

public class YouTubePreviewActivity extends Activity implements OnFullscreenListener {
    

    public static void start(Context c) {
        Intent intent = new Intent(c, YouTubePreviewActivity.class);
        c.startActivity(intent);
    }
    private String TAG = YouTubePreviewActivity.class.getSimpleName();
    public static final String TAG_URL = "videoId";
    
    private VideoFragment videoFragment;
    private boolean isFullscreen;
    private boolean mVisible;
  //  private static String youtubeLink;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_player);
        RelativeLayout mVideoBoxLayout = (RelativeLayout)findViewById(R.id.video_box_iframe);  
        mVideoBoxLayout.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {                 
                    onActionMenu(v);
                }
            });
    }
    
    public void getYoutubePlayer(String url)
    {
        final String videoId = CheckerUrl.getVideoIdFromYoutubeUrl(url);
        YoutubeTask task = new YoutubeTask(this, getFragmentManager(), videoId, findViewById(R.id.layoutMessage), findViewById(R.id.video_fragment_container));
        task.execute();
    }
 
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("ON_RESUME", "ausgeführt!");
        
        String urlFromIntent = null;
        if (getIntent().getStringExtra(Intent.EXTRA_TEXT) != null && getIntent().getAction().equals(Intent.ACTION_SEND)) {
            Log.d("IntentText: ", getIntent().getStringExtra(Intent.EXTRA_TEXT));
            urlFromIntent = getIntent().getStringExtra(Intent.EXTRA_TEXT);
            
            getYoutubePlayer(urlFromIntent);
        }
        if (getIntent().getAction().equals(Intent.ACTION_VIEW)) {
            urlFromIntent = getIntent().getDataString();
            getYoutubePlayer(urlFromIntent);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        getIntent().removeExtra(Intent.EXTRA_TEXT);
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


        final QuickAction quickAction = new QuickAction(YouTubePreviewActivity.this);

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
                            //getVideoInfo();
                            break;
                        case ID_VIDEO:
                            //getVideoPlayer();
                            break; 
                        case ID_VIDEO_PLAYLIST:
                            //getVideoPlaylist();
                            break; 
                        case ID_VIDEO_FOLDER:
                            //getVideoFolder();
                            break;     
                        case ID_VIDEO_CINEMA:
                            //getVideoCinema(true);  
                            break;     
                        case ID_VIDEO_RECORDER:
                            //getVideoRecorder();
                            break; 
                        case ID_VIDEO_3D:
                            //getVideo3D(true);
                            break;      
                        case ID_EDITOR:
                            //getVideoEditor();
                            break;   
                        case ID_CONVERT_TO_MP3:
                            //getVideoConverter();
                            break;
                        case ID_VIDEO_TRIMMER:
                            //getVideoDelete();
                            break;
                        case ID_SHARE:
                            //getVideoShare();
                            break;    
                        case ID_MANAGE:
                            //getVideoManage();
                            break;     
                        default:

                            Toast.makeText(YouTubePreviewActivity.this, actionItem.getTitle() + " selected", Toast.LENGTH_SHORT).show(); 
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
    
     @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        layout();
    }

    @Override
    public void onFullscreen(boolean isFullscreen) {
        this.isFullscreen = isFullscreen;

        layout();
    }

    /**
     * Sets up the layout programatically for the three different states. Portrait, landscape or
     * fullscreen+landscape. This has to be done programmatically because we handle the orientation
     * changes ourselves in order to get fluent fullscreen transitions, so the xml layout resources
     * do not get reloaded.
     */
    private void layout() {
        boolean isLandscap = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;   
        if (isFullscreen && isLandscap) {          
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);                 
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);                  
        }
    }

    @Override
    public void onBackPressed() {
        boolean isLandscap = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        if (isLandscap) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);                  
        } else {
            super.onBackPressed();
        }
    }
}
