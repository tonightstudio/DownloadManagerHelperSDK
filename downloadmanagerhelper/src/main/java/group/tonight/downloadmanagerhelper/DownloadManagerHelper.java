package group.tonight.downloadmanagerhelper;

import android.app.DownloadManager;
import android.arch.lifecycle.GenericLifecycleObserver;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/*
AndroidManifest.xml
<provider
    android:name="android.support.v4.content.FileProvider"
    android:authorities="${applicationId}.FileProvider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>

file_paths.xml内容
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <files-path
        name="my_images"
        path="images/" />
    <files-path
        name="my_docs"
        path="docs/" />
    <files-path
        name="files-path"
        path="" />
    <cache-path
        name="cache-path"
        path="" />
    <external-path
        name="external-path"
        path="" />
    <external-files-path
        name="external-files-path"
        path="" />
    <external-cache-path
        name="external-cache-path"
        path="" />
    <external-media-path
        name="external-media-path"
        path="" />
    <root-path
        name="root-path"
        path="" />
</paths>
 */

/**
 * DownloadManager帮助类自动下载url对应的apk并安装，兼容android 6.0、android 8.0
 */
public class DownloadManagerHelper extends BroadcastReceiver implements GenericLifecycleObserver {
    private IntentFilter mIntentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
    private final Context context;
    private final DownloadManager mDownloadManager;
    private long mDownloadId;


    public DownloadManagerHelper(Context context) {
        this.context = context;
        mDownloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if (context instanceof LifecycleOwner) {
            Lifecycle lifecycle = ((LifecycleOwner) context).getLifecycle();
            lifecycle.addObserver(this);
        }
    }

    public void enqueue(String url) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        mDownloadId = mDownloadManager.enqueue(request);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
        if (downloadId != mDownloadId) {
            return;
        }
//        Uri uri = mDownloadManager.getUriForDownloadedFile(downloadId);
//        String mimeType = mDownloadManager.getMimeTypeForDownloadedFile(downloadId);
        try {
            Intent apkIntent = new Intent(Intent.ACTION_VIEW);
            //判读版本是否在7.0以上
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //能过FileDescriptor读取contentUri中的输入流
                //并保存到app外部存储files目录下
                //用于共享给安装程序用于更新
                ParcelFileDescriptor parcelFileDescriptor = mDownloadManager.openDownloadedFile(downloadId);
//                    ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(uri, "r");

                InputStream fileInputStream = new FileInputStream(parcelFileDescriptor.getFileDescriptor());
//                    InputStream fileInputStream = getContentResolver().openInputStream(uri);

                byte[] buffer = new byte[fileInputStream.available()];
                int read = fileInputStream.read(buffer);
                //不设置DownloadManager.Request的DestinationUri
                String tempFilePath = context.getFilesDir().getPath() + File.separator + "aaa111.apk";
                //设置request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "app-release.apk");
                File targetFile = new File(tempFilePath);
                long length = targetFile.length();
                OutputStream outStream = new FileOutputStream(targetFile);
                outStream.write(buffer);
                fileInputStream.close();
                outStream.close();

                Uri apkUri = FileProvider.getUriForFile(context, context.getPackageName() + ".FileProvider", targetFile);
                apkIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                apkIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            } else {
                Cursor cursor = mDownloadManager.query(new DownloadManager.Query().setFilterById(downloadId));
                cursor.moveToFirst();
                String localUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));//contentUri
                String localFileName = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));//fileUri
                cursor.close();

                apkIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                apkIntent.setDataAndType(Uri.fromFile(new File(localFileName)), "application/vnd.android.package-archive");
            }
            context.startActivity(apkIntent);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStateChanged(LifecycleOwner source, Lifecycle.Event event) {
        if (event == Lifecycle.Event.ON_RESUME) {
            System.out.println("注册");
            context.registerReceiver(this, mIntentFilter);
        } else if (event == Lifecycle.Event.ON_PAUSE) {
            System.out.println("反注册");
            context.unregisterReceiver(this);
        }
    }
}
