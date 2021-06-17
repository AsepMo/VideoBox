package com.github.videobox.app.youtube.data;

import android.content.Context;
import android.os.Environment;
import android.text.format.DateUtils;
import android.util.Log;

import com.github.videobox.YouTubePreviewActivity;
import com.github.videobox.utils.VideoFolder;
import com.github.videobox.app.youtube.utils.TimeAgo;
import com.github.videobox.app.youtube.utils.YouTubeTimeConvert;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.apache.commons.io.FileUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.net.HttpURLConnection;
import java.net.URL;

public class YoutubeData implements Serializable {
    private String kind;
    private String id;
    private String videoTitle;
    private String channelTitle;
    private String videoId;
    private String channelId;
    private String description;
    private String smallThumbnail;
    private String mediumThumbnail;
    private String largeThumbnail;
    private String duration;
    private String viewCount;
    private String likeCount;
    private String dislikeCount;
    private String favouriteCount;
    private String commentCount;
    private String publishedAt;

    public static String TAG = YoutubeData.class.getSimpleName();
    public static String FOLDER = VideoFolder.ZFOLDER_YOUTUBE_ANALYTICS;
    public static String FILENAME = "youtube.json";
    
    private static Context mContext;
    private static String mFileName;
    
    public static String URL = "https://drive.google.com/uc?export=download&id=1QW12zqOIraKRCVOeriBCbB47QD0G-6By";
    
    //add description
    public static final String TITLE = "title";
    public static final String THUMBNAILS = "thumbnails";
    public static final String DESCRIPTIONS = "description";
    public static final String VIDEOID = "videoId";
    public static final String PUBLISHED = "publishedAt";
    
    public YoutubeData(Context activity)
    {
        this.mContext = activity;
    }
    
    public YoutubeData(Context context, String filename)
    {
        mContext = context;
        mFileName = filename;
    }
    
    public String getTimeAgo() {
        return timeAgo;
    }

    public void setTimeAgo(String timeAgo) {
        this.timeAgo = timeAgo;
    }

