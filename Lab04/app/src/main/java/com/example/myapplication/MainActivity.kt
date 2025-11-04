package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var output: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        output = findViewById(R.id.output)
    }

    // PDF：btnStart_Click()
    fun start_Click(view: View) {
        val intent = Intent(this, MusicService::class.java)
        intent.putExtra("ISPAUSE", false)
        startService(intent)
        output.text = "音樂播放中..."
    }

    // PDF：btnPause_Click()
    fun pause_Click(view: View) {
        val intent = Intent(this, MusicService::class.java)
        intent.putExtra("ISPAUSE", true)
        startService(intent)
        output.text = "音樂暫停中..."
    }

    // PDF：btnStop_Click()
    fun stop_Click(view: View) {
        val intent = Intent(this, MusicService::class.java)
        stopService(intent)
        output.text = "音樂已經停止播放..."
    }

    fun openVideo_Click(view: View) {
        startActivity(Intent(this, VideoActivity::class.java))
    }

    fun openRecord_Click(view: View) {
        startActivity(Intent(this, RecordActivity::class.java))
    }

    fun openDraw_Click(view: View) {
        startActivity(Intent(this, Draw2DActivity::class.java))
    }
}
