package com.github.videobox;

import android.app.Application;
import android.content.Context;

import com.github.videobox.app.converter.AudioConverter;
import com.github.videobox.app.converter.callback.ILoadCallback;
import com.github.videobox.utils.SharedPref;

public class VideoBoxApplication extends Application {
    
    public static SharedPref sharedPref;
    private static SharedPref appPreferences = null;
    
    @Override
    public void onCreate() {
        super.onCreate();
        sharedPref = new SharedPref(getApplicationContext());
        sharedPref.setFirstTimeLaunch(true);
        AudioConverter.load(this, new ILoadCallback() {
                @Override
                public void onStart() {

                }

                @Override
                public void onSuccess() {
                    // Great!
                }
                @Override
                public void onFailure(Exception error) {
                    // FFmpeg is not supported by device
                    error.printStackTrace();
                }
                
                @Override
                public void onFinish() {

                }
            });
    }
    
    public static SharedPref getAppPreferences(Context c)
    {
        if (appPreferences == null)
            appPreferences = SharedPref.loadPreferences(c);

        return appPreferences;
	}
}
