package com.github.videobox.app.converter.callback;

import java.io.File;

public interface IConvertCallback {
    void onStart();
    
    void onSuccess(File convertedFile);
    
    void onFailure(Exception error);

    void onFinish();
}
