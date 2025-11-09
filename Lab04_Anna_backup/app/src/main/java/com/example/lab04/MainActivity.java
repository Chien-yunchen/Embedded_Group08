package com.example.lab04; // ← 請改成你的實際 package 名稱

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;

public class MainActivity extends AppCompatActivity {

    // 共用狀態列
    private TextView output;

    // 音樂播放器
    private MediaPlayer musicPlayer;

    // 視訊播放器
    private VideoView video;

    // 錄音功能
    private MediaRecorder recorder;
    private Button recStartBtn, recStopBtn;
    private File recordFile;
    private static final int REQ_AUDIO = 1001;
    private boolean recorderReady = false; // ← 是否已 prepare 完成
    // 錄音播放
    private MediaPlayer recPlayer;

    // 是否在 2D 繪圖模式
    private boolean inDrawMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupMainLayout();

        // 錄音權限：有就初始化，沒有就申請
        if (!hasPerm(Manifest.permission.RECORD_AUDIO)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQ_AUDIO);
        } else {
            initRecorder(); // 權限在 -> 才能 prepare
        }
    }

    /** 設定主畫面（含音樂、影片、錄音、2D） */
    private void setupMainLayout() {
        setContentView(R.layout.activity_main);

        output = findViewById(R.id.output);

        // 音樂（從 res/raw/test.mp3 播放）
        releaseMusic();
        musicPlayer = MediaPlayer.create(this, R.raw.test);

        // 影片（從 res/raw/vid123.mp4 播放）
        video = findViewById(R.id.video1);
        if (video != null) {
            Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.vid123);
            video.setVideoURI(uri);
            video.setMediaController(new MediaController(this));
        }

        // 錄音設定（app 私有外部目錄，不需 WRITE 權限）
        recStartBtn = findViewById(R.id.start);
        recStopBtn  = findViewById(R.id.stop);
        if (recStartBtn != null && recStopBtn != null) {
            recStopBtn.setEnabled(false);
            recorder = new MediaRecorder();
            recordFile = new File(getExternalFilesDir(null), "myrecord.3gp");
            // 不要在這裡 prepare：等權限通過後由 initRecorder() 處理
        }

        Button recPlayBtn = findViewById(R.id.play_rec);
        if (recPlayBtn != null) recPlayBtn.setEnabled(false); // 尚未有檔案前先鎖住

        inDrawMode = false;
        if (output != null) output.setText("就緒");
    }

    // ================= 音樂控制 =================

    public void start_Click(View v) {
        if (musicPlayer == null) musicPlayer = MediaPlayer.create(this, R.raw.test);
        musicPlayer.start();
        output.setText("播放中");
    }

    public void pause_Click(View v) {
        if (musicPlayer != null && musicPlayer.isPlaying()) {
            musicPlayer.pause();
            output.setText("暫停中");
        }
    }

    public void stop_Click(View v) {
        if (musicPlayer != null) {
            musicPlayer.stop();
            musicPlayer.release();
            musicPlayer = null;
            output.setText("停止");
        }
    }

    private void releaseMusic() {
        if (musicPlayer != null) {
            try { musicPlayer.release(); } catch (Exception ignored) {}
            musicPlayer = null;
        }
    }

    // ================= 視訊控制 =================

    public void videoPlay_Click(View v) {
        if (video != null) {
            video.start();
            output.setText("播放中");
        }
    }

    public void videoStop_Click(View v) {
        if (video != null) {
            video.stopPlayback();
            Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.vid123);
            video.setVideoURI(uri);
            output.setText("停止");
        }
    }

    // ================= 錄音控制 =================

    public void recStart_Click(View v) {
        // 權限不在 → 先申請，等待回調
        if (!hasPerm(Manifest.permission.RECORD_AUDIO)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQ_AUDIO);
            return;
        }
        // 權限在但尚未 prepare → 先初始化
        if (!recorderReady) {
            initRecorder();
            if (!recorderReady) {
                if (output != null) output.setText("錄音初始化失敗");
                return;
            }
        }
        try {
            recorder.start();
            if (recStartBtn != null) recStartBtn.setEnabled(false);
            if (recStopBtn  != null) recStopBtn.setEnabled(true);
            if (output != null) output.setText("錄音中");
        } catch (Exception ex) {
            if (output != null) output.setText("錄音失敗");
        }
    }

    public void recStop_Click(View v) {
        try { recorder.stop(); } catch (Exception ignore) {}
        if (recStartBtn != null) recStartBtn.setEnabled(true);
        if (recStopBtn  != null) recStopBtn.setEnabled(false);
        output.setText("錄音停止");

        // 停止後可再次錄：重新 prepare
        recorderReady = false;
        initRecorder();

        // 啟用「播放錄音」按鈕
        Button recPlayBtn = findViewById(R.id.play_rec);
        if (recPlayBtn != null) recPlayBtn.setEnabled(true);

        // （選用）立即自動播放剛剛錄好的檔案：
        // playRecording();
    }

    public void recPlay_Click(View v) {
        playRecording();
    }
    private void playRecording() {
        if (recordFile == null || !recordFile.exists() || recordFile.length() == 0) {
            output.setText("沒有可播放的錄音檔");
            return;
        }

        // 先釋放上一個 recPlayer
        try {
            if (recPlayer != null) {
                recPlayer.stop();
                recPlayer.release();
            }
        } catch (Exception ignored) {}
        recPlayer = null;

        try {
            recPlayer = new MediaPlayer();
            recPlayer.setDataSource(recordFile.getAbsolutePath());
            recPlayer.setOnCompletionListener(mp -> {
                output.setText("播放完成");
                try { mp.release(); } catch (Exception ignored) {}
                recPlayer = null;
            });
            recPlayer.prepare();
            recPlayer.start();
            output.setText("播放中");
        } catch (Exception ex) {
            output.setText("播放失敗");
            try {
                if (recPlayer != null) recPlayer.release();
            } catch (Exception ignore) {}
            recPlayer = null;
        }
    }


    /** 僅在已取得 RECORD_AUDIO 權限後呼叫，負責 setSource/Format/Encoder/Output/prepare */
    private void initRecorder() {
        try {
            if (recorder == null) recorder = new MediaRecorder();
            recorder.reset();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

            if (recordFile == null) {
                recordFile = new File(getExternalFilesDir(null), "myrecord.3gp");
            }
            // 確保目錄存在
            File parent = recordFile.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();

            recorder.setOutputFile(recordFile.getAbsolutePath());
            recorder.prepare();           // ← 權限允許後才 prepare
            recorderReady = true;

            if (recStartBtn != null) recStartBtn.setEnabled(true);
            if (recStopBtn  != null) recStopBtn.setEnabled(false);
            if (output != null && !inDrawMode) output.setText("就緒");
        } catch (Exception ex) {
            recorderReady = false;
            if (output != null && !inDrawMode) output.setText("錄音初始化失敗");
        }
    }

    private boolean hasPerm(String p) {
        return ContextCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] perms, @NonNull int[] results) {
        super.onRequestPermissionsResult(requestCode, perms, results);
        if (requestCode == REQ_AUDIO) {
            if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
                initRecorder(); // 取得權限後才初始化（prepare）
            } else {
                if (output != null) output.setText("錄音權限被拒絕");
            }
        }
    }

    // ================= 2D 繪圖 =================

    public void openDraw_Click(View v) {
        setContentView(new Draw2D(this));
        inDrawMode = true;
        // 這個畫面沒有 output TextView，避免 NPE 不再動它
        Toast.makeText(this, "已進入 2D 畫布模式，按返回鍵可回主畫面", Toast.LENGTH_SHORT).show();
    }

    private static class Draw2D extends View {
        private final Paint paint = new Paint();

        public Draw2D(MainActivity ctx) { super(ctx); }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            // 背景
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            canvas.drawPaint(paint);

            // 圓形
            paint.setAntiAlias(true);
            paint.setColor(Color.RED);
            canvas.drawCircle(80f, 60f, 40f, paint);

            // 長方形
            paint.setColor(Color.BLUE);
            canvas.drawRect(20f, 120f, 200f, 260f, paint);

            // 文字
            paint.setColor(Color.GREEN);
            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(40f);
            canvas.drawText("我的畫布!", 50f, 340f, paint);

            // 旋轉文字
            canvas.save();
            paint.setColor(Color.BLACK);
            paint.setTextSize(36f);
            canvas.rotate(-45f, 300f, 300f);
            canvas.drawText("旋轉的文字!", 300f, 300f, paint);
            canvas.restore();

            // 圖片（示範）
            int resId = getResources().getIdentifier("ic_launcher_foreground", "drawable", getContext().getPackageName());
            if (resId != 0) {
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resId);
                canvas.drawBitmap(bitmap, 50f, 400f, paint);
            }
        }
    }

    // 返回鍵可從畫布回主畫面（建議開啟，體驗較佳）
//    @Override
//    public void onBackPressed() {
//        if (inDrawMode) {
//            setupMainLayout();
//            inDrawMode = false;
//            if (output != null) output.setText("就緒");
//        } else {
//            super.onBackPressed();
//        }
//    }


    // 資源釋放
    @Override
    protected void onPause() {
        super.onPause();
        if (video != null) video.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMusic();
        if (video != null) {
            try { video.stopPlayback(); } catch (Exception ignored) {}
        }
        if (recorder != null) {
            try { recorder.release(); } catch (Exception ignored) {}
            recorder = null;
        }
    }
}
