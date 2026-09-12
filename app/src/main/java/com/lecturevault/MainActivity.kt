package com.lecturevault

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.lecturevault.data.AppDatabase
import com.lecturevault.data.Course
import com.lecturevault.data.Material
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var adapter: MaterialAdapter
    private lateinit var emptyText: TextView
    private var defaultCourseId: Long = 1L

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let { processImage(it) }
    }

    private val pickFromGallery = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val bitmap = android.provider.MediaStore.Images.Media.getBitmap(contentResolver, it)
            processImage(bitmap)
        }
    }

    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) takePicture.launch(null)
        else Toast.makeText(this, "Camera permission needed to take photos", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = AppDatabase.getInstance(this)

        val recyclerView = findViewById<RecyclerView>(R.id.materialsList)
        emptyText = findViewById(R.id.emptyStateText)
        adapter = MaterialAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        findViewById<Button>(R.id.btnTakePhoto).setOnClickListener {
            checkCameraPermissionAndLaunch()
        }

        findViewById<Button>(R.id.btnPickGallery).setOnClickListener {
            pickFromGallery.launch("image/*")
        }

        ensureDefaultCourse()
        observeMaterials()
    }

    private fun ensureDefaultCourse() {
        lifecycleScope.launch {
            val courses = db.courseDao()
            var existing = courses.getById(1L)
            if (existing == null) {
                defaultCourseId = courses.insert(Course(name = "General", code = "GEN"))
            } else {
                defaultCourseId = existing.id
            }
        }
    }

    private fun observeMaterials() {
        lifecycleScope.launch {
            db.materialDao().getAll().collect { materials ->
                adapter.updateItems(materials)
                emptyText.visibility = if (materials.isEmpty())
                    android.view.View.VISIBLE else android.view.View.GONE
            }
        }
    }

    private fun checkCameraPermissionAndLaunch() {
        when {
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED -> takePicture.launch(null)
            else -> requestCameraPermission.launch(android.Manifest.permission.CAMERA)
        }
    }

    private fun processImage(bitmap: Bitmap) {
        Toast.makeText(this, "Reading text…", Toast.LENGTH_SHORT).show()
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val extracted = visionText.text
                if (extracted.isBlank()) {
                    Toast.makeText(this, "No text detected — try a clearer photo", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                saveMaterial(extracted)
            }
            .addOnFailureListener {
                Toast.makeText(this, "OCR failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveMaterial(text: String) {
        lifecycleScope.launch {
            db.materialDao().insert(
                Material(courseId = defaultCourseId, type = "photo", rawText = text)
            )
            Toast.makeText(this@MainActivity, "Saved", Toast.LENGTH_SHORT).show()
        }
    }
}
