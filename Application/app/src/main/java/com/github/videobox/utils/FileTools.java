package com.github.videobox.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.storage.StorageManager;
import android.util.Log;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;


import com.github.videobox.R;

public class FileTools {
    private static final String TAG = FileTools.class.getSimpleName();

    public static final String APK = ".apk", MP4 = ".mp4", MP3 = ".mp3", JPG = ".jpg", JPEG = ".jpeg", PNG = ".png", DOC = ".doc", DOCX = ".docx", XLS = ".xls", XLSX = ".xlsx", PDF = ".pdf";
    static String[][] sMediaTypes ={
        {"3gp","video/3gpp"},
        {"apk","application/vnd.android.package-archive"},
        {"asf","video/x-ms-asf"},
        {"avi","video/x-msvideo"},
        {"bin","application/octet-stream"},
        {"bmp","image/bmp"},
        {"c","text/plain"},
        {"class","application/octet-stream"},
        {"conf","text/plain"},
        {"cpp","text/plain"},
        {"doc","application/msword"},
        {"docx","application/vnd.openxmlformats-officedocument.wordprocessingml.document"},
        {"xls","application/vnd.ms-excel"},
        {"xlsx","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},
        {"exe","application/octet-stream"},
        {"gif","image/gif"},
        {"gtar","application/x-gtar"},
        {"gz","application/x-gzip"},
        {"h","text/plain"},
        {"htm","text/html"},
        {"html","text/html"},
        {"jar","application/java-archive"},
        {"java","text/plain"},
        {"jpeg","image/jpeg"},
        {"jpg","image/jpeg"},
        {"js","application/x-JavaScript"},
        {"log","text/plain"},
        {"m3u","audio/x-mpegurl"},
        {"m4a","audio/mp4a-latm"},
        {"m4b","audio/mp4a-latm"},
        {"m4p","audio/mp4a-latm"},
        {"m4u","video/vnd.mpegurl"},
        {"m4v","video/x-m4v"},
        {"mov","video/quicktime"},
        {"mp2","audio/x-mpeg"},
        {"mp3","audio/x-mpeg"},
        {"mp4","video/mp4"},
        {"mpc","application/vnd.mpohun.certificate"},
        {"mpe","video/mpeg"},
        {"mpeg","video/mpeg"},
        {"mpg","video/mpeg"},
        {"mpg4","video/mp4"},
        {"mpga","audio/mpeg"},
        {"msg","application/vnd.ms-outlook"},
        {"ogg","audio/ogg"},
        {"pdf","application/pdf"},
        {"png","image/png"},
        {"pps","application/vnd.ms-powerpoint"},
        {"ppt","application/vnd.ms-powerpoint"},
        {"pptx","application/vnd.openxmlformats-officedocument.presentationml.presentation"},
        {"prop","text/plain"},
        {"rc","text/plain"},
        {"rmvb","audio/x-pn-realaudio"},
        {"rtf","application/rtf"},
        {"sh","text/plain"},
        {"tar","application/x-tar"},
        {"tgz","application/x-compressed"},
        {"txt","text/plain"},
        {"wav","audio/x-wav"},
        {"wma","audio/x-ms-wma"},
        {"wmv","audio/x-ms-wmv"},
        {"wps","application/vnd.ms-works"},
        {"xml","text/plain"},
        {"z","application/x-compress"},
        {"zip","application/x-zip-compressed"},
        {"","*/*"}};

    /**
     * return the extension of the file
     * @param name name or path of the file
     * */
    public static String getExtension(String name) {
        int offset = name.lastIndexOf("/");//可能为路径,且路径中有"."
        String suffix = name.substring(offset+1, name.length());
        offset = suffix.lastIndexOf(".");
        if (offset > 0) {
            return suffix.substring(offset+1, suffix.length());
        }
        return "";
    }
    
    public static void requestPermission(Activity activity, String permission) {
        if (ContextCompat.checkSelfPermission(activity, permission)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{permission}, 0);
        }
    }
    public static class DirectoryNotEmptyException extends IOException {
        private static final long serialVersionUID = 1L;

