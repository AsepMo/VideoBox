package com.github.videobox.utils;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.github.videobox.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.github.videobox.VideoBoxActivity;

public class VideoUtils {

    public static void shareVideo(Context context, String video) throws IOException, FileNotFoundException {

        //Uri fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", new File(file));
        Uri fileUri = Uri.parse(video);
        Intent Shareintent = new Intent()
            .setAction(Intent.ACTION_SEND)
            .setType("video/*")
            .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .putExtra(Intent.EXTRA_STREAM, fileUri);
        context.startActivity(Intent.createChooser(Shareintent, context.getString(R.string.share_intent_notification_title)));
    }

    public void openVideoPlayer(Context mContext) {
        Uri uri = Uri.parse(VideoInfo.getVideoPath());
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setDataAndType(uri, "video/mp4");
        mContext.startActivity(intent);
        FileTools.Toast(mContext, uri.getPath());
    }

    public static void openVideoPlayer(Context mContext, String video) {
        Uri uri = Uri.parse(video);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setDataAndType(uri, "video/mp4");
        mContext.startActivity(intent);
        FileTools.Toast(mContext, video);
    }

    public static void openAudioPlayer(Context mContext, String audio) {
        Uri uri = Uri.parse(audio);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setDataAndType(uri, "audio/*");
        mContext.startActivity(intent);
         FileTools.Toast(mContext, audio);
    }
    
    
    


}
