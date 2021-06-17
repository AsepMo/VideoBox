package com.github.videobox.app.youtube.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Parcel;
import android.os.Parcelable;


public class YoutubeVideo implements Parcelable {
	private static String PREF_HIGH_QUALITY = "pref_high_quality";
	
	private int mId; //id in database
	private String mTitle; // file name
    private String mThumbnail; //file path
    private String mVideoId; // length of recording in seconds
    private long mTime; // date/time of the recording

    public YoutubeVideo()
    {
    }

    public YoutubeVideo(Parcel in) {
        mTitle = in.readString();
        mThumbnail = in.readString();
        mId = in.readInt();
        mVideoId= in.readString();
        mTime = in.readLong();
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getThumbnail() {
        return mThumbnail;
    }

    public void setThumbnail(String thumbnail) {
        mThumbnail = thumbnail;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getVideoId() {
        return mVideoId;
    }

    public void setVideoId(String videoId) {
        mVideoId = videoId;
    }

    public long getTime() {
        return mTime;
    }

    public void setTime(long time) {
        mTime = time;
    }
	
    public static void setPrefHighQuality(Context context, boolean isEnabled) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(PREF_HIGH_QUALITY, isEnabled);
        editor.apply();
    }

    public static boolean getPrefHighQuality(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(PREF_HIGH_QUALITY, false);
    }
	
    public static final Parcelable.Creator<YoutubeVideo> CREATOR = new Parcelable.Creator<YoutubeVideo>() {
        public YoutubeVideo createFromParcel(Parcel in) {
            return new YoutubeVideo(in);
        }

        public YoutubeVideo[] newArray(int size) {
            return new YoutubeVideo[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeLong(mTime);
        dest.writeString(mTitle);
        dest.writeString(mThumbnail);
		dest.writeString(mVideoId);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
