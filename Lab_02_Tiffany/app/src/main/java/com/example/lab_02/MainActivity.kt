package com.example.lab_02

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import android.graphics.Color
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private lateinit var btnMsg: Button
    private lateinit var btnConfirm: Button
    private lateinit var btnSingle: Button
    private lateinit var btnMulti: Button
    private lateinit var lblOutput: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初始化按鈕與文字
        btnMsg = findViewById(R.id.btnMsg)
        btnConfirm = findViewById(R.id.btnConfirm)
        btnSingle = findViewById(R.id.btnSingle)
        btnMulti = findViewById(R.id.btnMulti)
        lblOutput = findViewById(R.id.lblOutput)

        // 綁定事件
        btnMsg.setOnClickListener { showMessageDialog() }
        btnConfirm.setOnClickListener { showConfirmDialog() }
        btnSingle.setOnClickListener { showSingleChoiceDialog() }
        btnMulti.setOnClickListener { showMultiChoiceDialog() }
    }

    /** 1) 訊息對話方塊（About） */
    private fun showMessageDialog() {
        AlertDialog.Builder(this)
            .setTitle("關於")
            .setMessage("版本: 1.0\n課程: 嵌入式軟體設計\nLAB 02：對話方塊與資源管理")
            .setPositiveButton("確定", null)
            .setCancelable(true)
            .show()
    }

    /** 2) 確認對話方塊 */
    private fun showConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("確認")
            .setMessage("確定要離開嗎？")
            .setPositiveButton("是") { _, _ -> finish() }
            .setNegativeButton("否", null)
            .show()
    }

    /** 3) 單選對話方塊 */
    private fun showSingleChoiceDialog() {
        val colors = arrayOf("紅色", "綠色", "藍色")
        AlertDialog.Builder(this)
            .setTitle("選擇顏色")
            .setSingleChoiceItems(colors, -1) { dialog, which ->
                lblOutput.text = "選擇了 ${colors[which]}"
                lblOutput.setTextColor(
                    when (which) {
                        0 -> Color.RED
                        1 -> Color.GREEN
                        else -> Color.BLUE
                    }
                )
                dialog.dismiss()
            }
            .show()
    }

    /** 4) 多選對話方塊 */
    private fun showMultiChoiceDialog() {
        val items = arrayOf("Android", "Kotlin", "Embedded")
        val checked = booleanArrayOf(false, false, false)

        AlertDialog.Builder(this)
            .setTitle("請選擇喜好")
            .setMultiChoiceItems(items, checked) { _, which, isChecked ->
                checked[which] = isChecked
            }
            .setPositiveButton("確定") { _, _ ->
                val selected = items.filterIndexed { i, _ -> checked[i] }
                Toast.makeText(this, "選擇了: ${selected.joinToString()}", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }
}
