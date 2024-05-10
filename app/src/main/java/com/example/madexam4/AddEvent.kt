package com.example.madexam4

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.madexam4.databinding.ActivityAddEventBinding

class AddEvent : AppCompatActivity() {

    private  lateinit var binding: ActivityAddEventBinding
    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)

        binding.saveButton.setOnClickListener{
            val title = binding.title.text.toString()
            val description = binding.description.text.toString()
            val date = binding.date.text.toString()
            val location = binding.location.text.toString()
            val event = Events(0, title, description, date, location)
            db.insertEvent(event)
            finish()
            Toast.makeText(this, "Event added successfully", Toast.LENGTH_SHORT).show()

        }
    }
}