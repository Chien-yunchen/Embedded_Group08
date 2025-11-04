package com.example.hueyan.ch16_3_2;

import android.content.pm.ActivityInfo;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class VideoRecorder extends ActionBarActivity {
    // 宣告MediaRecorder和Preview物件變數
    private MediaRecorder recorder;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 建立MediaRecorder物件
        recorder = new MediaRecorder();
        // 指定錄影的參數
        recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
        // 建立錄影預覽的VideoPreview物件
        VideoPreview preview = new VideoPreview(this,recorder);
        // 橫向顯示
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(preview); // 指定活動的使用介面
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_videorecorder, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.stop:   // 停止錄影
                if (recorder != null) {
                    recorder.stop();
                    recorder.release(); // 釋放MediaRecorder物件佔用的資源
                    recorder = null;
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
