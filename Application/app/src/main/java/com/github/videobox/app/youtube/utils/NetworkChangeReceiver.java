package com.github.videobox.app.youtube.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;


public class NetworkChangeReceiver extends BroadcastReceiver {
    static boolean flag = false;

    @Override
    public void onReceive(final Context context, final Intent intent) {

        int status = NetworkUtil.getConnectivityStatusString(context);
        if(status == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
            Toast.makeText(context, "No Internet", Toast.LENGTH_LONG).show();
           // Intent retry = new Intent(context, NoInternetActivity.class);
            flag = true;
           // context.startActivity(retry);
        }
    }
}
