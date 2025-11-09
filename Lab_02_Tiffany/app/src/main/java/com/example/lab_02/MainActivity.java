package com.example.lab_02;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnMsg;
    private Button btnConfirm;
    private Button btnSingle;
    private Button btnMulti;
    private TextView lblOutput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lblOutput = findViewById(R.id.lblOutput);
        btnMsg = findViewById(R.id.btnMsg);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnSingle = findViewById(R.id.btnSingle);
        btnMulti = findViewById(R.id.btnMulti);

        btnMsg.setOnClickListener(v -> showMsgDialog());
        btnConfirm.setOnClickListener(v -> showConfirmDialog());
        btnSingle.setOnClickListener(v -> showSingleChoiceDialog());
        btnMulti.setOnClickListener(v -> showMultiChoiceDialog());
    }

    private void showMsgDialog() {
        new AlertDialog.Builder(this)
                .setTitle("標題")
                .setMessage("這是一個訊息對話方塊")
                .setPositiveButton("確定", null)
                .show();
    }

    private void showConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("確認")
                .setMessage("你確定要執行這個動作嗎？")
                .setPositiveButton("確定", (dialog, which) ->
                        Toast.makeText(this, "已確定", Toast.LENGTH_SHORT).show())
                .setNegativeButton("取消", (dialog, which) ->
                        Toast.makeText(this, "已取消", Toast.LENGTH_SHORT).show())
                .show();
    }

    private void showSingleChoiceDialog() {
        final String[] items = new String[]{"紅色", "綠色", "藍色"};
        final int[] selectedIndex = new int[]{-1};

        new AlertDialog.Builder(this)
                .setTitle("請選擇顏色")
                .setSingleChoiceItems(items, -1, (dialog, which) -> selectedIndex[0] = which)
                .setPositiveButton("確定", (dialog, which) -> {
                    if (selectedIndex[0] >= 0) {
                        String colorName = items[selectedIndex[0]];
                        lblOutput.setText("選擇了：" + colorName);
                        if ("紅色".equals(colorName)) {
                            lblOutput.setTextColor(Color.RED);
                        } else if ("綠色".equals(colorName)) {
                            lblOutput.setTextColor(Color.GREEN);
                        } else {
                            lblOutput.setTextColor(Color.BLUE);
                        }
                    } else {
                        Toast.makeText(this, "尚未選擇", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showMultiChoiceDialog() {
        final String[] items = new String[]{"語文", "數學", "自然", "社會"};
        final boolean[] checked = new boolean[]{false, false, false, false};

        new AlertDialog.Builder(this)
                .setTitle("請選擇喜好")
                .setMultiChoiceItems(items, checked, (dialog, which, isChecked) -> checked[which] = isChecked)
                .setPositiveButton("確定", (dialog, which) -> {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < items.length; i++) {
                        if (checked[i]) {
                            if (sb.length() > 0) sb.append(", ");
                            sb.append(items[i]);
                        }
                    }
                    Toast.makeText(this,
                            sb.length() > 0 ? "選擇了: " + sb : "未選擇任何項目",
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
