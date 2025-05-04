package com.example.art

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity

class PaintingInfoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_painting_info)

        val paintingName = intent.getStringExtra("painting_name") ?: "Невідома картина"
        val paintingDesc = intent.getStringExtra("painting_desc") ?: ""
        val paintingResId = intent.getIntExtra("painting_image_res", 0)

        findViewById<TextView>(R.id.paintingNameTextView).text = paintingName
        findViewById<TextView>(R.id.descriptionTextView).text = paintingDesc
        if (paintingResId != 0) {
            findViewById<ImageView>(R.id.paintingImageView).setImageResource(paintingResId)
        }
    }
}