        public DirectoryNotEmptyException(File file) {
            super("Directory " + file.getName() + " is not empty");
        }
    }

    public static class FileAlreadyExistsException extends IOException {
        private static final long serialVersionUID = 1L;

        public FileAlreadyExistsException(File file) {
            super("File " + file.getName() + " already exists in destination");
        }
    }

    public static void Toast(final Context mContext, final String message) {
        Handler mHandler = new Handler();
        mHandler.post(new Runnable() {
                public void run() {
                    Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
                }
            });
    }
    
    @SuppressLint("SdCardPath")
    private static final String SDCARD_DISPLAY_PATH = "/sdcard";
    private static final double FILE_APP_ICON_SCALE = 0.2;
    private static final int NUM_FOLDER_PREVIEWS = 6;
    private static final int THUMBNAIL_SIZE = 256;
    private static final int PREVIEW_WIDTH = (int) (THUMBNAIL_SIZE * 1.3),
    PREVIEW_HEIGHT = THUMBNAIL_SIZE;

    // user-friendly names for predefined folders
    public static final String 
    DISPLAY_NAME_ROOT = "Root",
    DISPLAY_NAME_SD_CARD = "SD Card";

    public final static int 
    KILOBYTE = 1024,
    MEGABYTE = KILOBYTE * 1024,
    GIGABYTE = MEGABYTE * 1024,
    MAX_BYTE_SIZE = KILOBYTE / 2,
    MAX_KILOBYTE_SIZE = MEGABYTE / 2,
    MAX_MEGABYTE_SIZE = GIGABYTE / 2;

    public static final String MIME_TYPE_ANY = "*/*";

    public static final FileFilter DEFAULT_FILE_FILTER = new FileFilter()
    {

        @Override
        public boolean accept(File pathname)
        {
            return pathname.isHidden() == false;
        }
    };

    /**
     * Compares files by name, where directories come always first
     */
    public static class FileNameComparator implements Comparator<File>
    {
        protected final static int 
        FIRST = -1,
        SECOND = 1;
        @Override
        public int compare(File lhs, File rhs)
        {
            if (lhs.isDirectory() || rhs.isDirectory())
            {
                if (lhs.isDirectory() == rhs.isDirectory())
                    return lhs.getName().compareToIgnoreCase(rhs.getName());
                else if (lhs.isDirectory()) return FIRST;
                else return SECOND;
            }
            return lhs.getName().compareToIgnoreCase(rhs.getName());
        }       
    }

    /**
     * Compares files by extension. 
     * Falls back to sort by name if extensions are the same or one of the objects is a Directory
     * @author Michal
     *
     */
    public static class FileExtensionComparator extends FileNameComparator
    {
        @Override
        public int compare(File lhs, File rhs)
        {
            if (lhs.isDirectory() || rhs.isDirectory())
                return super.compare(lhs, rhs);

            String ext1 = getFileExtension(lhs),
                ext2 = getFileExtension(rhs);

            if (ext1.equals(ext2))
                return super.compare(lhs, rhs);
            else
                return ext1.compareToIgnoreCase(ext2);
        }
    }

    public static class FileSizeComparator extends FileNameComparator
    {
        private final boolean ascending = false;

        @Override
        public int compare(File lhs, File rhs)
        {
            if (lhs.isDirectory() || rhs.isDirectory())
                return super.compare(lhs, rhs);

            if (lhs.length() > rhs.length())
                return ascending ? SECOND : FIRST;
            else if (lhs.length() < rhs.length())
                return ascending ? FIRST : SECOND;
            else return super.compare(lhs, rhs);
        }
    }

    public static String formatFileSize(File file)
    {
        return formatFileSize(file.length());       
    }

    public static String formatFileSize(long size)
    {
        if (size < MAX_BYTE_SIZE)
            return String.format(Locale.ENGLISH, "%d bytes", size);
        else if (size < MAX_KILOBYTE_SIZE)
            return String.format(Locale.ENGLISH, "%.2f kb", (float)size / KILOBYTE);
        else if (size < MAX_MEGABYTE_SIZE)
            return String.format(Locale.ENGLISH, "%.2f mb", (float)size / MEGABYTE);
        else 
            return String.format(Locale.ENGLISH, "%.2f gb", (float)size / GIGABYTE);
    }

