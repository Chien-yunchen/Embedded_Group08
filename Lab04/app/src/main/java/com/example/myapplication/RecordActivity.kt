package com.example.myapplication

import android.Manifest
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class RecordActivity : AppCompatActivity() {

    private var recorder: MediaRecorder? = null
    private lateinit var startBtn: Button
    private lateinit var stopBtn: Button
    private lateinit var output: TextView
    private lateinit var path: File

    private val permReq = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        // 簡化示範：實際錄音流程在按鈕事件中執行
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)

        startBtn = findViewById(R.id.start)
        stopBtn = findViewById(R.id.stop)
        output = findViewById(R.id.output)

        recorder = MediaRecorder()
        path = File(Environment.getExternalStorageDirectory(), "myrecord.3gp")
        resetRecorder()
    }

    override fun onDestroy() {
        super.onDestroy()
        recorder?.release()
        recorder = null
    }

    private fun resetRecorder() {
        try {
            recorder?.apply {
                reset()
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
                setOutputFile(path.absolutePath)
                prepare()
            }
            output.text = "錄音程序準備完成...."
            startBtn.isEnabled = true
            stopBtn.isEnabled = false
        } catch (ex: Exception) {
            Log.d("Lab4", "resetRecorder: ${ex.message}")
        }
    }

    // PDF：btnstart_Click()
    fun start_Click(view: View) {
        permReq.launch(
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )
        output.text = "開始錄音...."
        try {
            recorder?.start()
            startBtn.isEnabled = false
            stopBtn.isEnabled = true
        } catch (ex: Exception) {
            Log.d("Lab4", "start_Click ${ex.message}")
        }
    }

    // PDF：btnstop_Click()
    fun stop_Click(view: View) {
        output.text = "停止錄音...."
        try {
            recorder?.stop()
        } catch (_: Exception) {
        }
        startBtn.isEnabled = true
        stopBtn.isEnabled = false
        resetRecorder()
    }
}
