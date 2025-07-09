package com.example.scansmart

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class HomeScreen : AppCompatActivity() {

    private fun animateText(textView: TextView, text: String, delay: Long = 100) {
        var index = 0

        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                if (index <= text.length) {
                    textView.text = text.substring(0, index)
                    index++
                    handler.postDelayed(this, delay)
                }
            }
        }

        handler.post(runnable)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_homescreen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val scanbtn = findViewById<Button>(R.id.scanbtn)
        val generatebtn = findViewById<Button>(R.id.generatebtn)

        scanbtn.setOnHoverListener { v, event ->
            v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(150).start()
            false
        }

        generatebtn.setOnHoverListener { v, event ->
            v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(150).start()
            false
        }



        scanbtn.setOnClickListener {
            intent = Intent(applicationContext, Scan::class.java)
            startActivity(intent)
        }

        generatebtn.setOnClickListener {
            intent = Intent(applicationContext, Generate::class.java)
            startActivity(intent)
        }

        val animated = findViewById<TextView>(R.id.animatedtext)
        animateText(animated, "Welcome to ScanSmart!", 75L)

        val formUrl = "https://forms.gle/BrAgWxokkipRm24c6"
        val feedbackbtn = findViewById<Button>(R.id.feebackbtn)

        feedbackbtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(formUrl))
            startActivity(intent)
        }
    }
}