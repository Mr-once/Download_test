package com.example.mronce.download_test;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.io.File;

public class DownloadService extends Service {
    private DownloadTask downloadTask;
    private String downloadUrl;
    private int pause_progress=0;
    NotificationManager notificationManager;

    private DownloadListener listener=new DownloadListener() {
        @Override
        public void onProgress(int progress) {
                pause_progress=progress;
                getNotificationManager().notify(1,getNotification("下载中...",progress));
        }

        @Override
        public void onSuccess() {
                downloadTask=null;
                stopForeground(true);
                getNotificationManager().notify(1,getNotification("下载完成",-1));
        }

        @Override
        public void onFailed() {
                downloadTask=null;
                stopForeground(true);
                getNotificationManager().notify(1,getNotification("下载失败",-1));
        }

        @Override
        public void onPaused() {
                downloadTask=null;
                getNotificationManager().notify(1,getNotification("暂停中",pause_progress));
        }

        @Override
        public void onCanceled() {
                downloadTask=null;
                stopForeground(true);
            Toast.makeText(DownloadService.this, "下载取消", Toast.LENGTH_SHORT).show();
        }
    };
    class DownloadBinder extends Binder{//自定义Binder绑定服务与activity,服务与活动通讯
        public void StartDownload(String url){//暴露给活动的开始下载的方法
                if (downloadTask==null){
                    downloadUrl=url;
                    downloadTask =new DownloadTask(listener);
                    downloadTask.execute(downloadUrl);
                    startForeground(1,getNotification("下载中...",pause_progress));

                }
        }
        public void PauseDownload(){//暂停下载
                if (downloadTask!=null){
                    downloadTask.pauseDownload();
                }
        }
        public void CancelDownload(){//取消下载
            if (downloadTask!=null){
                downloadTask.cancelDownload();
            }
            if (downloadUrl!=null){
                String fileName=downloadUrl.substring((downloadUrl.lastIndexOf("/")));
                String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                File file = new File(directory + fileName);
                if (file.exists()){
                    file.delete();
                }
                getNotificationManager().cancel(1);
                stopForeground(true);

            }
        }
    }
    private DownloadBinder mBinder=new DownloadBinder();
    @Override
    public IBinder onBind(Intent intent) {
       return mBinder;
    }
    private NotificationManager getNotificationManager(){//通知管理器
        notificationManager=(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        return notificationManager;
    }
    private Notification getNotification (String title,int progress){//创建通知，前台服务的前提
        Intent intent=new Intent(this,Main2Activity.class) ;
        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,intent,0);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this);
        NotificationChannel b;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            b = new NotificationChannel("689","乱七八糟的其他信息",         NotificationManager.IMPORTANCE_HIGH);

                getNotificationManager().createNotificationChannel(b);

            builder.setChannelId("689");
        }


            builder.setSmallIcon(R.mipmap.ic_launcher);
            builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));//图片加载
            builder.setContentIntent(pendingIntent);
            builder.setContentTitle(title);
            if (progress >= 0) {
                //当进度大于或者等于0时才显示进度
                builder.setContentText(progress + "%");
                builder.setProgress(100, progress, false);
            }

        return builder.build();
    }
}
