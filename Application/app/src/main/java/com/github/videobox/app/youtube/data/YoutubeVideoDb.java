package com.github.videobox.app.youtube.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.github.videobox.app.youtube.listeners.OnDatabaseChangedListener;

import java.util.Comparator;

public class YoutubeVideoDb extends SQLiteOpenHelper {
    private Context mContext;

    private static final String LOG_TAG = "DBHelper";

    private static OnDatabaseChangedListener mOnDatabaseChangedListener;

    public static final String DATABASE_NAME = "saved_videoId.db";
    private static final int DATABASE_VERSION = 1;

    public static abstract class DBHelperItem implements BaseColumns {
        public static final String TABLE_NAME = "saved_videoId";

        public static final String COLUMN_NAME_YOUTUBE_TITLE = "title";
        public static final String COLUMN_NAME_YOUTUBE_THUMBNAIL = "thumbnail";
        public static final String COLUMN_NAME_YOUTUBE_VIDEOID = "videoId";
        public static final String COLUMN_NAME_TIME_ADDED = "time_added";
    }

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
	"CREATE TABLE " + DBHelperItem.TABLE_NAME + " (" +
	DBHelperItem._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
	DBHelperItem.COLUMN_NAME_YOUTUBE_TITLE + TEXT_TYPE + COMMA_SEP +
	DBHelperItem.COLUMN_NAME_YOUTUBE_THUMBNAIL + TEXT_TYPE + COMMA_SEP +
	DBHelperItem.COLUMN_NAME_YOUTUBE_VIDEOID + TEXT_TYPE + COMMA_SEP +
	DBHelperItem.COLUMN_NAME_TIME_ADDED + " INTEGER " + ")";

    @SuppressWarnings("unused")
    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + DBHelperItem.TABLE_NAME;

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public YoutubeVideoDb(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    public static void setOnDatabaseChangedListener(OnDatabaseChangedListener listener) {
        mOnDatabaseChangedListener = listener;
    }

    public YoutubeVideo getItemAt(int position) {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
			DBHelperItem._ID,
			DBHelperItem.COLUMN_NAME_YOUTUBE_TITLE,
			DBHelperItem.COLUMN_NAME_YOUTUBE_THUMBNAIL,
			DBHelperItem.COLUMN_NAME_YOUTUBE_VIDEOID,
			DBHelperItem.COLUMN_NAME_TIME_ADDED
        };
        Cursor c = db.query(DBHelperItem.TABLE_NAME, projection, null, null, null, null, null);
        if (c.moveToPosition(position)) {
            YoutubeVideo item = new YoutubeVideo();
            item.setId(c.getInt(c.getColumnIndex(DBHelperItem._ID)));
            item.setTitle(c.getString(c.getColumnIndex(DBHelperItem.COLUMN_NAME_YOUTUBE_TITLE)));
            item.setThumbnail(c.getString(c.getColumnIndex(DBHelperItem.COLUMN_NAME_YOUTUBE_THUMBNAIL)));
            item.setVideoId(c.getString(c.getColumnIndex(DBHelperItem.COLUMN_NAME_YOUTUBE_VIDEOID)));
            item.setTime(c.getLong(c.getColumnIndex(DBHelperItem.COLUMN_NAME_TIME_ADDED)));
            c.close();
            return item;
        }
        return null;
    }

    public void removeItemWithId(int id) {
        SQLiteDatabase db = getWritableDatabase();
        String[] whereArgs = { String.valueOf(id) };
        db.delete(DBHelperItem.TABLE_NAME, "_ID=?", whereArgs);
    }

    public int getCount() {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = { DBHelperItem._ID };
        Cursor c = db.query(DBHelperItem.TABLE_NAME, projection, null, null, null, null, null);
        int count = c.getCount();
        c.close();
        return count;
    }

    public Context getContext() {
        return mContext;
    }

    public class RecordingComparator implements Comparator<YoutubeVideo> {
        public int compare(YoutubeVideo item1, YoutubeVideo item2) {
            Long o1 = item1.getTime();
            Long o2 = item2.getTime();
            return o2.compareTo(o1);
        }
    }

    public long addPlaylist(YoutubeData data) {

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBHelperItem.COLUMN_NAME_YOUTUBE_TITLE, data.getVideoTitle());
        cv.put(DBHelperItem.COLUMN_NAME_YOUTUBE_THUMBNAIL, data.getMediumThumbnail());
        cv.put(DBHelperItem.COLUMN_NAME_YOUTUBE_VIDEOID, data.getVideoId());
        cv.put(DBHelperItem.COLUMN_NAME_TIME_ADDED, System.currentTimeMillis());
        long rowId = db.insert(DBHelperItem.TABLE_NAME, null, cv);

        if (mOnDatabaseChangedListener != null) {
            mOnDatabaseChangedListener.onNewDatabaseEntryAdded();
        }

        return rowId;
    }

    public void renameItem(YoutubeVideo item, String title, String thumbnail) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBHelperItem.COLUMN_NAME_YOUTUBE_TITLE, title);
        cv.put(DBHelperItem.COLUMN_NAME_YOUTUBE_THUMBNAIL, thumbnail);
        db.update(DBHelperItem.TABLE_NAME, cv,
				  DBHelperItem._ID + "=" + item.getId(), null);

        if (mOnDatabaseChangedListener != null) {
            mOnDatabaseChangedListener.onDatabaseEntryRenamed();
        }
    }

    public long restoreRecording(YoutubeVideo item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBHelperItem.COLUMN_NAME_YOUTUBE_TITLE, item.getTitle());
        cv.put(DBHelperItem.COLUMN_NAME_YOUTUBE_THUMBNAIL, item.getThumbnail());
        cv.put(DBHelperItem.COLUMN_NAME_YOUTUBE_VIDEOID, item.getVideoId());
        cv.put(DBHelperItem.COLUMN_NAME_TIME_ADDED, item.getTime());
        cv.put(DBHelperItem._ID, item.getId());
        long rowId = db.insert(DBHelperItem.TABLE_NAME, null, cv);
        if (mOnDatabaseChangedListener != null) {
            //mOnDatabaseChangedListener.onNewDatabaseEntryAdded();
        }
        return rowId;
    }
}
