package com.example.madexam4

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.madexam4.databinding.ActivityEventBinding
import com.example.madexam4.databinding.ActivityMainBinding

class Event : AppCompatActivity() {

    private lateinit var binding: ActivityEventBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.addButton.setOnClickListener{
            val intent = Intent(this, AddEvent::class.java)
            startActivity(intent)
        }
    }
}