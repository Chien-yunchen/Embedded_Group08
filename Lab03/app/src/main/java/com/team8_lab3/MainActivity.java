package com.team8_lab3;

import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;

public class MainActivity extends AppCompatActivity {
    private static final int READ_BLOCK_SIZE = 1024;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // 只接「真的存在而且型別正確」的元件
        EditText etFileName = findViewById(R.id.etFileName);
        EditText etContent = findViewById(R.id.etContent);
        Button btnSave = findViewById(R.id.btnSave);   // 存入檔案
        Button btnRead = findViewById(R.id.btnRead);  // 讀取檔案
        Button btnReadRaw = findViewById(R.id.btnReadRaw);
        TextView tvContent = findViewById(R.id.tvContent);
        tvContent.setMovementMethod(new ScrollingMovementMethod());
        btnReadRaw.setOnClickListener(v -> {
            String text = readRawText(R.raw.note); // 對應 res/raw/note.txt → R.raw.note
            if (text != null) {
                tvContent.setText(text);
            } else {
                Toast.makeText(this, "讀取失敗", Toast.LENGTH_LONG).show();
            }
        });

        if (etContent == null || btnSave == null || btnRead == null) {
            Toast.makeText(this, "找不到畫面元件，請確認 id", Toast.LENGTH_LONG).show();
            return;
            // 內容較長可捲動

        }

        btnSave.setOnClickListener(v -> {
            String fileName = etFileName.getText().toString().trim();
            String text = etContent.getText().toString();
            if (TextUtils.isEmpty(text)) {
                Toast.makeText(this, "內容是空的，請先輸入文字", Toast.LENGTH_SHORT).show();
                return;
            }
            if (writeText(fileName, text)) {
                Toast.makeText(this, "已寫入：" + fileName, Toast.LENGTH_SHORT).show();
            }
        });

        btnRead.setOnClickListener(v -> {
            String fileName = etFileName.getText().toString().trim();
            if (!getFileStreamPath(fileName).exists()) {
                Toast.makeText(this, "尚未找到檔案：" + fileName, Toast.LENGTH_SHORT).show();
                return;
            }
            String text = readText(fileName);
            if (text != null) etContent.setText(text);
        });
    }
    private boolean writeText(String fileName, String text) {
        try (FileOutputStream out = openFileOutput(fileName, MODE_PRIVATE);
             OutputStreamWriter writer = new OutputStreamWriter(out)) {
            writer.write(text);
            writer.flush();
            return true;
        } catch (Exception e) {
            Toast.makeText(this, "寫入失敗：" + e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
    }
    private String readText(String fileName) {
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[READ_BLOCK_SIZE];
        try (FileInputStream in = openFileInput(fileName);
             InputStreamReader reader = new InputStreamReader(in)) {
            int n;
            while ((n = reader.read(buf)) > 0) sb.append(buf, 0, n);
            return sb.toString();
        } catch (Exception e) {
            Toast.makeText(this, "讀取失敗：" + e.getMessage(), Toast.LENGTH_LONG).show();
            return null;
        }
    }

    /** 讀取 res/raw 文字檔為字串（UTF-8） */
    private String readRawText(int rawResId) {
        StringBuilder sb = new StringBuilder();
        try (InputStream is = getResources().openRawResource(rawResId);
             InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(isr)) {

            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
            String result = "讀取內容:\n" + sb;
            Toast.makeText(MainActivity.this, "讀取完成", Toast.LENGTH_SHORT).show(); // 先吐司
            return result; // 再回傳

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "讀取失敗", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

}