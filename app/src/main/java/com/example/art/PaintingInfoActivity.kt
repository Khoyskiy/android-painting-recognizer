package com.example.art

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import coil.Coil
import coil.load
import coil.request.ImageRequest

class PaintingInfoActivity : ComponentActivity() {

    private var isFullscreen = false
    private var fullscreenDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_painting_info)

        val name = intent.getStringExtra("name") ?: "Невідома картина"
        val description = intent.getStringExtra("description") ?: ""
        val imageUrl = intent.getStringExtra("imageUrl") ?: ""
        val author = intent.getStringExtra("author") ?: "Невідомий автор"

        val paintingNameTextView = findViewById<TextView>(R.id.paintingNameTextView)
        val paintingAuthorTextView = findViewById<TextView>(R.id.paintingAuthorTextView)
        val descriptionTextView = findViewById<TextView>(R.id.descriptionTextView)
        val imageView = findViewById<ImageView>(R.id.paintingImageView)
        imageView.scaleType = ImageView.ScaleType.MATRIX

        paintingNameTextView.text = name
        paintingAuthorTextView.text = author
        descriptionTextView.text = description

        val baseUrl = getString(R.string.server_base_url)
        val fullImageUrl = if (imageUrl.startsWith("http")) imageUrl else baseUrl + imageUrl

        val request = ImageRequest.Builder(this)
            .data(fullImageUrl)
            .target(
                onSuccess = { result ->
                    imageView.setImageDrawable(result)

                    // Після layout — гарантовано є ширина
                    imageView.post {
                        val imageWidth = result.intrinsicWidth
                        val viewWidth = imageView.width

                        val scale = viewWidth.toFloat() / imageWidth.toFloat()
                        val matrix = android.graphics.Matrix()
                        matrix.setScale(scale, scale)
                        matrix.postTranslate(0f, 0f)

                        imageView.imageMatrix = matrix

                        Log.d("PaintingInfoActivity", "✅ Зображення з кешу або мережі + масштабовано")
                    }
                },
                onStart = {
                    Log.d("PaintingInfoActivity", "📥 Завантаження зображення")
                },
                onError = {
                    Log.e("PaintingInfoActivity", "❌ Помилка завантаження")
                }
            )
            .build()

        Coil.imageLoader(this).enqueue(request)

        imageView.setOnClickListener {
            if (!isFullscreen) {
                showFullscreenImage(fullImageUrl)
            } else {
                fullscreenDialog?.dismiss()
                isFullscreen = false
            }
        }
    }

    private fun showFullscreenImage(imageUrl: String) {
        fullscreenDialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        fullscreenDialog?.setContentView(R.layout.dialog_fullscreen_image)

        val fullImageView = fullscreenDialog!!.findViewById<ImageView>(R.id.fullscreenImageView)
        fullImageView.load(imageUrl) {
            crossfade(true)
        }

        fullImageView.setOnClickListener {
            fullscreenDialog?.dismiss()
            isFullscreen = false
        }

        fullscreenDialog?.show()
        isFullscreen = true
    }
}
