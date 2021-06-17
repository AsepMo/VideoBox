package com.github.videobox.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import java.io.File;
import java.util.List;
import com.github.videobox.VideoBoxActivity;

public class IntentUtils
{
	public static void openFile(Context c, File file)
    {
        if (file.isDirectory())
            throw new IllegalArgumentException("File cannot be a directory!");

        Intent intent = createFileOpenIntent(file);

        try
        {
            c.startActivity(intent);
        }
        catch (ActivityNotFoundException e)
        {
            c.startActivity(Intent.createChooser(intent, file.getName()));
        }
        catch (Exception e)
        {
            new AlertDialog.Builder(c)
                .setMessage(e.getMessage())
                //.setTitle(R.string.error)
                .setPositiveButton(android.R.string.ok, null)
                .show();
        }
    }
	
	public static Intent createFileOpenIntent(File file)
	{
		Intent intent = new Intent(Intent.ACTION_VIEW);		
		intent.setDataAndType(Uri.fromFile(file), FileTools.getFileMimeType(file));
		return intent;
	}
	
	public static void createShortcut(Context context, File file)
	{
		final Intent shortcutIntent;
		if (file.isDirectory())
		{
			shortcutIntent = new Intent(context, VideoBoxActivity.class);
			//shortcutIntent.putExtra(VideoActivity.EXTRA_DIR, file.getAbsolutePath());
		}
		else 
		{
			shortcutIntent = createFileOpenIntent(file);
		}
		
		Intent addIntent = new Intent();
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, file.getName());
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, FileTools.createFileIcon(file, context, true));
		addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
		context.startActivity(addIntent);
	}
	
	public static List<ResolveInfo> getAppsThatHandleFile(File file, Context context)
	{
		return getAppsThatHandleIntent(createFileOpenIntent(file), context);
	}
	
	public static List<ResolveInfo> getAppsThatHandleIntent(Intent intent, Context context)
	{
		PackageManager packageManager = context.getPackageManager();
		return packageManager.queryIntentActivities(intent, 0);
	}
	
	public static Drawable getAppIconForFile(File file, Context context)
	{
		List<ResolveInfo> infos = getAppsThatHandleFile(file, context);
		PackageManager packageManager = context.getPackageManager();
		for (ResolveInfo info : infos)
		{
			Drawable drawable = info.loadIcon(packageManager);
			if (drawable != null)
				return drawable;
		}
		return null;
	}
	
}
