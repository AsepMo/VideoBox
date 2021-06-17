package com.github.videobox.app.youtube.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;
import android.text.format.DateUtils;

import com.github.videobox.R;
import com.github.videobox.app.youtube.data.YoutubeUtils;
import com.github.videobox.app.youtube.data.YoutubeVideo;
import com.github.videobox.app.youtube.data.YoutubeVideoDb;
import com.github.videobox.app.youtube.listeners.OnDatabaseChangedListener;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import com.github.videobox.utils.VideoFolder;

public class YoutubeVideoAdapter extends RecyclerView.Adapter<YoutubeVideoAdapter.RecordingsViewHolder> implements OnDatabaseChangedListener{

    private static final String LOG_TAG = "FileViewerAdapter";

    private YoutubeVideoDb mDatabase;
    private YoutubeVideo item;
    private Context mContext;
    private LinearLayoutManager llm;
	
    public YoutubeVideoAdapter(Context context, LinearLayoutManager linearLayoutManager) {
        super();
        mContext = context;
        mDatabase = new YoutubeVideoDb(mContext);
        mDatabase.setOnDatabaseChangedListener(this);
        llm = linearLayoutManager;
    }

    @Override
    public void onBindViewHolder(final RecordingsViewHolder holder, int position) {

        item = getItem(position);

		// Set title top margin
		holder.mTitleText.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					int singleLineHeight = holder.mTitleText.getMeasuredHeight();
					int topMargin = (holder.mPointFrame.getMeasuredHeight() - singleLineHeight) / 2;
					// Only update top margin when it is positive, preventing titles being truncated.
					if (topMargin > 0) {
						ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) holder.mTitleText.getLayoutParams();
						mlp.topMargin = topMargin;
					}
				}
			});

		// Capture position and set to the ImageView
		holder.mTitleText.setText(item.getTitle());
		holder.mVideoId.setText(item.getVideoId());
		Glide.with(mContext)
			.load(item.getThumbnail())
			.placeholder(R.drawable.no_thumbnail)
			.into(holder.mThumbnail);
        holder.mDate.setText(DateUtils.formatDateTime(mContext,item.getTime(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_YEAR));

        // define an on click listener to open PlaybackFragment
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    YoutubeUtils mYoutube = new YoutubeUtils(mContext);
					mYoutube.startPlayerActivity(mContext);
			
                } catch (Exception e) {
                    Log.e(LOG_TAG, "exception", e);
                }
            }
        });

        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                ArrayList<String> entrys = new ArrayList<String>();
                entrys.add(mContext.getString(R.string.dialog_file_share));
                entrys.add(mContext.getString(R.string.dialog_file_rename));
                entrys.add(mContext.getString(R.string.dialog_file_delete));

                final CharSequence[] items = entrys.toArray(new CharSequence[entrys.size()]);


                // File delete confirm
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(mContext.getString(R.string.dialog_title_options));
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        if (item == 0) {
                            shareFileDialog(holder.getPosition());
                        } if (item == 1) {
                            renameFileDialog(holder.getPosition());
                        } else if (item == 2) {
                            deleteFileDialog(holder.getPosition());
                        }
                    }
                });
                builder.setCancelable(true);
                builder.setNegativeButton(mContext.getString(R.string.dialog_action_cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert = builder.create();
                alert.show();

                return false;
            }
        });
    }

    @Override
    public RecordingsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_add_playlist_layout, parent, false);

        mContext = parent.getContext();

        return new RecordingsViewHolder(itemView);
    }

    public static class RecordingsViewHolder extends RecyclerView.ViewHolder {
        private View mLineView;
		private TextView mTitleText;
		private TextView mVideoId;
		private LinearLayout mPointFrame;
		private LinearLayout mRightContainer;
		private ImageView mThumbnail;
		private TextView mDate;
        private View cardView;

        public RecordingsViewHolder(View inflateView) {
            super(inflateView);
			cardView = inflateView.findViewById(R.id.vertical_stepper_item_view_layout);
            mLineView = inflateView.findViewById(R.id.stepper_line);
			mTitleText = inflateView.findViewById(R.id.stepper_title);
			mVideoId = inflateView.findViewById(R.id.stepper_videoId);
			mPointFrame = inflateView.findViewById(R.id.stepper_point_frame);
			mRightContainer = inflateView.findViewById(R.id.stepper_right_layout);
			mThumbnail = inflateView.findViewById(R.id.stepper_thumbnail);
			mDate = inflateView.findViewById(R.id.stepper_date_playlist);
        }
    }

    @Override
    public int getItemCount() {
        return mDatabase.getCount();
    }

    public YoutubeVideo getItem(int position) {
        return mDatabase.getItemAt(position);
    }

    @Override
    public void onNewDatabaseEntryAdded() {
        //item added to top of the list
        notifyItemInserted(getItemCount() - 1);
        llm.scrollToPosition(getItemCount() - 1);
    }

    @Override
    //TODO
    public void onDatabaseEntryRenamed() {

    }

    public void remove(int position) {
        //remove item from database, recyclerview and storage

        //delete file from storage
        //File file = new File(getItem(position).getFilePath());
        //file.delete();

        Toast.makeText(
            mContext,
            String.format(
                mContext.getString(R.string.toast_file_delete),
                getItem(position).getTitle()
            ),
            Toast.LENGTH_SHORT
        ).show();

        mDatabase.removeItemWithId(getItem(position).getId());
        notifyItemRemoved(position);
    }

    //TODO
    public void removeOutOfApp(String filePath) {
        //user deletes a saved recording out of the application through another application
    }

    public void rename(int position, String name) {
        //rename a file

        String mFilePath = VideoFolder.ZFOLDER_YOUTUBE;
        mFilePath += "/Download/" + name;
        File f = new File(mFilePath);

        if (f.exists() && !f.isDirectory()) {
            //file name is not unique, cannot rename file.
            Toast.makeText(mContext,
                    String.format(mContext.getString(R.string.toast_file_exists), name),
                    Toast.LENGTH_SHORT).show();

        } else {
            //file name is unique, rename file
            //File oldFilePath = new File(getItem(position).getFilePath());
            //oldFilePath.renameTo(f);
            mDatabase.renameItem(getItem(position), name, mFilePath);
            notifyItemChanged(position);
        }
    }

    public void shareFileDialog(int position) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("https://www.youtube.com/watch?v=" + getItem(position).getVideoId()));
        shareIntent.setType("audio/mp4");
        mContext.startActivity(Intent.createChooser(shareIntent, mContext.getText(R.string.send_to)));
    }

    public void renameFileDialog (final int position) {
        // File rename dialog
        AlertDialog.Builder renameFileBuilder = new AlertDialog.Builder(mContext);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.dialog_rename_file, null);

        final EditText input = (EditText) view.findViewById(R.id.new_name);

        renameFileBuilder.setTitle(mContext.getString(R.string.dialog_title_rename));
        renameFileBuilder.setCancelable(true);
        renameFileBuilder.setPositiveButton(mContext.getString(R.string.dialog_action_ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            String value = input.getText().toString().trim() + ".mp4";
                            rename(position, value);

                        } catch (Exception e) {
                            Log.e(LOG_TAG, "exception", e);
                        }

                        dialog.cancel();
                    }
                });
        renameFileBuilder.setNegativeButton(mContext.getString(R.string.dialog_action_cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        renameFileBuilder.setView(view);
        AlertDialog alert = renameFileBuilder.create();
        alert.show();
    }

    public void deleteFileDialog (final int position) {
        // File delete confirm
        AlertDialog.Builder confirmDelete = new AlertDialog.Builder(mContext);
        confirmDelete.setTitle(mContext.getString(R.string.dialog_title_delete));
        confirmDelete.setMessage(mContext.getString(R.string.dialog_text_delete));
        confirmDelete.setCancelable(true);
        confirmDelete.setPositiveButton(mContext.getString(R.string.dialog_action_yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            //remove item from database, recyclerview, and storage
                            remove(position);

                        } catch (Exception e) {
                            Log.e(LOG_TAG, "exception", e);
                        }

                        dialog.cancel();
                    }
                });
        confirmDelete.setNegativeButton(mContext.getString(R.string.dialog_action_no),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = confirmDelete.create();
        alert.show();
    }
}