    public static String formatFileSize(Collection<File> files)
    {
        return formatFileSize(getFileSize(files));
    }

    public static long getFileSize(File... files)
    {
        if (files == null) return 0l;
        long size=0;
        for (File file : files)
        {
            if (file.isDirectory())
                size += getFileSize(file.listFiles());
            else size += file.length();
        }
        return size;
    }

    public static long getFileSize(Collection<File> files)
    {
        return getFileSize(files.toArray(new File[files.size()]));
    }

    public static String getFileExtension(File file)
    {
        return getFileExtension(file.getName());
    }

    /**
     * Gets extension of the file name excluding the . character
     */
    public static String getFileExtension(String fileName)
    {
        if (fileName.contains("."))
            return fileName.substring(fileName.lastIndexOf('.')+1);
        else 
            return "";
    }

    public static String getFileMimeType(File file)
    {
        String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(getFileExtension(file));
        if (type == null) return "*/*";
        return type;
    }

    public static int getNumFilesInFolder(File folder)
    {
        if (folder.isDirectory() == false) return 0;
        File [] files = folder.listFiles(DEFAULT_FILE_FILTER);
        if (files == null) return 0;
        return files.length;
    }

    /**
     * Attempts to get common mime type for a collection of assorted files
     */
    public static String getCollectiveMimeType(Collection<File> files)
    {
        String typeStart=null;
        String type=null;
        for (File file : files) if (file.isDirectory() == false)
            {
                String thisType = getFileMimeType(file);
                if (thisType == null) continue;
                if (type == null)
                {
                    type = thisType;
                    try
                    {
                        typeStart = thisType.substring(0, thisType.indexOf('/'));
                    }
                    catch(Exception e)
                    {
                        return MIME_TYPE_ANY;
                    }
                }
                else if (type.equalsIgnoreCase(thisType))
                    continue;
                else if (thisType.startsWith(typeStart))
                    type = typeStart + "*";
                else return MIME_TYPE_ANY;
            }
        if (type == null) return MIME_TYPE_ANY;
        return type;
    }

    public static StringBuilder combineFileNames(Collection<File> files)
    {
        StringBuilder fileNamesStringBuilder = new StringBuilder();
        boolean first=true;
        for (File file : files)
        {
            if (first == false) fileNamesStringBuilder.append(", ");
            fileNamesStringBuilder.append(file.getName());
            first=false;
        }
        return fileNamesStringBuilder;
    }


    public static void flattenDirectory(File directory, List<File> result)
    {
        if (directory.isDirectory())
        {
            for (File file : directory.listFiles(DEFAULT_FILE_FILTER))
            {
                if (file.isDirectory())
                    flattenDirectory(file, result);
                else result.add(file);
            }
        }
        else result.add(directory);
    }
	
    public static int countFilesIn(Collection<File> roots) {
        int result=0;
        for (File file : roots)
            result += countFilesIn(file);
        return result;
    }

    public static int countFilesIn(File root) {
        if (root.isDirectory() == false) return 1;
        File[] files = root.listFiles();
        if (files == null) return 0;

        int n = 0;

        for (File file : files) {
            if (file.isDirectory())
                n += countFilesIn(file);
            else
                n ++;
        }
        return n;
	}
    
    public static String getUserFriendlySdcardPath(File file)
    {
        String path;
        try
        {
            path = file.getCanonicalPath();
        } catch (IOException e)
        {
            path = file.getAbsolutePath();
        }
        return path
                .replace(Environment.getExternalStorageDirectory().getAbsolutePath(), SDCARD_DISPLAY_PATH);
    }
    
    public static String getFolderDisplayName(File folder)
    {
        if (Environment.getExternalStorageDirectory().equals(folder))
            return DISPLAY_NAME_SD_CARD;
        else if ("/".equals(folder.getAbsolutePath()))
            return DISPLAY_NAME_ROOT;
        else return folder.getName();
    }
   
