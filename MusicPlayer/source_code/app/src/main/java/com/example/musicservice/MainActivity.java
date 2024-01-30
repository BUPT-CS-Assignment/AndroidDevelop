package com.example.musicservice;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.SeekBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import com.example.musicservice.MyMusicService.ServiceBinder;

import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity{
    private PlayerConn conn;
    private ServiceBinder binder;
    TextView tv_1;
    TextView tv_cur, tv_dur;
    private SeekBar mSeekBar;   // 进度条
    int mProgress = 0;  // 记录音乐进度
    private boolean mStarted = false;   // 标识音乐播放是否启动
    private Timer mTimer;   // 定时器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_1 = (TextView)findViewById(R.id.tv_1);
        tv_cur = (TextView)findViewById(R.id.time_l);
        tv_dur = (TextView)findViewById(R.id.time_r);
        mSeekBar = (SeekBar)findViewById(R.id.seekbar);

        conn = new PlayerConn();    // 新建服务连接
        bindService(new Intent(this,MyMusicService.class),conn,BIND_AUTO_CREATE);

        // 创建进度条终端处理事件
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override // 当进度条数据变化时
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mProgress = progress;
                setCurText(progress);   // 设置进度文字
            }

            @Override   // 当开始拖动进度条时
            public void onStartTrackingTouch(SeekBar seekBar) {
                cancelTimer();  // 暂停定时器
            }

            @Override // 当进度条停止拖动时
            public void onStopTrackingTouch(SeekBar seekBar) {
                binder.set_progress(mProgress);
                restartTimer(200);  // 200ms后恢复定时器
            }
        });

        reset();
    }

    private void intentAction(String action){   // 服务消息封装
        Intent intent = new Intent(this,MyMusicService.class);
        intent.putExtra("action",action);
        startService(intent);
    }
    public void play_onclick(View view){
        intentAction("play");
        tv_1.setText("正在播放: new year");
        if(!mStarted){
            restartTimer(100);    // 100ms后启动定时器
            mStarted = true;            // 播放器已启动
        }
    }

    public void stop_onclick(View view)
    {
        intentAction("stop");
        reset();
    }
    public void pause_onclick(View view)
    {
        mTimer.cancel();
        intentAction("pause");
        tv_1.setText("播放暂停: new year");
    }

    public void exit_onclick(View view)
    {
        stop_onclick(view);
        finish();
    }

    public void reset(){
        cancelTimer();
        mStarted = false;
        mSeekBar.setProgress(0);
        mProgress = 0;
        tv_dur.setText("00:00");
        tv_cur.setText("00:00");
        tv_1.setText("未播放");
    }

    private void cancelTimer(){
        if(mTimer != null) mTimer.cancel();
    }

    private void restartTimer(int delay){
        if(mTimer != null) mTimer.cancel();
        mTimer = new Timer();
        mTimer.schedule(new ProgressTask(), delay, 200);
    }

    public void setCurText() { tv_cur.setText(binder.get_current_str()); }
    public void setCurText(int p){
        tv_cur.setText(binder.get_current_str(p));
    }
    public void setDurText(){
        tv_dur.setText(binder.get_duration_str());
    }

    private class ProgressTask extends TimerTask {
        @Override
        public void run() {
            if(binder == null || !mStarted) return;
            mProgress = binder.get_progress();
            mSeekBar.setProgress(mProgress);
            setDurText();
            setCurText();
            if(binder.is_complete()){
                this.cancel();
                stop_onclick(null);
            }
        }
    }

    private class PlayerConn implements ServiceConnection{
        @Override
        public void onServiceConnected(ComponentName name, IBinder service){
            binder = (ServiceBinder)service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name){
            binder = null;
        }

    }
}