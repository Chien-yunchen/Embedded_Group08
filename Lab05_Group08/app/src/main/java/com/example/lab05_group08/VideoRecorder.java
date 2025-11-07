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

    /** 停止錄影並帶結果回 MainActivity */
    private void stopAndReturn() {
        if (preview != null) {
            // 停止並讓 preview 把資源收乾淨（Camera / MediaRecorder 都在裡面）
            preview.stopRecording();
        }
        String path = (preview != null) ? preview.getLastPath() : null;

        Intent data = new Intent();
        if (path != null && !path.isEmpty()) {
            data.putExtra(EXTRA_OUTPUT_PATH, path);
        }
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void onBackPressed() {
        // 「返回鍵」行為與按停止一樣：先優雅收尾，再呼叫 super 滿足 Lint
        stopAndReturn();
        try {
            super.onBackPressed();
        } catch (Exception ignored) {
            // 已經 finish() 過了，再呼叫 super 可能多一次 finish，不影響流程
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 讓音量鍵也能停止
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            stopAndReturn();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        // 離開頁面時再保險釋放一次
        if (preview != null) preview.releaseIfAny();
        super.onDestroy();
    }
}
