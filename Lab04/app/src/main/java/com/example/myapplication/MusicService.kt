package com.example.myapplication

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log

class MusicService : Service() {

    private var player: MediaPlayer? = null
    private val musicFile = "/sdcard/test.mp3" // PDF 指定

    override fun onCreate() {
        super.onCreate()
        try {
            player = MediaPlayer().apply {
                setDataSource(musicFile)
                setOnCompletionListener {
                    try {
                        stop()
                        prepare() // 回到準備狀態
                    } catch (ex: Exception) {
                        Log.d("Lab4", "Listener: ${ex.message}")
                    }
                }
                prepare() // 準備
            }
        } catch (ex: Exception) {
            Log.d("Lab4", "onCreate: ${ex.message}")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val isPause = intent?.getBooleanExtra("ISPAUSE", true) ?: true
        try {
            player?.let { mp ->
                if (isPause) {
                    if (mp.isPlaying) mp.pause()
                } else {
                    mp.start()
                }
            }
        } catch (ex: Exception) {
            Log.d("Lab4", "onStart(): ${ex.message}")
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            player?.run {
                if (isPlaying) stop()
                release()
            }
            player = null
        } catch (ex: Exception) {
            Log.d("Lab4", "onDestroy(): ${ex.message}")
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
