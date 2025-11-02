package com.example.lab05_group08;

import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class VideoPlayer extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        VideoView vv = new VideoView(this);
        setContentView(vv);

        String path = getIntent().getStringExtra(MainActivity.EXTRA_LAST_PATH);
        if (path == null || path.isEmpty()) {
            Toast.makeText(this, "沒有檔案路徑可播", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        vv.setVideoURI(Uri.parse(path));
        vv.setMediaController(new MediaController(this));
        vv.setOnPreparedListener(mp -> vv.start());
    }
}
