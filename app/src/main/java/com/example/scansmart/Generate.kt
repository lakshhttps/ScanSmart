package com.example.scansmart

import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder


class Generate : AppCompatActivity() {

    lateinit var qrbitmap: Bitmap
    lateinit var barcodebitmap: Bitmap

    private fun saveToGallery(bitmap: Bitmap, fileName: String) {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$fileName.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ScanSmart")
        }

        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            contentResolver.openOutputStream(it)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
            Toast.makeText(this, "$fileName saved to gallery!", Toast.LENGTH_SHORT).show()
        } ?: Toast.makeText(this, "Failed to save $fileName", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_generate)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val encodetext = findViewById<TextInputEditText>(R.id.encodetext)
        val genbtn = findViewById<Button>(R.id.genbtn)
        val qr = findViewById<ImageView>(R.id.qrimage)
        val barcode = findViewById<ImageView>(R.id.barcodeimage)
        val saveqr = findViewById<Button>(R.id.saveqr)
        val savebarcode = findViewById<Button>(R.id.savebarcode)

        genbtn.setOnClickListener {
            val text = encodetext.text.toString().trim()
            if(text.isNotEmpty()) {
                val writer = MultiFormatWriter()
                try {
                    val qrmatrix: BitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 200, 200)
                    qrbitmap = BarcodeEncoder().createBitmap(qrmatrix)
                    qr.setImageBitmap(qrbitmap)

                    val barcodematrix: BitMatrix = writer.encode(text, BarcodeFormat.CODE_128, 200, 100)
                    barcodebitmap = BarcodeEncoder().createBitmap(barcodematrix)
                    barcode.setImageBitmap(barcodebitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            else {
                Toast.makeText(this, "No text entered!", Toast.LENGTH_SHORT).show()
            }
        }

        saveqr.setOnClickListener {
            if (::qrbitmap.isInitialized) {
                saveToGallery(qrbitmap, "QR_${System.currentTimeMillis()}")
            }
        }

        savebarcode.setOnClickListener {
            if (::barcodebitmap.isInitialized) {
                saveToGallery(barcodebitmap, "Barcode_${System.currentTimeMillis()}")
            }
        }
    }
}