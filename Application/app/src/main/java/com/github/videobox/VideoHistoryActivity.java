package com.github.videobox;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Menu;
import android.view.MenuItem;
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

import com.github.videobox.app.permissions.PermissionsManager;
import com.github.videobox.app.permissions.PermissionsResultAction;
import com.github.videobox.models.VideoModel;
import com.github.videobox.models.ActionItem;
import com.github.videobox.models.FileItem;
import com.github.videobox.utils.FileTools;
import com.github.videobox.utils.IntentUtils;
import com.github.videobox.utils.Thumbnail;
import com.github.videobox.utils.SharedPref;
import com.github.videobox.utils.DirectoryNavigationView;
import com.github.videobox.listeners.onUpdatePathListener;
import com.github.videobox.widget.QuickAction;
public class VideoHistoryActivity extends Activity implements DirectoryNavigationView.OnNavigateListener, onUpdatePathListener {
    public final static String ACTION_SHOW_FOLDER = "com.github.folders.action.saveFolder";
    public final static String ACTION_SHOW_HISTORY = "com.github.folders.action.saveHistory";
    public static final int SELECT_FILE_CODE = 121;
    public static final int SELECT_FOLDER_CODE = 122;
    
    public static String FOLDER = "folders";
    private LinearLayout welcomeLayout;
    private LinearLayout loadingLayout;
    private ListView listView;
    private String currentFolder;
    private String[] extensions = new String[]{FileTools.MP4};
    private static DirectoryNavigationView mNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_loader_layout); 
        String action = getIntent().getAction();
        listView = (ListView)findViewById(R.id.history_list);
        View header = getLayoutInflater().inflate(R.layout.history_header_view, listView, false);
        listView.addHeaderView(header, null, false);

        loadingLayout = (LinearLayout)findViewById(R.id.layoutMessage);     
        welcomeLayout = (LinearLayout)findViewById(R.id.welcome_layout);

        mNavigation = new DirectoryNavigationView(this);

        // add listener for navigation view
        if (mNavigation.listeners.isEmpty())
            mNavigation.addonNavigateListener(this);

        if (action != null && action.equals(ACTION_SHOW_HISTORY)) {
            currentFolder = getIntent().getStringExtra(FOLDER);
            if (TextUtils.isEmpty(currentFolder)) {
                String lastNavigatedPath = SharedPref.getWorkingFolder(this);

                File file = new File(lastNavigatedPath);

                if (!file.exists()) {
                    SharedPref.setWorkingFolder(this, SharedPref.SD_CARD_ROOT);
                    file = new File(SharedPref.SD_CARD_ROOT);
                }

                UpdateList(file.getAbsolutePath(), extensions);
            } else {
                UpdateList(currentFolder, extensions);
            }

        } else {
            currentFolder = SharedPref.SD_CARD_ROOT + "/VideoBox";
            if (TextUtils.isEmpty(currentFolder)) {
                String lastNavigatedPath = SharedPref.getWorkingFolder(this);

                File file = new File(lastNavigatedPath);

                if (!file.exists()) {
                    SharedPref.setWorkingFolder(this, SharedPref.SD_CARD_ROOT);
                    file = new File(SharedPref.SD_CARD_ROOT);
                }

                UpdateList(file.getAbsolutePath(), extensions);
            } else {
                UpdateList(currentFolder, extensions);
            }        
        }

    }

    @Override
    public void onNavigate(String path) {
        currentFolder = path;
        if (currentFolder.isEmpty() || currentFolder.equals("/")) {
            finish();
        } else if (currentFolder.isEmpty() || currentFolder.equals("/storage")) {
            finish();
        } else if (currentFolder.isEmpty() || currentFolder.equals("/storage/emulated")) {
            finish();
        } else if (currentFolder.isEmpty() || currentFolder.equals("/storage/extSdCard")) {
            finish();
        } else {
            File file = new File(currentFolder);
            String parentFolder = file.getParent();
            UpdateList(parentFolder, extensions);
        }
    }

    @Override
    public void onUpdatePath(String path) {
        currentFolder = path;
        mNavigation.setDirectoryButtons(currentFolder);    
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mNavigation != null)
            mNavigation.removeOnNavigateListener(this);
    }

    @Override
    public void onBackPressed() {
        if (currentFolder.isEmpty() || currentFolder.equals("/")) {
            finish();
        } else if (currentFolder.isEmpty() || currentFolder.equals("/storage")) {
            finish();
        } else if (currentFolder.isEmpty() || currentFolder.equals("/storage/emulated")) {
            finish();
        } else if (currentFolder.isEmpty() || currentFolder.equals("/storage/extSdCard")) {
            finish();
        } else {
            File file = new File(currentFolder);
            String parentFolder = file.getParent();
            UpdateList(parentFolder, extensions);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO: Implement this method

        menu.add("Folder")
            .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    Intent intent = new Intent(getApplication(), VideoSelectorActivity.class);
                    intent.putExtra("path" , "");
                    intent.putExtra("action", VideoSelectorActivity.Actions.SelectFolder);
                    startActivityForResult(intent, SELECT_FOLDER_CODE);
                    return true;
                }
            }).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.add("History")
            .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    String lastNavigatedPath = SharedPref.getWorkingFolder(VideoHistoryActivity.this);

                    Intent intent = new Intent(getApplication(), VideoHistoryActivity.class);
                    intent.setAction(ACTION_SHOW_HISTORY);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra(VideoHistoryActivity.FOLDER, lastNavigatedPath);            
                    startActivity(intent);
                    return true;
                }
            })
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM); 
        getMenuInflater().inflate(R.menu.menu_history, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String lastNavigatedPath = SharedPref.getWorkingFolder(this);
                
        switch (item.getItemId()) {         
            case R.id.action_apk:
                UpdateList(lastNavigatedPath, new String[]{ FileTools.APK});
                return true;
            case R.id.action_mp3:
                UpdateList(lastNavigatedPath, new String[]{ FileTools.MP3});
                return true;
            case R.id.action_mp4:
                UpdateList(lastNavigatedPath, new String[]{ FileTools.MP4});
                return true; 
            case R.id.action_image:
                UpdateList(lastNavigatedPath, new String[]{ FileTools.PNG});            
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == SELECT_FOLDER_CODE) {
            String path = data.getStringExtra("path");
              if (TextUtils.isEmpty(path)) {
                  String lastNavigatedPath = SharedPref.getWorkingFolder(this);

                    File file = new File(lastNavigatedPath);

                    if (!file.exists()) {
                        SharedPref.setWorkingFolder(this, SharedPref.SD_CARD_ROOT);
                        file = new File(SharedPref.SD_CARD_ROOT);
                    }

                    UpdateList(file.getAbsolutePath(), extensions);
                } else {
                    UpdateList(path, extensions);
                    SharedPref.setWorkingFolder(this, path);                
                }               
                Toast.makeText(VideoHistoryActivity.this, path, Toast.LENGTH_SHORT).show();
            }
        
    }

    private void UpdateList(String path, String[] extensions) {
        HistoryLoader historyLoaderTwo = new HistoryLoader(VideoHistoryActivity.this, listView, path, extensions, this);
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
        private final int ID_INFO    = 1;
        private final int ID_VIDEO   = 2;
        private final int ID_VIDEO_PLAYLIST  = 3;
        private final int ID_VIDEO_FOLDER  = 4;
        private final int ID_VIDEO_CINEMA   = 5;
        private final int ID_VIDEO_RECORDER   = 6;
        private final int ID_VIDEO_3D   = 7;
        private final int ID_VIDEO_TRIMMER   = 8;
        private final int ID_EDITOR   = 9;
        private final int ID_CONVERT_TO_MP3   = 10;
        private final int ID_SHARE   = 11;
        private final int ID_MANAGE    = 12;
        private onUpdatePathListener mUpdatePathListener;

        public HistoryLoader(Context context, ListView listView, String path, String[] extensions, onUpdatePathListener mUpdatePathListener) {
            this.mContext = context;
            this.listView = listView;
            this.path = path;
            this.extensions = extensions;
            this.mUpdatePathListener = mUpdatePathListener;
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
            finish();
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
                mUpdatePathListener.onUpdatePath(path);
            } else {
                loadingLayout.setVisibility(View.GONE); 
                welcomeLayout.setVisibility(View.INVISIBLE);

                final ArrayAdapter<FileItem> videoAdapter = new ArrayAdapter<FileItem>(VideoHistoryActivity.this, R.layout.history_list_item, AllVideo) {
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
                                    IntentUtils.openFile(VideoHistoryActivity.this, new File(video.getFilePath()));                          
                                    Toast.makeText(VideoHistoryActivity.this, video.getFilePath(), Toast.LENGTH_SHORT).show();              

                                }
                            });
                         convertView.setOnLongClickListener(new View.OnLongClickListener(){
                             @Override
                             public boolean onLongClick(View v){
                                 onActionMenu(v);
                                 return true;
                             }
                         });
                        return convertView;
                    }
                };
                mUpdatePathListener.onUpdatePath(path);
                listView.setAdapter(videoAdapter);
                listView.setVisibility(View.VISIBLE);
            }
         } 
         
        public void onActionMenu(View view) {
            ActionItem infoItem     = new ActionItem(ID_INFO, "Info", getResources().getDrawable(R.drawable.ic_information_outline));
            ActionItem videoItem       = new ActionItem(ID_VIDEO, "Video", getResources().getDrawable(R.drawable.ic_video_player));
            ActionItem videoRecorderItem     = new ActionItem(ID_VIDEO_RECORDER, "Recorder", getResources().getDrawable(R.drawable.ic_video));
            ActionItem videoCinemaItem       = new ActionItem(ID_VIDEO_CINEMA, "Movie", getResources().getDrawable(R.drawable.ic_movie));
            ActionItem video3DItem       = new ActionItem(ID_VIDEO_3D, "3D", getResources().getDrawable(R.drawable.ic_video_3d));   
            ActionItem videoTrimmerItem     = new ActionItem(ID_VIDEO_TRIMMER, "Cut", getResources().getDrawable(R.drawable.ic_video_cut));     
            ActionItem shareItem       = new ActionItem(ID_SHARE, "Share", getResources().getDrawable(R.drawable.ic_share_variant));
            ActionItem manageItem       = new ActionItem(ID_MANAGE, "Manage", getResources().getDrawable(R.drawable.ic_settings));
            ActionItem editItem       = new ActionItem(ID_EDITOR, "Editor", getResources().getDrawable(R.drawable.ic_video_edit));
            ActionItem videoPlaylistItem     = new ActionItem(ID_VIDEO_PLAYLIST, "Library", getResources().getDrawable(R.drawable.ic_library_video));
            ActionItem convertToMp3Item       = new ActionItem(ID_CONVERT_TO_MP3, "Convert To Mp3", getResources().getDrawable(R.drawable.ic_convert_to_mp3));
            ActionItem videoFolderItem       = new ActionItem(ID_VIDEO_FOLDER, "Folder", getResources().getDrawable(R.drawable.ic_folder));     

            final QuickAction quickAction = new QuickAction(mContext);

            //add action items into QuickAction
            quickAction.addActionItem(infoItem);  
            quickAction.addActionItem(videoItem);
            quickAction.addActionItem(videoPlaylistItem);
            quickAction.addActionItem(videoFolderItem);
            quickAction.addActionItem(videoCinemaItem);
            quickAction.addActionItem(video3DItem);
            quickAction.addActionItem(videoRecorderItem);
            quickAction.addActionItem(videoTrimmerItem);
            quickAction.addActionItem(editItem);  
            quickAction.addActionItem(convertToMp3Item);
            quickAction.addActionItem(shareItem);        
            quickAction.addActionItem(manageItem);
            

            //Set listener for action item clicked
            quickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {          
                    @Override
                    public void onItemClick(QuickAction source, int pos, int actionId) {
                        //here we can filter which action item was clicked with pos or actionId parameter
                        ActionItem actionItem = quickAction.getActionItem(pos);
                        switch (actionId) {

                            case ID_INFO:  
                                //getVideoInfo();
                                break;
                            case ID_VIDEO:
                                //getVideoPlayer();
                                break; 
                            case ID_VIDEO_PLAYLIST:
                                //getVideoPlaylist();
                                break; 
                            case ID_VIDEO_FOLDER:
                                //getVideoFolder();
                                break;     
                            case ID_VIDEO_CINEMA:
                                //getVideoCinema(true);  
                                break;     
                            case ID_VIDEO_RECORDER:
                                //getVideoRecorder();
                                break; 
                            case ID_VIDEO_3D:
                                //getVideo3D(true);
                                break;      
                            case ID_EDITOR:
                                //getVideoEditor();
                                break;   
                            case ID_CONVERT_TO_MP3:
                                //getVideoConverter();
                                break;
                            case ID_VIDEO_TRIMMER:
                                 //getVideoTrimmer();
                                 break;
                            case ID_SHARE:
                                //getVideoShare();
                                break;    
                            case ID_MANAGE:
                                //getVideoManage();
                                break;     
                            default:

                                Toast.makeText(mContext, actionItem.getTitle() + " selected", Toast.LENGTH_SHORT).show(); 
                                break;
                        }


                    }

                });

            //set listnener for on dismiss event, this listener will be called only if QuickAction dialog was dismissed
            //by clicking the area outside the dialog.
            quickAction.setOnDismissListener(new QuickAction.OnDismissListener() {          
                    @Override
                    public void onDismiss() {

                    }
                });
            quickAction.show(view); 
        
        }
    }
}
