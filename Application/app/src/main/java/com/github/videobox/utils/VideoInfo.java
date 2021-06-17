package com.github.videobox.utils;

import android.content.Context;
import android.provider.MediaStore;
import android.database.Cursor;
import android.net.Uri;
import android.media.MediaPlayer;
import android.media.MediaMetadataRetriever;
import android.graphics.Color;
import android.text.format.Formatter;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.VideoView;
import android.widget.TextView;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.github.videobox.models.VideoModel;
import com.github.videobox.widget.HtmlBuilder;
import android.os.Environment;

public class VideoInfo {
    
    public static String TAG = VideoInfo.class.getSimpleName();
    
    private Context mContext;
    public static String OutputDir = Environment.getExternalStorageDirectory() + "/VideoBox";
    
    public static final String DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";
    /**
     * Method to convert bits per second to MB/s
     *
     * @param bps float bitsPerSecond
     * @return float
     */
    private float bitsToMb(float bps) {
        return bps / (1024 * 1024);
    }

    public VideoInfo(Context context) {
        mContext = context; 
    }
    
    public static void initialise(VideoModel video)
    {
        try
        {
            JSONObject json = new JSONObject();
            json.put("videoTitle", video.getVideoTitle());
            json.put("videoThumbnail", video.getVideoThumb());
            json.put("videoPath", video.getVideoPath());
            
            String filePath = OutputDir + "/video_initialise.json";
            File file = new File(filePath);
            //file.getParentFile().mkdirs();
            FileUtils.writeStringToFile(file, json.toString());
        }
        catch (IOException | JSONException e)
        {
            e.printStackTrace();
        }
    }
    
    public void setVideoPath(String videoPath)
    {
        try
        {
            JSONObject json = new JSONObject();
            json.put("videoPath", videoPath);
            
            String filePath = OutputDir + "/video_info_path.json";
            File file = new File(filePath);
            //file.getParentFile().mkdirs();
            FileUtils.writeStringToFile(file, json.toString());
        }
        catch (IOException | JSONException e)
        {
            e.printStackTrace();
        }
    }
    
    public static String getVideoTitle()
    {
        try
        {
            File infoFile = new File(OutputDir + "/video_initialise.json");
            JSONObject json = new JSONObject(FileUtils.readFileToString(infoFile));
            return json.getString("videoTitle");
        }
        catch (IOException | JSONException e)
        {
            return null;
        }
    } 
   
    public static String getVideoThumbnail()
    {
        try
        {
            File infoFile = new File(OutputDir + "/video_initialise.json");
            JSONObject json = new JSONObject(FileUtils.readFileToString(infoFile));
            return json.getString("videoThumbnail");
        }
        catch (IOException | JSONException e)
        {
            return null;
        }
    }
    
    public static String getVideoPath()
    {
        try
        {
            File infoFile = new File(OutputDir + "/video_initialise.json");
            JSONObject json = new JSONObject(FileUtils.readFileToString(infoFile));
            return json.getString("videoPath");
        }
        catch (IOException | JSONException e)
        {
            return null;
        }
    }
    
    public static String getConvertFolder()
    {
        return VideoFolder.ZFOLDER_AUDIO_CONVERT;
    }
    
    public Spanned getVideoInfo() {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(getVideoPath());
        String width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        String height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        String bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
        String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        String format = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);

        File f = new File(getVideoPath());
        long fileSize = f.length();
        float bps = bitsToMb(Integer.parseInt(bitrate));
        //bitrate.setSummary(bps + " Mbps");

        Date d = new Date(f.lastModified());
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);

        HtmlBuilder html = new HtmlBuilder();
        html.h1("Video Info");
        html.font(0xFFCAE682, "Nama File :")
            .append(' ')
            .font(0xFFFFFFFF, f.getName()).br();
        html.font(0xFFCAE682, "Format :")
            .append(' ')
            .font(0xFFFFFFFF, format).br();
        if (format.contains("video/webm")) {

        } else {
            html.font(0xFFCAE682, "Resolusi :")
                .append(' ')
                .font(0xFFFFFFFF, width + "x" + height).br();

        }
        html.font(0xFFCAE682, "Bitrate :")
            .append(' ')
            .font(0xFFFFFFFF, bps + "Mbps").br();
        html.font(0xFFCAE682, "Ukuran File :")
            .append(' ')
            .font(0xFFFFFFFF, Formatter.formatFileSize(mContext, fileSize)).br();
        html.font(0xFFCAE682, "Duration :")
            .append(' ')
            .font(0xFFFFFFFF, getTimeString(Integer.valueOf(duration))).br();   
        html.font(0xFFCAE682, "Terakhir DiUbah :")
            .append(' ')
            .font(0xFFFFFFFF, formatter.format(d)).br();  
        html.font(0xFFCAE682, "Alur :")
            .append(' ')
            .font(0xFFFFFFFF, f.getAbsolutePath()).br();  


        return html.build();
    }
    
    private String getTimeString(int duration) {
        final StringBuilder sb = new StringBuilder(8);
        final int hours = duration / (60 * 60);
        final int minutes = (duration % (60 * 60)) / 60;
        final int seconds = ((duration % (60 * 60)) % 60);

        if (duration > 3600) {
            sb.append(String.format(Locale.getDefault(), "%02d", hours)).append(":");
        }

        sb.append(String.format(Locale.getDefault(), "%02d", minutes));
        sb.append(":").append(String.format(Locale.getDefault(), "%02d", seconds));

        return sb.toString();
    }
}
