package com.github.videobox.utils;

import android.support.v7.widget.CardView;
import android.content.Context;
import android.util.AttributeSet;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import com.github.videobox.R;

public class VideoConvert extends CardView {
    private float ratio = 1.2f;
    private TextView vName;
    private TextView vStatus;
    private TextView vOutput;
    public VideoConvert(Context context) {
        this(context, null);
    }

    public VideoConvert(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoConvert(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
   
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        setKeepScreenOn(true);

        // Instantiate and add VideoView for rendering
        final LayoutInflater li = LayoutInflater.from(getContext());
        View mVideoFrame = li.inflate(R.layout.video_convert_layout, this, false);
        addView(mVideoFrame);
        vName = (TextView) mVideoFrame.findViewById(R.id.current_package_name);
        vName.setSingleLine(false);
        vName.setEllipsize(TextUtils.TruncateAt.END);
        vName.setLines(1);
        
        vStatus = (TextView) mVideoFrame.findViewById(R.id.current_status);
        vStatus.setText("Convert To Mp3");
        vStatus.setSingleLine(false);
        vStatus.setEllipsize(TextUtils.TruncateAt.END);
        vStatus.setMovementMethod(LinkMovementMethod.getInstance()); 
        vStatus.setLines(1);

        vOutput = (TextView) mVideoFrame.findViewById(R.id.current_line);
        /* Gear Progress */
        final ImageView GearProgressLeft = (ImageView) findViewById(R.id.gear_progress_left);
        final ImageView GearProgressRight = (ImageView) findViewById(R.id.gear_progress_right);

        final RotateAnimation GearProgressLeftAnim = new RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        GearProgressLeftAnim.setRepeatCount(Animation.INFINITE);
        GearProgressLeftAnim.setDuration((long) 2 * 1500);
        GearProgressLeftAnim.setInterpolator(new LinearInterpolator());

        final RotateAnimation GearProgressRightAnim = new RotateAnimation(360.0f, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        GearProgressRightAnim.setRepeatCount(Animation.INFINITE);
        GearProgressRightAnim.setDuration((long) 1500);
        GearProgressRightAnim.setInterpolator(new LinearInterpolator());

        GearProgressLeft.post(new Runnable() {
                @Override
                public void run()
                {
                    GearProgressLeft.setAnimation(GearProgressLeftAnim);
                }
            });
        GearProgressLeft.post(new Runnable() {
                @Override
                public void run()
                {
                    GearProgressRight.setAnimation(GearProgressRightAnim);
                }
            });
    }
    
    public void setTitle(String title){
        vName.setText(title);
    }
    
    public void setMessage(String message){
        vOutput.setText(message);
    }
    
    public void setStatus(String status){
        vStatus.setText(status);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (ratio > 0) {
            int ratioHeight = (int) (getMeasuredWidth() * ratio);
            setMeasuredDimension(getMeasuredWidth(), ratioHeight);
            ViewGroup.LayoutParams lp = getLayoutParams();
            lp.height = ratioHeight;
            setLayoutParams(lp);
        }
    }
}

