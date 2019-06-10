package org.example.tictactoe;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;

public class MainActivity extends Activity {
    // 创建多媒体播放器
    MediaPlayer mMediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * 方法onResume()在活动可见时被调用
     */
    @Override
    protected void onResume() {
        super.onResume();
        // 将音频传入多媒体播放器
        mMediaPlayer = MediaPlayer.create(this, R.raw.a_guy_1_epicbuilduploop);
        // 调节音量大小
        mMediaPlayer.setVolume(0.5f, 0.5f);
        // 循环播放
        mMediaPlayer.setLooping(true);
        // 开始播放
        mMediaPlayer.start();
    }

    @Override
    // 离开MainActivity被调用
    protected void onPause() {
        super.onPause();
        // 停止播放
        mMediaPlayer.stop();
        // 重置多媒体播放器
        mMediaPlayer.reset();
        // 释放为播放器分配的资源 防止程序崩溃
        mMediaPlayer.release();
    }
}
