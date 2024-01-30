package com.bytedance.clockapplication;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.widget.TextView;

@SuppressLint("CI_StaticFieldLeak")
public class DownloadTask extends AsyncTask<String, Integer, String> {
    TextView mTextView;

    DownloadTask(TextView textView) {
        mTextView = textView;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mTextView.setText("开始下载....");
    }

    @Override
    protected String doInBackground(String... strings) {
        String url = strings[0];
        try {
            return downlaod(url);
        } catch (Exception e) {
            return "Fail";
        }
    }

    private String downlaod(String url) throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            publishProgress(i);
            Thread.sleep(50);
        }
        return "Success";

    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        mTextView.setText("下载进度：" + values[0]);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        mTextView.setText("下载完成：" + s);
    }
}
