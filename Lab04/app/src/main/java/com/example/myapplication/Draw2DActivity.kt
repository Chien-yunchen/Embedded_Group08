package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class Draw2DActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(Draw2D(this)) // PDF 做法：不使用 XML layout
    }
}
