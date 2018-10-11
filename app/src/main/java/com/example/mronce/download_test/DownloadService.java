package com.example.mronce.download_test;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class DownloadService extends Service {
    private DownloadTask downloadTask;
    private String downloadUrl;
    public DownloadService() {

    }
    private DownloadListener listener=new DownloadListener() {
        @Override
        public void onProgress(int progress) {

        }

        @Override
        public void onSuccess() {

        }

        @Override
        public void onFailed() {

        }

        @Override
        public void onPaused() {

        }

        @Override
        public void onCanceled() {

        }
    };
    class DownloadBinder extends Binder{//自定义Binder绑定服务与activity,服务与活动通讯

    }
    private DownloadBinder mBinder=new DownloadBinder();
    @Override
    public IBinder onBind(Intent intent) {
       return mBinder;
    }
    private NotificationManager getNotificationManager(){//通知管理器
        return (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    }
    private Notification getNotification (String title,int progress){
        Intent intent=new Intent(this,MainActivity.class) ;
        return null;
    }
}
