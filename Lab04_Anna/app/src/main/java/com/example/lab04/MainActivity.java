package com.example.lab04; // ← 請改成你的實際 package 名稱

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
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
        if (!hasPerm(Manifest.permission.RECORD_AUDIO)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQ_AUDIO);
            return;
        }
        if (!recorderReady) {           // ← 這時才準備
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
        // 釋放，確保檔案寫入完成且關檔
        try { recorder.release(); } catch (Exception ignore) {}
        recorder = null;
        recorderReady = false;

        if (recStartBtn != null) recStartBtn.setEnabled(true);
        if (recStopBtn  != null) recStopBtn.setEnabled(false);

        // 確認檔案大小 >0 再開放播放
        Button recPlayBtn = findViewById(R.id.play_rec);
        if (recPlayBtn != null) recPlayBtn.setEnabled(recordFile != null && recordFile.exists() && recordFile.length() > 0);

        if (output != null) output.setText(
                (recordFile != null ? "錄音停止，大小=" + recordFile.length() + " bytes" : "錄音停止")
        );
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
        FrameLayout root = new FrameLayout(this);

        Draw2D canvas = new Draw2D(this);
        root.addView(canvas, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        Button back = new Button(this);
        back.setText("返回");
        back.setOnClickListener(x -> {
            setupMainLayout();
            if (output != null) output.setText("就緒");
        });
        int m = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.END | Gravity.BOTTOM;
        lp.setMargins(20, 20, 20, 20);   // 左、上、右、下邊距

        root.addView(back, lp);

        setContentView(root);
        inDrawMode = true;
        Toast.makeText(this, "已進入 2D 畫布模式，按返回鍵或按鈕可回主畫面", Toast.LENGTH_SHORT).show();
    }

    public static class Draw2D extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        public Draw2D(Context ctx) { super(ctx); }                          // XML/程式都可用

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            float w = canvas.getWidth();
            float h = canvas.getHeight();
            float centerX = w / 2f;
            float centerY = h / 2f;

            // 背景
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            canvas.drawPaint(paint);

            // 圓形（置中上方）
            paint.setAntiAlias(true);
            paint.setColor(Color.RED);
            canvas.drawCircle(centerX, centerY - 200f, 60f, paint);

            // 長方形（置中）
            paint.setColor(Color.BLUE);
            canvas.drawRect(centerX - 150f, centerY - 80f, centerX + 150f, centerY + 80f, paint);

            // 文字（置中）
            paint.setColor(Color.GREEN);
            paint.setTextSize(48f);
            paint.setTextAlign(Paint.Align.CENTER);   // 這行重要！
            canvas.drawText("我的畫布!", centerX, centerY + 180f, paint);

            // 旋轉文字（也置中）
            canvas.save();
            paint.setColor(Color.BLACK);
            paint.setTextSize(36f);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.rotate(-45f, centerX + 200f, centerY + 150f);
            canvas.drawText("旋轉的文字!", centerX + 200f, centerY + 150f, paint);
            canvas.restore();
        }

    }

    private Bitmap bitmap; // 這個在 Draw2D 內


//     返回鍵可從畫布回主畫面（建議開啟，體驗較佳）
    @Override
    public void onBackPressed() {
        if (inDrawMode) {
            setupMainLayout();     // ← 切回主畫面並重新綁定按鈕
        } else {
            super.onBackPressed();
        }
    }


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
