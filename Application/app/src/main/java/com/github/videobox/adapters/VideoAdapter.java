package com.github.videobox.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;

import com.github.videobox.R;
import com.github.videobox.models.VideoModel;
import com.bumptech.glide.Glide;
import com.example.library.banner.BannerLayout;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.MzViewHolder> {

    private Context context;
    private ArrayList<VideoModel> urlList;
    private BannerLayout.OnBannerItemClickListener onBannerItemClickListener;

    public VideoAdapter(Context context, ArrayList<VideoModel> urlList) {
        this.context = context;
        this.urlList = urlList;
    }

    public void setOnBannerItemClickListener(BannerLayout.OnBannerItemClickListener onBannerItemClickListener) {
        this.onBannerItemClickListener = onBannerItemClickListener;
    }

    @Override
    public VideoAdapter.MzViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MzViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.video_row, parent, false));
    }

    @Override
    public void onBindViewHolder(VideoAdapter.MzViewHolder holder, final int position) {
        if (urlList == null || urlList.isEmpty())
            return;
        final int P = position % urlList.size();
        VideoModel video = urlList.get(P);
        ImageView thumbnail = (ImageView) holder.imageView;
        Glide.with(context)
            .load(video.getVideoThumb())
            .placeholder(R.drawable.video_placeholder)
            .into(thumbnail);

        thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onBannerItemClickListener != null) {
                        onBannerItemClickListener.onItemClick(P);
                    }

                }
            });
    }

    @Override
    public int getItemCount() {
        if (urlList != null) {
            return urlList.size();
        }
        return 0;
    }


    class MzViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        MzViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.video_thumbnail);
        }
    }

}


