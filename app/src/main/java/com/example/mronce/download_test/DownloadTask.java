package com.example.mronce.download_test;

import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Mronce on 2018/10/9.
 */
public class DownloadTask extends AsyncTask <String,Integer,Integer>{
    public static final int TYPE_SUCCESS = 0;//下载成功标志
    public static final int TYPE_FAILED = 1;//下载失败标志
    public static final int TYPE_PAUSED = 2;//下载暂停标志
    public static final int TYPE_CANCELED = 3;//下载取消标志
    private DownloadListener listener;//接口回调监听器
    private boolean isCanceled = false;//是否取消标志
    private boolean isPaused = false;//是否暂停标志
    private int lastProgress;//最后下载进度

    public DownloadTask(DownloadListener listener) {//获取接口实例
        this.listener = listener;
    }

    @Override
    protected Integer doInBackground(String... strings) {
         InputStream inputStream = null;//输入流
        RandomAccessFile savedFile = null;
        File file = null;
        try {
            long downloadedLength = 0;//已下载的长度
            String downloadUrl = (String) strings[0];
            String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));//获取文件名
            // .substring（int  i）方法是获取索引位置后的子字符串
            //.lastIndexOf(int ch)方法是获取ch字符最后一次出现的位置索引
            String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
            file = new File(directory + fileName);//路径名字符串转化为抽象路径，如果不存在则创建，存在则不创建；
            if (file.exists()) {//判断目录是否存在
                downloadedLength = file.length();
            }
            long contentLength = getContentLength(downloadUrl);//文件下载长度
            if (contentLength == 0) {
                return TYPE_FAILED;
            } else if (contentLength == downloadedLength) {
                return TYPE_SUCCESS;
            }
            /*
            网络请求操作
             */
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().addHeader("RANGE", "bytes=" + downloadedLength + "-").url(downloadUrl)
                    .build();

            Response response = client.newCall(request).execute();
            if (response != null) {
                inputStream = response.body().byteStream();
                savedFile = new RandomAccessFile(file, "rw");
                savedFile.seek(downloadedLength);//跳过已下载的字节
                byte[] b = new byte[1024];//字节流
                int total = 0;
                int len;
                while ((len = inputStream.read(b)) != -1) {//如果输入流写进字节流，字节流的长度不为-1，则继续操作，否则下载完成
                    if (isCanceled) {//在字节流写入文件过程中，判断用户是否有点击暂停和取消
                        return TYPE_CANCELED;
                    } else if (isPaused) {
                        return TYPE_PAUSED;
                    } else {//如果没有，在跳过的后面写入文件，
                        total += len;//叠加每一次的下载进度
                        savedFile.write(b, 0, len);//写入文件，从0，写到len
                        int progress = (int) ((total + downloadedLength) * 100 / contentLength);
                        publishProgress(progress);
                    }
                }
                response.body().close();
                return TYPE_SUCCESS;
            }
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (savedFile != null) {
                        savedFile.close();
                    }
                    if (isCanceled && file != null) {
                        file.delete();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return TYPE_FAILED;
    }



    @Override
    protected void onProgressUpdate(Integer...values) {//实时更新进程
        int progress =values[0];
        if (progress > lastProgress) {
            listener.onProgress(progress);
            lastProgress = progress;
        }
    }

    @Override
    protected void onPostExecute(Integer status) {//处理结果，将接口回调，将结果返回给服务，用来创建通知等等

        switch (status) {
            case TYPE_CANCELED:
                listener.onCanceled();
                break;
            case TYPE_PAUSED:
                listener.onPaused();
                break;
            case TYPE_FAILED:
                listener.onFailed();
                break;
            case TYPE_SUCCESS:
                listener.onSuccess();
                break;
            default:
                break;


        }
    }
    public void pauseDownload(){
        isPaused=true;
    }
    public void cancelDownload(){
        isCanceled=true;
    }


    private long getContentLength(String downloadUrl) throws IOException {
        OkHttpClient client=new OkHttpClient();
        Request request=new Request.Builder().url(downloadUrl).build();
        Response response=client.newCall(request).execute();
        if (response!=null&&response.isSuccessful()){
            long contentLength=response.body().contentLength();
            response.body().close();
            return contentLength;
        }
        return 0;
    }
}
