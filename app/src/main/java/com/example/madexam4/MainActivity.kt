package com.example.madexam4

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler

class MainActivity : AppCompatActivity() {
    private val SPLASH_DURATION: Long = 3000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide the status bar for the splash screen
        supportActionBar?.hide() // Hide action bar if present
        window.decorView.systemUiVisibility = (
                android.view.View.SYSTEM_UI_FLAG_FULLSCREEN or
                        android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )

        // Set status bar color
        window.statusBarColor = getColor(R.color.blue)

        setContentView(R.layout.activity_main)

        // Start timer to transition to main content after splash duration
        Handler().postDelayed({
            // Make main content visible
            startActivity(Intent(this, Home::class.java))
            finish() // Finish MainActivity to prevent returning to it with back button
        }, SPLASH_DURATION)
    }
}