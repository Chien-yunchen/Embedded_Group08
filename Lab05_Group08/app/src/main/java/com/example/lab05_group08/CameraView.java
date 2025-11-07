package com.example.lab05_group08;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("deprecation")
public class CameraView extends Activity implements SurfaceHolder.Callback {

    private static final int REQ_PERMS = 1001;

    private Camera camera;
    private boolean isPreviewRunning = false;
    private SurfaceHolder surfaceHolder;
    private SurfaceView surfaceView;
    private Button btnCapture;

    private final Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] imageData, Camera c) {
            if (imageData != null) {
                boolean ok = saveImage(CameraView.this, imageData, 92);
                Toast.makeText(CameraView.this, ok ? "照片已儲存" : "儲存失敗", Toast.LENGTH_SHORT).show();
                try { camera.startPreview(); } catch (Exception ignored) {}
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // 直向顯示；相機預覽會旋轉 90 度配合
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_cameraview);

        surfaceView = findViewById(R.id.cameraview);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);

        btnCapture = findViewById(R.id.btn_capture);
        btnCapture.setOnClickListener(v -> takePicture());

        // 點預覽也可拍照
        surfaceView.setOnClickListener(v -> takePicture());

        ensurePermissions();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera = Camera.open();
        } catch (Exception e) {
            Toast.makeText(this, "開啟相機失敗", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if (camera == null) return;

        if (isPreviewRunning) {
            try { camera.stopPreview(); } catch (Exception ignored) {}
        }
        try {
            Camera.Parameters p = camera.getParameters();

            // 選一個接近螢幕比例的預覽尺寸
            Camera.Size best = getBestPreviewSize(p.getSupportedPreviewSizes(), w, h);
            if (best != null) p.setPreviewSize(best.width, best.height);

            // 連續對焦（若支援）
            List<String> fm = p.getSupportedFocusModes();
            if (fm != null && fm.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                p.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }
            camera.setParameters(p);

            // 直向裝置時把預覽旋轉 90 度
            camera.setDisplayOrientation(90);

            camera.setPreviewDisplay(holder);
            camera.startPreview();
            isPreviewRunning = true;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "預覽啟動失敗", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (camera != null) {
            try { camera.stopPreview(); } catch (Exception ignored) {}
            isPreviewRunning = false;
            camera.release();
            camera = null;
        }
    }

    private void takePicture() {
        if (camera == null) return;
        try {
            camera.autoFocus((success, cam) -> {
                try { camera.takePicture(null, null, pictureCallback); }
                catch (Exception e) { try { camera.startPreview(); } catch (Exception ignored) {} }
            });
        } catch (Exception e) {
            try { camera.takePicture(null, null, pictureCallback); } catch (Exception ignored) {}
        }
    }

    private boolean saveImage(Context ctx, byte[] data, int quality) {
        File dir = ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (dir == null) dir = ctx.getFilesDir();
        if (!dir.exists()) dir.mkdirs();

        String time = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File out = new File(dir, "IMG_" + time + ".jpg");

        try {
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inSampleSize = 1;
            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length, opt);

            FileOutputStream fos = new FileOutputStream(out);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            bmp.compress(CompressFormat.JPEG, quality, bos);
            bos.flush();
            bos.close();

            Toast.makeText(this, "存檔：" + out.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- utils ---

    private Camera.Size getBestPreviewSize(List<Camera.Size> sizes, int w, int h) {
        if (sizes == null || sizes.isEmpty()) return null;
        Camera.Size best = sizes.get(0);
        int target = w * h, diff = Math.abs(best.width * best.height - target);
        for (Camera.Size s : sizes) {
            int d = Math.abs(s.width * s.height - target);
            if (d < diff) { best = s; diff = d; }
        }
        return best;
    }

    private void ensurePermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            boolean needCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED;
            boolean needWrite = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED;
            if (needCamera || needWrite) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQ_PERMS);
            }
        }
    }
}