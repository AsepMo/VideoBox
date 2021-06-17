package com.github.videobox.app.youtube.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class YoutubeDataBase extends SQLiteOpenHelper
{
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "YoutubeDatabase.db";

    private static final String TABLE_YOUTUBE_VIDEO_LIST = "youtube_video_list";

    private static final String KEY_ID = "id";
    private static final String KEY_VIDEO_ID = "video_id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_THUMBNAIL = "thumbnail";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_PUBLISHED = "published";

	public static final String COL_1 = "ID";
    public static final String COL_2 = "TITLE";
    public static final String COL_3 = "THUMBNAIL";
    public static final String COL_4 = "DESCRIPTIONS";
    public static final String COL_5 = "VIDEOID";
	public static final String COL_6 = "PUBLISHED";

	private static YoutubeDataBase mYoutubeDatabase;
    private SQLiteDatabase mSQLiteDatabase;

    public static synchronized YoutubeDataBase getDatabase(Context context)
	{
        if (mYoutubeDatabase == null)
		{
            mYoutubeDatabase = new YoutubeDataBase(context);
            mYoutubeDatabase.mSQLiteDatabase = mYoutubeDatabase.getWritableDatabase();
        }

        return mYoutubeDatabase;
    }

    public YoutubeDataBase(Context context)
	{
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
	{
		/*String urlListTable = "CREATE TABLE " + TABLE_VIDEO_LIST + "("
		 + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
		 + KEY_TYPE + " INTEGER)";

		 String generalVideoList = "CREATE TABLE " + TABLE_BROWSER_VIDEO_LIST + "("
		 + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
		 + KEY_VIDEO_ID + " INTEGER,"
		 + KEY_URL + " TEXT,"
		 + KEY_TITLE + " TEXT,"
		 + KEY_PACKAGE_NAME + " TEXT )";*/

		String YoutubePlaylist = "CREATE TABLE " + TABLE_YOUTUBE_VIDEO_LIST + "("
		    + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
		    + KEY_TITLE + " TEXT,"
		    + KEY_THUMBNAIL + " TEXT,"
			+ KEY_DESCRIPTION + " TEXT, "
			+ KEY_VIDEO_ID + " TEXT,"
		    + KEY_PUBLISHED + " TEXT )";
		db.execSQL(YoutubePlaylist);
        //db.execSQL("create table " + TABLE_YOUTUBE_VIDEO_LIST + " (ID INTEGER PRIMARY KEY AUTOINCREMENT,TITLE TEXT, THUMBNAIL TEXT, DESCRIPTION TEXT, VIDEOID TEXT, PUBLISHED TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_YOUTUBE_VIDEO_LIST);
        onCreate(db);
    }

    public boolean insertData(String title, String thumbnail, String description, String videoId, String published)
	{
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_TITLE, title);
        contentValues.put(KEY_THUMBNAIL, thumbnail);
        contentValues.put(KEY_DESCRIPTION, description);
        contentValues.put(KEY_VIDEO_ID, videoId);
		contentValues.put(KEY_PUBLISHED, published);

        long result = db.insert(TABLE_YOUTUBE_VIDEO_LIST, null , contentValues);
        if (result == -1)
            return false;
        else
            return true;
    }

    public Cursor getAllData()
	{
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_YOUTUBE_VIDEO_LIST, null);
        return res;
    }

	/*public YoutubeUtils getYoutube(Context context)
	{
		Cursor videoQueryCursor = getAllData();
		if (videoQueryCursor.getCount() == 0)
		{
			// show message
			new YoutubeUtils(context).sendShortMessage("Nothing found");

		}

		while (videoQueryCursor.moveToNext())
		{
			String title = videoQueryCursor.getString(0);
			String thumbnail = videoQueryCursor.getString(1);
			String description = videoQueryCursor.getString(2);
			String videoId = videoQueryCursor.getString(3);
			String published = videoQueryCursor.getString(4);

			YoutubeUtils youtubeVideo = new YoutubeUtils(title, thumbnail , videoId, description, published, true);
			youtubeVideo.setTitle(videoId);
			youtubeVideo.setVideoId(videoId);
			youtubeVideo.setThumbnail(thumbnail);
		    youtubeVideo.setPublished(published);
			videoQueryCursor.close();
			return youtubeVideo;
		}

		return null;
	}*/
	
	public ArrayList<YoutubeData> getLocallyStoredData(Context context)
	{
        ArrayList<YoutubeData> items = new ArrayList<YoutubeData>();
		Cursor videoQueryCursor = getAllData();
		if (videoQueryCursor.getCount() == 0)
		{
			// show message
			new YoutubeUtils(context).sendShortMessage("Nothing found");
		}

		while (videoQueryCursor.moveToNext())
		{
			String title = videoQueryCursor.getString(0);
			String thumbnail = videoQueryCursor.getString(1);
			String description = videoQueryCursor.getString(2);
			String videoId = videoQueryCursor.getString(3);
			String published = videoQueryCursor.getString(4);

			YoutubeData youtubeVideo = new YoutubeData(context);
			youtubeVideo.setVideoTitle(title);
			youtubeVideo.setVideoId(videoId);
			youtubeVideo.setMediumThumbnail(thumbnail);
            youtubeVideo.setDescription(description);
		    youtubeVideo.setPublishedAt(published);
			videoQueryCursor.close();
		    items.add(youtubeVideo);
			}
		return items;
	}
	
    public boolean updateData(String id, String title, String thumbnail, String description, String videoId, String published)
	{
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_ID, id);
        contentValues.put(KEY_TITLE, title);
        contentValues.put(KEY_THUMBNAIL, thumbnail);
        contentValues.put(KEY_DESCRIPTION, description);
        contentValues.put(KEY_VIDEO_ID, videoId);
		contentValues.put(KEY_PUBLISHED, published);

        db.update(TABLE_YOUTUBE_VIDEO_LIST, contentValues, "ID = ?", new String[] { id });
        return true;
    }

    public Integer deleteData(String id)
	{
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_YOUTUBE_VIDEO_LIST, "ID = ?", new String[] {id});
    }
}




