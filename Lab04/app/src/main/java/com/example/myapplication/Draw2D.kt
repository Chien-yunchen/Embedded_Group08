package com.example.myapplication

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.view.View

class Draw2D(context: Context) : View(context) {

    private val paint = Paint()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 背景
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        canvas.drawPaint(paint)

        // 圓（PDF）
        paint.isAntiAlias = true
        paint.color = Color.RED
        canvas.drawCircle(80f, 60f, 40f, paint)

        // 長方形（PDF）
        paint.color = Color.BLUE
        canvas.drawRect(20f, 120f, 200f, 260f, paint)

        // 文字（PDF）
        paint.color = Color.GREEN
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true
        paint.textSize = 40f
        canvas.drawText("我的畫布!", 50f, 340f, paint)

        // 旋轉文字（PDF）
        canvas.save()
        paint.color = Color.BLACK
        paint.textSize = 36f
        val str = "旋轉的文字!"
        canvas.rotate(-45f, 300f, 300f)
        paint.style = Paint.Style.FILL
        canvas.drawText(str, 300f, 300f, paint)
        canvas.restore()

        // 圖檔（PDF：畫出資源圖形）
        val res: Resources = resources
        val resId = resources.getIdentifier(
            "ic_launcher_foreground", "drawable", context.packageName
        )
        if (resId != 0) {
            val bitmap = BitmapFactory.decodeResource(res, resId)
            canvas.drawBitmap(bitmap, 50f, 400f, paint)
        }
    }
}
