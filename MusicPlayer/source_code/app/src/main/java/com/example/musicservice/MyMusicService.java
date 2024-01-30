package com.example.musicservice;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.Binder;
import android.util.Log;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MyMusicService extends Service {

    private MediaPlayer mediaPlayer;
    private boolean is_complete = false;

    public MyMusicService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return new ServiceBinder();
        //throw new UnsupportedOperationException("Not yet implemented");
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //获取意图传递的信息
        String action = intent.getStringExtra("action");

        switch (action)
        {
            case "play":
                if (mediaPlayer == null)
                {
                    is_complete = false;
                    mediaPlayer = MediaPlayer.create(this,R.raw.newyear);
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            is_complete = true;
                        }
                    });
                }
                mediaPlayer.start();
                break;
            case "stop":
                if (mediaPlayer !=null)
                {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
                break;
            case "pause":
                if (mediaPlayer !=null) mediaPlayer.pause();
                break;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public int getProgress() {
        if (mediaPlayer !=null) {
            float duration = (float)mediaPlayer.getDuration();
            float position = (float)mediaPlayer.getCurrentPosition();
            return (int)(position/duration * 100);
        }
        return 0;
    }

    public void setProgress(int progress){
        if(mediaPlayer !=null) {
            float duration = mediaPlayer.getDuration();
            mediaPlayer.seekTo((int)(duration * ((float)progress / 100f)));
        }
    }

    public String millisecToStr(int ms){
        int t = (int)(ms/1000);
        int minutes = t / 60;
        int seconds = t % 60;
        return String.format("%02d",minutes) + ":" + String.format("%02d",seconds);
    }

    public String getCurrentStr(){
        if(mediaPlayer != null){
            int position = mediaPlayer.getCurrentPosition();
            return millisecToStr((int)position);
        }
        return "00:00";
    }

    public String getDurationStr(){
        if(mediaPlayer != null){
            int dur = mediaPlayer.getDuration();
            return millisecToStr((int)dur);
        }
        return "00:00";
    }

    public String getCurrentStr(int p){
        if(mediaPlayer != null){
            int dur = mediaPlayer.getDuration();
            int position = (int)(dur * ((float)p / 100f));
            return millisecToStr((int)position);
        }
        return "00:00";
    }

    public class ServiceBinder extends Binder {
        public int get_progress() {
            return getProgress();
        }

        public void set_progress(int position) {
            setProgress(position);
        }

        public String get_current_str(){
            return getCurrentStr();
        }

        public String get_current_str(int p){
            return getCurrentStr(p);
        }

        public String get_duration_str(){
            return getDurationStr();
        }

        public boolean is_complete(){
            return is_complete;
        }
    }

}