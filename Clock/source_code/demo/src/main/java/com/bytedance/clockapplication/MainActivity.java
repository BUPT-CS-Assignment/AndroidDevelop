package com.bytedance.clockapplication;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.byted.clockapplication.R;

@SuppressLint("CI_HandlerLeak")
public class MainActivity extends AppCompatActivity {
    public static final int MSG_START_DOWNLOAD = 0;
    public static final int MSG_DOWNLOAD_SUCCESS = 1;
    public static final int MSG_DOWNLOAD_FAIL = 2;
    private TextView mText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mText = findViewById(R.id.text);
        // new DownloadThread("http://www.xxx.mp4").start();

        new DownloadTask(mText).execute("http://www.xxx.mp4");
    }

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_START_DOWNLOAD:
                    mText.setText("开始下载");
                    break;
                case MSG_DOWNLOAD_SUCCESS:
                    mText.setText("下载成功");
                    break;
                case MSG_DOWNLOAD_FAIL:
                    mText.setText("下载失败");
                    break;
            }
        }
    };

    private class DownloadThread extends Thread {
        private String videoId;

        public DownloadThread(String videoId) {
            this.videoId = videoId;
        }

        @Override
        public void run() {
            mHandler.sendMessage(Message.obtain(mHandler, MSG_START_DOWNLOAD));
            try {
                download(videoId);
                mHandler.sendMessage(Message.obtain(mHandler, MSG_DOWNLOAD_SUCCESS));
            } catch (Exception e) {
                mHandler.sendMessage(Message.obtain(mHandler, MSG_DOWNLOAD_FAIL));
            }
        }
    }

    private void download(String videoId) {
        // 后台下载...
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