    private String timeAgo;

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {

        try {
            DateTimeFormatter parser = ISODateTimeFormat.dateTimeParser();
            DateTime dt = parser.parseDateTime(publishedAt);

            long videoSecs = (dt.getMillis())/1000;
            long nowSecs = (new Date().getTime())/1000;
            long secs = nowSecs - videoSecs;
            setTimeAgo(TimeAgo.getTimeAgo(secs));
        } catch (Exception e) {
            e.printStackTrace();
            setTimeAgo("NA");
        } finally {
            this.publishedAt = publishedAt;
        }
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVideoTitle() {
        return videoTitle;
    }

    public void setVideoTitle(String videoTitle) {
        this.videoTitle = videoTitle;
    }

    public String getChannelTitle() {
        return channelTitle;
    }

    public void setChannelTitle(String channelTitle) {
        this.channelTitle = channelTitle;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSmallThumbnail() {
        return smallThumbnail;
    }

    public void setSmallThumbnail(String smallThumbnail) {
        this.smallThumbnail = smallThumbnail;
    }

    public String getMediumThumbnail() {
        return mediumThumbnail;
    }

    public void setMediumThumbnail(String mediumThumbnail) {
        this.mediumThumbnail = mediumThumbnail;
    }

    public String getLargeThumbnail() {
        return largeThumbnail;
    }

    public void setLargeThumbnail(String largeThumbnail) {
        this.largeThumbnail = largeThumbnail;
    }

    public String getDuration() {
        return YouTubeTimeConvert.convertYouTubeDuration(duration);
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getViewCount() {
        if(Integer.parseInt(viewCount) > 1000) {
            return viewCount.substring(0, viewCount.length() - 3) + "k";
        }
        return viewCount;
    }

    public void setViewCount(String viewCount) {
        this.viewCount = viewCount;
    }

    public String getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(String likeCount) {
        this.likeCount = likeCount;
    }

    public String getDislikeCount() {
        return dislikeCount;
    }

    public void setDislikeCount(String dislikeCount) {
        this.dislikeCount = dislikeCount;
    }

    public String getFavouriteCount() {
        return favouriteCount;
    }

    public void setFavouriteCount(String favouriteCount) {
        this.favouriteCount = favouriteCount;
    }

    public String getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(String commentCount) {
        this.commentCount = commentCount;
    }

    public void initialise(YoutubeData video)
    {
        try
        {
            JSONObject json = new JSONObject();
            json.put("title", video.getVideoTitle());
            json.put("thumbnails", video.getMediumThumbnail());
            json.put("videoId", video.getVideoId());
            json.put("description", video.getDescription());
            json.put("publishedAt", video.getPublishedAt());
            
            String filePath = FOLDER + "/yt_initialise.json";
            File file = new File(filePath);
            //file.getParentFile().mkdirs();
            FileUtils.writeStringToFile(file, json.toString());
        }
        catch (IOException | JSONException e)
        {
            e.printStackTrace();
        }
    }
    
	public YoutubeData(JSONObject jsonObject) throws JSONException
    {
        videoTitle = jsonObject.getString(TITLE);
        mediumThumbnail = jsonObject.getString(THUMBNAILS);
        description = jsonObject.getString(DESCRIPTIONS);
        videoId = jsonObject.getString(VIDEOID);
        publishedAt = jsonObject.getString(PUBLISHED);
     }

    public JSONObject toJSON() throws JSONException
    {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TITLE, videoTitle);
        jsonObject.put(THUMBNAILS, mediumThumbnail);
        jsonObject.put(DESCRIPTIONS, description);
        jsonObject.put(VIDEOID, videoId);
        jsonObject.put(PUBLISHED, publishedAt);
        
        return jsonObject;
    }
    
	
    public static JSONArray toJSONArray(ArrayList<YoutubeData> items) throws JSONException
	{
        JSONArray jsonArray = new JSONArray();
        for (YoutubeData item : items)
		{
            JSONObject jsonObject = item.toJSON();
            jsonArray.put(jsonObject);
        }
        return jsonArray;
    }

    public void saveToFile(ArrayList<YoutubeData> items) throws JSONException, IOException
	{

		File file = new File(FOLDER, mFileName);
		file.getParentFile().mkdirs();
		try
		{
			//FileOutputStream fos = mContext.openFileOutput(mFileName, Context.MODE_PRIVATE);
			FileOutputStream fos = new FileOutputStream(file);
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			osw.write(toJSONArray(items).toString());
			osw.write('\n');
			osw.flush();
			fos.flush();
			fos.getFD().sync();
			fos.close();

			Log.d(TAG, toJSONArray(items).toString());
		}
		catch (IOException e)
		{
			Log.e(TAG, "Exception writing to file", e);
		}
    }

	public void saveToFile(String file, ArrayList<YoutubeData> items) throws JSONException, IOException
	{

		try
		{
			FileOutputStream fos = mContext.openFileOutput(file, Context.MODE_PRIVATE);
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			osw.write(toJSONArray(items).toString());
			osw.write('\n');
			osw.flush();
			fos.flush();
			fos.getFD().sync();
			fos.close();

			Log.d(TAG, toJSONArray(items).toString());
		}
		catch (IOException e)
		{
			Log.e(TAG, "Exception writing to file", e);
		}
    }

    public ArrayList<YoutubeData> loadFromFile() throws IOException, JSONException
	{
        ArrayList<YoutubeData> items = new ArrayList<YoutubeData>();
        BufferedReader bufferedReader = null;
        FileInputStream fileInputStream = null;
		File file = new File(FOLDER, mFileName);
        try
		{
            fileInputStream = new FileInputStream(file);
            StringBuilder builder = new StringBuilder();
            String line;
            bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            while ((line = bufferedReader.readLine()) != null)
			{
                builder.append(line);
            }

            JSONArray jsonArray = (JSONArray) new JSONTokener(builder.toString()).nextValue();
            for (int i = 0; i < jsonArray.length(); i++)
			{
                YoutubeData item = new YoutubeData(jsonArray.getJSONObject(i));
                items.add(item);
            }


        }
		catch (FileNotFoundException fnfe)
		{
            //do nothing about it
            //file won't exist first time app is run
        }
		finally
		{
            if (bufferedReader != null)
			{
                bufferedReader.close();
            }
            if (fileInputStream != null)
			{
                fileInputStream.close();
            }

        }
        return items;
    }

	public static ArrayList<YoutubeData> loadFromFile(String file) throws IOException, JSONException
	{
        ArrayList<YoutubeData> items = new ArrayList<YoutubeData>();
        BufferedReader bufferedReader = null;
        FileInputStream fileInputStream = null;
		File files = new File(file);
        try
		{
            fileInputStream = new FileInputStream(files);
            StringBuilder builder = new StringBuilder();
            String line;
            bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            while ((line = bufferedReader.readLine()) != null)
			{
                builder.append(line);
            }

            JSONArray jsonArray = (JSONArray) new JSONTokener(builder.toString()).nextValue();
            for (int i = 0; i < jsonArray.length(); i++)
			{
                YoutubeData item = new YoutubeData(jsonArray.getJSONObject(i));
                items.add(item);
            }


        }
		catch (FileNotFoundException fnfe)
		{
            //do nothing about it
            //file won't exist first time app is run
        }
		finally
		{
            if (bufferedReader != null)
			{
                bufferedReader.close();
            }
            if (fileInputStream != null)
			{
                fileInputStream.close();
            }

        }
        return items;
    }

	public static void saveData(Context c, String title, String thumbUrl, String videoId, String description, String published) 
	{
		YoutubeData storeData = new YoutubeData(c, "youtube_recent.json");
        //mToDoItemsArrayList = getLocallyStoredData(storeRetrieveData);

		ArrayList<YoutubeData> items = new ArrayList<YoutubeData>();
		YoutubeData displaylist = new YoutubeData(c);
        displaylist.setVideoTitle(title);
        displaylist.setMediumThumbnail(thumbUrl);
        displaylist.setVideoId(videoId);
        displaylist.setDescription(description);
        displaylist.setPublishedAt(published);
		items.add(displaylist);
        try
		{
            storeData.saveToFile(items);
        }
		catch (JSONException | IOException e)
		{
            e.printStackTrace();
        }
    }

	public static ArrayList<YoutubeData> getLocallyStoredData(Context c)
	{
        ArrayList<YoutubeData> items = null;
        YoutubeData storeData = new YoutubeData(c, FILENAME);
      
        try
		{
            items = storeData.loadFromFile();

        }
		catch (IOException | JSONException e)
		{
            e.printStackTrace();
        }

        if (items == null)
		{
            items = new ArrayList<>();
        }
        return items;
    }
	
    public static String geYouTubeTitle()
    {
        try
        {
            File infoFile = new File(FOLDER + "/yt_initialise.json");
            JSONObject json = new JSONObject(FileUtils.readFileToString(infoFile));
            return json.getString("title");
        }
        catch (IOException | JSONException e)
        {
            return null;
        }    
    }

    public static String getYouTubePlaying()
    {
        try
        {
            File infoFile = new File(FOLDER + "/yt_initialise.json");
            JSONObject json = new JSONObject(FileUtils.readFileToString(infoFile));
            return json.getString("videId");
        }
        catch (IOException | JSONException e)
        {
            return null;
        }    
    }
    
	public static byte[] getUrlBytes(String urlSpec) throws IOException
	{
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        try
		{
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
			{
                throw new IOException(connection.getResponseMessage() +
									  ": with " +
									  urlSpec);
            }
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0)
			{
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        }
		finally
		{
            connection.disconnect();
        }
    }

    public static String getUrlString(String urlSpec) throws IOException
	{
        return new String(getUrlBytes(urlSpec));
    }
}
