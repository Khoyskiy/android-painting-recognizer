package com.example.art

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : ComponentActivity() {

    private val cameraPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted: Boolean ->
            if (granted) {
                Toast.makeText(this, "✅ Дозвіл на камеру надано", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "❌ Потрібен дозвіл на камеру", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        enableEdgeToEdge()

        // 1. Запитуємо дозвіл на камеру
        cameraPermissionRequest.launch(android.Manifest.permission.CAMERA)

        // 2. Кнопка відкриття камери
        val btnOpenCamera: ImageButton = findViewById(R.id.btnOpenCamera)
        btnOpenCamera.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }

        // 3. Обробка якщо ми повернулись з CameraActivity зі знайденою картиною
        val matchName = intent.getStringExtra("MATCH_NAME")
        if (matchName != null) {
            Toast.makeText(this, "✅ Знайдено картину: $matchName", Toast.LENGTH_LONG).show()
            // Тут можна ще показати інфу про картину окремо на новому екрані
        }
    }
}
