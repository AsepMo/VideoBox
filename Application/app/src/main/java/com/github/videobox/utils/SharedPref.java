package com.github.videobox.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import java.io.File;
import java.security.InvalidParameterException;
import java.util.Comparator;

public class SharedPref {

    private Context context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";
    private static final String SEEK_TO_POSITION = "position";
    public static final String SD_CARD_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
    private static final String 
    NAME = "FileExplorerPreferences",

    PREF_START_FOLDER = "start_folder",
    PREF_CARD_LAYOUT = "card_layout",
    PREF_SORT_BY = "sort_by";

    public static final int
    SORT_BY_NAME = 0,
    SORT_BY_TYPE = 1,
    SORT_BY_SIZE = 2;


    private final static int DEFAULT_SORT_BY = SORT_BY_NAME;

    File startFolder;
    int sortBy;
    public SharedPref(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public Boolean getIsNotification() {
        return sharedPreferences.getBoolean("noti", true);
    }

    public void setIsNotification(Boolean isNotification) {
        editor.putBoolean("noti", isNotification);
        editor.apply();
    }
    
    public void setSeekTo(int position) {
        editor.putInt(SEEK_TO_POSITION, position);
        editor.commit();
    }

    public int getCurrentPosition() {
        return sharedPreferences.getInt(SEEK_TO_POSITION, Context.MODE_PRIVATE);
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }

    public boolean isFirstTimeLaunch() {
        return sharedPreferences.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }
    
    private static SharedPreferences getPrefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    private static SharedPreferences.Editor getEditor(Context context) {
        return getPrefs(context).edit();
    }

    public SharedPref setStartFolder(File startFolder)
    {
        this.startFolder = startFolder;
        return this;
    }

    public SharedPref setSortBy(int sortBy)
    {
        if (sortBy < 0 || sortBy > 2)
            throw new InvalidParameterException(String.valueOf(sortBy)+" is not a valid id of sorting order");

        this.sortBy = sortBy;
        return this;
    }

    public static SharedPref loadPreferences(Context context)
    {
        SharedPref instance = new SharedPref(context);
        instance.loadFromSharedPreferences(context.getSharedPreferences(NAME, Context.MODE_PRIVATE));
        return instance;
    }
    // Getter Methods



    public static String getWorkingFolder(Context context) {
        return getPrefs(context).getString("working_folder", SD_CARD_ROOT);
    }

    public static String[] getSavedPaths(Context context) {
        return getPrefs(context).getString("savedPaths", "").split(",");
    }

    public static void setWorkingFolder(Context context, String value) {
        getEditor(context).putString("working_folder", value).commit();
    }

    public static void setSavedPaths(Context context, StringBuilder stringBuilder) {
        getEditor(context).putString("savedPaths", stringBuilder.toString()).commit();
    }

    private void loadFromSharedPreferences(SharedPreferences sharedPreferences)
    {
        String startPath = sharedPreferences.getString(PREF_START_FOLDER, null);
        if (startPath == null)
        {
            if (Environment.getExternalStorageDirectory().list() != null)
                startFolder = Environment.getExternalStorageDirectory();
            else 
                startFolder = new File("/");
        }
        else this.startFolder = new File(startPath);
        this.sortBy = sharedPreferences.getInt(PREF_SORT_BY, DEFAULT_SORT_BY);
    }

    private void saveToSharedPreferences(SharedPreferences sharedPreferences)
    {
        sharedPreferences.edit()
            .putString(PREF_START_FOLDER, startFolder.getAbsolutePath())
            .putInt(PREF_SORT_BY, sortBy)
            .apply();
    }

    public void saveChangesAsync(final Context context)
    {
        new Thread(new Runnable()
            {

                @Override
                public void run()
                {
                    saveChanges(context);

                }
            }).run();
    }

    public void saveChanges(Context context)
    {
        saveToSharedPreferences(context.getSharedPreferences(NAME, Context.MODE_PRIVATE));
    }


    public int getSortBy()
    {
        return sortBy;
    }

    public File getStartFolder()
    {
        if (startFolder.exists() == false)
            startFolder = new File("/");
        return startFolder;
    }

    public Comparator<File> getFileSortingComparator()
    {
        switch (sortBy)
        {
            case SORT_BY_SIZE:
                return new FileTools.FileSizeComparator();

            case SORT_BY_TYPE:
                return new FileTools.FileExtensionComparator();

            default:
                return new FileTools.FileNameComparator();
        }
    }
}
