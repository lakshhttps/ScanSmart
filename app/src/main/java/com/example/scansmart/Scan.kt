package com.example.scansmart

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer


class Scan : AppCompatActivity() {
    private val CAMERA_PERMISSION_CODE = 100
    private val PICK_IMAGE_REQUEST = 200
    private lateinit var codeScanner: CodeScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_scan)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val gallerybtn = findViewById<ImageButton>(R.id.gallerybtn)
        gallerybtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        checkCameraPermission()

    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            val scannerView = findViewById<CodeScannerView>(R.id.scanner_view)
            startScanner(scannerView)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        }
    }

    private fun startScanner(scannerView: CodeScannerView) {
        codeScanner = CodeScanner(this, scannerView)

        codeScanner.camera = CodeScanner.CAMERA_BACK
        codeScanner.formats = CodeScanner.ALL_FORMATS
        codeScanner.autoFocusMode = AutoFocusMode.SAFE
        codeScanner.scanMode = ScanMode.SINGLE
        codeScanner.isAutoFocusEnabled = true
        codeScanner.isFlashEnabled = false

        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                handleScannedResult(it.text)
            }
        }


        codeScanner.errorCallback = ErrorCallback {
            runOnUiThread {
                Toast.makeText(this, "Camera error: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }

        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                val scannerView = findViewById<CodeScannerView>(R.id.scanner_view)
                startScanner(scannerView)
            } else {
                Toast.makeText(this, "Camera permission is required to use the scanner", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun decodeBitmap(bitmap: Bitmap) {
        val intArray = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(intArray, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        val source = RGBLuminanceSource(bitmap.width, bitmap.height, intArray)
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

        try {
            val result = MultiFormatReader().decode(binaryBitmap)

            // Same result handling as camera scan
            handleScannedResult(result.text)

        } catch (e: Exception) {
            Toast.makeText(this, "No QR or barcode found in the image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleScannedResult(result: String) {
        when {
            result.startsWith("http://") || result.startsWith("https://") -> {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(result)))
            }
            result.startsWith("tel:") -> {
                startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(result)))
            }
            result.startsWith("mailto:") -> {
                startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse(result)))
            }
            result.startsWith("upi://") -> {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(result)))
            }
            result.startsWith("sms:") || result.startsWith("SMSTO:") -> {
                startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse(result)))
            }
            Patterns.WEB_URL.matcher(result).matches() -> {
                val fixedUrl = "http://$result"
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(fixedUrl)))
            }
            else -> {
                AlertDialog.Builder(this)
                    .setTitle("Scanned Result")
                    .setMessage(result)
                    .setPositiveButton("Copy") { _, _ ->
                        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Scan Result", result))
                        Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Close", null)
                    .show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val uri = data.data ?: return
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            decodeBitmap(bitmap)
        }
    }



    override fun onResume() {
        super.onResume()
        if (::codeScanner.isInitialized) {
            codeScanner.startPreview()
        }
    }


    override fun onPause() {
        if (::codeScanner.isInitialized) {
            codeScanner.releaseResources()
        }
        super.onPause()
    }
}