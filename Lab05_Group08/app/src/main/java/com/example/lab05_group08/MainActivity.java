package com.example.lab05_group08;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_LAST_PATH = "last_path";

    private TextView txtPath;

    // 系統相機錄影的輸出位置
    private File systemCamOutput;
    private Uri systemCamOutputUri;

    // 啟動「自家錄影頁」並接收輸出路徑
    private final ActivityResultLauncher<Intent> builtInRecorderLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), r -> {
                if (r.getResultCode() == RESULT_OK && r.getData() != null) {
                    String path = r.getData().getStringExtra(VideoRecorder.EXTRA_OUTPUT_PATH);
                    if (path != null && !path.isEmpty()) {
                        txtPath.setText(path);
                    }
                }
            });

    // 要錄影需要 CAMERA + RECORD_AUDIO
    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean cam = Boolean.TRUE.equals(result.get(Manifest.permission.CAMERA));
                boolean mic = Boolean.TRUE.equals(result.get(Manifest.permission.RECORD_AUDIO));
                if (!cam || !mic) {
                    Toast.makeText(this, "需要相機與錄音權限", Toast.LENGTH_SHORT).show();
                } else {
                    builtInRecorderLauncher.launch(new Intent(this, VideoRecorder.class));
                }
            });

    // 系統相機錄影（帶 EXTRA_OUTPUT）
    private final ActivityResultLauncher<Intent> systemCamLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), r -> {
                if (r.getResultCode() == RESULT_OK) {
                    if (systemCamOutputUri != null) {
                        txtPath.setText(systemCamOutputUri.toString());
                    } else if (r.getData() != null && r.getData().getData() != null) {
                        txtPath.setText(r.getData().getData().toString());
                    } else if (systemCamOutput != null) {
                        txtPath.setText(systemCamOutput.getAbsolutePath());
                    }
                } else {
                    Toast.makeText(this, "已取消或失敗", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtPath = findViewById(R.id.txtPath);

        Button btnBuiltIn = findViewById(R.id.btnBuiltIn);
        Button btnSystem = findViewById(R.id.btnSystemCam);
        Button btnPlay = findViewById(R.id.btnPlay);
        Button btnSurfacePhoto = findViewById(R.id.btnSurfacePhoto);

        // 內建錄影（MediaRecorder + SurfaceView）
        btnBuiltIn.setOnClickListener(v ->
                permissionLauncher.launch(new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO
                })
        );

        // 系統相機錄影（Intent）
        btnSystem.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

            // 準備輸出檔案：app 專屬外部目錄（免 WRITE_EXTERNAL_STORAGE）
            File dir = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
            if (dir != null && !dir.exists()) dir.mkdirs();
            systemCamOutput = new File(dir, "syscam_" + System.currentTimeMillis() + ".mp4");

            // 產生 content:// URI（要和 Manifest 的 authorities 一致：${applicationId}.fileprovider）
            systemCamOutputUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    systemCamOutput
            );

            // 指定輸出與授權
            intent.putExtra(MediaStore.EXTRA_OUTPUT, systemCamOutputUri);
            intent.setClipData(ClipData.newRawUri(null, systemCamOutputUri));
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // 逐一授權給可能接手的相機 App
            List<ResolveInfo> resInfoList =
                    getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo ri : resInfoList) {
                grantUriPermission(ri.activityInfo.packageName, systemCamOutputUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }

            // 可選：錄影畫質（0=低，1=高；實際看裝置）
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);

            systemCamLauncher.launch(intent);
        });

        // 播放影片（VideoPlayer 使用 VideoView）
        btnPlay.setOnClickListener(v -> {
            Intent it = new Intent(this, VideoPlayer.class);
            it.putExtra(EXTRA_LAST_PATH, txtPath.getText().toString());
            startActivity(it);
        });

        // 進入 SurfaceView 拍照頁（CameraView）
        btnSurfacePhoto.setOnClickListener(v ->
                startActivity(new Intent(this, CameraView.class))
        );
    }
}
