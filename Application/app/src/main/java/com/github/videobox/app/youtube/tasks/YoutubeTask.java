package com.github.videobox.app.youtube.tasks;

import com.github.videobox.R;
import com.github.videobox.app.youtube.data.YoutubeData;
import com.github.videobox.app.youtube.data.YoutubeVideoDb;
import com.github.videobox.app.youtube.config.EndPoints;
import com.github.videobox.app.youtube.config.JsonKeys;
import com.github.videobox.app.youtube.config.YoutubeConfig;
import com.github.videobox.app.fragments.VideoFragment;

import android.app.ProgressDialog;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class YoutubeTask extends AsyncTask<Void, Void, ArrayList<YoutubeData>> {


	//private ProgressDialog progressDialog;
	private Context mContext;
	private String videoId;
    private YoutubeData storeData;
    private YoutubeVideoDb mDataBase;
    private VideoFragment videoFragment;
    private View mProgressLayout;
    private View mVideoPlayer;
    
	public YoutubeTask(Context context, FragmentManager fm, String videoId, View mProgressLayout, View mVideoPlayer) {
		this.mContext = context;
		this.videoId = videoId;
        this.mProgressLayout = mProgressLayout;
        this.mVideoPlayer = mVideoPlayer;
        this.videoFragment = (VideoFragment) fm.findFragmentById(R.id.video_fragment_container);   
        this.mDataBase = new YoutubeVideoDb(mContext);
		this.storeData = new YoutubeData(mContext, YoutubeData.FILENAME);
	}

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
        mVideoPlayer.setVisibility(View.GONE);
		mProgressLayout.setVisibility(View.VISIBLE);
	}

	@Override
	protected ArrayList<YoutubeData> doInBackground(Void... params) {
		// TODO Auto-generated method stub
		ArrayList<YoutubeData> videoList = new ArrayList<YoutubeData>();
		try {

			String  URL =  EndPoints.VIDEO_DETAILS_URL + "&id=" + videoId;

			String response = getUrlString(URL);

			JSONObject json = new JSONObject(response.toString());

			JSONArray jsonArray = json.getJSONArray(JsonKeys.ITEMS);
			for (int i = 0; i < jsonArray.length(); i++) {
				YoutubeData video = new YoutubeData(mContext);

                JSONObject itemObject = jsonArray.getJSONObject(i);
                video.setVideoId(itemObject.getString(JsonKeys.ID));
                JSONObject snippet = itemObject.getJSONObject(JsonKeys.SNIPPET);
                video.setChannelTitle(snippet.getString(JsonKeys.CHANNEL_TITLE));
                video.setPublishedAt(snippet.getString(JsonKeys.PUBLISHED_AT));
                video.setChannelId(snippet.getString(JsonKeys.CHANNL_ID));
                video.setVideoTitle(snippet.getString(JsonKeys.VIDEO_TITLE));
                video.setDescription(snippet.getString(JsonKeys.DESCRIPTION));
                JSONObject thumbnails = snippet.getJSONObject(JsonKeys.THUMBNAILS);
                video.setSmallThumbnail(thumbnails.getJSONObject(JsonKeys.DEFAULT_THUMBNAIL).getString(JsonKeys.URL));
                video.setMediumThumbnail(thumbnails.getJSONObject(JsonKeys.MEDIUM_THUMBNAIL).getString(JsonKeys.URL));
                video.setLargeThumbnail(thumbnails.getJSONObject(JsonKeys.HIGH_THUMBNAIL).getString(JsonKeys.URL));
                JSONObject contentDetails = itemObject.getJSONObject(JsonKeys.CONTENT_DETAILS);
                video.setDuration(contentDetails.getString(JsonKeys.DURATION));
                JSONObject statistics = itemObject.getJSONObject(JsonKeys.STATISTICS);
                video.setViewCount(statistics.getString(JsonKeys.VIEW_COUNT));
                video.setLikeCount(statistics.getString(JsonKeys.LIKE_COUNT));
                video.setDislikeCount(statistics.getString(JsonKeys.DISLIKE_COUNT));
                video.setFavouriteCount(statistics.getString(JsonKeys.FAVORITE_COUNT));
                video.setCommentCount(statistics.getString(JsonKeys.COMMENT_COUNT));
                videoList.add(video);
                
                try {
                    storeData.saveToFile(videoList);
                    storeData.initialise(video);
                    mDataBase.addPlaylist(video);
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		return videoList;

	}

	@Override
	protected void onPostExecute(ArrayList<YoutubeData> result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
        if (result.size() < 1) {
            mProgressLayout.setVisibility(View.GONE);
            mVideoPlayer.setVisibility(View.GONE);
        } else {
            mProgressLayout.setVisibility(View.GONE);
            mVideoPlayer.setVisibility(View.VISIBLE);
            videoFragment.setVideoId(result.get(0).getVideoId());
        }
		
	}



    private byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() +
									  ": with " +
									  urlSpec);
            }
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    private String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

}
