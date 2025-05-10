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
import androidx.lifecycle.lifecycleScope
import com.example.art.analyzer.ImageAnalyzer
import com.example.art.data.ApiClient
import com.example.art.data.DatabaseManager
import com.example.art.model.PaintingDescriptor
import kotlinx.coroutines.launch
import org.opencv.android.OpenCVLoader
import java.util.concurrent.Executors

class CameraActivity : ComponentActivity() {
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var cameraExecutor: java.util.concurrent.ExecutorService
    private lateinit var paintings: List<PaintingDescriptor>
    private var recognitionDone = false
    private lateinit var imageAnalyzer: ImageAnalyzer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1) Init OpenCV (your existing check)
        if (!OpenCVLoader.initDebug()) {
            throw RuntimeException("❌ Unable to load OpenCV")
        }

        setContentView(R.layout.activity_camera)

        // 2) Load descriptors from your local SQLite
        val dbManager = DatabaseManager(this)
        dbManager.open()
        paintings = dbManager.getAllPaintings()
        dbManager.close()

        if (paintings.isEmpty()) {
            Toast.makeText(this, "❌ No paintings found in DB", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        cameraExecutor = Executors.newSingleThreadExecutor()

        // 3) Start CameraX
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCameraUseCases() {
        Log.d("CameraActivity", "🎨 Paintings ready: ${paintings.size}")

        // -- Preview setup
        val preview = Preview.Builder().build()
        val previewView = findViewById<PreviewView>(R.id.viewFinder)
        preview.setSurfaceProvider(previewView.surfaceProvider)

        // -- Analysis setup
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { analysis ->
                imageAnalyzer = ImageAnalyzer(
                    paintings,
                    onResult = { matchName ->
                        if (!recognitionDone) {
                            recognitionDone = true
                            runOnUiThread {
                                Toast.makeText(this, "✅ Recognized: $matchName", Toast.LENGTH_SHORT).show()
                                Log.d("CameraActivity", "Recognized: $matchName")
                            }
                            handleMatch(matchName)
                        }
                    },
                    matchThreshold = 15
                )

                analysis.setAnalyzer(cameraExecutor, imageAnalyzer)

            }

        // -- Bind to lifecycle
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            this,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            imageAnalysis
        )
    }

    private fun handleMatch(matchName: String) {
        val matchId = paintings.firstOrNull { it.name == matchName }?.id
        if (matchId == null) {
            runOnUiThread {
                Toast.makeText(this, "⚠️ No local ID for $matchName", Toast.LENGTH_LONG).show()
            }
            return
        }

        Log.d("CameraActivity", "🔎 Requesting info for painting id=$matchId")

        lifecycleScope.launch {
            try {
                val info = ApiClient.paintingApi.getPaintingInfo(matchId)
                Log.d("CameraActivity", "✅ Received from server: $info")

                Intent(this@CameraActivity, PaintingInfoActivity::class.java).apply {
                    putExtra("name", info.name)
                    putExtra("description", info.description)
                    putExtra("imageUrl", info.imageUrl)
                    putExtra("author", info.author)
                }.also {
                    Log.d("CameraActivity", "➡️ Launching PaintingInfoActivity with extras")
                    startActivity(it)
                    finish()
                }
            } catch (e: Exception) {
                Log.e("CameraActivity", "❌ Error fetching from server", e)
                runOnUiThread { showOfflineInfo(matchName) }
            }
        }
    }


    private fun showOfflineInfo(matchName: String) {
        // your existing offline fallback (e.g. open PaintingInfoActivity with local data)
        val descriptor = paintings.first { it.name == matchName }
        Intent(this, PaintingInfoActivity::class.java).apply {
            putExtra("name", descriptor.name)
            putExtra("description", "")  // no offline description yet
            putExtra("imageUrl", "")      // no offline image
        }.also {
            startActivity(it)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        recognitionDone = false
        if (::imageAnalyzer.isInitialized) {
            imageAnalyzer.resetRecognition()
        }
    }




    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        cameraProvider.unbindAll()
    }
}
