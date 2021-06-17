package com.github.videobox.models;

public class VideoModel {
    private String mVideoName;
    private String mVideoPath;
    private String mVideoThumb;
    private String mVideoSize;
    private String mPath;
    private boolean isSelected;

    public String getVideoTitle() {
        return mVideoName;
    }

    public void setVideoTitle(String mFileName) {
        this.mVideoName = mFileName;
    }

    public String getVideoPath() {
        return mVideoPath;
    }

    public void setPathName(String mPath) {
        this.mPath = mPath;
    }

    public String getPathName() {
        return mPath;
    }

    public void setVideoPath(String mFilePath) {
        this.mVideoPath = mFilePath;
    }

    public String getVideoThumb() {
        return mVideoThumb;
    }

    public void setVideoThumb(String mVideoThumb) {
        this.mVideoThumb = mVideoThumb;
    }

    public void setVideoSize(String mVideoSize) {
        this.mVideoSize = mVideoSize;
    }

    public String getVideoSize() {
        return mVideoSize;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}


