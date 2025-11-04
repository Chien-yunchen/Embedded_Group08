package com.example.hueyan.ch16_3_2;

import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;


public class VideoPlayerActivity extends ActionBarActivity {
    VideoView vv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        // 取得VideoView元件
        vv = (VideoView) this.findViewById(R.id.videoView);
        // 建立MediaController物件
        MediaController mc = new MediaController(this);
        vv.setMediaController(mc); // 指定控制物件
        // 指定媒體檔案的播放路徑的URI
        vv.setVideoURI(Uri.parse(Environment.getExternalStorageDirectory().getPath()+"/myVideo.3gp"));
        // 指定元件取得焦點
        vv.requestFocus();
    }
}
