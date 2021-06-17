package com.github.videobox;

import android.support.v4.view.ViewPager;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.support.v4.app.FragmentActivity;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import com.github.videobox.app.fragments.EditorFragment;
import com.github.videobox.app.fragments.RecentFragment;

import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

public class VideoEditorActivity extends FragmentActivity {
    
    public static void start(Context c)
    {
        Intent intent = new Intent(c, VideoEditorActivity.class);
        c.startActivity(intent);
    }
    private SmartTabLayout viewPagerTab;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_editor);
        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
            getSupportFragmentManager(), FragmentPagerItems.with(this)
            .add("Editor", EditorFragment.class)
            .add("Recent", RecentFragment.class)
            .create());
        ViewPager viewPager = (ViewPager) findViewById(R.id.container);
        viewPager.setAdapter(adapter);


        viewPagerTab = (SmartTabLayout) findViewById(R.id.viewPageTab);
        viewPagerTab.setViewPager(viewPager);
     }
}
