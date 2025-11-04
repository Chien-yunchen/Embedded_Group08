package com.example.myapplication

import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity

class VideoActivity : AppCompatActivity() {

    private lateinit var video: VideoView
    private val videoFile = "123.mp4" // PDF 指定

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)

        video = findViewById(R.id.video1)
        video.setVideoURI(Uri.parse("file:///sdcard/$videoFile"))
        video.setMediaController(MediaController(this))
        video.start()
    }

    override fun onPause() {
        super.onPause()
        video.stopPlayback()
    }
}
