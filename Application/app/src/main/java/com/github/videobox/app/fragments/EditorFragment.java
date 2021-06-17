package com.github.videobox.app.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.github.videobox.R;

public class EditorFragment extends Fragment
{
    
    public static String TAG = EditorFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video_cutter, container, false);
    }
    
}

