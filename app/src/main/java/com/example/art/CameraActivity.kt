package com.example.art

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.example.art.analyzer.ImageAnalyzer
import com.example.art.data.DatabaseManager
import com.example.art.model.PaintingDescriptor
import com.google.common.util.concurrent.ListenableFuture
import org.opencv.android.OpenCVLoader
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : ComponentActivity() {

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var paintings: List<PaintingDescriptor>
    private var recognitionDone = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!OpenCVLoader.initDebug()) {
            throw RuntimeException("❌ Unable to load OpenCV")
        }

        setContentView(R.layout.activity_camera)

        // Завантаження картин з бази даних
        val databaseManager = DatabaseManager(this)
        databaseManager.open()
        paintings = databaseManager.getAllPaintings()
        databaseManager.close()

        cameraExecutor = Executors.newSingleThreadExecutor()

        // Запуск камери
        val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCameraUseCases() {
        val preview = Preview.Builder().build()
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        val imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { analysis ->
                analysis.setAnalyzer(cameraExecutor, ImageAnalyzer(
                    paintings,
                    onResult = { matchName ->
                        if (!recognitionDone) {
                            recognitionDone = true
                            runOnUiThread {
                                Toast.makeText(this, "✅ Recognized: $matchName", Toast.LENGTH_SHORT).show()
                                Log.d("Recognition", "✅ Found: $matchName")

                                val intent = Intent(this, MainActivity::class.java)
                                intent.putExtra("MATCH_NAME", matchName)
                                startActivity(intent)
                                finish()
                            }
                        }
                    },
                    matchThreshold = 15
                ))
            }

        val previewView: PreviewView = findViewById(R.id.viewFinder)
        preview.setSurfaceProvider(previewView.surfaceProvider)

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        try {
            cameraProvider.unbindAll()
            Log.d("CameraActivity", "📸 Camera released.")
        } catch (e: Exception) {
            Log.e("CameraActivity", "⚠️ Error releasing camera", e)
        }
    }
}