    /**
     *get the internal or outside sd card path
     * @param is_removale true is is outside sd card
     * */
    public static String getStoragePath(Context mContext, boolean is_removale) {

        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (is_removale == removable) {
                    return path;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
     
    // save current string in ClipBoard
    public static void savetoClipBoard(final Context co, String dir1) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) co.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText( "Copied Text", dir1);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(co, "'" + dir1 + "' " + co.getString(R.string.copied_to_clipboard),
                Toast.LENGTH_SHORT).show();
    }
    
    public static Bitmap createFileIcon(File file, Context context, boolean homescreen) {
        final Bitmap bitmap;
        final Canvas canvas;
        if (file.isDirectory()) {
            // load Folder bitmap
            Bitmap folderBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.video_placeholder);

            bitmap = Bitmap.createBitmap(folderBitmap.getWidth(), folderBitmap.getHeight(), Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bitmap);
            canvas.drawBitmap(folderBitmap, 0, 0, null);
        } else {
            Bitmap folderBitmap = BitmapFactory.decodeResource(context.getResources(), homescreen ?R.drawable.video_placeholder: R.drawable.video_placeholder);

            bitmap = Bitmap.createBitmap(folderBitmap.getWidth(), folderBitmap.getHeight(), Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bitmap);
            canvas.drawBitmap(folderBitmap, 0, 0, null);

            Drawable appIcon = IntentUtils.getAppIconForFile(file, context);
            if (appIcon != null) {
                Rect bounds = canvas.getClipBounds();
                int shrinkage = (int)(bounds.width() * FILE_APP_ICON_SCALE);
                bounds.left += shrinkage;
                bounds.right -= shrinkage;
                bounds.top += shrinkage * 1.5;
                bounds.bottom -= shrinkage * 0.5;
                appIcon.setBounds(bounds);
                appIcon.draw(canvas);
            }
        }

