package com.example.lab05_group08;

import android.Manifest;
import android.content.ClipData;                         // ★★ 新增
import android.content.Intent;
import android.content.pm.PackageManager;              // ★★ 新增
import android.content.pm.ResolveInfo;                 // ★★ 新增
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;              // ★★ 新增

import java.io.File;
import java.util.List;                                 // ★★ 新增

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_LAST_PATH = "last_path";
    private TextView txtPath;

    private File systemCamOutput;       // 給系統相機 Intent 用（實體檔案）
    private Uri systemCamOutputUri;     // ★★ 新增：對應的 content:// URI

    // 啟動內建錄影頁（VideoRecorder），並接收錄影結果
    private final ActivityResultLauncher<Intent> builtInRecorderLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), r -> {
                if (r.getResultCode() == RESULT_OK && r.getData() != null) {
                    String path = r.getData().getStringExtra(VideoRecorder.EXTRA_OUTPUT_PATH);
                    if (path != null && !path.isEmpty()) {
                        txtPath.setText(path);
                    }
                }
            });

    // 權限請求完成後，開啟錄影頁
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

    // 啟動系統相機錄影（指定 EXTRA_OUTPUT）
    private final ActivityResultLauncher<Intent> systemCamLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), r -> {
                if (r.getResultCode() == RESULT_OK) {
                    // 指定了 EXTRA_OUTPUT，通常 data 為 null；直接用我們自己保存的 URI
                    if (systemCamOutputUri != null) {
                        txtPath.setText(systemCamOutputUri.toString());
                    } else if (r.getData() != null && r.getData().getData() != null) {
                        txtPath.setText(r.getData().getData().toString());
                    } else if (systemCamOutput != null) {
                        // 最後保底（理論上不會走到）：檔案路徑字串
                        txtPath.setText(systemCamOutput.getAbsolutePath());
                    }
                } else {
                    Toast.makeText(this, "已取消或失敗", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtPath = findViewById(R.id.txtPath);

        Button btnBuiltIn = findViewById(R.id.btnBuiltIn);
        Button btnSystem = findViewById(R.id.btnSystemCam);
        Button btnPlay = findViewById(R.id.btnPlay);

        // 內建錄影（MediaRecorder）
        btnBuiltIn.setOnClickListener(v -> {
            permissionLauncher.launch(new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
            });
        });

        // 系統相機錄影（★ FileProvider 版本）
        btnSystem.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

            // 1) 準備輸出檔案：/Android/data/<pkg>/files/Movies/xxx.mp4
            File dir = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
            if (dir != null && !dir.exists()) dir.mkdirs();
            systemCamOutput = new File(dir, "syscam_" + System.currentTimeMillis() + ".mp4");

            // 2) 透過 FileProvider 產生 content:// URI（authority 要與 Manifest 一致）
            systemCamOutputUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider", // 若 Manifest 用 ${applicationId}.fileprovider，這裡就用 getPackageName()+".fileprovider"
                    systemCamOutput
            );

            // 3) 指定輸出位置 + 授權
            intent.putExtra(MediaStore.EXTRA_OUTPUT, systemCamOutputUri);
            intent.setClipData(ClipData.newRawUri(null, systemCamOutputUri)); // 部分機型會依此授權
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // 4) 更保險：逐一授權給所有可處理此 Intent 的相機活動
            List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                grantUriPermission(resolveInfo.activityInfo.packageName, systemCamOutputUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }

            // 可選：畫質 0=低 1=高（實際仍看裝置）
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);

            systemCamLauncher.launch(intent);
        });

        // 播放影片
        btnPlay.setOnClickListener(v -> {
            Intent it = new Intent(this, VideoPlayer.class);
            it.putExtra(EXTRA_LAST_PATH, txtPath.getText().toString());
            startActivity(it);
        });
    }
}
