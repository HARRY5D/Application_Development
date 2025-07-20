package com.example.p2

import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Get the root view using the correct ID from your layout
        val rootView = findViewById<ConstraintLayout>(R.id.root)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val birthdayImage = findViewById<ImageView>(R.id.birthdayImage)
        val greetingText = findViewById<TextView>(R.id.greetingText)
        val messageText = findViewById<TextView>(R.id.messageText)

        val fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        fadeIn.duration = 1500

        birthdayImage.startAnimation(fadeIn)
        greetingText.startAnimation(fadeIn)
        messageText.startAnimation(fadeIn)
    }
}