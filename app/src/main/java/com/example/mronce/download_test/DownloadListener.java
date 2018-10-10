package com.example.mronce.download_test;

/**
 * Created by Mronce on 2018/10/9.
 */
//回调接口，解决返回数据问题，用于数据更新以及操作；
public interface DownloadListener {
    void onProgress(int progress);
    void onSuccess();
    void onFailed();
    void onPaused();
    void onCanceled();

}
