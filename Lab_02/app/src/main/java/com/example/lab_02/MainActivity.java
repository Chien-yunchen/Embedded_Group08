package com.example.lab_02;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // 單選：三個系所
    private final String[] departments = {"資工系", "資管系", "電機系"};
    private int selectedDept = -1;

    // 各系課程（複選）
    private final String[][] courses = {
            {"資料庫設計", "嵌入式軟體設計", "電腦視覺"},              // 資工系
            {"資料探勘", "高等資訊安全", "健康資訊管理"},              // 資管系
            {"類比積體電路設計", "高等電力電子學", "數位通訊"}          // 電機系
    };

    // 每系的複選狀態
    private final boolean[][] courseChecked = {
            {false, false, false},
            {false, false, false},
            {false, false, false}
    };

    private TextView tvDeptValue;
    private LinearLayout llCourses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnSingle = findViewById(R.id.btnSingle);
        Button btnMulti = findViewById(R.id.btnMulti);
        tvDeptValue = findViewById(R.id.tvDeptValue);
        llCourses = findViewById(R.id.llCourses);

        // 初始顯示「目前查詢系所」
        refreshDeptLine();

        btnSingle.setOnClickListener(v -> showDepartmentDialog());
        btnMulti.setOnClickListener(v -> showCourseDialog());
    }

    /** 更新「目前查詢系所」這一行 */
    private void refreshDeptLine() {
        if (selectedDept >= 0) {
            tvDeptValue.setText(departments[selectedDept]);
            tvDeptValue.setVisibility(View.VISIBLE);
        } else {
            tvDeptValue.setText("（未選擇）");
            tvDeptValue.setVisibility(View.VISIBLE); // 要隱藏可改成 GONE
        }
    }

    /** 單選：選擇系所 */
    private void showDepartmentDialog() {
        new AlertDialog.Builder(this)
                .setTitle("請選擇系所")
                .setSingleChoiceItems(departments, selectedDept, (dialog, which) -> selectedDept = which)
                .setPositiveButton("確定", (d, w) -> {
                    if (selectedDept >= 0) {
                        refreshDeptLine();
                        updateSelectedCoursesUI();
                        Toast.makeText(this, "你選擇了：" + departments[selectedDept], Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "尚未選擇系所", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /** 複選：選修科目（依所選系所顯示） */
    private void showCourseDialog() {
        if (selectedDept < 0) {
            Toast.makeText(this, "請先選擇系所", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] selectedCourses = courses[selectedDept];
        boolean[] checked = courseChecked[selectedDept];

        new AlertDialog.Builder(this)
                .setTitle(departments[selectedDept] + " 選修科目")
                .setMultiChoiceItems(selectedCourses, checked,
                        (dialog, which, isChecked) -> checked[which] = isChecked)
                .setPositiveButton("完成", (d, w) -> {
                    updateSelectedCoursesUI();  // 刷新主畫面清單
                    StringBuilder sb = new StringBuilder();
                    boolean any = false;
                    for (int i = 0; i < selectedCourses.length; i++) {
                        if (checked[i]) {
                            if (any) sb.append("、");
                            sb.append(selectedCourses[i]);
                            any = true;
                        }
                    }
                    Toast.makeText(this, any ? "已選課程：" + sb : "沒有勾選任何科目", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("清除", (d, w) -> {
                    for (int i = 0; i < checked.length; i++) checked[i] = false;
                    updateSelectedCoursesUI();
                    Toast.makeText(this, "已清除所有選修科目", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    /** 列出所有系目前被勾選的課程（每行：系所＋課名＋退選鈕） */
    private void updateSelectedCoursesUI() {
        llCourses.removeAllViews();

        boolean hasAny = false;

        for (int d = 0; d < departments.length; d++) {
            String deptName = departments[d];
            String[] cs = courses[d];
            boolean[] checked = courseChecked[d];

            for (int c = 0; c < cs.length; c++) {
                if (!checked[c]) continue;
                hasAny = true;

                final int depIdx = d;     // for lambda
                final int courseIdx = c;  // for lambda

                // 橫向一列：文字 + 退選按鈕
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setPadding(0, dp(6), 0, dp(6));
                row.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

                TextView tv = new TextView(this);
                tv.setText("• " + deptName + "： " + cs[courseIdx]);
                tv.setTextSize(16f);
                tv.setLayoutParams(new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

                Button btnDrop = new Button(this);
                btnDrop.setText("退選");
                btnDrop.setAllCaps(false);
                btnDrop.setOnClickListener(v -> {
                    courseChecked[depIdx][courseIdx] = false; // 取消該課
                    updateSelectedCoursesUI();                // 立即刷新列表
                    Toast.makeText(this,
                            "已退選：「" + departments[depIdx] + " - " + cs[courseIdx] + "」",
                            Toast.LENGTH_SHORT).show();
                });

                row.addView(tv);
                row.addView(btnDrop);
                llCourses.addView(row);
            }
        }

        if (!hasAny) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("（各系尚未選擇任何科目）");
            tvEmpty.setTextSize(14f);
            tvEmpty.setTextColor(0xFF888888);
            llCourses.addView(tvEmpty);
        }
    }

    private int dp(int value) {
        return Math.round(getResources().getDisplayMetrics().density * value);
    }
}
