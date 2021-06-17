package com.github.videobox.app.converter.callback;

public interface ILoadCallback {
    
    void onStart();
    
    void onSuccess();
    
    void onFailure(Exception error);
    
    void onFinish();
}
