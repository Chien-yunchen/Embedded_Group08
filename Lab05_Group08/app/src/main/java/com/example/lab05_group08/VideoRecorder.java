package com.example.lab05_group08;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class VideoRecorder extends AppCompatActivity {

    public static final String EXTRA_OUTPUT_PATH = "extra_output_path";
    private VideoPreview preview;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 底層：錄影預覽；上層：停止按鈕
        FrameLayout root = new FrameLayout(this);
        preview = new VideoPreview(this);
        root.addView(preview, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        Button stopBtn = new Button(this);
        stopBtn.setText("停止錄影");
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.BOTTOM | Gravity.END;
        lp.setMargins(32, 32, 32, 32);
        root.addView(stopBtn, lp);

        stopBtn.setOnClickListener(v -> stopAndReturn());
        setContentView(root);
    }

    private void stopAndReturn() {
        String path = (preview != null) ? preview.getLastPath() : null;
        if (preview != null) preview.stopRecording();
        Intent data = new Intent();
        if (path != null) data.putExtra(EXTRA_OUTPUT_PATH, path);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void onBackPressed() {
        stopAndReturn(); // 返回鍵也能停止
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 音量鍵也能停止
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            stopAndReturn();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (preview != null) preview.releaseIfAny();
    }
}
