package com.github.videobox;
 
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import com.github.videobox.app.permissions.PermissionsManager;
import com.github.videobox.app.permissions.PermissionsResultAction;
import com.github.videobox.widget.VideoBoxLayout;
import com.github.videobox.widget.Status;

public class VideoBoxActivity extends Activity { 

    public static final String EXTRA_SHORTCUT = "shortcut_path";
    private  VideoBoxLayout videoBox;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_box);
       
        videoBox = (VideoBoxLayout)findViewById(R.id.video_box_layout);
        videoBox.start();
    } 
    
 
}

