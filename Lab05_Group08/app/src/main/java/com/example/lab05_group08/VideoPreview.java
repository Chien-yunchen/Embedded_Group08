package com.example.lab05_group08;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class VideoPreview extends SurfaceView implements SurfaceHolder.Callback {

    private MediaRecorder recorder;
    private SurfaceHolder holder;
    private File outFile;

    public VideoPreview(Context context) {
        super(context);
        holder = getHolder();
        holder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            File dir = getContext().getExternalFilesDir(Environment.DIRECTORY_MOVIES);
            if (dir != null && !dir.exists()) dir.mkdirs();
            outFile = new File(dir, "myVideo_" + System.currentTimeMillis() + ".mp4");

            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            recorder.setOutputFile(outFile.getAbsolutePath());
            recorder.setPreviewDisplay(holder.getSurface());
            recorder.setVideoFrameRate(30);
            recorder.setVideoEncodingBitRate(3 * 1024 * 1024);

            recorder.prepare();
            recorder.start();

            Toast.makeText(getContext(), "開始錄影…", Toast.LENGTH_SHORT).show();

        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "啟動錄影失敗：" + e.getMessage(), Toast.LENGTH_LONG).show();
            releaseIfAny();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        stopRecording();
        releaseIfAny();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {}

    public void stopRecording() {
        if (recorder != null) {
            try {
                recorder.stop();
                Toast.makeText(getContext(), "已停止，檔案：" + outFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
    }

    public void releaseIfAny() {
        if (recorder != null) {
            try { recorder.reset(); } catch (Exception ignored) {}
            recorder.release();
            recorder = null;
        }
    }

    public String getLastPath() {
        return (outFile != null) ? outFile.getAbsolutePath() : null;
    }
}