        // add shortcut symbol
        if (homescreen)
            canvas.drawBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.video_placeholder), 0, 0, null);

        return  bitmap;
    }

    public static void getThumbnails(ImageView imageView, File image) {
        //if (image.isDirectory()) return buildFolderPreview(image);
        String type = getFileMimeType(image);
        if (type.startsWith("video/")) {
            Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(image.getAbsolutePath(), MediaStore.Video.Thumbnails.MINI_KIND);
            imageView.setImageBitmap(bitmap);
        } else if (type.startsWith("image/")) {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(image.getPath(), bounds);
            if ((bounds.outWidth == -1) || (bounds.outHeight == -1))
                return;

            int originalSize = (bounds.outHeight > bounds.outWidth) ? bounds.outHeight : bounds.outWidth;

            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = originalSize / THUMBNAIL_SIZE;
            Bitmap bitmap = BitmapFactory.decodeFile(image.getPath(), opts);  
            imageView.setImageBitmap(bitmap);
        } else if (type.startsWith("audio/")) {
            MediaMetadataRetriever metadataRetriever;
            metadataRetriever = new MediaMetadataRetriever();
            metadataRetriever.setDataSource(image.getAbsolutePath());
            byte[] picture = metadataRetriever.getEmbeddedPicture();
            if (picture == null) return;
            metadataRetriever.release();
            Bitmap bitmap = BitmapFactory.decodeByteArray(picture, 0, picture.length);
            imageView.setImageBitmap(bitmap);
        } else imageView.setImageResource(R.drawable.video_placeholder);
    }

    public static boolean isMediaDirectory(File file) {
        try {
            String path = file.getCanonicalPath();
            for (String directory : new String[]{Environment.DIRECTORY_DCIM,
                Environment.DIRECTORY_MUSIC,
                Environment.DIRECTORY_PICTURES}) {
                if (path.startsWith(Environment.getExternalStoragePublicDirectory(directory)
                                    .getAbsolutePath()))
                    return true;
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String readRawTextFile(Context context, int resId) {
        InputStream is = context.getResources().openRawResource(resId);
        InputStreamReader reader = new InputStreamReader(is);
        BufferedReader buf = new BufferedReader(reader);
        StringBuilder text = new StringBuilder();
        try {
            String line;
            while ((line = buf.readLine()) != null) {
                text.append(line).append('\n');
            }
        } catch (IOException e) {
            return null;
        }
        return text.toString();
    }

    /**
     * Requires Permission: Manifest.permission.WRITE_EXTERNAL_STORAGE
     */
    public static void writeToStorage(Context c, TextView textView, String PATH, String text) {
        File file = new File(PATH);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            outputStream.write(text.getBytes());
            textView.setText(String.format(Locale.getDefault(), c.getString(R.string.text_write), text));
        } catch (IOException e) {
            Log.e(TAG, "Unable to write to storage", e);
            textView.setText(R.string.text_failure_write);
        } finally {
            close(outputStream);
        }
    }

    /**
     * Requires Permission: Manifest.permission.READ_EXTERNAL_STORAGE
     */
    public static void readFromStorage(Context c, TextView textView, String PATH) {
        File file = new File(PATH);
        BufferedReader inputStream = null;
        FileInputStream input = null;
        try {
            input = new FileInputStream(file);
            inputStream = new BufferedReader(new InputStreamReader(input));
            String test = inputStream.readLine();
            textView.setText(String.format(Locale.getDefault(), c.getString(R.string.text_read), test));
        } catch (IOException e) {
            Log.e(TAG, "Unable to read from storage", e);
            textView.setText(R.string.text_failure_read);
        } finally {
            close(input);
            close(inputStream);
        }
    }

    private static void close(@Nullable Closeable closeable) {
        if (closeable == null) {return;}
        try {
            closeable.close();
        } catch (IOException ignored) {}
    }

    public static boolean deleteFile(String path) {
        boolean result = false;
        File file = new File(path);
        if (file.exists()) {
            result = file.delete();
        }
        return result;
    }

    public static String saveBitmap(String dir, Bitmap b) {
        long dataTake = System.currentTimeMillis();
        String jpegName = dir + File.separator + "picture_" + dataTake + ".jpg";
        try {
            FileOutputStream fos = new FileOutputStream(jpegName);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
            return jpegName;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void deleteTarget(Context c, String path) {
        File target = new File(path);

        if (target.isFile() && target.canWrite()) {
            target.delete();
        } else if (target.isDirectory() && target.canRead() && target.canWrite()) {
            String[] file_list = target.list();

            if (file_list != null && file_list.length == 0) {
                target.delete();
                return;
            } else if (file_list != null && file_list.length > 0) {
                for (String aFile_list : file_list) {
                    File temp_f = new File(target.getAbsolutePath() + "/"
                                           + aFile_list);

                    if (temp_f.isDirectory())
                        deleteTarget(c, temp_f.getAbsolutePath());
                    else if (temp_f.isFile()) {
                        temp_f.delete();
                    }
                }
            }

            if (target.exists())
                target.delete();
        } else {
            Toast(c, "Perlu Akses Root...");
        }
    }
    
    public static void validateCopyMoveDirectory(File file, File toFolder) throws IOException {
        if (toFolder.equals(file))
            throw new IOException("Folder cannot be copied to itself");
        else if (toFolder.equals(file.getParentFile()))
            throw new IOException("Source and target directory are the same");
        else if (toFolder.getAbsolutePath().startsWith(file.getAbsolutePath()))
            throw new IOException("Folder cannot be copied to its child folder");
    }

    public static void copyFile(File src, File dst) throws IOException {
        if (src.isDirectory())
            throw new IOException("Source is a directory");
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static void deleteEmptyFolders(Collection<File> directories) throws DirectoryNotEmptyException {
        for (File file : directories) if (file.isDirectory()) {         
                deleteFiles(Arrays.asList(file.listFiles()));
                file.delete();
            } else throw new DirectoryNotEmptyException(file);
    }

    public static int deleteFiles(Collection<File> files) {
        int n=0;
        for (File file : files) {
            if (file.isDirectory()) {
                n += deleteFiles(Arrays.asList(file.listFiles()));
            }
            if (file.delete()) n++;
        }
        return n;
	}
}
