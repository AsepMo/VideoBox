package com.github.videobox.app.fragments;

import android.support.v4.app.Fragment;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

import static android.provider.MediaStore.Images.Thumbnails.MICRO_KIND;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;

import com.github.videobox.R;
import com.github.videobox.models.FileItem;
import com.github.videobox.utils.FileTools;
import com.github.videobox.utils.IntentUtils;
import com.github.videobox.utils.Thumbnail;
import com.github.videobox.utils.VideoFolder;

public class RecentFragment extends Fragment
{

    public static String TAG = EditorFragment.class.getSimpleName();
    private LinearLayout welcomeLayout;
    private LinearLayout loadingLayout;
    private ListView listView;
    private String currentFolder = VideoFolder.ZFOLDER_VIDEO_CONVERTED;
    private String[] extensions = new String[]{FileTools.MP4};
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video_recent, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listView = (ListView)view.findViewById(R.id.history_list);
        
        View header = getLayoutInflater().inflate(R.layout.history_header_view, listView, false);
        listView.addHeaderView(header, null, false);

        loadingLayout = (LinearLayout)view.findViewById(R.id.layoutMessage);     
        welcomeLayout = (LinearLayout)view.findViewById(R.id.welcome_layout);
        
        UpdateList(currentFolder, extensions);
    }

    private void UpdateList(String path, String[] extensions) {
        HistoryLoader historyLoaderTwo = new HistoryLoader(getActivity(), listView, path, extensions);
        historyLoaderTwo.execute();
    }

    private static class ViewHolder {
        CardView cardView;
        TextView fileName;
        TextView fileSize;
        TextView fileDate;       
        ImageView fileThumbnail;
        int position;
    }

    private class HistoryLoader extends AsyncTask<String, String, List<FileItem>> {

        private ListView listView;
        private List<FileItem> historyItems;
        private String[] extensions;
        private Context mContext;
        private String path;

        
        public HistoryLoader(Context context, ListView listView, String path, String[] extensions) {
            this.mContext = context;
            this.listView = listView;
            this.path = path;
            this.extensions = extensions;
            
        }

        @Override
        protected void onPreExecute() {
            loadingLayout.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
            welcomeLayout.setVisibility(View.GONE);
        }

        @Override
        protected void onProgressUpdate(String... text) {

        }

        @Override
        protected void onCancelled(List<FileItem> result) {
            super.onCancelled(result);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            getActivity().finish();
        }

        @Override
        protected List<FileItem> doInBackground(String... params) {

            historyItems = new ArrayList<>();
            File MP4 = new File(path);
            if (MP4.exists()) {
                listOfFile(MP4);
            }

            return historyItems;
        }

        private void listOfFile(File dir) {
            File[] list = dir.listFiles();

            for (File file : list) {
                if (file.isDirectory()) {
                    if (!new File(file, ".nomedia").exists() && !file.getName().startsWith(".")) {
                        Log.w("LOG", "IS DIR " + file);
                        listOfFile(file);
                    }
                } else {
                    String path = file.getAbsolutePath();
                    //String[] extensions = new String[]{".mp4"};
                    for (String ext : extensions) {
                        if (path.endsWith(ext)) {
                            FileItem videoInfo = new FileItem();
                            String[] split = path.split("/");
                            String mTitle = split[split.length - 1];
                            videoInfo.setFileName(mTitle);
                            videoInfo.setFilePath(file.getAbsolutePath());
                            videoInfo.setFileSize(FileUtils.byteCountToDisplaySize(file.length()));
                            videoInfo.setFileThumbnail(path);
                            SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy  hh:mm a");
                            String date = format.format(file.lastModified());
                            videoInfo.setFileModified(date);
                            historyItems.add(videoInfo);
                            Log.i("LOG", "ADD " + videoInfo.getFileName() + " " + videoInfo.getFileThumbnail());
                        }
                    }
                }
            }
            Log.d("LOG", historyItems.size() + " DONE");
        }

        @Override
        protected void onPostExecute(final List<FileItem> AllVideo) {
            if (AllVideo.size() < 1) {
                loadingLayout.setVisibility(View.GONE);
                listView.setVisibility(View.GONE);
                welcomeLayout.setVisibility(View.VISIBLE);             
            } else {
                loadingLayout.setVisibility(View.GONE); 
                welcomeLayout.setVisibility(View.INVISIBLE);

                final ArrayAdapter<FileItem> videoAdapter = new ArrayAdapter<FileItem>(getActivity(), R.layout.history_list_item, AllVideo) {
                    @SuppressLint("InflateParams")
                    @Override
                    public View getView(final int position, View convertView, ViewGroup parent) {
                        if (convertView == null) {
                            convertView = getLayoutInflater().inflate(R.layout.history_list_item, null);
                        }

                        final FileItem file = getItem(position);

                        ViewHolder holder = new ViewHolder();

                        holder.cardView = (CardView) convertView.findViewById(R.id.card_layout);    
                        holder.fileName = (TextView) convertView.findViewById(R.id.history_item_label);
                        holder.fileSize = (TextView) convertView.findViewById(R.id.history_item_size);
                        holder.fileDate = (TextView) convertView.findViewById(R.id.history_item_date);                  
                        holder.fileThumbnail = (ImageView) convertView.findViewById(R.id.history_item_icon);

                        convertView.setTag(holder);

                        holder.cardView.setBackgroundColor(R.color.colorPrimary);
                        holder.fileName.setText(file.getFileName());
                        holder.fileSize.setText(file.getFileSize());
                        holder.fileDate.setText(file.getFileModified());

                        Thumbnail.setThumbnail(file.getFileThumbnail(), MICRO_KIND, holder.fileThumbnail, false);
                        convertView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    final FileItem video = AllVideo.get(position);
                                    IntentUtils.openFile(getActivity(), new File(video.getFilePath()));                          
                                    Toast.makeText(getActivity(), video.getFilePath(), Toast.LENGTH_SHORT).show();              

                                }
                            });
                        return convertView;
                    }
                };
                
                listView.setAdapter(videoAdapter);
                listView.setVisibility(View.VISIBLE);
            }
        }

    }
}

